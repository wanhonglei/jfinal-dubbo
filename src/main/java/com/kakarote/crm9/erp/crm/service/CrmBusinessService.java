package com.kakarote.crm9.erp.crm.service;


import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.jfinal.aop.Before;
import com.jfinal.aop.Inject;
import com.jfinal.kit.JsonKit;
import com.jfinal.kit.Kv;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.SqlPara;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.jfinal.plugin.rpc.annotation.RPCInject;
import com.kakarote.crm9.common.config.JfinalConfig;
import com.kakarote.crm9.common.config.paragetter.BasePageRequest;
import com.kakarote.crm9.common.constant.BaseConstant;
import com.kakarote.crm9.common.exception.CrmException;
import com.kakarote.crm9.common.theadpool.CrmThreadPool;
import com.kakarote.crm9.erp.admin.entity.AdminDept;
import com.kakarote.crm9.erp.admin.entity.AdminFile;
import com.kakarote.crm9.erp.admin.entity.AdminScene;
import com.kakarote.crm9.erp.admin.entity.AdminUser;
import com.kakarote.crm9.erp.admin.entity.CrmBusinessStatus;
import com.kakarote.crm9.erp.admin.entity.CrmBusinessStatusVerification;
import com.kakarote.crm9.erp.admin.service.AdminDeptService;
import com.kakarote.crm9.erp.admin.service.AdminFieldService;
import com.kakarote.crm9.erp.admin.service.AdminFileService;
import com.kakarote.crm9.erp.admin.service.AdminUserService;
import com.kakarote.crm9.erp.admin.service.CrmBusinessGroupService;
import com.kakarote.crm9.erp.admin.service.CrmBusinessStatusSalesActivityService;
import com.kakarote.crm9.erp.admin.service.CrmBusinessStatusService;
import com.kakarote.crm9.erp.admin.service.CrmBusinessStatusVerificationService;
import com.kakarote.crm9.erp.crm.common.BusinessOrderEnum;
import com.kakarote.crm9.erp.crm.common.CrmBusinessChangeLogEnum;
import com.kakarote.crm9.erp.crm.common.CrmBusinessEndEnum;
import com.kakarote.crm9.erp.crm.common.CrmBusinessShareholderRelationEnum;
import com.kakarote.crm9.erp.crm.common.CrmBusinessStatusOpenEnum;
import com.kakarote.crm9.erp.crm.common.CrmEnum;
import com.kakarote.crm9.erp.crm.common.CrmErrorInfo;
import com.kakarote.crm9.erp.crm.common.CrmSaleUsualEnum;
import com.kakarote.crm9.erp.crm.common.SiteFromChannelEnum;
import com.kakarote.crm9.erp.crm.constant.CrmConstant;
import com.kakarote.crm9.erp.crm.entity.BusinessCategoryPrice;
import com.kakarote.crm9.erp.crm.entity.BusinessCategoryPriceStatistic;
import com.kakarote.crm9.erp.crm.entity.BusinessProductPrice;
import com.kakarote.crm9.erp.crm.entity.BusinessProductPriceStatistic;
import com.kakarote.crm9.erp.crm.entity.BusinessReceivableInfo;
import com.kakarote.crm9.erp.crm.entity.BusinessReceivableInfoStatistic;
import com.kakarote.crm9.erp.crm.entity.BusinessReceivablePlanResult;
import com.kakarote.crm9.erp.crm.entity.BusinessRevenueInfo;
import com.kakarote.crm9.erp.crm.entity.CrmActionRecord;
import com.kakarote.crm9.erp.crm.entity.CrmBusiness;
import com.kakarote.crm9.erp.crm.entity.CrmBusinessChange;
import com.kakarote.crm9.erp.crm.entity.CrmBusinessExcel;
import com.kakarote.crm9.erp.crm.entity.CrmBusinessProduct;
import com.kakarote.crm9.erp.crm.entity.CrmBusinessStatusSalesActivityRecord;
import com.kakarote.crm9.erp.crm.entity.CrmBusinessStatusVerificationRecord;
import com.kakarote.crm9.erp.crm.entity.CrmCustomer;
import com.kakarote.crm9.erp.crm.entity.CrmProduct;
import com.kakarote.crm9.erp.crm.entity.CrmProductCategory;
import com.kakarote.crm9.erp.crm.entity.CrmProductDetail;
import com.kakarote.crm9.erp.crm.entity.CrmProductDetailDto;
import com.kakarote.crm9.erp.crm.entity.CrmReceivables;
import com.kakarote.crm9.erp.crm.entity.CrmReceivablesListByBusinessIdNoViewResult;
import com.kakarote.crm9.erp.crm.entity.CrmSiteMember;
import com.kakarote.crm9.erp.crm.entity.CustomerRevenueStatistic;
import com.kakarote.crm9.integration.client.PerformanceClient;
import com.kakarote.crm9.integration.common.PaymentTypeEnum;
import com.kakarote.crm9.integration.dto.PerformancePlanDto;
import com.kakarote.crm9.utils.BaseUtil;
import com.kakarote.crm9.utils.CrmDateUtil;
import com.kakarote.crm9.utils.FieldUtil;
import com.kakarote.crm9.utils.OssPrivateFileUtil;
import com.kakarote.crm9.utils.R;
import com.qxwz.galaxy.api.request.BaseRequest;
import com.qxwz.galaxy.api.response.BaseResponse;
import com.qxwz.venus.biz.api.RegisterBizService;
import com.qxwz.venus.biz.dto.VirtualRegInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
public class CrmBusinessService {

    @Inject
    private AdminFieldService adminFieldService;

    @Inject
    private FieldUtil fieldUtil;

    @Inject
    private CrmRecordService crmRecordService;

    @Inject
    private CrmChangeLogService crmChangeLogService;

    @Inject
    private AdminUserService adminUserService;

    @Inject
    private AdminFileService adminFileService;

    @Inject
    private CrmBusinessStatusVerificationService crmBusinessStatusVerificationService;

    @Inject
    private CrmBusinessStatusSalesActivityService crmBusinessStatusSalesActivityService;

    @Inject
    private CrmBusinessStatusService crmBusinessStatusService;

    @Inject
    private AdminDeptService adminDeptService;

    @Inject
    private CrmBusinessGroupService crmBusinessGroupService;

    @Inject
    private CrmMessageFromEsbService crmMessageFromEsbService;

    @RPCInject
    private RegisterBizService registerBizService;

    @Inject
    private PerformanceClient performanceClient;

    /**
     * @author wyq
     * 分页条件查询商机
     */
    public Page<Record> getBusinessPageList(BasePageRequest basePageRequest) {
        return Db.paginate(basePageRequest.getPage(), basePageRequest.getLimit(), new SqlPara().setSql("select * from businessview"));
    }

    /**
     * @author wyq
     * 新增或更新商机
     */
    @Before(Tx.class)
    public R addOrUpdate(JSONObject jsonObject, Long userId) {
        CrmBusiness crmBusiness = jsonObject.getObject("entity", CrmBusiness.class);
        JSONArray products = jsonObject.getJSONArray("product");
        log.info("business addOrUpdate entity :{} ", jsonObject);

        if (Objects.nonNull(crmBusiness.getBusinessId())) {
            Db.delete(Db.getSql("crm.business.clearBusinessProduct"), crmBusiness.getBusinessId());
        }

        String batchId = StrUtil.isNotEmpty(crmBusiness.getBatchId()) ? crmBusiness.getBatchId() : IdUtil.simpleUUID();
        JSONArray field = jsonObject.getJSONArray("field");
        crmRecordService.updateRecord(field, batchId);
        adminFieldService.save(field, batchId);

        boolean saveOrUpdate;

        //添加默认值
        if (crmBusiness.getShareholderRelation() == null){
            crmBusiness.setShareholderRelation(0);
        }

        if (crmBusiness.getBusinessId() != null) {
            crmBusiness.setUpdateTime(DateUtil.date());
            crmRecordService.updateRecord(CrmBusiness.dao.findById(crmBusiness.getBusinessId()), crmBusiness, CrmEnum.BUSINESS_TYPE_KEY.getTypes(), userId);
            saveOrUpdate = crmBusiness.update();
        } else {
            crmBusiness.setCreateTime(DateUtil.date());
            crmBusiness.setUpdateTime(DateUtil.date());
            crmBusiness.setCreateUserId(userId == null ? null : userId.intValue());
            crmBusiness.setOwnerUserId(userId == null ? null : userId.intValue());
            crmBusiness.setBatchId(batchId);
            crmBusiness.setRwUserId(",");
            crmBusiness.setRoUserId(",");

            saveOrUpdate = crmBusiness.save();
            crmRecordService.addRecord(crmBusiness.getBusinessId().intValue(), CrmEnum.BUSINESS_TYPE_KEY.getTypes(), userId);
        }

        if (saveOrUpdate && Objects.nonNull(crmBusiness.getBusinessId()) && Objects.nonNull(crmBusiness.getStatusId())) {
            crmRecordService.addActionRecord(
                    userId == null ? null : userId.intValue(),
                    CrmEnum.BUSINESS_TYPE_KEY.getTypes(),
                    crmBusiness.getBusinessId().intValue(),
                    "将" + Optional.ofNullable(crmBusinessStatusService.getStatusNameById(crmBusiness.getStatusId().longValue())).orElseGet(() -> crmBusiness.getStatusId().toString()) + "标记为当前阶段",
                    "可能性:" + crmBusiness.getStageWinRate() + "%");
        }

        //新建商机需要添加客户变更日志，按是否有负责人、部门区分类型
        CrmBusiness newBusiness = CrmBusiness.dao.findById(crmBusiness.getBusinessId());
        if (newBusiness.getOwnerUserId() != null){
            crmChangeLogService.saveBusinessChangeLog(CrmBusinessChangeLogEnum.BD.getCode(), newBusiness.getBusinessId(), Long.valueOf(newBusiness.getOwnerUserId()), null, userId);
        } else if (newBusiness.getDeptId() != null){
            crmChangeLogService.saveBusinessChangeLog(CrmBusinessChangeLogEnum.DEPT.getCode(), newBusiness.getBusinessId(), null, Long.valueOf(newBusiness.getDeptId()), userId);
        } else {
            crmChangeLogService.saveBusinessChangeLog(CrmBusinessChangeLogEnum.OPEN_SEA.getCode(), newBusiness.getBusinessId(), null, null, userId);
        }

        //处理商品大类 商品明细
        if (products != null) {
            List<CrmBusinessProduct> businessProductList = products.toJavaList(CrmBusinessProduct.class);
            saveBusinessProduct(businessProductList, crmBusiness);
        }
        return saveOrUpdate ? R.ok() : R.error();
    }

