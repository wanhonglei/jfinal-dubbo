package com.kakarote.crm9.erp.admin.service;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.jfinal.aop.Before;
import com.jfinal.aop.Inject;
import com.jfinal.kit.JsonKit;
import com.jfinal.log.Log;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.jfinal.upload.UploadFile;
import com.kakarote.crm9.erp.admin.entity.AdminFile;
import com.kakarote.crm9.erp.admin.entity.AdminUser;
import com.kakarote.crm9.erp.crm.acl.user.CrmUser;
import com.kakarote.crm9.erp.crm.service.CrmCustomerService;
import com.kakarote.crm9.erp.crm.service.CrmRecordService;
import com.kakarote.crm9.utils.OssPrivateFileUtil;
import com.kakarote.crm9.utils.R;
import org.apache.commons.collections4.CollectionUtils;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

public class AdminFileService {

    private Log logger = Log.getLog(getClass());

    @Inject
    private CrmCustomerService crmCustomerService;

    @Inject
    private CrmRecordService crmRecordService;

    /**
     * 上传文件
     * @author yue.li
     * @param file    文件
     * @param batchId 批次ID
     * @param fileType 文件类型
     * @param ossUrl ossUrl
     * @param ossPrivateFileUtil ossPrivateFileUtil
     * @param crmUser 登录对象
     */
    public R upload(UploadFile file, String batchId, String fileType, String ossUrl, OssPrivateFileUtil ossPrivateFileUtil, CrmUser crmUser) {
        if (batchId == null || batchId.isEmpty()) {
            batchId = IdUtil.simpleUUID();
        }

        AdminFile adminFile = new AdminFile();
        adminFile.setBatchId(batchId);
        adminFile.setCreateTime(new Date());
        adminFile.setCreateUserId(Objects.nonNull(crmUser) ? crmUser.getCrmAdminUser().getUserId().intValue() : null);
        adminFile.setPath(ossUrl);
        adminFile.setFilePath(ossUrl);
        adminFile.setName(file.getFileName());
        if (StrUtil.isNotBlank(fileType)) {
            adminFile.setFileType(fileType);
        }

        File file1 = file.getFile();

        adminFile.setSize((int) file1.length());

        // delete temp file on application server
        file1.delete();

        return adminFile.save() ? R.ok().put("batchId", batchId).put("name", file.getFileName()).put("url", ossPrivateFileUtil.presignedURL(adminFile.getFilePath())).put("size", file1.length() / 1000 + "KB").put("file_id", adminFile.getFileId()).put("ossUrl",ossUrl) : R.error();
    }

    public R uploadCustomerOrg(UploadFile file, String custId, String ossUrl, OssPrivateFileUtil ossPrivateFileUtil) {
        // delete temp file on application server
        file.getFile().delete();

        return crmCustomerService.updateCustomerOrgImgUrlById(custId, ossUrl) ? R.ok().put("url", ossPrivateFileUtil.presignedURL(ossUrl)) : R.error();
    }

    /**
     * 通过批次ID查询
     *
     * @param batchId 批次ID
     */
    public void queryByBatchId(String batchId, Record record, OssPrivateFileUtil ossPrivateFileUtil) {
        if (batchId == null || "".equals(batchId)) {
            record.set("img", new ArrayList<>()).set("file", new ArrayList<>());
            return;
        }
        List<AdminFile> adminFiles = AdminFile.dao.find(Db.getSql("admin.file.queryByBatchId"), batchId);
        convertFilePath(adminFiles, ossPrivateFileUtil);

        if (CollectionUtils.isNotEmpty(adminFiles)) {
            Map<String, List<AdminFile>> collect = adminFiles.stream().collect(Collectors.groupingBy(AdminFile::getFileType));
            collect.forEach(record::set);
            if (!record.getColumns().containsKey("img") || record.get("img") == null) {
                record.set("img", new ArrayList<>());
            }
            if (!record.getColumns().containsKey("file") || record.get("file") == null) {
                record.set("file", new ArrayList<>());
            }
        } else{
            record.set("img", new ArrayList<>()).set("file", new ArrayList<>());
        }
    }

