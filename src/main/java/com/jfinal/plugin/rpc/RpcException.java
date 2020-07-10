package com.jfinal.plugin.rpc;

/**
 * @Author: honglei.wan
 * @Description:rpc异常
 * @Date: Create in 2020/6/17 3:03 下午
 */
public class RpcException extends RuntimeException {
	public RpcException() {
		super();
	}

	public RpcException(String message) {
		super(message);
	}

	public RpcException(String message, Throwable cause) {
		super(message, cause);
	}

	public RpcException(Throwable cause) {
		super(cause);
	}

	protected RpcException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}
}
