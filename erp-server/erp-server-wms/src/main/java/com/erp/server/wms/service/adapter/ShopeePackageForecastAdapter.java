package com.erp.server.wms.service.adapter;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.json.JSONUtil;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.enums.PlatformDictEnum;
import com.common.business.utils.PdfUtil;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.ValidatorUtil;
import com.erp.model.dmp.dto.CfgAppClientDTO;
import com.erp.model.dmp.entity.CfgAppClientEntity;
import com.erp.model.dmp.enums.AppClientEnum;
import com.erp.model.oms.entity.ShopInfoEntity;
import com.erp.model.oms.entity.ShopAuthEntity;
import com.erp.model.oms.entity.SoB2cEntity;
import com.erp.model.oms.enums.AuthTypeEnum;
import com.erp.model.wms.dto.PackageForecastDTO;
import com.erp.model.wms.entity.PackageForecastDetailEntity;
import com.erp.model.wms.entity.PackageForecastEntity;
import com.erp.model.wms.enums.HandoverStatusEnum;
import com.erp.model.wms.enums.PackageForecastCollectModeEnum;
import com.erp.model.wms.enums.PackagePrintStatusEnum;
import com.erp.model.wms.enums.PackageUploadStatusEnum;
import com.erp.rpc.dmp.feign.DmpTaskFeign;
import com.erp.rpc.oms.feign.ShopInfoFeign;
import com.sdk.tms.shopee.model.base.BaseRequest;
import com.sdk.tms.shopee.model.firstmile.request.BindFirstMileTrackingNumberRequest;
import com.sdk.tms.shopee.model.firstmile.request.CourierDeliveryInfo;
import com.sdk.tms.shopee.model.firstmile.request.CourierDeliveryTrackingNumberListRequest;
import com.sdk.tms.shopee.model.firstmile.request.CourierDeliveryWaybillRequest;
import com.sdk.tms.shopee.model.firstmile.request.FirstMileOrder;
import com.sdk.tms.shopee.model.firstmile.request.FirstMileTrackingNumberListRequest;
import com.sdk.tms.shopee.model.firstmile.request.FirstMileWaybillRequest;
import com.sdk.tms.shopee.model.firstmile.request.GenerateAndBindFirstMileTrackingNumberRequest;
import com.sdk.tms.shopee.model.firstmile.request.GenerateFirstMileTrackingNumberRequest;
import com.sdk.tms.shopee.model.firstmile.request.UnbindFirstMileTrackingNumberAllRequest;
import com.sdk.tms.shopee.model.firstmile.response.BindFirstMileTrackingNumberOrder;
import com.sdk.tms.shopee.model.firstmile.response.BindFirstMileTrackingNumberResponse;
import com.sdk.tms.shopee.model.firstmile.response.CourierDeliveryChannelResponse;
import com.sdk.tms.shopee.model.firstmile.response.CourierDeliveryBindingInfo;
import com.sdk.tms.shopee.model.firstmile.response.CourierDeliveryTrackingNumberListResponse;
import com.sdk.tms.shopee.model.firstmile.response.CourierDeliveryWaybill;
import com.sdk.tms.shopee.model.firstmile.response.CourierDeliveryWaybillResponse;
import com.sdk.tms.shopee.model.firstmile.response.CourierLogisticsChannel;
import com.sdk.tms.shopee.model.firstmile.response.CourierService;
import com.sdk.tms.shopee.model.firstmile.response.FirstMileBindingFail;
import com.sdk.tms.shopee.model.firstmile.response.FirstMileBindingOrder;
import com.sdk.tms.shopee.model.firstmile.response.FirstMileChannel;
import com.sdk.tms.shopee.model.firstmile.response.FirstMileChannelListResponse;
import com.sdk.tms.shopee.model.firstmile.response.FirstMileTrackingNumber;
import com.sdk.tms.shopee.model.firstmile.response.FirstMileTrackingNumberListResponse;
import com.sdk.tms.shopee.model.firstmile.response.GenerateAndBindFirstMileTrackingNumberResponse;
import com.sdk.tms.shopee.model.firstmile.response.GenerateFirstMileTrackingNumberResponse;
import com.sdk.tms.shopee.model.firstmile.response.TransitWarehouse;
import com.sdk.tms.shopee.model.firstmile.response.TransitWarehouseListResponse;
import com.sdk.tms.shopee.model.firstmile.response.UnbindFirstMileTrackingNumberAllResponse;
import com.sdk.tms.shopee.model.firstmile.response.UnbindFirstMileTrackingNumberOrder;
import com.sdk.tms.shopee.model.logistics.response.ShopeeAddress;
import com.sdk.tms.shopee.model.logistics.response.ShopeeAddressListResponse;
import com.sdk.tms.shopee.model.merchant.request.MerchantPrepaidAccountRequest;
import com.sdk.tms.shopee.model.merchant.response.MerchantPrepaidAccount;
import com.sdk.tms.shopee.model.merchant.response.MerchantPrepaidAccountListResponse;
import com.sdk.tms.shopee.service.ShopeeLogisticsService;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Shopee first-mile 组包预报适配器。
 */
@Slf4j
@Component
public class ShopeePackageForecastAdapter extends AbstractPackageForecastPlatformAdapter {

    private static final String REGION_CN = "CN";
    private static final String PDF_PREFIX = "data:application/pdf;base64,";
    private static final String PAYMENT_PREPAID = "prepaid";
    private static final String PAYMENT_MONTHLY = "monthly";
    private static final String PAYMENT_MONTHLY_CN = "快递账号月结";
    private static final Long SHOPEE_PREPAID_LOGISTICS_PRODUCT_ID = 1010004L;
    private static final String FIRST_MILE_PACKAGE_HAS_NOT_BIND = "firstmile.package_has_not_bind";
    private static final String SHOPEE_PLATFORM_STATUS_NOT_AVAILABLE = "NOT_AVAILABLE";
    private static final String SHOPEE_PLATFORM_STATUS_DELIVERED = "DELIVERED";
    private static final int SHOPEE_TRACKING_PAGE_SIZE = 50;
    private static final long SHOPEE_TRACKING_QUERY_PADDING_DAYS = 1L;

    @Resource
    private DmpTaskFeign dmpTaskFeign;

    @Resource
    private ShopInfoFeign shopInfoFeign;

    @Resource
    private ShopeeLogisticsService shopeeLogisticsService;

    @Override
    public String platform() {
        return PlatformDictEnum.SHOPEE.getCode();
    }

    @Override
    public List<BatchResultDTO> upload(PackageForecastDTO.UploadDTO dto) {
        // Shopee 上传按整批平台接口提交；平台调用后异常不直接批量写 UPLOAD_FAILURE，避免覆盖平台侧可能已成功的状态。
        ShopeeForecastContext context = buildContext(dto.getIds());
        validateUploadEntities(context);
        if (PackageForecastCollectModeEnum.SHOPEE_COURIER_DELIVERY.getCode().equals(dto.getCollectMode())) {
            return uploadCourierDelivery(dto, context);
        }
        return uploadFirstMile(dto, context);
    }

    public PackageForecastDTO.ShopeeCourierDeliveryOptionsDTO courierDeliveryOptions(PackageForecastDTO.ShopeeOptionParamDTO dto) {
        ShopeeForecastContext context = buildBaseContext(dto.getIds());
        BaseRequest baseRequest = buildBaseRequest(context.getShopId());
        String region = defaultRegion(dto.getRegion());
        TransitWarehouseListResponse warehouseResp = shopeeLogisticsService.getTransitWarehouseList(baseRequest, region,
                PackageForecastCollectModeEnum.SHOPEE_COURIER_DELIVERY.getCode());
        ShopeeAddressListResponse addressResp = shopeeLogisticsService.getAddressList(baseRequest);
        CourierDeliveryChannelResponse channelResp = shopeeLogisticsService.getCourierDeliveryChannelList(baseRequest, region);

        PackageForecastDTO.ShopeeCourierDeliveryOptionsDTO result = new PackageForecastDTO.ShopeeCourierDeliveryOptionsDTO();
        result.setWarehouseList(toWarehouseDTOList(warehouseResp));
        result.setAddressList(toAddressDTOList(addressResp));
        result.setLogisticsChannelList(toCourierChannelDTOList(channelResp));
        result.setPrepaidAccountList(listPrepaidAccount(context.getShopId(), baseRequest));
        return result;
    }

