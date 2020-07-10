package com.kakarote.crm9.erp.crm.service;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.util.TypeUtils;
import com.google.common.collect.Maps;
import com.jfinal.aop.Inject;
import com.jfinal.kit.Kv;
import com.jfinal.log.Log;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.kakarote.crm9.common.constant.BaseConstant;
import com.kakarote.crm9.erp.admin.entity.AdminField;
import com.kakarote.crm9.erp.admin.entity.AdminRecord;
import com.kakarote.crm9.erp.admin.service.AdminDataDicService;
import com.kakarote.crm9.erp.crm.common.CrmEnum;
import com.kakarote.crm9.erp.crm.constant.CrmTagConstant;
import com.kakarote.crm9.erp.crm.entity.CrmActionRecord;
import com.kakarote.crm9.erp.crm.entity.CrmBusiness;
import com.kakarote.crm9.erp.crm.entity.CrmContacts;
import com.kakarote.crm9.erp.crm.entity.CrmCustomer;
import com.kakarote.crm9.erp.crm.entity.CrmLeads;
import com.kakarote.crm9.erp.crm.entity.CrmProduct;
import com.kakarote.crm9.erp.crm.entity.CrmReceivables;
import com.kakarote.crm9.erp.crm.entity.CrmReceivablesPlan;
import com.kakarote.crm9.utils.BaseUtil;
import com.kakarote.crm9.utils.DeepClone;
import com.kakarote.crm9.utils.R;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * crm模块操作记录
 *
 * @author hmb
 */
public class CrmRecordService {
    @Inject
    private AdminDataDicService adminDataDicService;

    private Log logger = Log.getLog(getClass());

    /**
     * 属性kv
     */
    private Map<String, Map<String, String>> propertiesMap = new HashMap<>(100);
    private static final String CRM_PROPERTIES_KEY = "crm:properties_map";

    private void init() {
        List<Record> recordList = Db.findByCache(CRM_PROPERTIES_KEY, CRM_PROPERTIES_KEY, Db.getSql("crm.record.getProperties"));
        Map<String,List<Record>> pMap = recordList.stream().collect(Collectors.groupingBy(record -> record.get("type")));
        setProperties(pMap);
    }

    private void setProperties(Map<String,List<Record>> pMap) {
        pMap.forEach((k,v)->{
            HashMap<String,String> resultMap = Maps.newHashMapWithExpectedSize(v.size());
            v.forEach(record-> resultMap.put(record.getStr("COLUMN_NAME"), record.getStr("COLUMN_COMMENT")));
            propertiesMap.put(k,resultMap);
        });
    }

    private static List<String> textList = new ArrayList<>();

