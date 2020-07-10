package com.kakarote.crm9.erp.crm.service;

import com.jfinal.aop.Before;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.kakarote.crm9.erp.crm.entity.CrmContract;
import com.qxwz.merak.billing.installment.model.response.CreateInstallmentBillBatchModel;
import com.qxwz.merak.billing.installment.model.response.CreateInstallmentBillExecRstModel;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 付款条款
 * @Author: haihong.wu
 * @Date: 2020/6/15 4:34 下午
 */
@Slf4j
public class CrmContractPaymentService {

    /**
     * 再同步完履约系统之后，根据返回的信息，更新crm付款条款的同步状态
     * @param model
     */
    @Before(Tx.class)
    public void updatePaymentAfterAgreement(CreateInstallmentBillBatchModel model){
        if (model == null){
            log.error("履约返回数据异常");
            return;
        }

        Date date = new Date();
        List<CreateInstallmentBillExecRstModel> rstModels = model.getSuccessExecRstModels();
        if (CollectionUtils.isNotEmpty(rstModels)){
            List<String> contractNoList = rstModels.stream().map(CreateInstallmentBillExecRstModel::getParentBizNo).collect(Collectors.toList());
            List<CrmContract> contractList = CrmContract.dao.findListWithColValues("contract_num", contractNoList);
            contractList.forEach(o -> {
                //已同步
                o.setSyncStatus(1);
                o.setSyncTime(date);
            });
            Db.batchUpdate(contractList,contractList.size());
        }

        rstModels = model.getFailedExecRstModels();
        if (CollectionUtils.isNotEmpty(rstModels)){
            rstModels.forEach(o -> log.error("履约同步部分合同数据失败：contractNum：{},errorCode：{},errorMsg：{}",o.getParentBizNo(),o.getErrorCode(),o.getErrorMsg()));
        }
    }
}
