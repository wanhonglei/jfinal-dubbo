package com.kakarote.crm9.mobile.service;

import com.jfinal.aop.Aop;
import com.kakarote.crm9.BaseTest;
import com.kakarote.crm9.common.config.paragetter.BasePageRequest;
import com.kakarote.crm9.mobile.entity.MobileSignInListRequest;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author: honglei.wan
 * @Description:
 * @Date: Create in 2020/5/19 11:48 上午
 */
public class MobileSignInServiceTest extends BaseTest {

	private MobileSignInService mobileSignInService = Aop.get(MobileSignInService.class);

	@Test
	public void setupSqlPara() {
		MobileSignInListRequest mobileSignInListRequest = new MobileSignInListRequest();
		mobileSignInListRequest.setCustomerName("test");
		mobileSignInListRequest.setBizType(1);
		mobileSignInListRequest.setCreateTimeType("2020-02-02 00:00:00");
		mobileSignInListRequest.setCustomerType(1);
		mobileSignInListRequest.setCustomerGrade(1);
		mobileSignInListRequest.setOwnerUserLoginIds("2376");
		mobileSignInListRequest.setSceneId(1212);
		BasePageRequest<MobileSignInListRequest> pageRequest = new BasePageRequest<>(1,10,mobileSignInListRequest);

		List<Integer> list = new ArrayList<>();
		list.add(2376);

		mobileSignInService.setupSqlPara(pageRequest, list);
	}
}