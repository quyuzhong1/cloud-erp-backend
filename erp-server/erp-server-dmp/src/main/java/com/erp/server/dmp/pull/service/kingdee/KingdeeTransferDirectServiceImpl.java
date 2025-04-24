package com.erp.server.dmp.pull.service.kingdee;

import cn.hutool.core.bean.BeanUtil;
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
import com.common.core.constant.CommonConstants;
import com.common.core.exception.ServiceException;
import com.common.core.utils.MapUtil;
import com.common.core.utils.date.DateUtil;
import com.common.core.utils.date.EnumTimePattern;
import com.common.core.utils.date.LocalDateUtil;
import com.common.message.constant.RocketMqTopic;
import com.common.message.enums.RocketMqTagEnum;
import com.common.message.service.mq.MQProducerService;
import com.erp.model.dmp.dto.DmpTransferInfoDTO;
import com.erp.model.dmp.dto.DmpTransferInfoDetailDTO;
import com.erp.model.dmp.dto.OrderMongoDTO;
import com.erp.model.dmp.enums.CleanStatusEnum;
import com.erp.model.dmp.enums.PlatformEnum;
import com.erp.model.dmp.enums.SettingEnum;
import com.erp.model.dmp.kingdee.KingdeeTransferDirectEntity;
import com.erp.model.dmp.kingdee.item.KingdeeTransferDirectItemEntity;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 金蝶云星空订单
 */
@Slf4j
@Component
@SaveData(method = PlatformApiEnum.STK_TRANSFERDIRECT)
public class KingdeeTransferDirectServiceImpl implements IReportSaveService<KingdeeTransferDirectEntity> {
    @Resource
    private MongoService mongoService;

    @Resource
    private MQProducerService<DmpTransferInfoDTO> mqProducerService;

    @Resource
    private CfgSettingService cfgSettingService;


    @Override
    // @Transactional(rollbackFor = Exception.class, transactionManager = "mongoTransactionManager")
    public void pullDataSave(RequestDTO dto) {
        List<KingdeeTransferDirectEntity> entityList = pullDate(dto);
        if (CollectionUtil.isEmpty(entityList)) {
            log.info("拉取直接调拨订单列表数据为空 entityList.size = 0 ");
            return;
        }
        log.info("拉取直接调拨订单列表数据 entityList.size = {} ", entityList.size());
        List<KingdeeTransferDirectEntity> insertList = new ArrayList<>();
        List<KingdeeTransferDirectEntity> pushToMqList = new ArrayList<>();
        for (KingdeeTransferDirectEntity entity : entityList) {
            OrderMongoDTO orderMongoDTO = new OrderMongoDTO(entity.getId());
            List<KingdeeTransferDirectEntity> mongoData = mongoService.findMongoData(orderMongoDTO, 0, 0, MongoTableNameContant.ORIGINAL_KINGDEE_DIRECT_TRANSFER, KingdeeTransferDirectEntity.class);
            entity.setIsClean(CleanStatusEnum.UNCLEAN.getCode());
            entity.setDownloadTime(LocalDateUtil.formatTime(LocalDateTime.now(), DateUtil.fmt));
            if(CollectionUtil.isEmpty(mongoData)){
                insertList.add(entity);
                pushToMqList.add(entity);
                continue;
            }
            KingdeeTransferDirectEntity mongoDatum = mongoData.get(0);
            // 比较数据是否相同
            if (DataCompareUtil.compareObject(mongoDatum , entity)) {
                continue;
            }
            pushToMqList.add(entity);
            MapUtil mapUtil = JSONObject.parseObject(JSONObject.toJSONString(entity), MapUtil.class);
            OrderMongoDTO updateDto = new OrderMongoDTO(mongoDatum.getId());
            mongoService.updateMongoData(updateDto, mapUtil, MongoTableNameContant.ORIGINAL_KINGDEE_DIRECT_TRANSFER, KingdeeTransferDirectEntity.class);
        }
        if(CollectionUtil.isNotEmpty(insertList)){
            mongoService.saveMongoDataMult(insertList, MongoTableNameContant.ORIGINAL_KINGDEE_DIRECT_TRANSFER);
        }
        if (CollectionUtil.isEmpty(pushToMqList)){
            log.warn("金蝶直接调拨订单, 无需推送到MQ dto={}", JSONUtil.toJsonStr(dto));
            return;
        }
        // 构造订单结构
        List<DmpTransferInfoDTO> entityToMqlist = pushToMqList.stream()
                .map(this::initOrderInfoEntity)
                .filter(ObjectUtil::isNotEmpty)
                .collect(Collectors.toList());


        // 异步推送到MQ
        List<DmpTransferInfoDTO> dmpTransferInfoDTOList = entityToMqlist.stream().peek(msg ->{
            if (ObjectUtil.isNotEmpty(msg.getBillDate())) {
                if (msg.getBillDate().toLocalDate().compareTo(LocalDate.parse("2023-07-06")) <= 0) {
                    return;
                }
            }
            SendResult result = mqProducerService.syncClassMsg(RocketMqTopic.DMP_ERP_ORDER_TOPIC, RocketMqTagEnum.KINGDEE_TRANSFER_DIRECT_TAG.getName(),
                    msg, StrUtil.format("{}_{}", msg.getSourceId(), msg.getCode()));
            if (!SendStatus.SEND_OK.equals(result.getSendStatus())){
                throw new ServiceException(StrUtil.format("发送金蝶直接调拨MQ数据异常，{}", JSONUtil.toJsonStr(result)));
            }
        }).collect(Collectors.toList());
        log.debug("金蝶调拨发送数据为：{}" , JSON.toJSONString(dmpTransferInfoDTOList));
    }

