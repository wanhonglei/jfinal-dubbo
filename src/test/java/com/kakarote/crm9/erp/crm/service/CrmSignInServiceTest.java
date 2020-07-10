package com.kakarote.crm9.erp.crm.service;

import com.jfinal.aop.Aop;
import com.jfinal.aop.Inject;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.kakarote.crm9.BaseTest;
import com.kakarote.crm9.common.config.paragetter.BasePageRequest;
import com.kakarote.crm9.erp.crm.acl.user.CrmUser;
import com.kakarote.crm9.erp.crm.dto.CrmSignInPageRequest;
import com.kakarote.crm9.erp.crm.entity.LocationInfo;
import com.kakarote.crm9.mobile.entity.MobileNewSignInRequest;
import com.kakarote.crm9.utils.R;
import org.junit.Test;

import java.util.List;

/**
 * Crm SignIn Service Unit Test
 *
 * @author hao.fu
 * @since 2020/1/2 11:33
 */
public class CrmSignInServiceTest extends BaseTest {

    @Inject
    private CrmSignInService crmSignInService = Aop.get(CrmSignInService.class);

    @Test
    public void querySignInPageList() {
        CrmUser user = mockCrmUser("0946");
        BasePageRequest<CrmSignInPageRequest> signInRequest = mockSignInPageListRequest();
        Page<Record> results = crmSignInService.querySignInPageList(signInRequest, user);
    }

    @Test
    public void querySignInRecords() {
        CrmUser user = mockCrmUser("0946");
        BasePageRequest<CrmSignInPageRequest> request = mockSignInPageListRequest();
        List<Record> records = crmSignInService.querySignInRecords(request, user);
    }

    @Test
    public void updateSignInNotes() {
        R result = crmSignInService.updateSignInNotes("fc71bb8299ba4441bf6c500cf32f069e", "");
    }

    @Test
    public void getLocation() {
        CrmUser user = mockCrmUser("0946");
        LocationInfo locationInfo = crmSignInService.getLocation(user, 121.50272, 31.339844);
    }

    @Test
    public void getSigninDetail() {
        Record record = crmSignInService.getSigninDetail("fioritocks029v92xg",null);
    }

    @Test
    public void deleteSigninBySigninId() {
        R result = crmSignInService.deleteSigninBySigninId("a08345dd98ad4f3a97812a92b4f503d6");
    }

    @Test
    public void addSignInRecord() {
        MobileNewSignInRequest request = new MobileNewSignInRequest("121.50232", "31.335844", "上海市宝山区", "170000", "test", "上海市", "上海市", "宝山区", 1, false,null,null);
        CrmUser user = mockCrmUser("0946");
        R result = crmSignInService.addSignInRecord(request, user);
    }

    private BasePageRequest<CrmSignInPageRequest> mockSignInPageListRequest() {
        CrmSignInPageRequest request = new CrmSignInPageRequest("", "", "", "", "", "", "", false, 8, 1,0);
        return new BasePageRequest<>(request.toString(), CrmSignInPageRequest.class);
    }

}

