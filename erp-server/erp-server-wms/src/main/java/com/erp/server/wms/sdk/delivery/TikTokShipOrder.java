package com.erp.server.wms.sdk.delivery;

import cn.hutool.core.lang.Tuple;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.common.business.annotation.PlatformShipOrderAnno;
import com.common.business.dto.PlatformDeliveryInterceptDTO;
import com.common.business.dto.PlatformOrderQueryDTO;
import com.common.business.dto.PlatformShipOrderDTO;
import com.common.business.enums.OrderDeliveryMarkTypeEnum;
import com.common.business.enums.PlatformDictEnum;
import com.common.business.service.IPlatformService;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.HttpCommonUtil;
import com.erp.model.oms.dto.SoB2cDTO;
import com.erp.model.oms.dto.SoB2cDetailDTO;
import com.erp.model.oms.entity.SoB2cDetailEntity;
import com.erp.model.oms.entity.SoB2cEntity;
import com.erp.model.oms.entity.SoB2cLogisticsEntity;
import com.erp.model.oms.enums.SoB2cSourcePlatformEnum;
import com.erp.model.tms.dto.LogisticsChannelDTO;
import com.erp.rpc.oms.feign.ShopInfoFeign;
import com.erp.rpc.oms.feign.SoB2cFeign;
import com.erp.rpc.tms.feign.LogisticsFeign;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sdk.oms.tiktok.constant.TikTokConstant;
import com.sdk.oms.tiktok.dto.TikTokShopInfoDTO;
import com.sdk.oms.tiktok.dto.tiktok.ship.SelfShipmentBean;
import com.sdk.oms.tiktok.dto.tiktok.ship.ShipOrderOtherParam;
import com.sdk.oms.tiktok.dto.tiktok.ship.ShipOrderUS;
import com.sdk.oms.tiktok.dto.tiktok.ship.ShipOrderUSParam;
import com.sdk.oms.tiktok.service.TikTokSdkClientService;
import com.sdk.oms.tiktok.util.EncryptionUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.checkerframework.checker.units.qual.A;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
@PlatformShipOrderAnno(method = PlatformDictEnum.TIK_TOK)
public class TikTokShipOrder extends AbstractShipOrder {

    @Resource
    private LogisticsFeign logisticsFeign;

    @Resource
    private TikTokSdkClientService tikTokSdkClientService;
    public static void main(String[] args) {
        String url = TikTokConstant.URL;
        String path = "/fulfillment/" + TikTokConstant.VERSION + "/orders/" + "576714980457877909" + "/packages";
        String clientSecret = "8ff628de24faf70c24855de4d967fb6a17a47e3f";
        String clientId = "6buinkjt3hmld";
        String shopCipher = "TTP_pEhpJwAAAADvOkDJ2jIoaS9Uak191t0d";
        String token = "ROW_GwP5NAAAAACj-JAAAriAWjVtF2MrUIFdiwpvtmvHXedAYA9cevCkZepCOiMyd4q0eyFfSnzeQNSPiYsbBmXgfkz3-MVFEcyD6QqmkIhMBjdTRnBo-Bw7DGPY7IOCJaPkSrfdNZzBNU8CEErOfMSN6MZ1W5t4afUtvZMR-kf9UrZgKZlXLXi8GQ";
        // 定义查询参数
        Map<String, Object> params = new HashMap<>();
        params.put("access_token", token);
        params.put("app_key", clientId);
        params.put("shop_cipher", shopCipher);
        Long timestamp = System.currentTimeMillis() / 1000;
        params.put("timestamp", timestamp);
        params.put("version", TikTokConstant.VERSION);

        //设置请求头
        Map<String, String> headerMap = new HashMap<>(2);
        headerMap.put("x-tts-access-token", token);
        headerMap.put("content-type", "application/json");

        Gson gson = new GsonBuilder()
                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .create();

        List<String> sourceDetailIds = new ArrayList<>();
        sourceDetailIds.add("576714980458140053");

        ShipOrderUSParam paramDTO = new ShipOrderUSParam();
        paramDTO.setTrackingNumber("9212490357610605711333");
        paramDTO.setOrderLineItemIds(sourceDetailIds);
        paramDTO.setShippingProviderId("USPS-BPARCEL");
        String bodyJson = gson.toJson(paramDTO);

        //组装入参排序计算签名字符串
        String input = EncryptionUtils.urlParamsSort(params, path, headerMap, clientSecret, bodyJson);

        // 追加请求路径获取签名
        String sign = EncryptionUtils.generateSHA256(input, clientSecret);

        //加入sign签名入参
        params.put("sign", sign);

        //组装url
        StringBuffer sb = new StringBuffer();
        sb.append(url);
        sb.append(path);
        sb.append("?access_token=" + token + "");
        sb.append("&app_key=" + clientId + "");
        sb.append("&shop_cipher=" + shopCipher);
        sb.append("&sign=" + sign + "");
        sb.append("&timestamp=" + timestamp + "");
        sb.append("&version=" + TikTokConstant.VERSION + "");
        //拉取数据
        ApiResult apiResult = HttpCommonUtil.sendOkHttpApiResult(sb.toString(), bodyJson, null, headerMap, RequestMethod.POST);
        if (!Objects.equals(apiResult.getCode(), 200) && !Objects.equals(apiResult.getCode(), 201)) {
            log.error("调用url={},入参params={}, TikTok订单发货（美国站）失败，返回值 responseMap={}", sb.toString(), params.toString(), JSONUtil.toJsonStr(apiResult));
            throw new RuntimeException(CharSequenceUtil.format("调用url={},入参params={}, TikTok订单发货（美国站）失败，返回值 responseMap={}",
                    sb.toString(), headerMap.toString(), JSONUtil.toJsonStr(apiResult)));
        }
        //解析数据
        ShipOrderUS shipOrderUS = null;
        try {
            shipOrderUS = JSONUtil.toBean(JSONUtil.toJsonStr(apiResult.getData()), ShipOrderUS.class);
        } catch (Exception e) {
            throw new RuntimeException(CharSequenceUtil.format("调用url={},入参params={}, TikTok订单发货（美国站）返回值 responseMap={}，转换成实体错误", apiResult.getData()));
        }
    }

