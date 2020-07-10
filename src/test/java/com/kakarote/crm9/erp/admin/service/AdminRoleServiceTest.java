package com.kakarote.crm9.erp.admin.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.jfinal.aop.Aop;
import com.kakarote.crm9.BaseTest;
import com.kakarote.crm9.erp.admin.entity.AdminRole;
import com.kakarote.crm9.erp.admin.entity.AdminUserRole;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

/**
 * Admin Role Service Test
 *
 * @author hao.fu
 * @since 2019/12/30 13:37
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class AdminRoleServiceTest extends BaseTest {

    private AdminRoleService adminRoleService = Aop.get(AdminRoleService.class);

    @Test
    public void getRoleByUserId() {
        int userId = 813;
        adminRoleService.getRoleByUserId(userId);
    }

    @Test
    public void getAllRoleList() {
        adminRoleService.getAllRoleList();
    }

    @Test
    public void save() {
        AdminRole adminRole = AdminRole.dao.findById(10);
        adminRoleService.save(adminRole);
    }

    @Test
    public void update() {
        AdminRole adminRole = AdminRole.dao.findById(10);
        adminRoleService.update(adminRole);
    }

    @Test
    public void updateRoleMenu() {
        JSONObject jsonObject = JSON.parseObject("{\"rules\":[9,17,18,19,20,21,22,23,24,25,159,160,26,27,28,29,30,31,32,33,36,161,11,40,41,42,43,44,45,107,108,12,46,47,48,49,50,51,52,14,60,61,62,63,64,147,148,149,150,151,152,153,154,155,156,157,158],\"type\":1,\"id\":12,\"title\":\"销售员角色\"}");
        adminRoleService.updateRoleMenu(jsonObject);
    }

    @Test
    public void auth() {
        adminRoleService.auth(2736L);
    }

    @Test
    public void delete() {
        adminRoleService.delete(999);
    }

    @Test
    public void copy() {
        adminRoleService.copy(20);
    }

    @Test
    public void relatedUser() {
        AdminUserRole adminUserRole = new AdminUserRole();
        adminUserRole.setRoleIds("23,24");
        adminUserRole.setUserIds("111");
        adminRoleService.relatedUser(adminUserRole);
    }

    @Test
    public void unbindingUser() {
        AdminUserRole adminUserRole = new AdminUserRole();
        adminUserRole.setRoleId(23);
        adminUserRole.setUserId(111L);
        adminRoleService.unbindingUser(adminUserRole);
    }

    @Test
    public void queryRoleIdsByUserId() {
        adminRoleService.queryRoleIdsByUserId(2376L);
    }
}
