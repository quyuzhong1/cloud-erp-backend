package com.erp.server.dmp.pull.service.lingxing;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.NumberUtil;
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
import com.erp.model.dmp.lingxing.ShopEntity;
import com.erp.server.dmp.convert.DmpShopInfoConverter;
import com.erp.server.dmp.pull.mongo.MongoService;
import com.erp.server.dmp.service.CfgSettingService;
import com.erp.server.dmp.service.ShopInfoMappingService;
import com.erp.server.dmp.utils.DataCompareUtil;
import com.sdk.third.lingxing.dto.ShopInfoDTO;
import com.sdk.third.lingxing.utils.LingxingApiUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 领星店铺
 */
@Slf4j
@Component
@SaveData(method = PlatformApiEnum.LX_ERP_SHOP_LIST_GET)
public class LxShopInfoServiceImpl implements IReportSaveService<ShopEntity> {
    @Resource
    private MongoService mongoService;
    @Resource
    private MQProducerService mqProducerService;
    @Resource
    private CfgSettingService cfgSettingService;
    @Resource
    private ShopInfoMappingService shopInfoMappingService;

    @Override
    // @Transactional(rollbackFor = Exception.class, transactionManager = "mongoTransactionManager")
    public void pullDataSave(RequestDTO dto) {
        List<ShopEntity> entityList = pullDate();

        if (CollectionUtil.isEmpty(entityList)) {
            log.info("拉取领星店铺列表数据为空 entityList.size = 0 ");
            return;
        }
        log.info("拉取领星店铺列表 entityList.size = {} ", entityList.size());
        // 历史映射记录
        List<ShopInfoMappingEntity> mappingEntityList = shopInfoMappingService.listByType(PlatformEnum.LINGXING.getName());
        List<String> platformShopIdList = mappingEntityList.stream().map(ShopInfoMappingEntity::getThirdPlatformShopId).distinct().collect(Collectors.toList());

        List<ShopEntity> newShopEntityList = entityList.stream()
                .filter(e -> CollectionUtils.isEmpty(platformShopIdList) || !platformShopIdList.contains(e.getSid().toString()))
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(newShopEntityList)){
            log.warn("已映射所有领星店铺列表");
            return;
        }

        List<ShopEntity> insertList = new ArrayList<>();
        List<ShopEntity> entityToMqlist = new ArrayList<>();
        for (ShopEntity entity : entityList) {
            UniqueDto orderMongoDTO = UniqueDto.getUniqId(entity.getSid().toString());
            List<ShopEntity> mongoData = mongoService.findMongoData(orderMongoDTO, 0, 0, MongoTableNameContant.ORIGINAL_LX_SHOP_LIST, ShopEntity.class);
            entity.setIsClean(CleanStatusEnum.UNCLEAN.getCode());
            entity.setDownloadTime(LocalDateUtil.formatTime(LocalDateTime.now(), DateUtil.fmt));
            if(CollectionUtil.isEmpty(mongoData)){
                insertList.add(entity);
                entityToMqlist.add(entity);
                continue;
            }
            ShopEntity mongoDatum = mongoData.get(0);
            // 比较数据是否相同
            if (DataCompareUtil.compareObject(mongoDatum , entity)) {
                continue;
            }
            entityToMqlist.add(entity);
            MapUtil mapUtil = JSONObject.parseObject(JSONObject.toJSONString(entity), MapUtil.class);
            mongoService.updateMongoData(orderMongoDTO, mapUtil, MongoTableNameContant.ORIGINAL_LX_SHOP_LIST, ShopEntity.class);
        }
        if(CollUtil.isNotEmpty(insertList)){
            mongoService.saveMongoDataMult(insertList, MongoTableNameContant.ORIGINAL_LX_SHOP_LIST);
        }
        if (CollectionUtil.isEmpty(entityToMqlist)){
            log.warn("领星店铺信息, 无需推送到MQ dto={}", JSONUtil.toJsonStr(dto));
            return;
        }

