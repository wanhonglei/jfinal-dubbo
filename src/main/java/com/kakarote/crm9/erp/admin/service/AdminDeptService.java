package com.kakarote.crm9.erp.admin.service;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.jfinal.aop.Aop;
import com.jfinal.aop.Inject;
import com.jfinal.kit.Kv;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Record;
import com.kakarote.crm9.common.constant.BaseConstant;
import com.kakarote.crm9.erp.admin.entity.AdminDept;
import com.kakarote.crm9.erp.crm.constant.CrmConstant;
import com.kakarote.crm9.erp.crm.cron.CrmBaseDataCron;
import com.kakarote.crm9.utils.BaseUtil;
import com.kakarote.crm9.utils.R;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Admin Dept Service
 *
 * @author yue.li
 * @since 2019/11/20 14:42
 */
@Slf4j
public class AdminDeptService {

    @Inject
    private CrmBaseDataCron crmBaseDataCron;

    private CopyOnWriteArraySet<String> businessDeptList = new CopyOnWriteArraySet<>();

    public void setBusinessDeptList(List<String> businessDeptList) {
        this.businessDeptList.clear();
        this.businessDeptList.addAll(businessDeptList);
    }

    public R setDept(AdminDept adminDept) {
        boolean bol;
        if (adminDept.getDeptId() == null) {
            bol = adminDept.save();
        } else {
            if (adminDept.getPid() != null && adminDept.getPid() != 0) {
                List<Record> topDeptList = queryDeptTree("update", adminDept.getDeptId().intValue());
                boolean isContain = false;
                for (Record record : topDeptList) {
                    if (record.getInt("id").equals(adminDept.getPid())) {
                        isContain = true;
                        break;
                    }
                }
                if (!isContain) {
                    return R.error("该部门的下级部门不能设置为上级部门");
                }
            }
            bol = adminDept.update();
        }
        return R.isSuccess(bol, "设置失败");
    }

    public List<Record> queryDeptTree(String type, Integer id) {
        List<Record> allDeptList = new ArrayList<>();
        List<Record> adminDeptList = Db.find("select dept_id as id,name,pid from 72crm_admin_dept order by dept_level");
        List<Record> recordList = buildTreeBy2Loop(adminDeptList, -1, allDeptList);
        if (StrUtil.isNotBlank(type) && "tree".equals(type)) {
            return recordList;
        } else if (StrUtil.isBlank(type) || "save".equals(type)) {
            return adminDeptList;
        } else if (StrUtil.isNotBlank(type) && "update".equals(type)) {
            return queryTopDeptList(id);
        } else {
            return new ArrayList<>();
        }
    }

    /**
     * 查询一级部门信息
     *
     * @return list of first level departments
     */
    public List<Record> queryFirstLevelDept() {
        return Db.query(Db.getSql("admin.dept.queryFirstLevelDept"));
    }

    /**
     * 查询可设置为上级的部门
     */
    private List<Record> queryTopDeptList(Integer deptId) {
        List<Record> recordList = Db.find("select dept_id as id,name,pid from 72crm_admin_dept");
        List<Long> subDeptList = Aop.get(AdminUserService.class).queryChileDeptIds(deptId, BaseConstant.AUTH_DATA_RECURSION_NUM);
        recordList.removeIf(record -> subDeptList.contains(record.getLong("id")));
        recordList.removeIf(record -> record.getInt("id").equals(deptId));
        return recordList;
    }

