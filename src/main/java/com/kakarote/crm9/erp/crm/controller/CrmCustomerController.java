package com.kakarote.crm9.erp.crm.controller;

import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.rocketmq.shade.io.netty.util.internal.StringUtil;
import com.google.common.collect.Lists;
import com.jfinal.aop.Before;
import com.jfinal.aop.Inject;
import com.jfinal.core.Controller;
import com.jfinal.core.paragetter.Para;
import com.jfinal.log.Log;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.upload.UploadFile;
import com.kakarote.crm9.common.annotation.CrmEventAnnotation;
import com.kakarote.crm9.common.annotation.HttpEnum;
import com.kakarote.crm9.common.annotation.LogApiOperation;
import com.kakarote.crm9.common.annotation.NotNullValidate;
import com.kakarote.crm9.common.annotation.Permissions;
import com.kakarote.crm9.common.config.JfinalConfig;
import com.kakarote.crm9.common.config.paragetter.BasePageRequest;
import com.kakarote.crm9.common.exception.CrmException;
import com.kakarote.crm9.common.midway.NotifyService;
import com.kakarote.crm9.common.spring.IocInterceptor;
import com.kakarote.crm9.erp.admin.entity.AdminLookUpLog;
import com.kakarote.crm9.erp.admin.entity.AdminRecord;
import com.kakarote.crm9.erp.admin.entity.AdminUser;
import com.kakarote.crm9.erp.admin.service.AdminDataDicService;
import com.kakarote.crm9.erp.admin.service.AdminFieldService;
import com.kakarote.crm9.erp.admin.service.AdminLookUpLogService;
import com.kakarote.crm9.erp.admin.service.AdminSceneService;
import com.kakarote.crm9.erp.admin.service.AdminUserService;
import com.kakarote.crm9.erp.crm.acl.user.CrmUser;
import com.kakarote.crm9.erp.crm.common.CrmEnum;
import com.kakarote.crm9.erp.crm.common.CrmEventEnum;
import com.kakarote.crm9.erp.crm.common.CrmReduceReasonsEnum;
import com.kakarote.crm9.erp.crm.common.CustomerStorageTypeEnum;
import com.kakarote.crm9.erp.crm.common.customer.FromSourceEnum;
import com.kakarote.crm9.erp.crm.constant.CrmConstant;
import com.kakarote.crm9.erp.crm.dto.CrmCustomerQueryParamDto;
import com.kakarote.crm9.erp.crm.dto.CrmGroupMemberDto;
import com.kakarote.crm9.erp.crm.entity.CrmCustomer;
import com.kakarote.crm9.erp.crm.entity.CrmCustomerPaymentChannel;
import com.kakarote.crm9.erp.crm.entity.CrmCustomerSalesLog;
import com.kakarote.crm9.erp.crm.entity.DistributorStatistic;
import com.kakarote.crm9.erp.crm.service.CrmBusinessService;
import com.kakarote.crm9.erp.crm.service.CrmChangeLogService;
import com.kakarote.crm9.erp.crm.service.CrmContactsService;
import com.kakarote.crm9.erp.crm.service.CrmCustomerExtService;
import com.kakarote.crm9.erp.crm.service.CrmCustomerSceneService;
import com.kakarote.crm9.erp.crm.service.CrmCustomerService;
import com.kakarote.crm9.erp.crm.service.CrmGroupMemberService;
import com.kakarote.crm9.erp.crm.service.CrmNotesService;
import com.kakarote.crm9.erp.crm.service.CrmRecordService;
import com.kakarote.crm9.erp.crm.service.CrmSiteMemberService;
import com.kakarote.crm9.erp.crm.vo.CrmCustomerGuideQueryVO;
import com.kakarote.crm9.erp.crm.vo.CrmGroupMemberVO;
import com.kakarote.crm9.integration.common.EsbConfig;
import com.kakarote.crm9.utils.BaseUtil;
import com.kakarote.crm9.utils.OssPrivateFileUtil;
import com.kakarote.crm9.utils.R;
import com.kakarote.crm9.utils.common.StreamUtil;
import com.mysql.jdbc.StringUtils;
import org.apache.poi.hssf.usermodel.DVConstraint;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFDataValidation;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.velocity.app.VelocityEngine;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Before(IocInterceptor.class)
public class CrmCustomerController extends Controller {

    // 推广分销商事业部的部门id，以“,”分割
    public String promotionDistributorDeptIds = JfinalConfig.crmProp.get("customer.distributor.promotion.DeptIds");

    @Inject
    private CrmCustomerService crmCustomerService;

    @Inject
    private CrmContactsService crmContactsService;//联系人

    @Inject
    private CrmBusinessService crmBusinessService;//商机x

    @Inject
    private AdminDataDicService adminDataDicService;//数据字典

    @Inject
    private AdminLookUpLogService adminLookUpLogService; //操作记录

    @Inject
    private CrmSiteMemberService crmSiteMemberService;

    @Autowired
    private OssPrivateFileUtil ossPrivateFileUtil;

    @Inject
    private CrmNotesService crmNotesService;

    @Inject
    private CrmRecordService crmRecordService;

    @Inject
    private AdminSceneService adminSceneService;

    @Autowired
    private NotifyService notifyService;

    @Autowired
    private VelocityEngine velocityEngine;

    @Autowired
    private EsbConfig esbConfig;

    @Inject
    private AdminUserService adminUserService;

    @Inject
    private CrmChangeLogService crmChangeLogService;

    @Inject
    private CrmCustomerExtService crmCustomerExtService;

    @Inject
    private CrmCustomerSceneService crmCustomerSceneService;

    @Inject
    private CrmGroupMemberService crmGroupMemberService;

    private Log logger = Log.getLog(getClass());

    /**
     * @author wyq
     * 分页条件查询客户
     */
    public void queryList(BasePageRequest<CrmCustomer> basePageRequest) {
        try {
            renderJson(R.ok().put("data", crmCustomerService.getCustomerPageList(basePageRequest)));
        } catch (Exception e) {
            logger.error(String.format("queryList customer msg:%s", BaseUtil.getExceptionStack(e)));
            renderJson(R.error(e.getMessage()));
        }
    }

