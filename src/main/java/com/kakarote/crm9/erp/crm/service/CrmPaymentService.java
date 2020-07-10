package com.kakarote.crm9.erp.crm.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import com.kakarote.crm9.erp.crm.common.CrmPayTypeEnum;
import org.apache.commons.lang3.StringUtils;

import com.alibaba.fastjson.JSONObject;
import com.jfinal.aop.Before;
import com.jfinal.aop.Inject;
import com.jfinal.kit.Kv;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.SqlPara;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.kakarote.crm9.common.config.paragetter.BasePageRequest;
import com.kakarote.crm9.common.constant.BaseConstant;
import com.kakarote.crm9.erp.admin.common.AdminEnum;
import com.kakarote.crm9.erp.admin.service.AdminDeptService;
import com.kakarote.crm9.erp.admin.service.AdminSceneService;
import com.kakarote.crm9.erp.admin.service.AdminUserService;
import com.kakarote.crm9.erp.crm.constant.CrmConstant;
import com.kakarote.crm9.erp.crm.dto.CrmPaymentDto;
import com.kakarote.crm9.erp.crm.entity.BopsPayment;
import com.kakarote.crm9.erp.crm.entity.CrmReceivables;
import com.kakarote.crm9.erp.crm.entity.CrmReceivablesPlan;
import com.kakarote.crm9.utils.BaseUtil;
import com.kakarote.crm9.utils.R;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;

/**
 * Crm Payment Service
 *
 * @author hao.fu
 * @create 2019/10/24 14:36
 */
public class CrmPaymentService {

    @Inject
    private AdminUserService adminUserService;

    @Inject
    private AdminDeptService adminDeptService;

    @Inject
    private AdminSceneService adminSceneService;

    @Inject
    private CrmReceivablesPlanService crmReceivablesPlanService;

    @Before(Tx.class)
    public R bindPayment(JSONObject request) {
        String bopsPaymentId = request.getString("bopsPaymentId");
        Integer crmCustomerId = request.getInteger("crmCustomerId");
        Integer businessId = request.getInteger("businessId");
        Integer planId = request.getInteger("planId");

        if (Objects.isNull(bopsPaymentId) || Objects.isNull(crmCustomerId) || Objects.isNull(businessId) || Objects.isNull(planId)) {
            return R.error("参数非法，请检查！");
        }

        Record record = Db.findFirst(Db.getSql("crm.payment.getPaymentByBopsPaymentId"), bopsPaymentId);
        if (record == null) {
            return R.error(String.format("绑定失败！回款信息不存在: %s", bopsPaymentId));
        }
        BopsPayment payment = new BopsPayment()._setAttrs(record.getColumns());

        Record receviableRecord = Db.findFirst(Db.getSql("crm.receivables.getReceivableByBopsPaymentId"), bopsPaymentId);
        if (receviableRecord != null) {
            return R.error("该回款已经被关联过商机，一条回款记录不能被关联多个商机！");
        }

        // create a receivable record
        CrmReceivables receivables = new CrmReceivables();
        receivables.setBopsPaymentId(bopsPaymentId);
        receivables.setCustomerId(crmCustomerId);
        receivables.setPlanId(planId);
        receivables.setBusinessId(businessId);
        Integer userId = BaseUtil.getUserId() == null ? null : BaseUtil.getUserId().intValue();
        receivables.setCreateUserId(userId);
        receivables.setOwnerUserId(userId);
        receivables.setMoney(payment.getTotalAmount());
        receivables.setReturnTime(payment.getPayTime());
        receivables.setCreateTime(new Date());
        receivables.setUpdateTime(new Date());
        String batchId = StrUtil.isNotEmpty(receivables.getBatchId()) ? receivables.getBatchId() : IdUtil.simpleUUID();
        receivables.setBatchId(batchId);
        return receivables.save() && updatePaymentStatus(bopsPaymentId, CrmConstant.PAYMENT_STATUS_BIND_RECEIVABLE_PLAN) ? R.ok() : R.error("关联失败！");
    }

    @Before(Tx.class)
    public R bindCustomerManually(String custId, String bopsPaymentId) {
        if (StringUtils.isEmpty(custId) || StringUtils.isEmpty(bopsPaymentId)) {
            return R.error("参数非法！");
        }

        Record record = Db.findFirst(Db.getSql("crm.payment.getPaymentByBopsPaymentId"), bopsPaymentId);
        if (Objects.nonNull(record)) {
            BopsPayment payment = new BopsPayment()._setAttrs(record.getColumns());
            if (payment.getCrmCustomerId() != null) {
                return R.error("该回款已经被绑定，一条回款记录不能被绑定多次！");
            }
        }

        boolean bindResult = Db.update(Db.getSql("crm.payment.bindCustomerManually"), custId, bopsPaymentId) > 0;
        boolean updateStatusResult = updatePaymentStatus(bopsPaymentId, CrmConstant.PAYMENT_STATUS_BIND_CUSTOMER);
        return bindResult && updateStatusResult ? R.ok() : R.error("绑定客户失败！");
    }

    public boolean updatePaymentStatus(String bopsPaymentId, int status) {
        return Db.update(Db.getSql("crm.payment.updatePaymentStatus"), status, bopsPaymentId) > 0;
    }

