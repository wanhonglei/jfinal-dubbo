package com.kakarote.crm9.erp.admin.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jfinal.aop.Inject;
import com.jfinal.core.Controller;
import com.jfinal.log.Log;
import com.kakarote.crm9.common.config.paragetter.BasePageRequest;
import com.kakarote.crm9.erp.admin.entity.CrmBusinessType;
import com.kakarote.crm9.erp.admin.service.AdminBusinessTypeService;
import com.kakarote.crm9.utils.BaseUtil;
import com.kakarote.crm9.utils.R;

import java.util.List;

/**
 * 商机组设置
 *
 * @author hmb
 */
public class AdminBusinessTypeController extends Controller {

    @Inject
    private AdminBusinessTypeService adminBusinessTypeService;

    private Log logger = Log.getLog(getClass());
    /**
     * @author hmb
     * 设置商机组
     */
    public void setBusinessType() {
        try{
            JSONObject jsonObject = JSON.parseObject(getRawData());
            logger.info(String.format("setBusinessType方法json %s",jsonObject.toJSONString()));
            CrmBusinessType crmBusinessType = jsonObject.getObject("crmBusinessType", CrmBusinessType.class);
            Integer userId = BaseUtil.getUserId().intValue();
            if(jsonObject.getJSONArray("deptIds") != null){
                List<Integer> deptIds = jsonObject.getJSONArray("deptIds").toJavaList(Integer.class);
                String deptIdsStr = "";
                for(Integer deptId:deptIds){
                    deptIdsStr = String.valueOf(deptId);
                }
                crmBusinessType.setDeptIds(deptIdsStr);
            }
            JSONArray crmBusinessStatus = jsonObject.getJSONArray("crmBusinessStatus");
            renderJson(adminBusinessTypeService.addBusinessType(crmBusinessType,crmBusinessStatus,userId));
        }catch (Exception e){
            logger.error(String.format("setBusinessType AdminBusinessType msg:%s",BaseUtil.getExceptionStack(e)));
            renderJson(R.error(e.getMessage()));
        }
    }

    /**
     * @author hmb
     * @param basePageRequest 分页对象
     * 查询商机组列表
     */
    public void queryBusinessTypeList(BasePageRequest<Void> basePageRequest) {
        try{
            renderJson(R.ok().put("data", adminBusinessTypeService.queryBusinessTypeList(basePageRequest)));
        }catch (Exception e){
            logger.error(String.format("queryBusinessTypeList AdminBusinessType msg:%s",BaseUtil.getExceptionStack(e)));
            renderJson(R.error(e.getMessage()));
        }
    }

    /**
     * @author hmb
     * 获取详细信息
     */
    public void getBusinessType() {
        try{
            String typeId = getPara("id");
            renderJson(adminBusinessTypeService.getBusinessType(typeId));
        }catch (Exception e){
            logger.error(String.format("getBusinessType AdminBusinessType msg:%s",BaseUtil.getExceptionStack(e)));
            renderJson(R.error(e.getMessage()));
        }
    }

    /**
     * @author hmb
     * 删除商机状态组
     */
    public void deleteById() {
        try{
            String typeId = getPara("id");
            renderJson(adminBusinessTypeService.deleteById(typeId));
        }catch (Exception e){
            logger.error(String.format("deleteById AdminBusinessType msg:%s",BaseUtil.getExceptionStack(e)));
            renderJson(R.error(e.getMessage()));
        }
    }


}