    public List<PackageForecastDTO.ShopeeFirstMileChannelDTO> firstMileChannelList(PackageForecastDTO.ShopeeOptionParamDTO dto) {
        ShopeeForecastContext context = buildBaseContext(dto.getIds());
        FirstMileChannelListResponse response = shopeeLogisticsService.getFirstMileChannelList(buildBaseRequest(context.getShopId()), defaultRegion(dto.getRegion()));
        if (Objects.isNull(response) || CollectionUtils.isEmpty(response.getLogisticsChannelList())) {
            return Collections.emptyList();
        }
        return response.getLogisticsChannelList().stream()
                .filter(item -> StringUtils.isBlank(dto.getCollectMode()) || dto.getCollectMode().equals(item.getShipmentMethod()))
                .map(this::toFirstMileChannelDTO)
                .collect(Collectors.toList());
    }

    public List<PackageForecastDTO.ShopeeTrackingNumberDTO> trackingNumberList(PackageForecastDTO.ShopeeOptionParamDTO dto) {
        ShopeeForecastContext context = buildBaseContext(dto.getIds());
        LocalDate declareDate = Objects.nonNull(dto.getDeclareDate()) ? dto.getDeclareDate() : LocalDate.now();
        BaseRequest baseRequest = buildBaseRequest(context.getShopId());
        List<FirstMileTrackingNumber> trackingNumberList = new ArrayList<>();
        String cursor = null;
        do {
            FirstMileTrackingNumberListRequest request = FirstMileTrackingNumberListRequest.builder()
                    .fromDate(declareDate.toString())
                    .toDate(declareDate.toString())
                    .pageSize(SHOPEE_TRACKING_PAGE_SIZE)
                    .cursor(cursor)
                    .build();
            FirstMileTrackingNumberListResponse response = shopeeLogisticsService.getTrackNumberList(baseRequest, request);
            if (Objects.isNull(response)) {
                break;
            }
            trackingNumberList.addAll(CollectionUtils.emptyIfNull(response.getFirstMileTrackingNumberList()));
            cursor = response.getNextCursor();
            if (!Boolean.TRUE.equals(response.getMore())) {
                cursor = null;
            }
        } while (StringUtils.isNotBlank(cursor));
        return toTrackingNumberDTOList(trackingNumberList);
    }

    @Override
    public String print(String id) {
        PackageForecastEntity entity = packageForecastMapper.selectById(id);
        if (Objects.isNull(entity)) {
            throw new ServiceException(ApiError.BILL_NOT_EXIST_WITH_TYPE, "组包预报单");
        }
        if (!PackageUploadStatusEnum.UPLOAD_SUCCESS.getCode().equals(entity.getUploadStatus())) {
            throw new ServiceException("仅上传成功后可操作");
        }
        ShopeeForecastContext context = buildContext(Collections.singletonList(id));
        String base64;
        if (PackageForecastCollectModeEnum.SHOPEE_COURIER_DELIVERY.getCode().equals(entity.getCollectMode())) {
            base64 = printCourierDelivery(entity, context.getShopId());
        } else {
            base64 = printFirstMile(entity, context.getShopId());
        }
        if (StringUtils.isBlank(base64)) {
            throw new ServiceException("打印失败");
        }
        return withPdfPrefix(base64);
    }

    @Override
    public List<BatchResultDTO> cancel(List<String> ids) {
        ShopeeForecastContext context = buildContext(ids);
        validateCancelEntities(context);
        boolean hasCourier = context.getEntityList().stream()
                .anyMatch(entity -> PackageForecastCollectModeEnum.SHOPEE_COURIER_DELIVERY.getCode().equals(entity.getCollectMode()));
        boolean hasOther = context.getEntityList().stream()
                .anyMatch(entity -> !PackageForecastCollectModeEnum.SHOPEE_COURIER_DELIVERY.getCode().equals(entity.getCollectMode()));
        if (hasCourier && hasOther) {
            throw new ServiceException("虾皮平台组包不支持多种揽收模式同时取消组包下单");
        }
        try {
            Map<String, String> failReasonMap;
            if (hasCourier) {
                failReasonMap = cancelCourierDelivery(context);
            } else {
                failReasonMap = cancelFirstMile(context);
            }
            List<BatchResultDTO> resultList = new ArrayList<>(context.getEntityList().size());
            for (PackageForecastEntity entity : context.getEntityList()) {
                List<String> orderKeys = context.getForecastOrderKeyMap().get(entity.getId());
                if (CollectionUtils.isEmpty(orderKeys)) {
                    entity.setRemark("取消失败原因:组包预报单未匹配到Shopee订单");
                    resultList.add(BatchResultDTO.fail(entity.getId(), entity.getCode(), entity.getRemark()));
                    continue;
                }
                List<String> failureReasons = CollectionUtils.emptyIfNull(orderKeys).stream()
                        .filter(failReasonMap::containsKey)
                        .map(failReasonMap::get)
                        .collect(Collectors.toList());
                if (CollectionUtils.isEmpty(failureReasons)) {
                    resetAfterCancel(entity);
                    resultList.add(BatchResultDTO.success(entity.getId(), entity.getCode(), "取消上传"));
                } else {
                    entity.setRemark("取消失败原因:" + String.join(";", failureReasons));
                    resultList.add(BatchResultDTO.fail(entity.getId(), entity.getCode(), entity.getRemark()));
                }
            }
            try {
                updateEntities(context.getEntityList());
            } catch (Exception updateException) {
                log.error("虾皮组包预报平台取消结果本地更新失败, ids: {}", ids, updateException);
                return context.getEntityList().stream()
                        .map(entity -> BatchResultDTO.fail(entity.getId(), entity.getCode(),
                                "Shopee平台取消结果本地更新失败，请同步状态或人工处理"))
                        .collect(Collectors.toList());
            }
            return resultList;
        } catch (Exception e) {
            log.error("虾皮组包预报取消上传失败, ids: {}", ids, e);
            String failureMessage = userFailureMessage("取消上传", e);
            context.getEntityList().forEach(entity -> {
                entity.setRemark(failureMessage);
            });
            try {
                updateEntities(context.getEntityList());
            } catch (Exception updateException) {
                log.error("虾皮组包预报取消失败后更新失败原因失败, ids: {}", ids, updateException);
            }
            return context.getEntityList().stream()
                    .map(entity -> BatchResultDTO.fail(entity.getId(), entity.getCode(), failureMessage))
                    .collect(Collectors.toList());
        }
    }

    @Override
    public void syncTrackingStatus(PackageForecastEntity entity) {
        if (Objects.isNull(entity)) {
            return;
        }
        Optional<ShopeeTrackingSyncKey> keyOptional = buildTrackingSyncKey(entity);
        if (!keyOptional.isPresent()) {
            return;
        }
        ShopeeTrackingSyncKey key = keyOptional.get();
        if (PackageForecastCollectModeEnum.SHOPEE_COURIER_DELIVERY.getCode().equals(key.getCollectMode())) {
            syncCourierDeliveryTrackingStatus(Collections.singletonList(entity), key);
            return;
        }
        syncFirstMileTrackingStatus(Collections.singletonList(entity), key);
    }

    @Override
    public void syncTrackingStatus(List<PackageForecastEntity> entityList,
                                   BiConsumer<PackageForecastEntity, Exception> errorHandler) {
        if (CollectionUtils.isEmpty(entityList)) {
            return;
        }
        Map<String, String> trackingShopIdMap = buildTrackingShopIdMap(entityList);
        Map<ShopeeTrackingSyncKey, List<PackageForecastEntity>> groupMap = new LinkedHashMap<>();
        for (PackageForecastEntity entity : entityList) {
            if (Objects.isNull(entity)) {
                continue;
            }
            try {
                Optional<ShopeeTrackingSyncKey> keyOptional = buildTrackingSyncKey(entity, trackingShopIdMap);
                if (!keyOptional.isPresent()) {
                    continue;
                }
                groupMap.computeIfAbsent(keyOptional.get(), key -> new ArrayList<>()).add(entity);
            } catch (Exception e) {
                errorHandler.accept(entity, e);
            }
        }
        for (Map.Entry<ShopeeTrackingSyncKey, List<PackageForecastEntity>> entry : groupMap.entrySet()) {
            try {
                ShopeeTrackingSyncKey key = entry.getKey();
                if (PackageForecastCollectModeEnum.SHOPEE_COURIER_DELIVERY.getCode().equals(key.getCollectMode())) {
                    syncCourierDeliveryTrackingStatus(entry.getValue(), key);
                } else {
                    syncFirstMileTrackingStatus(entry.getValue(), key);
                }
            } catch (Exception e) {
                entry.getValue().forEach(entity -> errorHandler.accept(entity, e));
            }
        }
    }

