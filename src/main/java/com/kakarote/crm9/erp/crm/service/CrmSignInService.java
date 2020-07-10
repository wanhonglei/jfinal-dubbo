package com.kakarote.crm9.erp.crm.service;

import cn.hutool.core.util.IdUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.jfinal.aop.Inject;
import com.jfinal.kit.Kv;
import com.jfinal.log.Log;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.SqlPara;
import com.kakarote.crm9.common.config.paragetter.BasePageRequest;
import com.kakarote.crm9.erp.admin.service.AdminFileService;
import com.kakarote.crm9.erp.crm.acl.user.CrmUser;
import com.kakarote.crm9.erp.crm.constant.CrmConstant;
import com.kakarote.crm9.erp.crm.dto.CrmSignInPageRequest;
import com.kakarote.crm9.erp.crm.entity.CrmCustomerAddressHistory;
import com.kakarote.crm9.erp.crm.entity.CrmSignInHistory;
import com.kakarote.crm9.erp.crm.entity.CustomerLocationInfo;
import com.kakarote.crm9.erp.crm.entity.LocationInfo;
import com.kakarote.crm9.erp.crm.entity.SignInItem;
import com.kakarote.crm9.mobile.entity.MobileNewSignInRequest;
import com.kakarote.crm9.utils.BaseUtil;
import com.kakarote.crm9.utils.CrmDateUtil;
import com.kakarote.crm9.utils.OssPrivateFileUtil;
import com.kakarote.crm9.utils.R;
import com.kakarote.crm9.utils.SceneUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Crm Sign In Service
 *
 * @author hao.fu
 * @since 2019/11/25 14:29
 */
public class CrmSignInService {

    private Log logger = Log.getLog(getClass());

    @Inject
    private CrmCustomerService crmCustomerService;

    @Inject
    private CrmNotesService crmNotesService;

    @Inject
    private AdminFileService adminFileService;

    public Page<Record> querySignInPageList(BasePageRequest<CrmSignInPageRequest> signInRequest, CrmUser user) {
        CrmSignInPageRequest request = signInRequest.getData();
        if (Objects.isNull(request) || Objects.isNull(user)) {
            return new Page<>();
        }

        List<Integer> authorizedUserIds = SceneUtil.getAuthorizedUserIdsForBizScene(request.getBizType(), request.getSceneId(), user);
        if(CollectionUtils.isEmpty(authorizedUserIds)){
            return new Page<>();
        }

        SqlPara sqlPara = prepareSqlParaForList(request, authorizedUserIds);
        Page<Record> records = Db.paginate(signInRequest.getPage(), signInRequest.getLimit(), sqlPara);
        if (Objects.nonNull(records) && CollectionUtils.isNotEmpty(records.getList())) {
            final List<SignInItem> signInItems = records.getList().stream().map(item -> JSON.parseObject(item.toJson(), SignInItem.class)).collect(Collectors.toList());
            signInItems.forEach(item -> item.setLocationMatch(item.getLocation() != null && item.getLocation().equals(item.getCustomerAddress())));

            List<Record> result = signInItems.stream().map(pojo -> {
                try {
                    return BaseUtil.convertPojo2Record(pojo);
                } catch (Exception e) {
                    return new Record();
                }
            }).collect(Collectors.toList());
            records.setList(result);
            return records;
        }

        return records;
    }

    public List<Record> querySignInRecords(BasePageRequest<CrmSignInPageRequest> signInRequest, CrmUser crmUser) {
        CrmSignInPageRequest request = signInRequest.getData();
        if (Objects.isNull(request)) {
            return null;
        }

        List<Integer> authorizedUserIds = SceneUtil.getAuthorizedUserIdsForBizScene(request.getBizType(), request.getSceneId(), crmUser);
        if(CollectionUtils.isEmpty(authorizedUserIds)){
            return Lists.newArrayList();
        }
        if(CollectionUtils.isNotEmpty(authorizedUserIds)){
            SqlPara sqlPara = prepareSqlParaForList(request,authorizedUserIds);
            return Db.find(sqlPara);
        }
        return Collections.emptyList();
    }

    public SqlPara prepareSqlParaForList(CrmSignInPageRequest request,List<Integer> authorizedUserIds) {
        Kv kv = assembleQueryParameters(request,authorizedUserIds);
        SqlPara sqlPara = Db.getSqlPara("crm.signin.querySignInPageList", kv);
        logger.info(String.format("querySignInPageList sql: %s", sqlPara));
        return sqlPara;
    }

