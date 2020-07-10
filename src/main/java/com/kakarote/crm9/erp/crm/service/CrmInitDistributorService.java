package com.kakarote.crm9.erp.crm.service;

import com.jfinal.aop.Inject;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.kakarote.crm9.erp.crm.common.CrmEnum;
import com.kakarote.crm9.erp.crm.entity.CrmCustomer;
import org.apache.commons.collections.CollectionUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * CrmInitDistributorService.
 *
 * @author yue.li
 * @create 2020/04/20 10:00
 */
public class CrmInitDistributorService {

    @Inject
    private CrmRecordService crmRecordService;

    /**
     * 初始化分销商平台数据
     * @author yue.li
     */
    public void initDistributor() {
        initIsCertified();
        initNotCertified();
    }

    /**
     * 处理已认证的UID集合
     * @author yue.li
     */
    private void initIsCertified() {
        // 查询已认证的无负责人无部门集合
        List<Record> recordList = Db.find(Db.getSql("crm.customer.isCertifiedNoDeal"));
        dealNoOwnerUserIdAndNoDept(recordList);
    }

    /**
     * 处理未认证的UID集合
     * @author yue.li
     */
    private void initNotCertified() {
        // 查询已认证的无负责人无部门集合
        List<Record> recordList = Db.find(Db.getSql("crm.customer.isNotCertifiedNotDeal"));
        dealNoOwnerUserIdAndNoDept(recordList);
    }

    /**
     * 处理无负责人且无部门
     * @author yue.li
     */
    private void dealNoOwnerUserIdAndNoDept(List<Record> recordList) {
        // 客户对应多个siteMemberId的客户集合
        List<Long> customerList = getMultipleRelationCustomerList();

        for(Record record : recordList) {
            Long customerId = record.get("customerId");
            String realName = record.getStr("realName");
            Date gmtCreate = record.getDate("gmtCreate");
            // 不包含多个官网用户id
            if(CollectionUtils.isNotEmpty(customerList) && Objects.nonNull(customerId) && !customerList.contains(customerId)){
                // 记录更新日志
                updateDistributorLog(null,customerId.intValue(),realName,gmtCreate);
            }
        }
    }

    /**
     * 查询客户对应多个siteMemberId的客户集合
     * @author yue.li
     */
    private List<Long> getMultipleRelationCustomerList() {
        List<Long> customerIdList = new ArrayList<>();
        List<CrmCustomer> crmCustomerList = CrmCustomer.dao.find(Db.getSql("crm.customer.getMultipleRelationCustomerList"));
        crmCustomerList.forEach(item -> customerIdList.add(item.getCustomerId()));
        return customerIdList;
    }

    /**
     * 处理分销商推广日志
     * @author yue.li
     * @param userId  用户id
     * @param actionId 业务id
     * @param distributorName 上游分销商名称
     */
    private void addDistributorLog(Long userId,Integer actionId,String distributorName) {
        String content = "系统新建了客户,与上游分销商[+"+distributorName+"+]建立推广关系";
        saveDistributorLog(userId,actionId,content,null);
    }

    /**
     * 修改分销商推广日志
     * @author yue.li
     * @param userId  用户id
     * @param actionId 业务id
     * @param distributorName 上游分销商名称
     * @param date 上游分销商绑定时间
     */
    private void updateDistributorLog(Long userId,Integer actionId,String distributorName,Date date) {
        String content = "系统修改了客户,与上游分销商["+distributorName+"]建立推广关系";
        saveDistributorLog(userId,actionId,content,date);
    }

    /**
     * 保存分销商推广关系日志
     * @author yue.li
     * @param userId 用户id
     * @param actionId 业务id
     * @param content 内容
     * @param date 日期
     */
    private void saveDistributorLog(Long userId, Integer actionId, String content, Date date) {
        crmRecordService.dealRecordLog(userId,CrmEnum.CUSTOMER_TYPE_KEY.getTypes(),actionId,content,date);
    }
}
