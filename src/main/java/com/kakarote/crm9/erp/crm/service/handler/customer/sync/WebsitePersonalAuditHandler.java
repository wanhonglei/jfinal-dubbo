package com.kakarote.crm9.erp.crm.service.handler.customer.sync;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import com.alibaba.fastjson.JSONObject;
import com.jfinal.kit.JsonKit;
import com.kakarote.crm9.common.CrmContext;
import com.kakarote.crm9.common.exception.CrmException;
import com.kakarote.crm9.erp.crm.common.CrmEnum;
import com.kakarote.crm9.erp.crm.common.DistributorCertifiedEnum;
import com.kakarote.crm9.erp.crm.common.SiteMemberUserTypeEnum;
import com.kakarote.crm9.erp.crm.common.customer.FromSourceEnum;
import com.kakarote.crm9.erp.crm.constant.CrmConstant;
import com.kakarote.crm9.erp.crm.constant.CrmTagConstant;
import com.kakarote.crm9.erp.crm.entity.CrmContacts;
import com.kakarote.crm9.erp.crm.entity.CrmCustomer;
import com.kakarote.crm9.erp.crm.entity.CrmDistributorPromotionRelation;
import com.kakarote.crm9.erp.crm.entity.CrmSiteMember;
import com.kakarote.crm9.integration.common.ThirdCustomerOriginEnum;
import com.kakarote.crm9.integration.entity.SiteMember;
import com.kakarote.crm9.utils.R;
import com.qxwz.venus.api.v2.model.CertPersonalResult;
import com.qxwz.venus.common.utils.IdCardEncryptUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

/**
 * 网站个人认证
 *
 * @Author: haihong.wu
 * @Date: 2020/6/1 10:02 上午
 */
@Slf4j
public class WebsitePersonalAuditHandler extends AbstractCustomerWebsiteAuditHandler {

    @Override
    public void handle(Object param) {
        log.info("==官网认证-个人认证-开始:{}==", JsonKit.toJson(param));
        if (param instanceof CertPersonalResult) {
            CertPersonalResult result = (CertPersonalResult) param;
            if (!result.getIsSucc()) {
                log.info("==官网认证-个人认证-IsSucc->false context:{}==", JsonKit.toJson(result));
                return;
            }
            SiteMember member = convert(result);
            log.info("==官网认证-个人认证-参数转换:from:{},to:{}==", JsonKit.toJson(param), JsonKit.toJson(member));
            doHandle(member);
        } else if (param instanceof SiteMember) {
            //线上貌似根本没有这种消息体，找机会要干掉这个分支，逻辑过于复杂
            SiteMember member = (SiteMember) param;
            member.setDataOrigin(ThirdCustomerOriginEnum.SITE_MEMBER.getCode());
            doHandle(member);
        } else {
            throw new CrmException("未知参数类型:" + param.getClass().getSimpleName());
        }
    }

    private void doHandle(SiteMember param) {
        /* 上下文对象 */
        CrmContext crmContext = new CrmContext();
        /* 查询推广关系 */
        queryDistributorRelation(param.getId(), crmContext);
        /* 客户表逻辑 */
        handleCustomer(param, crmContext);
        /* 网站会员表逻辑 */
        handleSiteMember(param, crmContext);
        /* 联系人逻辑 */
        handleContacts(param, crmContext);
        /* 附加逻辑 */
        /* 1.特殊渠道分发 */
        handleChannel(param, crmContext);
        /* 2.更新推广关系 */
        handleDistributionRelation(param, crmContext);
        /* 99.冗余上游分销商 */
        redundancyParentSiteMemberId(param.getId(), crmContext);
    }

    private SiteMember convert(CertPersonalResult param) {
        SiteMember result = new SiteMember();
        BeanUtil.copyProperties(param, result, CopyOptions.create().setFieldMapping(PARAM_KEY_MAPPING));
        return result;
    }

    private static final Map<String, String> PARAM_KEY_MAPPING = new HashMap<>();

