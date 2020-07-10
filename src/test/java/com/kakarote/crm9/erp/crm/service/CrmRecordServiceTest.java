package com.kakarote.crm9.erp.crm.service;

import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.JSON;
import com.jfinal.aop.Aop;
import com.kakarote.crm9.BaseTest;
import com.kakarote.crm9.erp.admin.entity.AdminField;
import com.kakarote.crm9.erp.crm.common.CrmEnum;
import com.kakarote.crm9.erp.crm.constant.CrmConstant;
import com.kakarote.crm9.erp.crm.entity.CrmActionRecord;
import com.kakarote.crm9.erp.crm.entity.CrmBusiness;
import com.kakarote.crm9.erp.crm.entity.CrmContacts;
import com.kakarote.crm9.erp.crm.entity.CrmContract;
import com.kakarote.crm9.erp.crm.entity.CrmCustomer;
import com.kakarote.crm9.erp.crm.entity.CrmLeads;
import com.kakarote.crm9.erp.crm.entity.CrmProduct;
import com.kakarote.crm9.erp.crm.entity.CrmReceivables;
import com.kakarote.crm9.erp.crm.entity.CrmReceivablesPlan;
import com.kakarote.crm9.utils.R;
import org.junit.Test;

import java.util.Collections;

/**
 * @Descriotion:
 * @param:
 * @return:
 * @author:hao.fu Created by hao.fu on 2019/8/5.
 */
public class CrmRecordServiceTest extends BaseTest {

    private CrmRecordService crmRecordService = Aop.get(CrmRecordService.class);

    @Test
    public void addDeleteAttachmentRecord() {
        crmRecordService.addDeleteAttachmentRecord(12332, "1", "unittest.zip", 813L);
        R record = crmRecordService.queryRecordList("12332", "1");

    }

    @Test
    public void updateRecord() {
        AdminField adminField = AdminField.dao.findById(142);
        if (adminField.getValue() == null){
            adminField.setValue("12");
        }else {
            adminField.setValue(null);
        }
        crmRecordService.updateRecord(JSON.parseArray("[" + JSON.toJSONString(adminField) + "]"),"hjagjdgfgjs");
    }

    @Test
    public void formatCrmLeads() {
        CrmLeads crmLeads = CrmLeads.dao.findById(1);
        crmRecordService.formatCrmLeads(crmLeads);
    }

    @Test
    public void formatCrmCustomer() {
        CrmCustomer crmCustomer = CrmCustomer.dao.findById(607520);
        crmRecordService.formatCrmCustomer(crmCustomer);
    }

    @Test
    public void formatCrmContacts() {
        CrmContacts crmCustomer = CrmContacts.dao.findById(441);
        crmRecordService.formatCrmContacts(crmCustomer);
    }

    @Test
    public void formatCrmBusiness() {
        CrmBusiness crmBusiness = CrmBusiness.dao.findById(23);
        crmRecordService.formatCrmBusiness(crmBusiness);
    }

    @Test
    public void addRecord() {
        crmRecordService.addRecord(1,CrmEnum.CUSTOMER_TYPE_KEY.getTypes(),2736L);
    }

    @Test
    public void testAddRecord() {
        crmRecordService.addRecord(1,CrmEnum.CUSTOMER_TYPE_KEY.getTypes(),CrmEnum.CUSTOMER_TYPE_KEY.getTypes(),2736L);
    }

    @Test
    public void dealRecordLog() {
        crmRecordService.dealRecordLog(2736L,CrmEnum.CUSTOMER_TYPE_KEY.getTypes(),1,"测试内容",DateUtil.date() );
    }

    @Test
    public void addCrmActionRecord() {
        CrmActionRecord crmActionRecord = CrmActionRecord.dao.findById(1);
        crmActionRecord.setId(null);
        crmRecordService.addCrmActionRecord(crmActionRecord);
    }

