package com.kakarote.crm9.erp.crm.service;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.util.TypeUtils;
import com.alibaba.rocketmq.shade.com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.jfinal.aop.Aop;
import com.jfinal.aop.Before;
import com.jfinal.aop.Inject;
import com.jfinal.kit.Kv;
import com.jfinal.log.Log;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.SqlPara;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.kakarote.crm9.common.config.paragetter.BasePageRequest;
import com.kakarote.crm9.common.constant.BaseConstant;
import com.kakarote.crm9.erp.admin.entity.AdminRecord;
import com.kakarote.crm9.erp.admin.entity.AdminUser;
import com.kakarote.crm9.erp.admin.service.AdminFileService;
import com.kakarote.crm9.erp.admin.service.AdminUserService;
import com.kakarote.crm9.erp.crm.acl.user.CrmUser;
import com.kakarote.crm9.erp.crm.common.CrmCustomerDateEnum;
import com.kakarote.crm9.erp.crm.common.CrmEnum;
import com.kakarote.crm9.erp.crm.common.CrmNotesChannelEnum;
import com.kakarote.crm9.erp.crm.common.scene.NotesSceneEnum;
import com.kakarote.crm9.erp.crm.constant.CrmConstant;
import com.kakarote.crm9.erp.crm.dto.CrmNearestRecordDto;
import com.kakarote.crm9.erp.crm.dto.CrmNotesPageRequest;
import com.kakarote.crm9.erp.crm.entity.CrmActionRecord;
import com.kakarote.crm9.erp.crm.entity.CrmBusiness;
import com.kakarote.crm9.erp.crm.entity.CrmContacts;
import com.kakarote.crm9.erp.crm.entity.CrmCustomer;
import com.kakarote.crm9.erp.crm.entity.CrmLeads;
import com.kakarote.crm9.utils.BaseUtil;
import com.kakarote.crm9.utils.CrmDateUtil;
import com.kakarote.crm9.utils.OssPrivateFileUtil;
import com.kakarote.crm9.utils.R;
import com.kakarote.crm9.utils.SceneUtil;
import com.kakarote.crm9.utils.TagUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * Crm notes service.
 *
 * @Author chaokun.ding
 */
public class CrmNotesService {

    private Log logger = Log.getLog(getClass());

    @Inject
    private AdminFileService adminFileService;

    @Inject
    private CommentService commentService;

    @Inject
    private CrmRecordService crmRecordService;

    @Inject
    private AdminUserService adminUserService;
    /**
     * @param object object
     * @return 响应结果
     * @author chaokun.ding
     */
    @Before(Tx.class)
    public R addOrUpdate(JSONObject object, Long userId) {
        AdminRecord adminRecord = object.toJavaObject(AdminRecord.class);
        adminRecord.setSendUserIds(StringUtils.isEmpty(adminRecord.getSendUserIds()) ? null : TagUtil.fromString(adminRecord.getSendUserIds()));

        String customerIds = adminRecord.getCustomerIds();
        String businessIds = adminRecord.getBusinessIds();
        String leadsIds = adminRecord.getLeadsIds();
        String contactsIds = adminRecord.getContactsIds();

        Integer actionId = null;
        String actionType = "";
        if (StringUtils.isNoneEmpty(customerIds)) {
            adminRecord.setTypes(CrmConstant.CRM_CUSTOMER);
            actionId = Integer.valueOf(customerIds);
            actionType = CrmEnum.CUSTOMER_TYPE_KEY.getTypes();
        } else if (StringUtils.isNoneEmpty(businessIds)) {
            adminRecord.setTypes(CrmConstant.CRM_BUSINESS);
            actionId = Integer.valueOf(adminRecord.getBusinessIds());
            actionType = CrmEnum.BUSINESS_TYPE_KEY.getTypes();
        } else if (StringUtils.isNoneEmpty(leadsIds)) {
            adminRecord.setTypes(CrmConstant.CRM_LEADS);
            actionId = Integer.valueOf(adminRecord.getLeadsIds());
            actionType = CrmEnum.LEADS_TYPE_KEY.getTypes();
        } else if (StringUtils.isNoneEmpty(contactsIds)) {
            adminRecord.setTypes(CrmConstant.CRM_CONTACTS);
            actionId = Integer.valueOf(adminRecord.getContactsIds());
            actionType = CrmEnum.CONTACTS_TYPE_KEY.getTypes();
        }

        if (Objects.nonNull(actionId)) {
            adminRecord.setTypesId(actionId);
            // add new notes
            if (Objects.isNull(adminRecord.getRecordId())) {
                Integer createUserId = userId == null ? null : userId.intValue();
                adminRecord.setCreateUserId(createUserId);
                adminRecord.setCreateTime(DateUtil.date());
                adminRecord.setUpdateTime(DateUtil.date());
                R result = adminRecord.save() ? crmRecordService.addCrmActionRecord(new CrmActionRecord(createUserId, actionType, actionId, CrmNotesChannelEnum.getName(adminRecord.getChannel()) + "新建了联系小记")) : R.error("新建联系小记失败!");
                result.put(CrmConstant.ADMIN_NOTES_ID_KEY, adminRecord.getRecordId());
                return result;
                // update notes
            } else {
                adminRecord.setUpdateTime(DateUtil.date());
                R result = adminRecord.update() ? R.ok() : R.error("编辑失败!");
                result.put(CrmConstant.ADMIN_NOTES_ID_KEY, adminRecord.getRecordId());
                return result;
            }
        } else {
            return R.error("请选择业务模板!");
        }
    }

