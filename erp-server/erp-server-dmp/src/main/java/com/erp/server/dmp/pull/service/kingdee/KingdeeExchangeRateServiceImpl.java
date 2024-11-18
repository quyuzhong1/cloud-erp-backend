package com.erp.server.dmp.pull.service.kingdee;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.common.business.annotation.SaveData;
import com.common.business.constant.MongoTableNameContant;
import com.common.business.dto.RequestDTO;
import com.common.business.enums.PlatformApiEnum;
import com.common.business.service.IReportSaveService;
import com.common.core.exception.ServiceException;
import com.common.core.utils.MapUtil;
import com.common.core.utils.date.EnumTimePattern;
import com.common.message.constant.RocketMqTopic;
import com.common.message.enums.RocketMqTagEnum;
import com.common.message.service.mq.MQProducerService;
import com.erp.model.dmp.dto.DmpExchangeRateDTO;
import com.erp.model.dmp.dto.OrderMongoDTO;
import com.erp.model.dmp.enums.CleanStatusEnum;
import com.erp.model.dmp.enums.PlatformEnum;
import com.erp.model.dmp.enums.SettingEnum;
import com.erp.model.dmp.kingdee.KingdeeExchangeRateEntity;
import com.erp.sdk.third.kingdee.utils.KingdeeApiUtils;
import com.erp.server.dmp.pull.mongo.MongoService;
import com.erp.server.dmp.service.CfgSettingService;
import com.erp.server.dmp.utils.DataCompareUtil;

import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Will
 * @version 1.0
 * @description: 拉取金蝶汇率
 * @date 2023/8/14 14:13
 */
@Slf4j
@Component
@SaveData(method = PlatformApiEnum.BD_RATE)
public class KingdeeExchangeRateServiceImpl implements IReportSaveService<KingdeeExchangeRateEntity> {

    @Resource
    private MongoService mongoService;

    @Resource
    private MQProducerService<DmpExchangeRateDTO> mqProducerService;

    @Resource
    private CfgSettingService cfgSettingService;

    @Override
    // @Transactional(rollbackFor = Exception.class, transactionManager = "mongoTransactionManager")
    public void pullDataSave(RequestDTO dto) {
        List<KingdeeExchangeRateEntity> entityList = pullDate(dto);
        if (CollectionUtil.isEmpty(entityList)) {
            log.info("拉取金蝶汇率列表数据为空 entityList.size = 0 ");
            return;
        }
        log.info("拉取金蝶汇率列表数据 entityList.size = {} ", entityList.size());
        List<KingdeeExchangeRateEntity> insertList = new ArrayList<>();
        List<KingdeeExchangeRateEntity> pushToMqList = new ArrayList<>();
        for (KingdeeExchangeRateEntity entity : entityList) {
            OrderMongoDTO orderMongoDTO = new OrderMongoDTO(entity.getId());
            List<KingdeeExchangeRateEntity> mongoData = mongoService.findMongoData(orderMongoDTO, 0, 0, MongoTableNameContant.ORIGINAL_KINGDEE_EXCHANGE_RATE, KingdeeExchangeRateEntity.class);
            entity.setIsClean(CleanStatusEnum.UNCLEAN.getCode());
            entity.setDownloadTime(LocalDateTime.now().toString());
            if(CollectionUtil.isEmpty(mongoData)){
                insertList.add(entity);
                pushToMqList.add(entity);
                continue;
            }
            KingdeeExchangeRateEntity mongoDatum = mongoData.get(0);
            // 比较数据是否相同
            if (DataCompareUtil.compareObject(mongoDatum , entity)) {
                continue;
            }
            pushToMqList.add(entity);
            MapUtil mapUtil = JSONObject.parseObject(JSONObject.toJSONString(entity), MapUtil.class);
            OrderMongoDTO updateDto = new OrderMongoDTO(mongoDatum.getId());
            mongoService.updateMongoData(updateDto, mapUtil, MongoTableNameContant.ORIGINAL_KINGDEE_EXCHANGE_RATE, KingdeeExchangeRateEntity.class);
        }
        if(CollectionUtil.isNotEmpty(insertList)){
            mongoService.saveMongoDataMult(insertList, MongoTableNameContant.ORIGINAL_KINGDEE_EXCHANGE_RATE);
        }
        if (CollectionUtil.isEmpty(pushToMqList)){
            log.warn("金蝶汇率列表, 无需推送到MQ dto={}", JSONUtil.toJsonStr(dto));
            return;
        }
        // 构造订单结构
        List<DmpExchangeRateDTO> entityToMqlist = pushToMqList.stream()
                .map(this::initExchangeRateEntity)
                .filter(ObjectUtil::isNotEmpty)
                .collect(Collectors.toList());


        // 异步推送到MQ
        List<DmpExchangeRateDTO> dmpExchangeRateDTOList = entityToMqlist.stream().peek(msg ->{
            SendResult result = mqProducerService.syncClassMsg(RocketMqTopic.DMP_ERP_ORDER_TOPIC, RocketMqTagEnum.KINGDEE_EXCHANGE_RATE_TAG.getName(),
                    msg, StrUtil.format("{}_{}", msg.getSourceId(), msg.getSourceCurrencyCode()));
            if (!SendStatus.SEND_OK.equals(result.getSendStatus())){
                throw new ServiceException(StrUtil.format("发送金蝶汇率列表MQ数据异常，{}", JSONUtil.toJsonStr(result)));
            }
        }).collect(Collectors.toList());
        log.debug("金蝶汇率发送数据为：{}" , JSON.toJSONString(dmpExchangeRateDTOList));
    }

