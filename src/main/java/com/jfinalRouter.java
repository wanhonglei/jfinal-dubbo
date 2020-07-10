package com;

import com.jfinal.config.Routes;
import com.rpc.dubbo.test.controller.CrmTemporaryController;

/**
 * Integration router.
 *
 * @create 2019/6/26 14:45
 */
public class jfinalRouter extends Routes {

    @Override
    public void config() {
//        add("/crm/integration/temporary", CrmTemporaryController.class);
    }
}
