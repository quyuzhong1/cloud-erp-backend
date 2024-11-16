package com.erp.server.dmp.pull.service.mabang;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.common.business.annotation.SaveData;
import com.common.business.constant.MongoTableNameContant;
import com.common.business.dto.RequestDTO;
import com.common.business.enums.PlatformApiEnum;
import com.common.business.service.IReportSaveService;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapUtil;
import com.common.core.utils.MapUtil;
import com.common.core.utils.StrUtils;
import com.common.core.utils.date.DateUtil;
import com.common.core.utils.date.LocalDateUtil;
import com.common.message.constant.RocketMqTopic;
import com.common.message.enums.RocketMqTagEnum;
import com.common.message.service.mq.MQProducerService;
import com.erp.model.dmp.dto.OrderMongoDTO;
import com.erp.model.dmp.entity.DmpBomEntity;
import com.erp.model.dmp.entity.DmpFbaDeliveryDetailEntity;
import com.erp.model.dmp.entity.DmpFbaDeliveryEntity;
import com.erp.model.dmp.entity.DmpWarehouseMappingEntity;
import com.erp.model.dmp.enums.CleanStatusEnum;
import com.erp.model.dmp.enums.FbaDeliveryStatusEnum;
import com.erp.model.dmp.enums.PlatformEnum;
import com.erp.model.dmp.enums.SettingEnum;
import com.erp.model.dmp.mabang.DeliveryEntity;
import com.erp.model.dmp.mabang.item.DeliveryItemEntity;
import com.erp.server.dmp.pull.mongo.MongoService;
import com.erp.server.dmp.service.CfgSettingService;
import com.erp.server.dmp.service.DmpBomService;
import com.erp.server.dmp.service.DmpWarehouseMappingService;
import com.erp.server.dmp.utils.DataCompareUtil;
import com.erp.server.dmp.utils.MabangApiUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 马帮发货单列表
 * @CreateTime: 2023-06-29  16:02
 * @Author: zhangchunlin
 */
@Slf4j
@Component
@SaveData(method = PlatformApiEnum.MABANG_DELIVERY)
public class MabangDeliveryServiceImpl implements IReportSaveService<DeliveryEntity> {

    @Resource
    private MongoService mongoService;

    @Resource
    private CfgSettingService cfgSettingService;

    @Resource
    private MQProducerService mqProducerService;

    @Autowired
    private DmpBomService dmpBomService;

    @Autowired
    private DmpWarehouseMappingService dmpWarehouseMappingService;

    /**
     * 拉去数据
     * @param dto
     */
    // @Transactional(rollbackFor = Exception.class, transactionManager = "mongoTransactionManager")
    @Override
    public void pullDataSave(RequestDTO dto) {
        log.warn("开始拉取马帮FBA发货单数据，拉取的条件：【{}】", JSONObject.toJSONString(dto));
        List<DeliveryEntity> pullList = this.pullData(dto);
        if (CollUtil.isEmpty(pullList)) {
            log.warn("拉取马帮发货单列表数据为空，拉取的条件：{}", JSONObject.toJSONString(dto));
            return;
        }
        log.warn("本次拉取到马帮发货单数据共{}条",pullList.size());

        List<DeliveryEntity> insertList = new ArrayList<>();
        List<DeliveryEntity> pushToMqList = new ArrayList<>();

        for(DeliveryEntity entity : pullList) {
            OrderMongoDTO orderMongoDTO = OrderMongoDTO.getByDeliveryNo(entity.getDelivery_no());
            List<DeliveryEntity> mongoData = mongoService.findMongoData(orderMongoDTO, 0, 0, MongoTableNameContant.ORIGINAL_MABANG_DELIVERY, DeliveryEntity.class);
            entity.setIsClean(CleanStatusEnum.UNCLEAN.getCode());
            entity.setDownloadTime(LocalDateUtil.formatTime(LocalDateTime.now(), DateUtil.fmt));

            // 拉取下来的数据在mongodb中不存在，新增到mongodb，并推送到mq
            if(CollectionUtil.isEmpty(mongoData)){
                insertList.add(entity);
                pushToMqList.add(entity);
                continue;
            }
            DeliveryEntity mongoDatum = mongoData.get(0);
            // 比较数据是否相同（已经拉取过）
            if (DataCompareUtil.compareObject(mongoDatum , entity)) {
                log.warn("发货单号：{}本次拉取数据相同，不做更新", mongoDatum.getDelivery_no());
                continue;
            }
            pushToMqList.add(entity);

            MapUtil mapUtil = JSONObject.parseObject(JSONObject.toJSONString(entity), MapUtil.class);
            OrderMongoDTO updateDto = new OrderMongoDTO(mongoDatum.get_id());
            mongoService.updateMongoData(updateDto, mapUtil, MongoTableNameContant.ORIGINAL_MABANG_DELIVERY, DeliveryEntity.class);
        }

        // 拉取新数据存储到mongodb中
        if(CollectionUtil.isNotEmpty(insertList)){
            log.warn("本次拉取到马帮发货单数据需要新增{}条数据", insertList.size());
            mongoService.saveMongoDataMult(insertList, MongoTableNameContant.ORIGINAL_MABANG_DELIVERY);
        }

        if (CollectionUtil.isEmpty(pushToMqList)){
            log.warn("马帮发货单列表, 无需推送到MQ dto={}", JSONUtil.toJsonStr(dto));
            return;
        }

        // 构造dmp调拨发货数据
        List<DmpFbaDeliveryEntity> entityToMqlist = pushToMqList.stream()
                .map(this::initDeliveryEntity)
                .filter(ObjectUtil::isNotEmpty)
                .collect(Collectors.toList());

        if(CollUtil.isEmpty(entityToMqlist)) {
            log.warn("马帮发货单列表, 清洗后 无需推送到MQ dto={}", JSONUtil.toJsonStr(dto));
            return;
        }
        if(CollUtil.isNotEmpty(entityToMqlist)) {
            log.warn("马帮发货单列表, 需推送到MQ 共{}条数据", entityToMqlist.size());
            // 异步推送到MQ
            List<DmpFbaDeliveryEntity> dmpFbaDeliveryEntityList = entityToMqlist.stream().peek(msg ->{
                SendResult result = mqProducerService.syncClassMsg(RocketMqTopic.DMP_ERP_ORDER_TOPIC, RocketMqTagEnum.MABANG_FBA_DELIVERY_TAG.getName(),
                        msg, StrUtil.uuid().toLowerCase());
                if (!SendStatus.SEND_OK.equals(result.getSendStatus())){
                    throw new ServiceException(StrUtil.format("发送MQ数据异常，{}", JSONUtil.toJsonStr(result)));
                }
            }).collect(Collectors.toList());
            log.debug("马帮FBA发货数据为：{}" , JSON.toJSONString(dmpFbaDeliveryEntityList));
        }
    }

