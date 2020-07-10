package com.kakarote.crm9.erp.crm.service.handler.customer.sync;

import com.google.common.base.Strings;
import com.jfinal.aop.Inject;
import com.kakarote.crm9.common.CrmContext;
import com.kakarote.crm9.common.exception.CrmException;
import com.kakarote.crm9.erp.admin.service.AdminDataDicService;
import com.kakarote.crm9.erp.admin.service.AdminUserService;
import com.kakarote.crm9.erp.crm.common.CustomerOriginEnum;
import com.kakarote.crm9.erp.crm.constant.CrmConstant;
import com.kakarote.crm9.erp.crm.constant.CrmTagConstant;
import com.kakarote.crm9.erp.crm.entity.CrmCustomer;
import com.kakarote.crm9.erp.crm.entity.CrmCustomerExt;
import com.kakarote.crm9.erp.crm.entity.CrmDistributorPromotionRelation;
import com.kakarote.crm9.erp.crm.service.*;
import com.kakarote.crm9.integration.entity.SiteMember;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.Objects;

/**
 * @Author: haihong.wu
 * @Date: 2020/6/2 4:00 下午
 */
@Slf4j
public abstract class BaseSyncHandler {

    @Inject
    protected CrmDistributorPromotionRelationService crmDistributorPromotionRelationService;

    @Inject
    protected AdminDataDicService adminDataDicService;

    @Inject
    protected CrmCustomerService crmCustomerService;

    @Inject
    protected CrmSiteMemberService crmSiteMemberService;

    @Inject
    protected CrmContactsService crmContactsService;

    @Inject
    protected AdminUserService adminUserService;

    @Inject
    protected CrmCustomerExtService crmCustomerExtService;

    @Inject
    protected CrmRecordService crmRecordService;

    @Inject
    protected CrmChangeLogService crmChangeLogService;

    /**
     * 认证逻辑入口
     *
     * @param param
     */
    public abstract void handle(Object param);

    /**
     * 查询分销商推广关系并放入上下文
     *
     * @param siteMemberId
     * @param crmContext
     */
    protected void queryDistributorRelation(Long siteMemberId, CrmContext crmContext) {
        CrmDistributorPromotionRelation relation = crmDistributorPromotionRelationService.findByUserId(siteMemberId);
        crmContext.put(CrmContext.DISTRIBUTION_RELATION, relation);
    }

    /**
     * 冗余上游分销商
     *
     * @param siteMemberId
     * @param crmContext
     */
    protected void redundancyParentSiteMemberId(Long siteMemberId, CrmContext crmContext) {
        CrmDistributorPromotionRelation relation = crmContext.getAs(CrmContext.DISTRIBUTION_RELATION);
        if (Objects.isNull(relation)) {
            return;
        }
        if (!syncBindInfo(relation, siteMemberId)) {
            throw new CrmException("冗余数据失败");
        }
    }

    /**
     * 同步推广关系到冗余表
     *
     * @param relation
     * @param siteMemberId
     * @return
     */
    private boolean syncBindInfo(CrmDistributorPromotionRelation relation, Long siteMemberId) {
        if (Objects.isNull(relation)) {
            // 没有推广关系，不做处理
            return true;
        }
        // 同步客户扩展表的上游分销商的网站会员id
        // 根据网站会员id查询客户信息（包含customer_ext）
        CrmCustomerExt crmCustomerExt = crmCustomerExtService.queryCrmCustomerExtbySiteMemberId(siteMemberId);
        if (Objects.nonNull(crmCustomerExt) && Objects.nonNull(crmCustomerExt.getCustomerId())) {
            log.info("--------->syncBindInfo customer_Ext in db is not null: {}", crmCustomerExt);
            crmCustomerExt.setParentSiteMemberId(relation.getPUid());
            return crmCustomerExt.update();
        } else if (Objects.nonNull(crmCustomerExt) && Objects.nonNull(crmCustomerExt.getMainCustomerId())) {
            log.info("--------->syncBindInfo customer_Ext in db is null,site_member_id: {}", siteMemberId);
            crmCustomerExt.setCustomerId(crmCustomerExt.getMainCustomerId().intValue());
            crmCustomerExt.setParentSiteMemberId(relation.getPUid());
            return crmCustomerExt.save();
        } else {
            log.info("--------->syncBindInfo customer in db is null,site_member_id: {}", siteMemberId);
        }
        return true;
    }

