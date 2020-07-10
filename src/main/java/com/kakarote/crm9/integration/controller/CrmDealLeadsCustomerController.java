package com.kakarote.crm9.integration.controller;

import com.jfinal.aop.Before;
import com.jfinal.core.Controller;
import com.jfinal.log.Log;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.kakarote.crm9.common.spring.IocInterceptor;
import com.kakarote.crm9.erp.crm.constant.CrmConstant;
import com.kakarote.crm9.erp.crm.entity.CrmCustomer;
import com.kakarote.crm9.utils.R;
import org.apache.commons.collections.CollectionUtils;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.List;
import java.util.stream.Collectors;

/**
 * CrmDealLeadsCustomerController.
 *
 * @author yue.li
 * @create 2019/12/29 10:00
 */
@Before(IocInterceptor.class)
public class CrmDealLeadsCustomerController extends Controller {

    private Log logger = Log.getLog(getClass());

    /**
     * 处理线索转化的客户生成客编
     * @author yue.li
     */
    public void dealLeadsCustomer() {
        long start = System.currentTimeMillis();
        logger.info("dealLeadsCustomer: " + LocalDate.now());
        List<Record> recordList = Db.find(Db.getSql("crm.customer.queryCustomerNoCustomerNo"));
        List<CrmCustomer> customerList = recordList.stream().map(item-> new CrmCustomer()._setOrPut(item.getColumns())).collect(Collectors.toList());
        if(CollectionUtils.isNotEmpty(customerList)) {
            for(CrmCustomer crmCustomer : customerList) {
                String customerNo = CrmConstant.QXWZ + crmCustomer.getCustomerId() + "_" + (Calendar.getInstance().get(Calendar.YEAR) -1);
                Db.update(Db.getSql("crm.customer.dealLeadsCustomer"), customerNo, crmCustomer.getCustomerId());
            }
        }
        long end = System.currentTimeMillis();
        logger.info("dealLeadsCustomer cost:" + (end - start)/1000 + "s");
        renderJson(R.ok());
    }
}
