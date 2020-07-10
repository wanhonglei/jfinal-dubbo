package com.kakarote.crm9.erp.admin.service;

import com.jfinal.aop.Aop;
import com.kakarote.crm9.BaseTest;
import com.kakarote.crm9.erp.admin.entity.AdminDeptCapacity;
import org.junit.Test;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * @Author: haihong.wu
 * @Date: 2020/3/31 9:28 上午
 */
public class AdminDeptCapacityServiceTest extends BaseTest {

    private AdminDeptCapacityService adminDeptCapacityService = Aop.get(AdminDeptCapacityService.class);

    @Test
    public void list() {
        System.out.println(adminDeptCapacityService.list(null));
        System.out.println(adminDeptCapacityService.list(96L));
    }

    @Test
    public void deleteDeptCapacityById() {
        AdminDeptCapacity entity = new AdminDeptCapacity();
        entity.setBizDeptId(96L);
        entity.setDeptId(186L);
        entity.setDeptName("业务中台事业部");
        entity.setCapacity(20);
        adminDeptCapacityService.addDeptCapacity(entity);
        adminDeptCapacityService.deleteDeptCapacityById(entity.getId().longValue());
    }

    @Test
    public void getEnableReleaseAdminDeptCapacityList() {
    }

    @Test
    public void addDeptCapacity() {
    }

    @Test
    public void getDeptCapacityByDeptId() {
    }

    @Test
    public void editCustomerCapacityRule() {
        List<AdminDeptCapacity> capacities = new ArrayList<>();
        capacities.add(new AdminDeptCapacity().setId(BigInteger.valueOf(1)).setInspectFlag(1).setInspectDays(7).setRelateOutFlag(2).setRelateOutDays(0).setRelateLockFlag(1).setRelateLockDays(30));
        capacities.add(new AdminDeptCapacity().setId(BigInteger.valueOf(3)).setInspectFlag(1).setInspectDays(14).setRelateOutFlag(1).setRelateOutDays(30).setRelateLockFlag(2).setRelateLockDays(30));
        adminDeptCapacityService.editCustomerCapacityRule(capacities);
    }
}