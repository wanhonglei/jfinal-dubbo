package com.kakarote.crm9.erp.crm.service.handler.customer.sync;

import com.alibaba.fastjson.JSONObject;
import com.jfinal.plugin.activerecord.Db;
import com.kakarote.crm9.BaseTest;
import com.kakarote.crm9.erp.crm.common.CrmDistributorEnum;
import com.kakarote.crm9.erp.crm.common.CustomerStorageTypeEnum;
import com.kakarote.crm9.erp.crm.entity.*;
import com.kakarote.crm9.integration.entity.DistributorAuditDTO;
import com.kakarote.crm9.integration.entity.SiteMember;
import com.qxwz.venus.api.v2.model.CertPersonalResult;
import org.junit.Assert;
import org.junit.Test;

import java.util.Date;

/**
 * @Author: haihong.wu
 * @Date: 2020/6/2 7:39 下午
 */
public class CustomerSyncHandlerTest extends BaseTest {

    @Test
    public void getHandler() {
        Db.tx(() -> {
            long userId = 9999999L;
            long pUserId = 9999998L;
            String mobile = "18551780333";
            String name = "UnitTest999";
            CrmCustomer crmCustomer = new CrmCustomer();
            crmCustomer.setCustomerId(9999999L);
            crmCustomer.setCustomerName(name);
            crmCustomer.setCustomerNo("aaa" + crmCustomer.getCustomerId());
            crmCustomer.setBatchId("ttt");
            crmCustomer.save();
            CrmContacts crmContacts = new CrmContacts();
            crmContacts.setMobile(mobile);
            crmContacts.setCustomerId(crmCustomer.getCustomerId().intValue());
            crmContacts.setBatchId(crmCustomer.getBatchId());
            crmContacts.save();
            CertPersonalResult p1 = JSONObject.parseObject(
                    "{\"succ\": true, " +
                            "\"email\": \"5207bb41-3014-4870-8850-e4256e5c5931@abc.com\", " +
                            "\"channel\": \"tmall\", " +
                            "\"isSucc\": true, " +
                            "\"mobile\": \"" + mobile + "\", " +
                            "\"memberId\": " + userId + ", " +
                            "\"userName\":\"" + name + "\", " +
                            "\"userType\": 0, " +
                            "\"gmtCreate\": 1415701066000, " +
                            "\"returnMsg\": \"\", " +
                            "\"auditStatus\": 2" +
                            "}", CertPersonalResult.class);
            CustomerSyncHandler.getHandler(CustomerSyncHandler.SyncHandlersEnum.Website_Personal_Audit).handle(p1);
            CrmDistributorPromotionRelation relation = new CrmDistributorPromotionRelation();
            relation.setUid(userId);
            relation.setPUid(pUserId);
            relation.save();
            CrmSiteMember siteMember = new CrmSiteMember();
            siteMember.setCustId(crmCustomer.getCustomerNo());
            siteMember.setSiteMemberId(userId - 2);
            siteMember.save();
            CustomerSyncHandler.getHandler(CustomerSyncHandler.SyncHandlersEnum.Website_Personal_Audit).handle(p1);
            SiteMember p3 = JSONObject.parseObject(
                    "{\"id\": 16899605, " +
                            "\"salt\": \"MEjVdv\", " +
                            "\"email\": \"qxwz334l5@live.com\", " +
                            "\"regIp\": \"172.16.217.23\", " +
                            "\"mobile\": \"13433333345\", " +
                            "\"source\": 1, " +
                            "\"status\": 0, " +
                            "\"channel\": \"inviter\", " +
                            "\"linkMan\": \"扫地僧\", " +
                            "\"password\": \"8be205459a984116b727d60feba5c9f0\", " +
                            "\"realName\": \"qxwz3345\", " +
                            "\"userFlag\": \"normal\", " +
                            "\"userName\": \"qxwz3345\", " +
                            "\"userType\": 1, " +
                            "\"customUse\": \"车辆船舶监控\", " +
                            "\"gmtCreate\": 1588837693000, " +
                            "\"loginName\": \"533788756964531\", " +
                            "\"maxAppNum\": 500, " +
                            "\"synStatus\": 1, " +
                            "\"serviceUse\": 3, " +
                            "\"auditStatus\": 2, " +
                            "\"emailVerify\": 1, " +
                            "\"gmtModified\": 1588837835000, " +
                            "\"mobileVerify\": 1, " +
                            "\"contactAddress\": \"青岛宁夏路288号一号楼5楼\", " +
                            "\"ntripUserCount\": 0, " +
                            "\"auditFailureMsg\": \"\", " +
                            "\"auditSubmitTime\": 1588837836000, " +
                            "\"maxNtripUserNum\": 1000, " +
                            "\"ntripUserPrefix\": \"\", " +
                            "\"faccAccountStatus\": 1, " +
                            "\"businessLicenceNum\": \"435621232322334512\", " +
                            "\"businessLicenceImgUrl\": \"https://test-venus-pri.oss-cn-beijing.aliyuncs.com/license/785/352/d214260812cbdf756677682940538be8.png\"" +
                            "}", SiteMember.class);
//        CustomerSyncHandler.getHandler(CustomerSyncHandler.SyncHandlersEnum.Website_Company_Audit).handle(p2);
            return false;
        });
    }

