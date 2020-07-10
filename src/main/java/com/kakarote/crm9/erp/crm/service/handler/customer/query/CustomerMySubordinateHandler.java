package com.kakarote.crm9.erp.crm.service.handler.customer.query;

import com.jfinal.kit.Kv;
import com.kakarote.crm9.common.constant.BaseConstant;
import com.kakarote.crm9.erp.crm.dto.CrmCustomerQueryParamDto;
import com.kakarote.crm9.utils.BaseUtil;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Objects;

/**
 * 下属负责的客户
 *
 * @Author: haihong.wu
 * @Date: 2020/5/7 5:11 下午
 */
public class CustomerMySubordinateHandler extends BaseCustomerQueryHandler {

    @Override
    protected Kv attachParam(CrmCustomerQueryParamDto paramDto, Kv params) {
        //OWNER为空的时候查出所有直接下属和间接下属作为负责人的客户
        if (StringUtils.isBlank(paramDto.getOwnerUserIds())) {
            Long userId = BaseUtil.getUserId();
            List<Long> userIds;
            if (Objects.requireNonNull(BaseUtil.getUser()).getRoles().contains(BaseConstant.SUPER_ADMIN_ROLE_ID)) {
                userIds = adminUserService.queryUserByParentUser(userId, BaseConstant.AUTH_DATA_RECURSION_NUM);
            } else {
                userIds = adminUserService.queryUserByAuth(userId);
                userIds.remove(userId);
            }
            if (userIds.size() > 0) {
                params.set("ownerUserIds", userIds);
            }else{
                //没有下属则直接返回
                params.set(RETURN_FLAG, Boolean.TRUE);
            }
        }
        //客户所属部门逻辑
        customerOwnDept(params, paramDto);
        return params;
    }
}