    /**
     * 通过userId查询权限内部门
     *
     * @param userId 用户ID
     * @return 权限内部门
     * @author zhangzhiwei
     */
    public List<Record> queryDeptByAuth(Long userId) {
        //查询用户数据权限，从高到低排序
        List<Integer> list = Db.query(Db.getSql("admin.role.queryDataTypeByUserId"), userId);
        List<Record> adminDepts = new ArrayList<>();
        if (list.size() == 0) {
            return adminDepts;
        }
        //拥有最高数据权限
        if (list.contains(5)) {
            return Db.find("select dept_id as id,name,pid from 72crm_admin_dept");
        } else {
            adminDepts.add(Db.findFirst("select dept_id as id,name,pid from 72crm_admin_dept where dept_id=?", BaseUtil.getDeptId()));
            if (list.contains(4)) {
                adminDepts.addAll(queryDeptByParentDept(BaseUtil.getDeptId(), BaseConstant.AUTH_DATA_RECURSION_NUM));
            }
            if (list.contains(2)) {
                adminDepts.addAll(queryDeptByParentUser(userId, BaseConstant.AUTH_DATA_RECURSION_NUM));
            }
        }
        HashSet<Record> hashSet = new HashSet<>(adminDepts);
        adminDepts.clear();
        adminDepts.addAll(hashSet);
        return adminDepts;
    }

    public List<Record> queryDeptByParentDept(Integer deptId, Integer deepness) {
        List<Record> recordList = new ArrayList<>();
        if (deepness > 0) {
            List<Record> records = Db.find("select dept_id as id,name,pid from 72crm_admin_dept where pid=?", deptId);
            if(records != null) {
                recordList.addAll(records);
                records.forEach(record -> recordList.addAll(queryDeptByParentDept(record.getInt("id"), deepness - 1)));
            }
        }
        return recordList;
    }

    private List<Record> queryDeptByParentUser(Long userId, Integer deepness) {
        List<Record> recordList = new ArrayList<>();
        if (deepness > 0) {
            List<Record> records = Db.find("SELECT a.dept_id AS id,a.name,a.pid,b.user_id FROM 72crm_admin_dept as a LEFT JOIN 72crm_admin_user as b on a.dept_id=b.dept_id WHERE b.parent_id = ?", userId);
            recordList.addAll(records);
            records.forEach(record -> recordList.addAll(queryDeptByParentUser(record.getLong("user_id"), deepness - 1)));
        }
        return recordList;
    }

    private List<Record> buildTreeBy2Loop(List<Record> treeNodes, Integer root, List<Record> allDeptList) {
        List<Record> trees = new ArrayList<>();
        for (Record node : treeNodes) {
            if (root.equals(node.getInt("pid"))) {
                node.set("level", 1);
                node.set("label", node.getStr("name"));
                trees.add(node);
                allDeptList.add(node);
            }
            List<Record> childTrees = new ArrayList<>();
            for (Record treeNode : treeNodes) {
                if (Objects.nonNull(node.getInt("level")) && node.getInt("id").equals(treeNode.getInt("pid"))) {
                    treeNode.set("level", node.getInt("level") + 1);
                    treeNode.set("label", treeNode.getStr("name"));
                    childTrees.add(treeNode);
                    allDeptList.add(treeNode);
                }
            }
            if (childTrees.size() != 0) {
                node.set("children", childTrees);
            }
        }
        return trees;
    }

    @Deprecated
    public R deleteDept(String id) {
       /* Integer userCount = Db.queryInt("select count(*) from 72crm_admin_user where dept_id = ?", id);
        if (userCount > 0) {
            return R.error("该部门下有员工，不能删除！");
        }
        Integer childDeptCount = Db.queryInt("select count(*) from 72crm_admin_dept where pid = ?", id);
        if (childDeptCount > 0) {
            return R.error("该部门下有下级部门，不能删除！");
        }
        int delete = Db.delete("delete from 72crm_admin_dept where dept_id = ?", id);
        return delete > 0 ? R.ok() : R.error();*/

       return R.error();
    }

    /**
     * Return all department dept_id.
     *
     * @return all department ids
     */
    public List<Record> getAllDeptIds() {
        return Db.find(Db.getSql("admin.dept.getAllDeptIds"));
    }