    /**
     * 查询联系小计
     *
     * @param basePageRequest 分页参数
     * @param isPage 是否分页
     * @author chaokun.ding
     */
    public Page<Record> queryList(BasePageRequest<AdminRecord> basePageRequest, OssPrivateFileUtil ossPrivateFileUtil,boolean isPage) {
        Page<Record> recordList = new Page<>();
        JSONObject object = basePageRequest.getJsonObject();
        AdminUser user = BaseUtil.getUser();
        Integer by = TypeUtils.castToInt(object.getOrDefault("by", 4));
        Kv kv = Kv.by("by", by);
        List<Long> userIds;
        if (user.getRoles().contains(BaseConstant.SUPER_ADMIN_ROLE_ID)) {
            //超级管理员获取除自己之外的所有用户ids
            userIds = Db.query(Db.getSql("admin.user.getAllUserIds"));
        } else {
            //查询数据权限。
            userIds = Aop.get(AdminUserService.class).queryUserByAuth(user.getUserId());
        }
        //客户名称
        if (object.containsKey("customerName")) {
            kv.set("customer_name", object.get("customerName"));
        }
        //记录人
        if (object.containsKey("createUserId")) {
            kv.set("create_user_id", object.get("createUserId"));
        }
        //类型
        if (object.containsKey("type")) {
            kv.set("type", object.get("type"));
        }
        //提交时间
        if (object.containsKey("createTime") && object.containsKey("endTime")) {
            kv.set("create_time", object.get("createTime")).set("end_time", object.get("endTime"));
        }
        //联系方式
        if (object.containsKey("category")) {
            kv.set("category", object.get("category"));
        }
        //我发出的
        if (by == 1) {
            kv.set("create_user_id", user.getUserId());
            SqlPara sqlPara = Db.getSqlPara("crm.record.queryListByOwer", kv);
            if(isPage){
                recordList = Db.paginate(basePageRequest.getPage(), basePageRequest.getLimit(), sqlPara);
            }else{
                recordList.setList(Db.find(sqlPara));
            }
            //我收到的
        } else if (by == 2) {
            if (object.containsKey("deptId") && object.getInteger("deptId") != null) {
                //我收到的场景--部门查询的是发送给我的人所在的部门
                //查询数据权限
                List<Integer> deptIds = Aop.get(AdminUserService.class).queryMyDeptAndSubDeptId(object.getInteger("deptId"));
                kv.set("send_user_ids", user.getUserId()).set("dept_id", deptIds);
            } else {
                kv.set("send_user_ids", user.getUserId());
            }
            SqlPara sqlPara = Db.getSqlPara("crm.record.queryListBySendToMe", kv);
            if(isPage){
                recordList = Db.paginate(basePageRequest.getPage(), basePageRequest.getLimit(), sqlPara);
            }else{
                recordList.setList(Db.find(sqlPara));
            }
        } else {//全部
            //全部场景下的部门条件
            if (object.containsKey("deptId") && object.getInteger("deptId") != null) {
                //查询部门下的用户id
                //查询数据权限。
                List<Long> deptUserIds = Aop.get(AdminUserService.class).queryMyDeptAndSubUserByDeptId(object.getInteger("deptId"));
                //取我拥有权限能查看的用户id和部门下用户id的交集
                List<Long> longs = userIds.stream().filter(item -> deptUserIds.contains(item)).collect(Collectors.toList());
                //查询数据权限。
                List<Integer> deptIds = Aop.get(AdminUserService.class).queryMyDeptAndSubDeptId(object.getInteger("deptId"));

                //deptUserId:我能看的用户ids
                //send_user:抄送人
                //dept_id：下属所有部门id
                //查询的数据：记录人在这个部门+发送给我的人在这个部门
                kv.set("deptUserId", longs).set("send_user", user.getUserId()).set("dept_id", deptIds);
            } else {
                kv.set("send_user_ids", user.getUserId()).set("userIds", userIds);
            }
            SqlPara sqlPara = Db.getSqlPara("crm.record.queryListByAll", kv);
            if(isPage){
                recordList = Db.paginate(basePageRequest.getPage(), basePageRequest.getLimit(),sqlPara );
            }else{
                recordList.setList(Db.find(sqlPara));
            }
        }

        // 分页时获取全量信息
        // 不分页目前主要用于导出操作，只填充客户、线索、联系人及商机名称
        if (isPage) {
            recordList.getList().forEach((record -> {
                queryLogDetail(record, ossPrivateFileUtil);

                //填充通话记录
                this.queryCallRecord(record,basePageRequest);
            }));
        } else {
        	// 填充客户、线索、联系人及商机名称
        	this.queryLogDetailOptimizeIO(recordList);

        }
        return recordList;
    }

