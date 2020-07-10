package com.kakarote.crm9.erp.admin.service;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.jfinal.aop.Aop;
import com.jfinal.aop.Before;
import com.jfinal.aop.Inject;
import com.jfinal.json.Json;
import com.jfinal.kit.Kv;
import com.jfinal.log.Log;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.jfinal.plugin.redis.Redis;
import com.kakarote.crm9.common.config.paragetter.BasePageRequest;
import com.kakarote.crm9.common.constant.BaseConstant;
import com.kakarote.crm9.erp.admin.common.AdminEnum;
import com.kakarote.crm9.erp.admin.constant.RedisConstant;
import com.kakarote.crm9.erp.admin.entity.AdminConfig;
import com.kakarote.crm9.erp.admin.entity.AdminDept;
import com.kakarote.crm9.erp.admin.entity.AdminRole;
import com.kakarote.crm9.erp.admin.entity.AdminScene;
import com.kakarote.crm9.erp.admin.entity.AdminSceneDefault;
import com.kakarote.crm9.erp.admin.entity.AdminUser;
import com.kakarote.crm9.erp.crm.common.CrmEnum;
import com.kakarote.crm9.erp.crm.common.CrmPayTypeEnum;
import com.kakarote.crm9.erp.crm.constant.CrmConstant;
import com.kakarote.crm9.erp.crm.constant.CrmTagConstant;
import com.kakarote.crm9.erp.crm.service.CrmBusinessService;
import com.kakarote.crm9.erp.crm.service.CrmCustomerService;
import com.kakarote.crm9.erp.crm.service.CrmLeadsService;
import com.kakarote.crm9.integration.common.PaymentTypeEnum;
import com.kakarote.crm9.utils.BaseUtil;
import com.kakarote.crm9.utils.CrmDateUtil;
import com.kakarote.crm9.utils.FieldUtil;
import com.kakarote.crm9.utils.R;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.regex.Pattern;

public class AdminSceneService {

    private Log logger = Log.getLog(getClass());

    @Inject
    private AdminFieldService adminFieldService;

    @Inject
    private CrmBusinessService crmBusinessService;

    @Inject
    private AdminDeptService adminDeptService;

    @Inject
    private AdminDataDicService adminDataDicService;

    @Inject
    private AdminUserService adminUserService;

    @Inject
    private CrmCustomerService crmCustomerService;

    @Inject
    private CrmLeadsService crmLeadsService;

    @Inject
    private AdminConfigService adminConfigService;

    /**
     * @author wyq
     * 查询场景字段
     */
    public R queryField(Integer label) {
        List<Record> recordList = new LinkedList<>();
        FieldUtil fieldUtil = new FieldUtil(recordList);
        String[] settingArr = new String[]{};
        if (1 == label) {
            fieldUtil.add("contact_user", "联系人", "text", settingArr)
                    .add("telephone", "联系电话", "text", settingArr)
                    .add("company", "公司", "text", settingArr)
                    .add("position", "职位", "text", settingArr)
                    .add("accuracy_requirements", "精度需求", "accuracyRequirements", settingArr)
                    .add("lead_come", "线索来源", "leadCome", settingArr)
                    .add("create_user_id", "创建人", "user", settingArr)
                    .add("update_time", "更新时间", "datetime", settingArr)
                    .add("create_time", "创建时间", "datetime", settingArr);
        } else if (2 == label) {
            fieldUtil.add("customer_name", "客户名称", "text", settingArr)
                    .add("customer_grade", "客户等级", "customerGrade", settingArr)
                    .add("distributor", "分销商等级", "customerCategory", settingArr)
                    .add("customer_type", "客户类型", "customerType", settingArr)
                    .add("create_user_id", "创建人", "user", settingArr)
                    .add("update_time", "更新时间", "datetime", settingArr)
                    .add("create_time", "创建时间", "datetime", settingArr);
        } else if (3 == label) {
            fieldUtil.add("name", "姓名", "text", settingArr)
                    .add("customer_name", "客户名称", "customer", settingArr)
                    .add("mobile", "手机", "mobile", settingArr)
                    .add("telephone", "电话", "text", settingArr)
                    .add("email", "电子邮箱", "email", settingArr)
                    .add("post", "职务", "text", settingArr)
                    .add("remark", "备注", "text", settingArr)
                    .add("create_user_id", "创建人", "user", settingArr)
                    .add("update_time", "更新时间", "datetime", settingArr)
                    .add("create_time", "创建时间", "datetime", settingArr);
        } else if (4 == label) {
            fieldUtil.add("name", "产品名称", "text", settingArr)
                    .add("category_id", "产品分类", "category", settingArr)
                    .add("num", "产品编码", "number", settingArr)
                    .add("price", "价格", "floatnumber", settingArr)
                    .add("description", "产品描述", "text", settingArr)
                    .add("owner_user_id", "负责人", "user", settingArr)
                    .add("create_user_id", "创建人", "user", settingArr)
                    .add("update_time", "更新时间", "datetime", settingArr)
                    .add("create_time", "创建时间", "datetime", settingArr);
        } else if (5 == label) {
            fieldUtil.add("business_name", "商机名称", "text", settingArr)
                    .add("customer_name", "客户名称", "customer", settingArr)
                    .add("type_id", "商机状态组", "business_type", crmBusinessService.queryBusinessStatusOptions("condition"))
                    .add("money", "商机金额", "floatnumber", settingArr)
                    .add("deal_date", "预计成交日期", "datetime", settingArr)
                    .add("remark", "备注", "text", settingArr)
                    //并无此字段，注释掉
//                    .add("product", "产品", "product", settingArr)
                    .add("owner_user_id", "负责人", "user", settingArr)
                    .add("create_user_id", "创建人", "user", settingArr)
                    .add("update_time", "更新时间", "datetime", settingArr)
                    .add("create_time", "创建时间", "datetime", settingArr);
        } else if (6 == label) {
            List<Map<String, Object>> checkList = new ArrayList<>();
            checkList.add(new JSONObject().fluentPut("name", "待审核").fluentPut("value", 0));
            checkList.add(new JSONObject().fluentPut("name", "审核中").fluentPut("value", 1));
            checkList.add(new JSONObject().fluentPut("name", "审核通过").fluentPut("value", 2));
            checkList.add(new JSONObject().fluentPut("name", "审核未通过").fluentPut("value", 3));
            checkList.add(new JSONObject().fluentPut("name", "已撤回").fluentPut("value", 4));
            fieldUtil.add("num", "合同编号", "number", settingArr)
                    .add("name", "合同名称", "text", settingArr)
                    .add("check_status", "审核状态", "checkStatus", checkList)
                    .add("customer_name", "客户名称", "customer", settingArr)
                    .add("business_name", "商机名称", "business", settingArr)
                    .add("order_date", "下单时间", "date", settingArr)
                    .add("money", "合同金额", "floatnumber", settingArr)
                    .add("start_time", "合同开始时间", "datetime", settingArr)
                    .add("end_time", "合同结束时间", "datetime", settingArr)
                    .add("contacts_id", "客户签约人", "contacts", settingArr)
                    .add("company_user_id", "公司签约人", "user", settingArr)
                    .add("remark", "备注", "number", settingArr)
                    .add("product", "产品", "product", settingArr)
                    .add("owner_user_id", "负责人", "user", settingArr)
                    .add("create_user_id", "创建人", "user", settingArr)
                    .add("update_time", "更新时间", "datetime", settingArr)
                    .add("create_time", "创建时间", "datetime", settingArr);
        } else if (7 == label) {
            List<Map<String, Object>> checkList = new ArrayList<>();
            checkList.add(new JSONObject().fluentPut("name", "待审核").fluentPut("value", 0));
            checkList.add(new JSONObject().fluentPut("name", "审核中").fluentPut("value", 1));
            checkList.add(new JSONObject().fluentPut("name", "审核通过").fluentPut("value", 2));
            checkList.add(new JSONObject().fluentPut("name", "审核未通过").fluentPut("value", 3));
            checkList.add(new JSONObject().fluentPut("name", "已撤回").fluentPut("value", 4));
            fieldUtil.add("number", "回款编号", "number", settingArr)
                    .add("check_status", "审核状态", "checkStatus", checkList)
                    .add("customer_name", "客户名称", "customer", settingArr)
                    .add("contract_num", "合同编号", "contract", settingArr)
                    .add("return_time", "回款日期", "date", settingArr)
                    .add("money", "回款金额", "floatnumber", settingArr)
                    .add("remark", "备注", "textarea", settingArr)
                    .add("owner_user_id", "负责人", "user", settingArr)
                    .add("create_user_id", "创建人", "user", settingArr)
                    .add("update_time", "更新时间", "datetime", settingArr)
                    .add("create_time", "创建时间", "datetime", settingArr);
        } else {
            return R.error("场景label不符合要求！");
        }
        recordList = fieldUtil.getRecordList();
        List<Record> records = adminFieldService.list(label.toString());
        /*线索移除系统字段*/
        if (1 == label) {
            List<Record> removeList = new ArrayList<>();
            for (Record record : records) {
                if (record.getStr("fieldName").equals(CrmConstant.LEADS_ORIGIN) ||
                        record.getStr("fieldName").equals(CrmConstant.CUSTOMER_LEVEL) ||
                        record.getStr("fieldName").equals(CrmConstant.CUSTOER_INDUSTRY) ||
                        record.getStr("fieldName").equals(CrmConstant.DEPT) ||
                        record.getStr("fieldName").equals(CrmConstant.USER)) {
                    removeList.add(record);
                }
            }
            records.removeAll(removeList);
        }
        /*线索移除系统字段*/
        if (2 == label) {
            List<Record> removeList = new ArrayList<>();
            for (Record record : records) {
                if (record.getStr("fieldName").equals(CrmConstant.CUSTOMER_LEVEL) ||
                        record.getStr("fieldName").equals(CrmConstant.CUSTOER_INDUSTRY) ||
                        record.getStr("fieldName").equals(CrmConstant.CUSTOMER_ORIGIN)
                ) {
                    removeList.add(record);
                }
            }
            records.removeAll(removeList);
        }
        /*客户移除系统自定义字段*/

        /*客户移除系统自定义字段*/
        if (recordList != null && records != null) {
            for (Record r : records) {
                r.set("field_name", r.getStr("name"));
            }
            recordList.addAll(records);
        }
        return R.ok().put("data", recordList);
    }

