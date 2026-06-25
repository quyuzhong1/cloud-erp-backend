package com.erp.server.wms.service.adapter;

import cn.hutool.core.date.DateTime;
import com.alibaba.fastjson.JSONObject;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.enums.PlatformDictEnum;
import com.common.core.controller.vo.ApiResult;
import com.common.core.exception.ServiceException;
import com.erp.model.dmp.dto.CfgAppClientDTO;
import com.erp.model.dmp.entity.CfgAppClientEntity;
import com.erp.model.dmp.enums.AppClientEnum;
import com.erp.model.oms.entity.ShopAuthEntity;
import com.erp.model.oms.entity.SoB2cEntity;
import com.erp.model.oms.entity.SoB2cLogisticsEntity;
import com.erp.model.tms.entity.LogisticsAddressEntity;
import com.erp.model.wms.dto.PackageForecastDTO;
import com.erp.model.wms.entity.PackageForecastDetailEntity;
import com.erp.model.wms.entity.PackageForecastEntity;
import com.erp.model.wms.enums.PackageForecastCollectModeEnum;
import com.erp.model.wms.enums.PackagePrintStatusEnum;
import com.erp.model.wms.enums.PackageUploadStatusEnum;
import com.erp.rpc.dmp.feign.DmpTaskFeign;
import com.erp.rpc.oms.feign.ShopInfoFeign;
import com.erp.rpc.tms.feign.LogisticsFeign;
import com.erp.server.wms.constant.PackageForecastConstant;
import com.erp.server.wms.convert.PackageForecastConverter;
import com.erp.tms.aliexpress.api.IopResponse;
import com.erp.tms.aliexpress.model.handover.AddressBase;
import com.erp.tms.aliexpress.model.handover.AddressInfo;
import com.erp.tms.aliexpress.model.handover.ParcelOrder;
import com.erp.tms.aliexpress.model.handover.SellerParcelOrder;
import com.erp.tms.aliexpress.model.handover.UserInfo;
import com.erp.tms.aliexpress.model.handover.request.CancelRequest;
import com.erp.tms.aliexpress.model.handover.request.CommitRequest;
import com.erp.tms.aliexpress.model.handover.request.HandoverQueryRequest;
import com.erp.tms.aliexpress.model.handover.request.PdfRequest;
import com.erp.tms.aliexpress.model.handover.response.BaseResponse;
import com.erp.tms.aliexpress.model.handover.response.HandoverCommitResult;
import com.erp.tms.aliexpress.model.handover.response.HandoverQueryResponse;
import com.erp.tms.aliexpress.model.handover.response.PdfResponse;
import com.erp.tms.aliexpress.model.order.response.BaseResult;
import com.erp.tms.aliexpress.model.order.response.ErrorResponse;
import com.erp.tms.aliexpress.service.AliExpressHandoverService;
import com.erp.tms.aliexpress.util.ApiException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.Resource;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * 速卖通组包预报适配器。
 */
@Slf4j
@Component
public class AliExpressPackageForecastAdapter extends AbstractPackageForecastPlatformAdapter {

    @Resource
    private LogisticsFeign logisticsFeign;

    @Resource
    private DmpTaskFeign dmpTaskFeign;

    @Resource
    private ShopInfoFeign shopInfoFeign;

    @Resource
    private AliExpressHandoverService aliExpressHandoverService;

    @Resource(name = "packAsyncExecutor")
    private ThreadPoolTaskExecutor packAsyncExecutor;

    @Override
    public String platform() {
        return PlatformDictEnum.ALI_EXPRESS.getCode();
    }

