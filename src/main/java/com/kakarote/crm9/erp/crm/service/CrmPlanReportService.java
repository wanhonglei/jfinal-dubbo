package com.kakarote.crm9.erp.crm.service;

import com.jfinal.aop.Aop;
import com.jfinal.aop.Inject;
import com.jfinal.kit.Kv;
import com.jfinal.log.Log;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.SqlPara;
import com.kakarote.crm9.common.config.paragetter.BasePageRequest;
import com.kakarote.crm9.erp.admin.entity.CrmBusinessGroup;
import com.kakarote.crm9.erp.admin.service.AdminDeptService;
import com.kakarote.crm9.erp.admin.service.AdminUserService;
import com.kakarote.crm9.erp.crm.common.CrmPlanEnum;
import com.kakarote.crm9.erp.crm.constant.CrmConstant;
import com.kakarote.crm9.erp.crm.entity.CrmPlanReport;
import com.kakarote.crm9.erp.crm.entity.CrmPlanReportResult;
import com.kakarote.crm9.utils.BaseUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * CrmPlanReportService service
 *
 * @author yue.li
 */
public class CrmPlanReportService {

    @Inject
    private AdminDeptService adminDeptService;

    private Log logger = Log.getLog(getClass());

    /****
     * 获取PPL报表
     */
    public CrmPlanReportResult queryPlanReportList(BasePageRequest<CrmPlanReport> request,Long userId,boolean isPage) {
        CrmPlanReportResult result = new CrmPlanReportResult();
        String businessTypeId = request.getData().getBusinessTypeId();
        String businessUserId = request.getData().getBusinessUserId();
        String dealStartDate = request.getData().getDealStartDate();
        String dealEndDate =  request.getData().getDealEndDate();
        String createStartTime = request.getData().getCreateStartTime();
        String createEndTime = request.getData().getCreateEndTime();
        String mapAddress = request.getData().getMapAddress();
        Long deptId = request.getData().getDeptId();
        Long groupId = request.getData().getGroupId();
        String statusId = request.getData().getStatusId();
        Record totalMoneyRecord = new Record();
        Record totalReceiveMoneyRecord = new Record();
        String isEnd = null;
        Long groupDeptId = null;
        List<Long> groupIds = new ArrayList<>();
        if(StringUtils.isNotEmpty(statusId)){
            if(CrmPlanEnum.WIN_TYPE_KEY.getTypes().equals(statusId)){
                isEnd = CrmConstant.ONE_FLAG;
                statusId = null;
            }
            if(CrmPlanEnum.LOSE_TYPE_KEY.getTypes().equals(statusId)){
                isEnd = CrmConstant.TWO_FLAG;
                statusId = null;
            }
        }
        // 根据事业部获取商机组集合
        List<CrmBusinessGroup> crmBusinessGroupList = getBusinessGroupByDeptId(deptId);
        if(CollectionUtils.isNotEmpty(crmBusinessGroupList)) {
            for(CrmBusinessGroup crmBusinessGroup : crmBusinessGroupList) {
                groupIds.add(crmBusinessGroup.getDeptIds());
            }
        }
        // 商机组ID获取部门ID
        CrmBusinessGroup crmBusinessGroup = selectDeptIdByGroupId(groupId);
        if(Objects.nonNull(crmBusinessGroup)) {
            groupDeptId = crmBusinessGroup.getDeptIds();
        }

        // 查询数据权限
        List<Long> longs= Aop.get(AdminUserService.class).queryUserByAuth(userId);
        Page<Record> paginate = new Page<Record>();
        Kv kv = Kv.by("businessTypeId", businessTypeId);
        kv.set("businessUserId", businessUserId)
        .set("dealStartDate", dealStartDate).set("dealEndDate", dealEndDate)
        .set("groupId",groupDeptId).set("createStartTime",createStartTime)
        .set("createEndTime",createEndTime).set("isEnd",isEnd).set("owner_user_id",longs)
        .set("mapAddress",mapAddress).set("groupIds",groupIds).set("statusId",statusId);
        SqlPara sqlPara = Db.getSqlPara("crm.business.queryPlanReportList", kv);
        if(isPage){
            paginate = Db.paginate(request.getPage(), request.getLimit(),sqlPara);
        }else{
            SqlPara sqlDetailPara = Db.getSqlPara("crm.business.queryPlanReportDetailList", kv);
            paginate.setList(Db.find(sqlDetailPara));
        }
        if(paginate.getList() != null && paginate.getList().size() >0){
            for(Record record:paginate.getList()){
                record.set("productName",getProductInfoByBusinessId(record.getStr("businessId")));
                record.set("receiveblesMoney",getReceiveblesMoneyByBusinessId(record.getStr("businessId")));
                record.set("money",getReceiveblesPlanMoneyByBusinessId(record.getStr("businessId")));
            }
            // 计划回款合计
            totalMoneyRecord = Db.findFirst(Db.getSqlPara("crm.business.queryPlanReportSumMoneyList", kv));
            // 实际回款合计
            totalReceiveMoneyRecord = Db.findFirst(Db.getSqlPara("crm.business.queryPlanReportSumReceivablesMoneyList", kv));
        }
        result.setTotalReceiveblesMoney(totalReceiveMoneyRecord == null ? "":totalReceiveMoneyRecord.getStr("money"));
        result.setTotalMoney(totalMoneyRecord == null ? "":totalMoneyRecord.getStr("money"));
        result.setPageResult(paginate);
        return result;
    }