    /**
     * @author wyq
     * 增加场景
     */
    @Before(Tx.class)
    public R addScene(AdminScene adminScene) {
        Long userId = BaseUtil.getUserId();
        if (isExists(adminScene, userId)) {
            return R.error("场景名称不能重复");
        }
        adminScene.setIsHide(0).setSort(99999).setIsSystem(0).setCreateTime(DateUtil.date()).setUserId(userId);
        adminScene.save();
        if (1 == adminScene.getIsDefault()) {
            AdminSceneDefault adminSceneDefault = new AdminSceneDefault();
            adminSceneDefault.setSceneId(adminScene.getSceneId().intValue()).setType(adminScene.getType()).setUserId(userId).save();
        }
        return R.ok();
    }

    /**
     * 判断添加场景是否重复
     *
     * @param adminScene 场景实体
     * @param userId     用户ID
     * @author yue.li
     */
    public boolean isExists(AdminScene adminScene, Long userId) {
        boolean existFlag = false;
        List<Record> valueList = Db.find(Db.getSql("admin.scene.queryScene"), adminScene.getType(), userId);
        for (Record record : valueList) {
            if (adminScene.getSceneId() == null) {
                if (record.getStr("name").equals(adminScene.getName())) {
                    existFlag = true;
                }
            } else {
                if (record.getStr("name").equals(adminScene.getName()) && !adminScene.getSceneId().equals(record.getLong("scene_id"))) {
                    existFlag = true;
                }
            }
        }
        return existFlag;
    }

    /**
     * @author wyq
     * 更新场景
     */
    @Before(Tx.class)
    public R updateScene(AdminScene adminScene) {
        Long userId = BaseUtil.getUserId();
        if (isExists(adminScene, userId)) {
            return R.error("场景名称不能重复");
        }
        AdminScene oldAdminScene = AdminScene.dao.findById(adminScene.getSceneId());
        if (1 == adminScene.getIsDefault()) {
            Db.update("update 72crm_admin_scene_default set scene_id = ? where user_id = ? and type = ?", adminScene.getSceneId(), userId, oldAdminScene.getType());
        }
        adminScene.setUserId(userId).setType(oldAdminScene.getType()).setSort(oldAdminScene.getSort()).setIsSystem(oldAdminScene.getIsSystem()).setUpdateTime(DateUtil.date());
        return adminScene.update() ? R.ok() : R.error();
    }

    /**
     * @author wyq
     * 设置默认场景
     */
    @Before(Tx.class)
    public R setDefaultScene(Integer sceneId) {
        Long userId = BaseUtil.getUserId();
        AdminScene oldAdminScene = AdminScene.dao.findById(sceneId);
        Db.delete("delete from 72crm_admin_scene_default where user_id = ? and type = ?", userId, oldAdminScene.getType());
        AdminSceneDefault adminSceneDefault = new AdminSceneDefault();
        return adminSceneDefault.setSceneId(sceneId).setType(oldAdminScene.getType()).setUserId(userId).save() ? R.ok() : R.error();
    }

    /**
     * @author wyq
     * 删除场景
     */
    @Before(Tx.class)
    public R deleteScene(AdminScene adminScene) {
        if (1 == AdminScene.dao.findById(adminScene.getSceneId()).getIsSystem()) {
            return R.error("系统场景不能删除");
        }
        return AdminScene.dao.deleteById(adminScene.getSceneId()) ? R.ok() : R.error();
    }

    /**
     * @author wyq
     * 查询场景
     */
    @Before(Tx.class)
    public R queryScene(int type, Long userId) {
        //查询userId下是否有系统场景，没有则插入
        Integer number = Db.queryInt(Db.getSql("admin.scene.querySystemNumber"), type, userId);
        if (number == 0) {
            initScene(type, userId);
        }
        List<Record> sceneList = Db.find(Db.getSql("admin.scene.queryScene"), type, userId);
        determineDefaultScene(sceneList, record -> record.getStr("name"));
        return R.ok().put("data", sceneList);
    }

    /**
     * 判断默认场景
     *
     * @param sceneList
     */
    public void determineDefaultScene(List<Record> sceneList, Function<Record,String> sceneNameFunction) {
        if (CollectionUtils.isEmpty(sceneList)) {
            return;
        }
        List<AdminRole> userRoles = BaseUtil.getCrmUser().getUserRoles();
        //事业部负责人（测量测绘）、事业部负责人、销售支持（测量测绘）、销售支持 默认显示下属负责的***
        boolean showSubDefault = false;
        if (CollectionUtils.isNotEmpty(userRoles)) {
            AdminConfig roleIdShowSub = AdminConfig.dao.findByName(CrmConstant.CONFIG_KEY_ROLE_SHOW_SUB_SCENE);
            if (Objects.nonNull(roleIdShowSub) && StringUtils.isNotBlank(roleIdShowSub.getValue())) {
                String roleIdShowSubStr = roleIdShowSub.getValue();
                showSubDefault = userRoles.stream().anyMatch(adminRole -> Arrays.stream(roleIdShowSubStr.split(",")).anyMatch(roleId -> Long.valueOf(roleId).equals(adminRole.getRoleId())));
            }
        }
        //其他角色默认展示我负责的***
        Pattern pattern = showSubDefault ? SUB_SCENE_PATTERN : MINE_SCENE_PATTERN;
        sceneList.forEach(record -> {
            String sceneName = sceneNameFunction.apply(record);
            if (pattern.matcher(sceneName).matches()) {
                record.set("isDefault", 1);
            } else {
                record.set("isDefault", 0);
            }
        });
    }

