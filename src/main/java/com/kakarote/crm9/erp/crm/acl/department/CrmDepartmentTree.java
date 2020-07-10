package com.kakarote.crm9.erp.crm.acl.department;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.kakarote.crm9.erp.admin.entity.AdminDept;
import com.kakarote.crm9.erp.admin.entity.AdminUser;
import org.apache.commons.collections.CollectionUtils;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Crm Department Tree
 *
 * @author hao.fu
 * @since 2019/11/20 15:34
 */
public class CrmDepartmentTree {

    private static final int DEPT_LEVEL_ROOT = 1;

    private Node<AdminDept, List<AdminUser>> tree;

    /**
     * Private Constructor.
     */
    private CrmDepartmentTree() {
        List<AdminDept> departments = new AdminDept().findAll();
        List<AdminUser> staffs = new AdminUser().findAll();
        if (CollectionUtils.isEmpty(departments) || CollectionUtils.isEmpty(staffs)) {
            return;
        }

        // initialize all nodes
        List<Node<AdminDept, List<AdminUser>>> nodes = Lists.newArrayListWithCapacity(departments.size());
        for (AdminDept dept : departments) {
            List<AdminUser> members = staffs.stream().filter(item -> item.getDeptId() == dept.getDeptId().intValue()).collect(Collectors.toList());
            Map<AdminDept, List<AdminUser>> data = Maps.newHashMap();
            data.put(dept, members);

            Node node = new Node<>(data);
            node.setNodeDepth(dept.getDeptLevel());
            node.setNodeId(dept.getDeptId().intValue());
            nodes.add(node);
        }

        // build department tree
        buildTree(nodes, DEPT_LEVEL_ROOT);
    }

    private void buildTree(List<Node<AdminDept, List<AdminUser>>> nodes, int depth) {
        if (CollectionUtils.isEmpty(nodes)) {
            return;
        }
        List<Node> depthNodes = nodes.stream().filter(item -> depth == item.getNodeDepth()).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(depthNodes)) {
            // root depth is 1
            if (DEPT_LEVEL_ROOT == depth) {
                if (depthNodes.size() == 1) {
                    tree = depthNodes.get(0);
                }else {
                    depthNodes.stream().min(Comparator.comparing(Node::getNodeId)).ifPresent(o -> tree = o);
                }
            } else {
                List<Node<AdminDept, List<AdminUser>>> parents = nodes.stream().filter(item -> (depth - 1) == item.getNodeDepth()).collect(Collectors.toList());
                List<Node<AdminDept, List<AdminUser>>> childen = nodes.stream().filter(item -> depth == item.getNodeDepth()).collect(Collectors.toList());

                // parent node has only 1 node means root node
                if (parents.size() == 1) {
                    tree.addChildren(childen);
                } else {
                    for (Node<AdminDept, List<AdminUser>> parent : parents) {
                        List<Node<AdminDept, List<AdminUser>>> childrenNodes = childen.stream().filter(item -> parent.getNodeDataKey().getDeptId().intValue() == item.getNodeDataKey().getPid()).collect(Collectors.toList());
                        parent.addChildren(childrenNodes);
                    }
                }
            }
            buildTree(nodes, depth + 1);
        }
    }

    private Node<AdminDept, List<AdminUser>> getTree() {
        return tree;
    }

    private static class CrmDeptTreeHolder {
        private static Node<AdminDept, List<AdminUser>> INSTANCE = new CrmDepartmentTree().getTree();
    }

    /**
     * Get crm department tree.
     *
     * @return crm department root node
     */
    public static Node<AdminDept, List<AdminUser>> getCrmDepartmentTree() {
        Node<AdminDept, List<AdminUser>> instance = CrmDeptTreeHolder.INSTANCE;
        if (instance == null){
            Node<AdminDept, List<AdminUser>> tree = new CrmDepartmentTree().getTree();
            CrmDeptTreeHolder.INSTANCE =tree;
        }

        return instance;
    }

    /**
     * Get business unit.
     *
     * @return list of business unit nodes
     */
    public static List<Node<AdminDept, List<AdminUser>>> getBusinessUnitNodes() {
        return getCrmDepartmentTree().getChildren();
    }

    /**
     * Get all departments under QianXun root node.
     *
     * @return all departments
     */
    public static List<Node<AdminDept, List<AdminUser>>> getAllLeavesUnderRoot() {
        return getCrmDepartmentTree().getAllLeaves();
    }

    /**
     * Get all QianXun staffs.
     *
     * @return all QianXun staffs.
     */
    public static List<AdminUser> getAllStaffs() {
        List<Node<AdminDept, List<AdminUser>>> nodes = getAllLeavesUnderRoot();
        List<AdminUser> staffs = Lists.newArrayList();
        nodes.stream().filter(item -> item.getNodeDataValue() != null).forEach(item -> staffs.addAll(item.getNodeDataValue()));
        return staffs;
    }

    /**
     * Get department by department id.
     *
     * @param deptId department id
     * @return department node
     */
    public static Node<AdminDept, List<AdminUser>> getDepartmentById(Integer deptId) {
        return getCrmDepartmentTree().getLeafNodeById(deptId);
    }

    /**
     * Return direct staffs in the department.
     *
     * @param deptId department id
     * @return user list
     */
    public static List<AdminUser> getDirectStaffsInDepartment(Integer deptId) {
        Node<AdminDept, List<AdminUser>> node = getDepartmentById(deptId);
        return Objects.isNull(node) ? Collections.emptyList() : node.getNodeDataValue();
    }

    /**
     * Return all staffs in the department(include the specified dept and branch departments).
     *
     * @param deptId department id
     * @return user list
     */
    public static List<AdminUser> getAllStaffsInDepartment(Integer deptId) {
        List<Node<AdminDept, List<AdminUser>>> allNodes = getAllDeptsForSpecifiedDept(deptId);

        if (CollectionUtils.isEmpty(allNodes)) {
            return Collections.emptyList();
        }

        List<AdminUser> users = Lists.newArrayList();
        allNodes.stream().filter(item -> Objects.nonNull(item.getNodeDataValue())).forEach(item -> users.addAll(item.getNodeDataValue()));
        return users;
    }

    /**
     * Return departments under the specified department(does not include the specified department)
     *
     * @param deptId department id
     * @return node list
     */
    public static List<Node<AdminDept, List<AdminUser>>> getDeptsUnderSpecifiedDept(Integer deptId) {
        Node<AdminDept, List<AdminUser>> userDept = getDepartmentById(deptId);
        if (Objects.isNull(userDept)) {
            return Collections.emptyList();
        }
        return userDept.getAllLeaves();
    }

    /**
     * Return all departments for the specified dept id(include the specified department)
     *
     * @param deptId department id
     * @return node list
     */
    public static List<Node<AdminDept, List<AdminUser>>> getAllDeptsForSpecifiedDept(Integer deptId) {
        Node<AdminDept, List<AdminUser>> userDept = getDepartmentById(deptId);
        if (Objects.isNull(userDept)) {
            return Collections.emptyList();
        }
        List<Node<AdminDept, List<AdminUser>>> leaves = userDept.getAllLeaves();
        leaves.add(userDept);
        return leaves;
    }


}
