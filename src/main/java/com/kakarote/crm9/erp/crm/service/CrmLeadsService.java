package com.kakarote.crm9.erp.crm.service;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jfinal.aop.Before;
import com.jfinal.aop.Inject;
import com.jfinal.kit.Kv;
import com.jfinal.log.Log;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.kakarote.crm9.common.config.paragetter.BasePageRequest;
import com.kakarote.crm9.common.midway.NotifyService;
import com.kakarote.crm9.common.theadpool.CrmThreadPool;
import com.kakarote.crm9.erp.admin.common.AdminEnum;
import com.kakarote.crm9.erp.admin.common.CustomerIndustryEnum;
import com.kakarote.crm9.erp.admin.entity.AdminIndustryOfDept;
import com.kakarote.crm9.erp.admin.service.*;
import com.kakarote.crm9.erp.crm.common.*;
import com.kakarote.crm9.erp.crm.common.customer.FromSourceEnum;
import com.kakarote.crm9.erp.crm.constant.CrmConstant;
import com.kakarote.crm9.erp.crm.constant.CrmEmailConstant;
import com.kakarote.crm9.erp.crm.constant.CrmTagConstant;
import com.kakarote.crm9.erp.crm.entity.*;
import com.kakarote.crm9.utils.BaseUtil;
import com.kakarote.crm9.utils.FieldUtil;
import com.kakarote.crm9.utils.R;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

import java.io.StringWriter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 线索服务类
 * @author honglei.wan
 */
public class CrmLeadsService {
    @Inject
    private AdminFieldService adminFieldService;

    @Inject
    private FieldUtil fieldUtil;

    @Inject
    private CrmRecordService crmRecordService;

    @Inject
    private AdminFileService adminFileService;

    @Inject
    private CrmParamValid crmParamValid;

    @Inject
    private CrmBatchRecordService crmBatchRecordService;

    @Inject
    private AdminDataDicService adminDataDicService;

    @Inject
    private CommentService commentService;
    @Inject
    private CrmNotesService crmNotesService;

    @Inject
    private AdminDeptService adminDeptService;

    @Inject
    private CrmCustomerService crmCustomerService;

    @Inject
    private CrmPrivateTagService crmPrivateTagService;

    @Inject
    private AdminIndustryOfDeptService adminIndustryOfDeptService;

    private Log logger = Log.getLog(getClass());

    private static final String CUSTOMER_CHANGE_CHANNEL_EVENT = "from_channel_event";

    private static final String CUSTOMER_CHANGE_HISTORY = "change_history";

    /**
     * @author wyq
     * 分页条件查询线索
     */
    public Page<Record> getLeadsPageList(BasePageRequest<CrmLeads> basePageRequest) {
        String leadsName = basePageRequest.getData().getLeadsName();
        if (!crmParamValid.isValid(leadsName)){
            return new Page<>();
        }
        String telephone = basePageRequest.getData().getTelephone();
        String mobile = basePageRequest.getData().getMobile();
        if (StrUtil.isEmpty(leadsName) && StrUtil.isEmpty(telephone) && StrUtil.isEmpty(mobile)){
            return new Page<>();
        }
        return Db.paginate(basePageRequest.getPage(), basePageRequest.getLimit(), Db.getSqlPara("crm.leads.getLeadsPageList",Kv.by("leadsName",leadsName).set("telephone",telephone).set("mobile",mobile)));
    }

