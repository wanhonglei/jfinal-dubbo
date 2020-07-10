package com.kakarote.crm9.erp.crm.service;

import com.jfinal.aop.Aop;
import com.kakarote.crm9.BaseTest;
import com.kakarote.crm9.common.config.paragetter.BasePageRequest;
import com.kakarote.crm9.erp.crm.entity.CrmProduct;
import org.junit.Test;

/**
 * @Author: haihong.wu
 * @Date: 2020/5/22 11:38 上午
 */
public class CrmProductServiceTest extends BaseTest {

    private CrmProductService crmProductService = Aop.get(CrmProductService.class);

    @Test
    public void queryPage() {
        crmProductService.queryPage(new BasePageRequest<>("{}", CrmProduct.class));
    }

    @Test
    public void saveAndUpdate() {
//        crmProductService.saveAndUpdate(JSON.parseObject("{}"), 1992L);
    }

    @Test
    public void queryById() {
        crmProductService.queryById(1);
    }

    @Test
    public void information() {
        crmProductService.information(1);
        crmProductService.information(999999991);
    }

    @Test
    public void deleteById() {
        crmProductService.deleteById(999999991);
    }

    @Test
    public void updateStatus() {
        crmProductService.updateStatus("1", 0);
        crmProductService.updateStatus("1", 1);
    }

    @Test
    public void queryField() {
        crmProductService.queryField();
        crmProductService.queryField(1);
    }

    @Test
    public void exportProduct() {
        crmProductService.exportProduct("1");
    }

    @Test
    public void getCheckingField() {
        crmProductService.getCheckingField();
    }

    @Test
    public void uploadExcel() {
//        crmProductService.uploadExcel()
    }

    @Test
    public void queryByStatus() {
        mockBaseUtil("1011");
        crmProductService.queryByStatus(new BasePageRequest<>("{}", CrmProduct.class));
    }
}