package com;

import com.jfinal.kit.Prop;
import com.jfinal.kit.PropKit;

import java.util.Properties;

/**
 * CRM properties.
 *
 * @author hao.fu
 * @since 2019/7/24 9:11
 */
public class JfinalProps extends Prop {

    private JfinalProps() {
        //放在第一位，可以让sr和columnBus的配置覆盖本地配置
        initRpcConfig();
    }

    private static class CrmPropsHolder {
        private static final JfinalProps instance = new JfinalProps();
    }

    /**
     * Return singleton instance of CrmProps.
     *
     * @return
     */
    public static JfinalProps getInstance() {
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