    /**
     * @author wyq
     * 新增或更新客户
     */
    @Permissions({"crm:customer:save", "crm:customer:update"})
    @LogApiOperation(methodName = "客户编辑保存")
    public void addOrUpdate() {
        try {
            JSONObject jsonObject = JSON.parseObject(getRawData());
            CrmCustomer crmCustomer = jsonObject.getObject("entity", CrmCustomer.class);
            Long userId = BaseUtil.getUserId();

            //判断备注remark字段长度
            if (org.apache.commons.lang3.StringUtils.isNotBlank(crmCustomer.getRemark()) && crmCustomer.getRemark().length() > 500) {
                renderJson(R.error("备注信息 不能超过500"));
                return;
            }

            //如果传了customerId，查询历史客户信息
            CrmCustomer oldCrmCustomer = null;
            if (Objects.nonNull(crmCustomer.getCustomerId())) {
                oldCrmCustomer = CrmCustomer.dao.findById(crmCustomer.getCustomerId());

                if (oldCrmCustomer == null) {
                    renderJson(R.error("参数： crmCustomer.getCustomerId() 查不到对应的客户"));
                    return;
                }

                //客户编辑时不能编辑负责人
                Integer newOwnerUserId = crmCustomer.getOwnerUserId();
                Integer oldOwnerUserId = oldCrmCustomer.getOwnerUserId();
                if (!Objects.equals(newOwnerUserId, oldOwnerUserId)) {
                    renderJson(R.error("编辑时，不能直接修改负责人"));
                    return;
                }
            }


            //  判断用户名是否重复（前置）
            if (org.apache.commons.lang3.StringUtils.isBlank(crmCustomer.getCustomerName())) {
                renderJson(R.error("参数：customerName 必传"));
                return;
            }
            R r = crmCustomerService.checkCompanyCustomerName(crmCustomer.getCustomerName().trim(), crmCustomer.getCustomerId() == null ? null : Math.toIntExact(crmCustomer.getCustomerId()));
            if (!r.isSuccess()) {
                Record record = (Record) r.get("data");
                if (Objects.nonNull(crmCustomer.getCustomerId())) {
                    if (!crmCustomer.getCustomerId().equals(record.getLong("customer_id"))) {
                        renderJson(R.error(crmCustomerService.customerExistInfo(record)));
                        return;
                    }
                } else {
                    if (Objects.nonNull(record)) {
                        renderJson(R.error(crmCustomerService.customerExistInfo(record)));
                        return;
                    }
                }
            }

            //判断有负责人的情况，storageType类型参数 是否存在
            if (crmCustomer.getOwnerUserId() != null) {
                try {
                    Integer storageType = crmCustomer.getStorageType();
                    if (storageType == null) {
                        renderJson(R.error("客户库容类型必传"));
                        return;
                    } else if (storageType != CustomerStorageTypeEnum.INSPECT_CAP.getCode() && storageType != CustomerStorageTypeEnum.RELATE_CAP.getCode()) {
                        renderJson(R.error("客户库容类型 值非法"));
                        return;
                    }

                    //判断新增客户只能是关联库类型
                    if (crmCustomer.getCustomerId() == null && storageType == CustomerStorageTypeEnum.INSPECT_CAP.getCode()) {
                        renderJson(R.error("新增客户时，客户库容类型 只能选择关联库"));
                        return;
                    }
                } catch (NumberFormatException e) {
                    renderJson(R.error("参数：storageType 类型错误"));
                    return;
                }
            }

            //设置客户来源
            if (crmCustomer.getCustomerId() == null) {
                jsonObject.getJSONObject("entity").put("fromSource", FromSourceEnum.BY_MANUAL.getCode());
            }

            r = crmCustomerService.addOrUpdate(jsonObject, userId);
            if (r.isSuccess()) {
                if (crmCustomer.getCustomerId() != null) {
                    crmRecordService.updateRecord(oldCrmCustomer, crmCustomer, CrmEnum.CUSTOMER_TYPE_KEY.getTypes(), userId);
                } else {
                    JSONObject resultJsonObject = (JSONObject) JSON.toJSON(r.get("data"));
                    CrmCustomer newCrmCustomer = new CrmCustomer().dao().findById(resultJsonObject.get("customer_id"));
                    crmRecordService.addRecord(newCrmCustomer.getCustomerId().intValue(), CrmEnum.CUSTOMER_TYPE_KEY.getTypes(), userId);
                }
            }
            renderJson(r);
        } catch (Exception e) {
            logger.error(String.format("addOrUpdate customer msg:%s", BaseUtil.getExceptionStack(e)));
            renderJson(R.error(e.getMessage()));
        }


    }

    /**
     * @author wyq
     * 根据客户id查询
     */
    @Permissions("crm:customer:read")
    @NotNullValidate(value = "customerId", message = "客户id不能为空")
    public void queryById(@Para("customerId") Integer customerId) {
        try {
            saveAdminLookUpLog(customerId);
            renderJson(R.ok().put("data", crmCustomerService.queryById(customerId, ossPrivateFileUtil)));
        } catch (Exception e) {
            logger.error(String.format("queryById customer msg:%s", BaseUtil.getExceptionStack(e)));
            renderJson(R.error(e.getMessage()));
        }
    }

    /**
     * 根据客户id查询
     *
     * @author yue.li
     */
    @Permissions("crm:customer:read")
    @NotNullValidate(value = "customerId", message = "客户id不能为空")
    public void queryWebsiteById(@Para("customerId") Integer customerId) {
        try {
            saveAdminLookUpLog(customerId);
            renderJson(R.ok().put("data", crmCustomerService.queryById(customerId, ossPrivateFileUtil)));
        } catch (Exception e) {
            logger.error(String.format("queryWebsiteById customer msg:%s", BaseUtil.getExceptionStack(e)));
            renderJson(R.error(e.getMessage()));
        }
    }

    /***
     * 保存操作记录
     * @author yue.li
     * @param customerId 客户ID
     */
    public void saveAdminLookUpLog(Integer customerId) {
        try {
            AdminLookUpLog adminLookUpLog = new AdminLookUpLog();
            adminLookUpLog.setBillsId(String.valueOf(customerId));
            adminLookUpLog.setLookUpName(CrmEnum.CUSTOMER_BASE_INFO.getTypes());
            adminLookUpLogService.addLookUpLog(adminLookUpLog, BaseUtil.getUserId());
        } catch (Exception e) {
            logger.error(String.format("saveAdminLookUpLog customer msg:%s", BaseUtil.getExceptionStack(e)));
            renderJson(R.error(e.getMessage()));
        }
    }

    /**
     * @author wyq
     * 根据客户名称查询
     */
    @NotNullValidate(value = "name", message = "客户名称不能为空")
    public void queryByName(@Para("name") String name) {
        try {
            renderJson(R.ok().put("data", crmCustomerService.queryByName(name)));
        } catch (Exception e) {
            logger.error(String.format("queryByName customer msg:%s", BaseUtil.getExceptionStack(e)));
            renderJson(R.error(e.getMessage()));

        }
    }

    /**
     * @author wyq
     * 根据客户id查询联系人
     */
    public void queryContacts(BasePageRequest<CrmCustomer> basePageRequest) {
        try {
            renderJson(crmCustomerService.queryContacts(basePageRequest, false));
        } catch (Exception e) {
            logger.error(String.format("queryContacts customer msg:%s", BaseUtil.getExceptionStack(e)));
            renderJson(R.error(e.getMessage()));

        }
    }

    /**
     * 根据客户id查询联系人(包含脱敏电话号码）
     *
     * @param basePageRequest rq
     */
    public void queryContactsWithSensitive(BasePageRequest<CrmCustomer> basePageRequest) {
        try {
            renderJson(crmCustomerService.queryContacts(basePageRequest, true));
        } catch (Exception e) {
            logger.error(String.format("queryContacts customer msg:%s", BaseUtil.getExceptionStack(e)));
            renderJson(R.error(e.getMessage()));

        }
    }

    /**
     * @author wyq
     * 根据id删除客户
     */
    @Permissions("crm:customer:delete")
    @NotNullValidate(value = "customerIds", message = "客户id不能为空")
    public void deleteByIds(@Para("customerIds") String customerIds) {
        try {
            renderJson(crmCustomerService.deleteByIds(customerIds));
            //crmCustomerService.redisReset();
        } catch (Exception e) {
            logger.error(String.format("deleteByIds customer msg:%s", BaseUtil.getExceptionStack(e)));
            renderJson(R.error(e.getMessage()));

        }
    }

    /**
     * @author wyq
     * 根据客户id查找商机
     */
    public void queryBusiness(BasePageRequest<CrmCustomer> basePageRequest) {
        try {
            renderJson(crmCustomerService.queryBusiness(basePageRequest));
        } catch (Exception e) {
            logger.error(String.format("queryBusiness customer msg:%s", BaseUtil.getExceptionStack(e)));
            renderJson(R.error(e.getMessage()));

        }
    }

