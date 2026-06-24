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
import com.erp.model.wms.enums.PackagePrintStatusEnum;
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
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * TikTok 全托管组包预报适配器。
 */
@Slf4j
@Component
public class TikTokFullyPackageForecastAdapter extends AbstractPackageForecastPlatformAdapter {

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
            return dto.getIds().stream()
                    .map(id -> BatchResultDTO.fail(id, id, e.getMessage()))
                    .collect(Collectors.toList());
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
        entity.setPrintStatus(PackagePrintStatusEnum.ALREADY.getCode());
        updateForecastOrThrow(entity);
        return base64;
    }

    @Override
    public List<BatchResultDTO> cancel(List<String> ids) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
        for (String id : ids) {
            PackageForecastEntity entity = null;
            try {
                entity = getForecastOrThrow(id);
                if (isCanceled(entity)) {
                    resultDTOS.add(BatchResultDTO.success(entity.getId(), entity.getCode(), "已取消上传"));
                    continue;
                }
                validateUploaded(entity);
                tikTokFullyCancel(entity);
                resetAfterCancel(entity);
                updateForecastOrThrow(entity);
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

    public void tikTokFullyCancel(PackageForecastEntity entity) {
        if (StringUtils.isBlank(entity.getHandoverNo())) {
            return;
        }
        List<PackageForecastDetailEntity> detailEntityList = packageForecastDetailService.listDbByMainId(entity.getId());
        List<String> soIds = detailEntityList.stream().map(PackageForecastDetailEntity::getSoId).distinct().collect(Collectors.toList());
        List<SoB2cEntity> soB2cEntityList = soB2cFeign.listByIds(soIds);
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
        List<PackageForecastEntity> sameCodeList = packageForecastMapper.selectList(
                Wrappers.<PackageForecastEntity>lambdaQuery()
                        .eq(PackageForecastEntity::getHandoverNo, entity.getHandoverNo())
                        .ne(PackageForecastEntity::getId, entity.getId())
        );
        if (CollectionUtils.isNotEmpty(sameCodeList)) {
            for (PackageForecastEntity packageForecastEntity : sameCodeList) {
                resetAfterCancel(packageForecastEntity);
            }
            updateForecastBatchOrThrow(sameCodeList);
        }
    }

    private List<BatchResultDTO> doUpload(PackageForecastDTO.UploadDTO dto) {
        List<PackageForecastDetailEntity> detailEntityList = packageForecastDetailService.listDbByMainIds(dto.getIds());
        if (CollectionUtils.isEmpty(detailEntityList)) {
            throw new ServiceException(ApiError.BILL_NOT_EXIST_WITH_TYPE, "组包预报单明细");
        }
        List<String> soIds = detailEntityList.stream().map(PackageForecastDetailEntity::getSoId).distinct().collect(Collectors.toList());
        List<SoB2cLogisticsEntity> soB2cLogisticsEntityList = soB2cFeign.listSoB2cLogisticsByMainIdList(soIds);
        List<String> deliveryCodes = soB2cLogisticsEntityList.stream().map(SoB2cLogisticsEntity::getCode).collect(Collectors.toList());
        List<PackageForecastEntity> packageForecastEntityList = packageForecastMapper.selectBatchIds(dto.getIds());
        List<SoB2cEntity> soB2cEntityList = soB2cFeign.listByIds(soIds);
        if (CollectionUtils.isEmpty(packageForecastEntityList)) {
            throw new ServiceException(ApiError.BILL_NOT_EXIST_WITH_TYPE, "组包预报单");
        }
        packageForecastEntityList.forEach(this::validateUploadable);
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
            throw new ServiceException("TikTok全托管不支持多店铺组包预报");
        }
        TikTokFullyShippingReq tikTokFullyShippingReq = new TikTokFullyShippingReq();
        tikTokFullyShippingReq.setDeliveryOrderCodes(deliveryCodes);
        LogisticsAddressEntity addressEntity = logisticsFeign.getLogisticsAddressById(dto.getCollectAddressId());
        if (Objects.isNull(addressEntity)) {
            throw new ServiceException("揽收地址不存在");
        }
        String addressName = addressEntity.getName();
        tikTokFullyShippingReq.setSenderContactId(dto.getAddressId());
        validateReserveInfo(dto);
        if (PackageForecastCollectModeEnum.SELF_SEND.getCode().equals(dto.getCollectMode())) {
            tikTokFullyShippingReq.setDeliveryMode("SELF_DELIVERY");
            TikTokFullyShippingReq.ReserveInfoDTO reserveInfoDTO = new TikTokFullyShippingReq.ReserveInfoDTO();
            reserveInfoDTO.setPredictedShipTime((int) dto.getDeliveryTime().atStartOfDay().toInstant(ZoneOffset.ofHours(8)).getEpochSecond());
            reserveInfoDTO.setPredictedArrivedTime((int) dto.getArrivedTime().atStartOfDay().toInstant(ZoneOffset.ofHours(8)).getEpochSecond());
            tikTokFullyShippingReq.setReserveInfo(reserveInfoDTO);
        } else {
            tikTokFullyShippingReq.setDeliveryMode("PLATFORM_DELIVERY");
            tikTokFullyShippingReq.setShippingBoxQuantity(dto.getTotalBox());
            tikTokFullyShippingReq.setTotalWeight(new TikTokFullyShippingReq.TotalWeightDTO(String.valueOf(dto.getDeliveryWeight()), "GRAM"));
            tikTokFullyShippingReq.setLogistics(new TikTokFullyShippingReq.LogisticsDTO(dto.getLogisticType(), dto.getProviderCode(), dto.getProviderName()));
            TikTokFullyShippingReq.ReserveInfoDTO reserveInfoDTO = new TikTokFullyShippingReq.ReserveInfoDTO();
            reserveInfoDTO.setPredictedPickupTime((int) dto.getCollectDate().atStartOfDay().toInstant(ZoneOffset.ofHours(8)).getEpochSecond());
            reserveInfoDTO.setPredictedPickupGe((int) dto.getStartTime().toInstant(ZoneOffset.ofHours(8)).getEpochSecond());
            reserveInfoDTO.setPredictedPickupLt((int) dto.getEndTime().toInstant(ZoneOffset.ofHours(8)).getEpochSecond());
            tikTokFullyShippingReq.setReserveInfo(reserveInfoDTO);
        }
        try {
            TikTokFullyShippingResp tikTokFullyShippingResp = tikTokFullService.shipment(shopIds.get(0), tikTokFullyShippingReq);
            if (Objects.isNull(tikTokFullyShippingResp)
                    || Objects.isNull(tikTokFullyShippingResp.getData())
                    || StringUtils.isBlank(tikTokFullyShippingResp.getData().getLogisticsOrder())) {
                throw new ServiceException(ApiError.COMMON_NOT_FOUND, "TikTok全托管预约发货返回物流单号");
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
            updateForecastBatchOrThrow(packageForecastEntityList);
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
        return packageForecastEntityList.stream()
                .map(entity -> BatchResultDTO.success(entity.getId(), entity.getCode(), "上传成功"))
                .collect(Collectors.toList());
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
        List<String> soIds = forecastDetailList.stream().map(PackageForecastDetailEntity::getSoId).distinct().collect(Collectors.toList());
        List<SoB2cEntity> soB2cEntityList = soB2cFeign.listByIds(soIds);
        List<String> shopIds = soB2cEntityList.stream().map(SoB2cEntity::getShopId).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(shopIds)) {
            throw new ServiceException("销售订单店铺未找到");
        }
        String logisticsCode = resolvePrintLogisticsCode(entity, soB2cEntityList.get(0).getShopId());
        String url = tikTokFullService.printLogistics(soB2cEntityList.get(0).getShopId(), logisticsCode);
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
}
