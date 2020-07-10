package com.kakarote.crm9.erp.crm;

import cn.hutool.core.util.IdUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.jfinal.aop.Aop;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Record;
import com.kakarote.crm9.BaseTest;
import com.kakarote.crm9.erp.admin.entity.AdminDept;
import com.kakarote.crm9.erp.admin.entity.AdminUser;
import com.kakarote.crm9.erp.admin.service.AdminDeptService;
import com.kakarote.crm9.erp.crm.entity.CrmBdDeptChangeLog;
import com.kakarote.crm9.erp.crm.entity.CrmReceivablesPlan;
import com.kakarote.crm9.utils.BaseUtil;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @Author: honglei.wan
 * @Description:
 * @Date: Create in 2020/1/16 11:33
 */
public class MyTest extends BaseTest {

    public static void main(String[] args) {
        System.out.println(11);
    }

    @Test
    public void testMyTest(){
        Db.tx(() -> {
            AdminUser adminUser = AdminUser.dao.findById(2826);
            adminUser.setId(null);
            adminUser.setUserId(011000L);
            adminUser.setStartTime("20200202010100");
            adminUser.put("myTime","20200202010100");
            Db.batchSave(Collections.singletonList(adminUser),1);

            return false;
        });


    }

}