    /**
     * Return true if update successful.
     *
     * @param deptIds dept id list
     * @param value delete flag
     * @return true if update successfully
     */
    public boolean batchUpdateDeleteFlag(List<Long> deptIds, int value) {
        if (CollectionUtils.isEmpty(deptIds)) {
            return false;
        }
        return Db.update(Db.getSqlPara("admin.dept.updateDeleteFlag", Kv.by("value", value).set("deptIds", deptIds))) > 0;
    }

    public boolean batchInsertDepts(List<AdminDept> depts) {
        if (CollectionUtils.isEmpty(depts)) {
            return false;
        }
        return Db.batchSave("72crm_admin_dept", convertDeptModelToRecord(depts), depts.size()).length > 0;
    }

    public boolean batchUpdateDepts(List<AdminDept> depts) {
        if (CollectionUtils.isEmpty(depts)) {
            return false;
        }
        return Db.batchUpdate("72crm_admin_dept","dept_id", convertDeptModelToRecord(depts), depts.size()).length > 0;
    }

    /**
     * Convert AdminDept list to Record list.
     *
     * @param depts
     * @return
     */
    private List<Record> convertDeptModelToRecord(List<AdminDept> depts) {
        if (CollectionUtils.isNotEmpty(depts)) {
            return depts.stream().map(Model::toRecord).collect(Collectors.toList());
        }
        return Lists.newArrayList();
    }

    /**
     * 获取部门对应的事业部部门
     * @author  liyue
     * @param deptId 部门id
     * @return
     */
    public String getBusinessDepartmentByDeptId(String deptId) {
        List<AdminDept> adminDeptList = BaseUtil.getAdminDeptList();
        if (adminDeptList == null) {
            crmBaseDataCron.run();
            adminDeptList = BaseUtil.getAdminDeptList();

        }

        return getBusinessDeptId(adminDeptList, deptId);
    }

    /**
     * 获取部门对应的事业部部门new
     *
     * @param deptId
     * @return
     */
    public String getBusinessDepartmentByDeptIdNew(String deptId) {
        List<AdminDept> adminDeptList = getAllAdminDeptList();
        return getBusinessDeptIdNew(adminDeptList, deptId);
    }

    /**
     * 获取部门对应的事业部部门PO
     *
     * @param deptId
     * @return
     */
    public AdminDept getBusinessDepartmentPOByDeptId(Long deptId) {
        if (Objects.isNull(deptId)) {
            return null;
        }
        List<AdminDept> adminDeptList = getAllAdminDeptList();
        Map<Long, AdminDept> deptMap = Maps.newHashMapWithExpectedSize(adminDeptList.size());
        for (AdminDept adminDept : adminDeptList) {
            deptMap.put(adminDept.getDeptId(), adminDept);
        }
        return getBusinessDeptByMap(deptMap, deptId);
    }

    /**
     * 查询所有的部门
     *
     * @return
     */
    public List<AdminDept> getAllAdminDepts() {
        List<AdminDept> adminDeptList = BaseUtil.getAdminDeptList();
        if (adminDeptList == null) {
            crmBaseDataCron.run();
            adminDeptList = BaseUtil.getAdminDeptList();
        }
        return adminDeptList;
    }

    /**
     * 获取部门对应的事业部名称(最近事业部)
     * @author  liyue
     * @param adminDeptList 部门集合
     * @param deptId 部门id
     * @return
     */
    private String getBusinessDeptId(List<AdminDept> adminDeptList, String deptId) {
        String result = null;
        for (AdminDept adminDept : adminDeptList) {
            if (Objects.nonNull(adminDept.getDeptId()) && String.valueOf(adminDept.getDeptId()).equals(deptId)) {
                // 部门为一级或者二级直接返回
                if (CrmConstant.ONE_DEPT_LEVEL.equals(adminDept.getDeptLevel()) || CrmConstant.TWO_DEPT_LEVEL.equals(adminDept.getDeptLevel())) {
                    result = deptId;
                    break;
                } else {
                    // 商机组部门能查询到,则返回商机组部门
                    if (isExistBusinessGroup(deptId)) {
                        return deptId;
                    } else {
                        return getBusinessDeptId(adminDeptList, Objects.nonNull(adminDept.getPid()) ? String.valueOf(adminDept.getPid()) : null);
                    }
                }
            }
        }
        return result;
    }