    @Test
    public void distributorAudit() {
        Db.tx(() -> {
            long userId = 9999991L;
            CrmDistributorPromotionRelation relation = new CrmDistributorPromotionRelation();
            relation.setUid(userId);
            relation.setPUid(userId - 2);
            relation.save();
            DistributorAuditDTO param = JSONObject.parseObject("{\n" +
                    "  \"addressAreaCode\": \"120103\",\n" +
                    "  \"addressAreaName\": \"天津市市辖区河西区\",\n" +
                    "  \"addressDetail\": \"测试测试\",\n" +
                    "  \"applyTime\": 1587889274000,\n" +
                    "  \"areaCode\": \"150000\",\n" +
                    "  \"areaName\": \"内蒙古自治区\",\n" +
                    "  \"auditSuccessTime\": 1587889551000,\n" +
                    "  \"bdDept\": \"48284112\",\n" +
                    "  \"bdUserEmail\": \"1957345722-1907849355@wz-inc.com\",\n" +
                    "  \"bdUserName\": \"Person\",\n" +
                    "  \"businessLicenceBegin\": 1587830400000,\n" +
                    "  \"businessLicenceEnd\": 1682524799000,\n" +
                    "  \"businessLicenceImgUrl\": \"https://dev-venus-pri.oss-cn-beijing.aliyuncs.com/license/878/537/1405e277a1be74f3e8c60b879c3ce061.png\",\n" +
                    "  \"businessLicenceNum\": \"444433333266666234\",\n" +
                    "  \"contactAddress\": \"天津市市辖区河西区 测试测试\",\n" +
                    "  \"contactEmail\": \"\",\n" +
                    "  \"contactNumber\": \"14499983000\",\n" +
                    "  \"distributorStatus\": 3,\n" +
                    "  \"email\": \"445335@qq.com\",\n" +
                    "  \"gmtCreate\": 1587889274000,\n" +
                    "  \"id\": " + userId + ",\n" +
                    "  \"isDistributor\": 1,\n" +
                    "  \"level\": \"platinum\",\n" +
                    "  \"linkMan\": \"qweqwe\",\n" +
                    "  \"mobile\": \"14499983000\",\n" +
                    "  \"operateStatus\": 1,\n" +
                    "  \"realName\": \"unitTestt1\",\n" +
                    "  \"userName\": \"cscsx\",\n" +
                    "  \"userType\": 1\n" +
                    "}", DistributorAuditDTO.class);
            CustomerSyncHandler.getHandler(CustomerSyncHandler.SyncHandlersEnum.Distributor_Audit).handle(param);
            CrmSiteMember crmSiteMember = CrmSiteMember.dao.findFirst("select * from 72crm_crm_site_member where site_member_id = ?", userId);
            Assert.assertNotNull(crmSiteMember);
            Assert.assertEquals(Integer.valueOf(CrmDistributorEnum.IS_DISTRIBUTOR.getTypes()), crmSiteMember.getIsDistributor());
            CrmCustomer customer = CrmCustomer.dao.findFirst("select * from 72crm_crm_customer where customer_no = ?", crmSiteMember.getCustId());
            Assert.assertNotNull(customer);
            return false;
        });
    }