    static {
        PARAM_KEY_MAPPING.put("memberId", "id");
        PARAM_KEY_MAPPING.put("name", "realName");
        PARAM_KEY_MAPPING.put("idCard", "idNum");
    }

    /**
     * 客户表逻辑
     * <p>
     * 2.1、该网站会员账号的手机号码与某一个crm客户的联系人手机号码一致的
     * <p>
     * 2.1.1、该网站会员账号为分销伙伴终端用户或二级分销伙伴：生成新的crm客户，生成的crm客户与该网站会员账号关联
     * <p>
     * 2.1.2、该网站会员账号为分销商：
     * <p>
     * 1）、匹配到的crm客户中，关联网站会员账号（uid）中无分销身份（分销身份包含：分销商、二级分销伙伴、分销伙伴终端用户）的uid时，将网站会员账号合并到crm客户上（建立关联关系）
     * <p>
     * 2）、匹配到的crm客户中，关联网站会员账号（uid）中有分销身份（分销身份包含：分销商、二级分销伙伴、分销伙伴终端用户）的uid时，生成新的crm客户，生成的crm客户与该网站会员账号关联
     * <p>
     * 2.1.3、该网站会员账号为无分销身份：
     * <p>
     * 1）、匹配到的crm客户中，关联网站会员账号（uid）中无分销身份的uid时，将网站会员账号合并到crm客户上（建立关联关系）
     * <p>
     * 2）、匹配到的crm客户中，关联网站会员账号（uid）中有分销商的uid时，将网站会员账号合并到crm客户上（建立关联关系）
     * <p>
     * 3）、匹配到的crm客户中，关联网站会员账号（uid）中有二级分销伙伴或分销伙伴终端用户的uid时，生成新的crm客户，生成的crm客户与该网站会员账号关联
     * <p>
     * 2.2、该网站会员账号的手机号码与2个及以上的crm客户的联系人手机号码一致的
     * <p>
     * 1）、生成新的crm客户，生成的crm客户与该网站会员账号关联
     * <p>
     * 2.3、该网站会员账号的手机号码未找到相同的crm客户的联系人手机号码
     * <p>
     * 1）、生成新的crm客户，生成的crm客户与该网站会员账号关联
     *
     * @param param
     * @param context
     */
    private void handleCustomer(SiteMember param, CrmContext context) {
        CrmCustomer crmCustomer = null;
        CrmCustomer existCustomer = crmCustomerService.queryCustomerBySiteMemberId(param.getId());
        if (Objects.nonNull(existCustomer)) {
            //如果已存在，只更新数据，不判断合并逻辑
            crmCustomer = existCustomer;
            crmCustomer.setCustomerName(param.getRealName());
            crmCustomer.setIdCard(param.getIdNum());
            crmCustomer.update();
            log.info("==官网认证-个人认证-已存在客户数据(uid:{})==", param.getId());
        } else {
            //合并拆分逻辑
            List<CrmContacts> contactsList = crmContactsService.getByMobile(param.getMobile());
            //该网站会员账号的手机号码与某一个crm客户的联系人手机号码一致
            //同步过来的客户不会是分销商
            //该网站会员无分销身份
            if (contactsList != null && contactsList.size() == 1 && context.get(CrmContext.DISTRIBUTION_RELATION) == null) {
                CrmContacts crmContacts = contactsList.get(0);
                //根据匹配到的联系人查询客户数据
                existCustomer = crmCustomerService.queryById(crmContacts.getCustomerId().longValue());
                if (Objects.isNull(existCustomer)) {
                    //垃圾数据，删除，生成新的CRM客户
                    log.info("==个人客户认证-根据联系人找不到客户数据，删除联系人:{}==", JsonKit.toJson(crmContacts));
                    crmContacts.delete();
                } else {
                    //根据匹配到的联系人查询官网用户列表
                    List<CrmSiteMember> crmSiteMembersByContact = crmSiteMemberService.queryByCustomerId(crmContacts.getCustomerId().longValue());
                    boolean doMerge = false;
                    if (CollectionUtils.isEmpty(crmSiteMembersByContact)) {
                        //没有关联网站会员，合并
                        doMerge = true;
                    } else {
                        for (CrmSiteMember crmSiteMemberByContact : crmSiteMembersByContact) {
                            if (Objects.equals(crmSiteMemberByContact.getSiteMemberId(), param.getId())) {
                                continue;
                            }
                            //如果是二级分销伙伴或者终端用户，不合并
                            CrmDistributorPromotionRelation siteMemberRelation = crmDistributorPromotionRelationService.findByUserId(crmSiteMemberByContact.getSiteMemberId());
                            if (Objects.nonNull(siteMemberRelation)) {
                                doMerge = false;
                                break;
                            }
                            //其余情况合并
                            doMerge = true;
                        }
                    }
                    if (doMerge) {
                        //合并
                        crmCustomer = existCustomer;
                        //尝试更新老的customer
                        tryUpdateCustomer(crmCustomer, param);
                        //插入合并日志
                        crmRecordService.addActionRecord(null, CrmEnum.CUSTOMER_TYPE_KEY.getTypes(), crmCustomer.getCustomerId().intValue(), String.format("合并网站会员账号(网站用户ID:%s)", param.getId()));

                        log.info("==官网认证-个人认证-合并客户(uid:{},cid:{})==", param.getId(), crmCustomer.getCustomerId());
                    }
                }
            }
            if (Objects.isNull(crmCustomer)) {
                //其他生成新的CRM客户
                crmCustomer = new CrmCustomer();
                crmCustomer.setMemberIds("" + param.getId());
                crmCustomer.setIdCard(param.getIdNum());
                //个人用户不需要判重
                crmCustomer.setCustomerName(param.getRealName());
                crmCustomer.setIsLock(0);
                crmCustomer.setUpdateTime(new Date());
                crmCustomer.setCreateTime(new Date());
                crmCustomer.setBatchId("");
                crmCustomer.setMobile(param.getMobile());
                crmCustomer.setRegistrationDate(param.getGmtCreate());
                //设置客户来源
                crmCustomer.setFromSource(FromSourceEnum.FROM_WEBSITE.getCode());
                String customerType = adminDataDicService.formatTagValueName(CrmTagConstant.CUSTOMER_TYPE, CrmConstant.CRM_SITE_USER_TYPE_PERSONAL);
                crmCustomer.setCustomerType(customerType);
                R addResult = crmCustomerService.addOrUpdate(new JSONObject().fluentPut("entity", crmCustomer), null);
                if (!addResult.isSuccess()) {
                    throw new CrmException("个人客户认证失败-插入客户失败,原因:" + addResult.get("msg") + "==");
                }
                crmCustomer.setCustomerId((Long) ((Map) addResult.get("data")).get("customer_id"));
                crmCustomer.setCustomerNo((String) ((Map) addResult.get("data")).get("customer_no"));

                //插入客户插入日志
                crmRecordService.addRecord(crmCustomer.getCustomerId().intValue(), CrmEnum.CUSTOMER_TYPE_KEY.getTypes(), null);

                log.info("==官网认证-个人认证-插入客户(uid:{},customer:{})==",param.getId(), JsonKit.toJson(crmCustomer));
            }
        }
        context.put(CrmContext.CRM_CUSTOMER, crmCustomer);
    }

