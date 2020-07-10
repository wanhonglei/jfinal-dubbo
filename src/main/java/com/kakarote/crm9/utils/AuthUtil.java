package com.kakarote.crm9.utils;

import com.jfinal.aop.Aop;
import com.jfinal.kit.Kv;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.kakarote.crm9.erp.admin.entity.AdminConfig;
import com.kakarote.crm9.erp.admin.service.AdminConfigService;
import com.kakarote.crm9.erp.admin.service.AdminUserService;
import com.kakarote.crm9.erp.crm.common.CrmEnum;
import com.kakarote.crm9.erp.crm.constant.CrmConstant;
import com.kakarote.crm9.erp.crm.service.CrmGroupMemberService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author wyq
 */
@Slf4j
public class AuthUtil {

    private static AdminConfigService adminConfigService = Aop.get(AdminConfigService.class);
    private static AdminUserService adminUserService = Aop.get(AdminUserService.class);
    private static CrmGroupMemberService crmGroupMemberService = Aop.get(CrmGroupMemberService.class);

    public static Map<String,String> getCrmTablePara(String urlPrefix){
        Map<String,String> tableParaMap = new HashMap<>(2);
        switch(urlPrefix){
            case "CrmCustomer":
                tableParaMap.put("tableName", "72crm_crm_customer");
                tableParaMap.put("tableId", "customer_id");
                break;
            case "CrmLeads":
                tableParaMap.put("tableName", "72crm_crm_leads");
                tableParaMap.put("tableId", "leads_id");
                break;
            case "CrmContract":
                tableParaMap.put("tableName", "72crm_crm_contract");
                tableParaMap.put("tableId", "contract_id");
                break;
            case "CrmContacts":
                tableParaMap.put("tableName", "72crm_crm_contacts");
                tableParaMap.put("tableId", "contacts_id");
                break;
            case "CrmBusiness":
                tableParaMap.put("tableName", "72crm_crm_business");
                tableParaMap.put("tableId", "business_id");
                break;
            case "CrmReceivables":
                tableParaMap.put("tableName", "72crm_crm_receivables");
                tableParaMap.put("tableId", "receivables_id");
                break;
            default:
                return null;
        }
        return tableParaMap;
    }

    public static boolean isCrmAuth(Map<String,String> tablePara, Integer id){
        if(tablePara == null){
            return false;
        }
        Long userId = BaseUtil.getUserId();
        List<Long> longs = Aop.get(AdminUserService.class).queryUserByAuth(userId);

        if("72crm_crm_contacts".equals(tablePara.get("tableName"))){
            Record list = Db.findFirst(Db.getSqlPara("crm.contact.queryNumByOwnerUserId",
                    Kv.by("contacts_id", id).set("owner_user_id",longs)));
            if(list.getStr("customer_ownerId") == null || "".equals(list.getStr("customer_ownerId"))){
                return false;
            }
            if(list.getInt("countNum") ==0){
                return true;
            }
        }else if("72crm_crm_customer".equals(tablePara.get("tableName"))){
            //1、取客户信息
            Record record = Db.findFirst(Db.getSql("crm.customer.queryByCustomerId"), id);
            if (Objects.isNull(record)) {
                log.info("AuthUtil.isCrmAuth->客户[{}]不存在", id);
                //客户为空，无权访问
                return true;
            }
            //2、查询所属权限
            //优化20200325
            Long ownerUserId = record.getLong("owner_user_id");
            boolean hasRight = true;
            //longs为当前用户有权限查询的用户ID列表
            if (CollectionUtils.isNotEmpty(longs)) {
                //如果longs不为空(其实肯定不会为空)，判断是否包含用户的ownerUserId
                hasRight = longs.contains(ownerUserId);
            }
            //如果owner是当前客户，条件短路，否则判断团队成员中是否有当前用户
            if (!hasRight && Objects.nonNull(userId)) {
                hasRight = crmGroupMemberService.isMember(id.longValue(), userId, Integer.valueOf(CrmEnum.CUSTOMER_TYPE_KEY.getTypes()));
            }
            if(!hasRight) {
                //未查到所属权，判断是否为网站客户池或部门池信息
                Integer deptId = record.getInt("dept_id");
                String createUserId = record.getStr("create_user_id");
                if (deptId == null && ownerUserId == null) {
                    //3、网站客户池：deptId is null & owner_user_id is null，有权限
                    return false;
                } else if (deptId != null && ownerUserId == null) {
                    //4、部门池：deptId is not null & owner_user_id is null，有权限
                    return false;
                } else if (createUserId != null && createUserId.equals(String.valueOf(userId))) {
                    //创建人为登录用户
                    return false;
                } else {
                    //判断是否电销团队成员
                    AdminConfig phoneSaleDeptIdConfig = adminConfigService.queryAdminConfig(CrmConstant.PHONE_SALE_BUSINESS_DEPT_ID);
                    if (phoneSaleDeptIdConfig == null) {
                        log.error("Auth Warning:phoneSale business departmentId config is missing!");
                        //找不到配置不可以访问
                        return true;
                    }
                    //获取客户Owner的部门ID
                    Long ownerDeptId = adminUserService.queryDeptIdOfUser(ownerUserId);
                    if (ownerDeptId == null) {
                        log.error("Auth Warning:the owner of customer[customerId:{},ownerUserId:{}] has no department", id, ownerUserId);
                        return true;
                    }
                    return !Objects.equals(ownerDeptId, Long.valueOf(phoneSaleDeptIdConfig.getValue()));
                }
            }
        }else if("72crm_crm_leads".equals(tablePara.get("tableName"))){
            //1、取线索信息
            Record record =  Db.findFirst(Db.getSql("crm.leads.queryById"), id);
            //2、查询所属权限
            Record list = Db.findFirst(Db.getSqlPara("crm.leads.queryNumByOwnerUserId",
                    Kv.by("leads_id", id).set("owner_user_id",longs)));
            if(list.getInt("countNum") ==0){
                //未查到所属权，判断是否为网站客户池或部门池信息
                if(record.getStr("dept_id") == null && record.getInt("owner_user_id") == null){
                    //3、线索公海：deptId is null & owner_user_id is null，有权限
                    return false;
                }else if(record.getStr("dept_id") != null && record.getInt("owner_user_id") == null){
                    //4、事业部线索：deptId is not null & owner_user_id is null，有权限
                    return false;
                }else if(record.getStr("create_user_id") != null && record.getStr("create_user_id").equals(String.valueOf(userId))){
                    //创建人为登录用户
                    return false;
                }else{
                    //全部不满足，无权限
                    return true;
                }
            }
        }else if("72crm_crm_business".equals(tablePara.get("tableName"))){
            Record list = Db.findFirst(Db.getSqlPara("crm.business.queryNumByOwnerUserId",
                    Kv.by("business_id", id).set("owner_user_id",longs).set("ro_user_id",userId)));
            if(list.getInt("countNum") ==0){
                return true;
            }
        }

        return false;
    }

    public static boolean checkPageListPower(Integer sceneId ,Long userId){
        if(sceneId != null){
            Record record =  Db.findFirst(Db.getSql("admin.scene.queryBySceneId"), sceneId);
            //sceneId对应的ower是否等于token对应的员工
            if(record != null && userId.compareTo(record.getLong("user_id")) == 0){
                return false;
            }
            return true;
        }else{
            return false;
        }
    }
}
