package com.kakarote.crm9.erp.crm.service;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.jfinal.aop.Before;
import com.jfinal.aop.Inject;
import com.jfinal.kit.Kv;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.kakarote.crm9.common.config.paragetter.BasePageRequest;
import com.kakarote.crm9.erp.admin.service.AdminFieldService;
import com.kakarote.crm9.erp.crm.common.CrmErrorInfo;
import com.kakarote.crm9.erp.crm.common.contract.PaymentProgressEnum;
import com.kakarote.crm9.erp.crm.constant.CrmTagConstant;
import com.kakarote.crm9.erp.crm.entity.CrmReceivables;
import com.kakarote.crm9.erp.crm.entity.CrmReceivablesPlan;
import com.kakarote.crm9.erp.crm.entity.CrmReceivablesPlanProduct;
import com.kakarote.crm9.integration.client.PerformanceClient;
import com.kakarote.crm9.integration.dto.PerformancePlanDto;
import com.kakarote.crm9.utils.FieldUtil;
import com.kakarote.crm9.utils.R;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * 回款计划服务类
 * @author honglei.wan
 */
@Slf4j
public class CrmReceivablesPlanService {
    @Inject
    private FieldUtil fieldUtil;
    @Inject
    private AdminFieldService adminFieldService;
    
    @Inject
    private PerformanceClient performanceClient;

    /**
     * 添加或修改回款计划
     */
    public R saveAndUpdate(JSONObject jsonObject,Long userId) {
        CrmReceivablesPlan crmReceivablesPlan = jsonObject.getObject("entity", CrmReceivablesPlan.class);
        String batchId = StrUtil.isNotEmpty(crmReceivablesPlan.getFileBatch()) ? crmReceivablesPlan.getFileBatch() : IdUtil.simpleUUID();
        adminFieldService.save(jsonObject.getJSONArray("field"), batchId);
        if (null == crmReceivablesPlan.getPlanId()) {
            crmReceivablesPlan.setCreateTime(DateUtil.date());
            crmReceivablesPlan.setCreateUserId(userId == null ? null : userId.intValue());
            crmReceivablesPlan.setFileBatch(batchId);
            CrmReceivablesPlan receivablesPlan = CrmReceivablesPlan.dao.findFirst(Db.getSql("crm.receivablesplan.queryByBusinessId"), crmReceivablesPlan.getBusinessId());
            if (receivablesPlan == null) {
                crmReceivablesPlan.setNum("1");
            } else {
                crmReceivablesPlan.setNum(Integer.parseInt(receivablesPlan.getNum()) + 1 + "");
            }
            return Db.tx(() -> {
                crmReceivablesPlan.save();
                savePlanProduct(crmReceivablesPlan.getBusinessProds(),crmReceivablesPlan.getPlanId(),userId);
                return true;
            }) ? R.ok().put("data", Kv.by("plan_id", crmReceivablesPlan.getPlanId())) : R.error();
        } else {
            crmReceivablesPlan.setUpdateTime(DateUtil.date());
            return Db.tx(() -> {
                crmReceivablesPlan.update();
                Db.delete(Db.getSql("crm.receivablesplan.deletePlanProductIds"),crmReceivablesPlan.getPlanId());
                savePlanProduct(crmReceivablesPlan.getBusinessProds(),crmReceivablesPlan.getPlanId(),userId);
                return true;
            }) ? R.ok().put("data", Kv.by("plan_id", crmReceivablesPlan.getPlanId())) : R.error();
        }
    }