    /**
     * 更新记录
     *
     * @param oldObj   之前对象
     * @param newObj   新对象
     * @param crmTypes 类型
     */
    public void updateRecord(Object oldObj, Object newObj, String crmTypes, Long userId) {
        init();
        CrmActionRecord crmActionRecord = new CrmActionRecord();
        crmActionRecord.setCreateUserId(userId == null ? null:userId.intValue());
        crmActionRecord.setCreateTime(new Date());

        if (crmTypes.equals(CrmEnum.PRODUCT_TYPE_KEY.getTypes())) {
            CrmProduct oldObj1 = (CrmProduct) oldObj;
            CrmProduct newObj1 = (CrmProduct) newObj;
            searchChange(textList, oldObj1._getAttrsEntrySet(), newObj1._getAttrsEntrySet(), CrmEnum.PRODUCT_TYPE_KEY.getTypes());
            crmActionRecord.setTypes(CrmEnum.PRODUCT_TYPE_KEY.getTypes());
            crmActionRecord.setActionId(oldObj1.getProductId().intValue());
        } else if (crmTypes.equals(CrmEnum.CONTACTS_TYPE_KEY.getTypes())) {
            CrmContacts oldObj1 = formatCrmContacts((CrmContacts) new DeepClone().deepCopy(oldObj));
            CrmContacts newObj1 =formatCrmContacts((CrmContacts) new DeepClone().deepCopy(newObj));
            searchChange(textList, oldObj1._getAttrsEntrySet(), newObj1._getAttrsEntrySet(), CrmEnum.CONTACTS_TYPE_KEY.getTypes());
            crmActionRecord.setTypes(CrmEnum.CONTACTS_TYPE_KEY.getTypes());
            crmActionRecord.setActionId(oldObj1.getContactsId().intValue());
        } else if (crmTypes.equals(CrmEnum.CUSTOMER_TYPE_KEY.getTypes())) {
            CrmCustomer oldObj1 = formatCrmCustomer((CrmCustomer) new DeepClone().deepCopy(oldObj));
            CrmCustomer newObj1 = formatCrmCustomer((CrmCustomer) new DeepClone().deepCopy(newObj));
            searchChange(textList, oldObj1._getAttrsEntrySet(), newObj1._getAttrsEntrySet(), CrmEnum.CUSTOMER_TYPE_KEY.getTypes());
            crmActionRecord.setTypes(CrmEnum.CUSTOMER_TYPE_KEY.getTypes());
            crmActionRecord.setActionId(oldObj1.getCustomerId().intValue());
        } else if (crmTypes.equals(CrmEnum.LEADS_TYPE_KEY.getTypes())) {
            CrmLeads oldObj1 = formatCrmLeads((CrmLeads) new DeepClone().deepCopy(oldObj));
            CrmLeads newObj1 = formatCrmLeads((CrmLeads) new DeepClone().deepCopy(newObj));
            searchChange(textList, oldObj1._getAttrsEntrySet(), newObj1._getAttrsEntrySet(), CrmEnum.LEADS_TYPE_KEY.getTypes());
            crmActionRecord.setTypes(CrmEnum.LEADS_TYPE_KEY.getTypes());
            crmActionRecord.setActionId(oldObj1.getLeadsId().intValue());
        }  else if (crmTypes.equals(CrmEnum.RECEIVABLES_TYPE_KEY.getTypes())) {
            CrmReceivables oldObj1 = (CrmReceivables) oldObj;
            CrmReceivables newObj1 = (CrmReceivables) newObj;
            searchChange(textList, oldObj1._getAttrsEntrySet(), newObj1._getAttrsEntrySet(), CrmEnum.RECEIVABLES_TYPE_KEY.getTypes());
            crmActionRecord.setTypes(CrmEnum.RECEIVABLES_TYPE_KEY.getTypes());
            crmActionRecord.setActionId(oldObj1.getReceivablesId().intValue());
        } else if (crmTypes.equals(CrmEnum.BUSINESS_TYPE_KEY.getTypes())) {
            CrmBusiness oldObj1 = formatCrmBusiness((CrmBusiness) new DeepClone().deepCopy(oldObj));
            CrmBusiness newObj1 = formatCrmBusiness((CrmBusiness) new DeepClone().deepCopy(newObj));
            searchChange(textList, oldObj1._getAttrsEntrySet(), newObj1._getAttrsEntrySet(), CrmEnum.BUSINESS_TYPE_KEY.getTypes());
            crmActionRecord.setTypes(CrmEnum.BUSINESS_TYPE_KEY.getTypes());
            crmActionRecord.setActionId(oldObj1.getBusinessId().intValue());
        }else if(crmTypes.equals(CrmEnum.RECEIVABLES_PLAN_TYPE_KEY.getTypes())) {
            CrmReceivablesPlan oldObj1 = (CrmReceivablesPlan) oldObj;
            CrmReceivablesPlan newObj1 = (CrmReceivablesPlan) newObj;
            searchChange(textList, oldObj1._getAttrsEntrySet(), newObj1._getAttrsEntrySet(), CrmEnum.RECEIVABLES_PLAN_TYPE_KEY.getTypes());
            crmActionRecord.setTypes(CrmEnum.RECEIVABLES_PLAN_TYPE_KEY.getTypes());
            crmActionRecord.setActionId(oldObj1.getPlanId().intValue());

        } else if (crmTypes.equals(CrmEnum.CUSTOMER_DISTRIBUTE_KEY.getTypes())) {
            CrmCustomer oldObj1 = formatCrmCustomer((CrmCustomer) new DeepClone().deepCopy(oldObj));
            CrmCustomer newObj1 = formatCrmCustomer((CrmCustomer) new DeepClone().deepCopy(newObj));
            textList.add("系统将客户分发给" + newObj1.getOwnerUserName() + "。");
            crmActionRecord.setTypes(CrmEnum.CUSTOMER_DISTRIBUTE_KEY.getTypes());
            crmActionRecord.setActionId(oldObj1.getCustomerId().intValue());
        }

        crmActionRecord.setContent(JSON.toJSONString(textList));
        if (textList.size() > 0) {
            crmActionRecord.save();
        }
        logger.info(String.format("用户id：%s, %s",userId == null ? null:userId.intValue(),JSON.toJSONString(textList)));
        textList.clear();

    }

