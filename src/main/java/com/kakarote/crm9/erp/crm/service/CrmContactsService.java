package com.kakarote.crm9.erp.crm.service;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.poi.excel.ExcelReader;
import cn.hutool.poi.excel.ExcelUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jfinal.aop.Before;
import com.jfinal.aop.Inject;
import com.jfinal.kit.Kv;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.SqlPara;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.jfinal.upload.UploadFile;
import com.kakarote.crm9.common.config.paragetter.BasePageRequest;
import com.kakarote.crm9.common.exception.CrmException;
import com.kakarote.crm9.erp.admin.service.AdminDataDicService;
import com.kakarote.crm9.erp.admin.service.AdminFieldService;
import com.kakarote.crm9.erp.crm.common.CrmEnum;
import com.kakarote.crm9.erp.crm.common.CrmErrorInfo;
import com.kakarote.crm9.erp.crm.common.CrmParamValid;
import com.kakarote.crm9.erp.crm.constant.CrmConstant;
import com.kakarote.crm9.erp.crm.constant.CrmTagConstant;
import com.kakarote.crm9.erp.crm.entity.CrmContacts;
import com.kakarote.crm9.erp.crm.entity.CrmContactsBusiness;
import com.kakarote.crm9.erp.crm.entity.CrmDistributorPromotionRelation;
import com.kakarote.crm9.erp.crm.entity.CrmLeads;
import com.kakarote.crm9.utils.FieldUtil;
import com.kakarote.crm9.utils.R;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
public class CrmContactsService {
    @Inject
    private AdminFieldService adminFieldService;

    @Inject
    private FieldUtil fieldUtil;

    @Inject
    private CrmRecordService crmRecordService;

    @Inject
    private CrmParamValid crmParamValid;

    @Inject
    private AdminDataDicService adminDataDicService;

    @Inject
    private CrmDistributorPromotionRelationService crmDistributorPromotionRelationService;

    /**
     * @author wyq
     * 分页条件查询联系人
     */
    public Page<Record> queryList(BasePageRequest<CrmContacts> basePageRequest) {
        String contactsName = basePageRequest.getData().getName();
        String telephone = basePageRequest.getData().getTelephone();
        String mobile = basePageRequest.getData().getMobile();
        String customerName = basePageRequest.getData().getCustomerName();
        if (!crmParamValid.isValid(customerName)) {
            return new Page<>();
        }
        if (StrUtil.isEmpty(contactsName) && StrUtil.isEmpty(telephone) && StrUtil.isEmpty(mobile)) {
            return new Page<>();
        }
        return Db.paginate(basePageRequest.getPage(), basePageRequest.getLimit(), Db.getSqlPara("crm.contact.getContactsPageList",
                Kv.by("contactsName", contactsName).set("customerName", customerName).set("telephone", telephone).set("mobile", mobile)));
    }

    /**
     * @author wyq
     * 根据id查询联系人
     */
    public Record queryById(Integer contactsId) {
        return Db.findFirst(Db.getSql("crm.contact.queryById"), contactsId);
    }

