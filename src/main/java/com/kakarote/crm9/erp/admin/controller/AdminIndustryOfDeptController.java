package com.kakarote.crm9.erp.admin.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.jfinal.aop.Inject;
import com.jfinal.core.Controller;
import com.jfinal.log.Log;
import com.kakarote.crm9.common.annotation.HttpEnum;
import com.kakarote.crm9.common.annotation.NotNullValidate;
import com.kakarote.crm9.erp.admin.entity.AdminIndustryOfDept;
import com.kakarote.crm9.erp.admin.service.AdminIndustryOfDeptService;
import com.kakarote.crm9.utils.BaseUtil;
import com.kakarote.crm9.utils.R;

/**
 * 部门归属客户行业设置
 *
 * @author haihong.wu
 */

public class AdminIndustryOfDeptController extends Controller {

    Log logger = Log.getLog(getClass());

    @Inject
    private AdminIndustryOfDeptService adminIndustryOfDeptService;

    /**
     * 获取列表
     */
    public void getList() {
        try {
            renderJson(R.ok().put("data", adminIndustryOfDeptService.queryDeptListWithIndustryInfo()));
        } catch (Exception e) {
            logger.error(String.format("%s %s msg:%s", getClass().getSimpleName(), "getList", BaseUtil.getExceptionStack(e)));
            renderJson(R.error(e.getMessage()));
        }
    }

    /**
     * 获取客户行业
     */
    public void initIndustryList() {
        try {
            renderJson(R.ok().put("data", adminIndustryOfDeptService.initIndustryList()));
        } catch (Exception e) {
            logger.error(String.format("%s %s msg:%s", getClass().getSimpleName(), "initIndustryList", BaseUtil.getExceptionStack(e)));
            renderJson(R.error(e.getMessage()));
        }
    }

    /**
     * 删除客户行业
     */
    @NotNullValidate(value = "configId", type = HttpEnum.JSON, message = "configId is null")
    public void deleteIndustry() {
        try {
            JSONObject rawData = JSON.parseObject(getRawData());
            //行业ID(实际是dic_code)
            Long configId = rawData.getLong("configId");
            AdminIndustryOfDept.dao.deleteById(configId);
            renderJson(R.ok());
        } catch (Exception e) {
            logger.error(String.format("%s %s msg:%s", getClass().getSimpleName(), "deleteIndustry", BaseUtil.getExceptionStack(e)));
            renderJson(R.error(e.getMessage()));
        }
    }

    /**
     * 添加客户行业
     */
    @NotNullValidate(value = "deptId", type = HttpEnum.JSON, message = "deptId is null")
    @NotNullValidate(value = "industryCode", type = HttpEnum.JSON, message = "industryCode is null")
    @NotNullValidate(value = "industryName", type = HttpEnum.JSON, message = "industryName is null")
    @NotNullValidate(value = "industryType", type = HttpEnum.JSON, message = "industryType is null")
    public void addIndustry() {
        try {
            JSONObject param = JSON.parseObject(getRawData());
            Long deptId = param.getLong("deptId");
            String industryCode = param.getString("industryCode");
            String industryName = param.getString("industryName");
            Integer industryType = param.getInteger("industryType");
            adminIndustryOfDeptService.addIndustry(deptId, industryCode, industryName, industryType);
            renderJson(R.ok());
        } catch (Exception e) {
            logger.error(String.format("%s %s msg:%s", getClass().getSimpleName(), "addIndustry", BaseUtil.getExceptionStack(e)));
            renderJson(R.error(e.getMessage()));
        }
    }

    /**
     * 获取客户行业
     */
    public void queryReceiveIndustry() {
        try {
            renderJson(R.ok().put("data", adminIndustryOfDeptService.queryReceiveIndustry()));
        } catch (Exception e) {
            logger.error(String.format("%s %s msg:%s", getClass().getSimpleName(), "getList", BaseUtil.getExceptionStack(e)));
            renderJson(R.error(e.getMessage()));
        }
    }

}
