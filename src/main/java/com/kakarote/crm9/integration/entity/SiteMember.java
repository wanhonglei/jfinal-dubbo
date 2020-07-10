package com.kakarote.crm9.integration.entity;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * Site member.
 *
 * @author hao.fu
 * @create 2019/7/4 19:54
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SiteMember implements Serializable {

    private static final long serialVersionUID = 4552275189988695195L;

    /**网站注册时间 **/
    protected Date gmtCreate;

    /**网站修改时间 **/
    protected Date gmtModified;

    /**
     * 官网用户自增ID
     */
    private Long id;

    /**
     * 登录名 唯一
     */
    private String loginName;

    /**
     * 用户类型
     */
    private int userType;

    /**
     * 用户名
     */
    private String userName;

    private String email;

    private String mobile;

    /**
     * 企业名称、个人名称
     */
    private String realName;

    /**
     * 邮箱验证标记，0-未验证 1-已验证
     */
    private Byte emailVerify;

    /**手机验证标记，0-未验证 1-已验证 **/
    private Byte mobileVerify;

    /**省份 **/
    private String prov;

    /**城市 **/
    private String city;

    /**其他信息，json串 **/
    private String options;

    /**options字段的版本控制 **/
    private Long optionsVer;

    /**用户状态，0-正常用户 1-被屏蔽用户 2-删除用户 **/
    private Byte status;

    /**注册IP **/
    private String regIp;

    private Integer maxNtripUserNum;


    private Integer maxAppNum;


    private String ntripUserPrefix;

    /**
     * 身份证号码
     */
    private String idNum;

    /**
     * 身份证照片url
     */
    private String idImgUrl;

    /**
     * 营业执照编号
     */
    private String businessLicenceNum;

    /**营业执照照片url **/
    private String businessLicenceImgUrl;

    /**审核状态，1:未审核，2:审核中，3:审核通过，4:审核未通过, 5:已完善部分信息，但未填写【身份证号以及身份证照片url/营业执照号以及营业执照照片】 **/
    private Integer auditStatus;


    private String auditFailureMsg;


    private Long ntripUserCount;


    private String idImgBackUrl;

    /**
     * 联系人
     */
    private String linkMan;

    /**
     * 联系人号码
     */
    private String contactNumber;

    /**
     * 联系人地址
     */
    private String contactAddress;

    /**
     * 注册来源,1：pc，2：移动设备
     */
    private Integer source;

    /**
     * 注册渠道
     */
    private String channel;

    /**
     * 个人/企业认证提交时间
     */
    private Date auditSubmitTime;

    /**
     * 个人/企业认证审核时间
     */
    private Date auditTime;

    /**
     * 试用状态，1表示已试用，0表示未试用
     */
    private Integer tryStatus;


    private String userFlag;

    /**
     * 服务用途
     */
    private Integer serviceUse;

    /**
     * 自定义用途
     */
    private String customUse;

    /**
     * 是否开通子账号权限1.开通，2未开通
     */
    private Integer authSubStatus;

    /**子账号后缀名称 @开头 **/
    private String authSubSuffix;

    /**子账号授权开通时间 **/
    private Date authSubTime;
    
    /** 数据来源：1-mq网站会员；2-mq分销商认证 */
    private String dataOrigin;
    
    /** 成为分销商的时间 */
    private Date distriAuditSuccessTime;

    /**
     * 原始客户姓名
     */
    private String originalCustomerName;

    /**
     * 冗余字段 推广标签
     */
    private String promotionTag;

    @Override
    public String toString() {
        return JSON.toJSONString(this,
                SerializerFeature.WriteMapNullValue,
                SerializerFeature.WriteNullListAsEmpty,
                SerializerFeature.WriteNullStringAsEmpty);
    }
}
