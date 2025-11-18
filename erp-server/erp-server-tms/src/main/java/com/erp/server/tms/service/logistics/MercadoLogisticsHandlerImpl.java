package com.erp.server.tms.service.logistics;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.json.JSONUtil;
import com.common.business.annotation.LogisticsPlatformType;
import com.common.business.enums.LogisticsPlatformEnum;
import com.common.core.controller.vo.ApiResult;
import com.common.core.exception.ServiceException;
import com.erp.model.dmp.dto.CfgAppClientDTO;
import com.erp.model.dmp.entity.CfgAppClientEntity;
import com.erp.model.dmp.enums.AppClientEnum;
import com.erp.model.file.dto.FileDTO;
import com.erp.model.oms.entity.ShopAuthEntity;
import com.erp.model.tms.entity.LogisticsSaleChannelEntity;
import com.erp.model.tms.enums.BusinessTypeEnum;
import com.erp.model.tms.enums.RequestStatusEnums;
import com.erp.model.tms.vo.request.ChanelQueryVO;
import com.erp.model.tms.vo.request.LogisticsCancelOrderVO;
import com.erp.model.tms.vo.request.LogisticsGetLabelVO;
import com.erp.model.tms.vo.request.LogisticsOrderVO;
import com.erp.model.tms.vo.response.CancelResponseVO;
import com.erp.model.tms.vo.response.LogisticsOrderResponseVO;
import com.erp.model.tms.vo.response.LogisticsPrintLabelResponse;
import com.erp.model.tms.vo.response.LogisticsServiceResponseVO;
import com.erp.rpc.dmp.feign.DmpTaskFeign;
import com.erp.rpc.file.feign.FileFeign;
import com.erp.rpc.oms.feign.ShopInfoFeign;
import com.erp.server.tms.handler.AbstractLogisticsHandler;
import com.erp.server.tms.service.LogisticsOperateService;
import com.sdk.oms.mercado.dto.MercadoShopInfoDTO;
import com.sdk.oms.mercado.dto.mercado.shipment.ShipmentViewDTO;
import com.sdk.oms.mercado.service.MercadoSdkClientService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.*;

/**
 * TikTok物流
 */
@Slf4j
@Component
@LogisticsPlatformType(LogisticsPlatformEnum.MERCADOLIBRE)
public class MercadoLogisticsHandlerImpl extends AbstractLogisticsHandler {
    @Resource
    private MercadoSdkClientService mercadoSdkClientService;
    @Resource
    private LogisticsOperateService logisticsOperateService;
    @Resource
    private ShopInfoFeign shopInfoFeign;
    @Resource
    private DmpTaskFeign dmpTaskFeign;
    @Resource
    private FileFeign fileFeign;
    /**
     * 查询店铺授权
     * @param shopId
     * @return
     */
    @Override
    public Map<String, String> getLogisticsAuthConfigByShopId(String shopId) {
        CfgAppClientDTO.FindDTO findDTO = new CfgAppClientDTO.FindDTO();
        AppClientEnum appClientEnum = AppClientEnum.MERCADO_ACCESS_TOKEN;
        findDTO.setBusinessType(appClientEnum.getBusinessType());
        findDTO.setDictPlatform(appClientEnum.getPlatform());
        findDTO.setPlatformType(appClientEnum.getPlatformType());
        CfgAppClientEntity cfgAppClient;
        try {
            cfgAppClient = dmpTaskFeign.getCfgAppClient(findDTO);
        } catch (Exception e) {
            log.error("erp-dmp服务dmpTaskFeign.getCfgAppClient接口异常：{}", e.getMessage());
            return new HashMap<>();
        }
        if (Objects.isNull(cfgAppClient)) return new HashMap<>();
        Map<String, String> map = new HashMap<>();
        map.put("id", cfgAppClient.getId());
        map.put("logisticsPlatform", getPlatForm().getCode());
        map.put("clientSecret", cfgAppClient.getClientSecret());
        map.put("clientId", cfgAppClient.getClientId());
        map.put("url", cfgAppClient.getUrl());
        if (CharSequenceUtil.isNotBlank(shopId)) {
            ShopAuthEntity shopAuth = shopInfoFeign.getShopAuthByShopId(shopId);
            if (Objects.nonNull(shopAuth)) {
                map.put("shopId", shopAuth.getShopId());
                map.put("token", shopAuth.getAccessToken());
            }
        }
        return map;
    }