    @Test
    public void testUpdateRecord() {
        try {
            CrmProduct oldObj = CrmProduct.dao.findById(1);
            CrmProduct newObj = CrmProduct.dao.findById(4);
            crmRecordService.updateRecord(oldObj,newObj,CrmEnum.PRODUCT_TYPE_KEY.getTypes(),2736L);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        try {
            CrmContacts oldObj1 = CrmContacts.dao.findById(441);
            CrmContacts newObj1 = CrmContacts.dao.findById(442);
            crmRecordService.updateRecord(oldObj1,newObj1,CrmEnum.CONTACTS_TYPE_KEY.getTypes(),2736L);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        try {
            CrmCustomer oldObj2 = CrmCustomer.dao.findById(53);
            CrmCustomer newObj2 = CrmCustomer.dao.findById(607520);
            crmRecordService.updateRecord(oldObj2,oldObj2,CrmEnum.CONTACTS_TYPE_KEY.getTypes(),2736L);
            crmRecordService.updateRecord(oldObj2,oldObj2,CrmEnum.CUSTOMER_DISTRIBUTE_KEY.getTypes(),2736L);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        try {
            CrmLeads oldObj3 = CrmLeads.dao.findById(1);
            CrmLeads newObj3 = CrmLeads.dao.findById(2);
            crmRecordService.updateRecord(oldObj3,oldObj3,CrmEnum.CONTACTS_TYPE_KEY.getTypes(),2736L);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        try {
            CrmContract oldObj4 = CrmContract.dao.findById(1);
            CrmContract newObj4 = CrmContract.dao.findById(2);
            crmRecordService.updateRecord(oldObj4,oldObj4,CrmEnum.CONTRACT_TYPE_KEY.getTypes(),2736L);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        try {
            CrmReceivables oldObj5 = CrmReceivables.dao.findById(8000148);
            CrmReceivables newObj5 = CrmReceivables.dao.findById(8000151);
            crmRecordService.updateRecord(oldObj5,oldObj5,CrmEnum.RECEIVABLES_TYPE_KEY.getTypes(),2736L);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        try {
            CrmBusiness oldObj6 = CrmBusiness.dao.findById(23);
            CrmBusiness newObj6 = CrmBusiness.dao.findById(24);
            crmRecordService.updateRecord(oldObj6,oldObj6,CrmEnum.RECEIVABLES_TYPE_KEY.getTypes(),2736L);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        try {
            CrmReceivablesPlan oldObj7 = CrmReceivablesPlan.dao.findById(11);
            CrmReceivablesPlan newObj7 = CrmReceivablesPlan.dao.findById(12);
            crmRecordService.updateRecord(oldObj7,oldObj7,CrmEnum.RECEIVABLES_PLAN_TYPE_KEY.getTypes(),2736L);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

    }

    @Test
    public void queryRecordList() {
        crmRecordService.queryRecordList("1",CrmEnum.CUSTOMER_TYPE_KEY.getTypes());
        crmRecordService.queryRecordList("9",CrmEnum.LEADS_TYPE_KEY.getTypes());

    }

    @Test
    public void addConversionRecord() {
        crmRecordService.addConversionRecord(1,CrmEnum.CUSTOMER_TYPE_KEY.getTypes(),2736);
    }

    @Test
    public void addConversionCustomerRecord() {
        crmRecordService.addConversionCustomerRecord(1,CrmEnum.CUSTOMER_TYPE_KEY.getTypes(),"ceshi");
    }

    @Test
    public void addPutIntoTheOpenSeaRecord() {
        crmRecordService.addPutIntoTheOpenSeaRecord(Collections.singletonList(53L),
                CrmEnum.CUSTOMER_TYPE_KEY.getTypes(), CrmConstant.PUBLIC_POOL + "，原因：客户自动释放");
    }

    @Test
    public void addDistributionRecord() {
        crmRecordService.addDistributionRecord("1",CrmEnum.CUSTOMER_TYPE_KEY.getTypes(),2736L);
    }

    @Test
    public void testAddDistributionRecord() {
        crmRecordService.addDistributionRecord("1",CrmEnum.CUSTOMER_TYPE_KEY.getTypes(),2736L,2736L);
    }

    @Test
    public void addActionRecord() {
        crmRecordService.addActionRecord(2736, "1", 1, "test");
    }

    @Test
    public void testAddActionRecord() {
        crmRecordService.addActionRecord(2736, "1", 1, DateUtil.date(),"test");
    }

    @Test
    public void deleteFollowRecord() {
        crmRecordService.deleteFollowRecord(1);
    }

    @Test
    public void deleteActionRecordsByTypeAndActionIds() {
        crmRecordService.deleteActionRecordsByTypeAndActionIds("1",Collections.singletonList(1L));
    }

    @Test
    public void saveUploadCustomerByExcelRecord() {
        crmRecordService.saveUploadCustomerByExcelRecord(2736L,1L);
    }
}