    @Override
    public void cleanDataSave(String tableName, int size) {
        // 查询mongo待推送数据
        String value = cfgSettingService.getValue(SettingEnum.CLEAN_JOB_DELAY_MINUTE);
        Integer delayMinute = null != value ? NumberUtil.parseInt(value) : 0;
        OrderMongoDTO orderMongoDTO = OrderMongoDTO.getByIsCleanDateStr(CleanStatusEnum.UNCLEAN.getCode(), delayMinute);
        List<KingdeeExchangeRateEntity> mongoData = mongoService.findMongoData(orderMongoDTO, 1, size, MongoTableNameContant.ORIGINAL_KINGDEE_EXCHANGE_RATE, KingdeeExchangeRateEntity.class);
        if (CollectionUtil.isEmpty(mongoData)) {
            return;
        }
        for (KingdeeExchangeRateEntity mongoDatum : mongoData) {
            mongoDatum.setIsClean(CleanStatusEnum.CLEANING.getCode());
            mongoDatum.setLastPushTime(LocalDateTime.now().toString());
            updateAndSaveDb(mongoDatum);
        }
    }

    @Override
    // @Transactional(rollbackFor = Exception.class, transactionManager = "mongoTransactionManager")
    public void updateAndSaveDb(KingdeeExchangeRateEntity mongoDatum) {
        DmpExchangeRateDTO exchangeRateDTO = initExchangeRateEntity(mongoDatum);
        OrderMongoDTO updateDto = new OrderMongoDTO(mongoDatum.getId());
        if(null == exchangeRateDTO){
            mongoDatum.setIsClean(CleanStatusEnum.CLEANED.getCode());
            MapUtil mapUtil = JSONObject.parseObject(JSONObject.toJSONString(mongoDatum), MapUtil.class);
            mongoService.updateMongoData(updateDto, mapUtil, MongoTableNameContant.ORIGINAL_KINGDEE_EXCHANGE_RATE, KingdeeExchangeRateEntity.class);
            return;
        }
        MapUtil mapUtil = JSONObject.parseObject(JSONObject.toJSONString(mongoDatum), MapUtil.class);
        mongoService.updateMongoData(updateDto, mapUtil, MongoTableNameContant.ORIGINAL_KINGDEE_EXCHANGE_RATE, KingdeeExchangeRateEntity.class);
        SendResult result = mqProducerService.syncClassMsg(RocketMqTopic.DMP_ERP_ORDER_TOPIC, RocketMqTagEnum.KINGDEE_EXCHANGE_RATE_TAG.getName(),
                exchangeRateDTO, StrUtil.format("{}_{}", exchangeRateDTO.getSourceId(), exchangeRateDTO.getSourceCurrencyCode()));
        if (!SendStatus.SEND_OK.equals(result.getSendStatus())){
            throw new RuntimeException(StrUtil.format("发送金蝶汇率列表MQ数据异常，{}", JSONUtil.toJsonStr(result)));
        }
    }

