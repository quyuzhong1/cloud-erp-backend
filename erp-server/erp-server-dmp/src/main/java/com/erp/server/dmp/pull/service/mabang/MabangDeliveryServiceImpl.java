package com.erp.server.dmp.pull.service.mabang;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSONObject;
import com.common.core.utils.BeanMapUtil;
import com.common.core.utils.MapUtil;
import com.common.message.constant.RocketMqTopic;
import com.common.message.enums.RocketMqTagEnum;
import com.common.message.service.mq.MQProducerService;
import com.erp.model.dmp.constant.MongoTableNameContant;
import com.erp.model.dmp.dto.OrderMongoDTO;
import com.erp.model.dmp.dto.RequestDTO;
import com.erp.model.dmp.entity.DmpFbaDeliveryDetailEntity;
import com.erp.model.dmp.entity.DmpFbaDeliveryEntity;
import com.erp.model.dmp.enums.CleanStatusEnum;
import com.erp.model.dmp.enums.PlatformApiEnum;
import com.erp.model.dmp.enums.PlatformEnum;
import com.erp.model.dmp.enums.SettingEnum;
import com.erp.model.dmp.mabang.DeliveryEntity;
import com.erp.model.dmp.mabang.item.DeliveryItemEntity;
import com.erp.server.dmp.pull.mongo.MongoService;
import com.erp.server.dmp.pull.service.IReportSaveService;
import com.erp.server.dmp.pull.service.SaveData;
import com.erp.server.dmp.service.CfgSettingService;
import com.erp.server.dmp.service.DmpBomService;
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

    /**
     * 拉去数据
     * @param dto
     */
    @Transactional(rollbackFor = Exception.class, transactionManager = "mongoTransactionManager")
    @Override
    public void pullDataSave(RequestDTO dto) {
        List<DeliveryEntity> pullList = this.pullData(dto);
        if (CollUtil.isEmpty(pullList)) {
            log.info("拉取马帮发货单列表数据为空");
            return;
        }
        log.info("本次拉取到马帮发货单数据共{}条",pullList.size());

        List<DeliveryEntity> insertList = new ArrayList<>();
        List<DeliveryEntity> pushToMqList = new ArrayList<>();

        for(DeliveryEntity entity : pullList) {
            OrderMongoDTO orderMongoDTO = OrderMongoDTO.getByDeliveryNo(entity.getDelivery_no());
            List<DeliveryEntity> mongoData = mongoService.findMongoData(orderMongoDTO, 0, 0, MongoTableNameContant.ORIGINAL_MABANG_DELIVERY, DeliveryEntity.class);
            entity.setIsClean(CleanStatusEnum.UNCLEAN.getCode());
            entity.setDownloadTime(LocalDateTime.now());

            // 拉取下来的数据在mongodb中不存在，新增到mongodb，并推送到mq
            if(CollectionUtil.isEmpty(mongoData)){
                insertList.add(entity);
                pushToMqList.add(entity);
                continue;
            }
            DeliveryEntity mongoDatum = mongoData.get(0);
            // 比较数据是否相同（已经拉取过）
            if (mongoDatum.toString().equals(entity.toString())) {
                continue;
            }
            pushToMqList.add(entity);

            MapUtil mapUtil = JSONObject.parseObject(JSONObject.toJSONString(entity), MapUtil.class);
            OrderMongoDTO updateDto = new OrderMongoDTO(mongoDatum.get_id());
            mongoService.updateMongoData(updateDto, mapUtil, MongoTableNameContant.ORIGINAL_MABANG_DELIVERY, DeliveryEntity.class);
        }

        // 拉取新数据存储到mongodb中
        if(CollectionUtil.isNotEmpty(insertList)){
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

        // 异步推送到MQ
        entityToMqlist.stream().peek(msg ->{
            SendResult result = mqProducerService.syncClassMsg(RocketMqTopic.DMP_ERP_ORDER_TOPIC, RocketMqTagEnum.MABANG_FBA_DELIVERY_TAG.getName(),
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
        List<DeliveryEntity> mongoData = mongoService.findMongoData(orderMongoDTO, 1, size, MongoTableNameContant.ORIGINAL_MABANG_DELIVERY, DeliveryEntity.class);
        if (CollectionUtil.isEmpty(mongoData)) {
            return;
        }
        for (DeliveryEntity mongoDatum : mongoData) {
            mongoDatum.setIsClean(CleanStatusEnum.CLEANING.getCode());
            mongoDatum.setLastPushTime(LocalDateTime.now());
            updateAndSaveDb(mongoDatum);
        }
    }

    @Transactional(rollbackFor = Exception.class, transactionManager = "mongoTransactionManager")
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
        if(deliveryMongo.getDelivery_status().intValue() != 1 && deliveryMongo.getDelivery_status().intValue() != 4) {
            return null;
        }

        DmpFbaDeliveryEntity dmpDeliveryEntity = new DmpFbaDeliveryEntity();
        BeanMapUtil.getInstance().copyAndParse(deliveryMongo, dmpDeliveryEntity);

        dmpDeliveryEntity.setPlatformSign(PlatformEnum.MABANG.getDesc());
        dmpDeliveryEntity.setCreateTime(LocalDateTime.now());
        dmpDeliveryEntity.setItemList(initItem(deliveryMongo));
        return dmpDeliveryEntity;
    }

    /**
     * 解调拨发货商品数据
     **/
    public List<DmpFbaDeliveryDetailEntity> initItem(DeliveryEntity deliveryMongo) {
        List<DeliveryItemEntity> mongoItems = deliveryMongo.getStockList();
        if(CollectionUtil.isEmpty(mongoItems)){
            log.warn("调拨发货单详情列表为空 {}", JSONUtil.toJsonStr(mongoItems));
            return null;
        }
        List<DmpFbaDeliveryDetailEntity> items = new ArrayList<>();
        for (int i = 0; i < mongoItems.size(); i++) {
            DeliveryItemEntity deliveryItemEntity = mongoItems.get(i);
            DmpFbaDeliveryDetailEntity dmpFbaDeliveryDetailEntity = new DmpFbaDeliveryDetailEntity();
            BeanUtil.copyProperties(deliveryItemEntity, dmpFbaDeliveryDetailEntity);
            dmpFbaDeliveryDetailEntity.setDeliveryDetailId(deliveryItemEntity.getId());
            //sku
            String skuNo = deliveryItemEntity.getSku();
            dmpFbaDeliveryDetailEntity.setSkuNo(skuNo);
            // 只取组合品的（因为马帮那边的sku不能修改，所以不用判断sku的变化）
            Boolean isBom = dmpBomService.checkIsBom(skuNo, PlatformEnum.MABANG.getDesc(), "machining");
            if(isBom) {
                items.add(dmpFbaDeliveryDetailEntity);
            }
        }
        return items;
    }

}