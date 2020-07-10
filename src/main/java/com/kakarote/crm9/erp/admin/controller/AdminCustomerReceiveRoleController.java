package com.kakarote.crm9.erp.admin.controller;

import com.alibaba.fastjson.JSON;
import com.jfinal.aop.Inject;
import com.jfinal.core.Controller;
import com.jfinal.log.Log;
import com.kakarote.crm9.erp.admin.entity.AdminCustomerReceiveRole;
import com.kakarote.crm9.erp.admin.service.AdminCustomerReceiveRoleService;
import com.kakarote.crm9.utils.BaseUtil;
import com.kakarote.crm9.utils.R;
import java.util.List;

/**
 * 客户领取规则设置
 * @author yue.ii
 *
 */

public class AdminCustomerReceiveRoleController extends Controller {

    private Log logger = Log.getLog(getClass());

    @Inject
    private AdminCustomerReceiveRoleService adminCustomerReceiveRoleService;

    /**
     * 客户领取规则列表
     * @author yue.li
     */
    public void getCustomerReceiveRoleList() {
        try {
            renderJson(R.ok().put("data", adminCustomerReceiveRoleService.getCustomerReceiveRoleList()));
        } catch (Exception e) {
            logger.error(String.format("%s %s msg:%s", getClass().getSimpleName(), "getCustomerReceiveRoleList", BaseUtil.getExceptionStack(e)));
            renderJson(R.error(e.getMessage()));
        }
    }

    /**
     * 保存领取规格设置
     * @author yue.li
     */
    public void saveCustomerReceiveRole() {
        try {
            logger.info(String.format("saveCustomerReceiveRole json %s",getRawData()));
            List<AdminCustomerReceiveRole> adminCustomerReceiveRoleList = JSON.parseArray(getRawData(), AdminCustomerReceiveRole.class);
            renderJson(adminCustomerReceiveRoleService.saveCustomerReceiveRole(adminCustomerReceiveRoleList));
        } catch (Exception e) {
            logger.error(String.format("%s %s msg:%s", getClass().getSimpleName(), "saveCustomerReceiveRole", BaseUtil.getExceptionStack(e)));
            renderJson(R.error(e.getMessage()));
        }
    }

}
