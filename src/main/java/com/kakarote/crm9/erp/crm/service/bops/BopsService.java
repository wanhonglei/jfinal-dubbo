package com.kakarote.crm9.erp.crm.service.bops;

import com.alibaba.rocketmq.shade.com.alibaba.fastjson.JSON;
import com.google.common.collect.Maps;
import com.jfinal.aop.Inject;
import com.jfinal.plugin.activerecord.Record;
import com.kakarote.crm9.common.exception.CrmException;
import com.kakarote.crm9.erp.admin.entity.AdminUser;
import com.kakarote.crm9.erp.admin.service.AdminUserService;
import com.kakarote.crm9.erp.crm.common.BopsOrderChannelEnum;
import com.kakarote.crm9.erp.crm.constant.BopsConstant;
import com.kakarote.crm9.erp.crm.service.CrmSiteMemberService;
import com.kakarote.crm9.erp.crm.vo.BackendOrderInfo;
import com.kakarote.crm9.erp.crm.vo.BackendOrderPager;
import com.kakarote.crm9.erp.crm.vo.BackendOrderResponse;
import com.kakarote.crm9.integration.common.EsbConfig;
import com.kakarote.crm9.utils.HttpUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @Author: haihong.wu
 * @Date: 2020/6/5 5:06 下午
 */
@Slf4j
public class BopsService {

    /*
     * 客户订单切割日期（用户领取客户时判断进单金额）
     */
    public static final String CUSTOMER_ORDER_CUT_DATE_STR = "2020-03-12";

    public static final BigDecimal DAYS_OF_YEAR = BigDecimal.valueOf(365);

    @Inject
    private AdminUserService adminUserService;

    @Inject
    private CrmSiteMemberService crmSiteMemberService;

