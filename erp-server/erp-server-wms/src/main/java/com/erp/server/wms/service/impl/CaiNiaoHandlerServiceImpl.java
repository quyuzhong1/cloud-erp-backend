package com.erp.server.wms.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.nacos.common.utils.StringUtils;
import com.common.business.enums.OmsPlatformEnum;
import com.common.business.enums.PlatformDictEnum;
import com.common.business.threadlocal.ThirdWarehouseContext;
import com.common.business.wrapper.FeignQuery;
import com.common.core.controller.vo.ApiResult;
import com.common.core.exception.ServiceException;
import com.erp.model.dmp.dto.CfgAppClientDTO;
import com.erp.model.dmp.entity.CfgAppClientEntity;
import com.erp.model.dmp.enums.AppClientEnum;
import com.erp.model.oms.entity.ShopInfoEntity;
import com.erp.model.oms.enums.AuthStatusEnum;
import com.erp.model.wms.dto.OverseasProviderDTO;
import com.erp.model.wms.dto.third.*;
import com.erp.model.wms.enums.ThirdWarehouseCancelResultEnum;
import com.erp.oms.aliexpress.dto.AliExpressShopInfoDTO;
import com.erp.oms.aliexpress.service.AliExpressOrderService;
import com.erp.rpc.dmp.feign.DmpTaskFeign;
import com.erp.server.wms.handler.AbstractThirdWarehouseHandler;
import com.erp.wms.aliexpress.model.AliexpressAuthDTO;
import com.erp.wms.aliexpress.model.inbound.AliexpressInboundDTO;
import com.erp.wms.aliexpress.model.inbound.ApiInboundResponseDTO;
import com.erp.wms.aliexpress.model.order.AliexpressCancelOrderDTO;
import com.erp.wms.aliexpress.model.order.AliexpressOrderDTO;
import com.erp.wms.aliexpress.model.order.ApiOrderResponseDTO;
import com.erp.wms.aliexpress.service.AliexpressWarehouseService;
import com.erp.wms.aliexpress.util.ApiException;
import com.sdk.wms.damai.dto.request.DaMaiGetOrderRequest;
import com.sdk.wms.damai.dto.response.DaMaiBaseResp;
import com.sdk.wms.damai.dto.response.DaMaiGetOrderResp;
import com.sdk.wms.jifeng.dto.response.JiFengBaseResp;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author liuruipeng
 */
@Slf4j
@Service
public class CaiNiaoHandlerServiceImpl extends AbstractThirdWarehouseHandler {

    @Resource
    private DmpTaskFeign dmpTaskFeign;

    @Resource
    private AliexpressWarehouseService aliexpressWarehouseService;

    @Resource
    private AliExpressOrderService aliExpressOrderService;

    @Override
    public OmsPlatformEnum getPlatForm() {
        return OmsPlatformEnum.CAI_NIAO;
    }


    @Override
    protected ApiResult<List<ThirdWarehouseSkuResp>> getSkuList(ThirdWarehouseProductReq productReq) {
        return null;
    }

    @Override
    protected ApiResult<String> createInboundBill(ThirdWarehouseCreateInboundReq createInboundReq) {
        AliexpressInboundDTO aliexpressInboundDTO = convertToInboundDto(createInboundReq);
        try {
            ApiInboundResponseDTO apiOrderResponseDTO = aliexpressWarehouseService.createInbound(aliexpressInboundDTO);
            if(!apiOrderResponseDTO.isSuccess()){
                log.error("创建菜鸟仓入库单失败，{}",JSONUtil.toJsonStr(apiOrderResponseDTO));
                return failure(apiOrderResponseDTO.getErrorResponse().getMsg()+";"+apiOrderResponseDTO.getErrorResponse().getSubMsg());
            }
            return success(apiOrderResponseDTO.getResult().getData().getEntryOrderId());
        }catch (Exception e){
            log.error("创建菜鸟仓入库单失败，入参：{}，错误信息：", JSONUtil.toJsonStr(aliexpressInboundDTO), e);
            throw new ServiceException("创建菜鸟仓出库单失败：" + e.getMessage());
        }
    }

