package com.kakarote.crm9.erp.crm.service;

import cn.hutool.core.util.IdUtil;
import com.google.common.collect.Lists;
import com.jfinal.aop.Aop;
import com.jfinal.kit.JsonKit;
import com.jfinal.plugin.activerecord.Db;
import com.kakarote.crm9.BaseTest;
import com.kakarote.crm9.erp.admin.entity.AdminFile;
import com.kakarote.crm9.erp.crm.entity.CrmBusiness;
import com.kakarote.crm9.erp.crm.entity.CrmContract;
import com.kakarote.crm9.erp.crm.entity.CrmContractPayment;
import com.kakarote.crm9.erp.crm.entity.CrmContractProduct;
import com.kakarote.crm9.erp.crm.entity.CrmCustomer;
import com.kakarote.crm9.erp.crm.entity.CrmProduct;
import com.kakarote.crm9.erp.crm.entity.CrmProductCategory;
import com.kakarote.crm9.erp.crm.entity.CrmReceivablesPlan;
import com.kakarote.crm9.erp.crm.entity.CrmReceivablesPlanProduct;
import com.kakarote.crm9.erp.crm.entity.CrmSiteMember;
import com.kakarote.crm9.integration.client.PerformanceClient;
import com.kakarote.crm9.integration.dto.PerformancePlanDto;
import org.junit.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

/**
 * @Author: honglei.wan
 * @Description:
 * @Date: Create in 2020/6/15 6:46 下午
 */
public class CrmContractServiceTest extends BaseTest {

    private CrmContractService crmContractService = Aop.get(CrmContractService.class);

    @Test
    public void addOrUpdateContract() {
       Db.tx(() -> {
        CrmContractProduct crmContractProduct = new CrmContractProduct();
        crmContractProduct.setProductCode("mm2m44ar305096952");
        crmContractProduct.setNum(1);
        crmContractProduct.setSalesMoney(new BigDecimal("32.12"));

        CrmContractProduct crmContractProduct1 = new CrmContractProduct();
        crmContractProduct1.setProductCode("ia14dmri818021946");
        crmContractProduct1.setNum(2);
        crmContractProduct1.setSalesMoney(new BigDecimal("33.12"));


        List<CrmContractProduct> contractProductList = new ArrayList<>();
        contractProductList.add(crmContractProduct);
        contractProductList.add(crmContractProduct1);


        CrmContractPayment crmContractPayment = new CrmContractPayment();
        crmContractPayment.setNum(1);
        crmContractPayment.setPaymentCondition("测试调价");
        crmContractPayment.setPaymentDate(new Date());
        crmContractPayment.setComputeMode(1);
        crmContractPayment.setPaymentMoney(new BigDecimal("12.13"));

        CrmContractPayment crmContractPayment1 = new CrmContractPayment();
        crmContractPayment1.setNum(2);
        crmContractPayment1.setPaymentCondition("测试调价2");
        crmContractPayment1.setPaymentDate(new Date());
        crmContractPayment1.setComputeMode(1);
        crmContractPayment1.setPaymentMoney(new BigDecimal("22.13"));

        List<CrmContractPayment> contractPaymentList = new ArrayList<>();
        contractPaymentList.add(crmContractPayment);
        contractPaymentList.add(crmContractPayment1);

        CrmContract crmContract = new CrmContract();
        crmContract.setOaNum("oa-100100011");
        crmContract.setContractName("测试合同1");
        crmContract.setSiteMemberId(16993L);
        crmContract.setBusinessId(164L);
        crmContract.setContractTarget("测试合同是否可用");
        crmContract.setContractType(1);
        crmContract.setContractMoney(new BigDecimal("34.26"));
        crmContract.setCurrencyType(1);

        crmContract.setCheckStatus(1);

        crmContract.setPartyA("A");
        crmContract.setPartyB("B");
        crmContract.setPartyC("C");
        crmContract.setPartyD("D");

        crmContract.setContractDeadline("长度限制100");
        crmContract.setPaymentType(1);
        crmContract.setApplyUserId(2736L);
        crmContract.setSignUserId(2736L);
        crmContract.setSignDeptId(287);

        crmContract.setRequestId("1111");

        crmContract.setContractPaymentList(contractPaymentList);
        crmContract.setContractProductList(contractProductList);

        crmContractService.addOrUpdateContract(crmContract);

        return false;
       });
    }