    /***
     * 根据商机ID获取商品信息
     * @author yue.li
     * @param businessId 商机ID
     */
    public String getProductInfoByBusinessId(String businessId){
        List<Record> productList = Db.find(Db.getSql("crm.business.selectProductInfoByBusinessId"),businessId);
        String productName = null;
        if(productList != null && productList.size() >0){
            for(Record productRecord:productList){
                if(productName == null){
                    productName =  productRecord.getStr("categoryName") + "-" + productRecord.getStr("name") ;
                }else{
                    productName += "," + productRecord.getStr("categoryName") + "-" + productRecord.getStr("name") ;
                }
            }
        }
        return productName == null ? "":productName;
    }

    /***
     * 根据客户ID获取客户信息
     * @author yue.li
     * @param customerId 客户ID
     */
    public String getCustomerInfoByCustomerId(String customerId){
        Record customerRecord= Db.findFirst(Db.getSql("crm.customer.queryByCustomerId"),customerId);
        return customerRecord == null? "": customerRecord.getStr("customer_name");
    }

    /***
     * 根据商机ID获取实际回款金额
     * @author yue.li
     * @param businessId 商机ID
     */
    public String getReceiveblesMoneyByBusinessId(String businessId){
        Record receivablesRecord= Db.findFirst(Db.getSql("crm.business.queryReceivablesByBusinessId"), businessId);
        return receivablesRecord.getStr("money") == null ? "" : receivablesRecord.getStr("money");
    }

    /***
     * 根据商机ID获取计划回款金额
     * @author yue.li
     * @param businessId 商机ID
     */
    public String getReceiveblesPlanMoneyByBusinessId(String businessId){
        Record receivablesPlanRecord= Db.findFirst(Db.getSql("crm.business.queryReceivablesPlanByBusinessId"), businessId);
        return receivablesPlanRecord.getStr("money") == null ? "" : receivablesPlanRecord.getStr("money");
    }
    /**
     * 查询回款信息专为报表展示
     * @author yue.li
     * @param businessId 商机ID
     * @return
     */
    public List<Record> queryReceivablesForPlanReportList(Integer businessId) {
        List<Record> list = new ArrayList<>();
        try{
            list = Db.find(Db.getSql("crm.business.queryReceivablesInfoByBusinessId"),businessId);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            if(list != null && list.size() >0){
                for(Record record:list){
                    record.set("planReturnDate",sdf.format(sdf.parse(record.getStr("planReturnDate"))));
                    record.set("returnTime",getMaxReceivablesDateByPlanId(record.getStr("planId")));
                    record.set("money",getMaxReceivablesMoneyByPlanId(record.getStr("planId")));
                }
            }
        }catch(Exception e){
            logger.error(String.format("queryReceivablesForPlanReportList msg:%s", BaseUtil.getExceptionStack(e)));
        }
        return list;
    }

    /**
     * 获取最大的回款日期根据预计回款计划ID
     * @author yue.li
     * @param planId 预计回款计划ID
     * @return
     */
    public String getMaxReceivablesDateByPlanId(String planId){
        Record receivablesRecord = Db.findFirst(Db.getSql("crm.business.getMaxReceivablesDateByPlanId"), planId);
        return receivablesRecord.getStr("returnTime") == null ? "" : receivablesRecord.getStr("returnTime");
    }

