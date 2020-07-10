package com.kakarote.crm9.erp.crm.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.jfinal.aop.Aop;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.upload.UploadFile;
import com.kakarote.crm9.BaseTest;
import com.kakarote.crm9.common.config.paragetter.BasePageRequest;
import com.kakarote.crm9.erp.admin.entity.AdminRecord;
import com.kakarote.crm9.erp.crm.constant.CrmConstant;
import com.kakarote.crm9.erp.crm.entity.CrmContacts;
import com.kakarote.crm9.erp.crm.entity.CrmContactsBusiness;
import com.kakarote.crm9.utils.OssPrivateFileUtil;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * 联系人TC
 */
public class CrmContactsServiceTest extends BaseTest {

    private CrmContactsService contactsService = Aop.get(CrmContactsService.class);
    private CrmNotesService notesService = Aop.get(CrmNotesService.class);

    @Autowired
    private OssPrivateFileUtil ossPrivateFileUtil;

    @Test
    public void add() {
        //新增
        JSONObject hjb = new JSONObject();
        JSONObject jb = new JSONObject();
        jb.put("name","ck_新增");//联系人名称
        jb.put("customerId","479");//客户id
        jb.put("post","开发");//职位
        jb.put("mobile","12312312222");//手机
        jb.put("email","111@qq.com");//邮箱
        jb.put("telephone","1323");//办公电脑
        jb.put("wechat","微信111");
        jb.put("role","2");
        jb.put("attitude","非常支持");
        jb.put("hobby","爱好111");
        jb.put("remark","备注111");
        hjb.put("entity",jb);

        contactsService.addOrUpdate(hjb, 931L);
    }



    /**
     * 添加联系小计
     */
    @Test
    public void addRecord() {
        JSONObject json = new JSONObject();

        AdminRecord adminRecord = new AdminRecord();
        adminRecord.setTypesId(455);
        adminRecord.setContent("测试小计222");
        notesService.addRecord(adminRecord, CrmConstant.CRM_CONTACTS);
    }

    /**
     * 获取联系小计
     */
    @Test
    public void getRecord() {
        CrmContacts con = new CrmContacts();
        con.setContactsId(455L);
        BasePageRequest<CrmContacts> basePageRequest = new BasePageRequest<>(1, 15, con);
        Page<Record> record = notesService.getRecord(basePageRequest, ossPrivateFileUtil,CrmConstant.CRM_CONTACTS);
        System.out.println(record.getList());
    }
    /**
     * 查看基本信息
     */
    @Test
    public void information() {
        List<Record> information = contactsService.information(455);
        System.out.println(information);

    }


    /**
     * 根据ID查询联系人
     */
    @Test
    public void queryById() {
        Record record = contactsService.queryById(455);
        System.out.println(record);

    }

    @Test
    public void getAuthorizedContactsList() {
        try {
            List<Integer> recordList = contactsService.getAuthorizedContactsList(mockCrmUser("0575").getAuthorizedUserIds());
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    @Test
    public void queryList() {
        CrmContacts crmContacts = new CrmContacts();
        crmContacts.setMobile("135641299992");
        BasePageRequest<CrmContacts> basePageRequest = new BasePageRequest<>(1,10,crmContacts);
        contactsService.queryList(basePageRequest);
    }

    @Test
    public void queryByName() {
        contactsService.queryByName("test");
    }

    @Test
    public void queryBusiness() {
        CrmContacts crmContacts = new CrmContacts();
        crmContacts.setContactsId(1213L);
        BasePageRequest<CrmContacts> basePageRequest = new BasePageRequest<>(1,10,crmContacts);
        basePageRequest.setPageType(0);
        contactsService.queryBusiness(basePageRequest);

        basePageRequest.setPageType(1);
        contactsService.queryBusiness(basePageRequest);
    }

    @Test
    public void relateBusiness() {
        try {
            CrmContactsBusiness crmContactsBusiness = CrmContactsBusiness.dao.findById(1);
            contactsService.relateBusiness(crmContactsBusiness);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    @Test
    public void unrelateBusiness() {
        contactsService.unrelateBusiness(1);
    }

    @Test
    public void addOrUpdate() {
        JSONObject jsonObject = JSON.parseObject("{\"entity\":{\"name\":\"honglei wan\",\"customerId\":967618,\"post\":\"\",\"mobile\":\"32135453121354\",\"email\":\"\",\"telephone\":\"051789992222\",\"wechat\":\"1212\",\"attitude\":\"\",\"role\":\"134\",\"hobby\":\"\",\"remark\":\"\",\"contactsId\":3068,\"batchId\":\"0bbbde6e3a684af69abc2ee74020a88f\"},\"field\":[]}");
        contactsService.addOrUpdate(jsonObject,2736L);
    }

    @Test
    public void updateLeadsStatus() {
        CrmContacts crmContacts = new CrmContacts();
        crmContacts.setMobile("13564130092");
        crmContacts.setCustomerId(53);
        contactsService.updateLeadsStatus(crmContacts);
    }

    @Test
    public void deleteByIds() {
        contactsService.deleteByIds("1,3,4");
    }

    @Test
    public void transfer() {
        CrmContacts crmContacts = new CrmContacts();
        crmContacts.setContactsIds("1,2,3");
        contactsService.transfer(crmContacts);
    }

    @Test
    public void updateOwnerUserId() {
        contactsService.updateOwnerUserId(53,2736);
    }

    @Test
    public void queryField() {
        contactsService.queryField();
    }

    @Test
    public void testQueryField() {
        contactsService.queryField(441);
    }

    @Test
    public void exportContacts() {
        contactsService.exportContacts("441");
    }

    @Test
    public void getCheckingField() {
        contactsService.getCheckingField();
    }

    @Test
    public void uploadExcel() {
        try {
            UploadFile file = new UploadFile("file","/home/admin/logs/app","crm.log","crm.log","");
            contactsService.uploadExcel(file,2736L);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    @Test
    public void findByCustIdAndMobile() {
        contactsService.findByCustIdAndMobile("1212","135641300020");
    }

    @Test
    public void getWechatByContactsId() {
        contactsService.getWechatByContactsId("1212");
    }

    @Test
    public void getTelephoneByContactsId() {
        contactsService.getTelephoneByContactsId("1212");
    }

    @Test
    public void getMobileByContactsId() {
        contactsService.getMobileByContactsId("1212");
    }

    @Test
    public void getEmailByContactsId() {
        contactsService.getEmailByContactsId("1212");
    }

    @Test
    public void checkDistributor() {
        try {
            contactsService.checkDistributor("13564130092");
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
