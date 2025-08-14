package com.sdk.oms.mercadolocal.service;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.common.business.constant.RedisCacheConstants;
import com.common.business.dto.JobTaskDTO;
import com.common.business.enums.PlatformDictEnum;
import com.common.business.utils.RedisUtil;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.HttpCommonUtil;
import com.common.core.utils.OkHttpUtils;
import com.erp.model.dmp.dto.CfgAppClientDTO;
import com.erp.model.dmp.entity.CfgAppClientEntity;
import com.erp.model.dmp.enums.AppClientEnum;
import com.erp.model.oms.dto.ShopDTO;
import com.erp.model.oms.entity.ShopAuthEntity;
import com.erp.model.oms.entity.ShopInfoEntity;
import com.erp.rpc.dmp.feign.DmpTaskFeign;
import com.erp.rpc.oms.feign.ShopInfoFeign;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.sdk.oms.mercadolocal.constant.MercadoConstant;
import com.sdk.oms.mercadolocal.dto.MercadoInvoiceDTO;
import com.sdk.oms.mercadolocal.dto.MercadoShipOrderDTO;
import com.sdk.oms.mercadolocal.dto.MercadoShopInfoDTO;
import com.sdk.oms.mercadolocal.dto.mercadolocal.PlatformMercadoRefreshTokenDTO;
import com.sdk.oms.mercadolocal.dto.mercadolocal.PlatformMercadoTokenDTO;
import com.sdk.oms.mercadolocal.dto.mercadolocal.cost.CostDTO;
import com.sdk.oms.mercadolocal.dto.mercadolocal.listing.ListingDTO;
import com.sdk.oms.mercadolocal.dto.mercadolocal.listing.ListingViewDTO;
import com.sdk.oms.mercadolocal.dto.mercadolocal.order.OrderDataDTO;
import com.sdk.oms.mercadolocal.dto.mercadolocal.order.OrderViewDTO;
import com.sdk.oms.mercadolocal.dto.mercadolocal.shipment.ShipmentViewDTO;
import jodd.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RequestMethod;
import org.thymeleaf.util.StringUtils;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 美客多平台SDK
 *
 * @Author Luo_WG
 * @Date 2024/2/27 18:04
 **/
@Slf4j
@Component
public class MercadoLocalSdkClientService {

//    public static void main(String[] args) {
//
//        String accessToken = "APP_USR-5344160433223219-041806-588ef91706f1afe3e367873d31517ced-2119968271";
//        //组装授权url
//        String url = MercadoConstant.URL;
//        String path = "/orders/2000011159453786";
//        StringBuffer sb = new StringBuffer();
//        sb.append(url);
//        sb.append(path);
//        //入参
//        //设置请求头
//        Map<String, String> headerMap = new HashMap<>(1);
//        headerMap.put("Authorization", "Bearer " +accessToken);
////        headerMap.put("Content-Type", "application/xml");
//
//        //拉取数据
//        ApiResult apiResult = new ApiResult();
//        apiResult = HttpCommonUtil.sendOkHttpApiResult(sb.toString(), "", null, headerMap, RequestMethod.GET);
//        if (!Objects.equals(apiResult.getCode(), 200) && !Objects.equals(apiResult.getCode(), 201)) {
//            System.out.println(apiResult.getData());
//        }
//
//    }
    public static void main(String[] args) {

        String accessToken = "APP_USR-8670168511142898-071521-847aaf5a4146d0393292cc6172452e50-2277013170";
        //组装授权url
        String url = MercadoConstant.URL;
        String path = "/orders/{order_id}/billing_info".replace("{order_id}","2000012184838380");
        StringBuffer sb = new StringBuffer();
        sb.append(url);
        sb.append(path);
        //入参
        //设置请求头
        Map<String, String> headerMap = new HashMap<>(1);
        headerMap.put("Authorization", "Bearer " +accessToken);
        headerMap.put("x-version", "2");

        //拉取数据
        ApiResult apiResult = new ApiResult();
        apiResult = HttpCommonUtil.sendOkHttpApiResult(sb.toString(), "", null, headerMap, RequestMethod.GET);
        if (!Objects.equals(apiResult.getCode(), 200) && !Objects.equals(apiResult.getCode(), 201)) {
            System.out.println(apiResult.getData());
        }

    }