    /**
     * @author wyq
     * 新增或更新线索
     */
    @Before(Tx.class)
    public R addOrUpdate(JSONObject object, NotifyService notifyService, VelocityEngine velocityEngine, Long userId) {
        logger.info(String.format("leads addOrUpdate方法json %s",object.toJSONString()));
        CrmLeads crmLeads = object.getObject("entity", CrmLeads.class);
        if(crmLeads.getStr("dept_id") == null || "".equals(crmLeads.getStr("dept_id"))){
            crmLeads.setDeptId(null);
        }
        String batchId = StrUtil.isNotEmpty(crmLeads.getBatchId()) ? crmLeads.getBatchId() : IdUtil.simpleUUID();

        JSONArray jsonArray = object.getJSONArray("field");
        crmRecordService.updateRecord(jsonArray, crmLeads.getBatchId());
        adminFieldService.save(jsonArray, batchId);
        /*校验公司名称或者联系人是否存在*/
        Record companyRecord;
        Record telephoneRecord;
        if(crmLeads.getCompany() != null && !"".equals(crmLeads.getCompany())){
            companyRecord =  queryByCompany(crmLeads.getCompany());
            if(isExists(String.valueOf(crmLeads.getLeadsId()),companyRecord)){
                return getCheckInfo(companyRecord);
            }
        }
        if(crmLeads.getTelephone() != null && !"".equals(crmLeads.getTelephone())){
            telephoneRecord = queryByTelephone(crmLeads.getTelephone());
            if(isExists(String.valueOf(crmLeads.getLeadsId()),telephoneRecord)){
                return getCheckInfo(telephoneRecord);
            }
        }
        /*校验公司名称或者联系人是否存在*/

        if (crmLeads.getLeadsId() != null) {
            crmLeads.setCustomerId(0);
            crmLeads.setUpdateTime(DateUtil.date());
            crmLeads.setReceiveTime(DateUtil.date());
            crmRecordService.updateRecord(new CrmLeads().dao().findById(crmLeads.getLeadsId()), crmLeads, CrmEnum.LEADS_TYPE_KEY.getTypes(),userId);
            /*发送邮件*/
            if(crmLeads.getDeptId() != null && !"".equals(crmLeads.getDeptId())){
                sendEmail(notifyService,velocityEngine,crmLeads);
            }
            /*发送邮件*/
            return crmLeads.update() ? R.ok() : R.error();
        } else {
            crmLeads.setCreateTime(DateUtil.date());
            crmLeads.setUpdateTime(DateUtil.date());
            crmLeads.setReceiveTime(DateUtil.date());
            crmLeads.setCreateUserId(userId == null ? null:userId.intValue());
            crmLeads.setBatchId(batchId);
            crmLeads.setLeadsNo(crmBatchRecordService.getBatchNo(CrmConstant.LEADS_TYPE));
            boolean save = crmLeads.save();
            crmRecordService.addRecord(crmLeads.getLeadsId().intValue(), CrmEnum.LEADS_TYPE_KEY.getTypes(),userId);
            /*发送邮件*/
            if(crmLeads.getDeptId() != null && !"".equals(crmLeads.getDeptId())){
                sendEmail(notifyService,velocityEngine,crmLeads);
            }
            /*发送邮件*/
            return save ? R.ok() : R.error();
        }
    }

    /**
     * @author whl
     * 新增或更新线索（用于excel导入）
     */
    @Before(Tx.class)
    public void addOrUpdateForExcel(List<JSONObject> dataList, NotifyService notifyService, VelocityEngine velocityEngine, Long userId) {
        dataList.forEach(o -> {
            logger.info(String.format("leads addOrUpdateForExcel %s", o.toJSONString()));
            CrmLeads crmLeads = o.getObject("entity", CrmLeads.class);

            if (crmLeads.getLeadsId() != null) {
                crmLeads.setCustomerId(0);
                crmLeads.setReceiveTime(new Date());
                crmLeads.update();

                if(o.get("oldEntity") != null){
                    crmRecordService.updateRecord(o.getObject("oldEntity", CrmLeads.class), crmLeads, CrmEnum.LEADS_TYPE_KEY.getTypes(),userId);
                }
            } else {
                crmLeads.setReceiveTime(new Date());
                crmLeads.setCreateUserId(userId == null ? null:userId.intValue());
                crmLeads.setBatchId(IdUtil.simpleUUID());
                crmLeads.setLeadsNo(crmBatchRecordService.getBatchNo(CrmConstant.LEADS_TYPE));
                crmLeads.save();

                crmRecordService.addRecord(crmLeads.getLeadsId().intValue(), CrmEnum.LEADS_TYPE_KEY.getTypes(),userId);
            }

            /*发送邮件*/
            if(StringUtils.isNotBlank(crmLeads.getDeptId()) && !"外部获取".equals(crmLeads.getRequireDescription())){
                CrmThreadPool.INSTANCE.getInstance().execute(() -> sendEmail(notifyService,velocityEngine,crmLeads));
            }
        });
    }

    /**
     * 校验是否存在
     * @author yue.li
     * @param leadsId 线索ID
     * @param record  校验实体
     */
    public boolean isExists(String leadsId,Record record){
        boolean isExists = false;
        if(record != null){
            if(leadsId == null || "".equals(leadsId)){
                isExists = true;
            }else{
                if(!leadsId.equals(record.getStr("leads_id"))){
                    isExists = true;
                }
            }
        }
        return isExists;
    }

