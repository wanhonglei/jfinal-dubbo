package com.kakarote.crm9.integration.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.rocketmq.common.message.MessageExt;
import com.alibaba.rocketmq.shade.io.netty.util.internal.StringUtil;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.jfinal.aop.Aop;
import com.jfinal.aop.Before;
import com.jfinal.kit.JsonKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.kakarote.crm9.common.config.JfinalConfig;
import com.kakarote.crm9.erp.admin.entity.AdminUser;
import com.kakarote.crm9.erp.admin.service.AdminDataDicService;
import com.kakarote.crm9.erp.admin.service.AdminUserService;
import com.kakarote.crm9.erp.crm.common.CustomerOriginEnum;
import com.kakarote.crm9.erp.crm.common.customer.FromSourceEnum;
import com.kakarote.crm9.erp.crm.constant.CrmConstant;
import com.kakarote.crm9.erp.crm.constant.CrmTagConstant;
import com.kakarote.crm9.erp.crm.entity.CrmContacts;
import com.kakarote.crm9.erp.crm.entity.CrmCustomer;
import com.kakarote.crm9.erp.crm.entity.CrmSiteMember;
import com.kakarote.crm9.erp.crm.entity.MqMsg;
import com.kakarote.crm9.erp.crm.service.CrmContactsService;
import com.kakarote.crm9.erp.crm.service.CrmCustomerService;
import com.kakarote.crm9.erp.crm.service.CrmMqMessageService;
import com.kakarote.crm9.erp.crm.service.CrmSiteMemberService;
import com.kakarote.crm9.integration.entity.SiteMember;
import com.kakarote.crm9.integration.entity.SpecialCustomerAllocateRuleDto;
import com.kakarote.crm9.utils.R;
import com.qxwz.venus.common.utils.IdCardEncryptUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Site Member MqMessage Service
 *
 * @author hao.fu
 * @create 2019/7/6 15:54
 */
@Slf4j
public class SiteMemberMqMessageService {

    private CrmCustomerService crmCustomerService = Aop.get(CrmCustomerService.class);

    private CrmSiteMemberService crmSiteMemberService = Aop.get(CrmSiteMemberService.class);

    private CrmContactsService crmContactsService = Aop.get(CrmContactsService.class);

    private AdminDataDicService adminDataDicService = Aop.get(AdminDataDicService.class);

    private CrmMqMessageService crmMqMessageService = Aop.get(CrmMqMessageService.class);

    private AdminUserService adminUserService = Aop.get(AdminUserService.class);

    private final String specialCustomerAllocateRule = JfinalConfig.crmProp.get("special.customer.allocate.rule");

    /**
     * 创建客户与联系人
     * @param siteMember
     * @return
     */
    @Before(Tx.class)
    public R addNewCrmCustomerAndContactorReturnR(SiteMember siteMember) {
        // 创建客户
        JSONObject obj = new JSONObject();
        CrmCustomer crmCustomer = assembleCrmCustomer(new CrmCustomer(), siteMember);
        crmCustomer.setFromSource(FromSourceEnum.FROM_WEBSITE.getCode());
        obj.put("entity", crmCustomer);
        R result = crmCustomerService.addOrUpdate(obj,null);
        if (result.isSuccess()) {
	        if (((Map) result.get("data")).get("customer_id") == null) {
	            log.error("新建客户失败, site member info: {}", JSON.toJSONString(siteMember));
	            return null;
	        }
            //查询会员注册渠道是否配置了默认分派bd,如果配置，绑定bd,设置关联库
            Long customerId = (Long) ((Map) result.get("data")).get("customer_id");
            if (StringUtils.isNotBlank(siteMember.getChannel())) {
                Integer newOwnerUserId = getMemberChannelOwnerUserId(siteMember.getChannel());
                if (Objects.nonNull(newOwnerUserId)) {
                    crmCustomerService.memberChannelUpdateOwnerUserId(customerId, newOwnerUserId);
                }
            }
	
	        log.info("新建客户成功, site member info: {}", JSON.toJSONString(result));
	
	        // 创建联系人
	        addCrmContactor((Long)((Map)result.get("data")).get("customer_id"), siteMember);
	        return result;
        } else {
            return null;
        }
    }

