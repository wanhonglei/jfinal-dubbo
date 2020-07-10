package com.kakarote.crm9.utils;

import com.jfinal.plugin.activerecord.Record;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 返回数据
 */
public class R extends LinkedHashMap<String, Object> implements Serializable {
	private static final long serialVersionUID = 1L;

	public R() {
		put("code", 0);
	}

	public static R error() {
		return error(500, "未知异常，请联系管理员");
	}

	public static R error(String msg) {
		return error(500, msg);
	}

	/**
	 * 返回错误
	 * @param code 500 默认错误code 10000 前端不自动关闭错误
	 * @param msg
	 * @return
	 */
	public static R error(int code, String msg) {
		R r = new R();
		r.put("code", code);
		r.put("msg", msg);
		return r;
	}
	public static R error( String msg, Record data) {
		R r = new R();
		r.put("code", 500);
		r.put("msg", msg);
		r.put("data", data);
		return r;
	}
	public static R ok(String msg) {
		R r = new R();
		r.put("msg", msg);
		return r;
	}

	public static R ok(Map<String, Object> map) {
		R r = new R();
		r.putAll(map);
		return r;
	}

	public static R ok() {
		return new R();
	}

	public static R okWithData(Object data) {
		R r = ok();
		r.put("data", data);
		return r;
	}

	@Override
	public R put(String key, Object value) {
		super.put(key, value);
		return this;
	}

	public boolean isSuccess(){
		return super.containsKey("code")&&super.get("code").equals(0);
	}

	public boolean isFailed(){
		return !super.containsKey("code") || !super.get("code").equals(0);
	}

	public static R isSuccess(boolean b,String msg){
		return b?R.ok(msg):R.error(msg);
	}
	public static R isSuccess(boolean b){
		return isSuccess(b,null);
	}

}
