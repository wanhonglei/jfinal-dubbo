package com.kakarote.crm9.mobile.service;

import com.jfinal.aop.Aop;
import com.jfinal.plugin.activerecord.Record;
import com.kakarote.crm9.BaseTest;
import org.junit.Test;

/**
 * Mobile Contacts Service Test
 *
 * @author hao.fu
 * @since 2020/2/24 9:45
 */
public class MobileContactsServiceTest extends BaseTest {

    private MobileContactsService mobileContactsService = Aop.get(MobileContactsService.class);

    @Test
    public void getContactsDetail() {
        Record record = mobileContactsService.getContactsDetail(441);
    }

}
