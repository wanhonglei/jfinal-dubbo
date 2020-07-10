package com.kakarote.crm9.erp.admin.service;

import com.jfinal.kit.Kv;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.SqlPara;
import com.kakarote.crm9.erp.admin.entity.CrmBusinessStatusVerification;
import org.apache.commons.collections.CollectionUtils;
import com.kakarote.crm9.erp.crm.entity.CrmBusinessStatusVerificationRecord;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * @author haihong.wu
 */
public class CrmBusinessStatusVerificationService {

    /**
     * 统计商机可验证结果记录数量
     * @param businessId 商机ID
     * @param statusId 商机组阶段ID
     * @return
     */
    public int countRecordByStatusIdOfBusiness(Long businessId, Long statusId) {
        return Optional.ofNullable(Db.queryInt(Db.getSql("crm.business.status.verification.record.countRecordByStatusIdOfBusiness"),businessId,statusId)).orElse(0);
    }


    public List<Record> listByStatusIdOfBusiness(Long businessId, Long statusId) {
        return Db.find(Db.getSql("crm.business.status.verification.record.listByStatusIdOfBusiness"), businessId, statusId);
    }

    /**
     * 通过商机阶段查询商机可验证结果列表
     * @param statusId
     * @return
     */
    public List<CrmBusinessStatusVerification> selectAllByStatusId(Long statusId) {
        if (Objects.isNull(statusId)) {
            return null;
        }
        return CrmBusinessStatusVerification.dao.find(Db.getSql("crm.businessStatusVerification.selectBystatusId"), statusId);
    }

    /**
     * 查询关联验证结果的商机验证结果记录信息
     *
     * @param verificationIds
     * @return
     */
    public List<Record> selectVerificationRecordsByVerificationIds(List<Long> verificationIds) {
        if (CollectionUtils.isEmpty(verificationIds)) {
            return Collections.emptyList();
        }
        Kv kv = Kv.by("verificationIds", verificationIds);
        SqlPara sqlPara = Db.getSqlPara("crm.business.status.verification.record.selectRecordsByVerificationIds", kv);
        return Db.find(sqlPara);
    }

    /**
     * 根据商机ID和商机组可验证结果ID查找商机可验证结果记录
     * @param businessId
     * @param verificationId
     * @return
     */
    public CrmBusinessStatusVerificationRecord findRecordByBizIdAndVeriId(Long businessId, Long verificationId) {
        return CrmBusinessStatusVerificationRecord.dao.findFirst(Db.getSql("crm.business.status.verification.record.findRecordByBizIdAndVeriId"), businessId, verificationId);
    }
}
