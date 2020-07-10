package com.kakarote.crm9.mobile.common;

import com.jfinal.config.Routes;
import com.kakarote.crm9.common.interceptor.CrmEventInterceptor;
import com.kakarote.crm9.mobile.controller.MobileAuthController;
import com.kakarote.crm9.mobile.controller.MobileContactsController;
import com.kakarote.crm9.mobile.controller.MobileCustomerController;
import com.kakarote.crm9.mobile.controller.MobileFileController;
import com.kakarote.crm9.mobile.controller.MobileNotesController;
import com.kakarote.crm9.mobile.controller.MobileSceneController;
import com.kakarote.crm9.mobile.controller.MobileSignInController;
import com.kakarote.crm9.mobile.controller.MobileTagController;

/**
 * Mobile Router
 *
 * @author hao.fu
 * @since 2019/12/23 14:04
 */
public class MobileRouter extends Routes {

    @Override
    public void config() {
        addInterceptor(new CrmEventInterceptor());

        add("/crm-mobile/customer", MobileCustomerController.class);
        add("/crm-mobile/signin", MobileSignInController.class);
        add("/crm-mobile/auth", MobileAuthController.class);
        add("/crm-mobile/scene", MobileSceneController.class);
        add("/crm-mobile/file", MobileFileController.class);
        add("/crm-mobile/tag", MobileTagController.class);
        add("/crm-mobile/contacts", MobileContactsController.class);
        add("/crm-mobile/notes", MobileNotesController.class);
    }
}