    /**
     * @author wyq
     * 根据客户id查询合同
     */
    public void queryContract(BasePageRequest<CrmCustomer> basePageRequest) {
        try {
            renderJson(crmCustomerService.queryContract(basePageRequest));
        } catch (Exception e) {
            logger.error(String.format("queryContract customer msg:%s", BaseUtil.getExceptionStack(e)));
            renderJson(R.error(e.getMessage()));

        }
    }

    /**
     * @author zxy
     * 条件查询客户公海
     */
    public void queryPageGH(BasePageRequest basePageRequest) {
        try {
            renderJson(R.ok().put("data", crmCustomerService.queryPageGH(basePageRequest)));
        } catch (Exception e) {
            logger.error(String.format("queryPageGH customer msg:%s", BaseUtil.getExceptionStack(e)));
            renderJson(R.error(e.getMessage()));

        }
    }

    /**
     * @author wyq
     * 根据客户id查询回款计划
     */
    public void queryReceivablesPlan(BasePageRequest<CrmCustomer> basePageRequest) {
        try {
            renderJson(crmCustomerService.queryReceivablesPlan(basePageRequest));
        } catch (Exception e) {
            logger.error(String.format("queryReceivablesPlan customer msg:%s", BaseUtil.getExceptionStack(e)));
            renderJson(R.error(e.getMessage()));

        }
    }

    /**
     * @author zxy
     * 根据客户id查询回款
     */
    public void queryReceivables(BasePageRequest<CrmCustomer> basePageRequest) {
        try {
            renderJson(crmCustomerService.queryReceivables(basePageRequest));
        } catch (Exception e) {
            logger.error(String.format("queryReceivables customer msg:%s", BaseUtil.getExceptionStack(e)));
            renderJson(R.error(e.getMessage()));

        }
    }

    /**
     * @author wyq
     * 客户锁定
     */
    @Permissions("crm:customer:lock")
    @NotNullValidate(value = "ids", message = "客户id不能为空")
    @NotNullValidate(value = "isLock", message = "锁定状态不能为空")
    public void lock(@Para("") CrmCustomer crmCustomer) {
        try {
            renderJson(crmCustomerService.lock(crmCustomer));
        } catch (Exception e) {
            logger.error(String.format("lock customer msg:%s", BaseUtil.getExceptionStack(e)));
            renderJson(R.error(e.getMessage()));

        }
    }

    /**
     * 客户转移
     *
     * @author wyq
     */
    @Permissions("crm:customer:transfer")
    @NotNullValidate(value = "customerIds", message = "客户id不能为空")
    @NotNullValidate(value = "newOwnerUserId", message = "新负责人不能为空")
    @NotNullValidate(value = "transferType", message = "移除方式不能为空")
    @LogApiOperation(methodName = "客户分派")
    public void transfer(@Para("") CrmCustomer crmCustomer) {
        renderJson(crmCustomerService.transfer(crmCustomer));
    }

    /**
     * 网站客户转移
     *
     * @author wyq
     */
    @Permissions("crm:customerpublic:transfer")
    @NotNullValidate(value = "customerIds", message = "客户id不能为空")
    @NotNullValidate(value = "newOwnerUserId", message = "新负责人不能为空")
    @NotNullValidate(value = "transferType", message = "移除方式不能为空")
    @LogApiOperation(methodName = "transferWebiste")
    public void transferWebiste(@Para("") CrmCustomer crmCustomer) {
        try {
            renderJson(crmCustomerService.transfer(crmCustomer));
        } catch (Exception e) {
            logger.error(String.format("transferWebiste customer msg:%s", BaseUtil.getExceptionStack(e)));
            renderJson(R.error(e.getMessage()));

        }
    }

    /**
     * @author wyq
     * 查询团队成员
     */
    @NotNullValidate(value = "customerId", message = "客户id不能为空")
    public void getMembers(@Para("customerId") Long customerId) {
        try {
            renderJson(R.ok().put("data", crmCustomerService.getMembers(customerId, Integer.valueOf(CrmEnum.CUSTOMER_TYPE_KEY.getTypes()))));
        } catch (CrmException e) {
            renderJson(R.error(e.getMessage()));
        }
    }

    /**
     * @author wxw
     * 查询团队成员
     */
    @NotNullValidate(value = "customerIds", message = "客户id不能为空")
    public void getMembersBycustomerIds(@Para("customerIds") String customerIds) {
        try {
            String[] customerIdArray = customerIds.split(",");
            List<CrmGroupMemberVO> members = Lists.newArrayList();
            Arrays.stream(customerIdArray).forEach(customerIdstr -> {
                if (Objects.nonNull(customerIdstr)) {
                    members.addAll(crmCustomerService.getMembers(Long.valueOf(customerIdstr), Integer.valueOf(CrmEnum.CUSTOMER_TYPE_KEY.getTypes())));
                }
            });
            // 根据id去重并返回
            renderJson(R.ok().put("data", members.stream().filter(StreamUtil.distinctByKey(CrmGroupMemberVO::getId)).collect(Collectors.toList())));
        } catch (Exception e) {
            logger.error(String.format("getMembers customer msg:%s", BaseUtil.getExceptionStack(e)));
            renderJson(R.error(e.getMessage()));
        }
    }

    /**
     * @author wyq
     * 添加团队成员
     */
    @Permissions("crm:customer:teamsave")
    @NotNullValidate(value = "ids", message = "客户id不能为空")
    @NotNullValidate(value = "memberIds", message = "成员id不能为空")
    @NotNullValidate(value = "power", message = "读写权限不能为空")
    public void addMembers(@Para("") CrmCustomer crmCustomer) {
        try {
            crmGroupMemberService.addMembers(
                    CrmGroupMemberDto.builder()
                            .objIds(Lists.newArrayList(Arrays.stream(crmCustomer.getIds().split(",")).map(Long::valueOf).collect(Collectors.toList())))
                            .objType(Integer.valueOf(CrmEnum.CUSTOMER_TYPE_KEY.getTypes()))
                            .memberIds(Lists.newArrayList(Arrays.stream(crmCustomer.getMemberIds().split(",")).map(Long::valueOf).collect(Collectors.toList())))
                            .changeTypes(crmCustomer.getChangeType())
                            .power(crmCustomer.getPower())
                            .build()
            );
            renderJson(R.ok());
        } catch (CrmException e) {
            renderJson(R.error(e.getMessage()));
        }
    }

    /**
     * @author wyq
     * 编辑团队成员
     */
    @Permissions("crm:customer:teamsave")
    @NotNullValidate(value = "ids", message = "商机id不能为空")
    @NotNullValidate(value = "memberIds", message = "成员id不能为空")
    @NotNullValidate(value = "power", message = "读写权限不能为空")
    public void updateMembers(@Para("") CrmCustomer crmCustomer) {
        addMembers(crmCustomer);
    }

    /**
     * @author wyq
     * 删除团队成员
     */
    @Permissions("crm:customer:teamsave")
    @NotNullValidate(value = "ids", message = "客户id不能为空")
    @NotNullValidate(value = "memberIds", message = "成员id不能为空")
    public void deleteMembers(@Para("") CrmCustomer crmCustomer) {
        try {
            crmGroupMemberService.deleteMembers(
                    CrmGroupMemberDto.builder()
                            .objIds(Lists.newArrayList(Arrays.stream(crmCustomer.getIds().split(",")).map(Long::valueOf).collect(Collectors.toList())))
                            .objType(Integer.valueOf(CrmEnum.CUSTOMER_TYPE_KEY.getTypes()))
                            .memberIds(Lists.newArrayList(Arrays.stream(crmCustomer.getMemberIds().split(",")).map(Long::valueOf).collect(Collectors.toList())))
                            .changeTypes(crmCustomer.getChangeType())
                            .build()
            );
            renderJson(R.ok());
        } catch (CrmException e) {
            renderJson(R.error(e.getMessage()));
        }
    }