    /**
     * 查询小记关联的客户、商机、联系人、线索数据
     * 批量查询减少数据库IO消耗
     *
     * @param recordList
     */
    public void queryLogDetailOptimizeIO(Page<Record> recordList) {
    	// 准备数据
    	List<String> custmerIds = Lists.newArrayList();
    	List<String> businessIds = Lists.newArrayList();
    	List<String> contactsIds = Lists.newArrayList();
    	List<String> leadsIds = Lists.newArrayList();

    	recordList.getList().forEach((record -> {
    		if (StrUtil.isNotEmpty(record.getStr("customer_ids"))) {
    			custmerIds.addAll(Arrays.asList((record.getStr("customer_ids").split(","))));
    		}
    		if (StrUtil.isNotEmpty(record.getStr("business_ids"))) {
    			businessIds.addAll(Arrays.asList((record.getStr("business_ids").split(","))));
    		}
    		if (StrUtil.isNotEmpty(record.getStr("contacts_ids"))) {
    			contactsIds.addAll(Arrays.asList((record.getStr("contacts_ids").split(","))));
    		}
    		if (StrUtil.isNotEmpty(record.getStr("leads_ids"))) {
    			leadsIds.addAll(Arrays.asList((record.getStr("leads_ids").split(","))));
    		}
    	}));
    	// 过滤重复数据 + 批量查询客户、线索、联系人及商机数据
    	List<Long> customerIdsCondition = custmerIds.stream().map(str -> Long.valueOf(str))
    			.collect(Collectors.collectingAndThen(Collectors.toCollection(() -> new TreeSet<>(Comparator.comparing(o -> o))), ArrayList::new));
    	if (CollectionUtils.isEmpty(customerIdsCondition)) {
    		customerIdsCondition.add(-1L);
    	}
    	List<Record> customerRecords = Db.find(Db.getSqlPara("crm.customer.queryByIds", Kv.by("ids", customerIdsCondition)));

    	List<Long> businessIdsCondition = businessIds.stream().map(str -> Long.valueOf(str))
		.collect(Collectors.collectingAndThen(Collectors.toCollection(() -> new TreeSet<>(Comparator.comparing(o -> o))), ArrayList::new));
    	if (CollectionUtils.isEmpty(businessIdsCondition)) {
    		businessIdsCondition.add(-1L);
    	}
    	List<Record> businessRecords = Db.find(Db.getSqlPara("crm.business.queryByIds", Kv.by("ids", businessIdsCondition)));

    	List<Long> contactIdsCondition = contactsIds.stream().map(str -> Long.valueOf(str))
    			.collect(Collectors.collectingAndThen(Collectors.toCollection(() -> new TreeSet<>(Comparator.comparing(o -> o))), ArrayList::new));
    	if (CollectionUtils.isEmpty(contactIdsCondition)) {
    		contactIdsCondition.add(-1L);
    	}
    	List<Record> contactRecords = Db.find(Db.getSqlPara("crm.contact.queryByIds", Kv.by("ids", contactIdsCondition)));

    	List<Long> leadsIdsCondition = leadsIds.stream().map(str -> Long.valueOf(str))
    			.collect(Collectors.collectingAndThen(Collectors.toCollection(() -> new TreeSet<>(Comparator.comparing(o -> o))), ArrayList::new));
    	if (CollectionUtils.isEmpty(leadsIdsCondition)) {
    		leadsIdsCondition.add(-1L);
    	}
    	List<Record> leadsRecords = Db.find(Db.getSqlPara("crm.leads.queryByIds", Kv.by("ids", leadsIdsCondition)));

    	// 根据客户id、线索id、联系人id及商机id填充数据到recordList中
    	recordList.getList().forEach((record -> {

    		List<Record> customerList = Lists.newArrayList();
    		List<Record> businessList = Lists.newArrayList();
    		List<Record> contactList = Lists.newArrayList();
    		List<Record> leadsList = Lists.newArrayList();
    		//根据客户id填充customerList
    		if (StrUtil.isNotEmpty(record.getStr("customer_ids")) && record.getStr("customer_ids").split(",").length > 0) {
    			customerList =Arrays.stream(record.getStr("customer_ids").split(",")).map(customerId -> {
    				Optional<Record> opt = customerRecords.stream().filter(customerRecord -> customerRecord.getStr("customer_id").equals(customerId)).findFirst();
    				return opt.orElseGet(Record::new);
    			}).collect(Collectors.toList());
    		}

    		//根商机id填充businessList
    		if (StrUtil.isNotEmpty(record.getStr("business_ids")) && record.getStr("business_ids").split(",").length > 0) {
    			businessList =Arrays.stream(record.getStr("business_ids").split(",")).map(businessId -> {
    				Optional<Record> opt = businessRecords.stream().filter(businessRecord -> businessRecord.getStr("business_id").equals(businessId)).findFirst();
    				return opt.orElseGet(Record::new);
    			}).collect(Collectors.toList());
    		}

    		//根据联系人id填充contactList
    		if (StrUtil.isNotEmpty(record.getStr("contacts_ids")) && record.getStr("contacts_ids").split(",").length > 0) {
    			contactList =Arrays.stream(record.getStr("contacts_ids").split(",")).map(contactId -> {
    				Optional<Record> opt = contactRecords.stream().filter(contactRecord -> contactRecord.getStr("contacts_id").equals(contactId)).findFirst();
    				return opt.orElseGet(Record::new);
    			}).collect(Collectors.toList());
    		}

    		// 根据线索id填充leadsList
    		if (StrUtil.isNotEmpty(record.getStr("leads_ids")) && record.getStr("leads_ids").split(",").length > 0) {
    			leadsList =Arrays.stream(record.getStr("leads_ids").split(",")).map(leadsId -> {
    				Optional<Record> opt = leadsRecords.stream().filter(leadsRecord -> leadsRecord.getStr("leads_id").equals(leadsId)).findFirst();
    				return opt.orElseGet(Record::new);
    			}).collect(Collectors.toList());
    		}
    		record.set("customerList", (StrUtil.isNotEmpty(record.getStr("customer_ids")) && record.getStr("customer_ids").split(",").length > 0)
    				? customerList : new ArrayList<>());
            record.set("businessList", (StrUtil.isNotEmpty(record.getStr("business_ids")) && record.getStr("business_ids").split(",").length > 0)
            		? businessList : new ArrayList<>());
            record.set("contactsList", (StrUtil.isNotEmpty(record.getStr("contacts_ids")) && record.getStr("contacts_ids").split(",").length > 0)
            		? contactList : new ArrayList<>());
            record.set("leadsList", (StrUtil.isNotEmpty(record.getStr("leads_ids")) && record.getStr("leads_ids").split(",").length > 0)
            		? leadsList : new ArrayList<>());
        }));
    }

