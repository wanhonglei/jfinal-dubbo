package com.kakarote.crm9.erp.crm.acl.dataauth;

import com.google.common.collect.Maps;
import com.kakarote.crm9.erp.admin.entity.AdminUser;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Data Auth Utility
 *
 * @author hao.fu
 * @since 2019/11/21 16:36
 */
public class DataAuthUtil {

    private static Map<Integer, IDataAuth> dataAuthMap = Maps.newHashMap();

    static {
        dataAuthMap.put(CrmDataAuthEnum.ONESELF_TYPE_KEY.getTypes(), new DataAuthSelf());
        dataAuthMap.put(CrmDataAuthEnum.ONESELF_BRANCH_TYPE_KEY.getTypes(), new DataAuthSelfBranch());
        dataAuthMap.put(CrmDataAuthEnum.DEPARTMENT_TYPE_KEY.getTypes(), new DataAuthDepartment());
        dataAuthMap.put(CrmDataAuthEnum.DEPARTMENT_BRANCH_TYPE_KEY.getTypes(), new DataAuthDepartmentBranch());
        dataAuthMap.put(CrmDataAuthEnum.ALL_TYPE_KEY.getTypes(), new DataAuthAll());
    }

    /**
     * Return authorized user id list.
     *
     * @param dataType data auth type
     * @param user crm user
     * @return authorized user id list
     */
    public static List<Integer> getAuthorizedUserIds(Integer dataType, AdminUser user) {
        if (Objects.isNull(user) || Objects.isNull(user.getDeptId())) {
            return Collections.emptyList();
        }
        return dataAuthMap.get(dataType).getAuthorizedUserIds(user);
    }

    /**
     * Return authorized department id list.
     *
     * @param dataType data auth type
     * @param user crm user
     * @return authorized department id list
     */
    public static List<Integer> getAuthorizedDepartmentIds(Integer dataType, AdminUser user) {
        if (Objects.isNull(user) || Objects.isNull(user.getDeptId())) {
            return Collections.emptyList();
        }
        return dataAuthMap.get(dataType).getAuthorizedDeptIds(user);
    }
}