    @Test
    public void distributorAudit1() {
        Db.tx(() -> {
            long userId = 9999991L;
            String customerNo = "UUUU";
            CrmCustomer crmCustomer = new CrmCustomer();
            crmCustomer.setCustomerName("UnitTests1");
            crmCustomer.setOwnerUserId(1992);
            crmCustomer.setOwnerTime(new Date());
            crmCustomer.setOwnerUserName("wuhh");
            crmCustomer.setCustomerNo(customerNo);
            crmCustomer.setBatchId("--11");
            crmCustomer.save();
            CrmSiteMember siteMember1 = new CrmSiteMember();
            siteMember1.setSiteMemberId(userId);
            siteMember1.setRealName("unitTest");
            siteMember1.setCustId(customerNo);
            siteMember1.save();
            CrmDistributorPromotionRelation relation = new CrmDistributorPromotionRelation();
            relation.setUid(userId);
            relation.setPUid(userId - 2);
            relation.save();
            DistributorAuditDTO param = JSONObject.parseObject("{\n" +
                    "  \"addressAreaCode\": \"120103\",\n" +
                    "  \"addressAreaName\": \"天津市市辖区河西区\",\n" +
                    "  \"addressDetail\": \"测试测试\",\n" +
                    "  \"applyTime\": 1587889274000,\n" +
                    "  \"areaCode\": \"150000\",\n" +
                    "  \"areaName\": \"内蒙古自治区\",\n" +
                    "  \"auditSuccessTime\": 1587889551000,\n" +
                    "  \"bdDept\": \"48284112\",\n" +
                    "  \"bdUserEmail\": \"1957345722-1907849355@wz-inc.com\",\n" +
                    "  \"bdUserName\": \"Person\",\n" +
                    "  \"businessLicenceBegin\": 1587830400000,\n" +
                    "  \"businessLicenceEnd\": 1682524799000,\n" +
                    "  \"businessLicenceImgUrl\": \"https://dev-venus-pri.oss-cn-beijing.aliyuncs.com/license/878/537/1405e277a1be74f3e8c60b879c3ce061.png\",\n" +
                    "  \"businessLicenceNum\": \"444433333266666234\",\n" +
                    "  \"contactAddress\": \"天津市市辖区河西区 测试测试\",\n" +
                    "  \"contactEmail\": \"\",\n" +
                    "  \"contactNumber\": \"14499983000\",\n" +
                    "  \"distributorStatus\": 3,\n" +
                    "  \"email\": \"445335@qq.com\",\n" +
                    "  \"gmtCreate\": 1587889274000,\n" +
                    "  \"id\": " + userId + ",\n" +
                    "  \"isDistributor\": 1,\n" +
                    "  \"level\": \"platinum\",\n" +
                    "  \"linkMan\": \"qweqwe\",\n" +
                    "  \"mobile\": \"14499983000\",\n" +
                    "  \"operateStatus\": 1,\n" +
                    "  \"realName\": \"unitTestt1\",\n" +
                    "  \"userName\": \"cscsx\",\n" +
                    "  \"userType\": 1\n" +
                    "}", DistributorAuditDTO.class);
            CustomerSyncHandler.getHandler(CustomerSyncHandler.SyncHandlersEnum.Distributor_Audit).handle(param);
            CrmSiteMember crmSiteMember = CrmSiteMember.dao.findFirst("select * from 72crm_crm_site_member where site_member_id = ?", userId);
            Assert.assertNotNull(crmSiteMember);
            Assert.assertEquals(Integer.valueOf(CrmDistributorEnum.IS_DISTRIBUTOR.getTypes()), crmSiteMember.getIsDistributor());
            CrmCustomer customer = CrmCustomer.dao.findFirst("select * from 72crm_crm_customer where customer_no = ?", crmSiteMember.getCustId());
            Assert.assertNotNull(customer);
            return false;
        });
    }