    /**
     * 请求金蝶云星空订单接口
     * @param dto
     * @return
     */
    public List<KingdeeExchangeRateEntity> pullDate(RequestDTO dto) {
        LocalDateTime lastTime = dto.getJobTaskDTO().getLastTime();
        LocalDateTime nextTime = dto.getJobTaskDTO().getNextTime();
        DateTimeFormatter sdf = DateTimeFormatter.ofPattern(EnumTimePattern.y_m_dhms.toTimePattern());
        LinkedList<String> queryFilters = new LinkedList<>();
        queryFilters.add(StrUtil.format("FDocumentStatus in ({})", "'B','C','D'"));
        //组织默认查唯迹
        queryFilters.add(StrUtil.format("FCreateOrgId.FNumber = '100'"));
        queryFilters.add(StrUtil.format("FUseOrgId.FNumber = '100'"));
        //现在只查固定汇率
        queryFilters.add(StrUtil.format("FRATETYPEID.FNumber = 'HLTX01_SYS'"));
        queryFilters.add(StrUtil.format("((FForbidDate >= '{}' and FForbidDate < '{}') or (FAuditDate >= '{}' and FAuditDate < '{}') or FAuditDate is null)",sdf.format(lastTime.minusMinutes(8)),sdf.format(nextTime),sdf.format(lastTime.minusMinutes(8)),sdf.format(nextTime)));

        String filterStr = String.join(" and ",  queryFilters );

        String fieldKeys = "FRateID,FRATETYPEID.FNumber,FCyForID.FNumber,FCyToID.FNumber,FExchangeRate,FReverseExRate,FBegDate,FEndDate,FDocumentStatus,FAuditDate,FForbidDate";

        boolean dataSign = true;
        //当前页数
        Integer pageIndex = 1;
        //每次最多获取100条
        Integer pageSize = 10000;
        List<Map<String, Object>> resultAll = new ArrayList<>();
        while (dataSign) {
            //"StartRow\":0,"+// 分页取数开始行索引，从0开始，例如每页10行数据，第2页开始是10，第3页开始是20
            KingdeeApiUtils kingdeeApiUtils = new KingdeeApiUtils(dto.getPlatformApiEnum().getTaskName());
            List<Map<String, Object>> result = kingdeeApiUtils.queryList(filterStr, fieldKeys, pageSize, pageIndex, 0);
            log.info("获取金蝶汇率列表数据第[{}]页 有{}条记录", pageIndex, pageSize);
            if (result.size() < pageSize){
                dataSign = false;
            }
            if (CollectionUtil.isEmpty(result)) {
                break;
            }
            resultAll.addAll(result);
            pageIndex++;
        }
        List<KingdeeExchangeRateEntity> entityList = resultAll.stream().map(entity ->
                        BeanUtil.toBeanIgnoreError(entity, KingdeeExchangeRateEntity.class)).distinct()
                .collect(Collectors.toList());

        return entityList;
    }

    /**
     * 解析订单数据
     **/
    public DmpExchangeRateDTO initExchangeRateEntity(KingdeeExchangeRateEntity entity) {
        //此处不跳过订单，避免订单修改仓库编码后，数据无法同步
        DmpExchangeRateDTO resultEntity = new DmpExchangeRateDTO();
        resultEntity.setType(entity.getFRateTypeNumber());
        resultEntity.setSourceCurrencyCode(entity.getFCyForIDFNumber());
        resultEntity.setTargetCurrencyCode(entity.getFCyToIDFNumber());
        resultEntity.setExchangeRate(entity.getFExchangeRate());
        resultEntity.setIndirectExchangeRate(entity.getFReverseExRate());
        resultEntity.setSettlementDateBegin(entity.getFBegDate());
        resultEntity.setSettlementDateEnd(entity.getFEndDate());
        resultEntity.setSourceId(entity.getId());
        resultEntity.setPlatformSign(PlatformEnum.KINGDEE.getDesc());
        resultEntity.setApproveStatus(entity.getFDocumentStatus());
        resultEntity.setApproveDate(entity.getFAuditDate());
        resultEntity.setDisabledDate(entity.getFForbidDate());
        resultEntity.setDisabled(ObjectUtils.isEmpty(entity.getFForbidDate()) ? Boolean.FALSE : Boolean.TRUE);
        return resultEntity;
    }

}
