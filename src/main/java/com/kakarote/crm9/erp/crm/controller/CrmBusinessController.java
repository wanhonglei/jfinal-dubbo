package com.kakarote.crm9.erp.crm.controller;

import com.alibaba.excel.EasyExcel;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.jfinal.aop.Before;
import com.jfinal.aop.Inject;
import com.jfinal.core.Controller;
import com.jfinal.core.paragetter.Para;
import com.jfinal.log.Log;
import com.jfinal.plugin.activerecord.Record;
import com.kakarote.crm9.common.annotation.HttpEnum;
import com.kakarote.crm9.common.annotation.LogApiOperation;
import com.kakarote.crm9.common.annotation.NotNullValidate;
import com.kakarote.crm9.common.annotation.Permissions;
import com.kakarote.crm9.common.config.paragetter.BasePageRequest;
import com.kakarote.crm9.common.exception.CrmException;
import com.kakarote.crm9.common.spring.IocInterceptor;
import com.kakarote.crm9.erp.admin.entity.AdminRecord;
import com.kakarote.crm9.erp.admin.entity.AdminUser;
import com.kakarote.crm9.erp.admin.service.AdminDeptService;
import com.kakarote.crm9.erp.admin.service.CrmBusinessGroupService;
import com.kakarote.crm9.erp.crm.constant.CrmConstant;
import com.kakarote.crm9.erp.crm.entity.CrmBusiness;
import com.kakarote.crm9.erp.crm.entity.CrmReceivables;
import com.kakarote.crm9.erp.crm.listener.CrmBusinessDataListener;
import com.kakarote.crm9.erp.crm.service.CrmBusinessService;
import com.kakarote.crm9.erp.crm.service.CrmNotesService;
import com.kakarote.crm9.utils.BaseUtil;
import com.kakarote.crm9.utils.OssPrivateFileUtil;
import com.kakarote.crm9.utils.R;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Before(IocInterceptor.class)
public class CrmBusinessController extends Controller {

    private Log logger = Log.getLog(getClass());

    @Inject
    private CrmBusinessService crmBusinessService;

    @Autowired
    private OssPrivateFileUtil ossPrivateFileUtil;

    @Inject
    private CrmNotesService crmNotesService;

    @Inject
    private CrmBusinessGroupService crmBusinessGroupService;

    @Inject
    private AdminDeptService adminDeptService;

    /**
     * @author wyq
     * 分页条件查询商机
     */
    public void queryList(BasePageRequest basePageRequest){
        try{
            renderJson(R.ok().put("data",crmBusinessService.getBusinessPageList(basePageRequest)));
        }catch (Exception e){
            logger.error(String.format("queryList business error msg:%s",BaseUtil.getExceptionStack(e)));
            renderJson(R.error(e.getMessage()));

        }

    }

    /**
     * @author wyq
     * 新增或更新商机
     */
    @Permissions({"crm:business:save","crm:business:update"})
    public void addOrUpdate(){
        try{
            JSONObject jsonObject = JSON.parseObject(getRawData());
            renderJson(crmBusinessService.addOrUpdate(jsonObject,BaseUtil.getUserId()));
        }catch (Exception e){
            logger.error(String.format("addOrUpdate business error msg:%s",BaseUtil.getExceptionStack(e)));
            renderJson(R.error(e.getMessage()));
        }
    }

    /**
     * @author wyq
     * 老接口
     * 根据商机id查询
     */
    @Permissions("crm:business:read")
    @NotNullValidate(value = "businessId",message = "商机id不能为空")
    public void queryById(@Para("businessId")Integer businessId){
        try{
            renderJson(R.ok().put("data",crmBusinessService.queryById(businessId)));
        }catch (Exception e){
            logger.error(String.format("queryById business error msg:%s",BaseUtil.getExceptionStack(e)));
            renderJson(R.error(e.getMessage()));
        }
    }

    /**
     * 获取商机详情
     * 新接口（只查询了页面需要的字段,不走businessView）
     * @param businessId
     */
    @Permissions("crm:business:read")
    @NotNullValidate(value = "businessId",message = "商机id不能为空")
    public void getById(Integer businessId) {
        renderJson(R.ok().put("data",crmBusinessService.getById(businessId)));
    }

