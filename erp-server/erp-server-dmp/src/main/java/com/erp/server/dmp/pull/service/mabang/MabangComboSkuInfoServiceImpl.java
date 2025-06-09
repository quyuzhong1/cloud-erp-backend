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
import com.erp.model.dmp.enums.CleanStatusEnum;
import com.erp.model.dmp.enums.PlatformEnum;
import com.erp.model.dmp.enums.SettingEnum;
import com.erp.model.dmp.mabang.ComboSkuInfoEntity;
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
 * 马帮商品
 * @author Cloud
 */
@Slf4j
@Component
@SaveData(method = PlatformApiEnum.STOCK_DO_SEARCH_COMBO_SKU)
public class MabangComboSkuInfoServiceImpl implements IReportSaveService<ComboSkuInfoEntity> {

    @Resource
    private MongoService mongoService;
    @Resource
    private CfgSettingService cfgSettingService;
    @Resource
    private MQProducerService<ComboSkuInfoEntity> mqProducerService;

    @Override
    // @Transactional(rollbackFor = Exception.class, transactionManager = "mongoTransactionManager")
    public void pullDataSave(RequestDTO dto) {
        List<ComboSkuInfoEntity> entityList = pullDate(dto);
        if (CollectionUtil.isEmpty(entityList)) {
            log.info("拉取马帮组合SKU信息列表数据为空 entityList.size = 0 ");
            return;
        }
        log.info("拉取马帮组合SKU信息列表数据 entityList.size = {} ", entityList.size());
        List<ComboSkuInfoEntity> insertList = new ArrayList<>();
        List<ComboSkuInfoEntity> pushToMqList = new ArrayList<>();
        for (ComboSkuInfoEntity entity : entityList) {
            OrderMongoDTO orderMongoDTO = OrderMongoDTO.getByComboSku(entity.getComboSku());
            List<ComboSkuInfoEntity> mongoData = mongoService.findMongoData(orderMongoDTO, 0, 0, MongoTableNameContant.ORIGINAL_MABANG_COMBO_SKU, ComboSkuInfoEntity.class);
            entity.setIsClean(CleanStatusEnum.UNCLEAN.getCode());
            entity.setDownloadTime(LocalDateUtil.formatTime(LocalDateTime.now(), DateUtil.fmt));
            if(CollectionUtil.isEmpty(mongoData)){
                insertList.add(entity);
                pushToMqList.add(entity);
                continue;
            }
            ComboSkuInfoEntity mongoDatum = mongoData.get(0);
            // 比较数据是否相同
            if (DataCompareUtil.compareObject(mongoDatum , entity)) {
                continue;
            }
            pushToMqList.add(entity);
            MapUtil mapUtil = JSONObject.parseObject(JSONObject.toJSONString(entity), MapUtil.class);
            mongoService.updateMongoData(orderMongoDTO, mapUtil, MongoTableNameContant.ORIGINAL_MABANG_COMBO_SKU, ComboSkuInfoEntity.class);
        }
        if(CollectionUtil.isNotEmpty(insertList)){
            mongoService.saveMongoDataMult(insertList, MongoTableNameContant.ORIGINAL_MABANG_COMBO_SKU);
        }
        if (CollectionUtil.isEmpty(pushToMqList)){
            log.warn("马帮组合退款订单, 无需推送到MQ dto={}", JSONUtil.toJsonStr(dto));
            return;
        }
        // 构造订单结构
        List<ComboSkuInfoEntity> entityToMqlist = pushToMqList.stream()
                .map(this::initOrderInfoEntity)
                .filter(ObjectUtil::isNotEmpty)
                .collect(Collectors.toList());

        // 异步推送到MQ
        List<ComboSkuInfoEntity> comboSkuInfoEntityList = entityToMqlist.stream().peek(msg ->{
            SendResult result = mqProducerService.syncClassMsg(RocketMqTopic.DMP_ERP_ORDER_TOPIC, RocketMqTagEnum.MABANG_SKU_COMBO_INFO_TAG.getName(),
                    msg, StrUtil.format("{}_{}", msg.getComboSku(), msg.getStatus()));
            if (!SendStatus.SEND_OK.equals(result.getSendStatus())){
                throw new ServiceException(StrUtil.format("发送MQ数据异常，{}", JSONUtil.toJsonStr(result)));
            }
        }).collect(Collectors.toList());
        log.debug("马帮组合品数据为：{}" , JSON.toJSONString(comboSkuInfoEntityList));

    }

    @Override
    public void cleanDataSave(String tableName, int size) {
        // 查询mongo待推送数据
        String value = cfgSettingService.getValue(SettingEnum.CLEAN_JOB_DELAY_MINUTE);
        Integer delayMinute = null != value ? NumberUtil.parseInt(value) : 0;
        OrderMongoDTO orderMongoDTO = OrderMongoDTO.getByIsCleanDateStr(CleanStatusEnum.UNCLEAN.getCode(), delayMinute);
        List<ComboSkuInfoEntity> mongoData = mongoService.findMongoData(orderMongoDTO, 1, size, MongoTableNameContant.ORIGINAL_MABANG_COMBO_SKU, ComboSkuInfoEntity.class);
        if (CollectionUtil.isEmpty(mongoData)) {
            return;
        }
        for (ComboSkuInfoEntity mongoDatum : mongoData) {
            mongoDatum.setIsClean(CleanStatusEnum.CLEANING.getCode());
            mongoDatum.setLastPushTime(LocalDateUtil.formatTime(LocalDateTime.now(), DateUtil.fmt));
            updateAndSaveDb(mongoDatum);
        }
    }

