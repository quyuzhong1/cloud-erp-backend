package com.sdk.oms.dht.service;

import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.TypeReference;
import com.common.core.utils.OkHttpUtils;
import com.sdk.oms.dht.config.DhtConfig;
import com.sdk.oms.dht.constant.DhtConstants;
import com.sdk.oms.dht.dto.DhtAuthDTO;
import com.sdk.oms.dht.dto.DhtBaseResp;
import com.sdk.oms.dht.dto.req.*;
import com.sdk.oms.dht.dto.resp.DhtQueryCustomerAddressResp;
import com.sdk.oms.dht.util.DhtUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

/**
 * 订货通客户地址
 **/
@Slf4j
@Component
public class DhtCustomerAddressService {

    @Resource
    private DhtConfig dhtConfig;
    
    @Resource
    private DhtCommonService dhtCommonService;

    /**
     * 创建客户地址
     * @param req
     * @return
     */
    public DhtBaseResp<String> createCustomerAddress(DhtOperationCustomerAddressReq req) {
        DhtAuthDTO authDTO = dhtCommonService.getCorpAccessToken();
        req.setCorpAccessToken(authDTO.getCorpAccessToken());
        req.setCorpId(authDTO.getCorpId());
        req.getData().getObjectData().setDataObjectApiName(DhtConstants.CUSTOMER_ADDRESS_API_NAME);
        req.getData().getObjectData().setObjectDescribeApiName(DhtConstants.CUSTOMER_ADDRESS_API_NAME);
        String api = DhtConstants.CRM_CREATE_URL;
        Map<String, String> headerMap = new HashMap<>();
        log.warn("订货通创建客户地址请求参数：{}", JSONUtil.toJsonStr(req));
        String bodyStr = OkHttpUtils.doPostJson(dhtConfig.url() + api, JSONUtil.toJsonStr(req), headerMap);
        return DhtUtils.parseToJiFengResp(bodyStr, new TypeReference<DhtBaseResp<String>>() {});
    }
    /**
     * 更新客户地址
     * @param req
     * @return
     */
    public DhtBaseResp<String> updateCustomerAddress(DhtOperationCustomerAddressReq req) {
        DhtAuthDTO authDTO = dhtCommonService.getCorpAccessToken();
        req.setCorpAccessToken(authDTO.getCorpAccessToken());
        req.setCorpId(authDTO.getCorpId());
        req.getData().getObjectData().setDataObjectApiName(DhtConstants.CUSTOMER_ADDRESS_API_NAME);
        req.getData().getObjectData().setObjectDescribeApiName(DhtConstants.CUSTOMER_ADDRESS_API_NAME);
        String api = DhtConstants.CRM_UPDATE_URL;
        Map<String, String> headerMap = new HashMap<>();
        log.warn("订货通更新客户地址请求参数：{}", JSONUtil.toJsonStr(req));
        String bodyStr = OkHttpUtils.doPostJson(dhtConfig.url() + api, JSONUtil.toJsonStr(req), headerMap);
        return DhtUtils.parseToJiFengResp(bodyStr, new TypeReference<DhtBaseResp<String>>() {});
    }
    /**
     * 查询客户地址
     * @param req
     * @return
     */
    public DhtBaseResp<DhtQueryCustomerAddressResp> queryCustomerAddress(DhtCommonQueryReq req) {
        DhtAuthDTO authDTO = dhtCommonService.getCorpAccessToken();
        req.setCorpAccessToken(authDTO.getCorpAccessToken());
        req.setCorpId(authDTO.getCorpId());
        req.getData().setDataObjectApiName(DhtConstants.CUSTOMER_ADDRESS_API_NAME);
        String api = DhtConstants.CRM_QUERY_URL;
        Map<String, String> headerMap = new HashMap<>();
        String bodyStr = OkHttpUtils.doPostJson(dhtConfig.url() + api, JSONUtil.toJsonStr(req), headerMap);
        return DhtUtils.parseToJiFengResp(bodyStr, new TypeReference<DhtBaseResp<DhtQueryCustomerAddressResp>>() {});
    }

    /**
     * 启用客户地址
     * @return
     */
    public DhtBaseResp<String> enableCustomerAddress(DhtRecoverReq req) {
        DhtAuthDTO authDTO = dhtCommonService.getCorpAccessToken();
        req.setCorpAccessToken(authDTO.getCorpAccessToken());
        req.setCorpId(authDTO.getCorpId());
        req.getData().setDataObjectApiName(DhtConstants.CUSTOMER_ADDRESS_API_NAME);
        String api = DhtConstants.CRM_RECOVER_URL;
        Map<String, String> headerMap = new HashMap<>();
        log.warn("订货通启用客户地址请求参数：{}", JSONUtil.toJsonStr(req));
        String bodyStr = OkHttpUtils.doPostJson(dhtConfig.url() + api, JSONUtil.toJsonStr(req), headerMap);
        return DhtUtils.parseToJiFengResp(bodyStr, new TypeReference<DhtBaseResp<String>>() {});
    }


    /**
     * 禁用客户地址
     * @return
     */
    public DhtBaseResp<String> invalidCustomerAddress(DhtInvalidReq req) {
        DhtAuthDTO authDTO = dhtCommonService.getCorpAccessToken();
        req.setCorpAccessToken(authDTO.getCorpAccessToken());
        req.setCorpId(authDTO.getCorpId());
        req.getData().setDataObjectApiName(DhtConstants.CUSTOMER_ADDRESS_API_NAME);
        String api = DhtConstants.CRM_INVALID_URL;
        Map<String, String> headerMap = new HashMap<>();
        log.warn("订货通禁用客户地址请求参数：{}", JSONUtil.toJsonStr(req));
        String bodyStr = OkHttpUtils.doPostJson(dhtConfig.url() + api, JSONUtil.toJsonStr(req), headerMap);
        return DhtUtils.parseToJiFengResp(bodyStr, new TypeReference<DhtBaseResp<String>>() {});
    }


    /**
     * 删除客户地址
     * @return
     */
    public DhtBaseResp<String> deleteCustomerAddress(DhtDeleteReq req) {
        DhtAuthDTO authDTO = dhtCommonService.getCorpAccessToken();
        req.setCorpAccessToken(authDTO.getCorpAccessToken());
        req.setCorpId(authDTO.getCorpId());
        req.getData().setDataObjectApiName(DhtConstants.CUSTOMER_ADDRESS_API_NAME);
        String api = DhtConstants.CRM_DELETE_URL;
        Map<String, String> headerMap = new HashMap<>();
        log.warn("订货通删除客户地址请求参数：{}", JSONUtil.toJsonStr(req));
        String bodyStr = OkHttpUtils.doPostJson(dhtConfig.url() + api, JSONUtil.toJsonStr(req), headerMap);
        return DhtUtils.parseToJiFengResp(bodyStr, new TypeReference<DhtBaseResp<String>>() {});
    }
}