    /**
     * 获取部门对应的事业部名称(最近事业部) 优化方法
     *
     * @param adminDepts
     * @param deptId
     * @return
     */
    public String getBusinessDeptIdNew(List<AdminDept> adminDepts, String deptId) {
        Map<String, AdminDept> map = Maps.newHashMapWithExpectedSize(adminDepts.size());
        for (AdminDept adminDept : adminDepts) {
            map.put(adminDept.getDeptId().toString(), adminDept);
        }
        return getBusinessDeptIdByMap(map, deptId);
    }

    private String getBusinessDeptIdByMap(Map<String, AdminDept> map, String deptId) {
        if (StringUtils.isBlank(deptId)) {
            return null;
        }
        AdminDept adminDept = map.get(deptId);
        if (adminDept == null) {
            return null;
        }
        if (CrmConstant.ONE_DEPT_LEVEL.equals(adminDept.getDeptLevel()) || CrmConstant.TWO_DEPT_LEVEL.equals(adminDept.getDeptLevel())) {
            return adminDept.getDeptId().toString();
        }
        if (isExistBusinessGroup(deptId)) {
            return deptId;
        }
        return getBusinessDeptIdByMap(map, adminDept.getPid().toString());
    }

    private AdminDept getBusinessDeptByMap(Map<Long, AdminDept> map, Long deptId) {
        if (Objects.isNull(deptId)) {
            return null;
        }
        AdminDept adminDept = map.get(deptId);
        if (Objects.isNull(adminDept)) {
            return null;
        }
        if (CrmConstant.ONE_DEPT_LEVEL.equals(adminDept.getDeptLevel()) || CrmConstant.TWO_DEPT_LEVEL.equals(adminDept.getDeptLevel())) {
            return adminDept;
        }
        if (isExistBusinessGroup(String.valueOf(deptId))) {
            return adminDept;
        }
        return getBusinessDeptByMap(map, Long.valueOf(adminDept.getPid()));
    }


    /**
     * 判断部门是否在商机组部门中
     * @author  liyue
     * @param deptId 部门id
     * @return
     */
    public boolean isExistBusinessGroup(String deptId) {
//        List<String> businessDeptList = crmbusinessService.getBusinessGroup();
        if (businessDeptList == null){
            crmBaseDataCron.run();
        }

        return businessDeptList != null && businessDeptList.contains(deptId);
    }

    /**
     * @author  liyue
     * 获取对应的事业部其下部门
     */
    public List<String> getAllDeptsByBusinessDepartmentId(String deptId){
        List<String> resultList = new ArrayList<>();
        List<Record> recordList = Db.find(Db.getSql("admin.dept.queryDeptInfoByPid"), deptId);
        if(recordList != null && recordList.size() >0){
            for(Record record:recordList){
                resultList.add(record.getStr("dept_id"));
            }
        }
        return resultList;
    }
    
    /**
     * 获取下属部门（只包含第一级）
     * @param deptId
     * @return
     */
    public List<Record> getSubDeptsByBusinessDepartmentId(Long deptId){
        return Db.find(Db.getSql("admin.dept.queryDeptInfoByPid"), deptId);
    }

    /***
     * 获取本部门及下属部门
     * @author yue.li
     * @param deptId 部门ID
     * @return
     */
    public List<Integer> getDeptAndBranchDept(Integer deptId){
        List<Integer> deptIds = new ArrayList<>();
        if(deptId != null){
            List<Record> records = queryDeptByParentDept(deptId, BaseConstant.AUTH_DATA_RECURSION_NUM);
            /*添加本部门所有下属部门ID*/
            records.forEach(record -> deptIds.add(record.getInt("id")));
            /*添加本部门ID*/
            deptIds.add(deptId);
        }
        return deptIds;
    }

