package com.kakarote.crm9.erp.admin.service;

import com.jfinal.plugin.activerecord.ActiveRecordException;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.kakarote.crm9.common.constant.BaseConstant;
import com.kakarote.crm9.erp.crm.constant.CrmTagConstant;

import java.util.*;
import java.util.stream.Collectors;

/**
 * CRM 管理后台-客户管理-部门归属用户行业配置Service
 * @author haihong.wu
 */
public class AdminIndustryOfDeptService {

    /**
     * 查询带绑定用户行业配置的部门列表
     */
    public Collection<Record> queryDeptListWithIndustryInfo() {
        //查询部门
        List<Record> deptRecords = Db.find("select b.dept_id deptId,b.name deptName from 72crm_crm_business_type a" +
                "      left join 72crm_admin_dept b on a.dept_ids = b.dept_id and a.status = '1' and b.`is_delete`=0 where b.id is not null");
        if (deptRecords == null || deptRecords.isEmpty()) {
            return new ArrayList<>();
        }
        //查询所有用户行业映射关系，并根据部门ID进行分类
        Map<Long, List<Record>> allMappings = new HashMap<>();
        Db.find("select id,dept_id,industry_code,industry_name,industry_type from 72crm_admin_industry_of_dept where is_deleted=0").forEach(record -> {
            Long key = record.getLong("dept_id");
            List<Record> industries = allMappings.computeIfAbsent(key, k -> new ArrayList<>());
            industries.add(buildRecord(record));
        });
        //组装数据
        if (allMappings.isEmpty()) {
            return deptRecords;
        }
        deptRecords.forEach(dept -> {
            Long key = dept.getLong("deptId");
            dept.set("industry", allMappings.get(key));
        });
        return deptRecords;
    }

    public List<Record> initIndustryList() {
        //查询所有已分配行业
        List<String> usedIndustry = Db.find("select industry_code from 72crm_admin_industry_of_dept where is_deleted=0").stream()
                .map(record -> record.getStr("industry_code")).distinct().collect(Collectors.toList());
        //过滤掉已分配行业
        return queryReceiveIndustry().stream()
                .filter(record ->
                        !usedIndustry.contains(record.getStr("industryCode"))).collect(Collectors.toList());
    }

    private Record buildRecord(Record record) {
        Record result = new Record();
        result.set("configId", record.getLong("id"));
        result.set("industryCode", record.getStr("industry_code"));
        result.set("industryName", record.getStr("industry_name"));
        result.set("industryType", record.getInt("industry_type"));
        return result;
    }

    public Record addIndustry(Long deptId, String industryCode, String industryName, Integer industryType) {
        try {
            Record record = new Record();
            record.set("dept_id", deptId);
            record.set("industry_code", industryCode);
            record.set("industry_name", industryName);
            record.set("industry_type", industryType);
            Db.save("72crm_admin_industry_of_dept", record);
            return record;
        } catch (ActiveRecordException e) {
            if (e.getMessage().contains(BaseConstant.MYSQL_INTEGRITY_CONSTRAINT_VIOLATION_EXCEPTION_NAME)) {
                throw new RuntimeException(String.format("行业[%s]已在其他部门设置", industryName), e);
            } else {
                throw e;
            }
        }
    }

    /**
     * queryReceiveIndustry
     */
    public List<Record> queryReceiveIndustry() {
        return Db.find("select dic_value industryCode,dic_name industryName from 72crm_admin_data_dic where tag_name=?", CrmTagConstant.INDUSTRY);
    }

    public Record findConfigByIndustryCode(String industryCode) {
        return Db.findFirst("select dept_id,industry_code,industry_type,industry_name from 72crm_admin_industry_of_dept where industry_code=?", industryCode);
    }
}
