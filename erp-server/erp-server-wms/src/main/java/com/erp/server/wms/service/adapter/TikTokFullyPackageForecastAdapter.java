package com.erp.server.wms.service.adapter;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.enums.PlatformDictEnum;
import com.common.business.utils.PdfUtil;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.erp.model.oms.entity.SoB2cEntity;
import com.erp.model.oms.entity.SoB2cLogisticsEntity;
import com.erp.model.tms.entity.LogisticsAddressEntity;
import com.erp.model.wms.dto.PackageForecastDTO;
import com.erp.model.wms.entity.PackageForecastDetailEntity;
import com.erp.model.wms.entity.PackageForecastEntity;
import com.erp.model.wms.enums.PackageForecastCollectModeEnum;
import com.erp.model.wms.enums.PackageUploadStatusEnum;
import com.erp.rpc.tms.feign.LogisticsFeign;
import com.sdk.oms.tiktok.dto.tiktok.fully.TikTokFullyLogisticResp;
import com.sdk.oms.tiktok.dto.tiktok.fully.TikTokFullyShippingReq;
import com.sdk.oms.tiktok.dto.tiktok.fully.TikTokFullyShippingResp;
import com.sdk.oms.tiktok.service.TikTokFullService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * TikTok 全托管组包预报适配器。
 */
@Slf4j
@Component
public class TikTokFullyPackageForecastAdapter extends AbstractPackageForecastPlatformAdapter {

    private static final String DELIVERY_MODE_SELF = "SELF_DELIVERY";
    private static final String DELIVERY_MODE_PLATFORM = "PLATFORM_DELIVERY";

    @Resource
    private LogisticsFeign logisticsFeign;

    @Resource
    private TikTokFullService tikTokFullService;

    @Override
    public String platform() {
        return PlatformDictEnum.TIK_TOK_FULLY.getCode();
    }

    @Override
    public List<BatchResultDTO> upload(PackageForecastDTO.UploadDTO dto) {
        try {
            return doUpload(dto);
        } catch (Exception e) {
            log.error("TikTok全托管组包预报上传失败>>>>", e);
            return failUploadResult(dto, e);
        }
    }

    @Override
    public String print(String id) {
        PackageForecastEntity entity = getForecastOrThrow(id);
        validatePrintable(entity);
        String base64 = tikTokFullyPrint(entity);
        if (StringUtils.isBlank(base64)) {
            throw new ServiceException("打印失败");
        }
        return base64;
    }

