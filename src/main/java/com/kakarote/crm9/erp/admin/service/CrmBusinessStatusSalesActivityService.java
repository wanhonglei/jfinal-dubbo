package com.kakarote.crm9.erp.admin.service;

import com.jfinal.kit.Kv;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.SqlPara;
import com.kakarote.crm9.erp.admin.entity.CrmBusinessStatusSalesActivity;
import com.kakarote.crm9.erp.crm.entity.CrmBusinessStatusSalesActivityRecord;
import org.apache.commons.collections.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * @author haihong.wu
 */
public class CrmBusinessStatusSalesActivityService {

    /**
     * 获取商机对应销售活动列表
     * @param businessId 商机ID
     * @param statusId 商机组阶段ID
     * @return
     */
    public List<Record> listByStatusIdOfBusiness(Long businessId, Long statusId) {
        return Db.find(Db.getSql("crm.business.status.salesActivity.record.listByStatusIdOfBusiness"), businessId, statusId);
    }

    /**
     * 获取销售活动记录列表
     * @param businessId 商机ID
     * @param statusId 商机组阶段ID
     * @return
     */
    public List<Record> listRecordByStatusIdOfBusiness(Long businessId, Long statusId) {
        return Db.find(Db.getSql("crm.business.status.salesActivity.record.listRecordByStatusIdOfBusiness"), businessId, statusId);
    }

    public List<CrmBusinessStatusSalesActivity> listByStatusId(Long statusId) {
        return CrmBusinessStatusSalesActivity.dao.find(Db.getSql("crm.businessStatusSalesActivity.listByStatusId"), statusId);
    }

    /**
     * 通过阶段编号查询阶段下的关键销售活动列表
     * @param statusId
     * @return
     */
    public List<CrmBusinessStatusSalesActivity> selectAllByStatusId(Long statusId) {
        if (Objects.isNull(statusId)) {
            return null;
        }
        List<CrmBusinessStatusSalesActivity> list = CrmBusinessStatusSalesActivity.dao.find(Db.getSql("crm.businessStatusSalesActivity.selectBystatusId"), statusId);
        if (CollectionUtils.isEmpty(list)) {
            return Collections.emptyList();
        }
        return list;
    }

    /**
     * 查询关联活动的商机活动记录信息
     *
     * @param activityIds
     * @return
     */
    public List<Record> selectActivityRecordsByActivityIds(List<Long> activityIds) {
        if (CollectionUtils.isEmpty(activityIds)) {
            return Collections.emptyList();
        }
        Kv kv = Kv.by("activityIds", activityIds);
        SqlPara sqlPara = Db.getSqlPara("crm.business.status.salesActivity.record.selectRecordsByActivityIds", kv);
        return Db.find(sqlPara);
    }

    public int countRecordByStatusIdOfBusiness(Long businessId, Long statusId) {
        return Optional.ofNullable(Db.queryInt(Db.getSql("crm.business.status.salesActivity.record.countRecordByStatusIdOfBusiness"), businessId, statusId)).orElse(0);
    }
}
