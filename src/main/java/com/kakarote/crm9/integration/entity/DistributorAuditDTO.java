package com.kakarote.crm9.integration.entity;

import java.io.Serializable;
import java.util.Date;

/**
 * 分销商业务实体
 * @author xiaowen.wu
 *
 */
public class DistributorAuditDTO implements Serializable{

	private static final long serialVersionUID = -6942509580541397820L;
    
	/**
     * 用户ID
     */
    private Long              id;

    /**
     * 用户名(即昵称)
     */
    private String            userName;

    /**
     * 用户名称
     */
    private String            realName;
    /**
     * 公司名称
     */
    private String            companyName;
    /**
     * 手机号
     */
    private String            mobile;
    /**
     * 联系人
     */
    private String            linkMan;
    /**
     * 联系人手机号
     */
    private String            contactNumber;

    /**
     * 申请开始时间
     */
    private Date               applyStartTime;
    /**
     * 申请时间
     */
    private Date               applyTime;
    /**
     * 审核通过时间
     */
    private Date                auditSuccessTime;
    /**
     * 驳回理由
     *
     */
    private String              auditFailMsg;

    /**
     * 用户状态
     */
    private Integer             distributorStatus;

    /**
     * 是否分销商用户
     */
    private Integer             isDistributor;
    /**
     * 经营状态
     */
    private Integer             operateStatus;
    /**
     * 用户等级
     */
    private String              level;

    /**
     * 用户类别
     */
    private Integer              userType;
    /**
     * 邮箱
     */
    private String  email;
    private String              contactEmail;
    /**
     * 营业执照注册号
     */
    private String  businessLicenceNum;
    /**
     * 营业执照照片
     */
    private String  businessLicenceImgUrl;

    private Date businessLicenceBegin;

    private Date businessLicenceEnd;

    private String bdDept;
    private String bdUserName;
    private String bdUserEmail;

    private String addressAreaCode;

    private String addressAreaName;

    private String addressDetail;

    /**网站注册时间 **/
    protected Date gmtCreate;

    /**省份 **/
    private String prov;

    /**城市 **/
    private String city;

    /**
     * 身份证号码
     */
    private String idNum;

    /**
     * 身份证照片url
     */
    private String idImgUrl;

    /**
     * 联系人地址
     */
    private String contactAddress;

    /**
     * 分销商销售区域(省)
     */
    private String areaCode;
 
    /**
     * 分销商销售区域(省)名称
     */
    private String areaName;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getRealName() {
		return realName;
	}

	public void setRealName(String realName) {
		this.realName = realName;
	}

	public String getCompanyName() {
		return companyName;
	}

	public void setCompanyName(String companyName) {
		this.companyName = companyName;
	}

	public String getMobile() {
		return mobile;
	}

	public void setMobile(String mobile) {
		this.mobile = mobile;
	}

	public String getLinkMan() {
		return linkMan;
	}

	public void setLinkMan(String linkMan) {
		this.linkMan = linkMan;
	}

	public String getContactNumber() {
		return contactNumber;
	}

	public void setContactNumber(String contactNumber) {
		this.contactNumber = contactNumber;
	}

	public Date getApplyStartTime() {
		return applyStartTime;
	}

	public void setApplyStartTime(Date applyStartTime) {
		this.applyStartTime = applyStartTime;
	}

	public Date getApplyTime() {
		return applyTime;
	}

	public void setApplyTime(Date applyTime) {
		this.applyTime = applyTime;
	}

	public Date getAuditSuccessTime() {
		return auditSuccessTime;
	}

	public void setAuditSuccessTime(Date auditSuccessTime) {
		this.auditSuccessTime = auditSuccessTime;
	}

	public String getAuditFailMsg() {
		return auditFailMsg;
	}

	public void setAuditFailMsg(String auditFailMsg) {
		this.auditFailMsg = auditFailMsg;
	}

	public Integer getDistributorStatus() {
		return distributorStatus;
	}

	public void setDistributorStatus(Integer distributorStatus) {
		this.distributorStatus = distributorStatus;
	}

	public Integer getIsDistributor() {
		return isDistributor;
	}

	public void setIsDistributor(Integer isDistributor) {
		this.isDistributor = isDistributor;
	}

	public Integer getOperateStatus() {
		return operateStatus;
	}

	public void setOperateStatus(Integer operateStatus) {
		this.operateStatus = operateStatus;
	}

	public String getLevel() {
		return level;
	}

	public void setLevel(String level) {
		this.level = level;
	}

	public Integer getUserType() {
		return userType;
	}

