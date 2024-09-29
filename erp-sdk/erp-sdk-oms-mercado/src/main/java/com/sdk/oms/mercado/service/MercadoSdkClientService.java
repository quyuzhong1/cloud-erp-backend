package com.sdk.oms.mercado.service;

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
import com.sdk.oms.mercado.constant.MercadoConstant;
import com.sdk.oms.mercado.dto.MercadoShipOrderDTO;
import com.sdk.oms.mercado.dto.MercadoShopInfoDTO;
import com.sdk.oms.mercado.dto.mercado.PlatformMercadoRefreshTokenDTO;
import com.sdk.oms.mercado.dto.mercado.PlatformMercadoTokenDTO;
import com.sdk.oms.mercado.dto.mercado.cost.CostDTO;
import com.sdk.oms.mercado.dto.mercado.listing.ListingDTO;
import com.sdk.oms.mercado.dto.mercado.listing.ListingViewDTO;
import com.sdk.oms.mercado.dto.mercado.order.OrderDTO;
import com.sdk.oms.mercado.dto.mercado.order.OrderViewDTO;
import com.sdk.oms.mercado.dto.mercado.order.ResultsBean;
import com.sdk.oms.mercado.dto.mercado.shipment.ShipmentViewDTO;
import jodd.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RequestMethod;
import org.thymeleaf.util.StringUtils;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 美客多平台SDK
 * @Author Luo_WG
 * @Date 2024/2/27 18:04
 **/
@Slf4j
@Component
public class MercadoSdkClientService {
    public static void main(String[] args) {

        String orderUrl = "https://api.mercadolibre.com/marketplace/orders/2000006213527517";

        //入参
        HashMap<String, Object> orderParams = new HashMap<>(1);

        //设置请求头
        Map<String, String> orderHeaderMap = new HashMap<>(1);
        orderHeaderMap.put("Authorization", "Bearer APP_USR-3457166802805723-082902-95af1fbcbc57490cafb6081deaca410e-1509269799");

        //拉取数据
        ApiResult orderResult = HttpCommonUtil.sendOkHttpApiResult(orderUrl, JSONUtil.toJsonStr(orderParams), null, orderHeaderMap, RequestMethod.GET);
        if (!Objects.equals(orderResult.getCode(), 200) && !Objects.equals(orderResult.getCode(), 201)) {
            log.error("调用url={},入参params={}, 美客多marketplace/orders数据失败，返回值 responseMap={}", orderUrl, orderParams.toString(), JSONUtil.toJsonStr(orderResult));
            throw new RuntimeException(StrUtil.format("调用url={},入参params={}, 数据解析失败，返回值 responseMap={}",
                    orderUrl, orderParams.toString(), JSONUtil.toJsonStr(orderResult)));
        }
    }


    private static RedisUtil redisUtil;


    @Resource
    private ShopInfoFeign shopInfoFeign;


    @Resource
    private DmpTaskFeign dmpTaskFeign;

    @Resource
    public void setRedisUtil(RedisUtil redisUtil) {
        MercadoSdkClientService.redisUtil = redisUtil;
    }