    @Test
    public void distributorAudit2() {
        Db.tx(() -> {
            long userId = 9999991L;
            String customerNo = "UUUU";
            CrmCustomer crmCustomer = new CrmCustomer();
            crmCustomer.setCustomerName("UnitTests1");
            crmCustomer.setOwnerUserId(1992);
            crmCustomer.setOwnerTime(new Date());
            crmCustomer.setOwnerUserName("wuhh");
            crmCustomer.setCustomerNo(customerNo);
            crmCustomer.setBatchId("--11");
            crmCustomer.save();
            CrmSiteMember siteMember1 = new CrmSiteMember();
            siteMember1.setSiteMemberId(userId);
            siteMember1.setRealName("unitTest");
            siteMember1.setCustId(customerNo);
            siteMember1.save();
            CrmSiteMember siteMember2 = new CrmSiteMember();
            siteMember2.setSiteMemberId(userId-10);
            siteMember2.setRealName("unitTest");
            siteMember2.setCustId(customerNo);
            siteMember2.save();
            CrmDistributorPromotionRelation relation = new CrmDistributorPromotionRelation();
            relation.setUid(userId);
            relation.setPUid(userId - 2);
            relation.save();
            DistributorAuditDTO param = JSONObject.parseObject("{\n" +
                    "  \"addressAreaCode\": \"120103\",\n" +
                    "  \"addressAreaName\": \"天津市市辖区河西区\",\n" +
                    "  \"addressDetail\": \"测试测试\",\n" +
                    "  \"applyTime\": 1587889274000,\n" +
                    "  \"areaCode\": \"150000\",\n" +
                    "  \"areaName\": \"内蒙古自治区\",\n" +
                    "  \"auditSuccessTime\": 1587889551000,\n" +
                    "  \"bdDept\": \"48284112\",\n" +
                    "  \"bdUserEmail\": \"1957345722-1907849355@wz-inc.com\",\n" +
                    "  \"bdUserName\": \"Person\",\n" +
                    "  \"businessLicenceBegin\": 1587830400000,\n" +
                    "  \"businessLicenceEnd\": 1682524799000,\n" +
                    "  \"businessLicenceImgUrl\": \"https://dev-venus-pri.oss-cn-beijing.aliyuncs.com/license/878/537/1405e277a1be74f3e8c60b879c3ce061.png\",\n" +
                    "  \"businessLicenceNum\": \"444433333266666234\",\n" +
                    "  \"contactAddress\": \"天津市市辖区河西区 测试测试\",\n" +
                    "  \"contactEmail\": \"\",\n" +
                    "  \"contactNumber\": \"14499983000\",\n" +
                    "  \"distributorStatus\": 3,\n" +
                    "  \"email\": \"445335@qq.com\",\n" +
                    "  \"gmtCreate\": 1587889274000,\n" +
                    "  \"id\": " + userId + ",\n" +
                    "  \"isDistributor\": 1,\n" +
                    "  \"level\": \"platinum\",\n" +
                    "  \"linkMan\": \"qweqwe\",\n" +
                    "  \"mobile\": \"14499983000\",\n" +
                    "  \"operateStatus\": 1,\n" +
                    "  \"realName\": \"unitTestt1\",\n" +
                    "  \"userName\": \"cscsx\",\n" +
                    "  \"userType\": 1\n" +
                    "}", DistributorAuditDTO.class);
            CustomerSyncHandler.getHandler(CustomerSyncHandler.SyncHandlersEnum.Distributor_Audit).handle(param);
            CrmSiteMember crmSiteMember = CrmSiteMember.dao.findFirst("select * from 72crm_crm_site_member where site_member_id = ?", userId);
            Assert.assertNotNull(crmSiteMember);
            Assert.assertEquals(Integer.valueOf(CrmDistributorEnum.IS_DISTRIBUTOR.getTypes()), crmSiteMember.getIsDistributor());
            CrmCustomer customer = CrmCustomer.dao.findFirst("select * from 72crm_crm_customer where customer_no = ?", crmSiteMember.getCustId());
            Assert.assertNotNull(customer);
            return false;
        });
    }

