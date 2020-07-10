package com.kakarote.crm9.integration.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.jfinal.aop.Before;
import com.jfinal.aop.Inject;
import com.jfinal.core.Controller;
import com.jfinal.log.Log;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.kakarote.crm9.common.config.JfinalConfig;
import com.kakarote.crm9.common.midway.NotifyService;
import com.kakarote.crm9.common.spring.IocInterceptor;
import com.kakarote.crm9.erp.admin.common.AdminEnum;
import com.kakarote.crm9.erp.admin.service.AdminDataDicService;
import com.kakarote.crm9.erp.crm.common.DistributorCertifiedEnum;
import com.kakarote.crm9.erp.crm.common.SiteMemberUserTypeEnum;
import com.kakarote.crm9.erp.crm.constant.CrmConstant;
import com.kakarote.crm9.erp.crm.constant.CrmTagConstant;
import com.kakarote.crm9.erp.crm.entity.MqMsg;
import com.kakarote.crm9.erp.crm.service.CrmCustomerService;
import com.kakarote.crm9.erp.crm.service.CrmMqMessageService;
import com.kakarote.crm9.erp.crm.service.CrmSiteMemberService;
import com.kakarote.crm9.erp.crm.service.handler.customer.sync.BaseSyncHandler;
import com.kakarote.crm9.erp.crm.service.handler.customer.sync.CustomerSyncHandler;
import com.kakarote.crm9.integration.entity.SiteMember;
import com.kakarote.crm9.integration.service.SiteMemberMqMessageService;
import com.kakarote.crm9.utils.BaseUtil;
import com.kakarote.crm9.utils.R;
import com.kakarote.crm9.utils.TraceIdUtil;
import com.qxwz.venus.api.v2.model.CertPersonalResult;
import com.qxwz.venus.api.v2.model.MemberCancelConfirmParam;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * MQ message handler
 *
 * @author hao.fu
 * @create 2019/8/27 13:35
 */
@Before(IocInterceptor.class)
public class MqMessageCronController extends Controller {

    private Log logger = Log.getLog(getClass());

    /**
     * audit success
     */
    private final String topicMemberAuditSuccess = JfinalConfig.crmProp.get("backend.member.audit.success.topic");
    private final String tagPersonal = JfinalConfig.crmProp.get("backend.personal.audit.tag");
    private final String tagCompany = JfinalConfig.crmProp.get("backend.company.audit.tag");

    /**
     * auth change
     */
    private final String topicMemberAuditInfoChange = JfinalConfig.crmProp.get("backend.member.audit.info.change.topic");
    private final String tagPersonalInfoChange = JfinalConfig.crmProp.get("backend.personal.info.change.tag");

    /**
     * member cancel
     */
    private final String topicMemberCancel = JfinalConfig.crmProp.get("backend.member.cancel.topic");
    private final String tagMemberCancel = JfinalConfig.crmProp.get("backend.member.cancel.tag");

    private final String warningEmailTopic = JfinalConfig.crmProp.get("mq.warning.email.topic");
    private final String warningEmailToList = JfinalConfig.crmProp.get("mq.warning.email.tolist");

    @Inject
    private CrmCustomerService crmCustomerService;

    @Inject
    private CrmMqMessageService crmMqMessageService;

    @Inject
    private SiteMemberMqMessageService siteMemberMqMessageService;

    @Inject
    private AdminDataDicService adminDataDicService;

    @Inject
    private CrmSiteMemberService crmSiteMemberService;

    @Autowired
    private NotifyService notifyService;

    /**
     * Handle MQ message in CRM DB.
     */
    public void handleMsg() {
        List<Record> records = crmMqMessageService.getUnconsumedMessage();
        if (records != null && records.size() > 0) {
            List<MqMsg> msgs = records.stream().map(item -> new MqMsg()._setAttrs(item.getColumns())).collect(Collectors.toList());

            // audit success message 个人认证、企业认证
            List<MqMsg> auditSuccessMsg = msgs.stream().filter(item -> topicMemberAuditSuccess.equals(item.getTopic())).collect(Collectors.toList());
            handleAuthSuccessMessage(auditSuccessMsg);

            // auth change message 个转企
            List<MqMsg> authChangeMsg = msgs.stream().filter(item -> topicMemberAuditInfoChange.equals(item.getTopic())).collect(Collectors.toList());
            handleAuthChangeMessage(authChangeMsg);

            // member cancel message
            List<MqMsg> cancelMsg = msgs.stream().filter(item -> topicMemberCancel.equals(item.getTopic())).collect(Collectors.toList());
            handleMemberCancelMessage(cancelMsg);
        }
        TraceIdUtil.remove();
        renderJson(R.ok());
    }