    public void queryLogDetail(Record record, OssPrivateFileUtil ossPrivateFileUtil) {
        adminFileService.queryByBatchId(record.get("batch_id"), record, ossPrivateFileUtil);
        record.set("sendUserList", (StrUtil.isNotEmpty(record.getStr("send_user_ids")) && record.getStr("send_user_ids").split(",").length > 0) ? Db.find(Db.getSqlPara("admin.user.queryByIds", Kv.by("ids", record.getStr("send_user_ids").split(",")))) : new ArrayList<>());
        record.set("sendDeptList", (StrUtil.isNotEmpty(record.getStr("send_dept_ids")) && record.getStr("send_dept_ids").split(",").length > 0) ? Db.find(Db.getSqlPara("admin.dept.queryByIds", Kv.by("ids", record.getStr("send_dept_ids").split(",")))) : new ArrayList<>());
        record.set("customerList", (StrUtil.isNotEmpty(record.getStr("customer_ids")) && record.getStr("customer_ids").split(",").length > 0) ? Db.find(Db.getSqlPara("crm.customer.queryByIds", Kv.by("ids", record.getStr("customer_ids").split(",")))) : new ArrayList<>());
        record.set("businessList", (StrUtil.isNotEmpty(record.getStr("business_ids")) && record.getStr("business_ids").split(",").length > 0) ? Db.find(Db.getSqlPara("crm.business.queryByIds", Kv.by("ids", record.getStr("business_ids").split(",")))) : new ArrayList<>());
        record.set("contactsList", (StrUtil.isNotEmpty(record.getStr("contacts_ids")) && record.getStr("contacts_ids").split(",").length > 0) ? Db.find(Db.getSqlPara("crm.contact.queryByIds", Kv.by("ids", record.getStr("contacts_ids").split(",")))) : new ArrayList<>());
        record.set("leadsList", (StrUtil.isNotEmpty(record.getStr("leads_ids")) && record.getStr("leads_ids").split(",").length > 0) ? Db.find(Db.getSqlPara("crm.leads.queryByIds", Kv.by("ids", record.getStr("leads_ids").split(",")))) : new ArrayList<>());
        record.set("createUser", Db.findFirst(Db.getSql("admin.user.queryUserByUserId"), record.getStr("create_user_id")));

        record.set("commentTotal", Db.queryInt(Db.getSql("crm.record.queryNumOfComment"), record.getStr("record_id"), 2));
        record.set("replyList", commentService.queryCommentList(record.getStr("record_id"), "2"));

    }