    private void addCrmContactor(Long custId, SiteMember siteMember) {
        CrmContacts contacts = new CrmContacts();
        contacts.setCustomerId(custId.intValue());
        contacts.setName(siteMember.getLinkMan());
        contacts.setMobile(StringUtils.isNotEmpty(siteMember.getMobile()) ? siteMember.getMobile() : siteMember.getContactNumber());
        contacts.setEmail(siteMember.getEmail());
        contacts.setAddress(siteMember.getContactAddress());
        contacts.setCreateTime(new Date());
        contacts.setBatchId("");

        JSONObject obj = new JSONObject();
        obj.put("entity", contacts);
        R result = crmContactsService.addOrUpdate(obj, null);

        if (result.isSuccess()) {
            log.info("#############add new crm contactor success, contactor info: {}", contacts.toJson());
        } else {
            log.info("#############add new crm contactor failed, contactor info: {}", contacts.toJson());
        }
    }

    /**
     * add crm customer
     *
     * @return
     */
    private Record addCrmCustomer(SiteMember siteMember) {
        JSONObject obj = new JSONObject();
        CrmCustomer crmCustomer = assembleCrmCustomer(new CrmCustomer(), siteMember);
        crmCustomer.setFromSource(FromSourceEnum.FROM_WEBSITE.getCode());
        obj.put("entity", crmCustomer);
        R result = crmCustomerService.addOrUpdate(obj,null);
        if (result.isSuccess()) {
            //查询会员注册渠道是否配置了默认分派bd,如果配置，绑定bd,设置关联库
            Long customerId = (Long) ((Map) result.get("data")).get("customer_id");
            if (StringUtils.isNotBlank(siteMember.getChannel())) {
                Integer newOwnerUserId = getMemberChannelOwnerUserId(siteMember.getChannel());
                if (Objects.nonNull(newOwnerUserId)) {
                    crmCustomerService.memberChannelUpdateOwnerUserId(customerId, newOwnerUserId);
                }
            }
            String userType =  siteMember.getUserType() + "";
            return crmCustomerService.getUniqueCustomer(userType, CrmConstant.USER_TYPE_COMPANY.equals(userType) ? siteMember.getRealName() : siteMember.getIdNum());
        } else {
            return null;
        }
    }

    /**
     * 更新客户
     * @param siteMember
     * @param crmCustomer
     * @return
     */
    public boolean updateCustomer(SiteMember siteMember, Record crmCustomer) {
        CrmCustomer crmCustomerUpdate = new CrmCustomer()._setAttrs(crmCustomer.getColumns());
        // 使用网站会员信息填充客户
        assembleCrmCustomer(crmCustomerUpdate, siteMember);
        // 填充更新时间
        crmCustomerUpdate.setUpdateTime(new Date());
        boolean update = crmCustomerUpdate.update();
        if (!update) {
            log.error("updateCustomer crmCustomerUpdate is faild,crmCustomerUpdate:{}", JSONObject.toJSONString(crmCustomerUpdate));
            return false;
        }
        Integer newOwnerUserId = getMemberChannelOwnerUserId(siteMember.getChannel());
        //如果配置了会员注册渠道默认分派bd，绑定负责人
        if (Objects.nonNull(newOwnerUserId)) {
            boolean updateOwnerUserId = crmCustomerService.memberChannelUpdateOwnerUserId(crmCustomerUpdate.getCustomerId(), newOwnerUserId);
            if (!updateOwnerUserId) {
                log.error("updateCustomer memberChannelUpdateOwnerUserId update is faild ,customerId:{},newOwnerUserId:{}",
                        JSON.toJSONString(crmCustomerUpdate.getCustomerId()), newOwnerUserId);
                return false;
            }
        }
        return true;
    }

