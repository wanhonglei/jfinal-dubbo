package com.kakarote.crm9.erp.admin.service;

import com.jfinal.aop.Aop;
import com.jfinal.plugin.activerecord.Record;
import com.kakarote.crm9.BaseTest;
import com.kakarote.crm9.erp.admin.entity.AdminCustomerReceiveRole;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;


/**
 * 客户领取规则设置
 * @author yue.li
 *
 */
public class AdminCustomerReceiveRoleServiceTest extends BaseTest {

    private AdminCustomerReceiveRoleService adminCustomerReceiveRoleService = Aop.get(AdminCustomerReceiveRoleService.class);

    @Test
    public void saveCustomerReceiveRole() {
        try {
            List<AdminCustomerReceiveRole> adminCustomerReceiveRoleList = new ArrayList<>();
            AdminCustomerReceiveRole adminCustomerReceiveRole = new AdminCustomerReceiveRole();
            adminCustomerReceiveRole.setDeptId(Long.valueOf("1"));
            adminCustomerReceiveRole.setIsNeedCheck(Integer.valueOf("0"));
            adminCustomerReceiveRole.setMoney(new BigDecimal(100));
            adminCustomerReceiveRoleList.add(adminCustomerReceiveRole);
            adminCustomerReceiveRoleService.saveCustomerReceiveRole(adminCustomerReceiveRoleList);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    @Test
    public void getCustomerReceiveRoleList() {
        List<Record> recordList = adminCustomerReceiveRoleService.getCustomerReceiveRoleList();
        System.out.println(recordList);
    }

    @Test
    public void getCustomerReceiveRole() {
        AdminCustomerReceiveRole record = adminCustomerReceiveRoleService.getCustomerReceiveRole(186);
        System.out.println(record);
    }
}
