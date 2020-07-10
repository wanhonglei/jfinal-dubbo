package com.kakarote.crm9.integration.client;

import cn.hutool.core.util.RandomUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONValidator;
import com.jfinal.kit.JsonKit;
import com.kakarote.crm9.common.config.JfinalConfig;
import com.kakarote.crm9.common.exception.CrmException;
import com.kakarote.crm9.integration.common.EsbConfigs;
import com.kakarote.crm9.integration.dto.PerformancePlanDto;
import com.kakarote.crm9.integration.dto.PerformanceVerificationDto;
import com.kakarote.crm9.utils.Assert;
import com.kakarote.crm9.utils.HttpUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 履约客户端
 * 在这个类中调用履约的接口
 *
 * @Author: haihong.wu
 * @Date: 2020/6/15 2:07 下午
 */
@Slf4j
public class PerformanceClient {
    /**
     * 远程访问请求id
     */
    private static final String PARAM_KEY_REQUEST_ID = "requestId";
    /**
     * 调用系统标识
     */
    private static final String PARAM_KEY_CALLER = "caller";
    /**
     * 参数
     */
    private static final String PARAM_KEY_PARAM = "param";
    /**
     * 合同回款计划号
     */
    private static final String PARAM_KEY_BIZ_NO = "bizNo";
    /**
     * 合同回款计划号列表
     */
    private static final String PARAM_KEY_BIZ_NO_MODEL = "bizNoExtModels";
    /**
     * 合同号
     */
    private static final String PARAM_KEY_CONTRACT_NO = "parentBizNo";
    /**
     * 业务类型：contract_fulfill-合同
     */
    private static final String PARAM_KEY_BIZ_TYPE = "bizType";

    private static final String RESULT_KEY_MESSAGE = "message";
    private static final String RESULT_KEY_SUCCESS = "success";
    private static final String RESULT_KEY_DATA = "data";
    private static final String RESULT_KEY_LIST = "list";

    /**
     * 调用系统标识
     */
    private static final String CALLER = JfinalConfig.crmProp.get("performance.caller");
    /**
     * 业务类型：contract_fulfill-合同
     */
    private static final String BIZ_TYPE = "contract_fulfill";

    /**
     * 查询分期账单列表
     *
     * @param contractNo 合同编号
     * @param planNos    付款条款编号
     * @return 分期账单列表
     */
    public List<PerformancePlanDto> listInstallmentBill(String contractNo, String... planNos) {
        Assert.isFalse(StringUtils.isBlank(contractNo) && (planNos == null || planNos.length == 0), "合同编号和付款条款编号必须填一个");
        /* URL */
        String url = PerformanceApi.getUrl(PerformanceApi.LIST_INSTALLMENT_BILL_URL);
        /* 组装BODY */
        JSONObject body = buildBaseBody();
        JSONObject param = new JSONObject();
        if (planNos != null && planNos.length > 0) {
            param.put(PARAM_KEY_BIZ_NO_MODEL, Arrays.stream(planNos).map(planNo -> new JSONObject().fluentPut(PARAM_KEY_BIZ_NO, planNo).fluentPut(PARAM_KEY_BIZ_TYPE, BIZ_TYPE)).collect(Collectors.toList()));
        }
        if (StringUtils.isNotBlank(contractNo)) {
            param.put(PARAM_KEY_CONTRACT_NO, contractNo);
            param.put(PARAM_KEY_BIZ_TYPE, BIZ_TYPE);
        }
        body.put(PARAM_KEY_PARAM, param);
        /* 组装Header */
        Map<String, String> headers = HttpUtil.buildJsonHeader(null);
        /* 请求接口 */
        log.info("---请求履约侧接口[查询分期账单列表]开始---");
        log.info("---Url:{}---", url);
        log.info("---Header:{}---", JsonKit.toJson(headers));
        log.info("---Body:{}---", JsonKit.toJson(body));
        String apiResult;
        try {
            apiResult = HttpUtil.post(url, body.toJSONString(), headers);
        } catch (Exception e) {
            log.error("---请求履约接口异常---", e);
            throw new CrmException("请求履约接口异常[" + e.getMessage() + "]");
        }
        log.info("---Result:{}---", apiResult);
        log.info("---请求履约侧接口结束---");
        if (!JSONValidator.from(apiResult).validate()) {
            log.info("履约接口返回解析异常[param:[{}],result:[{}]]", JsonKit.toJson(body), apiResult);
            throw new CrmException("履约接口异常");
        }
        /* 解析返回结果 */
        JSONObject apiResultJ = JSON.parseObject(apiResult);
        if (!apiResultJ.getBooleanValue(RESULT_KEY_SUCCESS)) {
            throw new CrmException("履约接口返回异常:" + apiResultJ.getString(RESULT_KEY_MESSAGE));
        }
        JSONObject dataJ = apiResultJ.getJSONObject(RESULT_KEY_DATA);
        if (Objects.nonNull(dataJ)) {
            JSONArray listJ = dataJ.getJSONArray(RESULT_KEY_LIST);
            if (Objects.nonNull(listJ) && listJ.size() > 0) {
                return listJ.toJavaList(PerformancePlanDto.class);
            }
        }
        return null;
    }

