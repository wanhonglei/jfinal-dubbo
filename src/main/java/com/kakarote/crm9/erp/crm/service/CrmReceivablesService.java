package com.kakarote.crm9.erp.crm.service;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jfinal.aop.Before;
import com.jfinal.aop.Inject;
import com.jfinal.kit.Kv;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.kakarote.crm9.common.config.paragetter.BasePageRequest;
import com.kakarote.crm9.erp.admin.service.AdminFieldService;
import com.kakarote.crm9.erp.crm.common.CrmEnum;
import com.kakarote.crm9.erp.crm.dto.CrmVerificationInfoDTO;
import com.kakarote.crm9.erp.crm.entity.CrmReceivables;
import com.kakarote.crm9.erp.crm.entity.CrmReceivablesPlan;
import com.kakarote.crm9.integration.client.PerformanceClient;
import com.kakarote.crm9.integration.common.PaymentTypeEnum;
import com.kakarote.crm9.integration.dto.PerformanceVerificationDto;
import com.kakarote.crm9.utils.FieldUtil;
import com.kakarote.crm9.utils.R;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 回款信息服务类
 * @author honglei.wan
 */
@Slf4j
public class CrmReceivablesService {

    @Inject
    private AdminFieldService adminFieldService;

    @Inject
    private FieldUtil fieldUtil;

    @Inject
    private CrmRecordService crmRecordService;
    
    @Inject
    private PerformanceClient performanceClient;

    /**
     * 获取用户审核通过和未通过的回款
     *
     * @param userId 用户ID
     * @author HJP
     */
    public List<CrmReceivables> queryListByUserId(Integer userId) {
        CrmReceivables crmReceivables = new CrmReceivables();
        String sql = "select re.receivables_id,re.contract_id,c.`name`,r.check_time,r.check_user_id,u.username,re.check_status from 72crm_crm_receivables re"
                + "left join 72crm_crm_contract c"
                + "on c.contract_id=re.contract_id"
                + "left join 72crm_admin_examine_step s"
                + "on re.flow_id=s.flow_id and re.order_id=s.order_id"
                + "left join 72crm_admin_examine_record r"
                + "on s.flow_id=r.flow_id and s.step_id=r.step_id"
                + "left join 72crm_admin_user u"
                + "on r.check_user_id=u.id"
                + "where re.check_status=2 or re.check_status=3 and re.create_user_id=" + userId;
        return crmReceivables.find(sql);
    }

    /**
     * 分页查询回款
     */
    public Page<Record> queryPage(BasePageRequest<CrmReceivables> basePageRequest) {
        return Db.paginate(basePageRequest.getPage(), basePageRequest.getLimit(), Db.getSqlPara("crm.receivables.getReceivablesPageList"));
    }

    /**
     * 新建或者修改回款
     *
     * @param jsonObject
     */
    @Before(Tx.class)
    public R saveOrUpdate(JSONObject jsonObject,Long userId) {
        if (userId == null) {
            return R.error("用户id不能为空！");
        }

        R result;
        CrmReceivables crmReceivable = jsonObject.getObject("entity", CrmReceivables.class);

        // update customized fields
        String batchId = StrUtil.isNotEmpty(crmReceivable.getBatchId()) ? crmReceivable.getBatchId() : IdUtil.simpleUUID();
        crmReceivable.setBatchId(batchId);
        updateCustomizedField(jsonObject.getJSONArray("field"), batchId);

        if (Objects.isNull(crmReceivable.getReceivablesId())) {
            // add receivable record
            result = addReceivableRecord(crmReceivable, userId.intValue());

            // add operation record
            crmRecordService.addRecord(crmReceivable.getReceivablesId().intValue(), CrmEnum.RECEIVABLES_TYPE_KEY.getTypes(), userId);
        } else {
            // update receivable record
            crmReceivable.setCheckStatus(0);
            crmReceivable.setUpdateTime(DateUtil.date());
            result = crmReceivable.update() ? R.ok() : R.error();

            // update operation record
            crmRecordService.updateRecord(new CrmReceivables().dao().findById(crmReceivable.getReceivablesId()), crmReceivable, CrmEnum.RECEIVABLES_TYPE_KEY.getTypes(),userId);
        }
        // update receivable plan
        updateReceivablePlanStatus(crmReceivable.getPlanId(), crmReceivable.getReceivablesId());

        return result;
    }

