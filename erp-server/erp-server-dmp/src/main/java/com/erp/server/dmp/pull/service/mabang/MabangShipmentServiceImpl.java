package com.erp.server.dmp.pull.service.mabang;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSONObject;
import com.common.core.utils.MapUtil;
import com.common.message.constant.RocketMqTopic;
import com.common.message.enums.RocketMqTagEnum;
import com.common.message.service.mq.MQProducerService;
import com.erp.model.dmp.constant.MongoTableNameContant;
import com.erp.model.dmp.dto.OrderMongoDTO;
import com.erp.model.dmp.dto.RequestDTO;
import com.erp.model.dmp.entity.DmpShipmentDetailEntity;
import com.erp.model.dmp.entity.DmpShipmentEntity;
import com.erp.model.dmp.enums.CleanStatusEnum;
import com.erp.model.dmp.enums.PlatformApiEnum;
import com.erp.model.dmp.enums.SettingEnum;
import com.erp.model.dmp.mabang.ShipmentEntity;
import com.erp.model.dmp.mabang.item.ShipmentItemEntity;
import com.erp.server.dmp.pull.mongo.MongoService;
import com.erp.server.dmp.pull.service.IReportSaveService;
import com.erp.server.dmp.pull.service.SaveData;
import com.erp.server.dmp.service.CfgSettingService;
import com.erp.server.dmp.utils.MabangApiUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 马帮调拨发货列表
 * @CreateTime: 2023-06-29  16:02
 * @Author: zhangchunlin
 */
@Slf4j
@Component
@SaveData(method = PlatformApiEnum.MABANG_SHIPMENT)
public class MabangShipmentServiceImpl implements IReportSaveService<ShipmentEntity> {

    @Resource
    private MongoService mongoService;

    @Resource
    private CfgSettingService cfgSettingService;

    @Resource
    private MQProducerService mqProducerService;

    /**
     * 拉去数据
     * @param dto
     */
    @Transactional(rollbackFor = Exception.class, transactionManager = "mongoTransactionManager")
    @Override
    public void pullDataSave(RequestDTO dto) {
        List<ShipmentEntity> pullList = this.pullData(dto);
        if (CollUtil.isEmpty(pullList)) {
            log.info("拉取马帮调拨发货列表数据为空");
            return;
        }
        log.info("本次拉取到马帮调拨发货列表数据共{}条",pullList.size());

        List<ShipmentEntity> insertList = new ArrayList<>();
        List<ShipmentEntity> pushToMqList = new ArrayList<>();

        for(ShipmentEntity entity : pullList) {
            OrderMongoDTO orderMongoDTO = OrderMongoDTO.getByShipmentId(entity.getShipmentId());
            List<ShipmentEntity> mongoData = mongoService.findMongoData(orderMongoDTO, 0, 0, MongoTableNameContant.ORIGINAL_MABANG_SHIPMENT, ShipmentEntity.class);
            entity.setIsClean(CleanStatusEnum.UNCLEAN.getCode());
            entity.setDownloadTime(LocalDateTime.now());

            // 拉取下来的数据在mongodb中不存在，新增到mongodb，并推送到mq
            if(CollectionUtil.isEmpty(mongoData)){
                insertList.add(entity);
                pushToMqList.add(entity);
                continue;
            }
            ShipmentEntity mongoDatum = mongoData.get(0);
            // 比较数据是否相同（已经拉取过）
            if (mongoDatum.toString().equals(entity.toString())) {
                continue;
            }
            pushToMqList.add(entity);

            MapUtil mapUtil = JSONObject.parseObject(JSONObject.toJSONString(entity), MapUtil.class);
            OrderMongoDTO updateDto = new OrderMongoDTO(mongoDatum.get_id());
            mongoService.updateMongoData(updateDto, mapUtil, MongoTableNameContant.ORIGINAL_MABANG_SHIPMENT, ShipmentEntity.class);
        }

        // 拉取新数据存储到mongodb中
        if(CollectionUtil.isNotEmpty(insertList)){
            mongoService.saveMongoDataMult(insertList, MongoTableNameContant.ORIGINAL_MABANG_SHIPMENT);
        }

        if (CollectionUtil.isEmpty(pushToMqList)){
            log.warn("马帮调拨发货列表, 无需推送到MQ dto={}", JSONUtil.toJsonStr(dto));
            return;
        }

        // 构造dmp调拨发货数据
        List<DmpShipmentEntity> entityToMqlist = pushToMqList.stream()
                .map(MabangShipmentServiceImpl::initShipEntity)
                .filter(ObjectUtil::isNotEmpty)
                .collect(Collectors.toList());

        // 异步推送到MQ
        entityToMqlist.stream().peek(msg ->{
            SendResult result = mqProducerService.syncClassMsg(RocketMqTopic.DMP_ERP_ORDER_TOPIC, RocketMqTagEnum.MABANG_SHIPMENT_TAG.getName(),
                    msg, StrUtil.uuid().toLowerCase());
            if (!SendStatus.SEND_OK.equals(result.getSendStatus())){
                throw new RuntimeException(StrUtil.format("发送MQ数据异常，{}", JSONUtil.toJsonStr(result)));
            }
        }).collect(Collectors.toList());

    }

