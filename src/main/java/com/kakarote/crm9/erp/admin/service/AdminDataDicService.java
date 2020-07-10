package com.kakarote.crm9.erp.admin.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jfinal.aop.Before;
import com.jfinal.kit.Kv;
import com.jfinal.log.Log;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.kakarote.crm9.common.config.paragetter.BasePageRequest;
import com.kakarote.crm9.common.constant.BaseConstant;
import com.kakarote.crm9.erp.admin.entity.AdminDataDic;
import com.kakarote.crm9.erp.admin.entity.TagDetails;
import com.kakarote.crm9.erp.crm.common.CrmBusinessShareholderRelationEnum;
import com.kakarote.crm9.erp.crm.constant.CrmConstant;
import com.kakarote.crm9.integration.common.EsbConfig;
import com.kakarote.crm9.utils.BaseUtil;
import com.kakarote.crm9.utils.HttpUtil;
import com.kakarote.crm9.utils.R;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

public class AdminDataDicService {

    private Log logger = Log.getLog(getClass());

    /**
     * @param adminDataDic 数据字典实体
     *                     添加数据字典
     * @author liyue
     */
    @Before(Tx.class)
    public void addDataDic(AdminDataDic adminDataDic, Long userId) {
        if (adminDataDic.getDicId() == null) {
            adminDataDic.setCreateTime(new Date());
            adminDataDic.setCreateUser(userId);
            adminDataDic.save();
        } else {
            adminDataDic.setUpdateTime(new Date());
            adminDataDic.update();
        }
    }

    /**
     * @param request 请求实体
     *                查询数据字典
     * @author liyue
     */
    public Page<Record> queryDataDicList(BasePageRequest<AdminDataDic> request, String tagName) {
        return Db.paginate(request.getPage(), request.getLimit(), Db.getSqlPara("admin.dataDic.queryDataDicList",Kv.by("tagName",tagName)));
    }

    /**
     * @author liyue
     * 查询数据字典
     */
    public List<Record> queryDataDicNoPageList(List<TagDetails> tagDetailsList, String tagName) {
        saveTagInfo(tagDetailsList, tagName);
        return Db.find(Db.getSqlPara("admin.dataDic.queryDataDicList", Kv.by("tagName", tagName)));
    }

    /***
     * 保存标签系统数据
     * @param tagDetailsList 标签数据
     * @param tagName 标签名称
     */
    public void saveTagInfo(List<TagDetails> tagDetailsList,String tagName){
        if(tagDetailsList != null && tagDetailsList.size() >0){
            AdminDataDic adminDataDic = new AdminDataDic();
            for(TagDetails tagDetail:tagDetailsList){
                Record record = Db.findFirst(Db.getSqlPara("admin.dataDic.queryDataDicList",Kv.by("name",tagDetail.getValue()).set("tagName",tagName)));
                if(record == null){
                    adminDataDic.clear();
                    adminDataDic.setTagName(tagName);
                    adminDataDic.setDicName(tagDetail.getValue());
                    adminDataDic.setDicValue(tagDetail.getId());
                    adminDataDic.save();
                }
            }
        }
    }

    /**
     * @param dataDicId 删除实体
     *                  删除数据字典
     * @author liyue
     */
    @Before(Tx.class)
    public R deleteById(String dataDicId) {
        Db.update(Db.getSql("admin.dataDic.updateDataDicById"), CrmConstant.ZERO_FLAG, dataDicId);
        return R.ok();
    }


    /**
     * 查询数据字典信息
     *
     * @param dicType 字典类型
     * @return
     */
    public List<Record> queryDataDicList(String dicType) {
        return Db.find(Db.getSql("admin.dataDic.queryDataDicListByDicType"),dicType);
    }

    /**
     * 格式化标签将标签ID值转化标签名称
     *
     * @param tagName 标签名称
     * @param valueId 标签ID值
     * @return 标签值
     * @author liyue
     */
    public String formatTagValueId(String tagName, String valueId) {
        String result = "";
        Record record = null;
        if(valueId != null && !"".equals(valueId)){
            record = Db.findFirst(Db.getSqlPara("admin.dataDic.queryDataDicList",Kv.by("tagName",tagName).set("id",valueId)));
        }
        if (record != null) {
            result = record.getStr("name");
        }
        return result;
    }