    /**
     * 查询所有部门信息
     * @author yue.li
     * @return
     */
    public List<Record> getAllDepts()  {
        return Db.find(Db.getSql("admin.dept.deptSql"));
    }

    /**
     * 根据部门名称查询部门ID
     * @author yue.li
     * @return
     */
    public Integer getDeptIdByDeptName(String deptName) {
        Integer deptId = null;
        Record deptRecord = Db.findFirst(Db.getSql("admin.dept.queryDeptInfoByDeptName"), deptName);
        if(deptRecord != null){
            deptId = deptRecord.getInt("dept_id");
        }
        return deptId;
    }

    /**
     * 根据部门id获取部门信息
     *
     * @param deptId department id
     * @return {@code AdminDept}
     */
    public AdminDept getDeptByDeptId(Integer deptId) {
        if (Objects.isNull(deptId)) {
            return null;
        }
        Record record = Db.findFirst(Db.getSql("admin.dept.getDeptByDeptId"), deptId);
        if (Objects.nonNull(record)) {
            return new AdminDept()._setAttrs(record.getColumns());
        } else {
            return null;
        }
    }

    /**
     * 查询！！事业部！！部门树
     * @return
     */
    public Record queryBizDeptTree() {
        Record root;
        HashMap<Integer, List<Record>> deptMap = queryDeptList().stream().reduce(new HashMap<>(), (result, dept) -> {
            List<Record> list;
            Integer pid = dept.getInt("pid");
            if (result.containsKey(pid)) {
                list = result.get(pid);
            } else {
                list = new ArrayList<>();
                result.put(pid, list);
            }
            list.add(dept);
            return result;
        }, (integerListHashMap, integerListHashMap2) -> null);
        if (deptMap.isEmpty()) {
            return null;
        }
        Integer rootPid = -1;
        List<Record> rootList = deptMap.get(rootPid);
        if (CollectionUtils.isEmpty(rootList)) {
            return null;
        }
        root = rootList.get(0);
        List<Record> businessDeptList = queryBusinessDeptList();
        if (CollectionUtils.isNotEmpty(businessDeptList)) {
            root.set("children", businessDeptList);
            businessDeptList.forEach(dept -> buildBizTree(dept, deptMap));
        }
        return root;
    }

    private void buildBizTree(Record node, HashMap<Integer, List<Record>> deptMap) {
        Integer pid = node.getInt("dept_id");
        if (deptMap.containsKey(pid)) {
            List<Record> childrenDept = deptMap.get(pid);
            node.set("children", childrenDept);
            childrenDept.forEach(childDept -> buildBizTree(childDept, deptMap));
        }
    }

    public List<Record> queryBusinessDeptList() {
        return Db.find(Db.getSql("admin.dept.queryBusinessDeptList"));
    }

    public List<Record> queryDeptList() {
        return Db.find(Db.getSql("admin.dept.queryDeptList"));
    }

    public List<AdminDept> listByPid(Integer pid) {
        return AdminDept.dao.find(Db.getSql("admin.dept.listDeptByPid"), pid);
    }

    /**
     * 查询部门所有子部门的ID(包含当前部门)
     * @param deptId 部门ID
     * @return
     */
    public List<Long> queryAllSonDeptIds(Long deptId) {
        List<Long> result = new ArrayList<>();
        if (Objects.isNull(deptId)) {
            return result;
        }
        result.add(deptId);
        List<AdminDept> children = listByPid(deptId.intValue());
        if (CollectionUtils.isNotEmpty(children)) {
            List<Long> childrenIds = children.stream().map(AdminDept::getDeptId).collect(Collectors.toList());
            childrenIds.forEach(childDeptId-> result.addAll(queryAllSonDeptIds(childDeptId)));
        }
        return result;
    }