    /**
     * @author wyq
     * 基本信息
     */
    public List<Record> information(Integer contactsId) {
        Record record = Db.findFirst("select * from contactsview where contacts_id = ?", contactsId);
        if (null == record) {
            return null;
        }
        String role = adminDataDicService.formatTagValueId(CrmTagConstant.CUSTOMER_ROLE, record.getStr("role"));
        List<Record> fieldList = new ArrayList<>();
        FieldUtil field = new FieldUtil(fieldList);

        field.set("姓名", "name",record.getStr("name"))
                .set("客户名称", "customerId",record.getStr("customer_name"))
                .set("职务", "post",record.getStr("post"))
                .set("手机", "mobile",getSensitiveField("mobile", record))
                .set("电话", "telephone",getSensitiveField("telephone", record))
                .set("邮箱", "email",getSensitiveField("email", record))
                .set("微信", "wechat",getSensitiveField("wechat", record))
                .set("态度", "attitude",record.getStr("attitude"))
                .set("角色", "role",role)
                .set("兴趣爱好", "hobby",record.getStr("hobby"))
                .set("备注", "remark",record.getStr("remark"));
        List<Record> fields = adminFieldService.list("3");
        for (Record r : fields) {
            field.set(r.getStr("name"), record.getStr(r.getStr("name")));
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
     * 根据联系人名称查询
     */
    public Record queryByName(String name) {
        return Db.findFirst(Db.getSql("crm.contact.queryByName"), name);
    }

    /**
     * @author wyq
     * 根据联系人id查询商机
     */
    public R queryBusiness(BasePageRequest<CrmContacts> basePageRequest) {
        Integer contactsId = basePageRequest.getData().getContactsId().intValue();
        Integer pageType = basePageRequest.getPageType();
        if (0 == pageType) {
            return R.ok().put("data", Db.find(Db.getSql("crm.contact.queryBusiness"), contactsId));
        } else {
            return R.ok().put("data", Db.paginate(basePageRequest.getPage(), basePageRequest.getLimit(), new SqlPara().setSql(Db.getSql("crm.contact.queryBusiness")).addPara(contactsId)));
        }
    }

    /**
     * @author wyq
     * 联系人关联商机
     */
    public R relateBusiness(CrmContactsBusiness crmContactsBusiness) {
        return crmContactsBusiness.save() ? R.ok() : R.error();
    }

    /**
     * @author wyq
     * 联系人解除关联商机
     */
    public R unrelateBusiness(Integer id) {
        return CrmContactsBusiness.dao.deleteById(id) ? R.ok() : R.error();
    }

    /**
     * @author wyq
     * 新建或更新联系人
     */
    @Before(Tx.class)
    public R addOrUpdate(JSONObject jsonObject, Long userId) {
        CrmContacts crmContacts = jsonObject.getObject("entity", CrmContacts.class);
        String batchId = StrUtil.isNotEmpty(crmContacts.getBatchId()) ? crmContacts.getBatchId() : IdUtil.simpleUUID();
        if (crmContacts.getMobile() == null || "".equals(crmContacts.getMobile())) {
            return R.error("联系人手机为空");
        }

        // 防止恶意拼接
        if(!Pattern.matches(CrmConstant.MOBILE_PATTERN,crmContacts.getMobile())){
            return R.error("联系人手机不合法");
        }
        if(StringUtils.isNotEmpty(crmContacts.getTelephone()) && !Pattern.matches(CrmConstant.MOBILE_PATTERN,crmContacts.getTelephone())) {
            return R.error("联系人电话不合法");
        }
        JSONArray field = jsonObject.getJSONArray("field");
        crmRecordService.updateRecord(field, batchId);
        adminFieldService.save(field, batchId);

        if (crmContacts.getContactsId() != null) {
            //查重规则：客户名称+手机号
            Record repeatField = Db.findFirst(Db.getSqlPara("crm.contact.queryRepeatFieldNumber",
                    Kv.by("customerId", crmContacts.getCustomerId()).set("mobile", crmContacts.getMobile()).set("contactsId",crmContacts.getContactsId())));
            Integer number = repeatField.getInt("number");
            if (number != 0) {
                return R.error("该客户下存在重复的手机号!");
            }

            crmContacts.setUpdateTime(DateUtil.date());
            crmRecordService.updateRecord(new CrmContacts().dao().findById(crmContacts.getContactsId()), crmContacts, CrmEnum.CONTACTS_TYPE_KEY.getTypes(),userId);

            // 新建联系人，如果联系人手机与线索联系电话相同，将线索变为已转化
            updateLeadsStatus(crmContacts);

            //保存之前，检查联系人名称，如果为空则保存成 客户
            if (StringUtils.isBlank(crmContacts.getName())){
                crmContacts.setName("客户");
            }

            return crmContacts.update() ? R.ok() : R.error();
        } else {
            //查重规则：客户名称+手机号
            Record repeatField = Db.findFirst(Db.getSqlPara("crm.contact.queryRepeatFieldNumber",
                    Kv.by("customerId", crmContacts.getCustomerId()).set("mobile", crmContacts.getMobile())));
            Integer number = repeatField.getInt("number");
            if (number != 0) {
                return R.error("该客户下存在重复的手机号!");
            }
            crmContacts.setCreateTime(DateUtil.date());
            crmContacts.setUpdateTime(DateUtil.date());

            crmContacts.setCreateUserId(userId != null ? userId.intValue() : null);
            crmContacts.setOwnerUserId(userId != null ? userId.intValue() : null);

            crmContacts.setBatchId(batchId);

            // 新建联系人，如果联系人手机与线索联系电话相同，将线索变为已转化
            updateLeadsStatus(crmContacts);

            //保存之前，检查联系人名称，如果为空则保存成 客户
            if (StringUtils.isBlank(crmContacts.getName())){
                crmContacts.setName("客户");
            }

            boolean save = crmContacts.save();
            crmRecordService.addRecord(crmContacts.getContactsId().intValue(), CrmEnum.CONTACTS_TYPE_KEY.getTypes(),userId);
            return save ? R.ok() : R.error();
        }
    }


    /**
     * 新建联系人，如果联系人手机与线索联系电话相同，将线索变为已转化
     * @author yue.li
     * @param crmContacts 联系人对象
     */
    public void updateLeadsStatus(CrmContacts crmContacts) {
        // 新建联系人，如果联系人手机与线索联系电话相同，将线索变为已转化
        List<Record> leadsRecordList = Db.find(Db.getSqlPara("crm.leads.getLeadsPageList", Kv.by("telephone", crmContacts.getMobile())));
        if(CollectionUtils.isNotEmpty(leadsRecordList)) {
            List<CrmLeads> leadsList = leadsRecordList.stream().map(item -> new CrmLeads()._setAttrs(item.getColumns())).collect(Collectors.toList());
            if(CollectionUtils.isNotEmpty(leadsList)) {
                for(CrmLeads leads : leadsList) {
                    Db.update(Db.getSql("crm.leads.updateIsTransform"), DateUtil.date(), crmContacts.getCustomerId(),leads.getLeadsId());
                }
            }
        }
    }

    /**
     * @author wyq
     * 根据id删除联系人
     */
    public R deleteByIds(String contactsIds) {
        String[] idsArr = contactsIds.split(",");
        List<Record> idsList = new ArrayList<>(idsArr.length);
        for (String id : idsArr) {
            Record record = new Record();
            idsList.add(record.set("contacts_id", Integer.valueOf(id)));
        }
        return Db.tx(() -> {
            Db.batch(Db.getSql("crm.contact.deleteByIds"), "contacts_id", idsList, 100);
            return true;
        }) ? R.ok() : R.error();
    }

    /**
     * @author wyq
     * 联系人转移
     */
    public R transfer(CrmContacts crmContacts) {
        String[] contactsIdsArr = crmContacts.getContactsIds().split(",");
        int update = Db.update(Db.getSqlPara("crm.contact.transfer", Kv.by("ownerUserId", crmContacts.getNewOwnerUserId()).set("ids", contactsIdsArr)));
        for (String contactsId : contactsIdsArr) {
            crmRecordService.addConversionRecord(Integer.valueOf(contactsId), CrmEnum.CONTACTS_TYPE_KEY.getTypes(), crmContacts.getNewOwnerUserId());
        }
        return update > 0 ? R.ok() : R.error();
    }

    /**
     * 根据客户id变更负责人
     *
     * @param customerId  客户ID
     * @param ownerUserId 负责人ID
     * @author zzw
     */
    public boolean updateOwnerUserId(Integer customerId, Integer ownerUserId) {
        String sql = "update 72crm_crm_contacts set owner_user_id = " + ownerUserId + " where customer_id = " + customerId;
        int update = Db.update(sql);
        crmRecordService.addConversionRecord(customerId, CrmEnum.CUSTOMER_TYPE_KEY.getTypes(), ownerUserId);
        return update > 0;
    }

    /**
     * @author chaokun.ding
     * 查询新增字段
     */
    public List<Record> queryField() {
        List<Record> fieldList = new LinkedList<>();
        String[] settingArr = new String[]{};
        fieldUtil.getFixedField(fieldList, "name", "联系人姓名", "", "text", settingArr, 1);
        fieldUtil.getFixedField(fieldList, "customerId", "所属客户", "", "customer", settingArr, 1);
        fieldUtil.getFixedField(fieldList, "post", "职务", "", "text", settingArr, 0);
        fieldUtil.getFixedField(fieldList, "mobile", "手机", "", "mobile", settingArr, 1);
        fieldUtil.getFixedField(fieldList, "email", "邮箱", "", "email", settingArr, 0);
        fieldUtil.getFixedField(fieldList, "telephone", "办公电话", "", "text", settingArr, 0);
        fieldUtil.getFixedField(fieldList, "wechat", "微信", "", "text", settingArr, 0);
        fieldUtil.getFixedField(fieldList, "attitude", "态度", "", "text", settingArr, 0);
        fieldUtil.getFixedField(fieldList, "role", "角色", "", "role", settingArr, 1);
        fieldUtil.getFixedField(fieldList, "hobby", "兴趣爱好", "", "textarea", settingArr, 0);
        fieldUtil.getFixedField(fieldList, "remark", "备注", "", "textarea", settingArr, 0);
        fieldList.addAll(adminFieldService.list("3"));
        return fieldList;
    }

    /**
     * @author wyq
     * 查询编辑字段
     */
    public List<Record> queryField(Integer contactsId) {
        List<Record> fieldList = new LinkedList<>();
        Record record = Db.findFirst("select * from contactsview where contacts_id = ?", contactsId);
        String[] settingArr = new String[]{};
        fieldUtil.getFixedField(fieldList, "name", "联系人姓名", record.getStr("name"), "text", settingArr, 1);
        List<Record> customerList = new ArrayList<>();
        Record customer = new Record();
        customerList.add(customer.set("customer_id", record.getInt("customer_id")).set("customer_name", record.getStr("customer_name")));
        fieldUtil.getFixedField(fieldList, "customerId", "所属客户", customerList, "customer", settingArr, 1);
        fieldUtil.getFixedField(fieldList, "post", "职务", record.getStr("post"), "text", settingArr, 0);
        fieldUtil.getFixedField(fieldList, "mobile", "手机", record.getStr("mobile"), "mobile", settingArr, 0);
        fieldUtil.getFixedField(fieldList, "email", "邮箱", record.getStr("email"), "email", settingArr, 0);
        fieldUtil.getFixedField(fieldList, "telephone", "办公电话", record.getStr("telephone"), "text", settingArr, 0);
        fieldUtil.getFixedField(fieldList, "wechat", "微信", record.getStr("wechat"), "text", settingArr, 0);
        fieldUtil.getFixedField(fieldList, "attitude", "态度", record.getStr("attitude"), "text", settingArr, 0);
        fieldUtil.getFixedField(fieldList, "role", "角色", record.getStr("role"), "role", settingArr, 1);
        fieldUtil.getFixedField(fieldList, "hobby", "兴趣爱好", record.getStr("hobby"), "textarea", settingArr, 0);
        fieldUtil.getFixedField(fieldList, "remark", "备注", record.getStr("remark"), "textarea", settingArr, 0);
        fieldList.addAll(adminFieldService.queryByBatchId(record.getStr("batch_id")));
        return fieldList;
    }


    /**
     * @author wyq
     * 联系人导出
     */
    public List<Record> exportContacts(String contactsIds) {
        String[] contactsIdsArr = contactsIds.split(",");
        return Db.find(Db.getSqlPara("crm.contact.excelExport", Kv.by("ids", contactsIdsArr)));
    }

    /**
     * @author wyq
     * 获取联系人导入查重字段
     */
    public R getCheckingField() {
        return R.ok().put("data", "联系人姓名,电话,手机");
//        return R.ok().put("data",Db.getSql("crm.contacts.getCheckingField"));
    }

    /**
     * @author wyq
     * 导入联系人
     */
    @Before(Tx.class)
    public R uploadExcel(UploadFile file, Long userId) {
        try (ExcelReader reader = ExcelUtil.getReader(FileUtil.file(file.getUploadPath() + "\\" + file.getFileName()))) {
            List<List<Object>> read = reader.read();
            List<Object> list = read.get(0);
            AdminFieldService adminFieldService = new AdminFieldService();
            Kv kv = new Kv();
            for (int i = 0; i < list.size(); i++) {
                kv.set(list.get(i), i);
            }
            List<Record> recordList = adminFieldService.list("3");
            List<Record> fieldList = queryField();
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
                R status;
                JSONObject object = new JSONObject();
                StringBuilder errorMsg = new StringBuilder();
                for (int i = 1; i < read.size(); i++) {
                    List<Object> contactsList = read.get(i);
                    if (contactsList.size() < list.size()) {
                        for (int j = contactsList.size() - 1; j < list.size(); j++) {
                            contactsList.add(null);
                        }
                    }
                    String contactsName = contactsList.get(kv.getInt("联系人姓名(*)")) != null ? contactsList.get(kv.getInt("联系人姓名(*)")).toString() : null;
                    String customerName = contactsList.get(kv.getInt("所属客户(*)")) != null ? contactsList.get(kv.getInt("所属客户(*)")).toString() : null;
                    String post = contactsList.get(kv.getInt("职务")) != null ? contactsList.get(kv.getInt("职务")).toString() : null;
                    String mobile = contactsList.get(kv.getInt("手机(*)")) != null ? contactsList.get(kv.getInt("手机(*)")).toString() : null;
                    String email = contactsList.get(kv.getInt("邮箱")) != null ? contactsList.get(kv.getInt("邮箱")).toString() : null;
                    String telephone = contactsList.get(kv.getInt("办公电话")) != null ? contactsList.get(kv.getInt("办公电话")).toString() : null;
                    String wechat = contactsList.get(kv.getInt("微信")) != null ? contactsList.get(kv.getInt("微信")).toString() : null;
                    String role = contactsList.get(kv.getInt("角色(*)")) != null ? contactsList.get(kv.getInt("角色(*)")).toString() : null;
                    String attitude = contactsList.get(kv.getInt("态度")) != null ? contactsList.get(kv.getInt("态度")).toString() : null;
                    String hobby = contactsList.get(kv.getInt("兴趣爱好")) != null ? contactsList.get(kv.getInt("兴趣爱好")).toString() : null;
                    String remark = contactsList.get(kv.getInt("备注")) != null ? contactsList.get(kv.getInt("备注")).toString() : null;

                    //校验必输项
                    if (StringUtils.isEmpty(contactsName)) {
                        errorMsg.append(CrmErrorInfo.CONTACTS_NAME_IS_NOT_NULL);
                        continue;
                    } else if (StringUtils.isEmpty(customerName)) {
                        errorMsg.append(CrmErrorInfo.CONTACTS_CUSTOMER_IS_NOT_NULL);
                        continue;
                    } else if (StringUtils.isEmpty(mobile)) {
                        errorMsg.append(CrmErrorInfo.CONTACTS_MOBILE_IS_NOT_NULL);
                        continue;
                    } else if (StringUtils.isEmpty(role)) {
                        errorMsg.append(CrmErrorInfo.CONTACTS_ROLE_IS_NOT_NULL);
                        continue;
                    }
                    //校验客户是否存在
                    Record fieldRecord = Db.findFirst(Db.getSqlPara("crm.customer.queryCustomerInfo", Kv.by("customer_name", customerName)));
                    Integer customerId = null;
                    if (fieldRecord == null) {
                        errorMsg.append('第').append(i).append("行【").append(customerName).append("】客户名称不存在,请修正Excel中的数据重新导入!");
                        continue;
                    } else {
                        customerId = fieldRecord.getInt("customer_id");
                    }
                    //校验角色是否存在
                    String roleId = adminDataDicService.formatTagValueName(CrmTagConstant.CUSTOMER_ROLE, role);
                    if (roleId == null || "".equals(roleId)) {
                        errorMsg.append('第').append(i).append("行【").append(role).append("】角色不存在,请修正Excel中的数据重新导入!");
                        continue;

                    }
                    //查重规则：客户+联系人
                    Record repeatField = Db.findFirst(Db.getSqlPara("crm.contact.queryRepeatFieldNumber", Kv.by("customerId", customerId).set("mobile", mobile)));
                    Integer number = repeatField.getInt("number");

                    if (0 == number) {
                        object.fluentPut("entity",
                                new JSONObject().fluentPut("name", contactsName)
                                        .fluentPut("customer_id", customerId)
                                        .fluentPut("post", post)
                                        .fluentPut("mobile", mobile)
                                        .fluentPut("email", email)
                                        .fluentPut("telephone", telephone)
                                        .fluentPut("wechat", wechat)
                                        .fluentPut("role", roleId)
                                        .fluentPut("attitude", attitude)
                                        .fluentPut("hobby", hobby)
                                        .fluentPut("remark", remark));
                    } else if (number == 1) {
                        Record contacts = Db.findFirst(Db.getSqlPara("crm.contact.queryRepeatField", Kv.by("customerId", customerId).set("mobile", mobile)));
                        object.fluentPut("entity",
                                new JSONObject().fluentPut("contacts_id", contacts.getInt("contacts_id"))
                                        .fluentPut("name", contactsName)
                                        .fluentPut("customer_id", customerId)
                                        .fluentPut("post", post)
                                        .fluentPut("mobile", mobile)
                                        .fluentPut("email", email)
                                        .fluentPut("telephone", telephone)
                                        .fluentPut("wechat", wechat)
                                        .fluentPut("role", roleId)
                                        .fluentPut("attitude", attitude)
                                        .fluentPut("hobby", hobby)
                                        .fluentPut("remark", remark)
                                        .fluentPut("batch_id", contacts.getStr("batch_id")));
                    }
                    JSONArray jsonArray = new JSONArray();
                    for (Record record : recordList) {
                        Integer columnsNum = kv.getInt(record.getStr("name")) != null ? kv.getInt(record.getStr("name")) : kv.getInt(record.getStr("name") + "(*)");
                        record.set("value", contactsList.get(columnsNum));
                        jsonArray.add(JSONObject.parseObject(record.toJson()));
                    }
                    object.fluentPut("field", jsonArray);
                    status = addOrUpdate(object, userId);
                    if ("500".equals(status.get("code"))) {
                        errorMsg.append('第').append(i).append("行保存报错:").append(status.get("msg"));
                    }
                }
                if (errorMsg.length() > 0) {
                    return R.error(errorMsg.toString());
                }
            }
        } catch (Exception e) {
            log.error("CrmContactsService uploadExcel error:", e);
            return R.error();
        }
        return R.ok();
    }

    public Record findByCustIdAndMobile(String custId, String mobile) {
        return Db.findFirst(Db.getSql("crm.contact.findByCustIdAndMobile"), custId, mobile);
    }

    public R getWechatByContactsId(String id) {
        return getSensitiveInformationFromContacts(id, "wechat");
    }

    public R getTelephoneByContactsId(String id) {
        return getSensitiveInformationFromContacts(id, "telephone");
    }

    public R getMobileByContactsId(String id) {
        return getSensitiveInformationFromContacts(id, "mobile");
    }

    public R getEmailByContactsId(String id) {
        return getSensitiveInformationFromContacts(id, "email");
    }

    private R getSensitiveInformationFromContacts(String id, String fieldName) {
        if (id == null || id.isEmpty()) {
            return R.error("invalid Contacts id!");
        }
        Record record = queryById(Integer.valueOf(id.trim()));
        if (record != null) {
            return R.ok().put("data", record.getStr(fieldName));
        } else {
            return R.error("record not found");
        }
    }

    /**
     * 封装权限联系人查询SQL
     * @author yue.li
     * @param authorizedUserIds 权限用户集合
     */
    public List<Integer> getAuthorizedContactsList(List<Integer> authorizedUserIds) {
        List<Integer> contactIds = new ArrayList<>();
        if(CollectionUtils.isNotEmpty(authorizedUserIds)){
            Kv kv = Kv.by("ownerUserIds", authorizedUserIds);
            SqlPara sqlPara = Db.getSqlPara("crm.contact.getAuthorizedContactList", kv);
            List<Record> recordList = Db.find(sqlPara);
            if(CollectionUtils.isNotEmpty(recordList)) {
                List<CrmContacts> crmContactsList = recordList.stream().map(item -> new CrmContacts()._setAttrs(item.getColumns())).collect(Collectors.toList());
                crmContactsList.forEach(crmContact -> contactIds.add(crmContact.getContactsId().intValue()));
            }
        }
        return contactIds;
    }

    /**
     * 根据手机号校验分销商
     * @param phone
     */
    public void checkDistributor(String phone) {
        CrmDistributorPromotionRelation relation = crmDistributorPromotionRelationService.findByMobile(phone);
        if (Objects.nonNull(relation)) {
            throw new CrmException(String.format("该手机号码所对应的用户已被分销商([%s])绑定，请输入其他号码", relation.getRealName()));
        }
    }

    /**
     * 根据客户ID和手机号进行校验
     * @param customerId
     * @param mobile
     * @return
     */
    public boolean exists(Long customerId, String mobile) {
        return getByCustomerIdAndMobile(customerId, mobile) != null;
    }

    /**
     * 根据客户ID和手机号查询
     * @param customerId
     * @param mobile
     * @return
     */
    public CrmContacts getByCustomerIdAndMobile(Long customerId, String mobile) {
        return CrmContacts.dao.findFirst(Db.getSql("crm.contact.findByCustIdAndMobile"), customerId, mobile);
    }

    public List<CrmContacts> getByMobile(String mobile) {
        return CrmContacts.dao.find(Db.getSql("crm.contact.getByMobile"), mobile);
    }
}
