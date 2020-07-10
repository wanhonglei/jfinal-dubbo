package com.kakarote.crm9.erp.admin.service;


import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.util.TypeUtils;
import com.jfinal.aop.Before;
import com.jfinal.kit.Kv;
import com.jfinal.log.Log;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.kakarote.crm9.common.config.JfinalConfig;
import com.kakarote.crm9.common.config.paragetter.BasePageRequest;
import com.kakarote.crm9.erp.admin.entity.AdminScenario;
import com.kakarote.crm9.erp.crm.constant.CrmConstant;
import com.kakarote.crm9.utils.R;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 应用场景相关
 * @author honglei.wan
 */
public class AdminScenarioService {

    private Log logger = Log.getLog(getClass());

    /**
     * 获取应用场景列表
     * @author yue.li
     */
    public Page<Record> queryScenarioList(BasePageRequest<AdminScenario> request) {
        Long deptId = request.getData().getDeptId();
        return Db.paginate(request.getPage(), request.getLimit(), Db.getSqlPara("admin.scenario.queryScenarioList",Kv.by("deptId",deptId)));
    }

    /**
     * 获取事业部列表
     * @author yue.li
     */
    public R getDeptList() {
        Record record = new Record();
        List<Record> deptList = Db.find(Db.getSql("admin.scenario.queryDeptList"));
        record.set("deptList", deptList);
        return R.ok().put("data", record);
    }

    /**
     * 获取商机组事业部列表
     * @author yue.li
     */
    public R getBusnessDeptList() {
        Record record = new Record();
        List<Record> busnessDeptList = Db.find(Db.getSql("admin.businessType.queryBusinessDeptList"));
        //根据不同的部门展示不同的字段
        String deptAttribute = JfinalConfig.crmProp.get("deptAttribute").replace('\'','\"');
        logger.info("columnbus-->deptAttribute信息：" + deptAttribute);
        JSONObject jsonObject = JSONObject.parseObject(deptAttribute);
        JSONObject deptJson = new JSONObject();
        for (Record value : busnessDeptList) {
            deptJson.clear();
            String deptName = value.get("name") + "";
            switch (deptName) {
                case CrmConstant.PUBLIC_SAFETY:
                    deptJson = jsonObject.getJSONObject("publicSafety");
                    break;
                case CrmConstant.FUTURE_CITY:
                    deptJson = jsonObject.getJSONObject("futureCity");
                    break;
                case CrmConstant.POWER:
                    deptJson = jsonObject.getJSONObject("power");
                    break;
                case CrmConstant.DRIVER:
                    deptJson = jsonObject.getJSONObject("driver");
                    break;
                default:
                    break;
            }
            StringBuilder deptArr  = new StringBuilder();
            for (String key : deptJson.keySet()) {
                if (deptArr.length() == 0) {
                    deptArr.append(key);
                } else {
                    deptArr.append(',').append(key);
                }
            }

            value.set("deptAtt", deptArr.toString());
        }

        record.set("deptList", busnessDeptList);
        return R.ok().put("data", record);
    }

    /**
     * 添加或更新应用场景
     * @author yue.li
     */
    @Before(Tx.class)
    public R addScenario(JSONObject scenario){
        AdminScenario adminScenario = TypeUtils.castToJavaBean(scenario, AdminScenario.class);
        logger.info(String.format("addScenario方法json %s",adminScenario.toJson()));
        List<Record> scenarioList = Db.find(Db.getSql("admin.scenario.queryScenarioByName"),adminScenario.getName());
        if (scenarioList != null &&  scenarioList.size() > 0){
            boolean existsFlag = false;
            for(Record record:scenarioList){
                if (adminScenario.getScenarioId() == null){
                    if(record.getStr("name").equals(adminScenario.getName())){
                        existsFlag = true;
                    }
                }else{
                    if(record.getStr("name").equals(adminScenario.getName()) && !record.getStr("scenario_id").equals(String.valueOf(adminScenario.getScenarioId()))){
                        existsFlag = true;
                    }
                }
            }
            if(existsFlag){
                return R.error("场景已经在系统中存在，请重新输入");
            }else{
                saveOrUpdate(adminScenario);
                return R.ok();
            }
        }else {
            saveOrUpdate(adminScenario);
            return R.ok();
        }
    }

    /**
     * 保存或更新应用场景
     * @author yue.li
     *
     */
    public void saveOrUpdate(AdminScenario adminScenario){
        if (adminScenario.getScenarioId() == null) {
            adminScenario.setCreateTime(new Date());
            adminScenario.save();
        } else {
            adminScenario.setUpdateTime(new Date());
            adminScenario.update();
        }
    }

    /**
     * 根据id 删除应用场景
     * @author yue.li
     *
     */
    public R deleteByIds(JSONObject jsonObject) {
        String[] idsArr = jsonObject.getString("scenarioIds").split(",");
        List<Record> idsList = new ArrayList<>(idsArr.length);
        for (String id : idsArr) {
            Record record = new Record();
            idsList.add(record.set("scenario_id", Integer.valueOf(id)));
        }
        return Db.tx(() -> {
            Db.batch(Db.getSql("admin.scenario.deleteByIds"), "scenario_id", idsList, 100);
            return true;
        }) ? R.ok() : R.error();
    }
}