    @Override
    public List<BatchResultDTO> upload(PackageForecastDTO.UploadDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        for (String id : dto.getIds()) {
            try {
                resultDTOS.add(uploadOne(id, dto.getCollectMode(), dto.getCollectAddressId()));
            } catch (Exception e) {
                log.error("组包预报上传失败>>>>>", e);
                PackageForecastEntity entity = packageForecastMapper.selectById(id);
                if (Objects.isNull(entity)) {
                    resultDTOS.add(BatchResultDTO.fail(id, id, "组包预报单不存在, 上传失败"));
                } else {
                    resultDTOS.add(BatchResultDTO.fail(entity.getId(), entity.getCode(), userFailureMessage("上传")));
                }
            }
        }
        return resultDTOS;
    }

    private BatchResultDTO uploadOne(String id, String collectMode, String collectAddressId) {
        PackageForecastEntity entity = getForecastOrThrow(id);
        validateUploadable(entity);
        try {
            entity.setCollectMode(collectMode);
            entity.setCollectAddressId(collectAddressId);
            uploadAliExpress(entity, collectAddressId);
            return BatchResultDTO.success(entity.getId(), entity.getCode(), "上传成功");
        } catch (Exception e) {
            if (hasAliExpressPlatformIdentifiers(entity)) {
                try {
                    entity.setUploadStatus(PackageUploadStatusEnum.UPLOAD_SUCCESS.getCode());
                    entity.setPlatformNo(buildPlatformNo(entity.getHandoverNo(), entity.getPlatformPackageNo()));
                    queryAliExpressInfo(entity);
                    return BatchResultDTO.success(entity.getId(), entity.getCode(), "上传成功");
                } catch (Exception syncException) {
                    log.error("速卖通组包预报上传失败后按平台交接单回查补偿失败, id: {}, code: {}, handoverNo: {}, platformPackageNo: {}",
                            entity.getId(), entity.getCode(), entity.getHandoverNo(), entity.getPlatformPackageNo(), syncException);
                }
            }
            entity.setUploadStatus(PackageUploadStatusEnum.UPLOAD_FAILURE.getCode());
            entity.setRemark("上传失败:" + e.getMessage());
            try {
                updateForecastOrThrow(entity);
            } catch (Exception updateException) {
                log.error("组包预报上传失败后更新失败状态失败, id: {}, code: {}", entity.getId(), entity.getCode(), updateException);
            }
            log.error("组包预报上传失败>>>>>", e);
            return BatchResultDTO.fail(entity.getId(), entity.getCode(), userFailureMessage("上传"));
        }
    }

    private boolean hasAliExpressPlatformIdentifiers(PackageForecastEntity entity) {
        return StringUtils.isNotBlank(entity.getHandoverNo())
                && StringUtils.isNotBlank(entity.getPlatformPackageNo());
    }

    private String userFailureMessage(String operation) {
        return operation + "失败，请查看单据备注或日志";
    }