    /**
     * Assemble query parameters to kv.
     *
     * @param request request object
     * @param authorizedUserIds authorizedUserIds
     * @return Kv
     * @throws Exception
     */
    private Kv assembleQueryParameters(CrmSignInPageRequest request,List<Integer> authorizedUserIds) {
        Kv kv = null;
        try{
            kv = Kv.by("ids", authorizedUserIds);

            kv.putAll(JSONObject.parseObject(com.alibaba.rocketmq.shade.com.alibaba.fastjson.JSON.toJSONString(request)));

            if(Objects.nonNull(request.getHaveNotes())){
                if (request.getHaveNotes()) {
                    kv.set("haveNotes", Boolean.TRUE);
                } else {
                    kv.set("noNotes", Boolean.TRUE);
                }
            }

        }catch(Exception e){
            logger.error(String.format("assembleQueryParameters exception: %s",BaseUtil.getExceptionStack(e)));
        }
        return kv;
    }

    public R updateSignInNotes(String signInId, String notesId) {
        return Db.update(Db.getSql("crm.signin.updateSignInNotes"), notesId, signInId) > 0 ? R.ok() : R.error();
    }

    public LocationInfo getLocation(CrmUser user, Double latitude, Double longitude) {
        Double top = latitude + 0.001;
        Double bottom = latitude - 0.001;
        Double left = longitude - 0.001;
        Double right = longitude + 0.001;
        int userId = user.getCrmAdminUser().getUserId().intValue();
        List<Record> signInAddressList = Db.find(Db.getSql("crm.signin.queryByLocation"), left, right, bottom, top, userId);
        LocationInfo locationInfo;
        if (CollectionUtils.isNotEmpty(signInAddressList)) {
            List<CustomerLocationInfo> locations = signInAddressList.stream().map(item -> JSON.parseObject(item.toJson(), CustomerLocationInfo.class)).collect(Collectors.toList());
            locationInfo = calculateDistance(latitude, longitude, locations);
        } else {
            locationInfo = new LocationInfo(CrmDateUtil.formatDateHours(new Date()));
        }
        return locationInfo;
    }

    /**
     * Return the shortest location
     * @param latitude
     * @param longitude
     * @param locations
     * @return
     */
    private LocationInfo calculateDistance(Double latitude, Double longitude, List<CustomerLocationInfo> locations) {
        if (CollectionUtils.isNotEmpty(locations)) {
            Map<String, Double> maps = Maps.newHashMapWithExpectedSize(locations.size());
            Map<String, CustomerLocationInfo> resultMap = Maps.newHashMapWithExpectedSize(locations.size());
            for(CustomerLocationInfo item : locations) {
                maps.put(item.getAddressId(), getDistance(latitude.toString(), longitude.toString(), item.getLatitude(), item.getLongitude()));
                resultMap.put(item.getAddressId(), item);
            }

            LocationInfo locationInfo = new LocationInfo();
            maps.entrySet().stream().min(Comparator.comparingDouble(Map.Entry::getValue)).ifPresent(o -> {
                CustomerLocationInfo result = resultMap.get(o.getKey());
                locationInfo.setLocation(result.getLocation());
                locationInfo.setCustomerId(result.getCustomerId());
                locationInfo.setCustomerName(result.getCustomerName());
                locationInfo.setCustomerPCD(result.getCustomerPCD());
                locationInfo.setCustomerAddress(result.getCustomerAddress());
                locationInfo.setSignInTime(result.getSignInTime());
            });

            return locationInfo;
        } else {
            return new LocationInfo(CrmDateUtil.formatDateHours(new Date()));
        }
    }