    /**
     * 查询通话记录
     * @param record
     */
    public void queryCallRecord(Record record ,BasePageRequest basePageRequest) {

    	Page<Record> records = Db.paginate(basePageRequest.getPage(), basePageRequest.getLimit(), Db.getSqlPara("crm.contractRecord.queryContactEditRelatedList",
                Kv.by("types", record.getStr("types"))
                        .set("typesId", record.getLong("types_id"))
                        .set("recordId",record.getLong("record_id"))
                        .set("recordType", 1)));//已关联通话记录

        record.set("haveCallRecord", records.getTotalRow()>0 ? true : false );
        record.set("callRecords", records);

    }

    /**
     * 根据id删除联系小计
     *
     * @param recordId 日志ID
     * @author chaokun.ding
     */
    @Before(Tx.class)
    public boolean deleteById(Integer recordId, OssPrivateFileUtil ossPrivateFileUtil, AdminUser user) {
        AdminRecord record = AdminRecord.dao.findById(recordId);
        if (record != null) {
            //删除联系小计
            Db.deleteById("72crm_admin_record", "record_id", recordId);
            //删除回复
            Db.deleteById("72crm_task_comment", "type_id", recordId);
            //删除附件
            adminFileService.removeByBatchId(record.getBatchId(), ossPrivateFileUtil, user.getRealname());

            //添加操作记录
            String types = record.getTypes();
            String type = "";
            if (CrmConstant.CRM_LEADS.equals(types)) {
                type = CrmEnum.LEADS_TYPE_KEY.getTypes();
            } else if (CrmConstant.CRM_CUSTOMER.equals(types)) {
                type = CrmEnum.CUSTOMER_TYPE_KEY.getTypes();
            } else if (CrmConstant.CRM_CONTACTS.equals(types)) {
                type = CrmEnum.CONTACTS_TYPE_KEY.getTypes();
            } else if (CrmConstant.CRM_BUSINESS.equals(types)) {
                type = CrmEnum.BUSINESS_TYPE_KEY.getTypes();
            }

            CrmActionRecord crmActionRecord = new CrmActionRecord(user.getUserId().intValue(), type, record.getTypesId(), "删除了联系小记");
            crmRecordService.addCrmActionRecord(crmActionRecord);
            return true;
        }
        return false;
    }

