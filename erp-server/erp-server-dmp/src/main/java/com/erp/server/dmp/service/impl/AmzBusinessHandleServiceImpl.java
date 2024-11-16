package com.erp.server.dmp.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSONObject;
import com.common.business.constant.MongoTableNameContant;
import com.common.business.dto.*;
import com.common.business.enums.*;
import com.common.business.wrapper.FeignQuery;
import com.common.core.controller.vo.ApiResult;
import com.common.core.exception.ServiceException;
import com.common.core.utils.MapUtil;
import com.common.core.utils.date.DateUtil;
import com.common.core.utils.date.LocalDateUtil;
import com.common.message.constant.RocketMqTopic;
import com.common.message.service.mq.MQProducerService;
import com.erp.model.dmp.DmpPullOtherOutStockDTO;
import com.erp.model.dmp.dto.DmpPullSoOutStockDTO;
import com.erp.model.dmp.entity.DmpPullTaskEntity;
import com.erp.model.dmp.enums.CleanStatusEnum;
import com.erp.model.oms.dto.SoB2cErrorDTO;
import com.erp.model.oms.entity.SoB2cDetailEntity;
import com.erp.model.oms.entity.SoB2cErrorEntity;
import com.erp.model.oms.entity.SoMultiChannelDetailEntity;
import com.erp.model.oms.enums.SoB2cErrorTypeEnum;
import com.erp.model.wms.dto.SoOutstockDetailDTO;
import com.erp.rpc.oms.feign.SoB2cFeign;
import com.erp.rpc.wms.feign.SoOutstockFeign;
import com.erp.rpc.wms.feign.WmsAmazonFeign;
import com.erp.sdk.oms.amz.spapi.dto.PlatformAmazonFulfilledShipmentsDTO;
import com.erp.sdk.oms.amz.spapi.enums.AmazonHandleStatusEnum;
import com.erp.sdk.oms.amz.spapi.handler.AmazonFulfilledShipmentsHandler;
import com.erp.server.dmp.enums.DownloadStatusEnum;
import com.erp.server.dmp.pull.mongo.MongoService;
import com.erp.server.dmp.service.AmzBusinessHandleService;
import com.erp.server.dmp.service.DmpPullTaskService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 亚马逊处理 服务类
 *
 * @author Jim
 * @date 2024/3/12
 */
@Slf4j
@Service
public class AmzBusinessHandleServiceImpl implements AmzBusinessHandleService {

    @Resource
    private MongoTemplate mongoTemplate;
    @Resource
    private BusinessServiceImpl businessService;
    @Resource
    private SoOutstockFeign soOutstockFeign;
    @Resource
    private MongoService mongoService;
    @Resource
    private DmpPullTaskService dmpPullTaskService;
    @Resource
    private WmsAmazonFeign wmsAmazonFeign;
    @Resource
    private AmazonFulfilledShipmentsHandler amazonFulfilledShipmentsHandler;
    @Resource
    private SoB2cFeign soB2cFeign;
    @Resource
    private AmzBusinessHandleService amzBusinessHandleService;

