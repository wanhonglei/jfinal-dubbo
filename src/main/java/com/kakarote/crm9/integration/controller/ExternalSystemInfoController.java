package com.kakarote.crm9.integration.controller;

import cn.hutool.core.util.IdUtil;
import com.alibaba.fastjson.JSONObject;
import com.jfinal.aop.Before;
import com.jfinal.aop.Inject;
import com.jfinal.core.Controller;
import com.jfinal.log.Log;
import com.jfinal.plugin.activerecord.Record;
import com.kakarote.crm9.common.midway.NotifyService;
import com.kakarote.crm9.common.spring.IocInterceptor;
import com.kakarote.crm9.erp.admin.common.ProductCategoryEnum;
import com.kakarote.crm9.erp.admin.common.TagCategoryEnum;
import com.kakarote.crm9.erp.admin.entity.TagDetails;
import com.kakarote.crm9.erp.admin.service.AdminDataDicService;
import com.kakarote.crm9.erp.admin.service.AdminSendEmailService;
import com.kakarote.crm9.erp.crm.constant.CrmConstant;
import com.kakarote.crm9.erp.crm.entity.BopsProductResponse;
import com.kakarote.crm9.erp.crm.entity.CrmProduct;
import com.kakarote.crm9.erp.crm.entity.CrmProductDetail;
import com.kakarote.crm9.erp.crm.entity.CrmProductDetailDto;
import com.kakarote.crm9.erp.crm.service.CrmBusinessService;
import com.kakarote.crm9.integration.common.EsbConfig;
import com.kakarote.crm9.utils.BaseUtil;
import com.kakarote.crm9.utils.HttpUtil;
import com.kakarote.crm9.utils.R;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

/**
 * ExternalSystemInfoController.
 *
 * @author yue.li
 * @create 2019/11/15 10:00
 */
@Before(IocInterceptor.class)
public class ExternalSystemInfoController extends Controller {

    private Log logger = Log.getLog(getClass());

    @Inject
    private AdminSendEmailService adminSendEmailService;

    @Inject
    private AdminDataDicService adminDataDicService;

    @Inject
    private CrmBusinessService crmBusinessService;

    @Autowired
    private NotifyService notifyService;

    @Autowired
    private EsbConfig esbConfig;

    /**
     * 标签系统获取标签信息
     * @author yue.li
     */
    public void getTagInfo() {
        try{
            logger.info("getTagInfo开始执行");
            for(TagCategoryEnum tagCategoryEnum : TagCategoryEnum.values()) {
                List<TagDetails> tagDetailsList = adminDataDicService.getTagDetail(tagCategoryEnum.getName(),esbConfig);
                adminDataDicService.saveTagInfo(tagDetailsList, tagCategoryEnum.getName());
            }
            logger.info("getTagInfo结束执行");
        } catch(Exception e) {
            logger.error(String.format("getTagInfo %s",e.getMessage()));
            /**发送失败消息通知*/
            adminSendEmailService.sendErrorMessage(e,notifyService);
        }
        renderJson(R.ok());
    }

    /**
     * bops系统获取商品信息
     * @author yue.li
     */
    public void getProductInfo() {
        try{
            logger.info("getProductInfo开始执行");
            Map<String, Integer> categoryMap = crmBusinessService.getCategoryMap();
            for(ProductCategoryEnum productCategoryEnum : ProductCategoryEnum.values()) {
                String response =  HttpUtil.get(esbConfig.getProductListUrl(), queryParasMap(productCategoryEnum.getTypes()), esbConfig.getProductListHeader());
                logger.info(String.format("getProductInfo msg:%s",response));
                if(StringUtils.isNotEmpty(response)) {
                    BopsProductResponse productResponse = JSONObject.parseObject(response, BopsProductResponse.class);
                    if(StringUtils.isNotEmpty(productResponse.getCode()) && productResponse.getCode().equals(CrmConstant.CODE) && Objects.nonNull(productResponse.getData())) {
                        List<CrmProductDetail> resultList = productResponse.getData().getContent();
                        crmBusinessService.dealProductList(resultList,categoryMap.get(productCategoryEnum.getTypes()));
                    }
                }
            }
            logger.info("getProductInfo结束执行");
        } catch(Exception e) {
            logger.error(String.format("getProductInfo error msg:%s", BaseUtil.getExceptionStack(e)));
        }
        renderJson(R.ok());
    }

    /**
     * 封装查询商品map
     * @author yue.li
     * @param category 商品大类
     * @return
     */
    public Map<String, String> queryParasMap(String category) {
        Map<String, String> queryParas = new HashMap<String, String>(3);
        queryParas.put("category", category);
        queryParas.put("pageNo", CrmConstant.PAGE_NO);
        queryParas.put("pageSize",CrmConstant.PAGE_SIZE);
        return queryParas;
    }

}