    /**
     * @author chaokun.ding
     * 业务模块下添加联系小计
     */
    @Before(Tx.class)
    public R addRecord(AdminRecord adminRecord, String types) {
        String type = "";
        adminRecord.setCreateUserId(adminRecord.getCreateUserId());
        adminRecord.setCreateTime(DateUtil.date());
        adminRecord.setUpdateTime(DateUtil.date());
        adminRecord.setTypes(types);

        if (CrmConstant.CRM_LEADS.equals(types)) {
            adminRecord.setLeadsIds(adminRecord.getTypesId().toString());
            type = CrmEnum.LEADS_TYPE_KEY.getTypes();
        } else if (CrmConstant.CRM_CUSTOMER.equals(types)) {
            adminRecord.setCustomerIds(adminRecord.getTypesId().toString());
            type = CrmEnum.CUSTOMER_TYPE_KEY.getTypes();
        } else if (CrmConstant.CRM_CONTACTS.equals(types)) {
            adminRecord.setContactsIds(adminRecord.getTypesId().toString());
            type = CrmEnum.CONTACTS_TYPE_KEY.getTypes();
        } else if (CrmConstant.CRM_BUSINESS.equals(types)) {
            adminRecord.setBusinessIds(adminRecord.getTypesId().toString());
            type = CrmEnum.BUSINESS_TYPE_KEY.getTypes();
        }

        //添加操作记录
        CrmActionRecord crmActionRecord = new CrmActionRecord(adminRecord.getCreateUserId(), type,
                adminRecord.getTypesId(), "新建了联系小记");

        crmRecordService.addCrmActionRecord(crmActionRecord);
        if(adminRecord.save()) {
        	//添加成功返回联系小计id
        	HashMap<String, Long> result = Maps.newHashMap();
        	result.put("recordId", adminRecord.getRecordId());
        	return R.ok().put("data", result);
        } else {
        	return R.error();
        }
    }

    public Page<Record> getRecord(BasePageRequest basePageRequest, OssPrivateFileUtil ossPrivateFileUtil, String type) {
        Long types = null;
        String noteContent = null;
        if (CrmConstant.CRM_LEADS.equals(type)) {
            CrmLeads crmLeads = (CrmLeads) basePageRequest.getData();
            types = crmLeads.getLeadsId();
        } else if (CrmConstant.CRM_CUSTOMER.equals(type)) {
            CrmCustomer crmCustomer = (CrmCustomer) basePageRequest.getData();
            types = crmCustomer.getCustomerId();
        } else if (CrmConstant.CRM_CONTACTS.equals(type)) {
            CrmContacts crmContacts = (CrmContacts) basePageRequest.getData();
            types = crmContacts.getContactsId();
        } else if (CrmConstant.CRM_BUSINESS.equals(type)) {
            CrmBusiness crmBusiness = (CrmBusiness) basePageRequest.getData();
            types = crmBusiness.getBusinessId();
            noteContent = crmBusiness.getNoteContent();
        }
        Kv kv = Kv.by("types_id", types).set("type", type).set("noteContent",noteContent);

        Page<Record> recordList = Db.paginate(basePageRequest.getPage(), basePageRequest.getLimit(), Db.getSqlPara("crm.record.queryListByType", kv));
        logger.info("联系小计数量：" + recordList.getPageSize());
        recordList.getList().forEach((record -> {
            this.queryLogDetail(record, ossPrivateFileUtil);
            //填充通话记录
            this.queryCallRecord(record,basePageRequest);
        }));
        return recordList;
    }

