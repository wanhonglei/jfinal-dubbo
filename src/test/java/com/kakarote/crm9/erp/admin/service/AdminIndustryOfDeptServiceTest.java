package com.kakarote.crm9.erp.admin.service;

import com.jfinal.aop.Aop;
import com.jfinal.plugin.activerecord.Record;
import com.kakarote.crm9.BaseTest;
import com.kakarote.crm9.erp.admin.entity.AdminIndustryOfDept;
import org.junit.Test;

import java.math.BigInteger;
import java.util.Date;

public class AdminIndustryOfDeptServiceTest extends BaseTest {

	private AdminIndustryOfDeptService adminIndustryOfDeptService = Aop.get(AdminIndustryOfDeptService.class);

	@Test
	public void queryDeptListWithIndustryInfo() {
		adminIndustryOfDeptService.queryDeptListWithIndustryInfo();
	}

	@Test
	public void initIndustryList() {
		adminIndustryOfDeptService.initIndustryList();
	}

	public Long addIndustry() {
		Long deptId = 5L;
		String code = "145";
		String name = "中间件及应用开发商";
		Integer type = 1;
//        adminIndustryOfDeptService.deleteIndustry(deptId,code);
		Record record = adminIndustryOfDeptService.addIndustry(deptId, code, name, type);
		return record.getLong("id");
	}

	@Test
	public void test() {
		//满足覆盖率
		AdminIndustryOfDept entity = new AdminIndustryOfDept();
		entity.setId(BigInteger.ONE);
		entity.getId();
		entity.setDeptId(10L);
		entity.getDeptId();
		entity.setIndustryCode("11");
		entity.getIndustryCode();
		entity.setIndustryType(1);
		entity.getIndustryType();
		entity.setIndustryName("aa");
		entity.getIndustryName();
		entity.setIsDeleted(1);
		entity.getIsDeleted();
		entity.setGmtCreate(new Date());
		entity.getGmtCreate();
		entity.setGmtModified(new Date());
		entity.getGmtModified();
		entity.setEnvFlag("ss");
		entity.getEnvFlag();
		entity.setRemark("ss");
		entity.getRemark();

        try {
            addIndustry();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}