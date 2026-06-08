package com.erp.server.wms.service.adapter;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.text.CharSequenceUtil;
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
import com.erp.model.oms.entity.SoB2cDetailEntity;
import com.erp.model.oms.entity.SoB2cEntity;
import com.erp.model.oms.enums.AuthStatusEnum;
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
import com.erp.rpc.oms.feign.SoB2cFeign;
import com.erp.server.wms.mapper.PackageForecastMapper;
import com.erp.server.wms.service.PackageForecastDetailService;
import com.sdk.tms.shopee.model.base.BaseRequest;
import com.sdk.tms.shopee.model.firstmile.request.BindFirstMileTrackingNumberRequest;
import com.sdk.tms.shopee.model.firstmile.request.CourierDeliveryInfo;
import com.sdk.tms.shopee.model.firstmile.request.CourierDeliveryWaybillRequest;
import com.sdk.tms.shopee.model.firstmile.request.FirstMileOrder;
import com.sdk.tms.shopee.model.firstmile.request.FirstMileTrackingNumberListRequest;
import com.sdk.tms.shopee.model.firstmile.request.FirstMileWaybillRequest;
import com.sdk.tms.shopee.model.firstmile.request.GenerateAndBindFirstMileTrackingNumberRequest;
import com.sdk.tms.shopee.model.firstmile.request.GenerateFirstMileTrackingNumberRequest;
import com.sdk.tms.shopee.model.firstmile.request.UnbindFirstMileTrackingNumberAllRequest;
import com.sdk.tms.shopee.model.firstmile.request.UnbindFirstMileTrackingNumberRequest;
import com.sdk.tms.shopee.model.firstmile.response.BindFirstMileTrackingNumberOrder;
import com.sdk.tms.shopee.model.firstmile.response.BindFirstMileTrackingNumberResponse;
import com.sdk.tms.shopee.model.firstmile.response.CourierDeliveryChannelResponse;
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
import com.sdk.tms.shopee.model.firstmile.response.UnbindFirstMileTrackingNumberResponse;
import com.sdk.tms.shopee.model.logistics.response.ShopeeAddress;
import com.sdk.tms.shopee.model.logistics.response.ShopeeAddressListResponse;
import com.sdk.tms.shopee.model.merchant.request.MerchantPrepaidAccountRequest;
import com.sdk.tms.shopee.model.merchant.response.MerchantPrepaidAccount;
import com.sdk.tms.shopee.model.merchant.response.MerchantPrepaidAccountListResponse;
import com.sdk.tms.shopee.service.ShopeeLogisticsService;
import lombok.Data;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Shopee first-mile 组包预报适配器。
 */
@Component
public class ShopeePackageForecastAdapter implements PackageForecastPlatformAdapter {

    private static final String REGION_CN = "CN";
    private static final String PDF_PREFIX = "data:application/pdf;base64,";
    private static final String PAYMENT_PREPAID = "prepaid";
    private static final String PAYMENT_MONTHLY = "monthly";
    private static final String PAYMENT_MONTHLY_CN = "快递账号月结";

    @Resource
    private PackageForecastMapper packageForecastMapper;

    @Resource
    private PackageForecastDetailService packageForecastDetailService;