    /**
     * Update customized field and add operation record.
     *
     * @param fields
     * @param batchId
     */
    private void updateCustomizedField(JSONArray fields, String batchId) {
        crmRecordService.updateRecord(fields, batchId);
        adminFieldService.save(fields, batchId);
    }

    private R addReceivableRecord(CrmReceivables crmReceivable, Integer loginUserId) {
        crmReceivable.setCreateTime(DateUtil.date());
        crmReceivable.setUpdateTime(DateUtil.date());
        crmReceivable.setCheckStatus(0);
        crmReceivable.setCreateUserId(loginUserId);
        crmReceivable.setOwnerUserId(loginUserId);
        return crmReceivable.save() ? R.ok() : R.error();
    }

    /**
     * If there is a new receivable, update receivable plan to indicate that the plan has a latest receivable.
     *
     * @param planId
     * @param receivableId
     */
    private void updateReceivablePlanStatus(Integer planId, Long receivableId) {
        CrmReceivablesPlan crmReceivablesPlan = CrmReceivablesPlan.dao.findById(planId);
        if (crmReceivablesPlan != null) {
            crmReceivablesPlan.setReceivablesId(receivableId.intValue());
            crmReceivablesPlan.setUpdateTime(DateUtil.date());
            crmReceivablesPlan.update();
        }
    }

    /**
     * 根据id查询回款
     */
    public R queryById(Integer id) {
//        Record record = Db.findFirst(Db.getSqlPara("crm.receivables.queryReceivablesById", Kv.by("id", id)));
        Record record = Db.findFirst(Db.getSqlPara("crm.business.queryReceivablesPageListNoView", Kv.by("receivablesId", id)));
        
        return R.ok().put("data", record);
    }

    /**
     * 根据id查询回款基本信息
     */
    public List<Record> information(Integer id) {
//        Record record = Db.findFirst(Db.getSqlPara("crm.receivables.queryReceivablesById", Kv.by("id", id)));
//        if (record == null) {
//            return null;
//        }
//        List<Record> fieldList = new ArrayList<>();
//        FieldUtil field = new FieldUtil(fieldList);
//        field.set("回款编号", record.getStr("receivables_id"))
//                .set("客户名称", record.getStr("customer_name"))
//                .set("回款日期", DateUtil.formatDate(record.getDate("return_time")))
//                .set("回款金额", record.getStr("money"))
//                .set("期数", record.getStr("plan_num"))
//                .set("备注", record.getStr("remark"));
//        List<Record> fields = adminFieldService.list("7");

//        Record record = Db.findFirst(Db.getSql("crm.business.queryReceivablesPageListNoView"),null,id);
        Record record = Db.findFirst(Db.getSqlPara("crm.business.queryReceivablesPageListNoView", Kv.by("receivablesId", id)));
        if (record == null) {
            return null;
        }
        List<Record> fieldList = new ArrayList<>();
        FieldUtil field = new FieldUtil(fieldList);
        field.set("回款编号", record.getStr("receivablesNum"))
        		.set("回款金额", record.getStr("receivablesMoney"))
                .set("网站会员id", record.getStr("siteMemberIds"))
                .set("客户CID", record.getStr("crmCustomerId"))
                .set("付款人名称", record.getStr("payName"))
                .set("回款时间", DateUtil.formatDate(record.getDate("returnTime")))
//                .set("客户名称", record.getStr("customerName"))
//                .set("关联期数", record.getStr("num"))
                .set("支付方式", record.getStr("payType") == null? "": formatePayType(Integer.valueOf(record.getStr("payType"))))
                .set("支付流水号", record.getStr("paymentNo"))
                .set("支付备注", record.getStr("remark"));
        List<Record> fields = adminFieldService.list("9");
        for (Record r:fields){
            field.set(r.getStr("name"),record.getStr(r.getStr("name")));
        }
        return fieldList;
    }