    @Override
    public void cleanDataSave(String tableName, int size) {
        // 查询mongo待推送数据
        String value = cfgSettingService.getValue(SettingEnum.CLEAN_JOB_DELAY_MINUTE);
        Integer delayMinute = null != value ? NumberUtil.parseInt(value) : 0;
        OrderMongoDTO orderMongoDTO = OrderMongoDTO.getByIsCleanDateStr(CleanStatusEnum.UNCLEAN.getCode(), delayMinute);
        List<KingdeeTransferDirectEntity> mongoData = mongoService.findMongoData(orderMongoDTO, 1, size, MongoTableNameContant.ORIGINAL_KINGDEE_DIRECT_TRANSFER, KingdeeTransferDirectEntity.class);
        if (CollectionUtil.isEmpty(mongoData)) {
            return;
        }
        for (KingdeeTransferDirectEntity mongoDatum : mongoData) {
            mongoDatum.setIsClean(CleanStatusEnum.CLEANING.getCode());
            mongoDatum.setLastPushTime(LocalDateUtil.formatTime(LocalDateTime.now(), DateUtil.fmt));
            updateAndSaveDb(mongoDatum);
        }
    }

    @Override
    // @Transactional(rollbackFor = Exception.class, transactionManager = "mongoTransactionManager")
    public void updateAndSaveDb(KingdeeTransferDirectEntity mongoDatum) {
        DmpTransferInfoDTO orderInfo = initOrderInfoEntity(mongoDatum);
        OrderMongoDTO updateDto = new OrderMongoDTO(mongoDatum.getId());
        if(null == orderInfo){
            mongoDatum.setIsClean(CleanStatusEnum.CLEANED.getCode());
            MapUtil mapUtil = JSONObject.parseObject(JSONObject.toJSONString(mongoDatum), MapUtil.class);
            mongoService.updateMongoData(updateDto, mapUtil, MongoTableNameContant.ORIGINAL_KINGDEE_DIRECT_TRANSFER, KingdeeTransferDirectEntity.class);
            return;
        }
        MapUtil mapUtil = JSONObject.parseObject(JSONObject.toJSONString(mongoDatum), MapUtil.class);
        mongoService.updateMongoData(updateDto, mapUtil, MongoTableNameContant.ORIGINAL_KINGDEE_DIRECT_TRANSFER, KingdeeTransferDirectEntity.class);
        SendResult result = mqProducerService.syncClassMsg(RocketMqTopic.DMP_ERP_ORDER_TOPIC, RocketMqTagEnum.KINGDEE_TRANSFER_DIRECT_TAG.getName(),
                orderInfo, StrUtil.format("{}_{}", orderInfo.getSourceId(), orderInfo.getCode()));
        if (!SendStatus.SEND_OK.equals(result.getSendStatus())){
            throw new RuntimeException(StrUtil.format("发送金蝶直接调拨MQ数据异常，{}", JSONUtil.toJsonStr(result)));
        }
    }

