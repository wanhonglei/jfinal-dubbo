package com.kakarote.crm9.erp.admin.service;

import com.jfinal.aop.Aop;
import com.kakarote.crm9.BaseTest;
import com.kakarote.crm9.common.config.paragetter.BasePageRequest;
import com.kakarote.crm9.erp.admin.entity.AdminLookUpLog;
import com.kakarote.crm9.erp.admin.entity.AdminScenario;
import org.junit.Test;

/**
 * @Descriotion:
 * @param:
 * @return:
 * @author:yue.li Created by yue.li on 2019/8/6.
 */
public class AdminLookUpLogServiceTest extends BaseTest {

    AdminLookUpLogService adminLookUpLogService = Aop.get(AdminLookUpLogService.class);

    @Test
    public void addLookUpLog() {
        AdminLookUpLog adminLookUpLog = new AdminLookUpLog();
        adminLookUpLog.setLookUpName("测试数据");
        adminLookUpLog.setBillsId("1");
        adminLookUpLogService.addLookUpLog(adminLookUpLog, 3L);
    }

    @Test
    public void queryLookUpLogList() {
        AdminScenario adminScenario = new AdminScenario();
        adminScenario.setDeptId(281L);
        BasePageRequest<AdminScenario> request = new BasePageRequest<>(1,10,adminScenario);
        adminLookUpLogService.queryLookUpLogList(request);
    }
}
