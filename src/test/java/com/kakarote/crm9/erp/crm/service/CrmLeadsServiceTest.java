package com.kakarote.crm9.erp.crm.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.jfinal.aop.Aop;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.kakarote.crm9.BaseTest;
import com.kakarote.crm9.common.config.paragetter.BasePageRequest;
import com.kakarote.crm9.common.midway.NotifyService;
import com.kakarote.crm9.erp.crm.entity.CrmBaseTag;
import com.kakarote.crm9.erp.crm.entity.CrmLeads;
import com.kakarote.crm9.erp.crm.entity.CrmServiceTag;
import org.apache.velocity.app.VelocityEngine;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @Author: honglei.wan
 * @Description:
 * @Date: Create in 2020/3/2 18:11
 */
public class CrmLeadsServiceTest extends BaseTest {

    private CrmLeadsService crmLeadsService = Aop.get(CrmLeadsService.class);
    private NotifyService notifyService = Aop.get(NotifyService.class);
    private VelocityEngine velocityEngine = Aop.get(VelocityEngine.class);

    @Test
    public void addOrUpdateForExcel() {
        Db.tx(() -> {
            List<JSONObject> dataList = new ArrayList<>();
            JSONObject jsonObject = JSONObject.parseObject("{\"entity\":{\"address\":\"江苏省,南京市,鼓楼区\",\"company\":\"上海大>大戏\",\"contactDeptName\":\"数字地信事业部\",\"contactUser\":\"dada系\",\"customerLevel\":\"\",\"customerLevelName\":\"好大\",\"detailAddress\":\"空间环境狂欢节好\",\"email\":\"jkhjh@qq.com\",\"leadCome\":373,\"ownerUserId\":2736,\"position\":\"BD\",\"requireDescription\":\"老客户解决客户空间和机会\",\"telephone\":\"897897767\"}}");
            dataList.add(jsonObject);

            crmLeadsService.addOrUpdateForExcel(dataList, notifyService, velocityEngine,2736l);
            return false;
        });
    }

    @Test
    public void getLeadsPageList() {
        CrmLeads crmLeads = new CrmLeads();
        crmLeads.setLeadsName("李月-测试0034");
        BasePageRequest<CrmLeads> basePageRequest = new BasePageRequest<>(1,10,crmLeads);
        crmLeadsService.getLeadsPageList(basePageRequest);
    }

