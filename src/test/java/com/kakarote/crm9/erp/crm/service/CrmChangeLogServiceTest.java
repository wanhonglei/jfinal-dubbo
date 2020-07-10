package com.kakarote.crm9.erp.crm.service;

import cn.hutool.core.date.DateTime;
import com.google.common.collect.Maps;
import com.jfinal.aop.Aop;
import com.kakarote.crm9.BaseTest;
import com.kakarote.crm9.erp.admin.entity.AdminDept;
import com.kakarote.crm9.erp.admin.service.AdminDeptService;
import com.kakarote.crm9.erp.crm.common.CrmOperateChannelEnum;
import com.kakarote.crm9.erp.crm.common.CrmOperateChannelEventEnum;
import com.kakarote.crm9.erp.crm.dto.CrmChangeLogDto;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 变更历史记录日志
 *
 * @author liming.guo
 */
public class CrmChangeLogServiceTest extends BaseTest {

	private CrmChangeLogService crmChangeLogService = Aop.get(CrmChangeLogService.class);
	private AdminDeptService adminDeptService = Aop.get(AdminDeptService.class);


	@Test
	public void saveChangeLog() {
		CrmChangeLogDto crmChangeLogDto = CrmChangeLogDto.builder()
				.requestId("564daa7492d74db88d09f26f9d702fed")
				.channelEnum(CrmOperateChannelEnum.CUSTOMER)
				.channelEvent(CrmOperateChannelEventEnum.CUSTOMER_AUTO_PUT_PUBLIC_POOL.getName())
				.fromId("1099050")
				.changeHistory("商机分派变更了负责人")
				.oldOwnerUserId(421L)
				.newOwnerUserId(null)
				.operatorId(null)
				.operateTime(DateTime.now())
				.build();
		Long logId = crmChangeLogService.saveChangeLog(crmChangeLogDto);
		Assert.assertNotNull(logId);
	}

	@Test
	public void saveCustomerChangeLog() {
		try {
			crmChangeLogService.saveCustomerChangeLog(0,1L,null,null,null);
			crmChangeLogService.saveCustomerChangeLog(1,1L,2736L,null,null);
			crmChangeLogService.saveCustomerChangeLog(2,1L,null,280L,null);
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}

	@Test
	public void saveBusinessChangeLog() {
		try {
			crmChangeLogService.saveBusinessChangeLog(0,1L,null,null,null);
			crmChangeLogService.saveBusinessChangeLog(1,1L,2736L,null,null);
			crmChangeLogService.saveBusinessChangeLog(2,1L,null,280L,null);
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}

	@Test
	public void saveBdDeptChangeLog() {
		try {
			crmChangeLogService.saveBdDeptChangeLog(2736L,280L,null);
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}

	}

	@Test
	public void saveDeptChangeLog() {
		try {
			List<AdminDept> adminDeptList = adminDeptService.getAllAdminDepts();
			Map<Long, AdminDept> adminDeptHashMap = Maps.newLinkedHashMapWithExpectedSize(350);
			for (AdminDept adminDept : adminDeptList) {
				adminDeptHashMap.put(adminDept.getDeptId(), adminDept);
			}

			Map<Long, List<AdminDept>> pidDeptMap = Maps.newLinkedHashMapWithExpectedSize(50);
			for (AdminDept adminDept : adminDeptList) {
				List<AdminDept> deptList = pidDeptMap.getOrDefault(Long.valueOf(adminDept.getPid()), new ArrayList<>());
				deptList.add(adminDept);

				pidDeptMap.put(Long.valueOf(adminDept.getPid()),deptList);
			}

			crmChangeLogService.saveDeptChangeLog(AdminDept.dao.findById(1),2736L,null,adminDeptHashMap,pidDeptMap);
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}

	@Test
	public void saveDeptAddLog() {
		try {
			crmChangeLogService.saveDeptAddLog(new ArrayList<>());
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}
}
