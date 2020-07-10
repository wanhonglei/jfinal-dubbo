package com.kakarote.crm9.erp.crm.service;

import com.jfinal.aop.Aop;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.kakarote.crm9.BaseTest;
import com.kakarote.crm9.common.config.paragetter.BasePageRequest;
import com.kakarote.crm9.erp.crm.dto.CrmCallRecordDto;
import org.junit.Assert;
import org.junit.Test;

/**
 * @Author: honglei.wan
 * @Description:
 * @Date: Create in 2020/1/9 17:00
 */
public class CrmContactRecordServiceTest extends BaseTest {

    private CrmContactRecordService crmContactRecordService = Aop.get(CrmContactRecordService.class);

    @Test
    public void queryContactList() {
        BasePageRequest<CrmCallRecordDto> basePageRequest = new BasePageRequest<>(1,15);
        CrmCallRecordDto con = new CrmCallRecordDto();
        con.setQueryType(2);
        con.setRecordId(1);
        con.setRecordType(1);

        con.setTypes("crm_customer");
        con.setTypesId(1212);

        basePageRequest.setData(con);

        Page<Record> recordPage = crmContactRecordService.queryContactList(basePageRequest, 121L);

        Assert.assertNotNull(recordPage);
    }
}
