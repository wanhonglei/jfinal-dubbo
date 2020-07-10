package com.kakarote.crm9.erp.admin.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.util.TypeUtils;
import com.jfinal.aop.Aop;
import com.jfinal.kit.Kv;
import com.kakarote.crm9.BaseTest;
import com.kakarote.crm9.erp.admin.entity.AdminField;
import com.kakarote.crm9.erp.admin.entity.AdminFieldSort;
import org.junit.Test;

import java.util.Collections;

/**
 * @Author: honglei.wan
 * @Description:
 * @Date: Create in 2020/5/14 4:16 下午
 */
public class AdminFieldServiceTest extends BaseTest {

	private AdminFieldService adminFieldService = Aop.get(AdminFieldService.class);

	@Test
	public void save() {
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("data", Collections.singletonList(JSON.parseObject("{\"name\":\"单位12\",\"type\":3,\"label\":4,\"parentId\":0,\"sorting\":0,\"options\":\"上架,下架\",\"operating\":0}")));
		jsonObject.put("label",4);
		jsonObject.put("categoryId",1);
		adminFieldService.save(jsonObject);
	}

	@Test
	public void verify() {
		adminFieldService.verify(Kv.by("types","1").set("name","test").set("val","11"));
	}

	@Test
	public void testSave() {
		JSONArray array = new JSONArray();
		array.add(JSON.parseObject("{\"name\":\"单位12\",\"type\":3,\"label\":4,\"FieldId\":0,\"sorting\":0,\"options\":\"上架,下架\",\"operating\":0}"));
		try {
			adminFieldService.save(array,"adfadsfasdfasdf");
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}

	@Test
	public void testSave1() {
		AdminField field = TypeUtils.castToJavaBean(JSON.parseObject("{\"name\":\"单位12\",\"type\":3,\"label\":4,\"fieldId\":0,\"sorting\":0,\"options\":\"上架,下架\",\"operating\":0}"), AdminField.class);
		adminFieldService.save(Collections.singletonList(field),"hjagjdgfgjs");
	}

	@Test
	public void queryFieldsByBatchId() {
		adminFieldService.queryFieldsByBatchId("hjagjdgfgjs","123");
	}

	@Test
	public void queryByBatchId() {
		adminFieldService.queryByBatchId("hjagjdgfgjs");
	}

	@Test
	public void testQueryByBatchId() {
		adminFieldService.queryByBatchId("adminFieldService",4);
	}

	@Test
	public void queryFields() {
		adminFieldService.queryFields();
	}

	@Test
	public void list() {
		adminFieldService.list("4");
	}

	@Test
	public void testList() {
		adminFieldService.list("4", null);
	}

	@Test
	public void setFelidStyle() {
		Kv kv = new Kv();
		kv.set("types","crm_leads");
		kv.set("field","crm_leads");
		kv.set("width","crm_leads");

		adminFieldService.setFelidStyle(kv);
	}

	@Test
	public void queryFieldStyle() {
		adminFieldService.queryFieldStyle("123");
	}

	@Test
	public void queryListHead() {
		AdminFieldSort adminFieldSort = new AdminFieldSort();
		adminFieldSort.setLabel(1);
		try {
			adminFieldService.queryListHead(adminFieldSort);
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		adminFieldSort.setLabel(2);
		try {
			adminFieldService.queryListHead(adminFieldSort);
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		adminFieldSort.setLabel(3);
		try {
			adminFieldService.queryListHead(adminFieldSort);
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		adminFieldSort.setLabel(4);
		try {
			adminFieldService.queryListHead(adminFieldSort);
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		adminFieldSort.setLabel(5);
		try {
			adminFieldService.queryListHead(adminFieldSort);
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		adminFieldSort.setLabel(6);
		try {
			adminFieldService.queryListHead(adminFieldSort);
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		adminFieldSort.setLabel(7);
		try {
			adminFieldService.queryListHead(adminFieldSort);
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}

	@Test
	public void queryFieldConfig() {
		AdminFieldSort adminFieldSort = new AdminFieldSort();
		adminFieldSort.setLabel(4);
		adminFieldService.queryFieldConfig(adminFieldSort);
	}

	@Test
	public void fieldConfig() {
		AdminFieldSort adminFieldSort = new AdminFieldSort();
		adminFieldSort.setNoHideIds("12,23,1234");
		adminFieldService.fieldConfig(adminFieldSort);
	}
}