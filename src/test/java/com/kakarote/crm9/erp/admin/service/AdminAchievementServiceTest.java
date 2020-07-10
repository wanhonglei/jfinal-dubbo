package com.kakarote.crm9.erp.admin.service;

import com.jfinal.aop.Aop;
import com.kakarote.crm9.BaseTest;
import com.kakarote.crm9.erp.admin.entity.CrmAchievement;
import com.kakarote.crm9.utils.R;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author: haihong.wu
 * @Date: 2020/3/10 3:21 下午
 */
public class AdminAchievementServiceTest extends BaseTest {

    private AdminAchievementService adminAchievementService = Aop.get(AdminAchievementService.class);

    @Test
    public void test() {
        List<CrmAchievement> list = new ArrayList<>();
        CrmAchievement data1 = new CrmAchievement();
        data1.setObjId(1);
        data1.setType(3);
        data1.setYear("2020");
        data1.setStatus(1);
        list.add(data1);
        CrmAchievement data2 = new CrmAchievement();
        data2.setObjId(2);
        data2.setType(3);
        data2.setYear("2020");
        data2.setStatus(1);
        list.add(data2);
        CrmAchievement data3 = new CrmAchievement();
        data3.setObjId(3);
        data3.setType(3);
        data3.setYear("2020");
        data3.setStatus(1);
        list.add(data3);
        Assert.assertEquals(adminAchievementService.setAchievement(list), R.ok());
        adminAchievementService.queryAchievementList(data1, "1", null);
    }

    @Test
    public void setAchievement() {
        List<CrmAchievement> list = new ArrayList<>();
        CrmAchievement data1 = new CrmAchievement();
        data1.setObjId(1);
        data1.setType(3);
        data1.setYear("2020");
        data1.setStatus(1);
        list.add(data1);
        CrmAchievement data2 = new CrmAchievement();
        data2.setObjId(2);
        data2.setType(3);
        data2.setYear("2020");
        data2.setStatus(1);
        list.add(data2);
        CrmAchievement data3 = new CrmAchievement();
        data3.setObjId(3);
        data3.setType(3);
        data3.setYear("2020");
        data3.setStatus(1);
        list.add(data3);
        adminAchievementService.setAchievement(list);
    }

    @Test
    public void queryAchievementList() {
        CrmAchievement data1 = new CrmAchievement();
        data1.setObjId(1);
        data1.setType(3);
        data1.setYear("2020");
        data1.setStatus(1);
        adminAchievementService.queryAchievementList(data1, "1", null);
    }
}