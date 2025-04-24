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
import com.common.business.dto.JobTaskDTO;
import com.common.business.dto.RequestDTO;
import com.common.business.enums.ErpServerModuleEnum;
import com.common.business.enums.PlatformApiEnum;
import com.common.business.service.IReportSaveService;
import com.common.business.utils.RedisUtil;
import com.common.core.exception.ServiceException;
import com.common.core.utils.MapUtil;
import com.common.core.utils.date.DateUtil;
import com.common.core.utils.date.LocalDateUtil;
import com.common.message.constant.RedisKeyConstant;
import com.common.message.constant.RocketMqTopic;
import com.common.message.enums.RocketMqTagEnum;
import com.common.message.service.mq.MQProducerService;
import com.erp.model.dmp.dto.OrderMongoDTO;
import com.erp.model.dmp.entity.DmpErrorLogEntity;
import com.erp.model.dmp.enums.CleanStatusEnum;
import com.erp.model.dmp.enums.PlatformEnum;
import com.erp.model.dmp.enums.SettingEnum;
import com.erp.model.dmp.mabang.ComboSkuInfoEntity;
import com.erp.model.dmp.mabang.RedisMabngSkuEntity;
import com.erp.model.dmp.mabang.SkuInfoEntity;
import com.erp.model.msg.dto.WarnMsgInfoDTO;
import com.erp.model.msg.enums.WarnMsgTypeEnum;
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
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 马帮商品
 */
@Slf4j
@Component
@SaveData(method = PlatformApiEnum.STOCK_DO_SEARCH_SKU_LIST)
public class MabangSkuInfoServiceImpl implements IReportSaveService<SkuInfoEntity> {

    @Resource
    private MongoService mongoService;
    @Resource
    private CfgSettingService cfgSettingService;
    @Resource
    private MQProducerService<ComboSkuInfoEntity> mqProducerService;
    @Resource
    private RedisUtil redisUtil;

