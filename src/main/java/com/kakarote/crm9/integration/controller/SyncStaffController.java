package com.kakarote.crm9.integration.controller;

import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.jfinal.aop.Before;
import com.jfinal.aop.Inject;
import com.jfinal.core.Controller;
import com.jfinal.kit.HttpKit;
import com.jfinal.kit.JsonKit;
import com.jfinal.log.Log;
import com.jfinal.plugin.activerecord.Record;
import com.kakarote.crm9.common.midway.NotifyService;
import com.kakarote.crm9.common.spring.IocInterceptor;
import com.kakarote.crm9.erp.admin.entity.AdminUser;
import com.kakarote.crm9.erp.admin.service.AdminSendEmailService;
import com.kakarote.crm9.erp.admin.service.AdminUserService;
import com.kakarote.crm9.erp.crm.constant.CrmConstant;
import com.kakarote.crm9.erp.crm.service.CrmChangeLogService;
import com.kakarote.crm9.integration.common.EsbConfig;
import com.kakarote.crm9.integration.entity.OaStaff;
import com.kakarote.crm9.integration.entity.OaStaffs;
import com.kakarote.crm9.utils.BaseUtil;
import com.kakarote.crm9.utils.R;
import com.mysql.jdbc.StringUtils;
import org.apache.poi.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Staff sync controller.
 *
 * @author hao.fu
 * @create 2019/6/26 11:57
 */
@Before(IocInterceptor.class)
public class SyncStaffController extends Controller {

    private Log logger = Log.getLog(getClass());

    @Inject
    private AdminUserService adminUserService;

    @Autowired
    private EsbConfig esbConfig;

    @Inject
    private AdminSendEmailService adminSendEmailService;

    @Autowired
    private NotifyService notifyService;

    @Inject
    private CrmChangeLogService crmChangeLogService;

    /**
     * Sync all staffs.
     */
    public void syncAll() {
        long start = System.currentTimeMillis();
        AdminUser user = BaseUtil.getUser();
        logger.info(Objects.isNull(user) ? "" : user.getRealname() + " sync all staffs at: " + LocalDate.now());
        String rsps = HttpKit.get(esbConfig.getSyncStaffUrl(), null, esbConfig.getSyncStaffHeader());
        logger.info(rsps);

        handleResult(rsps, false);

        long end = System.currentTimeMillis();
        logger.info("sync all staffs cost:" + (end - start)/1000 + "s");
        renderJson(R.ok());
    }

    /**
     * Sync staffs partially.
     */
    public void partialSync() {
        logger.info("partial sync staffs: " + LocalDate.now());
        String rsps = HttpKit.get(esbConfig.getSyncStaffUrl(), null, esbConfig.getSyncStaffHeader());
        logger.info(rsps);

        handleResult(rsps, true);
        renderJson(R.ok());
    }

    /**
     * Sync increasement departments.
     */
    public void syncIncreasement() {
        logger.info("sync increasement staffs: " + LocalDate.now());

        Map<String, String> params = Maps.newHashMap();
        params.put("", LocalDate.now().minusDays(CrmConstant.THE_DAY_BEFORE_NOW).toString());

        String rsps = HttpKit.get(esbConfig.getSyncStaffUrl(), params, esbConfig.getSyncStaffHeader());
        logger.info(rsps);

        handleResult(rsps, false);
        renderJson(R.ok());
    }

    private void handleResult(String rsps, boolean ignoreIntersection) {
        try{
            if (StringUtils.isNullOrEmpty(rsps)) {
                logger.error("get empty result when sync all staffs at " + LocalDate.now());
            } else {
                OaStaffs result = JSON.parseObject(rsps, OaStaffs.class);
                if (CrmConstant.SUCCESS_CODE.equals(result.getCode())) {
                    handleSync(result.getData(), ignoreIntersection);
                } else {
                    throw new Exception("error occurs when sync all staffs at " + LocalDate.now() + ", result code " + result.getCode());
                }
            }
        }catch(Exception e){
            logger.error("handle result exception: " + e);
            adminSendEmailService.sendErrorMessage(e,notifyService);
        }

    }