    /**
     * @author wyq
     * 根据商机名称查询
     */
    @NotNullValidate(value = "name",message = "名称不能为空")
    public void queryByName(@Para("name")String name){
        try{
            renderJson(R.ok().put("data",crmBusinessService.queryByName(name)));
        }catch (Exception e){
            logger.error(String.format("queryByName business error msg:%s",BaseUtil.getExceptionStack(e)));
            renderJson(R.error(e.getMessage()));

        }
    }

    /**
     * @author wyq
     * 根据商机id查询产品
     */
    public void queryProduct(BasePageRequest<CrmBusiness> basePageRequest){
        try{
        	// 入参check，商机id不能为空 
        	// fail fast
            if (Objects.isNull(basePageRequest) || Objects.isNull(basePageRequest.getData()) || Objects.isNull(basePageRequest.getData().getBusinessId())) {
            	renderJson(R.error("商机id不能为null"));
            	return;
            }
            renderJson(crmBusinessService.queryProduct(basePageRequest));
        }catch (Exception e){
            logger.error(String.format("queryProduct business error msg:%s",BaseUtil.getExceptionStack(e)));
            renderJson(R.error(e.getMessage()));

        }
    }

    /**
     * @author wyq
     * 根据商机id查询合同
     */
    public void queryContract(BasePageRequest<CrmBusiness> basePageRequest){
        renderJson(crmBusinessService.queryContract(basePageRequest));
    }

    /**
     * @author wyq
     * 根据商机id查询联系人
     */
    public void queryContacts(BasePageRequest<CrmBusiness> basePageRequest){
        renderJson(crmBusinessService.queryContacts(basePageRequest));
    }

    /**
     * @author wyq
     * 根据id删除商机
     */
    @Permissions("crm:business:delete")
    @NotNullValidate(value = "businessIds",message = "商机id不能为空")
    public void deleteByIds(@Para("businessIds")String businessIds){
        try{
            renderJson(crmBusinessService.deleteByIds(businessIds));
        }catch (Exception e){
            logger.error(String.format("deleteByIds business error msg:%s",BaseUtil.getExceptionStack(e)));
            renderJson(R.error(e.getMessage()));

        }
    }

    /**
     * @author wyq
     * 根据商机id变更负责人
     */
    @Permissions("crm:business:transfer")
    @NotNullValidate(value = "businessIds",message = "商机id不能为空")
    @NotNullValidate(value = "newOwnerUserId",message = "负责人id不能为空")
    @NotNullValidate(value = "transferType",message = "移除方式不能为空")
    @LogApiOperation(methodName = "商机分派")
    public void transfer(@Para("")CrmBusiness crmBusiness){
        try{
            renderJson(crmBusinessService.transfer(crmBusiness));
        }catch (Exception e){
            logger.error(String.format("queryProduct business error msg:%s",BaseUtil.getExceptionStack(e)));
            renderJson(R.error(e.getMessage()));

        }
    }

    /**
     * @author wyq
     * 查询团队成员
     */
    @NotNullValidate(value = "businessId",message = "商机id不能为空")
    public void getMembers(@Para("businessId")Integer businessId){
        try{
            renderJson(R.ok().put("data",crmBusinessService.getMembers(businessId)));
        }catch (Exception e){
            logger.error(String.format("getMembers business error msg:%s",BaseUtil.getExceptionStack(e)));
            renderJson(R.error(e.getMessage()));

        }
    }

    /**
     * @author wyq
     * 添加团队成员
     */
    @Permissions("crm:business:teamsave")
    @NotNullValidate(value = "ids",message = "商机id不能为空")
    @NotNullValidate(value = "memberIds",message = "成员id不能为空")
    @NotNullValidate(value = "power",message = "读写权限不能为空")
    public void addMembers(@Para("")CrmBusiness crmBusiness){
        try{
            renderJson(crmBusinessService.addMember(crmBusiness));
        }catch (Exception e){
            logger.error(String.format("addMembers business error msg:%s",BaseUtil.getExceptionStack(e)));
            renderJson(R.error(e.getMessage()));

        }
    }

