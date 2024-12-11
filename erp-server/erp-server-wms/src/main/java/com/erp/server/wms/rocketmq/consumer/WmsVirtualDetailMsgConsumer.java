package com.erp.server.wms.rocketmq.consumer;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.common.message.constant.RocketMqConsumerGroup;
import com.common.message.constant.RocketMqTopic;
import com.erp.model.wms.entity.VirtualTransFlowEntity;
import com.erp.rpc.dmp.feign.DmpMongoDbFeign;
import com.erp.rpc.dmp.feign.DmpTaskFeign;
import com.erp.server.wms.rocketmq.sync.SyncSoReturnService;
import com.erp.server.wms.service.VirtualTransFlowDetailService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
@Slf4j
@RocketMQMessageListener(topic = RocketMqTopic.WMS_VIRTUAL_DETAIL_MSG_TOPIC,
        selectorExpression = "wms_virtual_detail_msg_tag",
        consumerGroup = RocketMqConsumerGroup.WMS_VIRTUAL_DETAIL_MSG_CONSUMER)
public class WmsVirtualDetailMsgConsumer implements RocketMQListener<Object> {

    @Resource
    private VirtualTransFlowDetailService virtualTransFlowDetailService;
    @Resource
    private DmpMongoDbFeign dmpMongoDbFeign;
    @Resource
    private DmpTaskFeign dmpTaskFeign;

    @Override
    public void onMessage(Object ext) {
        //json数据
        JSONObject jsonObject = JSONUtil.parseObj(ext);
        if (ObjUtil.isEmpty(jsonObject)) {
            log.error("未找到需要消费的数据，ext= {}",ext);
            return;
        }
        VirtualTransFlowEntity virtualTransFlowEntity = BeanUtil.toBean(jsonObject, VirtualTransFlowEntity.class);

        //先进先出扣减虚拟仓流水
        virtualTransFlowDetailService.consumeMessage(virtualTransFlowEntity);
    }
}
