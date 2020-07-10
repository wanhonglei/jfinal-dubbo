package com.kakarote.crm9.erp.crm.service;

import static com.kakarote.crm9.erp.crm.constant.CrmConstant.EMAIL_SUBJECT_PAYMENT;

import java.io.StringWriter;
import java.math.BigDecimal;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.rocketmq.shade.com.alibaba.fastjson.JSON;
import com.alibaba.rocketmq.shade.io.netty.util.internal.StringUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.jfinal.aop.Before;
import com.jfinal.aop.Inject;
import com.jfinal.kit.JsonKit;
import com.jfinal.kit.Kv;
import com.jfinal.plugin.activerecord.ActiveRecordException;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.SqlPara;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.jfinal.plugin.redis.Redis;
import com.jfinal.upload.UploadFile;
import com.kakarote.crm9.common.config.JfinalConfig;
import com.kakarote.crm9.common.config.cache.RedisCache;
import com.kakarote.crm9.common.config.paragetter.BasePageRequest;
import com.kakarote.crm9.common.constant.BaseConstant;
import com.kakarote.crm9.common.exception.CrmException;
import com.kakarote.crm9.common.midway.NotifyService;
import com.kakarote.crm9.common.theadpool.CrmThreadPool;
import com.kakarote.crm9.erp.admin.common.AdminEnum;
import com.kakarote.crm9.erp.admin.common.CustomerIndustryEnum;
import com.kakarote.crm9.erp.admin.constant.RedisConstant;
import com.kakarote.crm9.erp.admin.entity.AdminCustomerReceiveRole;
import com.kakarote.crm9.erp.admin.entity.AdminDept;
import com.kakarote.crm9.erp.admin.entity.AdminRole;
import com.kakarote.crm9.erp.admin.entity.AdminUser;
import com.kakarote.crm9.erp.admin.service.AdminConfigService;
import com.kakarote.crm9.erp.admin.service.AdminCustomerReceiveRoleService;
import com.kakarote.crm9.erp.admin.service.AdminDataDicService;
import com.kakarote.crm9.erp.admin.service.AdminDeptService;
import com.kakarote.crm9.erp.admin.service.AdminFieldService;
import com.kakarote.crm9.erp.admin.service.AdminFileService;
import com.kakarote.crm9.erp.admin.service.AdminIndustryOfDeptService;
import com.kakarote.crm9.erp.admin.service.AdminRoleService;
import com.kakarote.crm9.erp.admin.service.AdminSceneService;
import com.kakarote.crm9.erp.admin.service.AdminUserService;
import com.kakarote.crm9.erp.crm.acl.user.CrmUser;
import com.kakarote.crm9.erp.crm.common.CrmAllTabsEnum;
import com.kakarote.crm9.erp.crm.common.CrmCustomerChangeLogEnum;
import com.kakarote.crm9.erp.crm.common.CrmCustomerDateEnum;
import com.kakarote.crm9.erp.crm.common.CrmDistributorEnum;
import com.kakarote.crm9.erp.crm.common.CrmEnum;
import com.kakarote.crm9.erp.crm.common.CrmErrorInfo;
import com.kakarote.crm9.erp.crm.common.CrmLabelEnum;
import com.kakarote.crm9.erp.crm.common.CrmOperateChannelEventEnum;
import com.kakarote.crm9.erp.crm.common.CrmParamValid;
import com.kakarote.crm9.erp.crm.common.CrmPayTypeEnum;
import com.kakarote.crm9.erp.crm.common.CrmReduceReasonsEnum;
import com.kakarote.crm9.erp.crm.common.CrmRepeatNameEnum;
import com.kakarote.crm9.erp.crm.common.CustomerOriginEnum;
import com.kakarote.crm9.erp.crm.common.CustomerStorageTypeEnum;
import com.kakarote.crm9.erp.crm.common.PerformanceChangeChannelEnum;
import com.kakarote.crm9.erp.crm.common.PerformanceFromChannelEnum;
import com.kakarote.crm9.erp.crm.common.PerformanceObjectTypeEnum;
import com.kakarote.crm9.erp.crm.common.PerformanceTargetTypeEnum;
import com.kakarote.crm9.erp.crm.common.PromotionTagEnum;
import com.kakarote.crm9.erp.crm.common.customer.FromSourceEnum;
import com.kakarote.crm9.erp.crm.common.scene.CrmCustomerSceneEnum;
import com.kakarote.crm9.erp.crm.common.scene.CustomerSceneEnum;
import com.kakarote.crm9.erp.crm.constant.CrmConstant;
import com.kakarote.crm9.erp.crm.constant.CrmTagConstant;
import com.kakarote.crm9.erp.crm.cron.CrmBaseDataCron;
import com.kakarote.crm9.erp.crm.dto.CrmCustomerPageRequest;
import com.kakarote.crm9.erp.crm.dto.CrmPerformanceDto;
import com.kakarote.crm9.erp.crm.dto.EffectiveCustomerDto;
import com.kakarote.crm9.erp.crm.entity.BopsPayment;
import com.kakarote.crm9.erp.crm.entity.CrmActionRecord;
import com.kakarote.crm9.erp.crm.entity.CrmCustomer;
import com.kakarote.crm9.erp.crm.entity.CrmCustomerExt;
import com.kakarote.crm9.erp.crm.entity.CrmCustomerPaymentChannel;
import com.kakarote.crm9.erp.crm.entity.CrmCustomerSalesLog;
import com.kakarote.crm9.erp.crm.entity.CrmLabel;
import com.kakarote.crm9.erp.crm.entity.CrmOwnerRecord;
import com.kakarote.crm9.erp.crm.entity.CrmSiteMember;
import com.kakarote.crm9.erp.crm.entity.CrmWorkFlowData;
import com.kakarote.crm9.erp.crm.entity.CrmWorkFlowInfo;
import com.kakarote.crm9.erp.crm.entity.CrmWorkflowResponse;
import com.kakarote.crm9.erp.crm.entity.CustomerIdSiteMemberIdRecord;
import com.kakarote.crm9.erp.crm.entity.DistributorBdSalesStatistic;
import com.kakarote.crm9.erp.crm.entity.DistributorStatistic;
import com.kakarote.crm9.erp.crm.service.bops.BopsService;
import com.kakarote.crm9.erp.crm.vo.CrmGroupMemberVO;
import com.kakarote.crm9.integration.common.EsbConfig;
import com.kakarote.crm9.integration.common.PaymentTypeEnum;
import com.kakarote.crm9.utils.BaseUtil;
import com.kakarote.crm9.utils.CrmDateUtil;
import com.kakarote.crm9.utils.FieldUtil;
import com.kakarote.crm9.utils.HttpUtil;
import com.kakarote.crm9.utils.OssPrivateFileUtil;
import com.kakarote.crm9.utils.R;
import com.kakarote.crm9.utils.SceneUtil;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.poi.excel.ExcelReader;
import cn.hutool.poi.excel.ExcelUtil;

/**
 * CrmCustomerService
 *
 * @author yue.li
 * @date 2020/01/07
 */
public class CrmCustomerService {

	// 推广分销商事业部的部门id，以“,”分割
	private  static final String PROMOTION_DISTRIBUTOR_DEPTIDS = JfinalConfig.crmProp.get("customer.distributor.promotion.DeptIds");
	// 推广分销商事业部的部门id，以“,”分割
	private  static final String DUAL_ADDRESS = JfinalConfig.crmProp.get("receivables.mail.dual.address");
	
    @Inject
    private AdminFieldService adminFieldService;

    @Inject
    private FieldUtil fieldUtil;

    @Inject
    private CrmRecordService crmRecordService;

    @Inject
    private AdminFileService adminFileService;

    @Inject
    private AdminSceneService adminSceneService;

    @Inject
    private CrmParamValid crmParamValid;

    @Inject
    private AdminDataDicService adminDataDicService;

    @Inject
    private AdminDeptService adminDeptService;

    @Inject
    private AdminRoleService adminRoleService;

    @Inject
    private CrmContactsService crmContactsService;

    @Inject
    private CrmBusinessService crmBusinessService;

    @Inject
    private CrmSiteMemberService crmSiteMemberService;

    @Inject
    private AdminUserService adminUserService;

    @Inject
    private AdminIndustryOfDeptService adminIndustryOfDeptService;

    @Inject
    private AdminConfigService adminConfigService;

    @Inject
    private CrmPerformanceService crmPerformanceService;

    @Inject
    private CrmBaseDataCron crmBaseDataCron;

    @Inject
    private AdminCustomerReceiveRoleService adminCustomerReceiveRoleService;

    @Inject
    private CrmChangeLogService crmChangeLogService;

    @Inject
    private CrmDistributorPromotionRelationService distributorPromotionRelationService;

    @Inject
    private CrmGroupMemberService crmGroupMemberService;

    @Inject
    private RedisCache redisCache;

    @Inject
    private CrmCustomerSceneService crmCustomerSceneService;

    @Inject
    private BopsService bopsService;

    private static final Logger logger = LoggerFactory.getLogger(CrmCustomerService.class);

    //数字正则
    private static final Pattern P = Pattern.compile("[\\d()（）\\t ]"), PW = Pattern.compile("[\\d \\t]");

    private static final String CUSTOMER_CHANGE_CHANNEL_EVENT = "from_channel_event";

    private static final String CUSTOMER_CHANGE_HISTORY = "change_history";

    /**
     * @author wyq
     * 分页条件查询客户
     */
    public Page<Record> getCustomerPageList(BasePageRequest<CrmCustomer> basePageRequest) {
        String customerName = basePageRequest.getData().getCustomerName();
        if (!crmParamValid.isValid(customerName)) {
            return new Page<>();
        }
        String mobile = basePageRequest.getData().getMobile();
        String telephone = basePageRequest.getData().getTelephone();
        if (StrUtil.isEmpty(customerName) && StrUtil.isEmpty(telephone)) {
            return new Page<>();
        }
        return Db.paginate(basePageRequest.getPage(), basePageRequest.getLimit(), Db.getSqlPara("crm.customer.getCustomerPageList", Kv.by("customerName", customerName).set("mobile", mobile).set("telephone", telephone)));
    }

    /**
     * @author wyq
     * 新增或更新客户
     */
    @Before(Tx.class)
    public R addOrUpdate(JSONObject jsonObject, Long userId) {
        logger.info("customer service addOrUpdate jsonObject: {}", jsonObject.toJSONString());
        CrmCustomer crmCustomer = jsonObject.getObject("entity", CrmCustomer.class);
        //去除客户姓名的前后空格
        crmCustomer.setCustomerName(crmCustomer.getCustomerName().trim());
        if (Objects.isNull(crmCustomer.getDeptId())) {
            crmCustomer.setDeptId(null);
        }
        // 客户类型为企业客户验证客户名称是否存在
        String customerType;
        if (StringUtils.isNotEmpty(crmCustomer.getCustomerType())) {
            // 标签中查找客户类型
            customerType = adminDataDicService.formatTagValueId(CrmTagConstant.CUSTOMER_TYPE, crmCustomer.getCustomerType());
            //企业客户
            if (CrmConstant.BUSINESS_CLIENTS.equals(customerType)) {
                String customerName = crmCustomer.getCustomerName();

                Record record = Db.findFirst(Db.getSqlPara("crm.customer.queryRepeatCustomerInfo", Kv.by("customer_name", customerName).set("customer_type", crmCustomer.getCustomerType()).set("customer_id",crmCustomer.getCustomerId())));
                if (Objects.nonNull(crmCustomer.getCustomerId())) {
                    if (Objects.nonNull(record) && !crmCustomer.getCustomerId().equals(record.getLong("customer_id"))) {
                        return R.error(customerExistInfo(record));
                    }
                } else {
                    if (Objects.nonNull(record)) {
                        return R.error(customerExistInfo(record));
                    }
                }
            } else {
                if (Objects.isNull(crmCustomer.getCustomerId())) {
                    if (CustomerOriginEnum.CUSTOMER_NEW_KEY.getTypes().equals(crmCustomer.getCustomerOrigin()) || CustomerOriginEnum.LEADS_TRANSFORM_KEY.getTypes().equals(crmCustomer.getCustomerOrigin())) {
                        return R.error(CrmErrorInfo.BUSINESS_CLIENTS_NEED);
                    }
                }
            }
        } else {
            //客户类型参数不能为空
            return R.error(CrmErrorInfo.CUSTOMER_TYPE_IS_NULL);
        }

        if (crmCustomer.getCustomerId() != null) {
            //更新
            boolean storageTypeChanged = false;
            //获取现有客户信息
            CrmCustomer oldCrmCustomer = CrmCustomer.dao.findByIdLoadColumns(new Object[]{crmCustomer.getCustomerId()}, "owner_user_id");
            if (oldCrmCustomer == null){
                return R.error("customerId:" + crmCustomer.getCustomerId() + ",客户不存在");
            }

            Integer newOwnerUserId;
            Integer oldOwnerUserId = oldCrmCustomer.getOwnerUserId();

            if (crmCustomer.getOwnerUserId() != null) {
                //负责人不为空，归属部门必须为空
                crmCustomer.setDeptId(null);

                if (oldCrmCustomer.getOwnerUserId() == null){
                    return R.error("编辑时，不能直接修改负责人");
                }
                if (crmCustomer.getOwnerUserId().intValue() != oldCrmCustomer.getOwnerUserId().intValue()) {
                    return R.error("编辑时，不能直接修改负责人");
                }

                Integer storageType = crmCustomer.getStorageType();
                if (storageType != null){
                    Record record = Db.findFirst(Db.getSqlPara("crm.customerExt.getCustomerStorageType", Kv.by("customerId", crmCustomer.getCustomerId())));
                    //判断库存类型是否变化(查询不到记录则表示变化)
                    if (record == null || !Objects.equals(record.getInt("storage_type"),storageType)){
                        //关联库库容类型不能改成考察库库容类型
                        if (record != null && record.getInt("storage_type") != null && record.getInt("storage_type") == CustomerStorageTypeEnum.RELATE_CAP.getCode()){
                            return R.error("编辑时，不能把关联库类型变更考察库类型");
                        }

                        boolean checkUserCapacity = checkUserCapacity(Long.valueOf(crmCustomer.getOwnerUserId()), storageType, 1);
                        if (!checkUserCapacity){
                            return R.error("您的" + CustomerStorageTypeEnum.getNameByCode(storageType) + "库容已满，无法新建客户");
                        }else {
                            //保存用户扩展表
                            saveCrmCustomerExt(crmCustomer.getCustomerId(),crmCustomer.getStorageType(),null,null);
                            storageTypeChanged = true;
                        }
                    }
                }

            }

            crmCustomer.setUpdateTime(DateUtil.date());
            boolean updateFlag = crmCustomer.update();
            if (updateFlag) {
                //newOwnerUserId 为null，可能并未更新customer对象，所以必须再次查询一遍
                CrmCustomer newCrmCustomer = CrmCustomer.dao.findByIdLoadColumns(new Object[]{crmCustomer.getCustomerId()}, "owner_user_id");
                newOwnerUserId = newCrmCustomer.getOwnerUserId();

                //变更了负责人或者库容类型变更，则记录业绩日志
                boolean saveLogFlag = !Objects.equals(oldOwnerUserId, newOwnerUserId) || storageTypeChanged;
                if (saveLogFlag){
                    //客户负责人变更记录日志
                    crmChangeLogService.saveCustomerChangeLog(CrmCustomerChangeLogEnum.getBdByStorageType(crmCustomer.getStorageType()),crmCustomer.getCustomerId(),Long.valueOf(newOwnerUserId),null,userId);
                }

            }
            return updateFlag ? R.ok() : R.error(CrmErrorInfo.CUSTOMER_UPDATE_FAILD);
        } else {
            crmCustomer.setCreateTime(DateUtil.date());
            crmCustomer.setUpdateTime(DateUtil.date());
            crmCustomer.setCreateUserId(Objects.nonNull(userId) ? userId.intValue() : null);
            crmCustomer.setBatchId(StringUtils.isNotEmpty(crmCustomer.getBatchId()) ? crmCustomer.getBatchId() : IdUtil.simpleUUID());
            crmCustomer.setRwUserId(",");
            crmCustomer.setRoUserId(",");

            //重名判断
            String repeatCustomerType = checkRepeatCustomerName(crmCustomer.getCustomerId(), crmCustomer.getCustomerName());
            if (org.apache.commons.lang.StringUtils.isNotBlank(repeatCustomerType)) {
                if (CrmConstant.BUSINESS_CLIENTS.equals(customerType) && repeatCustomerType.equals(CrmConstant.BUSINESS_CLIENTS)) {
                    //1.企业与企业客户重复
                    crmCustomer.setRepeatMark(1);
                } else if (CrmConstant.BUSINESS_CLIENTS.equals(customerType) && repeatCustomerType.equals(CrmConstant.CRM_SITE_USER_TYPE_PERSONAL)) {
                    //2.企业与个人客户重复
                    crmCustomer.setRepeatMark(2);
                } else if (CrmConstant.CRM_SITE_USER_TYPE_PERSONAL.equals(customerType) && repeatCustomerType.equals(CrmConstant.BUSINESS_CLIENTS)) {
                    //2.企业与个人客户重复
                    crmCustomer.setRepeatMark(2);
                } else {
                    crmCustomer.setRepeatMark(0);
                }
            }

            //如果新建客户有负责人，则进入库容判断
            if (crmCustomer.getOwnerUserId() != null){
                //新增客户，库存类型不能为空
                Integer storageType = crmCustomer.getStorageType();
                if (Objects.isNull(storageType)) {
                    return R.error("请设置客户库存类型");
                }
                //有负责人，部门字段必须为null
                crmCustomer.setDeptId(null);
                crmCustomer.setOwnerTime(DateUtil.date());

                //新增
                boolean checkUserCapacity = checkUserCapacity(Long.valueOf(crmCustomer.getOwnerUserId()), storageType, 1);
                if (!checkUserCapacity) {
                    return R.error("您的" + CustomerStorageTypeEnum.getNameByCode(storageType) + "库容已满，无法新建客户");
                }

            }else if (crmCustomer.getDeptId() != null){
                JSONObject capacityJson = checkDeptCapacity(Long.valueOf(crmCustomer.getDeptId()), 1);
                if (!capacityJson.getBoolean("result")){
                    return R.error("您的部门周转库库容已满，客户无法放入部门客户池");
                } else {
                    Integer deptId = capacityJson.getInteger("deptId");
                    //如果可以领取，但是部门是空的，代表应使用当前部门的事业部, 否则使用返回的dept_id
                    if (deptId == null){
                        deptId = Integer.valueOf(adminDeptService.getBusinessDepartmentByDeptId(crmCustomer.getDeptId().toString()));
                    }
                    crmCustomer.setDeptId(deptId);

                    //清空当前客户库容类型
                    Db.update(Db.getSqlPara("crm.customerExt.deleteStorageTypeByCustomerIds", Kv.by("customerIds",Collections.singletonList(crmCustomer.getCustomerId()))));
                }
            }

            boolean save = crmCustomer.save();
            /*生成客编*/
            String customerNo = CrmConstant.QXWZ + crmCustomer.getCustomerId() + "_" + Calendar.getInstance().get(Calendar.YEAR);
            crmCustomer.setCustomerNo(customerNo);
            boolean update = crmCustomer.update();
            boolean operate = false;
            if (save && update) {
                //保存用户扩展表
                saveCrmCustomerExt(crmCustomer.getCustomerId(),crmCustomer.getStorageType(),null,crmCustomer.getOriginalCustomerName());

                operate = true;
            }
            Long changeLogId = null;
            if (operate){
                //新建客户需要添加客户变更日志，按是否有负责人、部门区分类型
                if (crmCustomer.getOwnerUserId() != null){
                    changeLogId = crmChangeLogService.saveCustomerChangeLog(CrmCustomerChangeLogEnum.getBdByStorageType(crmCustomer.getStorageType()), crmCustomer.getCustomerId(), Long.valueOf(crmCustomer.getOwnerUserId()), null, userId).longValue();
                } else if (crmCustomer.getDeptId() != null){
                    changeLogId = crmChangeLogService.saveCustomerChangeLog(CrmCustomerChangeLogEnum.DEPT.getCode(), crmCustomer.getCustomerId(), null, Long.valueOf(crmCustomer.getDeptId()), userId).longValue();
                } else {
                    changeLogId = crmChangeLogService.saveCustomerChangeLog(CrmCustomerChangeLogEnum.OPEN_SEA.getCode(), crmCustomer.getCustomerId(), null, null, userId).longValue();
                }
            }

            //记录客户导入操作日志
            if (StringUtils.isNotBlank(jsonObject.getString("uploadExcel"))) {
                crmRecordService.saveUploadCustomerByExcelRecord(userId, crmCustomer.getCustomerId());
            }

            /*生成客编*/
            return operate ? R.ok().put("data", Kv.by("customer_id", crmCustomer.getCustomerId())
                    .set("customer_name", crmCustomer.getCustomerName())
                    .set("customer_no", customerNo)
                    .set("changeLogId", changeLogId)) : R.error();
        }
    }

    /**
     * 保存客户扩展表
     * @param customerId 客户id
     * @param storageType 库容类型 为null则不更新
     * @param drawFlag 是否领取中 为null则不更新
     * @param originalCustomerName 客户原始姓名 为null则不更新
     */
    public void saveCrmCustomerExt(Long customerId,Integer storageType,Integer drawFlag, String originalCustomerName){
        if (customerId == null){
            return;
        }
        CrmCustomerExt customerExt = CrmCustomerExt.dao.findFirst("select * from 72crm_crm_customer_ext where customer_id = ?", customerId);

        if (customerExt != null){
            if (storageType != null){
                customerExt.setStorageType(storageType);
            }
            if (drawFlag != null){
                customerExt.setDrawFlag(drawFlag);
            }
            if (StringUtils.isNotBlank(originalCustomerName)){
                customerExt.setOriginalCustomerName(originalCustomerName);
            }
            customerExt.update();
        }else{
            customerExt = new CrmCustomerExt();
            customerExt.setCustomerId(customerId.intValue());

            if (storageType != null){
                customerExt.setStorageType(storageType);
            }
            if (drawFlag != null) {
                customerExt.setDrawFlag(drawFlag);
            }
            if (StringUtils.isNotBlank(originalCustomerName)){
                customerExt.setOriginalCustomerName(originalCustomerName);
            }

            customerExt.save();
        }
    }

