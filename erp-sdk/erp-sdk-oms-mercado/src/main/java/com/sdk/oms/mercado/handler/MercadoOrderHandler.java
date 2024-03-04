package com.sdk.oms.mercado.handler;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.common.business.annotation.BusinessType;
import com.common.business.annotation.PlatformCategoryType;
import com.common.business.annotation.PlatformType;
import com.common.business.dto.JobTaskDTO;
import com.common.business.dto.PlatformOrderDTO;
import com.common.business.enums.BusinessTypeEnum;
import com.common.business.enums.PlatformCategoryEnum;
import com.common.business.enums.PlatformDictEnum;
import com.common.business.handler.AbstractOrderHandler;
import com.common.business.utils.RedisUtil;
import com.common.core.controller.vo.ApiResult;
import com.common.core.utils.HttpCommonUtil;
import com.erp.model.dmp.enums.PlatformEnum;
import com.erp.rpc.dmp.feign.DmpTaskFeign;
import com.erp.rpc.oms.feign.ShopInfoFeign;
import com.sdk.oms.mercado.dto.MercadoOrderDTO;
import com.sdk.oms.mercado.dto.MercadoShopInfoDTO;
import com.sdk.oms.mercado.service.MercadoSdkClientService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.annotation.Resource;
import java.util.*;

@Slf4j
@Component
@PlatformCategoryType(PlatformCategoryEnum.THIRD_SYSTEM)
@PlatformType(PlatformDictEnum.SHOPEE)
@BusinessType(BusinessTypeEnum.ORDER)
public class MercadoOrderHandler extends AbstractOrderHandler<MercadoOrderDTO, PlatformOrderDTO> {
    @Resource
    private DmpTaskFeign dmpTaskFeign;
    @Resource
    private RedisUtil redisUtil;
    @Resource
    private ShopInfoFeign shopInfoFeign;
    @Resource
    private MercadoSdkClientService mercadoSdkClientService;

    public static void main(String[] args) {

        String baseUrl = "https://api.mercadolibre.com/marketplace/orders/search";


        //入参
        HashMap<String, Object> params = new HashMap<>(2);

        //设置请求头
        Map<String, String> headerMap = new HashMap<>(1);
        headerMap.put("Authorization", "Bearer "+ "");

        //拉取数据
        ApiResult apiResult = HttpCommonUtil.sendOkHttpApiResult(baseUrl, JSONUtil.toJsonStr(params), null, headerMap, RequestMethod.GET);
        if (!Objects.equals(apiResult.getCode(), 200)) {
            log.error("调用url={},入参params={}, 美客多items/search数据失败，返回值 responseMap={}", baseUrl, params.toString(), JSONUtil.toJsonStr(apiResult));
            throw new RuntimeException(StrUtil.format("调用url={},入参params={}, 美客多items/search数据失败，返回值 responseMap={}",
                    baseUrl, params.toString(), JSONUtil.toJsonStr(apiResult)));
        }

    }

    @Override
    public List<MercadoOrderDTO> download(JobTaskDTO data) {
        // Shopify订单下载
        MercadoShopInfoDTO shopInfoDTO = mercadoSdkClientService.getShopInfoByShopId(data.getShopId());
        if (null == shopInfoDTO) {
            log.error("[Shopify订单下载]从缓存中获取shopify token 失败: shopId={}", data.getShopId());
            return Collections.emptyList();
        }
        String shopifyShopDomain = shopInfoDTO.getShopDomain();
        String accessToken = shopInfoDTO.getAccessToken();

        /*ZoneOffset zoneOffset = ZoneOffset.systemDefault().getRules().getOffset(Instant.now());
        // 上次执行时间
        OffsetDateTime lastOffSetTime = data.getLastTime().atOffset(zoneOffset);
        // 下次执行时间
        OffsetDateTime nextOffSetTime = data.getNextTime().atOffset(zoneOffset);
        // 当前时间
//        OffsetDateTime nowOffSetTime = OffsetDateTime.now(ZoneId.systemDefault());
//        OffsetDateTime nowOffSetTime = null;

        // Shopify产品下载所有(SDK已分页查询所有)
        List<ShopifyOrder> orders = shopifyRestClientService.getShopifyRestClient(shopifyShopDomain, accessToken)
                .getAllUpdatedOrdersCreatedBefore(lastOffSetTime, nextOffSetTime, null);

        if (CollectionUtils.isEmpty(orders)) {
            return Collections.emptyList();
        }
        // 返回下载源数据
        return orders.stream()
                .map(e -> new PlatformShopifyOrderDTO(data, e, shopInfoDTO))
                .collect(Collectors.toList());*/

        return null;
    }

    @Override
    public List<PlatformOrderDTO> convert(List<MercadoOrderDTO> sourceDataList) {
        return null;
    }

    @Override
    public String getTargetPlatform() {
        return PlatformEnum.ERP.getDesc();
    }
}

