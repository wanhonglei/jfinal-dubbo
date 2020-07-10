package com.kakarote.crm9.erp.crm.service;

import com.alibaba.fastjson.JSONObject;
import com.jfinal.aop.Aop;
import com.jfinal.kit.JsonKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.kakarote.crm9.BaseTest;
import com.kakarote.crm9.common.config.paragetter.BasePageRequest;
import com.kakarote.crm9.erp.crm.common.CustomerStorageTypeEnum;
import com.kakarote.crm9.erp.crm.common.customer.DistributorIdentityEnum;
import com.kakarote.crm9.erp.crm.common.scene.CrmCustomerSceneEnum;
import com.kakarote.crm9.erp.crm.dto.CrmCustomerQueryParamDto;
import com.kakarote.crm9.erp.crm.vo.CrmCustomerGuideQueryVO;
import org.junit.Test;

import java.util.Collections;
import java.util.List;

/**
 * @Author: haihong.wu
 * @Description:
 * @Date: Create in 2020/5/11 6:22 下午
 */
public class CrmCustomerSceneServiceTest extends BaseTest {

    private CrmCustomerSceneService crmCustomerSceneService = Aop.get(CrmCustomerSceneService.class);

    @Test
    public void queryPageWithSceneCode() {
		mockBaseUtil("1011");
		for (CrmCustomerSceneEnum sceneEnum : CrmCustomerSceneEnum.values()) {
            JSONObject params = new JSONObject();
            params.put("sceneCode", sceneEnum.getCode());
			BasePageRequest<CrmCustomerQueryParamDto> basePageRequest = new BasePageRequest<>(params.toJSONString(), CrmCustomerQueryParamDto.class);
			Page<Record> recordPage = crmCustomerSceneService.queryPageWithSceneCode(basePageRequest);
			System.out.println(recordPage.getList());
		}
		mockBaseUtil("0870");
        for (CrmCustomerSceneEnum sceneEnum : CrmCustomerSceneEnum.values()) {
            JSONObject params = new JSONObject();
            params.put("sceneCode", sceneEnum.getCode());
            BasePageRequest<CrmCustomerQueryParamDto> basePageRequest = new BasePageRequest<>(params.toJSONString(), CrmCustomerQueryParamDto.class);
            Page<Record> recordPage = crmCustomerSceneService.queryPageWithSceneCode(basePageRequest);
            System.out.println(recordPage.getList());
        }
        mockBaseUtil("1011");
        for (CrmCustomerSceneEnum sceneEnum : CrmCustomerSceneEnum.values()) {
            CrmCustomerQueryParamDto params = new CrmCustomerQueryParamDto();
            params.setSceneCode(sceneEnum.getCode());
            params.setOrderKey("create_time");
            params.setOrderType("desc");
            params.setDistributionIdentityId("distributor");
            params.setIsSiteMember(1);
            params.setHasContacts(1);
            params.setHasNotes(1);
            params.setHasBusiness(1);
            params.setDisposeStatus(0);
            params.setStorageType(CustomerStorageTypeEnum.INSPECT_CAP.getCode());
            params.setDeptId(280L);
            params.setSiteMemberIds("2,");
            params.setProvince("江苏省");
            params.setCity("南京市");
            params.setArea("江宁区");
            BasePageRequest<CrmCustomerQueryParamDto> basePageRequest = new BasePageRequest<>(1, 20, params);
            Page<Record> recordPage = crmCustomerSceneService.queryPageWithSceneCode(basePageRequest);
            System.out.println(recordPage.getList());
        }
        for (CrmCustomerSceneEnum sceneEnum : CrmCustomerSceneEnum.values()) {
            CrmCustomerQueryParamDto params = new CrmCustomerQueryParamDto();
            params.setSceneCode(sceneEnum.getCode());
            params.setOrderKey("create_time");
            params.setOrderType("desc");
            params.setDistributionIdentityId(DistributorIdentityEnum.DISTRIBUTOR_PARTNER_TERMINAL_USER.getCode());
            params.setIsSiteMember(0);
            params.setHasContacts(0);
            params.setHasNotes(0);
            params.setHasBusiness(0);
            params.setDisposeStatus(1);
            params.setStorageType(CustomerStorageTypeEnum.RELATE_CAP.getCode());
            params.setDeptId(280L);
            params.setSiteMemberIds("2,");
            params.setProvince("江苏省");
            params.setCity("南京市");
            params.setArea("江宁区");
            BasePageRequest<CrmCustomerQueryParamDto> basePageRequest = new BasePageRequest<>(1, 20, params);
            Page<Record> recordPage = crmCustomerSceneService.queryPageWithSceneCode(basePageRequest);
            System.out.println(recordPage.getList());
        }
    }

    @Test
    public void queryCustomerGuidePageList() {
        mockBaseUtil("1011");
        CrmCustomerGuideQueryVO crmCustomerGuideQueryVO = new CrmCustomerGuideQueryVO();
        crmCustomerGuideQueryVO.setSiteMemberId("16899454");
        List<Record> records = crmCustomerSceneService.queryCustomerGuidePageList(crmCustomerGuideQueryVO);
        System.out.println(records.size());

        crmCustomerGuideQueryVO.setSiteMemberId(null);
        crmCustomerGuideQueryVO.setCustomerName("qxwz");
        records = crmCustomerSceneService.queryCustomerGuidePageList(crmCustomerGuideQueryVO);
        System.out.println(records.size());

        crmCustomerGuideQueryVO.setSiteMemberId(null);
        crmCustomerGuideQueryVO.setCustomerName("qxwz3");
        records = crmCustomerSceneService.queryCustomerGuidePageList(crmCustomerGuideQueryVO);
        System.out.println(records.size());
    }

    @Test
    public void listAllSelect() {
        System.out.println(JsonKit.toJson(crmCustomerSceneService.listAllSelect()));
    }

    @Test
    public void fuzzyQuerySubDistributor() {
        System.out.println(JsonKit.toJson(crmCustomerSceneService.fuzzyQuerySubDistributor("千寻")));
    }

    @Test
    public void queryScene() {
        mockBaseUtil("1011");
        System.out.println(crmCustomerSceneService.queryScene());
    }

    @Test
    public void customerDeptBySceneCode() {
        mockBaseUtil("0653");
        for (CrmCustomerSceneEnum value : CrmCustomerSceneEnum.values()) {
            System.out.println(crmCustomerSceneService.customerDeptBySceneCode(value.getCode()));
        }
    }

	@Test
	public void clearStorageTypeAndGroupMember() {
        crmCustomerSceneService.clearStorageTypeAndGroupMember(Collections.singletonList(19191L));
        Db.update(Db.getSql("crm.group.member.deleteGroupMemberWithOutOwner"));
        Db.update(Db.getSql("crm.customerExt.deleteStorageTypeWithOutOwner"));
	}
}