    /**
     * 销售类型转码
     * @param payTypeCode
     * @return
     */
    private String formatePayType(Integer payTypeCode) {
    	
    	return PaymentTypeEnum.getPaymentTypeNameInCrm(PaymentTypeEnum.getPaymentTypeDefinedInCrm(payTypeCode));
    }
    /**
     * 根据id删除回款
     */
    @Before(Tx.class)
    public R deleteByIds(String receivablesIds) {
        String[] idsArr = receivablesIds.split(",");
        List<Record> idsList = new ArrayList<>(idsArr.length);
        List<Integer> receivablesId = new ArrayList<>(idsArr.length);
        for (String id : idsArr) {
            Record record = new Record();
            idsList.add(record.set("receivables_id", Integer.valueOf(id)));
            receivablesId.add(Integer.valueOf(id));
        }
        return Db.tx(() -> {

            //批量更新bops_payment
            if (receivablesId.size() > 0) {
                Integer[] ints = new Integer[receivablesId.size()];
                receivablesId.toArray(ints);
                Db.update(Db.getSqlPara("crm.payment.batchUpdatePayment", Kv.by("status", 1).set("ids", ints)));
            }
            
            Db.batch(Db.getSql("crm.receivables.deleteByIds"), "receivables_id", idsList, 100);
            return true;
        }) ? R.ok() : R.error();
    }

    /**
     * @author zxy
     * 查询回款自定义字段(新增)
     */
    public List<Record> queryField() {
        List<Record> fieldList = new ArrayList<>();
        String[] settingArr = new String[]{};
        fieldUtil.getFixedField(fieldList, "customerId", "客户名称", "", "customer", settingArr, 1);
        fieldUtil.getFixedField(fieldList, "businessId", "商机", "", "business", settingArr, 1);
        fieldUtil.getFixedField(fieldList, "returnTime", "回款日期", "", "date", settingArr, 1);
        fieldUtil.getFixedField(fieldList, "money", "回款金额", "", "floatnumber", settingArr, 1);
        fieldUtil.getFixedField(fieldList, "planId", "期数", "", "receivables_plan", settingArr, 0);
        //TODO
//        fieldUtil.getFixedField(fieldList, "businessProds", "关联商品", "", "businessProds", settingArr, 0);
        fieldUtil.getFixedField(fieldList, "remark", "备注", "", "textarea", settingArr, 0);
        fieldList.addAll(adminFieldService.list(CrmEnum.RECEIVABLES_TYPE_KEY.getTypes()));
        return fieldList;
    }

    /**
     * @author zxy
     * 查询回款自定义字段(修改)
     */
    public List<Record> queryField(Integer receivablesId) {
        List<Record> fieldList = new ArrayList<>();
        Record record = Db.findFirst("select * from receivablesview where receivables_id = ?", receivablesId);
        String[] settingArr = new String[]{};
        fieldUtil.getFixedField(fieldList, "number", "回款编号", record.getStr("receivables_id"), "number", settingArr, 1);
        List<Record> customerList = new ArrayList<>();
        Record customer = new Record();
        customerList.add(customer.set("customerId", record.getInt("customer_id")).set("customerName", record.getStr("customer_name")));
        fieldUtil.getFixedField(fieldList, "customerId", "客户名称", customerList, "customer", settingArr, 1);

        customerList = new ArrayList<>();
        customer = new Record();
        customerList.add(customer.set("contractId", record.getStr("contract_id")).set("contract_num", record.getStr("contract_num")));
        fieldUtil.getFixedField(fieldList, "returnTime", "回款日期", DateUtil.formatDate(record.get("return_time")), "date", settingArr, 1);
        fieldUtil.getFixedField(fieldList, "money", "回款金额", record.getStr("money"), "floatnumber", settingArr, 1);
        fieldUtil.getFixedField(fieldList, "planId", "期数", record.getInt("plan_id"), "receivables_plan", settingArr, 0);
        fieldUtil.getFixedField(fieldList, "remark", "备注", record.getStr("remark"), "textarea", settingArr, 0);
        fieldList.addAll(adminFieldService.queryByBatchId(record.getStr("batch_id")));
        return fieldList;
    }

    /**
     * 根据条件查询回款
     */
    public List<Record> queryList(CrmReceivables receivables) {
        String sq = "select * from 72crm_crm_receivables where 1 = 1 ";
        StringBuilder sql = new StringBuilder(sq);
        if (receivables.getCustomerId() != null) {
            sql.append(" and customer_id = ").append(receivables.getCustomerId());
        }
        if (receivables.getContractId() != null) {
            sql.append(" and contract_id = ").append(receivables.getContractId());
        }
        return Db.find(sql.toString());
    }