    private AliexpressInboundDTO convertToInboundDto(ThirdWarehouseCreateInboundReq createInboundReq) {
        //授权信息
        AliexpressAuthDTO aliexpressAuthDTO = buildAuthDTO(createInboundReq.getShopId(),createInboundReq.getOwnerCode());

        List<AliexpressInboundDTO.OrderLines> orderLines = new ArrayList<>();
        List<ThirdWarehouseCreateInboundReq.Item> itemList = createInboundReq.getItems();
        //相同sku合并数量
        if(CollectionUtils.isNotEmpty(itemList)){
            Map<String,Integer> mergeSkuMap = itemList.stream().collect(Collectors.toMap(ThirdWarehouseCreateInboundReq.Item::getProductSku, ThirdWarehouseCreateInboundReq.Item::getQuantity, Integer::sum));
            //将map转成List<Item>
            createInboundReq.setItems(mergeSkuMap.entrySet().stream().map(v->{
                ThirdWarehouseCreateInboundReq.Item item = itemList.stream().filter(i->i.getProductSku().equals(v.getKey())).findFirst().orElse(new ThirdWarehouseCreateInboundReq.Item());
                return new ThirdWarehouseCreateInboundReq.Item(v.getKey(),item.getProductSkuId(),v.getValue());
            }).collect(Collectors.toList()));
        }
        for (ThirdWarehouseCreateInboundReq.Item item : createInboundReq.getItems()) {
            AliexpressInboundDTO.OrderLines orderLine = AliexpressInboundDTO.OrderLines.builder()
                    .itemCode(item.getProductSku())
                    .itemId(item.getProductSkuId())
                    .planQty(item.getQuantity())
                    .inventoryType("1")
                    .ownerCode(createInboundReq.getOwnerCode())
                    .build();
            orderLines.add(orderLine);
        }

        return AliexpressInboundDTO.builder()
                .aliexpressAuthDTO(aliexpressAuthDTO)
                .entryOrder(AliexpressInboundDTO.EntryOrder.builder()
                        .orderType("SCRK")
                        .entryOrderCode(createInboundReq.getReferenceNo())
                        .ownerCode(createInboundReq.getOwnerCode())
                        .orderCreateTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                        .warehouseCode(createInboundReq.getWarehouseCode())
                        .build())
                .OrderLines(orderLines)
                .build();
    }

    @Override
    protected ApiResult<String> editInboundBill(ThirdWarehouseCreateInboundReq createInboundReq) {
        throw new ServiceException("该仓库入库单不允许修改，请取消入库单后重新创建");
    }

    @Override
    protected ApiResult<String> cancelInboundBill(ThirdWarehouseCancelInboundReq cancelInboundReq) {
        AliexpressCancelOrderDTO aliexpressCancelOrderDTO = new AliexpressCancelOrderDTO();
        AliexpressAuthDTO aliexpressAuthDTO = buildAuthDTO(cancelInboundReq.getShopId(),cancelInboundReq.getOwnerCode());
        aliexpressCancelOrderDTO.setAliexpressAuthDTO(aliexpressAuthDTO);
        aliexpressCancelOrderDTO.setOrderId(cancelInboundReq.getReceivingCode());
        aliexpressCancelOrderDTO.setOwnerCode(cancelInboundReq.getOwnerCode());
        aliexpressCancelOrderDTO.setWarehouseCode(cancelInboundReq.getWarehouseCode());
        aliexpressCancelOrderDTO.setOrderType("SCRK");
        aliexpressCancelOrderDTO.setOrderCode(cancelInboundReq.getSourceCode());
        try {
            ApiOrderResponseDTO apiOrderResponseDTO = aliexpressWarehouseService.cancelOrder(aliexpressCancelOrderDTO);
            if(!apiOrderResponseDTO.isSuccess()){
                log.error("取消菜鸟仓入库单失败，{}",JSONUtil.toJsonStr(apiOrderResponseDTO));
                return failure(apiOrderResponseDTO.getErrorResponse().getMsg()+";"+apiOrderResponseDTO.getErrorResponse().getSubMsg());
            }
            return success();
        } catch (ApiException e) {
            log.error("取消菜鸟仓入库单失败，入参：{}，错误信息：", JSONUtil.toJsonStr(aliexpressCancelOrderDTO), e);
            throw new ServiceException("取消菜鸟仓入库单失败：" + e.getMessage());
        }
    }

    @Override
    protected ApiResult<List<ThirdWarehouseCalculateFeeResponse>> getCalculateFeeBatch(ThirdWarehouseCalculateFeeReq calculateFeeReq) {
        return null;
    }

    @Override
    protected ApiResult<ThirdWarehouseUploadFileResponse> uploadFile(ThirdWarehouseUploadFileReq uploadFileReq) {
        return success(new ThirdWarehouseUploadFileResponse());
    }

    @Override
    protected ApiResult<ThirdWarehouseUploadOrderLabelResponse> uploadOrderLabel(ThirdWarehouseUploadOrderLabelReq uploadFileReq) {
        return null;
    }