    /**
     * 格式化标签将标签名称值转化标签ID值
     *
     * @param tagName   标签名称
     * @param valueName 标签值
     * @return 标签值
     * @author liyue
     */
    public String formatTagValueName(String tagName, String valueName) {
        String result = "";
        Record record = null;
        if(valueName != null && !"".equals(valueName)){
            record = Db.findFirst(Db.getSqlPara("admin.dataDic.queryDataDicList",Kv.by("tagName",tagName).set("name",valueName)));
        }
        if (record != null) {
            result = record.getStr("id");
        }
        return result;
    }

    /**
     * 获取标签详情
     *
     * @param tagName 标签名称
     * @return
     * @author yue.li
     */
    public List<TagDetails> getTagDetail(String tagName, EsbConfig esbConfig){
        Map<String, String> queryParas = new HashMap<>();
        queryParas.put("tagName",tagName);
        List<TagDetails> tagDetailsList = null;
        String result = "";
        try {
            result = HttpUtil.get(esbConfig.getTagDetailsUrl(), queryParas, esbConfig.getTagDetailsHeader());
        } catch (Exception e) {
            logger.error("获取标签详情信息异常信息" + e.getMessage());
        } finally {
            logger.info("获取标签详情信息response" + result);
            if(!"".equals(result)){
                JSONObject resultObject = JSONObject.parseObject(result);
                Object data = resultObject.get("data");
                if (data != null) {
                    JSONObject dataObject = JSONObject.parseObject(data.toString());
                    Object content = dataObject.get("content");
                    if (content != null) {
                        tagDetailsList = JSONArray.parseArray(content.toString(), TagDetails.class);
                    }
                }
            }
        }
        return tagDetailsList;
    }

    /**
     * 查询标签信息
     *
     * @param tagName 标签名称
     * @return
     * @author yue.li
     */
    public List<Record> queryTagList(String tagName) {
        if(StringUtils.isEmpty(tagName)) {
            return Collections.emptyList();
        }else{
            return Db.find(Db.getSql("admin.dataDic.queryDataDicListByTagName"),tagName);
        }
    }

    public String getNameByTagNameAndValue(String industryTagName, String industryCode) {
        return Db.queryFirst(Db.getSql("admin.dataDic.getNameByTagNameAndValue"),industryTagName,industryCode);
    }

    public Map<String, Map<String, String>> queryByTags(String... tagNames) {
        if (Objects.isNull(tagNames)) {
            return new HashMap<>(0);
        }
        return Db.find(Db.getSqlPara("admin.dataDic.getByTags", Kv.by("tagNames", tagNames)))
                .stream()
                .reduce(new HashMap<>(tagNames.length), (result, record) -> {
                    //将结果List映射为Map
                    String tagName = record.getStr("tag_name");
                    String dicValue = record.getStr("dic_value");
                    String dicName = record.getStr("dic_name");
                    Map<String, String> dicValueMap = result.get(tagName);
                    if (Objects.isNull(dicValueMap)) {
                        dicValueMap = new HashMap<>(1);
                        result.put(tagName, dicValueMap);
                    }
                    dicValueMap.put(dicValue, dicName);
                    return result;
                }, (stringMapHashMap, stringMapHashMap2) -> null);
    }

    /**
     * 根据登录人，获取股东关系
     */
    public List<JSONObject> getShareholderRelation() {
        List<String> roles = BaseUtil.getUser().getRoles().stream().map(String::valueOf).collect(Collectors.toList());

        return Arrays.stream(CrmBusinessShareholderRelationEnum.values()).filter(o -> {
            //如果登陆人角色包含超管，则返回所有场景
            if (roles.contains(String.valueOf(BaseConstant.SUPER_ADMIN_ROLE_ID))){
                return true;
            }

            for (String role : o.getRoleIds().split(",")) {
                if (roles.contains(role)){
                    return true;
                }
            }
            return false;
        }).map(
                o -> {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("shareholderRelationName", o.getName());
                    jsonObject.put("shareholderRelationCode", o.getType());
                    jsonObject.put("categoryCode", o.getCategory().getCategoryCode());
                    jsonObject.put("categoryName", o.getCategory().getCategoryName());
                    return jsonObject;
                }
        ).collect(Collectors.toList());
    }
}
