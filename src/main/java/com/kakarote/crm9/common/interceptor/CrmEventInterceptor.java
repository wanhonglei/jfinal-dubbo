package com.kakarote.crm9.common.interceptor;

import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.jfinal.aop.Interceptor;
import com.jfinal.aop.Invocation;
import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.render.JsonRender;
import com.jfinal.render.Render;
import com.kakarote.crm9.common.annotation.CrmEventAnnotation;
import com.kakarote.crm9.erp.crm.common.CrmEventEnum;
import com.kakarote.crm9.erp.crm.entity.CrmCustomerExt;
import com.kakarote.crm9.utils.BaseUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.Objects;

/**
 * @Author: honglei.wan
 * @Description:Crm事件拦截器
 * @Date: Create in 2020/5/15 2:14 下午
 */
@Slf4j
public class CrmEventInterceptor implements Interceptor {
	@Override
	public void intercept(Invocation inv) {

		CrmEventAnnotation crmEventAnnotation = inv.getMethod().getAnnotation(CrmEventAnnotation.class);
		inv.invoke();
		try {
			//当返回成功的时候，更新事件
			Controller controller = inv.getController();
			if (controller == null){
				log.info("Crm事件拦截器没有获取到正常返回信息，controller null异常");
				return;
			}

			Render controllerRender = controller.getRender();
			if (controllerRender instanceof JsonRender){
				JsonRender render = (JsonRender) controllerRender;
				if (crmEventAnnotation != null && "0".equals(JSON.parseObject(render.getJsonText()).getString("code"))) {
					CrmEventEnum crmEventEnum = crmEventAnnotation.crmEventEnum();
					switch (crmEventEnum) {
						case LATELY_FOLLOW_EVENT:
							doLatelyEvent(controller);
							break;
						default:
							break;
					}
				}
			} else {
				log.info("Crm事件拦截器没有获取到正常返回信息，默认正常业务点抛出了异常");
			}

		} catch (Exception e) {
			log.error("Crm事件拦截器异常", e);
		}
	}

	/**
	 * 最近跟进事件
	 *
	 * @param controller 控制类
	 */
	private void doLatelyEvent(Controller controller) {
		String customerIdString = null;
		JSONObject jsonObject = JSON.parseObject(controller.getRawData());
		if (jsonObject != null) {
			//添加通话记录,客户类型时
			if ("crm_customer".equals(jsonObject.getString("types"))) {
				customerIdString = jsonObject.getString("typesId");
			}
			//客户通话记录悬浮框、mobile 创建小记
			if (StringUtils.isBlank(customerIdString)) {
				customerIdString = jsonObject.getString("customerIds");
			}
			//保存小记、编辑小记
			if (StringUtils.isBlank(customerIdString)) {
				customerIdString = jsonObject.getString("customerId");
			}
		}

		//客户新建小记tab页
		if (StringUtils.isBlank(customerIdString)) {
			customerIdString = controller.getPara("typesId");
		}

		Integer customerId = null;
		if (StringUtils.isNotBlank(customerIdString)) {
			customerId = Integer.valueOf(customerIdString);
		}

		if (Objects.isNull(customerId)) {
			return;
		}

		CrmCustomerExt crmCustomerExt = CrmCustomerExt.dao.findFirst(Db.getSql("crm.customerExt.queryByCustomerId"), customerId);
		if (crmCustomerExt == null) {
			crmCustomerExt = new CrmCustomerExt();
			crmCustomerExt.setCustomerId(customerId);
			crmCustomerExt.setLatelyFollowUserId(Math.toIntExact(BaseUtil.getUserId()));
			crmCustomerExt.setLatelyFollowTime(DateUtil.date());
			crmCustomerExt.save();
		} else {
			crmCustomerExt.setLatelyFollowUserId(Math.toIntExact(BaseUtil.getUserId()));
			crmCustomerExt.setLatelyFollowTime(DateUtil.date());
			crmCustomerExt.update();
		}
	}

}
