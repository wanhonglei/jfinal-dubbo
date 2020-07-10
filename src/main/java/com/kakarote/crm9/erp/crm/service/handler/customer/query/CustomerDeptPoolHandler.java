package com.kakarote.crm9.erp.crm.service.handler.customer.query;

import com.alibaba.fastjson.JSONObject;
import com.jfinal.kit.Kv;
import com.kakarote.crm9.common.exception.CrmException;
import com.kakarote.crm9.erp.crm.dto.CrmCustomerQueryParamDto;
import com.kakarote.crm9.utils.BaseUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

/**
 * 部门客户池
 *
 * @Author: haihong.wu
 * @Date: 2020/5/7 5:10 下午
 */
@Slf4j
public class CustomerDeptPoolHandler extends BaseCustomerQueryHandler {

    @Override
    protected Kv attachParam(CrmCustomerQueryParamDto paramDto, Kv params) {
        if (params.isNull("deptId") && Objects.nonNull(paramDto.getDeptId())) {
        //部门ID不为空
            params.set("deptId", paramDto.getDeptId());
        } else if (params.isNull("deptId") && Objects.isNull(paramDto.getDeptId())) {
            //部门ID为空，解析最近配置周转库部门
            String deptId;
            Integer loginDeptId = BaseUtil.getDeptId();
            if (Objects.isNull(loginDeptId)) {
                throw new CrmException("login user dept is null");
            }
            JSONObject nearestDeptCapacity = adminDeptService.getNearestDeptCapacity(adminDeptService.getDeptMap(), adminDeptService.getDeptCapacityMap(), Long.valueOf(loginDeptId));
            if (Objects.nonNull(nearestDeptCapacity.get("deptId"))) {
                deptId = String.valueOf(nearestDeptCapacity.get("deptId"));
            } else {
                deptId = adminDeptService.getBusinessDepartmentByDeptId(String.valueOf(loginDeptId));
            }
            params.set("deptId", deptId);
        }
        //部门池肯定没有负责人
        params.set("noOwnerUserId", Boolean.TRUE);
        return params;
    }
}