    @Override
    public String print(String id) {
        PackageForecastEntity entity = getForecastOrThrow(id);
        validatePrintable(entity);
        String base64 = aliExpressPrint(entity);
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
                aliExpressCancel(entity);
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
                        log.error("组包预报取消失败后更新失败原因失败, id: {}, code: {}", entity.getId(), entity.getCode(), updateException);
                    }
                    resultDTOS.add(BatchResultDTO.fail(entity.getId(), entity.getCode(), userFailureMessage("取消上传")));
                } else {
                    resultDTOS.add(BatchResultDTO.fail(id, id, userFailureMessage("取消上传")));
                }
            }
        }
        return resultDTOS;
    }

    @Override
    public List<PackageForecastEntity> listSyncTrackingStatus(DateTime dateTime) {
        return packageForecastMapper.getAliExpressHandoverList(dateTime);
    }

    @Override
    public void syncTrackingStatus(PackageForecastEntity entity) {
        queryAliExpressInfo(entity);
    }

    public PackageForecastDTO.AlExpressHandoverBaseDTO getAlExpressHandoverBase(String logisticsPlatform, String shopId) {
        PackageForecastDTO.AlExpressHandoverBaseDTO alExpressHandoverBaseDTO = new PackageForecastDTO.AlExpressHandoverBaseDTO();
        CfgAppClientDTO.FindDTO findDTO = new CfgAppClientDTO.FindDTO();
        AppClientEnum appClientEnum = AppClientEnum.ALI_EXPRESS_TOKEN;
        findDTO.setBusinessType(appClientEnum.getBusinessType());
        findDTO.setDictPlatform(logisticsPlatform);
        findDTO.setPlatformType(appClientEnum.getPlatformType());
        CfgAppClientEntity cfgAppClient = dmpTaskFeign.getCfgAppClient(findDTO);
        if (Objects.isNull(cfgAppClient)) {
            throw new ServiceException("未找到对应平台");
        }
        ApiResult<List<ShopAuthEntity>> shopAuthResult = shopInfoFeign.getAuthShopByPlatformType(logisticsPlatform);
        if (Objects.isNull(shopAuthResult) || !shopAuthResult.isSuccess()) {
            throw new ServiceException("获取店铺token失败");
        }
        if (CollectionUtils.isEmpty(shopAuthResult.getData())) {
            throw new ServiceException("获取店铺token为空");
        }
        String sellerIdFlag = PackageForecastConstant.SELLER_ID;
        List<ShopAuthEntity> shopAuthList = shopAuthResult.getData().stream()
                .filter(s -> StringUtils.isNotBlank(s.getExtendData())
                        && s.getExtendData().contains(sellerIdFlag)
                        && shopId.equals(s.getShopId()))
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(shopAuthList)) {
            throw new ServiceException("获取店铺速卖通卖家id失败");
        }
        ShopAuthEntity shopAuthEntity = shopAuthList.get(0);
        if (StringUtils.isBlank(shopAuthEntity.getToken())) {
            throw new ServiceException("获取店铺token为空");
        }
        Map<String, String> authMap = new HashMap<>(4);
        authMap.put("clientId", cfgAppClient.getClientId());
        authMap.put("clientSecret", cfgAppClient.getClientSecret());
        authMap.put("token", shopAuthEntity.getToken());
        JSONObject jsonObject = JSONObject.parseObject(shopAuthEntity.getExtendData());
        String sellerId = jsonObject.getString("sellerId");
        if (StringUtils.isBlank(sellerId)) {
            throw new ServiceException("获取店铺速卖通卖家id失败");
        }
        UserInfo userInfo = UserInfo.builder().topUserKey(sellerId).build();
        alExpressHandoverBaseDTO.setClient(PackageForecastConstant.CLIENT);
        alExpressHandoverBaseDTO.setAuthMap(authMap);
        alExpressHandoverBaseDTO.setUserInfo(userInfo);
        alExpressHandoverBaseDTO.setLocale("zh_CN");
        return alExpressHandoverBaseDTO;
    }

    public void syncAliExpressInfo(PackageForecastEntity packageForecastEntity) {
        queryAliExpressInfo(packageForecastEntity);
    }

    public void queryAliExpressInfo(PackageForecastEntity packageForecastEntity) {
        PackageForecastDTO.AlExpressHandoverBaseDTO alExpressHandoverBase = null;
        try {
            List<PackageForecastDetailEntity> forecastDetailList = packageForecastDetailService.listDbByMainId(packageForecastEntity.getId());
            String shopId = resolveSingleShopId(forecastDetailList);
            alExpressHandoverBase = this.getAlExpressHandoverBase(platform(), shopId);
        } catch (Exception e) {
            log.error("syncPackageForecastInfo build base error, id: {}, handoverNo: {}",
                    packageForecastEntity.getId(), packageForecastEntity.getHandoverNo(), e);
            throw new ServiceException("速卖通状态同步初始化失败:" + e.getMessage());
        }
        if (Objects.isNull(alExpressHandoverBase)) {
            throw new ServiceException("速卖通状态同步授权信息为空");
        }
        HandoverQueryRequest handoverQueryRequest = HandoverQueryRequest.builder()
                .client(alExpressHandoverBase.getClient())
                .locale("zh_CN")
                .orderCode(packageForecastEntity.getHandoverNo())
                .userInfo(alExpressHandoverBase.getUserInfo())
                .build();
        try {
            IopResponse response = aliExpressHandoverService.queryContent(alExpressHandoverBase.getAuthMap(), handoverQueryRequest);
            if (StringUtils.isEmpty(response.getBody())) {
                throw new ServiceException("速卖通状态同步响应为空");
            }
            BaseResponse baseResponse = JSONObject.parseObject(response.getBody(), BaseResponse.class);
            if (StringUtils.isEmpty(baseResponse.getResult())) {
                throw new ServiceException("速卖通状态同步result为空");
            }
            BaseResult baseResult = JSONObject.parseObject(baseResponse.getResult(), BaseResult.class);
            if (StringUtils.isEmpty(baseResult.getData())) {
                throw new ServiceException("速卖通状态同步data为空");
            }
            HandoverQueryResponse queryResponse = JSONObject.parseObject(baseResult.getData(), HandoverQueryResponse.class);
            List<ParcelOrder> parcelOrderList = queryResponse.getParcelOrderList();
            new TransactionTemplate(transactionManager).execute(status -> {
                packageForecastEntity.setHandoverStatus(queryResponse.getStatus());
                packageForecastEntity.setTransportNo(queryResponse.getTrackingNumber());
                updateForecastOrThrow(packageForecastEntity);
                if (CollectionUtils.isNotEmpty(parcelOrderList)) {
                    batchUpdateDetailStatus(packageForecastEntity.getId(), parcelOrderList);
                }
                return null;
            });
        } catch (ApiException e) {
            log.error("接口调用异常记录：{}", e.getErrorMessage());
            throw new ServiceException("速卖通状态同步接口异常:" + e.getErrorMessage());
        }
    }

    private void aliExpressCancel(PackageForecastEntity entity) {
        List<PackageForecastDetailEntity> forecastDetailList = packageForecastDetailService.listDbByMainId(entity.getId());
        String shopId = resolveSingleShopId(forecastDetailList);
        PackageForecastDTO.AlExpressHandoverBaseDTO base = this.getAlExpressHandoverBase(platform(), shopId);
        Long platformPackageNo = parseAliExpressPlatformPackageNo(entity);
        CancelRequest cancelRequest = CancelRequest.builder()
                .userInfo(base.getUserInfo())
                .client(base.getClient())
                .handoverContentId(platformPackageNo)
                .build();
        try {
            IopResponse iopResponse = aliExpressHandoverService.cancel(base.getAuthMap(), cancelRequest);
            BaseResult baseResult = parseAliExpressBaseResult(iopResponse, "取消上传交接单");
            ErrorResponse errorResponse = baseResult.getErrorResponse();
            if (Objects.nonNull(errorResponse)) {
                throw new ServiceException(errorResponse.getSubMsg());
            }
        } catch (ApiException e) {
            log.error("取消上传交接单失败>>>>>>{}", e);
            throw new ServiceException(e.getMessage());
        }
    }

    private BatchResultDTO uploadAliExpress(PackageForecastEntity entity, String collectAddressId) {
        if (StringUtils.isBlank(collectAddressId)) {
            throw new ServiceException("揽收地址不能为空");
        }
        LogisticsAddressEntity addressEntity = logisticsFeign.getLogisticsAddressById(collectAddressId);
        if (Objects.isNull(addressEntity)) {
            throw new ServiceException("揽收地址不存在");
        }
        String addressName = addressEntity.getName();
        entity.setCollectAddress(addressName);
        boolean hasHandoverNo = StringUtils.isNotBlank(entity.getHandoverNo());
        boolean hasPlatformPackageNo = StringUtils.isNotBlank(entity.getPlatformPackageNo());
        if (!hasHandoverNo && !hasPlatformPackageNo) {
            addBigPackage(entity, addressEntity);
        } else if (hasHandoverNo && hasPlatformPackageNo) {
            throw new ServiceException("速卖通组包预报已存在平台交接单信息，请先同步状态或人工处理后再重试");
        } else {
            throw new ServiceException("速卖通组包预报已存在部分平台交接单信息，请先同步状态或人工处理后再重试");
        }
        entity.setUploadStatus(PackageUploadStatusEnum.UPLOAD_SUCCESS.getCode());
        entity.setPlatformNo(buildPlatformNo(entity.getHandoverNo(), entity.getPlatformPackageNo()));
        updateForecastOrThrow(entity);
        asyncSyncAfterCommit(entity);
        return BatchResultDTO.success(entity.getId(), entity.getCode(), "上传成功");
    }

    private void asyncSyncAfterCommit(PackageForecastEntity entity) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            submitAsyncSync(entity);
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
            @Override
            public void afterCommit() {
                submitAsyncSync(entity);
            }
        });
    }

    private void submitAsyncSync(PackageForecastEntity entity) {
        CompletableFuture.runAsync(() -> {
            try {
                syncAliExpressInfo(entity);
            } catch (Exception e) {
                log.error("速卖通组包状态异步同步失败, id: {}, code: {}, handoverNo: {}",
                        entity.getId(), entity.getCode(), entity.getHandoverNo(), e);
                markAsyncSyncFailed(entity.getId(), e.getMessage());
            }
        }, packAsyncExecutor);
    }

    private void markAsyncSyncFailed(String id, String message) {
        try {
            PackageForecastEntity entity = packageForecastMapper.selectById(id);
            if (Objects.isNull(entity)) {
                return;
            }
            entity.setRemark("速卖通状态同步失败:" + StringUtils.defaultString(message));
            updateForecastOrThrow(entity);
        } catch (Exception e) {
            log.error("速卖通组包状态异步同步失败后更新备注失败, id: {}", id, e);
        }
    }

    private String aliExpressPrint(PackageForecastEntity entity) {
        List<PackageForecastDetailEntity> forecastDetailList = packageForecastDetailService.listDbByMainId(entity.getId());
        String shopId = resolveSingleShopId(forecastDetailList);
        PackageForecastDTO.AlExpressHandoverBaseDTO base = getAlExpressHandoverBase(platform(), shopId);
        Long platformPackageNo = parseAliExpressPlatformPackageNo(entity);
        PdfRequest pdfRequest = PdfRequest.builder()
                .client(base.getClient())
                .handoverContentId(platformPackageNo)
                .locale("zh_CN")
                .type(1)
                .userInfo(base.getUserInfo())
                .build();
        try {
            IopResponse response = aliExpressHandoverService.getPdf(base.getAuthMap(), pdfRequest);
            BaseResult baseResult = parseAliExpressNestedBaseResult(response, "打印交接单");
            if (StringUtils.isNotEmpty(baseResult.getErrorMsg()) || StringUtils.isEmpty(baseResult.getData())) {
                throw new ServiceException(baseResult.getErrorMsg());
            }
            PdfResponse pdfResponse = JSONObject.parseObject(baseResult.getData(), PdfResponse.class);
            if (Objects.isNull(pdfResponse) || StringUtils.isBlank(pdfResponse.getBody())) {
                throw new ServiceException("速卖通打印交接单响应面单为空");
            }
            return "data:application/pdf;base64," + pdfResponse.getBody();
        } catch (ApiException e) {
            log.error("速卖通打印失败>>>{}", e);
            throw new ServiceException(e.getMessage());
        }
    }

    private void addBigPackage(PackageForecastEntity entity, LogisticsAddressEntity logisticsAddress) {
        List<PackageForecastDetailEntity> forecastDetailList = packageForecastDetailService.listDbByMainId(entity.getId());
        String shopId = resolveSingleShopId(forecastDetailList);
        PackageForecastDTO.AlExpressHandoverBaseDTO base = getAlExpressHandoverBase(platform(), shopId);

        List<String> soIdList = forecastDetailList.stream().map(PackageForecastDetailEntity::getSoId).collect(Collectors.toList());
        Map<String, String> authMap = base.getAuthMap();
        List<SoB2cLogisticsEntity> soB2cLogisticsList = soB2cFeign.listSoB2cLogisticsByMainIdList(soIdList);
        if (CollectionUtils.isEmpty(soB2cLogisticsList)) {
            throw new ServiceException("未获取到销售订单物流信息");
        }
        List<String> orderCodeList = soB2cLogisticsList.stream()
                .map(SoB2cLogisticsEntity::getCode)
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.toList());
        if (orderCodeList.size() != forecastDetailList.size()) {
            throw new ServiceException("未获取到小包第三方交易号");
        }
        String topUserKey = base.getUserInfo().getTopUserKey();
        SellerParcelOrder parcelOrder = new SellerParcelOrder();
        parcelOrder.setSellerId(topUserKey);
        parcelOrder.setOrderCodeList(orderCodeList);

        AddressBase addressBase = PackageForecastConverter.INSTANCE.convertAddressBase(logisticsAddress);
        AddressInfo addressInfo = PackageForecastConverter.INSTANCE.convertAddressInfo(logisticsAddress);
        addressInfo.setAddress(addressBase);
        String type = PackageForecastConstant.CAINIAO_PICKUP;
        if (PackageForecastCollectModeEnum.SELF_SEND.getCode().equals(entity.getCollectMode())) {
            type = PackageForecastConstant.SELF_SEND;
        }
        CommitRequest commitRequest = CommitRequest.builder().pickInfo(addressInfo)
                .skipInvalidParcel(Boolean.FALSE)
                .orderCodeList(orderCodeList)
                .handoverOrderId("")
                .appointmentType("bigbag")
                .weight(entity.getTotalPackageWeight().setScale(0, RoundingMode.HALF_UP))
                .weightUnit(entity.getWeightUnit())
                .userInfo(base.getUserInfo())
                .sellerParcelOrderList(Collections.singletonList(parcelOrder))
                .type(type)
                .client(PackageForecastConstant.CLIENT)
                .locale(base.getLocale())
                .build();
        try {
            IopResponse iopResponse = aliExpressHandoverService.commit(authMap, commitRequest);
            if (Objects.isNull(iopResponse) || StringUtils.isBlank(iopResponse.getBody())) {
                throw new ServiceException("速卖通提交交接单响应为空");
            }
            BaseResult baseResult = JSONObject.parseObject(iopResponse.getBody(), BaseResult.class);
            if (Objects.isNull(baseResult)) {
                throw new ServiceException("速卖通提交交接单响应为空");
            }
            if (Objects.nonNull(baseResult.getErrorResponse())) {
                throw new ServiceException(baseResult.getErrorResponse().getSubMsg());
            }
            if (StringUtils.isBlank(baseResult.getResult())) {
                throw new ServiceException(StringUtils.defaultIfBlank(baseResult.getErrorMsg(), "速卖通提交交接单响应为空"));
            }
            HandoverCommitResult handoverCommitResult = JSONObject.parseObject(baseResult.getResult(), HandoverCommitResult.class);
            if (Objects.isNull(handoverCommitResult) || !Boolean.TRUE.equals(handoverCommitResult.getSuccess())) {
                throw new ServiceException(StringUtils.defaultIfBlank(baseResult.getErrorMsg(), "速卖通提交交接单失败"));
            }
            if (Objects.isNull(handoverCommitResult.getResponse())) {
                throw new ServiceException("速卖通提交交接单响应为空");
            }
            entity.setHandoverNo(handoverCommitResult.getResponse().getHandoverContentCode());
            entity.setPlatformPackageNo(String.valueOf(handoverCommitResult.getResponse().getHandoverContentId()));
            entity.setRemark("");
        } catch (ApiException e) {
            log.error("创建交接单失败>>>>>>{}", e);
            throw new ServiceException(e.getMessage());
        }
    }

    private String resolveSingleShopId(List<PackageForecastDetailEntity> forecastDetailList) {
        if (CollectionUtils.isEmpty(forecastDetailList)) {
            throw new ServiceException("组包预报单明细未找到");
        }
        List<String> soIds = forecastDetailList.stream()
                .map(PackageForecastDetailEntity::getSoId)
                .filter(StringUtils::isNotBlank)
                .distinct()
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(soIds)) {
            throw new ServiceException("销售订单未找到");
        }
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
            throw new ServiceException("速卖通不支持多店铺组包预报");
        }
        return shopIds.get(0);
    }

    private void batchUpdateDetailStatus(String mainId, List<ParcelOrder> parcelOrderList) {
        Map<String, String> statusMap = parcelOrderList.stream()
                .filter(parcelOrder -> StringUtils.isNotBlank(parcelOrder.getOrderCode())
                        && StringUtils.isNotBlank(parcelOrder.getStatus()))
                .collect(Collectors.toMap(ParcelOrder::getOrderCode, ParcelOrder::getStatus, (oldValue, newValue) -> newValue));
        if (statusMap.isEmpty()) {
            return;
        }
        List<PackageForecastDetailEntity> detailList = packageForecastDetailService.lambdaQuery()
                .eq(PackageForecastDetailEntity::getMainId, mainId)
                .in(PackageForecastDetailEntity::getSourceCode, statusMap.keySet())
                .eq(PackageForecastDetailEntity::getIsDeleted, false)
                .list();
        if (CollectionUtils.isEmpty(detailList)) {
            return;
        }
        detailList.forEach(detail -> detail.setHandoverStatus(statusMap.get(detail.getSourceCode())));
        packageForecastDetailService.updateBatchById(detailList);
    }

    private Long parseAliExpressPlatformPackageNo(PackageForecastEntity entity) {
        String platformPackageNo = entity.getPlatformPackageNo();
        if (StringUtils.isBlank(platformPackageNo) || !StringUtils.isNumeric(platformPackageNo)) {
            throw new ServiceException("速卖通平台交接单ID为空或格式错误");
        }
        return Long.valueOf(platformPackageNo);
    }

    private BaseResult parseAliExpressBaseResult(IopResponse response, String scene) {
        if (Objects.isNull(response) || StringUtils.isBlank(response.getBody())) {
            throw new ServiceException("速卖通" + scene + "响应为空");
        }
        BaseResult baseResult = JSONObject.parseObject(response.getBody(), BaseResult.class);
        if (Objects.isNull(baseResult)) {
            throw new ServiceException("速卖通" + scene + "响应解析为空");
        }
        return baseResult;
    }

    private BaseResult parseAliExpressNestedBaseResult(IopResponse response, String scene) {
        if (Objects.isNull(response) || StringUtils.isBlank(response.getBody())) {
            throw new ServiceException("速卖通" + scene + "响应为空");
        }
        BaseResponse baseResponse = JSONObject.parseObject(response.getBody(), BaseResponse.class);
        if (Objects.isNull(baseResponse) || StringUtils.isBlank(baseResponse.getResult())) {
            throw new ServiceException("速卖通" + scene + "result为空");
        }
        BaseResult baseResult = JSONObject.parseObject(baseResponse.getResult(), BaseResult.class);
        if (Objects.isNull(baseResult)) {
            throw new ServiceException("速卖通" + scene + "result解析为空");
        }
        return baseResult;
    }

    private String buildPlatformNo(String handoverNo, String platformPackageNo) {
        if (StringUtils.isBlank(handoverNo) && StringUtils.isBlank(platformPackageNo)) {
            return "";
        }
        return StringUtils.defaultString(handoverNo) + "/" + StringUtils.defaultString(platformPackageNo);
    }
}