    /**
     * Get admin record by id.
     *
     * @param noteId note id
     * @return {@code Record}
     */
    public Record getNoteById(Integer noteId, OssPrivateFileUtil ossPrivateFileUtil) {
        AdminRecord record = AdminRecord.dao.findById(noteId);
        if (Objects.isNull(record)) {
            return new Record();
        }
        Record r = record.toRecord();
        queryLogDetail(r, ossPrivateFileUtil);
        return r;
    }

    /**
     * 获取联系小计列表(移动端兼容后期PC)
     * @author yue.li
     * @param notesRequest 请求参数对象
     * @param crmUser 登录对象
     */
    public Page<Record> getNotesList(BasePageRequest<CrmNotesPageRequest> notesRequest, CrmUser crmUser) {
        CrmNotesPageRequest request = notesRequest.getData();
        List<Integer> authorizedUserIds = SceneUtil.getAuthorizedUserIdsForBizScene(request.getBizType(), request.getSceneId(), crmUser);
        if(CollectionUtils.isEmpty(authorizedUserIds)){
            return new Page<>();
        }
        SqlPara sqlPara = prepareSqlParaNotesForList(request,authorizedUserIds,crmUser);
        return Db.paginate(notesRequest.getPage(), notesRequest.getLimit(), sqlPara);
    }

    /**
     * 封装联系小记查询SQL
     * @author yue.li
     * @param request 请求参数对象
     * @param authorizedUserIds 权限用户集合
     * @param crmUser 对象
     */
    public SqlPara prepareSqlParaNotesForList(CrmNotesPageRequest request,List<Integer> authorizedUserIds,CrmUser crmUser) {
        Kv kv = queryNotesParameters(request,authorizedUserIds,crmUser);
        if(Objects.isNull(kv)){
            return null;
        }
        SqlPara sqlPara = Db.getSqlPara("crm.record.getNotesList", kv);
        logger.info(String.format("prepareSqlParaNotesForList sql: %s", sqlPara));
        return sqlPara;
    }

    /**
     * 构建查询SQL查询条件
     * @author yue.li
     * @param request 请求参数对象
     * @param authorizedUserIds 权限用户集合
     * @param crmUser 客户对象
     */
    private Kv queryNotesParameters(CrmNotesPageRequest request,List<Integer> authorizedUserIds,CrmUser crmUser) {
        Kv kv = null;
        try{
            if(NotesSceneEnum.isOnlyMyReceive(request.getSceneId())){
                kv = Kv.by("onlyMyReceive", crmUser.getCrmAdminUser().getUserId());
            }else{
                kv = Kv.by("createUserIds", authorizedUserIds);
                // 包含我收到的
                if(NotesSceneEnum.isContainsMyReceive(request.getSceneId())){
                    kv.set("containsMyReceive",crmUser.getCrmAdminUser().getUserId());
                }
            }

            // 创建人域账号
            String createUserLoginIds = request.getCreateUserLoginIds();
            if(StringUtils.isNotEmpty(createUserLoginIds)){
                List<String> accounts = Arrays.asList(createUserLoginIds.split(","));
                List<Long> accountsCreateUserIds = adminUserService.getUserIdsByUserNames(accounts);
                if(CollectionUtils.isNotEmpty(accountsCreateUserIds)){
                    kv.set("accountsCreateUserIds",accountsCreateUserIds);
                }
            }

            // 创建时间按周查询
            if(CrmCustomerDateEnum.ONE_WEEK_KEY.getId().equals(request.getCreateTimeType())){
                kv.set("createTimeWeek",CrmDateUtil.getLastWeek());
            }
            if(CrmCustomerDateEnum.TWO_WEEK_KEY.getId().equals(request.getCreateTimeType())){
                kv.set("createTimeWeek",CrmDateUtil.getLastTwoWeek());
            }
            // 创建时间按月查询
            if(CrmCustomerDateEnum.ONE_MONTH_KEY.getId().equals(request.getCreateTimeType())){
                kv.set("createTimeMonth",CrmDateUtil.getLastOneMonth());
            }

			kv.putAll(JSONObject.parseObject(JSON.toJSONString(request)));
		} catch(Exception e){
            logger.error(String.format("queryNotesParameters exception: %s",BaseUtil.getExceptionStack(e)));
        }
        return kv;
    }

