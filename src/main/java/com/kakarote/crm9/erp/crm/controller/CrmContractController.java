package com.kakarote.crm9.erp.crm.controller;

import com.jfinal.aop.Before;
import com.jfinal.aop.Inject;
import com.jfinal.core.Controller;
import com.kakarote.crm9.common.annotation.NotNullValidate;
import com.kakarote.crm9.common.annotation.Permissions;
import com.kakarote.crm9.common.spring.IocInterceptor;
import com.kakarote.crm9.erp.crm.service.CrmContractService;
import com.kakarote.crm9.utils.OssPrivateFileUtil;
import com.kakarote.crm9.utils.R;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 合同
 *
 * @Author: haihong.wu
 * @Date: 2020/6/12 3:09 下午
 */
@Before(IocInterceptor.class)
public class CrmContractController extends Controller {

    @Inject
    private CrmContractService crmContractService;

    @Autowired
    private OssPrivateFileUtil ossPrivateFileUtil;

    /**
     * 根据商机ID查询合同
     *
     * @param businessId
     */
    @Permissions("crm:business:contract_view")
    @NotNullValidate(value = "businessId", message = "商机ID不能为空")
    public void queryList(Long businessId) {
        renderJson(R.okWithData(crmContractService.listByBusinessIdWithPayment(businessId)));
    }

    /**
     * 根据ID获取合同数据
     *
     * @param contractId
     */
    @NotNullValidate(value = "contractId", message = "合同ID不能为空")
    public void queryById(Long contractId) {
        renderJson(R.okWithData(crmContractService.queryById(contractId, ossPrivateFileUtil)));
    }

}