    /**
     * @author wyq
     * 查询自定义字段
     */
    public void queryField() {
        renderJson(R.ok().put("data", crmCustomerService.queryField()));
    }

    /**
     * @author wyq
     * 添加跟进记录
     */
    @NotNullValidate(value = "typesId", message = "客户id不能为空")
    @NotNullValidate(value = "content", message = "内容不能为空")
    @Permissions("crm:notes:save")
    @CrmEventAnnotation(crmEventEnum = CrmEventEnum.LATELY_FOLLOW_EVENT)
    public void addRecord(@Para("") AdminRecord adminRecord) {
        if (adminRecord.getContent().length() > 2000) {
            renderJson(R.error("小记内容长度不能超过2000"));
            return;
        }

        try {
            adminRecord.setCreateUserId(BaseUtil.getUserId().intValue());
            R r = crmNotesService.addRecord(adminRecord, CrmConstant.CRM_CUSTOMER);
            renderJson(r);
        } catch (Exception e) {
            logger.error(String.format("addRecord customer msg:%s", BaseUtil.getExceptionStack(e)));
            renderJson(R.error(e.getMessage()));

        }
    }

    /**
     * @author wyq
     * 查看跟进记录
     */
    @Permissions("crm:notes:index")
    public void getRecord(BasePageRequest<CrmCustomer> basePageRequest) {
        try {
            renderJson(R.ok().put("data", crmNotesService.getRecord(basePageRequest, ossPrivateFileUtil, CrmConstant.CRM_CUSTOMER)));
        } catch (Exception e) {
            logger.error(String.format("getRecord customer msg:%s", BaseUtil.getExceptionStack(e)));
            renderJson(R.error(e.getMessage()));

        }
    }

    /**
     * @author HJP
     * 员工客户分析
     */
    public void getUserCustomerAnalysis(BasePageRequest<AdminUser> basePageRequest) {
        renderJson(crmCustomerService.getUserCustomerAnalysis(basePageRequest));
    }

    /**
     * @author wyq
     * 客户批量导出
     */
    @Permissions("crm:customer:excelexport")
    public void batchExportExcel(@Para("ids") String customerIds) throws IOException {
        List<Record> recordList = crmCustomerService.exportCustomer(customerIds);
        export(recordList);
        renderNull();
    }

    /**
     * @author wyq
     * 全部导出
     */
    @Permissions("crm:customer:excelexport")
    public void allExportExcel(BasePageRequest basePageRequest) throws IOException {
        JSONObject jsonObject = basePageRequest.getJsonObject();
        jsonObject.fluentPut("excel", "yes").fluentPut("type", "2");
        AdminSceneService adminSceneService = new AdminSceneService();
        List<Record> recordList = (List<Record>) adminSceneService.filterConditionAndGetPageList(basePageRequest).get("data");
        export(recordList);
        renderNull();
    }

    private void export(List<Record> recordList) throws IOException {
        ExcelWriter writer = ExcelUtil.getWriter();
        AdminFieldService adminFieldService = new AdminFieldService();
        List<Record> fieldList = adminFieldService.list("2");
        writer.addHeaderAlias("customer_name", "客户名称");
        writer.addHeaderAlias("register_capital", "注册资本");
        writer.addHeaderAlias("legal_person", "法人");
        writer.addHeaderAlias("registration_number", "工商注册号");
        writer.addHeaderAlias("credit_code", "统一信用代码");
        writer.addHeaderAlias("remark", "备注");
        writer.addHeaderAlias("address", "省市区");
        writer.addHeaderAlias("detail_address", "详细地址");
        writer.addHeaderAlias("customer_grade", "客户等级");
        writer.addHeaderAlias("distributor", "分销商等级");
        writer.addHeaderAlias("customer_type", "客户类型");
        writer.addHeaderAlias("create_user_name", "创建人");
        writer.addHeaderAlias("owner_user_name", "负责人");
        writer.addHeaderAlias("dept_name", "部门");
        writer.addHeaderAlias("is_multiple", "是否允许该客户创建多个网站会员账号");
        writer.addHeaderAlias("create_time", "创建时间");
        writer.addHeaderAlias("update_time", "更新时间");

        HttpServletResponse response = getResponse();
        List<Map<String, Object>> list = new ArrayList<>(recordList.size());
        Record recordAdd = new Record();
        for (Record record : recordList) {
            recordAdd.clear();
            String isMultiple = record.getStr("is_multiple");
            String isMultipleExport = "";
            if (!StringUtils.isNullOrEmpty(isMultiple)) {
                if (isMultiple.equals(CrmConstant.ZERO_FLAG)) {
                    isMultipleExport = "否";
                }
                if (isMultiple.equals(CrmConstant.ONE_FLAG)) {
                    isMultipleExport = "是";
                }
            }
            recordAdd.set("customer_name", record.getStr("customer_name"));
            recordAdd.set("register_capital", record.getStr("register_capital"));
            recordAdd.set("legal_person", record.getStr("legal_person"));
            recordAdd.set("registration_number", record.getStr("registration_number"));
            recordAdd.set("credit_code", record.getStr("credit_code"));
            recordAdd.set("remark", record.getStr("remark"));
            recordAdd.set("address", record.getStr("address"));
            recordAdd.set("detail_address", record.getStr("detail_address"));
            recordAdd.set("customer_grade", record.getStr("customer_grade"));
            recordAdd.set("distributor", record.getStr("distributor"));
            recordAdd.set("customer_type", record.getStr("customer_type"));
            recordAdd.set("create_user_name", record.getStr("create_user_name"));
            recordAdd.set("owner_user_name", record.getStr("owner_user_name"));
            recordAdd.set("dept_name", record.getStr("dept_name"));
            recordAdd.set("is_multiple", isMultipleExport);
            recordAdd.set("create_time", record.getStr("create_time"));
            recordAdd.set("update_time", record.getStr("update_time"));
            list.add(recordAdd.getColumns());
        }
        writer.write(list, true);
        for (int i = 0; i < fieldList.size() + 15; i++) {
            writer.setColumnWidth(i, 20);
        }
        //自定义标题别名
        //response为HttpServletResponse对象
        response.setContentType("application/vnd.ms-excel;charset=utf-8");
        response.setCharacterEncoding("UTF-8");
        //test.xls是弹出下载对话框的文件名，不能为中文，中文请自行编码
        response.setHeader("Content-Disposition", "attachment;filename=customer.xls");
        ServletOutputStream out = response.getOutputStream();
        writer.flush(out);
        // 关闭writer，释放内存
        writer.close();
    }

    /**
     * 领取或分配客户
     *
     * @author zxy
     */
    @Permissions("crm:customer:distribute")
    @LogApiOperation(methodName = "getCustomersByIds")
    public void getCustomersByIds() {
        try {
            String ids = get("ids");
            Long userId = getLong("userId");
            /* JSONObject jsonObject= JSON.parseObject(getRawData());*/
            renderJson(crmCustomerService.getCustomersByIds(ids, userId));
        } catch (Exception e) {
            logger.error(String.format("getCustomersByIds customer msg:%s", BaseUtil.getExceptionStack(e)));
            renderJson(R.error(e.getMessage()));

        }
    }

