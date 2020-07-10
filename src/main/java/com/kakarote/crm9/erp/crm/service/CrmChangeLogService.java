package com.kakarote.crm9.erp.crm.service;

import cn.hutool.core.date.DateTime;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.jfinal.aop.Inject;
import com.jfinal.plugin.activerecord.Db;
import com.kakarote.crm9.common.exception.CrmException;
import com.kakarote.crm9.erp.admin.entity.AdminDept;
import com.kakarote.crm9.erp.admin.entity.AdminUser;
import com.kakarote.crm9.erp.admin.service.AdminConfigService;
import com.kakarote.crm9.erp.admin.service.AdminDeptService;
import com.kakarote.crm9.erp.admin.service.AdminUserService;
import com.kakarote.crm9.erp.crm.constant.CrmConstant;
import com.kakarote.crm9.erp.crm.dto.CrmChangeLogDto;
import com.kakarote.crm9.erp.crm.entity.CrmBdDeptChangeLog;
import com.kakarote.crm9.erp.crm.entity.CrmBusinessBelongChangeLog;
import com.kakarote.crm9.erp.crm.entity.CrmChangeLog;
import com.kakarote.crm9.erp.crm.entity.CrmCustomerBelongChangeLog;
import com.kakarote.crm9.erp.crm.entity.CrmDeptChangeLog;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 变更历史处理
 * @author honglei.wan
 */
@Slf4j
public class CrmChangeLogService {


    @Inject
    private AdminDeptService adminDeptService;

    @Inject
    private AdminUserService adminUserService;

    @Inject
    private AdminConfigService adminConfigService;

    private static final Long SYSTEM_OPERATOR = -1L;


    /**
     * 保存客户变更历史记录
     *
     * @param crmChangeLogDto
     * @return
     */
    public Long saveChangeLog(CrmChangeLogDto crmChangeLogDto) {
        if (crmChangeLogDto.getChannelEnum() == null) {
            log.info("saveOperateLog is faild, channelEnum is empty");
            return null;
        }
        try {
            CrmChangeLog crmChangeLog = new CrmChangeLog();
            crmChangeLog.setRequestId(crmChangeLogDto.getRequestId());
            crmChangeLog.setFromChannel(crmChangeLogDto.getChannelEnum().getType());
            crmChangeLog.setFromChannelEvent(crmChangeLogDto.getChannelEvent());
            crmChangeLog.setFromId(crmChangeLogDto.getFromId());
            crmChangeLog.setChangeHistory(crmChangeLogDto.getChangeHistory());
            crmChangeLog.setOldOwnerUserId(crmChangeLogDto.getOldOwnerUserId());
            crmChangeLog.setNewOwnerUserId(crmChangeLogDto.getNewOwnerUserId());

            Long oldOwnerUserDeptId = getOwnerUserDeptId(crmChangeLogDto.getOldOwnerUserDeptId(), crmChangeLogDto.getOldOwnerUserId());
            Long newOwnerUserDeptId = getOwnerUserDeptId(crmChangeLogDto.getNewOwnerUserDeptId(), crmChangeLogDto.getNewOwnerUserId());
            List<AdminDept> adminDepts = adminDeptService.getAllAdminDepts();
            Map<Long, AdminDept> adminDeptHashMap = Maps.newHashMapWithExpectedSize(adminDepts.size());
            for (AdminDept adminDept : adminDepts) {
                adminDeptHashMap.put(adminDept.getDeptId(), adminDept);
            }
            String oldOwnerUserDeptIdTree = getOwnerUserDeptIdTree(oldOwnerUserDeptId, adminDeptHashMap);
            String newOwnerUserDeptIdTree = getOwnerUserDeptIdTree(newOwnerUserDeptId, adminDeptHashMap);
            String oldOwnerUserDeptNameTree = getOwnerUserDeptNameTree(oldOwnerUserDeptId, adminDeptHashMap);
            String newOwnerUserDeptNameTree = getOwnerUserDeptNameTree(newOwnerUserDeptId, adminDeptHashMap);

            crmChangeLog.setOldOwnerUserDeptId(oldOwnerUserDeptId);
            crmChangeLog.setOldOwnerUserDeptIdTree(oldOwnerUserDeptIdTree);
            crmChangeLog.setOldOwnerUserDeptNameTree(oldOwnerUserDeptNameTree);

            crmChangeLog.setNewOwnerUserDeptId(newOwnerUserDeptId);
            crmChangeLog.setNewOwnerUserDeptIdTree(newOwnerUserDeptIdTree);
            crmChangeLog.setNewOwnerUserDeptNameTree(newOwnerUserDeptNameTree);
            crmChangeLog.setOperator(Objects.nonNull(crmChangeLogDto.getOperatorId()) ? crmChangeLogDto.getOperatorId()
                    : SYSTEM_OPERATOR);
            crmChangeLog.setOperateTime(crmChangeLogDto.getOperateTime());
            crmChangeLog.setGmtCreate(DateTime.now());
            crmChangeLog.setGmtModified(DateTime.now());
            crmChangeLog.setIsDeleted(0);
            crmChangeLog.setEnvFlag("");

            boolean save = crmChangeLog.save();
            if (!save) {
                log.info("saveOperateLog is faild, crmOperateLogDto:{}", JSONObject.toJSONString(crmChangeLog));
                return null;
            }
            BigInteger changeLogId = crmChangeLog.getId();
            if (Objects.nonNull(changeLogId)) {
                return changeLogId.longValue();
            }
            return null;
        } catch (Exception e) {
            log.error("saveChangeLog is error ,crmChangeLogDto:{}", JSONObject.toJSONString(crmChangeLogDto), e);
            return null;
        }
    }

