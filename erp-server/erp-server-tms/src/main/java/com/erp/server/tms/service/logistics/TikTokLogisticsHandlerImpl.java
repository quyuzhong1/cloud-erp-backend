package com.erp.server.tms.service.logistics;


import cn.hutool.json.JSONUtil;
import com.common.business.annotation.LogisticsPlatformType;
import com.common.business.enums.LogisticsPlatformEnum;
import com.common.business.utils.PdfUtil;
import com.common.core.controller.vo.ApiResult;
import com.common.core.exception.ServiceException;
import com.erp.model.file.dto.FileDTO;
import com.erp.model.tms.entity.LogisticsSaleChannelEntity;
import com.erp.model.tms.enums.BusinessTypeEnum;
import com.erp.model.tms.enums.DeliveryTypeEnum;
import com.erp.model.tms.enums.RequestStatusEnums;
import com.erp.model.tms.vo.request.ChanelQueryVO;
import com.erp.model.tms.vo.request.LogisticsGetLabelVO;
import com.erp.model.tms.vo.request.LogisticsOrderVO;
import com.erp.model.tms.vo.response.LogisticsOrderResponseVO;
import com.erp.model.tms.vo.response.LogisticsPrintLabelResponse;
import com.erp.model.tms.vo.response.LogisticsServiceResponseVO;
import com.erp.rpc.dmp.feign.DmpTaskFeign;
import com.erp.rpc.file.feign.FileFeign;
import com.erp.rpc.oms.feign.ShopInfoFeign;
import com.erp.server.tms.handler.AbstractLogisticsHandler;
import com.erp.server.tms.service.LogisticsOperateService;
import com.sdk.oms.tiktok.dto.TikTokShopInfoDTO;
import com.sdk.oms.tiktok.dto.tiktok.packages.PackageDetailDTO;
import com.sdk.oms.tiktok.dto.tiktok.packages.PackageDocumentDTO;
import com.sdk.oms.tiktok.dto.tiktok.ship.ShipOrderOther;
import com.sdk.oms.tiktok.dto.tiktok.ship.ShipOrderOtherParam;
import com.sdk.oms.tiktok.service.TikTokSdkClientService;
import com.sdk.tms.tiktok.channel.provider.ShippingProvidersBean;
import com.sdk.tms.tiktok.service.TikTokShipperService;
import io.seata.common.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.*;

/**
 * TikTok物流
 */
@Slf4j
@Component
@LogisticsPlatformType(LogisticsPlatformEnum.TIK_TOK)
public class TikTokLogisticsHandlerImpl extends AbstractLogisticsHandler {
    @Resource
    private DmpTaskFeign dmpTaskFeign;
    @Resource
    private ShopInfoFeign shopInfoFeign;
    @Resource
    private TikTokShipperService tikTokShipperService;

    @Resource
    private TikTokSdkClientService tikTokSdkClientService;

    @Resource
    private LogisticsOperateService logisticsOperateService;
    @Resource
    private FileFeign fileFeign;
    /**
     * 查询店铺
     * @param shopId
     * @return
     */
    @Override
    public Map<String, String> getLogisticsAuthConfigByShopId(String shopId) {
        //获取商铺配置信息
        Map<String, String> map = new HashMap<>();
        map.put("shopId", shopId);
        map.put("logisticsPlatform", getPlatForm().getCode());
        return map;
    }

    @Override
    public ApiResult<List<LogisticsSaleChannelEntity>> getChannel(ChanelQueryVO chanelQueryVO) {
        Map<String, String> authMap = chanelQueryVO.getAuthMap();
        List<LogisticsSaleChannelEntity> resuletList = new ArrayList<>();
        List<ShippingProvidersBean> providersBeanList = tikTokShipperService.sendTikTokLogisticsChannel(authMap.get("shopId"));
        for (ShippingProvidersBean providerDTO : providersBeanList) {
            LogisticsSaleChannelEntity logisticsSaleChannelEntity = new LogisticsSaleChannelEntity()
                    .setCode(providerDTO.getId())
                    .setPlatformChannelId(providerDTO.getId())
                    .setCnName(providerDTO.getName())
                    .setLogisticsPlatform(LogisticsPlatformEnum.TIK_TOK.getCode());
            resuletList.add(logisticsSaleChannelEntity);
        }
        return success(resuletList);
    }