    /**
     * 根据BatchId查询文件列表
     * @param batchId
     * @param ossPrivateFileUtil
     * @return
     */
    public List<AdminFile> queryByBatchId(String batchId, OssPrivateFileUtil ossPrivateFileUtil) {
        if (batchId == null) {
            return new ArrayList<>();
        }
        List<AdminFile> adminFiles = AdminFile.dao.find(Db.getSql("admin.file.queryByBatchId"), batchId);
        convertFilePath(adminFiles, ossPrivateFileUtil);
        return adminFiles;
    }

    private void convertFilePath(List<AdminFile> adminFiles, OssPrivateFileUtil ossPrivateFileUtil) {
        if (adminFiles != null && adminFiles.size() > 0) {
            adminFiles.forEach(item -> convertFilePath(item, ossPrivateFileUtil));
        }
    }

    private AdminFile convertFilePath(AdminFile item, OssPrivateFileUtil ossPrivateFileUtil) {
        if (item == null || item.getFilePath().isEmpty()) {
            return item;
        }
        String filePath = item.getFilePath();
        item.setFilePath(ossPrivateFileUtil.presignedURL(filePath));
        return item;
    }

    /**
     * 通过ID查询
     *
     * @param id 文件ID
     */
    public R queryById(String id, OssPrivateFileUtil ossPrivateFileUtil) {
        if (id == null) {
            return R.error("id参数为空");
        }
        AdminFile file = AdminFile.dao.findById(id);
        return R.ok().put("data", convertFilePath(file, ossPrivateFileUtil));
    }

    /**
     * 通过ID删除
     *
     * @param id 文件ID
     */
    @Before(Tx.class)
    public R removeById(String id, String bizId, String type, OssPrivateFileUtil ossPrivateFileUtil, AdminUser adminUser) {
        if (id == null) {
            return R.error("id参数为空");
        }
        logger.info(String.format("file id: %s, bizId: %s, type: %s", id, bizId, type));
        AdminFile adminFile = AdminFile.dao.findById(id);
        if (adminFile != null && adminFile.getPath() != null && !adminFile.getPath().isEmpty()) {
            ossPrivateFileUtil.removeFileFromOss(adminFile.getPath());
            adminFile.delete();
            logger.info(String.format("%s remove file: %s", adminUser.getRealname(), adminFile.getPath()));
            if (bizId != null && type != null) {
                crmRecordService.addDeleteAttachmentRecord(Integer.valueOf(bizId), type, adminFile.getName(), adminUser.getUserId());
            }
        }
        return R.ok();
    }

    /**
     * 通过批次ID删除
     *
     * @param batchId 批次ID
     */
    public void removeByBatchId(String batchId, OssPrivateFileUtil ossPrivateFileUtil,String realName) {
        if (StrUtil.isEmpty(batchId)) {
            return;
        }
        List<String> paths = Db.query(Db.getSql("admin.file.queryPathByBatchId"), batchId);

        paths.forEach(ossPrivateFileUtil::removeFileFromOss);
        Db.deleteById("72crm_admin_file", "batch_id", batchId);

        logger.info(String.format("%s remove file: %s", realName, JsonKit.toJson(paths)));
    }

    public boolean renameFileById(AdminFile file) {
        return Db.update("72crm_admin_file", "file_id", file.toRecord());
    }

    /**
     * 更新文件的批次号
     * @param fileId
     * @param batchId
     */
    public void updateBatchIdById(Long fileId, String batchId) {
        Db.update(Db.getSql("admin.file.updateBatchIdById"), batchId, fileId);
    }

    /**
     * 根据批次号ID查询所有文件ID
     * @param batchId
     * @return
     */
    public List<Long> queryIdsByBatchId(String batchId) {
        return AdminFile.dao.find(Db.getSql("admin.file.queryFileByBatchId"),batchId).stream().map(AdminFile::getFileId).collect(Collectors.toList());
    }

    public String queryNameByFileId(Long fileId) {
        if (Objects.nonNull(fileId)) {
            return Db.queryStr(Db.getSql("admin.file.queryNameByFileId"), fileId);
        }
        return null;
    }
}