    private CrmCustomer assembleCrmCustomer(CrmCustomer crmCustomer, SiteMember siteMember) {
        if (siteMember.getRealName() != null) {
            crmCustomer.setCustomerName(siteMember.getRealName());
        }
        if (siteMember.getMobile() != null) {
            crmCustomer.setMobile(siteMember.getMobile());
        }
        if (siteMember.getProv() != null) {
            crmCustomer.setAddress(checkStringEmpty(siteMember.getProv()) + checkStringEmpty(siteMember.getCity()));
        }
        if (siteMember.getContactAddress() != null) {
            crmCustomer.setDetailAddress(checkStringEmpty(siteMember.getContactAddress()));
        }
        int type = siteMember.getUserType();
        String userType = type == Integer.parseInt(CrmConstant.USER_TYPE_PERSONAL) ? CrmConstant.CRM_SITE_USER_TYPE_PERSONAL : CrmConstant.BUSINESS_CLIENTS;
        crmCustomer.setCustomerType(getCustomerType(userType));
        if (siteMember.getBusinessLicenceNum() != null) {
            crmCustomer.setRegistrationNumber(siteMember.getBusinessLicenceNum());
            crmCustomer.setCreditCode(siteMember.getBusinessLicenceNum());
        }
        if (siteMember.getBusinessLicenceImgUrl() != null) {
            crmCustomer.setRegistrationImgUrl(siteMember.getBusinessLicenceImgUrl());
        }
        if (siteMember.getIdNum() != null) {
            crmCustomer.setIdCard(siteMember.getIdNum());
        }
        if (siteMember.getIdImgUrl() != null) {
            crmCustomer.setIdCardUrl(siteMember.getIdImgUrl());
        }
        crmCustomer.setCustomerOrigin(CustomerOriginEnum.WEB_SITE_ORIGIN_KEY.getTypes());
        crmCustomer.setIsAttestation(siteMember.getAuditStatus() + "");

        if (siteMember.getGmtCreate() != null) {
            crmCustomer.setRegistrationDate(siteMember.getGmtCreate());
        }

        //当原始客户姓名不为空的时候，赋值给customer
        if (StringUtils.isNotBlank(siteMember.getOriginalCustomerName())){
            crmCustomer.setOriginalCustomerName(siteMember.getOriginalCustomerName());
        }

        return crmCustomer;
    }

    private String getCustomerType(String typeNameInSiteMember) {
        return adminDataDicService.formatTagValueName(CrmTagConstant.CUSTOMER_TYPE, typeNameInSiteMember);
    }

    private static String checkStringEmpty(String item) {
        return Strings.isNullOrEmpty(item) ? "" : item;
    }

    private void addCrmSiteMember(String custNo, SiteMember siteMember) {
        CrmSiteMember newCrmSiteMember = new CrmSiteMember();
        newCrmSiteMember.setCustId(custNo);
        CrmSiteMember crmSiteMember = assembleCrmSiteMember(newCrmSiteMember, siteMember);
        log.info("#############add crm site member: {}", crmSiteMember.toJson());
        crmSiteMemberService.addSiteMember(crmSiteMember);
    }

