package com.kakarote.crm9.mobile.controller;

import com.jfinal.aop.Before;
import com.jfinal.aop.Inject;
import com.jfinal.core.Controller;
import com.jfinal.core.paragetter.Para;
import com.jfinal.log.Log;
import com.kakarote.crm9.common.annotation.NotNullValidate;
import com.kakarote.crm9.common.spring.IocInterceptor;
import com.kakarote.crm9.erp.admin.service.AdminDataDicService;
import com.kakarote.crm9.erp.crm.acl.user.CrmUser;
import com.kakarote.crm9.erp.crm.constant.CrmConstant;
import com.kakarote.crm9.mobile.common.MobileUtil;
import com.kakarote.crm9.utils.BaseUtil;
import com.kakarote.crm9.utils.R;

import java.util.Objects;

/**
 * MobileTagController class
 *
 * @author yue.li
 * @date 2020/01/13
 */
@Before(IocInterceptor.class)
public class MobileTagController extends Controller {

    private Log logger = Log.getLog(getClass());

    @Inject
    private AdminDataDicService adminDataDicService;

    /**
     * 根据标签类型查询标签下的内容
     * @param tagName 标签名称
     */
    @NotNullValidate(value = "tagName",message = "标签类型不能为空")
    public void tag(@Para("tagName")String tagName) {
        try{
            CrmUser crmUser = MobileUtil.getCrmUser(getRequest());
            if(Objects.isNull(crmUser)){
                renderJson(R.error(CrmConstant.NO_CRM_PERMISSION));
            }else{
                renderJson(R.ok().put("data",adminDataDicService.queryDataDicNoPageList(null,tagName)));
            }
        }catch (Exception e){
            logger.error(String.format("mobile tag msg:%s", BaseUtil.getExceptionStack(e)));
            renderJson(R.error(e.getMessage()));
        }
    }
}
