package com.kakarote.crm9.mobile.controller;


import com.alibaba.fastjson.JSON;
import com.google.common.collect.Maps;
import com.jfinal.aop.Before;
import com.jfinal.aop.Inject;
import com.jfinal.core.Controller;
import com.jfinal.core.paragetter.Para;
import com.jfinal.kit.HttpKit;
import com.jfinal.log.Log;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.kakarote.crm9.common.annotation.CrmEventAnnotation;
import com.kakarote.crm9.common.annotation.NotNullValidate;
import com.kakarote.crm9.common.config.JfinalConfig;
import com.kakarote.crm9.common.config.paragetter.BasePageRequest;
import com.kakarote.crm9.common.spring.IocInterceptor;
import com.kakarote.crm9.erp.crm.acl.user.CrmUser;
import com.kakarote.crm9.erp.crm.common.CrmEventEnum;
import com.kakarote.crm9.erp.crm.constant.CrmConstant;
import com.kakarote.crm9.erp.crm.entity.LocationInfo;
import com.kakarote.crm9.erp.crm.service.CrmSignInService;
import com.kakarote.crm9.integration.common.EsbConfig;
import com.kakarote.crm9.mobile.common.MobileUtil;
import com.kakarote.crm9.mobile.entity.MobileNewSignInRequest;
import com.kakarote.crm9.mobile.entity.MobileSignInListRequest;
import com.kakarote.crm9.mobile.service.MobileSignInService;
import com.kakarote.crm9.utils.BaseUtil;
import com.kakarote.crm9.utils.OssPrivateFileUtil;
import com.kakarote.crm9.utils.R;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Objects;

/**
 * Mobile Sign In Controller
 *
 * @author hao.fu
 * @since 2019/12/23 14:04
 */
@Before(IocInterceptor.class)
public class MobileSignInController extends Controller {

    private Log logger = Log.getLog(getClass());

    @Autowired
    private EsbConfig esbConfig;

    @Inject
    private CrmSignInService crmSignInService;

    @Inject
    private MobileSignInService mobileSignInService;

    @Autowired
    private OssPrivateFileUtil ossPrivateFileUtil;

    /**
     * 获取签到列表
     *
     * @param basePageRequest
     */
    public void getSigninList(BasePageRequest<MobileSignInListRequest> basePageRequest){
        try{
            Page<Record> records = mobileSignInService.queryPageList(basePageRequest, MobileUtil.getCrmUser(getRequest()));
            renderJson(R.ok().put("data", records));
        }catch (Exception e){
            logger.error(String.format("MobileSignInController getSigninList msg:%s", BaseUtil.getExceptionStack(e)));
            renderJson(R.error(e.getMessage()));
        }
    }

    /**
     * 新建签到
     */
    @CrmEventAnnotation(crmEventEnum = CrmEventEnum.LATELY_FOLLOW_EVENT)
    public void addSignIn() {
        try{
            CrmUser user = MobileUtil.getCrmUser(getRequest());
            MobileNewSignInRequest request = JSON.parseObject(getRawData(), MobileNewSignInRequest.class);
            if(StringUtils.isEmpty(request.getAddress())) {
                renderJson(R.error("add sign address is null!"));
            }else{
                R r = crmSignInService.addSignInRecord(request, user);
                renderJson(r.isSuccess() ? R.ok() : R.error("add sign in record failed!"));
            }
        }catch (Exception e){
            logger.error(String.format("MobileSignInController addSignIn msg:%s", BaseUtil.getExceptionStack(e)));
            renderJson(R.error(e.getMessage()));
        }
    }

    /**
     * 根据经纬度和客户id匹配CRM库中的客户地址
     */
    @NotNullValidate(value = "latitude",message = "纬度不能为空")
    @NotNullValidate(value = "longitude",message = "经度不能为空")
    public void getLocationInfo(@Para("latitude")Double latitude, @Para("longitude")Double longitude) {
        try{
            CrmUser user = MobileUtil.getCrmUser(getRequest());

            if (Objects.nonNull(user) && Objects.nonNull(user.getCrmAdminUser())) {
                logger.info("mobile controller getLocationInfo, user info: " + user.getCrmAdminUser());
            } else {
                logger.info("mobile controller getLocationInfo, user is null");
                renderJson(R.error(CrmConstant.NO_CRM_PERMISSION));
                return;
            }
            if(Objects.isNull(latitude) || Objects.isNull(longitude)) {
                renderJson(R.error("param is null"));
                return;
            }

            BigDecimal lat = BigDecimal.valueOf(latitude);
            latitude = lat.setScale(6, BigDecimal.ROUND_UP).doubleValue();
            logger.info("sign in latitude in controller: " + latitude);

            BigDecimal logi = BigDecimal.valueOf(longitude);
            longitude = logi.setScale(6, BigDecimal.ROUND_UP).doubleValue();
            logger.info("sign in longitude in controller: " + longitude);

            LocationInfo record = crmSignInService.getLocation(user, latitude, longitude);

            Map<String, String> paras = Maps.newHashMap();
            paras.put(CrmConstant.AMAP_AROUND_POI_PARAM_LOCATION, longitude + "," + latitude);
            paras.put(CrmConstant.AMAP_AROUND_POI_PARAM_RADIUS, JfinalConfig.crmProp.get(CrmConstant.COLUMBUS_AMAP_AROUND_POI_RADIUS_KEY));
            paras.put(CrmConstant.AMAP_AROUND_POI_PARAM_TYPES, JfinalConfig.crmProp.get(CrmConstant.COLUMBUS_AMAP_AROUND_POI_TYPES_KEY));
            String url = esbConfig.getAmapAroundPoiUrl();
            logger.info("get location url: " + url);
            String rsps = HttpKit.get(url, paras, esbConfig.getAmapAroundPoiHeader());
            record = crmSignInService.assembleLocationInfo(record, JSON.parseObject(rsps));
            renderJson(R.ok().put("data", record));
        }catch (Exception e){
            logger.error(String.format("MobileSignInController getLocationInfo msg:%s", BaseUtil.getExceptionStack(e)));
            renderJson(R.error(e.getMessage()));
        }
    }

    /**
     * 根据签到id获取签到详情
     * @param signinId
     */
    @NotNullValidate(value = "signinId",message = "签到id不能为空")
    public void getSigninDetail(@Para("signinId")String signinId) {
        try{
            renderJson(R.ok().put("data", crmSignInService.getSigninDetail(signinId,ossPrivateFileUtil)));
        }catch (Exception e){
            logger.error(String.format("MobileSignInController getSigninDetail msg:%s", BaseUtil.getExceptionStack(e)));
            renderJson(R.error(e.getMessage()));
        }
    }

    /**
     * 根据签到id删除签到
     * @param signinId
     */
    @NotNullValidate(value = "signinId",message = "签到id不能为空")
    public void deleteSignin(@Para("signinId")String signinId) {
        try{
            renderJson(R.ok().put("data", crmSignInService.deleteSigninBySigninId(signinId)));
        }catch (Exception e){
            logger.error(String.format("MobileSignInController getSigninDetail msg:%s", BaseUtil.getExceptionStack(e)));
            renderJson(R.error(e.getMessage()));
        }
    }
}
