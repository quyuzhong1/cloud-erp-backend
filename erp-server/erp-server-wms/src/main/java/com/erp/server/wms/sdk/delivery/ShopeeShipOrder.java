package com.erp.server.wms.sdk.delivery;

import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.lang.Tuple;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.common.business.annotation.PlatformShipOrderAnno;
import com.common.business.constant.BusinessCommonConstants;
import com.common.business.dto.PlatformDeliveryInterceptDTO;
import com.common.business.dto.PlatformOrderQueryDTO;
import com.common.business.dto.PlatformShipOrderDTO;
import com.common.business.enums.OrderDeliveryMarkTypeEnum;
import com.common.business.enums.PlatformDictEnum;
import com.common.business.service.IPlatformService;
import com.common.core.entity.BaseEntity;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.erp.model.oms.entity.SoB2cDetailEntity;
import com.erp.model.oms.entity.SoB2cEntity;
import com.erp.model.oms.entity.SoB2cLogisticsEntity;
import com.erp.model.oms.enums.SoB2cBillStatusEnum;
import com.erp.model.tms.dto.LogisticsChannelDTO;
import com.erp.model.tms.dto.LogisticsMappingDTO;
import com.erp.model.tms.entity.LogisticsMappingEntity;
import com.erp.model.wms.dto.DictBasicDTO;
import com.erp.oms.aliexpress.dto.request.DeclareDeliverRequest;
import com.erp.oms.aliexpress.dto.response.AliExpressOrderDetail;
import com.erp.oms.aliexpress.dto.response.OrderItemDetail;
import com.erp.rpc.oms.feign.SoB2cFeign;
import com.erp.rpc.tms.feign.LogisticsFeign;
import com.erp.rpc.tms.feign.LogisticsMappingFeign;
import com.erp.server.wms.service.DictBasicService;
import com.sdk.oms.shopee.dto.logistics.request.Dropoff;
import com.sdk.oms.shopee.dto.logistics.request.ShipOrderRequest;
import com.sdk.oms.shopee.dto.logistics.request.ShipRequest;
import com.sdk.oms.shopee.dto.logistics.response.*;
import com.sdk.oms.shopee.service.ShopeeLogisticsService;
import com.sdk.oms.shopify.api.rest.ShopifyRestClient;
import com.sdk.oms.shopify.api.rest.model.*;
import com.sdk.oms.shopify.dto.ShopifyShopInfoDTO;
import com.sdk.oms.shopify.service.ShopSdkServer;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.sdk.oms.shopee.constants.ShopeeConstants.*;

@Slf4j
@Component
@PlatformShipOrderAnno(method = PlatformDictEnum.SHOPEE)
public class ShopeeShipOrder extends AbstractShipOrder {

    @Resource
    private SoB2cFeign soB2cFeign;

