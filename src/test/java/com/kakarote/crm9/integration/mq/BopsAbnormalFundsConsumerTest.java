package com.kakarote.crm9.integration.mq;

import com.jfinal.aop.Aop;
import com.kakarote.crm9.BaseTest;
import org.junit.Test;

/**
 * @Author: haihong.wu
 * @Date: 2020/7/3 1:58 下午
 */
public class BopsAbnormalFundsConsumerTest extends BaseTest {

    private BopsAbnormalFundsConsumer consumer = Aop.get(BopsAbnormalFundsConsumer.class);

    @Test
    public void consume() {
    }
}