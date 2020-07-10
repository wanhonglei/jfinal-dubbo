package com.rpc.dubbo.test.controller;

import cn.hutool.core.util.IdUtil;
import com.jfinal.core.Controller;
import com.jfinal.plugin.rpc.annotation.RPCInject;
import com.qxwz.venus.biz.api.RegisterBizService;
import com.qxwz.venus.biz.dto.VirtualRegInfo;
import lombok.extern.slf4j.Slf4j;

/**
 * @Author: honglei.wan
 * @Description: 测试调用dubbo消费者功能
 * @Date: Create in 2020/5/18 6:00 下午
 */
@Slf4j
public class CrmTemporaryController extends Controller {

	@RPCInject
	private RegisterBizService registerBizService;

	/**
	 * 清空登陆缓存
	 */
	public void getSomethingAsConsumer() {
		//TODO 测试消费者功能
	}

}
