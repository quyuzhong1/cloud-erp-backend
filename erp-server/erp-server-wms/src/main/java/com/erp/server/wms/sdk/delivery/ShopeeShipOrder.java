package com.erp.server.wms.sdk.delivery;

import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.lang.Tuple;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.common.business.annotation.PlatformShipOrderAnno;
import com.common.business.constant.RedisCacheConstants;
import com.common.business.dto.PlatformDeliveryInterceptDTO;
import com.common.business.dto.PlatformOrderQueryDTO;
import com.common.business.dto.PlatformShipOrderDTO;
import com.common.business.enums.OrderDeliveryMarkTypeEnum;
import com.common.business.enums.PlatformDictEnum;
import com.common.business.utils.RedisUtil;
import com.common.core.controller.vo.ApiResult;
import com.common.core.entity.BaseEntity;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.erp.model.dmp.dto.CfgAppClientDTO;
import com.erp.model.dmp.entity.CfgAppClientEntity;
import com.erp.model.dmp.enums.AppClientEnum;
import com.erp.model.oms.entity.*;
import com.erp.model.tms.dto.LogisticsChannelDTO;
import com.erp.rpc.dmp.feign.DmpTaskFeign;
import com.erp.rpc.oms.feign.ShopeeFeign;
import com.erp.rpc.tms.feign.LogisticsFeign;
import com.erp.server.wms.service.DictBasicService;
import com.sdk.oms.shopee.dto.base.ShopeeResponse;
import com.sdk.oms.shopee.dto.logistics.request.Dropoff;
import com.sdk.oms.shopee.dto.logistics.request.Integrated;
import com.sdk.oms.shopee.dto.logistics.request.ShipOrderRequest;
import com.sdk.oms.shopee.dto.logistics.request.ShipRequest;
import com.sdk.oms.shopee.dto.logistics.response.*;
import com.sdk.oms.shopee.service.ShopeeLogisticsService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Component
@PlatformShipOrderAnno(method = PlatformDictEnum.SHOPEE)
public class ShopeeShipOrder extends AbstractShipOrder {
    @Resource
    private ShopeeLogisticsService shopeeLogisticsService;
    @Resource
    private DictBasicService dictBasicService;
    @Resource
    private RedisUtil redisUtil;
    @Resource
    private ShopeeFeign shopeeFiegn;
    @Resource
    private DmpTaskFeign dmpTaskFeign;
    @Resource
    private LogisticsFeign logisticsFeign;
    @Override
    public List<String> shipOrder(PlatformShipOrderDTO dto) {
        // 查询所有信息
        Tuple tuple = super.allSourceOrderInfo(dto);
        // 所有源单信息
        List<SoB2cEntity> sourceOrderList = tuple.get(0);
        // 对应明细
        Map<String, List<SoB2cDetailEntity>> soB2cDetailEntityListMap = tuple.get(1);
        // 当前单据物流信息
        SoB2cLogisticsEntity logisticsEntity = tuple.get(2);
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
            if (CollectionUtils.isEmpty(currentDetailEntityList)) {
                log.warn("订单【{}】所有明细来源ID为空,不请求接口", mainEntity.getCode());
                continue;
            }
            if (currentDetailEntityList.stream().anyMatch(e -> StringUtils.isBlank(e.getSourceDetailId()))) {
                throw new ServiceException("平台来源详情ID为空");
            }
            List<SoB2cDetailEntity> detailEntityList = super.handleSplit(currentDetailEntityList, dto.isFalseDeliveryFlag());
            if (CollectionUtils.isEmpty(detailEntityList)) {
                log.warn("【虾皮标记发货】订单【{}】所有明细来源ID为空,不请求虾皮接口", mainEntity.getCode());
                continue;
            }
            Map<String, SoB2cDetailEntity> detailEntityMap = detailEntityList.stream().collect(Collectors.toMap(SoB2cDetailEntity::getSourceDetailId, Function.identity()));
            log.warn("[虾皮标记发货] 平台订单号【{}】,当前提交明细IDS:{}", mainEntity.getPlatformCode(), JSONUtil.toJsonStr(detailEntityMap.keySet()));
            String shopId = mainEntity.getShopId();
            ShipRequest shipRequest = getShopeeAuthByShopId(shopId);
            if (Objects.isNull(shipRequest)) {
                log.error("[虾皮标记发货]从缓存中获取 虾皮店铺 token 失败: shopId={}", shopId);
                throw new ServiceException();
            }
            String packageNumber = "";
            if (StrUtil.isNotBlank(mainEntity.getLabelJson())){
                JSONObject jsonObject = JSONUtil.parseObj(mainEntity.getLabelJson());
                packageNumber = jsonObject.getStr("package_number");
            }
            // 查询订单详情(获取子声明
            // 下标)
            ShipResponse shipResponse = shopeeLogisticsService.getShipping(shipRequest,mainEntity.getPlatformCode(), packageNumber);
            if (Objects.isNull(shipResponse) || Objects.isNull(shipResponse.getResponse())){
                log.error("【虾皮标记发货】订单【{}】查询订单详情为空", mainEntity.getPlatformCode());
                throw new ServiceException("查询订单详情为空");
            }
            ShipDetailResponse response = shipResponse.getResponse();
            ShipDropInfo dropoff = response.getDropoff();
            ShipInfo infoNeeded = response.getInfoNeeded();
            if ((Objects.isNull(dropoff) || CollectionUtils.isEmpty(dropoff.getBranchInfoList())) && Objects.isNull(infoNeeded)){
                log.error("【虾皮标记发货】订单【{}】订单明细列表为空", mainEntity.getPlatformCode());
                throw new ServiceException("订单明细列表为空");
            }
            //获取到的
            List<BranchInfo> branchInfoList = dropoff.getBranchInfoList();
            List<SlugInfo> slugInfoList = dropoff.getSlugInfoList();
            //获取渠道标发单号
            String standardOrderType = tmsSignShipDTO.checkAndGetOrderDeliveryMarkType();
            String logisticsNo = StrUtil.equals(OrderDeliveryMarkTypeEnum.TRANSPORT_NO.getCode(), standardOrderType)
                    ? logisticsEntity.getCode() : logisticsEntity.getTrackNo();
            if (StrUtil.isBlank(logisticsNo)) {
                throw new ServiceException("【虾皮标记发货】操作失败，渠道标发单号为空");
            }
            // 得到当前标记的子订单下标
//            List<String> sourceDetailIds = detailEntityList.stream().map(SoB2cDetailEntity::getSourceDetailId).collect(Collectors.toList());
//            List<Integer> subOrderIndexList = branchInfoList.stream()
//                    .filter(e -> sourceDetailIds.contains(e.getBranchId()))
//                    .map(BranchInfo::getBranchId)
//                    .distinct()
//                    .collect(Collectors.toList());
//            if (CollectionUtils.isEmpty(subOrderIndexList)){
//                log.error("【虾皮标记发货】订单【{}】数据异常未匹配到有效子订单下标: 需要标记的sourceDetailIds={}, 子订单={}",
//                        mainEntity.getPlatformCode(),
//                        sourceDetailIds,
//                        subOrderIndexList
//                );
//                throw new ServiceException("【虾皮标记发货】订单【{}】数据异常未匹配到有效子订单下标");
//            }

            // 都是全部发货
            Dropoff dropoff1 = Dropoff.builder()
                    .branchId(branchInfoList.get(0).getBranchId())
                    .senderRealName(tmsSignShipDTO.getLogisticsChannelName())
                    .slug(slugInfoList.get(0).getSlug())
                    .trackingNumber(logisticsNo)
                    .build();
            Integrated nonIntegrated = Integrated.builder().trackingNumber(logisticsNo).build();
            ShipOrderRequest shipOrderRequest = ShipOrderRequest.builder()
                    .orderSn(mainEntity.getPlatformCode())
                    .packageNumber(packageNumber)
                    .dropoff(dropoff1)
                    .nonIntegrated(nonIntegrated)
                    .build();
            try {
                ShopeeResponse shopeeResponse = shopeeLogisticsService.shippingOrder(shipRequest, shipOrderRequest);
                signShippedDetailList.addAll(detailEntityList.stream().map(BaseEntity::getId).collect(Collectors.toList()));
            } catch (ServiceException e){
                log.error("【虾皮标记发货】销售订单【{}】,平台订单【{}】虾皮标记发货API提示异常 >>>>{}", mainEntity.getCode(), mainEntity.getPlatformCode(), ExceptionUtil.stacktraceToString(e));
                throw new ServiceException("虾皮API标记发货失败:" + e.getMessage());
            } catch (Exception e) {
                log.error("【虾皮标记发货】销售订单【{}】,平台订单【{}】虾皮标记发货失败 >>>>{}", mainEntity.getCode(), mainEntity.getPlatformCode(), ExceptionUtil.stacktraceToString(e));
                throw new ServiceException("虾皮标记发货失败:" + e.getMessage());
            }
        }
        return signShippedDetailList;
    }

    private ShipRequest getShopeeAuthByShopId(String shopId) {
        String tokenKey = StrUtil.format(RedisCacheConstants.REDIS_PLATFORM_TOKEN, PlatformDictEnum.SHOPEE.getCode(), shopId);
        // 缓存获取
        Object tokenObj = redisUtil.get(tokenKey);
        if (null != tokenObj) {
            if (tokenObj instanceof ShipRequest) {
                return (ShipRequest) tokenObj;
            }
        }else {
            ApiResult<ShopAuthEntity> shopeeShopById = shopeeFiegn.getShopeeShopById(shopId);
            CfgAppClientDTO.FindDTO findDTO = new CfgAppClientDTO.FindDTO();
            AppClientEnum appClientEnum = AppClientEnum.SHOPEE_ACCESS_TOKEN;
            findDTO.setBusinessType(appClientEnum.getBusinessType());
            findDTO.setDictPlatform(appClientEnum.getPlatform());
            findDTO.setPlatformType(appClientEnum.getPlatformType());
            CfgAppClientEntity cfgAppClient = null;
            try {
                cfgAppClient = dmpTaskFeign.getCfgAppClient(findDTO);
            } catch (Exception e) {
                log.error("erp-dmp服务获取虾皮配置信息异常：{}", e.getMessage());
            }
            if (Objects.isNull(cfgAppClient)) {
                return null;
            }

            if (Objects.nonNull(shopeeShopById) && Objects.nonNull(shopeeShopById.getData())
                    && "shopee_shop".equalsIgnoreCase(shopeeShopById.getData().getType())) {
                ShipRequest orderRequest = ShipRequest.builder()
                        .tmpPartnerKey(cfgAppClient.getClientSecret())
                        .partnerId(Long.parseLong(cfgAppClient.getClientId()))
                        .token(shopeeShopById.getData().getAccessToken())
                        .shopId(Long.parseLong(shopeeShopById.getData().getShopeeId()))
                        .host(cfgAppClient.getUrl())
                        .build();
                redisUtil.set(tokenKey, orderRequest, shopeeShopById.getData().getExpiresIn());
                return orderRequest;
            }
        }
        return null;
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
