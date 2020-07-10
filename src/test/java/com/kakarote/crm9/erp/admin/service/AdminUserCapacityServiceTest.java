package com.kakarote.crm9.erp.admin.service;

import com.jfinal.aop.Aop;
import com.jfinal.kit.Kv;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.kakarote.crm9.BaseTest;
import com.kakarote.crm9.erp.admin.entity.AdminUserCapacity;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

/**
 * @Author: haihong.wu
 * @Date: 2020/3/25 3:45 下午
 */
public class AdminUserCapacityServiceTest extends BaseTest {

    private AdminUserCapacityService adminUserCapacityService = Aop.get(AdminUserCapacityService.class);

    @Test
    public void getPersonalCapacity() {
        System.out.println(adminUserCapacityService.getPersonalCapacity(1L, 1, 10).getList());
        System.out.println(adminUserCapacityService.getPersonalCapacity(186L, 1, 10).getList());
        System.out.println(adminUserCapacityService.getPersonalCapacity(280L, 1, 10).getList());
        System.out.println(adminUserCapacityService.getPersonalCapacity(224L, 1, 10).getList());
    }

    @Test
    public void searchCapacityByCustomerId(){
        Record record = adminUserCapacityService.searchCapacityByCustomerId(11L);
        Assert.assertTrue(true);
    }

    @Test
    public void editPersonalCapacity() {
        List<AdminUserCapacity> all = AdminUserCapacity.dao.findAll();
        all.get(0).setRelateCap(999);
        all.add(new AdminUserCapacity().setUserId(1992L).setUserName("吴海宏").setInspectCap(null).setRelateCap(null));
        adminUserCapacityService.editPersonalCapacity(all);
    }
}