    /***
     * 存储回款计划关联商品
     * @author yue.li
     * @param planProductIds 关联商品ids
     * @param planId 回款计划planId
     * @param userId 创建人userId
     * @return
     */
    public void savePlanProduct(String planProductIds,Long planId,Long userId){
        if(StringUtils.isNotEmpty(planProductIds)){
            String[] idsArr = planProductIds.split(",");
            CrmReceivablesPlanProduct CrmReceivablesPlanProduct = new CrmReceivablesPlanProduct();
            for (String id : idsArr) {
                CrmReceivablesPlanProduct.clear();
                CrmReceivablesPlanProduct.setPlanProductId(IdUtil.simpleUUID());
                CrmReceivablesPlanProduct.setPlanId(planId == null ? null:planId.intValue());
                CrmReceivablesPlanProduct.setProductId(Integer.valueOf(id));
                CrmReceivablesPlanProduct.setCreateUserId(userId == null ? null:userId.intValue());
                CrmReceivablesPlanProduct.save();
            }
        }
    }

    /**
     * @author wyq
     * 删除回款计划
     */
    @Before(Tx.class)
    public R deleteById(Integer planId){
        List<Record> receivables = Db.find(Db.getSql("crm.receivablesplan.queryReceivablesByPlanId"), planId);
        if (CollectionUtils.isNotEmpty(receivables) && receivables.size() > 0) {
            return R.error(CrmErrorInfo.RECEIVABLE_NOT_NULL);
        }

        // delete receivable plan products
        Db.delete(Db.getSql("crm.receivablesplan.deletePlanProductIds"), planId);

        // remove plan
        boolean result = Db.delete(Db.getSql("crm.receivablesplan.deleteById"), planId) > 0;

        return result ? R.ok() : R.error();
    }

    /**
     * @author zxy
     * 查询回款自定义字段（新增）
     */
    public List<Record> queryField() {
        List<Record> fieldList = new ArrayList<>();
        String[] settingArr = new String[]{};
        fieldUtil.getFixedField(fieldList, "customerId", "客户名称", "", "customer", settingArr, 1);
        fieldUtil.getFixedField(fieldList, "businessId", "商机", "", "business", settingArr, 1);
        fieldUtil.getFixedField(fieldList, "money", "计划回款金额", "", "number", settingArr, 1);
        fieldUtil.getFixedField(fieldList, "returnDate", "计划回款日期", "", "date", settingArr, 1);
        fieldUtil.getFixedField(fieldList, "remind", "提前几天提醒", "", "number", settingArr, 0);
        fieldUtil.getFixedField(fieldList, "winRate", "赢率", "", "tag", settingArr, 1);
        fieldUtil.getFixedField(fieldList, "businessProds", "关联商品", "", "businessProds", settingArr, 0);
        fieldUtil.getFixedField(fieldList, "loseReason", "原因", "", "tag", settingArr, 0);
        fieldUtil.getFixedField(fieldList, "remark", "备注", "", "textarea", settingArr, 0);
        return fieldList;
    }

    /**
     * 编辑回款计划
     *
     * @param planId
     * @return
     */
    public List<Record> queryField(Integer planId) {
        List<Record> fieldList = new ArrayList<>();
        Record record = Db.findFirst(Db.getSql("crm.receivablesplan.queryReceivablesPlanByPlanId"), planId);
        if (record == null) {
            return Collections.emptyList();
        }

        String[] settingArr = new String[]{};
        fieldUtil.getFixedField(fieldList, "customerId", "客户名称", record.getInt("customerId"), "customer", settingArr, 1);
        fieldUtil.getFixedField(fieldList, "businessId", "商机", record.getInt("business_id"), "business", settingArr, 1);
        fieldUtil.getFixedField(fieldList, "money", "计划回款金额", record.getStr("money"), "number", settingArr, 1);
        fieldUtil.getFixedField(fieldList, "returnDate", "计划回款日期", DateUtil.formatDate(record.get("returnDate")), "date", settingArr, 1);
        fieldUtil.getFixedField(fieldList, "remind", "提前几天提醒", record.getInt("remind"), "number", settingArr, 0);
        fieldUtil.getFixedField(fieldList, "num", "期数", record.getStr("num"), "number", settingArr, 0);
        fieldUtil.getFixedField(fieldList, "winRate", "赢率", record.getStr("winRateId"), "tag", settingArr, 1);
        fieldUtil.getFixedField(fieldList, "businessProds", "关联商品", record.getStr("prodIds"), "businessProds", settingArr, 0);
        fieldUtil.getFixedField(fieldList, "loseReason", "原因", record.getStr("loseReasonId"), "tag", settingArr, 0);
        fieldUtil.getFixedField(fieldList, "remark", "备注", record.getStr("remark"), "textarea", settingArr, 0);
        return fieldList;
    }

