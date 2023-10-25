package com.erp.server.plm.rocketmq.sync.kingdee.impl;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.common.business.enums.SyncStatusEnum;
import com.common.core.utils.MathUtil;
import com.common.message.constant.RocketMqTopic;
import com.common.message.enums.RocketMqTagEnum;
import com.common.message.service.mq.MQProducerService;
import com.erp.model.plm.entity.BomInfoEntity;
import com.erp.model.plm.entity.ProductBomHistoryEntity;
import com.erp.model.plm.entity.ProductBomSkuHistoryEntity;
import com.erp.server.plm.rocketmq.sync.kingdee.SyncKingdeeBomInfoService;
import com.erp.server.plm.service.ProductBomHistoryService;
import com.erp.server.plm.service.ProductBomSkuHistoryService;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * @author Will
 * @version 1.0

 * @date 2023/3/8 18:13
 */
@Service
public class SyncKingdeeBomInfoServiceImpl implements SyncKingdeeBomInfoService {
    @Resource
    private ProductBomHistoryService productBomHistoryService;

    @Resource
    private ProductBomSkuHistoryService productBomSkuHistoryService;

    @Resource
    private MQProducerService mQProducerService;

    /**
     * 组装数据发送到金蝶
     */
    @Override
    public void syncDataToKingdee(BomInfoEntity entity,String operate) {

        //bom历史数据
        List<ProductBomHistoryEntity> bomHistoryList = productBomHistoryService.listByBomId(entity.getId());
        if (CollectionUtils.isEmpty(bomHistoryList)) {
            return;
        }
        //bom历史明细数据
        List<String> bomHistoryIdList = bomHistoryList.stream().map(ProductBomHistoryEntity::getId).collect(Collectors.toList());
        List<ProductBomSkuHistoryEntity> skuHistoryList = productBomSkuHistoryService.getSkuByHistoryIds(bomHistoryIdList);
        if (CollectionUtils.isEmpty(bomHistoryIdList)) {
            return;
        }

        List<Map<String, Object>> listMap = new ArrayList<>();

        for (ProductBomHistoryEntity productBomHistoryEntity : bomHistoryList) {
            Map<String, Object> resultMap = new HashMap<>();
            if (SyncStatusEnum.SUCCESS_SYNC.getCode().equals(productBomHistoryEntity.getSyncKingdeeStatus())) {
                continue;
            }
            //金蝶id
            resultMap.put("syncKingdeeId",productBomHistoryEntity.getSyncKingdeeId());
            //操作（枚举SyncKingdeeOperateEnum）
            resultMap.put("operate", operate);

            List<ProductBomSkuHistoryEntity> childrenList = skuHistoryList.stream().filter(obj -> obj.getBomHistoryId().equals(productBomHistoryEntity.getId())).collect(Collectors.toList());
            if (CollectionUtils.isEmpty(childrenList)) {
                continue;
            }
            //父级物料
            ProductBomSkuHistoryEntity parent = childrenList.get(0);
            //父级sku编码
            resultMap.put("id",productBomHistoryEntity.getId());
            //父级sku编码
            resultMap.put("parentSkuNo",parent.getParentSkuNo());
            //版本
            resultMap.put("version",parent.getParentSkuNo().concat("_").concat(productBomHistoryEntity.getBomVersion().toString()));

            List<Map<String, Object>> mapList = new ArrayList<>();
            for (ProductBomSkuHistoryEntity child: childrenList) {
                Map<String, Object> detailMap = new HashMap<>(MathUtil.THREE);
                detailMap.put("skuNo",child.getSkuNo());
                detailMap.put("date", LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE));
                detailMap.put("quantity",child.getQuantity().toString());
                mapList.add(detailMap);
            }
            resultMap.put("list",mapList);
            listMap.add(resultMap);
        }
        if (CollectionUtils.isEmpty(listMap)) {
            return;
        }
        listMap.stream().forEach(obj -> {
            //异步推送mq
            CompletableFuture.supplyAsync(() -> {
                SendResult result = mQProducerService.syncClassMsg(RocketMqTopic.SYNC_KINGDEE_ERP_TOPIC, RocketMqTagEnum.KINGDEE_BOM_INFO_TAG.getName(), obj, (String)obj.get("id"));
                if (result.getSendStatus().equals(SendStatus.SEND_OK)) {
                    //mq发送成更新业务表状态及时间
                    return productBomHistoryService.updateSyncKingdeeStatus((String)obj.get("id"), SyncStatusEnum.IN_SYNC.getCode(),"");
                }
                return Boolean.TRUE;
            });
        });
    }



}