    @Override
    protected ApiResult<String> createOutboundBill(ThirdWarehouseCreateOutboundReq createOutboundReq) {
        AliexpressOrderDTO aliexpressOrderDTO = convertToOrderDto(createOutboundReq);
        try {
            ApiOrderResponseDTO apiOrderResponseDTO = aliexpressWarehouseService.createOutbound(aliexpressOrderDTO);
            if(!apiOrderResponseDTO.isSuccess()){
                log.error("创建菜鸟仓出库单失败，{}",JSONUtil.toJsonStr(apiOrderResponseDTO));
                return failure(apiOrderResponseDTO.getErrorResponse().getMsg()+";"+apiOrderResponseDTO.getErrorResponse().getSubMsg());
            }
            return success(apiOrderResponseDTO.getResult().getData().getDeliveryOrderId());
        }catch (Exception e){
            log.error("创建菜鸟仓出库单失败，入参：{}，错误信息：", JSONUtil.toJsonStr(createOutboundReq), e);
            throw new ServiceException("创建菜鸟仓出库单失败：" + e.getMessage());
        }
    }

    private AliexpressOrderDTO convertToOrderDto(ThirdWarehouseCreateOutboundReq createOutboundReq) {
        AliexpressOrderDTO aliexpressOrderDTO = new AliexpressOrderDTO();

        //授权信息
        AliexpressAuthDTO aliexpressAuthDTO = buildAuthDTO(createOutboundReq.getShopId(),createOutboundReq.getOwnerCode());
        aliexpressOrderDTO.setAliexpressAuthDTO(aliexpressAuthDTO);

        //发货信息
        AliexpressOrderDTO.DeliveryOrder deliveryOrder = new AliexpressOrderDTO.DeliveryOrder();
        deliveryOrder.setOrderType("JYCK");
        deliveryOrder.setOwnerCode(createOutboundReq.getOwnerCode());
        deliveryOrder.setCreateTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        deliveryOrder.setDeliveryOrderCode(createOutboundReq.getReferenceNo());
        deliveryOrder.setWarehouseCode(createOutboundReq.getWarehouseCode());
        deliveryOrder.setShopNick(createOutboundReq.getShopName());
        deliveryOrder.setLogisticsCode(createOutboundReq.getShippingMethod());
        deliveryOrder.setSourcePlatformCode("AE");
        deliveryOrder.setExpressCode(createOutboundReq.getTrackingNo());

        AliexpressOrderDTO.DeliveryOrder.ReceiverInfoDTO receiverInfoDTO = AliexpressOrderDTO.DeliveryOrder.ReceiverInfoDTO.builder()
                .countryCode(createOutboundReq.getReceiverInfo().getCountryCode())
                .province(createOutboundReq.getReceiverInfo().getProvince())
                .city(createOutboundReq.getReceiverInfo().getCity())
                .area(createOutboundReq.getReceiverInfo().getDistrict())
                .zipCode(createOutboundReq.getReceiverInfo().getZipCode())
                .mobile(createOutboundReq.getReceiverInfo().getPhone())
                .detailAddress(createOutboundReq.getReceiverInfo().getAddress1())
                .email(createOutboundReq.getReceiverInfo().getEmail())
                .build();

        deliveryOrder.setReceiverInfo(receiverInfoDTO);
        aliexpressOrderDTO.setDeliveryOrder(deliveryOrder);

        //明细信息
        List<AliexpressOrderDTO.OrderLines> OrderLines = new ArrayList<>();
        for (ThirdWarehouseCreateOutboundReq.Item item : createOutboundReq.getItems()) {
            AliexpressOrderDTO.OrderLines orderLines = AliexpressOrderDTO.OrderLines.builder()
                    .orderLineNo(item.getPlatformDetailId())
                    .inventoryType("1")
                    .planQty(item.getQuantity())
                    .ownerCode(createOutboundReq.getOwnerCode())
                    .itemCode(item.getProductSku())
                    .subSourceOrderCode(item.getPlatformDetailId())
                    .itemId(Integer.valueOf(item.getProductSkuId()))
                    .sourceOrderCode(createOutboundReq.getPlatformCode())
                    .build();
            OrderLines.add(orderLines);
        }
        aliexpressOrderDTO.setOrderLines(OrderLines);

        //扩展字段
        AliexpressOrderDTO.ExtendProps extendProps = new AliexpressOrderDTO.ExtendProps();
        extendProps.setMerchantType("POP");
        extendProps.setPrintInfo(createOutboundReq.getLabelUrl());
        aliexpressOrderDTO.setExtendProps(extendProps);

        return aliexpressOrderDTO;
    }


    @Override
    protected ApiResult<ThirdWarehouseUploadHandoverFileResponse> uploadHandoverFile(ThirdWarehouseUploadHandoverFileReq uploadHandoverFileReq) {
        return null;
    }

