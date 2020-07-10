package com.kakarote.crm9.mobile.controller;

import com.alibaba.fastjson.JSON;
import com.jfinal.aop.Before;
import com.jfinal.aop.Inject;
import com.jfinal.core.Controller;
import com.jfinal.core.paragetter.Para;
import com.jfinal.log.Log;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.kakarote.crm9.common.annotation.NotNullValidate;
import com.kakarote.crm9.common.annotation.Permissions;
import com.kakarote.crm9.common.config.paragetter.BasePageRequest;
import com.kakarote.crm9.common.spring.IocInterceptor;
import com.kakarote.crm9.erp.crm.acl.user.CrmUser;
import com.kakarote.crm9.erp.crm.constant.CrmConstant;
import com.kakarote.crm9.erp.crm.dto.CrmCustomerPageRequest;
import com.kakarote.crm9.erp.crm.entity.CrmCustomer;
import com.kakarote.crm9.erp.crm.entity.CrmCustomerSalesLog;
import com.kakarote.crm9.erp.crm.entity.DistributorStatistic;
import com.kakarote.crm9.erp.crm.service.CrmCustomerService;
import com.kakarote.crm9.mobile.common.MobileUtil;
import com.kakarote.crm9.mobile.service.MobileContactsService;
import com.kakarote.crm9.utils.BaseUtil;
import com.kakarote.crm9.utils.OssPrivateFileUtil;
import com.kakarote.crm9.utils.R;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * Mobile Customer Controller
 *
 * @author yue.li
 * @since 2019/12/30 14:08
 */
@Before(IocInterceptor.class)
public class MobileCustomerController extends Controller {

    private Log logger = Log.getLog(getClass());

    @Inject
    private CrmCustomerService crmCustomerService;

    @Inject
    private MobileContactsService mobileContactsService;

    @Autowired
    private OssPrivateFileUtil ossPrivateFileUtil;

    /**
     * 获取客户列表
     * @author yue.li
     * @param basePageRequest
     */
    @Permissions("crm:customer:index")
    public void getCustomerList(BasePageRequest<CrmCustomerPageRequest> basePageRequest){
        try{
            logger.info(String.format("token info%s", MobileUtil.getCrmToken(getRequest())));
            CrmUser crmUser = MobileUtil.getCrmUser(getRequest());
            if(Objects.isNull(crmUser)){
                renderJson(R.error(CrmConstant.NO_CRM_PERMISSION));
            }else{
                Page<Record> records = crmCustomerService.getCustomerList(basePageRequest, crmUser);
                if(Objects.isNull(records)){
                    renderJson(R.error(CrmConstant.NO_PERMISSIONS));
                }else{
                    renderJson(R.ok().put("data", records));
                }
            }
        }catch (Exception e){
            logger.error(String.format("MobileCustomerController getCustomerList msg:%s", BaseUtil.getExceptionStack(e)));
            renderJson(R.error(e.getMessage()));
        }
    }

    /**
     * 根据客户id查询客户详情
     * @author yue.li
     * @param  customerId 客户ID
     */
    @Permissions("crm:customer:read")
    @NotNullValidate(value = "customerId",message = "客户id不能为空")
    public void detail(@Para("customerId")Integer customerId){
        try{
            CrmUser crmUser = MobileUtil.getCrmUser(getRequest());
            if(Objects.isNull(crmUser)){
                renderJson(R.error(CrmConstant.NO_CRM_PERMISSION));
            }else{
                List<Integer> crmCustomerList = crmCustomerService.getAuthorizedCustomerList(crmUser.getAuthorizedUserIds());
                if(crmCustomerList.contains(customerId)){
                    Record record = crmCustomerService.queryById(customerId,ossPrivateFileUtil);
                    record.set("followTime",getFollowTimeByCustomerId(customerId));
                    renderJson(R.ok().put("data",record));
                }else{
                    renderJson(R.error(CrmConstant.NO_PERMISSIONS));
                }
            }
        }catch (Exception e){
            logger.error(String.format("detail customer msg:%s",BaseUtil.getExceptionStack(e)));
            renderJson(R.error(e.getMessage()));
        }
    }

    /**
     * 根据客户id查询客户签到列表
     * @author yue.li
     * @param  basePageRequest 请求对象
     */
    public void signList(BasePageRequest<CrmCustomerPageRequest> basePageRequest) {
        try{
            Page<Record> records = crmCustomerService.signList(basePageRequest);
            renderJson(R.ok().put("data", records));
        }catch (Exception e){
            logger.error(String.format("signList msg:%s",BaseUtil.getExceptionStack(e)));
            renderJson(R.error(e.getMessage()));
        }
    }