    private static final Pattern SUB_SCENE_PATTERN = Pattern.compile("^下属负责的.*$");
    private static final Pattern MINE_SCENE_PATTERN = Pattern.compile("^我负责的.*$");

    /**
     * 初始化场景数据
     *
     * @param type
     * @param userId
     */
    private void initScene(int type, Long userId) {
        List<String> roleList = Db.query(Db.getSql("admin.user.queryUserRolesByUserId"), userId);
        AdminScene systemScene = new AdminScene();
        systemScene.setUserId(userId).setSort(0).setData("").setIsHide(0).setIsSystem(1).setCreateTime(DateUtil.date()).setType(type);
        JSONObject ownerObject = new JSONObject();
        ownerObject.fluentPut("owner_user_id", new JSONObject().fluentPut("name", "owner_user_id").fluentPut("condition", "is").fluentPut("value", userId));
        JSONObject subOwnerObject = new JSONObject();
        subOwnerObject.fluentPut("owner_user_id", new JSONObject().fluentPut("name", "owner_user_id").fluentPut("condition", "in").fluentPut("value", getSubUserId(userId == null ? null : userId.intValue(), BaseConstant.AUTH_DATA_RECURSION_NUM).substring(1)));
        if (1 == type) {
            /*添加我负责的线索 、事业部线索、线索公海、我创建的线索*/
            systemScene.setSceneId(null).setName("我创建的线索").setData(new JSONObject().fluentPut("formType", new JSONObject().fluentPut("formType", AdminEnum.LEADS_CREATE_KEY.getTypes())).toString()).save();
            systemScene.setSceneId(null).setName("事业部线索").setData(new JSONObject().fluentPut("formType", new JSONObject().fluentPut("formType", AdminEnum.LEADS_DEPT_KEY.getTypes())).toString()).save();
            systemScene.setSceneId(null).setName("线索公海").setData(new JSONObject().fluentPut("formType", new JSONObject().fluentPut("formType", AdminEnum.LEADS_PUBLIC_KEY.getTypes())).toString()).save();
            /*仅客户、市场角色、超管角色展示*/
            if (roleList.contains(BaseConstant.SUPER_ADMIN_ROLE_NAME) || roleList.contains(BaseConstant.CUSTOMER_SERVICE_ROLE_NAME) || roleList.contains(BaseConstant.MARKET_ROLE_NAME)) {
                systemScene.setSceneId(null).setName("所有线索").setData(new JSONObject().fluentPut("formType", new JSONObject().fluentPut("formType", AdminEnum.LEADS_ALL_KEY.getTypes())).toString()).save();
                systemScene.setSceneId(null).setName("未领取的线索").setData(new JSONObject().fluentPut("formType", new JSONObject().fluentPut("formType", AdminEnum.LEADS_NOT_RECEIVE_KEY.getTypes())).toString()).save();
            }
            /*仅客户、市场角色、超管角色展示*/
            /*添加我负责的线索 、事业部线索、线索公海、我创建的线索*/
            systemScene.setSceneId(null).setName("我负责的线索").setData(ownerObject.toString()).save();
            subOwnerObject.fluentPut("owner_user_id", new JSONObject().fluentPut("name", "owner_user_id").fluentPut("condition", "in").fluentPut("value", getSubUserId(userId == null ? null : userId.intValue(), BaseConstant.AUTH_DATA_RECURSION_NUM).substring(1))).fluentPut("is_transform", new JSONObject().fluentPut("name", "is_transform").fluentPut("condition", "is").fluentPut("value", 0));
            systemScene.setSceneId(null).setName("下属负责的线索").setData(subOwnerObject.toString()).save();
            JSONObject jsonObject = new JSONObject();
            jsonObject.fluentPut("is_transform", new JSONObject().fluentPut("name", "is_transform").fluentPut("condition", "is").fluentPut("value", "1"));
            systemScene.setSceneId(null).setName("已转化的线索").setData(jsonObject.toString()).setBydata("transform").save();
            systemScene.setSceneId(null).setName("被退回").setData(new JSONObject().fluentPut("formType", new JSONObject().fluentPut("formType", AdminEnum.LEADS_BACK_KEY.getTypes())).toString()).save();
        } else if (2 == type) {
            // 添加我负责的客户、部门客户池、网站客户池
            systemScene.setSceneId(null).setName("我创建的客户").setData(new JSONObject().fluentPut("formType", new JSONObject().fluentPut("formType", AdminEnum.CUSTOMER_CREATE_KEY.getTypes())).toString()).save();
            systemScene.setSceneId(null).setName("部门客户池").setData(new JSONObject().fluentPut("formType", new JSONObject().fluentPut("formType", AdminEnum.CUSTOMER_DEPT_KEY.getTypes())).toString()).save();
            systemScene.setSceneId(null).setName("网站客户池").setData(new JSONObject().fluentPut("formType", new JSONObject().fluentPut("formType", AdminEnum.CUSTOMER_PUBLIC_KEY.getTypes())).toString()).save();
            systemScene.setSceneId(null).setName("电销客户").setData(new JSONObject().fluentPut("formType", new JSONObject().fluentPut("formType", AdminEnum.CUSTOMER_TELEMARKETING_KEY.getTypes())).toString()).save();
            Integer deptId = BaseUtil.getDeptId();
            AdminDept adminDept = null;
            if (Objects.nonNull(deptId)) {
                String businessDepartmentId = adminDeptService.getBusinessDepartmentByDeptId(String.valueOf(deptId));
                if (StringUtils.isNotEmpty(businessDepartmentId)) {
                    adminDept = adminDeptService.getDeptNameByDeptId(businessDepartmentId);
                }
            }

            if (Objects.nonNull(adminDept)) {
                /* 获取数字地信事业部部门ID */
                String surveyMappingDeptId = adminConfigService.getConfig(CrmConstant.SURVEY_MAPPING_DEPT_ID);
                // 超管或者数字地信事业部显示分销商负责的客户池
                if (roleList.contains(BaseConstant.SUPER_ADMIN_ROLE_NAME) || adminDept.getDeptId().toString().equals(surveyMappingDeptId)) {
                    systemScene.setSceneId(null).setName("分销商负责的客户池").setData(new JSONObject().fluentPut("formType", new JSONObject().fluentPut("formType", AdminEnum.CUSTOMER_TELEMARKETING_KEY.getTypes())).toString()).save();
                }
            }

            /*添加我负责的客户、部门客户池、网站客户池*/
            systemScene.setSceneId(null).setName("我负责的客户").setData(ownerObject.toString()).save();
            systemScene.setSceneId(null).setName("下属负责的客户").setData(subOwnerObject.toString()).save();
            systemScene.setSceneId(null).setName("我参与的客户").setData(new JSONObject().fluentPut("formType", new JSONObject().fluentPut("formType", AdminEnum.CUSTOMER_TAKE_PART.getTypes())).toString()).save();
        } else if (3 == type) {
            //systemScene.setName("全部联系人").save();
            systemScene.setSceneId(null).setName("下属负责的联系人").save();
            systemScene.setSceneId(null).setName("我负责的联系人").setData(ownerObject.toString()).save();
        } else if (4 == type) {
            systemScene.setName("上架的产品").setData(new JSONObject().fluentPut("是否上下架", new JSONObject().fluentPut("name", "是否上下架").fluentPut("condition", "is").fluentPut("value", "上架")).toString()).save();
            JSONObject jsonObject = new JSONObject();
            jsonObject.fluentPut("是否上下架", new JSONObject().fluentPut("name", "是否上下架").fluentPut("condition", "is").fluentPut("value", "下架"));
            systemScene.setSceneId(null).setName("下架的产品").setData(jsonObject.toString()).save();
        } else if (5 == type) {
            //systemScene.setName("全部商机").save();
            systemScene.setSceneId(null).setName("我负责的商机").setData(ownerObject.toString()).save();
            systemScene.setSceneId(null).setName("下属负责的商机").setData(subOwnerObject.toString()).save();
            JSONObject jsonObject = new JSONObject();
            jsonObject.fluentPut("ro_user_id", new JSONObject().fluentPut("name", "ro_user_id").fluentPut("condition", "takePart").fluentPut("value", userId));
            systemScene.setSceneId(null).setName("我参与的商机").setData(jsonObject.toString()).save();
        } else if (6 == type) {
            //systemScene.setName("全部合同").save();
            systemScene.setSceneId(null).setName("我负责的合同").setData(ownerObject.toString()).save();
            systemScene.setSceneId(null).setName("下属负责的合同").setData(subOwnerObject.toString()).save();
            JSONObject jsonObject = new JSONObject();
            jsonObject.fluentPut("ro_user_id", new JSONObject().fluentPut("name", "ro_user_id").fluentPut("condition", "takePart").fluentPut("value", userId));
            systemScene.setSceneId(null).setName("我参与的合同").setData(jsonObject.toString()).save();
        } else if (7 == type) {
            //systemScene.setName("全部回款").save();
            systemScene.setSceneId(null).setName("我负责的回款").setData(ownerObject.toString()).save();
            systemScene.setSceneId(null).setName("下属负责的回款").setData(subOwnerObject.toString()).save();
            JSONObject jsonObject = new JSONObject();
            jsonObject.fluentPut("ro_user_id", new JSONObject().fluentPut("name", "ro_user_id").fluentPut("condition", "takePart").fluentPut("value", userId));
            systemScene.setSceneId(null).setName("我参与的回款").setData(jsonObject.toString()).save();
        } else if (String.valueOf(type).equals(CrmEnum.RECEIVABLES_MANAGEMENT_KEY.getTypes())) {
            systemScene.setSceneId(null).setName("我负责的回款").save();
            systemScene.setSceneId(null).setName("下属负责的回款").save();
            if (roleList.contains(BaseConstant.SUPER_ADMIN_ROLE_NAME) || roleList.contains(BaseConstant.SALSE_SUPPORT)) {
                systemScene.setSceneId(null).setName("部门的回款").save();
            }
            if (roleList.contains(BaseConstant.SUPER_ADMIN_ROLE_NAME) || roleList.contains(BaseConstant.SALES_OPERATIONS)) {
                systemScene.setSceneId(null).setName("未关联客户的回款").save();
                systemScene.setSceneId(null).setName("所有回款").save();
            }
        }
    }

