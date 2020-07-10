package com.kakarote.crm9.mobile.service;

import com.jfinal.kit.Kv;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.SqlPara;
import com.kakarote.crm9.common.config.paragetter.BasePageRequest;
import com.kakarote.crm9.mobile.entity.AbstractPageListRequest;
import com.kakarote.crm9.mobile.entity.MobileSignInListRequest;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

/**
 * Mobile Sign In Service
 *
 * @author hao.fu
 * @since 2019/12/23 14:04
 */
public class MobileSignInService extends AbstractMobileService {

    @Override
    SqlPara setupSqlPara(BasePageRequest<? extends AbstractPageListRequest> pageRequest, List<Integer> authorizedUserIds) {
        MobileSignInListRequest request = (MobileSignInListRequest) pageRequest.getData();
        Kv kv = setCommonKv(request, authorizedUserIds);

        // customer name
        String customerName = request.getCustomerName();
        if (StringUtils.isNotEmpty(customerName)) {
            kv.set("customerName", customerName);
        }

        SqlPara sqlPara = Db.getSqlPara("crm.signin.queryMobileSignInPageList", kv);
        logger.info("mobile sign in list query: " + sqlPara);
        return sqlPara;
    }

}