    /**
     * @author wyq
     * 获取导入模板
     */
    public void downloadExcel() {
        HSSFWorkbook wb = new HSSFWorkbook();
        try {
            List<Record> recordList = crmCustomerService.queryExcelField();
            HSSFSheet sheet = wb.createSheet("客户导入表");
            HSSFRow row = sheet.createRow(0);
            for (int i = 0; i < recordList.size(); i++) {
                Record record = recordList.get(i);
                if ("map_address".equals(record.getStr("fieldName"))) {
                    record.set("name", "详细地址").set("setting", new String[]{});
                }
                String[] setting = record.get("setting");
                HSSFCell cell = row.createCell(i);
                if (record.getInt("is_null") == 1) {
                    cell.setCellValue(record.getStr("name") + "(*)");
                } else {
                    cell.setCellValue(record.getStr("name"));
                }
                if (setting != null && setting.length != 0) {
                    CellRangeAddressList regions = new CellRangeAddressList(0, Integer.MAX_VALUE, i, i);
                    DVConstraint constraint = DVConstraint.createExplicitListConstraint(setting);
                    HSSFDataValidation dataValidation = new HSSFDataValidation(regions, constraint);
                    sheet.addValidationData(dataValidation);
                }
            }
            HttpServletResponse response = getResponse();
            try {
                response.setContentType("application/vnd.ms-excel;charset=utf-8");
                response.setCharacterEncoding("UTF-8");
                //test.xls是弹出下载对话框的文件名，不能为中文，中文请自行编码
                response.setHeader("Content-Disposition", "attachment;filename=customer_import.xls");
                wb.write(response.getOutputStream());
                wb.close();
            } catch (Exception e) {
                renderJson(R.error(e.getMessage()));

            }
            renderNull();
        } catch (Exception e) {
            logger.error(String.format("downloadExcel customer msg:%s", BaseUtil.getExceptionStack(e)));
            renderJson(R.error(e.getMessage()));

        } finally {
            try {
                wb.close();
            } catch (Exception e) {
                logger.error(String.format("downloadExcel customer msg:%s", BaseUtil.getExceptionStack(e)));
                renderJson(R.error(e.getMessage()));
            }
        }
    }

    /**
     * @author wyq
     * 导入客户
     */
    @Permissions("crm:customer:excelimport")
    @NotNullValidate(value = "ownerUserId", message = "请选择负责人")
    public void uploadExcel(@Para("file") UploadFile file, @Para("repeatHandling") Integer repeatHandling, @Para("ownerUserId") Integer ownerUserId) {
        try {
            if (repeatHandling != 2) {
                renderJson(R.error("数据重复时的处理方式，只能选择跳过"));
                return;
            }

            R r = crmCustomerService.uploadExcelMultiThread(file, repeatHandling, ownerUserId);
            if (!r.isSuccess() && !"请使用最新导入模板".equals(r.get("msg"))) {
                renderJson(R.error(10000, (String) r.get("msg")).put("data", r.get("data")));
            } else {
                renderJson(r);
            }
        } catch (Exception e) {
            logger.error(String.format("uploadExcel customer msg:%s", BaseUtil.getExceptionStack(e)));
            renderJson(R.error(e.getMessage()));

        }
    }

    /**
     * @author vic
     * 导入客户
     */
    @Permissions("crm:customer:excelimport")
    @NotNullValidate(value = "ownerUserId", message = "请选择负责人")
    public void uploadExcelOld(@Para("file") UploadFile file, @Para("repeatHandling") Integer repeatHandling, @Para("ownerUserId") Integer ownerUserId) {
        try {
            renderJson(crmCustomerService.uploadExcel(file, repeatHandling));
        } catch (Exception e) {
            logger.error(String.format("uploadExcel customer msg:%s", BaseUtil.getExceptionStack(e)));
            renderJson(R.error(e.getMessage()));

        }
    }

    /**
     * Return DSK status for the site member
     *
     * @param page
     * @param limit
     * @param page
     */
    public void dskAccount(@Para("page") String page, @Para("limit") String limit, @Para("customerId") String customerId, @Para("siteMemberIds") String siteMemberIds) {
        try {
            Set<Integer> siteMemberIdList = new HashSet<>();
            if (org.apache.commons.lang3.StringUtils.isNotBlank(siteMemberIds)){
                try {
                    siteMemberIdList.addAll(Arrays.stream(siteMemberIds.split(",")).map(Integer::valueOf).collect(Collectors.toSet()));
                } catch (Exception e) {
                    renderJson(R.error("参数： siteMemberIds 值异常"));
                    return;
                }
            }

            renderJson(R.ok().put("data", crmCustomerService.getDskByCustId(Integer.parseInt(page), Integer.parseInt(limit), customerId,siteMemberIdList)));
        } catch (Exception e) {
            logger.error(String.format("dskAccount customer msg:%s", BaseUtil.getExceptionStack(e)));
            renderJson(R.error(e.getMessage()));

        }
    }

    /**
     * 获取客户详情tab
     *
     * @param ownerUserId
     * @param deptId      return
     * @author yue.li
     */
    public void tabs(@Para("ownerUserId") String ownerUserId, @Para("deptId") String deptId, @Para("siteMemberId") Integer siteMemberId, @Para("sceneCode") String sceneCode) {
        try {
            String userId = String.valueOf(BaseUtil.getUserId());
            renderJson(R.ok().put("data", crmCustomerService.queryTabs(ownerUserId, deptId, userId, siteMemberId, sceneCode)));
        }catch (CrmException e){
            renderJson(R.error(e.getMessage()));
        }catch (Exception e) {
            logger.error(String.format("tabs contacts msg:%s", BaseUtil.getExceptionStack(e)));
            renderJson(R.error(e.getMessage()));
        }
    }

    /**
     * 客户放入部门客户池
     *
     * @author yue.li
     */
    @Permissions("crm:customer:pool")
    @LogApiOperation(methodName = "客户放入部门客户池")
    public void putDeptPoolByIds() {
        try {
            String ids = get("ids");
            renderJson(crmCustomerService.putDeptPoolOrPublicPoolByIds(null, null, ids, CrmConstant.ONE_FLAG));
            //crmCustomerService.redisReset();
        } catch (Exception e) {
            logger.error(String.format("putDeptPoolByIds customer msg:%s", BaseUtil.getExceptionStack(e)));
            renderJson(R.error(e.getMessage()));

        }
    }

    /**
     * 客户放入网站客户池
     *
     * @author yue.li
     */
    @Permissions("crm:customer:putinpool")
    @LogApiOperation(methodName = "客户放入网站客户池")
    public void putPublicPoolByIds() {
        try {
            String ids = get("ids");
            String tagName = get("tagName");
            Integer tagId = getInt("tagId");
            if (org.apache.commons.lang.StringUtils.isBlank(tagName)) {
                renderJson(R.error("参数： tagName 不能为空"));
                return;
            }
            if (tagId == null) {
                renderJson(R.error("参数： tagId 不能为空"));
                return;
            }
            if (org.apache.commons.lang.StringUtils.isBlank(ids)) {
                renderJson(R.error("参数： ids 不能为空"));
                return;
            }

            renderJson(crmCustomerService.putDeptPoolOrPublicPoolByIds(tagName, tagId, ids, CrmConstant.TWO_FLAG));
            //crmCustomerService.redisReset();
        } catch (Exception e) {
            logger.error(String.format("putPublicPoolByIds customer msg:%s", BaseUtil.getExceptionStack(e)));
            renderJson(R.error(e.getMessage()));
        }
    }

