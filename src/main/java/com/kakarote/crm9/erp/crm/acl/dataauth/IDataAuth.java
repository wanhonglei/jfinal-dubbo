package com.kakarote.crm9.erp.crm.acl.dataauth;

import com.kakarote.crm9.erp.admin.entity.AdminUser;

import java.util.List;

/**
 * IDataAuth
 *
 * @author hao.fu
 * @since : Created by hao.fu on 2019/11/21.
 */
public interface IDataAuth {

    List<Integer> getAuthorizedUserIds(AdminUser user);

    List<Integer> getAuthorizedDeptIds(AdminUser user);
}