    /**
     * 由于3月12号订单逻辑发生了变化
     * 包括3月12号之前的订单
     * 1、下单来源为PC端、微信H5、APP、微信小程序的订单实付金额统计为归属于在线运营事业部的付费订单金额
     * 2、下单来源为OA的订单，其中下单人属于”在线运营事业部“的订单，统计为归属于在线运营事业部的付费订单金额
     * 3月12号之后的订单
     * 订单的实付金额统计为归属于在线运营事业部的付费订单金额
     */
    public BigDecimal getCustomerOrderAmount(Long customerId, Long onlineBusinessDeptId, EsbConfig esbConfig) {
        //请求esb接口
        List<Record> orderDatas = requestEsbCustomerOrderList(customerId, esbConfig);
        //计算结果
        LocalDate cutDate = LocalDate.parse(CUSTOMER_ORDER_CUT_DATE_STR, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        List<Long> orderSources = BopsOrderChannelEnum.listOnlineBusinessCodes();
        return orderDatas.stream().reduce(BigDecimal.ZERO, (sum, record) -> {
            LocalDate payDate = record.get("payDate");
            Long source = record.getLong("source");
            BigDecimal payAmount = record.getBigDecimal("payAmount");
            if (payDate == null || source == null || payAmount == null) {
                throw new CrmException(String.format("获取进单数据失败[%s为空]", payDate == null ? "支付时间" : source == null ? "下单来源" : "支付金额"));
            }
            if (payDate.compareTo(cutDate) <= 0) {
                if (BopsOrderChannelEnum.OA.getCode().equals(source)) {
                    String orderBd = record.getStr("orderBd");
                    if (StringUtils.isEmpty(orderBd)) {
                        log.error("{} {} exist OA order but staffNo is null [customerId:{} ,orderId:{}]", getClass().getSimpleName(), "getCustomerOrderAmount", customerId, record.getLong("orderId"));
                        //略过这条数据
                        return sum;
                    }
                    AdminUser orderBdUser = adminUserService.getUserByStaffNo(orderBd);
                    if (orderBdUser == null) {
                        log.error("{} {} staff not exist in crm [customerId:{} ,orderId:{} ,staffId:{}]", getClass().getSimpleName(), "getCustomerOrderAmount", customerId, record.getLong("orderId"), orderBd);
                        //略过这条数据
                        return sum;
                    }
                    Long bdBusinessDeptId = adminUserService.getBusinessDepartmentOfUserById(orderBdUser.getUserId());
                    if (Objects.equals(onlineBusinessDeptId, bdBusinessDeptId)) {
                        return sum.add(payAmount);
                    }
                    return sum;
                } else if (orderSources.contains(source)) {
                    return sum.add(payAmount);
                }
            } else {
                return sum.add(payAmount);
            }
            return sum;
        }, (n1, n2) -> null);
    }

    private List<Record> requestEsbCustomerOrderList(Long customerId, EsbConfig esbConfig) {
        List<Record> datas = new ArrayList<>();

        List<Long> siteMemberIds = crmSiteMemberService.listSiteMemberIdsByCustomerId(customerId);
        if (CollectionUtils.isEmpty(siteMemberIds)) {
            return datas;
        }
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        //组装参数
        Map<String, String> param = Maps.newHashMapWithExpectedSize(7);
        param.put("userIds", StringUtils.join(siteMemberIds, ","));
        LocalDateTime now = LocalDateTime.now();
        param.put("beginPayTime", now.minusDays(DAYS_OF_YEAR.longValue()).toLocalDate().atStartOfDay().format(timeFormatter));
        param.put("endPayTime", now.format(timeFormatter));
        param.put("test", "false");
        param.put("channelList", StringUtils.join(BopsOrderChannelEnum.listOnlineBusinessCodes(), ","));
        int pageSize = 100;
        param.put("size", String.valueOf(pageSize));
        int page = 1;
        int pageCount;
        try {
            do {
                param.put("page", String.valueOf(page));
                //请求esb接口
                long st = System.currentTimeMillis();
                String result = HttpUtil.get(esbConfig.getBopsCustomerOrderListUrl(), param, esbConfig.getBopsCustomerOrderListHeader());
                log.info("BOPS request cost {} ms", System.currentTimeMillis() - st);
                BackendOrderResponse response = JSON.parseObject(result, BackendOrderResponse.class);
                if (!BopsConstant.RESULT_CODE.OK.equals(response.getCode())) {
                    log.error("{} {} response fail[code:{} ,msg:{}]", getClass().getSimpleName(), "requestEsbCustomerOrderList", response.getCode(), response.getMsg());
                    throw new CrmException("请求BOPS接口失败");
                }
                BackendOrderPager backendOrderPager = response.getData();
                pageCount = backendOrderPager.getTotalPages();
                List<BackendOrderInfo> data = backendOrderPager.getContent();
                if (data != null) {
                    datas.addAll(data.stream().map(item -> {
                                if (item.getPayTime() == null || item.getTotalPay() == null || item.getChannel() == null) {
                                    String nullProp = item.getPayTime() == null ? "支付时间" : item.getTotalPay() == null ? "付款金额" : "下单渠道";
                                    log.error("requestEsbCustomerOrderList BOPS order data error->{} is null.[customerId:{},userId:{},orderId:{}]", nullProp, customerId, item.getUserId(), item.getOrderId());
                                    throw new CrmException(String.format("BOPS订单数据异常[%s为空]", nullProp));
                                }
                                return new Record()
                                        .set("payDate", LocalDateTime.parse(item.getPayTime(), timeFormatter).toLocalDate())
                                        .set("payAmount", item.getTotalPay())
                                        .set("source", Long.valueOf(item.getChannel()))
                                        .set("orderBd", item.getStaffNo())
                                        .set("orderId", item.getOrderId());
                            }
                    ).collect(Collectors.toList()));
                }
            } while (page++ < pageCount);
        } catch (CrmException e) {
            throw e;
        } catch (Exception e) {
            throw new CrmException("请求BOPS接口异常", e);
        }
        return datas;
    }
}
