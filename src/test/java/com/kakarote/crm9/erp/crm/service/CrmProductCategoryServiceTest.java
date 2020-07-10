package com.kakarote.crm9.erp.crm.service;

import com.jfinal.aop.Aop;
import com.kakarote.crm9.BaseTest;
import com.kakarote.crm9.erp.crm.entity.CrmProductCategory;
import org.junit.Test;

/**
 * @Author: haihong.wu
 * @Date: 2020/5/22 11:16 上午
 */
public class CrmProductCategoryServiceTest extends BaseTest {

    private CrmProductCategoryService crmProductCategoryService = Aop.get(CrmProductCategoryService.class);

    @Test
    public void queryById() {
        crmProductCategoryService.queryById(1);
    }

    @Test
    public void saveAndUpdate() {
        CrmProductCategory crmProductCategory = new CrmProductCategory();
        crmProductCategory.setName("UnitTestProductCategory");
        crmProductCategoryService.saveAndUpdate(crmProductCategory);
        crmProductCategory.setPid(2);
        crmProductCategoryService.saveAndUpdate(crmProductCategory);
        crmProductCategoryService.deleteById(crmProductCategory.getCategoryId().intValue());
    }

    @Test
    public void queryList() {
        crmProductCategoryService.queryList();
    }

    @Test
    public void queryListByPid() {
        crmProductCategoryService.queryListByPid(0);
    }

    @Test
    public void queryListById() {
        CrmProductCategory crmProductCategory = new CrmProductCategory();
        crmProductCategory.setName("UnitTestProductCategory");
        crmProductCategory.setPid(2);
        crmProductCategoryService.saveAndUpdate(crmProductCategory);
        crmProductCategoryService.queryListById(null, 0);
    }

    @Test
    public void queryId() {
        crmProductCategoryService.queryId(null,1);
    }
}