package com.kakarote.crm9.erp.admin.service;

import com.jfinal.aop.Aop;
import com.kakarote.crm9.BaseTest;
import com.kakarote.crm9.common.midway.NotifyService;
import com.kakarote.crm9.erp.admin.entity.SendEmailEntity;
import org.junit.Test;

import java.util.Arrays;

/**
 * @Author: honglei.wan
 * @Description:
 * @Date: Create in 2020/5/12 6:02 下午
 */
public class AdminSendEmailServiceTest extends BaseTest {

	private AdminSendEmailService adminSendEmailService = Aop.get(AdminSendEmailService.class);
	private NotifyService notifyService = Aop.get(NotifyService.class);

	@Test
	public void sendEmail() {
		try {
			SendEmailEntity emailEntity = new SendEmailEntity();
			emailEntity.setTitle("测试邮件");
			emailEntity.setContent("测试内容");
			emailEntity.setCopyUserList(Arrays.asList("zhixiang.liu"));
			adminSendEmailService.sendEmail(emailEntity);
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}

	@Test
	public void getEmail() {
		adminSendEmailService.getEmail("zhixiang.liu");
	}

	@Test
	public void sendErrorMessage() {
		try {
			adminSendEmailService.sendErrorMessage(new Exception(), notifyService);
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}
}
