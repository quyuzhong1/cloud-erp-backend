package com.erp.server.dmp.pull.service.mabang;

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
import com.common.core.utils.MapUtil;
import com.common.core.utils.date.DateUtil;
import com.common.core.utils.date.LocalDateUtil;
import com.common.message.constant.RocketMqTopic;
import com.common.message.enums.RocketMqTagEnum;
import com.common.message.service.mq.MQProducerService;
import com.erp.model.dmp.dto.OrderMongoDTO;
import com.erp.model.dmp.entity.BiShopInfoEntity;
import com.erp.model.dmp.enums.CleanStatusEnum;
import com.erp.model.dmp.enums.PlatformEnum;
import com.erp.model.dmp.enums.SettingEnum;
import com.erp.model.dmp.mabang.ShopEntity;
import com.erp.server.dmp.pull.mongo.MongoService;
import com.erp.server.dmp.service.CfgSettingService;
import com.erp.server.dmp.utils.DataCompareUtil;
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
import java.util.stream.Collectors;

/**
 * 马帮店铺
 */
@Slf4j
@Component
@SaveData(method = PlatformApiEnum.SYS_GET_SHOP_LIST)
public class MabangShopInfoServiceImpl implements IReportSaveService<ShopEntity> {
    @Resource
    private MongoService mongoService;

    @Resource
    private MQProducerService<BiShopInfoEntity> mqProducerService;
    @Resource
    private CfgSettingService cfgSettingService;

    @Override
    // @Transactional(rollbackFor = Exception.class, transactionManager = "mongoTransactionManager")
    public void pullDataSave(RequestDTO dto) {
        List<ShopEntity> entityList = pullDate(dto);
        if (CollectionUtil.isEmpty(entityList)) {
            log.info("拉取马帮店铺列表数据为空 entityList.size = 0 ");
            return;
        }
        log.info("拉取马帮店铺列表 entityList.size = {} ", entityList.size());
        List<ShopEntity> insertList = new ArrayList<>();
        List<ShopEntity> pushToMqList = new ArrayList<>();
        for (ShopEntity entity : entityList) {
            OrderMongoDTO orderMongoDTO = new OrderMongoDTO(entity.getId());
            List<ShopEntity> mongoData = mongoService.findMongoData(orderMongoDTO, 0, 0, MongoTableNameContant.ORIGINAL_MABANG_SHOP, ShopEntity.class);
            entity.setIsClean(CleanStatusEnum.UNCLEAN.getCode());
            entity.setDownloadTime(LocalDateUtil.formatTime(LocalDateTime.now(), DateUtil.fmt));
            if(CollectionUtil.isEmpty(mongoData)){
                insertList.add(entity);
                pushToMqList.add(entity);
                continue;
            }
            ShopEntity mongoDatum = mongoData.get(0);
            // 比较数据是否相同
            if (DataCompareUtil.compareObject(mongoDatum , entity)) {
                continue;
            }
            pushToMqList.add(entity);
            MapUtil mapUtil = JSONObject.parseObject(JSONObject.toJSONString(entity), MapUtil.class);
            mongoService.updateMongoData(orderMongoDTO, mapUtil, MongoTableNameContant.ORIGINAL_MABANG_SHOP, ShopEntity.class);
        }
        if(CollectionUtil.isNotEmpty(insertList)){
            mongoService.saveMongoDataMult(insertList, MongoTableNameContant.ORIGINAL_MABANG_SHOP);
        }
        if (CollectionUtil.isEmpty(pushToMqList)){
            log.warn("马帮店铺信息, 无需推送到MQ dto={}", JSONUtil.toJsonStr(dto));
            return;
        }
        // 构造订单结构
        List<BiShopInfoEntity> entityToMqlist = pushToMqList.stream()
                .map(this::initOrderInfoEntity)
                .filter(ObjectUtil::isNotEmpty)
                .collect(Collectors.toList());

        // 异步推送到MQ
        List<BiShopInfoEntity> biShopInfoEntityList = entityToMqlist.stream().peek(msg ->{
            SendResult result = mqProducerService.syncClassMsg(RocketMqTopic.DMP_ERP_ORDER_TOPIC, RocketMqTagEnum.MABANG_SHOP_INFO_TAG.getName(),
                    msg, StrUtil.format("{}_{}", msg.getPlatformShopNo(), msg.getFinanceCode()));
            if (!SendStatus.SEND_OK.equals(result.getSendStatus())){
                throw new ServiceException(StrUtil.format("发送MQ数据异常，{}", JSONUtil.toJsonStr(result)));
            }
        }).collect(Collectors.toList());
        log.debug("马帮店铺数据为：{}" , JSON.toJSONString(biShopInfoEntityList));
    }

