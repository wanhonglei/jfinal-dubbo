package com.kakarote.crm9.common.config.log;

import com.jfinal.log.ILogFactory;
import com.jfinal.log.Log;

/**
 * @Author: honglei.wan
 * @Description:
 * @Date: Create in 2020/3/6 15:43
 */
public class LogBackLogFactory implements ILogFactory {
    @Override
    public Log getLog(Class<?> clazz) {
        return new LogBackLog(clazz);
    }

    @Override
    public Log getLog(String name) {
        return new LogBackLog(name);
    }
}
