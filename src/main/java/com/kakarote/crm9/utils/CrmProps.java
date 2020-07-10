package com.kakarote.crm9.utils;

import com.jfinal.kit.JsonKit;
import com.jfinal.kit.Prop;
import com.jfinal.kit.PropKit;
import com.jfinal.log.Log;
import com.kakarote.crm9.Application;
import com.kakarote.crm9.erp.crm.constant.CrmConstant;
import com.qxwz.columbus.client.ProjectConfigClient;

import java.util.Objects;
import java.util.Properties;

/**
 * CRM properties.
 *
 * @author hao.fu
 * @since 2019/7/24 9:11
 */
public class CrmProps extends Prop {

   private Log logger = Log.getLog(Application.class);

    private CrmProps() {
        //放在第一位，可以让sr和columnBus的配置覆盖本地配置
        initRpcConfig();

        properties.putAll(initRemoteProps());
    }

    /**
     * Load configurations from columbus.
     */
    private Properties initRemoteProps() {
        ProjectConfigClient client = new ProjectConfigClient(CrmConstant.CRM_SERVICE_COLUMBUS_CODE, CrmConstant.CRM_SR_CODE);
        client.init();
        boolean serverAvailable = client.serverAvailable();
        if (serverAvailable && Objects.nonNull(client.configs())) {
            Properties prop = client.configs();
            logger.info(String.format("-------->>>>>>prop size: %s", prop.size()));
            logger.info(String.format("-------->>>>>>prop: %s", JsonKit.toJson(prop)));
            return prop;
        }
        return new Properties();
    }

    private static class CrmPropsHolder {
        private static final CrmProps instance = new CrmProps();
    }

    /**
     * Return singleton instance of CrmProps.
     *
     * @return
     */
    public static CrmProps getInstance() {
        return CrmPropsHolder.instance;
    }

    /**
     * 初始化prc配置信息
     */
    private void initRpcConfig(){
        Properties rpcPro = PropKit.use("config/jfinal-rpc.properties").getProperties();
        properties.putAll(rpcPro);
    }

}