    @Test
    public void listByBusinessIdWithPayment() {
        Db.tx(() -> {
            try {
                CrmCustomer customer = mockCustomer();

                CrmBusiness business = mockBusiness(customer.getCustomerId().intValue(), 1992);

                CrmContract contract = mockContract(business.getBusinessId(), 1992L);

                CrmProductCategory category = mockCategory();

                CrmProduct product = mockProduct(category.getCategoryId().intValue());

                CrmReceivablesPlan plan1 = mockPlan(customer.getCustomerId(), business.getBusinessId(), contract.getId(), "1", 100);
                CrmReceivablesPlan plan2 = mockPlan(customer.getCustomerId(), business.getBusinessId(), contract.getId(), "2", 400);
                CrmReceivablesPlan plan3 = mockPlan(customer.getCustomerId(), business.getBusinessId(), contract.getId(), "3", 900);

                contract.setContractMoney(plan1.getMoney().add(plan2.getMoney()).add(plan3.getMoney()));
                contract.update();

                CrmReceivablesPlanProduct planProduct1 = mockPlanProduct(plan1.getPlanId(), product.getProductId(), 1992);
                CrmReceivablesPlanProduct planProduct2 = mockPlanProduct(plan2.getPlanId(), product.getProductId(), 1992);
                CrmReceivablesPlanProduct planProduct3 = mockPlanProduct(plan3.getPlanId(), product.getProductId(), 1992);

//                CrmContractProduct product1 = mockContractProduct(contract.getId(), product.getProductId(), 10, 100);
//                CrmContractProduct product2 = mockContractProduct(contract.getId(), product.getProductId(), 12, 300);
                CrmContractProduct product3 = mockContractProduct(contract.getId(), product.getProductId(), 41, 1400);

                CrmContractPayment payment1 = mockContractPayment(contract.getId(), plan1.getPlanId(), plan1.getNum(), plan1.getPaymentCondition(), "2020-06-01", plan1.getMoney());
                CrmContractPayment payment2 = mockContractPayment(contract.getId(), plan2.getPlanId(), plan2.getNum(), plan2.getPaymentCondition(), "2020-06-01", plan2.getMoney());
                CrmContractPayment payment3 = mockContractPayment(contract.getId(), plan3.getPlanId(), plan3.getNum(), plan3.getPaymentCondition(), "2020-06-01", plan3.getMoney());

                PerformancePlanDto performancePlan1 = mockPerformancePlan(plan1.getMoney(), plan1.getMoney(), contract.getId().longValue(), payment1.getPaymentCode(), "2020-06-01");
                PerformancePlanDto performancePlan2 = mockPerformancePlan(plan2.getMoney(), plan2.getMoney().divide(BigDecimal.valueOf(2), RoundingMode.HALF_UP), contract.getId().longValue(), payment2.getPaymentCode(), "2020-06-02");
                PerformancePlanDto performancePlan3 = mockPerformancePlan(plan3.getMoney(), BigDecimal.ZERO, contract.getId().longValue(), payment3.getPaymentCode(), "2020-06-03");

                PerformanceClient client = Mockito.mock(PerformanceClient.class);
                Mockito.when(client.listInstallmentBill(contract.getContractNum())).thenReturn(Lists.newArrayList(performancePlan1, performancePlan2, performancePlan3));
                injectField(CrmContractService.class, crmContractService, "performanceClient", client);
                List<CrmContract> crmContracts = crmContractService.listByBusinessIdWithPayment(business.getBusinessId());
                String jsonStr = JsonKit.toJson(crmContracts);
                System.out.println(jsonStr);
            } catch (NoSuchFieldException | IllegalAccessException | ParseException e) {
                e.printStackTrace();
            }

            return false;
        });
    }

    @Test
    public void test() {
        System.out.println(JsonKit.toJson(crmContractService.listByBusinessIdWithPayment(164L)));
    }

    private void mockAdminFile(String fileBatchId) {
        List<AdminFile> adminFiles = AdminFile.dao.find("select * from 72crm_admin_file order by file_id desc limit 5");
        for (AdminFile adminFile : adminFiles) {
            adminFile.setBatchId(fileBatchId);
            adminFile.update();
        }
    }

