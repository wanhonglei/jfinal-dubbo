package com.kakarote.crm9.erp.crm.service;

import com.jfinal.aop.Aop;
import com.kakarote.crm9.BaseTest;
import com.kakarote.crm9.common.config.paragetter.BasePageRequest;
import com.kakarote.crm9.erp.crm.dto.CrmDepartmentIncomeReportDto;
import com.kakarote.crm9.erp.crm.entity.CustomerRevenueStatistic;
import org.junit.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * @Author: haihong.wu
 * @Date: 2020/5/20 6:03 下午
 */
public class CrmDepartmentIncomeServiceTest extends BaseTest {

    private CrmDepartmentIncomeService crmDepartmentIncomeService = Aop.get(CrmDepartmentIncomeService.class);

    @Test
    public void queryDepartmentIncomeReportList() throws NoSuchFieldException, IllegalAccessException {
        mockBaseUtil("0653");
        crmDepartmentIncomeService.queryDepartmentIncomeReportList(new BasePageRequest<>("{\"deptId\":373,\"startTime\":\"2020-01-01\",\"endTime\":\"2020-06-01\"}", CrmDepartmentIncomeReportDto.class), 2421L);
        CrmBusinessService crmBusinessService = Mockito.mock(CrmBusinessService.class);
        CustomerRevenueStatistic mockO = new CustomerRevenueStatistic();
        Map<String, Map<String, BigDecimal>> categoryCodeProductRevenueMap = new HashMap<>();
        Mockito.when(crmBusinessService.calculateCustomerRevenue(Mockito.anyMap())).thenReturn(mockO);
        crmDepartmentIncomeService.queryDepartmentIncomeReportList(new BasePageRequest<>("{\"deptId\":373,\"startTime\":\"2020-01-01\",\"endTime\":\"2020-06-01\"}", CrmDepartmentIncomeReportDto.class), 2421L);
        Map<String, BigDecimal> productIncome = new HashMap<>();
        productIncome.put("mockProduct", BigDecimal.TEN);
        categoryCodeProductRevenueMap.put("mockKey", productIncome);
        mockO.setCategoryCodeProductRevenueMap(categoryCodeProductRevenueMap);
        crmDepartmentIncomeService.queryDepartmentIncomeReportList(new BasePageRequest<>("{\"deptId\":373,\"startTime\":\"2020-01-01\",\"endTime\":\"2020-06-01\"}", CrmDepartmentIncomeReportDto.class), 2421L);
        Mockito.when(crmBusinessService.queryUserPayment(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(BigDecimal.TEN);
        injectField(CrmDepartmentIncomeService.class, crmDepartmentIncomeService, "crmBusinessService", crmBusinessService);
        crmDepartmentIncomeService.queryDepartmentIncomeReportList(new BasePageRequest<>("{\"startTime\":\"2020-01-01\",\"endTime\":\"2020-06-01\"}", CrmDepartmentIncomeReportDto.class), 2421L);
        crmDepartmentIncomeService.queryDepartmentIncomeReportList(new BasePageRequest<>("{\"deptId\":373,\"startTime\":\"2020-01-01\",\"endTime\":\"2020-06-01\"}", CrmDepartmentIncomeReportDto.class), 2421L);
    }
}