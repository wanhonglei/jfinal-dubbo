package com.kakarote.crm9.integration.service;

import com.jfinal.aop.Aop;
import com.kakarote.crm9.BaseTest;

/**
 * @author hao.fu
 * @create 2019/8/21 11:35
 */
public class SiteMemberMqMessageServiceTest extends BaseTest {

    private SiteMemberMqMessageService siteMemberMqMessageService = Aop.get(SiteMemberMqMessageService.class);

}