    /**
     * 根据合同查询回款计划
     */
    public R qureyListByContractId(BasePageRequest<CrmReceivables> basePageRequest) {

        Integer pageType = basePageRequest.getPageType();
        if (pageType == null || 0 == pageType) {
            return R.ok().put("data", Db.find(Db.getSql("crm.receivablesplan.queryListByContractId"), basePageRequest.getData().getContractId()));
        }
        if (1 == pageType) {
            return R.ok().put("data", Db.paginate(basePageRequest.getPage(), basePageRequest.getLimit(), Db.getSqlPara("crm.receivablesplan.queryListByContractId", basePageRequest.getData().getContractId())));
        }
        return R.error();
    }

    /**
     * 根据合同id和客户id查询未使用的回款计划
     */
    public R queryByContractAndCustomer(CrmReceivablesPlan receivablesPlan) {
        List<CrmReceivablesPlan> plans = CrmReceivablesPlan.dao.find(Db.getSql("crm.receivablesplan.queryByCustomerIdContractId"), receivablesPlan.getContractId(), receivablesPlan.getCustomerId());
        return R.ok().put("data", plans);
    }

    /**
     * 根据id查询回款计划基本信息
     *
     */
    public List<Record> information(Integer id) {
        Record record = Db.findFirst(Db.getSql("crm.receivablesplan.queryReceivablesPlanByPlanId"), id);
        if (record == null) {
            return Collections.emptyList();
        }
        List<Record> fieldList = new ArrayList<>();
        FieldUtil field = new FieldUtil(fieldList);
        field.set("期数", record.getStr("num"))
                .set("客户名称", record.getStr("customerName"))
                .set("计划回款日期", DateUtil.formatDate(record.getDate("returnDate")))
                .set("计划回款金额", record.getStr("money"))
                .set("提前几天提醒", record.getStr("remind"))
                .set("赢率", record.getStr("winRate"));

        if(StringUtils.isNotEmpty(record.getStr("winRate")) && record.getStr("winRate").equals(CrmTagConstant.LOST)) {
            field.set("原因", record.getStr("lose_reason"));
        }

        field.set("商机名称", record.getStr("business_name"))
                .set("负责人", record.getStr("realname"))
                .set("关联商品", record.getStr("prods"))
                .set("备注", record.getStr("remark"));
        return fieldList;
    }


    /**
     * 根据id查询回款计划信息
     *
     */
    public Record getById(Integer id) throws Exception {
        Record record = Db.findFirst(Db.getSql("crm.receivablesplan.queryReceivablesPlanByPlanId"), id);
        record.set("planId", id);
        // 失败原因	
        record.set("loseReason", record.getStr("lose_reason"));
        // 冗余产品信息
        record.set("businessProds", record.getStr("prods"));
        // 回款进度
        // 待回款金额
        // 最新回款日期
        return this.replenishBilling(record);
    }
    