    @Test
    public void queryById() {
        Db.tx(() -> {
            try {
                CrmCustomer customer = mockCustomer();

                CrmBusiness business = mockBusiness(customer.getCustomerId().intValue(), 1992);

                CrmContract contract = mockContract(business.getBusinessId(), 1992L);

                mockAdminFile(contract.getFileBatchId());

                CrmProductCategory category = mockCategory();

                CrmProduct product = mockProduct(category.getCategoryId().intValue());

                CrmReceivablesPlan plan1 = mockPlan(customer.getCustomerId(), business.getBusinessId(), contract.getId(), "1", 100);
                CrmReceivablesPlan plan2 = mockPlan(customer.getCustomerId(), business.getBusinessId(), contract.getId(), "2", 400);
                CrmReceivablesPlan plan3 = mockPlan(customer.getCustomerId(), business.getBusinessId(), contract.getId(), "3", 900);

                contract.setContractMoney(plan1.getMoney().add(plan2.getMoney()).add(plan3.getMoney()));
                contract.update();

                CrmReceivablesPlanProduct planProduct1 = mockPlanProduct(plan1.getPlanId(), product.getProductId(), 1992);
                CrmReceivablesPlanProduct planProduct2 = mockPlanProduct(plan2.getPlanId(), product.getProductId(), 1992);
                CrmReceivablesPlanProduct planProduct3 = mockPlanProduct(plan3.getPlanId(), product.getProductId(), 1992);

//                CrmContractProduct product1 = mockContractProduct(contract.getId(), product.getProductId(), 10, 100);
//                CrmContractProduct product2 = mockContractProduct(contract.getId(), product.getProductId(), 12, 300);
                CrmContractProduct product3 = mockContractProduct(contract.getId(), product.getProductId(), 41, 1400);

                CrmContractPayment payment1 = mockContractPayment(contract.getId(), plan1.getPlanId(), plan1.getNum(), plan1.getPaymentCondition(), "2020-06-01", plan1.getMoney());
                CrmContractPayment payment2 = mockContractPayment(contract.getId(), plan2.getPlanId(), plan2.getNum(), plan2.getPaymentCondition(), "2020-06-01", plan2.getMoney());
                CrmContractPayment payment3 = mockContractPayment(contract.getId(), plan3.getPlanId(), plan3.getNum(), plan3.getPaymentCondition(), "2020-06-01", plan3.getMoney());

                PerformancePlanDto performancePlan1 = mockPerformancePlan(plan1.getMoney(), plan1.getMoney(), contract.getId().longValue(), payment1.getPaymentCode(), "2020-06-01");
                PerformancePlanDto performancePlan2 = mockPerformancePlan(plan2.getMoney(), plan2.getMoney().divide(BigDecimal.valueOf(2), RoundingMode.HALF_UP), contract.getId().longValue(), payment2.getPaymentCode(), "2020-06-02");
                PerformancePlanDto performancePlan3 = mockPerformancePlan(plan3.getMoney(), BigDecimal.ZERO, contract.getId().longValue(), payment3.getPaymentCode(), "2020-06-03");

                PerformanceClient client = Mockito.mock(PerformanceClient.class);
                Mockito.when(client.listInstallmentBill(contract.getContractNum())).thenReturn(Lists.newArrayList(performancePlan1, performancePlan2, performancePlan3));
                injectField(CrmContractService.class, crmContractService, "performanceClient", client);
                CrmContract result = crmContractService.queryById(contract.getId().longValue(), mockOssUtil());
                String jsonStr = JsonKit.toJson(result);
                System.out.println(jsonStr);
            } catch (NoSuchFieldException | IllegalAccessException | ParseException e) {
                e.printStackTrace();
            }
            return false;
        });
    }

    private PerformancePlanDto mockPerformancePlan(BigDecimal totalAmount, BigDecimal paidAmount, Long contractId, String paymentNo, String lastPayTime) throws ParseException {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        PerformancePlanDto plan = new PerformancePlanDto();
        plan.setTotalAmount(totalAmount);
        plan.setAccumulateAmount(paidAmount);
        plan.setWaitAmount(totalAmount.subtract(paidAmount));
        plan.setParentBizNo(contractId.toString());
        plan.setBizNo(paymentNo);
        plan.setLastPayTime(format.parse(lastPayTime));
        return plan;
    }

