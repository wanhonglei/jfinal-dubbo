package com.kakarote.crm9.erp.crm.service;

import com.jfinal.log.Log;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.kakarote.crm9.erp.crm.common.CrmSensitiveEnum;
import com.kakarote.crm9.erp.crm.constant.CrmConstant;
import com.kakarote.crm9.erp.crm.entity.SensitiveAccessLog;

import java.util.Date;

/**
 * Crm Sensitive Access Log Service
 *
 * @author hao.fu
 * @create 2019/8/6 11:42
 */
public class CrmSensitiveAccessLogService {

    private Log logger = Log.getLog(getClass());

    public Long getSensitiveEntryId(CrmSensitiveEnum item) {
        Record record = Db.findFirst(Db.getSql("crm.sensitivelog.getSensitiveEntryId"), item.getTableName(), item.getFieldName());
        if (record != null) {
            return record.getLong("id");
        } else {
            logger.error(String.format("could not find sensitive entry in db. entry: %s, %s", item.getTableName(), item.getFieldName()));
        }
        return null;
    }

    public void addSensitiveAccessLog(CrmSensitiveEnum item, String username, String dataId) {
        Long data = 0L;
        if (dataId == null || dataId.isEmpty()) {
            logger.error("addSensitiveAccessLog failed, dataId is null.");
            return;
        } else {
            data = Long.valueOf(dataId);
        }

        Long entryId = getSensitiveEntryId(item);
        addLog(username, entryId, data);
    }

    private void addLog(String who, Long entryId, Long dataId) {
        SensitiveAccessLog log = new SensitiveAccessLog();
        log.setSecretEntityId(entryId);
        log.setDataId(dataId);
        log.setWho(who);
        log.setCreateTime(new Date());
        log.setUpdateTime(new Date());
        log.setIsDeleted(CrmConstant.DELETE_FLAG_NO);
        logger.info(String.format("%s access %s - %s", who, entryId.toString(), dataId.toString()));
        log.save();
    }
}
