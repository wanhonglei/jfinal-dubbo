package com;

import com.jfinal.core.JFinalFilter;
import com.jfinal.server.undertow.UndertowServer;

/**
 * CRM startup class.
 *
 * @author hao.fu
 */
public class Application {

    public static void main(String[] args) {
        UndertowServer
                .create(JfinalConfig.class, "config/undertow.txt")
                .configWeb(item -> {

                    // JFinal filter
                    item.addFilter("JFINAL", JFinalFilter.class.getName());
                    item.addFilterInitParam("JFINAL", "configClass", JfinalConfig.class.getName());
                    item.addFilterUrlMapping("JFINAL", "/*");
                })
                .start();
    }
}