    private CrmSiteMember assembleCrmSiteMember(CrmSiteMember crmSiteMember, SiteMember siteMember) {
        if(!Strings.isNullOrEmpty(siteMember.getAuditFailureMsg())) {
            crmSiteMember.setAuditFailureMsg(checkStringEmpty(siteMember.getAuditFailureMsg()));
        }

        crmSiteMember.setAuditStatus(siteMember.getAuditStatus());

        if(siteMember.getAuditSubmitTime() != null) {
            crmSiteMember.setAuditSubmitTime(siteMember.getAuditSubmitTime());
        }

        if(siteMember.getAuthSubStatus() != null) {
            crmSiteMember.setAuthSubStatus(siteMember.getAuthSubStatus());
        }

        if(siteMember.getAuthSubTime() != null) {
            crmSiteMember.setAuthSubTime(siteMember.getAuthSubTime());
        }

        if(!Strings.isNullOrEmpty(siteMember.getBusinessLicenceNum())) {
            crmSiteMember.setBusinessLicenceNum(siteMember.getBusinessLicenceNum());
        }


        if(!Strings.isNullOrEmpty(siteMember.getBusinessLicenceImgUrl())) {
            crmSiteMember.setBusinessLicenceImgUrl(siteMember.getBusinessLicenceImgUrl());
        }

        if(!Strings.isNullOrEmpty(siteMember.getChannel())) {
            crmSiteMember.setChannel(siteMember.getChannel());
        }

        if(!Strings.isNullOrEmpty(siteMember.getCity())) {
            crmSiteMember.setCity(siteMember.getCity());
        }

        if(!Strings.isNullOrEmpty(siteMember.getContactAddress())) {
            crmSiteMember.setContactAddress(siteMember.getContactAddress());
        }

        if(!Strings.isNullOrEmpty(siteMember.getLinkMan())) {
            crmSiteMember.setContactor(siteMember.getLinkMan());
        }

        if(!Strings.isNullOrEmpty(siteMember.getContactNumber())) {
            crmSiteMember.setContactNumber(siteMember.getContactNumber());
        }

        crmSiteMember.setCreateTime(new Date());

        if(!Strings.isNullOrEmpty(crmSiteMember.getCustId())) {
            crmSiteMember.setCustId(crmSiteMember.getCustId());
        }

        if(!Strings.isNullOrEmpty(siteMember.getCustomUse())) {
            crmSiteMember.setCustomUse(siteMember.getCustomUse());
        }

        if(!Strings.isNullOrEmpty(siteMember.getEmail())) {
            crmSiteMember.setEmail(siteMember.getEmail());
        }


        if(siteMember.getGmtCreate() != null) {
            crmSiteMember.setGmtCreate(siteMember.getGmtCreate());
        }

        if(siteMember.getGmtModified() != null) {
            crmSiteMember.setGmtModified(siteMember.getGmtModified());
        }

        if(!Strings.isNullOrEmpty(siteMember.getIdImgBackUrl())) {
            crmSiteMember.setIdImgBackUrl(siteMember.getIdImgBackUrl());
        }

        if(!Strings.isNullOrEmpty(siteMember.getIdNum())) {
            String idCard = siteMember.getIdNum() != null ? IdCardEncryptUtil.encrypt(siteMember.getIdNum()) : "";
            crmSiteMember.setIdNum(idCard);
        }

        if(!Strings.isNullOrEmpty(siteMember.getIdImgUrl())) {
            crmSiteMember.setIdImgUrl(siteMember.getIdImgUrl());
        }

        if(!Strings.isNullOrEmpty(siteMember.getLoginName())) {
            crmSiteMember.setLoginName(siteMember.getLoginName());
        }

        if (siteMember.getMaxAppNum() != null) {
            crmSiteMember.setMaxAppNum(siteMember.getMaxAppNum());
        }

        if (siteMember.getMaxNtripUserNum() != null) {
            crmSiteMember.setMaxNtripUserNum(siteMember.getMaxNtripUserNum());
        }

        if (!Strings.isNullOrEmpty(siteMember.getMobile())) {
            crmSiteMember.setMobile(siteMember.getMobile());
        }

        if (siteMember.getNtripUserCount() != null) {
            crmSiteMember.setNtripUserCount(siteMember.getNtripUserCount());
        }

        if (!Strings.isNullOrEmpty(siteMember.getNtripUserPrefix())) {
            crmSiteMember.setNtripPrefixOriginal(siteMember.getNtripUserPrefix());
        }

        if (!Strings.isNullOrEmpty(siteMember.getOptions())) {
            crmSiteMember.setOptions(siteMember.getOptions());
        }

        if (siteMember.getOptionsVer() != null) {
            crmSiteMember.setOptionsVer(siteMember.getOptionsVer());
        }

        if (!Strings.isNullOrEmpty(siteMember.getProv())) {
            crmSiteMember.setProv(siteMember.getProv());
        }

        if (!Strings.isNullOrEmpty(siteMember.getRealName())) {
            crmSiteMember.setRealName(siteMember.getRealName());
        }

        if (!Strings.isNullOrEmpty(siteMember.getRegIp())) {
            crmSiteMember.setRegIp(siteMember.getRegIp());
        }

        if (siteMember.getServiceUse() != null) {
            crmSiteMember.setServiceUse(siteMember.getServiceUse());
        }

        if (siteMember.getId() != null) {
            crmSiteMember.setSiteMemberId(siteMember.getId());
        }

        if (siteMember.getSource() != null) {
            crmSiteMember.setSource(siteMember.getSource());
        }

        if (siteMember.getStatus() != null) {
            crmSiteMember.setStatus(siteMember.getStatus() != null ? (int) siteMember.getStatus() : null);
        }

        if (siteMember.getTryStatus() != null) {
            crmSiteMember.setTryStatus(siteMember.getTryStatus());
        }

        crmSiteMember.setUpdateTime(new Date());

        if (!Strings.isNullOrEmpty(siteMember.getUserFlag())) {
            crmSiteMember.setUserFlag(siteMember.getUserFlag());
        }

        if (!Strings.isNullOrEmpty(siteMember.getUserName())) {
            crmSiteMember.setUserName(siteMember.getUserName());
        }

        crmSiteMember.setUserType(siteMember.getUserType());
        // 添加数据来源,1-网站会员；2-分销商认证。参照ThirdCustomerOriginEnum.java
        crmSiteMember.setDataOrigin(siteMember.getDataOrigin());

        // 添加成为分销商时间
        if (Objects.nonNull(siteMember.getDistriAuditSuccessTime())) {
            crmSiteMember.setDistriAuditSuccessTime(siteMember.getDistriAuditSuccessTime());
        }
        
        // 冗余推广标签
        if (StringUtils.isNotEmpty(siteMember.getPromotionTag())) {
        	crmSiteMember.setPromotionTag(siteMember.getPromotionTag());
        }
        return crmSiteMember;
    }