    /**
     * 获取用户所在部门 如果部门存在，以部门为准，否则查询用户所在的部门
     *
     * @param oldOwnerUserDeptId
     * @param oldOwnerUserId
     * @return
     */
    private Long getOwnerUserDeptId(Long oldOwnerUserDeptId, Long oldOwnerUserId) {
        if (Objects.nonNull(oldOwnerUserDeptId)) {
            return oldOwnerUserDeptId;
        }
        if (Objects.nonNull(oldOwnerUserId)) {
            AdminUser adminUser = adminUserService.getAdminUserByUserId(oldOwnerUserId);
            if (Objects.nonNull(adminUser)) {
                return Long.valueOf(adminUser.getDeptId());
            }
        }
        return null;
    }

    /**
     * 获取部门id树
     *
     * @param deptId
     * @param adminDeptMap
     * @return
     */
    private String getOwnerUserDeptIdTree(Long deptId, Map<Long, AdminDept> adminDeptMap) {
        if (Objects.isNull(deptId) || Objects.isNull(adminDeptMap)) {
            return null;
        }
        List<String> deptIds = Lists.newArrayListWithCapacity(adminDeptMap.size());
        AdminDept adminDept;
        while (Objects.nonNull(deptId) && ((adminDept = adminDeptMap.get(deptId)) != null)) {
            deptIds.add(deptId.toString());
            deptId = Objects.nonNull(adminDept.getPid()) ? Long.valueOf(adminDept.getPid()) : null;
        }
        return String.join(",", deptIds);
    }

    /**
     * 获取部门名称树
     *
     * @param deptId
     * @param adminDeptMap
     * @return
     */
    private String getOwnerUserDeptNameTree(Long deptId, Map<Long, AdminDept> adminDeptMap) {
        if (Objects.isNull(deptId) || Objects.isNull(adminDeptMap)) {
            return null;
        }
        List<String> deptNames = Lists.newArrayListWithCapacity(adminDeptMap.size());
        AdminDept adminDept;
        while (Objects.nonNull(deptId) && ((adminDept = adminDeptMap.get(deptId)) != null)) {
            deptNames.add(adminDept.getName());
            deptId = Objects.nonNull(adminDept.getPid()) ? Long.valueOf(adminDept.getPid()) : null;
        }
        return String.join("-", deptNames);
    }