    /**
     * 递归查询下属id
     */
    public String getSubUserId(Integer userId, Integer deepness) {
        StringBuilder ids = new StringBuilder();
        if (deepness > 0) {
            List<Long> list = Db.query("select user_id from 72crm_admin_user where parent_id = ?", userId);
            if (list != null && list.size() > 0) {
                for (Long l : list) {
                    ids.append(',').append(l).append(getSubUserId(l.intValue(), deepness - 1));
                }
            }
        }
        return StrUtil.isNotEmpty(ids.toString()) ? ids.toString() : " ";
    }

    /**
     * @author wyq
     * 查询场景设置
     */
    public R querySceneConfig(AdminScene adminScene) {
        Long userId = BaseUtil.getUserId();
        List<Record> valueList = Db.find(Db.getSql("admin.scene.queryScene"), adminScene.getType(), userId);
        for (Record scene : valueList) {
            if (StrUtil.isNotEmpty(scene.getStr("data"))) {
                JSONObject jsonObject = JSON.parseObject(scene.getStr("data"));
                scene.set("data", jsonObject);
            }
        }
        List<Record> hideValueList = Db.find(Db.getSql("admin.scene.queryHideScene"), adminScene.getType(), userId);
        for (Record hideScene : hideValueList) {
            if (StrUtil.isNotEmpty(hideScene.getStr("data"))) {
                JSONObject jsonObject = JSON.parseObject(hideScene.getStr("data"));
                hideScene.set("data", jsonObject);
            }
        }
        return R.ok().put("data", Kv.by("value", valueList).set("hide_value", hideValueList));
    }

    /**
     * @author wyq
     * 设置场景
     */
    @Before(Tx.class)
    public R sceneConfig(AdminScene adminScene) {
        Long userId = BaseUtil.getUserId();
        if (null != adminScene.getNoHideIds()) {
            String[] sortArr = adminScene.getNoHideIds().split(",");
            for (int i = 0; i < sortArr.length; i++) {
                Db.update(Db.getSql("admin.scene.sort"), i + 1, adminScene.getType(), userId, sortArr[i]);
            }
        }
        if (null != adminScene.getHideIds()) {
            String[] hideIdsArr = adminScene.getHideIds().split(",");
            Record number = Db.findFirst(Db.getSqlPara("admin.scene.queryIsHideSystem", Kv.by("ids", hideIdsArr)));
            if (number.getInt("number") > 0) {
                return R.error("系统场景不能隐藏");
            }
            Db.update(Db.getSqlPara("admin.scene.isHide", Kv.by("ids", hideIdsArr).set("type", adminScene.getType()).set("userId", userId)));
        }
        return R.ok();
    }

    public R filterConditionAndGetPageList(BasePageRequest basePageRequest) {
        JSONObject jsonObject = basePageRequest.getJsonObject();
        Integer sceneId = jsonObject.getInteger("sceneId");
        JSONObject data = new JSONObject();
        String sceneName = "";
        if (sceneId != null && sceneId != 0) {
            data = JSON.parseObject(AdminScene.dao.findById(sceneId).getData());
            sceneName = AdminScene.dao.findById(sceneId).getName();
        }
        //高级筛选没有sceneId，有data数据 线索
        if (sceneId == null && jsonObject.getInteger("type") == 1) {
            data = new JSONObject().fluentPut("is_transform", new JSONObject().fluentPut("name", "is_transform").fluentPut("condition", "is").fluentPut("value", "下架"));
        }
        //高级筛选
        JSONObject dataJ = jsonObject.getJSONObject("data");
        if (dataJ != null) {
            if (data != null) {
                dataJ.putAll(data);
            }
        } else {
            jsonObject.put("data", data);
        }

        jsonObject.put("sceneName", sceneName);
        basePageRequest.setJsonObject(jsonObject);

        Long userId = BaseUtil.getUserId();
        return getCrmPageList(basePageRequest, userId);
    }