    /**
     * 查询网站会员账号
     *
     * @author yue.li
     */
    public void memberAccount(@Para("page") String page, @Para("limit") String limit, @Para("customerId") String customerId) {
        try {
            renderJson(R.ok().put("data", crmSiteMemberService.getSiteMemberInfoByCustId(Integer.parseInt(page), Integer.parseInt(limit), customerId)));
        } catch (Exception e) {
            logger.error(String.format("memberAccount customer msg:%s", BaseUtil.getExceptionStack(e)));
            renderJson(R.error(e.getMessage()));

        }
    }

    /**
     * 查询推广信息
     */
    @NotNullValidate(value = "customerId", type = HttpEnum.JSON, message = "客户编号不能为空")
    @LogApiOperation(methodName = "promotionInformation")
    public void promotionInformation() {
        JSONObject jsonObject = JSONObject.parseObject(getRawData());
        String customerId = "";
        if (Objects.nonNull(jsonObject) && Objects.nonNull(jsonObject.getString("customerId"))) {
            customerId = jsonObject.getString("customerId");
        }
        renderJson(crmSiteMemberService.queryPromotionInfosBy(customerId));
    }

    /**
     * 查询推广信息手机号
     */
    @NotNullValidate(value = "siteMemberId", type = HttpEnum.PARA, message = "会员编号不能为空")
    @LogApiOperation(methodName = "getMobileBySiteMemberId")
    public void queryPromotionRelationMobile(@Para("siteMemberId") String siteMemberId) {
        renderJson(crmSiteMemberService.queryPromotionRelationMobile(Long.valueOf(siteMemberId)));
    }


    /**
     * 根据客户ID获取客户场景
     *
     * @param customerId return
     * @author yue.li
     */
    public void getSemById(@Para("customerId") String customerId) {
        try {
            renderJson(R.ok().put("data", crmCustomerService.getSemById(customerId, ossPrivateFileUtil)));
        } catch (Exception e) {
            logger.error(String.format("getSemById customer msg:%s", BaseUtil.getExceptionStack(e)));
            renderJson(R.error(e.getMessage()));

        }
    }

    public void siteMemberInfo(@Para("accountId") String accountId) {
        try {
            renderJson(R.ok().put("data", crmSiteMemberService.getSiteMemberInfoBySiteMemberId(accountId)));
        } catch (Exception e) {
            logger.error(String.format("siteMemberInfo customer msg:%s", BaseUtil.getExceptionStack(e)));
            renderJson(R.error(e.getMessage()));

        }
    }

    /**
     * 关联网站会员账号与客户
     *
     * @param custId
     * @param siteMemberId
     */
    public void bindAccount(@Para("userId") String custId, @Para("accountId") String siteMemberId) {
        try {
            renderJson(crmSiteMemberService.bindSiteMemberToCust(custId, siteMemberId));
        } catch (Exception e) {
            logger.error(String.format("bindAccount customer msg:%s", BaseUtil.getExceptionStack(e)));
            renderJson(R.error(e.getMessage()));

        }
    }

    /**
     * 根据客户ID获取组织架构图
     *
     * @param custId
     */
    public void organization(@Para("userId") String custId) {
        try {
            renderJson(R.ok().put("data", crmCustomerService.getOrganizationInformation(custId, ossPrivateFileUtil)));
        } catch (Exception e) {
            logger.error(String.format("organization customer msg:%s", BaseUtil.getExceptionStack(e)));
            renderJson(R.error(e.getMessage()));

        }
    }

    /**
     * 客户引导列表页查询
     */
    @Permissions("crm:customer:cusguide")
    public void queryCustomerGuidePageList() {
        CrmCustomerGuideQueryVO param = JSON.parseObject(getRawData(),CrmCustomerGuideQueryVO.class);
        renderJson(R.ok().put("data",crmCustomerSceneService.queryCustomerGuidePageList(param)));
    }

    /**
     * 网站客户池列表页查询
     */
    @Permissions("crm:customerpublic:index")
    public void queryCustomersWebSitePageList(BasePageRequest basePageRequest) {
        try {
            renderJson(adminSceneService.filterConditionAndGetPageList(basePageRequest));
        } catch (Exception e) {
            logger.error(String.format("queryCustomersWebSitePageList customer msg:%s", BaseUtil.getExceptionStack(e)));
            renderJson(R.error(e.getMessage()));

        }
    }


    /**
     * 查询基本信息
     *
     * @param id
     * @author yue.li
     */
    @Permissions("crm:customer:read")
    public void information(@Para("id") Integer id) {
        try {
            List<Record> recordList = crmCustomerService.information(id);
            renderJson(R.ok().put("data", recordList));
        } catch (Exception e) {
            logger.error(String.format("information customer msg:%s", BaseUtil.getExceptionStack(e)));
            renderJson(R.error(e.getMessage()));

        }
    }

    /**
     * 网站客户池查询基本信息
     *
     * @param id
     * @author yue.li
     */
    @Permissions("crm:customerpublic:read")
    public void informationWebsite(@Para("id") Integer id) {
        try {
            List<Record> recordList = crmCustomerService.information(id);
            renderJson(R.ok().put("data", recordList));
        } catch (Exception e) {
            logger.error(String.format("informationWebsite customer msg:%s", BaseUtil.getExceptionStack(e)));
            renderJson(R.error(e.getMessage()));

        }
    }

    /**
     * 官网注销删除客户相关信息
     *
     * @param siteMemberId 官网账号ID
     * @author yue.li
     */
    public void deleteCustomerInfoBySiteMemberId(@Para("siteMemberId") Integer siteMemberId) {
        try {
            crmCustomerService.deleteCustomerInfoBySiteMemberId(siteMemberId);
        } catch (Exception e) {
            logger.error(String.format("deleteCustomerInfoBySiteMemberId customer msg:%s", BaseUtil.getExceptionStack(e)));
            renderJson(R.error(e.getMessage()));

        }
    }

    /**
     * 根据客户id获取该客户下的支付渠道列表
     *
     * @param id 客户id
     */
    @NotNullValidate(value = "custId", message = "客户id不能为空")
    public void getPaymentChannelListByCustId(@Para("custId") Integer id) {
        try {
            renderJson(R.ok().put("data", crmCustomerService.getPaymentChannelListByCustId(id)));
        } catch (Exception e) {
            logger.error(String.format("getPaymentChannelListByCustId occurs exception: %s", BaseUtil.getExceptionStack(e)));
            renderJson(R.error(e.getMessage()));
        }
    }

    /**
     * 保存支付渠道
     */
    public void savePaymentChannel() {
        try {
            JSONObject requestJson = JSONObject.parseObject(getRawData());
            CrmCustomerPaymentChannel request = requestJson.getObject("entity", CrmCustomerPaymentChannel.class);
            Record paymentInDb = crmCustomerService.findPaymentByTypeAndAccount(request.getPayType(), request.getParternAccount());
            if (Objects.nonNull(paymentInDb)) {
                renderJson(R.error(String.format("该支付渠道已被(%s)绑定,如果需要合并客户请联系产品经理", paymentInDb.getStr("customer_name"))));
            } else {
                renderJson(crmCustomerService.savePaymentChannel(request));
            }
        } catch (Exception e) {
            logger.error(String.format("savePaymentChannel msg:%s", BaseUtil.getExceptionStack(e)));
            renderJson(R.error(e.getMessage()));
        }
    }

    /**
     * 根据客户id和支付渠道id删除该客户下的该支付渠道信息
     */
    public void deletePaymentChannel() {
        JSONObject requestJson = JSONObject.parseObject(getRawData());
        if (requestJson.getInteger("custId") == null || requestJson.getInteger("payChannelId") == null) {
            renderJson(R.error("参数非法"));
        }
        crmCustomerService.deletePaymentChannel(requestJson.getInteger("custId"), requestJson.getInteger("payChannelId"));
        renderJson(R.ok());
    }