    /**
     * 请求金蝶云星空订单接口
     * @param dto
     * @return
     */
    public List<KingdeeTransferDirectEntity> pullDate(RequestDTO dto) {
        LocalDateTime lastTime = dto.getJobTaskDTO().getLastTime();
        LocalDateTime nextTime = dto.getJobTaskDTO().getNextTime();
        DateTimeFormatter sdf = DateTimeFormatter.ofPattern(EnumTimePattern.y_m_dhms.toTimePattern());
        LinkedList<String> queryFilters = new LinkedList<>();
        queryFilters.add(StrUtil.format("FDocumentStatus in ({})", "'C'"));
        queryFilters.add(StrUtil.format("FThirdSystem != '{}'", CommonConstants.SYSTEM));
        queryFilters.add(StrUtil.format(" (FApproveDate >= '{}' and FApproveDate < '{}')",sdf.format(lastTime.minusMinutes(8)),sdf.format(nextTime)));
        String filterStr = String.join(" and ",  queryFilters );

        String fieldKeys = "FId,FBillNo,FBizType,FTransferDirect,FTransferBizType,FSaleOrgId,FSaleOrgId.FName," +
                "FSettleOrgId,FSettleOrgId.FName,FStockOutOrgId,FStockOutOrgId.FNumber,FStockOutOrgId.FName,FOwnerOutIdHead,FOwnerOutIdHead.FName," +
                "FStockOrgId,FStockOrgId.FNumber,FStockOrgId.FName,FSettleCurrId,FSettleCurrId.FName,FExchangeTypeId,FExchangeTypeId.FName,FExchangeRate," +
                "FDate,FNote,FBaseCurrId,FBaseCurrId.FName,FDocumentStatus,FDocumentStatus.FCaption,FApproverId,FApproverId.FName,FApproveDate,FSTOCKERID.FNumber," +
                "FCancellerId,FCancellerId.FName,FCreateDate,FCreatorId,FCreatorId.FName,FModifierId,FModifierId.FName,FModifyDate,FCancelStatus,FCancelStatus.FCaption,FCancelDate," +
                "FBillEntry_FEntryID,FSrcStockId,FSrcStockId.FNumber,FSrcStockId.FName,FSrcStockLocId.FF100014.FNumber,FDestStockId,FDestStockId.FNumber,FDestStockId.FName,FDestStockLocId.FF100014.FNumber," +
                "FRowType,FMaterialId,FMaterialId.FNumber,FMaterialId.FName,FUnitID,FUnitID.FName,FQty," +
                "FSrcStockStatusId,FSrcStockStatusId.FName,FDestStockStatusId,FDestStockStatusId.FName,FBusinessDate,FIsFree,FDestMaterialId,FDestMaterialId.FName";;

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
            log.info("获取金蝶直接调拨订单数据第[{}]页 有{}条记录", pageIndex, pageSize);
            if (result.size() < pageSize){
                dataSign = false;
            }
            if (CollectionUtil.isEmpty(result)) {
                break;
            }
            resultAll.addAll(result);
            pageIndex++;
        }
        List<KingdeeTransferDirectEntity> entityList = resultAll.stream().map(entity ->
                BeanUtil.toBeanIgnoreError(entity, KingdeeTransferDirectEntity.class)).distinct()
                .collect(Collectors.toList());

