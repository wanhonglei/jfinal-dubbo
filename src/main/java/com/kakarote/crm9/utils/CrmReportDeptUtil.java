package com.kakarote.crm9.utils;

import com.jfinal.aop.Aop;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.kakarote.crm9.erp.admin.service.AdminDeptService;
import com.kakarote.crm9.erp.crm.acl.dataauth.CrmDataAuthEnum;

import java.util.ArrayList;
import java.util.List;

public class CrmReportDeptUtil {

    /***
     * 获取报表对应的展示部门
     * @author yue.li
     * @return
     */
    public static List<Record>  getCrmReportDeptList() {
        Long userId = BaseUtil.getUser() == null ? null :BaseUtil.getUserId();
        String deptId = BaseUtil.getUser() == null ? null:String.valueOf(BaseUtil.getDeptId());
        List<Record> deptList = Db.find(Db.getSql("admin.businessType.queryBusinessDeptList"));
        List<Integer> list = Db.query(Db.getSql("admin.role.queryDataTypeByUserId"), userId);
        List<Record> resultList = new ArrayList<>();
        if(list.contains(CrmDataAuthEnum.ALL_TYPE_KEY.getTypes())){
            resultList.addAll(deptList);
        } else {
            String serviceDepartmentId = Aop.get(AdminDeptService.class).getBusinessDepartmentByDeptId(deptId);
            List<String> allDeptList =  Aop.get(AdminDeptService.class).getAllDeptsByBusinessDepartmentId(deptId);
            allDeptList.add(serviceDepartmentId);
            allDeptList.add(deptId);
            for(Record deptRecord : deptList){
                for(String serviceDeptId : allDeptList){
                    if(deptRecord.getStr("id").equals(serviceDeptId)){
                        if(!resultList.contains(deptRecord)){
                            resultList.add(deptRecord);
                        }
                    }
                }
            }
        }
        return resultList;
    }
}