    @Test
    public void addOrUpdate() {
        JSONObject jsonObject = JSONObject.parseObject("{\"entity\":{\"address\":\"安徽省,宿州市,埇桥>区\",\"accuracyRequirements\":\"47\",\"customerIndustry\":\"\",\"deptId\":294,\"telephone\":\"17855772777\",\"customerLevel\":\"\",\"weChat\":\"\",\"detailAddress\":\"\",\"company\":\"宿州方韦测绘有限公司\",\"contactUser\":\"刘先生\",\"position\":\"\",\"leadCome\":148,\"contactDeptName\":\"\",\"requireDescription\":\"客户咨询想要合作\",\"email\":\"\"}}");
        try {
            crmLeadsService.addOrUpdate(jsonObject,notifyService,velocityEngine,2736L);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    @Test
    public void isExists() {
        crmLeadsService.isExists("1212",new Record());
    }

    @Test
    public void getCheckInfo() {
        Record record = new Record();
        crmLeadsService.getCheckInfo(record);
        record.set("owner_user_id","2736");
        crmLeadsService.getCheckInfo(record);
    }

    @Test
    public void sendEmail() {
        CrmLeads leads = new CrmLeads();
        leads.setDeptId("373");
        try {
            crmLeadsService.sendEmail(notifyService,velocityEngine,leads);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    @Test
    public void information() {
        crmLeadsService.information(98884);
    }

    @Test
    public void queryById() {
        crmLeadsService.queryById(98884);
    }

    @Test
    public void getTagNameByTypeAndId() {
        crmLeadsService.getTagNameByTypeAndId(98878,"crm_leads");
    }

    @Test
    public void queryByName() {
        crmLeadsService.queryByName("李月-测试0034");
    }

    @Test
    public void queryByCompany() {
        crmLeadsService.queryByCompany("千寻位置");
    }

    @Test
    public void queryByTelephone() {
        crmLeadsService.queryByTelephone("12356222222");
    }

    @Test
    public void deleteByIds() {
        crmLeadsService.deleteByIds("123,232,17782");
    }

    @Test
    public void updateOwnerUserId() {
        crmLeadsService.updateOwnerUserId("98884",2736);
    }

    @Test
    public void translate() {
        JSONObject jsonObject = JSON.parseObject("{\n" +
                "\t\"entity\": {\n" +
                "\t\t\"address\": \"江苏省,南京市,鼓楼区\",\n" +
                "\t\t\"location\": \"\",\n" +
                "\t\t\"detailAddress\": \"空间环境狂欢节好\",\n" +
                "\t\t\"customerName\": \"上海大大戏\",\n" +
                "\t\t\"registerCapital\": \"\",\n" +
                "\t\t\"legalPerson\": \"\",\n" +
                "\t\t\"registrationNumber\": \"\",\n" +
                "\t\t\"creditCode\": \"\",\n" +
                "\t\t\"remark\": \"老客户解决客户空间和机会\",\n" +
                "\t\t\"customerGrade\": \"157\",\n" +
                "\t\t\"customerCategory\": null,\n" +
                "\t\t\"customerType\": \"153\",\n" +
                "\t\t\"ownerUserId\": 2736,\n" +
                "\t\t\"isMultiple\": 0,\n" +
                "\t\t\"registrationImgUrl\": \"\",\n" +
                "\t\t\"url\": \"\",\n" +
                "\t\t\"customerSource\": \"\",\n" +
                "\t\t\"industry\": \"\",\n" +
                "\t\t\"storageType\": 2,\n" +
                "\t\t\"leadsId\": 98884,\n" +
                "\t\t\"customerOrigin\": 1\n" +
                "\t}\n" +
                "}");
        crmLeadsService.translate(jsonObject,2736L);
    }

    @Test
    public void queryField() {
        crmLeadsService.queryField();
    }

    @Test
    public void queryExcelField() {
        crmLeadsService.queryExcelField();
    }

    @Test
    public void getDeptInfo() {
        Record record = new Record();
        record.set("formType","structure");
        crmLeadsService.getDeptInfo(Collections.singletonList(record));
    }

    @Test
    public void testQueryField() {
        crmLeadsService.queryField(98882);
    }

    @Test
    public void exportLeads() {
        crmLeadsService.exportLeads("98882");
    }

    @Test
    public void getCheckingField() {
        crmLeadsService.getCheckingField();
    }

    @Test
    public void receive() {
        crmLeadsService.receive("98882");
    }

    @Test
    public void getSemById() {
        crmLeadsService.getSemById("98882");
    }

    @Test
    public void updateRulesSetting() {
        crmLeadsService.updateRulesSetting(12,12,12,-1);
    }

    @Test
    public void getRulesSetting() {
        crmLeadsService.getRulesSetting();
    }

    @Test
    public void pullLeadsPublicPool() {
        JSONObject jsonObject = JSON.parseObject(" {\"entity\":{\"id\":98862,\"tag\":[{\"name\":\"无法满足的需求\",\"isPublic\":true,\"id\":\"198\"}]}}");
        crmLeadsService.pullLeadsPublicPool(jsonObject,2736L);
    }

    @Test
    public void constructCrmPrivateTag() {
        CrmServiceTag serviceTag = new CrmServiceTag();
        serviceTag.setId("1212");
        serviceTag.setTag(Collections.singletonList(new CrmBaseTag()));
        crmLeadsService.constructCrmPrivateTag(serviceTag);
    }

    @Test
    public void getWechatByLeadsId() {
        crmLeadsService.getWechatByLeadsId("98884");
    }

    @Test
    public void getTelephoneByLeadsId() {
        crmLeadsService.getTelephoneByLeadsId("98884");
    }

    @Test
    public void getEmailByLeadsId() {
        crmLeadsService.getEmailByLeadsId("98884");
    }

    @Test
    public void getDeptListByIndustryId() {
        crmLeadsService.getDeptListByIndustryId("252");
    }
}
