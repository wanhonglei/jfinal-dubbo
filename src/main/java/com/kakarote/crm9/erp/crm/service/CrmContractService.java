package com.kakarote.crm9.erp.crm.service;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RandomUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.jfinal.aop.Before;
import com.jfinal.aop.Inject;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.kakarote.crm9.common.exception.CrmException;
import com.kakarote.crm9.common.theadpool.CrmThreadPool;
import com.kakarote.crm9.erp.admin.common.ProductCategoryEnum;
import com.kakarote.crm9.erp.admin.entity.AdminDept;
import com.kakarote.crm9.erp.admin.entity.AdminFile;
import com.kakarote.crm9.erp.admin.entity.AdminUser;
import com.kakarote.crm9.erp.admin.service.AdminFileService;
import com.kakarote.crm9.erp.crm.common.contract.CheckStatusEnum;
import com.kakarote.crm9.erp.crm.common.contract.PaymentProgressEnum;
import com.kakarote.crm9.erp.crm.constant.CrmConstant;
import com.kakarote.crm9.erp.crm.entity.CrmBusiness;
import com.kakarote.crm9.erp.crm.entity.CrmContract;
import com.kakarote.crm9.erp.crm.entity.CrmContractPayment;
import com.kakarote.crm9.erp.crm.entity.CrmContractProduct;
import com.kakarote.crm9.erp.crm.entity.CrmCustomer;
import com.kakarote.crm9.erp.crm.entity.CrmProduct;
import com.kakarote.crm9.erp.crm.entity.CrmReceivablesPlan;
import com.kakarote.crm9.integration.client.PerformanceClient;
import com.kakarote.crm9.integration.dto.PerformancePlanDto;
import com.kakarote.crm9.integration.service.CrmTimeTaskService;
import com.kakarote.crm9.utils.OssPrivateFileUtil;
import com.kakarote.crm9.utils.R;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @Author: haihong.wu
 * @Date: 2020/6/12 5:08 下午
 */
@Slf4j
public class CrmContractService {

    @Inject
    private PerformanceClient performanceClient;

    @Inject
    private AdminFileService adminFileService;

    @Inject
    private CrmReceivablesPlanService receivablesPlanService;

    @Inject
    private CrmTimeTaskService crmTimeTaskService;

    /**
     * 审核中
     */
    private static final int AUDIT_START = 1;
    /**
     * 审核通过
     */
    private static final int AUDIT_DONE = 2;

    /**
     * 根据商机ID查询合同列表
     *
     * @param businessId
     * @return
     */
    public List<CrmContract> listByBusinessIdWithPayment(Long businessId) {
        List<CrmContract> contracts = CrmContract.dao.find(Db.getSql("crm.contract.listByBusinessIdWithPayment"), businessId);
        if (CollectionUtils.isNotEmpty(contracts)) {
            contracts.forEach(contract -> {
                // 调用履约接口
                List<PerformancePlanDto> performancePlanDtos = null;
                if (StringUtils.isNotBlank(contract.getContractNum())) {
                    performancePlanDtos = performanceClient.listInstallmentBill(contract.getContractNum());
                }
                // 计算回款金额并转换参数类型
                BigDecimal paidAmount = BigDecimal.ZERO;
                List<CrmContractPayment> payments = new ArrayList<>();
                //遍历履约账单
                if (CollectionUtils.isNotEmpty(performancePlanDtos)) {
                    for (PerformancePlanDto performancePlanDto : performancePlanDtos) {
                        //获取付款计划
                        CrmContractPayment payment = CrmContractPayment.dao.getByPaymentCode(performancePlanDto.getBizNo());
                        if (payment == null) {
                            continue;
                        }

                        //计算合同的回款金额
                        paidAmount = paidAmount.add(performancePlanDto.getAccumulateAmount());
                        //付款条款回款进度
                        payment.setPaymentProgress(judgePaymentProgress(payment.getPaymentMoney(), performancePlanDto.getAccumulateAmount()));
                        payment.setPaidAmount(performancePlanDto.getAccumulateAmount());
                        payment.setUnPaidAmount(performancePlanDto.getWaitAmount());
                        payment.setLastPayTime(performancePlanDto.getLastPayTime());
                        payments.add(payment);
                    }
                }
                //合同回款进度
                contract.setPaymentProgress(judgePaymentProgress(contract.getContractMoney(), paidAmount));
                //未回款金额
                contract.setPendingPaymentMoney(contract.getContractMoney().subtract(paidAmount));
                contract.setContractPaymentList(payments);

                transField(contract);
            });
        }
        return contracts;
    }

