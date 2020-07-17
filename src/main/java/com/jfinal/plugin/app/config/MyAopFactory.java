package com.jfinal.plugin.app.config;

import com.jfinal.aop.AopFactory;
import com.jfinal.core.Controller;
import com.jfinal.log.Log;
import com.jfinal.plugin.activerecord.CrmModel;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.rpc.Irpc;
import com.jfinal.plugin.rpc.RpcManager;
import com.jfinal.plugin.rpc.annotation.RPCInject;
import com.jfinal.plugin.rpc.config.RpcReferenceConfig;

import java.lang.reflect.Field;

/**
 * @Author: honglei.wan
 * @Description:
 * @Date: Create in 2020/6/18 11:09 上午
 */
public class MyAopFactory extends AopFactory {

	private static final Log logger = Log.getLog(MyAopFactory.class);

	private static MyAopFactory me = new MyAopFactory();

	public static MyAopFactory me() {
		return me;
	}

	private MyAopFactory() {
		setInjectSuperClass(true);
	}

	@Override
	protected void doInject(Class<?> targetClass, Object targetObject) throws ReflectiveOperationException {
		super.doInject(targetClass,targetObject);

		targetClass = getUsefulClass(targetClass);
		Field[] fields = targetClass.getDeclaredFields();

		if (fields.length != 0) {

			for (Field field : fields) {

				RPCInject rpcInject = field.getAnnotation(RPCInject.class);
				if (rpcInject != null) {
					doInjectRpc(targetObject, field, rpcInject);
				}
			}
		}

		// 是否对超类进行注入
		if (injectSuperClass) {
			Class<?> c = targetClass.getSuperclass();
			if (c != Controller.class
					&& c != Object.class
					&& c != CrmModel.class
					&& c != Model.class
					&& c != null
			) {
				doInject(c, targetObject);
			}
		}
	}

	/**
	 * 注入 rpc service
	 *
	 * @param targetObject
	 * @param field
	 * @param rpcInject
	 */
	private void doInjectRpc(Object targetObject, Field field, RPCInject rpcInject) {
		try {
			setFieldValue(field, targetObject, RpcManager.me().getRpc().serviceObtain(field.getType(), new RpcReferenceConfig(rpcInject)));
		} catch (Exception ex) {
			logger.error("can not inject rpc service in " + targetObject.getClass() + " by config " + rpcInject, ex);
		}
	}

	protected void setFieldValue(Field field, Object toObj, Object data) throws IllegalAccessException {
		field.setAccessible(true);
		field.set(toObj, data);
	}

}