    /**
     * Handle all staff sync.
     *
     * @param staffs
     */
    private void handleSync(List<OaStaff> staffs, boolean ignoreIntersection) {
        List<AdminUser> userList = AdminUser.dao.findAll();

        List<String> idsInDb;
        if (userList != null && userList.size() > 0) {
            // ids in db
            idsInDb = userList.stream().map(item -> item.getUserId().toString()).collect(Collectors.toList());
            logger.info("staff sync on " + LocalDate.now() + ", user ids in CRM DB, size: " + idsInDb.size() + "\nvalues:" + String.join(",", idsInDb));

            // ids from oa
            List<String> idsFromOa = staffs.stream().map(item -> item.getId().trim()).collect(Collectors.toList());
            logger.info("staff sync on " + LocalDate.now() + ", user ids from OA, size: " + idsFromOa.size() + "\nvalues:" + String.join(",", idsFromOa));

            // update inter section record
            if (!ignoreIntersection) {
                List<String> intersection = idsInDb.stream().filter(idsFromOa::contains).collect(Collectors.toList());

                //获取当前userID与deptID的Map
                Map<Long, Integer> userDeptMap = userList.stream().collect(Collectors.toMap(AdminUser::getUserId, AdminUser::getDeptId));
                updateUserIntersectionRecord(intersection, staffs,userDeptMap);
            }

            // update delete flag for records only exist in DB
            List<String> idsOnlyExistInDb = idsInDb.stream().filter(item -> !idsFromOa.contains(item)).collect(Collectors.toList());
            updateUserStatus(idsOnlyExistInDb);

            // insert record which only exist in OA
            List<String> idsOnlyExistInOa = idsFromOa.stream().filter(item -> !idsInDb.contains(item)).collect(Collectors.toList());
            insertNewUserRecord(idsOnlyExistInOa, staffs);
        }
    }

    private void updateUserIntersectionRecord(List<String> ids, List<OaStaff> staffs,Map<Long, Integer> userDeptMap) {
        if (ids == null || ids.size() == 0) {
            return;
        }

        List<AdminUser> userList = getUserModelForSpecifiedIds(ids, staffs);

        boolean result = adminUserService.batchUpdateStaff(userList);
        logger.info("staff sync on " + LocalDate.now() + ", updateUserIntersectionRecord status: " + (result ? "success" : "fail"));

        //更新成功后,判断如果部门变化则插入BD变更日志
        if (result) {
            userList.forEach(o -> {
                Integer ordDeptId = userDeptMap.get(o.getUserId());
                if (!Objects.equals(o.getDeptId(), ordDeptId)) {
                    crmChangeLogService.saveBdDeptChangeLog(o.getUserId(),Long.valueOf(o.getDeptId()),null);
                }
            });
        }
    }

    private void updateUserStatus(List<String> ids) {
        if (ids == null || ids.size() == 0) {
            return;
        }
        List<Long> userIds = ids.stream().map(item -> Long.valueOf(item.trim())).collect(Collectors.toList());
        boolean result = adminUserService.batchUpdateUserStatus(userIds, CrmConstant.ADMIN_USER_STATUS_FORBIDDEN);

        logger.info("staff sync on " + LocalDate.now() + "set user status to forbidden: " + StringUtil.join(userIds.toString(), ","));
        logger.info("staff sync on " + LocalDate.now() + ", updateUserStatus status: " + (result ? "success" : "fail"));
    }

    private void insertNewUserRecord(List<String> newIds, List<OaStaff> staffs) {
        if (newIds == null || newIds.size() == 0) {
            return;
        }

        List<AdminUser> userList = getUserModelForSpecifiedIds(newIds, staffs);
        boolean result = adminUserService.batchInsertNewUser(userList);
        logger.info("staff sync on " + LocalDate.now() + ", insertNewUserRecord status: " + (result ? "success" : "fail"));

        //插入成功之后，插入BD部门变化表信息
        if (result){
            userList.forEach(o -> crmChangeLogService.saveBdDeptChangeLog(o.getUserId(),Long.valueOf(o.getDeptId()),null));
        }
    }

    private List<AdminUser> getUserModelForSpecifiedIds(List<String> ids, List<OaStaff> staffs) {
        List<OaStaff> oaStaffs = staffs.stream().filter(item -> ids.contains(item.getId())).collect(Collectors.toList());
        List<AdminUser> results = Lists.newArrayList();
        oaStaffs.forEach(item -> {

            AdminUser user = new AdminUser();
            user.setUserId(item.getId() != null ? Long.valueOf(item.getId()) : null);
            user.setDeptId(item.getDeptId() != null ? Integer.valueOf(item.getDeptId()) : null);
            user.setEmail(item.getEmail());
            user.setUsername(item.getEmail() != null ? item.getEmail().substring(0, item.getEmail().indexOf('@')) : "");

            Date date;
            try {
                date = new SimpleDateFormat("yyyy-MM-dd").parse(item.getHireDate());
            } catch (ParseException e) {
                date = DateUtil.parseDate("2000-01-01 00:00:00");
            }
            user.setHireTime(date);
            user.setLeaderNum(item.getLeaderNum());
            user.setParentId(item.getParentId() != null ? Long.valueOf(item.getParentId().trim()) : null);
            user.setNum(item.getNum());
            user.setMobile(item.getMobile() == null ? "" : item.getMobile());
            user.setPost(item.getPost());
            user.setSex(item.getSex() == null ? 0 : Integer.parseInt(item.getSex().trim()));
            user.setStatus(item.getStatus() == null ? 0 : Integer.parseInt(item.getStatus().trim()));
            user.setRealname(item.getUserName());
            user.setPassword("123456");
            user.setUpdateTime(new Date());
            logger.info(JsonKit.toJson(user));
            results.add(user);
        });
        return results;
    }
}