    @Override
    public ApiResult<LogisticsOrderResponseVO> createOrder(LogisticsOrderVO logisticsOrderVO) {
        Map<String, String> authMap = logisticsOrderVO.getAuthMap();
        String shopId = authMap.get("shopId");
        String packageId = logisticsOrderVO.getPackageId();
        String deliveryType = logisticsOrderVO.getDeliveryType();
        if(StringUtils.isBlank(packageId)){
            throw new ServiceException("包裹id不能为空");
        }
        if(StringUtils.isBlank(deliveryType)){
            throw new ServiceException("发货方式不能为空");
        }
        TikTokShopInfoDTO tikTokShopInfoDTO = tikTokSdkClientService.getShopInfoByShopId(shopId);
        //先查询是否有跟踪号，没有再下单
        PackageDetailDTO packageDetailDTO;
        try {
            packageDetailDTO = tikTokSdkClientService.getPackageDetail(tikTokShopInfoDTO,packageId);
        }catch (Exception e){
            logisticsOperateService.pushOperateLog(logisticsOrderVO.getSourceId(),
                    logisticsOrderVO.getDeliveryNo(), BusinessTypeEnum.CREATE_ORDER.getCode(), LogisticsPlatformEnum.TIK_TOK.getCode(),
                    RequestStatusEnums.FAILED.getCode(), JSONUtil.toJsonStr(logisticsOrderVO), "", false);
            return failure("获取跟踪异常："+e.getMessage());
        }
        if(StringUtils.isNotBlank(packageDetailDTO.getData().getTracking_number())){
            logisticsOperateService.pushOperateLog(logisticsOrderVO.getSourceId(),
                    logisticsOrderVO.getDeliveryNo(), BusinessTypeEnum.CREATE_ORDER.getCode(), LogisticsPlatformEnum.TIK_TOK.getCode(),
                    RequestStatusEnums.SUCCESS.getCode(), JSONUtil.toJsonStr(logisticsOrderVO), JSONUtil.toJsonStr(packageDetailDTO), false);
            return success(LogisticsOrderResponseVO.builder()
                    .transportNo(packageDetailDTO.getData().getTracking_number())
                    .deliveryNo(logisticsOrderVO.getDeliveryNo())
                    .trackNo(packageDetailDTO.getData().getTracking_number())
                    .build());
        }
        ShipOrderOtherParam paramDTO = new ShipOrderOtherParam();
        if(DeliveryTypeEnum.DOOR_PICKUP.getCode().equals(deliveryType)){
            paramDTO.setHandoverMethod("PICKUP");
        }else{
            paramDTO.setHandoverMethod("DROP_OFF");
        }
        try {
            ShipOrderOther shipOrderOther = tikTokSdkClientService.sendTikTokShipOrderOther(tikTokShopInfoDTO,packageId,paramDTO);
            if(shipOrderOther.getCode()!=0){
                logisticsOperateService.pushOperateLog(logisticsOrderVO.getSourceId(),
                        logisticsOrderVO.getDeliveryNo(), BusinessTypeEnum.CREATE_ORDER.getCode(), LogisticsPlatformEnum.TIK_TOK.getCode(),
                        RequestStatusEnums.FAILED.getCode(), JSONUtil.toJsonStr(logisticsOrderVO), JSONUtil.toJsonStr(shipOrderOther), false);
                return failure("向TIKTOK平台下物流单异常："+shipOrderOther.getMessage());
            }
        }catch (Exception e){
            logisticsOperateService.pushOperateLog(logisticsOrderVO.getSourceId(),
                    logisticsOrderVO.getDeliveryNo(), BusinessTypeEnum.CREATE_ORDER.getCode(), LogisticsPlatformEnum.TIK_TOK.getCode(),
                    RequestStatusEnums.SUCCESS.getCode(), JSONUtil.toJsonStr(logisticsOrderVO), "", false);
            return failure("向平台下物流单异常："+e.getMessage());
        }
        try {
            packageDetailDTO = tikTokSdkClientService.getPackageDetail(tikTokShopInfoDTO,packageId);
        }catch (Exception e){
            logisticsOperateService.pushOperateLog(logisticsOrderVO.getSourceId(),
                    logisticsOrderVO.getDeliveryNo(), BusinessTypeEnum.CREATE_ORDER.getCode(), LogisticsPlatformEnum.TIK_TOK.getCode(),
                    RequestStatusEnums.SUCCESS.getCode(), JSONUtil.toJsonStr(logisticsOrderVO), "", false);
            return failure("获取跟踪异常："+e.getMessage());
        }
        if(StringUtils.isNotBlank(packageDetailDTO.getData().getTracking_number())){
            logisticsOperateService.pushOperateLog(logisticsOrderVO.getSourceId(),
                    logisticsOrderVO.getDeliveryNo(), BusinessTypeEnum.CREATE_ORDER.getCode(), LogisticsPlatformEnum.TIK_TOK.getCode(),
                    RequestStatusEnums.SUCCESS.getCode(), JSONUtil.toJsonStr(logisticsOrderVO), JSONUtil.toJsonStr(packageDetailDTO), false);
            return success(LogisticsOrderResponseVO.builder()
                    .transportNo(packageDetailDTO.getData().getTracking_number())
                    .deliveryNo(logisticsOrderVO.getDeliveryNo())
                    .trackNo(packageDetailDTO.getData().getTracking_number())
                    .build());
        }else{
            logisticsOperateService.pushOperateLog(logisticsOrderVO.getSourceId(),
                    logisticsOrderVO.getDeliveryNo(), BusinessTypeEnum.CREATE_ORDER.getCode(), LogisticsPlatformEnum.TIK_TOK.getCode(),
                    RequestStatusEnums.SUCCESS.getCode(), JSONUtil.toJsonStr(logisticsOrderVO), JSONUtil.toJsonStr(packageDetailDTO), false);
            return failure("跟踪号获取为空，请稍后重试");
        }
    }

