package com.kakarote.crm9.erp.admin.controller;

import com.kakarote.crm9.erp.crm.acl.user.CrmUser;
import com.kakarote.crm9.erp.crm.constant.CrmConstant;
import org.springframework.beans.factory.annotation.Autowired;

import com.jfinal.aop.Before;
import com.jfinal.aop.Inject;
import com.jfinal.core.Controller;
import com.jfinal.upload.UploadFile;
import com.kakarote.crm9.common.spring.IocInterceptor;
import com.kakarote.crm9.erp.admin.entity.AdminFile;
import com.kakarote.crm9.erp.admin.service.AdminFileService;
import com.kakarote.crm9.utils.BaseUtil;
import com.kakarote.crm9.utils.OssPrivateFileUtil;
import com.kakarote.crm9.utils.R;

import java.util.Objects;

@Before(IocInterceptor.class)
public class AdminFileController extends Controller {

    @Inject
    private AdminFileService adminFileService;

    @Autowired
    private OssPrivateFileUtil ossPrivateFileUtil;

    public void index(){
        renderJson(R.ok());
    }

    /**
     * @author zhangzhiwei
     * 上传附件
     *
     */
    public void upload(){
        String prefix=BaseUtil.getDate();
        UploadFile file = getFile("file",prefix);
        CrmUser crmUser = BaseUtil.getCrmUser();
        String ossUrl = ossPrivateFileUtil.uploadToOss(file, Objects.nonNull(crmUser) ? crmUser.getCrmAdminUser().getNum() : null);

        if (ossUrl != null) {
            renderJson(adminFileService.upload(file, getPara(CrmConstant.BATCH_ID), getPara("type"), ossUrl, ossPrivateFileUtil,crmUser));
        } else {
            renderJson(R.error("upload file failed!"));
        }
    }

    public void uploadCustomerOrg() {
        String prefix=BaseUtil.getDate();
        UploadFile file = getFile("file",prefix);
        String ossUrl = ossPrivateFileUtil.uploadToOss(file, BaseUtil.getUser().getNum());
        if (ossUrl != null) {
            renderJson(adminFileService.uploadCustomerOrg(file, getPara("custId"), ossUrl, ossPrivateFileUtil));
        } else {
            renderJson(R.error("upload file failed!"));
        }
    }

    /**
     * @author zhangzhiwei
     * 通过批次ID查询
     */
    public void queryByBatchId(){
        renderJson(R.ok().put("data", adminFileService.queryByBatchId(getPara(CrmConstant.BATCH_ID), ossPrivateFileUtil)));
    }

    /**
     * @author zhangzhiwei
     * 通过ID查询
     */
    public void queryById(){
        renderJson(adminFileService.queryById(getPara("id"), ossPrivateFileUtil));
    }

    /**
     * @author zhangzhiwei
     * 通过ID删除
     */
    public void removeById(){
        renderJson(adminFileService.removeById(getPara("id"), getPara("bizId"), getPara("type"), ossPrivateFileUtil,BaseUtil.getCrmUser().getCrmAdminUser()));
    }

    /**
     * @author zhangzhiwei
     * 通过批次ID删除
     */
    public void removeByBatchId(){
        adminFileService.removeByBatchId(getPara(CrmConstant.BATCH_ID), ossPrivateFileUtil,BaseUtil.getUser().getRealname());
        renderJson(R.ok());
    }

    /**
     * @author zhangzhiwei
     * 重命名文件
     */
    public void renameFileById(){
        AdminFile file=new AdminFile();
        file.setFileId(getInt("fileId").longValue());
        file.setName(getPara("name"));
        renderJson(adminFileService.renameFileById(file)?R.ok():R.error());
    }
}