    /**
     * 根据联系小计ID获取该联系小计的签到信息
     * @author yue.li
     * @param noteId 联系小记ID
     */
    public Record getSignInfoByNoteId(Integer noteId) {
        Record record = Db.findFirst(Db.getSqlPara("crm.record.getSignInfoByNoteId", Kv.by("noteId", noteId)));
        if(Objects.nonNull(record)) {
            return record;
        }else{
            return null;
        }
    }

    /**
     * 移动端联系小计详情
     * @author yue.li
     * @param noteId 联系小记ID
     * @param ossPrivateFileUtil oss类对象
     */
    public Record getNoteMobileById(Integer noteId, OssPrivateFileUtil ossPrivateFileUtil) {
        Record record = getNoteById(noteId,ossPrivateFileUtil);
        if(Objects.nonNull(record)) {
            Record signInfoRecord = getSignInfoByNoteId(noteId);
            record.set("signInTime",Objects.nonNull(signInfoRecord) ? signInfoRecord.get("signInTime") : null);
            record.set("signAddress",Objects.nonNull(signInfoRecord) ? signInfoRecord.get("signAddress") : null);
            record.set("signInHistoryId",Objects.nonNull(signInfoRecord) ? signInfoRecord.get("signInHistoryId") : null);
            record.set("createUser",null);
        }
        return record;
    }

    /**
     * 封装权限联系小计查询SQL
     * @author yue.li
     * @param authorizedUserIds 权限用户集合
     * @param userId 登录人
     */
    public List<Integer> getAuthorizedNotesList(List<Integer> authorizedUserIds,Long userId) {
        List<Integer> noteIds = new ArrayList<>();
        if(CollectionUtils.isNotEmpty(authorizedUserIds)){
            Kv kv = Kv.by("createUserIds", authorizedUserIds).set("containsMyReceive",userId);
            SqlPara sqlPara = Db.getSqlPara("crm.record.getAuthorizedNoteList", kv);
            List<Record> recordList = Db.find(sqlPara);
            if(CollectionUtils.isNotEmpty(recordList)) {
                List<AdminRecord> crmNotesList = recordList.stream().map(item -> new AdminRecord()._setAttrs(item.getColumns())).collect(Collectors.toList());
                crmNotesList.forEach(crmNote -> noteIds.add(crmNote.getRecordId().intValue()));
            }
        }
        return noteIds;
    }


    public List<CrmNearestRecordDto> getNearestRecordsGroupByCustomerIdAndUserId() {
        List<Record> records = Db.find(Db.getSql("crm.record.getNearestRecordsGroupByCustomerIdAndUserId"));
        if (CollectionUtils.isEmpty(records)) {
            return Collections.emptyList();
        }
        List<CrmNearestRecordDto> list = Lists.newArrayListWithCapacity(records.size());
        for (Record record : records) {
            Long recordId = record.getLong("record_id");
            Long customerId = record.getLong("types_id");
            Long userId = record.getLong("create_user_id");
            Date createTime = record.getDate("create_time");
            CrmNearestRecordDto crmNearestRecordDto = CrmNearestRecordDto.builder()
                    .recordId(recordId)
                    .customerId(customerId)
                    .userId(userId)
                    .createTime(createTime).build();
            list.add(crmNearestRecordDto);
        }
        return list;
    }
}