    /**
     * 根据ID获取合同数据
     *
     * @param contractId
     * @return
     */
    public CrmContract queryById(Long contractId, OssPrivateFileUtil ossPrivateFileUtil) {
        CrmContract contract = CrmContract.dao.findById(contractId);
        if (Objects.isNull(contract)) {
            return null;
        }
        //商机数据
        CrmBusiness business = CrmBusiness.dao.findById(contract.getBusinessId());
        if (Objects.nonNull(business)) {
            contract.setBusinessName(business.getBusinessName());
            if (business.getCustomerId() != null) {
                contract.setCustomerId(business.getCustomerId().longValue());
            }
            if (business.getOwnerUserId() != null) {
                contract.setOwnerUserId(business.getOwnerUserId().longValue());
            }
        }

        //商品信息
        contract.setContractProductList(CrmContractProduct.dao.queryProductDetailByContractId(contractId));
        //回款条款
        contract.setContractPaymentList(CrmContractPayment.dao.listByContractId(contractId));
        //附件
        contract.setFiles(adminFileService.queryByBatchId(contract.getFileBatchId(), ossPrivateFileUtil));
        //计算回款进度
        calPaymentProgress(contract);

        transField(contract);

        return contract;
    }

    /**
     * 计算回款进度
     *
     * @param contract
     */
    private void calPaymentProgress(CrmContract contract) {
        // 调用履约接口
        List<PerformancePlanDto> performancePlanDtos = null;
        if (StringUtils.isNotBlank(contract.getContractNum())) {
            performancePlanDtos = performanceClient.listInstallmentBill(contract.getContractNum());
        }
        // 计算回款金额并转换参数类型
        BigDecimal paidAmount = BigDecimal.ZERO;
        Date lastPayTime = null;
        //遍历履约账单
        if (CollectionUtils.isNotEmpty(performancePlanDtos)) {
            for (PerformancePlanDto performancePlanDto : performancePlanDtos) {
                //计算合同的回款金额
                paidAmount = paidAmount.add(performancePlanDto.getAccumulateAmount());
                //比较最近回款日期
                boolean after = performancePlanDto.getLastPayTime() != null && (lastPayTime == null || performancePlanDto.getLastPayTime().compareTo(lastPayTime) > 0);
                if (after) {
                    lastPayTime = performancePlanDto.getLastPayTime();
                }
            }
        }
        //合同回款进度
        contract.setPaymentProgress(judgePaymentProgress(contract.getContractMoney(), paidAmount));
        //未回款金额
        contract.setPendingPaymentMoney(contract.getContractMoney().subtract(paidAmount));
        //最新回款时间
        contract.setLastPaymentDate(lastPayTime);
    }

    /**
     * 翻译合同字段
     *
     * @param contract
     */
    private void transField(CrmContract contract) {
        //审批状态
        CheckStatusEnum checkStatusEnum = CheckStatusEnum.findByCode(contract.getCheckStatus());
        if (Objects.nonNull(checkStatusEnum)) {
            contract.setCheckStatusName(checkStatusEnum.getDesc());
        }
        //客户数据
        if (StringUtils.isBlank(contract.getCustomerName())) {
            contract.setCustomerName(CrmCustomer.dao.queryNameByCustomerId(contract.getCustomerId()));
        }
        //负责人
        if (StringUtils.isBlank(contract.get("ownerUserName"))) {
            contract.setOwnerUserName(AdminUser.dao.queryNameByUserId(contract.get("ownerUserId")));
        }
        //签订人姓名
        if (StringUtils.isBlank(contract.getSignUserName())) {
            contract.setSignUserName(AdminUser.dao.queryNameByUserId(contract.getSignUserId()));
        }
        //签订人部门
        if (StringUtils.isBlank(contract.getSignDeptName())) {
            contract.setSignDeptName(AdminDept.dao.queryNameByDeptId(contract.getSignDeptId()));
        }
        //申请人姓名
        if (StringUtils.isBlank(contract.getApplyUserName())) {
            contract.setApplyUserName(AdminUser.dao.queryNameByUserId(contract.getApplyUserId()));
        }
    }