    @Override
    public List<String> shipOrder(PlatformShipOrderDTO dto) {
        Tuple tuple = super.allSourceOrderInfo(dto);
        // 所有源单信息
        List<SoB2cEntity> sourceOrderList = tuple.get(0);
        // 对应明细
        Map<String, List<SoB2cDetailEntity>> soB2cDetailEntityListMap = tuple.get(1);
        // 当前单据物流信息
        SoB2cLogisticsEntity logisticsEntity = tuple.get(2);

        List<String> resultDetailIds = new ArrayList<>();

        for (SoB2cEntity entity : sourceOrderList) {
            //检查销售订单详情是否存在
            List<SoB2cDetailEntity> detailEntityList = soB2cDetailEntityListMap.get(entity.getId());
            if (CollectionUtils.isEmpty(detailEntityList)) {
                throw new ServiceException(ApiError.ERROR_SO_B2C_DETAIL_NOT_EXIST);
            }
            // 校验捆绑商品拆分
            // 来源明细ID为空代表是手工添加的明细忽略
            detailEntityList =  detailEntityList.stream()
                    .filter(e -> CharSequenceUtil.isNotBlank(e.getPlatformLineNumber())).distinct()
                    .collect(Collectors.toList());
            if (CollectionUtils.isEmpty(detailEntityList)) {
                log.warn("订单【{}】所有明细来源ID为空,不请求接口", entity.getCode());
                continue;
            }
            List<String> sourceDetailIds = new ArrayList<>();
            for (SoB2cDetailEntity soB2cDetailEntity : detailEntityList) {
                String[] split = soB2cDetailEntity.getPlatformLineNumber().split(",");
                if (split.length > 0) {
                    sourceDetailIds.addAll(Arrays.asList(split));
                }
            }
            List<String> detailIdList = detailEntityList.stream().map(SoB2cDetailEntity::getId).collect(Collectors.toList());

            TikTokShopInfoDTO tikTokShopInfoDTO = tikTokSdkClientService.getShopInfoByShopId(entity.getShopId());

            //获取销售渠道信息
            LogisticsChannelDTO.SignShipDTO tmsScaleChannelShipDTO = logisticsFeign.getScaleChannelByChannelById(
                    logisticsEntity.getLogisticsChannelId(),
                    PlatformDictEnum.TIK_TOK.getCode()
            );
            if (null == tmsScaleChannelShipDTO){
                throw new ServiceException("找不到渠道信息");
            }
            //获取渠道标发单号
            String standardOrderType = tmsScaleChannelShipDTO.checkAndGetOrderDeliveryMarkType();
            String trackingNumber = CharSequenceUtil.equals(OrderDeliveryMarkTypeEnum.TRANSPORT_NO.getCode(),standardOrderType)
                    ? logisticsEntity.getCode() : logisticsEntity.getTrackNo();
            if (CharSequenceUtil.isBlank(trackingNumber)) {
                throw new ServiceException("操作失败，渠道标发单号为空");
            }

            if ("US".equalsIgnoreCase(tikTokShopInfoDTO.getSite())) {
                ShipOrderUSParam paramDTO = new ShipOrderUSParam();
                paramDTO.setTrackingNumber(trackingNumber);
                paramDTO.setOrderLineItemIds(sourceDetailIds);
                paramDTO.setShippingProviderId(tmsScaleChannelShipDTO.getCode());
                ShipOrderUS shipOrderUS = tikTokSdkClientService.sendTikTokShipOrderUS(tikTokShopInfoDTO, entity.getPlatformCode(), paramDTO);
                resultDetailIds.addAll(detailIdList);
                if (shipOrderUS.getCode() != 0) {
                    if (!"Package has been shipped. Please not ship the package again.".equalsIgnoreCase(shipOrderUS.getMessage())
                            && !"fulfillment not allow forward".equalsIgnoreCase(shipOrderUS.getMessage())) {
                        throw new ServiceException(shipOrderUS.getMessage());
                    }
                }

            } else {
                for (SoB2cDetailEntity detailEntity : detailEntityList) {
                    ShipOrderOtherParam paramDTO = new ShipOrderOtherParam();
                    SelfShipmentBean selfShipmentBean = new SelfShipmentBean();
                    selfShipmentBean.setTrackingNumber(trackingNumber);
                    selfShipmentBean.setShippingProviderId(tmsScaleChannelShipDTO.getCode());
                    paramDTO.setSelfShipment(selfShipmentBean);
                    tikTokSdkClientService.sendTikTokShipOrderOther(tikTokShopInfoDTO, detailEntity.getPlatformPackageId(), paramDTO);
                }
                resultDetailIds.addAll(detailIdList);
            }
        }
        return resultDetailIds;
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
    public Boolean asyncBatchQueryAndUpdateOrderStatus(List<PlatformOrderQueryDTO> dtoList) {
        return null;
    }
}