        Map<String, List<KingdeeTransferDirectItemEntity>> itemMap = resultAll.stream().map(entity ->
                BeanUtil.toBeanIgnoreError(entity, KingdeeTransferDirectItemEntity.class))
                .collect(Collectors.groupingBy(KingdeeTransferDirectItemEntity::getFBillNo));
        List<KingdeeTransferDirectEntity> kingdeeTransferDirectEntityList = entityList.stream().peek(m -> m.setItemList(itemMap.get(m.getFBillNo())))
                .distinct()
                .collect(Collectors.toList());
        log.debug("金蝶调拨原始数据为：{}" , JSON.toJSONString(kingdeeTransferDirectEntityList));
        return entityList;
    }

    /**
     * 解析订单数据
     **/
    public DmpTransferInfoDTO initOrderInfoEntity(KingdeeTransferDirectEntity entity) {
        // 过滤小隼和优至胜的订单
        if (StrUtil.isNotBlank(entity.getFSaleOrgIdFName()) && (entity.getFSaleOrgIdFName().contains("小隼") || entity.getFSaleOrgIdFName().contains("优至胜"))) {
            return null;
        }
        //此处不跳过订单，避免订单修改仓库编码后，数据无法同步
        DmpTransferInfoDTO resultEntity = new DmpTransferInfoDTO();
        resultEntity.setCode(entity.getFBillNo());
        resultEntity.setType(entity.getFBizType());
        resultEntity.setTransferType(entity.getFTransferBizType());
        resultEntity.setTransferTypeCode(entity.getFTransferBizType());
        resultEntity.setInOrgId(entity.getFStockOrgId());
        resultEntity.setInOrgCode(entity.getFStockOrgIdFNumber());
        resultEntity.setInOrgName(entity.getFStockOrgIdFName());
        resultEntity.setOutOrgId(entity.getFStockOutOrgId());
        resultEntity.setOutOrgCode(entity.getFStockOutOrgIdFNumber());
        resultEntity.setOutOrgName(entity.getFStockOutOrgIdFName());
        resultEntity.setBillDate(entity.getFDate());
        resultEntity.setApproveStatus(entity.getFDocumentStatus());
        resultEntity.setTransferDirection(entity.getFTransferDirect());
        resultEntity.setPlatformCreateUserName(entity.getFCreatorIdFName());
        resultEntity.setPlatformCreateTime(entity.getFCreateDate());
        resultEntity.setApproveUserName(entity.getFApproverIdFName());
        resultEntity.setApproveTime(entity.getFApproveDate());
        resultEntity.setInvalidStatus(StrUtil.isNotBlank(entity.getFCancelStatus()) && "B".equals(entity.getFCancelStatus()) ? Boolean.TRUE : Boolean.FALSE);
        resultEntity.setInvalidTime(entity.getFCancelDate());
        resultEntity.setInvalidUserName(entity.getFCancellerIdFName());
        resultEntity.setRemark(entity.getFNote());
        resultEntity.setLastUpdatedTime(entity.getFModifyDate());
        resultEntity.setLastUpdatedUserName(entity.getFModifierIdFName());
        resultEntity.setSourceId(entity.getId());
        resultEntity.setPlatformSign(PlatformEnum.KINGDEE.getDesc());
        resultEntity.setDetailList(initOrderItem(entity));
        return resultEntity;
    }

    /**
     * 解析订单商品数据
     **/
    public List<DmpTransferInfoDetailDTO> initOrderItem(KingdeeTransferDirectEntity entity) {
        List<KingdeeTransferDirectItemEntity> itemList = entity.getItemList();
        List<DmpTransferInfoDetailDTO> orderItemList = new ArrayList<>();
        for (KingdeeTransferDirectItemEntity item : itemList) {
            DmpTransferInfoDetailDTO itemEntity = new DmpTransferInfoDetailDTO();
            itemEntity.setSkuNo(item.getFMaterialIdFNumber());
            itemEntity.setProductName(item.getFDestMaterialIdFName());
            itemEntity.setUnit(item.getFUnitIDFName());
            itemEntity.setQty(item.getFQty());
            itemEntity.setSourceDetailId(item.getFEntryId());
            itemEntity.setReceiveTime(item.getFBusinessDate());
            itemEntity.setInStockStatusCode(item.getFDestStockStatusId());
            itemEntity.setInStockStatusName(item.getFDestStockStatusIdFName());
            itemEntity.setOutStockStatusCode(item.getFSrcStockStatusId());
            itemEntity.setOutStockStatusName(item.getFSrcStockStatusIdFName());
            itemEntity.setInWarehouseCode(item.getFDestStockIdFNumber());
            itemEntity.setInWarehouseName(item.getFDestStockIdFName());
            itemEntity.setOutWarehouseCode(item.getFSrcStockIdFNumber());
            itemEntity.setOutWarehouseName(item.getFSrcStockIdFName());
            itemEntity.setInWarehouseLocation(item.getFDestStockLocIdLocation());
            itemEntity.setOutWarehouseLocation(item.getFSrcStockLocIdLocation());
            orderItemList.add(itemEntity);
        }
        return orderItemList;
    }
}
