package com.kakarote.crm9.erp.crm.service;

import com.google.common.collect.Lists;
import com.jfinal.aop.Aop;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.kakarote.crm9.BaseTest;
import com.kakarote.crm9.common.config.paragetter.BasePageRequest;
import com.kakarote.crm9.erp.crm.entity.*;
import com.kakarote.crm9.utils.R;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CrmBusinessServiceTest extends BaseTest {

    CrmBusinessService crmBusinessService = Aop.get(CrmBusinessService.class);

    @Test
    public void getStatysById() {
        List<Record> list = crmBusinessService.getStatysById("96", null);
        R data = R.ok().put("data", list);
        System.out.println(data);
    }

    @Test
    public void deleteByIds() {
        crmBusinessService.deleteByIds("113");
    }

    @Test
    public void queryRevenueByBusinessIdInSpecifiedPeriod() {
        List<Integer> ids = Lists.newArrayList();
        ids.add(32);
        ids.add(33);
        ids.add(34);
        List<BusinessRevenueInfo> result = crmBusinessService.queryRevenueByBusinessIdInSpecifiedPeriod(ids, null, null);
        result.forEach(item -> System.out.println(item.toString()));
        Assert.assertTrue(result.size() > 0);
    }

    @Test
    public void queryCategoryPriceByBusinessId() {
        List<Integer> ids = Lists.newArrayList();
        ids.add(32);
        ids.add(33);
        ids.add(34);
        List<BusinessCategoryPrice> result = crmBusinessService.queryCategoryPriceByBusinessId(ids);
        Assert.assertTrue(result.size() > 0);
    }

    @Test
    public void getCategoryPriceStatisticByBizId() {
        List<Integer> ids = Lists.newArrayList();
        ids.add(32);
        ids.add(33);
        ids.add(34);
        Map<Long, BusinessCategoryPriceStatistic> result = crmBusinessService.getCategoryPriceStatisticByBizId(ids);
        result.keySet().forEach(item -> System.out.println(item + ":" + result.get(item)));
        Assert.assertTrue(result.size() > 0);
    }

    @Test
    public void queryProductPriceStatisticByBizId() {
        List<Integer> ids = Lists.newArrayList();
        ids.add(32);
        ids.add(33);
        ids.add(34);
        List<BusinessProductPrice> result = crmBusinessService.queryProductPriceStatisticByBizId(ids);
        result.forEach(item -> System.out.println(item.toString()));
        Assert.assertTrue(result.size() > 0);
    }

    @Test
    public void getProductPriceStatisticByBizId() {
        try {
            List<Integer> ids = Lists.newArrayList();
            ids.add(32);
            ids.add(33);
            ids.add(34);
            ids.add(23);
            Map<Long, BusinessProductPriceStatistic> result = crmBusinessService.getProductPriceStatisticByBizId(ids);
            result.values().forEach(item -> System.out.println(item.toString()));

            BusinessProductPriceStatistic statistic32 = result.get(32L);
            List<Long> prods = Lists.newArrayList();
            prods.add(18L);
            prods.add(19L);
            prods.add(22L);
            Map<Long, Double> pricePercentage = statistic32.getPercentageMapForSpecifiedProducts(prods);
            pricePercentage.keySet().forEach(item -> System.out.println(item + ":" + pricePercentage.get(item)));

            prods.clear();
            prods.add(18L);
            Map<Long, Double> resultMap = statistic32.getPercentageMapForSpecifiedProducts(prods);
            resultMap.keySet().forEach(item -> System.out.println(item + ":" + resultMap.get(item)));
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

    }

    @Test
    public void queryReceivablesForSpecifiedBusiness() {
        List<Integer> ids = Lists.newArrayList();
        ids.add(32);
        ids.add(33);
        ids.add(34);
        List<BusinessReceivableInfo> result = crmBusinessService.queryReceivablesForSpecifiedBusiness(ids, null, null);
        result.forEach(item -> System.out.println(item.toString()));
        Assert.assertTrue(result.size() > 0);
    }

    @Test
    public void getReceivableInfoStatisticForSpecifiedBusiness() {
        List<Integer> ids = Lists.newArrayList();
        ids.add(32);
        ids.add(33);
        ids.add(34);
        Map<Long, BusinessReceivableInfoStatistic> result = crmBusinessService.getReceivableInfoStatisticForSpecifiedBusiness(ids, null, null);
        result.values().forEach(item -> System.out.println(item.toString()));
        Assert.assertTrue(result.size() > 0);
    }

    @Test
    public void getBusinessRevenueStatisticInfo() {
        List<Integer> ids = Lists.newArrayList();
        ids.add(32);
        ids.add(33);
        ids.add(34);
        Map<Long, BusinessRevenueInfo> result = crmBusinessService.getBusinessRevenueStatisticInfo(ids, null, null);
        result.values().forEach(item -> System.out.println(item.toString()));
        Assert.assertTrue(result.size() > 0);
    }

    @Test
    public void calculateCustomerRevenue() {
        List<Integer> ids = Lists.newArrayList();
        ids.add(31);
        Map<Long, BusinessRevenueInfo> result = crmBusinessService.getBusinessRevenueStatisticInfo(ids, null, null);
        if (result.size() > 0) {
            CustomerRevenueStatistic customerRevenueStatistic = crmBusinessService.calculateCustomerRevenue(result);

            System.out.println(customerRevenueStatistic.getTotalRevenue());
            Assert.assertTrue(customerRevenueStatistic.getCategoryRevenueMap().size() > 0);

            customerRevenueStatistic.getCategoryRevenueMap().keySet().forEach(catId -> System.out.println(catId + ":" + customerRevenueStatistic.getCategoryRevenueMap().get(catId)));
        }
    }

    @Test
    public void hasProduct() {
        crmBusinessService.hasProduct(32);
    }

    @Test
    public void getById() {
        System.out.println(crmBusinessService.getById(185));
    }

    @Test
    public void queryBusinessPageList() {
        BasePageRequest bpr = new BasePageRequest("{\"page\":1,\"limit\":15,\"search\":\"\",\"type\":5,\"sceneId\":1130}", null);
        System.out.println(crmBusinessService.queryBusinessPageList(bpr, mockCrmUser("1011").getCrmAdminUser()).getList());
        bpr = new BasePageRequest("{\"page\":1,\"limit\":15,\"search\":\"121\",\"type\":5,\"sceneId\":4678,\"data\":{\"owner_user_id\":{\"condition\":\"is\",\"value\":1992,\"formType\":\"user\",\"name\":\"owner_user_id\"}}}", null);
        System.out.println(crmBusinessService.queryBusinessPageList(bpr, mockCrmUser("1011").getCrmAdminUser()).getList());
    }

    @Test
    public void queryBusinessStatusNew() {
        System.out.println(crmBusinessService.queryBusinessStatusNew(48L));
    }

    @Test
    public void queryFieldNew() {
        try {
            System.out.println(crmBusinessService.queryFieldNew(null, 399L));
            System.out.println(crmBusinessService.queryFieldNew(48, 399L));
            System.out.println(crmBusinessService.queryFieldNew(179,399L));
            System.out.println(crmBusinessService.queryFieldNew(185,399L));
            System.out.println(crmBusinessService.queryFieldNew(23, 399L));
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    @Test
    public void statusSalesActivityList() {
        System.out.println(crmBusinessService.statusSalesActivityList(185L, 118L));
    }

    @Test
    public void statusVerificationList() {
        System.out.println(crmBusinessService.statusVerificationList(185L, 118L, mockOssUtil()));
    }

    @Test
    public void statusSalesActivityEdit() {
        Long bizId = 185L;
        Long sttusId = 118L;
        crmBusinessService.statusSalesActivityEdit(new Record()
                .set("userId", 1992L)
                .set("businessId", bizId)
                .set("statusId", sttusId)
                .set("activities", new ArrayList<>())
        );
        crmBusinessService.statusSalesActivityEdit(new Record()
                .set("userId", 1992L)
                .set("businessId", bizId)
                .set("statusId", sttusId)
                .set("activities", new ArrayList<Record>() {{
                    add(new Record().set("business_id", bizId).set("activity_id", 1L).set("status_id", sttusId));
                    add(new Record().set("business_id", bizId).set("activity_id", 3L).set("status_id", sttusId));
                    add(new Record().set("business_id", bizId).set("activity_id", 5L).set("status_id", sttusId));
                }})
        );
        crmBusinessService.statusSalesActivityEdit(new Record()
                .set("userId", 1992L)
                .set("businessId", bizId)
                .set("statusId", sttusId)
                .set("activities", new ArrayList<Record>() {{
                    add(new Record().set("business_id", bizId).set("activity_id", 1L).set("status_id", sttusId));
                }})
        );
    }

    @Test
    public void statusVerificationEdit() {
        crmBusinessService.statusVerificationEdit(new Record().set("business_id", 185L)
                        .set("verification_id", 1L)
                        .set("status_id", 118L)
                        .set("content", "")
                        .set("fileIds", new ArrayList<Long>() {{
                            add(1023L);
                            add(1026L);
                        }})
                , mockOssUtil(), 1992L);
        crmBusinessService.statusVerificationEdit(new Record().set("business_id", 185L)
                        .set("verification_id", 1L)
                        .set("status_id", 118L)
                        .set("content", "")
                        .set("fileIds", new ArrayList<Long>() {{
                            add(1023L);
                        }})
                , mockOssUtil(), 1992L);
    }

    @Test
    public void information() {
        System.out.println(crmBusinessService.information(209));
        System.out.println(crmBusinessService.information(210));
        System.out.println(crmBusinessService.information(212));
    }

    @Test
    public void createSiteMemberAndBindCid() {
        try {
            Db.tx(() -> {
                crmBusinessService.createSiteMemberAndBindCid(800153);
                return false;
            });
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}