    /**
     * @author wyq
     * 编辑团队成员
     */
    @Permissions("crm:business:teamsave")
    @NotNullValidate(value = "ids",message = "商机id不能为空")
    @NotNullValidate(value = "memberIds",message = "成员id不能为空")
    @NotNullValidate(value = "power",message = "读写权限不能为空")
    public void updateMembers(@Para("")CrmBusiness crmBusiness){
        try{
            renderJson(crmBusinessService.addMember(crmBusiness));
        }catch (Exception e){
            logger.error(String.format("updateMembers business error msg:%s",BaseUtil.getExceptionStack(e)));
            renderJson(R.error(e.getMessage()));

        }
    }

    /**
     * @author wyq
     * 删除团队成员
     */
    @Permissions("crm:business:teamsave")
    @NotNullValidate(value = "ids",message = "商机id不能为空")
    @NotNullValidate(value = "memberIds",message = "成员id不能为空")
    public void deleteMembers(@Para("")CrmBusiness crmBusiness){
        try{
            renderJson(crmBusinessService.deleteMembers(crmBusiness));
        }catch (Exception e){
            logger.error(String.format("deleteMembers business error msg:%s",BaseUtil.getExceptionStack(e)));
            renderJson(R.error(e.getMessage()));
        }
    }

    /**
     * @author
     * 商机状态组展示
     */
    @NotNullValidate(value = "businessId",message = "商机id不能为空")
    public void queryBusinessStatus(@Para("businessId")Integer businessId){
        try{
            renderJson(R.ok().put("data",crmBusinessService.queryBusinessStatus(businessId)));
        }catch (Exception e){
            logger.error(String.format("queryBusinessStatus business error msg:%s",BaseUtil.getExceptionStack(e)));
            renderJson(R.error(e.getMessage()));
        }
    }

    @NotNullValidate(value = "businessId",message = "商机id不能为空")
    public void queryBusinessStatusNew(Long businessId) {
        try {
            renderJson(R.ok().put("data", crmBusinessService.queryBusinessStatusNew(businessId)));
        } catch (CrmException e) {
            renderJson(R.error(e.getMessage()));
        }
    }

    /**
     * @author wyq
     * 商机状态组推进
     */
    @NotNullValidate(value = "businessId",message = "商机id不能为空")
    public void boostBusinessStatus(@Para("")CrmBusiness crmBusiness){
        try{
            renderJson(crmBusinessService.boostBusinessStatus(crmBusiness));
        }catch (Exception e){
            logger.error(String.format("boostBusinessStatus business error msg:%s",BaseUtil.getExceptionStack(e)));
            renderJson(R.error(e.getMessage()));
        }
    }

    /**
     * @author wyq
     * 查询自定义字段
     */
    public void queryField(){
        renderJson(R.ok().put("data",crmBusinessService.queryField()));
    }

    /**
     * @author wyq
     * 查询商机状态组及商机状态
     */
    public void queryBusinessStatusOptions(){
        try{
            renderJson(R.ok().put("data",crmBusinessService.queryBusinessStatusOptions(null)));
        }catch (Exception e){
            logger.error(String.format("queryBusinessStatusOptions business error msg:%s",BaseUtil.getExceptionStack(e)));
            renderJson(R.error(e.getMessage()));
        }
    }

    /**
     * @author wyq
     * 添加跟进记录
     */
    @NotNullValidate(value = "typesId",message = "商机id不能为空")
    @NotNullValidate(value = "content",message = "内容不能为空")
    @Permissions("crm:notes:save")
    public void addRecord(@Para("")AdminRecord adminRecord){
        try{
            adminRecord.setCreateUserId(BaseUtil.getUserId().intValue());
            R r = crmNotesService.addRecord(adminRecord, CrmConstant.CRM_BUSINESS);
            renderJson(r);
        }catch (Exception e){
            logger.error(String.format("addRecord business error msg:%s",BaseUtil.getExceptionStack(e)));
            renderJson(R.error(e.getMessage()));
        }
    }