    @Override
    public Boolean checkAndSendSoOutStock(DmpPullSoOutStockDTO dto) {
        // 查询来源明细ID
        // 查询异常内容
        SoB2cErrorEntity b2cError = soB2cFeign.getB2cError(dto.getSoB2cId(), SoB2cErrorTypeEnum.GENERATE_OUTSTOCK.getCode());
        if(null == b2cError){
            return true;
        }

        String category = PlatformCategoryEnum.THIRD_SYSTEM.getCode();
        String platform = PlatformDictEnum.AMAZON.getCode();
        String business = BusinessTypeEnum.SO_OUT_STOCK.getCode();
        String tableName = StrUtil.format("{}_{}_{}", category, platform, business);


        // 查询是否有为处理的记录来源
        List<PlatformAmazonFulfilledShipmentsDTO> list = getPlatformAmazonFulfilledShipmentsDTOS(dto, b2cError, tableName);

        if (CollectionUtils.isEmpty(list)) {
            // 移除历史异常
            String type = SoB2cErrorTypeEnum.GENERATE_OUTSTOCK.getCode();
            SoB2cErrorDTO.DeleteDetailDTO deleteDTO = new SoB2cErrorDTO.DeleteDetailDTO();
            deleteDTO.setMainId(dto.getSoB2cId());
            deleteDTO.setType(type);
            deleteDTO.setDetailIdList(Collections.singletonList(b2cError.getDetailId()));
            soB2cFeign.deleteDetailError(deleteDTO);
            return true;
        }

        String topic = RocketMqTopic.PLATFORM_PULL_DATA_TOPIC;
        String tag = StrUtil.format("{}_{}", category, business) + "_tag";
        BusinessTypeEnum businessType = BusinessTypeEnum.getByCodeAndThrow(business);

        for (PlatformAmazonFulfilledShipmentsDTO currentDTO : list) {
            currentDTO.setDownloadStatus(DownloadStatusEnum.FINISH.getCode());
            currentDTO.setHandleStatus(AmazonHandleStatusEnum.HANDLE.getCode());
            amzBusinessHandleService.singleHandlerConsumer(currentDTO, tableName, platform, businessType, topic, tag);
        }
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void singleHandlerConsumer(PlatformAmazonFulfilledShipmentsDTO currentDTO, String tableName, String platform, BusinessTypeEnum businessType, String topic, String tag) {
        List<PlatformSoOutStockDTO> convertList = amazonFulfilledShipmentsHandler.convert(Collections.singletonList(currentDTO));
        PlatformSoOutStockDTO platformSoOutStockDTO = convertList.get(0);
        log.info("手动重试销售出库单：{}", JSONUtil.toJsonStr(platformSoOutStockDTO));
        ApiResult<?> apiResult = wmsAmazonFeign.consumerSoOutStock(platformSoOutStockDTO);
        if (200 != apiResult.getCode()) {
            throw new ServiceException("手动重试销售出库单失败");
        }

        MapUtil mapUtil = getMapParam();
        UniqueDto updateDto = UniqueDto.getUniqId(currentDTO.getUniqueId());
        finishClean(mapUtil, updateDto,tableName, PlatformAmazonFulfilledShipmentsDTO.class);

        String modelTaskId = dmpPullTaskService.saveOrUpdateDmpSyncTask(new DmpPullTaskEntity(platform, businessType.getSourceType().getCode(), "ERP", topic, tag, platformSoOutStockDTO));
        // 同步处理
        dmpPullTaskService.updateSyncInfo(String.valueOf(modelTaskId), SyncStatusEnum.SUCCESS_SYNC.getCode(), "同步成功");

        log.info("手动重试销售出库单结果：{}", JSONUtil.toJsonStr(apiResult));
    }

    @Override
    public Boolean checkAndSendOtherOutStock(DmpPullOtherOutStockDTO dto) {
        // 查询来源明细ID
        List<SoMultiChannelDetailEntity> detailEntityList =  FeignQuery.create(SoMultiChannelDetailEntity.class)
                .eq(SoMultiChannelDetailEntity::getMainId, dto.getMainId())
                .list();

        if (CollectionUtils.isEmpty(detailEntityList)){
            throw new ServiceException("检查生成亚马逊多渠道订单其他出库单失败,为找到明细:id=" + dto.getMainId());
        }
        List<String> sourceDetailIds = detailEntityList.stream()
                .map(SoMultiChannelDetailEntity::getSourceDetailId)
                .collect(Collectors.toList());

        BusinessTypeEnum businessType = BusinessTypeEnum.OTHER_OUT_STOCK;
        String category = PlatformCategoryEnum.THIRD_SYSTEM.getCode();
        String business = businessType.getCode();
        String tableName = MongoTableNameContant.THIRD_SYSTEM_AMAZON_SO_OUT_STOCK;
        // 查询是否有为处理的记录来源
        Query query = new Query();
        query.addCriteria(
                Criteria.where("amazonOrderId").is(dto.getPlatformCode())
                        // 亚马逊多渠道订单的OrderItemId=merchantOrderItemId
                        .and("merchantOrderItemId").in(sourceDetailIds)
//                        .and("isClean").ne(CleanStatusEnum.CLEANED.getCode())
        );
        List<PlatformAmazonFulfilledShipmentsDTO> list = mongoTemplate.find(query, PlatformAmazonFulfilledShipmentsDTO.class, tableName);
        if (CollectionUtils.isEmpty(list)) {
            return true;
        }
        String topic = RocketMqTopic.PLATFORM_PULL_DATA_TOPIC;
        String tag = StrUtil.format("{}_{}", category, business) + "_tag";


        Map<String, SoMultiChannelDetailEntity> detailMap = detailEntityList.stream().collect(Collectors.toMap(SoMultiChannelDetailEntity::getSourceDetailId, Function.identity()));

        // 其他出库单生产者
        for (PlatformAmazonFulfilledShipmentsDTO currentDTO : list) {
            currentDTO.setDownloadStatus(DownloadStatusEnum.FINISH.getCode());
            currentDTO.setHandleStatus(AmazonHandleStatusEnum.HANDLE.getCode());

            MapUtil mapUtil = getMapParam();
            UniqueDto updateDto = UniqueDto.getUniqId(currentDTO.getUniqueId());
            finishClean(mapUtil, updateDto,tableName, PlatformAmazonFulfilledShipmentsDTO.class);

            PlatformOtherOutStockDTO msg = this.convertMsg(currentDTO, detailMap.get(currentDTO.getMerchantOrderItemId()));

            String modelTaskId = dmpPullTaskService.saveOrUpdateDmpSyncTask(new DmpPullTaskEntity(msg.getPlatform(), businessType.getSourceType().getCode(), "ERP", topic, tag, msg));
            // 同步处理
            msg.setDmpSyncTaskId(modelTaskId);
            ApiResult<?> apiResult = wmsAmazonFeign.consumerOtherSoOutStock(msg);
            if (200 != apiResult.getCode()) {
                throw new ServiceException("重试其他出库单失败");
            }

        }
        return true;
    }

    /**
     * mongoDTO转mq DTO
     * @param sourceDTO 来源DTO
     * @return mq DTO
     */
    private PlatformOtherOutStockDTO convertMsg(PlatformAmazonFulfilledShipmentsDTO sourceDTO, SoMultiChannelDetailEntity detailEntity) {
        if (null == detailEntity){
            String msg = StrUtil.format("未找到对应明细：平台订单={}, 来源明细ID={}", sourceDTO.getAmazonOrderId(), sourceDTO.getAmazonOrderItemId());
            throw new ServiceException(msg);
        }
        PlatformOtherOutStockDetailDTO detailDTO = new PlatformOtherOutStockDetailDTO(detailEntity.getSkuId(), detailEntity.getSkuNo(), Integer.parseInt(sourceDTO.getQuantityShipped()), "", sourceDTO.getUniqueId());

        return new PlatformOtherOutStockDTO(
                sourceDTO.getAmazonOrderId(),
                sourceDTO.getShopId(),
                sourceDTO.getShipmentDateLocale(),
                Collections.singletonList(detailDTO),
                sourceDTO.getUniqueId(),
                PlatformDictEnum.AMAZON.getCode(),
                sourceDTO.getAmazonOrderId()
        );
    }


    private <T extends CleanBaseDTO> void finishClean(MapUtil mapUtil, UniqueDto updateDto, String tableName, Class<T> clazz) {
        List<T> mongoData = mongoService.findMongoData(updateDto, 0, 0, tableName, clazz);
        if(CollectionUtil.isEmpty(mongoData)){
            log.warn("mongo暂未写入数据, 请稍后重试");
            throw new RuntimeException("mongo暂未写入数据, 请稍后重试");
        }
        if(CleanStatusEnum.CLEANED.getCode().equals(mongoData.get(0).getIsClean())){
            return;
        }
        mongoService.updateMongoData(updateDto, mapUtil, tableName, clazz);
    }

    private static MapUtil getMapParam() {
        CleanBaseDTO updateParam = new CleanBaseDTO();
        updateParam.setIsClean(CleanStatusEnum.CLEANED.getCode());
        updateParam.setLastPushTime(LocalDateUtil.formatTime(LocalDateTime.now(), DateUtil.fmt));
        return JSONObject.parseObject(JSONObject.toJSONString(updateParam), MapUtil.class);
    }

    /**
     * 查询待处理的出库信息
     */
    private List<PlatformAmazonFulfilledShipmentsDTO> getPlatformAmazonFulfilledShipmentsDTOS(DmpPullSoOutStockDTO dto, SoB2cErrorEntity b2cError, String tableName) {
        Query query = new Query();
        if (b2cError.getParamJson().startsWith("{")){
            // json
            PlatformSoOutStockDTO sourceDTO = JSONUtil.toBean(b2cError.getParamJson(), PlatformSoOutStockDTO.class);
            if (null == sourceDTO){
                throw new ServiceException("未找到出库信息内容");
            }
            List<String> shipmentItemIds = sourceDTO.getDetailList().stream()
                    .map(PlatformSoOutStockDetailDTO::getPlatformDetailId)
                    .collect(Collectors.toList());
            query.addCriteria(
                    Criteria.where("amazonOrderId").is(dto.getPlatformCode())
                            .and("shipmentItemId").in(shipmentItemIds));
        } else {
            List<SoB2cDetailEntity> detailEntityList = soB2cFeign.listDetailByMainIds(Collections.singletonList(dto.getSoB2cId()));
            SoB2cDetailEntity detailEntity = detailEntityList.stream().filter(e -> e.getId().equalsIgnoreCase(b2cError.getDetailId())).findFirst().orElse(null);
            if (null == detailEntity){
                throw new ServiceException("未找到明细");
            }
            query.addCriteria(
                    Criteria.where("amazonOrderId").is(dto.getPlatformCode())
                            .and("amazonOrderItemId").is(detailEntity.getSourceDetailId())
            );
        }

        return mongoTemplate.find(query, PlatformAmazonFulfilledShipmentsDTO.class, tableName);
    }
}