    @Resource
    private SoB2cFeign soB2cFeign;

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
    public boolean isForecast(List<String> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return false;
        }
        List<PackageForecastDetailEntity> detailList = packageForecastDetailService.listDbByMainIds(ids);
        if (CollectionUtils.isEmpty(detailList)) {
            return false;
        }
        List<String> soIds = detailList.stream().map(PackageForecastDetailEntity::getSoId).distinct().collect(Collectors.toList());
        List<SoB2cEntity> soList = soB2cFeign.listByIds(soIds);
        if (CollectionUtils.isEmpty(soList)) {
            return false;
        }
        long platformCount = soList.stream().map(SoB2cEntity::getDictPlatform).distinct().count();
        if (platformCount > 1 && soList.stream().anyMatch(item -> PlatformDictEnum.SHOPEE.getCode().equals(item.getDictPlatform()))) {
            throw new ServiceException("组包预报单明细数据平台不一致");
        }
        return soList.stream().allMatch(item -> PlatformDictEnum.SHOPEE.getCode().equals(item.getDictPlatform()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<BatchResultDTO> upload(PackageForecastDTO.UploadDTO dto) {
        ShopeeForecastContext context = buildContext(dto.getIds());
        validateUploadEntities(context);
        if (PackageForecastCollectModeEnum.SHOPEE_COURIER_DELIVERY.getCode().equals(dto.getCollectMode())) {
            return uploadCourierDelivery(dto, context);
        }
        return uploadFirstMile(dto, context);
    }

    public PackageForecastDTO.ShopeeCourierDeliveryOptionsDTO courierDeliveryOptions(PackageForecastDTO.ShopeeOptionParamDTO dto) {
        ShopeeForecastContext context = buildContext(dto.getIds());
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
        ShopeeForecastContext context = buildContext(dto.getIds());
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
        ShopeeForecastContext context = buildContext(dto.getIds());
        LocalDate declareDate = Objects.nonNull(dto.getDeclareDate()) ? dto.getDeclareDate() : LocalDate.now();
        FirstMileTrackingNumberListRequest request = FirstMileTrackingNumberListRequest.builder()
                .fromDate(declareDate.toString())
                .toDate(declareDate.toString())
                .pageSize(50)
                .build();
        FirstMileTrackingNumberListResponse response = shopeeLogisticsService.getTrackNumberList(buildBaseRequest(context.getShopId()), request);
        return toTrackingNumberDTOList(response);
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
        entity.setPrintStatus(PackagePrintStatusEnum.ALREADY.getCode());
        packageForecastMapper.updateById(entity);
        return withPdfPrefix(base64);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
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
            if (hasCourier) {
                cancelCourierDelivery(context);
            } else {
                cancelFirstMile(context);
            }
            context.getEntityList().forEach(this::resetAfterCancel);
            updateEntities(context.getEntityList());
            return context.getEntityList().stream()
                    .map(entity -> BatchResultDTO.success(entity.getId(), entity.getCode(), "取消上传"))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            context.getEntityList().forEach(entity -> {
                entity.setRemark("取消失败原因:" + e.getMessage());
                packageForecastMapper.updateById(entity);
            });
            return context.getEntityList().stream()
                    .map(entity -> BatchResultDTO.fail(entity.getId(), entity.getCode(), "取消上传失败:" + e.getMessage()))
                    .collect(Collectors.toList());
        }
    }

    @Override
    public void syncTrackingStatus(PackageForecastEntity entity) {
        if (Objects.isNull(entity) || StringUtils.isBlank(entity.getTransportNo())) {
            return;
        }
        String shopId = entity.getShopId();
        if (StringUtils.isBlank(shopId)) {
            ShopeeForecastContext context = buildContext(Collections.singletonList(entity.getId()));
            shopId = context.getShopId();
        }
        LocalDate fromDate = Objects.nonNull(entity.getBillDate()) ? entity.getBillDate() : LocalDate.now().minusMonths(3);
        LocalDate toDate = LocalDate.now();
        String cursor = null;
        do {
            FirstMileTrackingNumberListRequest request = FirstMileTrackingNumberListRequest.builder()
                    .fromDate(fromDate.toString())
                    .toDate(toDate.toString())
                    .pageSize(50)
                    .cursor(cursor)
                    .build();
            FirstMileTrackingNumberListResponse response = shopeeLogisticsService.getTrackNumberList(buildBaseRequest(shopId), request);
            if (Objects.isNull(response) || CollectionUtils.isEmpty(response.getFirstMileTrackingNumberList())) {
                return;
            }
            FirstMileTrackingNumber number = response.getFirstMileTrackingNumberList().stream()
                    .filter(item -> entity.getTransportNo().equals(item.getFirstMileTrackingNumber()))
                    .findFirst().orElse(null);
            if (Objects.nonNull(number)) {
                String status = mapShopeeHandoverStatus(number.getStatus());
                if (StringUtils.isNotBlank(status)) {
                    entity.setHandoverStatus(status);
                    packageForecastMapper.updateById(entity);
                }
                return;
            }
            cursor = response.getNextCursor();
            if (!Boolean.TRUE.equals(response.getMore())) {
                cursor = null;
            }
        } while (StringUtils.isNotBlank(cursor));
    }

    @Override
    public List<PackageForecastEntity> listSyncTrackingStatus(DateTime dateTime) {
        return packageForecastMapper.getShopeeHandoverList(dateTime);
    }

    private List<BatchResultDTO> uploadCourierDelivery(PackageForecastDTO.UploadDTO dto, ShopeeForecastContext context) {
        validateCourierDeliveryDTO(dto);
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
        GenerateAndBindFirstMileTrackingNumberResponse response =
                shopeeLogisticsService.generateAndBindFirstMileTrackingNumber(buildBaseRequest(context.getShopId()), request);

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
        String trackingNumber = dto.getFirstMileTrackingNumber();
        if (Boolean.TRUE.equals(dto.getGenerateNewTrackingNumber())) {
            GenerateFirstMileTrackingNumberRequest generateRequest = GenerateFirstMileTrackingNumberRequest.builder()
                    .declareDate(dto.getDeclareDate().toString())
                    .quantity(1)
                    .build();
            ValidatorUtil.validateEntity(generateRequest);
            GenerateFirstMileTrackingNumberResponse generateResponse =
                    shopeeLogisticsService.generateFirstMileTrackingNumber(buildBaseRequest(context.getShopId()), generateRequest);
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

        Map<String, String> failReasonMap = CollectionUtils.emptyIfNull(response.getOrderList()).stream()
                .filter(item -> StringUtils.isNotBlank(item.getFailError()) || StringUtils.isNotBlank(item.getFailMessage()))
                .collect(Collectors.toMap(item -> orderKey(item.getOrderSn(), item.getPackageNumber()),
                        this::buildFailReason, (left, right) -> left));
        Set<String> successKeys = CollectionUtils.emptyIfNull(response.getOrderList()).stream()
                .filter(item -> StringUtils.isBlank(item.getFailError()) && StringUtils.isBlank(item.getFailMessage()))
                .map(item -> orderKey(item.getOrderSn(), item.getPackageNumber()))
                .collect(Collectors.toSet());
        String finalTrackingNumber = StringUtils.defaultIfBlank(response.getFirstMileTrackingNumber(), trackingNumber);
        return updateUploadResult(context, successKeys, failReasonMap, entity -> {
            entity.setCollectMode(dto.getCollectMode());
            entity.setTransportNo(finalTrackingNumber);
            entity.setLogisticsSupplierName(PackageForecastCollectModeEnum.SHOPEE_SELF_DELIVER.getCode().equals(dto.getCollectMode())
                    ? "自行配送" : StringUtils.defaultIfBlank(dto.getLogisticsChannelName(), entity.getLogisticsSupplierName()));
            entity.setHandoverStatus(HandoverStatusEnum.SHOPEE_ORDER_CREATED.getCode());
        });
    }

    private List<BatchResultDTO> updateUploadResult(ShopeeForecastContext context, Set<String> successKeys,
                                                    Map<String, String> failReasonMap,
                                                    java.util.function.Consumer<PackageForecastEntity> successConsumer) {
        List<BatchResultDTO> resultList = new ArrayList<>();
        for (PackageForecastEntity entity : context.getEntityList()) {
            List<String> orderKeys = context.getForecastOrderKeyMap().get(entity.getId());
            List<String> failureReasons = orderKeys.stream()
                    .filter(failReasonMap::containsKey)
                    .map(failReasonMap::get)
                    .collect(Collectors.toList());
            boolean success = failureReasons.isEmpty() && (CollectionUtils.isEmpty(successKeys) || successKeys.containsAll(orderKeys));
            if (success) {
                successConsumer.accept(entity);
                entity.setUploadStatus(PackageUploadStatusEnum.UPLOAD_SUCCESS.getCode());
                entity.setRemark("");
                resultList.add(BatchResultDTO.success(entity.getId(), entity.getCode(), "上传成功"));
            } else {
                entity.setUploadStatus(PackageUploadStatusEnum.UPLOAD_FAILURE.getCode());
                entity.setRemark("上传失败:" + StringUtils.defaultIfBlank(String.join(";", failureReasons), "Shopee返回失败"));
                resultList.add(BatchResultDTO.fail(entity.getId(), entity.getCode(), entity.getRemark()));
            }
            packageForecastMapper.updateById(entity);
        }
        return resultList;
    }

    private String printCourierDelivery(PackageForecastEntity entity, String shopId) {
        if (StringUtils.isBlank(entity.getPlatformPackageNo())) {
            throw new ServiceException("虾皮快递寄送绑定ID为空");
        }
        CourierDeliveryWaybillRequest request = CourierDeliveryWaybillRequest.builder()
                .bindingIdList(Collections.singletonList(entity.getPlatformPackageNo()))
                .build();
        CourierDeliveryWaybillResponse response = shopeeLogisticsService.getCourierDeliveryWaybill(buildBaseRequest(shopId), request);
        String url = CollectionUtils.emptyIfNull(response.getWaybillList()).stream()
                .filter(item -> entity.getPlatformPackageNo().equals(item.getBindingId()))
                .map(CourierDeliveryWaybill::getShippingLabelUrl)
                .filter(StringUtils::isNotBlank)
                .findFirst()
                .orElseThrow(() -> new ServiceException("虾皮快递寄送交接面单为空"));
        try {
            return PdfUtil.convertPdfUrlToBase64(url, true);
        } catch (IOException e) {
            throw new ServiceException("虾皮快递寄送交接面单下载失败:" + e.getMessage());
        }
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

    private void cancelCourierDelivery(ShopeeForecastContext context) {
        UnbindFirstMileTrackingNumberAllRequest request = UnbindFirstMileTrackingNumberAllRequest.builder()
                .orderList(context.getOrderList())
                .build();
        UnbindFirstMileTrackingNumberAllResponse response =
                shopeeLogisticsService.unbindFirstMileTrackingNumberAll(buildBaseRequest(context.getShopId()), request);
        List<String> failureReasons = CollectionUtils.emptyIfNull(response.getFailList()).stream()
                .map(this::buildFailReasonWithOrder)
                .collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(failureReasons)) {
            throw new ServiceException(String.join(";", failureReasons));
        }
    }

    private void cancelFirstMile(ShopeeForecastContext context) {
        Map<String, List<FirstMileOrder>> trackingOrderMap = new HashMap<>();
        for (PackageForecastEntity entity : context.getEntityList()) {
            if (StringUtils.isBlank(entity.getTransportNo())) {
                throw new ServiceException("虾皮头程追踪号为空");
            }
            List<String> orderKeys = context.getForecastOrderKeyMap().get(entity.getId());
            List<FirstMileOrder> orderList = context.getOrderList().stream()
                    .filter(order -> orderKeys.contains(orderKey(order.getOrderSn(), order.getPackageNumber())))
                    .collect(Collectors.toList());
            trackingOrderMap.computeIfAbsent(entity.getTransportNo(), key -> new ArrayList<>()).addAll(orderList);
        }
        for (Map.Entry<String, List<FirstMileOrder>> entry : trackingOrderMap.entrySet()) {
            UnbindFirstMileTrackingNumberRequest request = UnbindFirstMileTrackingNumberRequest.builder()
                    .firstMileTrackingNumber(entry.getKey())
                    .orderList(entry.getValue())
                    .build();
            UnbindFirstMileTrackingNumberResponse response =
                    shopeeLogisticsService.unbindFirstMileTrackingNumber(buildBaseRequest(context.getShopId()), request);
            List<String> failureReasons = CollectionUtils.emptyIfNull(response.getOrderList()).stream()
                    .filter(item -> StringUtils.isNotBlank(item.getFailError()) || StringUtils.isNotBlank(item.getFailMessage()))
                    .map(this::buildFailReasonWithOrder)
                    .collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(failureReasons)) {
                throw new ServiceException(String.join(";", failureReasons));
            }
        }
    }

    private ShopeeForecastContext buildContext(List<String> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            throw new ServiceException("组包预报单不能为空");
        }
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
        List<SoB2cDetailEntity> soDetailList = soB2cFeign.listDetailByMainIds(soIds);
        Map<String, SoB2cEntity> soMap = soList.stream().collect(Collectors.toMap(SoB2cEntity::getId, Function.identity(), (left, right) -> left));
        Map<String, List<SoB2cDetailEntity>> soDetailMap = CollectionUtils.emptyIfNull(soDetailList).stream()
                .collect(Collectors.groupingBy(SoB2cDetailEntity::getMainId));
        LinkedHashMap<String, FirstMileOrder> orderMap = new LinkedHashMap<>();
        Map<String, List<String>> forecastOrderKeyMap = new HashMap<>();
        for (PackageForecastDetailEntity detail : detailList) {
            SoB2cEntity so = soMap.get(detail.getSoId());
            if (Objects.isNull(so) || StringUtils.isBlank(so.getPlatformCode())) {
                throw new ServiceException("销售订单:{}未获取Shopee平台单号", detail.getSoCode());
            }
            String packageNumber = CollectionUtils.emptyIfNull(soDetailMap.get(so.getId())).stream()
                    .map(SoB2cDetailEntity::getPlatformPackageId)
                    .filter(StringUtils::isNotBlank)
                    .findFirst()
                    .orElseThrow(() -> new ServiceException("销售订单:{}未获取Shopee平台包裹号", detail.getSoCode()));
            String key = orderKey(so.getPlatformCode(), packageNumber);
            orderMap.putIfAbsent(key, FirstMileOrder.builder().orderSn(so.getPlatformCode()).packageNumber(packageNumber).build());
            forecastOrderKeyMap.computeIfAbsent(detail.getMainId(), item -> new ArrayList<>()).add(key);
        }
        if (orderMap.size() > 50) {
            throw new ServiceException("Shopee组包下单单次最多支持50个订单");
        }
        ShopeeForecastContext context = new ShopeeForecastContext();
        context.setEntityList(entityList);
        context.setDetailList(detailList);
        context.setSoList(soList);
        context.setShopId(shopIds.get(0));
        context.setOrderList(new ArrayList<>(orderMap.values()));
        context.setForecastOrderKeyMap(forecastOrderKeyMap);
        return context;
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
        if (isPrepaidPayment(dto.getPaymentMode()) && Objects.isNull(dto.getPrepaidAccountId())) {
            throw new ServiceException("月结账号不能为空");
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
        if (Objects.isNull(shopAuthResult) || Objects.isNull(shopAuthResult.getData())) {
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

    private void resetAfterCancel(PackageForecastEntity entity) {
        entity.setUploadStatus(PackageUploadStatusEnum.WAIT.getCode());
        entity.setHandoverStatus("");
        entity.setTransportNo("");
        entity.setHandoverNo("");
        entity.setRemark("");
        entity.setPlatformPackageNo("");
        entity.setPrintStatus(PackagePrintStatusEnum.NOT.getCode());
    }

    private void updateEntities(List<PackageForecastEntity> entities) {
        for (PackageForecastEntity entity : entities) {
            packageForecastMapper.updateById(entity);
        }
    }

    private String mapShopeeHandoverStatus(String status) {
        if ("NOT_AVAILABLE".equals(status)) {
            return "";
        }
        if ("DELIVERED".equals(status)) {
            return HandoverStatusEnum.SHOPEE_DELIVERED.getCode();
        }
        return status;
    }

    private String orderKey(String orderSn, String packageNumber) {
        return StringUtils.defaultString(orderSn) + "|" + StringUtils.defaultString(packageNumber);
    }

    private String buildFailReason(FirstMileBindingFail fail) {
        return StringUtils.defaultIfBlank(StringUtils.defaultIfBlank(fail.getFailMessage(), fail.getFailError()), "Shopee返回失败");
    }

    private String buildFailReason(BindFirstMileTrackingNumberOrder fail) {
        return StringUtils.defaultIfBlank(StringUtils.defaultIfBlank(fail.getFailMessage(), fail.getFailError()), "Shopee返回失败");
    }

    private String buildFailReasonWithOrder(FirstMileBindingFail fail) {
        return buildOrderPrefix(fail.getOrderSn(), fail.getPackageNumber()) + buildFailReason(fail);
    }

    private String buildFailReasonWithOrder(UnbindFirstMileTrackingNumberOrder fail) {
        return buildOrderPrefix(fail.getOrderSn(), fail.getPackageNumber())
                + StringUtils.defaultIfBlank(StringUtils.defaultIfBlank(fail.getFailMessage(), fail.getFailError()), "Shopee返回失败");
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
        MerchantPrepaidAccountRequest request = MerchantPrepaidAccountRequest.builder()
                .host(shopBaseRequest.getHost())
                .accessToken(merchantAuth.getAccessToken())
                .partnerId(shopBaseRequest.getPartnerId())
                .partnerKey(shopBaseRequest.getPartnerKey())
                .merchantId(parseLong(merchantAuth.getShopeeId(), "Shopee merchantId"))
                .pageNo(1)
                .pageSize(100)
                .build();
        ValidatorUtil.validateEntity(request);
        MerchantPrepaidAccountListResponse response = shopeeLogisticsService.getMerchantPrepaidAccountList(request);
        if (Objects.isNull(response) || CollectionUtils.isEmpty(response.getList())) {
            return Collections.emptyList();
        }
        return response.getList().stream().map(this::toPrepaidAccountDTO).collect(Collectors.toList());
    }

    private ShopAuthEntity findMerchantAuth(String shopId) {
        ShopInfoEntity shopInfo = shopInfoFeign.getShopInfoById(shopId);
        ApiResult<List<ShopAuthEntity>> merchantAuthResult = shopInfoFeign.getShopListByParam(AuthTypeEnum.MERCHANT.getCode(),
                AuthStatusEnum.ALREADY.getCode(), PlatformDictEnum.SHOPEE.getCode());
        if (Objects.isNull(merchantAuthResult) || CollectionUtils.isEmpty(merchantAuthResult.getData())) {
            return null;
        }
        List<ShopInfoEntity> merchantShopList = shopInfoFeign.listShopInfoByIds(merchantAuthResult.getData().stream()
                .map(ShopAuthEntity::getShopId)
                .filter(StringUtils::isNotBlank)
                .distinct()
                .collect(Collectors.toList()));
        Map<String, ShopInfoEntity> merchantShopMap = CollectionUtils.emptyIfNull(merchantShopList).stream()
                .collect(Collectors.toMap(ShopInfoEntity::getId, Function.identity(), (left, right) -> left));
        return merchantAuthResult.getData().stream()
                .filter(item -> StringUtils.isNotBlank(item.getAccessToken()) && StringUtils.isNotBlank(item.getShopeeId()))
                .filter(item -> hasSameAccount(shopInfo, merchantShopMap.get(item.getShopId())))
                .findFirst()
                .orElse(null);
    }

    private boolean hasSameAccount(ShopInfoEntity shopInfo, ShopInfoEntity merchantShop) {
        if (Objects.isNull(shopInfo) || Objects.isNull(merchantShop)) {
            return false;
        }
        return StringUtils.isNotBlank(shopInfo.getAccount()) && shopInfo.getAccount().equals(merchantShop.getAccount());
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

    private List<PackageForecastDTO.ShopeeTrackingNumberDTO> toTrackingNumberDTOList(FirstMileTrackingNumberListResponse response) {
        if (Objects.isNull(response) || CollectionUtils.isEmpty(response.getFirstMileTrackingNumberList())) {
            return Collections.emptyList();
        }
        return response.getFirstMileTrackingNumberList().stream()
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
}
