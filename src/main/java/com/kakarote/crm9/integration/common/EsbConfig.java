package com.kakarote.crm9.integration.common;

import com.google.common.collect.Maps;
import com.kakarote.crm9.common.config.JfinalConfig;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

/**
 * Esb Config.
 *
 * @author hao.fu
 * @since 2019/6/26 15:36
 */
@Data
@NoArgsConstructor
public class EsbConfig {

    private String deptSyncUrl;

    private String staffSyncUrl;

    private String esbOaUser;

    private String esbOaPassword;

    private String esbHost;

    private String esbPort;

    private String esbBopsPort;

    private String crmEsbClientId;

    private String opCodeSyncDept;

    private String opCodeSyncStaff;

    private String opCodeTagDetails;

    private String opCodeProductList;

    private String opCodePaymentList;

    private String opCodePaymentNoList;

    private String opCodeCrmWorkflow;

    private String tagDetailsUrl;

    private String esbBopsUser;

    private String esbBopsPassword;

    private String productListUrl;

    private String paymentListUrl;

    private String paymentNoListUrl;

    private String createCrmWorkflowUrl;

    private String amapDeGeoUrl;

    private String opCodeAmapDegeo;

    private String amapAroundPoiUrl;

    private String bopsCustomerOrderListUrl;

    private String bopsCustomerOrderListCode;

    private String bopsCustomerOrderClientId;

    private String opCodeAmapAroundPoi;

    private String esbAmapUser;

    private String esbAmapPassword;


    private Map<String, String> buildEsbHeader(String operationCode, String esbUser, String esbPw) {
        return buildEsbHeader(crmEsbClientId, operationCode, esbUser, esbPw);
    }

    private Map<String, String> buildEsbHeader(String clientId,String operationCode, String esbUser, String esbPw) {
        String authUp = String.format("%s:%s", esbUser, esbPw);
        String auth = String.format("Basic %s", Base64.getEncoder().encodeToString(authUp.getBytes(StandardCharsets.UTF_8)));
        Map<String, String> map = Maps.newHashMap();
        map.put("Content-Type", "application/json; charset=utf-8");
        map.put("ClientId", clientId);
        map.put("OperationCode", operationCode);
        map.put("Authorization", auth);
        map.put("Timestamp", String.valueOf(System.currentTimeMillis() / 1000));
        return map;
    }

    private String buildUrl(String path) {
        return String.format("http://%s:%s/%s", esbHost, esbPort, path);
    }

    private String buildBopsUrl(String path) {
        return String.format("http://%s:%s/%s", esbHost, esbBopsPort, path);
    }


    public String getSyncStaffUrl() {
        return buildUrl(staffSyncUrl);
    }

    public String getSyncDeptUrl() {
        return buildUrl(deptSyncUrl);
    }

    public String getBopsPaymentListUrl() {
        return buildBopsUrl(paymentListUrl);
    }

    public String getBopsUpdatedPaymentUrl() {return buildBopsUrl(paymentNoListUrl);}

    public String getAmapDeGeoUrl() {
        return buildUrl(amapDeGeoUrl);
    }

    public String getAmapAroundPoiUrl() {
        return buildUrl(amapAroundPoiUrl);
    }

    public Map<String, String> getBopsPaymentListHeader() {
        return buildEsbHeader(opCodePaymentList, esbBopsUser, esbBopsPassword);
    }

    public Map<String, String> getBopsUpdatedPaymentListHeader() {
        return buildEsbHeader(opCodePaymentNoList, esbBopsUser, esbBopsPassword);
    }

    public Map<String, String> getSyncStaffHeader() {
        return buildEsbHeader(opCodeSyncStaff, esbOaUser, esbOaPassword);
    }

    public Map<String, String> getSyncDeptHeader() {
        return buildEsbHeader(opCodeSyncDept, esbOaUser, esbOaPassword);
    }

    public Map<String, String> getTagDetailsHeader() {
        return buildEsbHeader(opCodeTagDetails, esbBopsUser, esbBopsPassword);
    }

    public Map<String, String> getProductListHeader() {
        return buildEsbHeader(opCodeProductList, esbBopsUser, esbBopsPassword);
    }

    public Map<String, String> getCrmWorkflowHeader() {
        return buildEsbHeader(opCodeCrmWorkflow, esbOaUser, esbOaPassword);
    }

    public Map<String, String> getAmapDeGeoHeader() {
        return buildEsbHeader(opCodeAmapDegeo, esbAmapUser, esbAmapPassword);
    }

    public Map<String, String> getAmapAroundPoiHeader() {
        return buildEsbHeader(opCodeAmapAroundPoi, esbAmapUser, esbAmapPassword);
    }

    public Map<String, String> getBopsCustomerOrderListHeader() {
        return buildEsbHeader(bopsCustomerOrderClientId, bopsCustomerOrderListCode, esbBopsUser, esbBopsPassword);
    }

    public String getTagDetailsUrl() {
        return buildBopsUrl(tagDetailsUrl);
    }

    public String getCrmWorkFlowUrl() {
        return buildUrl(createCrmWorkflowUrl);
    }

    public String getProductListUrl() {
        return buildBopsUrl(productListUrl);
    }

