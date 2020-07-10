package com.kakarote.crm9.mobile.service;

import com.google.common.collect.Lists;
import com.jfinal.aop.Aop;
import com.jfinal.kit.Kv;
import com.jfinal.log.Log;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.SqlPara;
import com.kakarote.crm9.common.config.paragetter.BasePageRequest;
import com.kakarote.crm9.erp.admin.service.AdminUserService;
import com.kakarote.crm9.erp.crm.acl.user.CrmUser;
import com.kakarote.crm9.erp.crm.common.CrmCustomerDateEnum;
import com.kakarote.crm9.mobile.entity.AbstractPageListRequest;
import com.kakarote.crm9.utils.CrmDateUtil;
import com.kakarote.crm9.utils.SceneUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Abstarct Mobile Service
 *
 * @author hao.fu
 * @since 2020/1/17 16:48
 */
public abstract class AbstractMobileService {

    protected Log logger = Log.getLog(getClass());

    private AdminUserService adminUserService = Aop.get(AdminUserService.class);

    public Page<Record> queryPageList(BasePageRequest<? extends AbstractPageListRequest> pageRequest, CrmUser user) {
        List<Integer> authorizedUserIds = getAuthorizedUserIds(pageRequest, user);
        if(CollectionUtils.isEmpty(authorizedUserIds)){
            return new Page<>();
        }

        SqlPara para = setupSqlPara(pageRequest, authorizedUserIds);
        if (para == null) {
            return new Page<>();
        } else {
            return Db.paginate(pageRequest.getPage(), pageRequest.getLimit(), para);
        }
    }

    /**
     * 获取授权访问的用户数据的用户id
     *
     * @param pageRequest {@code BasePageRequest}
     * @param user crm user
     * @return 授权可访问的用户id集合
     */
    protected List<Integer> getAuthorizedUserIds(BasePageRequest<? extends AbstractPageListRequest> pageRequest, CrmUser user) {
        AbstractPageListRequest request =  pageRequest.getData();
        return SceneUtil.getAuthorizedUserIdsForBizScene(request.getBizType(), request.getSceneId(), user);
    }

    /**
     * 设置sql parameter
     *
     * @param pageRequest {@code BasePageRequest}
     * @param authorizedUserIds 授权可访问的用户id集合
     * @return {@code SqlPara}
     */
    abstract SqlPara setupSqlPara(BasePageRequest<? extends AbstractPageListRequest> pageRequest, List<Integer> authorizedUserIds);

    /**
     * Set common kv for sql para.
     *
     * @param request
     * @return
     */
    Kv setCommonKv(AbstractPageListRequest request, List<Integer> authorizedUserIds) {
        Kv kv = Kv.by("ids", authorizedUserIds);

        // ownerUserLoginIds
        String requestDomainAccount = request.getOwnerUserLoginIds();
        List<Long> accountsUserIds = getUserIdFromDomainAccount(requestDomainAccount);
        if (CollectionUtils.isNotEmpty(accountsUserIds)) {
            kv = Kv.by("accountsUserIds", accountsUserIds);
        }

        // create time
        String createTime = request.getCreateTimeType();
        if (StringUtils.isNotEmpty(createTime)) {
            String startTime = getStartTime(createTime);
            if (StringUtils.isNotEmpty(startTime)) {
                kv.set("startTime", startTime);
            }
        }

        // customer type
        Integer customerType = request.getCustomerType();
        if (customerType != null) {
            kv.set("customerType", customerType);
        }

        // customer grade
        Integer customerGrade = request.getCustomerGrade();
        if (customerGrade != null) {
            kv.set("customerGrade", customerGrade);
        }

        return kv;
    }

    /**
     * 根据时间周期类型获取开始时间
     *
     * @param frontTimeType 前端时间周期类型
     * @return 起始时间
     */
    protected String getStartTime(String frontTimeType) {
        if (CrmCustomerDateEnum.ONE_WEEK_KEY.getId().toString().equals(frontTimeType)) {
            return CrmDateUtil.getLastWeek();
        } else if (CrmCustomerDateEnum.TWO_WEEK_KEY.getId().toString().equals(frontTimeType)) {
            return CrmDateUtil.getLastTwoWeek();
        } else if (CrmCustomerDateEnum.ONE_MONTH_KEY.getId().toString().equals(frontTimeType)) {
            return CrmDateUtil.getLastOneMonth();
        }
        return "";
    }

    /**
     * 根据员工域账号返回员工userId的集合，多个域账号用逗号分隔
     *
     * @param requestDomainAccount 员工域账号，多个域账号用逗号分隔
     * @return 员工userId的list
     */
    protected List<Long> getUserIdFromDomainAccount(String requestDomainAccount) {
        if (StringUtils.isEmpty(requestDomainAccount)) {
            return Collections.emptyList();
        }
        List<Long> accountsUserIds = Lists.newArrayList();
        String[] domainAccounts = requestDomainAccount.split(",");
        if (domainAccounts.length > 0) {
            List<String> reqIdList = Arrays.asList(domainAccounts);
            accountsUserIds = adminUserService.getUserIdsByUserNames(reqIdList);
        }
        return accountsUserIds;
    }
}
