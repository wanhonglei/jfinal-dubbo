package com.kakarote.crm9.erp.crm.service;

import cn.hutool.core.date.DateUtil;
import com.jfinal.kit.Kv;
import com.jfinal.log.Log;
import com.jfinal.plugin.activerecord.Db;
import com.kakarote.crm9.common.exception.CrmException;
import com.kakarote.crm9.erp.crm.common.PerformanceFromChannelEnum;
import com.kakarote.crm9.erp.crm.common.PerformanceTargetTypeEnum;
import com.kakarote.crm9.erp.crm.entity.CrmPerformance;
import com.kakarote.crm9.erp.crm.entity.CrmPerformanceLog;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Objects;

/**
 * @Author: haihong.wu
 * @Date: 2020/3/11 1:33 下午
 */
public class CrmPerformanceService {

    private final Log logger = Log.getLog(getClass());

    /**
     * 新增业绩
     *
     * @param objId                    对象ID
     * @param objType                  对象类型
     * @param currentPerformanceAmount 完成业绩增加金额
     * @param targetPerformanceAmount  目标业绩增加金额
     * @param fromChannel              来源渠道
     * @param fromId                   来源ID
     * @param targetType               目标类型
     * @param targetId                 目标ID
     */
    public void addPerformance(String batchId, Long objId, Integer objType, BigDecimal currentPerformanceAmount, BigDecimal targetPerformanceAmount, PerformanceFromChannelEnum fromChannel, Long fromId, PerformanceTargetTypeEnum targetType, Long targetId, String remark, Date createTime) {
        logger.info(String.format("%s %s: param:[objId:%s ,objType:%s ,currentPerformanceAmount:%s ,targetPerformanceAmount:%s ,fromChannel:%s ,fromId:%s ,targetType:%s ,targetId:%s,createTime:%s]", getClass().getSimpleName(), "addPerformance", objId, objType, currentPerformanceAmount, targetPerformanceAmount, fromChannel, fromId, targetId, targetType,createTime));
        if (objId == null || objType == null || (currentPerformanceAmount == null && targetPerformanceAmount == null)) {
            throw new CrmException("必要参数为空");
        }
        Db.tx(() -> {
            //初始化绩效记录(目前不需要处理业绩数据，只需要初始化即可)
            getPerformance(objId, objType);
            //计算员工绩效
            BigDecimal currentAmountChange = currentPerformanceAmount == null ? BigDecimal.ZERO : currentPerformanceAmount;
            BigDecimal targetAmountChange = targetPerformanceAmount == null ? BigDecimal.ZERO : targetPerformanceAmount;
            //插入日志
            new CrmPerformanceLog()
                    .setBatchId(batchId)
                    .setObjId(objId)
                    .setObjType(objType)
                    .setCurrentAmountChange(currentAmountChange)
                    .setTargetAmountChange(targetAmountChange)
                    .setFromChannel(fromChannel.getCode())
                    .setFromId(fromId)
                    .setTargetId(targetId)
                    .setTargetType(targetType.getCode())
                    .setRemark(remark)
                    .setGmtCreate(createTime == null ? DateUtil.date() : createTime)
                    .save();
            //更新员工业绩
            return Db.update(Db.getSqlPara("crm.performance.update", Kv.by("currentAmountChange", currentAmountChange).set("targetAmountChange", targetAmountChange).set("objId", objId).set("objType", objType))) == 1;
        });
    }

    /**
     * 获取绩效记录
     *
     * @param objId   对象ID
     * @param objType 对象类型 PerformanceObjectTypeEnum
     * @return
     */
    public CrmPerformance getPerformance(Long objId, Integer objType) {
        CrmPerformance performance = CrmPerformance.dao.findFirst(Db.getSql("crm.performance.query"), objId, objType);
        if (Objects.isNull(performance)) {
            performance = new CrmPerformance().setObjId(objId).setObjType(objType);
            performance.save();
        }
        return performance;
    }
}
