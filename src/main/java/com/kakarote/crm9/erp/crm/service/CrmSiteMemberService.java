package com.kakarote.crm9.erp.crm.service;


import com.google.common.collect.Lists;
import com.jfinal.aop.Inject;
import com.jfinal.kit.Kv;
import com.jfinal.log.Log;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.SqlPara;
import com.kakarote.crm9.erp.crm.common.CrmErrorInfo;
import com.kakarote.crm9.erp.crm.common.DistributorCertifiedEnum;
import com.kakarote.crm9.erp.crm.common.PromotionTagEnum;
import com.kakarote.crm9.erp.crm.common.SiteMemberUserTypeEnum;
import com.kakarote.crm9.erp.crm.dto.PromotionInformationDto;
import com.kakarote.crm9.erp.crm.entity.CrmCustomer;
import com.kakarote.crm9.erp.crm.entity.CrmDistributorPromotionRelation;
import com.kakarote.crm9.erp.crm.entity.CrmSiteMember;
import com.kakarote.crm9.utils.R;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Crm Site Member Service
 *
 * @author hao.fu
 * @create 2019/7/6 9:31
 */
public class CrmSiteMemberService {

    @Inject
    private CrmDistributorPromotionRelationService crmDistributorPromotionRelationService;

    private Log logger = Log.getLog(getClass());

    public R addSiteMember(CrmSiteMember siteMember) {
        Record record = siteMember.toRecord();
        boolean success = Db.save("72crm_crm_site_member", record);
        return success ? R.ok() : R.error("add crm site member failed: " + siteMember.toJson());
    }

    public void updateSiteMember(CrmSiteMember siteMember) {
        Db.update("72crm_crm_site_member", siteMember.toRecord());
    }

    public void updateSiteMember(Record record) {
        Db.update("72crm_crm_site_member", record);
    }

    /**
     * Find site member by cust no.
     *
     * @param custId 客户编号
     * @return
     */
    public Record findByCustId(String custId) {
        return Db.findFirst(Db.getSql("crm.sitemember.findByCustId"), custId);
    }

    public void updateSiteMemberDeleteFlag(String custNo, int isDelete) {
        Db.update(Db.getSql("crm.sitemember.updateSiteMemberDeleteFlag"), isDelete, custNo);
    }

    /**
     * 通过客户ID获取会员账号信息
     *
     * @param page
     * @param limit
     * @param customerId
     * @return
     */
    public Page<Record> getSiteMemberInfoByCustId(int page, int limit, String customerId) {
        if (StringUtils.isEmpty(customerId)) {
            return new Page<>();
        }
        long customerSearchId;
        try {
            customerSearchId = Long.parseLong(customerId);
        } catch (Exception e) {
            // customer id is invalid
            logger.error("getSiteMemberInfoByCustId failed, customer id is invalid: " + customerId);
            return new Page<>();
        }
        Page<Record> recordPage = Db.paginate(page, limit, new SqlPara().setSql(Db.getSql("crm.sitemember.selectSiteMemberInfosByCustId"))
                .addPara(customerSearchId));
        if (CollectionUtils.isNotEmpty(recordPage.getList())) {
            recordPage.getList().forEach(record -> {
                if (Objects.nonNull(record.getInt("userType"))) {
                    SiteMemberUserTypeEnum userTypeEnum = SiteMemberUserTypeEnum.getUserTypeByCode(record.getInt("userType"));
                    record.set("customerTypeDesc", Objects.nonNull(userTypeEnum) ? userTypeEnum.getDesc() : "");
                    record.set("promotionTag", PromotionTagEnum.getDesc(record.getStr("promotionTag")));
                }
            });
        }

        return recordPage;
    }

    public Record getSiteMemberInfoBySiteMemberId(String siteMemberId) {
        return Db.findFirst(Db.getSql("crm.sitemember.findBySiteMemberId"), siteMemberId);
    }

    public Record getSiteMemberAllFieldBySiteMemberId(Long siteMemberId) {
        return Db.findFirst(Db.getSql("crm.sitemember.findAllColumnBySiteMemberId"), siteMemberId);
    }

    /**
     * bind site member to customer.
     *
     * @param custId       customer id
     * @param siteMemberId site member id
     * @return
     */
    public R bindSiteMemberToCust(String custId, String siteMemberId) {
        if (custId.isEmpty() || siteMemberId.isEmpty()) {
            return R.error("id could not be empty");
        }
        long customerId;
        long siteMemId;
        try {
            customerId = Long.parseLong(custId);
            siteMemId = Long.parseLong(siteMemberId);
        } catch (Exception e) {
            return R.error("id invalid");
        }

        Record customer = Db.findFirst(Db.getSqlPara("crm.customer.queryCustomerInfo", Kv.by("customer_id", customerId)));
        String custNo = customer.getStr("customer_no");

        Record siteMemberInDb = Db.findFirst(Db.getSql("crm.sitemember.findByCustId"), custNo);
        if (siteMemberInDb != null && siteMemberInDb.getStr("cust_id") != null) {
            return R.error(String.format(CrmErrorInfo.SITE_MEMBER_INFO + "(%s)" + CrmErrorInfo.SITE_MEMBER_NOT_NULL, customer.getStr("customer_name")));
        } else {
            int count = Db.update(Db.getSql("crm.sitemember.updateCustId"), custNo, siteMemId);
            return count > 0 ? R.ok("bind successful") : R.error();
        }
    }