    @Override
    public void cleanDataSave(String tableName, int size) {
        // 查询mongo待推送数据
        String value = cfgSettingService.getValue(SettingEnum.CLEAN_JOB_DELAY_MINUTE);
        Integer delayMinute = null != value ? NumberUtil.parseInt(value) : 0;
        OrderMongoDTO orderMongoDTO = OrderMongoDTO.getByIsCleanDateStr(CleanStatusEnum.UNCLEAN.getCode(), delayMinute);
        List<ShopEntity> mongoData = mongoService.findMongoData(orderMongoDTO, 1, size, MongoTableNameContant.ORIGINAL_MABANG_SHOP, ShopEntity.class);
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
    public void updateAndSaveDb(ShopEntity mongoDatum) {
        BiShopInfoEntity shopInfo = initOrderInfoEntity(mongoDatum);
        OrderMongoDTO updateDto = new OrderMongoDTO(mongoDatum.getId());
        if(null == shopInfo){
            mongoDatum.setIsClean(CleanStatusEnum.CLEANED.getCode());
            MapUtil mapUtil = JSONObject.parseObject(JSONObject.toJSONString(mongoDatum), MapUtil.class);
            mongoService.updateMongoData(updateDto, mapUtil, MongoTableNameContant.ORIGINAL_MABANG_SHOP, ShopEntity.class);
            return;
        }
        MapUtil mapUtil = JSONObject.parseObject(JSONObject.toJSONString(mongoDatum), MapUtil.class);
        mongoService.updateMongoData(updateDto, mapUtil, MongoTableNameContant.ORIGINAL_MABANG_SHOP, ShopEntity.class);
        SendResult result = mqProducerService.syncClassMsg(RocketMqTopic.DMP_ERP_ORDER_TOPIC, RocketMqTagEnum.MABANG_SHOP_INFO_TAG.getName(),
                shopInfo, StrUtil.format("{}_{}", shopInfo.getPlatformShopNo(), shopInfo.getFinanceCode()));
        if (!SendStatus.SEND_OK.equals(result.getSendStatus())){
            throw new RuntimeException(StrUtil.format("发送MQ数据异常，{}", JSONUtil.toJsonStr(result)));
        }
    }

    /**
     * 请求马帮店铺信息接口
     */
    private List<ShopEntity> pullDate(RequestDTO dto) {
        return MabangApiUtils.queryShopList(dto.getPlatformApiEnum().getTaskName());
    }

    /**
     * 解析店铺数据
     **/
    private BiShopInfoEntity initOrderInfoEntity(ShopEntity shopEntity) {
        BiShopInfoEntity biShopInfoEntity = new BiShopInfoEntity();
        //平台店铺编号
        biShopInfoEntity.setPlatformShopNo(shopEntity.getId());
        //平台店铺账户
        biShopInfoEntity.setAccountUserName(shopEntity.getAccountUsername());
        //平台店铺标识
        biShopInfoEntity.setAccountStoreName(shopEntity.getAccountStoreName());
        //店铺名称
        biShopInfoEntity.setName(shopEntity.getName());
        // 店铺站点
        if(StrUtil.isNotBlank(shopEntity.getAmazonsite())){
            biShopInfoEntity.setSite(shopEntity.getAmazonsite());
        }
        //店铺状态
        biShopInfoEntity.setStatus(shopEntity.getStatus());
        //平台名称
        biShopInfoEntity.setPlatformName(shopEntity.getPlatformName());
        // 财务编码
        biShopInfoEntity.setFinanceCode(shopEntity.getFinanceCode());
        //平台标识
        biShopInfoEntity.setPlatformSign(PlatformEnum.MABANG.getDesc());
        biShopInfoEntity.setCreateTime(LocalDateTime.now());
        return biShopInfoEntity;
    }
}