    private AliexpressAuthDTO buildAuthDTO(String shopId,String ownerCode) {
        AliexpressAuthDTO aliexpressAuthDTO = new AliexpressAuthDTO();
        AliExpressShopInfoDTO shopInfoDTO = aliExpressOrderService.getShopInfoByShopId(shopId);
        aliexpressAuthDTO.setUrl(shopInfoDTO.getBaseUrl());
        aliexpressAuthDTO.setAccessToken(shopInfoDTO.getToken());
        aliexpressAuthDTO.setAppKey(shopInfoDTO.getClientId());
        aliexpressAuthDTO.setAppSecret(shopInfoDTO.getClientSecret());
        aliexpressAuthDTO.setOwnerCode(ownerCode);
        return aliexpressAuthDTO;
    }

    @Override
    protected ApiResult<String> cancelOutboundBill(ThirdWarehouseCancelOutboundReq cancelOutboundReq) {
        AliexpressCancelOrderDTO aliexpressCancelOrderDTO = new AliexpressCancelOrderDTO();
        AliexpressAuthDTO aliexpressAuthDTO = buildAuthDTO(cancelOutboundReq.getShopId(),cancelOutboundReq.getOwnerCode());
        aliexpressCancelOrderDTO.setAliexpressAuthDTO(aliexpressAuthDTO);
        aliexpressCancelOrderDTO.setOrderId(cancelOutboundReq.getOrderCode());
        aliexpressCancelOrderDTO.setOrderType("JYCK");
        aliexpressCancelOrderDTO.setOwnerCode(cancelOutboundReq.getOwnerCode());
        aliexpressCancelOrderDTO.setOrderCode(cancelOutboundReq.getErpOrderCode());
        aliexpressCancelOrderDTO.setWarehouseCode(cancelOutboundReq.getWarehouseCode());
        try {
            ApiOrderResponseDTO apiOrderResponseDTO = aliexpressWarehouseService.cancelOrder(aliexpressCancelOrderDTO);
            if(!apiOrderResponseDTO.isSuccess()){
                log.error("取消菜鸟仓出库单失败，{}",JSONUtil.toJsonStr(apiOrderResponseDTO));
                return failure(apiOrderResponseDTO.getErrorResponse().getMsg()+";"+apiOrderResponseDTO.getErrorResponse().getSubMsg());
            }
            return success(ThirdWarehouseCancelResultEnum.INTERCEPTION_SUCCESSFUL.getCode());
        } catch (ApiException e) {
            log.error("取消菜鸟仓出库单失败，入参：{}，错误信息：", JSONUtil.toJsonStr(cancelOutboundReq), e);
            throw new ServiceException("取消菜鸟仓出库单失败：" + e.getMessage());
        }
    }

    @Override
    protected ApiResult<String> queryOutboundBill(@Valid ThirdWarehouseQueryOutboundReq queryOutboundReq){
        return ApiResult.error("查询菜鸟仓出库单失败");
    }
    @Override
    protected Boolean warehouseAuthorize(OverseasProviderDTO.AuthorizeParamDTO dto) {
        Map<String, Object> authJson = dto.getAuthJson();
        String shopAccount = authJson.get("shopAccount").toString();
        if( shopAccount == null || shopAccount.isEmpty()) {
            throw new ServiceException("店铺账号不能为空");
        }
        //查询店铺是否已授权，是则自动授权成功
        List<ShopInfoEntity> shopInfoEntityList = FeignQuery.create(ShopInfoEntity.class)
                .eq(ShopInfoEntity::getAccount, shopAccount)
                .eq(ShopInfoEntity::getDictPlatform, PlatformDictEnum.ALI_EXPRESS.getCode())
                .eq(ShopInfoEntity::getAuthStatus, AuthStatusEnum.ALREADY.getCode())
                .list();
        if(CollectionUtils.isEmpty(shopInfoEntityList)){
            throw new ServiceException("未找到已授权的速卖通店铺");
        }
        CfgAppClientDTO.FindDTO findDTO = new CfgAppClientDTO.FindDTO();
        AppClientEnum appClientEnum = AppClientEnum.ALI_EXPRESS_TOKEN;
        findDTO.setBusinessType(appClientEnum.getBusinessType());
        findDTO.setDictPlatform(appClientEnum.getPlatform());
        findDTO.setPlatformType(appClientEnum.getPlatformType());
        CfgAppClientEntity cfgAppClient = dmpTaskFeign.getCfgAppClient(findDTO);
        if (Objects.isNull(cfgAppClient)) {
            String msg = StrUtil.format("速卖通获取授权信息为空:{}", JSONUtil.toJsonStr(findDTO));
            throw new ServiceException(msg);
        }
        authJson.put("shopId",shopInfoEntityList.get(0).getId());
        authJson.put("baseUrl",cfgAppClient.getUrl());
        authJson.put("clientId",cfgAppClient.getClientId());
        authJson.put("clientSecret",cfgAppClient.getClientSecret());
        dto.setAuthJson(authJson);

        return true;
    }

    public <T> boolean isSuccess(JiFengBaseResp<T> resp){
        return resp.getCode()==0;
    }

}
