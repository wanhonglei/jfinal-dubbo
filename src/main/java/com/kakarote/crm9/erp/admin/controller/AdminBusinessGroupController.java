package com.kakarote.crm9.erp.admin.controller;

import com.google.gson.Gson;
import com.jfinal.aop.Inject;
import com.jfinal.core.Controller;
import com.kakarote.crm9.common.annotation.LogApiOperation;
import com.kakarote.crm9.erp.admin.dto.CrmBusinessGroupDetailDto;
import com.kakarote.crm9.erp.admin.service.CrmBusinessGroupService;
import com.kakarote.crm9.erp.crm.common.CrmErrorInfo;
import com.kakarote.crm9.utils.R;
import org.apache.commons.lang3.StringUtils;

import java.util.Objects;

public class AdminBusinessGroupController  extends Controller {

    private static Gson gson = new Gson();

    @Inject
    private CrmBusinessGroupService crmBusinessGroupService;

    /**
     * 获取商机组列表
     */
    @LogApiOperation(methodName = "queryBusinessGroupList")
    public void queryBusinessGroupList() {
        renderJson(crmBusinessGroupService.queryBusinessGroupList());
    }

    /**
     * 获取商机组详情
     */
    @LogApiOperation(methodName = "queryBusinessGroupDetail")
    public void queryBusinessGroupDetail() {
        CrmBusinessGroupDetailDto businessGroupDetailDto = gson.fromJson(getRawData(), CrmBusinessGroupDetailDto.class);
        if(Objects.isNull(businessGroupDetailDto)||Objects.isNull(businessGroupDetailDto.getGroupId())){
            renderJson(R.error(CrmErrorInfo.PARAMS_NOT_EXSIT));
            return;
        }
        renderJson(crmBusinessGroupService.queryBUsinessGroupDetail(businessGroupDetailDto.getGroupId()));
    }

    /**
     * 新增商机组
     */
    @LogApiOperation(methodName = "addBusinessGroup")
    public void addBusinessGroup() {
        CrmBusinessGroupDetailDto businessGroupDetailDto = gson.fromJson(getRawData(),
                CrmBusinessGroupDetailDto.class);
        if (Objects.isNull(businessGroupDetailDto) || StringUtils.isBlank(businessGroupDetailDto.getGroupName())
                || StringUtils.isBlank(businessGroupDetailDto.getGroupEmail())
                || Objects.isNull(businessGroupDetailDto.getDeptId())) {
            renderJson(R.error(CrmErrorInfo.PARAMS_NOT_EXSIT));
            return;
        }
        renderJson(crmBusinessGroupService.addBusinessGroup(businessGroupDetailDto));
    }

    /**
     * 校验商机组部门是否重复设置
     */
    @LogApiOperation(methodName = "checkDept")
    public void checkDept() {
        CrmBusinessGroupDetailDto businessGroupDetailDto = gson.fromJson(getRawData(),
                CrmBusinessGroupDetailDto.class);
        if (Objects.isNull(businessGroupDetailDto) || Objects.isNull(businessGroupDetailDto.getDeptId())) {
            renderJson(R.error(CrmErrorInfo.PARAMS_NOT_EXSIT));
            return;
        }
        renderJson(crmBusinessGroupService.checkDept(businessGroupDetailDto.getDeptId()));
    }

    /**
     * 编辑商机组
     */
    @LogApiOperation(methodName = "updateBusinessGroup")
    public void updateBusinessGroup() {
        CrmBusinessGroupDetailDto businessGroupDetailDto = gson.fromJson(getRawData(),
                CrmBusinessGroupDetailDto.class);
        renderJson(crmBusinessGroupService.updateBusinessGroup(businessGroupDetailDto));
    }


    /**
     * 校验是否能够删除
     */
    @LogApiOperation(methodName = "checkEnableDelete")
    public void checkEnableDelete() {
        CrmBusinessGroupDetailDto businessGroupDetailDto = gson.fromJson(getRawData(),
                CrmBusinessGroupDetailDto.class);
        if (Objects.isNull(businessGroupDetailDto) || Objects.isNull(businessGroupDetailDto.getGroupId())) {
            renderJson(R.error(CrmErrorInfo.PARAMS_NOT_EXSIT));
            return;
        }
        renderJson(crmBusinessGroupService.checkEnableDelete(businessGroupDetailDto.getGroupId()));
    }

    /**
     * 删除商机组
     */
    @LogApiOperation(methodName = "deleteBusinessGroup")
    public void deleteBusinessGroup() {
        CrmBusinessGroupDetailDto businessGroupDetailDto = gson.fromJson(getRawData(),
                CrmBusinessGroupDetailDto.class);
        if (Objects.isNull(businessGroupDetailDto) || Objects.isNull(businessGroupDetailDto.getGroupId())) {
            renderJson(R.error(CrmErrorInfo.PARAMS_NOT_EXSIT));
            return;
        }
        renderJson(crmBusinessGroupService.deleteBusinessGroup(businessGroupDetailDto.getGroupId()));
    }


    /**
     * 复制商机组
     */
    @LogApiOperation(methodName = "copyBusinessGroup")
    public void copyBusinessGroup(){
        CrmBusinessGroupDetailDto businessGroupDetailDto = gson.fromJson(getRawData(),
                CrmBusinessGroupDetailDto.class);
        if (Objects.isNull(businessGroupDetailDto) || Objects.isNull(businessGroupDetailDto.getGroupId())) {
            renderJson(R.error(CrmErrorInfo.PARAMS_NOT_EXSIT));
            return;
        }
        renderJson(crmBusinessGroupService.copyBusinessGroup(businessGroupDetailDto.getGroupId()));

    }

}
