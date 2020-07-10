package com.kakarote.crm9.erp.admin.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jfinal.aop.Aop;
import com.kakarote.crm9.BaseTest;
import com.kakarote.crm9.common.config.paragetter.BasePageRequest;
import com.kakarote.crm9.erp.admin.entity.CrmBusinessType;
import org.junit.Test;

/**
 * @Descriotion:
 * @param:
 * @return:
 * @author:yue.li Created by yue.li on 2019/8/6.
 */
public class AdminBusinessTypeServiceTest extends BaseTest {

    private AdminBusinessTypeService adminBusinessTypeService = Aop.get(AdminBusinessTypeService.class);

    @Test
    public void addBusinessType() {
        CrmBusinessType crmBusinessType = new CrmBusinessType();
        crmBusinessType.setTypeId(null);
        crmBusinessType.setDeptEmailGroup("11111111@qq.com");
        crmBusinessType.setName("1");
        crmBusinessType.setDeptIds("223");
        JSONArray crmBusinessStatusList = new JSONArray();
        JSONObject object = new JSONObject();
        object.put("name","124");
        object.put("rate","10");
        object.put("businessStateType",1);
        crmBusinessStatusList.add(object);
        adminBusinessTypeService.addBusinessType(crmBusinessType,crmBusinessStatusList,3);
    }

    @Test
    public void getBusinessType() {
        String type = "10";
        adminBusinessTypeService.getBusinessType(type);
    }

    @Test
    public void deleteById() {
        String type = "58";
        adminBusinessTypeService.deleteById(type);
    }

    @Test
    public void queryBusinessTypeList() {
        BasePageRequest request = new BasePageRequest(1,10,null);
        adminBusinessTypeService.queryBusinessTypeList(request);
    }
}
