package com.kakarote.crm9.erp.admin.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jfinal.aop.Inject;
import com.jfinal.core.Controller;
import com.jfinal.core.paragetter.Para;
import com.jfinal.plugin.activerecord.Record;
import com.kakarote.crm9.common.annotation.HttpEnum;
import com.kakarote.crm9.common.annotation.NotNullValidate;
import com.kakarote.crm9.common.exception.CrmException;
import com.kakarote.crm9.erp.admin.entity.AdminDeptCapacity;
import com.kakarote.crm9.erp.admin.entity.AdminUserCapacity;
import com.kakarote.crm9.erp.admin.service.AdminDeptCapacityService;
import com.kakarote.crm9.erp.admin.service.AdminUserCapacityService;
import com.kakarote.crm9.utils.PagerUtil;
import com.kakarote.crm9.utils.R;

import java.util.Objects;
import java.util.stream.Collectors;


/**
 * 部门周转库配置
 *
 * @Author: haihong.wu
 * @Date: 2020/3/24 2:40 下午
 */
public class AdminCapacityController extends Controller {

    @Inject
    private AdminDeptCapacityService adminDeptCapacityService;

    @Inject
    private AdminUserCapacityService adminUserCapacityService;

    /**
     * 获取部门周转库库容设置
     *
     * @param bizDeptId
     */
    public void getDeptCapacityList(Long bizDeptId) {
        renderJson(R.ok().put("data", adminDeptCapacityService.list(bizDeptId)));
    }

    /**
     * 添加部门周转库库容设置
     */
    @NotNullValidate(value = "bizDeptId", type = HttpEnum.JSON, message = "业务部门ID不能为空")
    @NotNullValidate(value = "deptId", type = HttpEnum.JSON, message = "部门ID不能为空")
    @NotNullValidate(value = "deptName", type = HttpEnum.JSON, message = "部门名称不能为空")
    @NotNullValidate(value = "capacity", type = HttpEnum.JSON, message = "容量不能为空")
    public void addDeptCapacity() {
        try {
            JSONObject param = JSON.parseObject(getRawData());
            AdminDeptCapacity entity = new AdminDeptCapacity()
                    .setBizDeptId(param.getLong("bizDeptId"))
                    .setDeptId(param.getLong("deptId"))
                    .setDeptName(param.getString("deptName"))
                    .setCapacity(param.getInteger("capacity"));
            adminDeptCapacityService.addDeptCapacity(entity);
            renderJson(R.ok());
        } catch (CrmException e) {
            renderJson(R.error(e.getMessage()));
        }
    }

    /**
     * 编辑客户释放规则
     */
    public void editCustomerCapacityRule() {
        try {
            JSONArray ja = JSON.parseArray(getRawData());
            if (ja == null || ja.size() == 0) {
                renderJson(R.error("参数为空"));
                return;
            }
            adminDeptCapacityService.editCustomerCapacityRule(ja.stream().map(o -> {
                JSONObject jo = (JSONObject) o;
                AdminDeptCapacity entity = new AdminDeptCapacity();
                entity.setId(jo.getBigInteger("id"));
                entity.setInspectFlag(jo.getInteger("inspectFlag"));
                entity.setInspectDays(jo.getInteger("inspectDays"));
                entity.setRelateOutFlag(jo.getInteger("relateOutFlag"));
                entity.setRelateOutDays(jo.getInteger("relateOutDays"));
                entity.setRelateLockFlag(jo.getInteger("relateLockFlag"));
                entity.setRelateLockDays(jo.getInteger("relateLockDays"));
                return entity;
            }).collect(Collectors.toList()));
            renderJson(R.ok());
        } catch (CrmException e) {
            renderJson(R.error(e.getMessage()));
        }
    }

    /**
     * 删除部门周转库库容设置
     */
    @NotNullValidate(value = "id", type = HttpEnum.JSON, message = "ID不能为空")
    public void deleteDeptCapacity() {
        JSONObject param = JSON.parseObject(getRawData());
        Long id = param.getLong("id");
        adminDeptCapacityService.deleteDeptCapacityById(id);
        renderJson(R.ok());
    }

    /**
     * 查询个人客户库容设置
     *
     * @param deptId 部门ID
     * @param page
     * @param limit
     */
    public void getPersonalCapacity(Long deptId, Integer page, Integer limit) {
        if (Objects.isNull(page)) {
            page = PagerUtil.DEFAULT_PAGE;
        }
        if (Objects.isNull(limit)) {
            limit = PagerUtil.DEFAULT_PAGE_SIZE;
        }
        renderJson(R.ok().put("data", adminUserCapacityService.getPersonalCapacity(deptId, page, limit)));
    }

    /**
     * 批量编辑个人客户库存容量设置
     */
    public void editPersonalCapacity() {
        JSONArray param = JSON.parseArray(getRawData());
        adminUserCapacityService.editPersonalCapacity(param.stream().map(item -> {
            //将参数映射为Model
            JSONObject paramItem = (JSONObject) item;
            return new AdminUserCapacity()
                    .setUserId(paramItem.getLong("userId"))
                    .setUserName(paramItem.getString("userName"))
                    .setInspectCap(paramItem.getInteger("inspectCap"))
                    .setRelateCap(paramItem.getInteger("relateCap"));
        }).filter(item -> Objects.nonNull(item.getUserId())).collect(Collectors.toList()));
        renderJson(R.ok());
    }

    /**
     * 查询某个客户的客户库类型
     *
     * @param customerId 客户id
     */
    @NotNullValidate(value = "customerId", message = "customerId 不能为空")
    public void searchCapacityByCustomerId(@Para("customerId") Long customerId) {
        Record record = adminUserCapacityService.searchCapacityByCustomerId(customerId);
        if (record == null){
            record = new Record().set("storage_type",null);
        }
        renderJson(R.ok().put("data", record));
    }
}
