package com.erp.server.dmp.pull.service.lingxing;

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
import com.common.business.dto.UniqueDto;
import com.common.business.enums.PlatformApiEnum;
import com.common.business.service.IReportSaveService;
import com.common.core.exception.ServiceException;
import com.common.core.utils.MapUtil;
import com.common.core.utils.date.DateUtil;
import com.common.core.utils.date.LocalDateUtil;
import com.common.message.constant.RocketMqTopic;
import com.common.message.enums.RocketMqTagEnum;
import com.common.message.service.mq.MQProducerService;
import com.erp.model.dmp.dto.OrderMongoDTO;
import com.erp.model.dmp.entity.ShopInfoMappingEntity;
import com.erp.model.dmp.enums.CleanStatusEnum;
import com.erp.model.dmp.enums.PlatformEnum;
import com.erp.model.dmp.enums.SettingEnum;
import com.erp.model.dmp.lingxing.FbaReceiveDetailEntity;
import com.erp.model.dmp.lingxing.FbaReceiveGroupEntity;
import com.erp.model.oms.entity.ShopInfoEntity;
import com.erp.rpc.oms.feign.ShopInfoFeign;
import com.erp.server.dmp.convert.DmpFbaShipmentReceiveConverter;
import com.erp.server.dmp.pull.mongo.MongoService;
import com.erp.server.dmp.service.CfgSettingService;
import com.erp.server.dmp.service.ShopInfoMappingService;
import com.erp.server.dmp.utils.DataCompareUtil;
import com.sdk.third.lingxing.dto.FbaShipmentReceiveDTO;
import com.sdk.third.lingxing.utils.LingxingApiUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 领星FBA货件签收明细
 */
@Slf4j
@Component
@SaveData(method = PlatformApiEnum.LX_ERP_FBA_SHIPMENT_RECEIVE_GET)
public class LxFbaShipmentReceiveServiceImpl implements IReportSaveService<FbaReceiveGroupEntity> {
    @Resource
    private MongoService mongoService;
    @Autowired
    private MQProducerService mqProducerService;
    @Resource
    private ShopInfoFeign shopInfoFeign;
    @Resource
    private ShopInfoMappingService shopInfoMappingService;
    @Resource
    private CfgSettingService cfgSettingService;


    /**
     * 拉取货件签收明细数据
     *
     * @param dto 任务信息
     */
    @Override
    // @Transactional(rollbackFor = Exception.class, transactionManager = "mongoTransactionManager")
    public void pullDataSave(RequestDTO dto) {
        // 取上次开始的时间
        LocalDateTime requestTime = dto.getJobTaskDTO().getLastTime();
        ShopInfoEntity shopInfoEntity = shopInfoFeign.getShopInfoById(dto.getJobTaskDTO().getShopId());
        if (null == shopInfoEntity){
            throw new ServiceException("店铺不存在,id=" + dto.getJobTaskDTO().getShopId());
        }
        // 查询映射关系
        ShopInfoMappingEntity mappingEntity = shopInfoMappingService.getByShopIdAndType(shopInfoEntity.getId(), PlatformEnum.LINGXING.getName());
        if (null == mappingEntity){
            throw new ServiceException("数据异常:找不到领星映射关系, 店铺id=" + shopInfoEntity.getId());
        }
        String sid = mappingEntity.getThirdPlatformShopId();
        List<FbaShipmentReceiveDTO> dtoList = LingxingApiUtils.getAllReceivedInventory(Integer.parseInt(sid), requestTime.toLocalDate());
        if (CollectionUtil.isEmpty(dtoList)) {
            log.info("拉取领星货件签收明细数据列表数据为空,sid={}, date={}", sid, requestTime);
            return;
        }
        dtoList.forEach(e -> e.setShopId(shopInfoEntity.getId()));
        List<FbaReceiveDetailEntity> entityList = DmpFbaShipmentReceiveConverter.INSTANCE.dtoListToEntityList(dtoList);

        log.info("拉取领星货件签收明细数据 entityList.size = {} ", entityList.size());
        List<FbaReceiveGroupEntity> insertList = new ArrayList<>();
        List<FbaReceiveGroupEntity> pushToMqList = new ArrayList<>();
        // 按fba_shipment_id分组构造结构
        Map<String, List<FbaReceiveDetailEntity>> entityToMqList = entityList.stream()
                .filter(ObjectUtil::isNotEmpty)
                .collect(Collectors.groupingBy(FbaReceiveDetailEntity::getFbaShipmentId));
        for (Map.Entry<String, List<FbaReceiveDetailEntity>> entry : entityToMqList.entrySet()) {
            // 构造消息体
            FbaReceiveGroupEntity entity = FbaReceiveGroupEntity.init(entry, requestTime.toLocalDate(), sid, shopInfoEntity.getId());
            UniqueDto orderMongoDTO = UniqueDto.getUniqId(entity.getUniqueId());
            List<FbaReceiveGroupEntity> mongoData = mongoService.findMongoData(orderMongoDTO, 0, 0, MongoTableNameContant.ORIGINAL_LX_FBA_SHIPMENT_RECEIVE, FbaReceiveGroupEntity.class);
            entity.setIsClean(CleanStatusEnum.UNCLEAN.getCode());
            entity.setDownloadTime(LocalDateUtil.formatTime(LocalDateTime.now(), DateUtil.fmt));
            if(CollectionUtil.isEmpty(mongoData)){
                insertList.add(entity);
                pushToMqList.add(entity);
                continue;
            }
            FbaReceiveGroupEntity mongoDatum = mongoData.get(0);
            // 比较数据是否相同
            if (DataCompareUtil.compareObject(mongoDatum , entity)) {
                continue;
            }
            pushToMqList.add(entity);
            MapUtil mapUtil = JSONObject.parseObject(JSONObject.toJSONString(entity), MapUtil.class);
            OrderMongoDTO updateDto = new OrderMongoDTO(mongoDatum.getUniqueId());
            mongoService.updateMongoData(updateDto, mapUtil, MongoTableNameContant.ORIGINAL_LX_FBA_SHIPMENT_RECEIVE, FbaReceiveGroupEntity.class);
        }
        if(CollUtil.isNotEmpty(insertList)){
            mongoService.saveMongoDataMult(insertList, MongoTableNameContant.ORIGINAL_LX_FBA_SHIPMENT_RECEIVE);
        }
        if (CollectionUtil.isEmpty(pushToMqList)){
            log.warn("领星FBA货件明细, 无需推送到MQ dto={}", JSONUtil.toJsonStr(dto));
            return;
        }

        // 异步推送到MQ
        List<FbaReceiveGroupEntity> fbaReceiveGroupEntityList = pushToMqList.stream().peek(msgEntity ->{
            SendResult result = mqProducerService.syncClassMsg(RocketMqTopic.DMP_ERP_ORDER_TOPIC, RocketMqTagEnum.LX_FBA_SHIPMENT_RECEIVE_TAG.getName(),
                    JSONUtil.toJsonStr(msgEntity), msgEntity.getUniqueId());
            if (!SendStatus.SEND_OK.equals(result.getSendStatus())){
                throw new ServiceException(StrUtil.format("发送领星FBA货件签收MQ数据异常，{}", JSONUtil.toJsonStr(result)));
            }
        }).collect(Collectors.toList());
        log.debug("领星FBA数据为：{}" , JSON.toJSONString(fbaReceiveGroupEntityList));
    }