    /**
     * 判断回款进度
     * 若合同待回款金额=0，则回款进度=全部回款；
     * 若合同金额>合同待回款金额>0，则回款进度=部分回款；
     * 若合同待回款金额=合同金额，则回款进度=未回款。
     *
     * @param totalAmount 总金额
     * @param paidAmount  已回款金额
     * @return
     */
    private String judgePaymentProgress(BigDecimal totalAmount, BigDecimal paidAmount) {
        String paymentProgress;
        if (paidAmount.compareTo(totalAmount) >= 0) {
            paymentProgress = PaymentProgressEnum.ALL_PAID.getDesc();
        } else if (paidAmount.compareTo(BigDecimal.ZERO) <= 0) {
            paymentProgress = PaymentProgressEnum.UN_PAID.getDesc();
        } else {
            paymentProgress = PaymentProgressEnum.PARTIAL_PAID.getDesc();
        }
        return paymentProgress;
    }

    /**
     * 保存或者更新合同信息
     *
     * @param crmContract
     */
    @Before(Tx.class)
    public void addOrUpdateContract(CrmContract crmContract) {
        //验证合同参数
        validateContractParam(crmContract);

        Integer checkStatus = crmContract.getCheckStatus();

        //保存的时候，保证同步字段是没有值
        crmContract.setSyncStatus(0);
        crmContract.setSyncTime(null);

        //判断合同总金额与条款累加金额
        checkContractAmount(crmContract);
        //通过oa流水号查询是否存在，存在判断当前状态再更新
        CrmContract existContract = CrmContract.dao.findSingleByColumn("oa_num", crmContract.getOaNum());

        //审核中
        if (checkStatus == AUDIT_START) {
            //第一次传递为新增
            if (existContract == null) {
                // AUDIT_START.新增合同主表 2 新增合同付款条款表  3 新增合同商品表
                crmContract.setApplyTime(new Date());
                crmContract.save();
                addCrmContractPaymentList(crmContract.getContractPaymentList(), crmContract.getId().longValueExact(), crmContract.getBusinessId(),crmContract.getApplyUserId(),AUDIT_START);
                addCrmContractProductList(crmContract.getContractProductList(), crmContract.getId().longValueExact(), crmContract.getApplyUserId(),AUDIT_START);
            } else {
                //一次之后为更新
                updateContract(existContract, crmContract,AUDIT_START);
            }
        } else if (checkStatus == AUDIT_DONE) {
            //审核通过
            if (existContract == null){
                throw new CrmException("不存在oaNum：" + crmContract.getOaNum() + ",审核中的合同数据");
            }
            //保存合同附件
            saveContractFile(crmContract);
            //生成合同编号
            crmContract.setContractNum("SC" + DatePattern.PURE_DATETIME_FORMAT.format(new Date()) + RandomUtil.randomString(CrmConstant.BASE_CHAR, 4));
            crmContract.setExaminePassTime(new Date());
            updateContract(existContract, crmContract,AUDIT_DONE);

            //异步调用一次同步履约定时任务
            CrmThreadPool.INSTANCE.getInstance().execute(() ->crmTimeTaskService.transferContractPaymentToAgreement());
        }
    }

    /**
     * 验证合同参数
     * @param crmContract
     */
    private void validateContractParam(CrmContract crmContract){
        if (crmContract.getOaNum().length() > 100) {
            throw new CrmException("OA流水号 超长");
        }
        if (crmContract.getRequestId().length() > 100) {
            throw new CrmException("流程Id 超长");
        }
        if (crmContract.getContractName().length() > 100) {
            throw new CrmException("合同名称 超长");
        }
        if (crmContract.getContractTarget().length() > 100) {
            throw new CrmException("合同目的 超长");
        }
        if (crmContract.getPartyA() != null && crmContract.getPartyA().length() > 300) {
            throw new CrmException("甲方 超长");
        }
        if (crmContract.getPartyB() != null && crmContract.getPartyB().length() > 300) {
            throw new CrmException("乙方 超长");
        }
        if (crmContract.getPartyC() != null && crmContract.getPartyC().length() > 300) {
            throw new CrmException("丙方 超长");
        }
        if (crmContract.getPartyD() != null && crmContract.getPartyD().length() > 300) {
            throw new CrmException("丁方 超长");
        }
        if (crmContract.getContractDeadline() != null && crmContract.getContractDeadline().length() > 200) {
            throw new CrmException("合同期限 超长");
        }
    }