    private CrmContractPayment mockContractPayment(BigInteger contractId, Long planId, String num, String paymentCondition, String date, BigDecimal money) throws ParseException {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        CrmContractPayment payment = new CrmContractPayment();
        payment.setPaymentCode("UT-P-" + new Random().nextInt(10000000));
        payment.setContractId(contractId);
        payment.setPlanId(planId);
        payment.setNum(Integer.valueOf(num));
        payment.setPaymentCondition(paymentCondition);
        payment.setPaymentDate(format.parse(date));
        payment.setComputeMode(1);
        payment.setPaymentMoney(money);
        payment.save();
        return payment;
    }

    private CrmContractProduct mockContractProduct(BigInteger contractId, Long productId, int num, int amount) {
        CrmContractProduct product = new CrmContractProduct();
        product.setContractId(contractId);
        product.setProductId(productId);
        product.setNum(num);
        product.setSalesMoney(BigDecimal.valueOf(amount));
        product.setUnit(1);
        product.save();
        return product;
    }

    private CrmReceivablesPlanProduct mockPlanProduct(Long planId, Long productId, int createUserId) {
        CrmReceivablesPlanProduct planProduct = new CrmReceivablesPlanProduct();
        planProduct.setPlanProductId(IdUtil.fastSimpleUUID());
        planProduct.setPlanId(planId.intValue());
        planProduct.setProductId(productId.intValue());
        planProduct.setCreateUserId(createUserId);
        planProduct.save();
        return planProduct;
    }

    private CrmReceivablesPlan mockPlan(Long customerId, Long businessId, BigInteger contractId, String num, Integer amount) {
        CrmReceivablesPlan plan = new CrmReceivablesPlan();
        plan.setNum(num);
        plan.setMoney(BigDecimal.valueOf(amount));
        plan.setContractId(contractId.intValue());
        plan.setCustomerId(customerId.intValue());
        plan.setBusinessId(businessId.intValue());
        plan.setPaymentCondition("UTCondition");
        plan.setCreateUserId(1992);
        plan.save();
        return plan;
    }

    private CrmCustomer mockCustomer() {
        CrmCustomer customer = new CrmCustomer();
        customer.setCustomerName("UTCustomer");
        customer.setBatchId(IdUtil.fastSimpleUUID());
        customer.save();
        customer.setCustomerNo("UT" + customer.getCustomerId());
        customer.update();
        return customer;
    }

    private CrmProductCategory mockCategory() {
        CrmProductCategory category = new CrmProductCategory();
        category.setName("UTCateGory");
        category.setCategoryCode("UTCode");
        category.save();
        return category;
    }

    private CrmProduct mockProduct(Integer categoryId) {
        CrmProduct product = new CrmProduct();
        product.setName("UTProduct");
        product.setStatus(1);
        product.setCategoryId(categoryId);
        product.save();
        return product;
    }

    private CrmBusiness mockBusiness(Integer customerId, Integer ownerUserId) {
        CrmBusiness business = new CrmBusiness();
        business.setCreateUserId(1992);
        business.setOwnerUserId(ownerUserId);
        business.setBusinessName("UTBusiness");
        business.setCustomerId(customerId);
        business.setRoUserId(",");
        business.setRwUserId(",");
        business.save();
        return business;
    }

    private CrmContract mockContract(Long businessId, Long applyUserId) {
        CrmContract contract = new CrmContract();
        contract.setContractNum("UT" + businessId);
        contract.setOaNum("UT-OA-" + businessId);
        contract.setContractName("UTContract");
        CrmSiteMember crmSiteMember = CrmSiteMember.dao.findFirst("select * from 72crm_crm_site_member order by id desc limit 1 ");
        contract.setSiteMemberId(crmSiteMember.getSiteMemberId());
        contract.setBusinessId(businessId);
        contract.setContractTarget("UnitTest");
        contract.setContractType(1);
        contract.setCheckStatus(1);
        contract.setPaymentType(2);
        contract.setApplyUserId(applyUserId);
        contract.setApplyTime(new Date());
        contract.setSignUserId(applyUserId);
        contract.setSignDeptId(0);
        contract.setFileBatchId(IdUtil.fastSimpleUUID());
        contract.setRequestId("1001");
        contract.save();
        return contract;
    }
}