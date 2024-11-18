package com.erp.server.dmp.pull.service.gyy;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.common.business.annotation.SaveData;
import com.common.business.constant.MongoTableNameContant;
import com.common.business.dto.JobTaskDTO;
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
import com.erp.model.dmp.gyy.GyyShopInfoEntity;
import com.erp.server.dmp.pull.mongo.MongoService;
import com.erp.server.dmp.service.CfgSettingService;
import com.erp.server.dmp.utils.DataCompareUtil;
import com.erp.server.dmp.utils.GyyApiUtils;
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
 * 管易云店铺
 */
@Slf4j
@Component
@SaveData(method = PlatformApiEnum.GY_ERP_SHOP_GET)
public class GyyShopInfoServiceImpl implements IReportSaveService<GyyShopInfoEntity> {

    @Resource
    private MongoService mongoService;

    @Resource
    private MQProducerService<BiShopInfoEntity> mqProducerService;
    @Resource
    private CfgSettingService cfgSettingService;

    public static void main(String[] args) {
        GyyShopInfoServiceImpl gyyShopInfoService = new GyyShopInfoServiceImpl();
        PlatformApiEnum platformApiEnum = PlatformApiEnum.GY_ERP_SHOP_GET;
        JobTaskDTO jobTaskDTO = new JobTaskDTO();
        jobTaskDTO.setApiCode(platformApiEnum.getTaskName());
        jobTaskDTO.setPlatformApiId("12");
        jobTaskDTO.setApiName("管易云查询店铺列表");
        jobTaskDTO.setId("36");
        jobTaskDTO.setIntervalTime(1800);
        jobTaskDTO.setLastTime(null);
        jobTaskDTO.setNextTime(null);
        jobTaskDTO.setDictPlatform("1");
        jobTaskDTO.setStatus(1);
        RequestDTO requestDTO = new RequestDTO();
        requestDTO.setPlatformApiEnum(platformApiEnum);
        requestDTO.setJobTaskDTO(jobTaskDTO);
        List<GyyShopInfoEntity> orderEntities = null;
        try {
            orderEntities = gyyShopInfoService.pullDate(requestDTO);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        System.out.println(orderEntities);
    }


    @Override
    // @Transactional(rollbackFor = Exception.class, transactionManager = "mongoTransactionManager")
    public void pullDataSave(RequestDTO dto) {
        List<GyyShopInfoEntity> entityList = pullDate(dto);
        if (CollectionUtil.isEmpty(entityList)) {
            log.info("拉取管易店铺列表数据为空 entityList.size = 0 ");
            return;
        }
        List<GyyShopInfoEntity> insertList = new ArrayList<>();
        List<GyyShopInfoEntity> pushToMqList = new ArrayList<>();
        for (GyyShopInfoEntity entity : entityList) {
            OrderMongoDTO orderMongoDTO = new OrderMongoDTO(entity.getId());
            List<GyyShopInfoEntity> mongoData = mongoService.findMongoData(orderMongoDTO, 0, 0, MongoTableNameContant.ORIGINAL_GYY_SHOP, GyyShopInfoEntity.class);
            entity.setIsClean(CleanStatusEnum.UNCLEAN.getCode());
            entity.setDownloadTime(LocalDateUtil.formatTime(LocalDateTime.now(), DateUtil.fmt));
            if(CollectionUtil.isEmpty(mongoData)){
                insertList.add(entity);
                pushToMqList.add(entity);
                continue;
            }
            GyyShopInfoEntity mongoDatum = mongoData.get(0);
            // 比较数据是否相同
            if (DataCompareUtil.compareObject(mongoDatum , entity)) {
                continue;
            }
            pushToMqList.add(entity);
            MapUtil mapUtil = JSONObject.parseObject(JSONObject.toJSONString(entity), MapUtil.class);
            mongoService.updateMongoData(orderMongoDTO, mapUtil, MongoTableNameContant.ORIGINAL_GYY_SHOP, GyyShopInfoEntity.class);
        }
        if(CollectionUtil.isNotEmpty(insertList)){
            insertList = insertList.stream().distinct().collect(Collectors.toList());
            mongoService.saveMongoDataMult(insertList, MongoTableNameContant.ORIGINAL_GYY_SHOP);
        }
        if (CollectionUtil.isEmpty(pushToMqList)){
            log.warn("管易店铺数据, 无需推送到MQ dto={}", JSONUtil.toJsonStr(dto));
            return;
        }
        // 构造订单结构
        List<BiShopInfoEntity> entityToMqlist = pushToMqList.stream()
                .map(this::initOrderInfoEntity)
                .filter(ObjectUtil::isNotEmpty)
                .distinct()
                .collect(Collectors.toList());

        // 异步推送到MQ
        List<BiShopInfoEntity> biShopInfoEntityList = entityToMqlist.stream().peek(msg ->{
            SendResult result = mqProducerService.syncClassMsg(RocketMqTopic.DMP_ERP_ORDER_TOPIC, RocketMqTagEnum.GYY_SHOP_INFO_TAG.getName(),
                    msg, StrUtil.format("{}_{}", msg.getPlatformShopNo(), msg.getFinanceCode()));
            if (!SendStatus.SEND_OK.equals(result.getSendStatus())){
                throw new ServiceException(StrUtil.format("发送MQ数据异常，{}", JSONUtil.toJsonStr(result)));
            }
        }).collect(Collectors.toList());
        log.debug("gyyShop发送数据为：{}" , JSON.toJSONString(biShopInfoEntityList));
    }

    @Override
    public void cleanDataSave(String tableName, int size) {
        // 查询mongo待推送数据
        String value = cfgSettingService.getValue(SettingEnum.CLEAN_JOB_DELAY_MINUTE);
        Integer delayMinute = null != value ? NumberUtil.parseInt(value) : 0;
        OrderMongoDTO orderMongoDTO = OrderMongoDTO.getByIsCleanDateStr(CleanStatusEnum.UNCLEAN.getCode(), delayMinute);
        List<GyyShopInfoEntity> mongoData = mongoService.findMongoData(orderMongoDTO, 1, size, MongoTableNameContant.ORIGINAL_GYY_SHOP, GyyShopInfoEntity.class);
        if (CollectionUtil.isEmpty(mongoData)) {
            return;
        }
        for (GyyShopInfoEntity mongoDatum : mongoData) {
            mongoDatum.setIsClean(CleanStatusEnum.CLEANING.getCode());
            mongoDatum.setLastPushTime(LocalDateUtil.formatTime(LocalDateTime.now(), DateUtil.fmt));
            updateAndSaveDb(mongoDatum);
        }
    }

    @Override
    // @Transactional(rollbackFor = Exception.class, transactionManager = "mongoTransactionManager")
    public void updateAndSaveDb(GyyShopInfoEntity mongoDatum) {
        BiShopInfoEntity shopInfo = initOrderInfoEntity(mongoDatum);
        OrderMongoDTO updateDto = new OrderMongoDTO(mongoDatum.getId());
        if(null == shopInfo){
            mongoDatum.setIsClean(CleanStatusEnum.CLEANED.getCode());
            MapUtil mapUtil = JSONObject.parseObject(JSONObject.toJSONString(mongoDatum), MapUtil.class);
            mongoService.updateMongoData(updateDto, mapUtil, MongoTableNameContant.ORIGINAL_GYY_SHOP, GyyShopInfoEntity.class);
            return;
        }
        MapUtil mapUtil = JSONObject.parseObject(JSONObject.toJSONString(mongoDatum), MapUtil.class);
        mongoService.updateMongoData(updateDto, mapUtil, MongoTableNameContant.ORIGINAL_GYY_SHOP, GyyShopInfoEntity.class);
        SendResult result = mqProducerService.syncClassMsg(RocketMqTopic.DMP_ERP_ORDER_TOPIC, RocketMqTagEnum.GYY_SHOP_INFO_TAG.getName(),
                shopInfo, StrUtil.format("{}_{}", shopInfo.getPlatformShopNo(), shopInfo.getFinanceCode()));
        if (!SendStatus.SEND_OK.equals(result.getSendStatus())){
            throw new RuntimeException(StrUtil.format("发送MQ数据异常，{}", JSONUtil.toJsonStr(result)));
        }
    }

    /**
     * 请求管易云店铺接口
     *
     * @param dto
     * @return
     */
    private List<GyyShopInfoEntity> pullDate(RequestDTO dto) {
        LocalDateTime lastTime = dto.getJobTaskDTO().getLastTime();
        LocalDateTime nextTime = dto.getJobTaskDTO().getNextTime();
        if(null == lastTime || null == nextTime){
            lastTime = LocalDateTime.parse("2021-01-01T00:00:00");
            nextTime = LocalDateTime.now();
            dto.getJobTaskDTO().setNextTime(nextTime);
            dto.getJobTaskDTO().setLastTime(lastTime);
        }
        return GyyApiUtils.queryShopList(dto.getPlatformApiEnum().getTaskName(), lastTime, nextTime);
    }

    /**
     * 解析店铺数据
     **/
    private BiShopInfoEntity initOrderInfoEntity(GyyShopInfoEntity shopInfoEntity) {
        BiShopInfoEntity biShopInfoEntity = new BiShopInfoEntity();
        //平台店铺编号
        biShopInfoEntity.setPlatformShopNo(shopInfoEntity.getCode());
        //平台店铺账户
        biShopInfoEntity.setAccountUserName("");
        //平台店铺标识
        biShopInfoEntity.setAccountStoreName(shopInfoEntity.getNick());
        //店铺名称
        biShopInfoEntity.setName(shopInfoEntity.getName());
        //店铺站点
        biShopInfoEntity.setSite("CN");
        //店铺状态:1启用 2停用
        biShopInfoEntity.setStatus(1);
        //平台名称
        biShopInfoEntity.setPlatformName(shopInfoEntity.getTypeName());
        //财务编码
        biShopInfoEntity.setFinanceCode("");
        //平台标识
        biShopInfoEntity.setPlatformSign(PlatformEnum.GYY.getDesc());
        biShopInfoEntity.setCreateTime(LocalDateTime.now());
        return biShopInfoEntity;
    }

    /**
     * dmpShopInfoService.checkOrder(dmpShopInfoEntity);
     */
}
