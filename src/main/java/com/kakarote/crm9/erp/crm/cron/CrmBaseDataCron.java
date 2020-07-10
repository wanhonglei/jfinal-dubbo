package com.kakarote.crm9.erp.crm.cron;

import com.jfinal.aop.Aop;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.kakarote.crm9.erp.admin.entity.AdminDept;
import com.kakarote.crm9.erp.admin.service.AdminDeptService;
import com.kakarote.crm9.utils.BaseUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author whl
 */
@Slf4j
public class CrmBaseDataCron implements Runnable{

    private AdminDeptService adminDeptService = Aop.get(AdminDeptService.class);

    @Override
    public void run() {
        log.info("刷新部门列表定时任务开始。。。");
        List<Record> deptList = Db.find(Db.getSql("admin.dept.queryDeptList"));
        List<AdminDept> adminDeptList = deptList.stream().map(item -> new AdminDept()._setAttrs(item.getColumns())).collect(Collectors.toList());
        BaseUtil.setAdminDeptList(adminDeptList);

        List<String> businessDeptIdList = Db.find(Db.getSql("admin.businessType.queryBusinessDeptList"))
            .stream().map(item -> item.getStr("id")).collect(Collectors.toList());
        adminDeptService.setBusinessDeptList(businessDeptIdList);
        log.info("刷新部门列表定时任务结束。。。");
    }
}