    /**
     * 校验是否存在,返回结果信息
     * @author yue.li
     * @param record record实体
     */
    public R getCheckInfo(Record record){
        if(record.getStr("dept_name") != null && !"".equals(record.getStr("dept_name"))){
            return R.error("该线索已存在，目前归属于"+ record.getStr("dept_name") +"事业部");
        }else if(record.getStr("owner_user_id") != null && !"".equals(record.getStr("owner_user_id"))){
            /*查找人所对应事业部*/
            Record ownUserRecord = Db.findFirst(Db.getSql("admin.user.queryUserByUserId"), record.getStr("owner_user_id"));
            Integer deptId = Integer.valueOf(adminDeptService.getBusinessDepartmentByDeptId(ownUserRecord.getStr("dept_id")));
            Record deptRecord = Db.findFirst(Db.getSql("admin.dept.queryDeptInfoByDeptId"), deptId);
            return R.error("该线索已存在，当前负责人为"+record.getStr("owner_user_name")+",目前归属于"+ deptRecord.getStr("name"));
        }else{
            return R.error("该线索已存在，目前归属于线索公海");
        }
    }

    /**
     * 发送邮件
     * @author yue.li
     * @param notifyService notifyService
     * @param leads 线索实体
     */
    public void sendEmail(NotifyService notifyService,VelocityEngine velocityEngines,CrmLeads leads){
    	//  改为获取事业部邮箱
        Record emailGroupRecord = Db.findFirst(Db.getSql("crm.leads.queryEmailType"), leads.getDeptId());
        List<String> emailGroup = new ArrayList<>();
        if(emailGroupRecord != null){
            emailGroup.add(emailGroupRecord.getStr("dept_email_group"));
        }
        JSONObject leadsJson = toJSON(leads);

        StringWriter result = new StringWriter();
        VelocityContext velocityContext = new VelocityContext(leadsJson);
        velocityEngines.mergeTemplate(CrmEmailConstant.LEADS_TEMPLATE, "UTF-8", velocityContext, result);

        notifyService.email(CrmEmailConstant.LEADS_TITLE,result.toString(),emailGroup);
    }

    private JSONObject toJSON(CrmLeads crmLeads) {
        JSONObject result = (JSONObject) JSON.toJSON(crmLeads);
        result.put("createTime", crmLeads.getCreateTime() == null ? crmLeads.getUpdateTime():crmLeads.getCreateTime());
        result.put("contacts", crmLeads.getContactUser());
        result.put("company",crmLeads.getCompany());
        result.put("accuracyRequirements",adminDataDicService.formatTagValueId(CrmTagConstant.ACCURACY_REQUIREMENTS,crmLeads.getAccuracyRequirements()));
        result.put("requireDescription",crmLeads.getRequireDescription());
        return result;
    }