    @Override
    public List<PackageForecastEntity> listSyncTrackingStatus(DateTime dateTime) {
        return packageForecastMapper.getShopeeHandoverList(dateTime, PlatformDictEnum.SHOPEE.getCode(),
                Arrays.asList(
                        HandoverStatusEnum.SHOPEE_DELIVERED.getCode(),
                        HandoverStatusEnum.CANCELED_2.getCode()));
    }

    private List<BatchResultDTO> uploadCourierDelivery(PackageForecastDTO.UploadDTO dto, ShopeeForecastContext context) {
        validateCourierDeliveryDTO(dto);
        rejectRetryWithPlatformIdentifiers(context);
        GenerateAndBindFirstMileTrackingNumberRequest request = GenerateAndBindFirstMileTrackingNumberRequest.builder()
                .shipmentMethod(PackageForecastCollectModeEnum.SHOPEE_COURIER_DELIVERY.getCode())
                .region(defaultRegion(dto.getRegion()))
                .orderList(context.getOrderList())
                .courierDeliveryInfo(CourierDeliveryInfo.builder()
                        .addressId(parseLong(dto.getAddressId(), "寄件地址"))
                        .warehouseId(dto.getWarehouseId())
                        .logisticsProductId(dto.getLogisticsProductId())
                        .courierServiceId(dto.getCourierServiceId())
                        .prepaidAccountId(dto.getPrepaidAccountId())
                        .build())
                .build();
        ValidatorUtil.validateEntity(request);
        GenerateAndBindFirstMileTrackingNumberResponse response;
        try {
            response = shopeeLogisticsService.generateAndBindFirstMileTrackingNumber(buildBaseRequest(context.getShopId()), request);
        } catch (ServiceException e) {
            if (isPrepaidAccountRequiredError(e.getMessage())) {
                throw new ServiceException("当前收货仓/快递服务需要月结账号，请选择月结账号，或更换非月结快递服务/收货仓");
            }
            throw e;
        }
        if (Objects.isNull(response)) {
            throw new ServiceException("Shopee组包上传响应为空");
        }

        Set<String> successKeys = CollectionUtils.emptyIfNull(response.getSuccessList()).stream()
                .map(item -> orderKey(item.getOrderSn(), item.getPackageNumber()))
                .collect(Collectors.toSet());
        Map<String, String> failReasonMap = CollectionUtils.emptyIfNull(response.getFailList()).stream()
                .collect(Collectors.toMap(item -> orderKey(item.getOrderSn(), item.getPackageNumber()),
                        this::buildFailReason, (left, right) -> left));
        return updateUploadResult(context, successKeys, failReasonMap, entity -> {
            entity.setCollectMode(dto.getCollectMode());
            entity.setCollectAddressId(dto.getAddressId());
            entity.setPlatformPackageNo(response.getBindingId());
            entity.setTransportNo(response.getFirstMileTrackingNumber());
            entity.setLogisticsSupplierName(StringUtils.defaultIfBlank(dto.getProviderName(), entity.getLogisticsSupplierName()));
            entity.setHandoverStatus(HandoverStatusEnum.SHOPEE_ORDER_CREATED.getCode());
        });
    }

    private List<BatchResultDTO> uploadFirstMile(PackageForecastDTO.UploadDTO dto, ShopeeForecastContext context) {
        validateFirstMileDTO(dto);
        rejectRetryWithPlatformIdentifiers(context);
        String trackingNumber = dto.getFirstMileTrackingNumber();
        if (Boolean.TRUE.equals(dto.getGenerateNewTrackingNumber())) {
            GenerateFirstMileTrackingNumberRequest generateRequest = GenerateFirstMileTrackingNumberRequest.builder()
                    .declareDate(dto.getDeclareDate().toString())
                    .quantity(1)
                    .build();
            ValidatorUtil.validateEntity(generateRequest);
            GenerateFirstMileTrackingNumberResponse generateResponse =
                    shopeeLogisticsService.generateFirstMileTrackingNumber(buildBaseRequest(context.getShopId()), generateRequest);
            if (Objects.isNull(generateResponse)) {
                throw new ServiceException("虾皮生成揽收批次号响应为空");
            }
            trackingNumber = CollectionUtils.emptyIfNull(generateResponse.getFirstMileTrackingNumberList()).stream()
                    .filter(StringUtils::isNotBlank)
                    .findFirst()
                    .orElseThrow(() -> new ServiceException("虾皮生成揽收批次号为空"));
        }

        BindFirstMileTrackingNumberRequest request = BindFirstMileTrackingNumberRequest.builder()
                .firstMileTrackingNumber(trackingNumber)
                .shipmentMethod(dto.getCollectMode())
                .region(defaultRegion(dto.getRegion()))
                .logisticsChannelId(PackageForecastCollectModeEnum.SHOPEE_SELF_DELIVER.getCode().equals(dto.getCollectMode()) ? null : dto.getLogisticsChannelId())
                .orderList(context.getOrderList())
                .build();
        ValidatorUtil.validateEntity(request);
        BindFirstMileTrackingNumberResponse response =
                shopeeLogisticsService.bindFirstMileTrackingNumber(buildBaseRequest(context.getShopId()), request);
        if (Objects.isNull(response)) {
            throw new ServiceException("Shopee组包上传响应为空");
        }

        Map<String, String> failReasonMap = CollectionUtils.emptyIfNull(response.getOrderList()).stream()
                .filter(this::isBindFailed)
                .collect(Collectors.toMap(item -> orderKey(item.getOrderSn(), item.getPackageNumber()),
                        this::buildFailReason, (left, right) -> left));
        Set<String> successKeys = CollectionUtils.emptyIfNull(response.getOrderList()).stream()
                .filter(this::isBindSuccess)
                .map(item -> orderKey(item.getOrderSn(), item.getPackageNumber()))
                .collect(Collectors.toSet());
        String finalTrackingNumber = StringUtils.defaultIfBlank(response.getFirstMileTrackingNumber(), trackingNumber);
        return updateUploadResult(context, successKeys, failReasonMap, entity -> {
            entity.setCollectMode(dto.getCollectMode());
            entity.setTransportNo(finalTrackingNumber);
            entity.setHandoverStatus(HandoverStatusEnum.SHOPEE_ORDER_CREATED.getCode());
        });
    }

    private List<BatchResultDTO> updateUploadResult(ShopeeForecastContext context, Set<String> successKeys,
                                                    Map<String, String> failReasonMap,
                                                    java.util.function.Consumer<PackageForecastEntity> successConsumer) {
        Map<String, Boolean> successMap = new HashMap<>();
        for (PackageForecastEntity entity : context.getEntityList()) {
            List<String> orderKeys = context.getForecastOrderKeyMap().get(entity.getId());
            List<String> failureReasons = CollectionUtils.emptyIfNull(orderKeys).stream()
                    .filter(failReasonMap::containsKey)
                    .map(failReasonMap::get)
                    .collect(Collectors.toList());
            boolean success = CollectionUtils.isNotEmpty(orderKeys)
                    && CollectionUtils.isNotEmpty(successKeys)
                    && failureReasons.isEmpty()
                    && successKeys.containsAll(orderKeys);
            if (success) {
                successConsumer.accept(entity);
                entity.setUploadStatus(PackageUploadStatusEnum.UPLOAD_SUCCESS.getCode());
                entity.setRemark("");
            } else {
                entity.setUploadStatus(PackageUploadStatusEnum.UPLOAD_FAILURE.getCode());
                entity.setRemark("上传失败:" + StringUtils.defaultIfBlank(String.join(";", failureReasons), "Shopee返回失败"));
            }
            successMap.put(entity.getId(), success);
        }
        try {
            updateForecastBatchOrThrow(context.getEntityList());
            return buildUploadResult(context.getEntityList(), successMap);
        } catch (Exception e) {
            log.error("虾皮组包预报上传结果批量更新失败, size: {}", context.getEntityList().size(), e);
            return updateUploadResultOneByOne(context.getEntityList(), successMap);
        }
    }

