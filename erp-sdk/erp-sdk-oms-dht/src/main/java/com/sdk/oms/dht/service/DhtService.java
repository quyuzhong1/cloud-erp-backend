package com.sdk.oms.dht.service;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.common.business.constant.BusinessCommonConstants;
import com.common.business.constant.RedisCacheConstants;
import com.common.business.enums.PlatformDictEnum;
import com.common.business.utils.RedisUtil;
import com.common.core.utils.OkHttpUtils;
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
 * 订货通
 **/
@Slf4j
@Component
public class DhtService {

    @Resource
    private RedisUtil redisUtil;

    private final String appId;

    private final String appSecret;

    private final String permanentCode;

    private final String url;

    public DhtService(){
        if (BusinessCommonConstants.hasProfile("prod")) {
            permanentCode = "ECDE744727AA7F1310607DC5DEAF8364";
            url = "https://open.fxiaoke.com";
            appId = "FSAID_1320998";
            appSecret = "803c8d535711404281ddf5750245a779";
        } else {
            permanentCode = "ECDE744727AA7F1310607DC5DEAF8364";
            url = "https://open.fxiaoke.com";
            appId = "FSAID_1320998";
            appSecret = "803c8d535711404281ddf5750245a779";
        }
    }

    public String  queryObj(DhtQueryObjReq req) {
        DhtAuthDTO authDTO = getCorpAccessToken();
        req.setCorpAccessToken(authDTO.getCorpAccessToken());
        req.setCorpId(authDTO.getCorpId());
        String api = "/cgi/crm/v2/object/describe";
        Map<String, String> headerMap = new HashMap<>();
        String bodyStr = OkHttpUtils.doPostJson(url + api, JSONUtil.toJsonStr(req), headerMap);
        return bodyStr;
    }

    /**
     * 更新客户状态为无效
     * @return
     */
    public DhtBaseResp<String> invalidCustomer(DhtInvalidCustomerReq req) {
        DhtAuthDTO authDTO = getCorpAccessToken();
        req.setCorpAccessToken(authDTO.getCorpAccessToken());
        req.setCorpId(authDTO.getCorpId());
        req.getData().setDataObjectApiName(DhtConstants.CUSTOMER_API_NAME);
        String api = "/cgi/crm/v2/data/invalid";
        Map<String, String> headerMap = new HashMap<>();
        log.warn("订货通作废客户请求参数：{}", JSONUtil.toJsonStr(req));
        String bodyStr = OkHttpUtils.doPostJson(url + api, JSONUtil.toJsonStr(req), headerMap);
        return DhtUtils.parseToJiFengResp(bodyStr, new TypeReference<DhtBaseResp<String>>() {});
    }

    /**
     * 启用客户
     * @return
     */
    public DhtBaseResp<String> enableCustomer(DhtEnableCustomerReq req) {
        DhtAuthDTO authDTO = getCorpAccessToken();
        req.setCorpAccessToken(authDTO.getCorpAccessToken());
        req.setCorpId(authDTO.getCorpId());
        req.getData().setDataObjectApiName(DhtConstants.CUSTOMER_API_NAME);
        String api = "/cgi/crm/v2/data/recover";
        Map<String, String> headerMap = new HashMap<>();
        log.warn("订货通启用客户请求参数：{}", JSONUtil.toJsonStr(req));
        String bodyStr = OkHttpUtils.doPostJson(url + api, JSONUtil.toJsonStr(req), headerMap);
        return DhtUtils.parseToJiFengResp(bodyStr, new TypeReference<DhtBaseResp<String>>() {});
    }

