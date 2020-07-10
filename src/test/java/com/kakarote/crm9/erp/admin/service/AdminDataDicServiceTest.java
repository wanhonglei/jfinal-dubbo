package com.kakarote.crm9.erp.admin.service;

import com.alibaba.fastjson.JSONObject;
import com.jfinal.aop.Aop;
import com.jfinal.plugin.activerecord.Record;
import com.kakarote.crm9.BaseTest;
import com.kakarote.crm9.erp.admin.entity.AdminDataDic;
import com.kakarote.crm9.utils.R;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

/**
 * @Descriotion:
 * @param:
 * @return:
 * @author:yue.li Created by yue.li on 2019/8/6.
 */
public class AdminDataDicServiceTest extends BaseTest {

    AdminDataDicService adminDataDicService = Aop.get(AdminDataDicService.class);

    @Test
    public void addDataDic() {
        try {
            AdminDataDic adminDataDic = new AdminDataDic();
            adminDataDic.setDicName("测试");
            adminDataDic.setDicValue("1");
            adminDataDic.setDicType("2");
            adminDataDic.setTagName("测试");
            adminDataDicService.addDataDic(adminDataDic, 3L);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    @Test
    public void deleteById() {
        String id = "56";
        R r = adminDataDicService.deleteById(id);
        Assert.assertTrue( r.isSuccess());
    }

    @Test
    public void queryDataDicNoPageList() {
        List<Record> recordList = adminDataDicService.queryDataDicNoPageList(null,"客户等级");
        Assert.assertTrue(recordList.size() > 0);
    }

	@Test
	public void getShareholderRelation() {
        List<JSONObject> shareholderRelation = adminDataDicService.getShareholderRelation();
        System.out.println(shareholderRelation);
    }
}