    /**
     * 查询所有的部门信息
     *
     * @return
     */
    public List<AdminDept> getAllAdminDeptList() {
        List<AdminDept> adminDepts = AdminDept.dao.findAll();
        if (CollectionUtils.isEmpty(adminDepts)) {
            return Collections.emptyList();
        }
        return adminDepts;
    }

    /**
     * 获取所有部门的部门ID与部门映射MAP
     * @return
     */
    public Map<Long, AdminDept> getAllAdminDeptMap() {
        return getAllAdminDeptList().stream().collect(Collectors.toMap(AdminDept::getDeptId, Function.identity()));
    }

    /**
     * 获取部门路径
     * @param deptId 部门ID
     * @return
     */
    public String getDeptNamePath(Long deptId) {
        if (Objects.isNull(deptId)) {
            return null;
        }
        return getDeptNamePath(deptId, getAllAdminDeptMap());
    }

    /**
     * 获取部门路径
     * @param deptId 部门ID
     * @param adminDeptMap 部门ID与部门映射MAP
     * @return
     */
    public String getDeptNamePath(Long deptId, Map<Long, AdminDept> adminDeptMap) {
        if (Objects.isNull(deptId)) {
            return null;
        }
        List<String> deptNames = new ArrayList<>(4);
        AdminDept adminDept;
        while (Objects.nonNull(adminDept = adminDeptMap.get(deptId))) {
            deptNames.add(adminDept.getName());
            deptId = Objects.nonNull(adminDept.getPid()) ? Long.valueOf(adminDept.getPid()) : null;
        }
        if (CollectionUtils.isNotEmpty(deptNames)) {
            Collections.reverse(deptNames);
        }
        return String.join("-", deptNames);
    }

    /**
     * 获取父部门ID
     * @param deptId
     * @return
     */
    public Long getParentDeptId(Long deptId) {
        if (Objects.isNull(deptId)) {
            return null;
        }
        AdminDept adminDept = getAllAdminDeptMap().get(deptId);
        if (Objects.isNull(adminDept)) {
            return null;
        }
        return Long.valueOf(adminDept.getPid());
    }

    /**
     * 获取部门名称树，示例：智能驾驶事业部-业务扩展-智慧交通
     *
     * @param deptId 部门编号
     * @return
     */
    public String getDeptNameTree(Long deptId) {
        List<AdminDept> adminDepts = getAllAdminDepts();
        Map<Long, AdminDept> adminDeptHashMap = Maps.newHashMapWithExpectedSize(adminDepts.size());
        for (AdminDept adminDept : adminDepts) {
            adminDeptHashMap.put(adminDept.getDeptId(), adminDept);
        }
        if (Objects.isNull(deptId)) {
            return null;
        }
        List<String> deptNames = Lists.newArrayListWithCapacity(adminDeptHashMap.size());
        AdminDept adminDept;
        while (Objects.nonNull(deptId) && ((adminDept = adminDeptHashMap.get(deptId)) != null)) {
            Integer pid = adminDept.getPid();
            if (Objects.nonNull(pid) && adminDeptHashMap.containsKey(Long.valueOf(pid))) {
                deptNames.add(adminDept.getName());
            }
            deptId = Objects.nonNull(adminDept.getPid()) ? Long.valueOf(adminDept.getPid()) : null;
        }
        Collections.reverse(deptNames);
        return String.join("-", deptNames);
    }

    /**
     * 根据部门ID获取部门的信息
     * @author yue.li
     * @param deptId 部门ID
     */
    public AdminDept getDeptNameByDeptId(String deptId) {
        return AdminDept.dao.findFirst(Db.getSql("admin.dept.queryDeptInfoByDeptId"), deptId);
    }

    /**
     * 根据id查询对应事业部信息
     * @param deptId
     * @return
     */
    public AdminDept getBusinessDeptByDeptId(Long deptId){
        String businessDeptId = getBusinessDepartmentByDeptId(String.valueOf(deptId));
        return getDeptNameByDeptId(businessDeptId);
    }

