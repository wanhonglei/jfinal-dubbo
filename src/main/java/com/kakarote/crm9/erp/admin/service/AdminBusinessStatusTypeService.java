package com.kakarote.crm9.erp.admin.service;

import com.jfinal.aop.Before;
import com.jfinal.kit.Kv;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.kakarote.crm9.common.config.paragetter.BasePageRequest;
import com.kakarote.crm9.erp.admin.entity.AdminBusinessStatusType;
import com.kakarote.crm9.erp.crm.constant.CrmConstant;
import com.kakarote.crm9.utils.R;

import java.util.Date;
import java.util.List;

public class AdminBusinessStatusTypeService {

    /**
     * @author liyue
     * @param adminBusinessStatusType 阶段类别实体
     * 添加阶段类别
     */
    @Before(Tx.class)
    public R addOrUpdateBusinessStatusType(AdminBusinessStatusType adminBusinessStatusType,Long userId){
        Record record = queryBusinessStatusTypeList(adminBusinessStatusType.getStatusTypeName());
        if( record != null){
            if(adminBusinessStatusType.getId() == null){
              return R.error("请重新输入,阶段类别已经存在") ;
            }
            if(adminBusinessStatusType.getId() != null
               && !String.valueOf(adminBusinessStatusType.getId()).equals(record.getStr("id"))){
                return R.error("请重新输入,阶段类别已经存在") ;
            }
        }
        adminBusinessStatusType.setIsDelete(Integer.valueOf(CrmConstant.ONE_FLAG));
        if (adminBusinessStatusType.getId() == null) {
            adminBusinessStatusType.setCreateTime(new Date());
            adminBusinessStatusType.setCreateUser(userId);
            adminBusinessStatusType.save();
        } else {
            adminBusinessStatusType.setUpdateTime(new Date());
            adminBusinessStatusType.update();
        }
        return R.ok();
    }

    /**
     * @author liyue
     * @param request 请求实体
     * 查询阶段类别
     */
    public Page<Record> queryBusinessStatusTypeList(BasePageRequest<AdminBusinessStatusType> request) {
        Page<Record> paginate = Db.paginate(request.getPage(), request.getLimit(), Db.getSqlPara("admin.businessStatusType.queryBusinessStatusTypeList"));
        return paginate;
    }

    /**
     * @author liyue
     * @param ids 封存ids
     * 封存阶段类别
     */
    @Before(Tx.class)
    public R sealById(String ids) {
        String[] idsArr = ids.split(",");
        for(String id : idsArr){
            Db.update(Db.getSql("admin.businessStatusType.updateBusinessStatusTypeById"), CrmConstant.ZERO_FLAG, id);
        }
        return R.ok();
    }

    /**
     * @author liyue
     * @param ids 解封ids
     * 解封阶段类别
     */
    @Before(Tx.class)
    public R unSealById(String ids) {
        String[] idsArr = ids.split(",");
        for(String id : idsArr){
            Db.update(Db.getSql("admin.businessStatusType.updateBusinessStatusTypeById"), CrmConstant.ONE_FLAG, id);
        }
        return R.ok();
    }

    /**
     * 查询阶段类别信息
     * @return
     */
    public List<Record> queryBusinessStatusTypeList() {
        List<Record> businessStatusTypeList = Db.find(Db.getSql("admin.businessStatusType.queryBusinessStatusTypeList"));
        return businessStatusTypeList;
    }

    /**
     * 查询阶段类别未封存列表
     * @return
     */
    public List<Record> queryBusinessStatusTypeUnSealedList() {
        List<Record> businessStatusTypeList = Db.find(Db.getSql("admin.businessStatusType.queryBusinessStatusTypeUnSealedList"));
        return businessStatusTypeList;
    }

    /**
     * 根据阶段类别查询阶段类别信息
     * @return
     */
    public Record queryBusinessStatusTypeList(String businessStatusType) {
        Record record = Db.findFirst( Db.getSqlPara("admin.businessStatusType.queryBusinessStatusTypeList", Kv.by("businessStatusType",businessStatusType)));
        return record;
    }

    /**
     * 根据阶段ID查询阶段类别信息
     * @return
     */
    public Record queryBusinessStatusByStatusId(String statusId) {
        Record record = Db.findFirst( Db.getSqlPara("admin.businessStatusType.queryBusinessStatusByStatusId", Kv.by("statusId",statusId)));
        return record;
    }

    /**
     * 查询阶段信息专为报表展示
     * @author yue.li
     * @return
     */
    public List<Record> queryBusinessStatusTypeForPlanReportList() {
        List<Record> businessStatusTypeList = Db.find(Db.getSql("admin.businessStatusType.queryBusinessStatusTypeForPlanReportList"));
        return businessStatusTypeList;
    }

}