    /**
     * 保存商品大类、商品信息、
     *
     * @param businessProductList 商机产品类型
     * @param crmBusiness         商机信息
     * @author yue.li
     */
    public boolean saveBusinessProduct(List<CrmBusinessProduct> businessProductList, CrmBusiness crmBusiness) {
        List<CrmProductCategory> needAddCategory = Lists.newArrayList();
        List<CrmProduct> needAddProduct = Lists.newArrayList();
        boolean saveOrUpdate = true;
        // 校验商机产品
        this.checkBusinessProduct(businessProductList);

        businessProductList.forEach(item -> {
            // category
            String categoryCode = item.getCategoryCode();
            Record record = Db.findFirst(Db.getSql("crm.product.queryCategoryBycategoryCode"), categoryCode);
            if (record == null) {
                CrmProductCategory proCategory = new CrmProductCategory();
                proCategory.setCategoryCode(categoryCode);
                proCategory.setName(item.getCategoryName());
                proCategory.setCreateTime(DateUtil.date());
                proCategory.setUpdateTime(DateUtil.date());

                needAddCategory.add(proCategory);
            }

            // product
            Record proRecord = Db.findFirst(Db.getSql("crm.product.getProductByCodeName"), item.getCode(), item.getName());
            if (proRecord == null) {
                CrmProductDetailDto dto = new CrmProductDetailDto();
                dto.setCode(item.getCode());
                dto.setName(item.getName());
                dto.setBatchId(crmBusiness.getBatchId());
                dto.setCategoryCode(categoryCode);
                dto.setUserId(Math.toIntExact(BaseUtil.getUserId()));
                CrmProduct product = constructCrmProduct(dto);
                needAddProduct.add(product);
            }

        });

        // add new category
        if (needAddCategory.size() > 0) {
            List<Record> records = needAddCategory.stream().map(CrmProductCategory::toRecord).collect(Collectors.toList());
            Db.batchSave("72crm_crm_product_category", records, records.size());
        }

        // get category code and category id map
        Map<String, Integer> categoryIdMap = getCategoryMap();

        // add new product
        if (needAddProduct.size() > 0) {
            needAddProduct.forEach(item -> item.setCategoryId(categoryIdMap.get(item.getCategoryCode())));
            List<Record> prods = needAddProduct.stream().map(CrmProduct::toRecord).collect(Collectors.toList());
            Db.batchSave("72crm_crm_product", prods, prods.size());
        }

        // business and product relationship
        Map<String, Integer> prodCodeIdMap = Maps.newHashMap();
        List<String> codes = businessProductList.stream().map(CrmBusinessProduct::getCode).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(codes)) {
            List<Record> prodIds = Db.find(Db.getSqlPara("crm.product.getProdIdsByProdCode", Kv.by("codes", codes)));

            prodIds.forEach(item -> prodCodeIdMap.put(item.getStr("code") + item.getStr("product_name"), item.getInt("product_id")));

            // 保存商机产品信息
            log.info("business addOrUpdate prodCodeIdMap :{} ", prodCodeIdMap);
            if(businessProductList.size() > 0){
                CrmBusinessProduct crmBusinessProduct = new CrmBusinessProduct();
                for(CrmBusinessProduct businessProduct:businessProductList){
                    log.info("business addOrUpdate product code :{} name:{}", businessProduct.getCode(),businessProduct.getName());
                    // 如果已经存在商机、商品关联信息，删除
                    if(businessProduct.getId() != null){
                        Db.delete(Db.getSql("crm.business.clearBusinessProductById"), businessProduct.getId());
                    }
                    crmBusinessProduct.clear();
                    crmBusinessProduct.setBusinessId(crmBusiness.getBusinessId().intValue());
                    crmBusinessProduct.setProductId(prodCodeIdMap.get(businessProduct.getCode() + businessProduct.getName()));
                    crmBusinessProduct.setSalesPrice(businessProduct.getSalesPrice());
                    crmBusinessProduct.setCreateTime(DateUtil.date());
                    crmBusinessProduct.setUpdateTime(DateUtil.date());
                    crmBusinessProduct.setNum(businessProduct.getNum());
                    if (!crmBusinessProduct.save()) {
                        saveOrUpdate = false;
                    }
                }
            }
        }
        return saveOrUpdate;
    }

      /**
     * 校验商机产品
     */
    private boolean checkBusinessProduct(List<CrmBusinessProduct> businessProductList) {

        boolean isOK = true;
        StringBuilder errMsgBuffer = new StringBuilder();
        for (int i = 1; i <= businessProductList.size(); i++) {
            CrmBusinessProduct item = businessProductList.get(i - 1);
            // 销售价格不能为空
            if (Objects.isNull(item.getSalesPrice())) {
                isOK = false;
                errMsgBuffer.append("第").append(i).append("行商品的销售价格为空;\\r\\n");
            }
        }
        if (errMsgBuffer.length() != 0) {
            throw new CrmException(errMsgBuffer.toString());
        }
        return isOK;
    }

    /**
     * 构造产品信息
     *
     * @param dto 商品展示类
     * @return
     * @author yue.li
     */
    public CrmProduct constructCrmProduct(CrmProductDetailDto dto) {
        CrmProduct product = new CrmProduct();
        product.setCode(dto.getCode());
        product.setName(dto.getName());
        product.setBatchId(dto.getBatchId());
        product.setCategoryCode(dto.getCategoryCode());
        product.setCategoryId(dto.getCategoryId());
        product.setCreateUserId(dto.getUserId());
        product.setCreateTime(DateUtil.date());
        product.setUpdateTime(DateUtil.date());
        product.setCreateTime(DateUtil.date());
        product.setUpdateTime(DateUtil.date());
        product.setStatus(CrmConstant.PRODUCT_PULL_ON);
        return product;
    }

    /**
     * @author wyq
     * 根据商机id查询
     */
    public CrmBusiness queryById(Integer businessId) {
        CrmBusiness business = CrmBusiness.dao.findFirst(Db.getSqlPara("crm.business.queryBusiness",Kv.by("businessId",businessId)));
        if (business != null) {
            Map<String, Object> map = new HashMap<>();
            map.put("hasProduct", hasProduct(businessId));
            map.put("shareholder_relation", CrmBusinessShareholderRelationEnum.getName(business.getShareholderRelation()));
            business._setOrPut(map);
        }
        return business;
    }

    public boolean hasProduct(Integer businessId) {
        List<Record> records = Db.find(Db.getSql("crm.business.hasProduct"), businessId);
        return records != null && records.size() > 0;
    }

    /**
     * @author wyq
     * 基本信息
     */
    public List<Record> information(Integer busienssId) {
        Record record = Db.findFirst(Db.getSql("crm.business.queryDetailById"), busienssId);
        if (null == record) {
            return null;
        }
        String deptAttribute = JfinalConfig.crmProp.get("deptAttribute").replace('\'', '\"');
        JSONObject jsonObject = JSONObject.parseObject(deptAttribute);
        String deptName = record.getStr("dept_name");

        List<Record> fieldList = new ArrayList<>();
        FieldUtil field = new FieldUtil(fieldList);
        field.set("商机名称", record.getStr("business_name"))
                .set("客户名称", record.getStr("customer_name"))
                .set("业务线", deptName)
                .set("当前阶段", record.getStr("status_name"))
                .set("可能性", Optional.ofNullable(record.getStr("stage_win_rate")).orElse("-") + "%")
                .set("应用场景", record.getStr("application_scenario_name"))
                .set("合作伙伴(合同甲方)", record.getStr("partner"))
                .set("预计结束日期", DateUtil.formatDate(record.get("deal_date")))
                .set("项目", record.getStr("project"))
                .set("商机金额", record.getStr("money"))
                .set("备注", record.getStr("remark"));

        JSONObject deptJson = new JSONObject();
        if (CrmConstant.PUBLIC_SAFETY.equals(deptName)) {
            deptJson = jsonObject.getJSONObject("publicSafety");
        } else if (CrmConstant.FUTURE_CITY.equals(deptName)) {
            deptJson = jsonObject.getJSONObject("futureCity");
        } else if (CrmConstant.POWER.equals(deptName)) {
            deptJson = jsonObject.getJSONObject("power");
        } else if (CrmConstant.DRIVER.equals(deptName)) {
            deptJson = jsonObject.getJSONObject("driver");
        }
        //根据不同部门展示不同的字段
        deptJson.forEach((key, value) -> field.set((String) value, record.getStr(key)));

        List<Record> fields = adminFieldService.list("5");
        for (Record r : fields) {
            field.set(r.getStr("name"), record.getStr(r.getStr("name")));
        }
        return fieldList;
    }

    /**
     * @author wyq
     * 根据商机名称查询
     */
    public Record queryByName(String name) {
        return Db.findFirst(Db.getSql("crm.business.queryByName"), name);
    }

    /**
     * @author wyq
     * 根据商机id查询产品
     */
    public R queryProduct(BasePageRequest<CrmBusiness> basePageRequest) {
        CrmBusiness crmBusiness = basePageRequest.getData();
        if (crmBusiness == null){
            return R.error("参数异常");
        }

        Long businessId = crmBusiness.getBusinessId();
        if (businessId == null){
            return R.error("参数:businessId 不能为空");
        }

        Integer pageType = basePageRequest.getPageType();
        Record record = Db.findFirst(Db.getSql("crm.product.querySubtotalByBusinessId"), businessId);
        if (record.getStr("money") == null) {
            record.set("money", 0);
        }
        if (0 == pageType) {
            record.set("list", Db.find(Db.getSql("crm.business.queryProduct"), businessId));
        } else {
            record.set("list", Db.paginate(basePageRequest.getPage(), basePageRequest.getLimit(), new SqlPara().setSql(Db.getSql("crm.business.queryProduct")).addPara(businessId)));
        }
        return R.ok().put("data", record);
    }

    /**
     * @author wyq
     * 根据商机id查询合同
     */
    public R queryContract(BasePageRequest<CrmBusiness> basePageRequest) {
        Integer businessId = basePageRequest.getData().getBusinessId().intValue();
        Integer pageType = basePageRequest.getPageType();
        if (0 == pageType) {
            return R.ok().put("data", Db.find(Db.getSql("crm.business.queryContract"), businessId));
        } else {
            return R.ok().put("data", Db.paginate(basePageRequest.getPage(), basePageRequest.getLimit(), new SqlPara().setSql(Db.getSql("crm.business.queryContract")).addPara(businessId)));
        }
    }

    /**
     * @author wyq
     * 根据商机id查询联系人
     */
    public R queryContacts(BasePageRequest<CrmBusiness> basePageRequest) {
        Integer businessId = basePageRequest.getData().getBusinessId().intValue();
        Integer pageType = basePageRequest.getPageType();
        if (0 == pageType) {
            return R.ok().put("data", Db.find(Db.getSql("crm.business.queryContacts"), businessId));
        } else {
            return R.ok().put("data", Db.paginate(basePageRequest.getPage(), basePageRequest.getLimit(), new SqlPara().setSql(Db.getSql("crm.business.queryContacts")).addPara(businessId)));
        }
    }

    /**
     * @author wyq
     * 根据id删除商机
     */
    public R deleteByIds(String businessIds) {
        String[] idsArr = businessIds.split(",");
        CrmProduct crmProductDao = CrmProduct.dao.findFirst(Db.getSql("crm.business.queryBusinessProducts"), businessIds);
        CrmReceivables crmReceivablesDao = CrmReceivables.dao.findFirst(Db.getSql("crm.business.queryBusinessReceivables"), businessIds);
        if (Objects.nonNull(crmProductDao) && Objects.nonNull(crmReceivablesDao)) {
            return R.error(CrmErrorInfo.BUSINESS_PRODUCT_RECEIVABLES);
        }
        if (Objects.nonNull(crmProductDao)) {
            return R.error(CrmErrorInfo.BUSINESS_PRODUCT);
        }
        if (Objects.nonNull(crmReceivablesDao)) {
            return R.error(CrmErrorInfo.BUSINESS_RECEIVABLES);
        }
        List<Record> idsList = new ArrayList<>(idsArr.length);
        for (String id : idsArr) {
            Record record = new Record();
            idsList.add(record.set("business_id", Integer.valueOf(id)));
        }
        return Db.tx(() -> {
            Db.batch(Db.getSql("crm.business.deleteByIds"), "business_id", idsList, 100);
            return true;
        }) ? R.ok() : R.error();
    }

    /**
     * 根据客户id变更负责人
     *
     * @author wyq
     */
    public R updateOwnerUserId(CrmCustomer crmCustomer) {
        CrmBusiness crmBusiness = new CrmBusiness();
        crmBusiness.setNewOwnerUserId(crmCustomer.getNewOwnerUserId());
        crmBusiness.setTransferType(crmCustomer.getTransferType());
        crmBusiness.setPower(crmCustomer.getPower());
        String[] customerIdsArr = crmCustomer.getCustomerIds().split(",");
        StringBuilder stringBuilder = new StringBuilder();
        for (String customerId : customerIdsArr) {
            CrmBusiness crmBusinessDao = CrmBusiness.dao.findFirst("select business_id from 72crm_crm_business where customer_id = ?", Integer.valueOf(customerId));
            if (crmBusinessDao != null) {
                stringBuilder.append(',').append(crmBusinessDao.getBusinessId());
            }
        }
        if (!"".equals(stringBuilder.toString())) {
            stringBuilder.deleteCharAt(0);
        }
        crmBusiness.setBusinessIds(stringBuilder.toString());
        if (!"".equals(stringBuilder.toString())) {
            return transfer(crmBusiness);
        } else {
            return R.ok();
        }
    }

    /**
     * @author wyq
     * 根据商机id变更负责人
     */
    public R transfer(CrmBusiness crmBusiness) {
        String[] businessIdsArr = crmBusiness.getBusinessIds().split(",");
        return Db.tx(() -> {
            for (String businessId : businessIdsArr) {
                CrmBusiness oldBusiness = CrmBusiness.dao.findById(Integer.valueOf(businessId));
                if (Objects.isNull(oldBusiness) || Objects.isNull(oldBusiness.getBusinessId())) {
                    log.error("CrmBusiness transfer businessId is non-existent，businessId:{}", businessId);
                    return false;
                }
                String memberId = "," + crmBusiness.getNewOwnerUserId() + ",";
                Db.update(Db.getSql("crm.business.deleteMember"), memberId, memberId, Integer.valueOf(businessId));
                Integer oldOwnerUserId = oldBusiness.getOwnerUserId();
                Integer newOwnerUserId = crmBusiness.getNewOwnerUserId();

                if (2 == crmBusiness.getTransferType()) {
                    if (1 == crmBusiness.getPower()) {
                        crmBusiness.setRoUserId(oldBusiness.getRoUserId() + oldBusiness.getOwnerUserId() + ",");
                    }
                    if (2 == crmBusiness.getPower()) {
                        crmBusiness.setRwUserId(oldBusiness.getRwUserId() + oldBusiness.getOwnerUserId() + ",");
                    }
                }
                crmBusiness.setBusinessId(Integer.valueOf(businessId).longValue());
                crmBusiness.setOwnerUserId(crmBusiness.getNewOwnerUserId());
                crmBusiness.update();
                crmRecordService.addConversionRecord(Integer.valueOf(businessId), CrmEnum.BUSINESS_TYPE_KEY.getTypes(), crmBusiness.getNewOwnerUserId());

                //商机负责人发生了变更,记录变更日志
                if (!Objects.equals(oldOwnerUserId, newOwnerUserId)) {
                    crmChangeLogService.saveBusinessChangeLog(CrmBusinessChangeLogEnum.BD.getCode(),Long.valueOf(businessId),Long.valueOf(newOwnerUserId),null,BaseUtil.getUserId());
                }
            }
            return true;
        }) ? R.ok() : R.error();
    }

    /**
     * @author wyq
     * 查询团队成员
     */
    public List<Record> getMembers(Integer businessId) {
        CrmBusiness crmBusiness = CrmBusiness.dao.findById(businessId);
        List<Record> recordList = new ArrayList<>(10);
        if (crmBusiness.getOwnerUserId() != null) {
            Record ownerUser = Db.findFirst(Db.getSql("crm.customer.getMembers"), crmBusiness.getOwnerUserId());
            recordList.add(ownerUser.set("power", "负责人权限").set("groupRole", "负责人"));
        }
        String roUserId = crmBusiness.getRoUserId();
        String rwUserId = crmBusiness.getRwUserId();
        String memberIds = roUserId + rwUserId.substring(1);
        if (",".equals(memberIds)) {
            return recordList;
        }
        String[] memberIdsArr = memberIds.substring(1, memberIds.length() - 1).split(",");
        Set<String> memberIdsSet = new HashSet<>(Arrays.asList(memberIdsArr));
        for (String memberId : memberIdsSet) {
            Record record = Db.findFirst(Db.getSql("crm.customer.getMembers"), memberId);
            if (roUserId.contains(memberId)) {
                record.set("power", "只读").set("groupRole", "普通成员");
            }
            if (rwUserId.contains(memberId)) {
                record.set("power", "读写").set("groupRole", "普通成员");
            }
            recordList.add(record);
        }
        return recordList;
    }

    /**
     * @author wyq
     * 添加团队成员
     */
    @Before(Tx.class)
    public R addMember(CrmBusiness crmBusiness) {
        String[] businessIdsArr = crmBusiness.getIds().split(",");
        String[] memberArr = crmBusiness.getMemberIds().split(",");
        StringBuilder stringBuffer = new StringBuilder();
        for (String id : businessIdsArr) {
            if (StrUtil.isNotEmpty(id)) {
                Integer ownerUserId = CrmBusiness.dao.findById(Integer.valueOf(id)).getOwnerUserId();
                Record mebers = Db.findFirst(Db.getSql("crm.business.queryMember"), id);
                String[] userIds = (mebers.get("ro_user_id") + "," + mebers.get("rw_user_id")).split(",");

                if (crmBusiness.getBusinessIds() == null) {
                    //判断团队成员是否存在
                    for (String memberId : memberArr) {
                        if (ownerUserId.equals(Integer.valueOf(memberId))) {
                            return R.error("负责人不能重复选为团队成员");
                        }
                        for (String userId : userIds) {
                            if (memberId.equals(userId)) {
                                Record user = Db.findFirst(Db.getSql("admin.user.queryUserFromAdminUserByUserId"), memberId);
                                String realName = user.get("realname") + "";
                                return R.error("不能重复选为团队成员:" + realName);
                            }
                        }
                        Db.update(Db.getSql("crm.business.deleteMember"), "," + memberId + ",", "," + memberId + ",", Integer.valueOf(id));
                    }
                }
                if (1 == crmBusiness.getPower()) {
                    stringBuffer.setLength(0);
                    String roUserId = stringBuffer.append(CrmBusiness.dao.findById(Integer.valueOf(id)).getRoUserId()).append(crmBusiness.getMemberIds()).append(',').toString();
                    Db.update("update 72crm_crm_business set ro_user_id = ? where business_id = ?", roUserId, Integer.valueOf(id));
                }
                if (2 == crmBusiness.getPower()) {
                    stringBuffer.setLength(0);
                    String rwUserId = stringBuffer.append(CrmBusiness.dao.findById(Integer.valueOf(id)).getRwUserId()).append(crmBusiness.getMemberIds()).append(',').toString();
                    Db.update("update 72crm_crm_business set rw_user_id = ? where business_id = ?", rwUserId, Integer.valueOf(id));
                }
            }

        }
        return R.ok();
    }

    /**
     * @author wyq
     * 删除团队成员
     */
    public R deleteMembers(CrmBusiness crmBusiness) {
        String[] businessIdsArr = crmBusiness.getIds().split(",");
        String[] memberArr = crmBusiness.getMemberIds().split(",");
        return Db.tx(() -> {
            for (String id : businessIdsArr) {
                for (String memberId : memberArr) {
                    Db.update(Db.getSql("crm.business.deleteMember"), "," + memberId + ",", "," + memberId + ",", Integer.valueOf(id));
                }
            }
            return true;
        }) ? R.ok() : R.error();
    }

    /**
     * @author wyq
     * 商机状态组展示
     */
    public List<Record> queryBusinessStatus(Integer businessId) {
        return Db.find(Db.getSql("crm.business.queryBusinessStatus"), businessId);
    }

    /**
     * 查询商机组阶段
     * @param businessId
     * @return
     */
    public List<Record> queryBusinessStatusNew(Long businessId) {
        CrmBusiness business = CrmBusiness.dao.findById(businessId);
        if (Objects.isNull(business)) {
            throw new CrmException("商机数据不存在");
        }
        final Integer currentStatusId = business.getStatusId();
        return Db.find(Db.getSql("crm.business.status.queryBusinessStatus"), business.getDeptId())
                .stream().filter(record -> {
                    Long statusId = record.getLong("status_id");
                    List<Record> verificationRecords = crmBusinessStatusVerificationService.listByStatusIdOfBusiness(businessId, statusId);
                    record.set("verificationList", verificationRecords);

                    //商机组阶段没有被封存
                    boolean stateFlag = Objects.equals(BaseConstant.OpenState.OPEN, record.getInt("opened"));
                    //处于当前阶段
                    boolean currentStatus = Objects.equals(currentStatusId.longValue(), statusId);
                    //存在可验证结果或者销售活动数据
                    boolean dataFlag = CollectionUtils.isNotEmpty(verificationRecords) || crmBusinessStatusSalesActivityService.countRecordByStatusIdOfBusiness(businessId, statusId) > 0;
                    if (currentStatus) {
                        //当前阶段需要设置商机表的可能性字段
                        record.set("rate", business.getStageWinRate());
                    }
                    return stateFlag || currentStatus || dataFlag;
                }).collect(Collectors.toList());
    }

    /**
     * @author wyq
     * 商机状态组推进
     */
    @Before(Tx.class)
    public R boostBusinessStatus(CrmBusiness crmBusiness) {
        // 参数校验
        if(Objects.isNull(crmBusiness.getBusinessId())) {
            return R.error("商机id不能为空");
        }
        if(Objects.isNull(crmBusiness.getIsEnd())) {
            return R.error("商机状态不能为空");
        }
        // 只在赢单和输单校验
        if(CrmBusinessEndEnum.WIN_TYPE_KEY.getTypes().equals(crmBusiness.getIsEnd()) || CrmBusinessEndEnum.LOSE_TYPE_KEY.getTypes().equals(crmBusiness.getIsEnd())) {
            if(Objects.isNull(crmBusiness.getRemark())) {
                return R.error("商机备注不能为空");
            }
        }

        return Db.tx(() -> {
            // 记录操作日志内容
            String content;
            if(CrmBusinessEndEnum.WIN_TYPE_KEY.getTypes().equals(crmBusiness.getIsEnd())) {
                content = "将结束标记为赢单";
            }else if(CrmBusinessEndEnum.LOSE_TYPE_KEY.getTypes().equals(crmBusiness.getIsEnd())) {
                content = "将结束标记为输单";
            }else{
                CrmBusiness searchBusiness = CrmBusiness.dao.findById(crmBusiness.getBusinessId());
                if(CrmConstant.CURRENT.equals(crmBusiness.getStageType())) {
                    content = String.format("将%s标记为当前阶段", getStateNameByStatusId(crmBusiness.getStatusId()));
                }else {
                    content = String.format("将%s标记为完成", getStateNameByStatusId(searchBusiness.getStatusId()));
                }
            }
            // 保存操作日志
            CrmActionRecord crmActionRecord = new CrmActionRecord(Objects.nonNull(BaseUtil.getUserId()) ? BaseUtil.getUserId().intValue() : null
                    , CrmEnum.BUSINESS_TYPE_KEY.getTypes(), crmBusiness.getBusinessId().intValue(), content);
            crmRecordService.addCrmActionRecord(crmActionRecord);
            if (Objects.nonNull(crmBusiness.getStatusId())) {
                CrmBusinessChange change = new CrmBusinessChange();
                change.setBusinessId(crmBusiness.getBusinessId().intValue());
                change.setStatusId(crmBusiness.getStatusId());
                change.setCreateTime(DateUtil.date());
                change.setCreateUserId(Objects.nonNull(BaseUtil.getUserId()) ? BaseUtil.getUserId().intValue() : null);
                change.save();
                // 查询商机阶段赢率
                CrmBusinessStatus crmBusinessStatus = CrmBusinessStatus.dao.findFirst(Db.getSql("crm.business.queryStageWinRateByBusinessId"), crmBusiness.getBusinessId(), crmBusiness.getStatusId());
                if(Objects.nonNull(crmBusinessStatus)) {
                    String rate = crmBusinessStatus.getRate();
                    crmBusiness.setStageWinRate(StringUtils.isNotEmpty(rate) ? Integer.valueOf(rate) : null);
                }
            }
            crmBusiness.update();
            return true;
        }) ? R.ok() : R.error();
    }

    /**
     * @author wyq
     * 查询新增字段
     */
    public List<Record> queryField() {
        List<Record> fieldList = new LinkedList<>();
        String[] settingArr = new String[]{};
        fieldUtil.getFixedField(fieldList, "businessName", "商机名称", "", "text", settingArr, 1);
        fieldUtil.getFixedField(fieldList, "customerId", "客户名称", "", "customer", settingArr, 1);
        fieldUtil.getFixedField(fieldList, "deptId", "部门名称", "", "recommendBusiness", settingArr, 1);
        fieldUtil.getFixedField(fieldList, "statusId", "商机阶段", "", "business_status", settingArr, 1);
        fieldUtil.getFixedField(fieldList, "applicationScenario", "应用场景", "", "application_scenario", settingArr, 1);
        fieldUtil.getFixedField(fieldList, "partner", "合作伙伴(合同甲方)", "", "text", settingArr, 0);
        fieldUtil.getFixedField(fieldList, "dealDate", "预计成交日期", "", "date", settingArr, 1);
        fieldUtil.getFixedField(fieldList, "project", "项目", "", "text", settingArr, 0);
        Record map = new Record();
        fieldList.add(map.set("field_name", "mapAddress")
                .set("name", "区域")
                .set("form_type", "map_address")
                .set("fieldType", 1)
                .set("is_null", 0));
        fieldUtil.getFixedField(fieldList, "ascription", "归属", "", "ascription", settingArr, 0);
        fieldUtil.getFixedField(fieldList, "annualProduction", "年产量", "", "annual_production", settingArr, 0);
        fieldUtil.getFixedField(fieldList, "fixedTime", "定点时间", "", "date", settingArr, 0);
        fieldUtil.getFixedField(fieldList, "pruductTime", "量产时间", "", "date", settingArr, 0);
        fieldUtil.getFixedField(fieldList, "remark", "备注", "", "textarea", settingArr, 0);
        fieldUtil.getFixedField(fieldList, "product", "商品", Kv.by("discount_rate", "").set("product", new ArrayList<>()).set("total_price", ""), "product", settingArr, 1);
        fieldList.addAll(adminFieldService.list("5"));
        return fieldList;
    }

    /**
     * @author wyq
     * 查询编辑字段
     */
    public List<Record> queryField(Integer businessId) {
        List<Record> fieldList = new LinkedList<>();
        Record record = Db.findFirst("select * from businessview where business_id = ?", businessId);
        String[] settingArr = new String[]{};
        fieldUtil.getFixedField(fieldList, "business_name", "商机名称", record.getStr("business_name"), "text", settingArr, 1);
        List<Record> customerList = new ArrayList<>();
        Record customer = new Record();
        customerList.add(customer.set("customer_id", record.getInt("customer_id")).set("customer_name", record.getStr("customer_name")));
        fieldUtil.getFixedField(fieldList, "customerId", "客户名称", customerList, "customer", settingArr, 1);
        fieldUtil.getFixedField(fieldList, "deptId", "部门名称", record.getInt("dept_id"), "recommendBusiness", settingArr, 1);
        fieldUtil.getFixedField(fieldList, "statusId", "商机阶段", record.getInt("status_id"), "business_status", settingArr, 1);
        fieldUtil.getFixedField(fieldList, "applicationScenario", "应用场景", record.getInt("application_scenario_id"), "application_scenario", settingArr, 1);
        fieldUtil.getFixedField(fieldList, "partner", "合作伙伴(合同甲方)", record.getStr("partner"), "text", settingArr, 0);
        fieldUtil.getFixedField(fieldList, "dealDate", "预计成交日期", DateUtil.formatDateTime(record.get("deal_date")), "date", settingArr, 1);
        fieldUtil.getFixedField(fieldList, "project", "项目", record.getStr("project"), "text", settingArr, 0);
        Record map = new Record();
        fieldList.add(map.set("fieldName", "mapAddress")
                .set("name", "区域")
                .set("value", Kv.by("location", "")
                        .set("address", record.getStr("mapAddress"))
                        .set("detailAddress", "")
                        .set("lng", "")
                        .set("lat", ""))
                .set("formType", "map_address")
                .set("isNull", 0));
        fieldUtil.getFixedField(fieldList, "ascription", "归属", record.getStr("ascription"), "ascription", settingArr, 0);
        fieldUtil.getFixedField(fieldList, "annualProduction", "年产量", record.getStr("annualProduction"), "annual_production", settingArr, 0);
        fieldUtil.getFixedField(fieldList, "fixedTime", "定点时间", record.getStr("fixedTime"), "date", settingArr, 0);
        fieldUtil.getFixedField(fieldList, "pruductTime", "量产时间", record.getStr("pruductTime"), "date", settingArr, 0);
        fieldUtil.getFixedField(fieldList, "remark", "备注", record.getStr("remark"), "textarea", settingArr, 0);
        fieldList.addAll(adminFieldService.queryByBatchId(record.getStr("batch_id")));
        Record totalPrice = Db.findFirst("select IFNULL(SUM(sales_price),0) as total_price from 72crm_crm_business_product where business_id = ?", businessId);
        List<Record> productList = Db.find(Db.getSql("crm.business.queryBusinessProduct"), businessId);
        Kv kv = Kv.by("product", productList).set("total_price", totalPrice.getStr("total_price"));
        fieldUtil.getFixedField(fieldList, "product", "商品", kv, "product", settingArr, 1);

        return fieldList;
    }

    /**
     * @author wyq
     * 查询商机状态组及商机状态
     */
    public List<Record> queryBusinessStatusOptions(String type) {
        List<Record> businessTypeList = Db.find("select * from 72crm_crm_business_group where status = 1 and is_deleted = 0");
        for (Record record : businessTypeList) {
            Integer typeId = record.getInt("id");
            record.set("typeId", typeId);
            List<Record> businessStatusList = Db.find("select * from 72crm_crm_business_status where type_id = ?", typeId);
            if ("condition".equals(type)) {
                Record win = new Record();
                win.set("name", "赢单").set("typeId", typeId).set("statusId", "win");
                businessStatusList.add(win);
                Record lose = new Record();
                lose.set("name", "输单").set("typeId", typeId).set("statusId", "lose");
                businessStatusList.add(lose);
                Record invalid = new Record();
                invalid.set("name", "无效").set("typeId", typeId).set("statusId", "invalid");
                businessStatusList.add(invalid);
            }
            record.set("statusList", businessStatusList);
        }
        return businessTypeList;
    }

    /**
     * 根据根据部门 Id获取商机阶段
     *
     * @param deptId
     * @param businessId
     */
    public List<Record> getStatysById(String deptId, Long businessId) {
        Long currentStatusId = null;
        if (Objects.nonNull(businessId)) {
            CrmBusiness crmBusiness = CrmBusiness.dao.findById(businessId);
            currentStatusId = Long.valueOf(crmBusiness.getStatusId());
        }
        final Long finalCurrentStatusId = currentStatusId;
        return Db.find(Db.getSql("crm.business.getStatysById"), deptId).stream()
                .filter(record ->
                        Objects.equals(CrmBusinessStatusOpenEnum.OPEN.getStatus(), record.getInt("opened"))
                                || Objects.equals(finalCurrentStatusId, record.getLong("status_id"))).collect(Collectors.toList());
    }

    /**
     * 根据商机查询回款计划
     */
    public R qureyListByBusinessId(Integer businessId, String contractIds,Integer pageType,String hasReleativeContract, String checkStatus) {

        List<String> contractIdList = new ArrayList();
        if (StringUtils.isNotBlank(contractIds)) {
        	contractIdList.addAll(Arrays.asList(contractIds.split(",")));
        }
        BusinessReceivablePlanResult result = new BusinessReceivablePlanResult();
        if (pageType == null || CrmConstant.DISPLAY_NO_PAGING.intValue() == pageType) {
        	Kv kv = Kv.by("businessId", businessId).set("hasReleativeContract", hasReleativeContract).set("contractIdList", contractIdList);
    		if (StringUtils.isNotEmpty(checkStatus) && checkStatus.split(",").length>0) {
    			ArrayList< String> arrayList = new ArrayList<String>(checkStatus.split(",").length);
    			Collections.addAll(arrayList, checkStatus.split(","));
    			kv.set("checkStatus", arrayList);
    		}
            List<Record> resultList = Db.find(Db.getSqlPara("crm.business.queryListByBusinessId", kv));
            if (CollectionUtils.isNotEmpty(resultList)) {

            	/*
            	 * 根据履约接口填充字段
			     * 最新回款日期
			     * 待回款金额
			     * 已回款金额
            	 */
            	this.replenishBilling(resultList);

            	/*
            	 * 计算已关联/未关联合同的计划回款金额
            	 */
            	List<Record> resultListSum = Db.find(Db.getSqlPara("crm.business.queryListByBusinessId", Kv.by("businessId", businessId).set("contractIdList", contractIdList)));
            	// 已关联合同回款金额
            	result.setReleativeContractMoney(resultListSum.stream().filter(record -> Objects.nonNull(record.getInt("contractId")))
            			.map(record -> Objects.nonNull(record.getBigDecimal("money")) ? record.getBigDecimal("money") : BigDecimal.ZERO).reduce(BigDecimal.ZERO,BigDecimal::add));
            	//未关联合同回款金额
            	result.setNotReleativeContractMoney(resultListSum.stream().filter(record -> Objects.isNull(record.getInt("contractId")))
            			.map(record -> Objects.nonNull(record.getBigDecimal("money")) ? record.getBigDecimal("money") : BigDecimal.ZERO).reduce(BigDecimal.ZERO,BigDecimal::add));
                result.setReceivablePlans(resultList);

                result.setPlanTotalMoney(resultList.stream().map(item -> Objects.nonNull(item.getBigDecimal("money")) ? item.getBigDecimal("money") : BigDecimal.ZERO).reduce(BigDecimal::add).orElse(BigDecimal.ZERO));

                Record r = Db.findFirst(Db.getSql("crm.business.queryFromBusinessTableById"), businessId);
                BigDecimal money = Objects.nonNull(r) ? (r.getBigDecimal("money") == null ? new BigDecimal(0) : r.getBigDecimal("money")) : BigDecimal.ZERO;
                BigDecimal planTotalMoney = result.getPlanTotalMoney() == null ? new BigDecimal(0) : result.getPlanTotalMoney();
                result.setBusinessBalance(money.subtract(planTotalMoney).abs());
            }
            return R.ok().put("data", result);
        }

        return R.ok().put("data", null);
    }

    /**
     * 根据履约接口填充字段
     * 最新回款日期
     * 待回款金额
     * 已回款金额
     */
    private List<Record> replenishBilling (List<Record> resultList) {

    	List<String> planNoList = new ArrayList<>();
    	resultList.forEach(record -> {
    		String paymentCode = record.getStr("paymentCode");
    		if(StringUtils.isNoneEmpty(paymentCode)) {
        		planNoList.add(paymentCode);
    		}
    	});
    	if (CollectionUtils.isNotEmpty(planNoList)) {

        	List<PerformancePlanDto> performancePlanDtos = new ArrayList<>();
			try {
				performancePlanDtos = performanceClient.listInstallmentBill(null, planNoList.toArray(new String[planNoList.size()]));
			} catch (Exception e) {
				// 履约接口异常时，降级处理，只返回crm信息
				log.error("获取履约回款计划列表数据失败 -> performanceClient -> listInstallmentBil->输入参数：{}",JSON.toJSONString(planNoList) ,e);
				// 告警处理 TODO
			}

			for (Record record : resultList) {
				if (CollectionUtils.isNotEmpty(performancePlanDtos)) {
					for (PerformancePlanDto performancePlanDto : performancePlanDtos) {
	        			if (StringUtils.isNotEmpty(performancePlanDto.getBizNo()) && performancePlanDto.getBizNo().equals(record.getStr("paymentCode"))) {

	        				// 最后还款日期
	                		record.set("latestPaymentDate", performancePlanDto.getLastPayTime());
	                		// 已还款金额
	                		record.set("alreadlyPaymentMoney", performancePlanDto.getAccumulateAmount());
	                		// 待回款金额
	                		record.set("pendingPaymentMoney", performancePlanDto.getWaitAmount());
	        			}
	        		}
				}

        	}

			// 补全响应报文
			resultList.forEach(record -> {
				// 最后还款日期
				if (StringUtils.isEmpty(record.getStr("latestPaymentDate"))) {
	        		record.set("latestPaymentDate", "");
				}
        		// 已还款金额
				if (StringUtils.isEmpty(record.getStr("alreadlyPaymentMoney"))) {
	        		record.set("alreadlyPaymentMoney", "");
				}
        		// 待回款金额
				if (StringUtils.isEmpty(record.getStr("pendingPaymentMoney"))) {
	        		record.set("pendingPaymentMoney", "");
				}
			});

    	}
    	return resultList;
    }

    /**
     * 根据商机id查询回款
     */
    public R qureyReceivableListByBusinessId(BasePageRequest<CrmReceivables> basePageRequest) {
        Integer pageType = basePageRequest.getPageType();
        if (0 == pageType) {
            return R.ok().put("data", Db.find(Db.getSql("crm.business.queryReceivablesPageList"), basePageRequest.getData().getBusinessId()));
        } else {
            return R.ok().put("data", Db.paginate(basePageRequest.getPage(), basePageRequest.getLimit(), new SqlPara().setSql(Db.getSql("crm.business.queryReceivablesPageList")).addPara(basePageRequest.getData().getBusinessId())));
        }
    }

    /**
     * 根据商机id查询回款
     */
    public R qureyReceivableListByBusinessIdNoView(BasePageRequest<CrmReceivables> basePageRequest) {
        Integer pageType = basePageRequest.getPageType();
        CrmReceivablesListByBusinessIdNoViewResult result = new CrmReceivablesListByBusinessIdNoViewResult();
        if (0 == pageType) {
            List<Record> records = Db.find(Db.getSqlPara("crm.business.queryReceivablesPageListNoView", Kv.by("businessId",
            		basePageRequest.getData().getBusinessId()).set("receivablesId", basePageRequest.getData().getReceivablesId()).set("noContract","1")));

            records.forEach(record -> record.set("payType", record.getStr("payType") == null ? "" : formatePayType(Integer.valueOf(record.getStr("payType")))));
            result.setReceivables(records);
            result.setTotalMoney(records.stream().map(record -> record.getBigDecimal("receivablesMoney") == null ?
                    BigDecimal.ZERO : record.getBigDecimal("receivablesMoney"))
                    .reduce(BigDecimal.ZERO, BigDecimal::add).toString());

            List<Record> resultList = Db.find(Db.getSqlPara("crm.business.queryListByBusinessId", Kv.by("businessId", basePageRequest.getData().getBusinessId())));
            BigDecimal receivablesPlanMoneySum = BigDecimal.ZERO;
            if (CollectionUtils.isNotEmpty(resultList)) {
                receivablesPlanMoneySum = resultList.stream().map(item -> item.getBigDecimal("money")).reduce(BigDecimal::add).orElse(BigDecimal.ZERO);
            }
            result.setBusinessBalance(receivablesPlanMoneySum.subtract(new BigDecimal(result.getTotalMoney())).abs().toString());
            return R.ok().put("data", result);
        } else {
            return R.ok().put("data", Db.paginate(basePageRequest.getPage(), basePageRequest.getLimit(), new SqlPara().setSql(Db.getSql("crm.business.queryReceivablesPageListNoView")).addPara(basePageRequest.getData().getBusinessId()).addPara(basePageRequest.getData().getReceivablesId())));
        }
    }

    /**
     * 支付方式转码
     *
     * @param payTypeCode
     * @return
     */
    private String formatePayType(Integer payTypeCode) {

        return PaymentTypeEnum.getPaymentTypeNameInCrm(PaymentTypeEnum.getPaymentTypeDefinedInCrm(payTypeCode));
    }

    /**
     * 根据商机id查询商机下的商品信息
     *
     * @param businessId 商机id
     * @author yue.li
     */
    public List<Record> queryProductsByBusinessId(Integer businessId) {
        return Db.find(Db.getSql("crm.business.queryProductsByBusinessId"), businessId);
    }

    /**
     * 根据商机id集合和指定的时间区间，查询对应商机的总回款
     *
     * @param businessIds business ids
     * @param start       start date
     * @param end         end date
     * @return {@code List} of {@code BusinessRevenueInfo}
     */
    public List<BusinessRevenueInfo> queryRevenueByBusinessIdInSpecifiedPeriod(List<Integer> businessIds, String start, String end) {
        if (CollectionUtils.isEmpty(businessIds)) {
            return Collections.emptyList();
        }
        log.info("queryRevenueByBusinessIdInSpecifiedPeriod -> businessIds: {}", StringUtils.join(businessIds,","));
        List<Record> records = Db.find(Db.getSqlPara("crm.business.queryRevenueByBusinessIdInSpecifiedPeriod", Kv.by("startDate", start).set("endDate", end).set("ids", businessIds)));
        if (CollectionUtils.isNotEmpty(records)) {
            List<Map<String, Object>> recordsMap = records.stream().map(Record::getColumns).collect(Collectors.toList());
            return recordsMap.stream().map(item -> JSON.parseObject(JsonKit.toJson(item), BusinessRevenueInfo.class)).collect(Collectors.toList());
        } else {
            return Collections.emptyList();
        }
    }

    /**
     * 获取商机下所有商品类别的售价
     *
     * @param businessIds business ids
     * @return {@code List} of {@code BusinessCategoryPrice}
     */
    public List<BusinessCategoryPrice> queryCategoryPriceByBusinessId(List<Integer> businessIds) {
        if (CollectionUtils.isEmpty(businessIds)) {
            return Collections.emptyList();
        }
        List<Record> bizPriceCatRecords = Db.find(Db.getSqlPara("crm.business.queryCategoryPriceByBusinessId", Kv.by("ids", businessIds)));
        if (CollectionUtils.isNotEmpty(bizPriceCatRecords)) {
            List<Map<String, Object>> recordsMap = bizPriceCatRecords.stream().map(Record::getColumns).collect(Collectors.toList());
            return recordsMap.stream().map(item -> JSON.parseObject(JsonKit.toJson(item), BusinessCategoryPrice.class)).collect(Collectors.toList());
        } else {
            return Collections.emptyList();
        }
    }

    /**
     * 获取给定商机下各商品类别的售价百分比统计
     *
     * @param businessIds business ids
     * @return key is business id, value is {@code BusinessCategoryPriceStatistic}
     */
    public Map<Long, BusinessCategoryPriceStatistic> getCategoryPriceStatisticByBizId(List<Integer> businessIds) {
        if (CollectionUtils.isEmpty(businessIds)) {
            return Collections.emptyMap();
        }
        // key is business id, value is BusinessCategoryPriceStatistic
        Map<Long, BusinessCategoryPriceStatistic> results = Maps.newHashMap();
        // query category price by business id
        List<BusinessCategoryPrice> bizPriceCatList = queryCategoryPriceByBusinessId(businessIds);

        if (CollectionUtils.isNotEmpty(bizPriceCatList)) {
            bizPriceCatList.forEach(item -> {
                Long bizId = item.getBusinessId().longValue();
                BusinessCategoryPriceStatistic statistic = results.get(bizId);
                if (statistic != null) {
                    statistic.getCategoryPriceList().add(item);
                } else {
                    statistic = new BusinessCategoryPriceStatistic();
                    statistic.setBusinessId(bizId);
                    statistic.getCategoryPriceList().add(item);
                    results.put(bizId, statistic);
                }
            });

            // calculate percentage
            results.values().forEach(BusinessCategoryPriceStatistic::calculatePercentage);
        }
        return results;
    }

    /**
     * 获取商机下所有商品的售价
     *
     * @param businessIds business ids
     * @return {@code List} of {@code BusinessProductPrice}
     */
    public List<BusinessProductPrice> queryProductPriceStatisticByBizId(List<Integer> businessIds) {
        if (CollectionUtils.isEmpty(businessIds)) {
            return Collections.emptyList();
        }
        List<Record> prodPriceRecords = Db.find(Db.getSqlPara("crm.business.queryProductPriceForSpecifiedBusiness", Kv.by("ids", businessIds)));
        if (CollectionUtils.isNotEmpty(prodPriceRecords)) {
            List<Map<String, Object>> recordsMap = prodPriceRecords.stream().map(Record::getColumns).collect(Collectors.toList());
            return recordsMap.stream().map(item -> JSON.parseObject(JsonKit.toJson(item), BusinessProductPrice.class)).collect(Collectors.toList());
        } else {
            return Collections.emptyList();
        }
    }

    /**
     * 获取给定商机下各商品的售价统计
     * 如果要获取某商品在某次回款中的占比，需要单独调用{@code BusinessProductPriceStatistic}中的方法进行计算
     *
     * @param businessIds business ids
     * @return key is business id, value is {@code BusinessProductPriceStatistic}
     */
    public Map<Long, BusinessProductPriceStatistic> getProductPriceStatisticByBizId(List<Integer> businessIds) {
        if (CollectionUtils.isEmpty(businessIds)) {
            return Collections.emptyMap();
        }
        // key is business id, value is BusinessProductPriceStatistic
        Map<Long, BusinessProductPriceStatistic> results = Maps.newHashMap();
        // query product price by business id
        List<BusinessProductPrice> bizPriceProdList = queryProductPriceStatisticByBizId(businessIds);

        if (CollectionUtils.isNotEmpty(bizPriceProdList)) {
            bizPriceProdList.forEach(item -> {
                Long bizId = item.getBusinessId().longValue();
                BusinessProductPriceStatistic statistic = results.get(bizId);
                if (statistic != null) {
                    statistic.getProductPriceList().add(item);
                } else {
                    statistic = new BusinessProductPriceStatistic();
                    statistic.setBusinessId(bizId);
                    statistic.getProductPriceList().add(item);
                    results.put(bizId, statistic);
                }
            });
        }
        return results;
    }

    /**
     * 获取指定商机下所有回款信息
     *
     * @return {@code List} of {@code BusinessReceivableInfo}
     */
    public List<BusinessReceivableInfo> queryReceivablesForSpecifiedBusiness(List<Integer> businessIds, String start, String end) {
        if (CollectionUtils.isEmpty(businessIds)) {
            return Collections.emptyList();
        }
        List<Record> records = Db.find(Db.getSqlPara("crm.business.queryReceivablesForSpecifiedBusiness", Kv.by("startDate", start).set("endDate", end).set("ids", businessIds)));
        if (CollectionUtils.isNotEmpty(records)) {
            List<Map<String, Object>> recordsMap = records.stream().map(Record::getColumns).collect(Collectors.toList());
            return recordsMap.stream().map(item -> JSON.parseObject(JsonKit.toJson(item), BusinessReceivableInfo.class)).collect(Collectors.toList());
        } else {
            return Collections.emptyList();
        }
    }

    /**
     * 获取指定商机下所有回款的统计信息
     *
     * @param businessIds business ids
     * @param start       start date
     * @param end         end date
     * @return map of {@code BusinessReceivableInfoStatistic}, key is business id, value is {@code BusinessReceivableInfoStatistic}
     */
    public Map<Long, BusinessReceivableInfoStatistic> getReceivableInfoStatisticForSpecifiedBusiness(List<Integer> businessIds, String start, String end) {
        if (CollectionUtils.isEmpty(businessIds)) {
            return Collections.emptyMap();
        }
        Map<Long, BusinessReceivableInfoStatistic> results = Maps.newHashMap();
        List<BusinessReceivableInfo> receivableInfos = queryReceivablesForSpecifiedBusiness(businessIds, start, end);
        if (CollectionUtils.isNotEmpty(receivableInfos)) {
            receivableInfos.forEach(item -> {
                Long bizId = item.getBusinessId().longValue();
                BusinessReceivableInfoStatistic statistic = results.get(bizId);
                if (statistic != null) {
                    statistic.getReceivableInfoList().add(item);
                } else {
                    statistic = new BusinessReceivableInfoStatistic();
                    statistic.setBusinessId(bizId);
                    statistic.getReceivableInfoList().add(item);
                    results.put(bizId, statistic);
                }
            });
        }
        return results;
    }

    /**
     * Return business revenue statistic information for specified businesses in a duration.
     *
     * @param businessIds business ids
     * @param start       start date
     * @param end         end date
     * @return map of {@code BusinessRevenueInfo}, key is business id, value is {@code BusinessRevenueInfo}
     */
    public Map<Long, BusinessRevenueInfo> getBusinessRevenueStatisticInfo(List<Integer> businessIds, String start, String end) {
        if (CollectionUtils.isEmpty(businessIds)) {
            return Collections.emptyMap();
        }
        // total revenue for the business in specified period
        List<BusinessRevenueInfo> revenueInfoList = queryRevenueByBusinessIdInSpecifiedPeriod(businessIds, start, end);
        Map<Long, BusinessRevenueInfo> results = revenueInfoList.stream().collect(Collectors.toMap(item -> item.getBusinessId().longValue(), Function.identity()));

        // receivables for the business in specified period
        getReceivableInfoStatisticForSpecifiedBusiness(businessIds, start, end)
                .forEach((k, v) -> results.get(k).setReceivableInfoStatistic(v));

        // get category percentage map in business level
        getCategoryPriceStatisticByBizId(businessIds).forEach((bizId, v) -> {
            BusinessRevenueInfo businessRevenueInfo = results.get(bizId);
            if (businessRevenueInfo != null) {
                businessRevenueInfo.setCategoryPercentageMap4BizLevel(v.getCategoryPricePercentageMap());
                businessRevenueInfo.setCatIdCodeMap(v.getCategoryPriceList().stream().collect(Collectors.toMap(BusinessCategoryPrice::getCategoryId, BusinessCategoryPrice::getCategoryCode)));
            }
        });

        // get product price statistic
        getProductPriceStatisticByBizId(businessIds).forEach((bizId, v) -> {
            BusinessRevenueInfo businessRevenueInfo = results.get(bizId);
            if (businessRevenueInfo != null) {
                businessRevenueInfo.setProductPriceStatistic(v);
                businessRevenueInfo.setProdIdNameMap(v.getProductPriceList().stream().collect(Collectors.toMap(BusinessProductPrice::getProductId, BusinessProductPrice::getProductName)));
            }
        });

        // calculate revenue details separately
        results.values().forEach(BusinessRevenueInfo::calculateRevenue);

        return results;
    }

    /**
     * Customer Revenue Statistic
     *
     * @param businessRevenueInfoMap business level revenue statistic map
     * @return {@code CustomerRevenueStatistic}
     */
    public CustomerRevenueStatistic calculateCustomerRevenue(Map<Long, BusinessRevenueInfo> businessRevenueInfoMap) {
        if (businessRevenueInfoMap == null || businessRevenueInfoMap.size() == 0) {
            return null;
        }

        // filter business which does not have any product
        Map<Long, BusinessRevenueInfo> targetMap = Maps.newHashMap();
        businessRevenueInfoMap.keySet().forEach(key -> {
            BusinessRevenueInfo bizInfo = businessRevenueInfoMap.get(key);
            if (bizInfo.getTotalRevenue() != null && bizInfo.getTotalRevenue().intValue() != 0) {
                targetMap.put(key, bizInfo);
            }
        });

        CustomerRevenueStatistic customerRevenueStatistic = new CustomerRevenueStatistic();
        BigDecimal totalRevenue = targetMap.values().stream().map(BusinessRevenueInfo::getTotalRevenue).reduce(BigDecimal.ZERO, BigDecimal::add);
        customerRevenueStatistic.setTotalRevenue(totalRevenue);

        // Map<CategoryId, CategoryCode>
        Map<Long, String> categoryIdCodeMap = Maps.newHashMap();
        // Map<ProductId, ProductName>
        Map<Long, String> productIdNameMap = Maps.newHashMap();
        // Map<CategoryId, CategoryRevenue>
        Map<Long, BigDecimal> categoryRevenueMap = Maps.newHashMap();
        // Map<CategoryId, Map<ProductId, ProductRevenue>>
        Map<Long, Map<Long, BigDecimal>> productRevenueMap = Maps.newHashMap();
        targetMap.values().forEach(bizInfo -> {
            calculateCategoryIdCodeMap(categoryIdCodeMap, bizInfo.getCatIdCodeMap());
            calculateCategoryIdCodeMap(productIdNameMap, bizInfo.getProdIdNameMap());
            calculateIdRevenueMap(categoryRevenueMap, bizInfo.getCategoryRevenueMap());
            calculateProductRevenueMap(productRevenueMap, bizInfo.getProductsIncomeByCategory());
        });
        customerRevenueStatistic.setCategoryRevenueMap(categoryRevenueMap);
        customerRevenueStatistic.setCategoryCodeRevenueMap(replaceCatIdWithCode(categoryRevenueMap, categoryIdCodeMap));
        customerRevenueStatistic.setProductRevenueMap(productRevenueMap);
        customerRevenueStatistic.setCategoryCodeProductRevenueMap(replaceProductIdWithName(replaceCatIdWithCode(productRevenueMap, categoryIdCodeMap), productIdNameMap));
        return customerRevenueStatistic;
    }

    private <T> Map<String, T> replaceCatIdWithCode(Map<Long, T> categoryRevenueMap, Map<Long, String> categoryIdCodeMap) {
        Map<String, T> result = Maps.newHashMap();
        categoryRevenueMap.keySet().forEach(key -> result.put(categoryIdCodeMap.get(key), categoryRevenueMap.get(key)));
        return result;
    }

    private Map<String, Map<String, BigDecimal>> replaceProductIdWithName(Map<String, Map<Long, BigDecimal>> target, Map<Long, String> productIdNameMap) {
        Map<String, Map<String, BigDecimal>> result = Maps.newHashMap();
        target.keySet().forEach(key -> result.put(key, replaceCatIdWithCode(target.get(key), productIdNameMap)));
        return result;
    }

    private void calculateCategoryIdCodeMap(Map<Long, String> targetMap, Map<Long, String> newValueMap) {
        if (newValueMap != null && newValueMap.size() > 0) {
            targetMap.putAll(newValueMap);
        }
    }

    private void calculateIdRevenueMap(Map<Long, BigDecimal> targetMap, Map<Long, BigDecimal> newValueMap) {
        if (newValueMap != null && newValueMap.size() > 0) {
            newValueMap.keySet().forEach(key -> updateRevenueMap(targetMap, key, newValueMap.get(key)));
        }
    }

    private void calculateProductRevenueMap(Map<Long, Map<Long, BigDecimal>> targetMap, Map<Long, Map<Long, BigDecimal>> newValueMap) {
        if (newValueMap != null && newValueMap.size() > 0) {
            newValueMap.keySet().forEach(catId -> {
                Map<Long, BigDecimal> targetValue = targetMap.get(catId) == null ? Maps.newHashMap() : targetMap.get(catId);
                for (Long prodId : newValueMap.get(catId).keySet()) {
                    updateRevenueMap(targetValue, prodId, newValueMap.get(catId).get(prodId));
                }
                targetMap.put(catId, targetValue);
            });
        }
    }

    private void updateRevenueMap(Map<Long, BigDecimal> map, Long catId, BigDecimal singleIncome) {
        BigDecimal oldValue = getDecimalValueFromMap(map, catId, singleIncome);
        map.put(catId, oldValue.add(singleIncome));
    }

    private BigDecimal getDecimalValueFromMap(Map<Long, BigDecimal> map, Long key, BigDecimal value) {
        BigDecimal existingValue = map.putIfAbsent(key, value);
        return Objects.isNull(existingValue) ? new BigDecimal(0) : existingValue;
    }

    public R deleteProductById(Integer id, String money, Integer businessId) {
        List<Record> list = Db.find(Db.getSql("crm.business.queryReceivablePlanProductByBusinessProductId"), id);
        if (list != null && list.size() > 0) {
            return R.error("已关联回款计划的商品不能删除");
        } else {
            Db.update(Db.getSql("crm.business.updateBusinessMoneyByBusinessId"), money, businessId);
            return Db.delete(Db.getSql("crm.business.deleteProductById"), id) > 0 ? R.ok() : R.error();
        }
    }

    /**
     * 新增或更新商机对应商品信息
     *
     * @param jsonObject json实体
     * @param userId     用户ID
     * @author yue.li
     */
    @Before(Tx.class)
    public R addOrUpdateBusinessProduct(JSONObject jsonObject, Long userId) {
        Integer businessId = jsonObject.getInteger("businessId");
        String money = jsonObject.getString("money");
        JSONArray products = jsonObject.getJSONArray("product");
        boolean saveOrUpdate = false;

        //处理商品大类 商品明细
        if (products != null) {
            List<CrmBusinessProduct> businessProductList = products.toJavaList(CrmBusinessProduct.class);
            CrmBusiness business = CrmBusiness.dao.findFirst(Db.getSql("crm.business.queryById"), businessId);
            Db.update(Db.getSql("crm.business.updateBusinessMoneyByBusinessId"), money, businessId);
            saveOrUpdate = saveBusinessProduct(businessProductList, business);
        }
        return saveOrUpdate ? R.ok() : R.error();
    }

    /***
     * 按照省份统计商品收入
     * @author yue.li
     * @param businessRevenueStatisticInfo 商机统计实体
     * @param recordList 商机和省份对应关系
     */
    public Map<String, Map<Long, BigDecimal>> provinceProductIncome(Map<Long, BusinessRevenueInfo> businessRevenueStatisticInfo, List<Record> recordList) {
        Map<String, Map<Long, BigDecimal>> map = new LinkedHashMap<>(1);
        Map<String, Map<Long, BigDecimal>> resultMap = new LinkedHashMap<>(1);
        List<Record> businessNoProductList = new ArrayList<>();
        if (businessRevenueStatisticInfo != null && recordList != null && recordList.size() > 0) {
            log.info("provinceProductIncome businessRevenueStatisticInfo : {}", businessRevenueStatisticInfo);
            log.info("provinceProductIncome recordList : {}", recordList);
            for (Map.Entry<Long, BusinessRevenueInfo> businessProductIncome : businessRevenueStatisticInfo.entrySet()) {
                Long businessId = businessProductIncome.getKey();
                BusinessRevenueInfo businessRevenueInfo = businessProductIncome.getValue();
                log.info("provinceProductIncome businessId : {}", businessId);
                Record record = getBusinessInfoByBusinessId(recordList, businessId);
                log.info("provinceProductIncome businessEntity : {}", record == null ? null : record.toString());
                if (record != null) {
                    if (StringUtils.isEmpty(record.getStr("address")) || CrmConstant.PROVINCE_CITY_AREA.equals(record.getStr("address"))) {
                        record.set("address", CrmConstant.NO_PROVINCE);
                    }
                    if (map.get(record.getStr("address")) == null) {
                        Map<Long, BigDecimal> productSumIncome = new HashMap<>();
                        map.put(record.getStr("address"), productSumIncome(businessRevenueInfo, productSumIncome));
                    } else {
                        Map<Long, BigDecimal> productSumIncome = map.get(record.getStr("address"));
                        map.put(record.getStr("address"), productSumIncome(businessRevenueInfo, productSumIncome));
                    }
                }
            }
        }
        //如果没有商机也要统计
        if (CollectionUtils.isNotEmpty(recordList)) {
            for (Record record : recordList) {
                if (CollectionUtils.isNotEmpty(businessNoProductList)) {
                    if (!businessNoProductList.contains(record.get("business_id"))) {
                        map = addNoProvince(map, record);
                    }
                } else {
                    map = addNoProvince(map, record);
                }
            }
        }
        for (Map.Entry<String, Map<Long, BigDecimal>> mapResult : map.entrySet()) {
            resultMap.put(mapResult.getKey(), mapResult.getValue());
        }
        return resultMap;
    }

    /***
     * 添加为空省份数据
     * @author yue.li
     * @param map 省份数据
     * @param record 商机数据
     */
    public Map<String, Map<Long, BigDecimal>> addNoProvince(Map<String, Map<Long, BigDecimal>> map, Record record) {
        if (StringUtils.isEmpty(record.getStr("address")) || CrmConstant.PROVINCE_CITY_AREA.equals(record.getStr("address"))) {
            record.set("address", CrmConstant.NO_PROVINCE);
        }
        if (map.get(record.get("address")) == null) {
            if (CrmConstant.NO_PROVINCE.equals(record.get("address"))) {
                map.put(CrmConstant.NO_PROVINCE, null);
            } else {
                map.put(record.get("address"), null);
            }
        }
        return map;
    }

    /***
     * 根据商机ID获取该商机对应的省份信息
     * @author yue.li
     * @param recordList 商机省份集合
     * @param businessId 商机ID
     */
    public Record getBusinessInfoByBusinessId(List<Record> recordList, Long businessId) {
        if (businessId != null) {
            for (Record record : recordList) {
                if (String.valueOf(businessId).equals(record.getStr("business_id"))) {
                    return record;
                }
            }
        }
        return null;
    }

    /***
     * 统计商品总收入
     * @author yue.li
     * @param  businessRevenueInfo 商机收入集合
     * @param  productSumIncome 商品总收入
     * @return
     */
    public Map<Long, BigDecimal> productSumIncome(BusinessRevenueInfo businessRevenueInfo, Map<Long, BigDecimal> productSumIncome) {
        if (businessRevenueInfo.getProductsIncomeByCategory() != null) {
            for (Map.Entry<Long, Map<Long, BigDecimal>> resultMap : businessRevenueInfo.getProductsIncomeByCategory().entrySet()) {
                Map<Long, BigDecimal> productIncomeMap = resultMap.getValue();
                for (Map.Entry<Long, BigDecimal> productIncome : productIncomeMap.entrySet()) {
                    if (productSumIncome.get(productIncome.getKey()) == null) {
                        productSumIncome.put(productIncome.getKey(), productIncome.getValue());
                    } else {
                        productSumIncome.put(productIncome.getKey(), productSumIncome.get(productIncome.getKey()).add(productIncome.getValue()));
                    }
                }
            }
        }
        return productSumIncome;
    }

    /***
     * 将商品ID转成商品名称
     * @author yue.li
     * @param businessRevenueStatisticInfo
     * @param map 按省份统计的商品ID结果集
     * @return
     */
    public Map<String, Map<String, BigDecimal>> productIdConvertToProductName(Map<Long, BusinessRevenueInfo> businessRevenueStatisticInfo, Map<String, Map<Long, BigDecimal>> map, String startTime, String endTime, List<Long> deptOwnUserIds, List<Long> longs) {
        Map<Long, String> productIdNameMap = new LinkedHashMap<>();
        Map<String, Map<String, BigDecimal>> resultMap = new LinkedHashMap<>(1);
        List<Record> provincePaymentIncomeList = queryProvinceUserPayment(startTime, endTime, deptOwnUserIds, CrmConstant.PAYMENT_STATUS_BIND_CUSTOMER, longs);
        for (Map.Entry<Long, BusinessRevenueInfo> businessProductIncome : businessRevenueStatisticInfo.entrySet()) {
            BusinessRevenueInfo businessRevenueInfo = businessProductIncome.getValue();
            for (Map.Entry<Long, String> prodIdName : businessRevenueInfo.getProdIdNameMap().entrySet()) {
                productIdNameMap.computeIfAbsent(prodIdName.getKey(), k -> prodIdName.getValue());
            }
        }
        for (Map.Entry<String, Map<Long, BigDecimal>> provinceMap : map.entrySet()) {
            Map<String, BigDecimal> productIncomeMap = getProductNameMapByProductIdMap(productIdNameMap, provinceMap.getValue(), provinceMap.getKey(), startTime, endTime, deptOwnUserIds, longs, provincePaymentIncomeList);
            resultMap.put(provinceMap.getKey(), productIncomeMap);
        }
        return resultMap;
    }

    /***
     * 将商品ID收入map转成商品名称map
     * @author yue.li
     * @param productIdNameMap 商品ID名称关系map
     * @param productIdIncomeMap 商品ID收入map
     * @param province 省份
     * @return
     */
    public Map<String, BigDecimal> getProductNameMapByProductIdMap(Map<Long, String> productIdNameMap, Map<Long, BigDecimal> productIdIncomeMap, String province, String startTime, String endTime, List<Long> deptOwnUserIds, List<Long> longs, List<Record> provincePaymentIncomeList) {
        Map<String, BigDecimal> productIdIncomeResultMap = new LinkedHashMap<>();
        BigDecimal relevanceTotalIncome = new BigDecimal(0);
        BigDecimal noRelevanceTotalIncome = new BigDecimal(0);
        if (productIdIncomeMap != null) {
            for (Map.Entry<Long, BigDecimal> productIdIncome : productIdIncomeMap.entrySet()) {
                for (Map.Entry<Long, String> productIdName : productIdNameMap.entrySet()) {
                    if (Objects.nonNull(productIdName.getKey()) && Objects.nonNull(productIdIncome.getKey()) && productIdName.getKey().equals(productIdIncome.getKey())) {
                        productIdIncomeResultMap.put(productIdName.getValue(), productIdIncome.getValue());
                    }
                }
            }
            for (Map.Entry<String, BigDecimal> productIdIncomeResult : productIdIncomeResultMap.entrySet()) {
                relevanceTotalIncome = relevanceTotalIncome.add(productIdIncomeResult.getValue());
            }
        }
        //获取省份的回款信息
        if (CollectionUtils.isNotEmpty(provincePaymentIncomeList)) {
            for (Record record : provincePaymentIncomeList) {
                if (StringUtils.isEmpty(record.getStr("address")) || CrmConstant.PROVINCE_CITY_AREA.equals(record.getStr("address"))) {
                    if (CrmConstant.NO_PROVINCE.equals(province)) {
                        noRelevanceTotalIncome = record.get("totalAmount");
                        break;
                    }
                } else {
                    if (record.getStr("address").equals(province)) {
                        noRelevanceTotalIncome = record.get("totalAmount");
                        break;
                    }
                }
            }
        }
        productIdIncomeResultMap.put(CrmConstant.RELEVANCE_TOTAL_INCOME, relevanceTotalIncome.setScale(2, BigDecimal.ROUND_HALF_UP));
        productIdIncomeResultMap.put(CrmConstant.NO_RELEVANCE_TOTAL_INCOME, noRelevanceTotalIncome.setScale(2, BigDecimal.ROUND_HALF_UP));
        productIdIncomeResultMap.put(CrmConstant.TOTAL_INCOME, relevanceTotalIncome.add(noRelevanceTotalIncome).setScale(2, BigDecimal.ROUND_HALF_UP));
        productIdIncomeResultMap.put(CrmSaleUsualEnum.VISIT_COUNT_TYPE_KEY.getName(), new BigDecimal(visitOrRelation(startTime, endTime, CrmSaleUsualEnum.VISIT_TYPE_KEY.getTypes(), province, deptOwnUserIds, longs)));
        productIdIncomeResultMap.put(CrmSaleUsualEnum.RELATION_COUNT_TYPE_KEY.getName(), new BigDecimal(visitOrRelation(startTime, endTime, CrmSaleUsualEnum.RELATION_TYPE_KEY.getTypes(), province, deptOwnUserIds, longs)));
        return productIdIncomeResultMap;
    }

    /***
     * 按照省份统计联系小计数量
     * @author yue.li
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return category 0:拜访 1:联系
     * @prov province 省份
     */
    public int visitOrRelation(String startTime, String endTime, String category, String province, List<Long> deptOwnUserIds, List<Long> longs) {
        int count = 0;
        if (province.equals(CrmConstant.NO_PROVINCE)) {
            province = null;
        }
        List<Record> list = Db.find(Db.getSqlPara("crm.saleUsual.queryCustomerRecord", Kv.by("startTime", startTime).set("endTime", endTime).set("province", province).set("category", category).set("deptOwnUserIds", deptOwnUserIds).set("ownUserIds", longs)));
        if (list != null && list.size() > 0) {
            count = list.size();
        }
        return count;
    }

    /***
     * 时间区间统计BD或者部门负责客户的回款信息
     * @author yue.li
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param ownUserId 负责人ID
     * @param deptOwnUserIds 部门人员ids
     * @param status 付款信息状态
     * @param longs 权限人员ids
     */
    public BigDecimal queryUserPayment(String startTime, String endTime, String ownUserId, List<Long> deptOwnUserIds, Integer status, List<Long> longs) {
        Record record = Db.findFirst(Db.getSqlPara("crm.business.queryUserBopsPaymentList", Kv.by("startTime", startTime).set("endTime", endTime).set("ownUserId", ownUserId).set("deptOwnUserIds", deptOwnUserIds).set("status", status).set("longs", longs).set("payType", CrmConstant.CRM_PAYMENT_TYPE_CONSUME)));
        BigDecimal totalAmount = new BigDecimal(0);
        if (record != null && StringUtils.isNotEmpty(record.getStr("totalAmount"))) {
            totalAmount = new BigDecimal(record.getStr("totalAmount")).setScale(2, BigDecimal.ROUND_HALF_UP);
        }
        return totalAmount.setScale(2, BigDecimal.ROUND_HALF_UP);
    }

    /***
     * 时间区间按照省份统计部门负责客户的回款信息
     * @author yue.li
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param deptOwnUserIds 部门人员ids
     * @param status 付款信息状态
     * @param longs 权限人员ids
     */
    public List<Record> queryProvinceUserPayment(String startTime, String endTime, List<Long> deptOwnUserIds, Integer status, List<Long> longs) {
        return Db.find(Db.getSqlPara("crm.business.queryProvinceBopsPaymentList", Kv.by("startTime", startTime).set("endTime", endTime).set("deptOwnUserIds", deptOwnUserIds).set("status", status).set("longs", longs).set("payType", CrmConstant.CRM_PAYMENT_TYPE_CONSUME)));
    }

    /**
     * 根据商品大类获取商品列表
     *
     * @param category
     * @author yue.li
     */
    public List<Record> getProductList(String category) {
        return Db.find(Db.getSql("crm.business.queryProductListByCategory"), category, CrmConstant.PRODUCT_PULL_ON);
    }

    /**
     * 封装商品大类Map
     *
     * @return
     * @author yue.li
     */
    public Map<String, Integer> getCategoryMap() {
        Map<String, Integer> categoryMap = new HashMap<>(4);
        List<Record> catIdRecords = Db.find(Db.getSql("crm.product.getCategoryCodeIdMap"));
        catIdRecords.forEach(item -> categoryMap.put(item.getStr("category_code"), item.getInt("category_id")));
        return categoryMap;
    }

    /**
     * 保存新增商品集合
     *
     * @param productList 商品集合
     * @return
     * @author yue.li
     */
    public void saveProductInfo(List<CrmProduct> productList) {
        if (CollectionUtils.isNotEmpty(productList)) {
            List<Record> prods = productList.stream().map(CrmProduct::toRecord).collect(Collectors.toList());
            Db.batchSave("72crm_crm_product", prods, prods.size());
        }
    }

    /**
     * 根据商品编码和商品名称获取商品信息
     *
     * @param code 商品编码
     * @param name 商品名称
     * @return
     * @author yue.li
     */
    public Record getProductByCodeName(String code, String name) {
        return Db.findFirst(Db.getSql("crm.product.getProductByCodeName"), code, name);
    }

    /**
     * 更新商品状态为下架
     *
     * @param categoryId 商品大类
     * @return
     * @author yue.li
     */
    public void updatePutOff(Integer categoryId) {
        Db.update(Db.getSql("crm.product.updatePutOff"), CrmConstant.PRODUCT_PULL_OFF, categoryId);
    }

    /**
     * 更新商品状态为上架
     *
     * @param updateProductList 更新商品集合
     * @return
     * @author yue.li
     */
    public void updatePutOn(List<Record> updateProductList) {
        if (CollectionUtils.isNotEmpty(updateProductList)) {
            Db.batchUpdate("72crm_crm_product", "product_id", updateProductList, updateProductList.size());
        }
    }

    /**
     * 处理定时任务同步过来商品集合
     *
     * @param result     商品结果集
     * @param categoryId 商品大类ID
     * @return
     * @author yue.li
     */
    @Before(Tx.class)
    public void dealProductList(List<CrmProductDetail> result, Integer categoryId) {
        if (CollectionUtils.isNotEmpty(result)) {
            List<CrmProduct> addProductList = new ArrayList<>();
            List<Record> updateProductList = new ArrayList<>();

            // 更新商品状态为下架
            updatePutOff(categoryId);

            for (CrmProductDetail crmProductDetail : result) {
                Record productRecord = getProductByCodeName(crmProductDetail.getCode(), crmProductDetail.getName());
                if (Objects.isNull(productRecord)) {
                    CrmProductDetailDto dto = new CrmProductDetailDto();
                    dto.setCode(crmProductDetail.getCode());
                    dto.setName(crmProductDetail.getName());
                    dto.setBatchId(IdUtil.simpleUUID());
                    dto.setCategoryId(categoryId);
                    addProductList.add(constructCrmProduct(dto));
                } else {
                    productRecord.set("status", CrmConstant.PRODUCT_PULL_ON);
                    updateProductList.add(productRecord);
                }
            }

            // 保存商品信息
            saveProductInfo(addProductList);

            // 更新最新接口已经存在商品状态为上架(已经下架的不需要展示)
            updatePutOn(updateProductList);
        }
    }

    /**
     * 获取商机组ids
     *
     * @return
     * @author yue.li
     */
    public List<String> getBusinessGroup() {
        List<Record> list = Db.find(Db.getSql("admin.businessType.queryBusinessDeptList"));
        return list.stream().map(item -> item.getStr("id")).collect(Collectors.toList());
    }

    /**
     * 统计负责人下的商机数量
     *
     * @param ownerUserId
     * @return
     */
    public Integer countBusinessListByOwnerUserId(Long ownerUserId) {
        return Db.queryInt(Db.getSql("crm.business.countBusinessListByOwnerUserId"), ownerUserId);
    }

    /**
     * 将商机释放回公海
     *
     * @param businessId 商机ID
     */
    public void pullBusinessToPublicPool(Long businessId) {
        Db.update(Db.getSql("crm.business.updateBusinessToPublicPool"), businessId);
    }


    /**
     * 根据场景ID查询商机列表
     * @return
     */
    public Page<Record> queryBusinessPageList(BasePageRequest basePageRequest, AdminUser adminUser) {
        JSONObject jsonObject = basePageRequest.getJsonObject();
        //场景ID
        Integer sceneId = jsonObject.getInteger("sceneId");
        //商机名称
        String search = jsonObject.getString("search");
        //查询条件
        JSONObject data = jsonObject.getJSONObject("data");
        //场景名称
        String sceneName = "";
        if (Objects.isNull(data)) {
            data = new JSONObject();
        }
        if (Objects.nonNull(sceneId) && !Objects.equals(sceneId, 0)) {
            //将场景参数put到查询条件中
            AdminScene scene = AdminScene.dao.findById(sceneId);
            if (Objects.nonNull(scene) && StringUtils.isNotBlank(scene.getData())) {
                data.putAll(JSON.parseObject(scene.getData()));
                sceneName = scene.getName();
            }
        }

        List<JSONObject> params = data.values().stream().map(item -> (JSONObject) item).collect(Collectors.toList());

        //组装search
        if (StrUtil.isNotEmpty(search)) {
            if (isValid(search)) {
                throw new CrmException("参数包含非法字段");
            }
            JSONObject searchJ = new JSONObject();
            searchJ.put("name", "business_name");
            searchJ.put("condition", "contains");
            searchJ.put("value", search);
            params.add(searchJ);
        }

        StringBuilder whereSb = new StringBuilder(" where 1=1");
        //组装Where
        //后期商机列表升级会把这些复杂逻辑给干掉
        for (JSONObject param : params) {
            //参数名
            String name = attachAlias(param.getString("name"));
            //条件
            String condition = param.getString("condition");
            //参数
            String value = param.getString("value");
            //参数类型
            String formType = param.getString("formType");

            //只有高级筛选的商机组条件才会出现
            if ("business_type".equals(formType)) {
                whereSb.append(" and ").append(name).append(" = ").append(param.getInteger("typeId"));
                if (StrUtil.isNotEmpty(param.getString("statusId"))) {
                    if ("win".equals(param.getString("statusId"))) {
                        //赢单
                        whereSb.append(" and biz.is_end = 1");
                    } else if ("lose".equals(param.getString("statusId"))) {
                        //输单
                        whereSb.append(" and biz.is_end = 2");
                    } else if ("invalid".equals(param.getString("statusId"))) {
                        //非法
                        whereSb.append(" and biz.is_end = 3");
                    } else {
                        //商机组阶段ID
                        whereSb.append(" and biz.status_id = ").append(param.getString("statusId"));
                    }
                }
                continue;
            }

            //组装条件语句
            if (StrUtil.isNotEmpty(value) || StrUtil.isNotEmpty(param.getString("start")) || StrUtil.isNotEmpty(param.getString("end"))) {
                if ("takePart".equals(condition)) {
                    whereSb.append(" and ( biz.ro_user_id like '%,").append(value).append(",%' or biz.rw_user_id like '%,").append(value).append(",%')");
                } else {
                    whereSb.append(" and ").append(name);
                    if ("is".equals(condition)) {
                        whereSb.append(" = '").append(value).append('\'');
                    } else if ("isNot".equals(condition)) {
                        whereSb.append(" != '").append(value).append('\'');
                    } else if ("contains".equals(condition)) {
                        whereSb.append(" like '%").append(value).append("%'");
                    } else if ("notContains".equals(condition)) {
                        whereSb.append(" not like '%").append(value).append("%'");
                    } else if ("isNull".equals(condition)) {
                        whereSb.append(" is null");
                    } else if ("isNotNull".equals(condition)) {
                        whereSb.append(" is not null");
                    } else if ("gt".equals(condition)) {
                        whereSb.append(" > ").append(value);
                    } else if ("egt".equals(condition)) {
                        whereSb.append(" >= ").append(value);
                    } else if ("lt".equals(condition)) {
                        whereSb.append(" < ").append(value);
                    } else if ("elt".equals(condition)) {
                        whereSb.append(" <= ").append(value);
                    } else if ("in".equals(condition)) {
                        whereSb.append(" in (").append(value).append(')');
                    }
                    if ("datetime".equals(formType)) {
                        whereSb.append(" between '").append(param.getString("start")).append("' and '").append(param.getString("end")).append('\'');
                    }
                    if ("date".equals(formType)) {
                        whereSb.append(" between '").append(param.getString("startDate")).append("' and '").append(param.getString("endDate")).append('\'');
                    }
                }
            }
        }

        //超管
        if (Objects.nonNull(adminUser) && adminUser.getRoles().contains(BaseConstant.SUPER_ADMIN_ROLE_ID)) {
            // 下属负责的
            if (sceneName.startsWith(CrmConstant.CRM_SCENE_SUB_OWNER)) {
                //查询下属用户ID
                List<Long> userIds = adminUserService.queryUserByParentUser(adminUser.getUserId(), BaseConstant.AUTH_DATA_RECURSION_NUM);
                if (CollectionUtils.isNotEmpty(userIds)) {
                    whereSb.append(" and biz.owner_user_id in (").append(StrUtil.join(",", userIds)).append(')');
                } else {
                    whereSb.append(" and biz.owner_user_id ='' ");
                }
            }
        } else {
            Long userId = BaseUtil.getUserId();
            List<Long> userIds = adminUserService.queryUserByAuth(userId);
            if (CollectionUtils.isNotEmpty(userIds)) {
                if (sceneName.startsWith(CrmConstant.CRM_SCENE_SUB_OWNER)) {
                    //下属负责的
                    userIds.remove(userId);
                    if (CollectionUtils.isNotEmpty(userIds)) {
                        whereSb.append(" and biz.owner_user_id in (").append(StrUtil.join(",", userIds)).append(')');
                    } else {
                        whereSb.append(" and biz.owner_user_id ='' ");
                    }
                } else if (sceneName.startsWith(CrmConstant.CRM_SCENE_MY_OWN)) {
                    //我负责的(场景数据已有owner条件)
                    //do nothing
                } else if (sceneName.startsWith(CrmConstant.CRM_SCENE_MY_TAKE_PART_IN)) {
                    //我参与的(客户列表升级改造)
                    whereSb.append(" and (biz.ro_user_id like CONCAT('%,','").append(userId).append("',',%')").append(" or biz.owner_user_id like CONCAT('%,','").append(userId).append("',',%'))");
                } else {
                    //自定义场景(后期会废除)
                    whereSb.append(" and biz.owner_user_id in (").append(StrUtil.join(",", userIds)).append(')');
                }
            }
        }
        String sortField = basePageRequest.getJsonObject().getString("sortField");
        String orderNum = basePageRequest.getJsonObject().getString("order");
        if (StrUtil.isAllEmpty(sortField, orderNum)) {
            //默认ID降序
            sortField = "biz.business_id";
            orderNum = BusinessOrderEnum.DESC.name();
        } else {
            if (isValid(sortField)) {
                throw new CrmException("参数包含非法字段");
            }
            sortField = attachAlias(sortField);
            orderNum = BusinessOrderEnum.getByCode(orderNum);
        }
        String select = "select" +
                " biz.business_name," +
                " biz.business_id," +
                " customer.customer_id," +
                " customer.customer_name," +
                " adminuser.realname owner_user_name," +
                " dept.name as dept_name," +
                " case biz.is_end  WHEN 1 THEN '赢单' WHEN 2 THEN '输单' else bizStatus.name end status_name," +
                " biz.money," +
                " cast(biz.deal_date AS date) AS deal_date," +
                " biz.remark," +
                " createuser.realname as create_user_name," +
                " biz.is_end," +
                " biz.status_id," +
                " biz.type_id," +
                " groupdept.name as biz_group_dept_name," +
                " biz.create_time," +
                " biz.update_time";
        String from = " from 72crm_crm_business biz " +
                " left join 72crm_crm_customer customer on biz.customer_id = customer.customer_id" +
                " left join 72crm_admin_user adminuser on biz.owner_user_id = adminuser.user_id" +
                " left join 72crm_admin_dept dept on adminuser.dept_id = dept.dept_id" +
                " left join 72crm_crm_business_status bizStatus on biz.status_id = bizStatus.status_id and bizStatus.is_deleted = 0" +
                " left join 72crm_crm_business_group bizgroup on bizgroup.dept_ids = biz.dept_id and bizgroup.status = 1 and bizgroup.is_deleted = 0" +
                " left join 72crm_admin_dept groupdept on groupdept.dept_id = biz.dept_id" +
                " left join 72crm_admin_user createuser on biz.create_user_id = createuser.user_id " +
                whereSb.toString() +
                " order by " + sortField + " " + orderNum;
        return Db.paginate(basePageRequest.getPage(), basePageRequest.getLimit(), select, from);
    }

    /**
     * 防注入
     * @param param
     * @return
     */
    private boolean isValid(String param) {
        String reg = "(?:')|(?:--)|(/\\*(?:.|[\\n\\r])*?\\*/)|"
                + "(\\b(select|update|union|and|or|delete|insert|trancate|char|into|substr|ascii|declare|exec|count|master|drop|execute)\\b)";

        Pattern sqlPattern = Pattern.compile(reg, Pattern.CASE_INSENSITIVE);
        return sqlPattern.matcher(param).find();
    }

    /**
     * 附加别名
     * @param name
     * @return
     */
    private String attachAlias(String name){
        return Optional.ofNullable(ALIAS_MAP.get(name)).orElse(name);
    }

    private static final Map<String, String> ALIAS_MAP = new HashMap<String, String>(){
        private static final long serialVersionUID = 1171711823739629469L;
        {
        put("business_name","biz.business_name");
        put("customer_name","customer.customer_name");
        put("type_id","bizgroup.id");
        put("money","biz.money");
        put("deal_date","biz.deal_date");
        put("remark","biz.remark");
        put("owner_user_id","biz.owner_user_id");
        put("create_user_id","biz.create_user_id");
        put("update_time","biz.update_time");
        put("create_time","biz.create_time");
        put("ro_user_id","biz.ro_user_id");
        put("rw_user_id","biz.rw_user_id");
    }};

    /**
     * 根据商机ID获取商机详情
     * @param businessId
     * @return
     */
    public CrmBusiness getById(Integer businessId) {
        CrmBusiness business = CrmBusiness.dao.findFirst(Db.getSql("crm.business.queryDetailById"), businessId);
        if (business != null) {
            AdminDept ownerBizDept = adminDeptService.getBusinessDepartmentPOByDeptId(business.getOwnerDeptId());
            if (Objects.nonNull(ownerBizDept)) {
                business.setOwnerDeptName(ownerBizDept.getName());
            }
            if (!Objects.equals(business.getIsEnd(), 0)) {
                //商机完结阶段清空statusId
                business.setStatusId(null);
            }
            business.setHasProduct(hasProduct(businessId));
            business.put("shareholderRelationName", CrmBusinessShareholderRelationEnum.getName(business.getShareholderRelation()));
        }
        return business;
    }

    /**
     * 查询商机字段+详情
     * @param businessId 商机ID
     * @param deptId 当前用户的部门ID
     * @return
     */
    public List<Record> queryFieldNew(Integer businessId, Long deptId) {
        List<Record> fieldList = new LinkedList<>();

        Object[] jsonObjectArray = Arrays.stream(CrmBusinessShareholderRelationEnum.values()).map(o -> {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("name", o.getName());
            jsonObject.put("value", o.getType());
            return jsonObject;
        }).toArray();

        if (Objects.isNull(businessId)) {
            fieldUtil.getFixedField(fieldList, "businessName", "商机名称", "", "text", null, 1);
            fieldUtil.getFixedField(fieldList, "customerId", "客户名称", "", "customer", null, 1);
            fieldUtil.getFixedField(fieldList, "deptId", "业务线", Optional.ofNullable(crmBusinessGroupService.getConfiguredDept(deptId)).orElseGet(() -> new Record().set("typeId", null).set("deptId", null).set("bizDeptId", null).set("deptName", null)), "recommendBusiness", null, 1);
            fieldUtil.getFixedField(fieldList, "statusId", "当前阶段", "", "business_status", null, 1, "表示正在进行中的商机阶段");
            fieldUtil.getFixedField(fieldList, "applicationScenario", "应用场景", "", "application_scenario", null, 1);
            fieldList.add(new Record().set("name", "可能性").set("value", "").set("field_name", "stageWinRate").set("form_type", "number").set("field_type", 1).set("setting", null).set("is_null", 1).set("tooltipContent", "表示在当前商机阶段实现赢单的概率百分比").set("limit", new int[]{0, 100}));
            fieldUtil.getFixedField(fieldList, "dealDate", "预计结束日期", "", "date", null, 1, "表示商机进行到结束阶段（赢单/输单）的日期");
            fieldUtil.getFixedField(fieldList, "shareholderRelation", "股东关系", "", "shareholder_relation", jsonObjectArray, 0,"数据项介绍:<br/>中兵介绍项目：由中兵介绍给千寻的项目<br/>中兵合作项目：中兵与千寻合作的项目<br/>阿里介绍项目：由阿里介绍给千寻的项目<br/>阿里合作项目：阿里与千寻合作的项目<br/>上海国和&上海国际介绍项目：由上海国和&上海国际介绍给千寻的项目<br/>工银金融介绍项目：由工银金融介绍给千寻的项目<br/>深圳见微介绍项目：由深圳见微介绍给千寻的项目<br/>阿里云合作项目：阿里云与千寻合作的项目");
            fieldUtil.getFixedField(fieldList, "partner", "合作伙伴(合同甲方)", "", "text", null, 0);
            fieldUtil.getFixedField(fieldList, "project", "项目", "", "text", null, 0);
            Record map = new Record();
            fieldList.add(map.set("field_name", "mapAddress")
                    .set("name", "区域")
                    .set("form_type", "map_address")
                    .set("fieldType", 1)
                    .set("is_null", 0));
            fieldUtil.getFixedField(fieldList, "ascription", "归属", "", "ascription", null, 0);
            fieldUtil.getFixedField(fieldList, "annualProduction", "年产量", "", "annual_production", null, 0);
            fieldUtil.getFixedField(fieldList, "fixedTime", "定点时间", "", "date", null, 0);
            fieldUtil.getFixedField(fieldList, "pruductTime", "量产时间", "", "date", null, 0);
            fieldUtil.getFixedField(fieldList, "remark", "备注", "", "textarea", null, 0);
            fieldUtil.getFixedField(fieldList, "product", "商品", Kv.by("discount_rate", "").set("product", new ArrayList<>()).set("total_price", ""), "product", null, 1);
            fieldList.addAll(adminFieldService.list("5"));
        } else {
            Record record = Db.findFirst(Db.getSql("crm.business.queryDetailById"), businessId);
            if (Objects.isNull(record)) {
                throw new CrmException("商机数据不存在");
            }
            fieldUtil.getFixedField(fieldList, "business_name", "商机名称", record.getStr("business_name"), "text", null, 1);
            List<Record> customerList = new ArrayList<>();
            Record customer = new Record();
            customerList.add(customer.set("customer_id", record.getInt("customer_id")).set("customer_name", record.getStr("customer_name")));
            fieldUtil.getFixedField(fieldList, "customerId", "客户名称", customerList, "customer", null, 1);
            fieldUtil.getFixedField(fieldList, "deptId", "业务线", new Record().set("typeId", record.getLong("type_id")).set("deptId", record.getLong("dept_id")).set("deptName", record.getStr("dept_name")).set("bizDeptId", Long.valueOf(adminDeptService.getBusinessDepartmentByDeptIdNew(String.valueOf(record.getLong("dept_id"))))), "recommendBusiness", null, 1);
            fieldUtil.getFixedField(fieldList, "statusId", "当前阶段", record.getInt("status_id"), "business_status", null, 1, "表示正在进行中的商机阶段");
            fieldUtil.getFixedField(fieldList, "applicationScenario", "应用场景", record.getInt("application_scenario_id"), "application_scenario", null, 1);
            fieldList.add(new Record().set("name", "可能性").set("value", getStageWinRate(record)).set("field_name", "stageWinRate").set("form_type", "number").set("field_type", 1).set("setting", null).set("is_null", 1).set("tooltipContent", "表示在当前商机阶段实现赢单的概率百分比").set("limit", new int[]{0, 100}));
            fieldUtil.getFixedField(fieldList, "dealDate", "预计结束日期", DateUtil.formatDateTime(record.get("deal_date")), "date", null, 1, "表示商机进行到结束阶段（赢单/输单）的日期");
            fieldUtil.getFixedField(fieldList, "shareholderRelation", "股东关系", record.getInt("shareholder_relation"), "shareholder_relation", jsonObjectArray, 0, "数据项介绍:<br/>中兵介绍项目：由中兵介绍给千寻的项目<br/>中兵合作项目：中兵与千寻合作的项目<br/>阿里介绍项目：由阿里介绍给千寻的项目<br/>阿里合作项目：阿里与千寻合作的项目<br/>上海国和&上海国际介绍项目：由上海国和&上海国际介绍给千寻的项目<br/>工银金融介绍项目：由工银金融介绍给千寻的项目<br/>深圳见微介绍项目：由深圳见微介绍给千寻的项目<br/>阿里云合作项目：阿里云与千寻合作的项目");
            fieldUtil.getFixedField(fieldList, "partner", "合作伙伴(合同甲方)", record.getStr("partner"), "text", null, 0);
            fieldUtil.getFixedField(fieldList, "project", "项目", record.getStr("project"), "text", null, 0);
            fieldList.add(new Record().set("fieldName", "mapAddress")
                    .set("name", "区域")
                    .set("value", Kv.by("location", "")
                            .set("address", record.getStr("mapAddress"))
                            .set("detailAddress", "")
                            .set("lng", "")
                            .set("lat", ""))
                    .set("formType", "map_address")
                    .set("isNull", 0));
            fieldUtil.getFixedField(fieldList, "ascription", "归属", record.getStr("ascription"), "ascription", null, 0);
            fieldUtil.getFixedField(fieldList, "annualProduction", "年产量", record.getStr("annualProduction"), "annual_production", null, 0);
            fieldUtil.getFixedField(fieldList, "fixedTime", "定点时间", record.getStr("fixedTime"), "date", null, 0);
            fieldUtil.getFixedField(fieldList, "pruductTime", "量产时间", record.getStr("pruductTime"), "date", null, 0);
            fieldUtil.getFixedField(fieldList, "remark", "备注", record.getStr("remark"), "textarea", null, 0);
            fieldList.addAll(adminFieldService.queryByBatchId(record.getStr("batch_id")));
            Record totalPrice = Db.findFirst("select IFNULL(SUM(sales_price),0) as total_price from 72crm_crm_business_product where business_id = ?", businessId);
            List<Record> productList = Db.find(Db.getSql("crm.business.queryBusinessProduct"), businessId);
            Kv kv = Kv.by("product", productList).set("total_price", totalPrice.getStr("total_price"));
            fieldUtil.getFixedField(fieldList, "product", "商品", kv, "product", null, 1);
        }
        return fieldList;
    }

    /**
     * 获取阶段赢率(老数据兼容)
     * 先从字段获取，找不到查阶段表
     * @param record
     * @return
     */
    private String getStageWinRate(Record record) {
        Integer stageWinRate = record.getInt("stage_win_rate");
        if (Objects.nonNull(stageWinRate)) {
            return String.valueOf(stageWinRate);
        }
        Long statusId = record.getLong("status_id");
        if (Objects.nonNull(statusId)) {
            CrmBusinessStatus crmBusinessStatus = crmBusinessStatusService.getByStatusId(statusId);
            if (Objects.nonNull(crmBusinessStatus)) {
                return crmBusinessStatus.getRate();
            }
        }
        return null;
    }

    /**
     * 根据阶段 id，获取关键销售活动列表
     * @param businessId 商机ID
     * @param statusId 商机组阶段ID
     * @return List<Record>
     */
    public List<Record> statusSalesActivityList(Long businessId, Long statusId) {
        return Db.find(Db.getSql("crm.business.status.salesActivity.record.listByStatusIdOfBusiness"), businessId, statusId);
    }

    /**
     * 根据阶段 id，获取可验证结果列表
     * @param businessId 商机ID
     * @param statusId 商机组阶段ID
     * @param ossPrivateFileUtil
     * @return
     */
    public List<Record> statusVerificationList(Long businessId, Long statusId, OssPrivateFileUtil ossPrivateFileUtil) {
        return Db.find(Db.getSql("crm.business.status.verification.record.listRecordByStatusIdOfBusiness"), businessId, statusId).stream().peek(record->{
            List<Record> files = new ArrayList<>();
            String batchId = record.getStr("batch_id");
            if (StringUtils.isNotBlank(batchId)) {
                files.addAll(adminFileService.queryByBatchId(batchId, ossPrivateFileUtil).stream()
                        .map(file-> new Record().set("fileId",file.getFileId()).set("name", file.getName()).set("filePath", file.getFilePath())).collect(Collectors.toList()));
            }
            record.set("files", files);
        }).collect(Collectors.toList());
    }

    /**
     * 修改关键销售活动
     * @param request
     */
    @Before(Tx.class)
    public void statusSalesActivityEdit(Record request) {
        //用户ID
        Long userId = request.getLong("userId");
        //商机ID
        Long businessId = request.getLong("businessId");
        //阶段ID
        Long statusId = request.getLong("statusId");
        //活动列表
        List<Record> activities = request.get("activities");
        /*  查询现存销售活动数据 */
        List<Record> existRecord = crmBusinessStatusSalesActivityService.listRecordByStatusIdOfBusiness(businessId, statusId);
        /*  比较数据 */
        //删除的销售活动
        List<Record> deletedActivities = diffActivityList(activities, existRecord);
        //新增的销售活动
        List<Record> addActivities = diffActivityList(existRecord, activities);
        /*  删除数据 */
        deletedActivities.forEach(item -> CrmBusinessStatusSalesActivityRecord.dao.deleteById(item.getLong("id")));
        /*  新增数据 */
        addActivities.forEach(item -> {
            CrmBusinessStatusSalesActivityRecord newModal = new CrmBusinessStatusSalesActivityRecord()._setOrPut(item.getColumns());
            if (Objects.isNull(newModal.getBusinessId())) {
                throw new CrmException("关键销售活动[商机ID]不能为空");
            }
            if (Objects.isNull(newModal.getActivityId())) {
                throw new CrmException("关键销售活动[商机组关键销售活动ID]不能为空");
            }
            if (Objects.isNull(newModal.getStatusId())) {
                throw new CrmException("关键销售活动[商机组阶段ID]不能为空");
            }
            newModal.save();
        });
        /* 插入操作日志 */
        String opLog;
        CrmBusinessStatus businessStatus = crmBusinessStatusService.getByStatusId(statusId);
        String statusName = businessStatus == null ? statusId.toString() : businessStatus.getName();
        if (CollectionUtils.isEmpty(activities)) {
            opLog = "将[" + statusName + "]的关键销售活动修改为：空";
        } else {
            List<Long> activityIds = activities.stream().map(r -> r.getLong("activity_id")).collect(Collectors.toList());
            opLog = "将[" + statusName + "]的关键销售活动修改为：" +
                    crmBusinessStatusSalesActivityService.listByStatusId(statusId).stream()
                            .filter(activity -> activityIds.contains(activity.getLong("id")))
                            .map(activity -> activity.getStr("name"))
                            .collect(Collectors.joining(","));
        }
        crmRecordService.addActionRecord(userId.intValue(), CrmEnum.BUSINESS_TYPE_KEY.getTypes(), businessId.intValue(), opLog);
    }

    public void statusVerificationEdit(Record record, final OssPrivateFileUtil ossUtil, Long userId) {
        /* 判断是否需要新增 */
        CrmBusinessStatusVerificationRecord modal = new CrmBusinessStatusVerificationRecord()._setOrPut(record.getColumns());
        if (Objects.isNull(modal.getId())) {
            /* 插入 */
            verificationSave(modal, record.get("fileIds"), ossUtil, userId);
        } else {
            /* 更新 */
            verificationUpdate(modal, record.get("fileIds"), ossUtil, userId);
        }
    }

    /**
     * 保存可验证结果
     * @param modal
     * @param fileIds
     * @param ossUtil
     * @param userId
     */
    @Before(Tx.class)
    private void verificationSave(CrmBusinessStatusVerificationRecord modal, List<Long> fileIds, OssPrivateFileUtil ossUtil, Long userId) {
        /* 判断是否已存在 */
        CrmBusinessStatusVerificationRecord existModal = crmBusinessStatusVerificationService.findRecordByBizIdAndVeriId(modal.getBusinessId(), modal.getVerificationId());
        if (Objects.nonNull(existModal)) {
            modal.setId(existModal.getId());
            modal.setBatchId(existModal.getBatchId());
            verificationUpdate(modal, fileIds, ossUtil, userId);
            return;
        }
        /* 生成BatchId */
        String batchId = IdUtil.fastSimpleUUID();
        modal.setBatchId(batchId);
        /* 插入 */
        modal.save();
        /* 成功后更新文件的batchId */
        batchUpdateBatchId(fileIds, batchId);
        /* 插入操作日志 */
        saveFileOperationLog(userId, modal.getBusinessId(), modal.getStatusId(), modal.getVerificationId(), "上传", fileIds,null);
        if (StringUtils.isNoneBlank(modal.getContent())) {
            /* 插入备注修改日志 */
            crmRecordService.addActionRecord(userId.intValue(), CrmEnum.BUSINESS_TYPE_KEY.getTypes(), modal.getBusinessId().intValue(), "将备注由[]修改为[" + modal.getContent() + "]");
        }
    }

    /**
     * 更新可验证结果
     * @param modal
     * @param fileIds
     * @param ossUtil
     * @param userId
     */
    @Before(Tx.class)
    private void verificationUpdate(CrmBusinessStatusVerificationRecord modal, List<Long> fileIds, OssPrivateFileUtil ossUtil, Long userId) {
        List<String> removeFilePaths = new ArrayList<>();
        if (Db.tx(() -> {
            CrmBusinessStatusVerificationRecord oldRecord = CrmBusinessStatusVerificationRecord.dao.findById(modal.getId());
            /* 更新 */
            modal.update();
            if (!StringUtils.equals(oldRecord.getContent(), modal.getContent())) {
                /* 插入备注修改日志 */
                crmRecordService.addActionRecord(userId.intValue(), CrmEnum.BUSINESS_TYPE_KEY.getTypes(), modal.getBusinessId().intValue(), "将备注由[" + oldRecord.getContent() + "]修改为[" + modal.getContent() + "]");
            }
            /* 获取源文件ID列表 */
            List<Long> existIds = adminFileService.queryIdsByBatchId(modal.getBatchId());
            /* 比对文件ID */
            // 新增的文件ID
            List<Long> addFiles = diffFileList(existIds, fileIds);
            if (CollectionUtils.isNotEmpty(addFiles)) {
                batchUpdateBatchId(addFiles, modal.getBatchId());
                /* 插入文件操作日志 */
                saveFileOperationLog(userId, modal.getBusinessId(), modal.getStatusId(), modal.getVerificationId(), "上传", addFiles, null);
            }
            // 需要删除的文件ID
            List<Long> removeFiles = diffFileList(fileIds, existIds);
            if (CollectionUtils.isNotEmpty(removeFiles)) {
                List<String> deleteFileNames = new ArrayList<>();
                removeFiles.forEach(removeFileId -> {
                    AdminFile adminFile = AdminFile.dao.findByIdLoadColumns(removeFileId, "file_id,name,file_path");
                    if (Objects.nonNull(adminFile)) {
                        deleteFileNames.add(adminFile.getName());
                        //删除
                        adminFile.delete();
                        //删除OSS文件
                        removeFilePaths.add(adminFile.getFilePath());
                    }
                });
                /* 插入文件操作日志 */
                saveFileOperationLog(userId, modal.getBusinessId(), modal.getStatusId(), modal.getVerificationId(), "删除", null, deleteFileNames);
            }
            return true;
        })) {
            //事务提交成功后再删除OSS文件
            if (CollectionUtils.isNotEmpty(removeFilePaths)) {
                removeFilePaths.forEach(filePath -> CrmThreadPool.INSTANCE.getInstance().execute(() -> ossUtil.removeFileFromOss(filePath)));
            }
        }
    }

    /**
     * 插入可验证结果的文件操作日志
     *
     * @param userId 用户ID
     * @param businessId 商机ID
     * @param statusId   阶段ID
     * @param verificationId 可验证结果ID
     * @param op         操作
     * @param fileIds    文件ID
     * @param fileNames    文件名称
     */
    private void saveFileOperationLog(Long userId,Long businessId, Long statusId, Long verificationId, String op, List<Long> fileIds,List<String> fileNames) {
        if (Objects.isNull(userId) || Objects.isNull(businessId) || Objects.isNull(statusId) || Objects.isNull(verificationId) || Objects.isNull(op) || (CollectionUtils.isEmpty(fileIds) && CollectionUtils.isEmpty(fileNames))) {
            return;
        }
        String statusName = crmBusinessStatusService.getStatusNameById(statusId);
        CrmBusinessStatusVerification verification = CrmBusinessStatusVerification.dao.findById(verificationId);
        String verificationName = Objects.isNull(verification) ? null : verification.getName();
        if (CollectionUtils.isNotEmpty(fileNames)) {
            fileNames.forEach(fileName -> crmRecordService.addActionRecord(userId.intValue(), CrmEnum.BUSINESS_TYPE_KEY.getTypes(), businessId.intValue(), "编辑了" + statusName + "-" + verificationName + ":" + op + "了文件[" + fileName + "]"));
            return;
        }
        for (Long fileId : fileIds) {
            String name = adminFileService.queryNameByFileId(fileId);
            if (StringUtils.isNoneBlank(name)) {
                crmRecordService.addActionRecord(userId.intValue(), CrmEnum.BUSINESS_TYPE_KEY.getTypes(), businessId.intValue(), "编辑了" + statusName + "-" + verificationName + ":" + op + "了文件[" + name + "]");
            }
        }
    }

    /**
     * 找出target在source中不存在的记录
     * @return
     */
    private List<Record> diffActivityList(List<Record> source, List<Record> target) {
        if (CollectionUtils.isEmpty(source)) {
            return Objects.nonNull(target) ? target : new ArrayList<>();
        }
        if (CollectionUtils.isEmpty(target)) {
            return new ArrayList<>();
        }
        return target.stream().filter(targetRecord ->
                source.stream().noneMatch(sourceRecord ->
                        Objects.equals(targetRecord.getLong("activity_id"), sourceRecord.getLong("activity_id"))))
                .collect(Collectors.toList());
    }

    /**
     * 找出target在source中不存在的记录
     * @return
     */
    private List<Long> diffFileList(List<Long> source, List<Long> target) {
        if (CollectionUtils.isEmpty(source)) {
            return Objects.nonNull(target) ? target : new ArrayList<>();
        }
        if (CollectionUtils.isEmpty(target)) {
            return new ArrayList<>();
        }
        return target.stream().filter(targetRecord ->
                source.stream().noneMatch(sourceRecord ->
                        Objects.equals(targetRecord,sourceRecord)))
                .collect(Collectors.toList());
    }

    /**
     * 批量更新文件批次号
     * @param fileIds
     * @param batchId
     */
    private void batchUpdateBatchId(List<Long> fileIds, String batchId) {
        if (CollectionUtils.isNotEmpty(fileIds)) {
            fileIds.forEach(fileId-> adminFileService.updateBatchIdById(fileId, batchId));
        }
    }

    /**
     * 查询阶段集合下的商机
     *
     * @param statusIds
     * @return
     */
    public List<Record> selectByStatusIds(List<Long> statusIds) {
        if (CollectionUtils.isEmpty(statusIds)) {
            return null;
        }
        Kv kv = Kv.by("statusIds", statusIds);
        SqlPara sqlPara = Db.getSqlPara("crm.business.selectByStatusIds", kv);
        return Db.find(sqlPara);
    }

    /**
     * 新增或更新商机（用于excel导入）
     * @author yue.li
     * @param  dataList 数据集合
     */
    @Before(Tx.class)
    public void addOrUpdateBusinessForExcel(List<CrmBusinessExcel> dataList) {
        dataList.forEach(o -> {
            if(Objects.nonNull(o.getCustomerId())) {
                CrmBusiness crmBusiness = new CrmBusiness();
                crmBusiness.setBusinessName(o.getBusinessName());
                crmBusiness.setCustomerId(Objects.nonNull(o.getCustomerId()) ? o.getCustomerId().intValue() : null);
                crmBusiness.setCreateUserId(o.getCreateUserId());
                crmBusiness.setOwnerUserId(o.getOwnerUserId());
                crmBusiness.setDeptId(o.getDeptId());
                crmBusiness.setCreateTime(StringUtils.isNotEmpty(o.getCreateTime()) ? CrmDateUtil.parseDateHours(o.getCreateTime() + ":00") : new Date());
                crmBusiness.setUpdateTime(StringUtils.isNotEmpty(o.getUpdateTime()) ? CrmDateUtil.parseDateHours(o.getUpdateTime() + ":00") : new Date());
                crmBusiness.setRemark("excel import");
                crmBusiness.setBatchId(IdUtil.simpleUUID());
                crmBusiness.setRwUserId(",");
                crmBusiness.setRoUserId(",");
                crmBusiness.save();
            }
        });
    }

    /**
     * 根据阶段id获取阶段名称
     * @author yue.li
     * @param statusId 阶段id
     */
    public String getStateNameByStatusId(Integer statusId) {
        CrmBusinessStatus crmBusinessStatus = CrmBusinessStatus.dao.findFirst(Db.getSql("crm.business.getStateNameByStatusId"), statusId);
        if (Objects.nonNull(crmBusinessStatus)) {
            return crmBusinessStatus.getName();
        }
        return "";
    }

    /**
     * 创建虚拟网站账号并与客户进行绑定
     * @param customerId
     * @return
     */
    @Before(Tx.class)
    public Long createSiteMemberAndBindCid(Integer customerId){
        CrmCustomer crmCustomer = CrmCustomer.dao.findSingleByColumn("customer_id", customerId);
        if (crmCustomer == null){
            throw new CrmException("customerId:" + customerId + ",关联的客户不存在");
        }

        if (CrmSiteMember.dao.countByColumn("cust_id",crmCustomer.getCustomerNo()) > 0){
            throw new CrmException("customerId:" + customerId + ",已有关联的官网账号，无需再次生成");
        }

        VirtualRegInfo virtualRegInfo = new VirtualRegInfo();
        virtualRegInfo.setSource(SiteFromChannelEnum.E_CONTRACT.getCode());
        BaseRequest<VirtualRegInfo> regInfo = new BaseRequest<>();
        regInfo.setCaller(CrmConstant.CRM_SR_CODE);
        regInfo.setRequestId(IdUtil.simpleUUID());
        regInfo.setParam(virtualRegInfo);

        BaseResponse<Long> response = registerBizService.register4Virtual(regInfo);

        if (!response.isSuccess()){
            throw new CrmException("创建虚拟网站账号 接口失败，code:" + response.getCode() + "，message:" + response.getMessage());
        }

        Long siteMemberId = response.getData();
        if (siteMemberId == null){
            throw new CrmException("创建官网虚拟账号异常，会员ID 数据为空");
        }

        CrmSiteMember siteMember = new CrmSiteMember();
        siteMember.setSiteMemberId(siteMemberId);
        siteMember.setCustId(crmCustomer.getCustomerNo());
        siteMember.setChannel(SiteFromChannelEnum.E_CONTRACT.name());
        siteMember.save();

        return siteMemberId;
    }

}
