package com.kakarote.crm9.common.config.cache;

import com.jfinal.plugin.redis.serializer.FstSerializer;
import com.jfinal.plugin.redis.serializer.ISerializer;
import com.jfinal.plugin.redis.serializer.JdkSerializer;

/**
 * @Author: honglei.wan
 * @Description:自定义JDK序列化工具
 * @Date: Create in 2020/5/19 9:09 上午
 */
public class MyJdkSerializer extends JdkSerializer {

	public static final ISerializer MY_JDK_SERIALIZER = new MyJdkSerializer();

	private static final ISerializer FST_SERIALIZER = new FstSerializer();

	@Override
	public byte[] fieldToBytes(Object field) {
		return valueToBytes(field);
	}

	@Override
	public Object fieldFromBytes(byte[] bytes) {
		return valueFromBytes(bytes);
	}

	@Override
	public byte[] valueToBytes(Object value) {
		return super.valueToBytes(value);
	}

	@Override
	public Object valueFromBytes(byte[] bytes) {
		try {
			return super.valueFromBytes(bytes);
		} catch (Exception e) {
			//当转换报错的时候，尝试使用FST进行转换
			try {
				return FST_SERIALIZER.valueFromBytes(bytes);
			} catch (Exception ex) {
				throw new RuntimeException(ex);
			}
		}
	}
}