    /**
     * 组装客户数据
     *
     * @param crmCustomer
     * @param siteMember
     * @return
     */
    protected CrmCustomer assembleCrmCustomer(CrmCustomer crmCustomer, SiteMember siteMember) {
        if (validate(crmCustomer.getCustomerName(), siteMember.getRealName())) {
            crmCustomer.setCustomerName(siteMember.getRealName());
        }
        if (validate(crmCustomer.getMobile(), siteMember.getMobile())) {
            crmCustomer.setMobile(siteMember.getMobile());
        }
        if (validate(crmCustomer.getAddress(), siteMember.getProv())) {
            crmCustomer.setAddress(checkStringEmpty(siteMember.getProv()) + checkStringEmpty(siteMember.getCity()));
        }
        if (validate(crmCustomer.getDetailAddress(), siteMember.getContactAddress())) {
            crmCustomer.setDetailAddress(checkStringEmpty(siteMember.getContactAddress()));
        }
        if (crmCustomer.getCustomerType() == null) {
            int type = siteMember.getUserType();
            String userType = type == Integer.parseInt(CrmConstant.USER_TYPE_PERSONAL) ? CrmConstant.CRM_SITE_USER_TYPE_PERSONAL : CrmConstant.BUSINESS_CLIENTS;
            crmCustomer.setCustomerType(getCustomerType(userType));
        }
        if (validate(crmCustomer.getRegistrationNumber(), siteMember.getBusinessLicenceNum())) {
            crmCustomer.setRegistrationNumber(siteMember.getBusinessLicenceNum());
            crmCustomer.setCreditCode(siteMember.getBusinessLicenceNum());
        }
        if (validate(crmCustomer.getRegistrationImgUrl(), siteMember.getBusinessLicenceImgUrl())) {
            crmCustomer.setRegistrationImgUrl(siteMember.getBusinessLicenceImgUrl());
        }
        if (validate(crmCustomer.getIdCard(), siteMember.getIdNum())) {
            crmCustomer.setIdCard(siteMember.getIdNum());
        }
        if (validate(crmCustomer.getIdCardUrl(), siteMember.getIdImgUrl())) {
            crmCustomer.setIdCardUrl(siteMember.getIdImgUrl());
        }
        if (crmCustomer.getCustomerOrigin() == null) {
            crmCustomer.setCustomerOrigin(CustomerOriginEnum.WEB_SITE_ORIGIN_KEY.getTypes());
        }
        if (crmCustomer.getIsAttestation() == null) {
            crmCustomer.setIsAttestation(siteMember.getAuditStatus() + "");
        }
        if (validate(crmCustomer.getRegistrationDate(), siteMember.getGmtCreate())) {
            crmCustomer.setRegistrationDate(siteMember.getGmtCreate());
        }
        //当原始客户姓名不为空的时候，赋值给customer
        if (validate(crmCustomer.getOriginalCustomerName(), siteMember.getOriginalCustomerName())) {
            crmCustomer.setOriginalCustomerName(siteMember.getOriginalCustomerName());
        }
        return crmCustomer;
    }

    private boolean validate(Object oldVal, Object newVal) {
        if (oldVal instanceof String && newVal instanceof String) {
            return StringUtils.isBlank((String) oldVal) && StringUtils.isNotBlank((String) newVal);
        }
        if (oldVal instanceof String) {
            return StringUtils.isBlank((String) oldVal) && Objects.nonNull(newVal);
        }
        if (newVal instanceof String) {
            return Objects.isNull(oldVal) && StringUtils.isNotBlank((String) newVal);
        }
        return Objects.isNull(oldVal) && Objects.nonNull(newVal);
    }

    protected String getCustomerType(String typeNameInSiteMember) {
        return adminDataDicService.formatTagValueName(CrmTagConstant.CUSTOMER_TYPE, typeNameInSiteMember);
    }

    protected static String checkStringEmpty(String item) {
        return Strings.isNullOrEmpty(item) ? "" : item;
    }
}
