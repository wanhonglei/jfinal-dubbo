package com.kakarote.crm9.mobile.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.jfinal.aop.Before;
import com.jfinal.aop.Inject;
import com.jfinal.core.Controller;
import com.jfinal.upload.UploadFile;
import com.kakarote.crm9.common.spring.IocInterceptor;
import com.kakarote.crm9.erp.admin.entity.AdminFile;
import com.kakarote.crm9.erp.admin.entity.AdminFileResult;
import com.kakarote.crm9.erp.admin.service.AdminFileService;
import com.kakarote.crm9.erp.crm.acl.user.CrmUser;
import com.kakarote.crm9.erp.crm.constant.CrmConstant;
import com.kakarote.crm9.mobile.common.MobileUtil;
import com.kakarote.crm9.utils.BaseUtil;
import com.kakarote.crm9.utils.OssPrivateFileUtil;
import com.kakarote.crm9.utils.R;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Objects;

/**
 * MobileFileController
 *
 * @author yue.li
 * @since 20120/01/07 14:08
 */
@Before(IocInterceptor.class)
public class MobileFileController extends Controller {

    @Inject
    private AdminFileService adminFileService;

    @Autowired
    private OssPrivateFileUtil ossPrivateFileUtil;

    /**
     * 上传图片
     * @author yue.li
     */
    public void upload(){
        String nowTime = BaseUtil.getDate();
        UploadFile file = getFile("file",nowTime);
        CrmUser crmUser = MobileUtil.getCrmUser(getRequest());
        if(Objects.isNull(crmUser)){
            return;
        }
        String ossUrl = ossPrivateFileUtil.uploadToOss(file, crmUser.getCrmAdminUser().getNum());
        if (ossUrl != null) {
            R result = adminFileService.upload(file, getPara("batchId"), getPara("type"), ossUrl, ossPrivateFileUtil,crmUser);
            if(result.isSuccess()){
                renderJson(R.ok().put("data",constructAdminFileResult(result)));
            }else{
                renderJson(R.error("upload file failed!"));
            }
        } else {
            renderJson(R.error("upload file failed!"));
        }
    }

    /**
     * 构造上传文件返回结果
     * @author yue.li
     * @param result PC端返回结果
     */
    private AdminFileResult constructAdminFileResult(R result) {
        Object batchId = result.get("batchId");
        Object name = result.get("name");
        Object url = result.get("url");
        Object fileId = result.get("file_id");
        Object ossUrl = result.get("ossUrl");
        AdminFileResult adminFileResult = new AdminFileResult();
        adminFileResult.setBatchId(Objects.nonNull(batchId) ? batchId.toString(): null);
        adminFileResult.setName(Objects.nonNull(name) ? name.toString(): null);
        adminFileResult.setFilePath(Objects.nonNull(url) ? url.toString(): null);
        adminFileResult.setFileId(Objects.nonNull(fileId) ? Integer.valueOf(fileId.toString()): null);
        adminFileResult.setOssUrl(Objects.nonNull(ossUrl) ? ossUrl.toString(): null);
        return adminFileResult;
    }

    /**
     * 删除上传附件
     * @author yue.li
     */
    public void delete(){
        CrmUser crmUser = MobileUtil.getCrmUser(getRequest());
        if(Objects.isNull(crmUser)){
            renderJson(R.error(CrmConstant.NO_CRM_PERMISSION));
        }else{
            JSONObject jsonObject = JSON.parseObject(getRawData());
            AdminFile adminFile = jsonObject.toJavaObject(AdminFile.class);
            if(Objects.nonNull(adminFile.getFileId())) {
                renderJson(adminFileService.removeById(String.valueOf(adminFile.getFileId()), null, null, ossPrivateFileUtil,crmUser.getCrmAdminUser()));
            }else{
                renderJson(R.error("fileId is not empty!"));
            }
        }

    }
}
