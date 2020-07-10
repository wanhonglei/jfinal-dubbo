package com.kakarote.crm9.erp.crm.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.jfinal.aop.Aop;
import com.kakarote.crm9.BaseTest;
import com.kakarote.crm9.erp.crm.vo.CustomerWithUserNameVO;
import com.kakarote.crm9.utils.R;
import org.junit.Test;

import java.util.List;

/**
 * @Author: honglei.wan
 * @Description:
 * @Date: Create in 2020/4/10 7:45 下午
 */
public class CrmCustomerForEsbServiceTest extends BaseTest {

    private CrmCustomerForEsbService crmCustomerForEsbService = Aop.get(CrmCustomerForEsbService.class);

    @Test
    public void queryCrmCustomerInfo() {
        R r = crmCustomerForEsbService.queryCrmCustomerInfo(16840L, "1364130092");
        System.out.println(JSON.toJSONString(r, SerializerFeature.WRITE_MAP_NULL_FEATURES));
    }

	@Test
	public void queryCustomerByUsername() {
		List<CustomerWithUserNameVO> voList = crmCustomerForEsbService.queryCustomerByUsername("honglei.wan");
		System.out.println(voList);
	}
}
