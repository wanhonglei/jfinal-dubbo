package com.kakarote.crm9;

import com.jfinal.core.JFinalFilter;
import com.jfinal.server.undertow.UndertowServer;
import com.kakarote.crm9.common.config.JfinalConfig;
import com.kakarote.crm9.common.config.server.CrmMobileFilter;
import com.kakarote.crm9.common.config.server.CrmSsoFilter;
import com.kakarote.crm9.common.constant.BaseConstant;
import com.kakarote.crm9.erp.crm.constant.CrmConstant;
import com.kakarote.crm9.utils.CrmProps;
import com.qxwz.buc.sso.client.filter.helper.SSOConstant;
import org.springframework.web.context.ContextLoaderListener;

/**
 * CRM startup class.
 *
 * @author hao.fu
 */
public class Application {

    public static void main(String[] args) {
        UndertowServer
                .create(JfinalConfig.class, CrmConstant.UNDERTOW_CONFIG_FILE)
                .setResourcePath(CrmConstant.UNDERTOW_SOURCE_PATH + BaseConstant.UPLOAD_PATH)
                .configWeb(item -> {
                    // spring context listener
                    item.addListener(ContextLoaderListener.class.getName());

                    // CRM SSO filter
                    String crmSsoFilter = CrmSsoFilter.class.getName();
                    item.addFilter(crmSsoFilter, crmSsoFilter);
                    item.addFilterInitParam(crmSsoFilter, SSOConstant.FILTER_INIT_PARAM_BUC_SERVER_URL, CrmProps.getInstance().get(CrmConstant.BUC_SERVER_URL_KEY));
                    item.addFilterInitParam(crmSsoFilter, SSOConstant.FILTER_INIT_PARAM_BUC_SSO_SERVER_URL, CrmProps.getInstance().get(CrmConstant.BUC_SERVER_URL_KEY));
                    item.addFilterUrlMapping(crmSsoFilter, CrmConstant.SERVLET_MAPPING_ALL);

                    // Mobile filter
                    String mobileFilter = CrmMobileFilter.class.getName();
                    item.addFilter(mobileFilter, mobileFilter);
                    item.addFilterUrlMapping(mobileFilter, CrmConstant.MOBILE_URL_PREFIX);

                    // JFinal filter
                    item.addFilter(CrmConstant.CRM_JFINAL_FILTER_NAME, JFinalFilter.class.getName());
                    item.addFilterInitParam(CrmConstant.CRM_JFINAL_FILTER_NAME, CrmConstant.JFINAL_FILTER_PARAM, JfinalConfig.class.getName());
                    item.addFilterUrlMapping(CrmConstant.CRM_JFINAL_FILTER_NAME, CrmConstant.SERVLET_MAPPING_ALL);
                })
                .start();
    }
}
