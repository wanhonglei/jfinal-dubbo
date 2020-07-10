package com.kakarote.crm9.mq;

import cn.hutool.core.util.IdUtil;
import com.alibaba.rocketmq.client.exception.MQClientException;
import com.kakarote.crm9.BaseTest;
import com.kakarote.crm9.common.config.JfinalConfig;
import com.qxwz.lyra.common.mq.producer.LyraMqProducerImpl;
import org.junit.Test;

/**
 * @Author: haihong.wu
 * @Date: 2020/6/18 4:11 下午
 */
public class MQSender extends BaseTest {
    @Test
    public void send() throws MQClientException {
//        String topic = JfinalConfig.crmProp.get("bops.abnormal.funds.topic");
//        String topic =JfinalConfig.crmProp.get("mq.distributor.bind.topic");
        String topic =JfinalConfig.crmProp.get("backend.member.audit.success.topic");
//        String tag = JfinalConfig.crmProp.get("bops.abnormal.funds.tag");
//        String tag =JfinalConfig.crmProp.get("mq.distributor.bind.tag");
        String tag =JfinalConfig.crmProp.get("backend.company.audit.tag");
        LyraMqProducerImpl producer = new LyraMqProducerImpl();
        producer.setGroup(JfinalConfig.crmProp.get("mq.group"));
        producer.setInstance(JfinalConfig.crmProp.get("mq.instance"));
        producer.setNamesrvAddr(JfinalConfig.crmProp.get("mq.namesrv.address"));
        producer.init();
        producer.send(topic, tag, IdUtil.fastSimpleUUID(),"{\"id\": 615316, \"salt\": \"9h5udE\", \"email\": \"285993065@qq.com\", \"regIp\": \"58.212.211.54\", \"mobile\": \"13952051406\", \"source\": 1, \"status\": 0, \"channel\": \"seo.360so.0.0\", \"linkMan\": \"陈洁\", \"password\": \"b3dd29ed9d7c549829911963e258272f\", \"realName\": \"南京天方地圆测绘科技有限公司\", \"userFlag\": \"normal\", \"userName\": \"tfdy86222480\", \"userType\": 1, \"customUse\": \"测绘测量\", \"gmtCreate\": 1590042713000, \"loginName\": \"939202928858998\", \"maxAppNum\": 500, \"synStatus\": 1, \"serviceUse\": 30, \"auditStatus\": 2, \"emailVerify\": 1, \"gmtModified\": 1590042713000, \"mobileVerify\": 1, \"contactNumber\": \"13952051406\", \"contactAddress\": \"江苏省南京市鼓楼区 定淮门12号熊猫软件园7座北楼一层\", \"ntripUserCount\": 0, \"auditFailureMsg\": \"\", \"auditSubmitTime\": 1590042713000, \"maxNtripUserNum\": 0, \"ntripUserPrefix\": \"\", \"faccAccountStatus\": 2, \"businessLicenceNum\": \"9132010679711926X0\", \"businessLicenceImgUrl\": \"https://venus-pri.oss-cn-beijing.aliyuncs.com/license/533/778/388186556dcafefa2169f7316242d251.jpg\"}");
    }
}