    /***
     * 更新网站会员账号客编信息
     * @param  custNo 客编
     * @param  id 网站信息id
     */
    public void updateSiteMemberCustId(String custNo, String id) {
        Db.update(Db.getSql("crm.sitemember.updateSiteMemberCustId"), custNo, id);
    }

    public List<Long> listSiteMemberIdsByCustomerId(Long customerId) {
        return Db.query(Db.getSql("crm.sitemember.listSiteMemberIdsByCustomerId"), customerId);
    }

    /**
     * 更新网站会员用户的冻结状态
     *
     * @param siteMemberId
     * @param frozenStatus
     */
    public void updateFreezeStatusBySiteMemberId(Long siteMemberId, Integer frozenStatus) {
        Db.update(Db.getSql("crm.sitemember.updateFreezeStatusBySiteMemberId"), frozenStatus, siteMemberId);
    }

    /**
     * 查询客户推广信息
     *
     * @return
     */
    public R queryPromotionInfosBy(String custId) {
        if (StringUtils.isBlank(custId)) {
            return R.error(CrmErrorInfo.CUSTOMER_IS_NULL);
        }
        CrmCustomer crmCustomer = CrmCustomer.dao.findById(Long.valueOf(custId));
        if (Objects.isNull(crmCustomer)) {
            return R.error(CrmErrorInfo.CUSTOMER_IS_NULL);
        }
        String custNo = crmCustomer.getCustomerNo();
        List<Record> records = Db.find(Db.getSql("crm.distributorPromotionRelation.selectPromotionInfosByCustId"), custNo);
        if (CollectionUtils.isEmpty(records)) {
            return R.ok().put("data", Collections.emptyList());
        }
        List<PromotionInformationDto> promotionInformationDtos = Lists.newArrayList();
        records.forEach(record -> {
            String customerType = "";
            Integer userType = record.getInt("user_type");
            String customerId = record.getStr("customer_id");
            SiteMemberUserTypeEnum userTypeEnum = SiteMemberUserTypeEnum.getUserTypeByCode(userType);
            if (Objects.nonNull(userTypeEnum)) {
                customerType = userTypeEnum.getDesc();
            }
            String promotionTag = PromotionTagEnum.getDesc(record.getStr("promotion_tag"));

            PromotionInformationDto promotionInformationDto = PromotionInformationDto.builder()
                    .customerId(customerId)
                    .siteMemberName(record.getStr("user_name"))
                    .siteMemberId(Objects.nonNull(record.getLong("uid")) ? record.getLong("uid").toString() : null)
                    .realName(record.getStr("real_name"))
                    .mobile(StringUtils.isNotBlank(record.getStr("mobile")) ? "true" : "false")
                    .customerType(customerType)
                    .isAttestation(record.getInt("certified"))
                    .pSiteMemberId(Objects.nonNull(record.getLong("p_uid")) ? record.get("p_uid").toString() : null)
                    .promotionTag(promotionTag)
                    .build();
            promotionInformationDtos.add(promotionInformationDto);
        });
        return R.ok().put("data", promotionInformationDtos);
    }

    /**
     * 更新推广信息
     *  @param siteMemberUserTypeEnum
     * @param distributorCertifiedEnum
     * @param realName
     * @param memberId
     */
    public void updatePromotionRelationInfo(SiteMemberUserTypeEnum siteMemberUserTypeEnum,
                                            DistributorCertifiedEnum distributorCertifiedEnum, String realName, Long memberId) {
        Db.update(Db.getSql("crm.distributorPromotionRelation.updateUserTypeAndCertifiedByMemberId"), siteMemberUserTypeEnum.getCode(), distributorCertifiedEnum.getCode(), realName, memberId);
    }

    /**
     * 查询推广信息的手机号
     *
     * @param siteMemberId
     * @return
     */
    public R queryPromotionRelationMobile(Long siteMemberId) {
        CrmDistributorPromotionRelation relation = crmDistributorPromotionRelationService.findByUserId(siteMemberId);
        if (Objects.isNull(relation) || StringUtils.isBlank(relation.getMobile())) {
            return R.ok().put("data", null);
        }
        return R.ok().put("data", relation.getMobile());
    }

    public List<Record> queryByCustomerIds(List<Long> customerIds) {
        return Db.find(Db.getSqlPara("crm.sitemember.queryByCustomerIds",Kv.by("customerIds",customerIds)));
    }

    /**
     * 根据网站会员ID返回entity
     * @param memberId
     * @return
     */
    public CrmSiteMember getBySiteMemberId(Long memberId) {
        return CrmSiteMember.dao.findFirst(Db.getSql("crm.sitemember.findAllColumnBySiteMemberId"), memberId);
    }

    /**
     * 根据客户ID返回网站会员列表
     * @param customerId
     * @return
     */
    public List<CrmSiteMember> queryByCustomerId(Long customerId) {
        return CrmSiteMember.dao.find(Db.getSqlPara("crm.sitemember.queryByCustomerIds", Kv.by("customerIds", Collections.singletonList(customerId))));
    }
}
