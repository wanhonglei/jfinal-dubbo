package com.kakarote.crm9.integration.controller;

import com.jfinal.aop.Before;
import com.jfinal.aop.Inject;
import com.jfinal.core.Controller;
import com.jfinal.core.paragetter.Para;
import com.jfinal.plugin.redis.Redis;
import com.kakarote.crm9.common.config.cache.RedisCache;
import com.kakarote.crm9.common.exception.CrmException;
import com.kakarote.crm9.common.spring.IocInterceptor;
import com.kakarote.crm9.utils.R;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.ScanParams;
import redis.clients.jedis.ScanResult;

/**
 * @Author: honglei.wan
 * @Description:CRM临时性任务代码 计划20200603执行 后可删除
 * @Date: Create in 2020/5/18 6:00 下午
 */
@Before(IocInterceptor.class)
@Slf4j
public class CrmTemporaryController extends Controller {

	@Inject
	private RedisCache redisCache;

	/**
	 * 清空登陆缓存
	 */
	public void clearLoginCache(@Para("key") String key) {
		Jedis jedis = Redis.use().getJedis();
		ScanParams scanParams = new ScanParams();
		if (StringUtils.isBlank(key)) {
			scanParams.match("CRM:login_cache:");
		}else if(key.contains("*")){
			throw new CrmException("redis 缓存修复key 不合法");
		}
		scanParams.count(100);
		String scanRet = "0";
		do {
			ScanResult<String> scanResult = jedis.scan(scanRet, scanParams);
			scanRet = scanResult.getStringCursor();

			scanResult.getResult().forEach(o -> {
				try {
					jedis.del(o);
				} catch (Exception e) {
					log.error("redis值删除报错：{}，跳过key:{}", e.getMessage(), o);
				}
			});
		} while (!"0".equals(scanRet));

		renderJson(R.ok());
	}

}