    /**
     * 根据履约接口填充字段
     * 回款进度
     * 待回款金额
     * 最新回款日期
     * @throws ParseException 
     */
    private Record replenishBilling (Record record) throws Exception {

    	List<String> planNoList = new ArrayList<String>();
		String paymentCode = record.getStr("paymentCode");
		if(StringUtils.isNoneEmpty(paymentCode)) {
    		planNoList.add(paymentCode);
		}
    	if (CollectionUtils.isNotEmpty(planNoList)) {

        	List<PerformancePlanDto> performancePlanDtos = new ArrayList<PerformancePlanDto>();
			try {
				performancePlanDtos = performanceClient.listInstallmentBill(null, planNoList.toArray(new String[planNoList.size()]));
				log.info("获取履约回款计划列表数据成功 -> performanceClient -> listInstallmentBil->输入参数：{}，返回报文：{}",JSON.toJSONString(planNoList), JSON.toJSONString(performancePlanDtos));
			} catch (Exception e) {
				// 履约接口异常时，降级处理，只返回crm信息
				log.error("获取履约回款计划列表数据失败 -> performanceClient -> listInstallmentBil->输入参数：{}",JSON.toJSONString(planNoList) ,e);
				// 告警处理 TODO
			}
			
			// 最新支付时间
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			Date latestPaymentDateLast = sdf.parse("1900-01-01 00:00:00");
			// 计划回款金额总计
			BigDecimal PaymentMoneySum = BigDecimal.ZERO;
			// 待回款金额总计
			BigDecimal pendingPaymentMoneySum = BigDecimal.ZERO;
			if (CollectionUtils.isNotEmpty(performancePlanDtos)) {
				for (PerformancePlanDto performancePlanDto : performancePlanDtos) {
        			if (StringUtils.isNotEmpty(performancePlanDto.getBizNo()) && performancePlanDto.getBizNo().equals(record.getStr("paymentCode"))) {

        				pendingPaymentMoneySum = pendingPaymentMoneySum.add(Objects.nonNull(performancePlanDto.getWaitAmount()) ? performancePlanDto.getWaitAmount() : BigDecimal.ZERO);
        				PaymentMoneySum = PaymentMoneySum.add(Objects.nonNull(performancePlanDto.getTotalAmount()) ? performancePlanDto.getTotalAmount() : BigDecimal.ZERO);
        				// 冒泡获取最新回款时间
        				if (Objects.nonNull(performancePlanDto.getLastPayTime()) && !latestPaymentDateLast.after(performancePlanDto.getLastPayTime())) {
        					latestPaymentDateLast = performancePlanDto.getLastPayTime();
        				}
        				
        			}
        		}

				/*
				 *  若待回款金额=0，则回款进度=全部回款；
				 *	若计划回款金额>待回款金额>0，则回款进度=部分回款；
				 *	若待回款金额=计划回款金额，则回款进度=未回款。
				 */
		        // 回款进度
				if (BigDecimal.ZERO.compareTo(pendingPaymentMoneySum) == 0) {
			        record.set("paymentProgress", PaymentProgressEnum.ALL_PAID.getDesc()); 
				} else if (BigDecimal.ZERO.compareTo(pendingPaymentMoneySum) == -1 && pendingPaymentMoneySum.compareTo(PaymentMoneySum) == -1) {
					record.set("paymentProgress", PaymentProgressEnum.PARTIAL_PAID.getDesc()); 
				} else if (pendingPaymentMoneySum.compareTo(PaymentMoneySum) == 0) {
					record.set("paymentProgress", PaymentProgressEnum.UN_PAID.getDesc()); 
				} else {
					record.set("paymentProgress", ""); 
				}

		        // 待回款金额
		        record.set("pendingPaymentMoney", pendingPaymentMoneySum.setScale(2, BigDecimal.ROUND_HALF_UP).toString()); 
		        // 最新回款日期
		        record.set("latestPaymentDate", sdf.parse("1900-01-01 00:00:00").compareTo(latestPaymentDateLast) != 0 ? latestPaymentDateLast : "");
			} else {
	    		record.set("paymentProgress", PaymentProgressEnum.UN_PAID.getDesc()); 
	    		record.set("pendingPaymentMoney",record.getStr("money"));
	    		record.set("latestPaymentDate", "");
			}
			
    	} else {
    		record.set("paymentProgress", ""); 
    		record.set("pendingPaymentMoney","");
    		record.set("latestPaymentDate", "");
    	}
    	return record;
    }

    /**
     * 根据回款计划ID获取回款计划
     * @param planId 回款计划ID
     * @return 回款计划
     */
    public Record getPlanById(Integer planId) {
        return Db.findFirst(Db.getSql("crm.receivablesplan.getPlanById"), planId);
    }
}