    public String getBopsCustomerOrderListUrl() {
        return buildBopsUrl(bopsCustomerOrderListUrl);
    }
    /**
     * ESB 接口参数枚举，除了用户名和密码从静态配置取，其他直接从枚举中取才合适
     */
    public enum OrderApiEnum {
        /**
         * order 接口枚举
         */
        ORDER_QUERY("/orderbackend/esb/order/query", "com.primeton.esb.producer.orderbackend.orderPageQuery", "查询订单列表"),
        INSTANCE_QUERY("/orderbackend/esb/instance/query", "com.primeton.esb.producer.orderbackend.instandePageQuery", "查询服务实例列表"),
        SUMMARY_QUERY("/orderbackend/esb/order/summary/query", "com.primeton.esb.producer.orderbackend.orderSummaryQuery", "订单列表总金额查询"),
        ;

        private String url;
        private String operateCode;
        private String apiDesc;

        private static String esbHost = JfinalConfig.crmProp.get("esb.host");
        private static String esbPort = JfinalConfig.crmProp.get("esb.order.port");

        private static String user = JfinalConfig.crmProp.get("esb.order.user");
        private static String passWd = JfinalConfig.crmProp.get("esb.order.password");

        OrderApiEnum(String url,String operateCode,String apiDesc){
            this.url = url;
            this.operateCode = operateCode;
            this.apiDesc = apiDesc;
        }

        public String getUrl() {
            return String.format("http://%s:%s%s", esbHost, esbPort, url);
        }

        public String getOperateCode() {
            return operateCode;
        }

        public String getApiDesc() {
            return apiDesc;
        }

        public Map<String, String> buildEsbHeader() {
            String authUp = String.format("%s:%s", user, passWd);
            String auth = String.format("Basic %s", Base64.getEncoder().encodeToString(authUp.getBytes(StandardCharsets.UTF_8)));
            Map<String, String> map = Maps.newHashMap();
            map.put("Content-Type", "application/json; charset=utf-8");
            map.put("ClientId", "com.primeton.esb.consumer.crm");
            map.put("OperationCode", operateCode);
            map.put("Authorization", auth);
            map.put("Timestamp", String.valueOf(System.currentTimeMillis() / 1000));
            return map;
        }
    }

    public enum BopsApiEnum {
        /**
         * bops 接口枚举
         */
        QUERY_INVOICE_RECORD_PAGE("/bops/query-invoice-record-page",  "com.primeton.esb.producer.invoicebackend.invoiceRecordPageQuery", "查询发票列表信息"),
        QUERY_INVOICE_RECORD_TOTAL_AMOUNT("/bops/query-invoice-record-total-amount", "com.primeton.esb.producer.invoicebackend.invoiceRecordTotalAmountQuery", "查询发票信息列表总金额"),
        QUERY_REDEEM_CODE("/bops/redeem/search/new", "com.primeton.esb.producer.bops.redeem.search.new", "查询兑换码信息"),
        QUERY_PAYMENT_INFORMATION("/bops/payment/info/query", "com.primeton.esb.producer.bops.payment.info.query", "查询支付信息"),
        QUERY_COUPONS("/bops/query-page-coupons", "com.primeton.esb.producer.bops.query.page.coupons", "查询优惠券信息接口"),
        ORDER_DETAIL_QUERY("/bops/order/info", "com.primeton.esb.producer.bops.order.info", "根据订单号获取BOPS订单信息"),
        ;

        private String url;
        private String operateCode;
        private String apiDesc;

        private static String esbHost = JfinalConfig.crmProp.get("esb.host");
        private static String esbPort = JfinalConfig.crmProp.get("esb.bops.port");

        private static String user = JfinalConfig.crmProp.get("esb.bops.user");
        private static String passWd = JfinalConfig.crmProp.get("esb.bops.password");

        BopsApiEnum(String url,String operateCode,String apiDesc){
            this.url = url;
            this.operateCode = operateCode;
            this.apiDesc = apiDesc;
        }

        public String getUrl() {
            return String.format("http://%s:%s%s", esbHost, esbPort, url);
        }

        public String getOperateCode() {
            return operateCode;
        }

        public String getApiDesc() {
            return apiDesc;
        }

        public Map<String, String> buildEsbHeader() {
            String authUp = String.format("%s:%s", user, passWd);
            String auth = String.format("Basic %s", Base64.getEncoder().encodeToString(authUp.getBytes(StandardCharsets.UTF_8)));
            Map<String, String> map = Maps.newHashMap();
            map.put("Content-Type", "application/json; charset=utf-8");
            map.put("ClientId", "com.primeton.esb.consumer.crm");
            map.put("OperationCode", operateCode);
            map.put("Authorization", auth);
            map.put("Timestamp", String.valueOf(System.currentTimeMillis() / 1000));
            return map;
        }
    }

    /**
     * 快速注册接口枚举
     */
    public enum FastRegisterEnum {
        /**
         * 快速注册接口枚举
         */
        CREATE_BATCH_INSTALLMENT_BILL("/org.gocom.esb.rest.route/billing/create-batch-installment-bill","同步分期账单"),
        ;

        private String url;
        private String apiDesc;

        private static String esbHost = JfinalConfig.crmProp.get("esb.host");
        private static String esbFastPort = JfinalConfig.crmProp.get("esb.rest.port");

        FastRegisterEnum(String url,String apiDesc){
            this.url = url;
            this.apiDesc = apiDesc;
        }

        public String getUrl() {
            return String.format("http://%s:%s%s", esbHost, esbFastPort, url);
        }

        public String getApiDesc() {
            return apiDesc;
        }

        public Map<String, String> buildEsbHeader() {
            Map<String, String> map = Maps.newHashMap();
            map.put("Content-Type", "application/json; charset=utf-8");
            map.put("Timestamp", String.valueOf(System.currentTimeMillis() / 1000));
            return map;
        }
    }

}
