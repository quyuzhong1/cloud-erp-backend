package com.erp.oms.aliexpress.service;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.common.business.constant.RedisCacheConstants;
import com.common.business.enums.PlatformDictEnum;
import com.common.business.utils.RedisUtil;

import com.erp.model.dmp.dto.CfgAppClientDTO;
import com.erp.model.dmp.entity.CfgAppClientEntity;
import com.erp.model.dmp.enums.AppClientEnum;
import com.erp.model.oms.entity.ShopAuthEntity;
import com.erp.oms.aliexpress.api.IopClient;
import com.erp.oms.aliexpress.api.IopClientImpl;
import com.erp.oms.aliexpress.api.IopRequest;
import com.erp.oms.aliexpress.api.IopResponse;
import com.erp.oms.aliexpress.dto.AliExpressShopInfoDTO;
import com.erp.oms.aliexpress.dto.request.OrderRequest;
import com.erp.oms.aliexpress.dto.response.AliExpressOrder;
import com.erp.oms.aliexpress.enums.Protocol;
import com.erp.oms.aliexpress.util.ApiException;
import com.erp.rpc.dmp.feign.DmpTaskFeign;
import com.erp.rpc.oms.feign.ShopInfoFeign;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.erp.oms.aliexpress.constants.AliexpressConstants.pageSize;

/**
 * 速卖通订单服务
 *
 * @author yl
 * @date 2023-11-22
 */
@Slf4j
@Component
public class AliExpressOrderService {

    private static RedisUtil redisUtil;

    @Resource
    private ShopInfoFeign shopInfoFeign;

    @Resource
    private DmpTaskFeign dmpTaskFeign;

    @Resource
    public void setRedisUtil(RedisUtil redisUtil) {
        AliExpressOrderService.redisUtil = redisUtil;
    }

    /**
     * 拉取订单
     *
     * @return
     * @parms
     * @author yl
     * @date 2023-11-22
     */
    public void listOrder(OrderRequest orderRequest,List<AliExpressOrder> orderList) throws ApiException {

        String appKey = orderRequest.getClientId();
        String appSecret = orderRequest.getClientSecret();
        String baseUrl = orderRequest.getBaseUrl();
        String apiName=orderRequest.getApiName();
        Integer currentPage=orderRequest.getCurrentPage();
        IopClient client = new IopClientImpl(baseUrl, appKey, appSecret);
        IopRequest request = new IopRequest();
        request.setApiName(apiName);
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("current_page", orderRequest.getCurrentPage());
        paramMap.put("page_size", pageSize);
        paramMap.put("create_date_start", orderRequest.getStartTime());
        paramMap.put("create_date_end", orderRequest.getEndTime());
        request.addApiParameter("simplify", "true");
        request.addApiParameter("param_aeop_order_query", JSONObject.toJSONString(paramMap));
        String token = orderRequest.getToken();
        IopResponse response = client.execute(request, token, Protocol.TOP);
        JSONObject jsonObject=JSONObject.parseObject(response.getBody());
        JSONObject resultJsONObject=jsonObject.getJSONObject("result");
        Boolean  success=resultJsONObject.getBooleanValue("success");
        //失败
        if(!success){
            log.error("拉取速卖通订单失败>>>>>>>{}",resultJsONObject.getOrDefault("error_message","").toString());
            return;
        }
        JSONArray jsonArray = (JSONArray) resultJsONObject.get("target_list");
        if (Objects.isNull(jsonArray)){
            return;
        }
        //目录列表
        List<AliExpressOrder> orderInfoList = JSONObject.parseArray(jsonArray.toJSONString(), AliExpressOrder.class);
        orderList.addAll(orderInfoList);
        //总页数
        Integer totalPage=resultJsONObject.getInteger("total_page");
        //表示还有
        if(Objects.nonNull(totalPage)&&!totalPage.equals(currentPage)){
            orderRequest.setCurrentPage(currentPage+1);
            listOrder(orderRequest,orderInfoList);
        }

    }


    /**
     * 获取基础信息
     *
     * @param shopId listOrder
     * @return
     * @author yl
     * @date 2023-11-29 12:13
     */

    public AliExpressShopInfoDTO getShopInfoByShopId(String shopId) {
        String tokenKey = StrUtil.format(RedisCacheConstants.REDIS_PLATFORM_TOKEN, PlatformDictEnum.ALI_EXPRESS.getCode(), shopId);
        // 缓存获取
        Object tokenObj = redisUtil.get(tokenKey);
        if (null != tokenObj) {
            if (tokenObj instanceof AliExpressShopInfoDTO) {
                return (AliExpressShopInfoDTO) tokenObj;
            }
        } else {
            ShopAuthEntity shopAuthEntity = shopInfoFeign.getShopAuthByShopId(shopId);
            CfgAppClientDTO.FindDTO findDTO = new CfgAppClientDTO.FindDTO();
            AppClientEnum appClientEnum = AppClientEnum.ALI_EXPRESS_TOKEN;
            findDTO.setBusinessType(appClientEnum.getBusinessType());
            findDTO.setDictPlatform(appClientEnum.getPlatform());
            findDTO.setPlatformType(appClientEnum.getPlatformType());
            CfgAppClientEntity cfgAppClient = dmpTaskFeign.getCfgAppClient(findDTO);
            if (Objects.isNull(cfgAppClient)) {
                return null;
            }
            AliExpressShopInfoDTO result = new AliExpressShopInfoDTO();
            result.setBaseUrl(cfgAppClient.getUrl());
            result.setClientId(cfgAppClient.getClientId());
            result.setClientSecret(cfgAppClient.getClientSecret());
            result.setId(shopId);
            if(Objects.nonNull(shopAuthEntity)){
                result.setToken(shopAuthEntity.getToken());
            }
            return  result;
        }
        return null;

    }

    public static void main(String[] args) throws ApiException {
        AliExpressOrderService orderService = new AliExpressOrderService();
        Map<String, String> map = new HashMap<>();
        map.put("clientId", "502978");
        map.put("clientSecret", "DfFGCAXMY7pptKfhz7IkWEa0zC0xddhY");
        map.put("baseUrl", "https://api-sg.aliexpress.com");
        //orderService.listOrder();
    }


}