    /**
     * @author wyq
     * 基本信息
     */
    public List<Record> information(Integer leadsId) {
        Record record = Db.findFirst(Db.getSql("crm.leads.queryLeadInfoByLeadId"), leadsId);
        if (null == record) {
            return null;
        }
        // 格式化标签数据
        String accuracyRequirements = adminDataDicService.formatTagValueId(CrmTagConstant.ACCURACY_REQUIREMENTS,record.getStr("accuracy_requirements"));
        String customerLevel = adminDataDicService.formatTagValueId(CrmTagConstant.CUSTOMER_GRADE,record.getStr("customer_level"));

        /*格式化地址*/
        if(CrmConstant.PROVINCE_CITY_AREA.equals(record.getStr("address"))){
            record.set("address","");
        }
        /*格式化地址*/
        List<Record> fieldList = new ArrayList<>();
        FieldUtil field = new FieldUtil(fieldList);
        field.set("联系人", "contactUser", record.getStr("contact_user"))
                .set("公司", "company", record.getStr("company"))
                .set("联系电话", "telephone", getSensitiveField("telephone", record))
                .set("所在部门", "contactDeptName", record.getStr("contact_dept_name"))
                .set("职位", "position", record.getStr("position"))
                .set("邮箱", "email", getSensitiveField("email", record))
                .set("微信", "wechat", getSensitiveField("we_chat", record))
                .set("精度需求", "accuracyRequirements", accuracyRequirements)
                .set("推荐事业部", "deptId", record.getStr("dept_name"))
                .set("需求描述", "requireDescription", record.getStr("require_description"))
                .set("省市区", "map_address", record.getStr("address"))
                .set("详细地址", "detailAddress", record.getStr("detail_address"))
                .set("公司规模", "customerLevel", customerLevel).set("行业", record.getStr("industryName"));
        List<Record> fields = adminFieldService.list("1");
        for (Record r:fields){
            field.set(r.getStr("name"),record.getStr(r.getStr("name")));
        }
        return fieldList;
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
     * @author wyq
     * 根据线索id查询
     */
    public Record queryById(Integer leadsId) {
        Record record = Db.findFirst(Db.getSql("crm.leads.queryLeadInfoByLeadId"), leadsId);
        if (Objects.nonNull(record)) {
            record.set("reason", getTagNameByTypeAndId(leadsId, CrmNoteEnum.CRM_LEADS_KEY.getTypes()));
            String deptId = record.getStr("dept_id");
            record.set("dept_id",StringUtils.isNotEmpty(deptId) ? Integer.valueOf(deptId) : null);

            // 如果事业部部门名称为空,取负责人部门名称
            if (StringUtils.isEmpty(deptId)) {
                String ownerUserDeptId = record.getStr("owner_user_dept_id");
                if (StringUtils.isNotEmpty(ownerUserDeptId)) {
                    Integer businessDeptId = Integer.valueOf(adminDeptService.getBusinessDepartmentByDeptId(ownerUserDeptId));
                    Record businessDepartmentRecord = Db.findFirst(Db.getSql("admin.dept.queryDeptInfoByDeptId"), businessDeptId);
                    if (Objects.nonNull(businessDepartmentRecord)) {
                        record.set("owner_dept_name", businessDepartmentRecord.getStr("name"));
                    }
                }
            }
        }
        return record;
    }

    /**
     * 根据业务类型和业务ID获取打标标签
     * @author yue.li
     * @param id 业务ID
     * @param type 业务类型
     * @return
     */
    public String getTagNameByTypeAndId(Integer id,String type) {
        StringBuilder tagName = null;
        Record privateRecord = Db.findFirst(Db.getSql("crm.privateTag.queryPrivateTagByTypeAndId"), id, type);
        if(privateRecord != null){
            List<CrmBaseTag> privateList = JSON.parseArray(privateRecord.get("content"),CrmBaseTag.class);
            if(privateList != null && privateList.size() >0) {
                for(CrmBaseTag tag:privateList){
                    if(tagName == null){
                        tagName = new StringBuilder(tag.getName());
                    }else{
                        tagName.append(',').append(tag.getName());
                    }
                }
            }
        }
        return tagName == null ? null : tagName.toString();
    }

    /**
     * @author wyq
     * 根据线索名称查询
     */
    public Record queryByName(String name) {
        return Db.findFirst(Db.getSql("crm.leads.queryByName"), name);
    }

    /**
     * @author yue.li
     * 根据公司名称查询
     */
    public Record queryByCompany(String company) {
        return Db.findFirst(Db.getSql("crm.leads.queryByCompany"), company);
    }

    /**
     * @author yue.li
     * 根据联系电话查询
     */
    public Record queryByTelephone(String telephone) {
        return Db.findFirst(Db.getSql("crm.leads.queryByTelephone"), telephone);
    }

    /**
     * @author wyq
     * 根据id 删除线索
     */
    public R deleteByIds(String leadsIds) {
        String[] idsArr = leadsIds.split(",");
        List<Record> idsList = new ArrayList<>(idsArr.length);
        for (String id : idsArr) {
            Record record = new Record();
            idsList.add(record.set("leads_id", Integer.valueOf(id)));
        }
        return Db.tx(() -> {
            Db.batch(Db.getSql("crm.leads.deleteByIds"), "leads_id", idsList, 100);
            return true;
        }) ? R.ok() : R.error();
    }

    /**
     * @author wyq
     * 变更负责人
     */
    public R updateOwnerUserId(String leadsIds, Integer ownerUserId) {
        String[] ids = leadsIds.split(",");
        logger.info(String.format("updateOwnerUserId方法json %s",leadsIds));
        int update = Db.update(Db.getSqlPara("crm.leads.updateOwnerUserId", Kv.by("ownerUserId", ownerUserId).set("ids", ids)));
        for (String id : ids) {
            crmRecordService.addConversionRecord(Integer.valueOf(id), CrmEnum.LEADS_TYPE_KEY.getTypes(), ownerUserId);
        }
        return update > 0 ? R.ok() : R.error();
    }

    /**
     * @author wyq
     * 线索转客户
     */
    @Before(Tx.class)
    public R translate(JSONObject object,Long userId) {
        logger.info(String.format("translate方法json %s",object.toJSONString()));
        CrmCustomer crmCustomer = object.getObject("entity", CrmCustomer.class);
        Long leadsId = crmCustomer.getLeadsId();
        Record crmLeads = Db.findFirst(Db.getSql("crm.leads.queryLeadInfoByLeadId"), leadsId);
        // 线索是否已转化
        if (CrmLeadsTransformEnum.LEADS_TRANSFORM_KEY.getTypes().equals(crmLeads.getInt("is_transform"))) {
            return R.error(CrmErrorInfo.LEADS_IS_TRANSFORM);
        }
        // 线索公司名称是否与客户公司名称重复
        List<Record> recordList = Db.find(Db.getSqlPara("crm.customer.queryCustomerInfo", Kv.by("customer_name", crmCustomer.getCustomerName())));
        if(CollectionUtils.isNotEmpty(recordList)){
            return R.error(CrmErrorInfo.LEADS_COMPANY_EXISTS);
        }
        object.put(CUSTOMER_CHANGE_CHANNEL_EVENT, CrmOperateChannelEventEnum.LEADS_TO_CUSTOMER.getName());
        object.put(CUSTOMER_CHANGE_HISTORY, "线索转客户");
        // 保存客户
        //设置客户来源
        crmCustomer.setFromSource(FromSourceEnum.FROM_LEADS.getCode());
        //默认放到考察库
        crmCustomer.setStorageType(CustomerStorageTypeEnum.INSPECT_CAP.getCode());
        object.put("entity",crmCustomer);

        R saveInfo = crmCustomerService.addOrUpdate(object,userId);
        if(Objects.nonNull(saveInfo)){
            if(CrmConstant.SUCCESS.equals(saveInfo.get("code"))) {
                if(Objects.nonNull(saveInfo.get("data"))){
                    JSONObject resultJsonObject = (JSONObject) JSON.toJSON(saveInfo.get("data"));
                    crmCustomer.setCustomerId(Objects.nonNull(resultJsonObject) ? Long.valueOf(resultJsonObject.get("customer_id").toString()) : null);
                }

            }else{
                return R.error(saveInfo.get("msg").toString());
            }
        }
        // 添加转化日志信息
        crmRecordService.addConversionCustomerRecord(Objects.nonNull(crmCustomer.getCustomerId()) ? crmCustomer.getCustomerId().intValue() : null, CrmEnum.CUSTOMER_TYPE_KEY.getTypes(), crmCustomer.getCustomerName());

        // 更新线索为已转化
        Db.update(Db.getSql("crm.leads.updateIsTransform"), DateUtil.date(), crmCustomer.getCustomerId(),leadsId);

        return R.ok().put("data", Kv.by("customer_id", crmCustomer.getCustomerId()).set("customer_name", crmCustomer.getCustomerName()).set("leadInfo", crmLeads));
    }

    /**
     * @author wyq
     * 查询新增字段
     */
    public List<Record> queryField() {
        List<Record> fieldList = new LinkedList<>();
        String[] settingArr = new String[]{};
        fieldUtil.getFixedField(fieldList, "contactUser", "联系人", "", "text", settingArr, 1);
        fieldUtil.getFixedField(fieldList, "company", "公司", "", "text", settingArr, 0);
        fieldUtil.getFixedField(fieldList, "telephone", "联系电话", "", "mobile", settingArr, 1);
        fieldUtil.getFixedField(fieldList, "contactDeptName", "所在部门", "", "text", settingArr, 0);
        fieldUtil.getFixedField(fieldList, "position", "职位", "", "text", settingArr, 0);
        fieldUtil.getFixedField(fieldList, "email", "邮箱", "", "email", settingArr, 0);
        fieldUtil.getFixedField(fieldList, "wechat", "微信", "", "text", settingArr, 0);
        fieldUtil.getFixedField(fieldList, "accuracyRequirements", "精度需求", "", "tag", settingArr, 0);
        fieldUtil.getFixedField(fieldList, "deptId", "推荐事业部", "", "recommendBusiness", settingArr, 1);
        fieldUtil.getFixedField(fieldList, "requireDescription", "需求描述", "", "textarea", settingArr, 1);
        Record map = new Record();
        fieldList.add(map.set("field_name", "map_address")
                .set("name", "地区定位")
                .set("form_type", "map_address")
                .set("is_null", 0));
        fieldUtil.getFixedField(fieldList, "detailAddress", "详细地址", "", "text", settingArr, 0);
        fieldUtil.getFixedField(fieldList, "customerLevel", "公司规模", "", "tag", settingArr, 0);
        //fieldUtil.getFixedField(fieldList, "customerQuality", "客户性质", "", "tag", settingArr, 0);
        fieldUtil.getFixedField(fieldList, "customerIndustry", "行业", "", "tag", settingArr, 0);
        fieldList.addAll(adminFieldService.list("1"));
        return fieldList.stream().distinct().collect(Collectors.toList());
    }

    /**
     * @author liyue
     * 查询导入字段字段
     */
    public List<Record> queryExcelField() {
        List<Record> fieldList = new LinkedList<>();
        String[] settingArr = new String[]{};
        fieldUtil.getFixedField(fieldList, "contactUser", "联系人", "", "text", settingArr, 1);
        fieldUtil.getFixedField(fieldList, "company", "公司", "", "text", settingArr, 0);
        fieldUtil.getFixedField(fieldList, "telephone", "联系电话", "", "mobile", settingArr, 0);
        fieldUtil.getFixedField(fieldList, "contactDeptName", "所在部门", "", "text", settingArr, 0);
        fieldUtil.getFixedField(fieldList, "position", "职位", "", "text", settingArr, 0);
        fieldUtil.getFixedField(fieldList, "email", "邮箱", "", "email", settingArr, 0);
        fieldUtil.getFixedField(fieldList, "weChat", "微信", "", "text", settingArr, 0);
        fieldUtil.getFixedField(fieldList, "accuracyRequirements", "精度需求", "", "tag", settingArr, 0);
        fieldUtil.getFixedField(fieldList, "ownerId", "负责人(邮箱前缀)", "", "text", settingArr, 0);
        fieldUtil.getFixedField(fieldList, "deptId", "推荐事业部", "", "recommendBusiness", settingArr, 0);
        fieldUtil.getFixedField(fieldList, "requireDescription", "需求描述", "", "text", settingArr, 1);
        fieldUtil.getFixedField(fieldList, "province", "省", "", "text", settingArr, 0);
        fieldUtil.getFixedField(fieldList, "city", "市", "", "text", settingArr, 0);
        fieldUtil.getFixedField(fieldList, "region", "区", "", "text", settingArr, 0);
        fieldUtil.getFixedField(fieldList, "detailAddress", "详细地址", "", "text", settingArr, 0);
        fieldUtil.getFixedField(fieldList, "customerLevel", "公司规模", "", "tag", settingArr, 0);
        fieldUtil.getFixedField(fieldList, "customerIndustry", "行业", "", "tag", settingArr, 0);
        return fieldList.stream().distinct().collect(Collectors.toList());
    }

    /**
     * @author liyue
     * 自定义字段过滤出部门信息
     */
    public List<Record> getDeptInfo( List<Record> fixedFieldList){
        List<Record> fixedFieldListAdd = new ArrayList<>();
        if(fixedFieldList != null && fixedFieldList.size() >0){
            for(Record record:fixedFieldList){
                if(record.get("formType").equals(CrmConstant.STRUCTURE)){
                    fixedFieldListAdd.add(record);
                }
            }
        }
        return fixedFieldListAdd;
    }
    /**
     * @author wyq
     * 查询编辑字段
     */
    public List<Record> queryField(Integer leadsId) {
        List<Record> fieldList = new LinkedList<>();
        Record leads = Db.findFirst(Db.getSql("crm.leads.queryLeadInfoByLeadId"), leadsId);
        String[] settingArr = new String[]{};
        Integer deptId = null;
        if(leads.getStr("dept_id") != null && !"".equals(leads.getStr("dept_id"))){
            deptId = Integer.valueOf(leads.getStr("dept_id"));
        }
        fieldUtil.getFixedField(fieldList, "contactUser", "联系人", leads.getStr("contact_user"), "text", settingArr, 1);
        fieldUtil.getFixedField(fieldList, "company", "公司", leads.getStr("company"), "text", settingArr, 0);
        fieldUtil.getFixedField(fieldList, "telephone", "联系电话", leads.getStr("telephone"), "text", settingArr, 1);
        fieldUtil.getFixedField(fieldList, "contactDeptName", "所在部门",  leads.getStr("contact_dept_name"), "text", settingArr, 0);
        fieldUtil.getFixedField(fieldList, "position", "职位", leads.getStr("position"), "text", settingArr, 0);
        fieldUtil.getFixedField(fieldList, "email", "邮箱", leads.getStr("email"), "text", settingArr, 0);
        fieldUtil.getFixedField(fieldList, "weChat", "微信", leads.getStr("we_chat"), "text", settingArr, 0);
        fieldUtil.getFixedField(fieldList, "accuracyRequirements", "精度需求", leads.getStr("accuracy_requirements"), "tag", settingArr, 0);
        fieldUtil.getFixedField(fieldList, "deptId", "推荐事业部", deptId, "recommendBusiness", settingArr, 1);
        fieldUtil.getFixedField(fieldList, "requireDescription", "需求描述", leads.getStr("require_description"), "textarea", settingArr, 1);
        Record map = new Record();
        fieldList.add(map.set("fieldName", "map_address")
                .set("name", "地区定位")
                .set("value", Kv.by("location", leads.getStr("location"))
                        .set("address", leads.getStr("address"))
                        .set("detailAddress", leads.getStr("detail_address"))
                        .set("lng", leads.getStr("lng"))
                        .set("lat", leads.getStr("lat")))
                .set("formType", "map_address")
                .set("isNull", 0));
        fieldList.addAll(adminFieldService.queryByBatchId(leads.getStr("batch_id")));
        fieldUtil.getFixedField(fieldList, "detailAddress", "详细地址", leads.getStr("detail_address"), "text", settingArr, 0);
        fieldUtil.getFixedField(fieldList, "customerLevel", "公司规模", leads.getStr("customer_level"), "tag", settingArr, 0);
        fieldUtil.getFixedField(fieldList, "customerIndustry", "行业", leads.getStr("customer_industry"), "tag", settingArr, 0);
        return fieldList;
    }


    /**
     * @author wyq
     * 线索导出
     */
    public List<Record> exportLeads(String leadsIds) {
        String[] leadsIdsArr = leadsIds.split(",");
        return Db.find(Db.getSqlPara("crm.leads.excelExport", Kv.by("ids", leadsIdsArr)));
    }

    /**
     * @author wyq
     * 获取线索导入查重字段
     */
    public R getCheckingField(){
        return R.ok().put("data","线索名称");
    }

    /**
     * @author liyue
     * 领取
     */
    public R receive(String leadsId) {
        logger.info(String.format("receive方法json %s",leadsId));
        Integer userId = Integer.valueOf(String.valueOf(BaseUtil.getUserId()));
        int update = Db.update(Db.getSqlPara("crm.leads.updateOwnerUserId", Kv.by("ownerUserId", userId).set("ids", leadsId)));
        crmRecordService.addConversionRecord(Integer.valueOf(leadsId), CrmEnum.LEADS_TYPE_KEY.getTypes(), userId);
        return update > 0 ? R.ok() : R.error();
    }

    /**
     * 根据线索ID获取线索场景
     * @author liyue
     * @param leadId
     * @return
     *
     */
    public Record getSemById(String leadId) {
        if(StringUtils.isEmpty(leadId)){
            return null;
        }
        Record record = queryById(Integer.valueOf(leadId));
        Record recordResult = new Record();
        if(record != null){
            String ownUserId = record.getStr("owner_user_id");
            String deptId = record.getStr("dept_id");
            Integer isTransform =  record.getInt("is_transform");
            if(CrmLeadsTransformEnum.LEADS_TRANSFORM_KEY.getTypes().equals(isTransform)){
                recordResult.set("name", AdminEnum.LEADS_TRANSFORM.getName());
                recordResult.set("value", AdminEnum.LEADS_TRANSFORM.getTypes());
                return recordResult;
            }else if(StringUtils.isEmpty(ownUserId) && StringUtils.isEmpty(deptId)){
                recordResult.set("name", AdminEnum.LEADS_PUBLIC_KEY.getName());
                recordResult.set("value", AdminEnum.LEADS_PUBLIC_KEY.getTypes());
                return recordResult;
            }else if(StringUtils.isNotEmpty(ownUserId) && StringUtils.isEmpty(deptId)){
                recordResult.set("name", AdminEnum.LEADS_OWN_KEY.getName());
                recordResult.set("value", AdminEnum.LEADS_OWN_KEY.getTypes());
                return recordResult;
            }else if(StringUtils.isEmpty(ownUserId) && (StringUtils.isNotEmpty(deptId))){
                recordResult.set("name", AdminEnum.LEADS_DEPT_KEY.getName());
                recordResult.set("value", AdminEnum.LEADS_DEPT_KEY.getTypes());
                return recordResult;
            }else{
                return recordResult;
            }
        }else{
            return recordResult;
        }
    }

    /**
     * @author yue.li
     * 线索保护规则设置
     */
    @Before(Tx.class)
    public R updateRulesSetting(Integer pullDeptPoolContactSubtotalDay,Integer pullDeptPoolNotTransformDay,Integer pullPublicPoolDay,Integer type) {
        Db.update(Db.getSql("crm.leads.updatePoolDay"), pullDeptPoolContactSubtotalDay, type,CrmConstant.LEADS_DEPT_POOL_CONTACT_SUBTOTAL_SETTING);
        Db.update(Db.getSql("crm.leads.updatePoolDay"), pullDeptPoolNotTransformDay,type, CrmConstant.LEADS_DEPT_POOL_NOT_TRANSFORM_SETTING);
        Db.update(Db.getSql("crm.leads.updatePoolDay"), pullPublicPoolDay,type, CrmConstant.LEADS_PUBLIC_POOL_SETTING);
        return R.ok();
    }

    /**
     * 获取客户保护规则设置
     * @author yue.li
     *
     */
    @Before(Tx.class)
    public R getRulesSetting() {
        Record deptPoolContactSubtotal = Db.findFirst(Db.getSql("crm.leads.adminConfigInfoByName"), CrmConstant.LEADS_DEPT_POOL_CONTACT_SUBTOTAL_SETTING);
        Record deptPoolNotTransform = Db.findFirst(Db.getSql("crm.leads.adminConfigInfoByName"), CrmConstant.LEADS_DEPT_POOL_NOT_TRANSFORM_SETTING);
        Record publicPool = Db.findFirst(Db.getSql("crm.leads.adminConfigInfoByName"), CrmConstant.LEADS_PUBLIC_POOL_SETTING);
        return R.ok().put("data",Kv.by("pullDeptPoolContactSubtotalDay",deptPoolContactSubtotal.getStr("value")).set("pullDeptPoolNotTransformDay",deptPoolNotTransform.getStr("value")).set("pullPublicPoolDay",publicPool.getStr("value")).set("type",publicPool.getStr("status")));
    }

    /**
     * 线索放入线索公海
     * @author yue.li
     * @param object 对象
     */
    @Before(Tx.class)
    public R pullLeadsPublicPool(JSONObject object,Long userId) {
        logger.info(String.format("pullLeadsPublicPool方法json %s",object.toJSONString()));
        CrmServiceTag serviceTag = object.getObject("entity", CrmServiceTag.class);
        String id = serviceTag.getId();
        crmPrivateTagService.addOrUpdate(constructCrmPrivateTag(serviceTag),userId);
        int update = Db.update(Db.getSqlPara("crm.leads.pullLeadsPublicPool", Kv.by("id", id)));
        return update > 0 ? R.ok() : R.error();
    }

    /***
     * 构造线索业务标签
     * @author yue.li
     * @param serviceTag 业务标签
     */
    public CrmPrivateTag constructCrmPrivateTag(CrmServiceTag serviceTag) {
        CrmPrivateTag crmPrivateTag = new CrmPrivateTag();
        crmPrivateTag.setEntityId(serviceTag.getId() == null ? null:Integer.valueOf(serviceTag.getId()));
        crmPrivateTag.setEntityType(CrmNoteEnum.CRM_LEADS_KEY.getTypes());
        crmPrivateTag.setContent(JSON.toJSONString(serviceTag.getTag()));
        return crmPrivateTag;
    }

    public R getWechatByLeadsId(String id) {
        return getSensitiveInformationFromLeads(id, "we_chat");
    }

    public R getTelephoneByLeadsId(String id) {
        return getSensitiveInformationFromLeads(id, "telephone");
    }

    public R getEmailByLeadsId(String id) {
        return getSensitiveInformationFromLeads(id, "email");
    }

    private R getSensitiveInformationFromLeads(String id, String fieldName) {
        if (id == null || id.isEmpty()) {
            return R.error("invalid leads id!");
        }
        Record record = queryById(Integer.valueOf(id.trim()));
        if (record != null) {
            return R.ok().put("data", record.getStr(fieldName));
        } else {
            return R.error("record not found");
        }
    }

    /**
     * 根据行业id获取推荐事业部
     * @author yue.li
     * @param industryId 行业ID
     */
    public R getDeptListByIndustryId(String industryId) {
        if (StringUtils.isNotEmpty(industryId)) {
            AdminIndustryOfDept adminIndustryOfDept = AdminIndustryOfDept.dao.findFirst(Db.getSql("crm.industryOfDept.getIndustryInfoByIndustryCode"), industryId);
            List<Record> list;
            if(Objects.nonNull(adminIndustryOfDept) && CustomerIndustryEnum.EXCLUSIVE.getCode().equals(adminIndustryOfDept.getIndustryType())) {
                list = Db.find(Db.getSql("crm.leads.getDeptListByIndustryId"),industryId);
            } else {
                list = Db.find(Db.getSql("admin.businessType.queryBusinessDeptList"));
            }
            return R.ok().put("data",list);
        } else {
            return R.ok();
        }
    }
}
