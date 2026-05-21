package com.erp.server.wms.service.adapter;

import cn.hutool.core.date.DateTime;
import com.alibaba.fastjson.JSONObject;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.enums.PlatformDictEnum;
import com.common.business.utils.PdfUtil;
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
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;

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
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
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
                    resultDTOS.add(BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage()));
                }
            }
        }
        return resultDTOS;
    }

    private BatchResultDTO uploadOne(String id, String collectMode, String collectAddressId) {
        PackageForecastEntity entity = getForecastOrThrow(id);
        validateUploadable(entity);
        try {
            entity.setUploadStatus(PackageUploadStatusEnum.UPLOAD_SUCCESS.getCode());
            entity.setCollectMode(collectMode);
            entity.setCollectAddressId(collectAddressId);
            uploadAliExpress(entity, collectAddressId);
            return BatchResultDTO.success(entity.getId(), entity.getCode(), "上传成功");
        } catch (Exception e) {
            entity.setUploadStatus(PackageUploadStatusEnum.UPLOAD_FAILURE.getCode());
            entity.setRemark("上传失败:" + e.getMessage());
            packageForecastMapper.updateById(entity);
            log.error("组包预报上传失败>>>>>", e);
            return BatchResultDTO.fail(entity.getId(), entity.getCode(), "上传失败" + e.getMessage());
        }
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
        packageForecastMapper.updateById(entity);
        return base64;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
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
                packageForecastMapper.updateById(entity);
                resultDTOS.add(BatchResultDTO.success(entity.getId(), entity.getCode(), "取消上传"));
            } catch (Exception e) {
                log.error("取消上传失败>>>>", e);
                if (Objects.nonNull(entity)) {
                    entity.setRemark("取消失败原因:" + e.getMessage());
                    packageForecastMapper.updateById(entity);
                    resultDTOS.add(BatchResultDTO.fail(entity.getId(), entity.getCode(), "取消上传失败:" + e.getMessage()));
                } else {
                    resultDTOS.add(BatchResultDTO.fail(id, id, e.getMessage()));
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
        ApiResult<List<ShopAuthEntity>> shopAuthResult = shopInfoFeign.getAuthShopByPlatformType(logisticsPlatform);
        if (!shopAuthResult.isSuccess()) {
            throw new ServiceException("获取店铺token失败");
        }
        String sellerIdFlag = PackageForecastConstant.SELLER_ID;
        List<ShopAuthEntity> shopAuthList = shopAuthResult.getData().stream()
                .filter(s -> s.getExtendData().contains(sellerIdFlag) && shopId.equals(s.getShopId()))
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(shopAuthList)) {
            throw new ServiceException("获取店铺速卖通卖家id失败");
        }
        if (Objects.isNull(cfgAppClient)) {
            throw new ServiceException("未找到对应平台");
        }
        ShopAuthEntity shopAuthEntity = shopAuthList.get(0);
        Map<String, String> authMap = new HashMap<>(4);
        authMap.put("clientId", cfgAppClient.getClientId());
        authMap.put("clientSecret", cfgAppClient.getClientSecret());
        authMap.put("token", shopAuthEntity.getToken());
        JSONObject jsonObject = JSONObject.parseObject(shopAuthEntity.getExtendData());
        String sellerId = jsonObject.getString("sellerId");
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
            List<String> soIds = forecastDetailList.stream().map(PackageForecastDetailEntity::getSoId).distinct().collect(Collectors.toList());
            List<SoB2cEntity> soB2cEntityList = soB2cFeign.listByIds(soIds);
            List<String> shopIds = soB2cEntityList.stream().map(SoB2cEntity::getShopId).collect(Collectors.toList());
            if (CollectionUtils.isEmpty(shopIds)) {
                throw new ServiceException("销售订单店铺未找到");
            }
            alExpressHandoverBase = this.getAlExpressHandoverBase(platform(), shopIds.get(0));
        } catch (Exception e) {
            log.error("syncPackageForecastInfo error : {}", e.getMessage());
        }
        if (Objects.isNull(alExpressHandoverBase)) {
            return;
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
                return;
            }
            BaseResponse baseResponse = JSONObject.parseObject(response.getBody(), BaseResponse.class);
            if (StringUtils.isEmpty(baseResponse.getResult())) {
                return;
            }
            BaseResult baseResult = JSONObject.parseObject(baseResponse.getResult(), BaseResult.class);
            if (StringUtils.isEmpty(baseResult.getData())) {
                return;
            }
            HandoverQueryResponse queryResponse = JSONObject.parseObject(baseResult.getData(), HandoverQueryResponse.class);
            packageForecastEntity.setHandoverStatus(queryResponse.getStatus());
            packageForecastEntity.setTransportNo(queryResponse.getTrackingNumber());
            packageForecastMapper.updateById(packageForecastEntity);
            List<ParcelOrder> parcelOrderList = queryResponse.getParcelOrderList();
            if (CollectionUtils.isEmpty(parcelOrderList)) {
                return;
            }
            parcelOrderList.forEach(parcelOrder -> {
                packageForecastDetailService.updateStatusByOrderCode(parcelOrder.getOrderCode(), parcelOrder.getStatus());
            });
        } catch (ApiException e) {
            log.error("接口调用异常记录：{}", e.getErrorMessage());
        }
    }

    private void aliExpressCancel(PackageForecastEntity entity) {
        List<PackageForecastDetailEntity> forecastDetailList = packageForecastDetailService.listDbByMainId(entity.getId());
        List<String> soIds = forecastDetailList.stream().map(PackageForecastDetailEntity::getSoId).distinct().collect(Collectors.toList());
        List<SoB2cEntity> soB2cEntityList = soB2cFeign.listByIds(soIds);
        List<String> shopIds = soB2cEntityList.stream().map(SoB2cEntity::getShopId).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(shopIds)) {
            throw new ServiceException("销售订单店铺未找到");
        }
        PackageForecastDTO.AlExpressHandoverBaseDTO base = this.getAlExpressHandoverBase(platform(), shopIds.get(0));
        CancelRequest cancelRequest = CancelRequest.builder()
                .userInfo(base.getUserInfo())
                .client(base.getClient())
                .handoverContentId(Long.valueOf(entity.getPlatformPackageNo()))
                .build();
        try {
            IopResponse iopResponse = aliExpressHandoverService.cancel(base.getAuthMap(), cancelRequest);
            BaseResult baseResult = JSONObject.parseObject(iopResponse.getBody(), BaseResult.class);
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
        addBigPackage(entity, addressEntity);
        asyncSyncAfterCommit(entity);
        packageForecastMapper.updateById(entity);
        return BatchResultDTO.success(entity.getId(), entity.getCode(), "上传成功");
    }

    private void asyncSyncAfterCommit(PackageForecastEntity entity) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            CompletableFuture.runAsync(() -> this.syncAliExpressInfo(entity), packAsyncExecutor);
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
            @Override
            public void afterCommit() {
                CompletableFuture.runAsync(() -> syncAliExpressInfo(entity), packAsyncExecutor);
            }
        });
    }

    private String aliExpressPrint(PackageForecastEntity entity) {
        List<PackageForecastDetailEntity> forecastDetailList = packageForecastDetailService.listDbByMainId(entity.getId());
        List<String> soIds = forecastDetailList.stream().map(PackageForecastDetailEntity::getSoId).distinct().collect(Collectors.toList());
        List<SoB2cEntity> soB2cEntityList = soB2cFeign.listByIds(soIds);
        List<String> shopIds = soB2cEntityList.stream().map(SoB2cEntity::getShopId).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(shopIds)) {
            throw new ServiceException("销售订单店铺未找到");
        }
        PackageForecastDTO.AlExpressHandoverBaseDTO base = getAlExpressHandoverBase(platform(), shopIds.get(0));
        PdfRequest pdfRequest = PdfRequest.builder()
                .client(base.getClient())
                .handoverContentId(Long.valueOf(entity.getPlatformPackageNo()))
                .locale("zh_CN")
                .type(1)
                .userInfo(base.getUserInfo())
                .build();
        try {
            IopResponse response = aliExpressHandoverService.getPdf(base.getAuthMap(), pdfRequest);
            BaseResponse baseResponse = JSONObject.parseObject(response.getBody(), BaseResponse.class);
            BaseResult baseResult = JSONObject.parseObject(baseResponse.getResult(), BaseResult.class);
            if (StringUtils.isNotEmpty(baseResult.getErrorMsg()) || StringUtils.isEmpty(baseResult.getData())) {
                throw new ServiceException(baseResult.getErrorMsg());
            }
            PdfResponse pdfResponse = JSONObject.parseObject(baseResult.getData(), PdfResponse.class);
            return "data:application/pdf;base64," + pdfResponse.getBody();
        } catch (ApiException e) {
            log.error("速卖通打印失败>>>{}", e);
            throw new ServiceException(e.getMessage());
        }
    }

    private void addBigPackage(PackageForecastEntity entity, LogisticsAddressEntity logisticsAddress) {
        List<PackageForecastDetailEntity> forecastDetailList = packageForecastDetailService.listDbByMainId(entity.getId());
        List<String> soIds = forecastDetailList.stream().map(PackageForecastDetailEntity::getSoId).distinct().collect(Collectors.toList());
        List<SoB2cEntity> soB2cEntityList = soB2cFeign.listByIds(soIds);
        List<String> shopIds = soB2cEntityList.stream().map(SoB2cEntity::getShopId).distinct().collect(Collectors.toList());
        if (CollectionUtils.isEmpty(shopIds)) {
            throw new ServiceException("销售订单店铺未找到");
        }
        if (shopIds.size() > 1) {
            throw new ServiceException("速卖通不支持多店铺组包预报");
        }
        PackageForecastDTO.AlExpressHandoverBaseDTO base = getAlExpressHandoverBase(platform(), shopIds.get(0));

        List<String> soIdList = forecastDetailList.stream().map(PackageForecastDetailEntity::getSoId).collect(Collectors.toList());
        Map<String, String> authMap = base.getAuthMap();
        List<SoB2cLogisticsEntity> soB2cLogisticsList = soB2cFeign.listSoB2cLogisticsByMainIdList(soIdList);
        List<String> orderCodeList = soB2cLogisticsList.stream()
                .map(SoB2cLogisticsEntity::getCode)
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.toList());
        if (orderCodeList.size() != forecastDetailList.size()) {
            throw new ServiceException("未获取到小包第三方交易号");
        }
        String topUserKey = base.getUserInfo().getTopUserKey();
        packageForecastDetailService.updateBatchById(forecastDetailList);
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
            BaseResult baseResult = JSONObject.parseObject(iopResponse.getBody(), BaseResult.class);
            if (Objects.nonNull(baseResult.getErrorResponse())) {
                throw new ServiceException(baseResult.getErrorResponse().getSubMsg());
            }
            HandoverCommitResult handoverCommitResult = JSONObject.parseObject(baseResult.getResult(), HandoverCommitResult.class);
            if (Objects.nonNull(handoverCommitResult) && handoverCommitResult.getSuccess()) {
                entity.setHandoverNo(handoverCommitResult.getResponse().getHandoverContentCode());
                entity.setPlatformPackageNo(String.valueOf(handoverCommitResult.getResponse().getHandoverContentId()));
                entity.setRemark("");
            }
        } catch (ApiException e) {
            log.error("创建交接单失败>>>>>>{}", e);
            throw new ServiceException(e.getMessage());
        }
    }
}
