package com.kakarote.crm9.integration.controller;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.jfinal.aop.Before;
import com.jfinal.aop.Inject;
import com.jfinal.core.Controller;
import com.jfinal.kit.HttpKit;
import com.jfinal.log.Log;
import com.jfinal.plugin.activerecord.Record;
import com.kakarote.crm9.common.midway.NotifyService;
import com.kakarote.crm9.common.spring.IocInterceptor;
import com.kakarote.crm9.erp.admin.entity.AdminDept;
import com.kakarote.crm9.erp.admin.entity.AdminUser;
import com.kakarote.crm9.erp.admin.service.AdminDeptService;
import com.kakarote.crm9.erp.admin.service.AdminSendEmailService;
import com.kakarote.crm9.erp.crm.constant.CrmConstant;
import com.kakarote.crm9.erp.crm.cron.CrmBaseDataCron;
import com.kakarote.crm9.erp.crm.entity.CrmDeptChangeLog;
import com.kakarote.crm9.erp.crm.service.CrmChangeLogService;
import com.kakarote.crm9.integration.common.EsbConfig;
import com.kakarote.crm9.integration.entity.OaDepartment;
import com.kakarote.crm9.integration.entity.OaDepartments;
import com.kakarote.crm9.utils.BaseUtil;
import com.kakarote.crm9.utils.R;
import com.mysql.jdbc.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Department sync controller.
 *
 * @author hao.fu
 * @create 2019/6/26 14:43
 */
@Before(IocInterceptor.class)
public class SyncDepartmentController extends Controller {

    private Log logger = Log.getLog(getClass());

    @Inject
    private AdminDeptService adminDeptService;

    @Autowired
    private EsbConfig esbConfig;

    @Inject
    private AdminSendEmailService adminSendEmailService;

    @Autowired
    private NotifyService notifyService;

    @Inject
    private CrmChangeLogService crmChangeLogService;

    @Inject
    private CrmBaseDataCron crmBaseDataCron;

    /**
     * Sync all departments.
     */
    public void syncAll() {
        long start = System.currentTimeMillis();
        AdminUser user = BaseUtil.getUser();
        logger.info(Objects.isNull(user) ? "" : user.getRealname() + "sync all depts: " + LocalDate.now());
        String rsps = HttpKit.get(esbConfig.getSyncDeptUrl(), null, esbConfig.getSyncDeptHeader());
        logger.info(rsps);

        handleResult(rsps);

        long end = System.currentTimeMillis();
        logger.info("sync all depts cost:" + (end - start)/1000 + "s");
        renderJson(R.ok());
    }

    private void handleResult(String rsps) {
        try{
            if (StringUtils.isNullOrEmpty(rsps)) {
                logger.error("get empty result when sync all depts at " + LocalDate.now());
            } else {
                OaDepartments result = JSON.parseObject(rsps, OaDepartments.class);
                if (result.getCode().equals(CrmConstant.SUCCESS_CODE)) {
                    List<OaDepartment> detps = result.getData();
                    handleAllSync(detps);
                } else {
                    logger.error("get result code " + result.getCode() + "when sync all detps at " + LocalDate.now());
                }
            }
        }catch(Exception e){
            /**发送失败消息通知*/
            logger.error("handleResult exception " + e);
            adminSendEmailService.sendErrorMessage(e,notifyService);
        }
    }

