package com.kakarote.crm9.erp.crm.service;

import cn.hutool.core.util.IdUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.jfinal.aop.Aop;
import com.jfinal.kit.JsonKit;
import com.jfinal.log.Log;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.kakarote.crm9.BaseTest;
import com.kakarote.crm9.common.config.paragetter.BasePageRequest;
import com.kakarote.crm9.erp.admin.entity.AdminLookUpLog;
import com.kakarote.crm9.erp.admin.service.AdminLookUpLogService;
import com.kakarote.crm9.erp.crm.common.CrmEnum;
import com.kakarote.crm9.erp.crm.constant.CrmConstant;
import com.kakarote.crm9.erp.crm.dto.CrmCustomerPageRequest;
import com.kakarote.crm9.erp.crm.entity.CrmCustomer;
import com.kakarote.crm9.erp.crm.entity.CrmCustomerPaymentChannel;
import com.kakarote.crm9.erp.crm.entity.DistributorBdSalesStatistic;
import com.kakarote.crm9.integration.common.EsbConfig;
import com.kakarote.crm9.utils.OssPrivateFileUtil;
import com.kakarote.crm9.utils.R;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Crm Customer Service Test
 *
 * @author hao.fu
 * @create 2019/9/1 10:17
 */
public class CrmCustomerServiceTest extends BaseTest {

    private CrmCustomerService crmCustomerService = Aop.get(CrmCustomerService.class);
    private AdminLookUpLogService adminLookUpLogService = Aop.get(AdminLookUpLogService.class);

    @Autowired
    private OssPrivateFileUtil ossPrivateFileUtil;

    private EsbConfig esbConfig = Aop.get(EsbConfig.class);

    private Log logger = Log.getLog(getClass());

    @Test
    public void deleteCrmCustomerRecord() {
        Record record = crmCustomerService.getByRealName("zzz");
        if (record!= null) {
            boolean result = crmCustomerService.deleteCrmCustomerRecord(record);
            Assert.assertTrue(result);
        }
    }

    @Test
    public void getPaymentChannelListByCustId() {
        List<Record> result = crmCustomerService.getPaymentChannelListByCustId(1);
    }

    @Test
    public void deletePaymentChannel() {
        crmCustomerService.deletePaymentChannel(2, 2);
    }

    @Test
    public void savePaymentChannel() {
        CrmCustomerPaymentChannel channel = new CrmCustomerPaymentChannel();
        channel.setChannelId(IdUtil.simpleUUID());
        channel.setCrmCustomerId(3);
        channel.setPayType(1);
        channel.setPayName("test");
        channel.setParternAccount("123321");
        channel.setGmtCreate(new Date());
        channel.setGmtModified(new Date());
        channel.setIsDeleted(CrmConstant.DELETE_FLAG_NO);
        crmCustomerService.savePaymentChannel(channel);

        List<Record> result = crmCustomerService.getPaymentChannelListByCustId(3);
        CrmCustomerPaymentChannel a = new CrmCustomerPaymentChannel()._setAttrs(result.get(0).getColumns());
        a.setPayName("newpayname");
        a.setPayType(0);
        a.setParternAccount("1111111");
        crmCustomerService.savePaymentChannel(a);

    }

    @Test
    public void getByCustomerId() {
        CrmCustomer result = CrmCustomer.dao.findById(1L);
        System.out.println(result);
    }

    @Test
    public void getBdSalesStatisticInfoByCustomerIds() {
        List<Integer> ids = Lists.newArrayList();
        ids.add(800180);

        List<DistributorBdSalesStatistic> results = crmCustomerService.getBdSalesStatisticInfoByCustomerIds(ids);
        Assert.assertTrue(results.size() > 0);
    }

    @Test
    public void updateCustomerAddressBySigninAddress() {
        crmCustomerService.updateCustomerAddressBySigninAddress("dkferocm34kdpd25mecz");
    }