    /**
     *
     * 保存客户变更日志
     *   `customer_id` bigint(20) NOT NULL COMMENT '客户id',
     *   `type` tinyint(4) NOT NULL COMMENT '变更类型：0.公海, 1.部门池, 2.考察库BD, 3.关联库BD',
     *   `bd_staff_id` bigint(20) NOT NULL COMMENT 'BD的员工id',
     *   `bd_staff_account` varchar(50) NOT NULL COMMENT 'bd的员工域账号',
     *   `dept_id` bigint(20) NOT NULL COMMENT '部门id',
     *   `dept_tree` varchar(200) NOT NULL COMMENT '所属部门层级树',
     *   `operator` varchar(50) DEFAULT NULL COMMENT '操作人'
     * @param type 变更类型：0.公海, 1.部门, 2.考察库BD，3.关联库BD
     * @param customerId 客户id
     * @param userId BD的员工id
     * @param deptId 部门id
     * @param operatorId 所属部门层级树
     */
    public BigInteger saveCustomerChangeLog(Integer type,Long customerId,Long userId, Long deptId, Long operatorId){
        if (customerId == null){
            throw new CrmException("客户id 不能为空");
        }
        if (type == null){
            throw new CrmException("变更类型 不能为空");
        }
        CrmCustomerBelongChangeLog crmCustomerBelongChangeLog = new CrmCustomerBelongChangeLog();
        crmCustomerBelongChangeLog.setCustomerId(customerId);
        crmCustomerBelongChangeLog.setType(type);
        crmCustomerBelongChangeLog.setOperator(getStaffAccount(operatorId));

        switch (type){
            //公海
            case 0:
                crmCustomerBelongChangeLog.setDeptId(getOnlineBusinessDept());
                crmCustomerBelongChangeLog.setDeptTree(getDeptJsonTree(crmCustomerBelongChangeLog.getDeptId(),getAllDept()));
                break;
            //部门
            case 1:
                if (deptId == null){
                    throw new CrmException("deptId 不能为空");
                }
                crmCustomerBelongChangeLog.setDeptId(deptId);
                crmCustomerBelongChangeLog.setDeptTree(getDeptJsonTree(deptId,getAllDept()));
                break;
            //考察库BD
            case 2:
            //关联库BD
            case 3:
                AdminUser adminUser = getAdminUser(userId);
                crmCustomerBelongChangeLog.setBdStaffId(adminUser.getUserId());
                crmCustomerBelongChangeLog.setBdStaffAccount(adminUser.getUsername());
                crmCustomerBelongChangeLog.setDeptId(Long.valueOf(adminUser.getDeptId()));
                crmCustomerBelongChangeLog.setDeptTree(getDeptJsonTree(Long.valueOf(adminUser.getDeptId()),getAllDept()));
                break;
            default:
                throw new CrmException("type 值非法：" + type);
        }

        crmCustomerBelongChangeLog.save();

        return crmCustomerBelongChangeLog.getId();
    }

    /**
     *
     * 保存商机变更日志
     *   `businessId` bigint(20) NOT NULL COMMENT '客户id',
     *   `type` tinyint(4) NOT NULL COMMENT '变更类型：0.公海, 1.BD, 2.部门',
     *   `bd_staff_id` bigint(20) NOT NULL COMMENT 'BD的员工id',
     *   `bd_staff_account` varchar(50) NOT NULL COMMENT 'bd的员工域账号',
     *   `dept_id` bigint(20) NOT NULL COMMENT '部门id',
     *   `dept_tree` varchar(200) NOT NULL COMMENT '所属部门层级树',
     *   `operator` varchar(50) DEFAULT NULL COMMENT '操作人'
     * @param type 变更类型：0.公海, 1.BD, 2.部门
     * @param businessId 商机id
     * @param userId BD的员工id
     * @param deptId 部门id
     * @param operatorId 所属部门层级树
     */
    public void saveBusinessChangeLog(Integer type, Long businessId, Long userId, Long deptId, Long operatorId){
        if (businessId == null){
            throw new CrmException("商机id 不能为空");
        }
        if (type == null){
            throw new CrmException("变更类型 不能为空");
        }
        CrmBusinessBelongChangeLog crmBusinessBelongChangeLog = new CrmBusinessBelongChangeLog();
        crmBusinessBelongChangeLog.setBusinessId(businessId);
        crmBusinessBelongChangeLog.setType(type);
        crmBusinessBelongChangeLog.setOperator(getStaffAccount(operatorId));

        switch (type){
            //公海
            case 0:
                crmBusinessBelongChangeLog.setDeptId(getOnlineBusinessDept());
                crmBusinessBelongChangeLog.setDeptTree(getDeptJsonTree(crmBusinessBelongChangeLog.getDeptId(),getAllDept()));
                break;
            //BD
            case 1:
                AdminUser adminUser = getAdminUser(userId);
                crmBusinessBelongChangeLog.setBdStaffId(adminUser.getUserId());
                crmBusinessBelongChangeLog.setBdStaffAccount(adminUser.getUsername());
                crmBusinessBelongChangeLog.setDeptId(Long.valueOf(adminUser.getDeptId()));
                crmBusinessBelongChangeLog.setDeptTree(getDeptJsonTree(Long.valueOf(adminUser.getDeptId()),getAllDept()));
                break;
            //部门
            case 2:
                if (deptId == null){
                    throw new CrmException("deptId 不能为空");
                }
                crmBusinessBelongChangeLog.setDeptId(deptId);
                crmBusinessBelongChangeLog.setDeptTree(getDeptJsonTree(deptId,getAllDept()));
                break;
            default:
                throw new CrmException("type 值非法：" + type);
        }

        crmBusinessBelongChangeLog.save();
    }