    @Override
    public void cleanDataSave(String tableName, int size) {
        // 查询mongo待推送数据
        String value = cfgSettingService.getValue(SettingEnum.CLEAN_JOB_DELAY_MINUTE);
        Integer delayMinute = null != value ? NumberUtil.parseInt(value) : 0;
        OrderMongoDTO orderMongoDTO = OrderMongoDTO.getByIsCleanDateStr(CleanStatusEnum.UNCLEAN.getCode(), delayMinute);
        List<DeliveryEntity> mongoData = mongoService.findMongoData(orderMongoDTO, 1, size, MongoTableNameContant.ORIGINAL_MABANG_DELIVERY, DeliveryEntity.class);
        if (CollectionUtil.isEmpty(mongoData)) {
            return;
        }
        for (DeliveryEntity mongoDatum : mongoData) {
            mongoDatum.setIsClean(CleanStatusEnum.CLEANING.getCode());
            mongoDatum.setLastPushTime(LocalDateUtil.formatTime(LocalDateTime.now(), DateUtil.fmt));
            updateAndSaveDb(mongoDatum);
        }
    }

    // @Transactional(rollbackFor = Exception.class, transactionManager = "mongoTransactionManager")
    @Override
    public void updateAndSaveDb(DeliveryEntity mongoDatum) {
        DmpFbaDeliveryEntity dmpDeliveryEntity = initDeliveryEntity(mongoDatum);
        OrderMongoDTO updateDto = new OrderMongoDTO(mongoDatum.get_id());
        if(null == dmpDeliveryEntity){
            mongoDatum.setIsClean(CleanStatusEnum.CLEANED.getCode());
            MapUtil mapUtil = JSONObject.parseObject(JSONObject.toJSONString(mongoDatum), MapUtil.class);
            mongoService.updateMongoData(updateDto, mapUtil,  MongoTableNameContant.ORIGINAL_MABANG_DELIVERY, DeliveryEntity.class);
            return;
        }
        MapUtil mapUtil = JSONObject.parseObject(JSONObject.toJSONString(mongoDatum), MapUtil.class);
        mongoService.updateMongoData(updateDto, mapUtil, MongoTableNameContant.ORIGINAL_MABANG_DELIVERY, DeliveryEntity.class);
        SendResult result = mqProducerService.syncClassMsg(RocketMqTopic.DMP_ERP_ORDER_TOPIC, RocketMqTagEnum.MABANG_FBA_DELIVERY_TAG.getName(),
                dmpDeliveryEntity, StrUtil.uuid().toLowerCase());
        if (!SendStatus.SEND_OK.equals(result.getSendStatus())){
            throw new RuntimeException(StrUtil.format("发送MQ数据异常，{}", JSONUtil.toJsonStr(result)));
        }
    }

    /**
     * 请求马帮发货单接口
     *
     * @param dto
     * @return
     */
    private List<DeliveryEntity> pullData(RequestDTO dto) {
        LocalDateTime lastTime = dto.getJobTaskDTO().getLastTime();
        LocalDateTime nextTime = dto.getJobTaskDTO().getNextTime();
        return MabangApiUtils.queryDeliveryList(dto.getPlatformApiEnum().getTaskName(), lastTime, nextTime);
    }