    /**
     * 检查合同总金额与条款累加金额是否一致
     * @param crmContract
     */
    private void checkContractAmount(CrmContract crmContract){
        BigDecimal contractMoney = crmContract.getContractMoney();
        if (contractMoney == null) {
            throw new CrmException("合同金额 不能为空");
        }
        List<CrmContractPayment> contractPaymentList = crmContract.getContractPaymentList();
        if (CollectionUtils.isEmpty(contractPaymentList)) {
            throw new CrmException("合同付款条款list不能为空");
        }

        BigDecimal totalAmount = contractPaymentList.stream().map(o -> Optional.ofNullable(o.getPaymentMoney()).orElse(BigDecimal.ZERO)).reduce(BigDecimal.ZERO, BigDecimal::add);

        if (contractMoney.compareTo(totalAmount) != 0){
            throw new CrmException("合同总金额 与条款累加金额 不一致");
        }
    }

    /**
     * 更新合同信息
     *
     * @param existContract
     * @param crmContract
     * @param checkStatus
     */
    private void updateContract(CrmContract existContract, CrmContract crmContract,Integer checkStatus) {
        if (existContract.getCheckStatus() != AUDIT_START) {
            throw new CrmException("CRM合同当前状态不是审核中，不能变更");
        }
        if (ObjectUtil.notEqual(crmContract.getBusinessId(), existContract.getBusinessId())) {
            throw new CrmException("合同不能变更商机ID");
        }
        if (ObjectUtil.notEqual(crmContract.getSiteMemberId(), existContract.getSiteMemberId())) {
            throw new CrmException("合同不能变更官网用户ID");
        }
        //1.更新合同主表 2 更新合同付款条款表 3 更新合同商品表
        crmContract.setId(existContract.getId());
        crmContract.setApplyTime(existContract.getApplyTime());
        crmContract.update();

        updateCrmContractPaymentList(crmContract.getContractPaymentList(), crmContract.getId().longValueExact(), crmContract.getBusinessId(),crmContract.getApplyUserId(),checkStatus);
        updateCrmContractProductList(crmContract.getContractProductList(), crmContract.getId().longValueExact(),crmContract.getApplyUserId(),checkStatus);
    }

    /**
     * 更新合同付款条款list
     *
     * @param contractPaymentList
     * @param contractId
     * @param businessId
     * @param applyUserId
     * @param checkStatus
     */
    private void updateCrmContractPaymentList(List<CrmContractPayment> contractPaymentList, Long contractId, Long businessId,Long applyUserId,Integer checkStatus) {
        if (CollectionUtils.isEmpty(contractPaymentList)) {
            throw new CrmException("合同付款条款list不能为空");
        }

        Set<Long> planIdSet = new HashSet<>();
        contractPaymentList.forEach(o -> {
            Long planId = o.getPlanId();
            if (planId == null) {
                //审核中，没有回款计划id则跳过
                if (checkStatus == AUDIT_START){
                    return;
                }
                log.info("合同付款条款list中，回款计划ID为空 开始新增回款计划");
                planId = addReceivablesPlan(o, contractId, businessId, applyUserId);
                o.setPlanId(planId);
            }
            if (!planIdSet.add(planId)) {
                throw new CrmException("合同付款条款list中，回款计划ID 不能重复");
            }
        });

        //当前存在的list
        List<CrmContractPayment> existContractPaymentList = CrmContractPayment.dao.findListByColumn("contract_id", contractId);

        //更新的list
        List<CrmContractPayment> updatePaymentList = contractPaymentList.stream()
                .filter(o -> existContractPaymentList.stream().anyMatch(o1 -> {
                    if (ObjectUtil.equal(o1.getPlanId(), o.getPlanId())){
                        o.setId(o1.getId());
                        return true;
                    }
                    return false;
                })).collect(Collectors.toList());
        updatePaymentList.forEach(o -> {
            if (o.getNum() == null) {
                throw new CrmException("合同付款条款list中，期数 不能为空");
            }
            if (o.getPaymentDate() == null) {
                throw new CrmException("合同付款条款list中，付款日期 不能为空");
            }
            if (o.getPaymentMoney() == null) {
                throw new CrmException("合同付款条款list中，付款金额 不能为空");
            }

            //审核通过时，更新回款计划信息
            if (checkStatus == AUDIT_DONE){
                //「计划回款日期」、「计划回款金额」和「付款条件」字段做更新  注：若OA侧相应字段值不为空，则做覆盖；若OA侧相应字段为空，则不变
                CrmReceivablesPlan receivablesPlan = CrmReceivablesPlan.dao.findById(o.getPlanId());
                if (receivablesPlan == null) {
                    throw new CrmException("合同付款条款list中，回款计划ID对应回款信息异常");
                }
                receivablesPlan.setReturnDate(o.getPaymentDate());
                receivablesPlan.setMoney(o.getPaymentMoney());
                if (StringUtils.isNotBlank(o.getPaymentCondition())){
                    receivablesPlan.setPaymentCondition(o.getPaymentCondition());
                }

                receivablesPlan.setContractId(Math.toIntExact(contractId));
                receivablesPlan.update();
            }

            o.update();
        });

        //新增的list
        List<CrmContractPayment> addPaymentList = contractPaymentList.stream()
                .filter(o -> updatePaymentList.stream().noneMatch(o1 -> ObjectUtil.equal(o1.getPlanId(), o.getPlanId()))).collect(Collectors.toList());
        if (addPaymentList.size() > 0) {
            addCrmContractPaymentList(addPaymentList, contractId, businessId,applyUserId,checkStatus);
        }

        //删除的list
        existContractPaymentList.stream()
                .filter(o -> updatePaymentList.stream().noneMatch(o1 -> ObjectUtil.equal(o1.getPlanId(), o.getPlanId())))
                .forEach(Model::delete);
    }