    private static RedisUtil redisUtil;


    @Resource
    private ShopInfoFeign shopInfoFeign;


    @Resource
    private DmpTaskFeign dmpTaskFeign;

    @Resource
    public void setRedisUtil(RedisUtil redisUtil) {
        MercadoLocalSdkClientService.redisUtil = redisUtil;
    }

    /**
     * 发送请求到美客多获取token
     *
     * @param paramMap
     * @return com.sdk.oms.mercado.dto.mercado.MercadoTokenDTO
     * @Author Luo_WG
     * @Date 2024/2/27 18:05
     **/
    public PlatformMercadoTokenDTO sendMercadoPostToken(Map<String, String> paramMap) {

        //组装授权url
        String clientId = paramMap.get("clientId");
        String clientSecret = paramMap.get("clientSecret");
        String redirectUri = paramMap.get("redirectUri");
        String url = paramMap.get("baseUrl");
        String code = paramMap.get("code");

        //https://api.mercadolibre.com/oauth/token?grant_type=authorization_code&client_id=%s&client_secret=%s&code=%s&redirect_uri=%s
        String path = "/oauth/token?grant_type=authorization_code&client_id=%s&client_secret=%s&code=%s&redirect_uri=%s";
        String baseUrl = String.format(url + path, clientId, clientSecret, code, redirectUri);

        //入参（无）
        Map<String, Object> param = new HashMap<>();
        param.put("code", code);

        //请求头
        Map<String, String> headerMap = new HashMap<>();
        headerMap.put("content-type", "application/x-www-form-urlencoded");
        headerMap.put("accept", "application/json");

        //发起POST请求
        String bodyStr = OkHttpUtils.doPost(baseUrl, param, headerMap);

        //解析数据
        PlatformMercadoTokenDTO tokenDTO = null;
        try {
            tokenDTO = JSONUtil.toBean(bodyStr, PlatformMercadoTokenDTO.class);
//            log.info(String.format("::::: 美客多授权 ::::: 请求地址 => %s, 平台返回值 => %s ", baseUrl, tokenDTO));
        } catch (Exception e) {
            log.error("调用url={},入参params={}, 美客多授权失败，返回值 responseMap={}, 错误信息={}", bodyStr, param.toString(), JSONUtil.toJsonStr(bodyStr), e.getMessage());
            throw new RuntimeException(StrUtil.format("调用url={},入参params={}, 美客多授权失败，返回值 responseMap={}, 错误信息={}",
                    bodyStr, param.toString(), JSONUtil.toJsonStr(bodyStr), e.getMessage()));
        }
        if (StringUtil.isBlank(tokenDTO.getAccessToken())) {
            throw new ServiceException(ApiError.ERROR_SHOP_AUTHORIZE_FAIL, PlatformDictEnum.MERCADOLIBRE_LOCAL.getName(), bodyStr);
        }

        //返回token实体
        return tokenDTO;
    }

