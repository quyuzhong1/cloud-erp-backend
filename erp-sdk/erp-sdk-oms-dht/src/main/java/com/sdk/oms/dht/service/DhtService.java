package com.sdk.oms.dht.service;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.common.business.constant.BusinessCommonConstants;
import com.common.business.constant.RedisCacheConstants;
import com.common.business.enums.PlatformDictEnum;
import com.common.business.utils.RedisUtil;
import com.common.core.utils.OkHttpUtils;
import com.erp.tms.aliexpress.service.AliExpressShipperService;
import com.sdk.oms.dht.dto.DhtAuthDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 订货通
 **/
@Slf4j
@Component
public class DhtService {

    @Resource
    private RedisUtil redisUtil;

    private final String appId = "FSAID_1320998";

    private final String appSecret = "803c8d535711404281ddf5750245a779";

    private final String permanentCode = "ECDE744727AA7F1310607DC5DEAF8364";

    private String getPreUrl(){
        if (BusinessCommonConstants.hasProfile("prod")) {
            return "https://open.fxiaoke.com";
        } else {
            return "https://open.fxiaoke.com";
        }
    }


    public DhtAuthDTO getCorpAccessToken(){
        //查询redis数据
        //如果没有数据，则调用接口获取
        String api = "/cgi/corpAccessToken/get/V2";
        String tokenKey =  CharSequenceUtil.format(RedisCacheConstants.REDIS_PLATFORM_TOKEN, PlatformDictEnum.DHT.getCode());
        Object token = redisUtil.get(tokenKey);
        if( token != null){
            return JSON.parseObject(token.toString(), new TypeReference<DhtAuthDTO>() {});
        }else{
            Map<String, String> headerMap = new HashMap<>();
            Map<String, Object> bodyMap = new HashMap<>();
            bodyMap.put("appId", appId);
            bodyMap.put("appSecret", appSecret);
            bodyMap.put("permanentCode", permanentCode);
            String bodyStr = OkHttpUtils.doPostJson(getPreUrl() + api, bodyMap, headerMap);
            DhtAuthDTO dto = JSON.parseObject(bodyStr, new TypeReference<DhtAuthDTO>() {});
            //缓存一个半小时
            redisUtil.set(tokenKey, JSONUtil.toJsonStr(dto), 60*90);
            return dto;
        }
    }
}