    /**
     * 网站会员表逻辑
     *
     * @param param
     * @param crmContext
     */
    private void handleSiteMember(SiteMember param, CrmContext crmContext) {
        CrmCustomer crmCustomer = crmContext.getAs(CrmContext.CRM_CUSTOMER);
        CrmDistributorPromotionRelation relation = crmContext.getAs(CrmContext.DISTRIBUTION_RELATION);
        CrmSiteMember crmSiteMember = crmSiteMemberService.getBySiteMemberId(param.getId());
        if (Objects.nonNull(crmSiteMember)) {
            crmSiteMember.setRealName(param.getRealName());
            String idCard = param.getIdNum() != null ? IdCardEncryptUtil.encrypt(param.getIdNum()) : "";
            crmSiteMember.setIdNum(idCard);
            crmSiteMember.setMobile(param.getMobile());
            crmSiteMember.setUserName(param.getUserName());
            crmSiteMember.setUserType(param.getUserType());
            if (Objects.nonNull(relation) && StringUtils.isNotBlank(relation.getPromotionTag())) {
                // 冗余推广标签
                crmSiteMember.setPromotionTag(relation.getPromotionTag());
            }
            crmSiteMember.setGmtCreate(param.getGmtCreate());
            crmSiteMember.setUpdateTime(new Date());
            crmSiteMember.setAuditStatus(CrmConstant.AUDIT_SUCCESS);
            crmSiteMember.setChannel(StringUtils.isNotBlank(param.getChannel()) ? param.getChannel() : "");
            crmSiteMember.update();

            log.info("==官网认证-个人认证-更新网站会员数据(uid:{})==", param.getId());
        } else {
            crmSiteMember = new CrmSiteMember();
            String idCard = param.getIdNum() != null ? IdCardEncryptUtil.encrypt(param.getIdNum()) : "";
            crmSiteMember.setIdNum(idCard);
            crmSiteMember.setRealName(param.getRealName());
            crmSiteMember.setSiteMemberId(param.getId());
            crmSiteMember.setCustId(crmCustomer.getCustomerNo());
            crmSiteMember.setMobile(param.getMobile());
            crmSiteMember.setUserName(param.getUserName());
            crmSiteMember.setUserType(param.getUserType());
            if (Objects.nonNull(relation) && StringUtils.isNotBlank(relation.getPromotionTag())) {
                // 冗余推广标签
                crmSiteMember.setPromotionTag(relation.getPromotionTag());
            }
            crmSiteMember.setGmtCreate(param.getGmtCreate());
            crmSiteMember.setAuditStatus(CrmConstant.AUDIT_SUCCESS);
            crmSiteMember.setGmtModified(new Date());
            crmSiteMember.setCreateTime(new Date());
            crmSiteMember.setUpdateTime(new Date());
            crmSiteMember.setChannel(StringUtils.isNotBlank(param.getChannel()) ? param.getChannel() : "");
            crmSiteMember.save();
            log.info("==官网认证-个人认证-插入网站会员数据(uid:{})==", param.getId());
        }
    }