    /**
     * 发送请求到美客多获取token
     * @Author Luo_WG
     * @Date 2024/2/27 18:05
     * @param paramMap
     * @return com.sdk.oms.mercado.dto.mercado.MercadoTokenDTO
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
            throw new ServiceException(ApiError.ERROR_SHOP_AUTHORIZE_FAIL, PlatformDictEnum.MERCADOLIBRE.getName(), bodyStr);
        }

        //返回token实体
        return tokenDTO;
    }

    /**
     * 美客多刷新token方法
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

        //发起POST请求
        String bodyStr = OkHttpUtils.doPost(baseUrl, param, headerMap);

        //解析数据
        PlatformMercadoRefreshTokenDTO refreshTokenDTO = null;
        try {
            refreshTokenDTO = JSONUtil.toBean(bodyStr, PlatformMercadoRefreshTokenDTO.class);
//            log.info(String.format("::::: 美客多刷新token ::::: 请求地址 => %s, 平台返回值 => %s ", baseUrl, refreshTokenDTO));
        } catch (Exception e) {
            log.error("调用url={},入参params={}, 美客多刷新token失败，返回值 responseMap={}, 错误信息={}", bodyStr, param.toString(), JSONUtil.toJsonStr(bodyStr));
            throw new RuntimeException(StrUtil.format("调用url={},入参params={}, 美客多刷新token失败，返回值 responseMap={}",
                    bodyStr, param.toString(), JSONUtil.toJsonStr(bodyStr), ExceptionUtil.stacktraceToString(e)));
        }
        if (StringUtil.isBlank(refreshTokenDTO.getAccessToken())) {
            throw new ServiceException(ApiError.ERROR_SHOP_AUTHORIZE_FAIL, PlatformDictEnum.MERCADOLIBRE.getName(), bodyStr);
        }

        //返回token实体
        return refreshTokenDTO;
    }

    //https://global-selling.mercadolibre.com/authorization?client_id=3457166802805723&redirect_uri=https://erptest.ulanzi.cn:8020/store-permission-result&response_type=code


    /**
     * 发送请求获取指定店铺的sku信息
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
        String baseUrl = "https://api.mercadolibre.com/users/"+shopInfoDTO.getUserId()+"/items/search";
        while (nexflag) {
            int offset = pageSize * pageNo;

            //入参
            HashMap<String, Object> params = new HashMap<>(2);
            params.put("limit", pageSize);
            params.put("offset", offset);

            //设置请求头
            Map<String, String> headerMap = new HashMap<>(1);
            headerMap.put("Authorization", "Bearer "+ shopInfoDTO.getAccessToken());

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
            params.put("ids", StringUtils.join(list,","));

            //设置请求头
            Map<String, String> headerMap = new HashMap<>(1);
            headerMap.put("Authorization", "Bearer "+ accessToken);

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
                dataList = objectMapper.readValue(JSONUtil.toJsonStr(apiResult.getData()), new TypeReference<List<ListingViewDTO>>() {});
            } catch (JsonProcessingException e) {
                e.printStackTrace();
                log.error("调用url={},入参params={}, 数据解析失败，返回值 responseMap={}", baseUrl, params.toString(), JSONUtil.toJsonStr(apiResult));
                throw new RuntimeException(StrUtil.format("调用url={},入参params={}, 数据解析失败，返回值 responseMap={}",
                        baseUrl, params.toString(), JSONUtil.toJsonStr(apiResult)));
            }
            if(CollectionUtil.isEmpty(dataList)){
                break;
            }
            resultList.addAll(dataList);
        }

        return resultList;
    }

    /**
     * 发送请求获取指定店铺的订单
     * @param shopInfoDTO
     * @return
     */
    public List<OrderViewDTO> sendMercadoGetOrder(MercadoShopInfoDTO shopInfoDTO, JobTaskDTO task) {
        String url = MercadoConstant.URL;
        String path = "/marketplace/orders/search";
        //每次最多获取200条
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
            sb.append("?");
            //paid, cancelled, payment_required, confirmed
            sb.append("limit=");//每页最大50条
            sb.append(pageSize);
            sb.append("&offset=");
            sb.append(offset);

            //入参
            HashMap<String, Object> params = new HashMap<>(2);
//            params.put("seller.id", "1511265855");
//            params.put("seller.id", shopInfoDTO.getUserId());
            params.put("order.status", "cancelled,paid,invalid");
            params.put("last_updated.from", task.getLastTime());
            params.put("last_updated.to", task.getNextTime());
            params.put("limit", pageSize);
            params.put("offset", offset);
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
            OrderDTO orderDTO = null;
            try {
                orderDTO = objectMapper.readValue(JSONUtil.toJsonStr(apiResult.getData()), OrderDTO.class);
            } catch (JsonProcessingException e) {
                nexflag = false;
                log.error("美客多orders/search接口数据解析错误，数据={}", apiResult.getData());
                throw new RuntimeException(StrUtil.format("调用url={},入参params={}, 数据解析失败，返回值 responseMap={}",
                        url + path, params.toString(), JSONUtil.toJsonStr(apiResult)));
            }
            //解析数据
//            OrderDTO orderDTO = JSONUtil.toBean(JSONUtil.toJsonStr(apiResult.getData()), OrderDTO.class);
            if (CollectionUtils.isEmpty(orderDTO.getResults())) {
                nexflag = false;
                break;
            }
            pageNo++;


            for (ResultsBean result : orderDTO.getResults()) {
                List<Long> orderIdList = result.getOrders().stream().map(req -> req.getFid()).collect(Collectors.toList());

                for (Long id : orderIdList) {
                    String orderUrl = "https://api.mercadolibre.com/marketplace/orders/" + id + "";

                    //入参
                    HashMap<String, Object> orderParams = new HashMap<>(1);

                    //设置请求头
                    Map<String, String> orderHeaderMap = new HashMap<>(1);
                    orderHeaderMap.put("Authorization", "Bearer " + shopInfoDTO.getAccessToken());

                    //拉取数据
                    ApiResult orderDetailApiResult = new ApiResult();
                    Object data = null;
                    long sleepTime = 1000;
                    int count = 0;
                    while(ObjectUtil.isEmpty(data)) {
                        orderDetailApiResult = HttpCommonUtil.sendOkHttpApiResult(orderUrl, JSONUtil.toJsonStr(orderParams), null, orderHeaderMap, RequestMethod.GET);
                        if(orderDetailApiResult.getMsg().equalsIgnoreCase("Read timed out")) {
                            if(count == 10) {
                                throw new ServiceException("调用美客多" + url + path + "接口重试" + count + "失败");
                            }
                            try {
                                Thread.sleep(sleepTime);
                            } catch (InterruptedException e) {}
                            sleepTime = sleepTime + 1000;
                            count = count + 1;
                        }
                        data = orderDetailApiResult.getData();
                    }

                    if (!Objects.equals(orderDetailApiResult.getCode(), 200) && !Objects.equals(orderDetailApiResult.getCode(), 201)) {
                        orderDetailApiResult.getMsg().equalsIgnoreCase("Read timed out");
                        log.error("调用url={},入参params={}, 美客多marketplace/orders数据失败，返回值 responseMap={}", orderUrl, orderParams.toString(), JSONUtil.toJsonStr(orderDetailApiResult));
                        throw new RuntimeException(StrUtil.format("调用url={},入参params={}, 数据解析失败，返回值 responseMap={}",
                                orderUrl, orderParams.toString(), JSONUtil.toJsonStr(orderDetailApiResult)));
                    }

                    //解析数据
                    OrderViewDTO orderViewDTO = null;
                    ObjectMapper objectMapperBase = new ObjectMapper();
                    try {
                        orderViewDTO = objectMapperBase.readValue(JSONUtil.toJsonStr(orderDetailApiResult.getData()), OrderViewDTO.class);
                    } catch (JsonProcessingException e) {
                        log.error("调用url={},入参params={}, 数据解析失败，返回值 responseMap={}", orderUrl, orderParams.toString(), JSONUtil.toJsonStr(orderDetailApiResult.getData()));
                        throw new RuntimeException(StrUtil.format("调用url={},入参params={}, 数据解析失败，返回值 responseMap={}",
                                orderUrl, orderParams.toString(), JSONUtil.toJsonStr(orderDetailApiResult.getData())));
                    }

                    //根据发货id查询发货详情
                    ShipmentViewDTO shippingRecords = getShippingRecords(shopInfoDTO, orderViewDTO.getShipping().getFid());
                    if (ObjectUtil.isNotEmpty(shippingRecords)) {
                        orderViewDTO.setShipmentViewDTO(shippingRecords);
                    }

                    //根据发货id查询费用信息
                    CostDTO shippingCost = getShippingCost(shopInfoDTO, orderViewDTO.getShipping().getFid());
                    if (ObjectUtil.isNotEmpty(shippingCost)) {
                        orderViewDTO.setCostDTO(shippingCost);
                    }
                    resultList.add(orderViewDTO);
                }
            }
        }
        return resultList;
    }

    /**
     * 根据发货id查询发货详情
     * @param shopInfoDTO
     * @param shippingId
     * @return
     */
    private ShipmentViewDTO getShippingRecords(MercadoShopInfoDTO shopInfoDTO, Long shippingId) {
        String orderUrl = "https://api.mercadolibre.com/marketplace/shipments/" + shippingId + "";

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
        while(ObjectUtil.isEmpty(data)) {
            shipmentResult = HttpCommonUtil.sendOkHttpApiResult(orderUrl, JSONUtil.toJsonStr(orderParams), null, orderHeaderMap, RequestMethod.GET);
            if(shipmentResult.getMsg().equalsIgnoreCase("Read timed out")) {
                if(count == 10) {
                    throw new ServiceException("调用美客多" + orderUrl + "接口重试" + count + "失败");
                }
                try {
                    Thread.sleep(sleepTime);
                } catch (InterruptedException e) {}
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
     * @param shopInfoDTO
     * @param shippingId
     * @return
     */
    private CostDTO getShippingCost(MercadoShopInfoDTO shopInfoDTO, Long shippingId) {
        String orderUrl = "https://api.mercadolibre.com/marketplace/shipments/" + shippingId + "/costs";

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
        while(ObjectUtil.isEmpty(data)) {
            shipmentResult = HttpCommonUtil.sendOkHttpApiResult(orderUrl, JSONUtil.toJsonStr(orderParams), null, orderHeaderMap, RequestMethod.GET);
            if(shipmentResult.getMsg().equalsIgnoreCase("Read timed out")) {
                if(count == 10) {
                    throw new ServiceException("调用美客多" + orderUrl + "接口重试" + count + "失败");
                }
                try {
                    Thread.sleep(sleepTime);
                } catch (InterruptedException e) {}
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
     * @param shopId
     * @return
     */
    public MercadoShopInfoDTO getShopInfoByShopId(String shopId) {
        String tokenKey = StrUtil.format(RedisCacheConstants.REDIS_PLATFORM_TOKEN, PlatformDictEnum.MERCADOLIBRE.getCode(), shopId);
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
            result.setUserId(Integer.valueOf(extendData.get("userId")+""));

            result.setId(shopId);
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
     * @param authMap
     * @param shippingId
     * @return
     */
    public String printShippingLabel(Map<String, String> authMap, Long shippingId) {

        String orderUrl = "https://api.mercadolibre.com/marketplace/shipments/"+shippingId+"/labels";
        String token = authMap.get("token");

        //入参
        HashMap<String, Object> orderParams = new HashMap<>(1);

        //设置请求头
        Map<String, String> orderHeaderMap = new HashMap<>(1);
        orderHeaderMap.put("Authorization", "Bearer " + token);
        orderHeaderMap.put("x-format-new", "true");

        //拉取数据
        ApiResult shipmentResult = HttpCommonUtil.sendOkHttpApiResult(orderUrl, JSONUtil.toJsonStr(orderParams), null, orderHeaderMap, RequestMethod.GET);
        if (!Objects.equals(shipmentResult.getCode(), 200) && !Objects.equals(shipmentResult.getCode(), 201)) {
            log.error("调用url={},入参params={}, 美客多marketplace/shipments数据失败，返回值 responseMap={}", orderUrl, orderParams.toString(), JSONUtil.toJsonStr(shipmentResult));
            throw new RuntimeException(StrUtil.format("调用url={},入参params={}, 美客多marketplace/shipments数据失败，返回值 responseMap={}",
                    orderUrl, orderParams.toString(), JSONUtil.toJsonStr(shipmentResult)));
        }

        //解析数据
        return String.valueOf(shipmentResult.getData());

    }

    /**
     * 标记发货
     */
    public void shipOrder(MercadoShipOrderDTO shipOrderDTO) {
        //  根据店铺ID获取授权
        MercadoShopInfoDTO shopInfoByShopId = this.getShopInfoByShopId(shipOrderDTO.getShopId());

        String orderUrl = "https://api.mercadolibre.com/marketplace/shipments/"+shipOrderDTO.getShipmentId()+"/tracking ";
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
