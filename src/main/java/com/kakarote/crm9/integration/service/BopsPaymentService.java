package com.kakarote.crm9.integration.service;

import com.google.common.collect.Lists;
import com.jfinal.kit.Kv;
import com.jfinal.log.Log;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Bops Payment Service
 *
 * @author hao.fu
 * @since 2019/10/16 17:22
 */
public class BopsPaymentService {

    private Log logger = Log.getLog(getClass());

    public boolean batchSaveBopsPayment(List<Record> paymentList) {
        if (CollectionUtils.isEmpty(paymentList)) {
            return true;
        }
        logger.info(String.format("batchSaveBopsPayment, record size: %s", paymentList.size()));
        return Db.batchSave("72crm_bops_payment", paymentList, paymentList.size()).length > 0;
    }

    public List<Record> getUnmatchedPayment() {
        return Db.find(Db.getSql("crm.payment.getUnmatchedPayment"));
    }

    public boolean batchUpdateBopsPayment(List<Record> paymentList) {
        if (CollectionUtils.isEmpty(paymentList)) {
            return true;
        }
        return Db.batchUpdate("72crm_bops_payment", paymentList, paymentList.size()).length > 0;
    }

    public List<String> getAllPaymentNos() {
        List<Record> ids = Db.find(Db.getSql("crm.payment.getAllPaymentNos"));
        if (CollectionUtils.isNotEmpty(ids)) {
            return ids.stream().map(item -> item.getStr("payment_no")).collect(Collectors.toList());
        }
        return null;
    }

    public List<Record> getNoOrderPayment() {
        return Db.find(Db.getSql("crm.payment.getNoOrderPayment"));
    }

    public List<Record> getPaymentsByPaymentNo(List<String> paymentNos) {
        if (CollectionUtils.isEmpty(paymentNos)) {
            return Lists.newArrayList();
        }
        return Db.find(Db.getSqlPara("crm.payment.getPaymentsByPaymentNo", Kv.by("ids", paymentNos)));
    }

    public List<Record> getPayTypeNullPayment() {
        return Db.find(Db.getSql("crm.payment.getPayTypeNullPayment"));
    }

    public List<Record> getPaymentWithouOrderNoAndPayType() {
        return Db.find(Db.getSql("crm.payment.getPaymentWithouOrderNoAndPayType"));
    }


}