    /***
     * 格式化线索信息
     * @author yue.li
     * @param crmLeads 线索实体
     * @return 格式化线索实体
     */
    public CrmLeads formatCrmLeads(CrmLeads crmLeads){
        crmLeads.setReceiveTime(null);
        String accuracyRequirements;
        String customerLevel;
        String customerIndustry;
        String deptId = null;
        /*格式化标签数据*/
        if (StringUtils.isNotBlank(crmLeads.getAccuracyRequirementsName())){
            crmLeads.setAccuracyRequirements(crmLeads.getAccuracyRequirementsName());
        }else if(StringUtils.isNotEmpty(crmLeads.getStr("accuracy_requirements"))){
            accuracyRequirements = adminDataDicService.formatTagValueId(CrmTagConstant.ACCURACY_REQUIREMENTS,crmLeads.getStr("accuracy_requirements"));
            crmLeads.setAccuracyRequirements(accuracyRequirements);
        }

        if (StringUtils.isNotBlank(crmLeads.getCustomerLevelName())){
            crmLeads.setCustomerLevel(crmLeads.getCustomerLevelName());
        }else if(StringUtils.isNotEmpty(crmLeads.getStr("customer_level"))){
            customerLevel = adminDataDicService.formatTagValueId(CrmTagConstant.CUSTOMER_GRADE,crmLeads.getStr("customer_level"));
            crmLeads.setCustomerLevel(customerLevel);
        }

        if (StringUtils.isNotBlank(crmLeads.getCustomerIndustryName())){
            crmLeads.setCustomerIndustry(crmLeads.getCustomerIndustryName());
        }else if(StringUtils.isNotEmpty(crmLeads.getStr("customer_industry"))){
            customerIndustry = adminDataDicService.formatTagValueId(CrmTagConstant.CUSTOMER_INDUSTRY,crmLeads.getStr("customer_industry"));
            crmLeads.setCustomerIndustry(customerIndustry);
        }

        if (StringUtils.isNotBlank(crmLeads.getDeptName())){
            crmLeads.setDeptId(crmLeads.getDeptName());
        }else if(StringUtils.isNotEmpty(crmLeads.getDeptId())){
            Record record = Db.findFirst(Db.getSql("admin.dept.queryDeptInfoByDeptId"), crmLeads.getDeptId());
            if(record != null) {
                deptId = record.getStr("name");
            }
            crmLeads.setDeptId(deptId);
        }

        return crmLeads;
    }