    /**
     * 客户重复提示信息
     *
     * @param record record实体
     * @author yue.li
     */
    public String customerExistInfo(Record record) {
        if (StringUtils.isNotEmpty(record.getStr("owner_user_name"))) {
            return CrmErrorInfo.CUSTOMER_BELONG_TO_USER + record.getStr("owner_user_name");
        } else if (StringUtils.isNotEmpty(record.getStr("dept_name"))) {
            return CrmErrorInfo.CUSTOMER_BELONG_TO_DEPT + record.getStr("dept_name");
        } else {
            return CrmErrorInfo.CUSTOMER_EXISTS_PUBLIC_POOL;
        }
    }

    /**
     * @author wyq
     * 根据客户id查询
     */
    public Record queryById(Integer customerId, OssPrivateFileUtil ossPrivateFileUtil) {
        Map<String, Object> attr = new HashMap<>();
        Record record = Db.findFirst(Db.getSqlPara("crm.customer.queryCustomerInfo", Kv.by("customer_id", customerId)));
        if (Objects.isNull(record)) {
            return new Record();
        }
        if (StringUtils.isNotEmpty(record.getStr("address")) && CrmConstant.PROVINCE_CITY_AREA.equals(record.getStr("address"))) {
            record.set("address", "");
        }
        /*如果事业部部门名称为空,取负责人部门名称*/
        if (StringUtils.isEmpty(record.getStr("deptName"))) {
            String userId = record.getStr("owner_user_id");
            if (StringUtils.isNotEmpty(userId)) {
                Integer deptId = Integer.valueOf(adminDeptService.getBusinessDepartmentByDeptId(record.getStr("owner_user_dept_id")));
                Record businessDepartmentRecord = Db.findFirst(Db.getSql("admin.dept.queryDeptInfoByDeptId"), deptId);
                if (businessDepartmentRecord != null) {
                    record.set("dept_name", businessDepartmentRecord.getStr("name"));
                }
            }
        }
        // 转化图片OssUrl
        if (StringUtils.isNotEmpty(record.getStr("registration_img_url"))) {
            attr.put("registration_real_img_url", record.getStr("registration_img_url"));
            record.setColumns(attr);
            record.set("registration_img_url", ossPrivateFileUtil.presignedURL(record.getStr("registration_img_url")));
        }
        if (StringUtils.isNotEmpty(record.getStr("id_card_url"))) {
            record.set("id_card_url", ossPrivateFileUtil.presignedURL(record.getStr("id_card_url")));
        }

        //获取客户跟进状态
        Record first = Db.findFirst(Db.getSqlPara("crm.customer.getCustomerFollowMsgWithCustomerId", Kv.by("customerId", record.get("customer_id"))));
        record.set("lately_follow_user_name", first.get("lately_follow_user_name"));
        record.set("lately_follow_time", first.get("lately_follow_time"));
        record.set("distrbute_time", first.get("distrbute_time"));
        record.set("dispose_status", first.get("dispose_status"));

        //获取放回客户池原因
        Record labelRecord = Db.findFirst(Db.getSqlPara("crm.label.findLabelComment",
                Kv.by("relatedId", customerId).set("relatedType", CrmEnum.CUSTOMER_TYPE_KEY.getTypes())
                        .set("labelKey", CrmLabelEnum.BACK_CUSTOMER_REASON.getTypes())));
        if (labelRecord != null) {
            record.set("toPublicPoolReason", labelRecord.getStr("label_comment"));
        } else {
            record.set("toPublicPoolReason", "");
        }

        CrmSiteMember crmSiteMember = getSiteMemberInfoByCustomerId(customerId);
        record.set("site_member_id", Objects.nonNull(crmSiteMember) ? crmSiteMember.getSiteMemberId() : null);
        record.set("is_distributor", Objects.nonNull(crmSiteMember) ? crmSiteMember.getIsDistributor() : null);

        //获取上游分销商ID，获取上游分销商名称，推广标签，多个逗号连接
        String customerNo = record.getStr("customer_no");
        Record parentCustomerInfo = distributorPromotionRelationService.queryParentCustIdByCustId(customerNo);
        record.set("pCustomerId", Objects.nonNull(parentCustomerInfo) ? parentCustomerInfo.getStr("pCustomerId") : null);
        record.set("pCustomerName", Objects.nonNull(parentCustomerInfo) ? parentCustomerInfo.getStr("pCustomerName") : "");
        //设置推广标签
        String promotionTagDesc = null;
        if(Objects.nonNull(parentCustomerInfo)) {
            String promotionTags = parentCustomerInfo.getStr("promotionTags");
            promotionTagDesc = PromotionTagEnum.getDesc(promotionTags);
        }
        record.set("promotionTags", promotionTagDesc);

        Record record1 = Db.findFirst(Db.getSqlPara("crm.customerExt.getCustomerStorageType", Kv.by("customerId", customerId)));
        if (record1 == null){
            record.set("storage_type",null);
        } else {
            record.set("storage_type",record1.get("storage_type"));
        }

        /*获取网站ID*/
        return record;
    }

    /**
     * @author wyq
     * 基本信息
     */
    public List<Record> information(Integer customerId) {
        Record record = Db.findFirst(Db.getSqlPara("crm.customer.queryCustomerInfo", Kv.by("customer_id", customerId)));
        if (null == record) {
            return null;
        }
        List<Record> fieldList = new ArrayList<>();
        FieldUtil field = new FieldUtil(fieldList);
        field.set("客户名称", record.getStr("customer_name"))
                .set("成交状态", record.getStr("deal_status"))
                .set("下次联系时间", DateUtil.formatDateTime(record.get("next_time")))
                .set("网址", record.getStr("website"))
                .set("备注", record.getStr("remark"))
                .set("电话", record.getStr("telephone"))
                .set("定位", record.getStr("location"))
                .set("区域", record.getStr("address"))
                .set("详细地址", record.getStr("detail_address"));
        List<Record> fields = adminFieldService.list("2");
        for (Record r : fields) {
            field.set(r.getStr("name"), record.getStr(r.getStr("name")));
        }
        return fieldList;
    }

    /**
     * @author wyq
     * 根据客户名称查询
     */
    public Record queryByName(String name) {
        return Db.findFirst(Db.getSqlPara("crm.customer.queryCustomerInfo", Kv.by("customer_name", name)));
    }

    /**
     * @author wyq
     * 根据客户id查找商机
     */
    public R queryBusiness(BasePageRequest<CrmCustomer> basePageRequest) {
        JSONObject jsonObject = basePageRequest.getJsonObject();
        Integer customerId = jsonObject.getInteger("customerId");
        String search = jsonObject.getString("search");
        Integer pageType = basePageRequest.getPageType();
        if (0 == pageType) {
            List<Record> recordList = Db.find(Db.getSqlPara("crm.customer.queryBusiness", Kv.by("customerId", customerId).set("businessName", search)));
            adminSceneService.setBusinessStatus(recordList);
            return R.ok().put("data", recordList);
        } else {
            Page<Record> paginate = Db.paginate(basePageRequest.getPage(), basePageRequest.getLimit(), Db.getSqlPara("crm.customer.queryBusiness", Kv.by("customerId", customerId).set("businessName", search)));
            adminSceneService.setBusinessStatus(paginate.getList());
            return R.ok().put("data", paginate);
        }
    }


    /**
     * @author wyq
     * <p>
     * select contacts_id,name,mobile,post,telephone,email,role,attitude from contactsview where customer_id = ?
     * <p>
     * 根据客户id查询联系人
     */
    public R queryContacts(BasePageRequest<CrmCustomer> basePageRequest, boolean needSensitive) {
        Integer customerId = basePageRequest.getData().getCustomerId().intValue();
        Integer pageType = basePageRequest.getPageType();
        if (0 == pageType) {
            List<Record> records = Db.find(Db.getSql("crm.customer.queryContacts"), customerId);
            if (needSensitive) {
                records = filterSensitiveInformationOnlyForEmail(records);
            } else {
                records = filterSensitiveInformation(records);
            }

            return R.ok().put("data", records);
        } else {
            Page<Record> pageRecord = Db.paginate(basePageRequest.getPage(), basePageRequest.getLimit(), new SqlPara().setSql(Db.getSql("crm.customer.queryContacts")).addPara(customerId));
            if (pageRecord != null && pageRecord.getList() != null && pageRecord.getList().size() > 0) {
                List<Record> records;
                if (needSensitive) {
                    records = filterSensitiveInformationOnlyForEmail(pageRecord.getList());
                } else {
                    records = filterSensitiveInformation(pageRecord.getList());
                }
                pageRecord.setList(records);
            }
            return R.ok().put("data", pageRecord);
        }
    }

    private List<Record> filterSensitiveInformation(List<Record> records) {
        if (records != null && records.size() > 0) {
            records.forEach(item -> {
                item.set("mobile", getSensitiveField("mobile", item));
                item.set("telephone", getSensitiveField("telephone", item));
                item.set("email", getSensitiveField("email", item));
            });
        }
        return records;
    }

    /**
     * 去除敏感邮件信息，其他信息格式化显示
     *
     * @param records 记录列表
     * @return
     */
    private List<Record> filterSensitiveInformationOnlyForEmail(List<Record> records) {
        if (records != null && records.size() > 0) {
            records.forEach(item -> {
                item.set("email", getSensitiveField("email", item));


                String telephone = item.getStr("telephone");
                if (StringUtils.isNoneBlank(telephone) && telephone.length() > 8) {
                    telephone = telephone.substring(0, 4) + "****" + telephone.substring(telephone.length() - 4);
                }

                String mobile = item.getStr("mobile");
                if (StringUtils.isNoneBlank(mobile) && mobile.length() > 7) {
                    mobile = mobile.substring(0, 3) + "****" + mobile.substring(mobile.length() - 4);
                }

                item.set("telephone", telephone);
                item.set("mobile", mobile);
            });
        }
        return records;
    }

    /**
     * Get sensitive information, if has value return true to inform front end to show click button to get detail information, else return blank.
     *
     * @param fieldName
     * @param record
     * @return
     */
    private String getSensitiveField(String fieldName, Record record) {
        return record.getStr(fieldName) != null && !record.getStr(fieldName).isEmpty() ? "true" : "";
    }

    /**
     * @auyhor wyq
     * 根据客户id查询合同
     */
    public R queryContract(BasePageRequest<CrmCustomer> basePageRequest) {
        Integer customerId = basePageRequest.getData().getCustomerId().intValue();
        Integer pageType = basePageRequest.getPageType();
        if (basePageRequest.getData().getCheckstatus() != null) {
            if (0 == pageType) {
                return R.ok().put("data", Db.find(Db.getSql("crm.customer.queryPassContract"), customerId, basePageRequest.getData().getCheckstatus()));
            } else {
                return R.ok().put("data", Db.paginate(basePageRequest.getPage(), basePageRequest.getLimit(), new SqlPara().setSql(Db.getSql("crm.customer.queryPassContract")).addPara(customerId).addPara(basePageRequest.getData().getCheckstatus())));
            }
        }
        if (0 == pageType) {
            return R.ok().put("data", Db.find(Db.getSql("crm.customer.queryContract"), customerId));
        } else {
            return R.ok().put("data", Db.paginate(basePageRequest.getPage(), basePageRequest.getLimit(), new SqlPara().setSql(Db.getSql("crm.customer.queryContract")).addPara(customerId)));
        }
    }

    /**
     * @author wyq
     * 根据客户id查询回款计划
     */
    public R queryReceivablesPlan(BasePageRequest<CrmCustomer> basePageRequest) {
        Integer customerId = basePageRequest.getData().getCustomerId().intValue();
        Integer pageType = basePageRequest.getPageType();
        if (0 == pageType) {
            return R.ok().put("data", Db.find(Db.getSql("crm.customer.queryReceivablesPlan"), customerId));
        } else {
            return R.ok().put("data", Db.paginate(basePageRequest.getPage(), basePageRequest.getLimit(), new SqlPara().setSql(Db.getSql("crm.customer.queryReceivablesPlan")).addPara(customerId)));
        }
    }

    /**
     * @author wyq
     * 根据客户id查询回款
     */
    public R queryReceivables(BasePageRequest<CrmCustomer> basePageRequest) {
        Integer customerId = basePageRequest.getData().getCustomerId().intValue();
        if (0 == basePageRequest.getPageType()) {
            return R.ok().put("data", Db.find(Db.getSql("crm.customer.queryReceivables"), customerId));
        } else {
            return R.ok().put("data", Db.paginate(basePageRequest.getPage(), basePageRequest.getLimit(), new SqlPara().setSql(Db.getSql("crm.customer.queryReceivables")).addPara(customerId)));
        }
    }

    /**
     * @author wyq
     * 根据id删除客户
     */
    public R deleteByIds(String customerIds) {
        Integer contactsNum = Db.queryInt(Db.getSql("crm.customer.queryContactsNumber"), customerIds);
        Integer businessNum = Db.queryInt(Db.getSql("crm.customer.queryBusinessNumber"), customerIds);
        /*联系小计有记录不能删除*/
        Integer contactsNoteNum = Db.queryInt(Db.getSql("crm.customer.queryContactsNote"), customerIds);
        /*联系小计有记录不能删除*/
        if (contactsNum > 0 && businessNum > 0) {
            return R.error(CrmErrorInfo.CUSTOMER_CONTACTS_BUSINESS_NOT_NULL);
        }
        if (contactsNum > 0) {
            return R.error(CrmErrorInfo.CUSTOMER_CONTACTS_NOT_NULL);
        }
        if (businessNum > 0) {
            return R.error(CrmErrorInfo.CUSTOMER_BUSINESS_NOT_NULL);
        }
        if (contactsNoteNum > 0) {
            return R.error(CrmErrorInfo.CUSTOMER_NOTE_NOT_NULL);
        }
        String[] idsArr = customerIds.split(",");
        List<Record> idsList = new ArrayList<>(idsArr.length);
        for (String id : idsArr) {
            Record record = new Record();
            idsList.add(record.set("customer_id", Integer.valueOf(id)));
        }
        return Db.tx(() -> {
            //Db.batch(Db.getSql("crm.customer.updateIsDeleteByIds"), "customer_id", idsList, 100);
            Db.batch(Db.getSql("crm.customer.deleteByIds"), "customer_id", idsList, 100);
            return true;
        }) ? R.ok() : R.error();
    }

    /**
     * @author zxy
     * 条件查询客户公海
     */
    public Page<Record> queryPageGH(BasePageRequest<Object> basePageRequest) {
        return Db.paginate(basePageRequest.getPage(), basePageRequest.getLimit(), new SqlPara().setSql("select *  from customerview where owner_user_id = 0"));
    }

    /**
     * @author wyq
     * 客户锁定
     */
    public R lock(CrmCustomer crmCustomer) {
        String[] ids = crmCustomer.getIds().split(",");
        return Db.update(Db.getSqlPara("crm.customer.lock", Kv.by("isLock", crmCustomer.getIsLock()).set("ids", ids))) > 0 ? R.ok() : R.error();
    }


    /**
     * @author wyq
     * 根据客户ids获取合同ids
     */
    public String getContractIdsByCustomerIds(String customerIds) {
        String[] customerIdsArr = customerIds.split(",");
        StringBuilder stringBuffer = new StringBuilder();
        for (String id : customerIdsArr) {
            List<Record> recordList = Db.find("select contract_id from 72crm_crm_contract where customer_id = ?", id);
            if (recordList != null) {
                for (Record record : recordList) {
                    stringBuffer.append(',').append(record.getStr("contract_id"));
                }
            }
        }
        if (stringBuffer.length() > 0) {
            stringBuffer.deleteCharAt(0);
        }
        return stringBuffer.toString();
    }

    /**
     * @author wyq
     * 根据客户ids获取商机ids
     */
    public String getBusinessIdsByCustomerIds(String customerIds) {
        String[] customerIdsArr = customerIds.split(",");
        StringBuilder stringBuffer = new StringBuilder();
        for (String id : customerIdsArr) {
            List<Record> recordList = Db.find("select business_id from 72crm_crm_business where customer_id = ?", id);
            if (recordList != null) {
                for (Record record : recordList) {
                    stringBuffer.append(',').append(record.getStr("business_id"));
                }
            }
        }
        if (stringBuffer.length() > 0) {
            stringBuffer.deleteCharAt(0);
        }
        return stringBuffer.toString();
    }