    @Override
    public void cleanDataSave(String tableName, int size) {
        // 查询mongo待推送数据
        String value = cfgSettingService.getValue(SettingEnum.CLEAN_JOB_DELAY_MINUTE);
        Integer delayMinute = null != value ? NumberUtil.parseInt(value) : 0;
        OrderMongoDTO orderMongoDTO = OrderMongoDTO.getByIsClean(CleanStatusEnum.UNCLEAN.getCode(), delayMinute);
        List<ShipmentEntity> mongoData = mongoService.findMongoData(orderMongoDTO, 1, size, MongoTableNameContant.ORIGINAL_MABANG_SHIPMENT, ShipmentEntity.class);
        if (CollectionUtil.isEmpty(mongoData)) {
            return;
        }
        for (ShipmentEntity mongoDatum : mongoData) {
            mongoDatum.setIsClean(CleanStatusEnum.CLEANING.getCode());
            mongoDatum.setLastPushTime(LocalDateTime.now());
            updateAndSaveDb(mongoDatum);
        }
    }

    @Transactional(rollbackFor = Exception.class, transactionManager = "mongoTransactionManager")
    @Override
    public void updateAndSaveDb(ShipmentEntity mongoDatum) {
        DmpShipmentEntity dmpShipmentEntity = initShipEntity(mongoDatum);
        OrderMongoDTO updateDto = new OrderMongoDTO(mongoDatum.get_id());
        if(null == dmpShipmentEntity){
            mongoDatum.setIsClean(CleanStatusEnum.CLEANED.getCode());
            MapUtil mapUtil = JSONObject.parseObject(JSONObject.toJSONString(mongoDatum), MapUtil.class);
            mongoService.updateMongoData(updateDto, mapUtil,  MongoTableNameContant.ORIGINAL_MABANG_SHIPMENT, ShipmentEntity.class);
            return;
        }
        MapUtil mapUtil = JSONObject.parseObject(JSONObject.toJSONString(mongoDatum), MapUtil.class);
        mongoService.updateMongoData(updateDto, mapUtil, MongoTableNameContant.ORIGINAL_MABANG_SHIPMENT, ShipmentEntity.class);
        SendResult result = mqProducerService.syncClassMsg(RocketMqTopic.DMP_ERP_ORDER_TOPIC, RocketMqTagEnum.MABANG_SHIPMENT_TAG.getName(),
                dmpShipmentEntity, StrUtil.uuid().toLowerCase());
        if (!SendStatus.SEND_OK.equals(result.getSendStatus())){
            throw new RuntimeException(StrUtil.format("发送MQ数据异常，{}", JSONUtil.toJsonStr(result)));
        }
    }

    /**
     * 请求马帮调拨发货接口
     *
     * @param dto
     * @return
     */
    private List<ShipmentEntity> pullData(RequestDTO dto) {
        LocalDateTime lastTime = dto.getJobTaskDTO().getLastTime();
        LocalDateTime nextTime = dto.getJobTaskDTO().getNextTime();
        return MabangApiUtils.queryShipmentList(dto.getPlatformApiEnum().getTaskName(), lastTime, nextTime);
    }

    /**
     * 解析调拨发货数据
     **/

    public static DmpShipmentEntity initShipEntity(ShipmentEntity shipmentMongo){
        DmpShipmentEntity dmpShipmentEntity = new DmpShipmentEntity();
        BeanUtil.copyProperties(shipmentMongo, dmpShipmentEntity);

        dmpShipmentEntity.setCreateTime(LocalDateTime.now());
        dmpShipmentEntity.setItemList(initItem(shipmentMongo));
        return dmpShipmentEntity;
    }

    /**
     * 解调拨发货商品数据
     **/
    public static List<DmpShipmentDetailEntity> initItem(ShipmentEntity shipmentEntityMongo) {
        List<ShipmentItemEntity> mongoItems = shipmentEntityMongo.getItemList();
        if(CollectionUtil.isEmpty(mongoItems)){
            log.warn("调拨发货详情列表为空 {}", JSONUtil.toJsonStr(mongoItems));
            return null;
        }
        List<DmpShipmentDetailEntity> items = new ArrayList<>();
        for (int i = 0; i < mongoItems.size(); i++) {
            ShipmentItemEntity shipmentItemEntity = mongoItems.get(i);
            DmpShipmentDetailEntity dmpShipmentDetailEntity = new DmpShipmentDetailEntity();
            BeanUtil.copyProperties(shipmentItemEntity, dmpShipmentDetailEntity);
            //sku
            String skuNo = shipmentItemEntity.getStockSku();
            dmpShipmentDetailEntity.setSkuNo(skuNo);
            items.add(dmpShipmentDetailEntity);
        }
        return items;
    }

}