    /**
     * 联系人逻辑
     *
     * @param param
     * @param crmContext
     */
    private void handleContacts(SiteMember param, CrmContext crmContext) {
        CrmCustomer crmCustomer = crmContext.getAs(CrmContext.CRM_CUSTOMER);
        /* 判断是否存在现存联系人 */
        if (!crmContactsService.exists(crmCustomer.getCustomerId(), param.getMobile())) {
            //不存在则插入
            CrmContacts contacts = new CrmContacts();
            contacts.setCustomerId(crmCustomer.getCustomerId().intValue());
            contacts.setName(param.getRealName());
            contacts.setCreateTime(new Date());
            contacts.setUpdateTime(new Date());
            contacts.setBatchId("");
            contacts.setMobile(param.getMobile());
            contacts.setEmail(param.getEmail());

            if (contacts.save()) {
                log.info("==官网认证-个人认证-插入联系人成功：{}==", contacts.toJson());
            } else {
                log.info("==官网认证-个人认证-插入联系人失败：{}==", contacts.toJson());
            }
        }
    }

    /**
     * 更新分销商推广关系数据
     *
     * @param param
     * @param crmContext
     */
    private void handleDistributionRelation(SiteMember param, CrmContext crmContext) {
        handleDistributionRelation(crmContext, SiteMemberUserTypeEnum.PERSONAL.getCode(), DistributorCertifiedEnum.AUDIT.getCode(), param.getRealName());
    }
}