    /**
     * 从事业部树中截取子树
     *
     * @param deptId
     * @return
     */
    public Record getFromBizDeptTree(Long deptId) {
        return getFromBizDeptTree(deptId, queryBizDeptTree());
    }

    /**
     * 从事业部树中截取子树
     *
     * @param deptId
     * @return
     */
    private Record getFromBizDeptTree(Long deptId, Record bizDeptTree) {
        if (Objects.isNull(deptId) || Objects.equals(deptId, bizDeptTree.getLong("dept_id"))) {
            return bizDeptTree;
        }
        List<Record> children = bizDeptTree.get("children");
        if (CollectionUtils.isNotEmpty(children)) {
            for (Record child : children) {
                Record record = getFromBizDeptTree(deptId, child);
                if (Objects.nonNull(record)) {
                    return record;
                }
            }
        }
        return null;
    }

    /**
     * 遍历部门树中所有部门的部门id
     * @param deptTree
     * @return
     */
    public List<Long> loopDeptTreeForDeptIdList(Record deptTree) {
        if (Objects.isNull(deptTree)) {
            return Collections.emptyList();
        }
        List<Long> result = new ArrayList<>(Collections.singletonList(deptTree.getLong("dept_id")));
        List<Record> children = deptTree.get("children");
        if (CollectionUtils.isNotEmpty(children)) {
            for (Record child : children) {
                result.addAll(loopDeptTreeForDeptIdList(child));
            }
        }
        return result;
    }

    /**
     * 获取离传递部门id最近的部门配置库容信息
     *
     * @param deptMap         部门map
     * @param deptCapacityMap 配置库容map
     * @param deptId          部门id
     * @return
     */
    public JSONObject getNearestDeptCapacity(Map<Long, Integer> deptMap, Map<Long, Integer> deptCapacityMap, Long deptId) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("capacity", null);
        jsonObject.put("deptId", null);

        if (deptCapacityMap == null || deptCapacityMap.isEmpty()) {
            log.error("部门库容信息表为空，部门池将无限制");
            return jsonObject;
        }
        if (deptMap == null || deptMap.isEmpty()) {
            log.error("程序内存部门树缓存异常，请检查代码");

            jsonObject.put("errorCode", 500);
            return jsonObject;
        }

        Integer capacity = deptCapacityMap.get(deptId);
        if (capacity != null) {
            jsonObject.put("capacity", capacity);
            jsonObject.put("deptId", deptId);
            return jsonObject;
        } else {
            if (!deptMap.containsKey(deptId)) {
                log.error("程序内存部门树缓存异常，不包含部门{}", deptId);

                jsonObject.put("errorCode", 500);
                return jsonObject;
            }
            //获取上级部门id
            deptId = Long.valueOf(deptMap.get(deptId));
            if (deptId == -1) {
                //找到顶级还未有，则返回null，表示无限制
                return jsonObject;
            } else {
                return getNearestDeptCapacity(deptMap, deptCapacityMap, deptId);
            }
        }
    }

    /**
     * 配置的所有部门库容信息
     *
     * @author yue.li
     */
    public Map<Long, Integer> getDeptCapacityMap() {
        List<Record> recordList = Db.find(Db.getSql("admin.dept.capacity.findTotalDeptCapacity"));
        return recordList.stream().collect(Collectors.toMap(o -> o.getLong("dept_id"), o -> o.getInt("capacity")));
    }

    /**
     * 查询公司部门Map
     *
     * @return
     */
    public Map<Long, Integer> getDeptMap() {
        //获取公司所有部门
        List<AdminDept> adminDeptList = BaseUtil.getAdminDeptList();
        if (adminDeptList == null) {
            crmBaseDataCron.run();
            adminDeptList = BaseUtil.getAdminDeptList();
        }
        return adminDeptList.stream().collect(Collectors.toMap(AdminDept::getDeptId, AdminDept::getPid));
    }
}