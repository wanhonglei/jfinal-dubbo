package com.kakarote.crm9.erp.crm.service;

import com.jfinal.aop.Inject;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.kakarote.crm9.common.exception.CrmException;
import com.kakarote.crm9.erp.admin.service.AdminDeptService;
import com.kakarote.crm9.erp.crm.acl.user.CrmUser;
import com.kakarote.crm9.erp.crm.dto.NearestDeptInfo;
import com.kakarote.crm9.erp.crm.entity.CrmCustomerExt;
import com.kakarote.crm9.utils.R;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @Author: honglei.wan
 * @Description:
 * @Date: Create in 2020/4/10 6:33 下午
 */
@Slf4j
public class CrmCustomerExtService {

    @Inject
    private CrmCustomerService crmCustomerService;

    @Inject
    private AdminDeptService adminDeptService;

    /**
     * 获取登陆人的归属部门池信息
     * @param user
     */
    public R getDeptAndCapacity(CrmUser user) {
        if (user == null){
            return R.error("用户登陆信息异常，清重新登陆");
        }

        Long deptId = user.getUserDept().getDeptId();
        if (deptId == null){
            return R.error("登陆用户归属部门为空，请联系管理员");
        }

        List<Record> recordList = Db.find(Db.getSql("admin.dept.capacity.findTotalDeptCapacity"));
        Map<Long, Record> deptCapacityMap = recordList.stream().collect(Collectors.toMap(o -> o.getLong("dept_id"), Function.identity()));

        try {
            NearestDeptInfo deptInfo = getNearestDeptCapacity(crmCustomerService.getDeptMap(), deptCapacityMap, deptId);
            //无限制，取当前登陆人的事业部
            if (deptInfo == null){
                deptId = Long.valueOf(adminDeptService.getBusinessDepartmentByDeptId(deptId.toString()));

                String deptNamePath = adminDeptService.getDeptNamePath(deptId);
                deptInfo = new NearestDeptInfo();
                deptInfo.setDeptId(Math.toIntExact(deptId));
                deptInfo.setDeptName(deptNamePath);
            }

            Record record = Db.findFirst(Db.getSql("crm.customer.findDeptCustomerPoolCount"), deptInfo.getDeptId());
            deptInfo.setUsedCapacity(record.getInt("customerCount"));

            return R.ok().put("data",deptInfo);
        } catch (CrmException e) {
            log.error("业务异常:{}",e.getMessage());
            return R.error(e.getMessage());
        }


    }

    /**
     * 获取离传递部门id最近的部门配置库容信息
     * @param deptMap 部门map
     * @param deptCapacityMap 配置库容map
     * @param deptId 部门id
     * @return
     */
    private NearestDeptInfo getNearestDeptCapacity(Map<Long, Integer> deptMap, Map<Long, Record> deptCapacityMap, Long deptId) {
        if (deptCapacityMap == null || deptCapacityMap.isEmpty()){
            log.error("部门库容信息表为空，部门池将无限制");
            return null;
        }
        if (deptMap == null || deptMap.isEmpty()){
            throw new CrmException("程序内存部门树缓存异常，请检查代码");
        }

        //dept_id,dept_name,capacity
        Record record = deptCapacityMap.get(deptId);
        if (record != null){
            NearestDeptInfo deptInfo = new NearestDeptInfo();
            deptInfo.setDeptId(record.getInt("dept_id"));
            deptInfo.setDeptName(record.getStr("dept_name"));
            deptInfo.setCapacity(record.getInt("capacity"));
            return deptInfo;
        }else{
            if (!deptMap.containsKey(deptId)){
                throw new CrmException("程序内存部门树缓存异常，不包含部门:" + deptId);
            }
            //获取上级部门id
            deptId = Long.valueOf(deptMap.get(deptId));
            if (deptId == -1){
                //找到顶级还未有，则返回null，表示无限制
                return null;
            }else{
                return getNearestDeptCapacity(deptMap,deptCapacityMap,deptId);
            }
        }
    }
    
    /**
     * 根据网站会员id 查询客户扩展表
     * @param siteMemberId
     * @return
     */
    public CrmCustomerExt queryCrmCustomerExtbySiteMemberId(Long siteMemberId) {
    	
    	Record record = Db.findFirst(Db.getSql("crm.customerExt.queryCrmCustomerExtbySiteMemberId"), siteMemberId);
    	return Objects.nonNull(record) ? new CrmCustomerExt()._setOrPut(record.getColumns()) : new CrmCustomerExt();
    }

    public CrmCustomerExt getByCustomerId(Long customerId) {
        return CrmCustomerExt.dao.findFirst(Db.getSql("crm.customerExt.queryByCustomerId"), customerId);
    }
}
