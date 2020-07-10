package com.kakarote.crm9.erp.admin.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.util.TypeUtils;
import com.jfinal.log.Log;
import com.kakarote.crm9.common.config.paragetter.BasePageRequest;
import com.kakarote.crm9.erp.admin.entity.CrmBusinessStatus;
import com.kakarote.crm9.erp.admin.entity.CrmBusinessType;
import com.kakarote.crm9.erp.crm.constant.CrmConstant;
import com.kakarote.crm9.utils.R;
import com.jfinal.aop.Before;
import com.jfinal.kit.Kv;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class AdminBusinessTypeService {

    private Log logger = Log.getLog(getClass());

    @Before(Tx.class)
    public R addBusinessType(CrmBusinessType crmBusinessType, JSONArray crmBusinessStatusList,Integer userId){
        Record record = Db.findFirst(Db.getSql("admin.businessType.getBusinessTypeByDeptId"), crmBusinessType.getDeptIds());
        if( record != null){
            if(crmBusinessType.getTypeId() == null){
                return R.error("请重新输入,应用部门已经存在商机组") ;
            }
            if(crmBusinessType.getTypeId() != null
                    && !String.valueOf(crmBusinessType.getTypeId()).equals(record.getStr("type_id"))){
                return R.error("请重新输入,应用部门已经存在商机组") ;
            }
        }
        if (crmBusinessType.getTypeId() == null) {
            crmBusinessType.setCreateTime(new Date());
            crmBusinessType.setCreateUserId(userId);
            crmBusinessType.save();
        } else {
            crmBusinessType.setUpdateTime(new Date());
            crmBusinessType.update();
            Db.delete(Db.getSql("admin.businessType.deleteBusinessStatus"),crmBusinessType.getTypeId());
        }
        Integer typeId = crmBusinessType.getTypeId().intValue();
        for(int i = 0; i < crmBusinessStatusList.size(); i++){
            CrmBusinessStatus crmBusinessStatus = TypeUtils.castToJavaBean(crmBusinessStatusList.getJSONObject(i), CrmBusinessStatus.class);
            crmBusinessStatus.setStatusId(null);
            crmBusinessStatus.setTypeId(typeId);
            crmBusinessStatus.setOrderNum(i+1);
            crmBusinessStatus.save();
        }
        return R.ok();
    }

    public Page<Record> queryBusinessTypeList(BasePageRequest request) {
        Page<Record> paginate = Db.paginate(request.getPage(), request.getLimit(), Db.getSqlPara("admin.businessType.queryBusinessTypeList"));
        paginate.getList().forEach(record -> {
            if(record.getStr("dept_ids") != null && record.getStr("dept_ids").split(",").length > 0){
                List<Record> deptList = Db.find(Db.getSqlPara("admin.dept.queryByIds", Kv.by("ids", record.getStr("dept_ids").split(","))));
                record.set("deptIds", deptList);
            }else{
                record.set("deptIds", new ArrayList<>());
            }
        });
        return paginate;
    }

    public R getBusinessType(String typeId) {
        Record record = Db.findFirst(Db.getSql("admin.businessType.getBusinessType"), typeId);
        logger.info(String.format("getBusinessType方法json %s",record.toJson()));
        if(record.getStr("dept_ids") != null && record.getStr("dept_ids").split(",").length > 0){
            List<Record> deptList = Db.find(Db.getSqlPara("admin.dept.queryByIds", Kv.by("ids", record.getStr("dept_ids").split(","))));
            record.set("deptIds", deptList);
        }else{
            record.set("deptIds", new ArrayList<>());
        }
        List<Record> statusList = Db.find(Db.getSql("admin.businessType.queryBusinessStatus"), typeId);
        record.set("statusList", statusList);
        return R.ok().put("data", record);
    }

    @Before(Tx.class)
    public R deleteById(String typeId) {
        Db.update(Db.getSql("admin.businessType.updateBusinessStatus"), CrmConstant.ZERO_FLAG, typeId);
        return R.ok();
    }

}
