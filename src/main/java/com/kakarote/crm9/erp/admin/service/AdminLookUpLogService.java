package com.kakarote.crm9.erp.admin.service;

import com.jfinal.aop.Before;
import com.jfinal.kit.Kv;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.kakarote.crm9.common.config.paragetter.BasePageRequest;
import com.kakarote.crm9.erp.admin.entity.AdminLookUpLog;
import com.kakarote.crm9.erp.admin.entity.AdminScenario;
import com.kakarote.crm9.utils.R;

public class AdminLookUpLogService {


    /**
     * 获取操作记录列表
     * @author yue.li
     *
     */
    public Page<Record> queryLookUpLogList(BasePageRequest<AdminScenario> request) {
        Long deptId = request.getData().getDeptId();
        Page<Record> paginate = Db.paginate(request.getPage(), request.getLimit(), Db.getSqlPara("admin.scenario.queryScenarioList",Kv.by("deptId",deptId)));
        return paginate;
    }

    /**
     * 添加操作记录
     * @author yue.li
     *
     */
    @Before(Tx.class)
    public R addLookUpLog(AdminLookUpLog adminLookUpLog,Long userId){
            adminLookUpLog.setCreateUser(userId);
            adminLookUpLog.save();
            return R.ok();
    }
}
