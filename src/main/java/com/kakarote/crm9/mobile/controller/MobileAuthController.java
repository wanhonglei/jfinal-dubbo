package com.kakarote.crm9.mobile.controller;

import com.jfinal.aop.Inject;
import com.jfinal.core.Controller;
import com.jfinal.log.Log;
import com.kakarote.crm9.erp.admin.service.AdminRoleService;
import com.kakarote.crm9.erp.crm.acl.user.CrmUser;
import com.kakarote.crm9.erp.crm.constant.CrmConstant;
import com.kakarote.crm9.mobile.common.MobileUtil;
import com.kakarote.crm9.mobile.service.MobileAuthService;
import com.kakarote.crm9.utils.BaseUtil;
import com.kakarote.crm9.utils.R;

import java.util.Objects;

/**
 * Mobile Auth Controller
 *
 * @author hao.fu
 * @since 2019/12/24 9:49
 */
public class MobileAuthController extends Controller {

    private Log logger = Log.getLog(getClass());

    @Inject
    private MobileAuthService mobileAuthService;

    @Inject
    private AdminRoleService adminRoleService;

    /**
     * 通过钉钉code获取CRM token
     *
     */
    public void getCrmToken() {
        try{
            renderJson(mobileAuthService.getCrmToken(getRequest()));
        }catch (Exception e){
            logger.error(String.format("getCrmToken occurs exception: %s", BaseUtil.getExceptionStack(e)));
            renderJson(R.error(e.getMessage()));
        }

    }

    /**
     * 获取权限接口
     * @author yue.li
     */
    public void getAuth() {
        logger.info("mobile getAuth start");
        CrmUser crmUser = MobileUtil.getCrmUser(getRequest());
        if(Objects.isNull(crmUser) || Objects.isNull(crmUser.getCrmAdminUser())){
            renderJson(R.error(CrmConstant.NO_CRM_PERMISSION));
        }else{
            renderJson(R.ok().put("data", adminRoleService.auth(crmUser.getCrmAdminUser().getUserId())));
        }
    }
}