        // 异步推送到MQ
        List<ShopEntity> shopEntityList = entityToMqlist.stream().peek(msg ->{
            SendResult result = mqProducerService.syncClassMsg(RocketMqTopic.DMP_ERP_ORDER_TOPIC, RocketMqTagEnum.LX_SHOP_INFO_TAG.getName(), JSONUtil.toJsonStr(msg),  msg.getSid().toString());
            if (!SendStatus.SEND_OK.equals(result.getSendStatus())){
                throw new ServiceException(StrUtil.format("发送MQ数据异常，{}", JSONUtil.toJsonStr(result)));
            }
        }).collect(Collectors.toList());
        log.debug("领星店铺数据为：{}" , JSON.toJSONString(shopEntityList));
    }

    @Override
    public void cleanDataSave(String tableName, int size) {
        // 查询mongo待推送数据
        String value = cfgSettingService.getValue(SettingEnum.CLEAN_JOB_DELAY_MINUTE);
        Integer delayMinute = null != value ? NumberUtil.parseInt(value) : 0;
        OrderMongoDTO orderMongoDTO = OrderMongoDTO.getByIsCleanDateStr(CleanStatusEnum.UNCLEAN.getCode(), delayMinute);
        List<ShopEntity> mongoData = mongoService.findMongoData(orderMongoDTO, 1, size, MongoTableNameContant.ORIGINAL_LX_SHOP_LIST, ShopEntity.class);
        if (CollectionUtil.isEmpty(mongoData)) {
            return;
        }
        for (ShopEntity mongoDatum : mongoData) {
            mongoDatum.setIsClean(CleanStatusEnum.CLEANING.getCode());
            mongoDatum.setLastPushTime(LocalDateUtil.formatTime(LocalDateTime.now(), DateUtil.fmt));
            updateAndSaveDb(mongoDatum);
        }
    }

    @Override
    // @Transactional(rollbackFor = Exception.class, transactionManager = "mongoTransactionManager")
    public void updateAndSaveDb(ShopEntity shopInfo) {
        UniqueDto updateDto = UniqueDto.getUniqId(shopInfo.getSid().toString());
        if(null == shopInfo){
            shopInfo.setIsClean(CleanStatusEnum.CLEANED.getCode());
            MapUtil mapUtil = JSONObject.parseObject(JSONObject.toJSONString(shopInfo), MapUtil.class);
            mongoService.updateMongoData(updateDto, mapUtil, MongoTableNameContant.ORIGINAL_LX_SHOP_LIST, ShopEntity.class);
            return;
        }
        MapUtil mapUtil = JSONObject.parseObject(JSONObject.toJSONString(shopInfo), MapUtil.class);
        mongoService.updateMongoData(updateDto, mapUtil, MongoTableNameContant.ORIGINAL_LX_SHOP_LIST, ShopEntity.class);
        SendResult result = mqProducerService.syncClassMsg(RocketMqTopic.DMP_ERP_ORDER_TOPIC, RocketMqTagEnum.LX_SHOP_INFO_TAG.getName(),
                JSONUtil.toJsonStr(shopInfo), shopInfo.getSid().toString());
        if (!SendStatus.SEND_OK.equals(result.getSendStatus())){
            throw new RuntimeException(StrUtil.format("发送MQ数据异常，{}", JSONUtil.toJsonStr(result)));
        }
    }

    /**
     * 请求领星店铺信息接口
     */
    private List<ShopEntity> pullDate() {
        // 请求领星店铺信息接口
        // 滤得到授权正常的店铺
        List<ShopInfoDTO> allShopList = LingxingApiUtils.getAllShopList();
//                .stream()
//                .filter(e-> 1 == e.getStatus())
//                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(allShopList)){
            return Collections.emptyList();
        }
        return DmpShopInfoConverter.INSTANCE.dtoListToEntityList(allShopList);
    }
}
