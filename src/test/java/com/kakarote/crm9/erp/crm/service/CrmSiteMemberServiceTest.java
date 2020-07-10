package com.kakarote.crm9.erp.crm.service;

import com.jfinal.aop.Aop;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.kakarote.crm9.BaseTest;
import com.kakarote.crm9.erp.crm.common.DistributorCertifiedEnum;
import com.kakarote.crm9.erp.crm.common.SiteMemberUserTypeEnum;
import com.kakarote.crm9.erp.crm.entity.CrmSiteMember;
import org.junit.Test;

/**
 * @Descriotion:
 * @param:
 * @return:
 * @author:hao.fu Created by hao.fu on 2019/7/15.
 */
public class CrmSiteMemberServiceTest extends BaseTest {

    private CrmSiteMemberService crmSiteMemberService = Aop.get(CrmSiteMemberService.class);

    @Test
    public void updateSiteMember() {
        try {
            Record result = crmSiteMemberService.findByCustId("qxwz967589_2020");

            String email = result.getStr("email");
            String newEmail = email + "aaa";
            result.set("email", newEmail);

            crmSiteMemberService.updateSiteMember(result);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

    }

    @Test
    public void findByCustId() {
        crmSiteMemberService.findByCustId("qxwz968008_2020");
    }

    @Test
    public void updateSiteMemberDeleteFlag() {
        crmSiteMemberService.updateSiteMemberDeleteFlag("qxwz50_2019", 0);
    }

    @Test
    public void getSiteMemberInfoByCustId() {
        Page<Record> result = crmSiteMemberService.getSiteMemberInfoByCustId(1,15, "50");
    }

    @Test
    public void getSiteMemberInfoBySiteMemberId() {
        crmSiteMemberService.getSiteMemberInfoBySiteMemberId("11423");
    }

    @Test
    public void bindSiteMemberToCust() {
        try {
            crmSiteMemberService.bindSiteMemberToCust("968008", "15359");
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    @Test
    public void addSiteMember() {
        try {
            CrmSiteMember siteMember = CrmSiteMember.dao.findById(3);
            crmSiteMemberService.addSiteMember(siteMember);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    @Test
    public void getSiteMemberAllFieldBySiteMemberId() {
        crmSiteMemberService.getSiteMemberAllFieldBySiteMemberId(11570L);
    }

    @Test
    public void updateSiteMemberCustId() {
        crmSiteMemberService.updateSiteMemberCustId("test","1");
    }

    @Test
    public void listSiteMemberIdsByCustomerId() {
        crmSiteMemberService.listSiteMemberIdsByCustomerId(607520L);
    }

    @Test
    public void updateFreezeStatusBySiteMemberId() {
        crmSiteMemberService.updateFreezeStatusBySiteMemberId(11570L,1);
    }

    @Test
    public void queryPromotionInfosBy() {
        crmSiteMemberService.queryPromotionInfosBy("968008");
    }

    @Test
    public void updatePromotionRelationInfo() {
        crmSiteMemberService.updatePromotionRelationInfo(SiteMemberUserTypeEnum.COMPANY, DistributorCertifiedEnum.AUDIT,"测试用例",1L);
    }

    @Test
    public void queryPromotionRelationMobile() {
        crmSiteMemberService.queryPromotionRelationMobile(15359L);
    }
}
