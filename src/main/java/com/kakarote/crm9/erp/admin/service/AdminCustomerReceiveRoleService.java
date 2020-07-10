package com.kakarote.crm9.erp.admin.service;

import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.kakarote.crm9.erp.admin.entity.AdminCustomerReceiveRole;
import com.kakarote.crm9.utils.R;
import java.util.*;

/**
 * 客户领取规则设置
 * @author yue.li
 *
 */
public class AdminCustomerReceiveRoleService {


    /**
     * 客户领取规则列表
     * @author yue.li
     */
    public List<Record> getCustomerReceiveRoleList() {
        return Db.find(Db.getSql("admin.customerReceiveRole.getCustomerReceiveRoleList"));
    }

    /**
     * 添加客户领取规则
     * @author yue.li
     * @param adminCustomerReceiveRoleList 客户领取规则集合
     */
    public R saveCustomerReceiveRole(List<AdminCustomerReceiveRole> adminCustomerReceiveRoleList) {
        return Db.tx(() -> {
            for(AdminCustomerReceiveRole adminCustomerReceiveRole : adminCustomerReceiveRoleList) {
                if(Objects.nonNull(adminCustomerReceiveRole.getId())) {
                    adminCustomerReceiveRole.update();
                }else{
                    adminCustomerReceiveRole.save();
                }
            }
            return true;
        }) ? R.ok() : R.error("saveCustomerReceiveRole failed!");
    }

    /**
     * 按条件查询客户
     * @author yue.li
     * @param deptId 事业部ID
     */
    public AdminCustomerReceiveRole getCustomerReceiveRole(Integer deptId) {
        return AdminCustomerReceiveRole.dao.findFirst(Db.getSql("admin.customerReceiveRole.getCustomerReceiveRoleByDeptId"),deptId);
    }
}