    /**
     * 模糊查询客户（根据客户名称，官网id，手机号）
     */
    @NotNullValidate(value = "param", message = "查询参数不能为空")
    public void fuzzyMatchCustomer(@Para("param") String param) {
        try {
            renderJson(R.ok().put("data", crmCustomerService.fuzzyMatchCustomer(param)));
        } catch (Exception e) {
            logger.error(String.format("fuzzyMatchCustomer msg:%s", BaseUtil.getExceptionStack(e)));
            renderJson(R.error(e.getMessage()));
        }
    }

    /***
     * 客户省市区补充
     * @author yue.li
     * @param customerId 客户ID
     * @param address 省市区
     */
    @NotNullValidate(value = "customerId", message = "客户id不能为空")
    @NotNullValidate(value = "address", message = "省市区不能为空")
    public void addProvince(@Para("customerId") String customerId, @Para("address") String address) {
        renderJson(crmCustomerService.addProvince(customerId, address));
    }

    /**
     * 更新销售数量
     *
     * @author yue.li
     */
    public void saveSalesLog() {
        try {
            CrmCustomerSalesLog crmCustomerSalesLog = JSON.parseObject(getRawData(), CrmCustomerSalesLog.class);

            // 操作类型为减少时，减少原因不能为空
            if (Integer.valueOf(1).equals(crmCustomerSalesLog.getOperationType())
                    && Objects.isNull(crmCustomerSalesLog.getReduceReasons())) {
                renderJson(R.error("减少销售数量时，减少原因不能为空"));
                return;
            }
            // 减少原因为其他时，备注不能为空
            if (Integer.valueOf(1).equals(crmCustomerSalesLog.getOperationType())
                    && CrmReduceReasonsEnum.OTHERS.getCode().equals(crmCustomerSalesLog.getReduceReasons())
                    && StringUtils.isNullOrEmpty(crmCustomerSalesLog.getRemark())) {
                renderJson(R.error("减少原因为其他时，备注不能为空"));
                return;
            }
            Integer userId = Objects.isNull(BaseUtil.getUserId()) ? null : BaseUtil.getUserId().intValue();
            R salesLogResult = crmCustomerService.saveSalesLog(crmCustomerSalesLog, userId);
            renderJson(salesLogResult);
        } catch (Exception e) {
            logger.error(String.format("saveSalesLog msg:%s", BaseUtil.getExceptionStack(e)));
            renderJson(R.error(e.getMessage()));
        }
    }

    /**
     * 减少原因list
     *
     * @author vic
     */
    public void queryReduceReason() {
        try {
            renderJson(R.ok().put("data", crmCustomerService.queryReduceReason()));
        } catch (Exception e) {
            logger.error(String.format("queryReduceReason msg:%s", BaseUtil.getExceptionStack(e)));
            renderJson(R.error(e.getMessage()));
        }
    }

    /**
     * 获取更新销售数量详情信息
     *
     * @author yue.li
     */
    @NotNullValidate(value = "crmCustomerId", message = "客户id不能为空")
    @NotNullValidate(value = "productCode", message = "产品code不能为空")
    @NotNullValidate(value = "goodsCode", message = "商品code不能为空")
    @NotNullValidate(value = "goodsSpec", message = "规格不能为空")
    public void getSalesLog(@Para("crmCustomerId") String crmCustomerId, @Para("productCode") String productCode, @Para("goodsCode") String goodsCode, @Para("goodsSpec") String goodsSpec) {
        try {
            renderJson(R.ok().put("data", crmCustomerService.getSalesLog(crmCustomerId, productCode, goodsCode, goodsSpec)));
        } catch (Exception e) {
            logger.error(String.format("getSalesLog msg:%s", BaseUtil.getExceptionStack(e)));
            renderJson(R.error(e.getMessage()));
        }
    }

    @NotNullValidate(value = "siteMemberId", message = "网站会员id不能为空")
    public void getDistributorInfo(@Para("siteMemberId") Integer siteMemberId) {
        try {
            List<DistributorStatistic> result = crmCustomerService.getDistributorStatisticInfo(siteMemberId);
            List<Record> records = BaseUtil.convertModelList2RecordList(result);
            renderJson(R.ok().put("data", records));
        } catch (Exception e) {
            logger.error(String.format("getDistributorInfo msg:%s", BaseUtil.getExceptionStack(e)));
            renderJson(R.error(e.getMessage()));
        }
    }

    /**
     * 通过BD签到地址更新客户地址
     *
     * @param addressId customer address id
     */
    @NotNullValidate(value = "addressId", message = "签到历史地址id不能为空")
    public void updateAddressFromSignin(@Para("addressId") String addressId) {
        try {
            renderJson(crmCustomerService.updateCustomerAddressBySigninAddress(addressId));
        } catch (Exception e) {
            logger.error(String.format("updateAddressFromSigninAddress msg:%s", BaseUtil.getExceptionStack(e)));
            renderJson(R.error(e.getMessage()));
        }
    }

    /**
     * 客户领取
     *
     * @author yue.li
     */
    @Permissions("crm:customer:receive")
    @NotNullValidate(value = "customerId", message = "客户id不能为空")
    @NotNullValidate(value = "ownerUserId", message = "负责人id不能为空")
    @LogApiOperation(methodName = "receive")
    @Deprecated
    public void receive(@Para("customerId") Long customerId, @Para("ownerUserId") Integer ownerUserId) {
        try {
            renderJson(crmCustomerService.receive(customerId, ownerUserId));
        } catch (Exception e) {
            logger.error(String.format("receive customer msg:%s", BaseUtil.getExceptionStack(e)));
            renderJson(R.error(e.getMessage()));
        }
    }

    /**
     * 客户领取(调用OA提交流程)
     *
     * @author yue.li
     */
    @Permissions("crm:customer:receive")
    public void applyCrmWorkFlow() {
        CrmCustomer crmCustomer = JSON.parseObject(getRawData(), CrmCustomer.class);
        renderJson(crmCustomerService.applyCrmWorkFlow(crmCustomer.getCustomerId(), crmCustomer.getReason(), crmCustomer.getIndustryCode(), esbConfig));
    }

    /**
     * 检查客户是否需要领取客户
     *
     * @param customerName 客户名称
     * @param mobile       电话
     * @author yue.li
     */
    @Deprecated
    public void checkCustomerNeedCustomerReceive(@Para("customerName") String customerName, @Para("mobile") String mobile) {
        try {
            renderJson(crmCustomerService.checkCustomerNeedCustomerReceive(customerName, mobile));
        } catch (Exception e) {
            logger.error(String.format("checkContactsNeedCustomerReceive error msg:%s", BaseUtil.getExceptionStack(e)));
            renderJson(R.error(e.getMessage()));
        }
    }

    /**
     * 检查企业客户是否重名
     *
     * @param customerName 客户名称
     */
    public void checkCustomerName(@Para("customerName") String customerName, @Para("customerId") Integer customerId) {
        R r = crmCustomerService.checkCompanyCustomerName(customerName, customerId);

        if (!r.isSuccess()) {
            Record record = (Record) r.get("data");
            if (customerId != null) {
                if (Objects.equals(customerId, record.getInt("customer_id"))) {
                    renderJson(R.ok());
                } else {
                    renderJson(R.error(crmCustomerService.customerExistInfo(record)));
                }
            } else {
                renderJson(R.error(crmCustomerService.customerExistInfo(record)));
            }
            return;
        }

        renderJson(r);
    }