    @Override
    // @Transactional(rollbackFor = Exception.class, transactionManager = "mongoTransactionManager")
    public void pullDataSave(RequestDTO dto) {
        List<SkuInfoEntity> entityList = pullDate(dto);
        if (CollectionUtil.isEmpty(entityList)) {
            log.info("拉取马帮SKU信息列表数据为空 entityList.size = 0 ");
            return;
        }
        log.info("拉取马帮SKU信息列表数据 entityList.size = {} ", entityList.size());
        List<SkuInfoEntity> insertList = new ArrayList<>();
        List<SkuInfoEntity> pushToMqList = new ArrayList<>();
        for (SkuInfoEntity entity : entityList) {
            if(StrUtil.isBlank(entity.getFinancial())){
//                sendWarnMsg(StrUtil.format("财务编码为空， 库存sku编码 = {}", entity.getStockSku()), dto.getJobTaskDTO());
//                log.error(StrUtil.format("财务编码为空， 库存sku编码 = {}", entity.getStockSku()), dto.getJobTaskDTO());
                continue;
            }
            OrderMongoDTO orderMongoDTO = OrderMongoDTO.getByFinancial(entity.getFinancial());
            List<SkuInfoEntity> mongoData = mongoService.findMongoData(orderMongoDTO, 0, 0, MongoTableNameContant.ORIGINAL_MABANG_SKU, SkuInfoEntity.class);
            entity.setIsClean(CleanStatusEnum.UNCLEAN.getCode());
            entity.setDownloadTime(LocalDateUtil.formatTime(LocalDateTime.now(), DateUtil.fmt));
            if(CollectionUtil.isEmpty(mongoData)){
                insertList.add(entity);
                pushToMqList.add(entity);
                continue;
            }
            SkuInfoEntity mongoDatum = mongoData.get(0);
            // 比较数据是否相同
            if (DataCompareUtil.compareObject(mongoDatum , entity)) {
                continue;
            }
            pushToMqList.add(entity);
            MapUtil mapUtil = JSONObject.parseObject(JSONObject.toJSONString(entity), MapUtil.class);
            mapUtil.remove("id");
            mongoService.updateMongoData(orderMongoDTO, mapUtil, MongoTableNameContant.ORIGINAL_MABANG_SKU, SkuInfoEntity.class);
        }
        if(CollectionUtil.isNotEmpty(insertList)){
            mongoService.saveMongoDataMult(insertList, MongoTableNameContant.ORIGINAL_MABANG_SKU);
        }
        // 把所有马帮sku保存到redis中
        List<RedisMabngSkuEntity> mabangSkuInfo = mongoService.findMongoData(new OrderMongoDTO(), 0, 0, MongoTableNameContant.ORIGINAL_MABANG_SKU, RedisMabngSkuEntity.class);
        if (CollectionUtil.isNotEmpty(mabangSkuInfo)){
            Map<String, RedisMabngSkuEntity> mabangFinacialSkuMap = mabangSkuInfo.stream().distinct().filter(sku -> StrUtil.isNotBlank(sku.getFinancial())).collect(Collectors.toMap(RedisMabngSkuEntity::getFinancial, e -> e));
            redisUtil.putAllHashMap(RedisKeyConstant.MABANG_FINANCIAL_SKU_LIST_KEY, mabangFinacialSkuMap);
            Map<String, RedisMabngSkuEntity> mabangStockSkuMap = mabangSkuInfo.stream().distinct()
                    .filter(sku -> StrUtil.isNotBlank(sku.getStockSku()) && StrUtil.isNotBlank(sku.getFinancial()))
                    .collect(Collectors.toMap(RedisMabngSkuEntity::getStockSku, e -> e, (existingPerson, newPerson) -> newPerson));
            redisUtil.putAllHashMap(RedisKeyConstant.MABANG_STOCK_SKU_LIST_KEY, mabangStockSkuMap);
        }

        // 推送到MQ
        if (CollectionUtil.isEmpty(pushToMqList)){
            log.warn("马帮加工SKU订单, 无需推送到MQ dto={}", JSONUtil.toJsonStr(dto));
            return;
        }
        // 构造订单结构
        List<ComboSkuInfoEntity> entityToMqlist = pushToMqList.stream()
                .map(this::initOrderInfoEntity)
                .filter(ObjectUtil::isNotEmpty)
                .collect(Collectors.toList());

        // 异步推送到MQ
        List<ComboSkuInfoEntity> comboSkuInfoEntityList = entityToMqlist.stream().peek(msg ->{
            SendResult result = mqProducerService.syncClassMsg(RocketMqTopic.DMP_ERP_ORDER_TOPIC, RocketMqTagEnum.MABANG_SKU_MACHINING_INFO_TAG.getName(),
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
        List<SkuInfoEntity> mongoData = mongoService.findMongoData(orderMongoDTO, 1, size, MongoTableNameContant.ORIGINAL_MABANG_SKU, SkuInfoEntity.class);
        if (CollectionUtil.isEmpty(mongoData)) {
            return;
        }
        for (SkuInfoEntity mongoDatum : mongoData) {
            mongoDatum.setIsClean(CleanStatusEnum.CLEANING.getCode());
            mongoDatum.setLastPushTime(LocalDateUtil.formatTime(LocalDateTime.now(), DateUtil.fmt));
            updateAndSaveDb(mongoDatum);
        }
    }

    @Override
    // @Transactional(rollbackFor = Exception.class, transactionManager = "mongoTransactionManager")
    public void updateAndSaveDb(SkuInfoEntity mongoDatum) {
        ComboSkuInfoEntity comboSkuInfo = initOrderInfoEntity(mongoDatum);
        OrderMongoDTO updateDto = new OrderMongoDTO(mongoDatum.getId());
        if(null == comboSkuInfo){
            mongoDatum.setIsClean(CleanStatusEnum.CLEANED.getCode());
            MapUtil mapUtil = JSONObject.parseObject(JSONObject.toJSONString(mongoDatum), MapUtil.class);
            mongoService.updateMongoData(updateDto, mapUtil, MongoTableNameContant.ORIGINAL_MABANG_SKU, SkuInfoEntity.class);
            return;
        }
        MapUtil mapUtil = JSONObject.parseObject(JSONObject.toJSONString(mongoDatum), MapUtil.class);
        mongoService.updateMongoData(updateDto, mapUtil, MongoTableNameContant.ORIGINAL_MABANG_SKU, SkuInfoEntity.class);
        SendResult result = mqProducerService.syncClassMsg(RocketMqTopic.DMP_ERP_ORDER_TOPIC, RocketMqTagEnum.MABANG_SKU_MACHINING_INFO_TAG.getName(),
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
    private List<SkuInfoEntity> pullDate(RequestDTO dto) {
        LocalDateTime lastTime = dto.getJobTaskDTO().getLastTime();
        LocalDateTime nextTime = dto.getJobTaskDTO().getNextTime();
        return MabangApiUtils.querySkuList(dto.getPlatformApiEnum().getTaskName(), lastTime, nextTime);
    }

    /**
     * 解析订单数据
     **/
    public ComboSkuInfoEntity initOrderInfoEntity(SkuInfoEntity skuInfoEntity) {
        if(CollectionUtil.isEmpty(skuInfoEntity.getMachiningData())){
            return null;
        }
        ComboSkuInfoEntity comboSkuInfo = new ComboSkuInfoEntity();
        comboSkuInfo.setComboSku(skuInfoEntity.getStockSku());
        comboSkuInfo.setName(skuInfoEntity.getNameCN());
        comboSkuInfo.setNameEn(skuInfoEntity.getNameEN());
        comboSkuInfo.setRelationType("machining");
        comboSkuInfo.setPlatformSign(PlatformEnum.MABANG.getDesc());
        comboSkuInfo.set_id(skuInfoEntity.getId());
        comboSkuInfo.setFinancialCode(skuInfoEntity.getFinancial());
        comboSkuInfo.setComboProductDetail(initOrderItem(skuInfoEntity));
        return comboSkuInfo;
    }

    /**
     * 解析订单明细数据
     **/
    private List<ComboSkuInfoEntity.ComboProductDetail> initOrderItem(SkuInfoEntity skuInfoEntity){
        return skuInfoEntity.getMachiningData().stream().map(detail -> {
            ComboSkuInfoEntity.ComboProductDetail comboProductDetail = new ComboSkuInfoEntity.ComboProductDetail();
            comboProductDetail.setStockSku(detail.getStockSku());
            comboProductDetail.setNameCN(detail.getNameCN());
            comboProductDetail.setQuantity(detail.getQuantity());
            return comboProductDetail;
        }).collect(Collectors.toList());
    }


    /***
     * 发送预警信息
     */
    private void sendWarnMsg(String msg,  JobTaskDTO jobTaskDTO) {
        WarnMsgInfoDTO warnMsgInfo = new WarnMsgInfoDTO();
        warnMsgInfo.setBizName(jobTaskDTO.getApiName());
        warnMsgInfo.setErpServerModuleEnum(ErpServerModuleEnum.ERP_SERVER_DMP);
        warnMsgInfo.setTitle(StrUtil.format("【{}】从{}拉取至{}失败",jobTaskDTO.getApiName(),jobTaskDTO.getDictPlatform(),"ERP"));
        warnMsgInfo.setTableName("dmp_pull_task");
        warnMsgInfo.setTableId("");
        warnMsgInfo.setKeyInfo(msg);
        warnMsgInfo.setWarnMsgTypeEnum(WarnMsgTypeEnum.SYS_EXCEPTION);
        mqProducerService.sendWarnMsg(warnMsgInfo);
    }
}
