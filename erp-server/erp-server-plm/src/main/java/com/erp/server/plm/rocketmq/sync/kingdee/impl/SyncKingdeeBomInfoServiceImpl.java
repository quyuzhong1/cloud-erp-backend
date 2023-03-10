package com.erp.server.plm.rocketmq.sync.kingdee.impl;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.common.business.enums.SyncKingdeeStatusEnum;
import com.common.message.constant.RocketMqTopic;
import com.common.message.enums.RocketMqTagEnum;
import com.common.message.service.mq.MQProducerService;
import com.erp.model.plm.dto.BomChildrenSkuDTO;
import com.erp.model.plm.dto.BomSkuDTO;
import com.erp.model.plm.entity.BomInfoEntity;
import com.erp.server.plm.rocketmq.sync.kingdee.SyncKingdeeBomInfoService;
import com.erp.server.plm.service.BomInfoService;
import com.erp.server.plm.service.BomSkuService;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2023/3/8 18:13
 */
@Service
public class SyncKingdeeBomInfoServiceImpl implements SyncKingdeeBomInfoService {
    @Resource
    private BomInfoService bomInfoService;

    @Resource
    private MQProducerService mQProducerService;

    @Resource
    private BomSkuService bomSkuService;

    /**
     * 组装数据发送到金蝶
     */
    @Override
    public void syncDataToKingdee(BomInfoEntity entity) {


        Map<String, Object> resultMap = new HashMap<>();

        List<Map<String, Object>> mapList = new ArrayList<>();

        List<BomSkuDTO> bomList = bomSkuService.getByBomId(entity.getId());
        if (CollectionUtils.isEmpty(bomList)) {
            return;
        }
        //父级物料
        BomSkuDTO parent = bomList.get(0);
        //父级sku编码
        resultMap.put("parentSkuNo",parent.getSkuNo());
        //版本
        resultMap.put("version",parent.getSkuNo().concat("_").concat(entity.getVersion().toString()));
        //子级物料
        List<BomChildrenSkuDTO> childrenList = parent.getChildren();
        if (CollectionUtils.isEmpty(childrenList)) {
            return;
        }
        for (BomChildrenSkuDTO bomChildrenSkuDTO: childrenList) {
            Map<String, Object> detailMap = new HashMap<>();
            detailMap.put("skuNo",bomChildrenSkuDTO.getSkuNo());
            detailMap.put("quantity",bomChildrenSkuDTO.getQuantity());
            mapList.add(detailMap);
        }
        resultMap.put("list",mapList);
        //异步推送mq
        CompletableFuture.supplyAsync(() -> {
            SendResult result = mQProducerService.syncClassMsg(RocketMqTopic.SYNC_KINGDEE_ERP_TOPIC, RocketMqTagEnum.KINGDEE_BOM_INFO_TAG.getName(), resultMap, String.valueOf(resultMap.get("id")));
            if (result.getSendStatus().equals(SendStatus.SEND_OK)) {
                //mq发送成更新业务表状态及时间
                return bomInfoService.updateSyncKingdeeStatus(entity.getId(), SyncKingdeeStatusEnum.IN_SYNC.getCode());
            }
            return Boolean.TRUE;
        });
    }
}