    /**
     * 美客多刷新token方法
     *
     * @param dto
     * @return
     */
    public PlatformMercadoRefreshTokenDTO refreshToken(ShopDTO.RefreshTokenDTO dto) {

        //TG-65e12c65326e580001c3a0ba-1509269799
        //组装刷新token请求的url
        //https://api.mercadolibre.com
        String path = "/oauth/token?grant_type=refresh_token&client_id=%s&client_secret=%s&refresh_token=%s";
        String baseUrl = String.format(dto.getBaseUrl() + path, dto.getClientId(), dto.getClientSecret(), dto.getRefreshToken());

        //入参（无）
        Map<String, Object> param = new HashMap<>();

        //请求头（无）
        Map<String, String> headerMap = new HashMap<>();

        String token = "";
        long sleepTime = 1000;
        int count = 0;
        //解析数据
        PlatformMercadoRefreshTokenDTO refreshTokenDTO = null;
        while(StringUtil.isBlank(token)) {
            String bodyStr = OkHttpUtils.doPost(baseUrl, param, headerMap);
            try {
                refreshTokenDTO = JSONUtil.toBean(bodyStr, PlatformMercadoRefreshTokenDTO.class);
//            log.info(String.format("::::: 美客多刷新token ::::: 请求地址 => %s, 平台返回值 => %s ", baseUrl, refreshTokenDTO));
            } catch (Exception e) {
                log.error("调用url={},入参params={}, 美客多刷新token失败，返回值 responseMap={}, 错误信息={}", bodyStr, param.toString(), JSONUtil.toJsonStr(bodyStr));
                throw new RuntimeException(StrUtil.format("调用url={},入参params={}, 美客多刷新token失败，返回值 responseMap={}",
                        bodyStr, param.toString(), JSONUtil.toJsonStr(bodyStr), ExceptionUtil.stacktraceToString(e)));
            }

            if(StringUtil.isBlank(refreshTokenDTO.getAccessToken())) {
                if(count == 10) {
                    throw new ServiceException(ApiError.ERROR_SHOP_AUTHORIZE_FAIL, PlatformDictEnum.MERCADOLIBRE_LOCAL.getName(), bodyStr);

                }
                try {
                    Thread.sleep(sleepTime);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                sleepTime = sleepTime + 1000;
                count = count + 1;
            } else {
                token = refreshTokenDTO.getAccessToken();
            }

        }
        //返回token实体
        return refreshTokenDTO;
    }

    //https://global-selling.mercadolibre.com/authorization?client_id=3457166802805723&redirect_uri=https://erptest.ulanzi.cn:8020/store-permission-result&response_type=code


    /**
     * 发送请求获取指定店铺的sku信息
     *
     * @param shopInfoDTO
     * @return
     */
    public List<ListingViewDTO> sendMercadoGetListing(MercadoShopInfoDTO shopInfoDTO) {

        List<ListingViewDTO> resultsBeanList = new ArrayList<>();

        //每次最多获取200条
        Integer pageSize = 50;
        //当前页数
        Integer pageNo = 0;
        //总页数
        Integer pageCount = 1;

        Boolean nexflag = true;
        String baseUrl = "https://api.mercadolibre.com/users/" + shopInfoDTO.getUserId() + "/items/search";
        while (nexflag) {
            int offset = pageSize * pageNo;

            //入参
            HashMap<String, Object> params = new HashMap<>(2);
            params.put("limit", pageSize);
            params.put("offset", offset);

            //设置请求头
            Map<String, String> headerMap = new HashMap<>(1);
            headerMap.put("Authorization", "Bearer " + shopInfoDTO.getAccessToken());

            //拉取数据
            ApiResult apiResult = HttpCommonUtil.sendOkHttpApiResult(baseUrl, JSONUtil.toJsonStr(params), null, headerMap, RequestMethod.GET);
            if (!Objects.equals(apiResult.getCode(), 200)) {
                nexflag = false;
                log.error("调用url={},入参params={}, 美客多items/search数据失败，返回值 responseMap={}", baseUrl, params.toString(), JSONUtil.toJsonStr(apiResult));
                throw new RuntimeException(StrUtil.format("调用url={},入参params={}, 美客多items/search数据失败，返回值 responseMap={}",
                        baseUrl, params.toString(), JSONUtil.toJsonStr(apiResult)));
            }

            //解析数据
            ObjectMapper objectMapper = new ObjectMapper();
            ListingDTO listingDTO = null;
            try {
                listingDTO = objectMapper.readValue(JSONUtil.toJsonStr(apiResult.getData()), ListingDTO.class);
            } catch (JsonProcessingException e) {
                nexflag = false;
                log.error("调用url={},入参params={}, 数据解析失败，返回值 responseMap={}", baseUrl, params.toString(), JSONUtil.toJsonStr(apiResult));
                throw new RuntimeException(StrUtil.format("调用url={},入参params={}, 数据解析失败，返回值 responseMap={}",
                        baseUrl, params.toString(), JSONUtil.toJsonStr(apiResult)));
            }

            if (CollectionUtils.isEmpty(listingDTO.getResults())) {
                nexflag = false;
                break;
            }
            pageNo++;

            //获取到所有客户的产品id
            List<String> results = listingDTO.getResults();

            //根据产品id查询产品详情信息
            List<ListingViewDTO> listingViewDTOS = this.listItemView(results, shopInfoDTO.getAccessToken());
            resultsBeanList.addAll(listingViewDTOS);

        }

        if (CollectionUtils.isEmpty(resultsBeanList)) {
            return Collections.emptyList();
        }

        return resultsBeanList;
    }

    /**
     * 根据产品id查询产品详情信息
     *
     * @param results
     * @param accessToken
     * @return
     */
    private List<ListingViewDTO> listItemView(List<String> results, String accessToken) {
        List<List<String>> partition = Lists.partition(results, 20);

        List<ListingViewDTO> resultList = new ArrayList<>();
        for (List<String> list : partition) {
            String baseUrl = "https://api.mercadolibre.com/items";

            //入参
            HashMap<String, Object> params = new HashMap<>(1);
            params.put("ids", StringUtils.join(list, ","));

            //设置请求头
            Map<String, String> headerMap = new HashMap<>(1);
            headerMap.put("Authorization", "Bearer " + accessToken);

            //拉取数据
            ApiResult apiResult = HttpCommonUtil.sendOkHttpApiResult(baseUrl, JSONUtil.toJsonStr(params), null, headerMap, RequestMethod.GET);
            if (!Objects.equals(apiResult.getCode(), 200)) {
                log.error("调用url={},入参params={}, 美客多Listing数据失败，返回值 responseMap={}", baseUrl, params.toString(), JSONUtil.toJsonStr(apiResult));
                throw new RuntimeException(StrUtil.format("调用url={},入参params={}, 美客多Listing数据失败，返回值 responseMap={}",
                        baseUrl, params.toString(), JSONUtil.toJsonStr(apiResult)));
            }


            ObjectMapper objectMapper = new ObjectMapper();
            List<ListingViewDTO> dataList = null;
            try {
                dataList = objectMapper.readValue(JSONUtil.toJsonStr(apiResult.getData()), new TypeReference<List<ListingViewDTO>>() {
                });
            } catch (JsonProcessingException e) {
                e.printStackTrace();
                log.error("调用url={},入参params={}, 数据解析失败，返回值 responseMap={}", baseUrl, params.toString(), JSONUtil.toJsonStr(apiResult));
                throw new RuntimeException(StrUtil.format("调用url={},入参params={}, 数据解析失败，返回值 responseMap={}",
                        baseUrl, params.toString(), JSONUtil.toJsonStr(apiResult)));
            }
            if (CollectionUtil.isEmpty(dataList)) {
                break;
            }
            resultList.addAll(dataList);
        }

        return resultList;
    }

//    public static void main(String[] args) {
//        MercadoLocalSdkClientService sdkClientService = new MercadoLocalSdkClientService();
//        MercadoShopInfoDTO shopInfoDTO = new MercadoShopInfoDTO();
//        JobTaskDTO task = new JobTaskDTO();
//        shopInfoDTO.setUserId(2119968271L);
//        shopInfoDTO.setAccessToken("APP_USR-5344160433223219-031022-1a5190634d7aba85c9b279a5ffb4af4d-2119968271");
//        task.setLastTime(LocalDateTime.parse("2025-01-01 00:00:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
//        task.setNextTime(LocalDateTime.parse("2025-03-11 00:00:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
//        sdkClientService.sendMercadoGetOrder(shopInfoDTO, task);
//    }


    private String dateToStr(LocalDateTime dateTime) {

        // 转换为UTC时区的OffsetDateTime
        OffsetDateTime utcTime = dateTime
                .atZone(ZoneId.systemDefault())
                .toOffsetDateTime()
                .withOffsetSameInstant(ZoneOffset.UTC);

        // 自定义格式化
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
        String formatted = utcTime.format(formatter);

        // 字符串替换
        String target = formatted.replace("+00:00", "-00");
        return target;
    }

    /**
     * 发送请求获取指定店铺的订单
     *
     * @param shopInfoDTO
     * @return
     */
    public List<OrderViewDTO> sendMercadoGetOrder(MercadoShopInfoDTO shopInfoDTO, JobTaskDTO task) {
        String url = MercadoConstant.URL;
        String path = "/orders/search";
        //每页最大50条
        Integer pageSize = 50;
        //当前页数
        Integer pageNo = 0;
        //总页数
        Integer pageCount = 1;

        List<OrderViewDTO> resultList = new ArrayList<>();
        Boolean nexflag = true;
        while (nexflag) {
            int offset = pageSize * pageNo;

            StringBuffer sb = new StringBuffer();
            sb.append(url);
            sb.append(path);
            sb.append("?seller=");
            sb.append(shopInfoDTO.getUserId());
            sb.append("&limit=");
            sb.append(pageSize);
            sb.append("&offset=");
            sb.append(offset);
            sb.append("&order.date_last_updated.from=");
            sb.append(this.dateToStr(task.getLastTime()));
            sb.append("&order.date_last_updated.to=");
            sb.append(this.dateToStr(task.getNextTime()));
            sb.append("&order.status=");
            sb.append("cancelled,paid,invalid");

            //入参
            HashMap<String, Object> params = new HashMap<>(2);
            //设置请求头
            Map<String, String> headerMap = new HashMap<>(1);
            headerMap.put("Authorization", "Bearer " + shopInfoDTO.getAccessToken());

            //拉取数据
            ApiResult apiResult = HttpCommonUtil.sendOkHttpApiResult(sb.toString(), JSONUtil.toJsonStr(params), null, headerMap, RequestMethod.GET);
            if (!Objects.equals(apiResult.getCode(), 200) && !Objects.equals(apiResult.getCode(), 201)) {
                nexflag = false;
                log.error("调用url={},入参params={}, 美客多marketplace/orders/search数据失败，返回值 responseMap={}", url + path, params.toString(), JSONUtil.toJsonStr(apiResult));
                throw new RuntimeException(StrUtil.format("调用url={},入参params={}, 数据解析失败，返回值 responseMap={}",
                        url + path, params.toString(), JSONUtil.toJsonStr(apiResult)));
            }
            ObjectMapper objectMapper = new ObjectMapper();
            OrderDataDTO orderDataDTO = null;
            try {
                orderDataDTO = objectMapper.readValue(JSONUtil.toJsonStr(apiResult.getData()), OrderDataDTO.class);
            } catch (JsonProcessingException e) {
                nexflag = false;
                log.error("美客多orders/search接口数据解析错误，数据={}", apiResult.getData());
                throw new RuntimeException(StrUtil.format("调用url={},入参params={}, 数据解析失败，返回值 responseMap={}",
                        url + path, params.toString(), JSONUtil.toJsonStr(apiResult)));
            }
            //解析数据
//            OrderDTO orderDTO = JSONUtil.toBean(JSONUtil.toJsonStr(apiResult.getData()), OrderDTO.class);
            if (CollectionUtils.isEmpty(orderDataDTO.getResults())) {
                nexflag = false;
                break;
            }

            for (OrderViewDTO orderViewDTO : orderDataDTO.getResults()) {
                //根据发货id查询发货详情
                ShipmentViewDTO shippingRecords = getShippingRecords(shopInfoDTO, orderViewDTO.getShipping().getId());
                if (ObjectUtil.isNotEmpty(shippingRecords)) {
                    orderViewDTO.setShipmentViewDTO(shippingRecords);
                }

                //根据发货id查询费用信息
                CostDTO shippingCost = getShippingCost(shopInfoDTO, orderViewDTO.getShipping().getId());
                if (ObjectUtil.isNotEmpty(shippingCost)) {
                    orderViewDTO.setCostDTO(shippingCost);
                }
                resultList.add(orderViewDTO);
            }
            pageNo++;
        }
        return resultList;
    }

    /**
     * 根据发货id查询发货详情
     *
     * @param shopInfoDTO
     * @param shippingId
     * @return
     */
    public ShipmentViewDTO getShippingRecords(MercadoShopInfoDTO shopInfoDTO, Long shippingId) {
        String orderUrl = "https://api.mercadolibre.com/shipments/" + shippingId + "";

        //入参
        HashMap<String, Object> orderParams = new HashMap<>(1);

        //设置请求头
        Map<String, String> orderHeaderMap = new HashMap<>(1);
        orderHeaderMap.put("Authorization", "Bearer " + shopInfoDTO.getAccessToken());
        orderHeaderMap.put("x-format-new", "true");

        //拉取数据
        ApiResult shipmentResult = new ApiResult();
        Object data = null;
        long sleepTime = 1000;
        int count = 0;
        while (ObjectUtil.isEmpty(data)) {
            shipmentResult = HttpCommonUtil.sendOkHttpApiResult(orderUrl, JSONUtil.toJsonStr(orderParams), null, orderHeaderMap, RequestMethod.GET);
            if (shipmentResult.getMsg().equalsIgnoreCase("Read timed out")) {
                if (count == 10) {
                    throw new ServiceException("调用美客多" + orderUrl + "接口重试" + count + "失败");
                }
                try {
                    Thread.sleep(sleepTime);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                sleepTime = sleepTime + 1000;
                count = count + 1;
            }
            data = shipmentResult.getData();
        }

        if (!Objects.equals(shipmentResult.getCode(), 200) && !Objects.equals(shipmentResult.getCode(), 201)) {
            log.error("调用url={},入参params={}, 美客多marketplace/shipments数据失败，返回值 responseMap={}", orderUrl, orderParams.toString(), JSONUtil.toJsonStr(shipmentResult));
            throw new RuntimeException(StrUtil.format("调用url={},入参params={}, 数据解析失败，返回值 responseMap={}",
                    orderUrl, orderParams.toString(), JSONUtil.toJsonStr(shipmentResult)));
        }

        //解析数据
        ObjectMapper objectMapper = new ObjectMapper();
        ShipmentViewDTO orderViewDTO = null;
        try {
            orderViewDTO = objectMapper.readValue(JSONUtil.toJsonStr(shipmentResult.getData()), ShipmentViewDTO.class);
        } catch (JsonProcessingException e) {
            log.error("美客多shipments/'shippingId'/接口数据解析错误，数据={}", shipmentResult.getData());
            throw new RuntimeException(StrUtil.format("调用url={},入参params={}, 数据解析失败，返回值 responseMap={}",
                    orderUrl, orderParams.toString(), JSONUtil.toJsonStr(shipmentResult)));
        }
        return orderViewDTO;

    }

    /**
     * 根据发货id查询费用信息
     *
     * @param shopInfoDTO
     * @param shippingId
     * @return
     */
    private CostDTO getShippingCost(MercadoShopInfoDTO shopInfoDTO, Long shippingId) {
        String orderUrl = "https://api.mercadolibre.com/shipments/" + shippingId + "/costs";

        //入参
        HashMap<String, Object> orderParams = new HashMap<>(1);

        //设置请求头
        Map<String, String> orderHeaderMap = new HashMap<>(1);
        orderHeaderMap.put("Authorization", "Bearer " + shopInfoDTO.getAccessToken());
        orderHeaderMap.put("x-format-new", "true");

        //拉取数据
        ApiResult shipmentResult = new ApiResult();
        Object data = null;
        long sleepTime = 1000;
        int count = 0;
        while (ObjectUtil.isEmpty(data)) {
            shipmentResult = HttpCommonUtil.sendOkHttpApiResult(orderUrl, JSONUtil.toJsonStr(orderParams), null, orderHeaderMap, RequestMethod.GET);
            if (shipmentResult.getMsg().equalsIgnoreCase("Read timed out")) {
                if (count == 10) {
                    throw new ServiceException("调用美客多" + orderUrl + "接口重试" + count + "失败");
                }
                try {
                    Thread.sleep(sleepTime);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                sleepTime = sleepTime + 1000;
                count = count + 1;
            }
            data = shipmentResult.getData();
        }

        if (!Objects.equals(shipmentResult.getCode(), 200) && !Objects.equals(shipmentResult.getCode(), 201)) {
            log.error("调用url={},入参params={}, 美客多费用明细数据失败，返回值 responseMap={}", orderUrl, orderParams.toString(), JSONUtil.toJsonStr(shipmentResult));
            throw new RuntimeException(StrUtil.format("调用url={},入参params={}, 费用明细请求失败，返回值 responseMap={}",
                    orderUrl, orderParams.toString(), JSONUtil.toJsonStr(shipmentResult)));
        }

        //解析数据
        ObjectMapper objectMapper = new ObjectMapper();
        CostDTO costDTO = null;
        try {
            costDTO = objectMapper.readValue(JSONUtil.toJsonStr(shipmentResult.getData()), CostDTO.class);
        } catch (JsonProcessingException e) {
            log.error("美客多费用明细接口数据解析错误，数据={}", shipmentResult.getData());
            throw new RuntimeException(StrUtil.format("调用url={},入参params={}, 费用明细数据解析失败，返回值 responseMap={}",
                    orderUrl, orderParams.toString(), JSONUtil.toJsonStr(shipmentResult)));
        }
        return costDTO;

    }

    /**
     * 查询店铺信息
     *
     * @param shopId
     * @return
     */
    public MercadoShopInfoDTO getShopInfoByShopId(String shopId) {
        String tokenKey = StrUtil.format(RedisCacheConstants.REDIS_PLATFORM_TOKEN, PlatformDictEnum.MERCADOLIBRE_LOCAL.getCode(), shopId);
        // 缓存获取
        Object tokenObj = redisUtil.get(tokenKey);
        if (null != tokenObj) {
            if (tokenObj instanceof MercadoShopInfoDTO) {
                return (MercadoShopInfoDTO) tokenObj;
            }
        } else {
            ShopAuthEntity shopAuthEntity = shopInfoFeign.getShopAuthByShopId(shopId);
            ShopInfoEntity shopInfoEntity = shopInfoFeign.getShopInfoById(shopId);
            CfgAppClientDTO.FindDTO findDTO = new CfgAppClientDTO.FindDTO();
            AppClientEnum appClientEnum = AppClientEnum.MERCADO_ACCESS_TOKEN;
            findDTO.setBusinessType(appClientEnum.getBusinessType());
            findDTO.setDictPlatform(appClientEnum.getPlatform());
            findDTO.setPlatformType(appClientEnum.getPlatformType());
            CfgAppClientEntity cfgAppClient = dmpTaskFeign.getCfgAppClient(findDTO);
            if (Objects.isNull(cfgAppClient)) {
                return null;
            }
            MercadoShopInfoDTO result = new MercadoShopInfoDTO();
            result.setBaseUrl(cfgAppClient.getUrl());
            result.setClientId(cfgAppClient.getClientId());
            result.setClientSecret(cfgAppClient.getClientSecret());

            Map<String, Object> extendData = shopInfoEntity.getExtendData();
            Object userId = extendData.get("userId");
            result.setUserId(ObjectUtil.isEmpty(userId) ? null : Long.valueOf(userId.toString()));
            result.setSiteId(shopInfoEntity.getBusinessModel());
            if (Objects.nonNull(shopAuthEntity)) {
                result.setAccessToken(shopAuthEntity.getAccessToken());
                redisUtil.set(tokenKey, result, shopAuthEntity.getExpiresIn());
            }

            return result;
        }
        return null;

    }

    /**
     * 根据发货id查询发货详情
     *
     * @param authMap
     * @param shippingId
     * @return
     */
    public String printShippingLabel(Map<String, String> authMap, Long shippingId) {
//        String orderUrl = "https://api.mercadolibre.com/marketplace/shipments/"+shippingId+"/labels";
        String orderUrl = "https://api.mercadolibre.com/shipment_labels";
        String token = authMap.get("token");
        //入参
        HashMap<String, Object> orderParams = new HashMap<>(1);
        orderParams.put("shipment_ids",shippingId);
        orderParams.put("response_type","pdf");
        //设置请求头
        Map<String, String> orderHeaderMap = new HashMap<>(1);
        orderHeaderMap.put("Authorization", "Bearer " + token);
        orderHeaderMap.put("x-format-new", "true");
        //拉取数据
        return OkHttpUtils.doGetJsonBase64(orderUrl, orderParams, orderHeaderMap);
    }

    /**
     * 上传发票
     */
    public void uploadInvoice(MercadoInvoiceDTO mercadoInvoiceDTO) {

        //  根据店铺ID获取授权
        MercadoShopInfoDTO shopInfoByShopId = this.getShopInfoByShopId(mercadoInvoiceDTO.getShopId());

        String accessToken = shopInfoByShopId.getAccessToken();
        //组装授权url
        String url = MercadoConstant.URL;
        String path = "/shipments/{id}/invoice_data/?siteId=" + shopInfoByShopId.getSiteId();
        path = path.replace("{id}",mercadoInvoiceDTO.getShipmentId());
        StringBuffer sb = new StringBuffer();
        sb.append(url);
        sb.append(path);
        //入参
        String param = mercadoInvoiceDTO.getXmlContent();
        //设置请求头
        Map<String, String> headerMap = new HashMap<>(1);
        headerMap.put("Authorization", "Bearer " +accessToken);
        headerMap.put("Content-Type", "application/xml");

        //拉取数据
        ApiResult apiResult = new ApiResult();
        apiResult = HttpCommonUtil.sendOkHttpApiResult(sb.toString(), param, null, headerMap, RequestMethod.POST);
        if (!Objects.equals(apiResult.getCode(), 200) && !Objects.equals(apiResult.getCode(), 201)) {
            log.error("调用url={},入参params={}, 美客多上传发票数据失败，返回值 responseMap={}", path, param, JSONUtil.toJsonStr(apiResult));
            throw new ServiceException(StrUtil.format("美客多上传发票数据失败，返回值{}",JSONUtil.toJsonStr(apiResult)));
        }
    }
    /**
     * 标记发货
     */
    public void shipOrder(MercadoShipOrderDTO shipOrderDTO) {
        //  根据店铺ID获取授权
        MercadoShopInfoDTO shopInfoByShopId = this.getShopInfoByShopId(shipOrderDTO.getShopId());

        String orderUrl = "https://api.mercadolibre.com/shipments/" + shipOrderDTO.getShipmentId() + "/tracking ";
        String token = shopInfoByShopId.getAccessToken();

        //入参
        HashMap<String, Object> orderParams = new HashMap<>(3);
        orderParams.put("tracking_id", shipOrderDTO.getTrackingId());
        orderParams.put("tracking_url", shipOrderDTO.getTrackingUrl());
        orderParams.put("carrier", shipOrderDTO.getCarrier());
        //设置请求头
        Map<String, String> orderHeaderMap = new HashMap<>(1);
        orderHeaderMap.put("Authorization", "Bearer " + token);

        //拉取数据
        ApiResult shipmentResult = HttpCommonUtil.sendOkHttpApiResult(orderUrl, JSONUtil.toJsonStr(orderParams), null, orderHeaderMap, RequestMethod.POST);
        if (!Objects.equals(shipmentResult.getCode(), 200) && !Objects.equals(shipmentResult.getCode(), 201)) {
            log.error("调用url={},入参params={}, 美客多标记发货shipments/tracking数据失败，返回值 responseMap={}", orderUrl, orderParams.toString(), JSONUtil.toJsonStr(shipmentResult));
            throw new RuntimeException(StrUtil.format("调用url={},入参params={}, 美客多shipments/tracking数据失败，返回值 responseMap={}",
                    orderUrl, orderParams.toString(), JSONUtil.toJsonStr(shipmentResult)));
        }

    }
}