    public List<Long> getBusinessIdListByCustomerIds(List<Long> customerIds) {
        List<Long> result = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(customerIds)) {
            for (Long customerId : customerIds) {
                List<Record> recordList = Db.find("select business_id from 72crm_crm_business where customer_id = ?", customerId);
                result.addAll(recordList.stream().map(record -> record.getLong("business_id")).collect(Collectors.toList()));
            }
        }
        return result;
    }


    /**
     * @author zxy
     * 定时将客户放入公海
     */
    public void putInInternational(Record record) {
        List<Long> ids = Db.query(Db.getSql("crm.customer.selectOwnerUserId"), Integer.parseInt(record.getStr("followupDay")) * 60 * 60 * 24, Integer.parseInt(record.getStr("dealDay")) * 60 * 60 * 24);
        if (ids != null && ids.size() > 0) {
            crmRecordService.addPutIntoTheOpenSeaRecord(ids, CrmEnum.CUSTOMER_TYPE_KEY.getTypes(), CrmConstant.PUBLIC_POOL);
            List<CrmCustomer> crmCustomers = CrmCustomer.dao.find(Db.getSqlPara("crm.customer.getListByIds", Kv.by("ids", ids)));
            if (CollectionUtils.isEmpty(crmCustomers)) {
                return;
            }
            for (CrmCustomer crmCustomer : crmCustomers) {
                if (Objects.nonNull(crmCustomer.getOwnerUserId())) {
                    //记录客户负责人变更日志
                     crmChangeLogService.saveCustomerChangeLog(CrmCustomerChangeLogEnum.OPEN_SEA.getCode(), crmCustomer.getCustomerId(), null, null, null);

                }
            }
            Db.update(Db.getSqlPara("crm.customer.updateOwnerUserId", Kv.by("ids", ids)));
        }

    }

    /**
     * @author wyq
     * 查询新增字段
     */
    public List<Record> queryField() {
        List<Record> fieldList = new LinkedList<>();
        String[] settingArr = new String[]{};
        fieldUtil.getFixedField(fieldList, "customerName", "客户名称", "", "text", settingArr, 1);
        fieldUtil.getFixedField(fieldList, "registerCapital", "注册资本", "", "text", settingArr, 0);
        fieldUtil.getFixedField(fieldList, "legalPerson", "法人", "", "text", settingArr, 0);
        fieldUtil.getFixedField(fieldList, "registrationNumber", "工商注册号", "", "text", settingArr, 0);
        fieldUtil.getFixedField(fieldList, "creditCode", "统一信用代码", "", "text", settingArr, 0);
        fieldUtil.getFixedField(fieldList, "remark", "备注", "", "text", settingArr, 0);
        fieldUtil.getFixedField(fieldList, "customerGrade", "客户等级", "", "tag", settingArr, 0);
        fieldUtil.getFixedField(fieldList, "partner", "生态伙伴", "", "text", settingArr, 0);
        fieldUtil.getFixedField(fieldList, "distributor", "分销商等级", "", "tag", settingArr, 0);
        fieldUtil.getFixedField(fieldList, "customerType", "客户类型", "", "tag", settingArr, 0);
        fieldUtil.getFixedField(fieldList, "isMultiple", "是否允许创建多个账号", "", "radio", settingArr, 0);
        fieldUtil.getFixedField(fieldList, "ownerUserId", "负责人", "", "text", settingArr, 0);
        Record map = new Record();
        fieldList.add(map.set("field_name", "map_address")
                .set("name", "地区定位")
                .set("form_type", "map_address")
                .set("is_null", 0));
        return fieldList;
    }

    public List<Record> queryExcelField() {
        List<Record> fieldList = new LinkedList<>();
        String[] settingArr = new String[]{};
        fieldUtil.getFixedField(fieldList, "customerName", "客户名称", "", "text", settingArr, 1);
        fieldUtil.getFixedField(fieldList, "registerCapital", "注册资本", "", "text", settingArr, 0);
        fieldUtil.getFixedField(fieldList, "legalPerson", "法人", "", "text", settingArr, 0);
        fieldUtil.getFixedField(fieldList, "registrationNumber", "工商注册号", "", "text", settingArr, 0);
        fieldUtil.getFixedField(fieldList, "creditCode", "统一信用代码", "", "text", settingArr, 0);
        fieldUtil.getFixedField(fieldList, "remark", "备注", "", "text", settingArr, 0);
        fieldUtil.getFixedField(fieldList, "province", "省", "", "text", settingArr, 1);
        fieldUtil.getFixedField(fieldList, "city", "市", "", "text", settingArr, 1);
        fieldUtil.getFixedField(fieldList, "region", "区", "", "text", settingArr, 0);
        fieldUtil.getFixedField(fieldList, "detailAddress", "详细地址", "", "text", settingArr, 0);
        fieldUtil.getFixedField(fieldList, "customerGrade", "客户等级", "", "text", settingArr, 1);
        fieldUtil.getFixedField(fieldList, "customerType", "客户类型", "", "text", settingArr, 1);
        fieldUtil.getFixedField(fieldList, "partner", "生态伙伴", "", "text", settingArr, 0);
        fieldUtil.getFixedField(fieldList, "distributor", "分销商等级", "", "text", settingArr, 0);
        fieldUtil.getFixedField(fieldList, "ownerUserId", "负责人", "", "text", settingArr, 0);
        fieldUtil.getFixedField(fieldList, "storageType", "库类型", "", "text", settingArr, 0);
//        fieldUtil.getFixedField(fieldList, "deptId", "部门", "", "text", settingArr, 0);
        fieldUtil.getFixedField(fieldList, "isMultiple", "是否允许该客户创建多个网站会员账号", "", "text", settingArr, 0);

        return fieldList;
    }

    /**
     * @author wyq
     * 查询编辑字段
     */
    public List<Record> queryField(Integer customerId) {

        List<Record> fieldList = new LinkedList<>();
        Record record = Db.findFirst(Db.getSqlPara("crm.customer.queryCustomerInfo", Kv.by("customer_id", customerId)));
        String[] settingArr = new String[]{};
        fieldUtil.getFixedField(fieldList, "customerName", "客户名称", record.getStr("customer_name"), "text", settingArr, 1);
        fieldUtil.getFixedField(fieldList, "registerCapital", "注册资本", record.getStr("register_capital"), "text", settingArr, 0);
        fieldUtil.getFixedField(fieldList, "legalPerson", "法人", record.getStr("legal_person"), "text", settingArr, 0);
        fieldUtil.getFixedField(fieldList, "registrationNumber", "工商注册号", record.getStr("registration_number"), "text", settingArr, 0);
        fieldUtil.getFixedField(fieldList, "creditCode", "统一信用代码", record.getStr("credit_code"), "text", settingArr, 0);
        fieldUtil.getFixedField(fieldList, "remark", "备注", record.getStr("remark"), "text", settingArr, 0);
        fieldUtil.getFixedField(fieldList, "customerGrade", "客户等级", record.getStr("customer_grade"), "tag", settingArr, 0);
        fieldUtil.getFixedField(fieldList, "partner", "生态伙伴", record.getStr("partner"), "tag", settingArr, 0);
        fieldUtil.getFixedField(fieldList, "distributor", "分销商等级", record.getStr("distributor"), "tag", settingArr, 0);
        fieldUtil.getFixedField(fieldList, "customerType", "客户类型", record.getStr("customer_type"), "tag", settingArr, 0);
        fieldUtil.getFixedField(fieldList, "isMultiple", "是否允许创建多个账号", record.getStr("is_multiple"), "radio", settingArr, 0);
        fieldUtil.getFixedField(fieldList, "ownerUserId", "负责人", record.getStr("owner_user_id"), "text", settingArr, 0);

        Record map = new Record();
        fieldList.add(map.set("fieldName", "map_address")
                .set("name", "地区定位")
                .set("value", Kv.by("location", record.getStr("location"))
                        .set("address", record.getStr("address"))
                        .set("detailAddress", record.getStr("detail_address"))
                        .set("lng", record.getStr("lng"))
                        .set("lat", record.getStr("lat")))
                .set("formType", "map_address")
                .set("isNull", 0));
        fieldList.addAll(adminFieldService.queryByBatchId(record.getStr("batch_id")));
        return fieldList;
    }


    /**
     * @author HJP
     * 员工客户分析
     */
    public R getUserCustomerAnalysis(BasePageRequest<AdminUser> basePageRequest) {
        AdminUser adminUser = basePageRequest.getData();
        String sql = "select max(au.user_id) user_id,max(au.realname) realname,max(au.username) username,max(au.dept_id) dept_id,count(dd.customer_id) customerNum,max(sc.sc) finishCustomerNum,convert(max(sc.sc)*100/count(dd.customer_id),decimal(15,2)) finishCustomerR,sum(contractMoney) contractMoney,sum(receivablesMoney) receivablesMoney,sum(unfinishReR) unfinishReR,sum(reFinishR) reFinishR \n";
        StringBuilder stringBuilder = new StringBuilder();
        if (adminUser.getDeptId() != null) {
            stringBuilder.append(" and dept_id = ").append(adminUser.getDeptId());
        }
        if (adminUser.getUserId() != null) {
            stringBuilder.append(" and user_id = ").append(adminUser.getUserId());
        }
        StringBuilder where2 = new StringBuilder();
        StringBuilder where3 = new StringBuilder();
        if (adminUser.getStartTime() != null) {
            where2.append(" and contract.create_time >= ").append(adminUser.getStartTime());
            where2.append(" and cc.create_time >= ").append(adminUser.getStartTime());
            where2.append(" and cr.create_time >= ").append(adminUser.getStartTime());
            where3.append(" and create_time >= ").append(adminUser.getStartTime());
        }
        if (adminUser.getEndTime() != null) {
            where2.append(" and contract.create_time <= ").append(adminUser.getEndTime());
            where2.append(" and cc.create_time <= ").append(adminUser.getEndTime());
            where2.append(" and cr.create_time <= ").append(adminUser.getEndTime());
            where3.append(" and create_time <= ").append(adminUser.getEndTime());
        }
        String from = "from 72crm_admin_user au \n"
                + "left join(select cc.customer_id,max(cc.owner_user_id) owner_user_id,sum(contract.money) contractMoney,sum(cr.money) receivablesMoney,(sum(contract.money)-sum(cr.money)) as unfinishReR ,convert(sum(cr.money)*100/sum(contract.money),decimal(15,2)) as reFinishR  \n"
                + "from 72crm_crm_customer cc \n"
                + "left join 72crm_crm_contract contract \n"
                + "on cc.customer_id=contract.customer_id \n"
                + "left join 72crm_crm_receivables cr \n"
                + "on cc.customer_id=cr.customer_id where 1=1\n"
                + where2 + "\n"
                + "group by cc.customer_id) as dd \n"
                + "on au.user_id=dd.owner_user_id \n"
                + "left join (select owner_user_id,count(case when deal_status='成交' then customer_id end) as sc \n"
                + "from 72crm_crm_customer where 1=1 \n"
                + where3 + "\n"
                + "group by owner_user_id) sc on au.user_id=sc.owner_user_id \n"
                + "where au.status = 1 \n"
                + stringBuilder.toString() + "\n"
                + "group by au.user_id";
        List<Record> records = Db.find(sql + from);
        return R.ok().put("data", records);
    }

    /**
     * @author wyq
     * 导出客户
     */
    public List<Record> exportCustomer(String customerIds) {
        String[] customerIdsArr = customerIds.split(",");
        return Db.find(Db.getSqlPara("crm.customer.excelExport", Kv.by("ids", customerIdsArr)));
    }

    /**
     * 领取或分配客户
     *
     * @author zxy
     */
    @Before(Tx.class)
    public R getCustomersByIds(String ids, Long userId) {
        logger.info("customer getCustomersByIds方法ids {}", ids);
        crmRecordService.addDistributionRecord(ids, CrmEnum.CUSTOMER_TYPE_KEY.getTypes(), userId);
        if (userId == null) {
            userId = BaseUtil.getUserId();
        }
        String[] idsArr = ids.split(",");
        CrmOwnerRecord crmOwnerRecord = new CrmOwnerRecord();
        List<Long> idList = Lists.newArrayListWithCapacity(idsArr.length);
        for (String id : idsArr) {
            idList.add(Long.valueOf(id));
            crmOwnerRecord.clear();
            crmOwnerRecord.setTypeId(Integer.valueOf(id));
            crmOwnerRecord.setType(8);
            crmOwnerRecord.setPostOwnerUserId(userId == null ? null : userId.intValue());
            crmOwnerRecord.setCreateTime(DateUtil.date());
            crmOwnerRecord.save();
        }
        List<CrmCustomer> crmCustomers = queryCustomerListByCustomerIds(idList);
        if (CollectionUtils.isNotEmpty(crmCustomers)) {
            for (CrmCustomer crmCustomer : crmCustomers) {
                Long oldOwnerUserId = Objects.nonNull(crmCustomer.getOwnerUserId()) ? Long.valueOf(crmCustomer.getOwnerUserId()) : null;
                if (!Objects.equals(oldOwnerUserId, userId)) {
                    //记录客户负责人变更日志
                    crmChangeLogService.saveCustomerChangeLog(CrmCustomerChangeLogEnum.getBdByStorageType(crmCustomer.getStorageType()), crmCustomer.getCustomerId(), userId, null, userId);

                }
            }
        }

        String sql = "update 72crm_crm_customer set owner_user_id = " + userId + " where customer_id in (" + ids + ")";
        return Db.update(sql) > 0 ? R.ok() : R.error();
    }

    /**
     * @author wyq
     * 获取客户导入查重字段
     */
    public R getCheckingField() {
        return R.ok().put("data", "客户名称");
    }

    /**
     * 导入客户
     *
     * @author wyq
     */
    @Before(Tx.class)
    public R uploadExcel(UploadFile file, Integer repeatHandling) {
        try (ExcelReader reader = ExcelUtil.getReader(FileUtil.file(file.getUploadPath() + "\\" + file.getFileName()))) {
            List<List<Object>> read = reader.read();
            List<Object> list = read.get(0);
            Kv kv = new Kv();
            for (int i = 0; i < list.size(); i++) {
                kv.set(list.get(i), i);
            }
            List<Record> fieldList = queryExcelField();
            fieldList.forEach(record -> {
                if (record.getInt("is_null") == 1) {
                    record.set("name", record.getStr("name") + "(*)");
                }
            });
            List<String> nameList = fieldList.stream().map(record -> record.getStr("name")).collect(Collectors.toList());
            if (nameList.size() != list.size() || !nameList.containsAll(list)) {
                return R.error("请使用最新导入模板");
            }
            if (read.size() > 1) {
                JSONObject object = new JSONObject();
                for (int i = 1; i < read.size(); i++) {
                    List<Object> customerList = read.get(i);
                    if (customerList.size() < list.size()) {
                        for (int j = customerList.size() - 1; j < list.size(); j++) {
                            customerList.add(null);
                        }
                    }
                    Integer number;
                    String customerName;
                    if (customerList.get(kv.getInt("客户名称(*)")) != null) {
                        customerName = customerList.get(kv.getInt("客户名称(*)")).toString();
                        number = Db.queryInt("select count(*) from 72crm_crm_customer where customer_name = ?", customerName);
                    } else {
                        return R.error(CrmErrorInfo.CUSTOMER_IS_NOT_NULL);
                    }
                    /*格式化EXCEL数据操作*/
                    CrmCustomer crmCustomer;
                    R r = formatExcelInfo(customerList, kv);
                    if (r.get("code").equals(500)) {
                        return r;
                    } else {
                        crmCustomer = (CrmCustomer) r.get("entity");
                    }
                    /*格式化EXCEL数据操作*/
                    if (0 == number) {
                        object.fluentPut("entity", new JSONObject().fluentPut("customer_name", customerName)
                                .fluentPut("registerCapital", customerList.get(kv.getInt("注册资本")))
                                .fluentPut("legalPerson", customerList.get(kv.getInt("法人")))
                                .fluentPut("registrationNumber", customerList.get(kv.getInt("工商注册号")))
                                .fluentPut("creditCode", customerList.get(kv.getInt("统一信用代码")))
                                .fluentPut("remark", customerList.get(kv.getInt("备注")))
                                .fluentPut("address", crmCustomer.getAddress())
                                .fluentPut("detail_address", customerList.get(kv.getInt("详细地址")))
                                .fluentPut("customerGrade", crmCustomer.getCustomerGrade())
                                .fluentPut("distributor", crmCustomer.getDistributor())
                                .fluentPut("customerType", crmCustomer.getCustomerType())
                                .fluentPut("ownerUserId", crmCustomer.getOwnerUserId())
                                .fluentPut("deptId", crmCustomer.getDeptId())
                                .fluentPut("isMultiple", crmCustomer.getIsMultiple())
                                .fluentPut("partner", crmCustomer.getPartner())
                                .fluentPut("customer_origin", CustomerOriginEnum.CUSTOMER_NEW_KEY.getTypes())
                        );
                    } else if (number > 0 && repeatHandling == 1) {
                        Record leads = Db.findFirst("select customer_id,batch_id from 72crm_crm_customer where customer_name = ?", customerName);
                        object.fluentPut("entity", new JSONObject().fluentPut("customer_id", leads.getInt("customer_id"))
                                .fluentPut("customerName", customerName)
                                .fluentPut("registerCapital", customerList.get(kv.getInt("注册资本")))
                                .fluentPut("legalPerson", customerList.get(kv.getInt("法人")))
                                .fluentPut("registrationNumber", customerList.get(kv.getInt("工商注册号")))
                                .fluentPut("creditCode", customerList.get(kv.getInt("统一信用代码")))
                                .fluentPut("remark", customerList.get(kv.getInt("备注")))
                                .fluentPut("address", crmCustomer.getAddress())
                                .fluentPut("detail_address", customerList.get(kv.getInt("详细地址")))
                                .fluentPut("customerGrade", crmCustomer.getCustomerGrade())
                                .fluentPut("distributor", crmCustomer.getDistributor())
                                .fluentPut("customerType", crmCustomer.getCustomerType())
                                .fluentPut("ownerUserId", crmCustomer.getOwnerUserId())
                                .fluentPut("deptId", crmCustomer.getDeptId())
                                .fluentPut("isMultiple", crmCustomer.getIsMultiple())
                                .fluentPut("partner", crmCustomer.getPartner())
                                .fluentPut("customer_origin", CustomerOriginEnum.CUSTOMER_NEW_KEY.getTypes())
                                .fluentPut("batch_id", leads.getStr("batch_id")));
                    } else if (number > 0 && repeatHandling == 2) {
                        continue;
                    }

                    object.put(CUSTOMER_CHANGE_CHANNEL_EVENT, CrmOperateChannelEventEnum.CUSTOMER_UPLOAD_BY_EXCEL.getName());
                    object.put(CUSTOMER_CHANGE_HISTORY, "客户excel导入");

                    //设置客户来源
                    object.getJSONObject("entity").put("fromSource", FromSourceEnum.BY_MANUAL.getCode());

                    R saveInfo = addOrUpdate(object, BaseUtil.getUserId());
                    if (Objects.nonNull(saveInfo)) {
                        if (!CrmConstant.SUCCESS.equals(saveInfo.get("code"))) {
                            return R.error(saveInfo.get("msg").toString());
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return R.error();
        }
        return R.ok();
    }

    /**
     * 导入客户
     *
     * @author vic
     */
    @Before(Tx.class)
    public R uploadExcelMultiThread(UploadFile file, Integer repeatHandling, Integer ownerUserId) throws Exception {
        try (ExcelReader reader = ExcelUtil.getReader(FileUtil.file(file.getUploadPath() + "\\" + file.getFileName()))) {
            List<List<Object>> read = reader.read();
            List<Object> headList = read.get(0);
            Kv kv = new Kv();
            for (int i = 0; i < headList.size(); i++) {
                kv.set(headList.get(i), i);
            }
            List<Record> fieldList = queryExcelField();
            fieldList.forEach(record -> {
                if (record.getInt("is_null") == 1) {
                    record.set("name", record.getStr("name") + "(*)");
                }
            });
            List<String> nameList = fieldList.stream().map(record -> record.getStr("name")).collect(Collectors.toList());
            if (nameList.size() != headList.size() || !nameList.containsAll(headList)) {
                return R.error("请使用最新导入模板");
            }

            Map<Long,Record> userCapacityMap = Maps.newHashMap();
            if (read.size() > 1) {
                //前20个异常信息
                StringBuilder all = new StringBuilder();
                //异常个数计数器
                int j = 0;
                //判断库容
                Integer storageType;
                for (int i = 1; i < read.size(); i++) {
                    if (j >= 20){
                        return R.error("校验发现不合规数据，请您修改后再导入：\r\n" + all.toString());
                    }

                    List<Object> customerList = read.get(i);

                    int subSize = headList.size() - customerList.size();
                    if (subSize > 0){
                        for (int k = 0; k < subSize; k++) {
                            customerList.add(null);
                        }
                    }

                    //判断负责人字段
                    if (!StringUtil.isNullOrEmpty((String) customerList.get(kv.getInt(CrmTagConstant.EXCEL_USER)))) {
                        //负责人和库类型只能同时不为空或同时为空
                        String storageTypeString = (String)customerList.get(kv.getInt(CrmConstant.STORAGE_TYPE));
                        if (StringUtils.isBlank(storageTypeString)){
                            all.append('第').append(i + 1).append("行错误：").append(CrmErrorInfo.OWNER_NOT_NULL_STORAGE_NULL).append("；\r\n");
                            j++;
                            continue;
                        }
                        storageType = CustomerStorageTypeEnum.getCodeByName(storageTypeString);
                        if (storageType == null){
                            all.append('第').append(i + 1).append("行错误：").append(CrmErrorInfo.STORAGE_VALIDATE).append("；\r\n");
                            j++;
                            continue;
                        }

                        Record userRecord = Db.findFirst(Db.getSql("admin.user.getUserInfoByRealName"), customerList.get(kv.getInt(CrmTagConstant.EXCEL_USER)));
                        if (Objects.isNull(userRecord)) {
                            all.append('第').append(i + 1).append("行错误：").append(String.format(CrmErrorInfo.OWNER_USER_NAME + "(%s)" + CrmErrorInfo.OWNER_USER_NAME_NOT_NULL, customerList.get(kv.getInt(CrmTagConstant.EXCEL_USER)))).append("；\r\n");
                            j++;
                            continue;
                        }
                        Long userId = userRecord.getLong("user_id");
                        userRecord = userCapacityMap.get(userId);
                        if (userRecord == null){
                            userRecord = searchUserCapacity(userId);
                            userCapacityMap.put(userId,userRecord);
                        }
                        //判断库容类型
                        if (CustomerStorageTypeEnum.INSPECT_CAP.getCode() == storageType){
                            userRecord.set("checkedInspectCount", userRecord.getInt("checkedInspectCount") == null ? 1 : userRecord.getInt("checkedInspectCount") + 1);
                            if (userRecord.getInt("inspect_cap") != null && userRecord.getInt("inspect_cap") - userRecord.getInt("used_inspect_cap") - userRecord.getInt("checkedInspectCount") < 0){
                                all.append('第').append(i + 1).append("行错误：").append(customerList.get(kv.getInt(CrmTagConstant.EXCEL_USER)))
                                        .append("可用").append(CustomerStorageTypeEnum.INSPECT_CAP.getName()).append("库容不足(已用库容：")
                                        .append(userRecord.getInt("used_inspect_cap")).append('/').append(userRecord.getInt("inspect_cap"))
                                        .append(")，请删减后重试")
                                        .append("；\r\n");
                                j++;
                            }
                        }else {
                            userRecord.set("checkedRelateCount", userRecord.getInt("checkedRelateCount") == null ? 1 : userRecord.getInt("checkedRelateCount") + 1);
                            if (userRecord.getInt("relate_cap") != null && userRecord.getInt("relate_cap") - userRecord.getInt("used_relate_cap") - userRecord.getInt("checkedRelateCount") < 0){
                                all.append('第').append(i + 1).append("行错误：").append(customerList.get(kv.getInt(CrmTagConstant.EXCEL_USER)))
                                        .append("可用").append(CustomerStorageTypeEnum.RELATE_CAP.getName()).append("库容不足(已用库容：")
                                        .append(userRecord.getInt("used_relate_cap")).append('/').append(userRecord.getInt("relate_cap"))
                                        .append(")，请删减后重试")
                                        .append("；\r\n");
                                j++;
                            }
                        }
                    }

                }

                if (all.length() > 0){
                    return R.error("校验发现不合规数据，请您修改后再导入：\r\n" + all.toString());
                }

                List<R> listR = this.uploadTask(CrmThreadPool.INSTANCE.getInstance(), read, headList, kv, repeatHandling, ownerUserId);

                if (listR.stream().anyMatch(r -> !r.isSuccess())) {
                    // 行号
                    int i = 1;
                    // 待回归数据
                    List<R> rollbackR = new ArrayList<>();
                    for (R r : listR) {
                        if (!r.isSuccess()) {
                            if (j < 20) {
                                all.append('第').append(i + 1).append("行错误：").append(r.get("msg")).append("；\r\n");
                            }
                            j++;
                        } else {
                            rollbackR.add(r);
                        }
                        i++;
                    }
                    // 回滚数据
                    if (rollbackR.size() > 0) {
                        List<Long> idList = rollbackR.stream()
                                .filter(r -> Objects.nonNull(r.get("data")) && Objects.nonNull(((Map) r.get("data")).get("customer_id")))
                                .map(r -> (Long) ((Map) r.get("data")).get("customer_id")).collect(Collectors.toList());
                        List<Long> changeLogIds = rollbackR.stream().filter(r -> Objects.nonNull(r.get("changeLogId")))
                                .map(p -> (Long) p.get("changeLogId")).collect(Collectors.toList());
                        if (idList.size() > 0) {
                            Db.update(Db.getSqlPara("crm.customer.deleteByCustomerIds", Kv.by("ids", idList)));
                            crmRecordService.deleteActionRecordsByTypeAndActionIds(CrmEnum.CUSTOMER_UPLOAD_BY_EXCEL.getTypes(),
                                    idList);
                        }
                        if (CollectionUtils.isNotEmpty(changeLogIds)) {
                            Db.update(Db.getSqlPara("crm.changeLog.deleteCustomerLogsByIds", Kv.by("ids", changeLogIds)));
                        }
                    }

                    return R.error("校验发现不合规数据，请您修改后再导入：\r\n" + all.toString());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
        return R.ok();
    }

    private List<R> uploadTask(ExecutorService executor, List<List<Object>> read, List<Object> list, Kv kv, Integer repeatHandling, Integer ownerUserId) throws Exception {

        List<R> result = new ArrayList<>(64);
        logger.info("保存任务正在执行。。。");
        List<Future<R>> listF = new ArrayList<>(64);
        final CountDownLatch latch = new CountDownLatch(read.size() - 1);
        Long userId = BaseUtil.getUserId();
        AtomicInteger count = new AtomicInteger(0);
        for (int i = 1; i < read.size(); i++) {
            Future<R> fut = executor.submit(new CallableThread(count.incrementAndGet(), read, list, kv, repeatHandling, ownerUserId, latch, userId));
            listF.add(fut);
        }
        logger.info("保存任务结束");

        //等待所有线程执行完成后 对返回值进行合并处理
        latch.await();
        for (Future<R> fut : listF) {
            result.add(fut.get());
        }
        logger.info("单独提交的任务执行结果:{}",result);
        return result;
    }

    /**
     * 领取客户逻辑
     *
     * @param customerId   客户ID
     * @param industryCode 客户行业编码
     * @param deptId
     * @param esbConfig
     * @return R
     */
    public R canReceiveWithIndustry(Long customerId, String industryCode, Long deptId, EsbConfig esbConfig) {
        logger.info("{} {} param[customerId:{} ,industryCode:{} ,deptId:{}]", getClass().getSimpleName(), "canReceiveWithIndustry", customerId, industryCode, deptId);

        CrmCustomer crmCustomer = CrmCustomer.dao.findFirst(Db.getSql("crm.customer.queryByCustomerId"), customerId);
        if (Objects.isNull(crmCustomer)) {
            return R.error("客户数据不存在，无法领取");
        }

        //检查客户是否被领取
        CrmCustomerExt crmCustomerExt = CrmCustomerExt.dao.findFirst(Db.getSql("crm.customerExt.queryByCustomerId"),Kv.by("customerId",customerId));
        if (crmCustomerExt != null && crmCustomerExt.getDrawFlag() == 1){
            return R.error("该客户的领取申请审批中，无法重复领取");
        }

        Long userId = BaseUtil.getUserId();

        //判断用户是否可以领取
        //获取用户事业部ID
        String businessDeptId = adminDeptService.getBusinessDepartmentByDeptId(String.valueOf(deptId));
        //检查用户归属配置
        Record config = adminIndustryOfDeptService.findConfigByIndustryCode(industryCode);
        //配置为专属并且部门非本事业部的不可以领取
        if (Objects.nonNull(config) && Objects.equals(CustomerIndustryEnum.EXCLUSIVE.getCode(), config.getInt("industry_type")) && !Objects.equals(config.getLong("dept_id"), Long.valueOf(businessDeptId))) {
            return R.error("该行业的客户为其它部门专属，您无权领取");
        }

        //判断当前领取库容是否可以领取
        boolean checkUserCapacity = checkUserCapacity(userId, CustomerStorageTypeEnum.INSPECT_CAP.getCode(), 1);
        if (!checkUserCapacity){
            return R.error("您的考察库库容已满，无法领取客户");
        }

        String originBusinessDeptId = adminDeptService.getBusinessDepartmentByDeptId(String.valueOf(crmCustomer.getDeptId()));
        if (StringUtils.equals(businessDeptId, originBusinessDeptId)) {
            //同事业部无需计算业绩
            return R.ok();
        }

        CrmPerformanceDto crmPerformanceDto = getCustomerPerformanceMsg(customerId, userId, esbConfig,null);
        if (crmPerformanceDto.getOrderAmount().compareTo(BigDecimal.ZERO) == 0) {
            //到单数据为空，用户可以直接领取
            return R.ok();
        }

        //组装msg
        return R.ok(String.format("该客户过去365天归属在线运营部的订单总额为%s元，您领取该客户，在线运营事业部今年的业绩目标将减少%s元，您的业绩目标将增加%s元，请确认是否领取", crmPerformanceDto.getOrderAmount(), crmPerformanceDto.getWebsitePerformance(), crmPerformanceDto.getBdPerformance()));
    }

    /**
     * 从esb获取客户业绩等信息
     * @param customerId
     * @param userId
     * @param esbConfig
     * @param orderAmount
     * @return
     */
    public CrmPerformanceDto getCustomerPerformanceMsg(Long customerId,Long userId, EsbConfig esbConfig,BigDecimal orderAmount){
        if (orderAmount == null){
            //获取在线运营事业部部门ID
            String onlineBusinessDeptId = adminConfigService.getConfig(CrmConstant.ONLINE_BUSINESS_DEPT_ID, "在线运营事业部ID");
            //获取用户到单金额
            orderAmount = bopsService.getCustomerOrderAmount(customerId, Long.valueOf(onlineBusinessDeptId), esbConfig);
            logger.info("getCustomerOrderAmount - [customerId:{} ,orderAmount:{}]", customerId, orderAmount);
        }

        //查询完成部门业绩倍数
        BigDecimal websitePerformanceTime = getPerformanceTime(CrmConstant.WEBSITE_PERFORMANCE_INCLUDED);
        //查询BD业绩倍数
        BigDecimal bdPerformanceTime = getPerformanceTime(CrmConstant.TARGET_DEPT_PERFORMANCE_INCLUDED);
        //到年底的天数
        LocalDate now = LocalDate.now();
        LocalDate endOfYear = LocalDate.of(now.getYear(), 12, 31);
        int lastDays = endOfYear.getDayOfYear() - now.getDayOfYear();
        BigDecimal orderAmount1 = BigDecimal.valueOf(lastDays).divide(BopsService.DAYS_OF_YEAR, 6, BigDecimal.ROUND_HALF_UP).multiply(orderAmount);
        //计算部门业绩
        BigDecimal websitePerformance = orderAmount1.multiply(websitePerformanceTime).setScale(2, BigDecimal.ROUND_HALF_UP);
        //计算BD业绩
        BigDecimal bdPerformance = orderAmount1.multiply(bdPerformanceTime).setScale(2, BigDecimal.ROUND_HALF_UP);

        CrmPerformanceDto crmPerformanceDto = new CrmPerformanceDto();
        crmPerformanceDto.setWebsitePerformance(websitePerformance);
        crmPerformanceDto.setBdPerformance(bdPerformance);
        crmPerformanceDto.setOrderAmount(orderAmount);
        crmPerformanceDto.setUserId(userId);
        crmPerformanceDto.setCustomerId(customerId);
        crmPerformanceDto.setReceiveTime(DateUtil.date());

        //保存业绩信息到redis
        redisCache.put(CrmConstant.PERFORMANCE_USER_CUSTOMER + userId +"_" + customerId,crmPerformanceDto,30, TimeUnit.DAYS);

        return crmPerformanceDto;
    }


    /**
     * 确认领取客户
     *
     * @param customerId   客户ID
     * @param industryCode 行业编码
     * @param userId       用户ID
     * @param deptId       部门ID
     * @param esbConfig
     * @return result
     */
    @Before(Tx.class)
    public R confirmReceiveWithIndustry(Long customerId, String industryCode, Long userId, Long deptId, EsbConfig esbConfig) {
        logger.info("confirmReceiveWithIndustry param:[cid: {} ,industryCode: {} ,uid: {} ,did: {} ]", customerId, industryCode, userId, deptId);

        //获取用户事业部ID
        String businessDeptIdStr = adminDeptService.getBusinessDepartmentByDeptId(String.valueOf(deptId));
        logger.info("confirmReceiveWithIndustry businessDeptId: {}", businessDeptIdStr);
        if (StringUtils.isEmpty(businessDeptIdStr)) {
            return R.error("未能找到用户事业部");
        }
        Long businessDeptId = Long.valueOf(businessDeptIdStr);
        //判断用户是否可以领取
        Record customerInfo = queryOwnerByCustomerId(customerId);
        if (Objects.isNull(customerInfo)) {
            return R.error("没有查询到客户信息");
        }
        //检查用户归属配置
        Record config = adminIndustryOfDeptService.findConfigByIndustryCode(industryCode);
        //配置为专属并且部门非本事业部的不可以领取
        if (Objects.nonNull(config) && Objects.equals(CustomerIndustryEnum.EXCLUSIVE.getCode(), config.getInt("industry_type")) && !Objects.equals(config.getLong("dept_id"), businessDeptId)) {
            return R.error("该行业的客户为其它部门专属，您无权领取");
        }
        Db.tx(() -> {
            //获取原OwnerId
            Long originOwnerId = customerInfo.getLong("owner_user_id");
            Integer originDeptId = customerInfo.getInt("dept_id");
            String customerName = customerInfo.getStr("customer_name");
            if (Objects.equals(originOwnerId, userId)) {
                throw new CrmException("不可以重复领取自己的客户");
            }
            //领取人是否同事业部
            boolean sameBusinessDept = false;
            //是否网站池客户(owner空，deptId空)
            boolean onlineBusinessDept = Objects.isNull(originOwnerId) && Objects.isNull(originDeptId);
            //是否电销池客户(owner不空，owner的deptId是电销团队)
            boolean phoneSaleBusinessDept = false;
            //获取在线运营事业部部门ID
            String onlineBusinessDeptId = adminConfigService.getConfig(CrmConstant.ONLINE_BUSINESS_DEPT_ID, "在线运营事业部ID");
            if (Objects.nonNull(originOwnerId)) {
                //风险点：现在是通过判断事业部的方式，可能会造成在线运营事业部下的非电销团队也会被判断成电销团队用户
                //获取原Owner的事业部ID
                Long originBusinessDeptId = adminUserService.getBusinessDepartmentOfUserById(originOwnerId);
                sameBusinessDept = Objects.equals(businessDeptId, originBusinessDeptId);
                phoneSaleBusinessDept = Objects.equals(Long.valueOf(onlineBusinessDeptId), originBusinessDeptId);
            }

            //判断当前领取库容是否可以领取
            boolean checkUserCapacity = checkUserCapacity(userId, CustomerStorageTypeEnum.INSPECT_CAP.getCode(), 1);
            if (!checkUserCapacity) {
                throw new CrmException("您的考察库库容已满，无法领取客户");
            } else {
                //保存用户扩展表
                saveCrmCustomerExt(customerId, CustomerStorageTypeEnum.INSPECT_CAP.getCode(),null,null);
            }

            /*更新用户Owner与行业*/
            if (Db.update(Db.getSqlPara("crm.customer.receiveWithIndustry", Kv.by("userId", userId).set("industryCode", industryCode).set("customerId", customerId).set("originOwnerId", originOwnerId))) == 0) {
                //没更新到数据回滚
                throw new CrmException("领取失败，客户已被领取");
            }

            /* 删掉团队成员表中的userId为ownerUserId的数据 */
            crmGroupMemberService.deleteMember(customerId, userId, Integer.valueOf(CrmEnum.CUSTOMER_TYPE_KEY.getTypes()), null);

            //添加操作日志
            String dicName = adminDataDicService.getNameByTagNameAndValue(CrmTagConstant.INDUSTRY, industryCode);
            crmRecordService.addActionRecord(userId.intValue(), CrmEnum.CUSTOMER_TYPE_KEY.getTypes(), customerId.intValue(), String.format("领取了用户[%s]", customerName), String.format("行业修改成[%s]", Optional.ofNullable(dicName).orElse(industryCode)));
            //记录客户负责人变更日志
            crmChangeLogService.saveCustomerChangeLog(CrmCustomerChangeLogEnum.getBdByStorageType(CustomerStorageTypeEnum.INSPECT_CAP.getCode()), customerId, userId, null, userId);

            if (sameBusinessDept || (!onlineBusinessDept && !phoneSaleBusinessDept)) {
                //同部门或者非网站客户池和电销客户池的领取不需要处理业绩
                logger.info("confirmReceiveWithIndustry userId:{},customerId:{} same:{} online:false phone:{} no need to save performance", userId, customerId, sameBusinessDept, phoneSaleBusinessDept);
                return true;
            }
            //获取用户到单金额
            CrmPerformanceDto crmPerformanceDto = redisCache.get(CrmConstant.PERFORMANCE_USER_CUSTOMER + userId + "_" + customerId);
            if (crmPerformanceDto == null){
                crmPerformanceDto = getCustomerPerformanceMsg(customerId, userId, esbConfig,null);
            }
            if (crmPerformanceDto.getOrderAmount().compareTo(BigDecimal.ZERO) == 0) {
                //到单数据为空，用户可以直接领取,并清空redis
                ClearCustomerDrawFlagAndCache(customerId,userId);
                return true;
            }

            BigDecimal websitePerformance = crmPerformanceDto.getWebsitePerformance();
            BigDecimal bdPerformance = crmPerformanceDto.getBdPerformance();
            Date receiveTime = crmPerformanceDto.getReceiveTime();

            //记录部门绩效
            //-原部门目标业绩减少(如果是网站池领取，业绩加到在线运营事业部，如果是电销池领取，业绩加到BD)
            String batchId = IdUtil.fastSimpleUUID();
            if (onlineBusinessDept) {
                crmPerformanceService.addPerformance(batchId, Long.valueOf(onlineBusinessDeptId), PerformanceObjectTypeEnum.DEPARTMENT.getCode(), null, websitePerformance.negate(), PerformanceFromChannelEnum.WEBSITE_POOL, userId, PerformanceTargetTypeEnum.CUSTOMER, customerId, PerformanceChangeChannelEnum.CUSTOMER_RECEIVED.getDesc(),receiveTime);
            } else {
                crmPerformanceService.addPerformance(batchId, originOwnerId, PerformanceObjectTypeEnum.BD.getCode(), null, websitePerformance.negate(), PerformanceFromChannelEnum.MOBILE_SAIL_POOL, userId, PerformanceTargetTypeEnum.CUSTOMER, customerId, PerformanceChangeChannelEnum.CUSTOMER_RECEIVED.getDesc(),receiveTime);
            }
            //-目标BD目标业绩增加
            crmPerformanceService.addPerformance(batchId, userId, PerformanceObjectTypeEnum.BD.getCode(), null, bdPerformance, onlineBusinessDept ? PerformanceFromChannelEnum.WEBSITE_POOL : PerformanceFromChannelEnum.MOBILE_SAIL_POOL, onlineBusinessDept ? Long.valueOf(onlineBusinessDeptId) : originOwnerId, PerformanceTargetTypeEnum.CUSTOMER, customerId, PerformanceChangeChannelEnum.CUSTOMER_RECEIVE.getDesc(),receiveTime);

            ClearCustomerDrawFlagAndCache(customerId, userId);
            return true;
        });
        return R.ok("客户领取成功");
    }

    /**
     * 清空客户领取中标识和redis缓存
     * @param customerId
     * @param userId
     */
    public void ClearCustomerDrawFlagAndCache(Long customerId,Long userId) {
        //清除领取中标识
        Db.update(Db.getSqlPara("crm.customerExt.clearCustomerDrawFlag", Kv.by("customerId",customerId)));

        //清除redis缓存
        redisCache.remove(CrmConstant.PERFORMANCE_USER_CUSTOMER + userId + "_" + customerId);
    }

    /**
     * 清楚客户的Owner数据
     * ！！Warning！！仅供测试使用
     *
     * @param customerId
     */
    public void cleanOwnerOfCustomerOnlyForTest(Long customerId) {
        Db.update("update 72crm_crm_customer set owner_user_id = null,dept_id = null where customer_id=?", customerId);
    }

    private BigDecimal getPerformanceTime(String configName) {
        return BigDecimal.valueOf(Double.parseDouble(adminConfigService.getConfig(configName, "业绩倍数配置")));
    }

    public CrmCustomer queryCustomerBySiteMemberId(Long siteMemberId) {
        return CrmCustomer.dao.findFirst(Db.getSql("crm.customer.queryCustomerBySiteMemberId"),siteMemberId);
    }

    /**
     * 获取团队成员
     * @param objId
     * @param objType
     * @return
     */
    public List<CrmGroupMemberVO> getMembers(Long objId, Integer objType) {
        List<CrmGroupMemberVO> result = new ArrayList<>();
        //获取owner
        CrmCustomer customer = CrmCustomer.dao.findById(objId);
        if (Objects.isNull(customer)) {
            throw new CrmException("客户数据不存在");
        }
        if (Objects.nonNull(customer.getOwnerUserId())) {
            result.add(crmGroupMemberService.buildMemberVo(customer.getOwnerUserId().longValue(), true, null));
        }
        result.addAll(crmGroupMemberService.getMembers(objId, objType));
        return result;
    }

    /**
     * 根据客户名称左模糊查询
     * @param realName
     * @return
     */
    public List<CrmCustomer> getByRealNameLeftLike(String realName) {
        return CrmCustomer.dao.find(Db.getSql("crm.customer.getByRealNameLeftLike"), realName);
    }

    public CrmCustomer queryById(Long customerId) {
        return CrmCustomer.dao.findFirst(Db.getSql("crm.customer.getOrganizationInformationById"), customerId);
    }

    class CallableThread implements Callable<R> {

        int i;
        List<List<Object>> read;
        List<Object> list;
        Kv kv;
        Integer repeatHandling;
        //        Integer ownerUserId;
        //		JSONObject object;
        CountDownLatch latch;
        Long userId;

        public CallableThread(int i, List<List<Object>> read, List<Object> list, Kv kv, Integer repeatHandling,
                              Integer ownerUserId, CountDownLatch latch, Long userId) {
            super();
            this.i = i;
            this.read = read;
            this.list = list;
            this.kv = kv;
            this.repeatHandling = repeatHandling;
//            this.ownerUserId = ownerUserId;
//			this.object = object;
            this.latch = latch;
            this.userId = userId;
        }

        @Override
        public R call() {

            try {

                JSONObject object = new JSONObject();
                List<Object> customerList = read.get(i);
                if (customerList.size() < list.size()) {
                    for (int j = customerList.size() - 1; j < list.size(); j++) {
                        customerList.add(null);
                    }
                }

                int number = 0;
                String customerName;
                if (customerList.get(kv.getInt("客户名称(*)")) != null) {
                    customerName = customerList.get(kv.getInt("客户名称(*)")).toString();
//                    number = Db.queryInt("select count(*) from 72crm_crm_customer where customer_name = ? ", customerName);
                    R r = checkCompanyCustomerName(customerName,null);
                    if (!r.isSuccess()){
                        number = 1;
                    }

                } else {
                    return R.error(CrmErrorInfo.CUSTOMER_IS_NOT_NULL);
                }
                /*格式化EXCEL数据操作*/
                CrmCustomer crmCustomer;
                R r = formatExcelInfo(customerList, kv);
                if (r.get("code").equals(500)) {
                    return r;
                } else {
                    crmCustomer = (CrmCustomer) r.get("entity");
                }
                /*格式化EXCEL数据操作*/
                if (0 == number) {
                    object.fluentPut("entity", new JSONObject().fluentPut("customer_name", customerName)
                            .fluentPut("registerCapital", customerList.get(kv.getInt("注册资本")))
                            .fluentPut("legalPerson", customerList.get(kv.getInt("法人")))
                            .fluentPut("registrationNumber", customerList.get(kv.getInt("工商注册号")))
                            .fluentPut("creditCode", customerList.get(kv.getInt("统一信用代码")))
                            .fluentPut("remark", customerList.get(kv.getInt("备注")))
                            .fluentPut("address", crmCustomer.getAddress())
                            .fluentPut("detail_address", customerList.get(kv.getInt("详细地址")))
                            .fluentPut("customerGrade", crmCustomer.getCustomerGrade())
                            .fluentPut("distributor", crmCustomer.getDistributor())
                            .fluentPut("customerType", crmCustomer.getCustomerType())
                            .fluentPut("ownerUserId", crmCustomer.getOwnerUserId())
                            .fluentPut("deptId", crmCustomer.getDeptId())
                            .fluentPut("isMultiple", crmCustomer.getIsMultiple())
                            .fluentPut("partner", crmCustomer.getPartner())
                            .fluentPut("storageType", crmCustomer.getStorageType())
                            .fluentPut("customer_origin", CustomerOriginEnum.CUSTOMER_NEW_KEY.getTypes())
                    );
                } else if (repeatHandling == 1) {
                    Record leads = Db.findFirst("select customer_id,batch_id from 72crm_crm_customer where customer_name = ?", customerName);
                    object.fluentPut("entity", new JSONObject().fluentPut("customer_id", leads.getInt("customer_id"))
                            .fluentPut("customerName", customerName)
                            .fluentPut("registerCapital", customerList.get(kv.getInt("注册资本")))
                            .fluentPut("legalPerson", customerList.get(kv.getInt("法人")))
                            .fluentPut("registrationNumber", customerList.get(kv.getInt("工商注册号")))
                            .fluentPut("creditCode", customerList.get(kv.getInt("统一信用代码")))
                            .fluentPut("remark", customerList.get(kv.getInt("备注")))
                            .fluentPut("address", crmCustomer.getAddress())
                            .fluentPut("detail_address", customerList.get(kv.getInt("详细地址")))
                            .fluentPut("customerGrade", crmCustomer.getCustomerGrade())
                            .fluentPut("distributor", crmCustomer.getDistributor())
                            .fluentPut("customerType", crmCustomer.getCustomerType())
                            .fluentPut("ownerUserId", crmCustomer.getOwnerUserId())
                            .fluentPut("deptId", crmCustomer.getDeptId())
                            .fluentPut("isMultiple", crmCustomer.getIsMultiple())
                            .fluentPut("partner", crmCustomer.getPartner())
                            .fluentPut("storageType", crmCustomer.getStorageType())
                            .fluentPut("customer_origin", CustomerOriginEnum.CUSTOMER_NEW_KEY.getTypes())
                            .fluentPut("batch_id", leads.getStr("batch_id")));
                } else if (repeatHandling == 2) {
                    return R.ok();
                }
                object.put(CUSTOMER_CHANGE_CHANNEL_EVENT, CrmOperateChannelEventEnum.CUSTOMER_UPLOAD_BY_EXCEL.getName());
                object.put(CUSTOMER_CHANGE_HISTORY, "客户excel导入");
                object.put("uploadExcel", "uploadExcel");

                //设置客户来源
                object.getJSONObject("entity").put("fromSource", FromSourceEnum.BY_MANUAL.getCode());

                R saveInfo = addOrUpdate(object, userId);
                if (Objects.nonNull(saveInfo)) {
                    if (!CrmConstant.SUCCESS.equals(saveInfo.get("code"))) {
                        if (saveInfo.get("msg") != null && saveInfo.get("msg").toString().endsWith("库容已满，无法新建客户")){
                            return R.error(crmCustomer.getOwnerUserName() + "员工," + saveInfo.get("msg"));
                        }

                        return R.error(saveInfo.get("msg").toString());
                    } else {

                        return saveInfo;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                throw e;
            } finally {
                latch.countDown();
            }
            return R.ok();
        }
    }

    public Record getByIdCard(String idCard) {
        return Db.findFirst(Db.getSql("crm.customer.getByIdCard"), idCard);
    }

    public Record getByRealName(String realName) {
        return Db.findFirst(Db.getSql("crm.customer.getByRealName"), realName);
    }

    public List<Record> getListByRealName(String realName) {
        return Db.find(Db.getSql("crm.customer.getByRealName"), realName);
    }

    public Record getBySiteMemberId(Long siteMemberId) {
        return Db.findFirst(Db.getSql("crm.customer.getBySiteMemberId"), siteMemberId);
    }

    public boolean deleteCrmCustomerRecord(Record customer) {
        return Db.delete("72crm_crm_customer", "customer_id", customer);
    }

    /**
     * get unique customer.
     *
     * @param custType 0－普通开发者 1-企业用户
     * @param param
     * @return
     */
    public Record getUniqueCustomer(String custType, String param) {
        if (CrmConstant.USER_TYPE_PERSONAL.equals(custType)) {
            return getByIdCard(param);
        }
        if (CrmConstant.USER_TYPE_COMPANY.equals(custType)) {
            return getByRealName(param);
        }
        return null;
    }

    public Page<Record> getDskByCustId(int page, int limit, String customerId,Set<Integer> siteMemberIdList) {
        return Db.paginate(page, limit, Db.getSqlPara("crm.customer.getDskByCustId",Kv.by("customerId",customerId).set("siteMemberIdList",siteMemberIdList)));
    }

    public R updateCustomerDeleteFlag(String custNo, int isDelete) {
        return Db.update(Db.getSql("crm.customer.updateSiteMemberDeleteFlag"), isDelete, custNo) > 0 ? R.ok() : R.error();
    }

    /**
     * 格式化客户导入EXECL信息
     *
     * @param customerList
     * @param kv
     * @return
     * @author liyue
     */
    public R formatExcelInfo(List<Object> customerList, Kv kv) {
        R r = new R();
        String province;
        String city;
        String address;
        String deptId = null;
        String userId = null;
        String isMultiple = null;
        if (!StringUtil.isNullOrEmpty((String) customerList.get(kv.getInt(CrmTagConstant.EXCEL_PROVINCE)))) {
            province = customerList.get(kv.getInt(CrmTagConstant.EXCEL_PROVINCE)).toString();
        } else {
            return R.error(CrmErrorInfo.PROVINCE_NOT_NULL);
        }
        if (!StringUtil.isNullOrEmpty((String) customerList.get(kv.getInt(CrmTagConstant.EXCEL_CITY)))) {
            city = "," + customerList.get(kv.getInt(CrmTagConstant.EXCEL_CITY));
        } else {
            return R.error(CrmErrorInfo.CITY_NOT_NULL);
        }
        if (!StringUtil.isNullOrEmpty((String) customerList.get(kv.getInt(CrmTagConstant.EXCEL_AREA)))) {
            address = province + city + "," + customerList.get(kv.getInt(CrmTagConstant.EXCEL_AREA));
        } else {
            address = province + city;
        }
        CrmCustomer crmCustomer = new CrmCustomer();

        //判断部门字段
        /*if (!StringUtil.isNullOrEmpty((String) customerList.get(kv.getInt(CrmConstant.DEPT)))) {
            Record deptRecord = Db.findFirst(Db.getSql("admin.dept.queryDeptInfoByDeptName"), customerList.get(kv.getInt(CrmConstant.DEPT)));
            if (Objects.nonNull(deptRecord)) {
                deptId = deptRecord.getStr("dept_id");
                crmCustomer.setDeptName((String)customerList.get(kv.getInt(CrmConstant.DEPT)));
            } else {
                return R.error(String.format(CrmErrorInfo.DEPT + "(%s)" + CrmErrorInfo.DEPT_NOT_NULL, customerList.get(kv.getInt(CrmConstant.DEPT))));
            }
        }*/

        //判断负责人字段
        if (!StringUtil.isNullOrEmpty((String) customerList.get(kv.getInt(CrmTagConstant.EXCEL_USER)))) {
            //负责人和库类型只能同时不为空或同时为空
            String storageType = (String)customerList.get(kv.getInt(CrmConstant.STORAGE_TYPE));
            if (StringUtils.isBlank(storageType)){
                return R.error(CrmErrorInfo.OWNER_NOT_NULL_STORAGE_NULL);
            }
            crmCustomer.setStorageType(CustomerStorageTypeEnum.getCodeByName(storageType));
            if (crmCustomer.getStorageType() == null){
                return R.error(CrmErrorInfo.STORAGE_VALIDATE);
            }

            Record userRecord = Db.findFirst(Db.getSql("admin.user.getUserInfoByRealName"), customerList.get(kv.getInt(CrmTagConstant.EXCEL_USER)));
            if (Objects.isNull(userRecord)) {
                return R.error(String.format(CrmErrorInfo.OWNER_USER_NAME + "(%s)" + CrmErrorInfo.OWNER_USER_NAME_NOT_NULL, customerList.get(kv.getInt(CrmTagConstant.EXCEL_USER))));
            } else {
                userId = userRecord.getStr("user_id");
                crmCustomer.setOwnerUserName((String) customerList.get(kv.getInt(CrmTagConstant.EXCEL_USER)));
            }
        }

        if (!StringUtil.isNullOrEmpty((String) customerList.get(kv.getInt(CrmConstant.IS_MULTIPLE)))) {
            if (CrmConstant.NO.equals(customerList.get(kv.getInt(CrmConstant.IS_MULTIPLE)))) {
                isMultiple = CrmConstant.USER_TYPE_PERSONAL;
            }
            if (CrmConstant.YES.equals(customerList.get(kv.getInt(CrmConstant.IS_MULTIPLE)))) {
                isMultiple = CrmConstant.USER_TYPE_COMPANY;
            }
        } else {
            isMultiple = CrmConstant.USER_TYPE_PERSONAL;
        }

        if (StringUtil.isNullOrEmpty((String) customerList.get(kv.getInt(CrmTagConstant.EXCEL_CUSTOMER_GRADE)))) {
            return R.error(CrmErrorInfo.CUSTOMER_GRADE_NOT_NULL);
        }
        if (StringUtil.isNullOrEmpty((String) customerList.get(kv.getInt(CrmTagConstant.EXCEL_CUSTOMER_TYPE)))) {
            return R.error(CrmErrorInfo.CUSTOMER_TYPE_NOT_NULL);
        }
        // 格式化客户等级
        String customerGrade = adminDataDicService.formatTagValueName(CrmTagConstant.CUSTOMER_GRADE, customerList.get(kv.getInt(CrmTagConstant.EXCEL_CUSTOMER_GRADE)).toString());
        if (StringUtil.isNullOrEmpty(customerGrade)) {
            return R.error(CrmErrorInfo.CUSTOMER_GRADE_NOT_EXIST);
        }
        crmCustomer.setCustomerGrade(customerGrade);

        // 格式化分销等级
        if (Objects.nonNull(customerList.get(kv.getInt(CrmTagConstant.EXCEL_DISTRIBUTOR)))) {
            String getDistributor = adminDataDicService.formatTagValueName(CrmTagConstant.DISTRIBUTOR, customerList.get(kv.getInt(CrmTagConstant.EXCEL_DISTRIBUTOR)).toString());
            if (StringUtil.isNullOrEmpty(getDistributor)) {
                return R.error(CrmErrorInfo.DISTRIBUTOR_NOT_EXIST);
            }
            crmCustomer.setDistributor(getDistributor);
        }

        // 格式化客户类型
        String customerType = adminDataDicService.formatTagValueName(CrmTagConstant.CUSTOMER_TYPE, customerList.get(kv.getInt(CrmTagConstant.EXCEL_CUSTOMER_TYPE)).toString());
        if (StringUtil.isNullOrEmpty(customerType)) {
            return R.error(CrmErrorInfo.CUSTOMER_TYPE_NOT_EXIST);
        }
        if ("个人客户".equals(customerList.get(kv.getInt(CrmTagConstant.EXCEL_CUSTOMER_TYPE)).toString())) {
            return R.error(CrmErrorInfo.CUSTOMER_TYPE_NOT_PERSONAL);
        }
        crmCustomer.setCustomerType(customerType);

        // 格式化生态伙伴
        if (Objects.nonNull(customerList.get(kv.getInt(CrmTagConstant.EXCEL_PARTNER)))) {
            String partner = adminDataDicService.formatTagValueName(CrmTagConstant.PARTNER, customerList.get(kv.getInt(CrmTagConstant.EXCEL_PARTNER)).toString());
            if (StringUtil.isNullOrEmpty(partner)) {
                return R.error(CrmErrorInfo.PARTNER_NOT_EXIST);
            }
            crmCustomer.setPartner(partner);
        }
        crmCustomer.setAddress(address);

        /*if (StringUtils.isNotEmpty(deptId)) {
            crmCustomer.setDeptId(Integer.valueOf(deptId));
        }*/

        if (StringUtils.isNotEmpty(userId)) {
            crmCustomer.setOwnerUserId(Integer.valueOf(userId));
            crmCustomer.setDeptId(null);//同时填写负责人+负责部门时，以负责人为准
        } else {
            crmCustomer.setOwnerUserId(null);
        }
        if (isMultiple != null) {
            crmCustomer.setIsMultiple(Integer.valueOf(isMultiple));
        }
        r.put("entity", crmCustomer);
        return r;
    }

    /**
     * 获取客户详情tab
     *
     * @param ownerUserId
     * @param deptId
     * @param siteMemberId
     * @param sceneCode
     * @return
     * @author liyue
     */
    public List<Record> queryTabs(String ownerUserId, String deptId, String loginUserId, Integer siteMemberId,String sceneCode) {
        List<Record> results;
        List<Long> userIdList = Db.query(Db.getSql("admin.role.queryUserIdsByRole"), CrmConstant.WEB_SITE);
        boolean hasWebSiteRole = false;
        //查询客户上游分销商信息
        boolean hasParentCustomer = false;
        if (Objects.nonNull(siteMemberId)) {
            Record parentCustomerInfo = distributorPromotionRelationService.queryParentCustomerInfoByMemberId(Long.valueOf(siteMemberId));
            if (Objects.nonNull(parentCustomerInfo) && StringUtils.isNotBlank(parentCustomerInfo.getStr("pCustomerId"))) {
                hasParentCustomer = true;
            }
        }
        if (CollectionUtils.isNotEmpty(userIdList)) {
            hasWebSiteRole = userIdList.stream().anyMatch(p -> Objects.equals(p.toString(), loginUserId));
        }
        if (hasWebSiteRole) {
            results = filterTabsForNonDistributor(adminDataDicService.queryDataDicList(CrmConstant.ALL_TABS), siteMemberId);
        } else {
            //如果客户没有负责人且没有部门，且没有上游分销商，只展示基础tab
            if (StringUtils.isEmpty(ownerUserId) && StringUtils.isEmpty(deptId) && !hasParentCustomer) {
                results = adminDataDicService.queryDataDicList(CrmConstant.WEBSITE_TABS);
            } else {
                results = filterTabsForNonDistributor(adminDataDicService.queryDataDicList(CrmConstant.ALL_TABS), siteMemberId);
            }
        }

        //如果穿了场景id，且为电销场景,并且没有上游分销商，则只展示“基本信息”、“操作日志”
        if (StringUtils.isNotBlank(sceneCode)) {
            CrmCustomerSceneEnum crmCustomerSceneEnum = CrmCustomerSceneEnum.findByCode(sceneCode);
            if (Objects.isNull(crmCustomerSceneEnum)) {
                throw new CrmException("未知场景编码");
            }
            if (CrmCustomerSceneEnum.MOBILE_SALE_CUSTOMER == crmCustomerSceneEnum && !hasParentCustomer) {
                results = results.stream().filter(item -> CrmAllTabsEnum.BASICINFO.getName().equals(item.getStr("name"))
                        || CrmAllTabsEnum.RELATIVE_HANDLE.getName().equals(item.getStr("name"))
                ).collect(Collectors.toList());
            }
        }

        return results;
    }

    /**
     * 过滤分销商lab
     *
     * @param target
     * @param siteMemberId
     * @return
     */
    private List<Record> filterTabsForNonDistributor(List<Record> target, Integer siteMemberId) {
        if (CollectionUtils.isEmpty(target)) {
            return target;
        }

        if (Objects.isNull(siteMemberId)) {
            return target.stream().filter(item -> !getDistributorLabs().contains(item.getStr("name"))).collect(Collectors.toList());
        }
        Record record = crmSiteMemberService.getSiteMemberAllFieldBySiteMemberId(Long.valueOf(siteMemberId));
        if (Objects.nonNull(record)) {
            CrmSiteMember siteMember = new CrmSiteMember()._setAttrs(record.getColumns());
            if (Objects.isNull(siteMember.getIsDistributor()) || (Objects.nonNull(siteMember.getIsDistributor())
                    && !Objects.equals(CrmConstant.IS_DISTRIBUTOR, siteMember.getIsDistributor()))) {
                return target.stream().filter(item -> !getDistributorLabs().contains(item.getStr("name"))).collect(Collectors.toList());
            }
        }
        return target;
    }

    private List<String> getDistributorLabs() {
        List<String> list = Lists.newArrayList();
        list.add(CrmConstant.DISTRIBUTOR_TAB_NAME);
        list.add(CrmConstant.DISTRIBUTOR_PROMOTION_TAB_NAME);
        return list;
    }

    /**
     * 客户放入部门客户池、网站客户池
     *
     * @author liyue
     */
    @Before(Tx.class)
    public R putDeptPoolOrPublicPoolByIds(String tagName, Integer tagId, String ids, String type) {
        logger.info("putDeptPoolOrPublicPoolByIds方法json {}", ids);
        StringBuffer sql = new StringBuffer();
        List<Long> idsList = Arrays.stream(ids.split(",")).map(s -> Long.valueOf(s.trim())).collect(Collectors.toList());
        /*放入部门客户池*/
        //如果客户负责人变更，记录变更日志
        if (CollectionUtils.isEmpty(idsList)) {
            return R.error(CrmErrorInfo.CUSTOMER_IS_NULL);
        }
        List<CrmCustomer> crmCustomers = Lists.newArrayList();
        for (Long customerId : idsList) {
            CrmCustomer oldCrmCustomer = CrmCustomer.dao.findById(customerId);
            if (Objects.nonNull(oldCrmCustomer)) {
                //检查客户释放是否符合部门锁库规则
                R r = checkReleaseRule(Math.toIntExact(customerId));
                if (!r.isSuccess()){
                    return r;
                }

                crmCustomers.add(oldCrmCustomer);
            }else {
                return R.error(CrmErrorInfo.CUSTOMER_IS_NULL);
            }
        }

        Long userId = BaseUtil.getUserId();
        Integer deptId = BaseUtil.getUser().getDeptId();
        //放入部门客户池
        if (type.equals(CrmConstant.ONE_FLAG)) {
            crmRecordService.addPutIntoTheOpenSeaRecord(idsList, CrmEnum.CUSTOMER_TYPE_KEY.getTypes(), CrmConstant.DEPT_POOL);

            if (deptId != null){
                JSONObject capacityJson = checkDeptCapacity(Long.valueOf(deptId), 1);
                if (!capacityJson.getBoolean("result")){
                    return R.error("您的部门周转库库容已满，客户无法放入部门客户池");
                } else {
                    //如果可以领取，但是部门是空的，代表应使用当前部门的事业部, 否则使用返回的dept_id
                    if (capacityJson.getInteger("deptId") == null){
                        deptId = Integer.valueOf(adminDeptService.getBusinessDepartmentByDeptId(deptId.toString()));
                    }else{
                        deptId = capacityJson.getInteger("deptId");
                    }


                }
            }else{
                return R.error("登陆用户归属部门为空，请联系管理员");
            }

            sql = new StringBuffer("update 72crm_crm_customer SET owner_user_id = null,dept_id = ").append(deptId).append("  where customer_id in (").append(ids).append(')');
            logger.info("放入部门客户池方法sql {}", sql);
        }else if (type.equals(CrmConstant.TWO_FLAG)) {
            //放入网站客户池

            crmRecordService.addPutIntoTheOpenSeaRecord(idsList, CrmEnum.CUSTOMER_TYPE_KEY.getTypes(), CrmConstant.PUBLIC_POOL + "，原因：" + tagName);
            sql = new StringBuffer("update 72crm_crm_customer SET owner_user_id = null,dept_id = null  where customer_id in (");
            sql.append(ids).append(')');
            logger.info("放入网站客户池方法sql {}", sql);

            //放入网站客户池原因
            pullCustomerPublicPool(tagName, tagId, idsList);
        }

        //没有负责人的情况，清空库容类型和团队成员
        crmCustomerSceneService.clearStorageTypeAndGroupMember(idsList);

        //记录操作日志
        CrmOwnerRecord crmOwnerRecord = new CrmOwnerRecord();
        for (CrmCustomer customer : crmCustomers) {
            crmOwnerRecord.clear();
            crmOwnerRecord.setTypeId(customer.getCustomerId().intValue());
            crmOwnerRecord.setType(8);
            crmOwnerRecord.setPreOwnerUserId(customer.getOwnerUserId());
            crmOwnerRecord.setCreateTime(DateUtil.date());
            crmOwnerRecord.save();

            //记录客户变更日志
            if (Objects.equals(type, CrmConstant.ONE_FLAG)) {
                crmChangeLogService.saveCustomerChangeLog(CrmCustomerChangeLogEnum.DEPT.getCode(), customer.getCustomerId(), null, Long.valueOf(deptId), userId);
            }else if (Objects.equals(type, CrmConstant.TWO_FLAG)) {
                crmChangeLogService.saveCustomerChangeLog(CrmCustomerChangeLogEnum.OPEN_SEA.getCode(), customer.getCustomerId(), null, null, userId);
            }
        }

        return Db.update(sql.toString()) > 0 ? R.ok() : R.error();
    }

    /**
     * 根据客户ID获取客户场景
     *
     * @param customerId
     * @return
     * @author liyue
     */
    public Record getSemById(String customerId, OssPrivateFileUtil ossPrivateFileUtil) {
        Record record = queryById(Integer.valueOf(customerId), ossPrivateFileUtil);
        if (Objects.isNull(record)) {
            return new Record();
        }
        String pCustomerId = record.getStr("pCustomerId");
        Record recordResult = new Record();
        Integer ownerUserId = record.getInt("owner_user_id");
        Integer deptId = record.getInt("dept_id");
        //无负责人无部门，如果有上游分销商，则在分销商客户池，否则在网站客户池
        if (ownerUserId == null && deptId == null) {
            if (StringUtils.isNotBlank(pCustomerId)) {
                recordResult.set("name", AdminEnum.CUSTOMER_DISTRIBUTOR_KEY.getName());
                recordResult.set("value", AdminEnum.CUSTOMER_DISTRIBUTOR_KEY.getTypes());
                return recordResult;
            } else {
                recordResult.set("name", AdminEnum.CUSTOMER_PUBLIC_KEY.getName());
                recordResult.set("value", AdminEnum.CUSTOMER_PUBLIC_KEY.getTypes());
                return recordResult;
            }
        } else if (Objects.nonNull(ownerUserId) && Objects.isNull(deptId)) {
            recordResult.set("name", AdminEnum.CUSTOMER_OWN_KEY.getName());
            recordResult.set("value", AdminEnum.CUSTOMER_OWN_KEY.getTypes());
            return recordResult;
        } else {
            recordResult.set("name", AdminEnum.CUSTOMER_DEPT_KEY.getName());
            recordResult.set("value", AdminEnum.CUSTOMER_DEPT_KEY.getTypes());
            return recordResult;
        }
    }

    public boolean updateCustomerOrgImgUrlById(String custId, String orgImgUrl) {
        int result = Db.update(Db.getSql("crm.customer.updateCustomerOrgImgUrlById"), orgImgUrl, custId);
        return result > 0;
    }

    /**
     * 根据客户id获取组织结构信息
     *
     * @param customerId
     * @return
     */
    public Record getOrganizationInformation(String customerId, OssPrivateFileUtil ossPrivateFileUtil) {
        if (customerId == null || customerId.isEmpty()) {
            logger.info("getOrganizationInformation customerId is {}",customerId);
            return new Record();
        }
        Record record = Db.findFirst(Db.getSql("crm.customer.getOrganizationInformationById"), customerId);
        if (record != null) {
            String url = record.getStr("organization_img_url");
            logger.info("getOrganizationInformation organization_img_url is {}" ,url);
            if (url != null && !url.isEmpty()) {
                Record result = new Record();
                result.set("url", ossPrivateFileUtil.presignedURL(url));
                return result;
            }
        }
        return new Record();
    }

    /**
     * 客户转移
     *
     * @author yue.li
     */
    public R transfer(CrmCustomer crmCustomer) {
        if (Objects.isNull(crmCustomer) || StringUtils.isBlank(crmCustomer.getCustomerIds())) {
            return R.error("请选择需要分派的客户");
        }
        logger.info("transfer方法json {}", crmCustomer.toJson());

        //判断负责人库容
        String[] customerIdsArr = crmCustomer.getCustomerIds().split(",");
        if (crmCustomer.getNewOwnerUserId() != null){
            boolean checkUserCapacity = checkUserCapacity(Long.valueOf(crmCustomer.getNewOwnerUserId()), CustomerStorageTypeEnum.INSPECT_CAP.getCode(), customerIdsArr.length);
            if (!checkUserCapacity){
                return R.error(CustomerStorageTypeEnum.INSPECT_CAP.getName() + "库容已满，无法分派新客户");
            }
        }

        List<Integer> customerIds = Lists.newArrayList(customerIdsArr.length);
        //循环判断关联库锁库逻辑
        for (String customerId : customerIdsArr) {
            R r = checkReleaseRule(Integer.valueOf(customerId));
            if (!r.isSuccess()){
                return r;
            }
            customerIds.add(Integer.valueOf(customerId));
        }
        
        // 判断登录用户是否是侧脸测绘事业部、在线运营事业部的bd
        Long checkUserId = null;
        if (StringUtils.isNotEmpty(PROMOTION_DISTRIBUTOR_DEPTIDS)) {
        	Long currentUserId = Long.valueOf(crmCustomer.getNewOwnerUserId());
        	for (String deptId : PROMOTION_DISTRIBUTOR_DEPTIDS.split(",")) {
    			if (Long.valueOf(deptId).equals(adminUserService.getBusinessDepartmentOfUserById(currentUserId))) {
    				checkUserId = currentUserId;
    				break;
    			}
        	}
    	}
        // 客户有上游分销商，不允许分派
    	if (Objects.nonNull(checkUserId)) {

    		AdminUser adminUser = adminUserService.getAdminUserByUserId(checkUserId);
    		
            // 有上游分销商的客户不允许添加团队成员
            if (StringUtils.isNotEmpty(crmCustomer.getCustomerIds())) {

                if (CollectionUtils.isNotEmpty(this.checkParentDistributor(customerIds))) {
                	Record record = this.checkParentDistributor(customerIds).get(0);
                    return R.error("客户" + record.getStr("customerName") + "已被分销商"+record.getStr("realName")+"绑定;\r\n——不可分派给数字地信事业部和在线运营事业部的同学："
                        	+adminUser.getRealname());
                }
            }
    	}

        return Db.tx(() -> {
            for (String customerId : customerIdsArr) {
                crmCustomer.setCustomerId(Integer.valueOf(customerId).longValue());
                String changeType = crmCustomer.getChangeType();
                if (StrUtil.isNotEmpty(changeType)) {
                    String[] changeTypeArr = changeType.split(",");
                    for (String type : changeTypeArr) {
                        if ("1".equals(type)) {//更新联系人负责人
                            crmContactsService.updateOwnerUserId(crmCustomer.getCustomerId().intValue(), crmCustomer.getNewOwnerUserId());
                        }
                        if ("2".equals(type)) {//更新商机负责人
                            crmBusinessService.updateOwnerUserId(crmCustomer);
                        }
                    }
                }

                //更新客户负责人记录更新日志
                CrmCustomer oldCustomer = CrmCustomer.dao.findById(Integer.valueOf(customerId));
                Integer oldOwnerUserId = Objects.nonNull(oldCustomer) ? oldCustomer.getOwnerUserId() : null;

                crmCustomer.setCustomerId(Long.valueOf(customerId));
                crmCustomer.setOwnerUserId(crmCustomer.getNewOwnerUserId());
                crmCustomer.setOwnerTime(DateUtil.date());
                crmCustomer.setDeptId(null);
                crmCustomer.update();

                //删除owner在group表中的数据
                crmGroupMemberService.deleteMember(Long.valueOf(customerId), crmCustomer.getNewOwnerUserId().longValue(), Integer.valueOf(CrmEnum.CUSTOMER_TYPE_KEY.getTypes()), null);
                //转为团队成员
                if (CrmConstant.TRANSFER_TYPE.equals(crmCustomer.getTransferType()) && Objects.nonNull(oldOwnerUserId)) {
                    crmGroupMemberService.addMember(Long.valueOf(customerId), oldCustomer.getOwnerUserId().longValue(), Integer.valueOf(CrmEnum.CUSTOMER_TYPE_KEY.getTypes()), crmCustomer.getPower(), null);
                }

                //保存客户扩展表
                saveCrmCustomerExt(crmCustomer.getCustomerId(), CustomerStorageTypeEnum.INSPECT_CAP.getCode(),null,null);

                crmRecordService.addConversionRecord(Integer.valueOf(customerId), CrmEnum.CUSTOMER_DISTRIBUTE_KEY.getTypes(), crmCustomer.getNewOwnerUserId());

                // 如果客户分派负责人变更记录日志
                if (!Objects.equals(oldOwnerUserId, crmCustomer.getNewOwnerUserId())) {
                    //记录负责人变更日志
                    crmChangeLogService.saveCustomerChangeLog(CrmCustomerChangeLogEnum.getBdByStorageType(CustomerStorageTypeEnum.INSPECT_CAP.getCode()), Long.valueOf(customerId), Long.valueOf(crmCustomer.getNewOwnerUserId()), null, BaseUtil.getUserId());
                }
            }
            return true;
        }) ? R.ok() : R.error();
    }

    /**
     * 获取上游分销商
     * @return
     */
    public List<Record> checkParentDistributor(List<Integer> customerIds) {

        return Db.find(Db.getSqlPara("crm.customerExt.checkParentDistributor", Kv.by("customerIds", customerIds)));
    }
    /***
     * 根据siteMemberId删除客户相关信息
     * @param siteMemberId 官网用户ID
     */
    public void deleteCustomerInfoBySiteMemberId(Integer siteMemberId) {
        Record record = Db.findFirst(Db.getSql("crm.sitemember.findCustomerIdBySiteMemberId"), siteMemberId);
        String customerId = record.getStr("customer_id");
        String customerName = record.getStr("customer_name");
        Db.tx(() -> {
            /*删除客户联系小计*/
//            Db.delete(Db.getSql("crm.record.deleteRecordByCustomerId"), customerId);
            /*删除联系人联系小计*/
            //Db.delete(Db.getSql("crm.record.deleteContactsRecordByCustomerId"), customerId);
            /*删除商机联系小计*/
            // Db.delete(Db.getSql("crm.record.deleteBusinessRecordByCustomerId"), customerId);
            /*删除联系人*/
            // Db.delete(Db.getSql("crm.contact.deleteContactsByCustomerId"), customerId);
            /*删除商机*/
            //Db.delete(Db.getSql("crm.business.deleteBusinessByCustomerId"), customerId);

            /*商机备注记录客户删除*/
            /*List<Record> businessList = Db.find(Db.getSql("crm.business.selectBusinessByCustomerId"), customerId);
            if (businessList != null && businessList.size() > 0) {
                for (Record value : businessList) {
                    String remark = value.getStr("remark");
                    remark += "(该客户:" + customerName + ",已经被注销.)";
                    Db.update(Db.getSql("crm.business.updateBusinessRemarkByCustomerId"), remark, value.getStr("business_id"));
                }
            }*/
            /*商机备注记录客户删除*/

            /*记录删除客户日志*/
            CrmActionRecord crmActionRecord = new CrmActionRecord(null
                    , CrmEnum.CUSTOMER_TYPE_KEY.getTypes(), Integer.valueOf(customerId), "网站会员系统    注销网站会员账号："+ siteMemberId +"，客户解除该账号的绑定关系");
            crmRecordService.addCrmActionRecord(crmActionRecord);
            /*记录删除客户日志*/

            /*删除官网信息*/
            Db.delete(Db.getSql("crm.sitemember.deleteSiteMemberBySiteMemberId"), siteMemberId);
            /*删除客户*/
//            Db.delete(Db.getSql("crm.customer.deleteByIds"), customerId);
            return true;
        });
    }

    /***
     * 重新赋予网站客户池redis新值
     */
    public void redisReset() {
        Record redisPublicCount = Db.findFirst(Db.getSql("crm.customer.queryCountForPublicWebsite"));
        Record redisCount = Db.findFirst(Db.getSql("crm.customer.queryCountForWebsite"), CrmDateUtil.getLastWeek());
        if (redisCount != null) {
            Redis.use().setex(RedisConstant.WEBSITE_KEY, RedisConstant.SECOND, redisCount.getStr("count"));
        } else {
            Redis.use().setex(RedisConstant.WEBSITE_KEY, RedisConstant.SECOND, CrmConstant.INTEGER_ZERO);
        }
        if (redisPublicCount != null) {
            Redis.use().setex(RedisConstant.PUBLIC_WEBSITE_KEY, RedisConstant.SECOND, redisPublicCount.getStr("count"));
        } else {
            Redis.use().setex(RedisConstant.PUBLIC_WEBSITE_KEY, RedisConstant.SECOND, CrmConstant.INTEGER_ZERO);
        }
    }

    /**
     * 根据客户id获取该客户下的支付渠道列表
     *
     * @return 支付渠道列表
     */
    public List<Record> getPaymentChannelListByCustId(Integer custId) {
        return Db.find(Db.getSql("crm.customer.getPaymentChannelListByCustId"), custId);
    }

    /**
     * 根据客户id和支付渠道id删除该客户下的该支付渠道信息
     *
     * @param custId    客户id
     * @param channelId 支付渠道id
     */
    public void deletePaymentChannel(Integer custId, Integer channelId) {
        Db.delete(Db.getSql("crm.customer.deletePaymentChannel"), custId, channelId);
    }

    /**
     * 保存支付渠道信息
     *
     * @param request
     * @return
     */
    public R savePaymentChannel(CrmCustomerPaymentChannel request) {
        if (Objects.isNull(request)) {
            return R.error("请求参数非法");
        }

        if (request.getId() != null) {
            // update payment channel
            request.setGmtModified(new Date());
            request.setIsDeleted(CrmConstant.DELETE_FLAG_NO);
            return request.update() ? R.ok() : R.error("update payment channel failed");
        } else {
            request.setChannelId(IdUtil.simpleUUID());
            request.setGmtCreate(new Date());
            request.setGmtModified(new Date());
            request.setIsDeleted(CrmConstant.DELETE_FLAG_NO);
            return request.save() ? R.ok() : R.error("create payment channel failed");
        }
    }

    public Record findPaymentByTypeAndAccount(Integer type, String account) {
        if (Objects.isNull(type) || StringUtils.isEmpty(account)) {
            return null;
        }
        return Db.findFirst(Db.getSql("crm.paymentchannel.findPaymentByTypeAndAccount"), type, account);
    }

    public List<Record> getAllCustomerPaymentChannel() {
        return Db.find(Db.getSql("crm.paymentchannel.getAllCustomerPaymentChannel"));
    }

    public void batchSavePaymentChannel(List<Record> channels) {
        if (CollectionUtils.isEmpty(channels)) {
            return;
        }
        logger.info("batchSavePaymentChannel, record size: {}", channels.size());
        Db.batchSave("72crm_crm_customer_payment_channel", channels, channels.size());
    }

    public List<Record> fuzzyMatchCustomer(String param) {
        if (StringUtils.isEmpty(param)) {
            return Collections.emptyList();
        }
        return Db.find(Db.getSqlPara("crm.customer.fuzzyMatchCustomer", Kv.by("param", param)));
    }

    /**
     * 客户省市区补充
     *
     * @param customerId 客户ID
     * @param address    省市区
     * @return
     * @author yue.li
     */
    public R addProvince(String customerId, String address) {
        return Db.update(Db.getSql("crm.customer.addProvince"), address, customerId) > 0 ? R.ok() : R.error();
    }

    /**
     * Update crm customer payment channel according to the payment information
     *
     * @param map
     */
    public void updateCrmCustomerPaymentChannel(Map<BopsPayment, CrmCustomer> map) {
        if (Objects.isNull(map)) {
            return;
        }

        List<CrmCustomerPaymentChannel> channelList = Lists.newArrayList();
        for (Map.Entry<BopsPayment, CrmCustomer> set : map.entrySet()) {
            BopsPayment payment = set.getKey();
            CrmCustomer crmCustomer = set.getValue();

            int type = PaymentTypeEnum.getPaymentTypeDefinedInCrm(payment.getPayType());
            Record record = findPaymentByTypeAndAccount(type, payment.getParternAccount());
            if (Objects.isNull(record)) {
                CrmCustomerPaymentChannel channel = new CrmCustomerPaymentChannel();
                channel.setChannelId(IdUtil.simpleUUID());
                channel.setCrmCustomerId(crmCustomer.getCustomerId().intValue());
                channel.setPayType(type);
                channel.setPayName(Objects.isNull(payment.getPayName()) ? "" : payment.getPayName());
                channel.setParternAccount(payment.getParternAccount());
                channel.setGmtCreate(new Date());
                channel.setGmtModified(new Date());
                channel.setIsDeleted(CrmConstant.DELETE_FLAG_NO);
                channelList.add(channel);
            }
        }
        // 去重处理
        List<CrmCustomerPaymentChannel> distinctChannelList = channelList.stream()
        		.collect(Collectors.collectingAndThen(Collectors.toCollection(() -> new TreeSet<>(Comparator.comparing(o -> o.getPayType() + ";" + o.getParternAccount()))), ArrayList::new));

        List<Record> records = distinctChannelList.stream()
        		.map(Model::toRecord).collect(Collectors.toList());
        batchSavePaymentChannel(records);
    }

    public void sendNotifyEmailForIncome(VelocityEngine velocityEngine, NotifyService notifyService, BopsPayment payment, CrmCustomer customer) {
        if (Objects.isNull(customer) || Objects.isNull(customer.getOwnerUserId())) {
            return;
        }
        if (!Integer.valueOf(CrmPayTypeEnum.CONSUME_KEY.getTypes()).equals(payment.getCrmPayType())) {
            //只有消费类回款才发邮件
            logger.info("==sendNotifyEmailForIncome 非消费类回款无需发送邮件提醒 payment:{}==", JsonKit.toJson(payment));
            return;
        }

        List<String> toEmails = Lists.newArrayList();
        Record record = Db.findFirst(Db.getSql("admin.user.queryUserFromAdminUserByUserId"), customer.getOwnerUserId());
        if (Objects.isNull(record)) {
            return;
        }

        AdminUser adminUser = new AdminUser()._setAttrs(record.getColumns());
        if (StringUtils.isEmpty(adminUser.getEmail())) {
            return;
        }
        toEmails.add(adminUser.getEmail());

        String subject = String.format(EMAIL_SUBJECT_PAYMENT, payment.getPayName());
        if ( StringUtil.isNullOrEmpty(payment.getOrderNo())) {
            subject = CrmConstant.EMAIL_SUBJECT_PAYMENT_NO_MASTER;
        }
        Map<String, Object> params = Maps.newHashMap();
        params.put("customerName", payment.getPayName());
        params.put("income", payment.getTotalAmount());
        params.put("payType", Objects.nonNull(payment.getPayType()) ? PaymentTypeEnum.getByCode(payment.getPayType()).getDesc() : "");
        params.put("paymentNo", payment.getPaymentNo());
        params.put("payTime", payment.getPayTime());
        params.put("payComment", payment.getComment());
        //增加判断：若该笔支付未关联订单，则在下方增加提示：该笔回款支付未关联订单，请点此提交申请财务进行异常订单关联。（链接到http://work.wz-inc.com/workflow/request/AddRequest.jsp?workflowid=481&isagent=0&beagenter=0&f_weaver_belongto_userid=）
        //crm取数判断如下：
        //（1）pay_type=2  (银行收款)
        //（2）order_no =“null"为空
        if (Integer.valueOf(2).equals(payment.getPayType()) && StringUtil.isNullOrEmpty(payment.getOrderNo())) {
        	params.put("comment", "该笔回款支付未关联订单或合同，<a href=\""+DUAL_ADDRESS+"\">请点此进行人工干预申请</a>（若有疑问可咨询财务同事）");
        } else {
        	params.put("comment","");
        }

        StringWriter result = new StringWriter();
        VelocityContext velocityContext = new VelocityContext(params);
        velocityEngine.mergeTemplate(CrmConstant.PAYMENT_NOTIFY_TEMPLATE, "UTF-8", velocityContext, result);

        notifyService.email(subject, result.toString(), toEmails);
    }

    /**
     * Get distributor statistic information for specified user.
     *
     * @param siteMemberId site member id
     * @return statistic record list
     */
    public List<DistributorStatistic> getDistributorStatisticInfo(Integer siteMemberId) {
        List<DistributorStatistic> result;
        List<Record> records = Db.find(Db.getSql("crm.customer.getDistributorStatisticInfo"), siteMemberId);
        if (CollectionUtils.isEmpty(records)) {
            return Collections.emptyList();
        } else {
            result = records.stream().map(item -> new DistributorStatistic()._setAttrs(item.getColumns())).collect(Collectors.toList());

            Record customerRecord = getBySiteMemberId(siteMemberId.longValue());
            if (Objects.nonNull(customerRecord)) {
                CrmCustomer customer = new CrmCustomer()._setAttrs(customerRecord.getColumns());
                List<Integer> custIds = Lists.newArrayList();
                custIds.add(customer.getCustomerId().intValue());
                List<DistributorBdSalesStatistic> bdSalesStatistics = getBdSalesStatisticInfoByCustomerIds(custIds);

                assembleBdSalesInfo(result, bdSalesStatistics);
            }
        }
        return result;
    }

    private void assembleBdSalesInfo(List<DistributorStatistic> result, List<DistributorBdSalesStatistic> bdSalesStatistics) {
        if (CollectionUtils.isEmpty(result)) {
            return;
        }

        boolean bdSalesEmpty = CollectionUtils.isEmpty(bdSalesStatistics);
        for (DistributorStatistic ds : result) {
            ds.setBdSalesQuantity(0);
            if (!bdSalesEmpty) {
                Optional<DistributorBdSalesStatistic> temp = bdSalesStatistics.stream().filter(item -> item.getSiteMemberId() == ds.getSiteMemberId().intValue()
                        && item.getProductCode().equals(ds.getProductCode()) && item.getGoodsCode().equals(ds.getGoodsCode()) && item.getGoodsSpec().equals(ds.getGoodsSpec())).findFirst();
                temp.ifPresent(item -> ds.setBdSalesQuantity(item.getSalesQuantityValue()));
            }
            ds.setTheoryStock();
            ds.setActualStock();
        }

    }

    /**
     * 更新销售数量
     *
     * @param crmCustomerSalesLog 销售数量实体
     * @param userId              操作人ID
     * @author yue.li
     */
    public R saveSalesLog(CrmCustomerSalesLog crmCustomerSalesLog, Integer userId) {
        logger.info("customerSalesLog service saveSalesLog jsonObject: {}", crmCustomerSalesLog);
        crmCustomerSalesLog.setLogId(IdUtil.simpleUUID());
        crmCustomerSalesLog.setOperatorId(userId);
        // 添加销售数量
        if (Objects.nonNull(crmCustomerSalesLog.getOperationType()) && crmCustomerSalesLog.getOperationType().equals(CrmConstant.ADD_OPERATION_TYPE)) {
            crmCustomerSalesLog.setBdSalesQuantityValue(crmCustomerSalesLog.getBdSalesQuantity());
        }
        // 减少销售数量
        if (Objects.nonNull(crmCustomerSalesLog.getOperationType()) && crmCustomerSalesLog.getOperationType().equals(CrmConstant.REDUCE_OPERATION_TYPE)) {
            BigDecimal salesQuantityValue = new BigDecimal(0);
            if (Objects.nonNull(crmCustomerSalesLog.getBdSalesQuantity())) {
                salesQuantityValue = salesQuantityValue.subtract(new BigDecimal(crmCustomerSalesLog.getBdSalesQuantity()));
            }
            crmCustomerSalesLog.setBdSalesQuantityValue(Long.valueOf(salesQuantityValue.toString()));
        }
        boolean isSuccess = crmCustomerSalesLog.save();
        return isSuccess ? R.ok() : R.error();
    }

    /**
     * 根据客户ID查询销售数量
     *
     * @param customerIds 客户ids
     * @author yue.li
     */
    public List<DistributorBdSalesStatistic> getBdSalesStatisticInfoByCustomerIds(List<Integer> customerIds) {
        if (CollectionUtils.isEmpty(customerIds)) {
            return Collections.emptyList();
        }

        // get customer id and site member id from db
        List<Record> customerIdSiteMemberIdRecord = Db.find(Db.getSqlPara("crm.customer.getSiteMemberIdCustomerId", Kv.by("customerIds", customerIds)));
        List<CustomerIdSiteMemberIdRecord> idRecords = customerIdSiteMemberIdRecord.stream().map(item -> JSON.parseObject(item.toJson(), CustomerIdSiteMemberIdRecord.class)).collect(Collectors.toList());
        Map<String, String> idMap = Maps.newHashMap();
        idRecords.forEach(item -> idMap.put(item.getCrmCustomerId(), item.getSiteMemberId()));

        // get bd sales statistic information
        List<Record> records = Db.find(Db.getSqlPara("crm.customer.getBdSalesStatisticInfoByCustomerIds", Kv.by("customerIds", customerIds)));
        if (CollectionUtils.isEmpty(records)) {
            return Collections.emptyList();
        }

        // set site member id to bd sales statistic object
        List<DistributorBdSalesStatistic> results = records.stream().map(item -> JSON.parseObject(item.toJson(), DistributorBdSalesStatistic.class)).collect(Collectors.toList());
        results.forEach(item -> item.setSiteMemberId(StringUtils.isEmpty(idMap.get(item.getCrmCustomerId())) ? null : Integer.valueOf(idMap.get(item.getCrmCustomerId()))));
        return results;
    }

    /**
     * 获取更新销售数量详情信息
     *
     * @param crmCustomerId 客户id
     * @param productCode   产品code
     * @param goodsCode     商品code
     * @param goodsSpec     规格
     * @return
     * @author yue.li
     */
    public List<Record> getSalesLog(String crmCustomerId, String productCode, String goodsCode, String goodsSpec) {
        List<Record> record = Db.find(Db.getSqlPara("crm.customer.getSalesLog", Kv.by("crmCustomerId", crmCustomerId).set("productCode", productCode).set("goodsCode", goodsCode).set("goodsSpec", goodsSpec)));
        // 减少原因转码
        record.forEach(r -> {
            if (Objects.nonNull(r.getInt("reduceReasons"))) {
                r.set("reduceReasonsName", CrmReduceReasonsEnum.getName(r.getInt("reduceReasons")));
            }
        });
        return record;
    }

    /**
     * update customer address by sign in address id
     *
     * @param addressId sign in address id
     * @return R
     */
    public R updateCustomerAddressBySigninAddress(String addressId) {
        return Db.update(Db.getSql("crm.customer.updateCustomerAddressBySigninAddress"), addressId) > 0 ? R.ok() : R.error();
    }

    /**
     * 客户领取(此方法已经过期)
     *
     * @param customerId  客户id
     * @param ownerUserId 负责人id
     * @author yue.li
     */
    @Before(Tx.class)
    @Deprecated
    public R receive(Long customerId, Integer ownerUserId) {
        CrmCustomer crmCustomer = CrmCustomer.dao.findById(customerId);
        if (Objects.isNull(crmCustomer)) {
            logger.info("customerId is not exist");
            return R.error(CrmErrorInfo.CUSTOMER_IS_NULL);
        }
        return Db.tx(() -> {
            // 更新负责人
            Integer oldOwnerUserId = crmCustomer.getOwnerUserId();
            Db.update(Db.getSql("crm.customer.receive"), ownerUserId, customerId);
            // 添加操作日志
            crmRecordService.addDistributionRecord(Objects.nonNull(customerId) ? String.valueOf(customerId) : "", CrmEnum.CUSTOMER_TYPE_KEY.getTypes(), null);
            if(!Objects.equals(oldOwnerUserId,ownerUserId)){
                //记录客户负责人变更记录日志
                crmChangeLogService.saveCustomerChangeLog(CrmCustomerChangeLogEnum.getBdByStorageType(crmCustomer.getStorageType()), customerId, Long.valueOf(ownerUserId), null, BaseUtil.getUserId());
            }

            return true;
        }) ? R.ok() : R.error();
    }

    /**
     * OA网站池领取
     *
     * @param customerId  客户id
     * @param ownerUserId 负责人id
     * @author yue.li
     */
    @Before(Tx.class)
    public R receiveCustomerForOa(Long customerId, Integer ownerUserId) {
        return Db.tx(() -> {
            // 更新负责人
            Db.update(Db.getSql("crm.customer.receive"), ownerUserId, customerId);
            // 更新联系人负责人
            crmContactsService.updateOwnerUserId(customerId.intValue(), ownerUserId);
            // 更新商机负责人
            CrmCustomer crmCustomer = new CrmCustomer();
            crmCustomer.setNewOwnerUserId(ownerUserId);
            crmCustomer.setCustomerIds(String.valueOf(customerId));
            crmCustomer.setTransferType(CrmConstant.TRANSFER_TYPE);
            crmCustomer.setPower(CrmConstant.POWER_TWO);
            crmBusinessService.updateOwnerUserId(crmCustomer);
            // 添加操作日志
            crmRecordService.addDistributionRecord(String.valueOf(customerId), CrmEnum.CUSTOMER_DISTRIBUTE_KEY.getTypes(), null);
            return true;
        }) ? R.ok() : R.error();
    }

    /**
     * 客户领取，走OA审批
     * @param customerId
     * @param reason
     * @param industryCode
     * @param esbConfig
     * @return
     */
    @Before(Tx.class)
    public R applyCrmWorkFlow(Long customerId, String reason, String industryCode, EsbConfig esbConfig) {
        try {
            logger.info("applyCrmWorkFlow method param - [customerId:{} ,reason:{},industryCode:{}]", customerId, reason, industryCode);
            if (Objects.isNull(customerId) || StringUtils.isEmpty(reason) || StringUtils.isEmpty(industryCode)) {
                return R.error("applyCrmWorkFlow param is null");
            } else {
                CrmCustomer crmCustomer = constructCustomer(customerId, null, reason, industryCode);
                if (Objects.nonNull(crmCustomer)) {
                    //查看缓存中的业绩数据
                    Long userId = BaseUtil.getUserId();
                    CrmPerformanceDto crmPerformanceDto = redisCache.get(CrmConstant.PERFORMANCE_USER_CUSTOMER + userId + "_" + customerId);
                    if (crmPerformanceDto == null){
                        crmPerformanceDto = getCustomerPerformanceMsg(customerId, userId, esbConfig,null);
                    }

                    crmCustomer.setApplicantPerformance(crmPerformanceDto.getBdPerformance());
                    crmCustomer.setOrderPaymentAmount(crmPerformanceDto.getOrderAmount());

                    String crmJson = JSON.toJSONString(constructCrmWorkFlowInfo(crmCustomer));
                    logger.info("applyCrmWorkFlow request :{}", crmJson);
                    String result = HttpUtil.post(esbConfig.getCrmWorkFlowUrl(), crmJson, esbConfig.getCrmWorkflowHeader());
                    logger.info("applyCrmWorkFlow response :{}", result);
                    if (StringUtils.isBlank(result)){
                        return R.error("OA系统返回异常");
                    }
                    CrmWorkflowResponse crmWorkflowResponse = JSON.parseObject(result, CrmWorkflowResponse.class);
                    if (!CrmConstant.SUCCESS_CODE.equals(crmWorkflowResponse.getCode())) {
                        return R.error(crmWorkflowResponse.getMsg());
                    }

                    //保存用户扩展表，代表客户领取中,锁定客户
                    saveCrmCustomerExt(customerId, null,1,null);
                } else {
                    return R.error(CrmErrorInfo.CUSTOMER_IS_NULL);
                }
            }
        } catch (Exception e) {
            logger.error("applyCrmWorkFlow exception: {}", BaseUtil.getExceptionStack(e));
            return R.error("OA系统返回异常");
        }

        return R.ok();
    }

    /**
     * 封装发送OA客户领取实体
     *
     * @param customer 客户对象
     * @author yue.li
     */
    public CrmWorkFlowInfo constructCrmWorkFlowInfo(CrmCustomer customer) {
        CrmWorkFlowInfo crmWorkFlowInfo = new CrmWorkFlowInfo();
        CrmWorkFlowData crmWorkFlowData = new CrmWorkFlowData();
        Record record = Db.findFirst(Db.getSql("crm.leads.adminConfigInfoByName"), CrmConstant.CRM_WORK_FLOW);
        crmWorkFlowData.setMain(customer);
        crmWorkFlowInfo.setData(crmWorkFlowData);
        crmWorkFlowInfo.setType(record.get("value"));
        return crmWorkFlowInfo;
    }

    /**
     * 构造客户信息
     *
     * @param customerId   客户ID
     * @param customerName 客户名称
     * @param reason       申请原因
     * @param industryCode 行业code
     * @author yue.li
     */
    public CrmCustomer constructCustomer(Long customerId, String customerName, String reason, String industryCode) throws Exception {
        CrmCustomer customer = null;
        logger.info("getCustomerInfoByCustomerName开始执行");
        if (StringUtils.isNotEmpty(customerName)) {
            customerName = URLDecoder.decode(customerName, StandardCharsets.UTF_8.name());
            logger.info("customerName {}", customerName);
        }
        List<Record> recordList = Db.find(Db.getSqlPara("crm.customer.queryCustomerInfoForOA", Kv.by("customer_name", customerName).set("customer_id", customerId)));
        List<CrmCustomer> customerList = recordList.stream().map(item -> new CrmCustomer()._setOrPut(item.getColumns())).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(customerList)) {
            customer = formatCustomerInfo(customerList.get(0).setIndustryCode(industryCode));
            customer.setReason(reason);
        }
        logger.info("getCustomerInfoByCustomerName结束执行json {}", Objects.nonNull(customer) ? customer.toJson() : null);
        return customer;
    }

    /**
     * 格式化客户信息
     *
     * @author yue.li
     */
    public CrmCustomer formatCustomerInfo(CrmCustomer customer) {
        String customerOrigin = customer.getCustomerOrigin();
        List<String> crmPayTypes = new ArrayList<>();
        crmPayTypes.add(CrmPayTypeEnum.CONSUME_KEY.getTypes());
        crmPayTypes.add(CrmPayTypeEnum.REFUND_KEY.getTypes());
        // 是否分销商
        List<Record> distributorList = Db.find(Db.getSql("crm.customer.isDistributorByCustomerNo"), customer.getCustomerNo(), CrmConstant.IS_DISTRIBUTOR);
        if (CollectionUtils.isNotEmpty(distributorList)) {
            customer.setCustomerType(CrmDistributorEnum.IS_DISTRIBUTOR.getName());
        }
        // 客户来源
        if (CustomerOriginEnum.CUSTOMER_NEW_KEY.getTypes().equals(customerOrigin)) {
            customer.setCustomerOrigin(CustomerOriginEnum.CUSTOMER_NEW_KEY.getName());
        } else if (CustomerOriginEnum.LEADS_TRANSFORM_KEY.getTypes().equals(customerOrigin)) {
            customer.setCustomerOrigin(CustomerOriginEnum.LEADS_TRANSFORM_KEY.getName());
        } else if (CustomerOriginEnum.WEB_SITE_ORIGIN_KEY.getTypes().equals(customerOrigin)) {
            customer.setCustomerOrigin(CustomerOriginEnum.WEB_SITE_ORIGIN_KEY.getName());
        } else {
            customer.setCustomerOrigin(null);
        }
        // 申请人
        AdminUser user = BaseUtil.getUser();
        if(user != null){
            customer.setApplyUserLoginId(user.getUsername());
        }
        // 负责人
        Record ownerUserRecord = Db.findFirst(Db.getSql("admin.user.queryUserFromAdminUserByUserId"), customer.getOwnerUserId());
        if (Objects.nonNull(ownerUserRecord)) {
            AdminUser adminUser = new AdminUser()._setAttrs(ownerUserRecord.getColumns());
            customer.setOwnerUserLoginId(adminUser.getUsername());
        }
        // 客户创建时间
        customer.setCreateTimeView(CrmDateUtil.formatDate(customer.getCreateTime()));
        // 官网用户ID
        Record siteMemberRecord = crmSiteMemberService.findByCustId(customer.getCustomerNo());
        if (Objects.nonNull(siteMemberRecord)) {
            CrmSiteMember crmSiteMember = new CrmSiteMember()._setAttrs(siteMemberRecord.getColumns());
            customer.setUserId(crmSiteMember.getSiteMemberId());
        }
        // 订单累计收入
        Record record = Db.findFirst(Db.getSqlPara("crm.customer.getTotalBopsPaymentByCustomerId", Kv.by("customer_id", customer.getCustomerId()).set("crmPayTypes", crmPayTypes)));
        customer.setTotalAmount(record.getStr("totalAmount"));

        // 行业
        customer.setIndustryName(adminDataDicService.formatTagValueId(CrmTagConstant.INDUSTRY, customer.getIndustryCode()));
        return customer;
    }

    public R checkCustomerNeedCustomerReceive(String customerName, String mobile) {
        List<Record> recordList = Db.find(Db.getSqlPara("crm.customer.getCustomerBaseList", Kv.by("customerName", customerName).set("mobile", mobile)));
        if (CollectionUtils.isNotEmpty(recordList)) {
            List<CrmCustomer> customerList = recordList.stream().map(item -> new CrmCustomer()._setOrPut(item.getColumns())).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(customerList)) {
                CrmCustomer crmCustomer = customerList.get(0);
                return R.ok().put("data", crmCustomer);
            }
        }
        return R.ok();
    }

    /**
     * 检查企业客户是否重名
     * @param customerName 客户名称
     * @param customerId 客户ID
     */
    public R checkCompanyCustomerName(String customerName, Integer customerId) {
        if (StringUtils.isNotBlank(customerName)) {
            String customerType = adminDataDicService.formatTagValueName(CrmTagConstant.CUSTOMER_TYPE, CrmConstant.BUSINESS_CLIENTS);

            //编辑的时候，客户id不为空
            if (customerId != null){
                CrmCustomer crmCustomer = CrmCustomer.dao.findById(customerId);
                if (crmCustomer == null){
                    throw new CrmException("customerId：" + customerId + "，对应 客户不存在");
                }

                //不是企业客户，编辑时不判断重名
                if (!crmCustomer.getCustomerType().equals(customerType)){
                    return R.ok();
                }
            }

            //1.精准姓名判断
            Record record = Db.findFirst(Db.getSqlPara("crm.customer.queryRepeatCustomerInfo", Kv.by("customer_name", customerName).set("customer_type", customerType).set("customer_id", customerId)));
            if (record != null) {
                return R.error("此名称已经存在").put("data", record);
            }

            //2.客户更新的时候，判断是否企业用户，企业用户才需要判断重名
            if (customerId != null) {
                return R.ok();
                /*CrmCustomer customer = CrmCustomer.dao.findById(customerId);
                if (customer == null || !Objects.equals(customerType, customer.getCustomerType())) {
                    return R.ok();
                }
                //3.ext表查看原始客户姓名,相同则不需要走下面去除括号等模糊查询
                CrmCustomerExt crmCustomerExt = CrmCustomerExt.dao.findFirst(Db.getSqlPara("crm.customerExt.getCustomerExtByCustomerId",Kv.by("customerId",customerId)));
                if (crmCustomerExt != null && customerName.equals(crmCustomerExt.getOriginalCustomerName())){
                    return R.ok();
                }*/
            }

            //4.根据规则，首先去除空格和数字
            Matcher matcher = PW.matcher(customerName);
            customerName = matcher.replaceAll("").trim();
            record = Db.findFirst(Db.getSqlPara("crm.customer.queryRepeatCustomerInfo", Kv.by("customerNamelike", customerName).set("customer_id", customerId).set("customer_type", customerType).set("repeatList", CrmRepeatNameEnum.getAll())));
            if (record != null) {
                return R.error("此名称已经存在").put("data", record);
            }

            //5.根据规则，去除姓名中的关键字，然后进行模糊查询
            customerName = filterRuleCustomerName(customerName);
            record = Db.findFirst(Db.getSqlPara("crm.customer.queryRepeatCustomerInfo", Kv.by("customerNamelike", customerName).set("customer_id", customerId).set("customer_type", customerType).set("repeatList", CrmRepeatNameEnum.getAll())));
            if (record != null) {
                return R.error("此名称已经存在").put("data", record);
            } else {
                return R.ok();
            }
        } else {
            return R.ok();
        }
    }

    /**
     * 过滤符合规则客户名称
     * @author yue.li
     * @param customerName 客户名称
     */
    private String filterRuleCustomerName(String customerName) {
        //去除空格
        customerName = customerName.replace(" ", "");
        //去除数字、括号
        Matcher matcher = P.matcher(customerName);
        customerName = matcher.replaceAll("");
        //去除 最后的文字“公司、有限公司、有限责任公司”
        if (customerName.endsWith(CrmConstant.YOU_XIAN_ZE_REN_GONG_SI)) {
            customerName = customerName.substring(0, customerName.length() - 6);
        } else if (customerName.endsWith(CrmConstant.YOU_XIAN_GONG_SI)) {
            customerName = customerName.substring(0, customerName.length() - 4);
        } else if (customerName.endsWith(CrmConstant.GONG_SI)) {
            customerName = customerName.substring(0, customerName.length() - 2);
        }
        return customerName;
    }

    /**
     * 获取客户列表(移动端兼容后期PC)
     *
     * @param customerRequest 请求参数对象
     * @param crmUser         客户对象
     * @author yue.li
     */
    public Page<Record> getCustomerList(BasePageRequest<CrmCustomerPageRequest> customerRequest, CrmUser crmUser) {
        CrmCustomerPageRequest request = customerRequest.getData();

        List<Integer> authorizedUserIds = SceneUtil.getAuthorizedUserIdsForBizScene(request.getBizType(), request.getSceneId(), crmUser);
        if (CollectionUtils.isEmpty(authorizedUserIds)) {
            return new Page<>();
        }

        logger.info("crmUser authorizedUserIds: {}", crmUser.getAuthorizedUserIds());
        logger.info("mobile getCustomerList  authorizedUserIds {}", authorizedUserIds);

        SqlPara sqlPara = prepareSqlParaCustomerForList(request, authorizedUserIds, crmUser);
        return Db.paginate(customerRequest.getPage(), customerRequest.getLimit(), sqlPara);
    }

    /**
     * 封装客户查询SQL
     *
     * @param request           请求参数对象
     * @param authorizedUserIds 权限用户集合
     * @param crmUser           客户对象
     * @author yue.li
     */
    public SqlPara prepareSqlParaCustomerForList(CrmCustomerPageRequest request, List<Integer> authorizedUserIds, CrmUser crmUser) {
        Kv kv = queryCustomerParameters(request, authorizedUserIds, crmUser);
        if (Objects.isNull(kv)) {
            return null;
        }
        SqlPara sqlPara = Db.getSqlPara("crm.customer.getMobileOrPcCustomerList", kv);
        logger.info("prepareSqlParaCustomerForList sql: {}", sqlPara);
        return sqlPara;
    }

    /**
     * 构建查询SQL查询条件
     *
     * @param request           请求参数对象
     * @param authorizedUserIds 权限用户集合
     * @param crmUser           客户对象
     * @author yue.li
     */
    private Kv queryCustomerParameters(CrmCustomerPageRequest request, List<Integer> authorizedUserIds, CrmUser crmUser) {
        Kv kv = null;
        try {
            kv = Kv.by("ownerUserIds", authorizedUserIds);
            // 是否包含团队成员
            if (CustomerSceneEnum.isContainsTeamMember(request.getSceneId())) {
                kv.set("isMember", crmUser.getCrmAdminUser().getUserId());
                kv.set("ownerUserIds", null);
            }
            // 负责人域账号
            if (Objects.nonNull(request.getOwnerUserLoginIds())) {
                String ownerUserLoginIds = request.getOwnerUserLoginIds();
                List<String> accounts = new ArrayList<>();
                if (StringUtils.isNotEmpty(ownerUserLoginIds)) {
                    accounts = Arrays.asList(ownerUserLoginIds.split(","));
                }
                List<Long> accountsUserIds = adminUserService.getUserIdsByUserNames(accounts);
                if (CollectionUtils.isNotEmpty(accountsUserIds)) {
                    kv.set("accountsUserIds", accountsUserIds);
                }
            }
            // 创建时间按周查询
            if (CrmCustomerDateEnum.ONE_WEEK_KEY.getId().equals(request.getCreateTimeType())) {
                kv.set("createTimeWeek", CrmDateUtil.getLastWeek());
            }
            if (CrmCustomerDateEnum.TWO_WEEK_KEY.getId().equals(request.getCreateTimeType())) {
                kv.set("createTimeWeek", CrmDateUtil.getLastTwoWeek());
            }
            // 创建时间按月查询addOrUpdate
            if (CrmCustomerDateEnum.ONE_MONTH_KEY.getId().equals(request.getCreateTimeType())) {
                kv.set("createTimeMonth", CrmDateUtil.getLastOneMonth());
            }

            kv.putAll(JSONObject.parseObject(JSON.toJSONString(request)));
        } catch (Exception e) {
            logger.error("queryCustomerParameters exception: {}", BaseUtil.getExceptionStack(e));
        }
        return kv;
    }

    /**
     * 根据客户ID查询客户签到列表
     *
     * @param customerRequest 请求对象
     * @author yue.li
     */
    public Page<Record> signList(BasePageRequest<CrmCustomerPageRequest> customerRequest) {
        return Db.paginate(customerRequest.getPage(), customerRequest.getLimit(), new SqlPara().setSql(Db.getSql("crm.customer.querySignInListByCustomerId")).addPara(customerRequest.getData().getCustomerId()));
    }

    /**
     * 获取客户的最近跟进时间
     *
     * @param customerId 客户ID
     * @author yue.li
     */
    public Date getFollowTimeByCustomerId(Integer customerId) {
        Record record = Db.findFirst(Db.getSqlPara("crm.customer.getFollowTimeByCustomerId", Kv.by("customerId", customerId)));
        if (Objects.nonNull(record)) {
            return record.get("followTime");
        } else {
            return null;
        }
    }

    /**
     * 封装权限客户查询SQL
     *
     * @param authorizedUserIds 权限用户集合
     * @author yue.li
     */
    public List<Integer> getAuthorizedCustomerList(List<Integer> authorizedUserIds) {
        List<Integer> customerIds = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(authorizedUserIds)) {
            Kv kv = Kv.by("ownerUserIds", authorizedUserIds);
            SqlPara sqlPara = Db.getSqlPara("crm.customer.getAuthorizedCustomerList", kv);
            List<Record> recordList = Db.find(sqlPara);
            if (CollectionUtils.isNotEmpty(recordList)) {
                List<CrmCustomer> crmCustomerList = recordList.stream().map(item -> new CrmCustomer()._setAttrs(item.getColumns())).collect(Collectors.toList());
                crmCustomerList.forEach(crmCustomer -> customerIds.add(crmCustomer.getCustomerId().intValue()));
            }
        }
        return customerIds;
    }

    /**
     * 客户返回客户池
     *
     * @param tagName 返回原因内容
     * @param tagId   返回原因id
     * @param idsList 客户id列表
     */
    private void pullCustomerPublicPool(String tagName, Integer tagId, List<Long> idsList) {
        CrmLabel crmLabel = new CrmLabel();
        idsList.forEach(customerId -> {
            crmLabel.clear();
            crmLabel.setRelatedId(customerId);
            crmLabel.setRelatedType(Integer.valueOf(CrmEnum.CUSTOMER_TYPE_KEY.getTypes()));
            crmLabel.setLabelKey(CrmLabelEnum.BACK_CUSTOMER_REASON.getTypes());
            crmLabel.setLabelValue(tagId);
            crmLabel.setLabelComment(tagName);

            Record labelRecord = Db.findFirst(Db.getSqlPara("crm.label.findLabel",
                    Kv.by("relatedId", customerId).set("relatedType", CrmEnum.CUSTOMER_TYPE_KEY.getTypes())
                            .set("labelKey", CrmLabelEnum.BACK_CUSTOMER_REASON.getTypes())));
            if (labelRecord != null) {
                crmLabel.setId(labelRecord.getLong("id"));
                crmLabel.update();
            } else {
                try {
                    crmLabel.save();
                } catch (ActiveRecordException e) {
                    logger.error("function:putPublicPoolByIds crmLabel insert error:{}",e.getMessage());
                }
            }
        });
    }

    /**
     * 检查客户名称是否重复
     *
     * @param customerId   更新客户的id
     * @param customerName 客户的名称
     * @return 客户类型
     */
    public String checkRepeatCustomerName(Long customerId, String customerName) {
        Kv kv = Kv.by("customer_name", customerName);
        if (customerId != null) {
            kv.set("customer_id", customerId);
        }
        Record record = Db.findFirst(Db.getSqlPara("crm.customer.queryCustomerWithSameName", kv));
        if (record != null) {
            logger.info("customerName:{},与存量数据customerId:{}的名称重复", customerName, record.get("customer_id"));
            return record.get("customer_type");
        }
        return null;
    }

    /**
     * 获取客户下的网站会员信息
     *
     * @param customerId 客户id
     * @author yue.li
     */
    public CrmSiteMember getSiteMemberInfoByCustomerId(Integer customerId) {
        List<Record> recordList = Db.find(Db.getSqlPara("crm.customer.getSiteMemberInfoByCustomerId", Kv.by("customerId", customerId)));
        if (CollectionUtils.isNotEmpty(recordList)) {
            List<CrmSiteMember> siteMemberList = recordList.stream().map(item -> new CrmSiteMember()._setOrPut(item.getColumns())).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(siteMemberList)) {
                if (siteMemberList.size() == 1) {
                    return siteMemberList.get(0);
                } else {
                    List<CrmSiteMember> siteMemberListAfterFilter = siteMemberList.stream().filter(siteMember -> Integer.valueOf(CrmDistributorEnum.IS_DISTRIBUTOR.getTypes()).equals(siteMember.getIsDistributor())).collect(Collectors.toList());
                    //如果有分销商，返回分销商
                    if (CollectionUtils.isNotEmpty(siteMemberListAfterFilter)) {
                        return siteMemberListAfterFilter.get(0);
                    }
                    return siteMemberList.get(0);
                }
            }
        }
        return null;
    }

    /**
     * 根据客户id查询客户联系小计列表
     *
     * @param customerRequest    请求对象
     * @param ossPrivateFileUtil oss对象
     * @author yue.li
     */
    public Page<Record> getCustomerNotesList(BasePageRequest<CrmCustomerPageRequest> customerRequest, OssPrivateFileUtil ossPrivateFileUtil) {
        Page<Record> customerNotesRecordList = Db.paginate(customerRequest.getPage(), customerRequest.getLimit(), new SqlPara().setSql(Db.getSql("crm.customer.getCustomerNotesList")).addPara(customerRequest.getData().getCustomerId()));
        List<Record> resultList = customerNotesRecordList.getList();
        if (CollectionUtils.isNotEmpty(resultList)) {
            for (Record record : resultList) {
                // 联系小计图片
                adminFileService.queryByBatchId(record.get("batchId"), record, ossPrivateFileUtil);
            }
        }
        return customerNotesRecordList;
    }

    /**
     * 判断用户是否可以下单
     *
     * @param userName     用户域账号
     * @param siteMemberId 官网id
     */
    public R checkUserCanOrder(String userName, Long siteMemberId) {
        Record customerRecord = getBySiteMemberId(siteMemberId);
        if (customerRecord == null) {
            return R.error("该网站会员用户名无法找到关联的CRM客户，请先到CRM做关联操作");
        }

        Integer ownerUserId = customerRecord.getInt("owner_user_id");

        Record userRecord = Db.findFirst(Db.getSql("admin.user.getUserByUserName"), userName);
        if (userRecord == null) {
            return R.error("用户域账号在CRM中不存在，请确认是否已经同步到CRM");
        }

        Long userId = userRecord.getLong("user_id");
        boolean canOrder = true;
        //判断是否有BD角色
        List<AdminRole> userRoles = adminRoleService.getRoleByUserId(userId.intValue());
        if (userRoles != null && userRoles.size() == 1 && "BD".equals(userRoles.get(0).getRoleName())) {
            canOrder = false;
        }

        if (canOrder) {
            return R.ok().put("code", 200);
        }

        //判断用户和其下属用户是否是这个客户的负责人
        if (ownerUserId != null) {
            List<Long> adminUsers = new ArrayList<>();
            adminUsers.add(userId);

            String myselfAndSubKey = AdminUserService.MY_SELF_AND_SUB + userId;
            if (Redis.use().get(myselfAndSubKey) == null) {
                List<Long> userIds = adminUserService.queryUserByParentUser(userId, BaseConstant.AUTH_DATA_RECURSION_NUM);
                adminUsers.addAll(userIds);
                Redis.use().setex(myselfAndSubKey, 3600, userIds);
            } else {
                adminUsers.addAll(Redis.use().get(myselfAndSubKey));
            }

            canOrder = adminUsers.contains(Long.valueOf(ownerUserId));
        }

        //判断客户团队成员是否包含此用户
        if (!canOrder) {
            canOrder = crmGroupMemberService.isMember(customerRecord.getLong("customer_id"), userId, Integer.valueOf(CrmEnum.CUSTOMER_TYPE_KEY.getTypes()));
        }

        if (canOrder) {
            return R.ok().put("code", 200);
        } else {
            return R.error("您无权为该客户下单");
        }
    }

    /**
     * 查询部门和bd
     *
     * @param siteMemberId 官网id
     */
    public R queryDeptAndBdByUser(Long siteMemberId) {
        Record customerRecord = getBySiteMemberId(siteMemberId);
        if (customerRecord == null) {
            return R.error("该网站会员用户名无法找到关联的CRM客户，请先到CRM做关联操作");
        }

        customerRecord = Db.findFirst(Db.getSqlPara("crm.customer.getCustomerUserDeptMsg", Kv.by("customerId", customerRecord.getStr("customer_id"))));
        return Objects.requireNonNull(R.ok().put("code", 200)).put("data", customerRecord);
    }

    /**
     * 判断客户是否可以领取
     *
     * @param customerId 客户id
     * @param deptId     部门id
     */
    public R canReceive(Long customerId, Integer deptId) {
        CrmCustomer customer = CrmCustomer.dao.findById(customerId);
        if (customer == null) {
            return R.error("客户不存在");
        }

        String industryCode = customer.getIndustryCode();
        if (StringUtils.isBlank(industryCode)) {
            return R.error("客户的行业信息尚未维护");
        }

        String businessDeptId = adminDeptService.getBusinessDepartmentByDeptId(String.valueOf(deptId));

        int count = Db.findFirst(Db.getSqlPara("crm.industryOfDept.getCountByIndustryAndDept", Kv.by("industryCode", industryCode).set("deptId", businessDeptId))).getInt("count");

        return count > 0 ? R.ok() : R.error("您无权为该客户下单");

    }


    /**
     * 获取分销信息减少销售数量的原因
     *
     * @author yue.li
     */
    public List<Map<String, Object>> queryReduceReason() {
        List<Map<String, Object>> reduceReasonList = new ArrayList<>(3);
        for (CrmReduceReasonsEnum c : CrmReduceReasonsEnum.values()) {
            Map<String, Object> reduceReasonMap = Maps.newHashMapWithExpectedSize(4);
            reduceReasonMap.put("code", c.getCode());
            reduceReasonMap.put("name", c.getName());
            reduceReasonList.add(reduceReasonMap);
        }
        return reduceReasonList;
    }

    public Record queryOwnerByCustomerId(Long customerId) {
        return Db.findFirst(Db.getSql("crm.customer.queryOwnerByCustomerId"), customerId);
    }

    /**
     * 查询我负责客户的库容信息
     * @param userId
     */
    public Record searchUserCapacity(Long userId){
        return Db.findFirst(Db.getSqlPara("crm.userCapacity.findUsedAndTotalCapacityByUserId",Kv.by("userId", userId)));
    }

    /**
     * 查询我的总库容信息
     * @param userId
     */
    public Record searchUserTotalCapacity(Long userId){
        return Db.findFirst(Db.getSqlPara("crm.userCapacity.findTotalCapacityByUserId",Kv.by("userId", userId)));
    }

    /**
     * 检查用户库容是否符合要求
     * @param userId 用户id
     * @param storageType 仓库类型： 1 考察库 2 关联库
     * @param addNum 添加数量
     * @return
     */
    public boolean checkUserCapacity(Long userId, int storageType, int addNum){
        Record record = searchUserCapacity(userId);
        Integer inspectCap = record.getInt("inspect_cap") == null? null : record.getInt("inspect_cap") - record.getInt("used_inspect_cap");
        Integer relateCap = record.getInt("relate_cap") == null? null : record.getInt("relate_cap") - record.getInt("used_relate_cap");

        if (storageType == CustomerStorageTypeEnum.INSPECT_CAP.getCode()){
            return inspectCap == null || inspectCap >= addNum;
        } else if (storageType == CustomerStorageTypeEnum.RELATE_CAP.getCode()){
            return relateCap == null || relateCap >= addNum;
        }else {
            logger.error("仓库类型：{} 传值错误",storageType);
            return false;
        }
    }

    /**
     * 检查部门库容是否符合要求
     * @param deptId 部门id
     * @param addNum 添加数量
     * @return
     */
    public JSONObject checkDeptCapacity(Long deptId, int addNum){
        JSONObject jsonObject = getNearestDeptCapacity(getDeptMap (), getDeptCapacityMap(), deptId);
        Integer capacity = jsonObject.getInteger("capacity");
        if (jsonObject.containsKey("errorCode")){
            jsonObject.put("result",Boolean.FALSE);
        } else if (capacity == null){
            jsonObject.put("result",Boolean.TRUE);
        }else {
            //获取相应部门池下面已经存在的客户数量
            Record record = Db.findFirst(Db.getSql("crm.customer.findDeptCustomerPoolCount"), jsonObject.getString("deptId"));
            capacity = capacity - record.getInt("customerCount");
            jsonObject.put("result",capacity >= addNum);
            jsonObject.put("capacity",capacity);
        }

        return jsonObject;
    }

    /**
     * 获取离传递部门id最近的部门配置库容信息
     * @param deptMap 部门map
     * @param deptCapacityMap 配置库容map
     * @param deptId 部门id
     * @return
     */
    private JSONObject getNearestDeptCapacity(Map<Long, Integer> deptMap, Map<Long, Integer> deptCapacityMap, Long deptId) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("capacity",null);
        jsonObject.put("deptId",null);

        if (deptCapacityMap == null || deptCapacityMap.isEmpty()){
            logger.error("部门库容信息表为空，部门池将无限制");
            return jsonObject;
        }
        if (deptMap == null || deptMap.isEmpty()){
            logger.error("程序内存部门树缓存异常，请检查代码");

            jsonObject.put("errorCode",500);
            return jsonObject;
        }

        Integer capacity = deptCapacityMap.get(deptId);
        if (capacity != null){
            jsonObject.put("capacity",capacity);
            jsonObject.put("deptId",deptId);
            return jsonObject;
        }else{
            if (!deptMap.containsKey(deptId)){
                logger.error("程序内存部门树缓存异常，不包含部门{}",deptId);

                jsonObject.put("errorCode",500);
                return jsonObject;
            }
            //获取上级部门id
            deptId = Long.valueOf(deptMap.get(deptId));
            if (deptId == -1){
                //找到顶级还未有，则返回null，表示无限制
                return jsonObject;
            }else{
                return getNearestDeptCapacity(deptMap,deptCapacityMap,deptId);
            }
        }
    }

    /**
     * 获取有bd负责人。设置了考察库或者关联库的的客户
     *
     * @return
     */
    public List<EffectiveCustomerDto> getEffectiveCustomerDtoListByUserIds(List<Long> userIds) {
        List<Record> records = Db.find(Db.getSqlPara("crm.customerExt.getAllEffectiveCustomerByUserId",
                Kv.by("userIds", userIds)));
        if (CollectionUtils.isEmpty(records)) {
            return Collections.emptyList();
        }
        List<EffectiveCustomerDto> list = Lists.newArrayListWithCapacity(records.size());
        for (Record record : records) {
            EffectiveCustomerDto customerDto = new EffectiveCustomerDto();
            customerDto.setCustomerId(record.getLong("customer_id"));
            customerDto.setCustomerName(record.getStr("customer_name"));
            customerDto.setOwnerUserId(record.getLong("owner_user_id"));
            customerDto.setStorageType(record.getInt("storage_type"));
            if (Objects.nonNull(record.get("owner_time"))) {
                customerDto.setOwnerTime(record.getDate("owner_time"));
            }
            list.add(customerDto);
        }
        return list;
    }

    /**
     * 配置的所有部门库容信息
     * @author yue.li
     */
    private Map<Long, Integer> getDeptCapacityMap() {
        List<Record> recordList = Db.find(Db.getSql("admin.dept.capacity.findTotalDeptCapacity"));
        return recordList.stream().collect(Collectors.toMap(o -> o.getLong("dept_id"), o -> o.getInt("capacity")));
    }

    /**
     * 查询公司部门Map
     * @return
     */
    public Map<Long, Integer> getDeptMap (){
        //获取公司所有部门
        List<AdminDept> adminDeptList = BaseUtil.getAdminDeptList();
        if (adminDeptList == null){
            crmBaseDataCron.run();
            adminDeptList = BaseUtil.getAdminDeptList();
        }
        return adminDeptList.stream().collect(Collectors.toMap(AdminDept::getDeptId, AdminDept::getPid));
    }


    /**
     * 把客户放回公海
     *
     * @param customerId
     */
    public void pullCustomerPublicPool(Long customerId) {
        Db.update(Db.getSql("crm.customer.pullCustomerPublicPool"), customerId);
    }

    /**
     * 根据客户编号 清空客户库存信息
     *
     * @param customerId
     */
    public void deleteStorageTypeByCustomerIds(Long customerId) {
        if (Objects.isNull(customerId)) {
            return;
        }
        Db.update(Db.getSqlPara("crm.customerExt.deleteStorageTypeByCustomerIds", Kv.by("customerIds", Collections.singletonList(customerId))));
    }

    /**
     * 判断是否需要审批
     *
     * @param customerId 客户ID
     * @param deptId     事业部ID
     * @param esbConfig  esb实体
     * @return R
     * @author yue.li
     */
    @Before(Tx.class)
    public R judgeNeedToApproval(Long customerId, Integer deptId, EsbConfig esbConfig) {
        logger.info("judgeNeedToApproval method param[customerId:{} ,deptId:{}]", customerId, deptId);

        boolean needApprovalFlag = false;
        // 查询客户领取审核规则
        AdminCustomerReceiveRole adminCustomerReceiveRole = adminCustomerReceiveRoleService.getCustomerReceiveRole(deptId);
        if (Objects.nonNull(adminCustomerReceiveRole)) {
            if (Objects.nonNull(adminCustomerReceiveRole.getIsNeedCheck()) && Objects.equals(CrmConstant.NEED_CHECK, adminCustomerReceiveRole.getIsNeedCheck())) {
                //查看缓存中的业绩数据
                Long userId = BaseUtil.getUserId();
                CrmPerformanceDto crmPerformanceDto = redisCache.get(CrmConstant.PERFORMANCE_USER_CUSTOMER + userId + "_" + customerId);
                if (crmPerformanceDto == null){
                    crmPerformanceDto = getCustomerPerformanceMsg(customerId, userId, esbConfig,null);
                }

                if (Objects.nonNull(adminCustomerReceiveRole.getMoney()) && crmPerformanceDto.getOrderAmount().compareTo(adminCustomerReceiveRole.getMoney()) >= 0) {
                    needApprovalFlag = true;
                }
            }
        }
        return R.ok().put("data", new Record().set("needToApproval", needApprovalFlag));
    }

    /**
     * 统计负责人负责的客户数量
     *
     * @param ownerUserId
     * @return
     */
    public Integer countCustomerListByOwnerUserId(Long ownerUserId) {
        return Db.queryInt(Db.getSql("crm.customer.countCustomerListByOwnerUserId"), ownerUserId);
    }

    /**
     * 通过客户ID查询客户列表
     *
     * @param customerIds
     * @return
     */
    public List<CrmCustomer> queryCustomerListByCustomerIds(List<Long> customerIds) {
        if (CollectionUtils.isEmpty(customerIds)) {
            return Collections.emptyList();
        }
        List<CrmCustomer> crmCustomers = CrmCustomer.dao.find(Db.getSqlPara("crm.customer.getListByIds", Kv.by("ids", customerIds)));
        if (CollectionUtils.isEmpty(crmCustomers)) {
            return Collections.emptyList();
        }
        return crmCustomers;
    }

    /**
     * 检查客户释放是否符合部门锁库规则
     * 1.查询客户库类型
     * 2。查询客户负责人、负责时间
     * 3.查询出配置锁库的部门和天数
     * 4.判断负责人的最近配置部门是否存在
     * 5.比较owner_time与配置天数的关系，进行提示
     * @param customerId
     * @return
     */
    public R checkReleaseRule(Integer customerId){
        if (customerId == null){
            logger.info("function:checkReleaseRule --> customerId is null");
            return R.ok();
        }

        CrmCustomerExt customerExt = new CrmCustomerExt().dao().findFirst("select * from 72crm_crm_customer_ext where customer_id = ?", customerId);
        if (customerExt == null || customerExt.getStorageType() == null){
            return R.ok();
        }else if(customerExt.getStorageType() == CustomerStorageTypeEnum.RELATE_CAP.getCode()){
            CrmCustomer customer = CrmCustomer.dao.findById(customerId);
            if(customer == null){
                logger.error("function:checkReleaseRule --> customerId:{} customer not exist",customerId);
                return R.error("客户ID：" + customerId + "，查存不到客户信息");
            }

            Integer ownerUserId = customer.getOwnerUserId();
            Date ownerTime = customer.getOwnerTime();
            if (ownerUserId == null || ownerTime == null){
                logger.warn("function:checkReleaseRule --> customerId:{} ownerUserId is null or ownerTime is null",customerId);
                return R.ok();
            }

            //获取客户归属的部门
            Record record = Db.findFirst(Db.getSql("admin.user.queryUserFromAdminUserByUserId"), ownerUserId);
            if (Objects.isNull(record)) {
                logger.error("function:checkReleaseRule --> ownerUserId:{} adminUser not exist",ownerUserId);
                return R.ok();
            }
            Integer deptId = record.getInt("dept_id");
            //库类型为关联库时，则须查询关联库锁库规则
            JSONObject jsonObject = getNearestDeptCapacity(getDeptMap (), getDeptRelateLockDaysMap(), Long.valueOf(deptId));
            Integer relateLockDays = jsonObject.getInteger("capacity");
            if (jsonObject.containsKey("errorCode")){
                return R.error("获取离传递部门id最近的部门配置库容信息 发生错误");
            } else if (relateLockDays == null || relateLockDays == 0){
                return R.ok();
            }else {
                //获取当前时间与负责时间之间的天数
                long days = DateUtil.between(ownerTime, DateUtil.date().toJdkDate(), DateUnit.DAY);
                //天数大于锁定天数，返回true
                if (days > relateLockDays){
                    return  R.ok();
                }else{
                    return R.error("关联库客户从获得客户开始，有"+relateLockDays+"天的锁库时间，该客户当前处于锁库期，无法释放，待"+DateUtil.offsetDay(DateUtil.date(),relateLockDays + 1).toString(DatePattern.NORM_DATE_PATTERN)+"方可释放");
                }
            }
        }

        return R.ok();
    }

    /**
     * 配置的配置了关联库锁库规则的部门信息
     */
    private Map<Long, Integer> getDeptRelateLockDaysMap() {
        List<Record> recordList = Db.find(Db.getSql("admin.dept.capacity.findTotalDeptCapacity"));
        return recordList.stream().collect(Collectors.toMap(o -> o.getLong("dept_id"), o -> o.getInt("relate_lock_days")));
    }

    /**
     * 搜索客户名称
     * @param company 客户名称
     */
    public R searchCustomerName(String company) {
        if (StringUtils.isNotEmpty(company)) {
            company = filterRuleCustomerName(company);
            Page<Record> recordPage = Db.paginate(1, 10, Db.getSqlPara("crm.customer.searchCustomerName", Kv.by("customerName", company)));
            if(Objects.nonNull(recordPage)) {
                return R.ok().put("data",recordPage.getList());
            }
        }
        return R.ok();
    }

    /**
     * 会员注册渠道编辑负责人
     *
     * @param customerId
     * @param newOwnerUserId
     * @return
     */
    public boolean memberChannelUpdateOwnerUserId(Long customerId, Integer newOwnerUserId) {
        CrmCustomer crmCustomer = CrmCustomer.dao.findById(customerId);

        if (Objects.isNull(crmCustomer)) {
            return true;
        }
        if (Objects.nonNull(crmCustomer.getDeptId()) || Objects.nonNull(crmCustomer.getOwnerUserId())) {
            return true;
        }
        /*
         *  客户有上游分销商，不允许分派
         *  该规则仅限于分派给数字地信事业部、在线运营事业部的BD
         *  目前姚蕾归属于在线运营事业部
         */
        List<Integer> customerIds = Lists.newArrayList();
        customerIds.add(customerId.intValue());
        if (CollectionUtils.isNotEmpty(this.checkParentDistributor(customerIds))) {
        	
        	return true;
        }
        CrmCustomer oldCrmCustomer = new CrmCustomer()._setAttrs(crmCustomer.toRecord().getColumns());
        AdminUser adminUser = adminUserService.getAdminUserByUserId(Long.valueOf(newOwnerUserId));

        Integer oldOwnerUserId = crmCustomer.getOwnerUserId();
        crmCustomer.setOwnerUserId(newOwnerUserId);
        crmCustomer.setOwnerUserName(Objects.nonNull(adminUser) ? adminUser.getRealname() : "");
        crmCustomer.setOwnerTime(DateTime.now());
        boolean customerUpdate = crmCustomer.update();
        if (!customerUpdate) {
            return false;
        }
        //保存客户扩展表
        saveCrmCustomerExt(customerId, CustomerStorageTypeEnum.RELATE_CAP.getCode(), null,null);

        // 记录日志
        crmRecordService.updateRecord(oldCrmCustomer, crmCustomer, CrmEnum.CUSTOMER_DISTRIBUTE_KEY.getTypes(), null);

        if (!Objects.equals(oldOwnerUserId, newOwnerUserId)) {
            //客户负责人变更记录日志
            crmChangeLogService.saveCustomerChangeLog(CrmCustomerChangeLogEnum.INSPECT_BD.getCode(),crmCustomer.getCustomerId(),Long.valueOf(newOwnerUserId),null,null);
        }
        return true;
    }
}