    /**
     * @author wyq
     * Crm列表页查询
     */
    public R getCrmPageList(BasePageRequest basePageRequest, Long userId) {
        JSONObject data = basePageRequest.getJsonObject().getJSONObject("data");
        List<JSONObject> jsonObjectList = new ArrayList<>();
        if (data != null) {
            data.forEach((k, v) -> jsonObjectList.add(JSON.parseObject(v.toString())));
        }
        Integer searchType = basePageRequest.getJsonObject().getInteger("type");
        if (searchType == null) {
            return R.error("type不符合要求");
        }
        String serviceType = String.valueOf(searchType);
        String sceneName = basePageRequest.getJsonObject().getString("sceneName");
        StringBuffer whereSb = new StringBuffer(" where 1=1");
        for (JSONObject jsonObject : jsonObjectList) {
            String condition = jsonObject.getString("condition");
            String value = jsonObject.getString("value");
            String formType = jsonObject.getString("formType");
            if ("business_type".equals(formType)) {
                whereSb.append(" and ").append(jsonObject.getString("name")).append(" = ").append(jsonObject.getInteger("typeId"));
                if (StrUtil.isNotEmpty(jsonObject.getString("statusId"))) {
                    if ("win".equals(jsonObject.getString("statusId"))) {
                        whereSb.append(" and is_end = 1");
                    } else if ("lose".equals(jsonObject.getString("statusId"))) {
                        whereSb.append(" and is_end = 2");
                    } else if ("invalid".equals(jsonObject.getString("statusId"))) {
                        whereSb.append(" and is_end = 3");
                    } else {
                        whereSb.append(" and status_id = ").append(jsonObject.getString("statusId"));
                    }
                }
                continue;
            }

            /*格式化高级筛选*/
            /*精度需求*/
            if (CrmConstant.ACCURACY_REQUIREMENTS.equals(formType)) {
                String accuracyRequirements = adminDataDicService.formatTagValueName(CrmTagConstant.ACCURACY_REQUIREMENTS, value);
                if (!"".equals(accuracyRequirements)) {
                    value = accuracyRequirements;
                }
            }
            /*客户等级*/
            if (CrmConstant.CUSTOMER_GRADE.equals(formType)) {
                String customerGrade = adminDataDicService.formatTagValueName(CrmTagConstant.CUSTOMER_GRADE, value);
                if (!"".equals(customerGrade)) {
                    value = customerGrade;
                }
            }
            /*客户性质*/
            if (CrmConstant.DISTRIBUTOR.equals(formType)) {
                String customerCateGory = adminDataDicService.formatTagValueName(CrmTagConstant.DISTRIBUTOR, value);
                if (!"".equals(customerCateGory)) {
                    value = customerCateGory;
                }
            }
            /*客户类型*/
            if (CrmConstant.CUSTOMER_TYPE.equals(formType)) {
                String customerType = adminDataDicService.formatTagValueName(CrmTagConstant.CUSTOMER_TYPE, value);
                if (!"".equals(customerType)) {
                    value = customerType;
                }
            }
            /*线索来源*/
            if (CrmConstant.LEAD_COME.equals(formType)) {
                Record deptRecord = Db.findFirst(Db.getSql("admin.dept.queryDeptInfoByDeptName"), value);
                if (deptRecord != null) {
                    value = deptRecord.getStr("dept_id");
                }
            }
            /*格式化高级筛选*/

            /*添加个性化查询*/
            joinWheres(whereSb, serviceType, sceneName, userId);

            if (StrUtil.isNotEmpty(value) || StrUtil.isNotEmpty(jsonObject.getString("start")) || StrUtil.isNotEmpty(jsonObject.getString("end"))) {
                if ("takePart".equals(condition)) {
                    whereSb.append(" and (ro_user_id like '%,").append(value).append(",%' or rw_user_id like '%,").append(value).append(",%')");
                } else {
                    whereSb.append(" and ").append(jsonObject.getString("name"));
                    if ("is".equals(condition)) {
                        whereSb.append(" = '").append(value).append('\'');
                    } else if ("isNot".equals(condition)) {
                        whereSb.append(" != '").append(value).append('\'');
                    } else if ("contains".equals(condition)) {
                        whereSb.append(" like '%").append(value).append("%'");
                    } else if ("notContains".equals(condition)) {
                        whereSb.append(" not like '%").append(value).append("%'");
                    } else if ("isNull".equals(condition)) {
                        whereSb.append(" is null");
                    } else if ("isNotNull".equals(condition)) {
                        whereSb.append(" is not null");
                    } else if ("gt".equals(condition)) {
                        whereSb.append(" > ").append(value);
                    } else if ("egt".equals(condition)) {
                        whereSb.append(" >= ").append(value);
                    } else if ("lt".equals(condition)) {
                        whereSb.append(" < ").append(value);
                    } else if ("elt".equals(condition)) {
                        whereSb.append(" <= ").append(value);
                    } else if ("in".equals(condition)) {
                        whereSb.append(" in (").append(value).append(')');
                    }
                    if ("datetime".equals(formType)) {
                        whereSb.append(" between '").append(jsonObject.getString("start")).append("' and '").append(jsonObject.getString("end")).append('\'');
                    }
                    if ("date".equals(formType)) {
                        whereSb.append(" between '").append(jsonObject.getString("startDate")).append("' and '").append(jsonObject.getString("endDate")).append('\'');
                    }
                }
            }
        }
        String search = basePageRequest.getJsonObject().getString("search");
        if (StrUtil.isNotEmpty(search)) {
            if (isValid(search)) {
                return R.error("参数包含非法字段");
            }
            if (serviceType.equals(CrmEnum.LEADS_TYPE_KEY.getTypes())) {
                whereSb.append(" and (contact_user like '%").append(search).append("%' or company like '%")
                        .append(search).append("%')");
            } else if (serviceType.equals(CrmEnum.CUSTOMER_TYPE_KEY.getTypes()) || serviceType.equals(CrmEnum.WEBSITE_POOL.getTypes())) {
                whereSb.append(" and customer_id in (select customer_id from 72crm_crm_customer where customer_name like '%").append(search).append("%')");
            } else if (serviceType.equals(CrmEnum.CONTACTS_TYPE_KEY.getTypes())) {
                whereSb.append(" and (name like '%").append(search).append("%' or telephone like '%")
                        .append(search).append("%' or mobile like '%").append(search).append("%')");
            } else if (serviceType.equals(CrmEnum.PRODUCT_TYPE_KEY.getTypes()) || serviceType.equals(CrmEnum.CONTRACT_TYPE_KEY.getTypes())) {
                whereSb.append(" and (name like '%").append(search).append("%')");
            } else if (serviceType.equals(CrmEnum.BUSINESS_TYPE_KEY.getTypes())) {
                whereSb.append(" and (business_name like '%").append(search).append("%')");
            } else if (serviceType.equals(CrmEnum.RECEIVABLES_TYPE_KEY.getTypes())) {
                whereSb.append(" and (number like '%").append(search).append("%')");
            } else {
                return R.error("type不符合要求");
            }
        }

        //网站客户池，添加uids参数
        String uids = basePageRequest.getJsonObject().getString("uids");
        if (StringUtils.isNotBlank(uids) && serviceType.equals(CrmEnum.WEBSITE_POOL.getTypes())) {
            try {
                String[] uidStringArray = uids.split(",");
                Integer[] uidArray = (Integer[]) ConvertUtils.convert(uidStringArray, Integer.class);

                whereSb.append(" AND customer_no IN ( SELECT cust_id from 72crm_crm_site_member WHERE site_member_id IN (").append(StringUtils.join(uidArray, ",")).append(") )");
            } catch (Exception e) {
                return R.error("参数：uids 值非法");
            }
        }

        String viewName;
        if (serviceType.equals(CrmEnum.LEADS_TYPE_KEY.getTypes())) {
            viewName = "72crm_crm_leads";
        } else if (serviceType.equals(CrmEnum.CUSTOMER_TYPE_KEY.getTypes())) {
            viewName = "72crm_crm_customer";
        } else if (serviceType.equals(CrmEnum.CONTACTS_TYPE_KEY.getTypes())) {
            viewName = "contactsview";
        } else if (serviceType.equals(CrmEnum.PRODUCT_TYPE_KEY.getTypes())) {
            viewName = "productview";
        } else if (serviceType.equals(CrmEnum.BUSINESS_TYPE_KEY.getTypes())) {
            viewName = "businessview";
        } else if (serviceType.equals(CrmEnum.CONTRACT_TYPE_KEY.getTypes())) {
            viewName = "contractview";
        } else if (serviceType.equals(CrmEnum.RECEIVABLES_TYPE_KEY.getTypes())) {
            viewName = "receivablesview";
        } else if (serviceType.equals(CrmEnum.WEBSITE_POOL.getTypes())) {
            viewName = "72crm_crm_customer";
        } else {
            return R.error("type不符合要求");
        }
        String sortField = basePageRequest.getJsonObject().getString("sortField");
        String orderNum = basePageRequest.getJsonObject().getString("order");
        String from;
        if (StrUtil.isEmpty(sortField) || StrUtil.isEmpty(orderNum)) {
            sortField = "create_time";
            orderNum = "desc";
        } else {
            if (isValid(sortField)) {
                return R.error("参数包含非法字段");
            }
            if ("2".equals(orderNum)) {
                orderNum = "asc";
            } else {
                orderNum = "desc";
            }
        }
        if (serviceType.equals(CrmEnum.CUSTOMER_TYPE_KEY.getTypes())) {
            from = "from " + viewName + whereSb.toString();
        } else if (serviceType.equals(CrmEnum.WEBSITE_POOL.getTypes())) {
            from = "from " + viewName + whereSb.toString() + " and owner_user_id is null and dept_id is null";
        } else {
            from = "from " + viewName + whereSb.toString();
        }
        if (!serviceType.equals(CrmEnum.WEBSITE_POOL.getTypes()) && !serviceType.equals(CrmEnum.PRODUCT_TYPE_KEY.getTypes())) {
            // 超管无数据权限根据场景管控
            AdminUser adminUser = BaseUtil.getUser();
            logger.info("#######admin scene service user info: " + adminUser);

            if (Objects.nonNull(adminUser) && adminUser.getRoles().contains(BaseConstant.SUPER_ADMIN_ROLE_ID)) {
                // 下属负责的
                if (sceneName.startsWith(CrmConstant.CRM_SCENE_SUB_OWNER)) {
                    List<Long> userIds = adminUserService.queryUserByParentUser(adminUser.getUserId(), BaseConstant.AUTH_DATA_RECURSION_NUM);
                    if (userIds != null && userIds.size() > 0) {
                        from += " and owner_user_id in (" + StrUtil.join(",", userIds) + ")";
                    } else {
                        from += " and owner_user_id ='' ";
                    }
                }
            } else {
                List<Long> longs = Aop.get(AdminUserService.class).queryUserByAuth(userId);
                if (longs != null && longs.size() > 0) {
                    /*非线索、客户、网站客户池*/
                    if (!isExists(serviceType, sceneName)) {
                        if (sceneName.startsWith(CrmConstant.CRM_SCENE_SUB_OWNER)) {
                            longs.remove(userId);
                            if (longs.size() > 0) {
                                from += " and owner_user_id in (" + StrUtil.join(",", longs) + ")";
                            } else {
                                from += " and owner_user_id ='' ";
                            }
                        } else {
                            if (!sceneName.startsWith(CrmConstant.CRM_SCENE_MY_TAKE_PART_IN)) {
                                from += " and owner_user_id in (" + StrUtil.join(",", longs) + ")";
                            }
                        }
                    }
                    if (serviceType.equals(CrmEnum.CUSTOMER_TYPE_KEY.getTypes()) || serviceType.equals(CrmEnum.CONTRACT_TYPE_KEY.getTypes()) || serviceType.equals(CrmEnum.BUSINESS_TYPE_KEY.getTypes())) {
                        if (sceneName.startsWith(CrmConstant.CRM_SCENE_MY_TAKE_PART_IN)) {
                            from += " and (ro_user_id like CONCAT('%,','" + userId + "',',%')" + " or rw_user_id like CONCAT('%,','" + userId + "',',%'))";
                        }
                    }
                }
            }
        }

        String orderby = " order by " + viewName + "." + sortField + " " + orderNum;
        from = from + orderby;
        if (StrUtil.isNotEmpty(basePageRequest.getJsonObject().getString("excel"))) {
            return R.ok().put("data", Db.find("select * " + from));
        }
        if (serviceType.equals(CrmEnum.CUSTOMER_TYPE_KEY.getTypes()) || serviceType.equals(CrmEnum.WEBSITE_POOL.getTypes())) {
            String disposeStatus = basePageRequest.getJsonObject().getString("disposeStatus");
            String sql = null;
            if ("0".equals(disposeStatus) || "1".equals(disposeStatus)) {
                sql = " from\n ( \n"
                        + Db.getSqlPara("crm.customer.getCustomerFollowMsg").getSql()
                        + "\n where \n"
                        + from.split("where")[1].replace(orderby, "")
                        + "\n GROUP BY 72crm_crm_customer.customer_id \n"
                        + orderby
                        + " ) a \n"
                        + " where a.dispose_status = " + disposeStatus;
            }

            Page<Record> recordPage;
            if ((sceneName.startsWith(AdminEnum.CUSTOMER_PUBLIC_KEY.getName()) && jsonObjectList.size() == CrmConstant.INTEGER_ONE && StringUtils.isEmpty(search))
                    || (serviceType.equals(CrmEnum.WEBSITE_POOL.getTypes()) && jsonObjectList.size() == CrmConstant.INTEGER_ZERO && StringUtils.isEmpty(search) && StringUtils.isBlank(uids))) {
                if (sql != null) {
                    recordPage = getWebSiteInfo(basePageRequest, sql, serviceType);
                } else {
                    recordPage = getWebSiteInfo(basePageRequest, from, serviceType);
                }

            } else {
                if (sql != null) {
                    recordPage = Db.paginate(basePageRequest.getPage(), basePageRequest.getLimit(), "select *", sql);
                } else {
                    recordPage = Db.paginate(basePageRequest.getPage(), basePageRequest.getLimit(), "select *", from);
                }
            }
            if (recordPage.getList() != null && recordPage.getList().size() > 0) {
                List<Record> customerGradeList = adminDataDicService.queryTagList(CrmTagConstant.CUSTOMER_GRADE);
                List<Record> customerTypeList = adminDataDicService.queryTagList(CrmTagConstant.CUSTOMER_TYPE);
                for (Record record : recordPage.getList()) {
                    record = formatCustomer(record);
                    record.set("customer_grade", formatCustomerGradeTagInfo(record.getStr("customer_grade"), customerGradeList));
                    record.set("customer_type", formatCustomerGradeTagInfo(record.getStr("customer_type"), customerTypeList));

                    if (!"0".equals(disposeStatus) && !"1".equals(disposeStatus)) {
                        Record first = Db.findFirst(Db.getSqlPara("crm.customer.getCustomerFollowMsgWithCustomerId", Kv.by("customerId", record.get("customer_id"))));
                        record.set("lately_follow_user_name", first.get("lately_follow_user_name"));
                        record.set("lately_follow_time", first.get("lately_follow_time"));
                        record.set("distrbute_time", first.get("distrbute_time"));
                        record.set("dispose_status", first.get("dispose_status"));
                    }
                }
            }
            return R.ok().put("data", recordPage);
        }
        Page<Record> recordPage = Db.paginate(basePageRequest.getPage(), basePageRequest.getLimit(), "select *", from);
        if (serviceType.equals(CrmEnum.BUSINESS_TYPE_KEY.getTypes())) {
            setBusinessStatus(recordPage.getList());
        }
        if (serviceType.equals(CrmEnum.CONTRACT_TYPE_KEY.getTypes())) {
            Page<Record> page = Db.paginate(basePageRequest.getPage(), basePageRequest.getLimit(), "select *,IFNULL((select SUM(a.money) from 72crm_crm_receivables as a where a.contract_id = contractview.contract_id),0) as receivedMoney", from);
            Record totalMoney = Db.findFirst("select SUM(money) as contractMoney,GROUP_CONCAT(contract_id) as contractIds " + from);
            String receivedMoney = Db.queryStr("select SUM(money) from 72crm_crm_receivables where receivables_id in (" + totalMoney.getStr("contractIds") + ")");
            JSONObject jsonObject = JSONObject.parseObject(Json.getJson().toJson(page), JSONObject.class);
            return R.ok().put("data", jsonObject.fluentPut("money", new JSONObject().fluentPut("contractMoney", totalMoney.getStr("contractMoney")).fluentPut("receivedMoney", receivedMoney)));
        }
        /*联系人列表脱敏*/
        if (CrmEnum.CONTACTS_TYPE_KEY.getTypes().equals(serviceType)) {
            recordPage = formatContacts(recordPage);
        }

        /*线索格式化信息*/
        if (CrmEnum.LEADS_TYPE_KEY.getTypes().equals(serviceType)) {
            recordPage = formatLeads(recordPage);
        }

        return R.ok().put("data", recordPage);
    }

