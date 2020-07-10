package com.kakarote.crm9.erp.crm.service;

import com.alibaba.fastjson.JSONObject;
import com.jfinal.aop.Aop;
import com.jfinal.kit.JsonKit;
import com.jfinal.log.Log;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.kakarote.crm9.BaseTest;
import com.kakarote.crm9.common.config.paragetter.BasePageRequest;
import com.kakarote.crm9.common.midway.NotifyService;
import com.kakarote.crm9.erp.admin.entity.AdminUser;
import com.kakarote.crm9.erp.crm.dto.CrmCustomerPageRequest;
import com.kakarote.crm9.erp.crm.dto.CrmNotesPageRequest;
import com.kakarote.crm9.utils.OssPrivateFileUtil;
import com.kakarote.crm9.utils.R;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class CrmNotesServiceTest extends BaseTest{

    private CrmNotesService notesService = Aop.get(CrmNotesService.class);
    private NotifyService notifyService = Aop.get(NotifyService.class);

    @Autowired
    private OssPrivateFileUtil ossPrivateFileUtil;

    private Log logger = Log.getLog(getClass());

    @Test
    public void saveAndUpdate() {
        //新增
        JSONObject obj_add = new JSONObject();
        obj_add.put("content","通过电话跟进客户");
        obj_add.put("batchId","78186f1a8a611105628b253d96323111");
        obj_add.put("sendUserIds","929,837");
        obj_add.put("contactsIds","");
        obj_add.put("customerIds","");
        obj_add.put("businessIds","106");
        obj_add.put("contractIds","");
        obj_add.put("leadsIds","");
        Assert.assertNotNull(notesService.addOrUpdate(obj_add, 931L));

    }

    @Test
    public void update(){
        //编辑
        JSONObject obj_edit = new JSONObject();
        Record admin_record = Db.findFirst("select record_id,batch_id from 72crm_admin_record where batch_id = ?","78186f1a8a611105628b253d96323111");

        obj_edit.put("recordId",admin_record.get("record_id"));
        obj_edit.put("content","上门拜访客户");
        obj_edit.put("batchId",admin_record.get("batch_id"));
        obj_edit.put("sendUserIds","929,837");
        obj_edit.put("contactsIds","");
        obj_edit.put("customerIds","");
        obj_edit.put("businessIds","106");
        obj_edit.put("contractIds","");
        obj_edit.put("leadsIds","");
        Assert.assertNotNull(notesService.addOrUpdate(obj_edit, 931L));
    }


    @Test
    public void deleteById() {
        AdminUser user = new AdminUser();
        user.setUserId(931L);
        user.setRealname("丁朝坤");
        notesService.deleteById(260, ossPrivateFileUtil,user);
    }

    @Test
    public void addOrUpdate() {
        //新增
        JSONObject obj_add = new JSONObject();
        obj_add.put("content","通过电话跟进客户");
        obj_add.put("batchId","78186f1a8a611105628b253d96323111");
        obj_add.put("sendUserIds","929,837");
        obj_add.put("contactsIds","");
        obj_add.put("customerIds","");
        obj_add.put("businessIds","106");
        obj_add.put("contractIds","");
        obj_add.put("leadsIds","");
        R result = notesService.addOrUpdate(obj_add, 931L);
        Assert.assertTrue(result.isSuccess());
    }

    @Test
    public void getNotesList() {
        try {
            Page<Record> recordList = notesService.getNotesList(crmNotesPageRequest(),mockCrmUser("0575"));
            Assert.assertTrue(recordList.getList().size() > 0);
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }

    private BasePageRequest<CrmNotesPageRequest> crmNotesPageRequest() {
        CrmCustomerPageRequest request = new CrmCustomerPageRequest();
        request.setBizType(6);
        request.setSceneId(0);
        return new BasePageRequest<>(JsonKit.toJson(request), CrmNotesPageRequest.class);
    }

    @Test
    public void getAuthorizedNotesList() {
        try {
            List<Integer> recordList = notesService.getAuthorizedNotesList(mockCrmUser("0575").getAuthorizedUserIds(),mockCrmUser("0575").getCrmAdminUser().getUserId());
            Assert.assertTrue(recordList.size() > 0);
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }
}