    /**
     * 根据两个位置的经纬度，来计算两地的距离
     * 参数为String类型
     * @param lat1Str 地点A经度
     * @param lng1Str 地点A纬度
     * @param lat2Str 地点B经度
     * @param lng2Str 地点B纬度
     * @return distance
     */
    public static double getDistance(String lat1Str, String lng1Str, String lat2Str, String lng2Str) {
        double lat1 = Double.parseDouble(lat1Str);
        double lng1 = Double.parseDouble(lng1Str);
        double lat2 = Double.parseDouble(lat2Str);
        double lng2 = Double.parseDouble(lng2Str);

        double radLat1 = rad(lat1);
        double radLat2 = rad(lat2);
        double difference = radLat1 - radLat2;
        double mdifference = rad(lng1) - rad(lng2);
        double distance = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(difference / 2), 2)
                + Math.cos(radLat1) * Math.cos(radLat2)
                * Math.pow(Math.sin(mdifference / 2), 2)));
        return distance * CrmConstant.EARTH_RADIUS;
    }

    private static double rad(double d) {
        return d * Math.PI / 180.0;
    }

    public LocationInfo assembleLocationInfo(LocationInfo target, JSONObject jsonObject) {
        if (CrmConstant.AMAP_STATUS_OK_CODE.equals(jsonObject.get("infocode"))) {
            JSONArray jsonArray = jsonObject.getJSONArray("pois");
            if (jsonArray != null && !jsonArray.isEmpty()) {
                JSONObject poiItem = (JSONObject)jsonArray.get(0);
                target.setPoiName(StringUtils.isEmpty(poiItem.getString("name")) ? "" : poiItem.getString("name"));
                if (StringUtils.isEmpty(target.getLocation())) {
                    target.setLocation(StringUtils.isEmpty(poiItem.getString("address")) ? "" : poiItem.getString("address"));
                }
                target.setTypeCode(StringUtils.isEmpty(poiItem.getString("typecode")) ? "" : poiItem.getString("typecode"));
            }
        }
        target.setSignInTime(CrmDateUtil.formatDateHours(new Date()));
        return target;
    }

    /**
     * 根据signinId获取签到详情
     * @param signinId 签到id
     * @param ossPrivateFileUtil oss对象
     * @return
     */
    public Record getSigninDetail(String signinId, OssPrivateFileUtil ossPrivateFileUtil) {
        Record record = Db.findFirst(Db.getSql("crm.signin.getSigninDetailById"), signinId);
        // 添加联系小计图片和附件
        adminFileService.queryByBatchId(record.get("batch_id"), record, ossPrivateFileUtil);
        return record;
    }

    /**
     * 根据signinId删除签到记录
     * @param signinId 签到id
     * @return
     */
    public R deleteSigninBySigninId(String signinId) {
        return Db.delete(Db.getSql("crm.signin.deleteSigninBySigninId"), signinId) > 0 ? R.ok() : R.error("delete sign in failed!");
    }

    /**
     * 添加签到
     * @param request
     * @return
     */
    public R addSignInRecord(MobileNewSignInRequest request, CrmUser user) {
        return Db.tx(() -> {
            // add customer address record
            String addressHistoryId = addCustomerAddress(request, user);

            // add sign in history
            String signInHistoryId = addSignInHistory(request, user, addressHistoryId);

            // update customer address if need
            if (request.isUpdate()) {
                crmCustomerService.updateCustomerAddressBySigninAddress(addressHistoryId);
            }
            // add notes
            if(Objects.nonNull(request.getNoteEntity())) {
                R r = crmNotesService.addOrUpdate(JSON.parseObject(request.getNoteEntity().toJson()),user.getCrmAdminUser().getUserId());
                if (r.isSuccess() && Objects.nonNull(r.get(CrmConstant.ADMIN_NOTES_ID_KEY))) {
                    // 添加签到下的小计
                    updateSignInNotes(signInHistoryId,r.get(CrmConstant.ADMIN_NOTES_ID_KEY).toString());
                }
            }
            return true;
        }) ? R.ok() : R.error("addSignInRecord failed!");
    }

    private String addCustomerAddress(MobileNewSignInRequest request, CrmUser user) {
        CrmCustomerAddressHistory addressHistory = new CrmCustomerAddressHistory();
        addressHistory.setAddressId(IdUtil.simpleUUID());
        addressHistory.setCustomerId(request.getCustomerId());
        addressHistory.setAddress(request.getAddress());
        addressHistory.setTypeCode(request.getTypeCode());
        addressHistory.setProvince(request.getProvince());
        addressHistory.setCity(request.getCity());
        addressHistory.setDistrict(request.getDistrict());
        addressHistory.setPoiName(request.getPoiName());
        addressHistory.setLongitude(request.getLongitude());
        addressHistory.setLatitude(request.getLatitude());
        addressHistory.setCreatorId(Objects.nonNull(user) ? user.getCrmAdminUser().getUserId().intValue() : 0);
        addressHistory.setGmtCreate(new Date());
        addressHistory.setGmtModified(new Date());
        addressHistory.save();
        return addressHistory.getAddressId();
    }

    private String addSignInHistory(MobileNewSignInRequest request, CrmUser user, String addressHistoryId) {
        CrmSignInHistory signIn = new CrmSignInHistory();
        signIn.setHistoryId(IdUtil.simpleUUID());
        signIn.setCustomerAddressId(addressHistoryId);
        Date signInTime = CrmDateUtil.parseDateHours(request.getSignInTime());
        signIn.setSignInTime(Objects.isNull(signInTime) ? new Date() : signInTime);
        signIn.setSignUserId(Objects.nonNull(user) ? user.getCrmAdminUser().getUserId().intValue() : 0);
        signIn.setAdminRecordId(null);
        signIn.setGmtCreate(new Date());
        signIn.setGmtModified(new Date());
        signIn.save();
        return signIn.getHistoryId();
    }

}