    /***
     * 格式化回款支付方式
     * @author yue.li
     * @param  recordPage 分页结果数据
     * @return
     */
    public Page<Record> formatReceivablesManagement(Page<Record> recordPage) {
        List<Record> recordList = recordPage.getList();
        if (recordList != null && recordList.size() > 0) {
            for (Record record : recordList) {
                record.set("pay_type_name", PaymentTypeEnum.getPaymentTypeNameInCrm(PaymentTypeEnum.getPaymentTypeDefinedInCrm(record.get("pay_type"))));
            }
            recordPage.setList(recordList);
        }
        return recordPage;
    }

    /***
     * 格式化CRM系统支付类型(充值、消费、退款)
     * @author yue.li
     * @param  recordPage 分页结果数据
     * @return
     */
    public Page<Record> formatCrmPayType(Page<Record> recordPage) {
        List<Record> recordList = recordPage.getList();
        if (recordList != null && recordList.size() > 0) {
            for (Record record : recordList) {
                if (StringUtils.isNotEmpty(record.getStr("crm_pay_type")) && CrmPayTypeEnum.CONSUME_KEY.getTypes().equals(record.getStr("crm_pay_type"))) {
                    record.set("crm_pay_type_name", CrmPayTypeEnum.CONSUME_KEY.getName());
                } else if (StringUtils.isNotEmpty(record.getStr("crm_pay_type")) && CrmPayTypeEnum.RECHARGE_KEY.getTypes().equals(record.getStr("crm_pay_type"))) {
                    record.set("crm_pay_type_name", CrmPayTypeEnum.RECHARGE_KEY.getName());
                } else if (StringUtils.isNotEmpty(record.getStr("crm_pay_type")) && CrmPayTypeEnum.REFUND_KEY.getTypes().equals(record.getStr("crm_pay_type"))) {
                    record.set("crm_pay_type_name", CrmPayTypeEnum.REFUND_KEY.getName());
                } else {
                    record.set("crm_pay_type_name", null);
                }
            }
            recordPage.setList(recordList);
        }
        return recordPage;
    }

