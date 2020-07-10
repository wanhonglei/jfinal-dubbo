package com.kakarote.crm9.erp.crm.service;

import java.math.BigInteger;

import com.alibaba.fastjson.JSONObject;
import com.jfinal.kit.Kv;
import com.jfinal.plugin.activerecord.ActiveRecordException;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.kakarote.crm9.common.annotation.Permissions;
import com.kakarote.crm9.common.config.paragetter.BasePageRequest;
import com.kakarote.crm9.erp.crm.dto.CrmCallRecordDto;
import com.kakarote.crm9.erp.crm.entity.CrmCallRecord;

import cn.hutool.core.date.DateUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * @Author: honglei.wan
 * @Description:
 * @Date: Create in 2020/1/8 18:48
 */
@Slf4j
public class CrmContactRecordService {

    /**
     * 通话记录查询接口
     *
     * @param basePageRequest request
     * @param userId 用户id
     */
    @Permissions("crm:ccc:contact_list")
    public Page<Record> queryContactList(BasePageRequest<CrmCallRecordDto> basePageRequest,Long userId) {
        CrmCallRecordDto crmCallRecordDto = basePageRequest.getData();
        switch (crmCallRecordDto.getQueryType()){
                //情景2：新建小记关联
            case 2:
                Kv kv = Kv.by("types", crmCallRecordDto.getTypes())
                        .set("typesId", crmCallRecordDto.getTypesId())
                        .set("recordId", crmCallRecordDto.getRecordId())
                        .set("userId", userId);
//                return Db.paginateByFullSql(basePageRequest.getPage(), basePageRequest.getLimit(), Db.getSqlPara("crm.contractRecord.queryContactNewRelatedList",kv).getSql(),
//                        Db.getSqlPara("crm.contractRecord.queryContactNewRelatedList",kv).getSql(),Db.getSqlPara("crm.contractRecord.queryContactNewRelatedList",kv).getPara());
                if (Integer.valueOf(0).equals(crmCallRecordDto.getIsPage())) {
                	return Db.paginate(crmCallRecordDto.getPageNo(), crmCallRecordDto.getPageSize(), Db.getSqlPara("crm.contractRecord.queryContactNewRelatedList",kv));
                } else {
                	Page<Record> paginate = new Page<Record>();
                	paginate.setList(Db.find(Db.getSqlPara("crm.contractRecord.queryContactNewRelatedList",kv)));
                	return paginate;
                }
                //情景3：编辑小记关联
            case 3:
//                return Db.paginate(basePageRequest.getPage(), basePageRequest.getLimit(), Db.getSqlPara("crm.contractRecord.queryContactEditRelatedList",
//                        Kv.by("types", crmCallRecordDto.getTypes())
//                                .set("typesId", crmCallRecordDto.getTypesId())
//                                .set("recordId",crmCallRecordDto.getRecordId())
//                                .set("recordType", crmCallRecordDto.getRecordType())));
                
                if (Integer.valueOf(0).equals(crmCallRecordDto.getIsPage())) {
                	return Db.paginate(crmCallRecordDto.getPageNo(), crmCallRecordDto.getPageSize(), Db.getSqlPara("crm.contractRecord.queryContactEditRelatedList",
                            Kv.by("types", crmCallRecordDto.getTypes())
                                    .set("typesId", crmCallRecordDto.getTypesId())
                                    .set("recordId",crmCallRecordDto.getRecordId())
                                    .set("recordType", crmCallRecordDto.getRecordType())));
                } else {
                	Page<Record> paginate = new Page<Record>();
                	paginate.setList(Db.find(Db.getSqlPara("crm.contractRecord.queryContactEditRelatedList",
                            Kv.by("types", crmCallRecordDto.getTypes())
                            .set("typesId", crmCallRecordDto.getTypesId())
                            .set("recordId",crmCallRecordDto.getRecordId())
                            .set("recordType", crmCallRecordDto.getRecordType()))));
                	return paginate;
                }
            //情景1：通话记录tab
            default:
//                return Db.paginate(basePageRequest.getPage(), basePageRequest.getLimit(), Db.getSqlPara("crm.contractRecord.queryContactTabList",
//                        Kv.by("types", crmCallRecordDto.getTypes()).set("typesId", crmCallRecordDto.getTypesId())));
                
                if (Integer.valueOf(0).equals(crmCallRecordDto.getIsPage())) {
                	return Db.paginate(crmCallRecordDto.getPageNo(), crmCallRecordDto.getPageSize(), Db.getSqlPara("crm.contractRecord.queryContactTabList",
                            Kv.by("types", crmCallRecordDto.getTypes()).set("typesId", crmCallRecordDto.getTypesId())));
                } else {
                	Page<Record> paginate = new Page<Record>();
                	paginate.setList(Db.find(Db.getSqlPara("crm.contractRecord.queryContactTabList",
                            Kv.by("types", crmCallRecordDto.getTypes()).set("typesId", crmCallRecordDto.getTypesId()))));
                	return paginate;
                }
                
        }
    }

    public BigInteger contactSave(CrmCallRecord crmCallRecord, Long userId) {

        crmCallRecord.setGmtCreate(DateUtil.date());
        crmCallRecord.setGmtModified(DateUtil.date());
        try {
            crmCallRecord.save();
        } catch (ActiveRecordException e) {
            log.info("contactSave is error ,crmCallRecord:{},error :{}", JSONObject.toJSON(crmCallRecord), e.getMessage());
        }
        return crmCallRecord.getId();
    }
}
