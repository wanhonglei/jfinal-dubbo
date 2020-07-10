package com.kakarote.crm9.erp.crm.service;

import com.jfinal.aop.Aop;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.kakarote.crm9.BaseTest;
import com.kakarote.crm9.erp.crm.common.CrmSensitiveEnum;
import org.junit.Assert;
import org.junit.Test;

/**
 * @Descriotion:
 * @param:
 * @return:
 * @author:hao.fu Created by hao.fu on 2019/8/6.
 */
public class CrmSensitiveAccessLogServiceTest extends BaseTest {

    CrmSensitiveAccessLogService crmSensitiveAccessLogService = Aop.get(CrmSensitiveAccessLogService.class);

    @Test
    public void getSensitiveEntryId() {
        Long id = crmSensitiveAccessLogService.getSensitiveEntryId(CrmSensitiveEnum.LEADS_WECHAT);
        System.out.println(id);
        Assert.assertTrue(id != null);
    }

    @Test
    public void addSensitiveAccessLog() {
        crmSensitiveAccessLogService.addSensitiveAccessLog(CrmSensitiveEnum.LEADS_WECHAT, "hao.fu", "12");
        Record record = Db.findFirst("select * from 72crm_sensitive_access_log where who = 'hao.fu' and data_id = 12");
        System.out.println(record.toJson());
        Assert.assertTrue(record != null);
    }
}