    /***
     * 格式化线索信息
     * @author yue.li
     * @param  recordPage 分页结果数据
     * @return
     */
    public Page<Record> formatLeads(Page<Record> recordPage) {
        List<Record> allUsers = adminUserService.getAllUsers();
        List<Record> deptList = adminDeptService.getAllDepts();
        List<Record> recordList = recordPage.getList();
        if (recordList != null && recordList.size() > 0) {
            for (Record record : recordList) {
                record.set("create_user_name", formatUserName(record.getStr("create_user_id"), allUsers));
                record.set("owner_user_name", formatUserName(record.getStr("owner_user_id"), allUsers));
                record.set("dept_name", formatDeptName(record.getStr("dept_id"), deptList));
            }
            recordPage.setList(recordList);
        }
        return recordPage;
    }

    /***
     * 格式化联系人信息
     * @author yue.li
     * @param  recordPage 分页结果数据
     * @return
     */
    public Page<Record> formatContacts(Page<Record> recordPage) {
        List<Record> recordList = recordPage.getList();
        if (recordList != null && recordList.size() > 0) {
            for (Record record : recordList) {
                record.set("email", getSensitiveField("email", record));
                record.set("mobile", getSensitiveField("mobile", record));
                record.set("telephone", getSensitiveField("telephone", record));
                record.set("wechat", getSensitiveField("wechat", record));
            }
            recordPage.setList(recordList);
        }
        return recordPage;
    }

    /***
     * 获取网站客户池数据
     * @author yue.li
     * @param  basePageRequest 分页请求对象
     * @param  sql 网站客户池查询SQL
     * @param  serviceType 业务类型
     * @return
     */
    public Page<Record> getWebSiteInfo(BasePageRequest basePageRequest, String sql, String serviceType) {
        int totalRow = 0;
        if (serviceType.equals(CrmEnum.CUSTOMER_TYPE_KEY.getTypes())) {
            if (StringUtils.isEmpty(Redis.use().get(RedisConstant.WEBSITE_KEY))) {
                crmCustomerService.redisReset();
            }
            totalRow = Integer.parseInt(Redis.use().get(RedisConstant.WEBSITE_KEY));
        }
        if (serviceType.equals(CrmEnum.WEBSITE_POOL.getTypes())) {
            if (StringUtils.isEmpty(Redis.use().get(RedisConstant.PUBLIC_WEBSITE_KEY))) {
                crmCustomerService.redisReset();
                totalRow = Integer.parseInt(Redis.use().get(RedisConstant.PUBLIC_WEBSITE_KEY));
            } else {
                totalRow = Integer.parseInt(Redis.use().get(RedisConstant.PUBLIC_WEBSITE_KEY));
            }
        }
        int pageNumber = basePageRequest.getPage();
        int pageSize = basePageRequest.getLimit();
        int totalPage = new BigDecimal(totalRow).divide(new BigDecimal(pageSize), 0).setScale(0, BigDecimal.ROUND_UP).intValue();
        sql = "select * " + sql + " limit " + (pageNumber - 1) * pageSize + "," + pageSize;
        List<Record> list = Db.find(sql);
        Page<Record> recordPage = new Page<>();
        recordPage.setList(list);
        recordPage.setPageNumber(pageNumber);
        recordPage.setPageSize(pageSize);
        recordPage.setTotalRow(totalRow);
        recordPage.setTotalPage(totalPage);
        return recordPage;
    }

    /**
     * 格式化客户等级标签ID为标签名称
     *
     * @param tagId 标签ID
     * @param list  标签集合
     * @return
     * @author yue.li
     */
    public String formatCustomerGradeTagInfo(String tagId, List<Record> list) {
        String result = null;
        if (list != null && list.size() > 0) {
            for (Record record : list) {
                if (StringUtils.isNotEmpty(tagId) && record.getStr("label").equals(tagId)) {
                    result = record.getStr("name");
                    break;
                }
            }
        }
        return result;
    }

    /***
     * 格式化客户信息
     */
    public Record formatCustomer(Record record) {
        String createUserName = null;
        String ownUserName = null;
        String deptName = null;
        String ownerUserDeptId = null;
        String ownerUserDeptName = null;
        String siteMemberId = null;
        if (StringUtils.isNotEmpty(record.getStr("create_user_id"))) {
            createUserName = Db.findFirst(Db.getSql("admin.user.queryUserByUserId"), record.getStr("create_user_id")).getStr("realname");
        }
        if (StringUtils.isNotEmpty(record.getStr("owner_user_id"))) {
            Record userInfo = Db.findFirst(Db.getSql("admin.user.queryUserByUserId"), record.getStr("owner_user_id"));
            ownUserName = userInfo.getStr("realname");
            ownerUserDeptId = userInfo.getStr("dept_id");
            ownerUserDeptName = userInfo.getStr("deptName");
        }
        if (StringUtils.isNotEmpty(record.getStr("dept_id"))) {
            deptName = Db.findFirst(Db.getSql("admin.dept.queryDeptInfoByDeptId"), record.getStr("dept_id")).getStr("name");
        }
        Record siteMemberRecord = Db.findFirst(Db.getSql("crm.sitemember.getSiteMemberInfoByCustId"), record.getStr("customer_id"));
        if (siteMemberRecord != null) {
            siteMemberId = siteMemberRecord.getStr("userId");
        }
        record.set("create_user_name", createUserName);
        record.set("owner_user_name", ownUserName);
        record.set("owner_user_dept_id", ownerUserDeptId);
        record.set("owner_user_dept_name", ownerUserDeptName);
        record.set("dept_name", deptName);
        record.set("site_member_id", siteMemberId);
        return record;
    }