    /***
     * 查询回款管理列表
     * @author yue.li
     */
    public Page<Record> queryPaymentPageList(BasePageRequest<CrmPaymentDto> basePageRequest, Long userId) {

        String search = basePageRequest.getData().getSearch();
        String sceneId = basePageRequest.getData().getSceneId();
        String payStartTime = basePageRequest.getData().getPayStartTime();
        String payEndTime = basePageRequest.getData().getPayEndTime();
        String state = basePageRequest.getData().getState();
        Record record =  Db.findFirst(Db.getSql("admin.scene.queryBySceneId"), sceneId);
        List<String> crmPayTypes = new ArrayList<>();
        crmPayTypes.add(CrmPayTypeEnum.CONSUME_KEY.getTypes());
        crmPayTypes.add(CrmPayTypeEnum.REFUND_KEY.getTypes());
        Kv kv = Kv.by("search", search).set("payStartTime",payStartTime).set("payEndTime",payEndTime).set("state",state).set("crmPayTypes",crmPayTypes);
        if(record != null) {
            String sceneName = record.get("name");
            /*我负责的回款*/
            if(AdminEnum.MY_OWNER_RECEIVABLES_KEY.getName().equals(sceneName)) {
                kv.set("owner_user_id",userId).set("state",CrmConstant.PAYMENT_STATUS_BIND_CUSTOMER).set("ro_user_id","," +userId+ ",").set("rw_user_id","," +userId+ ",");
            }
            /*下属负责的回款*/
            if(AdminEnum.MY_BRANCH_RECEIVABLES_KEY.getName().equals(sceneName)) {
                List<Long> userIds = adminUserService.queryUserByParentUser(BaseUtil.getUserId(), BaseConstant.AUTH_DATA_RECURSION_NUM);
                kv.set("user_ids",userIds).set("state",CrmConstant.PAYMENT_STATUS_BIND_CUSTOMER);
                /*若无下属情况*/
                if(userIds == null || userIds.size() == 0) {
                    kv.set("no_branch","true");
                }
            }
            /*部门的回款*/
            if(AdminEnum.MY_DEPT_RECEIVABLES_KEY.getName().equals(sceneName)) {
                Record userRecord = Db.findFirst(Db.getSql("admin.user.queryUserByUserId"), userId);
                String deptId = adminDeptService.getBusinessDepartmentByDeptId(userRecord.getStr("dept_id"));
                if(StringUtils.isNotEmpty(deptId)) {
                    List<Long> ownerUserIds = adminUserService.queryMyDeptAndSubUserByDeptId(Integer.valueOf(deptId));
                    kv.set("owner_user_ids",ownerUserIds);
                }
                kv.set("dept_id",deptId).set("state",CrmConstant.PAYMENT_STATUS_BIND_CUSTOMER);
            }
            /*未关联客户的回款*/
            if(AdminEnum.NO_CUSTOMER_RECEIVABLES_KEY.getName().equals(sceneName)) {
                kv.set("state",CrmConstant.PAYMENT_STATUS_UNBIND_CUSTOMER);
            }
        }
        SqlPara sqlPara = Db.getSqlPara("crm.payment.queryPaymentList", kv);
        Page<Record> recordPage = Db.paginate(basePageRequest.getPage(), basePageRequest.getLimit(),sqlPara );
        recordPage = adminSceneService.formatReceivablesManagement(recordPage);
        recordPage = adminSceneService.formatCrmPayType(recordPage);
        return recordPage;
    }

    public R updateReceivablePlan(JSONObject request) {
        Integer planId = request.getInteger("planId");
        Integer winRateId = request.getInteger("winRateId");
        Integer loseReasonId = request.getInteger("loseReasonId");
        String remark = request.getString("remark");

        if (Objects.isNull(planId)) {
            return R.error("回款计划ID不能为空！");
        }

        Record record = crmReceivablesPlanService.getPlanById(planId);
        if (record != null) {
            CrmReceivablesPlan crmReceivablesPlan = new CrmReceivablesPlan()._setAttrs(record.getColumns());
            crmReceivablesPlan.setWinRate(winRateId + "");
            crmReceivablesPlan.setLoseReason(loseReasonId + "");
            crmReceivablesPlan.setRemark(remark);

            crmReceivablesPlan.update();
            return R.ok();
        }

        return R.error("未找到回款计划");
    }

    /***
     * 解绑
     * @author yue.li
     * @param bopsPaymentId 支付信息id
     */
    @Before(Tx.class)
    public R relieve(String bopsPaymentId,String crmCustomerId,String partnerAccount) {
        return Db.tx(() -> {
            Db.update(Db.getSql("crm.payment.relieve"), CrmConstant.PAYMENT_STATUS_UNBIND_CUSTOMER, bopsPaymentId);
            Db.delete(Db.getSql("crm.receivables.deleteByBopsPaymentId"),bopsPaymentId);
            Db.delete(Db.getSql("crm.receivables.deleteChannelByCustomerId"),crmCustomerId,partnerAccount);
            return true;
        }) ? R.ok() : R.error();
    }

    public Record queryByBopsPaymentId(String bopsPaymentId) {
        return Db.findFirst(Db.getSql("crm.payment.getPaymentByBopsPaymentId"), bopsPaymentId);
    }
}
