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

     /*   String baseUrl = "https://api.mercadolibre.com/marketplace/orders/search";


        //入参
        HashMap<String, Object> params = new HashMap<>(2);
        params.put("seller.id", "1511265855");
        params.put("limit", "1");
        params.put("offset", "1");
        //设置请求头
        Map<String, String> headerMap = new HashMap<>(1);
        headerMap.put("Authorization", "Bearer "+ "APP_USR-3457166802805723-030421-7ae5abf29d54cfe1274eedf82f2398d6-1509269799");

        //拉取数据
        ApiResult apiResult = HttpCommonUtil.sendOkHttpApiResult(baseUrl, JSONUtil.toJsonStr(params), null, headerMap, RequestMethod.GET);
        if (!Objects.equals(apiResult.getCode(), 200)) {
            log.error("调用url={},入参params={}, 美客多items/search数据失败，返回值 responseMap={}", baseUrl, params.toString(), JSONUtil.toJsonStr(apiResult));
            throw new RuntimeException(StrUtil.format("调用url={},入参params={}, 美客多items/search数据失败，返回值 responseMap={}",
                    baseUrl, params.toString(), JSONUtil.toJsonStr(apiResult)));
        }*/


        String baseUrl2 = "https://api.mercadolibre.com/marketplace/orders/2000007633674134";

        //入参
        HashMap<String, Object> params2 = new HashMap<>(2);
        //设置请求头
        Map<String, String> headerMap2 = new HashMap<>(1);
        headerMap2.put("Authorization", "Bearer "+ "APP_USR-3457166802805723-030421-7ae5abf29d54cfe1274eedf82f2398d6-1509269799");

        //拉取数据
        ApiResult apiResult2 = HttpCommonUtil.sendOkHttpApiResult(baseUrl2, JSONUtil.toJsonStr(params2), null, headerMap2, RequestMethod.GET);
        if (!Objects.equals(apiResult2.getCode(), 200)) {
            log.error("调用url={},入参params={}, 美客多items/search数据失败，返回值 responseMap={}", baseUrl2, params2.toString(), JSONUtil.toJsonStr(apiResult2));
            throw new RuntimeException(StrUtil.format("调用url={},入参params={}, 美客多items/search数据失败，返回值 responseMap={}",
                    baseUrl2, params2.toString(), JSONUtil.toJsonStr(apiResult2)));
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
        return null;
    }

    @Override
    public String getTargetPlatform() {
        return PlatformEnum.ERP.getDesc();
    }
}

