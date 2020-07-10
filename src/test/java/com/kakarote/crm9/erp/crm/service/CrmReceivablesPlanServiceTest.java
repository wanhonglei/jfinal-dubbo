package com.kakarote.crm9.erp.crm.service;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.alibaba.fastjson.JSONObject;
import com.jfinal.aop.Aop;
import com.jfinal.kit.JsonKit;
import com.jfinal.plugin.activerecord.Record;
import com.kakarote.crm9.BaseTest;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CrmReceivablesPlanServiceTest extends BaseTest {

    CrmReceivablesPlanService rpService = Aop.get(CrmReceivablesPlanService.class);

    @Test
    public void information() {
        List<Record> information = rpService.information(41);
        System.out.println(JsonKit.toJson(information));
        Assert.assertNotNull(information);
    }

    @Test
    public void saveAndUpdate() {
        JSONObject hj = new JSONObject();
        JSONObject entity = new JSONObject();
        entity.put("customerId",362);
        entity.put("businessId",2);
        entity.put("money","100");
        entity.put("returnDate","2019-08-30");
        hj.put("entity",entity);
        Assert.assertNotNull(rpService.saveAndUpdate(hj,new Long(929)));
    }

    @Test
    public void getById() {
        try {
			System.out.println(JsonKit.toJson(rpService.getById(39)));
		} catch (Exception e) {
			log.error("CrmReceivablesPlanServiceTest -> getById 异常", e);
		}
    }
}