    /**
     * Return crm customer data in db.
     *
     * @param siteMemberId
     * @return
     */
    public Record getCrmCustomerRecord(Long siteMemberId) {
        if (siteMemberId != null) {
            // site member id is unique
            log.info("#############site member id is: {}", siteMemberId);

            Record record = crmCustomerService.getBySiteMemberId(siteMemberId);
            log.info("#############SiteMemberMqMessageService.getCrmCustomerRecord, record: {}", record);
            return record;
        }
        return null;
    }

    /**
     * 根据客户名称查询企业类型客户
     *
     * @param customerName
     * @param customerTypeCode
     * @return
     */
    public Record getBusinessCustomerByCustomerName(String customerName, String customerTypeCode) {
        if (StringUtils.isNoneEmpty(customerName)) {
            log.info("#############getBusinessCustomerByCustomerName, customerName: {}", customerName);
            Record record = Db.findFirst(Db.getSql("crm.customer.getgetBusinessCustomerByRealName"), customerName, customerTypeCode);
            log.info("#############getBusinessCustomerByCustomerName, record: {}", record);
            return record;
        }
        return null;
    }

    /**
     * 以site_member_id作为主键对72crm_crm_site_member进行同步，并维护72crm_crm_site_member表customer_no与site_member_id关系
     * 1、site_member_id不存在，新建一条企业客户
     * 2、site_member_id存在，将个人客户更新为企业客户
     * @param siteMember
     * @param custNo
     */
    @Before(Tx.class)
    public boolean updateCrmDataForCompanyAuthSuccess(SiteMember siteMember, String custNo) {
        log.info("updateCrmDataForCompanyAuthSuccess -> siteMember: {}", siteMember == null ? "" : JsonKit.toJson(siteMember));
        log.info("updateCrmDataForCompanyAuthSuccess -> custNo: {}", custNo );

        if (Objects.isNull(siteMember) || StringUtil.isNullOrEmpty(custNo)) {
            return false;
        }

        // update or add site member
        Record record = crmSiteMemberService.getSiteMemberAllFieldBySiteMemberId(siteMember.getId());
        if (record != null) {
            CrmSiteMember sm = new CrmSiteMember()._setAttrs(record.getColumns());
            CrmSiteMember crmSiteMember = assembleCrmSiteMember(sm, siteMember);
            crmSiteMember.setCustId(custNo);
            log.info("--------->updateCrmDataForCompanyAuthSuccess crmSiteMember:  {}", JSON.toJSONString(crmSiteMember));
            crmSiteMemberService.updateSiteMember(crmSiteMember);
        } else {
            log.info("--------->updateCrmDataForCompanyAuthSuccess, site member does not exist in crm, add new one: {}" , JSON.toJSONString(siteMember));
            addCrmSiteMember(custNo, siteMember);
        }
        return true;
    }

