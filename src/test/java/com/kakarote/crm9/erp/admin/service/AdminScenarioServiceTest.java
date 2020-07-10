package com.kakarote.crm9.erp.admin.service;

import com.alibaba.fastjson.JSONObject;
import com.jfinal.aop.Aop;
import com.kakarote.crm9.BaseTest;
import org.junit.Test;

/**
 * @Descriotion:
 * @param:
 * @return:
 * @author:yue.li Created by yue.li on 2019/8/6.
 */
public class AdminScenarioServiceTest extends BaseTest {

    private AdminScenarioService adminScenarioService = Aop.get(AdminScenarioService.class);

    @Test
    public void addScenario() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("name","121212");
        jsonObject.put("deptId","8");
        adminScenarioService.addScenario(jsonObject);
    }

    @Test
    public void deleteByIds() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("scenarioIds","24");
        adminScenarioService.deleteByIds(jsonObject);
    }
}