    /**
     * 更新合同商品信息list
     *
     * @param contractProductList
     * @param contractId
     * @param applyUserId
     * @param checkStatus
     */
    private void updateCrmContractProductList(List<CrmContractProduct> contractProductList, Long contractId,Long applyUserId,Integer checkStatus) {
        if (CollectionUtils.isEmpty(contractProductList)) {
            throw new CrmException("合同商品信息list不能为空");
        }

//        Set<String> productCodeSet = new HashSet<>();
//        Set<Long> productIdSet = new HashSet<>();

        contractProductList.forEach(o -> {


            Long productId = o.getProductId();
            /*if (productId != null && !productIdSet.add(productId)) {
                throw new CrmException("合同商品信息list中，产品Id 不能重复");
            }*/

            if (!String.valueOf(ProductCategoryEnum.CUSTOMER_NOTHING_KEY.ordinal()).equals(o.getProductType()) && StringUtils.isBlank(o.getProductCode())){
                throw new CrmException("合同商品信息list中，非定制化的 产品Code 不能为空");
            }
            /*if (!String.valueOf(ProductCategoryEnum.CUSTOMER_NOTHING_KEY.ordinal()).equals(o.getProductType()) && !productCodeSet.add(o.getProductCode())){
                throw new CrmException("合同商品信息list中，非定制化的 产品Code 不能重复");
            }*/
        });

        //当前存在的list
        List<CrmContractProduct> existContractProductList = CrmContractProduct.dao.findListByColumn("contract_id", contractId);

        //更新的list
        List<CrmContractProduct> updateProductList = contractProductList.stream()
                .filter(o -> existContractProductList.stream().anyMatch(o1 -> {
                    if (ObjectUtil.equal(o1.getProductId(), o.getProductId())){
                        o.setId(o1.getId());
                        return true;
                    }
                    return false;
                })).collect(Collectors.toList());
        updateProductList.forEach(o -> {
            if (o.getNum() == null) {
                throw new CrmException("合同付款条款list中，期数 不能为空");
            }
            if (o.getSalesMoney() == null) {
                throw new CrmException("合同付款条款list中，销售金额 不能为空");
            }

            o.update();
        });

        //新增的list
        List<CrmContractProduct> addPaymentList = contractProductList.stream()
                .filter(o -> updateProductList.stream().noneMatch(o1 -> ObjectUtil.equal(o1.getProductId(), o.getProductId()))).collect(Collectors.toList());
        if (addPaymentList.size() > 0) {
            addCrmContractProductList(addPaymentList, contractId,applyUserId,checkStatus);
        }

        //删除的list
        existContractProductList.stream()
                .filter(o -> updateProductList.stream().noneMatch(o1 -> ObjectUtil.equal(o1.getProductId(), o.getProductId())))
                .forEach(Model::delete);
    }