    @Resource
    private LogisticsMappingFeign logisticsMappingFeign;
    @Resource
    private ShopSdkServer shopSdkServer;
    @Resource
    private LogisticsFeign logisticsFeign;
    @Resource
    private ShopeeLogisticsService shopeeLogisticsService;
    @Resource
    private DictBasicService dictBasicService;
    @Override
    public List<String> shipOrder(PlatformShipOrderDTO dto) {
        // 查询拆分前的原平台订单
//        dto.setHasFindSourcePlatformOrder(true);
        // 查询所有信息
        Tuple tuple = super.allSourceOrderInfo(dto);
        List<SoB2cEntity> sourceOrderList = tuple.get(0);
        Map<String, List<SoB2cDetailEntity>> soB2cDetailEntityListMap = tuple.get(1);
        // 当前单据物流信息
        SoB2cLogisticsEntity logisticsEntity = tuple.get(2);
        // 源平台仓单信息
//        Map<String, List<SoB2cDetailEntity>> sourceDetailEntityMap = tuple.get(3);

        //渠道
        String channelId = logisticsEntity.getLogisticsChannelId();

        //获取销售渠道信息
        LogisticsChannelDTO.SignShipDTO tmsSignShipDTO = logisticsFeign.getScaleChannelByChannelById(
                channelId,
                PlatformDictEnum.SHOPEE.getCode()
        );
        if (null == tmsSignShipDTO) {
            throw new ServiceException("找不到渠道信息");
        }

        List<String> signShippedDetailList = new ArrayList<>();
        for (SoB2cEntity mainEntity : sourceOrderList) {
            //检查销售订单详情是否存在
            List<SoB2cDetailEntity> currentDetailEntityList = soB2cDetailEntityListMap.get(mainEntity.getId());
            if (CollectionUtils.isEmpty(currentDetailEntityList)) {
                throw new ServiceException(ApiError.ERROR_SO_B2C_DETAIL_NOT_EXIST);
            }
            // 校验捆绑商品拆分
            // 来源明细ID为空代表是手工添加的明细忽略
            currentDetailEntityList = currentDetailEntityList.stream()
                    .filter(e -> StringUtils.isNotBlank(e.getSourceDetailId()))
                    .collect(Collectors.toList());
//            if (detailEntityList.stream().anyMatch(e -> StringUtils.isBlank(e.getSourceDetailId()))) {
//                throw new ServiceException("平台来源详情ID为空");
//            }
            List<SoB2cDetailEntity> detailEntityList = super.handleSplit(currentDetailEntityList, dto.isFalseDeliveryFlag());
            if (CollectionUtils.isEmpty(detailEntityList)) {
                log.warn("【虾皮标记发货】订单【{}】所有明细来源ID为空,不请求虾皮接口", mainEntity.getCode());
                continue;
            }

            //获取渠道标发单号
            String standardOrderType = tmsSignShipDTO.checkAndGetOrderDeliveryMarkType();
            String logisticsNo = StrUtil.equals(OrderDeliveryMarkTypeEnum.TRANSPORT_NO.getCode(), standardOrderType)
                    ? logisticsEntity.getCode() : logisticsEntity.getTrackNo();
            if (StrUtil.isBlank(logisticsNo)) {
                throw new ServiceException("【虾皮标记发货】操作失败，渠道标发单号为空");
            }
            ShipRequest shipRequest = ShipRequest.builder()
                    .host(host)
                    .token(shop_access_token)
                    .partnerId(partner_id)
                    .tmpPartnerKey(tmp_partner_key)
                    .shopId(shop_id)
                    .build();
            // 查询订单详情(获取子声明下标)
            ShipResponse shipResponse = shopeeLogisticsService.getShipping(shipRequest,mainEntity.getPlatformCode(), logisticsNo);
            if (Objects.isNull(shipResponse) || Objects.isNull(shipResponse.getResponse())){
                log.error("【虾皮标记发货】订单【{}】查询订单详情为空", mainEntity.getPlatformCode());
                throw new ServiceException("查询订单详情为空");
            }
            ShipDetailResponse response = shipResponse.getResponse();
            ShipDropInfo dropoff = response.getDropoff();
            if (Objects.isNull(dropoff) || CollectionUtils.isEmpty(dropoff.getBranchInfoList())){
                log.error("【虾皮标记发货】订单【{}】订单明细列表为空", mainEntity.getPlatformCode());
                throw new ServiceException("订单明细列表为空");
            }
            List<BranchInfo> branchInfoList = dropoff.getBranchInfoList();
            List<SlugInfo> slugInfoList = dropoff.getSlugInfoList();

            // 得到当前标记的子订单下标
            List<String> sourceDetailIds = detailEntityList.stream().map(SoB2cDetailEntity::getSourceDetailId).collect(Collectors.toList());
            List<Integer> subOrderIndexList = branchInfoList.stream()
                    .filter(e -> sourceDetailIds.contains(e.getBranchId()))
                    .map(BranchInfo::getBranchId)
                    .distinct()
                    .collect(Collectors.toList());
            if (CollectionUtils.isEmpty(subOrderIndexList)){
                log.error("【虾皮标记发货】订单【{}】数据异常未匹配到有效子订单下标: 需要标记的sourceDetailIds={}, 子订单={}",
                        mainEntity.getPlatformCode(),
                        sourceDetailIds,
                        subOrderIndexList
                );
                throw new ServiceException("【虾皮标记发货】订单【{}】数据异常未匹配到有效子订单下标");
            }

            // 源单信息明细
//            List<SoB2cDetailEntity> allSourceDetailEntityList = sourceDetailEntityMap.get(mainEntity.getPlatformCode());
//            if (CollectionUtils.isEmpty(allSourceDetailEntityList)){
//                throw new ServiceException("【虾皮标记发货】操作失败，原平台明细为空,平台单号=" + mainEntity.getCode());
//            }
            // 声明发货类型，all表示全部发货，part表示部分声明发货。
//            String sendType = convertSendType(detailEntityList, allSourceDetailEntityList);
            // 都是全部发货
            String sendType = "all";
            Dropoff dropoff1 = Dropoff.builder()
                    .branchId(1)
                    .build();
            ShipOrderRequest shipOrderRequest = ShipOrderRequest.builder()
                    .dropoff(dropoff1)
                    .build();
            // 非线上环境需要指定订单ID
            if (!BusinessCommonConstants.hasProfile("prod")) {
                List<DictBasicDTO.ListDTO> warehouseTypes = dictBasicService.getByKey("aliexpressAllowShipOrderId");
                if (CollectionUtils.isEmpty(warehouseTypes)) {
                    log.warn("【虾皮标记发货】【{}】不存在指定的订单ID配置,不请求虾皮接口:请求参数={}", mainEntity.getPlatformCode(), JSONUtil.toJsonStr(shipOrderRequest));
                    signShippedDetailList.addAll(detailEntityList.stream().map(BaseEntity::getId).collect(Collectors.toList()));
                    continue;
                }
                // 允许通过的ID
                DictBasicDTO.ListDTO configAllowPlatformOrderDTO = warehouseTypes.stream().filter(e -> mainEntity.getPlatformCode().equalsIgnoreCase(e.getValue())).findFirst().orElse(null);
                if (null == configAllowPlatformOrderDTO) {
                    log.warn("【虾皮标记发货】【{}】不属于配置指定的订单ID,不请求虾皮接口:请求参数={}", mainEntity.getPlatformCode(), JSONUtil.toJsonStr(shipOrderRequest));
                    signShippedDetailList.addAll(detailEntityList.stream().map(BaseEntity::getId).collect(Collectors.toList()));
                    continue;
                }
            }

            try {
                shopeeLogisticsService.shippingOrder(shipRequest,shipOrderRequest);
                signShippedDetailList.addAll(detailEntityList.stream().map(BaseEntity::getId).collect(Collectors.toList()));
            } catch (ServiceException e){
                if (-353 == e.getCode()) {
                    log.warn("【虾皮标记发货】销售订单【{}】,平台订单【{}】虾皮标记发货API提示重复操作(忽略) >>>>{}", mainEntity.getCode(), mainEntity.getPlatformCode(), ExceptionUtil.stacktraceToString(e));
                    return signShippedDetailList;
                }
                if (-999 == e.getCode() && e.getMessage().contains("系统已经重新路由")){
                    //更新跟踪号，重新申明下单
//                    return reShipOrder(e, detailEntityList, request, signShippedDetailList, mainEntity);
                }
                log.error("【虾皮标记发货】销售订单【{}】,平台订单【{}】虾皮标记发货API提示异常 >>>>{}", mainEntity.getCode(), mainEntity.getPlatformCode(), ExceptionUtil.stacktraceToString(e));
                throw new ServiceException("虾皮API标记发货失败:" + e.getMessage());
            } catch (Exception e) {
                log.error("【虾皮标记发货】销售订单【{}】,平台订单【{}】虾皮标记发货失败 >>>>{}", mainEntity.getCode(), mainEntity.getPlatformCode(), ExceptionUtil.stacktraceToString(e));
                throw new ServiceException("虾皮标记发货失败:" + e.getMessage());
            }
        }
        return signShippedDetailList;
    }

    @Override
    public Boolean deliveryIntercept(PlatformDeliveryInterceptDTO dto) {
        return null;
    }


    @Override
    public Boolean queryAndUpdateOrderStatus(PlatformDeliveryInterceptDTO dto) {
        return null;
    }

    @Override
    public Boolean asyncBatchQueryAndUpdateOrderStatus(List<PlatformOrderQueryDTO> dtoList){
        return null;
    }
}