    private List<BatchResultDTO> updateUploadResultOneByOne(List<PackageForecastEntity> entityList, Map<String, Boolean> successMap) {
        List<BatchResultDTO> resultList = new ArrayList<>();
        for (PackageForecastEntity entity : entityList) {
            boolean success = Boolean.TRUE.equals(successMap.get(entity.getId()));
            try {
                updateForecastOrThrow(entity);
            } catch (Exception e) {
                log.error("虾皮组包预报上传结果本地更新失败, id: {}, code: {}, platformPackageNo: {}, transportNo: {}",
                        entity.getId(), entity.getCode(), entity.getPlatformPackageNo(), entity.getTransportNo(), e);
                String message = success
                        ? "Shopee平台已绑定成功，本地更新失败，请同步状态或人工处理"
                        : "上传失败状态更新失败，请稍后重试或联系管理员处理";
                resultList.add(BatchResultDTO.fail(entity.getId(), entity.getCode(), message));
                continue;
            }
            resultList.add(toUploadResult(entity, success));
        }
        return resultList;
    }

    private List<BatchResultDTO> buildUploadResult(List<PackageForecastEntity> entityList, Map<String, Boolean> successMap) {
        return entityList.stream()
                .map(entity -> toUploadResult(entity, Boolean.TRUE.equals(successMap.get(entity.getId()))))
                .collect(Collectors.toList());
    }

    private BatchResultDTO toUploadResult(PackageForecastEntity entity, boolean success) {
        if (success) {
            return BatchResultDTO.success(entity.getId(), entity.getCode(), "上传成功");
        }
        return BatchResultDTO.fail(entity.getId(), entity.getCode(), entity.getRemark());
    }

    private String userFailureMessage(String operationName, Exception e) {
        if (e instanceof ServiceException && StringUtils.isNotBlank(e.getMessage())) {
            return operationName + "失败:" + e.getMessage();
        }
        return operationName + "失败，请稍后重试或联系管理员处理";
    }

    private String printCourierDelivery(PackageForecastEntity entity, String shopId) {
        if (StringUtils.isBlank(entity.getPlatformPackageNo())) {
            throw new ServiceException("虾皮快递寄送绑定ID为空");
        }
        CourierDeliveryBindingInfo bindingInfo = findCourierDeliveryBindingInfo(entity, shopId)
                .orElseThrow(() -> new ServiceException("虾皮快递寄送绑定结果暂未返回，请稍后重试,bindingId:" + entity.getPlatformPackageNo()));
        validateCourierDeliveryBindingStatus(entity, bindingInfo);
        String status = mapShopeeHandoverStatus(bindingInfo.getStatus());
        CourierDeliveryWaybillRequest request = CourierDeliveryWaybillRequest.builder()
                .bindingIdList(Collections.singletonList(entity.getPlatformPackageNo()))
                .build();
        CourierDeliveryWaybillResponse response = shopeeLogisticsService.getCourierDeliveryWaybill(buildBaseRequest(shopId), request);
        if (Objects.isNull(response) || CollectionUtils.isEmpty(response.getWaybillList())) {
            throw new ServiceException("虾皮快递寄送交接面单查询失败,bindingId:" + entity.getPlatformPackageNo());
        }
        CourierDeliveryWaybill waybill = CollectionUtils.emptyIfNull(response.getWaybillList()).stream()
                .filter(item -> entity.getPlatformPackageNo().equals(item.getBindingId()))
                .findFirst()
                .orElseThrow(() -> new ServiceException("虾皮快递寄送交接面单不存在,bindingId:" + entity.getPlatformPackageNo()));
        String url = waybill.getShippingLabelUrl();
        if (StringUtils.isBlank(url)) {
            throw new ServiceException("虾皮已接收快递寄送下单，但未返回交接面单链接，请核对收货仓/快递服务是否支持并已生成平台面单，或更换可出面单的收货仓/快递服务后重新下单,bindingId:"
                    + entity.getPlatformPackageNo() + ",firstMileTrackingNumber:"
                    + StringUtils.defaultString(bindingInfo.getFirstMileTrackingNumber()) + ",status:"
                    + StringUtils.defaultString(bindingInfo.getStatus()));
        }
        String base64;
        try {
            base64 = PdfUtil.convertPdfUrlToBase64(url, true);
        } catch (IOException e) {
            log.error("虾皮快递寄送交接面单下载失败, bindingId: {}", entity.getPlatformPackageNo(), e);
            throw new ServiceException("虾皮快递寄送交接面单下载失败");
        }
        if (StringUtils.isNotBlank(status) && !StringUtils.equals(status, entity.getHandoverStatus())) {
            entity.setHandoverStatus(status);
            try {
                updateForecastOrThrow(entity);
            } catch (Exception e) {
                log.warn("虾皮快递寄送打印成功但交接状态更新失败, id: {}, code: {}, status: {}",
                        entity.getId(), entity.getCode(), status, e);
            }
        }
        return base64;
    }

    private String printFirstMile(PackageForecastEntity entity, String shopId) {
        if (StringUtils.isBlank(entity.getTransportNo())) {
            throw new ServiceException("虾皮头程追踪号为空");
        }
        FirstMileWaybillRequest request = FirstMileWaybillRequest.builder()
                .firstMileTrackingNumberList(Collections.singletonList(entity.getTransportNo()))
                .build();
        return shopeeLogisticsService.getWaybill(buildBaseRequest(shopId), request);
    }

    private Optional<CourierDeliveryBindingInfo> findCourierDeliveryBindingInfo(PackageForecastEntity entity, String shopId) {
        if (StringUtils.isBlank(entity.getPlatformPackageNo())) {
            return Optional.empty();
        }
        ShopeeTrackingQueryRange range = buildTrackingQueryRange(entity);
        ShopeeTrackingSyncKey key = new ShopeeTrackingSyncKey(shopId,
                PackageForecastCollectModeEnum.SHOPEE_COURIER_DELIVERY.getCode(),
                range.getFromDate(), range.getToDate());
        return Optional.ofNullable(queryCourierDeliveryBindingInfoMap(key).get(entity.getPlatformPackageNo()));
    }

    private void syncFirstMileTrackingStatus(List<PackageForecastEntity> entityList, ShopeeTrackingSyncKey key) {
        Map<String, String> statusMap = queryFirstMileTrackingStatusMap(key);
        List<PackageForecastEntity> changedList = new ArrayList<>();
        for (PackageForecastEntity entity : entityList) {
            String status = mapShopeeHandoverStatus(statusMap.get(entity.getTransportNo()));
            if (StringUtils.isNotBlank(status) && !StringUtils.equals(status, entity.getHandoverStatus())) {
                entity.setHandoverStatus(status);
                changedList.add(entity);
            }
        }
        updateForecastBatchOrThrow(changedList);
    }

    private void syncCourierDeliveryTrackingStatus(List<PackageForecastEntity> entityList, ShopeeTrackingSyncKey key) {
        Map<String, CourierDeliveryBindingInfo> bindingInfoMap = queryCourierDeliveryBindingInfoMap(key);
        List<PackageForecastEntity> changedList = new ArrayList<>();
        for (PackageForecastEntity entity : entityList) {
            CourierDeliveryBindingInfo bindingInfo = bindingInfoMap.get(entity.getPlatformPackageNo());
            if (Objects.isNull(bindingInfo)) {
                continue;
            }
            String status = mapShopeeHandoverStatus(bindingInfo.getStatus());
            if (StringUtils.isNotBlank(status) && !StringUtils.equals(status, entity.getHandoverStatus())) {
                entity.setHandoverStatus(status);
                changedList.add(entity);
            }
        }
        updateForecastBatchOrThrow(changedList);
    }

    private Map<String, String> queryFirstMileTrackingStatusMap(ShopeeTrackingSyncKey key) {
        Map<String, String> resultMap = new HashMap<>();
        BaseRequest baseRequest = buildBaseRequest(key.getShopId());
        String cursor = null;
        do {
            FirstMileTrackingNumberListRequest request = FirstMileTrackingNumberListRequest.builder()
                    .fromDate(key.getFromDate().toString())
                    .toDate(key.getToDate().toString())
                    .pageSize(SHOPEE_TRACKING_PAGE_SIZE)
                    .cursor(cursor)
                    .build();
            FirstMileTrackingNumberListResponse response = shopeeLogisticsService.getTrackNumberList(baseRequest, request);
            if (Objects.isNull(response)) {
                return resultMap;
            }
            CollectionUtils.emptyIfNull(response.getFirstMileTrackingNumberList()).stream()
                    .filter(item -> StringUtils.isNotBlank(item.getFirstMileTrackingNumber()))
                    .forEach(item -> resultMap.put(item.getFirstMileTrackingNumber(), item.getStatus()));
            cursor = nextCursor(response.getNextCursor(), response.getMore());
        } while (StringUtils.isNotBlank(cursor));
        return resultMap;
    }

