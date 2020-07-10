package com.kakarote.crm9.erp.crm.service;

import com.jfinal.aop.Aop;
import com.kakarote.crm9.BaseTest;
import com.kakarote.crm9.erp.crm.acl.user.CrmUser;
import org.junit.Test;

/**
 * @Author: honglei.wan
 * @Description:
 * @Date: Create in 2020/5/19 11:43 上午
 */
public class CrmCustomerExtServiceTest extends BaseTest {

	private CrmCustomerExtService crmCustomerExtService = Aop.get(CrmCustomerExtService.class);

	@Test
	public void getDeptAndCapacity() {
		CrmUser user = super.mockCrmUser("0946");
		crmCustomerExtService.getDeptAndCapacity(user);
	}
}