package com.kakarote.crm9.erp.admin.service;

import com.jfinal.aop.Inject;
import com.jfinal.kit.Kv;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.kakarote.crm9.erp.admin.entity.AdminUserCapacity;
import org.apache.commons.collections.CollectionUtils;

import java.util.*;

/**
 * @Author: haihong.wu
 * @Date: 2020/3/24 2:41 下午
 */
public class AdminUserCapacityService {

    @Inject
    private AdminDeptService adminDeptService;

    /**
     * 获取个人容量配置
     *
     * @param deptId
     * @param page
     * @param limit
     * @return
     */
    public Page<Record> getPersonalCapacity(Long deptId, Integer page, Integer limit) {
        Page<Record> result;
        //查询事业部树(由于本功能只供事业部的设置用，所以只需要事业部树就可以)
        Record bizDeptTree = adminDeptService.queryBizDeptTree();
        //根据事业部找到所有子部门ID
        List<Long> deptIds = findChildrenDeptIds(bizDeptTree, deptId);
        if (CollectionUtils.isEmpty(deptIds)) {
            result = new Page<>(null, page, limit, 0, 0);
        } else {
            //分页查询
            result = Db.paginate(page, limit,
                    Db.getSqlPara("admin.user.capacity.getPersonalCapacity", Kv.by("deptIds", deptIds)));
            //遍历拼接路径
            //将事业部树映射为事业部路径
            Map<Long, String> deptMapping = mapDept(bizDeptTree);
            result.getList().forEach(record -> {
                Long deptIdR = record.getLong("dept_id");
                record.set("deptName", deptMapping.get(deptIdR));
            });
        }
        return result;
    }

    /**
     * 获取所有子部门ID
     *
     * @param root   事业部树
     * @param deptId 目标部门ID
     * @return
     */
    private List<Long> findChildrenDeptIds(Record root, Long deptId) {
        if (Objects.isNull(deptId)) {
            //部门ID为空，传整棵树
            deptId = root.getLong("dept_id");
        }
        root = findTree(root, deptId);
        if (root != null) {
            //遍历树中所有的部门ID
            return loopDeptForDeptId(root);
        }
        return new ArrayList<>();
    }

    /**
     * 根据部门ID找出子部门树
     *
     * @param tree
     * @param deptId
     * @return
     */
    private Record findTree(Record tree, Long deptId) {
        if (Objects.equals(tree.getLong("dept_id"), deptId)) {
            return tree;
        }
        List<Record> children = tree.get("children");
        if (CollectionUtils.isNotEmpty(children)) {
            for (Record child : children) {
                Record res = findTree(child, deptId);
                if (res != null) {
                    return res;
                }
            }
        }
        return null;
    }

    /**
     * 映射部门树为部门ID-部门名称
     *
     * @param node
     * @return
     */
    private Map<Long, String> mapDept(Record node) {
        HashMap<Long, String> result = new HashMap<>();
        mapDept(node, result, new LinkedList<>());
        return result;
    }

    private void mapDept(Record node, Map<Long, String> map, LinkedList<String> nameLink) {
        Long deptId = node.getLong("dept_id");
        String deptName = node.getStr("name");
        //将当前节点名称加入到链表中
        nameLink.addLast(deptName);
        //将结果放入map
        map.put(deptId, joinLink(nameLink));
        //遍历children
        List<Record> children = node.get("children");
        if (CollectionUtils.isNotEmpty(children)) {
            for (Record child : children) {
                mapDept(child, map, nameLink);
                //每个子节点退出后都移除最后一个节点
                nameLink.removeLast();
            }
        }
    }

    /**
     * 拼接部门名称
     *
     * @param nameLink
     * @return
     */
    private String joinLink(LinkedList<String> nameLink) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < nameLink.size(); i++) {
            sb.append(nameLink.get(i));
            if (i != nameLink.size() - 1) {
                sb.append('-');
            }
        }
        return sb.toString();
    }

    /**
     * 遍历部门树
     *
     * @param dept 部门树
     * @return 树中所有部门的id
     */
    private List<Long> loopDeptForDeptId(Record dept) {
        List<Long> result = new ArrayList<>();
        result.add(dept.getLong("dept_id"));
        List<Record> children = dept.get("children");
        if (CollectionUtils.isNotEmpty(children)) {
            for (Record childDept : children) {
                result.addAll(loopDeptForDeptId(childDept));
            }
        }
        return result;
    }

    public void editPersonalCapacity(List<AdminUserCapacity> datas) {
        if (CollectionUtils.isNotEmpty(datas)) {
            Db.tx(() -> {
                datas.forEach(item -> {
                    //先更新，没更新到则插入
                    int count = Db.update(Db.getSqlPara("admin.user.capacity.update",Kv.by("userId",item.getUserId()).set("inspectCap",item.getInspectCap()).set("relateCap",item.getRelateCap())));
                    if (count <= 0) {
                        item.save();
                    }
                });
                return true;
            });
        }
    }

    /**
     * 查询某个客户的客户库类型
     *
     * @param customerId 客户id
     * @return
     */
    public Record searchCapacityByCustomerId(Long customerId) {
        return Db.findFirst(Db.getSqlPara("crm.customerExt.getCustomerStorageType", Kv.by("customerId", customerId)));
    }

}