    @Test
    public void distributorAudit3() {
        Db.tx(() -> {
            long userId = 9999991L;
            String customerNo = "UUUU";
            CrmCustomer crmCustomer = new CrmCustomer();
            crmCustomer.setCustomerName("UnitTests1");
            crmCustomer.setOwnerUserId(1992);
            crmCustomer.setOwnerTime(new Date());
            crmCustomer.setCustomerType("153");
            crmCustomer.setOwnerUserName("wuhh");
            crmCustomer.setCustomerNo(customerNo);
            crmCustomer.setBatchId("--11");
            crmCustomer.save();
            CrmCustomerExt ext = new CrmCustomerExt();
            ext.setCustomerId(crmCustomer.getCustomerId().intValue());
            ext.setStorageType(CustomerStorageTypeEnum.INSPECT_CAP.getCode());
            ext.save();
            CrmSiteMember siteMember1 = new CrmSiteMember();
            siteMember1.setSiteMemberId(userId);
            siteMember1.setRealName("unitTest");
            siteMember1.setCustId(customerNo);
            siteMember1.save();
            CrmSiteMember siteMember2 = new CrmSiteMember();
            siteMember2.setSiteMemberId(userId-10);
            siteMember2.setRealName("unitTest");
            siteMember2.setCustId(customerNo);
            siteMember2.setIsDistributor(Integer.valueOf(CrmDistributorEnum.IS_DISTRIBUTOR.getTypes()));
            siteMember2.save();
            CrmDistributorPromotionRelation relation = new CrmDistributorPromotionRelation();
            relation.setUid(userId);
            relation.setPUid(userId - 2);
            relation.save();
            DistributorAuditDTO param = JSONObject.parseObject("{\n" +
                    "  \"addressAreaCode\": \"120103\",\n" +
                    "  \"addressAreaName\": \"天津市市辖区河西区\",\n" +
                    "  \"addressDetail\": \"测试测试\",\n" +
                    "  \"applyTime\": 1587889274000,\n" +
                    "  \"areaCode\": \"150000\",\n" +
                    "  \"areaName\": \"内蒙古自治区\",\n" +
                    "  \"auditSuccessTime\": 1587889551000,\n" +
                    "  \"bdDept\": \"48284112\",\n" +
                    "  \"bdUserEmail\": \"1957345722-1907849355@wz-inc.com\",\n" +
                    "  \"bdUserName\": \"Person\",\n" +
                    "  \"businessLicenceBegin\": 1587830400000,\n" +
                    "  \"businessLicenceEnd\": 1682524799000,\n" +
                    "  \"businessLicenceImgUrl\": \"https://dev-venus-pri.oss-cn-beijing.aliyuncs.com/license/878/537/1405e277a1be74f3e8c60b879c3ce061.png\",\n" +
                    "  \"businessLicenceNum\": \"444433333266666234\",\n" +
                    "  \"contactAddress\": \"天津市市辖区河西区 测试测试\",\n" +
                    "  \"contactEmail\": \"\",\n" +
                    "  \"contactNumber\": \"14499983000\",\n" +
                    "  \"distributorStatus\": 3,\n" +
                    "  \"email\": \"445335@qq.com\",\n" +
                    "  \"gmtCreate\": 1587889274000,\n" +
                    "  \"id\": " + userId + ",\n" +
                    "  \"isDistributor\": 1,\n" +
                    "  \"level\": \"platinum\",\n" +
                    "  \"linkMan\": \"qweqwe\",\n" +
                    "  \"mobile\": \"14499983000\",\n" +
                    "  \"operateStatus\": 1,\n" +
                    "  \"realName\": \"unitTestt1\",\n" +
                    "  \"userName\": \"cscsx\",\n" +
                    "  \"userType\": 1\n" +
                    "}", DistributorAuditDTO.class);
            CustomerSyncHandler.getHandler(CustomerSyncHandler.SyncHandlersEnum.Distributor_Audit).handle(param);
            CrmSiteMember crmSiteMember = CrmSiteMember.dao.findFirst("select * from 72crm_crm_site_member where site_member_id = ?", userId);
            Assert.assertNotNull(crmSiteMember);
            Assert.assertEquals(Integer.valueOf(CrmDistributorEnum.IS_DISTRIBUTOR.getTypes()), crmSiteMember.getIsDistributor());
            CrmCustomer customer = CrmCustomer.dao.findFirst("select * from 72crm_crm_customer where customer_no = ?", crmSiteMember.getCustId());
            Assert.assertNotNull(customer);
            Assert.assertNotEquals(customer.getCustomerId(), crmCustomer.getCustomerId());
            return false;
        });
    }
}