    /**
     * 拼接个性化查询条件
     *
     * @param whereSb     查询条件
     * @param serviceType 类型
     * @param sceneName   场景名称
     * @param userId      用户ID
     * @author yue.li
     */
    public void joinWheres(StringBuffer whereSb, String serviceType, String sceneName, Long userId) {

        /*事业部线索、线索公海、我创建的线索*/
        if (serviceType.equals(CrmEnum.LEADS_TYPE_KEY.getTypes())) {
            if (StrUtil.isNotEmpty(sceneName)) {
                if (sceneName.equals(AdminEnum.LEADS_OWN_KEY.getName())) {
                    whereSb.append(" and is_transform = 0");
                }
                if (sceneName.equals(AdminEnum.LEADS_DEPT_KEY.getName())) {
                    Record record = Db.findFirst(Db.getSql("admin.user.queryUserByUserId"), userId);
                    String deptId = adminDeptService.getBusinessDepartmentByDeptId(record.getStr("dept_id"));
                    if (deptId == null || "".equals(deptId)) {
                        whereSb.append(" and is_transform = 0 and owner_user_id is null");
                    } else {
                        List<String> deptList = adminDeptService.getAllDeptsByBusinessDepartmentId(deptId);
                        deptList.add(deptId);
                        whereSb.append(" and is_transform = 0 and owner_user_id is null and dept_id in ( ").append(StrUtil.join(",", deptList)).append(')');
                    }
                }
                if (sceneName.equals(AdminEnum.LEADS_PUBLIC_KEY.getName())) {
                    whereSb.append(" and is_transform != 1 and owner_user_id is null and dept_id is null");
                }
                if (sceneName.equals(AdminEnum.LEADS_CREATE_KEY.getName())) {
                    whereSb.append(" and create_user_id = ").append(userId);
                }
                if (sceneName.equals(AdminEnum.LEADS_NOT_RECEIVE_KEY.getName())) {
                    whereSb.append(" and is_transform = 0 and owner_user_id is null");
                }
                if (sceneName.equals(AdminEnum.LEADS_BACK_KEY.getName())) {
                    whereSb.append(" and is_transform = 2 and owner_user_id is null");
                }
            }
        }
        /*事业部线索、线索公海、我创建的线索*/

        /*事业部客户、客户公海、我创建的客户*/
        if (serviceType.equals(CrmEnum.CUSTOMER_TYPE_KEY.getTypes())) {
            if (StrUtil.isNotEmpty(sceneName)) {
                if (sceneName.equals(AdminEnum.CUSTOMER_DEPT_KEY.getName())) {
                    Record record = Db.findFirst(Db.getSql("admin.user.queryUserByUserId"), userId);
                    String deptId = adminDeptService.getBusinessDepartmentByDeptId(record.getStr("dept_id"));
                    if (deptId == null || "".equals(deptId)) {
                        whereSb.append(" and owner_user_id is null");
                    } else {
                        whereSb.append(" and owner_user_id is null and dept_id = ").append(deptId);
                    }
                }
                if (sceneName.equals(AdminEnum.CUSTOMER_PUBLIC_KEY.getName())) {
                    whereSb.append(" and dept_id is null and owner_user_id is null and (registration_date <= '").append(CrmDateUtil.getLastWeek()).append("' or registration_date is null)");
                }
                if (sceneName.equals(AdminEnum.CUSTOMER_CREATE_KEY.getName())) {
                    whereSb.append(" and create_user_id = ").append(userId);
                }
                if (AdminEnum.CUSTOMER_TELEMARKETING_KEY.getName().equals(sceneName)) {
                    whereSb.append(" and owner_user_id in (SELECT user_id FROM 72crm_admin_user where dept_id = (SELECT dept_id FROM 72crm_admin_dept WHERE `name` = '" + CrmConstant.TELEMARKETING_DEPT_NAME + "' LIMIT 1)) ").append(userId);
                }
            }
        }
        /*事业部客户、客户公海、我创建的客户*/
    }

    /**
     * 是否为线索(我负责的、事业部线索、线索公海、我创建的)/客户（我负责的、部门客户池、网站客户池、我创建的）
     *
     * @param serviceType 类型
     * @param sceneName   场景名称
     *                    return
     * @author yue.li
     */
    public boolean isExists(String serviceType, String sceneName) {
        boolean flag = false;
        /*事业部线索、线索公海、我创建的线索*/
        if (serviceType.equals(CrmEnum.LEADS_TYPE_KEY.getTypes())) {
            if (StrUtil.isNotEmpty(sceneName)) {
                if (sceneName.equals(AdminEnum.LEADS_DEPT_KEY.getName()) ||
                        sceneName.equals(AdminEnum.LEADS_PUBLIC_KEY.getName()) ||
                        sceneName.equals(AdminEnum.LEADS_CREATE_KEY.getName()) ||
                        sceneName.equals(AdminEnum.LEADS_ALL_KEY.getName()) ||
                        sceneName.equals(AdminEnum.LEADS_NOT_RECEIVE_KEY.getName()) ||
                        sceneName.equals(AdminEnum.LEADS_BACK_KEY.getName())) {
                    flag = true;
                }
            }
        }
        /*事业部线索、线索公海、我创建的线索*/

        /*事业部客户、客户公海、我创建的*/
        if (serviceType.equals(CrmEnum.CUSTOMER_TYPE_KEY.getTypes())) {
            if (StrUtil.isNotEmpty(sceneName)) {
                if (sceneName.equals(AdminEnum.CUSTOMER_DEPT_KEY.getName()) ||
                        sceneName.equals(AdminEnum.CUSTOMER_PUBLIC_KEY.getName()) ||
                        sceneName.equals(AdminEnum.CUSTOMER_CREATE_KEY.getName())) {
                    flag = true;
                }
            }
        }
        /*事业部客户、客户公海、我创建的*/

        /*网站客户池*/
        if (serviceType.equals(CrmEnum.WEBSITE_POOL.getTypes())) {
            flag = true;
        }
        /*网站客户池*/

        /*我负责的*/
        if (sceneName.startsWith(CrmConstant.CRM_SCENE_MY_OWN)) {
            flag = true;
        }
        /*我负责的*/
        return flag;
    }

    public void setBusinessStatus(List<Record> list) {
        list.forEach(
                record -> {
                    if (record.getInt("is_end") == 0) {
                        Integer sortNum = Db.queryInt("select order_num from 72crm_crm_business_status where status_id = ?", record.getInt("status_id"));
                        Integer totalStatsNum = Db.queryInt("select count(*) from 72crm_crm_business_status where type_id = ?", record.getInt("type_id")) + 1;
                        record.set("progressBar", sortNum + "/" + totalStatsNum);
                    } else if (record.getInt("is_end") == 1) {
                        Integer totalStatsNum = Db.queryInt("select count(*) from 72crm_crm_business_status where type_id = ?", record.getInt("type_id")) + 1;
                        record.set("progressBar", totalStatsNum + "/" + totalStatsNum);
                    } else if (record.getInt("is_end") == 2) {
                        int totalStatsNum = Db.queryInt("select count(*) from 72crm_crm_business_status where type_id = ?", record.getInt("type_id")) + 1;
                        record.set("progressBar", "0/" + totalStatsNum);
                    } else if (record.getInt("is_end") == 3) {
                        record.set("progressBar", "0/0");
                    }
                });
    }

    public boolean isValid(String param) {
        String reg = "(?:')|(?:--)|(/\\*(?:.|[\\n\\r])*?\\*/)|"
                + "(\\b(select|update|union|and|or|delete|insert|trancate|char|into|substr|ascii|declare|exec|count|master|drop|execute)\\b)";

        Pattern sqlPattern = Pattern.compile(reg, Pattern.CASE_INSENSITIVE);

        return sqlPattern.matcher(param).find();
    }

    /***
     * 字段脱敏
     * @param fieldName 字段名称
     * @param record 对象实体
     */
    private String getSensitiveField(String fieldName, Record record) {
        return record.getStr(fieldName) != null && !record.getStr(fieldName).isEmpty() ? "true" : "";
    }

    /**
     * 根据用户ID获取用户名称
     *
     * @param userId   用户ID
     * @param userList 用户集合
     *                 return
     * @author yue.li
     */
    public String formatUserName(String userId, List<Record> userList) {
        String userName = null;
        if (StringUtils.isNotEmpty(userId)) {
            for (Record record : userList) {
                if (record.getStr("userId").equals(userId)) {
                    userName = record.getStr("realName");
                    break;
                }
            }
        }
        return userName;
    }

    /**
     * 根据部门ID获取部门名称
     *
     * @param deptId   部门ID
     * @param deptList 部门集合
     *                 return
     * @author yue.li
     */
    public String formatDeptName(String deptId, List<Record> deptList) {
        String deptName = null;
        if (StringUtils.isNotEmpty(deptId)) {
            for (Record record : deptList) {
                if (record.getStr("dept_id").equals(deptId)) {
                    deptName = record.getStr("name");
                    break;
                }
            }
        }
        return deptName;
    }
}
