package com.kakarote.crm9.erp.admin.service;

import cn.hutool.core.util.StrUtil;
import com.jfinal.kit.Kv;
import com.jfinal.plugin.activerecord.ActiveRecordException;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Model;
import com.kakarote.crm9.common.constant.BaseConstant;
import com.kakarote.crm9.common.exception.CrmException;
import com.kakarote.crm9.erp.admin.entity.AdminDeptCapacity;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * 部门周转库配置
 *
 * @Author: haihong.wu
 * @Date: 2020/3/24 2:40 下午
 */
public class AdminDeptCapacityService {

    /**
     * 根据事业部ID查询配置列表
     *
     * @param bizDeptId
     * @return
     */
    public List<AdminDeptCapacity> list(Long bizDeptId) {
        List<AdminDeptCapacity> datas = AdminDeptCapacity.dao.find(Db.getSqlPara("admin.dept.capacity.list", Kv.by("bizDeptId", bizDeptId)));
        datas.sort(((o1, o2) -> {
            //一些特殊情况
            if (o1 == null) {
                return -1;
            }
            if (o2 == null) {
                return 1;
            }
            String deptNameJoin1 = o1.getDeptName();
            String deptNameJoin2 = o2.getDeptName();
            if (Objects.equals(deptNameJoin1, deptNameJoin2)) {
                return 0;
            }
            if (StringUtils.isBlank(deptNameJoin1)) {
                return -1;
            }
            if (StringUtils.isBlank(deptNameJoin2)) {
                return 1;
            }
            String[] deptNames1 = deptNameJoin1.split("-");
            String[] deptNames2 = deptNameJoin2.split("-");
            int idx = 0;
            /*
             * 根据部门层级的首字母来判断
             * o1: A-B-C o2: A-B o1>o2
             * o1: A-B-C o2: D-E o2>o1
             * o1: A-B-C o2: A-E-F o2>o1
             */
            while (idx < deptNames1.length || idx < deptNames2.length) {
                if (idx == deptNames1.length) {
                    //o2是o1的子部门
                    return -1;
                }
                if (idx == deptNames2.length) {
                    //o1是o2的子部门
                    return 1;
                }
                String dept1 = deptNames1[idx];
                String dept2 = deptNames2[idx];
                int res = StrUtil.compare(dept1, dept2, true);
                if (res != 0) {
                    return res;
                }
                idx++;
            }
            return 0;
        }));
        return datas;
    }

    /**
     * 插入部门容量池配置
     *
     * @param entity
     */
    public void addDeptCapacity(AdminDeptCapacity entity) {
        try {
            AdminDeptCapacity old = getDeptCapacityByDeptId(entity.getDeptId());
            //save or update
            if (old != null) {
                entity.setId(old.getId());
                entity.update();
            } else {
                entity.save();
            }
        } catch (ActiveRecordException e) {
            if (e.getMessage().contains(BaseConstant.MYSQL_INTEGRITY_CONSTRAINT_VIOLATION_EXCEPTION_NAME)) {
                throw new CrmException("部门容量池配置已存在", e);
            } else {
                throw e;
            }
        }
    }

    /**
     * 根据部门ID获取部门库容配置
     * @param deptId
     * @return
     */
    public AdminDeptCapacity getDeptCapacityByDeptId(Long deptId) {
        return AdminDeptCapacity.dao.findFirst(Db.getSql("admin.dept.capacity.getDeptCapacityByDeptId"), deptId);
    }

    /**
     * 删除部门容量池配置
     *
     * @param id
     */
    public void deleteDeptCapacityById(Long id) {
        AdminDeptCapacity.dao.deleteById(id);
    }

    /**
     * 查询配置出库规则的库存信息
     *
     * @return
     */
    public List<AdminDeptCapacity> getEnableReleaseAdminDeptCapacityList() {
        List<AdminDeptCapacity> list = AdminDeptCapacity.dao.find(Db.getSql("admin.dept.capacity.getEnableReleaseAdminDeptCapacityList"));
        if (CollectionUtils.isEmpty(list)) {
            return Collections.emptyList();
        }
        return list;
    }

    /**
     * 编辑客户库容规则
     * @param list
     */
    public void editCustomerCapacityRule(List<AdminDeptCapacity> list) {
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        if (list.stream().anyMatch(adminDeptCapacity -> Objects.isNull(adminDeptCapacity.getId()))) {
            throw new CrmException("存在ID为空的配置，更新失败");
        }
        //事务中一次更新成功
        Db.tx(() -> {
            list.forEach(Model::update);
            return true;
        });
    }
}
