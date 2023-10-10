package com.erp.server.dmp.push.consumer;

import cn.hutool.json.JSONUtil;
import com.common.message.constant.RocketMqConsumerGroup;
import com.common.message.constant.RocketMqTopic;
import com.erp.model.dmp.enums.KingdeePushModuleEnum;
import com.erp.server.dmp.push.service.business.KingdeeSysUserInfoConsumerService;
import com.erp.sdk.third.kingdee.utils.KingdeeApiUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Will
 * @version 1.0
 * @date 2023/4/10 11:56
 */
@Service
@Slf4j
@RocketMQMessageListener(topic = RocketMqTopic.SYNC_KINGDEE_ERP_TOPIC, selectorExpression = "kingdee_sys_user_info_tag", consumerGroup = RocketMqConsumerGroup.SYNC_KINGDEE_SYS_USER_INFO)
public class KingdeeSysUserInfoConsumer implements RocketMQListener<Map<String, Object>> {

    @Resource
    private KingdeeSysUserInfoConsumerService kingdeeSysUserInfoConsumerService;


    public static void main(String[] args) {

        Map<String, Object> resultMap = new LinkedHashMap<>();
        //读取配置，初始化SDK
        KingdeeApiUtils apiUtils = new KingdeeApiUtils(KingdeePushModuleEnum.BD_EMPINFO.getCode());


        LinkedHashMap<String, Object> viewMap = new LinkedHashMap<>();
        viewMap.put("id", "715911");

        //创建组织
        viewMap.put("CreateOrgId", 0);

        log.info("view方法数据查询,viewJson = {}", JSONUtil.toJsonStr(viewMap));


    }

    @Override
    public void onMessage(Map<String, Object> map) {
        try {
            kingdeeSysUserInfoConsumerService.executeSysUserConsumer(map);
        } catch (Exception e) {
            log.error("KingdeeSysUserInfoConsumer>>>onMessage>>>map ={}>>>e={}", map, e);
        }
    }


}