    /***
     * 格式化客户信息
     * @author yue.li
     * @param crmCustomer 客户实体
     * @return 格式化客户实体
     */
    public CrmCustomer formatCrmCustomer(CrmCustomer crmCustomer) {
        /*格式化标签数据*/
        String customerGrade = null;
        String customerType = null;
        String distributor = null;
        String partner = null;
        String ownerUserId = null;
        //获取owner_user_id的变动进行比较，把用户名记录到操作日志中
        if (crmCustomer.getOwnerUserId() != null) {
            ownerUserId = crmCustomer.getOwnerUserId().toString();
        } else if (StringUtils.isNotBlank(crmCustomer.getStr("owner_user_id"))) {
            ownerUserId = crmCustomer.getStr("owner_user_id");
        }
        if (StringUtils.isNotBlank(ownerUserId)) {
            String ownerUserName = null;
            Record record = Db.findFirst(Db.getSql("admin.user.queryUserFromAdminUserByUserId"), crmCustomer.getOwnerUserId());
            if (record != null) {
                ownerUserName = record.getStr("realname");
            }
            Map<String, Object> attr = new HashMap<>(1);
            attr.put("owner_user_id", ownerUserName);
            crmCustomer._setOrPut(attr);
        }
        if (StringUtils.isNotEmpty(crmCustomer.getStr("customer_grade"))) {
            customerGrade = adminDataDicService.formatTagValueId(CrmTagConstant.CUSTOMER_GRADE, crmCustomer.getStr("customer_grade"));
        }
        if (StringUtils.isNotEmpty(crmCustomer.getStr("customer_type"))) {
            customerType = adminDataDicService.formatTagValueId(CrmTagConstant.CUSTOMER_TYPE, crmCustomer.getStr("customer_type"));
        }
        if (StringUtils.isNotEmpty(crmCustomer.getStr("distributor"))) {
            distributor = adminDataDicService.formatTagValueId(CrmTagConstant.DISTRIBUTOR, crmCustomer.getStr("distributor"));
        }
        if (StringUtils.isNotEmpty(crmCustomer.getStr("partner"))) {
            partner = adminDataDicService.formatTagValueId(CrmTagConstant.PARTNER, crmCustomer.getStr("partner"));
        }
        crmCustomer.setCustomerGrade(customerGrade);
        crmCustomer.setCustomerType(customerType);
        crmCustomer.setDistributor(distributor);
        crmCustomer.setPartner(partner);
        crmCustomer.setCustomerOrigin(null);
        /*格式化标签数据*/
        return crmCustomer;
    }

    /***
     * 格式化联系人
     * @author yue.li
     * @param crmContacts 联系人实体
     */
    public CrmContacts formatCrmContacts(CrmContacts crmContacts){
        String role = null;
        if(StringUtils.isNotEmpty(crmContacts.getStr("role"))){
            role = adminDataDicService.formatTagValueId(CrmTagConstant.CUSTOMER_ROLE,crmContacts.getStr("role"));
        }
        crmContacts.setRole(role);
        return crmContacts;
    }

    /***
     * 格式化商机
     * @author yue.li
     * @param crmBusiness 商机实体
     */
    public CrmBusiness formatCrmBusiness(CrmBusiness crmBusiness){
        Map<String,Object> attr = new HashMap<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String deptId = null;
        String applicationScenario = null;
        String statusId = null;
        String dealDate = null;
        if(crmBusiness.getDeptId() != null){
            Record record = Db.findFirst(Db.getSql("admin.dept.queryDeptInfoByDeptId"), crmBusiness.getDeptId());
            if(record != null){
                deptId = record.getStr("name");
            }
        }
        if(crmBusiness.getApplicationScenario() != null){
            Record record = Db.findFirst( Db.getSqlPara("admin.scenario.queryScenarioByScenarioId", Kv.by("scenarioId",crmBusiness.getApplicationScenario())));
            if(record != null){
                applicationScenario = record.getStr("name");
            }
        }
        if(crmBusiness.getStatusId() != null){
            Record record = Db.findFirst(Db.getSqlPara("admin.businessStatusType.queryBusinessStatusByStatusId", Kv.by("statusId",crmBusiness.getStatusId())));
            if(record != null){
                statusId =  record.getStr("name");
            }
        }
        if(crmBusiness.getDealDate() != null){
            dealDate = sdf.format(crmBusiness.getDealDate());
        }
        attr.put("dept_id",deptId);
        crmBusiness._setOrPut(attr);
        attr.put("application_scenario",applicationScenario);
        crmBusiness._setOrPut(attr);
        attr.put("status_id",statusId);
        crmBusiness._setOrPut(attr);
        attr.put("deal_date",dealDate);
        crmBusiness._setOrPut(attr);
        return crmBusiness;
    }

    public void addRecord(Integer actionId, String crmTypes,Long userId) {
        addRecord(actionId, crmTypes, crmTypes, userId);
    }