    /**
     * 检查客户行业是否可领取
     */
    public void canReceive() {
        JSONObject jsonObject = JSON.parseObject(getRawData());
        Long customerId = jsonObject.getLong("customerId");
        if (customerId == null) {
            renderJson(R.error("参数： customerId 不能为空"));
            return;
        }

        renderJson(crmCustomerService.canReceive(customerId, BaseUtil.getDeptId()));
    }

    /**
     * 判断是否可以领取客户
     */
    @NotNullValidate(value = "customerId", type = HttpEnum.JSON, message = "客户ID不能为空")
    @NotNullValidate(value = "industryCode", type = HttpEnum.JSON, message = "行业编码不能为空")
    public void canReceiveWithIndustry() {
        try {
            JSONObject param = JSON.parseObject(getRawData());
            Long customerId = param.getLong("customerId");
            String industryCode = param.getString("industryCode");
            //获取BD部门
            AdminUser currentUser = BaseUtil.getUser();
            if (Objects.isNull(currentUser)) {
                renderJson(R.error("当前用户尚未登录"));
                return;
            }
            if (Objects.isNull(currentUser.getDeptId())) {
                renderJson(R.error("当前用户没有部门"));
                return;
            }
            renderJson(crmCustomerService.canReceiveWithIndustry(customerId, industryCode, Long.valueOf(currentUser.getDeptId()), esbConfig));
        } catch (CrmException e) {
            logger.error(String.format("%s %s msg:%s", "CrmCustomerController", "canReceiveWithIndustry", BaseUtil.getExceptionStack(e)));
            renderJson(R.error(e.getMessage()));
        } catch (Exception e) {
            logger.error(String.format("%s %s msg:%s", "CrmCustomerController", "canReceiveWithIndustry", BaseUtil.getExceptionStack(e)));
            renderJson(R.error("服务异常"));
        }
    }

    /**
     * 确认领取
     */
    @NotNullValidate(value = "customerId", type = HttpEnum.JSON, message = "客户ID不能为空")
    @NotNullValidate(value = "industryCode", type = HttpEnum.JSON, message = "行业编码不能为空")
    @LogApiOperation(methodName = "客户领取")
    public void confirmReceiveWithIndustry() {
        try {
            JSONObject param = JSON.parseObject(getRawData());
            Long customerId = param.getLong("customerId");
            String industryCode = param.getString("industryCode");
            //获取BD部门
            AdminUser currentUser = BaseUtil.getUser();
            if (Objects.isNull(currentUser)) {
                renderJson(R.error("当前用户尚未登录"));
                return;
            }
            if (Objects.isNull(currentUser.getDeptId())) {
                renderJson(R.error("当前用户没有部门"));
                return;
            }
            // 查询转移对象中，是否有数字地信事业部、在线运营事业部bd
            // 判断登录用户是否是侧脸测绘事业部、在线运营事业部的bd
            Long checkUserId = null;
            if (!StringUtil.isNullOrEmpty(promotionDistributorDeptIds)) {
                Long currentUserId = BaseUtil.getUserId();
                for (String deptId : Arrays.asList(promotionDistributorDeptIds.split(","))) {
                    if (Long.valueOf(deptId).equals(adminUserService.getBusinessDepartmentOfUserById(currentUserId))) {
                        checkUserId = currentUserId;
                    }
                }
            }

            if (!Objects.isNull(checkUserId)) {

                AdminUser adminUser = adminUserService.getAdminUserByUserId(checkUserId);

                // 有上游分销商的客户不允许添加团队成员
                if (!Objects.isNull(customerId)) {

                    List<Integer> customerIds = Lists.newArrayList();
                    customerIds.add(Math.toIntExact(customerId));
                    for (Record record : crmCustomerService.checkParentDistributor(customerIds)) {
                        renderJson(R.error("客户" + record.getStr("customerName") + "已被分销商" + record.getStr("realName") + "绑定;\r\n——数字地信事业部和在线运营事业部的同学不可作为其团队成员:"
                                + adminUser.getRealname()));
                        return;
                    }
                }
            }

            renderJson(crmCustomerService.confirmReceiveWithIndustry(customerId, industryCode, currentUser.getUserId(), Long.valueOf(currentUser.getDeptId()), esbConfig));
        } catch (CrmException e) {
            renderJson(R.error(e.getMessage()));
        } catch (Exception e) {
            logger.error(String.format("%s %s msg:%s", "CrmCustomerController", "confirmReceiveWithIndustry", BaseUtil.getExceptionStack(e)));
            renderJson(R.error("服务异常"));
        }
    }

    /**
     * 查询我负责客户的库容信息
     */
    public void searchCapacity() {
        renderJson(R.ok().put("data", crmCustomerService.searchUserCapacity(BaseUtil.getUserId())));
    }

    /**
     * 判断是否需要审批
     *
     * @author yue.li
     */
    @NotNullValidate(value = "customerId", type = HttpEnum.JSON, message = "客户ID不能为空")
    @NotNullValidate(value = "deptId", type = HttpEnum.JSON, message = "部门ID不能为空")
    public void judgeNeedToApproval() {
        try {
            JSONObject param = JSON.parseObject(getRawData());
            Long customerId = param.getLong("customerId");
            Integer deptId = param.getInteger("deptId");

            renderJson(crmCustomerService.judgeNeedToApproval(customerId, deptId, esbConfig));
        } catch (Exception e) {
            logger.error(String.format("%s %s msg:%s", "CrmCustomerController", "judgeNeedToApproval", BaseUtil.getExceptionStack(e)));
            renderJson(R.error(e.getMessage()));
        }
    }

    /**
     * 获取登陆人的归属部门池信息
     */
    public void getDeptAndCapacity() {
        CrmUser user = BaseUtil.getCrmUser();
        R r = crmCustomerExtService.getDeptAndCapacity(user);

        renderJson(r);
    }

    /**
     * 客户列表页下拉框接口
     */
    public void filterList() {
        renderJson(R.okWithData(crmCustomerSceneService.listAllSelect()));
    }

    /**
     * 模糊查询上游分销商
     * @param name
     */
    @NotNullValidate(value = "name", message = "请输入上游分销商名称")
    public void fuzzyQuerySubDistributor(String name) {
        renderJson(R.okWithData(crmCustomerSceneService.fuzzyQuerySubDistributor(name)));
    }

    /**
     * 获取当前登陆人的客户查询场景
     */
    public void queryScene(){
        renderJson(crmCustomerSceneService.queryScene());
    }

    /**
     * 列表查询
     */
    @LogApiOperation
    @Permissions({"crm:customer:index_customerAll",
            "crm:customer:index_customerCreate",
            "crm:customer:index_customerDept",
            "crm:customer:index_customerPublic",
            "crm:customer:index_customerTelemarketing",
            "crm:customer:index_customerDistributor",
            "crm:customer:index_customerOwn",
            "crm:customer:index_customerMySub",
            "crm:customer:index_customerTakePart"
    })
    public void queryCustomersPageListNew() {
        try {
            BasePageRequest<CrmCustomerQueryParamDto> request = new BasePageRequest<>(getRawData(), CrmCustomerQueryParamDto.class);
            renderJson(R.okWithData(crmCustomerSceneService.queryPageWithSceneCode(request)));
        } catch (CrmException e) {
            renderJson(R.error(e.getMessage()));
        }
    }

    /**
     * 根据场景获取客户所属部门
     * @param sceneCode
     */
    public void customerDeptBySceneCode(String sceneCode) {
        try {
            renderJson(R.okWithData(crmCustomerSceneService.customerDeptBySceneCode(sceneCode)));
        } catch (CrmException e) {
            renderJson(R.error(e.getMessage()));
        }
    }
}