    /**
     * 保存bd部门变化日志
     * @param userId 用户id
     * @param deptId 部门id
     * @param operatorId 操作人id
     */
    public void saveBdDeptChangeLog(Long userId, Long deptId, Long operatorId){
        AdminUser adminUser = getAdminUser(userId);
        String deptJsonTree = getDeptJsonTree(deptId, getAllDept());

        CrmBdDeptChangeLog crmBdDeptChangeLog = new CrmBdDeptChangeLog();
        crmBdDeptChangeLog.setBdStaffId(userId);
        crmBdDeptChangeLog.setBdStaffAccount(adminUser.getUsername());
        crmBdDeptChangeLog.setDeptId(deptId);
        crmBdDeptChangeLog.setDeptTree(deptJsonTree);
        crmBdDeptChangeLog.setOperator(getStaffAccount(operatorId));

        crmBdDeptChangeLog.save();
    }

    /**
     * 当部门层级变化，也就是pid变化时，记录此日志
     * @param adminDept
     * @param operatorId
     * @param staffAccount
     * @param allDeptMap
     * @param pidDeptMap
     */
    public void saveDeptChangeLog(AdminDept adminDept, Long operatorId,String staffAccount,Map<Long, AdminDept> allDeptMap,Map<Long, List<AdminDept>> pidDeptMap){
        if (adminDept == null){
            throw new CrmException("部门对象不能为空");
        }
        if (StringUtils.isBlank(staffAccount)){
            staffAccount = getStaffAccount(operatorId);
        }
        if (allDeptMap == null){
            allDeptMap = getAllDept();
        }
        CrmDeptChangeLog crmDeptChangeLog = new CrmDeptChangeLog();
        crmDeptChangeLog.setOperator(staffAccount);
        crmDeptChangeLog.setDeptId(adminDept.getDeptId());
        crmDeptChangeLog.setLevel(adminDept.getDeptLevel());
        crmDeptChangeLog.setPDeptId(Long.valueOf(adminDept.getPid()));
        crmDeptChangeLog.setStatus(adminDept.getIsDelete());
        crmDeptChangeLog.setDeptTree(getDeptJsonTree(adminDept.getDeptId(),allDeptMap));

        crmDeptChangeLog.save();

        //获取父节点是此deptId的部门，并添加日志
        List<AdminDept> deptList = pidDeptMap.get(adminDept.getDeptId());
        if (deptList != null){
            for (AdminDept dept : deptList){
                saveDeptChangeLog(dept, operatorId,staffAccount,allDeptMap,pidDeptMap);
            }
        }
    }

    /**
     * 新增部门时，记录此日志
     * @param deptList
     */
    public void saveDeptAddLog(List<AdminDept> deptList){
        if (deptList == null){
            return;
        }
        CrmDeptChangeLog crmDeptChangeLog = new CrmDeptChangeLog();
        deptList.forEach(o -> {
            crmDeptChangeLog.clear();
            crmDeptChangeLog.setOperator("system");
            crmDeptChangeLog.setDeptId(o.getDeptId());
            crmDeptChangeLog.setLevel(o.getDeptLevel());
            crmDeptChangeLog.setPDeptId(Long.valueOf(o.getPid()));
            crmDeptChangeLog.setStatus(o.getIsDelete());
            crmDeptChangeLog.setDeptTree(getDeptJsonTree(o.getDeptId(),getAllDept()));

            crmDeptChangeLog.save();
        });
    }

