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
import com.sdk.oms.mercado.dto.mercado.order.OrderViewDTO;
import com.sdk.oms.mercado.service.MercadoSdkClientService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
@PlatformCategoryType(PlatformCategoryEnum.THIRD_SYSTEM)
@PlatformType(PlatformDictEnum.MERCADO)
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
/*        String baseUrl = "https://api.mercadolibre.com/marketplace/orders/search";


        //入参
        HashMap<String, Object> params = new HashMap<>(2);
        //设置请求头
        Map<String, String> headerMap = new HashMap<>(1);
        headerMap.put("Authorization", "Bearer "+ "APP_USR-3457166802805723-031403-f51fe8b29ba502ea781d0ec93f2f3a08-1509269799");

        //拉取数据
        ApiResult apiResult = HttpCommonUtil.sendOkHttpApiResult(baseUrl, JSONUtil.toJsonStr(params), null, headerMap, RequestMethod.GET);
        if (!Objects.equals(apiResult.getCode(), 200)) {
            log.error("调用url={},入参params={}, 美客多items/search数据失败，返回值 responseMap={}", baseUrl, params.toString(), JSONUtil.toJsonStr(apiResult));
            throw new RuntimeException(StrUtil.format("调用url={},入参params={}, 美客多items/search数据失败，返回值 responseMap={}",
                    baseUrl, params.toString(), JSONUtil.toJsonStr(apiResult)));
        }
        System.out.println(apiResult.getData());*/


/*        String baseUrl2 = "https://api.mercadolibre.com/marketplace/orders/2000007633674134";

        //入参
        HashMap<String, Object> params2 = new HashMap<>(2);
        //设置请求头
        Map<String, String> headerMap2 = new HashMap<>(1);
        headerMap2.put("Authorization", "Bearer "+ "APP_USR-3457166802805723-030505-67f461af1a1b82d19d1f0463aeb98d4a-1509269799");

        //拉取数据
        ApiResult apiResult2 = HttpCommonUtil.sendOkHttpApiResult(baseUrl2, JSONUtil.toJsonStr(params2), null, headerMap2, RequestMethod.GET);
        if (!Objects.equals(apiResult2.getCode(), 200)) {
            log.error("调用url={},入参params={}, 美客多items/search数据失败，返回值 responseMap={}", baseUrl2, params2.toString(), JSONUtil.toJsonStr(apiResult2));
            throw new RuntimeException(StrUtil.format("调用url={},入参params={}, 美客多items/search数据失败，返回值 responseMap={}",
                    baseUrl2, params2.toString(), JSONUtil.toJsonStr(apiResult2)));
        }*/

//        String shippingUrl = "http://api.mercadolibre.com/marketplace/shipments/43116658829?access_token=APP_USR-3457166802805723-030522-d406385a02d509c2ecc8a9234e425f34-1509269799&seller=1511265855";
        String shippingUrl1 = "https://api.mercadolibre.com/marketplace/shipments/43106673373";
        String shippingUrl2 = "https://api.mercadolibre.com/marketplace/shipments/43116658829/costs";
        String shippingUrl3 = "http://api.mercadolibre.com/marketplace/shipments/43106673373/labels";
        String shippingUrl4 = "http://api.mercadolibre.com/marketplace/shipments/43116658829/tracking";
        String shippingUrl5 = "https://api.mercadolibre.com/marketplace/shipments/43116658829";
        String shippingUrl6 = "https://api.mercadolibre.com/marketplace/shipments/43116658829/history";

        //入参
        HashMap<String, Object> shippingParams = new HashMap<>(1);
        shippingParams.put("tracking_id","1");
        shippingParams.put("carrier","name carrier");

        //设置请求头

        Map<String, String> shippingHeaderMap = new HashMap<>(1);
        shippingHeaderMap.put("Authorization", "Bearer "+ "APP_USR-3457166802805723-031323-518d575dc107a24dd7710ce58ff860b8-1509269799");

        //拉取数据
        ApiResult apiResult2 = HttpCommonUtil.sendOkHttpApiResult(shippingUrl4, null, null, shippingHeaderMap, RequestMethod.GET);
        if (!Objects.equals(apiResult2.getCode(), 200)) {
            log.error("调用url={},入参params={}, 美客多items/search数据失败，返回值 responseMap={}", shippingUrl3, shippingParams.toString(), JSONUtil.toJsonStr(apiResult2));
            throw new RuntimeException(StrUtil.format("调用url={},入参params={}, 美客多items/search数据失败，返回值 responseMap={}",
                    shippingUrl3, shippingParams.toString(), JSONUtil.toJsonStr(apiResult2)));
        }

        System.out.println(JSONUtil.toJsonStr(apiResult2.getData()));

    }

    @Override
    public List<MercadoOrderDTO> download(JobTaskDTO task) {
        // Shopify订单下载
        MercadoShopInfoDTO shopInfoDTO = mercadoSdkClientService.getShopInfoByShopId(task.getShopId());
        if (null == shopInfoDTO) {
            log.error("[Shopify订单下载]从缓存中获取shopify token 失败: shopId={}", task.getShopId());
            return Collections.emptyList();
        }


        //发送请求
        List<OrderViewDTO> orders = mercadoSdkClientService.sendMercadoGetOrder(shopInfoDTO, task);

        // 返回下载源数据
        return orders.stream()
                .map(e -> new MercadoOrderDTO(e, task, shopInfoDTO.getId()))
                .collect(Collectors.toList());
    }

    @Override
    public List<PlatformOrderDTO> convert(List<MercadoOrderDTO> sourceDataList) {
        // 包含数据过滤数据 数据转换 数据合并拆分等操作
        return sourceDataList.stream()
                // 组装
                .map(MercadoOrderDTO::convertDTO).collect(Collectors.toList());
    }

    @Override
    public String getTargetPlatform() {
        return PlatformEnum.ERP.getDesc();
    }
}