    /**
     * 获取最大的回款日期根据预计回款计划ID
     * @author yue.li
     * @param planId 预计回款计划ID
     * @return
     */
    public String getMaxReceivablesMoneyByPlanId(String planId){
        Record receivablesRecord = Db.findFirst(Db.getSql("crm.business.getMaxReceivablesMoneyByPlanId"), planId);
        return receivablesRecord.getStr("money") == null ? "" : receivablesRecord.getStr("money");
    }

    /**
     * 根据事业部ID获取商机组信息
     * @author yue.li
     * @param deptId 事业部ID
     */
    public List<CrmBusinessGroup> getBusinessGroupByDeptId(Long deptId) {
        if(Objects.isNull(deptId)) {
            return CrmBusinessGroup.dao.find(Db.getSqlPara("crm.business.getBusinessGroupByDeptId"));
        }

        List<Long> businessGroupDeptIds = adminDeptService.queryAllSonDeptIds(deptId);
        if(CollectionUtils.isEmpty(businessGroupDeptIds)){
            return Collections.emptyList();
        }
        Kv kv = Kv.by("businessGroupDeptIds", businessGroupDeptIds);
        List<CrmBusinessGroup> crmBusinessGroupList = CrmBusinessGroup.dao.find(Db.getSqlPara("crm.business.getBusinessGroupByDeptId", kv));
        if(CollectionUtils.isNotEmpty(crmBusinessGroupList)) {
            return crmBusinessGroupList;
        }else{
            return Collections.emptyList();
        }
    }

    /**
     * 根据商机组获取商机阶段
     * @author yue.li
     * @param groupId 商机组ID
     */
    public List<Record> getBusinessStatusListByGroupId(Integer groupId) {
        List<Record> recordList = Db.find(Db.getSql("crm.business.getBusinessStatusListByGroupId"), groupId);
        if(CollectionUtils.isEmpty(recordList)) {
            return Collections.emptyList();
        }
        return constructBusinessStatus(recordList);
    }

    /**
     * 封装阶段添加赢单输单
     * @author yue.li
     * @param recordList 阶段集合
     */
    public List<Record> constructBusinessStatus(List<Record> recordList) {
        Record winRecord = new Record();
        winRecord.set("statusId", CrmPlanEnum.WIN_TYPE_KEY.getTypes());
        winRecord.set("statusName", CrmPlanEnum.WIN_TYPE_KEY.getName());
        recordList.add(winRecord);
        Record loseRecord = new Record();
        loseRecord.set("statusId", CrmPlanEnum.LOSE_TYPE_KEY.getTypes());
        loseRecord.set("statusName", CrmPlanEnum.LOSE_TYPE_KEY.getName());
        recordList.add(loseRecord);
        return recordList;
    }

    /**
     * 报表获取事业部
     * @author yue.li
     */
    public List<Record> getDeptList() {
        List<Record> resultList = Db.find(Db.getSql("crm.business.getDeptList"));

        if(CollectionUtils.isEmpty(resultList)) {
            return Collections.emptyList();
        }
        for(Record record : resultList) {
            record.set("deptName",adminDeptService.getDeptNameTree(record.getLong("deptId")));
        }
        return resultList;
    }

    /**
     * 根据商机组ID获取商机组对应的信息
     * @author yue.li
     * @param groupId 商机组ID
     */
    private CrmBusinessGroup selectDeptIdByGroupId(Long groupId) {
        return CrmBusinessGroup.dao.findFirst(Db.getSql("crm.businessGroup.selectDeptIdByGroupId"),groupId);
    }

    /**
     * 根据事业部ID获取商机组及商机组对应事业部信息
     * @author yue.li
     * @param deptId 事业部ID
     */
    public List<Record> getBusinessGroupContainsDeptByDeptId(Long deptId) {
        List<Record> recordList = new ArrayList<>();
        List<CrmBusinessGroup> crmBusinessGroupList = getBusinessGroupByDeptId(deptId);
        if(CollectionUtils.isEmpty(crmBusinessGroupList)) {
            return Collections.emptyList();
        }

        for(CrmBusinessGroup crmBusinessGroup : crmBusinessGroupList) {
            if(Objects.nonNull(crmBusinessGroup.getDeptIds())) {
                String businessDepartmentId = adminDeptService.getBusinessDepartmentByDeptId(String.valueOf(crmBusinessGroup.getDeptIds()));
                Record record = new Record();
                record.set("id",crmBusinessGroup.getId());
                record.set("name",crmBusinessGroup.getName());
                record.set("deptIds",StringUtils.isNotEmpty(businessDepartmentId) ? Integer.valueOf(businessDepartmentId) : null);
                recordList.add(record);
            }
        }
        return recordList;
    }
}