    /**
     * 新增合同付款条款list
     *
     * @param contractPaymentList
     * @param contractId
     * @param businessId
     * @param applyUserId
     * @param checkStatus 审核状态
     */
    private void addCrmContractPaymentList(List<CrmContractPayment> contractPaymentList, Long contractId, Long businessId,Long applyUserId,Integer checkStatus) {
        if (CollectionUtils.isEmpty(contractPaymentList)) {
            throw new CrmException("合同付款条款list不能为空");
        }

        Set<Long> planIdSet = new HashSet<>();

        //审核中，排除没有回款计划id的列表
        if (checkStatus == AUDIT_START) {
            contractPaymentList = contractPaymentList.stream().filter(o -> o.getPlanId() != null).collect(Collectors.toList());
        }

        contractPaymentList.forEach(o -> {
            Long planId = o.getPlanId();
            if (planId == null) {
                log.info("合同付款条款list中，回款计划ID为空 开始新增回款计划");
                planId = addReceivablesPlan(o, contractId, businessId, applyUserId);
                o.setPlanId(planId);
            }
            if (!planIdSet.add(planId)) {
                throw new CrmException("合同付款条款list中，回款计划ID 不能重复");
            }

            int count = CrmContractPayment.dao.countByColumn("plan_id", planId);
            if (count > 0) {
                throw new CrmException("合同付款条款list中，回款计划ID:" + planId + ",已经与其他合同关联");
            } else {
                CrmReceivablesPlan receivablesPlan = CrmReceivablesPlan.dao.findById(planId);
                if (receivablesPlan == null || (receivablesPlan.getBusinessId() != null && receivablesPlan.getBusinessId().longValue() != businessId)) {
                    throw new CrmException("合同付款条款list中，回款计划ID对应回款信息异常");
                }
            }

            if (o.getNum() == null) {
                throw new CrmException("合同付款条款list中，期数 不能为空");
            }
            if (o.getPaymentDate() == null) {
                throw new CrmException("合同付款条款list中，付款日期 不能为空");
            }
            if (o.getComputeMode() == null) {
                throw new CrmException("合同付款条款list中，计算方式 不能为空");
            }
            if (o.getPaymentMoney() == null) {
                throw new CrmException("合同付款条款list中，付款金额 不能为空");
            }

            o.setContractId(BigInteger.valueOf(contractId));
            o.setPaymentCode("p-" + IdUtil.getSnowflake(1, 1).nextId());

            o.save();
        });
    }

    /**
     * 新增付款条款中不存在的回款计划
     * @param contractPayment
     * @param contractId
     * @param businessId
     * @param applyUserId
     * @return
     */
    private Long addReceivablesPlan(CrmContractPayment contractPayment,Long contractId, Long businessId,Long applyUserId) {
        CrmBusiness businessInfo = CrmBusiness.dao.findById(businessId);
        if (businessInfo == null) {
            throw new CrmException("businessId:" + businessId + ",找不到商机数据");
        }
        CrmReceivablesPlan crmReceivablesPlan = new CrmReceivablesPlan();
        crmReceivablesPlan.setBusinessId(Math.toIntExact(businessId));
        crmReceivablesPlan.setCustomerId(businessInfo.getCustomerId());
        crmReceivablesPlan.setMoney(contractPayment.getPaymentMoney());
        crmReceivablesPlan.setReturnDate(contractPayment.getPaymentDate());
        crmReceivablesPlan.setCreateUserId(Math.toIntExact(applyUserId));
        crmReceivablesPlan.setContractId(Math.toIntExact(contractId));

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("entity", crmReceivablesPlan);
        R r = receivablesPlanService.saveAndUpdate(jsonObject, applyUserId);
        if (r.isFailed()) {
            throw new CrmException("保存新增回款计划失败");
        }
        JSONObject resultJsonObject = (JSONObject) JSON.toJSON(r.get("data"));
        return resultJsonObject.getLong("plan_id");
    }

