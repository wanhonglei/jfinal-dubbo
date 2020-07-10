package com.kakarote.crm9.erp.admin.controller;

import com.alibaba.fastjson.JSONObject;
import com.google.gson.Gson;
import com.jfinal.aop.Inject;
import com.jfinal.core.Controller;
import com.kakarote.crm9.common.annotation.HttpEnum;
import com.kakarote.crm9.common.annotation.LogApiOperation;
import com.kakarote.crm9.common.annotation.NotNullValidate;
import com.kakarote.crm9.erp.admin.dto.CrmBusinessStatusDto;
import com.kakarote.crm9.erp.admin.service.CrmBusinessStatusService;
import com.kakarote.crm9.erp.crm.common.CrmErrorInfo;
import com.kakarote.crm9.utils.R;

import java.util.Objects;

/**
 * 商机阶段
 *
 * @author liming.guo
 */
public class AdminBusinessStatusController extends Controller {

    @Inject
    private CrmBusinessStatusService crmBusinessStatusService;

    private static Gson gson = new Gson();


    /**
     * 通过商机组查询商机阶段列表
     */
    @LogApiOperation(methodName = "queryBusinessStatusList")
    public void queryBusinessStatusList() {
        CrmBusinessStatusDto crmBusinessStatusDto = gson.fromJson(getRawData(), CrmBusinessStatusDto.class);
        if (Objects.isNull(crmBusinessStatusDto) || Objects.isNull(crmBusinessStatusDto.getGroupId())) {
            renderJson(R.error(CrmErrorInfo.PARAMS_NOT_EXSIT));
            return;
        }
        renderJson(crmBusinessStatusService.queryBusinessStatusList(crmBusinessStatusDto.getGroupId()));
    }

    /**
     * 新增商机阶段
     */
    @LogApiOperation(methodName = "addBusinessStatus")
    public void addBusinessStatus() {
        CrmBusinessStatusDto crmBusinessStatusDto = gson.fromJson(getRawData(), CrmBusinessStatusDto.class);
        renderJson(crmBusinessStatusService.addBusinessStatus(crmBusinessStatusDto));
    }

    /**
     * 获取商机阶段详情
     */
    @LogApiOperation(methodName = "queryBusinessStatusDetail")
    public void queryBusinessStatusDetail() {
        CrmBusinessStatusDto crmBusinessStatusDto = gson.fromJson(getRawData(), CrmBusinessStatusDto.class);
        if (Objects.isNull(crmBusinessStatusDto) || Objects.isNull(crmBusinessStatusDto.getStatusId())) {
            renderJson(R.error(CrmErrorInfo.PARAMS_NOT_EXSIT));
            return;
        }
        renderJson(crmBusinessStatusService.queryBusinessStatusDetail(crmBusinessStatusDto.getStatusId()));
    }

    /**
     * 编辑商机阶段详情
     */
    @LogApiOperation(methodName = "updateBusinessStatus")
    public void updateBusinessStatus() {
        CrmBusinessStatusDto crmBusinessStatusDto = gson.fromJson(getRawData(), CrmBusinessStatusDto.class);
        if (Objects.isNull(crmBusinessStatusDto) || Objects.isNull(crmBusinessStatusDto.getStatusId())) {
            renderJson(R.error(CrmErrorInfo.PARAMS_NOT_EXSIT));
            return;
        }
        renderJson(crmBusinessStatusService.updateBusinessStatusInfo(crmBusinessStatusDto));
    }

    /**
     * 校验商机阶段是否可以删除
     */
    @NotNullValidate(value = "statusId", type = HttpEnum.JSON, message = "商机阶段不能为空")
    @LogApiOperation(methodName = "checkEnableDelete")
    public void checkEnableDelete() {
        JSONObject jsonObject = JSONObject.parseObject(getRawData());
        Long statusId = jsonObject.getLong("statusId");
        renderJson(crmBusinessStatusService.checkEnableDelete(statusId));
    }


    /**
     * 删除商机阶段
     */
    @LogApiOperation(methodName = "deleteBusinessStatus")
    public void deleteBusinessStatus(){
        CrmBusinessStatusDto crmBusinessStatusDto = gson.fromJson(getRawData(), CrmBusinessStatusDto.class);
        if (Objects.isNull(crmBusinessStatusDto) || Objects.isNull(crmBusinessStatusDto.getStatusId())) {
            renderJson(R.error(CrmErrorInfo.PARAMS_NOT_EXSIT));
            return;
        }
        renderJson(crmBusinessStatusService.deleteBusinessStatus(crmBusinessStatusDto.getStatusId()));
    }

    /**
     * 校验商机阶段是否可以封存
     */
    @LogApiOperation(methodName = "checkEnableClose")
    public void checkEnableClose() {
        CrmBusinessStatusDto crmBusinessStatusDto = gson.fromJson(getRawData(), CrmBusinessStatusDto.class);
        if (Objects.isNull(crmBusinessStatusDto) || Objects.isNull(crmBusinessStatusDto.getStatusId())) {
            renderJson(R.error(CrmErrorInfo.PARAMS_NOT_EXSIT));
            return;
        }
        renderJson(crmBusinessStatusService.checkEnableClose(crmBusinessStatusDto.getStatusId()));
    }

    /**
     * 封存商机阶段
     */
    @LogApiOperation(methodName = "closeBusinessStatus")
    public void closeBusinessStatus() {
        CrmBusinessStatusDto crmBusinessStatusDto = gson.fromJson(getRawData(), CrmBusinessStatusDto.class);
        if (Objects.isNull(crmBusinessStatusDto) || Objects.isNull(crmBusinessStatusDto.getStatusId())) {
            renderJson(R.error(CrmErrorInfo.PARAMS_NOT_EXSIT));
            return;
        }
        renderJson(crmBusinessStatusService.closeBusinessStatus(crmBusinessStatusDto.getStatusId()));
    }

    /**
     * 解封商机阶段
     */
    @LogApiOperation(methodName = "openBusinessStatus")
    public void openBusinessStatus() {
        CrmBusinessStatusDto crmBusinessStatusDto = gson.fromJson(getRawData(), CrmBusinessStatusDto.class);
        if (Objects.isNull(crmBusinessStatusDto) || Objects.isNull(crmBusinessStatusDto.getStatusId())) {
            renderJson(R.error(CrmErrorInfo.PARAMS_NOT_EXSIT));
            return;
        }
        renderJson(crmBusinessStatusService.openBusinessStatus(crmBusinessStatusDto.getStatusId()));
    }

}