    /**
     * 根据userID获取域账号
     * @param userId
     * @return
     */
    private String getStaffAccount(Long userId){
        //当是系统操作时，返回system
        if (userId == null){
            return "system";
        }
        AdminUser adminUser = AdminUser.dao.findFirst(Db.getSql("admin.user.queryBaseUserMsgByUserId"),userId);
        if (adminUser == null){
            log.error("userId:{}，查询不到用户信息",userId);
            return null;
        }

        return adminUser.getUsername();
    }

    /**
     * 根据userID获取用户信息
     * @param userId
     * @return
     */
    private AdminUser getAdminUser(Long userId){
        //当是系统操作时，返回system
        if (userId == null){
            throw new CrmException("userId 不能为空");
        }
        AdminUser adminUser = AdminUser.dao.findFirst(Db.getSql("admin.user.queryBaseUserMsgByUserId"),userId);
        if (adminUser == null){
            throw new CrmException("userId:" + userId + "，查询不到用户信息");
        }

        return adminUser;
    }

    /**
     * 获取在线运营事业部的dept_id
     * @return
     */
    private Long getOnlineBusinessDept(){
        //获取在线运营事业部部门ID
        String onlineBusinessDeptId = adminConfigService.getConfig(CrmConstant.ONLINE_BUSINESS_DEPT_ID, "在线运营事业部ID");
        if (StringUtils.isBlank(onlineBusinessDeptId)){
            throw new CrmException("adminConfig表中在线运营事业部ID为空");
        }

        return Long.valueOf(onlineBusinessDeptId);
    }

    /**
     * 获取所有部门Map
     * @return
     */
    private Map<Long, AdminDept> getAllDept(){
        List<AdminDept> adminDeptList = adminDeptService.getAllAdminDepts();
        Map<Long, AdminDept> adminDeptHashMap = Maps.newLinkedHashMapWithExpectedSize(350);
        for (AdminDept adminDept : adminDeptList) {
            adminDeptHashMap.put(adminDept.getDeptId(), adminDept);
        }
        return adminDeptHashMap;
    }

    /**
     * 获取Pid下所有部门的map
     * @return
     */
    private Map<Long, List<AdminDept>> getPidDeptMap(){
        List<AdminDept> adminDeptList = adminDeptService.getAllAdminDepts();
        Map<Long, List<AdminDept>> pidDeptMap = Maps.newLinkedHashMapWithExpectedSize(50);
        for (AdminDept adminDept : adminDeptList) {
            List<AdminDept> deptList = pidDeptMap.getOrDefault(Long.valueOf(adminDept.getPid()), new ArrayList<>());
            deptList.add(adminDept);

            pidDeptMap.put(Long.valueOf(adminDept.getPid()),deptList);
        }
        return pidDeptMap;
    }

    /**
     * 获取部门Json字符串
     * {"level1":{"id":0,"name":"千寻位置"},"level2":{"id":123,"name":"在线运营事业部"},"level3":{"id":456,"name":"用户运营组"}}
     * @param deptId
     * @param adminDeptMap
     * @return
     */
    private String getDeptJsonTree(Long deptId, Map<Long, AdminDept> adminDeptMap) {
        if (deptId == null) {
            throw new CrmException("部门Id不能为空");
        }
        List<JSONObject> deptList = Lists.newArrayList();

        AdminDept adminDept;
        while (Objects.nonNull(deptId) && (adminDept = adminDeptMap.get(deptId)) != null) {

            JSONObject jsonObject = new JSONObject();
            jsonObject.put("id",deptId);
            jsonObject.put("name",adminDept.getName());
            deptList.add(jsonObject);

            deptId = Objects.nonNull(adminDept.getPid()) ? Long.valueOf(adminDept.getPid()) : null;
        }

        JSONObject jsonObject = new JSONObject(true);
        int size = deptList.size();
        int j = 1;
        for (int i = size; i > 0; i--) {
            jsonObject.put("level" + j, deptList.get(i - 1));
            j++;
        }

        return jsonObject.toJSONString();
    }

}
