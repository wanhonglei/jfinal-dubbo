package com.kakarote.crm9.erp.crm.acl.user;

import com.alibaba.fastjson.annotation.JSONField;
import com.kakarote.crm9.erp.admin.entity.AdminDept;
import com.kakarote.crm9.erp.admin.entity.AdminRole;
import com.kakarote.crm9.erp.admin.entity.AdminUser;
import com.kakarote.crm9.erp.admin.entity.UserExpandInfo;
import com.kakarote.crm9.erp.crm.acl.department.CrmDepartmentTree;
import com.kakarote.crm9.erp.crm.acl.department.Node;
import org.apache.commons.collections.CollectionUtils;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Crm User
 *
 * @author hao.fu
 * @create 2019/11/20 10:52
 */
public class CrmUser implements Serializable {

    private static final long serialVersionUID = 1061692223071983334L;

    /** crm login user */
    private AdminUser crmAdminUser;

    /** roles of crm user */
    private List<AdminRole> userRoles;

    /** the direct department of crm user */
    private AdminDept userDept;

    /** crm user data permission */
    private CrmUserDataPermission crmUserDataPermission;

    /**
     * 人员扩展信息--所属的事业部
     */
    private UserExpandInfo userExpandInfo;

    /**
     * Constructor.
     *
     * @param user admin user
     * @param roles roles list
     */
    public CrmUser(AdminUser user, List<AdminRole> roles) {
        if (Objects.isNull(user) || CollectionUtils.isEmpty(roles)) {
            return;
        }
        crmAdminUser = user;
        userRoles = roles;

        Node<AdminDept, List<AdminUser>> node = CrmDepartmentTree.getCrmDepartmentTree().getLeafNodeById(crmAdminUser.getDeptId());
        userDept = Objects.nonNull(node) ? node.getNodeDataKey() : null;
        crmUserDataPermission = new CrmUserDataPermission(crmAdminUser, userRoles);
    }

    /**
     * Return authorized user ids for the crm user.
     *
     * @return authorized user id list
     */
    @JSONField(serialize = false)
    public List<Integer> getAuthorizedUserIds() {
        if (Objects.nonNull(crmUserDataPermission)) {
            return crmUserDataPermission.getAuthorizedUserIds();
        }
        return Collections.emptyList();
    }

    /**
     * Return authorized department ids for the crm user.
     *
     * @return authorized department id list
     */
    @JSONField(serialize = false)
    public List<Integer> getAuthorizedDepartmentIds() {
        if (Objects.nonNull(crmUserDataPermission)) {
            return crmUserDataPermission.getAuthorizedDeptIds();
        }
        return Collections.emptyList();
    }

    public AdminUser getCrmAdminUser() {
        return crmAdminUser;
    }

    public void setCrmAdminUser(AdminUser crmAdminUser) {
        this.crmAdminUser = crmAdminUser;
    }

    public List<AdminRole> getUserRoles() {
        return userRoles;
    }

    public void setUserRoles(List<AdminRole> userRoles) {
        this.userRoles = userRoles;
    }

    public AdminDept getUserDept() {
        return userDept;
    }

    public void setUserDept(AdminDept userDept) {
        this.userDept = userDept;
    }

    public CrmUserDataPermission getCrmUserDataPermission() {
        return crmUserDataPermission;
    }

    public void setCrmUserDataPermission(CrmUserDataPermission crmUserDataPermission) {
        this.crmUserDataPermission = crmUserDataPermission;
    }

    public UserExpandInfo getUserExpandInfo() {
        return userExpandInfo;
    }

    public void setUserExpandInfo(UserExpandInfo userExpandInfo) {
        this.userExpandInfo = userExpandInfo;
    }
}