    public void saveMqMsg2Crm(MessageExt msg, Object obj) {
        String msgId = msg.getMsgId();

        Record msgInDb = crmMqMessageService.findMessagByMsgId(msgId);
        log.info("get mq msg from crm db, msgInDb: {}", msgInDb != null ? msgInDb.toJson() : null);
        if (msgInDb == null) {
            MqMsg mqMsg = new MqMsg();
            mqMsg.setMsgId(msgId);
            mqMsg.setTopic(msg.getTopic());
            mqMsg.setTag(msg.getTags());
            mqMsg.setContent(JSON.toJSONString(obj));
            mqMsg.setCreateTime(new Date());
            mqMsg.setUpdateTime(new Date());
            log.info("save MQ message to CRM db: {}", mqMsg);
            mqMsg.save();
        } else {
            log.info("MQ message exists in CRM db: msg is: {}, obj is: {}", msg, obj);
        }
    }

    /**
     * 获取会员注册渠道默认bd配置规则
     * tmall:2804
     *
     * @return
     */
    private Map<String, Long> getChannelRuleMap() {
        //查询会员注册渠道默认bd分派规则，根据规则分派客户
        List<SpecialCustomerAllocateRuleDto> allocateRuleDtos = JSONObject.parseArray(specialCustomerAllocateRule, SpecialCustomerAllocateRuleDto.class);
        Map<String, Long> ruleMap = Maps.newHashMap();
        if (CollectionUtils.isNotEmpty(allocateRuleDtos)) {
            for (SpecialCustomerAllocateRuleDto ruleDto : allocateRuleDtos) {
                String memberChannel = ruleDto.getMemberChannel();
                Long ownerUserId = ruleDto.getOwnerUserId();
                if (Objects.nonNull(ownerUserId)) {
                    AdminUser adminUser = adminUserService.getAdminUserByUserId(ownerUserId);
                    if (Objects.nonNull(adminUser)) {
                        ruleMap.put(memberChannel, ownerUserId);
                    }
                }
            }
        }
        return ruleMap;
    }

    /**
     * 通过会员渠道查询默认分派的bd
     *
     * @param memberChannel
     * @return
     */
    private Integer getMemberChannelOwnerUserId(String memberChannel) {
        if (StringUtils.isBlank(memberChannel)) {
            return null;
        }
        Map<String, Long> memberChannelRuleMap = getChannelRuleMap();
        Long ownerUserId = memberChannelRuleMap.get(memberChannel);
        if (Objects.nonNull(ownerUserId)) {
            return ownerUserId.intValue();
        }
        return null;
    }
}