    /**
     * 查询分期核销单列表(合同编号、付款条款编号必传一个)
     *
     * @param contractNo 合同编号
     * @param planNo     付款条款编号
     * @return
     */
    public List<PerformanceVerificationDto> listInstallmentVerification(String contractNo, String planNo) {
        Assert.isFalse(StringUtils.isBlank(contractNo) && StringUtils.isBlank(planNo), "合同编号和付款条款编号必须填一个");
        /* URL */
        String url = PerformanceApi.getUrl(PerformanceApi.LIST_INSTALLMENT_VERIFICATION_URL);
        /* 组装BODY */
        JSONObject body = buildBaseBody();
        JSONObject param = new JSONObject();
        param.put(PARAM_KEY_BIZ_NO, planNo);
        param.put(PARAM_KEY_CONTRACT_NO, contractNo);
        param.put(PARAM_KEY_BIZ_TYPE, BIZ_TYPE);
        body.put(PARAM_KEY_PARAM, param);
        /* 组装Header */
        Map<String, String> headers = HttpUtil.buildJsonHeader(null);
        /* 请求接口 */
        log.info("---请求履约侧接口[查询分期核销单列表]开始---");
        log.info("---Url:{}---", url);
        log.info("---Header:{}---", JsonKit.toJson(headers));
        log.info("---Body:{}---", JsonKit.toJson(body));
        String apiResult;
        try {
            apiResult = HttpUtil.post(url, body.toJSONString(), headers);
        } catch (Exception e) {
            log.error("---请求履约接口异常---", e);
            throw new CrmException("请求履约接口异常[" + e.getMessage() + "]");
        }
        log.info("---Result:{}---", apiResult);
        log.info("---请求履约侧接口结束---");
        if (!JSONValidator.from(apiResult).validate()) {
            log.info("履约接口返回解析异常[param:[{}],result:[{}]]", JsonKit.toJson(body), apiResult);
            throw new CrmException("履约接口异常");
        }
        /* 解析返回结果 */
        JSONObject apiResultJ = JSON.parseObject(apiResult);
        if (!apiResultJ.getBooleanValue(RESULT_KEY_SUCCESS)) {
            throw new CrmException("履约接口返回异常:" + apiResultJ.getString(RESULT_KEY_MESSAGE));
        }
        JSONObject dataJ = apiResultJ.getJSONObject(RESULT_KEY_DATA);
        if (Objects.nonNull(dataJ)) {
            JSONArray listJ = dataJ.getJSONArray(RESULT_KEY_LIST);
            if (Objects.nonNull(listJ) && listJ.size() > 0) {
                return listJ.toJavaList(PerformanceVerificationDto.class);
            }
        }
        return null;
    }

    private JSONObject buildBaseBody() {
        JSONObject body = new JSONObject();
        body.put(PARAM_KEY_REQUEST_ID, CALLER + System.currentTimeMillis() + RandomUtil.randomNumbers(4));
        body.put(PARAM_KEY_CALLER, CALLER);
        return body;
    }

    private static class PerformanceApi {
        private static final String SYS_CODE = "org.gocom.esb.rest.route";
        /**
         * 查询分期账单列表
         */
        public static final String LIST_INSTALLMENT_BILL_URL = JfinalConfig.crmProp.get("performance.url.bill");
        /**
         * 查询分期核销单列表
         */
        public static final String LIST_INSTALLMENT_VERIFICATION_URL = JfinalConfig.crmProp.get("performance.url.verification");

        public static String getUrl(String url) {
            return String.format("http://%s:%s/%s/%s", EsbConfigs.HOST, EsbConfigs.REST_PORT, SYS_CODE, url);
        }
    }
}
