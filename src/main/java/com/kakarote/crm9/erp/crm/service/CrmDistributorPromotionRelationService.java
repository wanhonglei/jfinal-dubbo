package com.kakarote.crm9.erp.crm.service;

import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.kakarote.crm9.erp.crm.entity.CrmDistributorPromotionRelation;

/**
 * @Author: haihong.wu
 * @Date: 2020/4/24 2:24 下午
 */
public class CrmDistributorPromotionRelationService {

    /**
     * 根据用户ID找关联关系
     * @param userId 下游用户会员ID
     * @return
     */
    public CrmDistributorPromotionRelation findByUserId(Long userId) {
        return CrmDistributorPromotionRelation.dao.findFirst(Db.getSql("crm.distributorPromotionRelation.findByUserId"),userId);
    }

    /**
     * 通过客户编号查询客户上级分销商编号
     *
     * @param customerNo
     * @return
     */
    public Record queryParentCustIdByCustId(String customerNo) {
        return Db.findFirst(Db.getSql("crm.distributorPromotionRelation.selectParentCustomerInfoByCustId"), customerNo);
    }

    /**
     * 通过会员编号查询上级分销商信息
     *
     * @param memberId
     * @return
     */
    public Record queryParentCustomerInfoByMemberId(Long memberId) {
        return Db.findFirst(Db.getSql("crm.distributorPromotionRelation.selectParentCustomerInfoByMemberId"), memberId);
    }

    /**
     * 根据用户ID删除推广关系
     * @param userId
     */
    public void deleteByUid(Long userId) {
        Db.delete(Db.getSql("crm.distributorPromotionRelation.deleteByUserId"), userId);
    }

    public CrmDistributorPromotionRelation findByMobile(String phone) {
        return CrmDistributorPromotionRelation.dao.findFirst(Db.getSql("crm.distributorPromotionRelation.findByMobile"), phone);
    }
}
