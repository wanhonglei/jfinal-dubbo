package com.kakarote.crm9.erp.admin.service;

import com.jfinal.aop.Before;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.kakarote.crm9.common.exception.CrmException;
import com.kakarote.crm9.erp.admin.entity.AdminConfig;
import com.kakarote.crm9.erp.crm.constant.CrmConstant;
import com.kakarote.crm9.utils.R;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * AdminConfigService
 *
 * @author yue.li
 * @date 2020/03/10
 */
public class AdminConfigService {

    /**
     * 添加规则
     * @author yue.li
     * @param adminConfig 规格实体
     */
    @Before(Tx.class)
    public void addAdminConfig(AdminConfig adminConfig){
        if (Objects.isNull(adminConfig.getSettingId())) {
            adminConfig.setCreateTime(new Date());
            adminConfig.save();
        } else {
            adminConfig.setUpdateTime(new Date());
            adminConfig.update();
        }
    }

    /**
     * 批量添加规则
     * @author yue.li
     * @param adminConfigList 规则集合
     */
    public R addBatchAdminConfig(List<AdminConfig> adminConfigList) {
        return Db.tx(() -> {
            if(CollectionUtils.isNotEmpty(adminConfigList)){
                for(AdminConfig adminConfig : adminConfigList) {
                    adminConfig.setStatus(NumberUtils.INTEGER_ONE);
                    addAdminConfig(adminConfig);
                }
            }
            return true;
        }) ? R.ok() : R.error();
    }

    /**
     * 查询规则
     * @author yue.li
     * @param name 参数
     */
    public AdminConfig queryAdminConfig(String name) {
        List<Record> recordList = Db.find(Db.getSql("admin.config.adminConfigByName"), NumberUtils.INTEGER_ONE,name);
        List<AdminConfig> adminConfigList = recordList.stream().map(item-> new AdminConfig()._setOrPut(item.getColumns())).collect(Collectors.toList());
        if(CollectionUtils.isNotEmpty(adminConfigList)) {
            return adminConfigList.get(0);
        }else{
            return null;
        }
    }

    /**
     * 查询到单客户领取规则设置
     * @author yue.li
     */
    public List<AdminConfig> queryCustomerReceivingSetting() {
        List<AdminConfig> adminConfigList = new ArrayList<>();
        adminConfigList.add(queryAdminConfig(CrmConstant.WEBSITE_PERFORMANCE_INCLUDED));
        adminConfigList.add(queryAdminConfig(CrmConstant.TARGET_DEPT_PERFORMANCE_INCLUDED));
        return adminConfigList;
    }

    /**
     * 更新规则
     * @author yue.li
     * @param  name 类型名
     * @param value 类型值
     *
     */
    @Before(Tx.class)
    public R updateConfigByName(String name, String value) {
        Db.update(Db.getSql("admin.config.updateConfigByName"),value,name,NumberUtils.INTEGER_ONE);
        return R.ok();
    }

    /**
     * 批量更新规则
     * @author yue.li
     * @param  adminConfigList 集合
     */
    @Before(Tx.class)
    public R updateBatchConfigByName(List<AdminConfig> adminConfigList) {
        return Db.tx(() -> {
            if(CollectionUtils.isNotEmpty(adminConfigList)){
                for(AdminConfig adminConfig : adminConfigList) {
                    updateConfigByName(adminConfig.getName(),adminConfig.getValue());
                }
            }
            return true;
        }) ? R.ok() : R.error();
    }

    @Deprecated
    public String getConfig(String name, @Nullable String desc) {
        String result;
        AdminConfig config = queryAdminConfig(name);
        if (config == null || StringUtils.isEmpty(result = config.getValue())) {
            throw new CrmException("没有找到配置" + (desc == null ? "" : "[" + desc + "]"));
        }
        return result;
    }

    public String getConfig(String name) {
        if (StringUtils.isBlank(name)) {
            return null;
        }
        AdminConfig config = queryAdminConfig(name);
        return config == null ? null : config.getValue();
    }
}