    private Map<String, CourierDeliveryBindingInfo> queryCourierDeliveryBindingInfoMap(ShopeeTrackingSyncKey key) {
        Map<String, CourierDeliveryBindingInfo> resultMap = new HashMap<>();
        BaseRequest baseRequest = buildBaseRequest(key.getShopId());
        String cursor = null;
        do {
            CourierDeliveryTrackingNumberListRequest request = CourierDeliveryTrackingNumberListRequest.builder()
                    .fromDate(key.getFromDate().toString())
                    .toDate(key.getToDate().toString())
                    .pageSize(SHOPEE_TRACKING_PAGE_SIZE)
                    .cursor(cursor)
                    .build();
            ValidatorUtil.validateEntity(request);
            CourierDeliveryTrackingNumberListResponse response =
                    shopeeLogisticsService.getCourierDeliveryTrackingNumberList(baseRequest, request);
            if (Objects.isNull(response)) {
                return resultMap;
            }
            CollectionUtils.emptyIfNull(response.getTrackingNumberList()).stream()
                    .filter(item -> StringUtils.isNotBlank(item.getBindingId()))
                    .forEach(item -> resultMap.put(item.getBindingId(), item));
            cursor = nextCursor(response.getNextCursor(), response.getMore());
        } while (StringUtils.isNotBlank(cursor));
        return resultMap;
    }

    private void validateCourierDeliveryBindingStatus(PackageForecastEntity entity, CourierDeliveryBindingInfo bindingInfo) {
        String status = bindingInfo.getStatus();
        String bindingId = entity.getPlatformPackageNo();
        if (SHOPEE_PLATFORM_STATUS_NOT_AVAILABLE.equals(status)) {
            throw new ServiceException("虾皮快递寄送绑定ID暂未绑定订单，请稍后重试,bindingId:" + bindingId);
        }
        // bindingInfo.status 是 Shopee 平台原始状态；枚举 code 对应平台值，可直接比较。
        if (HandoverStatusEnum.SHOPEE_CANCELING.getCode().equals(status)
                || HandoverStatusEnum.CANCELED_2.getCode().equals(status)) {
            throw new ServiceException("虾皮快递寄送单已取消或取消中,bindingId:" + bindingId
                    + ",status:" + status + ",reason:" + StringUtils.defaultString(bindingInfo.getReason()));
        }
    }

    private Map<String, String> cancelCourierDelivery(ShopeeForecastContext context) {
        return cancelByOrder(context);
    }

    private Map<String, String> cancelFirstMile(ShopeeForecastContext context) {
        return cancelByOrder(context);
    }

    private Map<String, String> cancelByOrder(ShopeeForecastContext context) {
        UnbindFirstMileTrackingNumberAllRequest request = UnbindFirstMileTrackingNumberAllRequest.builder()
                .orderList(context.getOrderList())
                .build();
        UnbindFirstMileTrackingNumberAllResponse response =
                shopeeLogisticsService.unbindFirstMileTrackingNumberAll(buildBaseRequest(context.getShopId()), request);
        if (Objects.isNull(response)) {
            throw new ServiceException("Shopee取消组包响应为空");
        }
        Set<String> successKeys = CollectionUtils.emptyIfNull(response.getSuccessList()).stream()
                .map(item -> orderKey(item.getOrderSn(), item.getPackageNumber()))
                .collect(Collectors.toSet());
        Set<String> notBindKeys = CollectionUtils.emptyIfNull(response.getFailList()).stream()
                .filter(this::isPackageHasNotBind)
                .map(item -> orderKey(item.getOrderSn(), item.getPackageNumber()))
                .collect(Collectors.toSet());
        Map<String, String> failReasonMap = CollectionUtils.emptyIfNull(response.getFailList()).stream()
                .filter(item -> !isPackageHasNotBind(item))
                .collect(Collectors.toMap(item -> orderKey(item.getOrderSn(), item.getPackageNumber()),
                        this::buildFailReasonWithOrder, (left, right) -> left));
        return context.getOrderList().stream()
                .filter(item -> !successKeys.contains(orderKey(item.getOrderSn(), item.getPackageNumber())))
                .filter(item -> !notBindKeys.contains(orderKey(item.getOrderSn(), item.getPackageNumber())))
                .collect(Collectors.toMap(item -> orderKey(item.getOrderSn(), item.getPackageNumber()),
                        item -> failReasonMap.getOrDefault(orderKey(item.getOrderSn(), item.getPackageNumber()),
                                buildOrderPrefix(item.getOrderSn(), item.getPackageNumber()) + "Shopee返回失败"),
                        (left, right) -> left));
    }

