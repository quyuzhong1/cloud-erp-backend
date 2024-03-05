package com.sdk.oms.mercado.service;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSONObject;
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
import com.erp.rpc.dmp.feign.DmpTaskFeign;
import com.erp.rpc.oms.feign.ShopInfoFeign;
import com.google.common.collect.Lists;
import com.sdk.oms.mercado.dto.MercadoShopInfoDTO;
import com.sdk.oms.mercado.dto.mercado.PlatformMercadoRefreshTokenDTO;
import com.sdk.oms.mercado.dto.mercado.PlatformMercadoTokenDTO;
import com.sdk.oms.mercado.dto.mercado.listing.ListingDTO;
import com.sdk.oms.mercado.dto.mercado.listing.ListingViewDTO;
import com.sdk.oms.mercado.dto.mercado.order.OrderDTO;
import com.sdk.oms.mercado.dto.mercado.order.OrderViewDTO;
import com.sdk.oms.mercado.dto.mercado.order.ResultsBean;
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
//        String baseUrl = "https://api.mercadolibre.com/oauth/token?grant_type=authorization_code&grant_type=authorization_code&client_id=3457166802805723&client_secret=QucvI4VWHO0w3AZftOElz5liVOurfjQG&code=TG-65e12c65326e580001c3a0ba-1509269799&redirect_uri=https://erptest.ulanzi.cn:8020/store-permission-result";

        //组装刷新token请求的url
        String baseUrl = "https://api.mercadolibre.com/oauth/token?grant_type=refresh_token&client_id=3457166802805723&client_secret=QucvI4VWHO0w3AZftOElz5liVOurfjQG&refresh_token=TG-65e67868ceddf000015d733a-1509269799";

        //入参（无）
        Map<String, Object> param = new HashMap<>();

        //请求头
        Map<String, String> headerMap = new HashMap<>();
        headerMap.put("content-type", "application/x-www-form-urlencoded");
        headerMap.put("accept", "application/json");

        //发起POST请求
        String bodyStr = OkHttpUtils.doPost(baseUrl, param, headerMap);
        System.out.println(bodyStr);
    }


    private static RedisUtil redisUtil;


    @Resource
    private ShopInfoFeign shopInfoFeign;


    @Resource
    private DmpTaskFeign dmpTaskFeign;

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
        //https://api.mercadolibre.com
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
            throw new ServiceException(ApiError.ERROR_SHOP_AUTHORIZE_FAIL, PlatformDictEnum.MERCADO.getName(), bodyStr);
        }
        if (StringUtil.isBlank(tokenDTO.getAccessToken())) {
            throw new ServiceException(ApiError.ERROR_SHOP_AUTHORIZE_FAIL, PlatformDictEnum.MERCADO.getName(), bodyStr);
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
            throw new ServiceException(ApiError.ERROR_SHOP_AUTHORIZE_FAIL, PlatformDictEnum.MERCADO.getName(), bodyStr);
        }
        if (StringUtil.isBlank(refreshTokenDTO.getAccessToken())) {
            throw new ServiceException(ApiError.ERROR_SHOP_AUTHORIZE_FAIL, PlatformDictEnum.MERCADO.getName(), bodyStr);
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
        Integer pageSize = 200;
        //当前页数
        Integer pageNo = 0;
        //总页数
        Integer pageCount = 1;

        while(pageNo < pageCount) {

            //https://api.mercadolibre.com/marketplace/products/search?status=active&product_identifier=%s
            String baseUrl = "https://api.mercadolibre.com/users/"+shopInfoDTO.getUserId()+"/items/search";
/*            StringBuffer sb = new StringBuffer();
            sb.append(baseUrl);
            sb.append("?limit="+ pageSize +"");
            sb.append("&offset="+ pageNo +"");*/

            //入参
            HashMap<String, Object> params = new HashMap<>(2);
            params.put("limit", pageSize);
            params.put("offset", pageNo);

            //设置请求头
            Map<String, String> headerMap = new HashMap<>(1);
            headerMap.put("Authorization", "Bearer "+ shopInfoDTO.getAccessToken());

            //拉取数据
            ApiResult apiResult = HttpCommonUtil.sendOkHttpApiResult(baseUrl, JSONUtil.toJsonStr(params), null, headerMap, RequestMethod.GET);
            if (!Objects.equals(apiResult.getCode(), 200)) {
                log.error("调用url={},入参params={}, 美客多items/search数据失败，返回值 responseMap={}", baseUrl, params.toString(), JSONUtil.toJsonStr(apiResult));
                throw new RuntimeException(StrUtil.format("调用url={},入参params={}, 美客多items/search数据失败，返回值 responseMap={}",
                        baseUrl, params.toString(), JSONUtil.toJsonStr(apiResult)));
            }

            //解析数据
            ListingDTO listingDTO = JSONUtil.toBean(JSONUtil.toJsonStr(apiResult.getData()), ListingDTO.class);
            if (CollectionUtils.isEmpty(listingDTO.getResults())) {
                break;
            }
            pageCount = (listingDTO.getPaging().getTotal() + pageSize - 1) / pageSize;
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
            String baseUrl = "http://api.mercadolibre.com/items";

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

            List<ListingViewDTO> dataList = JSONObject.parseArray(JSONUtil.toJsonStr(apiResult.getData()), ListingViewDTO.class);
            if(CollectionUtil.isEmpty(dataList)){
                break;
            }
            resultList.addAll(dataList);
        }

        return resultList;
    }

    /**
     * 发送请求获取指定店铺的sku信息
     * @param shopInfoDTO
     * @return
     */
    public List<OrderViewDTO> sendMercadoGetOrder(MercadoShopInfoDTO shopInfoDTO, JobTaskDTO task) {

        String baseUrl = "https://api.mercadolibre.com/marketplace/orders/search";
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
            params.put("order.status", "cancelled,paid,invalid");
            params.put("last_updated.from", task.getLastTime());
            params.put("last_updated.to", task.getNextTime());
            params.put("limit", pageSize);
            params.put("offset", pageNo);
            //设置请求头
            Map<String, String> headerMap = new HashMap<>(1);
            headerMap.put("Authorization", "Bearer " + shopInfoDTO.getAccessToken());

            //拉取数据
            ApiResult apiResult = HttpCommonUtil.sendOkHttpApiResult(baseUrl, JSONUtil.toJsonStr(params), null, headerMap, RequestMethod.GET);
            if (!Objects.equals(apiResult.getCode(), 200)) {
                log.error("调用url={},入参params={}, 美客多marketplace/orders/search数据失败，返回值 responseMap={}", baseUrl, params.toString(), JSONUtil.toJsonStr(apiResult));
                throw new RuntimeException(StrUtil.format("调用url={},入参params={}, 美客多marketplace/orders/search数据失败，返回值 responseMap={}",
                        baseUrl, params.toString(), JSONUtil.toJsonStr(apiResult)));
            }

            //解析数据
            OrderDTO orderDTO = JSONUtil.toBean(JSONUtil.toJsonStr(apiResult.getData()), OrderDTO.class);
            if (CollectionUtils.isEmpty(orderDTO.getResults())) {
                break;
            }
            pageCount = (orderDTO.getPaging().getTotal() + pageSize - 1) / pageSize;
            pageNo++;


            for (ResultsBean result : orderDTO.getResults()) {
                List<Long> orderIdList = result.getOrders().stream().map(req -> req.getId()).collect(Collectors.toList());

                for (Long id : orderIdList) {
                    String orderUrl = "https://api.mercadolibre.com/marketplace/orders/" + id + "";

                    //入参
                    HashMap<String, Object> orderParams = new HashMap<>(1);

                    //设置请求头
                    Map<String, String> orderHeaderMap = new HashMap<>(1);
                    orderHeaderMap.put("Authorization", "Bearer " + shopInfoDTO.getAccessToken());

                    //拉取数据
                    ApiResult orderResult = HttpCommonUtil.sendOkHttpApiResult(orderUrl, JSONUtil.toJsonStr(orderParams), null, orderHeaderMap, RequestMethod.GET);
                    if (!Objects.equals(orderResult.getCode(), 200)) {
                        log.error("调用url={},入参params={}, 美客多marketplace/orders数据失败，返回值 responseMap={}", orderUrl, orderParams.toString(), JSONUtil.toJsonStr(orderResult));
                        throw new RuntimeException(StrUtil.format("调用url={},入参params={}, 美客多marketplace/orders数据失败，返回值 responseMap={}",
                                orderUrl, orderParams.toString(), JSONUtil.toJsonStr(orderResult)));
                    }

                    //解析数据
                    OrderViewDTO orderViewDTO = JSONUtil.toBean(JSONUtil.toJsonStr(orderResult.getData()), OrderViewDTO.class);

                    resultList.add(orderViewDTO);
                }
            }
        }
        return resultList;
    }

    /**
     * 查询店铺信息
     * @param shopId
     * @return
     */
    public MercadoShopInfoDTO getShopInfoByShopId(String shopId) {
        String tokenKey = StrUtil.format(RedisCacheConstants.REDIS_PLATFORM_TOKEN, PlatformDictEnum.MERCADO.getCode(), shopId);
        // 缓存获取
        Object tokenObj = redisUtil.get(tokenKey);
        if (null != tokenObj) {
            if (tokenObj instanceof MercadoShopInfoDTO) {
                return (MercadoShopInfoDTO) tokenObj;
            }
        } else {
            ShopAuthEntity shopAuthEntity = shopInfoFeign.getShopAuthByShopId(shopId);
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
     * 发送POST请求查询订单
     *
     * @param baseUrl
     * @param accessToken
     * @param paramMap 入参
     * @return
     */
    public String sendMercadoPost(String baseUrl, String accessToken, Map<String, Object> paramMap) {

        //请求头
        Map<String, String> headerMap = new HashMap<>();
        headerMap.put("Authorization", "Bearer "+ accessToken);

        //发起POST请求
        String bodyStr = OkHttpUtils.doPost(baseUrl, paramMap, headerMap);

        //返回token实体
        return bodyStr;
    }

    /**
     * 发送GET请求查询订单
     *
     * @param baseUrl
     * @param accessToken
     * @param paramMap 入参
     * @return
     */
    public String sendMercadoGet(String baseUrl, String accessToken, Map<String, Object> paramMap) {

        //请求头
        Map<String, String> headerMap = new HashMap<>();
        headerMap.put("Authorization", "Bearer "+ accessToken);

        //发起POST请求
        String bodyStr = OkHttpUtils.doGet(baseUrl, paramMap, headerMap);

        //返回token实体
        return bodyStr;
    }

}
