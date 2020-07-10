package com.kakarote.crm9.erp.crm.service;

import cn.hutool.core.lang.Assert;
import com.jfinal.aop.Aop;
import com.kakarote.crm9.BaseTest;
import com.kakarote.crm9.erp.crm.common.PerformanceFromChannelEnum;
import com.kakarote.crm9.erp.crm.common.PerformanceObjectTypeEnum;
import com.kakarote.crm9.erp.crm.common.PerformanceTargetTypeEnum;
import org.junit.Test;

import java.math.BigDecimal;

/**
 * @Author: haihong.wu
 * @Date: 2020/3/11 4:05 下午
 */
public class CrmPerformanceServiceTest extends BaseTest {

    private CrmPerformanceService crmPerformanceService = Aop.get(CrmPerformanceService.class);

    @Test
    public void test() {
        Assert.notNull(crmPerformanceService.getPerformance(321L, PerformanceObjectTypeEnum.DEPARTMENT.getCode()));
        crmPerformanceService.addPerformance("test" + System.currentTimeMillis(), 321L, PerformanceObjectTypeEnum.DEPARTMENT.getCode(), BigDecimal.TEN, BigDecimal.ONE, PerformanceFromChannelEnum.WEBSITE_POOL, 290L, PerformanceTargetTypeEnum.CUSTOMER, 12L, "test",null);
    }

}