package com.sdk.oms.dht.service;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.common.business.constant.RedisCacheConstants;
import com.common.business.enums.PlatformDictEnum;
import com.common.business.utils.RedisUtil;
import com.common.core.utils.OkHttpUtils;
import com.sdk.oms.dht.config.DhtConfig;
import com.sdk.oms.dht.constant.DhtConstants;
import com.sdk.oms.dht.dto.DhtAuthDTO;
import com.sdk.oms.dht.dto.DhtBaseResp;
import com.sdk.oms.dht.dto.req.*;
import com.sdk.oms.dht.dto.resp.DhtQueryCustomerResp;
import com.sdk.oms.dht.dto.resp.DhtUserResp;
import com.sdk.oms.dht.util.DhtUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 订货通公共方法
 **/
@Slf4j
@Component
public class DhtCommonService {

    @Resource
    private RedisUtil redisUtil;

    @Resource
    private DhtConfig dhtConfig;
    

    public String  queryObj(DhtQueryObjReq req) {
        DhtAuthDTO authDTO = getCorpAccessToken();
        req.setCorpAccessToken(authDTO.getCorpAccessToken());
        req.setCorpId(authDTO.getCorpId());
        String api = "/cgi/crm/v2/object/describe";
        Map<String, String> headerMap = new HashMap<>();
        String bodyStr = OkHttpUtils.doPostJson(dhtConfig.url() + api, JSONUtil.toJsonStr(req), headerMap);
        return bodyStr;
    }


    public String  download(DhtDownloadReq req) {
        DhtAuthDTO authDTO = getCorpAccessToken();
        req.setCorpAccessToken(authDTO.getCorpAccessToken());
        req.setCorpId(authDTO.getCorpId());
        String api = "/media/download";
        Map<String, String> headerMap = new HashMap<>();
        String bodyStr = OkHttpUtils.doPostJsonBase64(dhtConfig.url() + api, JSONUtil.toJsonStr(req), headerMap);
        return bodyStr;
    }

    /**
     * 简单查询
     * @param req
     * @return
     */
    public String simpleQuery(DhtSimpleQueryReq req) {
        DhtAuthDTO authDTO = getCorpAccessToken();
        req.setCorpAccessToken(authDTO.getCorpAccessToken());
        req.setCorpId(authDTO.getCorpId());
        String api = "/cgi/crm/custom/v2/data/findSimple";
        Map<String, String> headerMap = new HashMap<>();
        String bodyStr = OkHttpUtils.doPostJson(dhtConfig.url() + api, JSONUtil.toJsonStr(req), headerMap);
        return bodyStr;
    }

    /**
     * 根据手机号查询用户信息
     * @param mobile
     * @return
     */
    public DhtUserResp getUserByMobile(String mobile) {
        DhtAuthDTO authDTO = getCorpAccessToken();
        String api = "/cgi/user/getByMobile";
        Map<String, String> headerMap = new HashMap<>();
        Map<String, Object> bodyMap = new HashMap<>();
        bodyMap.put("corpAccessToken", authDTO.getCorpAccessToken());
        bodyMap.put("corpId", authDTO.getCorpId());
        bodyMap.put("mobile", mobile);
        String bodyStr = OkHttpUtils.doPostJson(dhtConfig.url() + api, bodyMap, headerMap);

        return JSON.parseObject(bodyStr, new TypeReference<DhtUserResp>() {});
    }

    /**
     * 查询授权信息
     * @return
     */
    public DhtAuthDTO getCorpAccessToken(){
        //查询redis数据
        //如果没有数据，则调用接口获取
        String api = "/cgi/corpAccessToken/get/V2";
        // cfg_app_client.id = 1600000000000000000
        String tokenKey =  CharSequenceUtil.format(RedisCacheConstants.REDIS_PLATFORM_TOKEN, PlatformDictEnum.DHT.getCode(),"1600000000000000000");
        Object token = redisUtil.get(tokenKey);
        if(token != null){
            return JSON.parseObject(token.toString(), new TypeReference<DhtAuthDTO>() {});
        }else{
//            Map<String, String> headerMap = new HashMap<>();
//            Map<String, Object> bodyMap = new HashMap<>();
//            bodyMap.put("appId", dhtConfig.appId());
//            bodyMap.put("appSecret", dhtConfig.appSecret());
//            bodyMap.put("permanentCode", dhtConfig.permanentCode());
//            String bodyStr = OkHttpUtils.doPostJson(dhtConfig.url() + api, bodyMap, headerMap);
//            DhtAuthDTO dto = JSON.parseObject(bodyStr, new TypeReference<DhtAuthDTO>() {});
//            if(Objects.isNull(dto)){
//                log.error("订货通获取accessToken失败，返回结果：{}", bodyStr);
//                throw new RuntimeException("订货通获取accessToken失败");
//            }
//            if(dto.getErrorCode()!=0){
//                log.error("订货通获取accessToken失败，返回结果：{}", bodyStr);
//                throw new RuntimeException("订货通获取accessToken失败，错误码：" + dto.getErrorCode() + "，错误信息：" + dto.getErrorMessage());
//            }
//            //缓存6900s 6600-7200s会获取新token  必须保证过期时间在这个范围内
//            redisUtil.set(tokenKey, dto, 6900);
//            return dto;
            return null;
        }
    }
}