    /**
     * Handle auth success message in crm db.
     *
     * @param msgs
     */
    private void handleAuthSuccessMessage(List<MqMsg> msgs) {
        if (msgs != null && msgs.size() > 0) {
            logger.info(String.format("MqMessageCronController -> handleAuthSuccessMessage -> auth success msg count: %d", msgs.size()));

            for (MqMsg item : msgs) {
                try {
                    logger.info(String.format("MqMessageCronController -> handleAuthSuccessMessage -> tag is: %s, msg is: %s", item.getTag(), item.getContent()));
                    // TOPIC_MSG_AUDIT_TAG_P_RESULT only has part fields
                    if (tagPersonal.equals(item.getTag())) {
                        handlePersonAuditResult(item);
                        // TAG_COMPANY has all fields
                    } else if (tagCompany.equals(item.getTag())) {
                        handleCompanyAuditResult(item);
                    }
                } catch (Exception e) {
                    sendWarning(e);
                }
            }
        }
    }

    /**
     * Handle auth change message in crm db.
     *
     * @param msgs
     */
    private void handleAuthChangeMessage(List<MqMsg> msgs) {
        if (msgs != null && msgs.size() > 0) {
            logger.info(String.format("MqMessageCronController -> handleAuthChangeMessage -> auth change msg count: %d", msgs.size()));

            for (MqMsg item : msgs) {
                try {
                    // from personal to company info change
                    if (tagPersonalInfoChange.equals(item.getTag())) {
                        logger.info(String.format("MqMessageCronController -> handleAuthChangeMessage -> tag is: %s, msg is: %s", item.getTag(), item.getContent()));
                        handleCompanyAuthChange(item);
                    }
                } catch (Exception e) {
                    sendWarning(e);
                }
            }
        }
    }

    /**
     * Handle member cancel message in crm db.
     *
     * @param msgs
     */
    private void handleMemberCancelMessage(List<MqMsg> msgs) {
        if (msgs != null && msgs.size() > 0) {
            logger.info(String.format("MqMessageCronController -> handleMemberCancelMessage -> member cancel msg count: %d", msgs.size()));

            for (MqMsg item : msgs) {
                try {
                    if (tagMemberCancel.equals(item.getTag())) {
                        logger.info(String.format("MqMessageCronController -> handleMemberCancelMessage -> tag is: %s, msg is: %s", item.getTag(), item.getContent()));
                        MemberCancelConfirmParam cancelResult = JSON.parseObject(item.getContent(), MemberCancelConfirmParam.class);

                        Record record = siteMemberMqMessageService.getCrmCustomerRecord(cancelResult.getUserId());

                        // 添加事务（支持嵌套）
                        Db.tx(() -> {

                            if (record != null && !record.getStr("customer_no").isEmpty()) {
                                logger.info(String.format("site member info in crm db: %s", record.toJson()));
                                crmCustomerService.deleteCustomerInfoBySiteMemberId(cancelResult.getUserId().intValue());
                            } else {
                                logger.info("site member info in crm db does not exist!");
                            }

                            // update mq message consume status
                            crmMqMessageService.updateConsumeStatus(item.getId());
                            return true;
                        });
                    }
                } catch (Exception e) {
                    sendWarning(e);
                }
            }
        }
    }


    private void handlePersonAuditResult(MqMsg item) {
        JSONObject json = JSON.parseObject(item.getContent());
        logger.info(String.format("handlePersonAuditResult mqMsg content: %s", item.getContent()));
        // 添加事务（支持嵌套）
        Db.tx(() -> {
            BaseSyncHandler handler = CustomerSyncHandler.getHandler(CustomerSyncHandler.SyncHandlersEnum.Website_Personal_Audit);
            if (json.containsKey("returnMsg") && json.containsKey("isSucc")) {
                CertPersonalResult certResult = JSON.parseObject(item.getContent(), CertPersonalResult.class);
                handler.handle(certResult);
            } else {
                SiteMember siteMember = JSON.parseObject(item.getContent(), SiteMember.class);
                handler.handle(siteMember);
            }
            // update mq message consume status
            crmMqMessageService.updateConsumeStatus(item.getId());
            return true;
        });
    }

    /**
     * Handle company auth success.
     * 公司类型客户认证
     *
     * @param item
     */
    private void handleCompanyAuditResult(MqMsg item) {
        SiteMember siteMember = JSON.parseObject(item.getContent(), SiteMember.class);

        // 添加事务（支持嵌套）
        Db.tx(() -> {
            BaseSyncHandler handler = CustomerSyncHandler.getHandler(CustomerSyncHandler.SyncHandlersEnum.Website_Company_Audit);
            handler.handle(siteMember);
            // update mq message consume status
            crmMqMessageService.updateConsumeStatus(item.getId());
            return true;
        });
    }