    /**
     * 根据条件查询回款
     */
    public List<Record> queryListByType(String type, Integer id) {
        String sq = "select * from 72crm_crm_receivables where ";
        StringBuilder sql = new StringBuilder(sq);
        if (type.equals(CrmEnum.CUSTOMER_TYPE_KEY.getTypes())) {
            sql.append("  customer_id = ? ");
        }
        if (type.equals(CrmEnum.CONTRACT_TYPE_KEY.getTypes())) {
            sql.append("  contract_id = ? ");
        }

        return Db.find(sql.toString(), id);
    }

    /**
     * 查询核销信息
     * @param paymentCode
     * @param orderNo
     * @return
     */
    public List<CrmVerificationInfoDTO> verificationInfo(String paymentCode, String orderNo) {


    	List<CrmVerificationInfoDTO> crmVerificationInfoDTOList = new ArrayList<>();
    	if (StringUtils.isNoneEmpty(orderNo)) {
    		// 查询订单支付信息
    		List<Record> records = Db.find(Db.getSql("crm.receivables.queryByOrderNo"), orderNo);
    		if (CollectionUtils.isNotEmpty(records)) {
    			for (int i=0; i<records.size(); i++) {

    				CrmVerificationInfoDTO crmVerificationInfoDTO = new CrmVerificationInfoDTO();
    				crmVerificationInfoDTO.setOrderNo(orderNo);
    				crmVerificationInfoDTO.setPaymentCode("");
    				crmVerificationInfoDTO.setType("订单");
    				crmVerificationInfoDTO.setVerificationDate(records.get(i).getDate("pay_time"));
    				crmVerificationInfoDTO.setVerificationMoney(records.get(i).getStr("total_amount"));
    				crmVerificationInfoDTOList.add(crmVerificationInfoDTO);
    			}
    		}
    		
    		return crmVerificationInfoDTOList;
    	}
    	List<PerformanceVerificationDto> performanceVerificationDtoList = new ArrayList<>();
    	try {
            if (StringUtils.isNotBlank(paymentCode)) {
                performanceVerificationDtoList = performanceClient.listInstallmentVerification(null, paymentCode);
            }
		} catch (Exception e) {
			// fail silent 履约接口异常时降级处理：吞掉异常，返回空list
			log.error("CrmReceivablesService -> verificationInfo -> 获取履约核销信息异常接口名:performanceClient.listInstallmentVerification,参数planNo:"+paymentCode, e);
			// 告警处理 TODO
		}
    	if (!CollectionUtils.isEmpty(performanceVerificationDtoList)) {
			for (int i=0;i<performanceVerificationDtoList.size();i++) {

				PerformanceVerificationDto performanceVerificationDto = performanceVerificationDtoList.get(i);
				if (Objects.isNull(performanceVerificationDto)) {
					continue;
				}
				CrmVerificationInfoDTO crmVerificationInfoDTO = new CrmVerificationInfoDTO();
				crmVerificationInfoDTO.setOrderNo(orderNo);
				crmVerificationInfoDTO.setPaymentCode(paymentCode);
				crmVerificationInfoDTO.setType(StringUtils.isNoneEmpty(paymentCode) ? "合同" : "订单");
				crmVerificationInfoDTO.setVerificationDate(performanceVerificationDto.getFinishTime());
				if (!Objects.isNull(performanceVerificationDto.getVerificationAmount())) {
					crmVerificationInfoDTO.setVerificationMoney(performanceVerificationDto.getVerificationAmount().setScale(0, BigDecimal.ROUND_HALF_UP).toString());
				}
				crmVerificationInfoDTOList.add(crmVerificationInfoDTO);
			}
    	}
    	return crmVerificationInfoDTOList;
    }

    /**
     * 根据计划id查询产品列表
     * @param planIds
     * @return
     */
    public List<Record> findProductByPlanIds(List<Long> planIds){
        return Db.find(Db.getSqlPara("crm.product.findProductByPlanIds",Kv.by("planIds",planIds)));
    }
}