    /**
     * @author wyq
     * 查看跟进记录
     */
    @Permissions("crm:notes:index")
    public void getRecord(BasePageRequest<CrmBusiness> basePageRequest){
        try{
            renderJson(R.ok().put("data",crmNotesService.getRecord(basePageRequest, ossPrivateFileUtil, CrmConstant.CRM_BUSINESS)));
        }catch (Exception e){
            logger.error(String.format("addRecord business error msg:%s",BaseUtil.getExceptionStack(e)));
            renderJson(R.error(e.getMessage()));
        }
    }

    /**
     * @author chaokun.ding
     * 获取商机阶段
     */
    public void getStatysById(@Para("deptId")String deptId,@Para("businessId")Long businessId) {
        try {
            renderJson(R.ok().put("data", crmBusinessService.getStatysById(deptId, businessId)));
        } catch (Exception e) {
            logger.error(String.format("getStatysById business error msg:%s", BaseUtil.getExceptionStack(e)));
            renderJson(R.error(e.getMessage()));
        }
    }

    /**
     * 根据商机ID查询回款计划
     * @author chaokun.ding
     */
    @Permissions("crm:receivables:index")
    public void qureyReceivablesPlanListByBusinessId(@Para("businessId")Integer businessId, 
    		@Para("contractIds")String contractIds, 
    		@Para("pageType")Integer pageType, 
    		@Para("hasReleativeContract")String hasReleativeContract,
    		@Para("checkStatus")String checkStatus){
        try{
            renderJson(crmBusinessService.qureyListByBusinessId(businessId, contractIds,pageType,hasReleativeContract, checkStatus));
        }catch (Exception e){
            logger.error(String.format("qureyReceivablesPlanListByBusinessId business error msg:%s",BaseUtil.getExceptionStack(e)));
            renderJson(R.error(e.getMessage()));
        }
    }
    /**
     * 根据商机ID查询回款
     * @author zxy
     */
    @Permissions("crm:receivables_plan:index")
    public void qureyReceivablesListByBusinessId(BasePageRequest<CrmReceivables> basePageRequest){
        try{
            renderJson(crmBusinessService.qureyReceivableListByBusinessId(basePageRequest));
        }catch (Exception e){
            logger.error(String.format("qureyReceivablesListByBusinessId business error msg:%s",BaseUtil.getExceptionStack(e)));
            renderJson(R.error(e.getMessage()));
        }
    }

    /**
     * 根据商机ID查询回款（去除视图）
     * @param basePageRequest
     */
    @Permissions("crm:receivables_plan:index")
    public void qureyReceivablesListByBusinessIdNoView(BasePageRequest<CrmReceivables> basePageRequest){
        try{
            renderJson(crmBusinessService.qureyReceivableListByBusinessIdNoView(basePageRequest));
        }catch (Exception e){
            logger.error(String.format("qureyReceivablesListByBusinessIdNoView business error msg:%s",BaseUtil.getExceptionStack(e)));
            renderJson(R.error(e.getMessage()));
        }
    }

    /**
     *  根据产品大类获取产品列表
     * @author yue.li
     *
     */
    public void getProductListByCategory(@Para("category")String category){
        try{
            renderJson(R.ok().put("data",crmBusinessService.getProductList(category)));
        }catch (Exception e){
            logger.error(String.format("getProductListByCategory business error msg:%s",BaseUtil.getExceptionStack(e)));
            renderJson(R.error(e.getMessage()));
        }
    }

    /**
     * 商机列表页查询
     */
    @Permissions("crm:business:index")
    public void queryBusinessPageList(BasePageRequest basePageRequest) {
        try {
            // 超管无数据权限根据场景管控
            AdminUser adminUser = BaseUtil.getUser();
            logger.info("#######admin scene service user info: " + adminUser);
            if (Objects.isNull(adminUser)) {
                renderJson(R.error("请先登录"));
                return;
            }

            renderJson(R.ok().put("data", crmBusinessService.queryBusinessPageList(basePageRequest, adminUser)));
//            renderJson(adminSceneService.filterConditionAndGetPageList(basePageRequest));
        } catch (CrmException e) {
            logger.error(String.format("queryBusinessPageList business error msg:%s", BaseUtil.getExceptionStack(e)));
            renderJson(R.error(e.getMessage()));
        }
    }

