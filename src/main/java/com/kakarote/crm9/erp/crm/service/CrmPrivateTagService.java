package com.kakarote.crm9.erp.crm.service;

import cn.hutool.core.date.DateUtil;
import com.jfinal.aop.Before;
import com.jfinal.log.Log;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.kakarote.crm9.erp.crm.entity.CrmPrivateTag;
import com.kakarote.crm9.utils.R;

public class CrmPrivateTagService {

    private Log logger = Log.getLog(getClass());

    /**
     * 新增或更新标签
     * @author yue.li
     */
    @Before(Tx.class)
    public R addOrUpdate(CrmPrivateTag crmPrivateTag, Long userId) {
        logger.info(String.format("privateTag addOrUpdate方法json %s",crmPrivateTag.toJson()));
        Record record = Db.findFirst(Db.getSql("crm.privateTag.queryPrivateTagByTypeAndId"), crmPrivateTag.getEntityId(),crmPrivateTag.getEntityType());
        if (record != null) {
            crmPrivateTag.setId(record.get("id"));
            crmPrivateTag.setCreateUserId(userId == null?null:userId.intValue());
            crmPrivateTag.setGmtModified(DateUtil.date());
            return crmPrivateTag.update() ? R.ok() : R.error();
        } else {
            crmPrivateTag.setCreateUserId(userId == null?null:userId.intValue());
            crmPrivateTag.setGmtCreate(DateUtil.date());
            boolean save = crmPrivateTag.save();
            return save ? R.ok() : R.error();
        }
    }
}