    private ShopeeForecastContext buildBaseContext(List<String> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            throw new ServiceException("组包预报单不能为空");
        }
        // 选项/上传/取消共用该上下文；入口权限和单据存在性由上层流程兜底，这里按已命中的单据构建平台上下文。
        List<PackageForecastEntity> entityList = packageForecastMapper.selectBatchIds(ids);
        if (CollectionUtils.isEmpty(entityList)) {
            throw new ServiceException(ApiError.BILL_NOT_EXIST_WITH_TYPE, "组包预报单");
        }
        List<PackageForecastDetailEntity> detailList = packageForecastDetailService.listDbByMainIds(ids);
        if (CollectionUtils.isEmpty(detailList)) {
            throw new ServiceException(ApiError.BILL_NOT_EXIST_WITH_TYPE, "组包预报单明细");
        }
        List<String> soIds = detailList.stream().map(PackageForecastDetailEntity::getSoId).distinct().collect(Collectors.toList());
        List<SoB2cEntity> soList = soB2cFeign.listByIds(soIds);
        if (CollectionUtils.isEmpty(soList)) {
            throw new ServiceException(ApiError.SO_B2C_NOT_FOUND);
        }
        if (soList.stream().anyMatch(item -> !PlatformDictEnum.SHOPEE.getCode().equals(item.getDictPlatform()))) {
            throw new ServiceException("仅Shopee平台订单可操作虾皮组包预报");
        }
        List<String> shopIds = soList.stream().map(SoB2cEntity::getShopId).filter(StringUtils::isNotBlank).distinct().collect(Collectors.toList());
        if (CollectionUtils.isEmpty(shopIds)) {
            throw new ServiceException("销售订单店铺未找到");
        }
        if (shopIds.size() > 1) {
            throw new ServiceException("Shopee组包预报要求勾选订单店铺一致");
        }
        ShopeeForecastContext context = new ShopeeForecastContext();
        context.setEntityList(entityList);
        context.setDetailList(detailList);
        context.setSoList(soList);
        context.setShopId(shopIds.get(0));
        return context;
    }

    private ShopeeForecastContext buildContext(List<String> ids) {
        ShopeeForecastContext context = buildBaseContext(ids);
        List<PackageForecastDetailEntity> detailList = context.getDetailList();
        List<SoB2cEntity> soList = context.getSoList();
        Map<String, SoB2cEntity> soMap = soList.stream().collect(Collectors.toMap(SoB2cEntity::getId, Function.identity(), (left, right) -> left));
        LinkedHashMap<String, FirstMileOrder> orderMap = new LinkedHashMap<>();
        Map<String, List<String>> forecastOrderKeyMap = new HashMap<>();
        for (PackageForecastDetailEntity detail : detailList) {
            SoB2cEntity so = soMap.get(detail.getSoId());
            if (Objects.isNull(so) || StringUtils.isBlank(so.getPlatformCode())) {
                throw new ServiceException("销售订单:{}未获取Shopee平台单号", detail.getSoCode());
            }
            String packageNumber = getShopeePackageNumber(so).orElse(null);
            String key = orderKey(so.getPlatformCode(), packageNumber);
            orderMap.putIfAbsent(key, FirstMileOrder.builder().orderSn(so.getPlatformCode()).packageNumber(packageNumber).build());
            forecastOrderKeyMap.computeIfAbsent(detail.getMainId(), item -> new ArrayList<>()).add(key);
        }
        if (orderMap.size() > 50) {
            throw new ServiceException("Shopee组包下单单次最多支持50个订单");
        }
        context.setOrderList(new ArrayList<>(orderMap.values()));
        context.setForecastOrderKeyMap(forecastOrderKeyMap);
        return context;
    }

    private Optional<String> getShopeePackageNumber(SoB2cEntity so) {
        if (Objects.isNull(so) || StringUtils.isBlank(so.getLabelJson())) {
            return Optional.empty();
        }
        String packageNumber = JSONUtil.parseObj(so.getLabelJson()).getStr("package_number");
        if (StringUtils.isBlank(packageNumber)) {
            return Optional.empty();
        }
        // 组包明细当前按 SO 维度关联，历史 label_json 可能以逗号保存 Shopee 包裹号；沿用首个包裹号保持原上传绑定口径。
        return Arrays.stream(packageNumber.split(","))
                .map(StringUtils::trimToEmpty)
                .filter(StringUtils::isNotBlank)
                .findFirst();
    }

    private void validateUploadEntities(ShopeeForecastContext context) {
        List<String> enableStatus = new ArrayList<>();
        enableStatus.add(PackageUploadStatusEnum.WAIT.getCode());
        enableStatus.add(PackageUploadStatusEnum.UPLOAD_FAILURE.getCode());
        for (PackageForecastEntity entity : context.getEntityList()) {
            if (!enableStatus.contains(entity.getUploadStatus())) {
                throw new ServiceException("仅待上传/上传失败可操作");
            }
        }
    }

    private void validateCancelEntities(ShopeeForecastContext context) {
        for (PackageForecastEntity entity : context.getEntityList()) {
            if (!PackageUploadStatusEnum.UPLOAD_SUCCESS.getCode().equals(entity.getUploadStatus())) {
                throw new ServiceException("仅上传成功可操作");
            }
        }
    }

    private void validateCourierDeliveryDTO(PackageForecastDTO.UploadDTO dto) {
        if (StringUtils.isBlank(dto.getAddressId())) {
            throw new ServiceException("寄件地址不能为空");
        }
        if (StringUtils.isBlank(dto.getWarehouseId())) {
            throw new ServiceException("收货仓不能为空");
        }
        if (Objects.isNull(dto.getLogisticsProductId())) {
            throw new ServiceException("物流名称不能为空");
        }
        if (StringUtils.isBlank(dto.getCourierServiceId())) {
            throw new ServiceException("快递服务不能为空");
        }
        if ((isPrepaidPayment(dto.getPaymentMode()) || isPrepaidLogisticsProduct(dto.getLogisticsProductId()))
                && Objects.isNull(dto.getPrepaidAccountId())) {
            throw new ServiceException("当前物流产品需要月结账号，请选择月结账号");
        }
    }

    private void validateFirstMileDTO(PackageForecastDTO.UploadDTO dto) {
        if (!PackageForecastCollectModeEnum.SHOPEE_PICKUP.getCode().equals(dto.getCollectMode())
                && !PackageForecastCollectModeEnum.SHOPEE_DROPOFF.getCode().equals(dto.getCollectMode())
                && !PackageForecastCollectModeEnum.SHOPEE_SELF_DELIVER.getCode().equals(dto.getCollectMode())) {
            throw new ServiceException("Shopee揽收方式不支持");
        }
        if (Boolean.TRUE.equals(dto.getGenerateNewTrackingNumber()) && Objects.isNull(dto.getDeclareDate())) {
            throw new ServiceException("取货日期不能为空");
        }
        if (!Boolean.TRUE.equals(dto.getGenerateNewTrackingNumber()) && StringUtils.isBlank(dto.getFirstMileTrackingNumber())) {
            throw new ServiceException("绑定批次号不能为空");
        }
        if (!PackageForecastCollectModeEnum.SHOPEE_SELF_DELIVER.getCode().equals(dto.getCollectMode()) && Objects.isNull(dto.getLogisticsChannelId())) {
            throw new ServiceException("物流名称不能为空");
        }
    }

    private BaseRequest buildBaseRequest(String shopId) {
        CfgAppClientDTO.FindDTO findDTO = new CfgAppClientDTO.FindDTO();
        AppClientEnum appClientEnum = AppClientEnum.SHOPEE_ACCESS_TOKEN;
        findDTO.setBusinessType(appClientEnum.getBusinessType());
        findDTO.setDictPlatform(appClientEnum.getPlatform());
        findDTO.setPlatformType(appClientEnum.getPlatformType());
        CfgAppClientEntity cfgAppClient = dmpTaskFeign.getCfgAppClient(findDTO);
        if (Objects.isNull(cfgAppClient)) {
            throw new ServiceException("Shopee应用配置不存在");
        }
        ApiResult<ShopAuthEntity> shopAuthResult = shopInfoFeign.getShopAuthById(shopId);
        if (Objects.isNull(shopAuthResult) || !shopAuthResult.isSuccess()) {
            throw new ServiceException("Shopee店铺授权查询失败");
        }
        if (Objects.isNull(shopAuthResult.getData())) {
            throw new ServiceException("Shopee店铺授权不存在");
        }
        ShopAuthEntity shopAuth = shopAuthResult.getData();
        BaseRequest baseRequest = BaseRequest.builder()
                .partnerKey(cfgAppClient.getClientSecret())
                .partnerId(parseLong(cfgAppClient.getClientId(), "Shopee partnerId"))
                .shopId(parseLong(shopAuth.getShopeeId(), "Shopee shopId"))
                .accessToken(shopAuth.getAccessToken())
                .host(cfgAppClient.getUrl())
                .build();
        ValidatorUtil.validateEntity(baseRequest);
        return baseRequest;
    }

    @Override
    protected void resetAfterCancel(PackageForecastEntity entity) {
        entity.setUploadStatus(PackageUploadStatusEnum.WAIT.getCode());
        entity.setHandoverStatus("");
        entity.setTransportNo("");
        entity.setHandoverNo("");
        entity.setRemark("");
        entity.setPlatformPackageNo("");
        entity.setPrintStatus(PackagePrintStatusEnum.NOT.getCode());
    }

    private void updateEntities(List<PackageForecastEntity> entities) {
        updateForecastBatchOrThrow(entities);
    }

    private String mapShopeeHandoverStatus(String status) {
        if (SHOPEE_PLATFORM_STATUS_NOT_AVAILABLE.equals(status)) {
            return "";
        }
        if (SHOPEE_PLATFORM_STATUS_DELIVERED.equals(status)) {
            return HandoverStatusEnum.SHOPEE_DELIVERED.getCode();
        }
        // Shopee 头程中间态需保留平台原始值，只有系统已定义的终态在这里映射为内部枚举。
        return status;
    }

    private void rejectRetryWithPlatformIdentifiers(ShopeeForecastContext context) {
        Optional<PackageForecastEntity> persistedPlatformEntity = context.getEntityList().stream()
                .filter(entity -> StringUtils.isNotBlank(entity.getPlatformPackageNo())
                        || StringUtils.isNotBlank(entity.getTransportNo()))
                .findFirst();
        if (persistedPlatformEntity.isPresent()) {
            PackageForecastEntity entity = persistedPlatformEntity.get();
            throw new ServiceException("虾皮组包预报已存在平台绑定信息，请先同步状态或人工处理后再重试，单号:"
                    + entity.getCode());
        }
    }

    private String orderKey(String orderSn, String packageNumber) {
        return StringUtils.defaultString(orderSn) + "|" + StringUtils.defaultString(packageNumber);
    }

    private String buildFailReason(FirstMileBindingFail fail) {
        return defaultShopeeFailReason(fail.getFailMessage(), fail.getFailError());
    }

    private String buildFailReason(BindFirstMileTrackingNumberOrder fail) {
        return defaultShopeeFailReason(fail.getFailMessage(), fail.getFailError());
    }

    private boolean isBindFailed(BindFirstMileTrackingNumberOrder item) {
        return Objects.nonNull(item) && (hasShopeeFailText(item.getFailError()) || hasShopeeFailText(item.getFailMessage()));
    }

    private boolean isBindSuccess(BindFirstMileTrackingNumberOrder item) {
        return Objects.nonNull(item) && !isBindFailed(item);
    }

    private String defaultShopeeFailReason(String failMessage, String failError) {
        if (hasShopeeFailText(failMessage)) {
            return failMessage;
        }
        if (hasShopeeFailText(failError)) {
            return failError;
        }
        return "Shopee返回失败";
    }

    private boolean hasShopeeFailText(String value) {
        String text = StringUtils.trimToEmpty(value);
        return StringUtils.isNotBlank(text) && !"{}".equals(text) && !"null".equalsIgnoreCase(text);
    }

    private String buildFailReasonWithOrder(FirstMileBindingFail fail) {
        return buildOrderPrefix(fail.getOrderSn(), fail.getPackageNumber()) + buildFailReason(fail);
    }

    private String buildFailReasonWithOrder(UnbindFirstMileTrackingNumberOrder fail) {
        return buildOrderPrefix(fail.getOrderSn(), fail.getPackageNumber())
                + defaultShopeeFailReason(fail.getFailMessage(), fail.getFailError());
    }

    private boolean isPackageHasNotBind(FirstMileBindingFail fail) {
        return Objects.nonNull(fail) && FIRST_MILE_PACKAGE_HAS_NOT_BIND.equals(fail.getFailError());
    }

    private boolean isPackageHasNotBind(UnbindFirstMileTrackingNumberOrder fail) {
        return Objects.nonNull(fail) && FIRST_MILE_PACKAGE_HAS_NOT_BIND.equals(fail.getFailError());
    }

    private String buildOrderPrefix(String orderSn, String packageNumber) {
        if (StringUtils.isBlank(orderSn) && StringUtils.isBlank(packageNumber)) {
            return "";
        }
        return "订单" + StringUtils.defaultString(orderSn) + "包裹" + StringUtils.defaultString(packageNumber) + ":";
    }

    private String defaultRegion(String region) {
        return StringUtils.defaultIfBlank(region, REGION_CN);
    }

    private List<PackageForecastDTO.ShopeePrepaidAccountDTO> listPrepaidAccount(String shopId, BaseRequest shopBaseRequest) {
        ShopAuthEntity merchantAuth = findMerchantAuth(shopId);
        if (Objects.isNull(merchantAuth)) {
            return Collections.emptyList();
        }
        List<PackageForecastDTO.ShopeePrepaidAccountDTO> resultList = new ArrayList<>();
        int pageNo = 1;
        while (true) {
            MerchantPrepaidAccountRequest request = MerchantPrepaidAccountRequest.builder()
                    .host(shopBaseRequest.getHost())
                    .accessToken(merchantAuth.getAccessToken())
                    .partnerId(shopBaseRequest.getPartnerId())
                    .partnerKey(shopBaseRequest.getPartnerKey())
                    .merchantId(parseLong(merchantAuth.getShopeeId(), "Shopee merchantId"))
                    .pageNo(pageNo)
                    .pageSize(100)
                    .build();
            ValidatorUtil.validateEntity(request);
            MerchantPrepaidAccountListResponse response = shopeeLogisticsService.getMerchantPrepaidAccountList(request);
            if (Objects.isNull(response) || CollectionUtils.isEmpty(response.getList())) {
                break;
            }
            resultList.addAll(response.getList().stream().map(this::toPrepaidAccountDTO).collect(Collectors.toList()));
            if (!Boolean.TRUE.equals(response.getMore()) || response.getList().size() < 100) {
                break;
            }
            pageNo++;
        }
        return resultList;
    }

    private ShopAuthEntity findMerchantAuth(String shopId) {
        List<ShopInfoEntity> relatedShopList = shopInfoFeign.getRelatedByShopId(shopId);
        if (CollectionUtils.isEmpty(relatedShopList)) {
            return null;
        }
        relatedShopList = relatedShopList.stream()
                .filter(item -> PlatformDictEnum.SHOPEE.getCode().equals(item.getDictPlatform()))
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(relatedShopList)) {
            return null;
        }
        Map<String, ShopInfoEntity> relatedShopMap = relatedShopList.stream()
                .collect(Collectors.toMap(ShopInfoEntity::getId, Function.identity(), (left, right) -> left));
        List<ShopAuthEntity> merchantAuthList = shopInfoFeign.listShopAuthByShopIds(relatedShopList.stream()
                .map(ShopInfoEntity::getId)
                .filter(StringUtils::isNotBlank)
                .distinct()
                .collect(Collectors.toList()));
        return CollectionUtils.emptyIfNull(merchantAuthList).stream()
                .filter(item -> AuthTypeEnum.MERCHANT.getCode().equals(item.getType()))
                .filter(item -> StringUtils.isNotBlank(item.getAccessToken()) && StringUtils.isNotBlank(item.getShopeeId()))
                .filter(item -> Objects.nonNull(relatedShopMap.get(item.getShopId())))
                .findFirst()
                .orElse(null);
    }

    private Long parseLong(String value, String fieldName) {
        if (StringUtils.isBlank(value)) {
            throw new ServiceException(fieldName + "不能为空");
        }
        try {
            return Long.valueOf(value);
        } catch (NumberFormatException e) {
            throw new ServiceException(fieldName + "格式错误");
        }
    }

    private boolean isPrepaidPayment(String paymentMode) {
        return PAYMENT_PREPAID.equalsIgnoreCase(paymentMode)
                || PAYMENT_MONTHLY.equalsIgnoreCase(paymentMode)
                || PAYMENT_MONTHLY_CN.equals(paymentMode);
    }

    private boolean isPrepaidLogisticsProduct(Long logisticsProductId) {
        return SHOPEE_PREPAID_LOGISTICS_PRODUCT_ID.equals(logisticsProductId);
    }

    private boolean isPrepaidAccountRequiredError(String message) {
        return StringUtils.containsIgnoreCase(message, "prepaid_account_id")
                && StringUtils.containsIgnoreCase(message, "required");
    }

    private Optional<ShopeeTrackingSyncKey> buildTrackingSyncKey(PackageForecastEntity entity) {
        return buildTrackingSyncKey(entity, Collections.emptyMap(), true);
    }

    private Optional<ShopeeTrackingSyncKey> buildTrackingSyncKey(PackageForecastEntity entity, Map<String, String> trackingShopIdMap) {
        return buildTrackingSyncKey(entity, trackingShopIdMap, false);
    }

    private Optional<ShopeeTrackingSyncKey> buildTrackingSyncKey(PackageForecastEntity entity, Map<String, String> trackingShopIdMap,
                                                                boolean allowSingleFallback) {
        boolean courierDelivery = PackageForecastCollectModeEnum.SHOPEE_COURIER_DELIVERY.getCode().equals(entity.getCollectMode());
        if (courierDelivery && StringUtils.isBlank(entity.getPlatformPackageNo())) {
            return Optional.empty();
        }
        if (!courierDelivery && StringUtils.isBlank(entity.getTransportNo())) {
            return Optional.empty();
        }
        String shopId = entity.getShopId();
        if (StringUtils.isBlank(shopId)) {
            shopId = trackingShopIdMap.get(entity.getId());
        }
        if (StringUtils.isBlank(shopId) && allowSingleFallback) {
            ShopeeForecastContext context = buildBaseContext(Collections.singletonList(entity.getId()));
            shopId = context.getShopId();
        }
        if (StringUtils.isBlank(shopId)) {
            throw new ServiceException("销售订单店铺未找到");
        }
        ShopeeTrackingQueryRange range = buildTrackingQueryRange(entity);
        return Optional.of(new ShopeeTrackingSyncKey(shopId, entity.getCollectMode(), range.getFromDate(), range.getToDate()));
    }

    private Map<String, String> buildTrackingShopIdMap(List<PackageForecastEntity> entityList) {
        List<String> missingShopForecastIds = CollectionUtils.emptyIfNull(entityList).stream()
                .filter(Objects::nonNull)
                .filter(entity -> StringUtils.isBlank(entity.getShopId()))
                .map(PackageForecastEntity::getId)
                .filter(StringUtils::isNotBlank)
                .distinct()
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(missingShopForecastIds)) {
            return Collections.emptyMap();
        }
        List<PackageForecastDetailEntity> detailList = packageForecastDetailService.listDbByMainIds(missingShopForecastIds);
        if (CollectionUtils.isEmpty(detailList)) {
            return Collections.emptyMap();
        }
        List<String> soIds = detailList.stream()
                .map(PackageForecastDetailEntity::getSoId)
                .filter(StringUtils::isNotBlank)
                .distinct()
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(soIds)) {
            return Collections.emptyMap();
        }
        List<SoB2cEntity> soList = soB2cFeign.listByIds(soIds);
        if (CollectionUtils.isEmpty(soList)) {
            return Collections.emptyMap();
        }
        Map<String, SoB2cEntity> soMap = soList.stream()
                .collect(Collectors.toMap(SoB2cEntity::getId, Function.identity(), (left, right) -> left));
        Map<String, Set<String>> forecastShopIdSetMap = new HashMap<>();
        for (PackageForecastDetailEntity detail : detailList) {
            SoB2cEntity so = soMap.get(detail.getSoId());
            if (Objects.isNull(so)
                    || !PlatformDictEnum.SHOPEE.getCode().equals(so.getDictPlatform())
                    || StringUtils.isBlank(so.getShopId())) {
                continue;
            }
            forecastShopIdSetMap.computeIfAbsent(detail.getMainId(), id -> new HashSet<>()).add(so.getShopId());
        }
        Map<String, String> resultMap = new HashMap<>(forecastShopIdSetMap.size());
        for (Map.Entry<String, Set<String>> entry : forecastShopIdSetMap.entrySet()) {
            if (entry.getValue().size() == 1) {
                resultMap.put(entry.getKey(), entry.getValue().iterator().next());
            } else {
                log.warn("Shopee轨迹同步解析到多店铺组包预报单, forecastId: {}, shopIds: {}", entry.getKey(), entry.getValue());
            }
        }
        return resultMap;
    }

    private ShopeeTrackingQueryRange buildTrackingQueryRange(PackageForecastEntity entity) {
        LocalDate now = LocalDate.now();
        LocalDate referenceDate = entity.getBillDate();
        if (Objects.isNull(referenceDate) && Objects.nonNull(entity.getCreateTime())) {
            referenceDate = entity.getCreateTime().toLocalDate();
        }
        if (Objects.isNull(referenceDate) || referenceDate.isAfter(now)) {
            referenceDate = now;
        }
        LocalDate fromDate = referenceDate.minusDays(SHOPEE_TRACKING_QUERY_PADDING_DAYS);
        LocalDate toDate = referenceDate.plusDays(SHOPEE_TRACKING_QUERY_PADDING_DAYS);
        if (toDate.isAfter(now)) {
            toDate = now;
        }
        if (fromDate.isAfter(toDate)) {
            fromDate = toDate;
        }
        return new ShopeeTrackingQueryRange(fromDate, toDate);
    }

    private String nextCursor(String cursor, Boolean more) {
        return Boolean.TRUE.equals(more) ? cursor : null;
    }

    private String withPdfPrefix(String base64) {
        if (StringUtils.startsWith(base64, PDF_PREFIX)) {
            return base64;
        }
        return PDF_PREFIX + base64;
    }

    private List<PackageForecastDTO.ShopeeWarehouseDTO> toWarehouseDTOList(TransitWarehouseListResponse response) {
        if (Objects.isNull(response) || CollectionUtils.isEmpty(response.getTransitWarehouseList())) {
            return Collections.emptyList();
        }
        return response.getTransitWarehouseList().stream().map(this::toWarehouseDTO).collect(Collectors.toList());
    }

    private PackageForecastDTO.ShopeeWarehouseDTO toWarehouseDTO(TransitWarehouse item) {
        PackageForecastDTO.ShopeeWarehouseDTO dto = new PackageForecastDTO.ShopeeWarehouseDTO();
        dto.setWarehouseId(item.getWarehouseId());
        dto.setWarehouseNameCn(item.getWarehouseNameCn());
        dto.setWarehouseNameEn(item.getWarehouseNameEn());
        dto.setWarehouseType(item.getWarehouseType());
        return dto;
    }

    private List<PackageForecastDTO.ShopeeAddressDTO> toAddressDTOList(ShopeeAddressListResponse response) {
        if (Objects.isNull(response) || CollectionUtils.isEmpty(response.getAddressList())) {
            return Collections.emptyList();
        }
        return response.getAddressList().stream().map(this::toAddressDTO).collect(Collectors.toList());
    }

    private PackageForecastDTO.ShopeeAddressDTO toAddressDTO(ShopeeAddress item) {
        PackageForecastDTO.ShopeeAddressDTO dto = new PackageForecastDTO.ShopeeAddressDTO();
        dto.setAddressId(item.getAddressId());
        dto.setRegion(item.getRegion());
        dto.setState(item.getState());
        dto.setCity(item.getCity());
        dto.setAddress(item.getAddress());
        dto.setZipcode(item.getZipcode());
        dto.setDistrict(item.getDistrict());
        dto.setTown(item.getTown());
        dto.setAddressType(item.getAddressType());
        return dto;
    }

    private List<PackageForecastDTO.ShopeeCourierChannelDTO> toCourierChannelDTOList(CourierDeliveryChannelResponse response) {
        if (Objects.isNull(response) || CollectionUtils.isEmpty(response.getLogisticsChannelList())) {
            return Collections.emptyList();
        }
        return response.getLogisticsChannelList().stream().map(this::toCourierChannelDTO).collect(Collectors.toList());
    }

    private PackageForecastDTO.ShopeeCourierChannelDTO toCourierChannelDTO(CourierLogisticsChannel item) {
        PackageForecastDTO.ShopeeCourierChannelDTO dto = new PackageForecastDTO.ShopeeCourierChannelDTO();
        dto.setLogisticsProductId(item.getLogisticsProductId());
        dto.setLogisticsProductName(item.getLogisticsProductName());
        dto.setRequiredPrepaidAccount(isPrepaidLogisticsProduct(item.getLogisticsProductId()));
        dto.setCourierList(CollectionUtils.emptyIfNull(item.getCourierList()).stream().map(this::toCourierServiceDTO).collect(Collectors.toList()));
        return dto;
    }

    private PackageForecastDTO.ShopeeCourierServiceDTO toCourierServiceDTO(CourierService item) {
        PackageForecastDTO.ShopeeCourierServiceDTO dto = new PackageForecastDTO.ShopeeCourierServiceDTO();
        dto.setCourierName(item.getCourierName());
        dto.setCourierServiceId(item.getCourierServiceId());
        dto.setCourierServiceName(item.getCourierServiceName());
        return dto;
    }

    private PackageForecastDTO.ShopeePrepaidAccountDTO toPrepaidAccountDTO(MerchantPrepaidAccount item) {
        PackageForecastDTO.ShopeePrepaidAccountDTO dto = new PackageForecastDTO.ShopeePrepaidAccountDTO();
        dto.setPrepaidAccountId(item.getPrepaidAccountId());
        dto.setPrepaidAccountCourierName(item.getPrepaidAccountCourierName());
        dto.setPrepaidAccountPartnerName(item.getPrepaidAccountPartnerName());
        dto.setPrepaidAccountIsDefault(item.getPrepaidAccountIsDefault());
        return dto;
    }

    private PackageForecastDTO.ShopeeFirstMileChannelDTO toFirstMileChannelDTO(FirstMileChannel item) {
        PackageForecastDTO.ShopeeFirstMileChannelDTO dto = new PackageForecastDTO.ShopeeFirstMileChannelDTO();
        dto.setShipmentMethod(item.getShipmentMethod());
        dto.setLogisticsChannelId(item.getLogisticsChannelId());
        dto.setLogisticsChannelName(item.getLogisticsChannelName());
        return dto;
    }

    private List<PackageForecastDTO.ShopeeTrackingNumberDTO> toTrackingNumberDTOList(List<FirstMileTrackingNumber> trackingNumberList) {
        if (CollectionUtils.isEmpty(trackingNumberList)) {
            return Collections.emptyList();
        }
        return trackingNumberList.stream()
                .map(this::toTrackingNumberDTO)
                .collect(Collectors.toList());
    }

    private PackageForecastDTO.ShopeeTrackingNumberDTO toTrackingNumberDTO(FirstMileTrackingNumber item) {
        PackageForecastDTO.ShopeeTrackingNumberDTO dto = new PackageForecastDTO.ShopeeTrackingNumberDTO();
        dto.setFirstMileTrackingNumber(item.getFirstMileTrackingNumber());
        dto.setStatus(item.getStatus());
        dto.setDeclareDate(item.getDeclareDate());
        return dto;
    }

    @Data
    private static class ShopeeForecastContext {
        private List<PackageForecastEntity> entityList;
        private List<PackageForecastDetailEntity> detailList;
        private List<SoB2cEntity> soList;
        private String shopId;
        private List<FirstMileOrder> orderList;
        private Map<String, List<String>> forecastOrderKeyMap;
    }

    @Data
    private static class ShopeeTrackingSyncKey {
        private final String shopId;
        private final String collectMode;
        private final LocalDate fromDate;
        private final LocalDate toDate;
    }

    @Data
    private static class ShopeeTrackingQueryRange {
        private final LocalDate fromDate;
        private final LocalDate toDate;
    }
}