    /**
     * 删除客户
     * @return
     */
    public DhtBaseResp<String> deleteCustomer(DhtDeleteCustomerReq req) {
        DhtAuthDTO authDTO = getCorpAccessToken();
        req.setCorpAccessToken(authDTO.getCorpAccessToken());
        req.setCorpId(authDTO.getCorpId());
        req.getData().setDataObjectApiName(DhtConstants.CUSTOMER_API_NAME);
        String api = "/cgi/crm/v2/data/delete";
        Map<String, String> headerMap = new HashMap<>();
        log.warn("订货通删除客户请求参数：{}", JSONUtil.toJsonStr(req));
        String bodyStr = OkHttpUtils.doPostJson(url + api, JSONUtil.toJsonStr(req), headerMap);
        return DhtUtils.parseToJiFengResp(bodyStr, new TypeReference<DhtBaseResp<String>>() {});
    }
    /**
     * 查询客户
     * @param req
     * @return
     */
    public DhtBaseResp<DhtQueryCustomerResp> queryCustomer(DhtQueryCustomerReq req) {
        DhtAuthDTO authDTO = getCorpAccessToken();
        req.setCorpAccessToken(authDTO.getCorpAccessToken());
        req.setCorpId(authDTO.getCorpId());
        req.getData().setDataObjectApiName(DhtConstants.CUSTOMER_API_NAME);
        String api = "/cgi/crm/v2/data/query";
        Map<String, String> headerMap = new HashMap<>();
        String bodyStr = OkHttpUtils.doPostJson(url + api, JSONUtil.toJsonStr(req), headerMap);
        return DhtUtils.parseToJiFengResp(bodyStr, new TypeReference<DhtBaseResp<DhtQueryCustomerResp>>() {});
    }

    /**
     * 创建客户
     * @param req
     * @return
     */
    public DhtBaseResp<String> createCustomer(DhtOperationCustomerReq req) {
        DhtAuthDTO authDTO = getCorpAccessToken();
        req.setCorpAccessToken(authDTO.getCorpAccessToken());
        req.setCorpId(authDTO.getCorpId());
        req.getData().getObjectData().setDataObjectApiName(DhtConstants.CUSTOMER_API_NAME);
        String api = "/cgi/crm/v2/data/create";
        Map<String, String> headerMap = new HashMap<>();
        log.warn("订货通创建客户请求参数：{}", JSONUtil.toJsonStr(req));
        String bodyStr = OkHttpUtils.doPostJson(url + api, JSONUtil.toJsonStr(req), headerMap);
        return DhtUtils.parseToJiFengResp(bodyStr, new TypeReference<DhtBaseResp<String>>() {});
    }

    /**
     * 更新客户
     * @param req
     * @return
     */
    public DhtBaseResp<String> updateCustomer(DhtOperationCustomerReq req) {
        DhtAuthDTO authDTO = getCorpAccessToken();
        req.setCorpAccessToken(authDTO.getCorpAccessToken());
        req.setCorpId(authDTO.getCorpId());
        String api = "/cgi/crm/v2/data/update";
        Map<String, String> headerMap = new HashMap<>();
        log.warn("订货通更新客户请求参数：{}", JSONUtil.toJsonStr(req));
        req.getData().getObjectData().setDataObjectApiName(DhtConstants.CUSTOMER_API_NAME);
        String bodyStr = OkHttpUtils.doPostJson(url + api, JSONUtil.toJsonStr(req), headerMap);
        return DhtUtils.parseToJiFengResp(bodyStr, new TypeReference<DhtBaseResp<String>>() {});
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
        String bodyStr = OkHttpUtils.doPostJson(url + api, bodyMap, headerMap);

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
        String tokenKey =  CharSequenceUtil.format(RedisCacheConstants.REDIS_PLATFORM_TOKEN, PlatformDictEnum.DHT.getCode());
        Object token = redisUtil.get(tokenKey);
        if(token != null){
            return JSON.parseObject(token.toString(), new TypeReference<DhtAuthDTO>() {});
        }else{
            Map<String, String> headerMap = new HashMap<>();
            Map<String, Object> bodyMap = new HashMap<>();
            bodyMap.put("appId", appId);
            bodyMap.put("appSecret", appSecret);
            bodyMap.put("permanentCode", permanentCode);
            String bodyStr = OkHttpUtils.doPostJson(url + api, bodyMap, headerMap);
            DhtAuthDTO dto = JSON.parseObject(bodyStr, new TypeReference<DhtAuthDTO>() {});
            if(Objects.isNull(dto)){
                log.error("订货通获取accessToken失败，返回结果：{}", bodyStr);
                throw new RuntimeException("订货通获取accessToken失败");
            }
            if(dto.getErrorCode()!=0){
                log.error("订货通获取accessToken失败，返回结果：{}", bodyStr);
                throw new RuntimeException("订货通获取accessToken失败，错误码：" + dto.getErrorCode() + "，错误信息：" + dto.getErrorMessage());
            }
            //缓存6900s 6600-7200s会获取新token  必须保证过期时间在这个范围内
            redisUtil.set(tokenKey, JSONUtil.toJsonStr(dto), 6900);
            return dto;
        }
    }
}