    /**
     * 新增合同商品信息list
     *
     * @param contractProductList
     * @param contractId
     * @param applyUserId
     * @param checkStatus 审核状态
     */
    private void addCrmContractProductList(List<CrmContractProduct> contractProductList, Long contractId, Long applyUserId, Integer checkStatus) {
        if (CollectionUtils.isEmpty(contractProductList)) {
            throw new CrmException("合同商品信息list不能为空");
        }

//        Set<String> productCodeSet = new HashSet<>();
//        Set<Long> productIdSet = new HashSet<>();

        //审核中，排除没有产品Id的列表
        if (checkStatus == AUDIT_START) {
            contractProductList = contractProductList.stream().filter(o -> o.getProductId() != null).collect(Collectors.toList());
        }

        contractProductList.forEach(o -> {
            Long productId = o.getProductId();
            /*if (!productIdSet.add(productId)) {
                throw new CrmException("合同商品信息list中，产品Id 不能重复");
            }*/

            if (!String.valueOf(ProductCategoryEnum.CUSTOMER_NOTHING_KEY.ordinal()).equals(o.getProductType()) && StringUtils.isBlank(o.getProductCode())){
                throw new CrmException("合同商品信息list中，非定制化的 产品Code 不能为空");
            }
            /*if (!String.valueOf(ProductCategoryEnum.CUSTOMER_NOTHING_KEY.ordinal()).equals(o.getProductType()) && !productCodeSet.add(o.getProductCode())){
                throw new CrmException("合同商品信息list中，非定制化的 产品Code 不能重复");
            }*/

            if (o.getNum() == null) {
                throw new CrmException("合同付款条款list中，期数 不能为空");
            }
            if (o.getSalesMoney() == null) {
                throw new CrmException("合同付款条款list中，销售金额 不能为空");
            }

            if (productId == null) {
                log.info("合同商品信息list中，产品code为空 开始新增产品");
                productId = addCustomizedProduct(o, applyUserId);
                o.setProductId(productId);
            }

            o.setContractId(BigInteger.valueOf(contractId));

            o.save();
        });

    }

    /**
     * 新增定制化的产品信息
     * @param contractProduct
     * @param applyUserId
     * @return
     */
    private Long addCustomizedProduct(CrmContractProduct contractProduct, Long applyUserId) {
        if(StringUtils.isBlank(contractProduct.getProductType())){
            throw new CrmException("产品类型 值不能为空");
        }

        ProductCategoryEnum categoryEnum = ProductCategoryEnum.getEnumByOrdinal(Integer.parseInt(contractProduct.getProductType()));
        if (categoryEnum == null){
            throw new CrmException("产品类型 ：" + contractProduct.getProductType() + "，值异常");
        }
        //不是定制化的产品，则查询
        if (categoryEnum != ProductCategoryEnum.CUSTOMER_NOTHING_KEY){
            if (StringUtils.isBlank(contractProduct.getProductCode())){
                throw new CrmException("新增的产品，非定制化 产品code 不能为空");
            }
            CrmProduct product = CrmProduct.dao.findSingleByColumn("code", contractProduct.getProductCode());
            if (product == null) {
                throw new CrmException("合同商品信息list中,非定制化新增产品code：" + contractProduct.getProductCode() + ",找不到关联的商品");
            }
            return product.getProductId();
        }

        //以下是新增定制化产品的逻辑
        if (StringUtils.isBlank(contractProduct.getProductName())){
            throw new CrmException("新增的定制化产品，产品名称不能为空");
        }

        CrmProduct product = new CrmProduct();
        product.setName(contractProduct.getProductName());
        product.setCategoryCode(ProductCategoryEnum.CUSTOMER_NOTHING_KEY.getTypes());
        product.setCategoryId(ProductCategoryEnum.CUSTOMER_NOTHING_KEY.ordinal() + 1);
        product.setCreateUserId(Math.toIntExact(applyUserId));
        product.setStatus(CrmConstant.PRODUCT_PULL_ON);
        product.save();

        return product.getProductId();
    }

    /**
     * 保存合同附件信息
     * @param crmContract
     */
    private void saveContractFile(CrmContract crmContract){
        List<AdminFile> files = crmContract.getFiles();
        if (files == null || files.size() == 0){
            return;
        }

        String simpleUUID = IdUtil.simpleUUID();
        files.forEach(o ->{
            String filePath = o.getFilePath();
            if (StringUtils.isBlank(filePath)){
                throw new CrmException("附件路径 不能为空");
            }
            String name = o.getName();
            if (StringUtils.isBlank(name)){
                throw new CrmException("附件名称 不能为空");
            }
            Integer size = o.getSize();
            if (size == null){
                throw new CrmException("附件大小 不能为空");
            }

            o.setCreateUserId(Math.toIntExact(crmContract.getApplyUserId()));
            o.setBatchId(simpleUUID);
        });

        Db.batchSave(files,files.size());
        crmContract.setFileBatchId(simpleUUID);
    }
}
