package com.kakarote.crm9.common.interceptor;

import cn.hutool.core.util.ArrayUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Strings;
import com.jfinal.aop.Interceptor;
import com.jfinal.aop.Invocation;
import com.jfinal.core.Controller;
import com.jfinal.plugin.redis.Redis;
import com.kakarote.crm9.common.annotation.HttpEnum;
import com.kakarote.crm9.common.annotation.NotNullValidate;
import com.kakarote.crm9.common.exception.CrmException;
import com.kakarote.crm9.erp.crm.constant.CrmConstant;
import com.kakarote.crm9.utils.BaseUtil;
import com.kakarote.crm9.utils.R;
import com.qxwz.buc.sso.client.filter.helper.LogonUser;
import com.qxwz.buc.sso.client.filter.helper.SSOFilterHelper;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.Objects;

/**
 * @author honglei.wan
 */
public class ErpInterceptor implements Interceptor {

    private Logger logger = LoggerFactory.getLogger(ErpInterceptor.class);

    @Override
    public void intercept(Invocation invocation) {
        Controller controller = invocation.getController();

        try {
            HttpServletRequest request = controller.getRequest();
            // important! before enter to controller, initialize request for base util.
            BaseUtil.setRequest(request);

            logger.info("###########ErpInterceptor request url: {}", controller.getRequest().getRequestURL());

            // check whether buc token is expired
            if (BaseUtil.needCheckLoginStatus(controller.getRequest())) {
                String bucToken = BaseUtil.getTokenWithOutPrefix(controller.getRequest());
                LogonUser bucUser = SSOFilterHelper.retrieveBucWebUser(bucToken);

                logger.info("###########ErpInterceptor bucToken: {}", bucToken);

                if (bucUser == null || Strings.isNullOrEmpty(bucUser.getStaffNo()) || Strings.isNullOrEmpty(bucUser.getEmail())) {
                    logger.info("#############session expired for: {}", bucToken);
                    Redis.use().del(bucToken);

                    controller.renderJson(R.error(HttpStatus.SC_MOVED_TEMPORARILY, "Please login first!"));
                    return;
                }

                logger.info("###########ErpInterceptor staffNo: {}, userName:{}", bucUser.getStaffNo(), bucUser.getFullName());

                // 如果PC非userInfo方法需校验
                if (StringUtils.isNotEmpty(controller.getRequest().getRequestURI())
                        && !controller.getRequest().getRequestURI().startsWith(CrmConstant.CRM_USER_INFO)
                        && !controller.getRequest().getRequestURI().startsWith(CrmConstant.CRM_SYSTEM_CONFIG)
                        && !controller.getRequest().getRequestURI().startsWith(CrmConstant.CRM_ROLE_AUTH)
                        && Objects.isNull(BaseUtil.getCrmUser())) {
                    logger.info("#############clear bucToken for: {}", bucToken);
                    Redis.use().del(bucToken);
                    controller.renderJson(R.error(HttpStatus.SC_MOVED_TEMPORARILY, "check crmUser is null,please login first!"));
                    return;
                }
            }

            NotNullValidate[] validates = invocation.getMethod().getAnnotationsByType(NotNullValidate.class);
            if (ArrayUtil.isNotEmpty(validates)) {
                // 枚举对象实例比较使用 ==
                if (HttpEnum.PARA == validates[0].type()) {
                    for (NotNullValidate validate : validates) {
                        if (controller.getPara(validate.value()) == null) {
                            controller.renderJson(R.error(500, validate.message()));
                            return;
                        }
                    }
                } else if (HttpEnum.JSON == validates[0].type()) {
                    JSONObject jsonObject = JSON.parseObject(controller.getRawData());
                    for (NotNullValidate validate : validates) {
                        if (Objects.isNull(jsonObject) || !jsonObject.containsKey(validate.value()) || jsonObject.get(validate.value()) == null) {
                            controller.renderJson(R.error(500, validate.message()));
                            return;
                        }
                    }
                }
            }
            invocation.invoke();
        } catch (Throwable t) {
            t = getEnhanceTargetException(t);
            if (t instanceof CrmException) {
                controller.renderJson(R.error(t.getMessage()));
                logger.error("CRM 业务异常:{}", t.getMessage());
            } else {
                controller.renderJson(R.error("服务器响应异常"));
                logger.error("服务器响应异常:请求地址:" + controller.getRequest().getRequestURL() +",请求参数:"+ controller.getRawData() +"," ,t);
            }
        } finally {
            BaseUtil.removeThreadLocal();
        }
    }

    /**
     * 解析异常信息
     * @param t
     * @return
     */
    private Throwable getEnhanceTargetException(Throwable t) {
        if (t instanceof UndeclaredThrowableException) {
            Throwable undeclaredThrowable = ((UndeclaredThrowableException) t).getUndeclaredThrowable();
            if (undeclaredThrowable instanceof InvocationTargetException) {
                Throwable targetException = ((InvocationTargetException) undeclaredThrowable).getTargetException();
                if (targetException instanceof CrmException) {
                    return targetException;
                }
            }
        }
        return t;
    }
}