    @Override
    public List<BatchResultDTO> cancel(List<String> ids) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
        TikTokFullyForecastContext context = buildContext(ids);
        Set<String> canceledHandoverNoSet = new HashSet<>();
        for (String id : ids) {
            PackageForecastEntity entity = null;
            try {
                entity = getForecastOrThrow(id, context);
                if (StringUtils.isNotBlank(entity.getHandoverNo()) && canceledHandoverNoSet.contains(entity.getHandoverNo())) {
                    resultDTOS.add(BatchResultDTO.success(entity.getId(), entity.getCode(), "取消上传"));
                    continue;
                }
                if (isCanceled(entity)) {
                    resultDTOS.add(BatchResultDTO.success(entity.getId(), entity.getCode(), "已取消上传"));
                    continue;
                }
                validateUploaded(entity);
                String handoverNo = entity.getHandoverNo();
                List<PackageForecastEntity> updateList = tikTokFullyCancel(entity, context);
                resetAfterCancel(entity);
                updateList.add(entity);
                try {
                    updateForecastBatchOrThrow(updateList);
                } catch (Exception updateException) {
                    log.error("TikTok全托管组包预报平台取消成功后本地更新失败, id: {}, code: {}, handoverNo: {}",
                            entity.getId(), entity.getCode(), handoverNo, updateException);
                    resultDTOS.add(BatchResultDTO.fail(entity.getId(), entity.getCode(),
                            "TikTok平台已取消，本地更新失败，请同步状态或人工处理:" + updateException.getMessage()));
                    continue;
                }
                if (StringUtils.isNotBlank(handoverNo)) {
                    canceledHandoverNoSet.add(handoverNo);
                }
                resultDTOS.add(BatchResultDTO.success(entity.getId(), entity.getCode(), "取消上传"));
            } catch (Exception e) {
                log.error("取消上传失败>>>>", e);
                if (Objects.nonNull(entity)) {
                    entity.setRemark("取消失败原因:" + e.getMessage());
                    try {
                        updateForecastOrThrow(entity);
                    } catch (Exception updateException) {
                        log.error("TikTok全托管组包预报取消失败后更新失败原因失败, id: {}, code: {}", entity.getId(), entity.getCode(), updateException);
                    }
                    resultDTOS.add(BatchResultDTO.fail(entity.getId(), entity.getCode(), "取消上传失败:" + e.getMessage()));
                } else {
                    resultDTOS.add(BatchResultDTO.fail(id, id, e.getMessage()));
                }
            }
        }
        return resultDTOS;
    }

    @Override
    public void syncTrackingStatus(PackageForecastEntity entity) {
        // TikTok 全托管状态同步保留在原有 syncPackageForecastInfo 任务中。
    }

    private List<PackageForecastEntity> tikTokFullyCancel(PackageForecastEntity entity, TikTokFullyForecastContext context) {
        if (StringUtils.isBlank(entity.getHandoverNo())) {
            return new ArrayList<>();
        }
        List<PackageForecastDetailEntity> detailEntityList = context.getDetailMap().get(entity.getId());
        if (CollectionUtils.isEmpty(detailEntityList)) {
            throw new ServiceException(ApiError.BILL_NOT_EXIST_WITH_TYPE, "组包预报单明细");
        }
        List<String> soIds = detailEntityList.stream()
                .map(PackageForecastDetailEntity::getSoId)
                .filter(StringUtils::isNotBlank)
                .distinct()
                .collect(Collectors.toList());
        List<SoB2cEntity> soB2cEntityList = soIds.stream()
                .map(id -> context.getSoMap().get(id))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(soB2cEntityList)) {
            throw new ServiceException("销售订单未找到");
        }
        List<String> shopIds = soB2cEntityList.stream()
                .map(SoB2cEntity::getShopId)
                .filter(StringUtils::isNotBlank)
                .distinct()
                .collect(Collectors.toList());
        if (PackageForecastCollectModeEnum.SELF_SEND.getCode().equals(entity.getCollectMode())) {
            throw new ServiceException("商家自配方式不支持取消组包");
        }
        if (CollectionUtils.isEmpty(shopIds)) {
            throw new ServiceException("销售订单店铺未找到");
        }
        if (shopIds.size() > 1) {
            throw new ServiceException("TikTok不支持多店铺取消组包");
        }
        tikTokFullService.cancelLogistics(shopIds.get(0), entity.getHandoverNo());
        List<PackageForecastEntity> sameCodeList = CollectionUtils.emptyIfNull(context.getSameHandoverMap().get(entity.getHandoverNo()))
                .stream()
                .filter(packageForecastEntity -> !entity.getId().equals(packageForecastEntity.getId()))
                .collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(sameCodeList)) {
            for (PackageForecastEntity packageForecastEntity : sameCodeList) {
                resetAfterCancel(packageForecastEntity);
            }
            return sameCodeList;
        }
        return new ArrayList<>();
    }

    private List<BatchResultDTO> doUpload(PackageForecastDTO.UploadDTO dto) {
        List<PackageForecastDetailEntity> detailEntityList = packageForecastDetailService.listDbByMainIds(dto.getIds());
        if (CollectionUtils.isEmpty(detailEntityList)) {
            throw new ServiceException(ApiError.BILL_NOT_EXIST_WITH_TYPE, "组包预报单明细");
        }
        List<String> soIds = detailEntityList.stream()
                .map(PackageForecastDetailEntity::getSoId)
                .filter(StringUtils::isNotBlank)
                .distinct()
                .collect(Collectors.toList());
        List<SoB2cLogisticsEntity> soB2cLogisticsEntityList = soB2cFeign.listSoB2cLogisticsByMainIdList(soIds);
        if (CollectionUtils.isEmpty(soB2cLogisticsEntityList)) {
            throw new ServiceException("销售订单物流信息未找到");
        }
        List<String> deliveryCodes = soB2cLogisticsEntityList.stream().map(SoB2cLogisticsEntity::getCode).collect(Collectors.toList());
        List<PackageForecastEntity> packageForecastEntityList = packageForecastMapper.selectBatchIds(dto.getIds());
        List<SoB2cEntity> soB2cEntityList = soB2cFeign.listByIds(soIds);
        if (CollectionUtils.isEmpty(packageForecastEntityList)) {
            throw new ServiceException(ApiError.BILL_NOT_EXIST_WITH_TYPE, "组包预报单");
        }
        packageForecastEntityList.forEach(this::validateUploadable);
        List<BatchResultDTO> persistedPlatformResult = buildRetryWithPlatformIdentifierResult(packageForecastEntityList);
        if (CollectionUtils.isNotEmpty(persistedPlatformResult)) {
            return persistedPlatformResult;
        }
        if (CollectionUtils.isEmpty(soB2cEntityList)) {
            throw new ServiceException("销售订单未找到");
        }
        validateOrderPlatform(soB2cEntityList);
        List<String> shopIds = soB2cEntityList.stream()
                .map(SoB2cEntity::getShopId)
                .filter(StringUtils::isNotBlank)
                .distinct()
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(shopIds)) {
            throw new ServiceException("销售订单店铺未找到");
        }
        if (shopIds.size() > 1) {
            throw new ServiceException("TikTok全托管不支持多店铺组包预报");
        }
        TikTokFullyShippingReq tikTokFullyShippingReq = new TikTokFullyShippingReq();
        tikTokFullyShippingReq.setDeliveryOrderCodes(deliveryCodes);
        LogisticsAddressEntity addressEntity = logisticsFeign.getLogisticsAddressById(dto.getCollectAddressId());
        if (Objects.isNull(addressEntity)) {
            throw new ServiceException("揽收地址不存在");
        }
        String addressName = addressEntity.getName();
        String senderContactId = StringUtils.defaultIfBlank(dto.getAddressId(), addressEntity.getAddressId());
        if (StringUtils.isBlank(senderContactId)) {
            throw new ServiceException("TikTok全托管寄件联系人不能为空");
        }
        tikTokFullyShippingReq.setSenderContactId(senderContactId);
        validateReserveInfo(dto);
        if (PackageForecastCollectModeEnum.SELF_SEND.getCode().equals(dto.getCollectMode())) {
            tikTokFullyShippingReq.setDeliveryMode(DELIVERY_MODE_SELF);
            TikTokFullyShippingReq.ReserveInfoDTO reserveInfoDTO = new TikTokFullyShippingReq.ReserveInfoDTO();
            reserveInfoDTO.setPredictedShipTime((int) dto.getDeliveryTime().atStartOfDay().toInstant(ZoneOffset.ofHours(8)).getEpochSecond());
            reserveInfoDTO.setPredictedArrivedTime((int) dto.getArrivedTime().atStartOfDay().toInstant(ZoneOffset.ofHours(8)).getEpochSecond());
            tikTokFullyShippingReq.setReserveInfo(reserveInfoDTO);
        } else {
            tikTokFullyShippingReq.setDeliveryMode(DELIVERY_MODE_PLATFORM);
            tikTokFullyShippingReq.setShippingBoxQuantity(dto.getTotalBox());
            tikTokFullyShippingReq.setTotalWeight(new TikTokFullyShippingReq.TotalWeightDTO(String.valueOf(dto.getDeliveryWeight()), "GRAM"));
            tikTokFullyShippingReq.setLogistics(new TikTokFullyShippingReq.LogisticsDTO(dto.getLogisticType(), dto.getProviderCode(), dto.getProviderName()));
            TikTokFullyShippingReq.ReserveInfoDTO reserveInfoDTO = new TikTokFullyShippingReq.ReserveInfoDTO();
            reserveInfoDTO.setPredictedPickupTime((int) dto.getCollectDate().atStartOfDay().toInstant(ZoneOffset.ofHours(8)).getEpochSecond());
            reserveInfoDTO.setPredictedPickupGe((int) dto.getStartTime().toInstant(ZoneOffset.ofHours(8)).getEpochSecond());
            reserveInfoDTO.setPredictedPickupLt((int) dto.getEndTime().toInstant(ZoneOffset.ofHours(8)).getEpochSecond());
            tikTokFullyShippingReq.setReserveInfo(reserveInfoDTO);
        }
        TikTokFullyShippingResp tikTokFullyShippingResp;
        try {
            tikTokFullyShippingResp = tikTokFullService.shipment(shopIds.get(0), tikTokFullyShippingReq);
            if (Objects.isNull(tikTokFullyShippingResp)
                    || Objects.isNull(tikTokFullyShippingResp.getData())
                    || StringUtils.isBlank(tikTokFullyShippingResp.getData().getLogisticsOrder())) {
                throw new ServiceException(ApiError.COMMON_NOT_FOUND, "TikTok全托管预约发货返回物流单号");
            }
        } catch (Exception e) {
            packageForecastEntityList.forEach(v -> {
                v.setUploadStatus(PackageUploadStatusEnum.UPLOAD_FAILURE.getCode());
                v.setRemark(e.getMessage());
            });
            try {
                updateForecastBatchOrThrow(packageForecastEntityList);
            } catch (Exception updateException) {
                log.error("TikTok全托管组包预报上传失败后更新失败状态失败, ids: {}", dto.getIds(), updateException);
            }
            return packageForecastEntityList.stream()
                    .map(entity -> BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage()))
                    .collect(Collectors.toList());
        }
        String logisticsOrder = tikTokFullyShippingResp.getData().getLogisticsOrder();
        packageForecastEntityList.forEach(v -> {
            v.setHandoverNo(logisticsOrder);
            v.setPlatformNo(buildPlatformNo(v.getHandoverNo(), v.getPlatformPackageNo()));
            v.setUploadStatus(PackageUploadStatusEnum.UPLOAD_SUCCESS.getCode());
            v.setCollectMode(dto.getCollectMode());
            v.setCollectAddressId(dto.getCollectAddressId());
            v.setCollectAddress(addressName);
            v.setRemark("");
        });
        try {
            updateForecastBatchOrThrow(packageForecastEntityList);
        } catch (Exception e) {
            log.error("TikTok全托管组包预报平台预约成功后本地更新失败, ids: {}, logisticsOrder: {}",
                    dto.getIds(), logisticsOrder, e);
            return packageForecastEntityList.stream()
                    .map(entity -> BatchResultDTO.fail(entity.getId(), entity.getCode(),
                            "TikTok平台已预约成功，本地更新失败，请人工处理，物流单号:" + logisticsOrder))
                    .collect(Collectors.toList());
        }
        return packageForecastEntityList.stream()
                .map(entity -> BatchResultDTO.success(entity.getId(), entity.getCode(), "上传成功"))
                .collect(Collectors.toList());
    }

    private TikTokFullyForecastContext buildContext(List<String> ids) {
        TikTokFullyForecastContext context = new TikTokFullyForecastContext();
        if (CollectionUtils.isEmpty(ids)) {
            return context;
        }
        List<PackageForecastEntity> entityList = packageForecastMapper.selectBatchIds(ids);
        if (CollectionUtils.isEmpty(entityList)) {
            return context;
        }
        context.setEntityMap(entityList.stream()
                .collect(Collectors.toMap(PackageForecastEntity::getId, entity -> entity, (left, right) -> left)));
        List<PackageForecastDetailEntity> detailList = packageForecastDetailService.listDbByMainIds(ids);
        if (CollectionUtils.isNotEmpty(detailList)) {
            context.setDetailMap(detailList.stream().collect(Collectors.groupingBy(PackageForecastDetailEntity::getMainId)));
            List<String> soIds = detailList.stream()
                    .map(PackageForecastDetailEntity::getSoId)
                    .filter(StringUtils::isNotBlank)
                    .distinct()
                    .collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(soIds)) {
                List<SoB2cEntity> soList = soB2cFeign.listByIds(soIds);
                if (CollectionUtils.isNotEmpty(soList)) {
                    validateOrderPlatform(soList);
                    context.setSoMap(soList.stream()
                            .collect(Collectors.toMap(SoB2cEntity::getId, entity -> entity, (left, right) -> left)));
                }
            }
        }
        List<String> handoverNoList = entityList.stream()
                .map(PackageForecastEntity::getHandoverNo)
                .filter(StringUtils::isNotBlank)
                .distinct()
                .collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(handoverNoList)) {
            List<PackageForecastEntity> sameHandoverList = packageForecastMapper.selectList(
                    Wrappers.<PackageForecastEntity>lambdaQuery()
                            .in(PackageForecastEntity::getHandoverNo, handoverNoList)
            );
            if (CollectionUtils.isNotEmpty(sameHandoverList)) {
                context.setSameHandoverMap(sameHandoverList.stream()
                        .collect(Collectors.groupingBy(PackageForecastEntity::getHandoverNo)));
            }
        }
        return context;
    }

    private List<BatchResultDTO> buildRetryWithPlatformIdentifierResult(List<PackageForecastEntity> entityList) {
        PackageForecastEntity persistedPlatformEntity = entityList.stream()
                .filter(entity -> StringUtils.isNotBlank(entity.getHandoverNo())
                        || StringUtils.isNotBlank(entity.getPlatformPackageNo())
                        || StringUtils.isNotBlank(entity.getPlatformNo()))
                .findFirst()
                .orElse(null);
        if (Objects.isNull(persistedPlatformEntity)) {
            return Collections.emptyList();
        }
        String message = "TikTok全托管组包预报已存在平台物流信息，请先同步状态或人工处理后再重试，单号:"
                + persistedPlatformEntity.getCode();
        return entityList.stream()
                .map(entity -> BatchResultDTO.fail(entity.getId(), entity.getCode(), message))
                .collect(Collectors.toList());
    }

    private void validateOrderPlatform(List<SoB2cEntity> soList) {
        boolean hasWrongPlatform = soList.stream()
                .anyMatch(entity -> !PlatformDictEnum.TIK_TOK_FULLY.getCode().equals(entity.getDictPlatform()));
        if (hasWrongPlatform) {
            throw new ServiceException("仅TikTok全托管平台订单可操作");
        }
    }

    private PackageForecastEntity getForecastOrThrow(String id, TikTokFullyForecastContext context) {
        PackageForecastEntity entity = context.getEntityMap().get(id);
        if (Objects.isNull(entity)) {
            throw new ServiceException(ApiError.BILL_NOT_EXIST_WITH_TYPE, "组包预报单");
        }
        return entity;
    }

    private List<BatchResultDTO> failUploadResult(PackageForecastDTO.UploadDTO dto, Exception e) {
        List<PackageForecastEntity> entityList = packageForecastMapper.selectBatchIds(dto.getIds());
        Map<String, PackageForecastEntity> entityMap = entityList.stream()
                .collect(Collectors.toMap(PackageForecastEntity::getId, entity -> entity, (left, right) -> left, LinkedHashMap::new));
        if (CollectionUtils.isNotEmpty(entityList)) {
            entityList.forEach(entity -> {
                entity.setUploadStatus(PackageUploadStatusEnum.UPLOAD_FAILURE.getCode());
                entity.setRemark(e.getMessage());
            });
            try {
                updateForecastBatchOrThrow(entityList);
            } catch (Exception updateException) {
                log.error("TikTok全托管组包预报上传失败后更新失败状态失败, ids: {}", dto.getIds(), updateException);
            }
        }
        List<BatchResultDTO> resultList = new ArrayList<>(dto.getIds().size());
        for (String id : dto.getIds()) {
            PackageForecastEntity entity = entityMap.get(id);
            if (Objects.isNull(entity)) {
                resultList.add(BatchResultDTO.fail(id, id, e.getMessage()));
            } else {
                resultList.add(BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage()));
            }
        }
        return resultList;
    }

    private void validateReserveInfo(PackageForecastDTO.UploadDTO dto) {
        if (PackageForecastCollectModeEnum.SELF_SEND.getCode().equals(dto.getCollectMode())) {
            if (Objects.isNull(dto.getDeliveryTime()) || Objects.isNull(dto.getArrivedTime())) {
                throw new ServiceException(ApiError.COMMON_PARAM_TIME_REQUIRED, "商家自配送货时间和到仓时间");
            }
            return;
        }
        if (Objects.isNull(dto.getCollectDate()) || Objects.isNull(dto.getStartTime()) || Objects.isNull(dto.getEndTime())) {
            throw new ServiceException(ApiError.COMMON_PARAM_TIME_REQUIRED, "平台揽收日期和揽收时间");
        }
    }

    private String tikTokFullyPrint(PackageForecastEntity entity) {
        List<PackageForecastDetailEntity> forecastDetailList = packageForecastDetailService.listDbByMainId(entity.getId());
        if (CollectionUtils.isEmpty(forecastDetailList)) {
            throw new ServiceException(ApiError.BILL_NOT_EXIST_WITH_TYPE, "组包预报单明细");
        }
        List<String> soIds = forecastDetailList.stream().map(PackageForecastDetailEntity::getSoId).distinct().collect(Collectors.toList());
        List<SoB2cEntity> soB2cEntityList = soB2cFeign.listByIds(soIds);
        if (CollectionUtils.isEmpty(soB2cEntityList)) {
            throw new ServiceException("销售订单未找到");
        }
        List<String> shopIds = soB2cEntityList.stream()
                .map(SoB2cEntity::getShopId)
                .filter(StringUtils::isNotBlank)
                .distinct()
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(shopIds)) {
            throw new ServiceException("销售订单店铺未找到");
        }
        if (shopIds.size() > 1) {
            throw new ServiceException("TikTok全托管不支持多店铺打印面单");
        }
        String shopId = shopIds.get(0);
        String logisticsCode = resolvePrintLogisticsCode(entity, shopId);
        String url = tikTokFullService.printLogistics(shopId, logisticsCode);
        try {
            String base64 = PdfUtil.convertPdfUrlToBase64(url, true);
            return "data:application/pdf;base64," + base64;
        } catch (IOException e) {
            throw new ServiceException(e.getMessage());
        }
    }

    private String resolvePrintLogisticsCode(PackageForecastEntity entity, String shopId) {
        if (StringUtils.isNotBlank(entity.getPlatformPackageNo())) {
            return entity.getPlatformPackageNo();
        }
        if (StringUtils.isBlank(entity.getHandoverNo())) {
            throw new ServiceException(ApiError.COMMON_PARAM_REQUIRED, "TikTok全托管平台物流单号");
        }
        TikTokFullyLogisticResp logisticResp = tikTokFullService.queryLogistics(shopId, Collections.singletonList(entity.getHandoverNo()));
        if (Objects.isNull(logisticResp)
                || Objects.isNull(logisticResp.getData())
                || CollectionUtils.isEmpty(logisticResp.getData().getLogisticsOrders())) {
            throw new ServiceException(ApiError.COMMON_NOT_FOUND, "TikTok全托管物流子单号");
        }
        List<String> subLogisticCodeList = logisticResp.getData().getLogisticsOrders().stream()
                .filter(logisticsOrder -> Objects.equals(entity.getHandoverNo(), logisticsOrder.getCode()))
                .flatMap(logisticsOrder -> CollectionUtils.emptyIfNull(logisticsOrder.getLogisticsSubOrders()).stream())
                .map(TikTokFullyLogisticResp.DataDTO.LogisticsOrdersDTO.LogisticsSubOrdersDTO::getCode)
                .filter(StringUtils::isNotBlank)
                .distinct()
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(subLogisticCodeList)) {
            throw new ServiceException(ApiError.COMMON_NOT_FOUND, "TikTok全托管物流子单号");
        }
        String logisticsCode = String.join(",", subLogisticCodeList);
        entity.setPlatformPackageNo(logisticsCode);
        entity.setPlatformNo(buildPlatformNo(entity.getHandoverNo(), entity.getPlatformPackageNo()));
        updateForecastOrThrow(entity);
        return logisticsCode;
    }

    private String buildPlatformNo(String handoverNo, String platformPackageNo) {
        if (StringUtils.isBlank(handoverNo) && StringUtils.isBlank(platformPackageNo)) {
            return "";
        }
        return StringUtils.defaultString(handoverNo) + "/" + StringUtils.defaultString(platformPackageNo);
    }

    private static class TikTokFullyForecastContext {

        private Map<String, PackageForecastEntity> entityMap = new HashMap<>();
        private Map<String, List<PackageForecastDetailEntity>> detailMap = new HashMap<>();
        private Map<String, SoB2cEntity> soMap = new HashMap<>();
        private Map<String, List<PackageForecastEntity>> sameHandoverMap = new HashMap<>();

        public Map<String, PackageForecastEntity> getEntityMap() {
            return entityMap;
        }

        public void setEntityMap(Map<String, PackageForecastEntity> entityMap) {
            this.entityMap = entityMap;
        }

        public Map<String, List<PackageForecastDetailEntity>> getDetailMap() {
            return detailMap;
        }

        public void setDetailMap(Map<String, List<PackageForecastDetailEntity>> detailMap) {
            this.detailMap = detailMap;
        }

        public Map<String, SoB2cEntity> getSoMap() {
            return soMap;
        }

        public void setSoMap(Map<String, SoB2cEntity> soMap) {
            this.soMap = soMap;
        }

        public Map<String, List<PackageForecastEntity>> getSameHandoverMap() {
            return sameHandoverMap;
        }

        public void setSameHandoverMap(Map<String, List<PackageForecastEntity>> sameHandoverMap) {
            this.sameHandoverMap = sameHandoverMap;
        }
    }
}
