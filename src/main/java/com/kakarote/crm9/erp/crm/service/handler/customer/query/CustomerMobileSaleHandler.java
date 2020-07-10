package com.kakarote.crm9.erp.crm.service.handler.customer.query;

import com.jfinal.kit.Kv;
import com.kakarote.crm9.erp.admin.entity.AdminConfig;
import com.kakarote.crm9.erp.admin.entity.AdminUser;
import com.kakarote.crm9.erp.admin.entity.base.BaseAdminUser;
import com.kakarote.crm9.erp.crm.constant.CrmConstant;
import com.kakarote.crm9.erp.crm.dto.CrmCustomerQueryParamDto;
import org.apache.commons.collections.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 电销池
 *
 * @Author: haihong.wu
 * @Date: 2020/5/7 5:10 下午
 */
public class CustomerMobileSaleHandler extends BaseCustomerQueryHandler {

    @Override
    protected Kv attachParam(CrmCustomerQueryParamDto paramDto, Kv params) {
        Long deptId = params.getLong("deptId");
        //电销池没有deptId
        params.remove("deptId");
        //查询ownerUserId
        if (Objects.isNull(deptId)) {
            AdminConfig phoneSaleDeptIdConfig = adminConfigService.queryAdminConfig(CrmConstant.PHONE_SALE_BUSINESS_DEPT_ID);
            if (phoneSaleDeptIdConfig != null) {
                deptId = Long.valueOf(phoneSaleDeptIdConfig.getValue());
            } else {
                Integer deptIdByDeptName = adminDeptService.getDeptIdByDeptName(CrmConstant.TELEMARKETING_DEPT_NAME);
                if (Objects.nonNull(deptIdByDeptName)) {
                    deptId = Long.valueOf(deptIdByDeptName);
                }
            }
        }
        if (Objects.nonNull(deptId)) {
            List<AdminUser> userListByDeptIds = adminUserService.getUserListByDeptIds(Collections.singletonList(deptId));
            if (CollectionUtils.isNotEmpty(userListByDeptIds)) {
                //电销用户ID列表
                List<Long> mobileSaleUserIdList = userListByDeptIds.stream().map(BaseAdminUser::getUserId).collect(Collectors.toList());
                List<Long> ownerUserIds = params.getAs("ownerUserIds");
                //取参数的ownerUserId的并集
                if (CollectionUtils.isNotEmpty(ownerUserIds)) {
                    mobileSaleUserIdList = mobileSaleUserIdList.stream().filter(ownerUserIds::contains).collect(Collectors.toList());
                }
                params.set("ownerUserIds", mobileSaleUserIdList);
            }
        }
        return params;
    }
}