    /**
     * 创建订单
     *
     * @param logisticsOrderVO
     * @return
     *  ready_to_ship 时会存在面单号
     */
    public ApiResult<LogisticsOrderResponseVO> createOrder(LogisticsOrderVO logisticsOrderVO) {
        Map<String, String> authMap = logisticsOrderVO.getAuthMap();
        String token = authMap.get("token");
        MercadoShopInfoDTO shopInfoDTO = new MercadoShopInfoDTO().setAccessToken(token);
        Long shipmentId = logisticsOrderVO.getShipmentId();
        try {
            ShipmentViewDTO shipmentViewDTO = mercadoSdkClientService.getShippingRecords(shopInfoDTO, shipmentId);
            if (Objects.isNull(shipmentViewDTO) || CharSequenceUtil.isBlank(shipmentViewDTO.getTrackingNumber())){
                throw new ServiceException(JSONUtil.toJsonStr(shipmentViewDTO));
            }
            logisticsOperateService.pullOperateLog(logisticsOrderVO.getSourceId(),
                    String.valueOf(shipmentId), BusinessTypeEnum.GET_TRACK_NUMBER.getCode(), LogisticsPlatformEnum.MERCADOLIBRE.getCode(),
                    RequestStatusEnums.SUCCESS.getCode(), JSONUtil.toJsonStr(logisticsOrderVO), JSONUtil.toJsonStr(shipmentViewDTO));
            LogisticsOrderResponseVO vo = LogisticsOrderResponseVO.builder()
                    .deliveryNo(logisticsOrderVO.getDeliveryNo())
                    .trackNo(shipmentViewDTO.getTrackingNumber())
                    .transportNo(shipmentViewDTO.getTrackingNumber())
                    .build();
            return success(vo);
        }catch (Exception e){
            //获取跟踪号异常
            logisticsOperateService.pullOperateLog(logisticsOrderVO.getSourceId(),
                    String.valueOf(shipmentId), BusinessTypeEnum.GET_TRACK_NUMBER.getCode(), LogisticsPlatformEnum.MERCADOLIBRE.getCode(),
                    RequestStatusEnums.FAILED.getCode(), JSONUtil.toJsonStr(logisticsOrderVO), e.getMessage());
            throw new ServiceException(CharSequenceUtil.format("美客多【{}】获取物流单信息异常请求异常:{}",shipmentId,e.getMessage()));
        }
    }

    /**
     * 获取物流面单
     * @param logisticsGetLabelVOList
     * @return
     * @throws IOException
     */
    @Override
    public ApiResult<List<LogisticsPrintLabelResponse>> getLabelList(List<LogisticsGetLabelVO> logisticsGetLabelVOList) throws IOException {
        LogisticsGetLabelVO logisticsGetLabelVO = logisticsGetLabelVOList.stream().filter(e -> Objects.nonNull(e.getAuthMap())).findFirst().orElse(null);
        assert logisticsGetLabelVO != null;
        Map<String, String> authMap = logisticsGetLabelVO.getAuthMap();
        List<LogisticsPrintLabelResponse> responseList = new ArrayList<>();
        List<String> errorList = new ArrayList<>();
        for (LogisticsGetLabelVO vo : logisticsGetLabelVOList) {
            try {
                String base64 = mercadoSdkClientService.printShippingLabel(authMap, Long.valueOf(vo.getDeliveryNo()));
                FileDTO.UploadBase64 uploadBase64 = FileDTO.UploadBase64.builder()
                        .base64(base64)
                        .fileName(logisticsGetLabelVO.getDeliveryNo() + ".pdf")
                        .build();
                String url = fileFeign.uploadFileByBase64(uploadBase64);
//                String prefix = "data:application/pdf;base64,";
//                String base64 = prefix + labelUrl;
                LogisticsPrintLabelResponse response = LogisticsPrintLabelResponse.builder()
                        .deliveryNoList(Collections.singletonList(vo.getDeliveryNo()))
                        .labelUrl(url).build();
                logisticsOperateService.pullOperateLog(vo.getOrderId(), vo.getDeliveryNo(), BusinessTypeEnum.DOWNLOAD_SHIPPING_DOCUMENT.getCode(), LogisticsPlatformEnum.SHOPEE.getCode(),
                        RequestStatusEnums.SUCCESS.getCode(), JSONUtil.toJsonStr(vo), JSONUtil.toJsonStr(url));
                responseList.add(response);
            }catch (Exception e){
                String message = e.getMessage();
                errorList.add(message);
                logisticsOperateService.pullOperateLog(vo.getOrderId(), vo.getDeliveryNo(), BusinessTypeEnum.DOWNLOAD_SHIPPING_DOCUMENT.getCode(), LogisticsPlatformEnum.SHOPEE.getCode(),
                        RequestStatusEnums.FAILED.getCode(), JSONUtil.toJsonStr(vo), e.getMessage());
            }
        }
        if (CollUtil.isNotEmpty(responseList)){
            return success(responseList);
        }else {
            return failure(String.join(";",errorList));
        }
    }
    @Override
    public ApiResult<List<LogisticsSaleChannelEntity>> getChannel(ChanelQueryVO chanelQueryVO) {
        return ApiResult.success(Collections.emptyList());
    }
    @Override
    public ApiResult<List<CancelResponseVO>> cancelOrder(List<LogisticsCancelOrderVO> cancelOrderVOList)  {
        return ApiResult.success();
    }
    @Override
    public ApiResult<Object>authorization(Map<String, String> authMap) {
        return ApiResult.success();
    }

    @Override
    public LogisticsPlatformEnum getPlatForm() {
        return LogisticsPlatformEnum.MERCADOLIBRE;
    }

    @Override
    public ApiResult<List<LogisticsServiceResponseVO>> listLogisticsService(Map<String, String> authMap) {
        return ApiResult.error(-1, "功能未开放");
    }
}