    @Override
    public ApiResult<Object>authorization(Map<String, String> authMap) {
        List<ShippingProvidersBean> providersBeanList = tikTokShipperService.sendTikTokLogisticsChannel(authMap.get("shopId"));
        if(CollectionUtils.isEmpty(providersBeanList)){
            return failure("授权失败");
        }
        return success("授权成功");
    }

    @Override
    public LogisticsPlatformEnum getPlatForm() {
        return LogisticsPlatformEnum.TIK_TOK;
    }
    /**
     * 获取标签
     *
     * @return
     */

    @Override
    public ApiResult<List<LogisticsPrintLabelResponse>> getLabelList(List<LogisticsGetLabelVO> logisticsGetLabelVOList) throws IOException {
        LogisticsGetLabelVO logisticsGetLabelVO = logisticsGetLabelVOList.stream().filter(e -> Objects.nonNull(e.getAuthMap())).findFirst().orElse(null);
        assert logisticsGetLabelVO != null;
        Map<String, String> authMap = logisticsGetLabelVO.getAuthMap();
        String shopId = authMap.get("shopId");
        TikTokShopInfoDTO tikTokShopInfoDTO = tikTokSdkClientService.getShopInfoByShopId(shopId);
        List<LogisticsPrintLabelResponse> resultList = new ArrayList<>();
        for (LogisticsGetLabelVO vo : logisticsGetLabelVOList) {
            if(StringUtils.isBlank(vo.getPackageId())){
                throw new ServiceException("包裹id不能为空");
            }
            PackageDocumentDTO packageDocumentDTO = tikTokSdkClientService.getPackageDocument(tikTokShopInfoDTO,vo.getPackageId(),"SHIPPING_LABEL");
            if(packageDocumentDTO.getCode()!=0){
                return failure("获取标签失败,"+ packageDocumentDTO.getMessage());
            }
            if(StringUtils.isBlank(packageDocumentDTO.getData().getDocUrl())){
                throw new ServiceException("获取标签失败");
            }else{
                String url = "";
                int maxRetries = 3; // 最大重试次数
                long retryInterval = 1000; // 重试间隔1秒
                Exception lastException = null;

                for (int attempt = 0; attempt <= maxRetries; attempt++) { // 包含初始请求+3次重试
                    try {
                        String base64 = PdfUtil.convertPdfUrlToBase64(packageDocumentDTO.getData().getDocUrl(),false);
                        FileDTO.UploadBase64 uploadBase64 = FileDTO.UploadBase64.builder()
                                .base64(base64)
                                .fileName(logisticsGetLabelVO.getDeliveryNo() + ".pdf")
                                .build();
                        url = fileFeign.uploadFileByBase64(uploadBase64);
                        break; // 成功则跳出循环
                    } catch (Exception e) {
                        lastException = e;
                        if (attempt < maxRetries) { // 非最后一次尝试时等待
                            try {
                                Thread.sleep(retryInterval);
                            } catch (InterruptedException ie) {
                                log.error("TikTok获取物流面单重试睡眠异常:", ie);
                                Thread.currentThread().interrupt();
                            }
                        }
                    }
                }

                if (url.isEmpty()) {
                    String errorMsg = "获取标签失败，重试" + maxRetries + "次后仍失败";
                    if (lastException != null) {
                        log.error("TokTok获取面单获取标签失败，最后一次异常:", lastException);
                        errorMsg += "，原因: " + lastException.getMessage();
                    }
                    throw new ServiceException(errorMsg);
                }
//                String prefix = "data:application/pdf;base64,";
                LogisticsPrintLabelResponse response = LogisticsPrintLabelResponse.builder()
                        .deliveryNoList(Collections.singletonList(vo.getDeliveryNo()))
                        .transportNoList(Collections.singletonList(vo.getTransportNo()))
                        .trackNoList(Collections.singletonList(vo.getTrackNo()))
                        .labelUrl(url).build();
                resultList.add(response);
            }
        }
        return success(resultList);
    }

    @Override
    public ApiResult<List<LogisticsServiceResponseVO>> listLogisticsService(Map<String, String> authMap) {
        return ApiResult.error(-1, "功能未开放");
    }
}