    public void addRecord(Integer actionId, String crmTypes, String targetType, Long userId) {
        String content = "新建了" + CrmEnum.getName(crmTypes);
        CrmActionRecord crmActionRecord = new CrmActionRecord(userId == null ? null:userId.intValue(), targetType, actionId, content);
        crmActionRecord.save();
        logger.info(String.format("用户id：%s,新建了 %s",userId == null ? null:userId.intValue(),CrmEnum.getName(crmTypes)));
    }

    /**
     * 记录操作日志
     * @author yue.li
     * @param userId 用户ID
     * @param actionId 操作业务ID
     * @param content 操作内容
     * @param date 日期
     */
    public void dealRecordLog(Long userId,String types,Integer actionId,String content,Date date) {
        CrmActionRecord crmActionRecord = new CrmActionRecord(userId == null ? null:userId.intValue(),types , actionId, content);
        crmActionRecord.setCreateTime(date);
        crmActionRecord.save();
    }

    public void addDeleteAttachmentRecord(Integer actionId, String crmTypes, String fileName, Long userId) {
        CrmActionRecord crmActionRecord = new CrmActionRecord();
        crmActionRecord.setCreateUserId(userId == null ? null:userId.intValue());
        crmActionRecord.setCreateTime(new Date());
        crmActionRecord.setTypes(crmTypes);
        crmActionRecord.setActionId(actionId);
        ArrayList<String> strings = new ArrayList<>();
        strings.add("删除了附件：" + fileName);
        crmActionRecord.setContent(JSON.toJSONString(strings));
        crmActionRecord.save();
        logger.info(String.format("用户id：%s,%s",userId == null ? null:userId.intValue(),JSON.toJSONString(strings)));
    }

    /**
     *  传入对象需set以下字段值
     * actionId      业务表主键
     * crmTypes      业务模块type，具体见CrmEnum类
     * userId        登录用户id
     * content       操作记录内容
     */
    public R addCrmActionRecord(CrmActionRecord crmActionRecord) {
        logger.info(String.format("新建操作记录, %s", crmActionRecord));
        return crmActionRecord.save() ? R.ok() : R.error(String.format("创建操作记录失败: %s", crmActionRecord));
    }

    public void updateRecord(JSONArray jsonArray, String batchId) {
        if (jsonArray == null) {
            return;
        }
        List<AdminField> oldFieldList = new AdminField().dao().find("select * from 72crm_admin_field where batch_id = ? and parent_id != 0", batchId);
        oldFieldList.forEach(oldField -> jsonArray.forEach(json -> {
            AdminField newField = TypeUtils.castToJavaBean(json, AdminField.class);
            String oldFieldValue;
            String newFieldValue;
            if (oldField.getValue() == null) {
                oldFieldValue = "空";
            } else {
                oldFieldValue = oldField.getValue();
            }
            if (newField.getValue() == null) {
                newFieldValue = "空";
            } else {
                newFieldValue = newField.getValue();
            }
            if (oldField.getName().equals(newField.getName()) && !oldFieldValue.equals(newFieldValue)) {
                textList.add("将" + oldField.getName() + " 由" + oldFieldValue + "修改为" + newFieldValue + "。");
            }
        }));
    }

    private void searchChange(List<String> textList, Set<Map.Entry<String, Object>> oldEntries, Set<Map.Entry<String, Object>> newEntries, String crmTypes) {
        oldEntries.forEach(x -> newEntries.forEach(y -> {
            Object oldValue = x.getValue();
            Object newValue = y.getValue();
            if (oldValue instanceof Date) {
                oldValue = DateUtil.formatDateTime((Date) oldValue);
            }
            if (newValue instanceof Date) {
                newValue = DateUtil.formatDateTime((Date) newValue);
            }
            if (oldValue == null || "".equals(oldValue)) {
                oldValue = "空";
            }
            if (newValue == null || "".equals(newValue)) {
                newValue = "空";
            }
            if (x.getKey().equals(y.getKey()) && !oldValue.equals(newValue)) {
                if (!"update_time".equals(x.getKey())) {
                    textList.add("将" + propertiesMap.get(crmTypes).get(x.getKey()) + " 由" + oldValue + "修改为" + newValue + "。");
                }
            }
        }));
    }

