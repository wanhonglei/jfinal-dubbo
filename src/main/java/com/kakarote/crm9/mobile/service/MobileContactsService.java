package com.kakarote.crm9.mobile.service;

import com.jfinal.kit.Kv;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.SqlPara;
import com.kakarote.crm9.common.config.paragetter.BasePageRequest;
import com.kakarote.crm9.erp.crm.constant.CrmConstant;
import com.kakarote.crm9.erp.crm.entity.CrmCustomer;
import com.kakarote.crm9.mobile.entity.AbstractPageListRequest;
import com.kakarote.crm9.mobile.entity.MobileContactsListRequest;
import com.kakarote.crm9.utils.R;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

/**
 * Mobile Contacts Service
 *
 * @author hao.fu
 * @since 2020/1/15 15:57
 */
public class MobileContactsService extends AbstractMobileService {

    @Override
    SqlPara setupSqlPara(BasePageRequest<? extends AbstractPageListRequest> pageRequest, List<Integer> authorizedUserIds) {
        MobileContactsListRequest request = (MobileContactsListRequest) pageRequest.getData();
        Kv kv = setCommonKv(request, authorizedUserIds);

        // search key: 联系人名称/手机号码
        String searchKey = request.getSearchKey();
        if (StringUtils.isNotEmpty(searchKey)) {
            kv.set("searchKey", searchKey);
        }

        // 联系人角色
        String contactsRole = request.getContactsRole();
        if (StringUtils.isNotEmpty(contactsRole)) {
            kv.set("contactsRole", contactsRole);
        }

        SqlPara sqlPara = Db.getSqlPara("crm.contact.queryMobileContactsPageList", kv);
        logger.info("mobile contacts list query: " + sqlPara);
        return sqlPara;
    }

    /**
     * Get contacts by id.
     *
     * @param contactsId contacts id
     * @return {@code Record}
     */
    public Record getContactsDetail(Integer contactsId) {
        return Db.findFirst(Db.getSql("crm.contact.getContactsDetailByContactsId"), contactsId);
    }


    public R getContacts(BasePageRequest<CrmCustomer> basePageRequest) {
        Integer customerId = basePageRequest.getData().getCustomerId().intValue();
        Page<Record> pageRecord = Db.paginate(basePageRequest.getPage(), basePageRequest.getLimit(), new SqlPara().setSql(Db.getSql("crm.customer.getMobileContacts")).addPara(customerId));
        if (pageRecord != null && pageRecord.getList() != null && pageRecord.getList().size() > 0) {
            List<Record> records = pageRecord.getList();
            records.stream().forEach(item -> {
                if (item.getStr(CrmConstant.SENSITIVE_MOBILE) != null && !item.getStr(CrmConstant.SENSITIVE_MOBILE).isEmpty()) {
                    item.set(CrmConstant.SENSITIVE_MOBILE, "true");
                }
            });
            pageRecord.setList(records);
        }
        return R.ok().put("data", pageRecord);
    }
}