    @Test
    public void getCustomerList() {
        try {
            Page<Record> recordList = crmCustomerService.getCustomerList(crmCustomerPageRequest(),mockCrmUser("0575"));
            Assert.assertTrue(recordList.getList().size() > 0);
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }

    @Test
    public void signList() {
        try {
            Page<Record> recordList = crmCustomerService.signList(signListPageRequest());
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }

    @Test
    public void queryContacts(){
        CrmCustomer request = new CrmCustomer();
        request.setCustomerId(10L);
        BasePageRequest<CrmCustomer> crmCustomerBasePageRequest = new BasePageRequest<>(JsonKit.toJson(request), CrmCustomer.class);

        R r = crmCustomerService.queryContacts(crmCustomerBasePageRequest, false);
        Assert.assertNotNull(r);
    }

    private BasePageRequest<CrmCustomerPageRequest> crmCustomerPageRequest() {
        CrmCustomerPageRequest request = new CrmCustomerPageRequest();
        request.setBizType(9);
        request.setSceneId(2);
        return new BasePageRequest<>(JsonKit.toJson(request), CrmCustomerPageRequest.class);
    }

    private BasePageRequest<CrmCustomerPageRequest> signListPageRequest() {
        CrmCustomerPageRequest request = new CrmCustomerPageRequest();
        request.setCustomerId(607520);
        return new BasePageRequest<>(JsonKit.toJson(request), CrmCustomerPageRequest.class);
    }

    /**
     * 查询部门和bd
     */
    @Test
    public void queryDeptAndBdByUser(){
        R r = crmCustomerService.queryDeptAndBdByUser(16203L);
        Assert.assertNotNull(r);
    }

    @Test
    public void checkUserCanOrder(){
        R r = crmCustomerService.checkUserCanOrder("honglei.wan",16203L);
        Assert.assertNotNull(r);
    }

    /**
     * 判断客户是否可以领取
     */
    @Test
    public void canReceive(){
        R r = crmCustomerService.canReceive(800152L,186);
        Assert.assertNotNull(r);
    }

    @Test
    public void canReceiveWithIndustry() throws NoSuchFieldException, IllegalAccessException {
        EsbConfig esbConfig = mockEsbConfig();
        crmCustomerService.canReceiveWithIndustry(800899L, "2", 193L, esbConfig);
    }

    @Test
    public void confirmReceiveWithIndustry() {
        EsbConfig esbConfig =mockEsbConfig();
        crmCustomerService.cleanOwnerOfCustomerOnlyForTest(800165L);
        crmCustomerService.confirmReceiveWithIndustry(800165L, "3", 974L, 193L, esbConfig);
    }

    public EsbConfig mockEsbConfig() {
        EsbConfig config = new EsbConfig();
        config.setEsbHost("172.16.227.248");
        config.setEsbBopsUser("oaOffline");
        config.setEsbBopsPassword("P@ssword01Offline");
        config.setEsbBopsPort("8094");
        config.setBopsCustomerOrderListUrl("bops/trade/order");
        config.setBopsCustomerOrderClientId("com.primeton.esb.consumer.oa");
        config.setBopsCustomerOrderListCode("com.primeton.esb.producer.bops.trade.order");
        return config;
    }

    @Test
    public void queryReduceReason() {
        List<Map<String,Object>> reduceReasonList = crmCustomerService.queryReduceReason();
        Assert.assertTrue(reduceReasonList.size() > 0);
    }

    @Test
    public void searchCapacity(){
        Record record = crmCustomerService.searchUserCapacity(2736L);
        System.out.println(record);
    }

    @Test
    public void searchUserTotalCapacity(){
        Record record = crmCustomerService.searchUserTotalCapacity(1905L);
    }

    @Test
    public void checkUserCapacity(){
        boolean record = crmCustomerService.checkUserCapacity(2736L,2,1);
        System.out.println(record);
    }

    @Test
    public void checkDeptCapacity(){
        JSONObject record = crmCustomerService.checkDeptCapacity(251L,1);
        System.out.println(record);
    }
    @Test
    public void checkCustomerName() {
        R r = crmCustomerService.checkCompanyCustomerName("test1",null);
        Assert.assertNotNull(r);
    }

    @Test
    public void judgeNeedToApproval() {
        R r = crmCustomerService.judgeNeedToApproval(Long.valueOf("1"),Integer.valueOf("186"),esbConfig);
        Assert.assertTrue(r.isSuccess());
    }

    @Test
    public void checkReleaseRule(){
        R releaseRule = crmCustomerService.checkReleaseRule(967710);
        System.out.println(releaseRule.get("msg"));
    }

    @Test
    public void getCustomerPageList(){
        String jsonString = "{\"type\":2,\"sceneId\":4792,\"storageType\":\"\"}";
        CrmCustomer crmCustomer = JSON.parseObject(jsonString,CrmCustomer.class);
        BasePageRequest<CrmCustomer> basePageRequest = new BasePageRequest<>(1,15);
        basePageRequest.setData(crmCustomer);
        crmCustomerService.getCustomerPageList(basePageRequest);
    }

    @Test
    public void addOrUpdate(){
        String jsonString = "{\"entity\":{\"partner\":\"\",\"address\":\"天津市,天津城区,河东区\",\"fromSource\":1,\"location\":\"\",\"detailAddress\":\"\",\"customerName\":\"阿斯顿发\",\"registerCapital\":\"\",\"legalPerson\":\"\",\"registrationNumber\":\"\",\"creditCode\":\"\",\"remark\":\"\",\"customerGrade\":\"155\",\"customerCategory\":\"\",\"customerType\":\"153\",\"ownerUserId\":1905,\"isMultiple\":0,\"registrationImgUrl\":\"\",\"url\":\"\",\"distributor\":\"\",\"customerSource\":\"\",\"industry\":\"\",\"storageType\":2,\"customerLevel\":\"\",\"customerQuality\":\"\",\"customerOrigin\":0}}";
        JSONObject jsonObject = JSON.parseObject(jsonString);
        R r = crmCustomerService.addOrUpdate(jsonObject, 1905L);
        System.out.println(r.isSuccess());

        jsonObject.getJSONObject("entity").put("customerId",967711);
        r = crmCustomerService.addOrUpdate(jsonObject, 1905L);
        System.out.println(r.isSuccess());
    }

    @Test
    public void queryById(){
        Integer customerId = 967711;

        AdminLookUpLog adminLookUpLog = new AdminLookUpLog();
        adminLookUpLog.setBillsId(String.valueOf(customerId));
        adminLookUpLog.setLookUpName(CrmEnum.CUSTOMER_BASE_INFO.getTypes());
        adminLookUpLogService.addLookUpLog(adminLookUpLog, 1905L);

        Record record = crmCustomerService.queryById(customerId, ossPrivateFileUtil);
        System.out.println(record);
    }

    @Test
    public void putDeptPoolOrPublicPoolByIds(){
        crmCustomerService.putDeptPoolOrPublicPoolByIds("长期无意向", 191, "967711", CrmConstant.TWO_FLAG);
        crmCustomerService.putDeptPoolOrPublicPoolByIds("长期无意向", 191, "967711", CrmConstant.ONE_FLAG);
    }

    @Test
    public void transfer(){
        CrmCustomer crmCustomer = new CrmCustomer();
        crmCustomer.setNewOwnerUserId(2589);
        crmCustomer.setTransferType(1);
        crmCustomer.setCustomerIds("968195");
        R r = crmCustomerService.transfer(crmCustomer);
        System.out.println(r.isSuccess());
    }

    @Test
    public void deleteCustomerInfoBySiteMemberId() {
        try {
            Record record = Db.findFirst(Db.getSql("crm.sitemember.findAllColumnBySiteMember"));

            crmCustomerService.deleteCustomerInfoBySiteMemberId(11570);
            crmCustomerService.constructCustomer(967711L,"wanhonglei","hahah","123");
            R r = crmCustomerService.checkCustomerNeedCustomerReceive("wanhonglei", "12365111222");
            System.out.println(r.isSuccess());
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    @Test
    public void baseTest(){
        crmCustomerService.searchUserCapacity(1905L);
        crmCustomerService.searchUserTotalCapacity(1905L);
        crmCustomerService.getEffectiveCustomerDtoListByUserIds(Arrays.asList(1905L));
        crmCustomerService.pullCustomerPublicPool(967711L);
        crmCustomerService.deleteStorageTypeByCustomerIds(967711L);
        crmCustomerService.countCustomerListByOwnerUserId(1905L);
    }


}
