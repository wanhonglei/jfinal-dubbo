package com.kakarote.crm9.erp.admin.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.jfinal.aop.Before;
import com.jfinal.aop.Inject;
import com.jfinal.core.Controller;
import com.jfinal.log.Log;
import com.kakarote.crm9.common.config.paragetter.BasePageRequest;
import com.kakarote.crm9.common.spring.IocInterceptor;
import com.kakarote.crm9.erp.admin.entity.AdminDataDic;
import com.kakarote.crm9.erp.admin.service.AdminDataDicService;
import com.kakarote.crm9.utils.BaseUtil;
import com.kakarote.crm9.utils.R;

/**
 * 数据字典设置
 *
 * @author yue.li
 */
@Before(IocInterceptor.class)
public class AdminDataDicController extends Controller {

    private Log logger = Log.getLog(getClass());

    @Inject
    private AdminDataDicService adminDataDicService;

    /**
     * 添加数据字典
     * @author yue.li
     */
    public void addDataDic() {
        try{
            JSONObject jsonObject = JSON.parseObject(getRawData());
            logger.info(String.format("addDataDic方法json %s",jsonObject.toJSONString()));
            AdminDataDic adminDataDic = jsonObject.getObject("entity", AdminDataDic.class);
            adminDataDicService.addDataDic(adminDataDic, BaseUtil.getUserId());
            renderJson(R.ok());
        }catch (Exception e){
            logger.error(String.format("addDataDic dataDic msg:%s",BaseUtil.getExceptionStack(e)));
            renderJson(R.error(e.getMessage()));
        }
    }

    /**
     * 查询数据字典列表
     * @author yue.li
     * @param basePageRequest 分页对象
     */
    public void tag(BasePageRequest<AdminDataDic> basePageRequest) {
        try{
            String tagName = basePageRequest.getData().getTagName();
            logger.info(String.format("tag方法tagName %s",tagName));
            renderJson(R.ok().put("data", adminDataDicService.queryDataDicList(basePageRequest, tagName)));
        }catch (Exception e){
            logger.error(String.format("tag dataDic msg:%s",BaseUtil.getExceptionStack(e)));
            renderJson(R.error(e.getMessage()));
        }
    }


    /**
     * 删除数据字典
     * @author yue.li
     *
     */
    public void deleteById() {
        try{
            String typeId = getPara("id");
            renderJson(adminDataDicService.deleteById(typeId));
        }catch (Exception e){
            logger.error(String.format("deleteById dataDic msg:%s",BaseUtil.getExceptionStack(e)));
            renderJson(R.error(e.getMessage()));
        }
    }

    /**
     * 根据登录人，获取股东关系
     */
    public void getShareholderRelation(){
        renderJson(R.okWithData(adminDataDicService.getShareholderRelation()));
    }
}