    public R queryRecordList(String actionId, String crmTypes) {
        List<Record> recordList;
        if (CrmEnum.CUSTOMER_TYPE_KEY.getTypes().equals(crmTypes)) {
            recordList = Db.find("select a.*,b.realname,b.img from 72crm_crm_action_record a left join 72crm_admin_user b on a.create_user_id = b.user_id where action_id = ? " +
                            " and types in ("
                            + CrmEnum.CUSTOMER_TYPE_KEY.getTypes() + ","
                            + CrmEnum.CUSTOMER_DISTRIBUTE_KEY.getTypes() + ","
                            + CrmEnum.CUSTOMER_UPLOAD_BY_EXCEL.getTypes() + ","
                            + CrmEnum.DISTRIBUTE_KEY.getTypes() +
                            ") order by create_time desc",
                    actionId);
        } else {
            recordList = Db.find("select a.*,b.realname,b.img from 72crm_crm_action_record a left join 72crm_admin_user b on a.create_user_id = b.user_id where action_id = ? and types = ? order by create_time desc", actionId, crmTypes);
        }
        recordList.forEach(record -> {
            List<String> list = JSON.parseArray(record.getStr("content"), String.class);
            record.set("content", list);
        });
        return R.ok().put("data", recordList);
    }

    /**
     * 添加转移记录
     *
     * @param actionId
     * @param crmTypes
     */
    public void addConversionRecord(Integer actionId, String crmTypes, Integer userId) {
        String name = Db.queryStr("select realname from 72crm_admin_user where user_id = ?", userId);
        CrmActionRecord crmActionRecord = new CrmActionRecord();
        crmActionRecord.setCreateUserId(Objects.nonNull(BaseUtil.getUserId()) ? BaseUtil.getUserId().intValue() : null);
        crmActionRecord.setCreateTime(new Date());
        crmActionRecord.setTypes(crmTypes);
        crmActionRecord.setActionId(actionId);
        ArrayList<String> strings = new ArrayList<>();
        strings.add("将" + CrmEnum.getName(crmTypes) + "分派给：" + name);
        crmActionRecord.setContent(JSON.toJSONString(strings));
        crmActionRecord.save();
    }
    /**
     * 线索转化客户
     *
     * @param actionId
     * @param crmTypes
     */
    public void addConversionCustomerRecord(Integer actionId, String crmTypes,String name) {
        CrmActionRecord crmActionRecord = new CrmActionRecord();
        crmActionRecord.setCreateUserId(Math.toIntExact(BaseUtil.getUserId()));
        crmActionRecord.setCreateTime(new Date());
        crmActionRecord.setTypes(crmTypes);
        crmActionRecord.setActionId(actionId);
        ArrayList<String> strings = new ArrayList<>();
        strings.add("将线索\""+name+"\"转化为客户");
        crmActionRecord.setContent(JSON.toJSONString(strings));
        crmActionRecord.save();
    }
    /**
     * 放入公海
     *
     * @param actionIds
     * @param crmTypes
     */
    public void addPutIntoTheOpenSeaRecord(Collection actionIds, String crmTypes,String content) {
        CrmActionRecord crmActionRecord = new CrmActionRecord();
        if(BaseUtil.getRequest() == null){
            crmActionRecord.setCreateUserId(BaseConstant.SUPER_ADMIN_USER_ID.intValue());
        }else {
            crmActionRecord.setCreateUserId(Objects.nonNull(BaseUtil.getUserId()) ? BaseUtil.getUserId().intValue() : null);
        }
        crmActionRecord.setCreateTime(new Date());
        crmActionRecord.setTypes(crmTypes);
        ArrayList<String> strings = new ArrayList<>();
        strings.add(content);
        crmActionRecord.setContent(JSON.toJSONString(strings));
        for(Object actionId : actionIds){
            crmActionRecord.remove("id");
            crmActionRecord.setActionId(((Long) actionId).intValue());
            crmActionRecord.save();
        }
    }

