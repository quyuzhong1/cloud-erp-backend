package com.sdk.oms.mercado.handler;

import cn.hutool.core.util.ObjectUtil;
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
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sdk.oms.mercado.constant.MercadoConstant;
import com.sdk.oms.mercado.dto.MercadoOrderDTO;
import com.sdk.oms.mercado.dto.MercadoShopInfoDTO;
import com.sdk.oms.mercado.dto.mercado.cost.CostDTO;
import com.sdk.oms.mercado.dto.mercado.order.OrderDTO;
import com.sdk.oms.mercado.dto.mercado.order.OrderViewDTO;
import com.sdk.oms.mercado.dto.mercado.order.OrdersBean;
import com.sdk.oms.mercado.dto.mercado.order.ResultsBean;
import com.sdk.oms.mercado.dto.mercado.shipment.ShipmentViewDTO;
import com.sdk.oms.mercado.service.MercadoSdkClientService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
@PlatformCategoryType(PlatformCategoryEnum.THIRD_SYSTEM)
@PlatformType(PlatformDictEnum.MERCADOLIBRE)
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



        String url = MercadoConstant.URL;
        String path = "/marketplace/orders/2000006225879447";
        //每次最多获取200条
        Integer pageSize = 200;
        //当前页数
        Integer pageNo = 0;
        //总页数
        Integer pageCount = 1;

        List<OrderViewDTO> resultList = new ArrayList<>();

        while (pageNo < pageCount) {

            //入参
            HashMap<String, Object> params = new HashMap<>(2);
//            params.put("seller.id", "1511265855");
//            params.put("seller.id", shopInfoDTO.getUserId());
            //设置请求头
            Map<String, String> headerMap = new HashMap<>(1);
            headerMap.put("Authorization", "Bearer " + "APP_USR-3457166802805723-082902-95af1fbcbc57490cafb6081deaca410e-1509269799");

            //拉取数据
            ApiResult apiResult = HttpCommonUtil.sendOkHttpApiResult(url + path, JSONUtil.toJsonStr(params), null, headerMap, RequestMethod.GET);
            if (!Objects.equals(apiResult.getCode(), 200) && !Objects.equals(apiResult.getCode(), 201)) {
                log.error("调用url={},入参params={}, 美客多marketplace/orders/search数据失败，返回值 responseMap={}", url + path, params.toString(), JSONUtil.toJsonStr(apiResult));
                throw new RuntimeException(StrUtil.format("调用url={},入参params={}, 数据解析失败，返回值 responseMap={}",
                        url + path, params.toString(), JSONUtil.toJsonStr(apiResult)));
            }
            ObjectMapper objectMapper = new ObjectMapper();
            OrderDTO orderDTO = null;
            try {
                orderDTO = objectMapper.readValue(JSONUtil.toJsonStr(apiResult.getData()), OrderDTO.class);
            } catch (JsonProcessingException e) {
                log.error("美客多orders/search接口数据解析错误，数据={}", apiResult.getData());
                throw new RuntimeException(StrUtil.format("调用url={},入参params={}, 数据解析失败，返回值 responseMap={}",
                        url + path, params.toString(), JSONUtil.toJsonStr(apiResult)));
            }
            //解析数据
//            OrderDTO orderDTO = JSONUtil.toBean(JSONUtil.toJsonStr(apiResult.getData()), OrderDTO.class);
            if (CollectionUtils.isEmpty(orderDTO.getResults())) {
                break;
            }

            for (ResultsBean result : orderDTO.getResults()) {
                for (OrdersBean order : result.getOrders()) {
                    System.out.println(order.getFid());
                }

            }
            pageCount = (orderDTO.getPaging().getTotal() + pageSize - 1) / pageSize;
            pageNo++;

        }
    }

    @Override
    public List<MercadoOrderDTO> download(JobTaskDTO task) {
        // Shopify订单下载
        MercadoShopInfoDTO shopInfoDTO = mercadoSdkClientService.getShopInfoByShopId(task.getShopId());
        if (null == shopInfoDTO) {
            log.error("[美客多订单下载]从缓存中获取美客多 token 失败: shopId={}", task.getShopId());
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