    private void sendWarning(Exception e) {
        String ex = BaseUtil.getExceptionStack(e);
        logger.error("handle MQ message occurs exception:" + ex);
        if (warningEmailToList != null && warningEmailToList.length() > 0) {
            List<String> toList = Arrays.asList(warningEmailToList.split(","));
            notifyService.email(warningEmailTopic, ex, toList);
        }
    }

    /**
     * Company auth change handler.
     * <p>
     * 1、以site_member_id作为主键，对72crm_crm_customer进行同步
     * 1.1、存在映射关系时，以site_member_id更新72crm_crm_customer
     * 1.2、不存在映射关系时，且拥有同名的企业类型客户，以customerName更新72crm_crm_customer
     * 1.3、否则，则新增客户
     * 2、以site_member_id作为主键对72crm_crm_site_member进行同步，并维护72crm_crm_site_member表customer_no与site_member_id关系
     * 2.1、site_member_id不存在，新建一条企业客户映射关系
     * 2.2、site_member_id存在，将个人客户更新为企业客户映射关系
     * 3、更新72crm_mq_msg状态
     *
     * @param item
     */
    private void handleCompanyAuthChange(MqMsg item) {
        SiteMember siteMember = JSON.parseObject(item.getContent(), SiteMember.class);

        // 1、以site_member_id作为主键，对72crm_crm_customer进行同步
        // 1.1、存在映射关系时，以site_member_id更新客户
        // 1.2、不存在映射关系时，且拥有同名的企业类型客户，以customerName更新72crm_crm_customer
        // 1.3、否则，则新增客户
        // 获取企业客户code
        String customerTypeCode = adminDataDicService.formatTagValueName(CrmTagConstant.CUSTOMER_TYPE, CrmConstant.BUSINESS_CLIENTS);
        // 根据分销商名称获取crm客户
        Record crmCustomerByName = siteMemberMqMessageService.getBusinessCustomerByCustomerName(siteMember.getRealName(), customerTypeCode);
        // 根据分销商id获取crm客户
        Record crmCustomerById = crmCustomerService.getBySiteMemberId(siteMember.getId());
        // 企业用户编码
        String typeClientsCode = adminDataDicService.formatTagValueName(AdminEnum.CUSTOMER_TYPE.getName(), CrmConstant.BUSINESS_CLIENTS);
        // 添加事务（支持嵌套）
        Db.tx(() -> {
	        String customerNo;// 客户编号
            Long memberId = siteMember.getId();

            if (Objects.nonNull(crmCustomerById)) {
                customerNo = crmCustomerById.getStr("customer_no");
                // Update
                if (!siteMemberMqMessageService.updateCustomer(siteMember, crmCustomerById)) {
                    // 更新客户失败时，回滚
                    return false;
                }
            } else {
                if (Objects.nonNull(crmCustomerByName) && typeClientsCode.equals(crmCustomerByName.getStr("customer_type"))) {

                    // Update
                    customerNo = crmCustomerByName.getStr("customer_no");
                    if (!siteMemberMqMessageService.updateCustomer(siteMember, crmCustomerByName)) {
                        // 更新客户失败时，回滚
                        return false;
                    }
                } else {
                    R result = siteMemberMqMessageService.addNewCrmCustomerAndContactorReturnR(siteMember);
                    // 新建客户失败时，回滚
                    if (Objects.nonNull(result) && result.isSuccess() && Objects.nonNull(result.get("data")) && Objects.nonNull(((Map) result.get("data")).get("customer_no"))) {
                        customerNo = (String) ((Map) result.get("data")).get("customer_no");
                    } else {
                        return false;
                    }
                }
            }

            // 2、以site_member_id作为主键对72crm_crm_site_member进行同步，并维护72crm_crm_site_member表customer_no与site_member_id关系
            // 2.1、site_member_id不存在，新建一条企业客户映射关系
            // 2.2、site_member_id存在，将个人客户更新为企业客户映射关系
            if (!siteMemberMqMessageService.updateCrmDataForCompanyAuthSuccess(siteMember, customerNo)) {
                // 处理site_member_id失败时，回滚
                return false;
            }
            //更新推广关系信息
            if (Objects.nonNull(memberId)) {
                crmSiteMemberService.updatePromotionRelationInfo(SiteMemberUserTypeEnum.COMPANY, DistributorCertifiedEnum.AUDIT, siteMember.getRealName(), memberId);
            }

            // 3、更新72crm_mq_msg状态
            crmMqMessageService.updateConsumeStatus(item.getId());
            return true;
        });
    }
}