    @Override
    public void cleanDataSave(String tableName, int size) {
        // 查询mongo待推送数据
        String value = cfgSettingService.getValue(SettingEnum.CLEAN_JOB_DELAY_MINUTE);
        Integer delayMinute = null != value ? NumberUtil.parseInt(value) : 0;
        OrderMongoDTO orderMongoDTO = OrderMongoDTO.getByIsCleanDateStr(CleanStatusEnum.UNCLEAN.getCode(), delayMinute);
        List<FbaReceiveGroupEntity> mongoData = mongoService.findMongoData(orderMongoDTO, 1, size, MongoTableNameContant.ORIGINAL_LX_FBA_SHIPMENT_RECEIVE, FbaReceiveGroupEntity.class);
        if (CollectionUtil.isEmpty(mongoData)) {
            return;
        }
        for (FbaReceiveGroupEntity mongoDatum : mongoData) {
            mongoDatum.setIsClean(CleanStatusEnum.CLEANING.getCode());
            mongoDatum.setLastPushTime(LocalDateUtil.formatTime(LocalDateTime.now(), DateUtil.fmt));
            updateAndSaveDb(mongoDatum);
        }
    }

    @Override
    // @Transactional(rollbackFor = Exception.class, transactionManager = "mongoTransactionManager")
    public void updateAndSaveDb(FbaReceiveGroupEntity mongoDatum) {
        UniqueDto updateDto = UniqueDto.getUniqId(mongoDatum.getUniqueId());
        if(null == mongoDatum){
            mongoDatum.setIsClean(CleanStatusEnum.CLEANED.getCode());
            MapUtil mapUtil = JSONObject.parseObject(JSONObject.toJSONString(mongoDatum), MapUtil.class);
            mongoService.updateMongoData(updateDto, mapUtil, MongoTableNameContant.ORIGINAL_LX_FBA_SHIPMENT_RECEIVE, FbaReceiveGroupEntity.class);
            return;
        }
        MapUtil mapUtil = JSONObject.parseObject(JSONObject.toJSONString(mongoDatum), MapUtil.class);
        mongoService.updateMongoData(updateDto, mapUtil, MongoTableNameContant.ORIGINAL_LX_FBA_SHIPMENT_RECEIVE, FbaReceiveGroupEntity.class);
        SendResult result = mqProducerService.syncClassMsg(RocketMqTopic.DMP_ERP_ORDER_TOPIC, RocketMqTagEnum.LX_FBA_SHIPMENT_RECEIVE_TAG.getName(),
                JSONUtil.toJsonStr(mongoDatum), mongoDatum.getFbaShipmentId());
        if (!SendStatus.SEND_OK.equals(result.getSendStatus())){
            throw new RuntimeException(StrUtil.format("发送MQ数据异常，{}", JSONUtil.toJsonStr(result)));
        }
    }

}