    @Override
    // @Transactional(rollbackFor = Exception.class, transactionManager = "mongoTransactionManager")
    public void updateAndSaveDb(ComboSkuInfoEntity mongoDatum) {
        ComboSkuInfoEntity comboSkuInfo = initOrderInfoEntity(mongoDatum);
        OrderMongoDTO updateDto = new OrderMongoDTO(mongoDatum.get_id());
        if(null == comboSkuInfo){
            mongoDatum.setIsClean(CleanStatusEnum.CLEANED.getCode());
            MapUtil mapUtil = JSONObject.parseObject(JSONObject.toJSONString(mongoDatum), MapUtil.class);
            mongoService.updateMongoData(updateDto, mapUtil, MongoTableNameContant.ORIGINAL_MABANG_COMBO_SKU, ComboSkuInfoEntity.class);
            return;
        }
        MapUtil mapUtil = JSONObject.parseObject(JSONObject.toJSONString(mongoDatum), MapUtil.class);
        mongoService.updateMongoData(updateDto, mapUtil,  MongoTableNameContant.ORIGINAL_MABANG_COMBO_SKU, ComboSkuInfoEntity.class);
        SendResult result = mqProducerService.syncClassMsg(RocketMqTopic.DMP_ERP_ORDER_TOPIC, RocketMqTagEnum.MABANG_SKU_COMBO_INFO_TAG.getName(),
                comboSkuInfo, StrUtil.format("{}_{}", comboSkuInfo.getComboSku(), comboSkuInfo.getStatus()));
        if (!SendStatus.SEND_OK.equals(result.getSendStatus())){
            throw new RuntimeException(StrUtil.format("发送MQ数据异常，{}", JSONUtil.toJsonStr(result)));
        }
    }

    /**
     * 请求马帮商品接口
     * @param dto
     * @return
     */
    private List<ComboSkuInfoEntity> pullDate(RequestDTO dto) {
        LocalDateTime lastTime = dto.getJobTaskDTO().getLastTime();
        LocalDateTime nextTime = dto.getJobTaskDTO().getNextTime();
        return MabangApiUtils.queryComboSkuList(dto.getPlatformApiEnum().getTaskName(), lastTime, nextTime);
    }

    /**
     * 解析订单数据
     **/
    public ComboSkuInfoEntity initOrderInfoEntity(ComboSkuInfoEntity skuInfoEntity) {
        ComboSkuInfoEntity comboSkuInfo = new ComboSkuInfoEntity();
        comboSkuInfo.setComboSku(skuInfoEntity.getComboSku());
        comboSkuInfo.setName(skuInfoEntity.getName());
        comboSkuInfo.setNameEn(skuInfoEntity.getNameEn());
        comboSkuInfo.setComboPicture(skuInfoEntity.getComboPicture());
        comboSkuInfo.setLength(skuInfoEntity.getLength());
        comboSkuInfo.setWidth(skuInfoEntity.getWidth());
        comboSkuInfo.setHeight(skuInfoEntity.getHeight());
        comboSkuInfo.setPackageX(skuInfoEntity.getPackageX());
        comboSkuInfo.setPackageQuantity(skuInfoEntity.getPackageQuantity());
        comboSkuInfo.setDeclareEname(skuInfoEntity.getDeclareEname());
        comboSkuInfo.setDeclareName(skuInfoEntity.getDeclareName());
        comboSkuInfo.setDeclareFee(skuInfoEntity.getDeclareFee());
        comboSkuInfo.setDeclareWeight(skuInfoEntity.getDeclareWeight());
        comboSkuInfo.setDeclareCustoms(skuInfoEntity.getDeclareCustoms());
        comboSkuInfo.setStatus(skuInfoEntity.getStatus());
        comboSkuInfo.setDownloadTime(LocalDateUtil.formatTime(LocalDateTime.now(), DateUtil.fmt));
        comboSkuInfo.setVirtualSku(skuInfoEntity.getVirtualSku());
        comboSkuInfo.setRelationType("combine");
        comboSkuInfo.setPlatformSign(PlatformEnum.MABANG.getDesc());
        comboSkuInfo.setComboProductDetail(initDetail(skuInfoEntity));
        return comboSkuInfo;
    }

    private List<ComboSkuInfoEntity.ComboProductDetail> initDetail(ComboSkuInfoEntity skuInfoEntity) {
        List<ComboSkuInfoEntity.ComboProductDetail> comboProductDetail = skuInfoEntity.getComboProductDetail();
        if(CollectionUtil.isEmpty(comboProductDetail)){
            return null;
        }
        List<ComboSkuInfoEntity.ComboProductDetail> detailList = new ArrayList<>();
        for (ComboSkuInfoEntity.ComboProductDetail productDetail : comboProductDetail) {
            productDetail.setQuantity(productDetail.getQuantity());
            productDetail.setStockSku(productDetail.getStockSku());
            detailList.add(productDetail);
        }
        return detailList;
    }
}
