package com.kakarote.crm9.erp.crm.service;

import com.alibaba.fastjson.JSONObject;
import com.jfinal.aop.Aop;
import com.jfinal.plugin.activerecord.Record;
import com.kakarote.crm9.BaseTest;
import com.kakarote.crm9.common.config.paragetter.BasePageRequest;
import com.kakarote.crm9.erp.crm.entity.CrmReceivables;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

/**
 * @Author: honglei.wan
 * @Description:
 * @Date: Create in 2020/5/13 11:30 上午
 */
public class CrmReceivablesServiceTest extends BaseTest {

	private CrmReceivablesService crmReceivablesService = Aop.get(CrmReceivablesService.class);

	@Test
	public void queryListByUserId() {
		try {
			crmReceivablesService.queryListByUserId(2441);
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}

	@Test
	public void queryPage() {
		BasePageRequest<CrmReceivables> basePageRequest = new BasePageRequest<>(1,10,null);
		crmReceivablesService.queryPage(basePageRequest);
	}

	@Test
	public void saveOrUpdate() {
		CrmReceivables crmReceivable = new CrmReceivables();
		crmReceivable.setReceivablesId(8000192L);
		crmReceivable.setPlanId(1162);
		crmReceivable.setCustomerId(967809);
		crmReceivable.setMoney(new BigDecimal(2000));
		crmReceivable.setOwnerUserId(1245);
		crmReceivable.setCreateUserId(1245);
		crmReceivable.setBusinessId(209);
		crmReceivable.setBatchId("abd4d78d4b9246d39ceb08b2462edca1");
		crmReceivable.setBopsPaymentId("a78acb4afc434734b536043bb5c07cff");

		JSONObject jsonObject = new JSONObject();
		jsonObject.put("entity",crmReceivable);


		try {
			crmReceivablesService.saveOrUpdate(jsonObject,1245L);
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}

	@Test
	public void queryById() {
		crmReceivablesService.queryById(8000192);
	}

	@Test
	public void information() {
		crmReceivablesService.information(8000192);
	}

	@Test
	public void deleteByIds() {
		crmReceivablesService.deleteByIds("1212,2323,4532");
	}

	@Test
	public void queryField() {
		crmReceivablesService.queryField();
	}

	@Test
	public void queryList() {
		CrmReceivables receivables = new CrmReceivables();
		receivables.setContractId(1231);
		receivables.setCustomerId(123123);
		crmReceivablesService.queryList(receivables);
	}

	@Test
	public void queryListByType() {
		crmReceivablesService.queryListByType("2",1212);
	}

	@Test
	public void findProductByPlanIds() {
		List<Long> longs = Arrays.asList(11L,12L,300L);
		List<Record> records = crmReceivablesService.findProductByPlanIds(longs);
		System.out.println(records);
	}
}