    /**
     * 解析调拨发货数据
     **/

    public  DmpFbaDeliveryEntity initDeliveryEntity(DeliveryEntity deliveryMongo){
        // 只取待配货和作废的单据
        if( (deliveryMongo.getDelivery_status() == null) ||
                (deliveryMongo.getDelivery_status().intValue() != FbaDeliveryStatusEnum.WAIT_DELIVERY.getCode() && deliveryMongo.getDelivery_status().intValue() != FbaDeliveryStatusEnum.INVALID.getCode()) ) {
            log.warn("马帮FBA发货单【{}】信息状态不为待配货，作废状态 为已作废，不需要推送，FBA发货单信息：{}", deliveryMongo.getDelivery_no(), JSONObject.toJSONString(deliveryMongo));
            return null;
        }

        DmpFbaDeliveryEntity dmpDeliveryEntity = new DmpFbaDeliveryEntity();
        // 下划线转驼峰复制
        BeanMapUtil.getInstance().copyAndParse(deliveryMongo, dmpDeliveryEntity);
        dmpDeliveryEntity.setTotalApplyQuantity(deliveryMongo.getTotalApplyQuantity());
        dmpDeliveryEntity.setStockSum(deliveryMongo.getStockSum());

        dmpDeliveryEntity.setDeliveryId(deliveryMongo.getDelivery_id());
        dmpDeliveryEntity.setPlatformSign(PlatformEnum.MABANG.getDesc());
        dmpDeliveryEntity.setCreateTime(LocalDateTime.now());

        // 填充仓库编码
        String warehouseId = StrUtils.null2EmptyWithTrim(deliveryMongo.getWarehouse_id());
        DmpWarehouseMappingEntity dmpWarehouseMappingEntity = dmpWarehouseMappingService.getSourceWarehouseId(warehouseId, PlatformEnum.MABANG.getDesc());
        if(Objects.isNull(dmpWarehouseMappingEntity)) {
            log.warn("马帮FBA发货单在表中未找到仓库信息，不推送，仓库id：{}, 原始马帮发货单号：{}", warehouseId, deliveryMongo.getDelivery_no());
            return null;
        }
        dmpDeliveryEntity.setWarehouseCode(dmpWarehouseMappingEntity.getWarehouseCode());

        dmpDeliveryEntity.setItemList(initItem(deliveryMongo));

        // 没有BOM的明细，则不添加
        if(CollUtil.isEmpty(dmpDeliveryEntity.getItemList())) {
            log.warn("马帮FBA发货单产品信息没有组合品，不需要推送，FBA发货单信息：{}", JSONObject.toJSONString(deliveryMongo));
            return null;
        }
        return dmpDeliveryEntity;
    }

    /**
     * 解调拨发货商品数据
     **/
    public List<DmpFbaDeliveryDetailEntity> initItem(DeliveryEntity deliveryMongo) {
        List<DeliveryItemEntity> mongoItems = deliveryMongo.getStockList();
        if(CollectionUtil.isEmpty(mongoItems)){
            log.warn("调拨发货单，发货单号：【{}】详情列表为空", deliveryMongo.getDelivery_no());
            return null;
        }
        List<DmpFbaDeliveryDetailEntity> items = new ArrayList<>();
        for (int i = 0; i < mongoItems.size(); i++) {
            DeliveryItemEntity deliveryItemEntity = mongoItems.get(i);
            DmpFbaDeliveryDetailEntity dmpFbaDeliveryDetailEntity = new DmpFbaDeliveryDetailEntity();
            BeanMapUtil.getInstance().copyAndParse(deliveryItemEntity, dmpFbaDeliveryDetailEntity);
            dmpFbaDeliveryDetailEntity.setDeliveryDetailId(deliveryItemEntity.getDelivery_detail_id());
            dmpFbaDeliveryDetailEntity.setApplyQuantity(deliveryItemEntity.getApplyQuantity());
            dmpFbaDeliveryDetailEntity.setShipmentStatus(deliveryItemEntity.getShipmentStatus());
            dmpFbaDeliveryDetailEntity.setPictururl(deliveryItemEntity.getPicturUrl());
            //sku
            String skuNo = deliveryItemEntity.getSku();
            dmpFbaDeliveryDetailEntity.setSkuNo(skuNo);
            // 只取加工品的（因为马帮那边的sku不能修改，所以不用判断sku种类的变化）
            List<DmpBomEntity> bomList = dmpBomService.findBom(skuNo, PlatformEnum.MABANG.getDesc(), "machining");
            if(CollUtil.isNotEmpty(bomList)) {
                log.warn("SKU：{}是加工组合品，需要推送到ERP生成加工单", skuNo);
                dmpFbaDeliveryDetailEntity.setBomList(bomList);
                items.add(dmpFbaDeliveryDetailEntity);
            } else {
                log.warn("SKU：{}不是加工组合品，不需要推送到ERP生成加工单", skuNo);
            }
        }
        return items;
    }

}