	public void setUserType(Integer userType) {
		this.userType = userType;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getContactEmail() {
		return contactEmail;
	}

	public void setContactEmail(String contactEmail) {
		this.contactEmail = contactEmail;
	}

	public String getBusinessLicenceNum() {
		return businessLicenceNum;
	}

	public void setBusinessLicenceNum(String businessLicenceNum) {
		this.businessLicenceNum = businessLicenceNum;
	}

	public String getBusinessLicenceImgUrl() {
		return businessLicenceImgUrl;
	}

	public void setBusinessLicenceImgUrl(String businessLicenceImgUrl) {
		this.businessLicenceImgUrl = businessLicenceImgUrl;
	}

	public Date getBusinessLicenceBegin() {
		return businessLicenceBegin;
	}

	public void setBusinessLicenceBegin(Date businessLicenceBegin) {
		this.businessLicenceBegin = businessLicenceBegin;
	}

	public Date getBusinessLicenceEnd() {
		return businessLicenceEnd;
	}

	public void setBusinessLicenceEnd(Date businessLicenceEnd) {
		this.businessLicenceEnd = businessLicenceEnd;
	}

	public String getBdDept() {
		return bdDept;
	}

	public void setBdDept(String bdDept) {
		this.bdDept = bdDept;
	}

	public String getBdUserName() {
		return bdUserName;
	}

	public void setBdUserName(String bdUserName) {
		this.bdUserName = bdUserName;
	}

	public String getBdUserEmail() {
		return bdUserEmail;
	}

	public void setBdUserEmail(String bdUserEmail) {
		this.bdUserEmail = bdUserEmail;
	}

	public String getAddressAreaCode() {
		return addressAreaCode;
	}

	public void setAddressAreaCode(String addressAreaCode) {
		this.addressAreaCode = addressAreaCode;
	}

	public String getAddressAreaName() {
		return addressAreaName;
	}

	public void setAddressAreaName(String addressAreaName) {
		this.addressAreaName = addressAreaName;
	}

	public String getAddressDetail() {
		return addressDetail;
	}

	public void setAddressDetail(String addressDetail) {
		this.addressDetail = addressDetail;
	}

	public String getAreaCode() {
		return areaCode;
	}

	public void setAreaCode(String areaCode) {
		this.areaCode = areaCode;
	}

	public Date getGmtCreate() {
		return gmtCreate;
	}

	public void setGmtCreate(Date gmtCreate) {
		this.gmtCreate = gmtCreate;
	}

	public String getProv() {
		return prov;
	}

	public void setProv(String prov) {
		this.prov = prov;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getIdNum() {
		return idNum;
	}

	public void setIdNum(String idNum) {
		this.idNum = idNum;
	}

	public String getIdImgUrl() {
		return idImgUrl;
	}

	public void setIdImgUrl(String idImgUrl) {
		this.idImgUrl = idImgUrl;
	}

	public String getContactAddress() {
		return contactAddress;
	}

	public void setContactAddress(String contactAddress) {
		this.contactAddress = contactAddress;
	}

	public String getAreaName() {
		return areaName;
	}

	public void setAreaName(String areaName) {
		this.areaName = areaName;
	}

	@Override
	public String toString() {
		return "DistributorAuditDTO [id=" + id + ", userName=" + userName + ", realName=" + realName + ", companyName="
				+ companyName + ", mobile=" + mobile + ", linkMan=" + linkMan + ", contactNumber=" + contactNumber
				+ ", applyStartTime=" + applyStartTime + ", applyTime=" + applyTime + ", auditSuccessTime="
				+ auditSuccessTime + ", auditFailMsg=" + auditFailMsg + ", distributorStatus=" + distributorStatus
				+ ", isDistributor=" + isDistributor + ", operateStatus=" + operateStatus + ", level=" + level
				+ ", userType=" + userType + ", email=" + email + ", contactEmail=" + contactEmail
				+ ", businessLicenceNum=" + businessLicenceNum + ", businessLicenceImgUrl=" + businessLicenceImgUrl
				+ ", businessLicenceBegin=" + businessLicenceBegin + ", businessLicenceEnd=" + businessLicenceEnd
				+ ", bdDept=" + bdDept + ", bdUserName=" + bdUserName + ", bdUserEmail=" + bdUserEmail
				+ ", addressAreaCode=" + addressAreaCode + ", addressAreaName=" + addressAreaName + ", addressDetail="
				+ addressDetail + ", gmtCreate=" + gmtCreate + ", prov=" + prov + ", city=" + city + ", idNum=" + idNum
				+ ", idImgUrl=" + idImgUrl + ", contactAddress=" + contactAddress + ", areaCode=" + areaCode
				+ ", areaName=" + areaName + "]";
	}

}