    /**
     * 查询基本信息
     * @author yue.li
     * @param id
     */
    @Permissions("crm:business:read")
    public void information(@Para("id")Integer id){
        try{
            List<Record> recordList= crmBusinessService.information(id);
            renderJson(R.ok().put("data",recordList));
        }catch (Exception e){
            logger.error(String.format("information business error msg:%s",BaseUtil.getExceptionStack(e)));
            renderJson(R.error(e.getMessage()));

        }
    }

    /**
     * 根据商机id查询商机下的商品信息
     * @author yue.li
     * @param businessId 商机id
     */
    @NotNullValidate(value = "businessId",message = "商机id不能为空")
    public void queryProductsByBusinessId(@Para("businessId")Integer businessId){
        try{
            renderJson(R.ok().put("data",crmBusinessService.queryProductsByBusinessId(businessId)));
        } catch (Exception e){
            logger.error(String.format("queryProductsByBusinessId error msg:%s",BaseUtil.getExceptionStack(e)));
            renderJson(R.error(e.getMessage()));
        }
    }

    /**
     * 根据主键ID删除商机对应商品信息
     * @author yue.li
     * @param id 业务主键id
     */
    @NotNullValidate(value = "id",message = "id不能为空")
    public void deleteProductById(@Para("id")Integer id,@Para("money")String money,@Para("businessId")Integer businessId) {
        try{
            renderJson(crmBusinessService.deleteProductById(id,money,businessId));
        } catch (Exception e){
            logger.error(String.format("deleteProductById error msg:%s",BaseUtil.getExceptionStack(e)));
            renderJson(R.error(e.getMessage()));
        }
    }

    /**
     * 新增或更新商机对应商品信息
     * @author yue.li
     */
    public void addOrUpdateBusinessProduct(){
        try{
            JSONObject jsonObject = JSON.parseObject(getRawData());
            renderJson(crmBusinessService.addOrUpdateBusinessProduct(jsonObject,BaseUtil.getUserId()));
        }catch (Exception e){
            logger.error(String.format("addOrUpdateBusinessProduct error msg:%s",BaseUtil.getExceptionStack(e)));
            renderJson(R.error(e.getMessage()));
        }
    }

    /**
     * 获取用户最近匹配的商机组配置部门
     */
    public void getConfiguredDeptOfUser() {
        Integer deptId = BaseUtil.getDeptId();
        if (Objects.isNull(deptId)) {
            renderJson(R.error("当前用户没有部门ID"));
            return;
        }
        Record configuredDept = crmBusinessGroupService.getConfiguredDept(Long.valueOf(deptId));
        if (Objects.nonNull(configuredDept)) {
            renderJson(R.ok().put("data", configuredDept));
        } else {
            renderJson(R.error("您所在的部门暂未进行商机组设置，如有需求，请联系上级或者CRM管理员"));
        }
    }

    /**
     * 根据AdminFieldController重构
     * @param id 商机ID
     */
    public void queryFieldNew(@Para("id")Integer id) {
        try {
            Integer userDeptId = BaseUtil.getDeptId();
            if (Objects.isNull(userDeptId)) {
                throw new CrmException("当前登录人部门ID为空");
            }
            renderJson(R.ok().put("data", crmBusinessService.queryFieldNew(id, Long.valueOf(userDeptId))));
        } catch (CrmException e) {
            renderJson(R.error(e.getMessage()));
        }
    }

    /**
     * 根据阶段 id，获取可验证结果
     * @param businessId 商机ID
     * @param statusId 商机组阶段ID
     */
    public void statusVerificationList(@Para("businessId") Long businessId, @Para("statusId") Long statusId) {
        renderJson(R.ok().put("data", crmBusinessService.statusVerificationList(businessId, statusId, ossPrivateFileUtil)));
    }