    /**
     * 添加分配客户记录
     *
     * @param actionId
     * @param crmTypes
     */
    public void addDistributionRecord(String actionId, String crmTypes, Long userId) {
        CrmActionRecord crmActionRecord = new CrmActionRecord();
        for(String id : actionId.split(",")){
            if(StrUtil.isEmpty(id)){
                continue;
            }
            ArrayList<String> strings = new ArrayList<>();
            String name = Db.queryStr("select realname from 72crm_admin_user where user_id = ?", userId);

            crmActionRecord.clear();
            crmActionRecord.setCreateUserId(Objects.nonNull(BaseUtil.getUserId()) ? BaseUtil.getUserId().intValue() : null);
            crmActionRecord.setCreateTime(new Date());
            crmActionRecord.setTypes(crmTypes);
            crmActionRecord.setActionId(Integer.valueOf(id));
            if(userId == null){
                //领取
                strings.add("领取了客户");
            }else {
                //管理员分配
                strings.add("将客户分配给：" + name);
            }
            crmActionRecord.setContent(JSON.toJSONString(strings));
            crmActionRecord.save();
        }
    }  /**
     * 添加分配客户记录
     *
     * @param actionId
     * @param crmTypes
     */
    public void addDistributionRecord(String actionId, String crmTypes,Long ownerId, Long userId) {
        CrmActionRecord crmActionRecord = new CrmActionRecord();
        for(String id : actionId.split(",")){
            if(StrUtil.isEmpty(id)){
                continue;
            }
            ArrayList<String> strings = new ArrayList<>();
            String name = Db.queryStr("select realname from 72crm_admin_user where user_id = ?", userId);

            crmActionRecord.clear();
            crmActionRecord.setCreateUserId(ownerId.intValue());
            crmActionRecord.setCreateTime(new Date());
            crmActionRecord.setTypes(crmTypes);
            crmActionRecord.setActionId(Integer.valueOf(id));
            if(userId == null){
                //领取
                strings.add("领取了客户");
            }else {
                //管理员分配
                strings.add("将客户分配给：" + name);
            }
            crmActionRecord.setContent(JSON.toJSONString(strings));
            crmActionRecord.save();
        }
    }

    public void addActionRecord(Integer createUserId, String type, Integer actionId, String... contents) {
        addActionRecord(createUserId, type, actionId, null, contents);
    }

    public void addActionRecord(Integer createUserId, String type, Integer actionId,Date createTime, String... contents) {
        CrmActionRecord record = new CrmActionRecord();
        record.setCreateUserId(createUserId);
        record.setTypes(type);
        record.setActionId(actionId);
        if (Objects.nonNull(createTime)) {
            record.setCreateTime(createTime);
        }
        record.setContent(JSON.toJSONString(contents));
        record.save();
    }


    /**
     * @author wyq
     * 删除跟进记录
     */
    public R deleteFollowRecord(Integer recordId){
        return AdminRecord.dao.deleteById(recordId) ? R.ok() : R.error();
    }

    /**
     * 删除历史操作记录
     *
     * @param types
     * @param actionIds
     */
    public void deleteActionRecordsByTypeAndActionIds(String types, List<Long> actionIds) {
        if (StringUtils.isBlank(types)) {
            return;
        }
        if (CollectionUtils.isEmpty(actionIds)) {
            return;
        }
        Db.update(Db.getSqlPara("crm.record.deleteActionRecordsByTypeAndActionIds",
                Kv.by("types", types).set("actionIds", actionIds)));
    }

    /**
     * 记录导入客户操作记录
     *  @param userId
     * @param customerId
     */
    public void saveUploadCustomerByExcelRecord(Long userId, Long customerId) {
        CrmActionRecord crmActionRecord = new CrmActionRecord();
        crmActionRecord.setCreateUserId(userId == null ? null : userId.intValue());
        crmActionRecord.setCreateTime(new Date());
        crmActionRecord.setTypes(CrmEnum.CUSTOMER_UPLOAD_BY_EXCEL.getTypes());
        crmActionRecord.setActionId(Objects.nonNull(customerId) ? customerId.intValue() : null);
        String content = "[\"通过excel导入客户\"]";
        crmActionRecord.setContent(content);
        crmActionRecord.save();
    }

}