    /**
     * Handle all dept sync.
     *
     * @param depts
     */
    private void handleAllSync(List<OaDepartment> depts) {
        List<Record> recordsInDb = adminDeptService.getAllDeptIds();
        List<String> idsInDb;
        if (recordsInDb != null && recordsInDb.size() > 0) {
            // ids in db
            idsInDb = recordsInDb.stream().filter(item-> !item.getLong("dept_id").equals(CrmConstant.ROOT_DEPARTMENT_ID)).map(item -> item.getLong("dept_id").toString()).collect(Collectors.toList());
            logger.info("department sync on " + LocalDate.now() + ", dept ids in CRM DB, size: " + idsInDb.size() + "\nvalues:" + String.join(",", idsInDb));

            // ids from oa
            List<String> idsFromOa = depts.stream().map(item -> item.getDeptId().trim()).collect(Collectors.toList());
            logger.info("department sync on " + LocalDate.now() + ", dept ids from OA, size: " + idsFromOa.size() + "\nvalues:" + String.join(",", idsInDb));


            // update delete flag for records only exist in DB
            List<String> idsOnlyExistInDb = idsInDb.stream().filter(item -> !idsFromOa.contains(item)).collect(Collectors.toList());
            idsOnlyExistInDb.forEach(item-> System.out.println("records only exist in DB:" + item));
            //updateDeleteFlag(idsOnlyExistInDb);

            // insert record which only exist in OA
            List<String> idsOnlyExistInOa = idsFromOa.stream().filter(item -> !idsInDb.contains(item)).collect(Collectors.toList());
            insertNewRecord(idsOnlyExistInOa, depts);
        }
    }

    private void updateIntersectionRecord(List<String> deptIdsInDb, List<OaDepartment> deptsFromOa) {
        if (deptIdsInDb == null || deptIdsInDb.size() < 1) {
            return;
        }
        boolean isSuccessful = adminDeptService.batchUpdateDepts(getModelForSpecifiedIds(deptIdsInDb, deptsFromOa));
        logger.info("department sync on " + LocalDate.now() + ", updateIntersectionRecord status: " + (isSuccessful ? "success" : "fail"));
    }

    private void updateDeleteFlag(List<String> deptIds) {
        if (deptIds == null || deptIds.size() < 1) {
            return;
        }
        List<Long> ids = deptIds.stream().map(item -> Long.valueOf(item.trim())).collect(Collectors.toList());
        boolean isSuccessful = adminDeptService.batchUpdateDeleteFlag(ids, CrmConstant.DELETE_FLAG_YES);
        logger.info("department sync on " + LocalDate.now() + ", updateDeleteFlag status: " + (isSuccessful ? "success" : "fail"));
    }

    private void insertNewRecord(List<String> newIds, List<OaDepartment> deptsFromOa) {
        if (newIds == null || newIds.size() < 1) {
            return;
        }

        List<AdminDept> deptList = getModelForSpecifiedIds(newIds, deptsFromOa);
        boolean isSuccessful = adminDeptService.batchInsertDepts(deptList);
        logger.info("department sync on " + LocalDate.now() + ", insertNewRecord status: " + (isSuccessful ? "success" : "fail"));

        if (isSuccessful){
            crmBaseDataCron.run();
            //保存部门变更日志
            crmChangeLogService.saveDeptAddLog(deptList);
        }
    }

    private List<AdminDept> getModelForSpecifiedIds(List<String> ids, List<OaDepartment> deptsFromOa) {
        List<OaDepartment> oaDepartments = deptsFromOa.stream().filter(item -> ids.contains(item.getDeptId())).collect(Collectors.toList());
        List<AdminDept> adminDeptList = Lists.newArrayList();
        oaDepartments.forEach(item -> {
            AdminDept adminDept = new AdminDept();
            adminDept.setDeptId(Long.valueOf(item.getDeptId().trim()));
            adminDept.setCreateTime(new Date());
            adminDept.setPid(Integer.valueOf(item.getPid().trim()));
            adminDept.setDeptLevel(Integer.parseInt(item.getDeptLevel().trim()) + 1);
            adminDept.setName(item.getName());
            adminDept.setRemark(item.getName());
            adminDept.setIsDelete(CrmConstant.DELETE_FLAG_NO);
            adminDept.setUpdateTime(new Date());
            adminDept.setLeaderId(item.getLeaderId());
            adminDeptList.add(adminDept);
        });
        return adminDeptList;
    }
}