    /**
     * 根据阶段 id，获取销售活动列表
     * @param businessId 商机ID
     * @param statusId 商机组阶段ID
     */
    public void statusSalesActivityList(@Para("businessId") Long businessId, @Para("statusId") Long statusId) {
        renderJson(R.ok().put("data", crmBusinessService.statusSalesActivityList(businessId, statusId)));
    }

    /**
     * 修改关键销售活动
     */
    @NotNullValidate(value = "businessId", type = HttpEnum.JSON, message = "商机ID")
    @NotNullValidate(value = "statusId", type = HttpEnum.JSON, message = "商机组阶段ID")
    public void statusSalesActivityEdit() {
        JSONObject params = JSON.parseObject(getRawData());
        Long businessId = params.getLong("businessId");
        Long statusId = params.getLong("statusId");
        Long userId = BaseUtil.getUserId();
        crmBusinessService.statusSalesActivityEdit(new Record()
                .set("userId", userId)
                .set("businessId", businessId)
                .set("statusId", statusId)
                .set("activities", params.getJSONArray("activityIds").stream().map(activityId -> new Record()
                        .set("business_id", businessId)
                        .set("activity_id", activityId)
                        .set("status_id", statusId)).collect(Collectors.toList()))
        );
        renderJson(R.ok());
    }

    @NotNullValidate(value = "businessId",type = HttpEnum.JSON,message = "商机ID")
    @NotNullValidate(value = "verificationId",type = HttpEnum.JSON,message = "商机组可验证结果ID")
    @NotNullValidate(value = "statusId",type = HttpEnum.JSON,message = "商机组阶段ID")
    public void statusVerificationEdit() {
        JSONObject params = JSON.parseObject(getRawData());
        Record paramObj = new Record()
                .set("id", params.getBigInteger("recordId"))
                .set("business_id", params.getLong("businessId"))
                .set("verification_id", params.getLong("verificationId"))
                .set("status_id", params.getLong("statusId"))
                .set("batch_id", params.getString("batchId"))
                .set("content", params.getString("content"));
        String fileIds = params.getString("fileIds");
        if (StringUtils.isNoneBlank(fileIds)) {
            paramObj.set("fileIds", Arrays.stream(fileIds.split(",")).filter(StringUtils::isNotBlank).map(Long::valueOf).collect(Collectors.toList()));
        }
        Long userId = BaseUtil.getUserId();
        crmBusinessService.statusVerificationEdit(paramObj, ossPrivateFileUtil, userId);
        renderJson(R.ok());
    }

    /**
     * 氚云商机导入
     * @author yue.li
     * @param file 导入的文件
     */
    //@Permissions("crm:business:excelimport")
    public void uploadExcel(@Para("file") File file) {

        CrmBusinessDataListener crmBusinessDataListener = new CrmBusinessDataListener(adminDeptService);
        EasyExcel.read(file).headRowNumber(1).registerReadListener(crmBusinessDataListener).sheet().doRead();

        if (StringUtils.isNotEmpty(crmBusinessDataListener.getErrMsg())){
            renderJson(R.error(10000,"校验发现不合规数据，请您修改后再导入：\r\n" + crmBusinessDataListener.getErrMsg()));
            return;
        }
        try{
            crmBusinessService.addOrUpdateBusinessForExcel(crmBusinessDataListener.getDataList());
        }catch(Exception e) {
            logger.error("商机导入异常：" + e.getMessage());
            renderJson(R.error(e.getMessage()));
            return;
        }finally {
            crmBusinessDataListener.clearDataList();
        }
        renderJson(R.ok());
    }

    /**
     * 创建官网用户用，并与crm客户关联
     */
    @Permissions("crm:business:contract_apply")
    @NotNullValidate(value = "customerId",type = HttpEnum.JSON,message = "客户ID 不能为空")
    public void createSiteMember(){
        JSONObject jsonObject = JSON.parseObject(getRawData());

        Integer customerId;
        try {
            customerId = jsonObject.getInteger("customerId");
        } catch (Exception e) {
            throw new CrmException("参数：customerId 异常");
        }
        Long siteMemberId = crmBusinessService.createSiteMemberAndBindCid(customerId);

        renderJson(R.okWithData(siteMemberId));
    }

}