    /**
     * 获取客户的最近跟进时间
     * @author yue.li
     * @param customerId 客户ID
     */
    public Date getFollowTimeByCustomerId(Integer customerId) {
        return crmCustomerService.getFollowTimeByCustomerId(customerId);
    }

    /**
     * 根据网站会员id获取分销信息
     * @param siteMemberId 网站会员id
     */
    @NotNullValidate(value = "siteMemberId",message = "网站会员id不能为空")
    public void getDistributorInfo(@Para("siteMemberId")Integer siteMemberId) {
        try{
            List<DistributorStatistic> result = crmCustomerService.getDistributorStatisticInfo(siteMemberId);
            List<Record> records = BaseUtil.convertModelList2RecordList(result);
            renderJson(R.ok().put("data", records));
        }catch (Exception e){
            logger.error(String.format("crm mobile controller getDistributorInfo msg:%s",BaseUtil.getExceptionStack(e)));
            renderJson(R.error(e.getMessage()));
        }
    }

    /**
     * 更新销售数量
     */
    public void saveSalesLog(){
        try{
            CrmUser user = MobileUtil.getCrmUser(getRequest());
            if (Objects.nonNull(user) && Objects.nonNull(user.getCrmAdminUser())) {
                logger.info("mobile controller saveSalesLog, user info: " + user.getCrmAdminUser());
            } else {
                logger.info("mobile controller saveSalesLog, user is null");
                renderJson(R.error(CrmConstant.NO_CRM_PERMISSION));
                return;
            }

            CrmCustomerSalesLog crmCustomerSalesLog = JSON.parseObject(getRawData(), CrmCustomerSalesLog.class);
            Integer userId = user.getCrmAdminUser().getUserId().intValue();
            R salesLogResult = crmCustomerService.saveSalesLog(crmCustomerSalesLog, userId);
            renderJson(salesLogResult);
        } catch (Exception e){
            logger.error(String.format("crm mobile controller saveSalesLog msg:%s", BaseUtil.getExceptionStack(e)));
            renderJson(R.error(e.getMessage()));
        }
    }

    /**
     * 获取更新销售数量详情信息
     */
    @NotNullValidate(value = "crmCustomerId",message = "客户id不能为空")
    @NotNullValidate(value = "productCode",message = "产品code不能为空")
    @NotNullValidate(value = "goodsCode",message = "商品code不能为空")
    @NotNullValidate(value = "goodsSpec",message = "规格不能为空")
    public void getSalesLog(@Para("crmCustomerId") String crmCustomerId, @Para("productCode") String productCode, @Para("goodsCode") String goodsCode, @Para("goodsSpec") String goodsSpec) {
        try {
            renderJson(R.ok().put("data", crmCustomerService.getSalesLog(crmCustomerId, productCode, goodsCode, goodsSpec)));
        } catch (Exception e) {
            logger.error(String.format("crm mobile controller getSalesLog msg:%s", BaseUtil.getExceptionStack(e)));
            renderJson(R.error(e.getMessage()));
        }
    }

    /**
     * 根据客户id查询客户联系小计列表
     * @author yue.li
     * @param  basePageRequest 请求对象
     */
    public void getCustomerNotesList(BasePageRequest<CrmCustomerPageRequest> basePageRequest) {
        try{
            Page<Record> records = crmCustomerService.getCustomerNotesList(basePageRequest,ossPrivateFileUtil);
            renderJson(R.ok().put("data", records));
        }catch (Exception e){
            logger.error(String.format("mobile getCustomerNotesList msg:%s",BaseUtil.getExceptionStack(e)));
            renderJson(R.error(e.getMessage()));
        }
    }

    /**
     * 根据客户id获取该客户下的联系人
     */
    public void getContacts(BasePageRequest<CrmCustomer> basePageRequest){
        try{
            renderJson(mobileContactsService.getContacts(basePageRequest));
        }catch (Exception e){
            logger.error(String.format("mobile getContacts customer msg:%s",BaseUtil.getExceptionStack(e)));
            renderJson(R.error(e.getMessage()));
        }
    }

    /**
     * 获取分销信息减少销售数量的原因
     * @author yue.li
     */
    public void queryReduceReason() {
        try{
            renderJson(R.ok().put("data", crmCustomerService.queryReduceReason()));
        }catch (Exception e){
            logger.error(String.format("crm mobile queryReduceReason msg:%s",BaseUtil.getExceptionStack(e)));
            renderJson(R.error(e.getMessage()));
        }
    }

}
