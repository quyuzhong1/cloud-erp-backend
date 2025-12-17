package com.sdk.oms.dht.service;

import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.TypeReference;
import com.common.core.utils.OkHttpUtils;
import com.sdk.oms.dht.config.DhtConfig;
import com.sdk.oms.dht.constant.DhtConstants;
import com.sdk.oms.dht.dto.DhtAuthDTO;
import com.sdk.oms.dht.dto.DhtBaseResp;
import com.sdk.oms.dht.dto.req.*;
import com.sdk.oms.dht.dto.resp.DhtQueryCustomerAccountResp;
import com.sdk.oms.dht.util.DhtUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

/**
 * 订货通客户账户
 **/
@Slf4j
@Component
public class DhtCustomerAccountService {

    @Resource
    private DhtConfig dhtConfig;
    
    @Resource
    private DhtCommonService dhtCommonService;

    /**
     * 查询账户信息
     * @param req
     * @return
     */
    public DhtBaseResp<DhtQueryCustomerAccountResp> queryCustomerAccount(DhtCommonQueryReq req) {
        DhtAuthDTO authDTO = dhtCommonService.getCorpAccessToken();
        req.setCorpAccessToken(authDTO.getCorpAccessToken());
        req.setCorpId(authDTO.getCorpId());
        req.getData().setDataObjectApiName(DhtConstants.CUSTOMER_ACCOUNT_NEW_API_NAME);
        String api = DhtConstants.CRM_QUERY_URL;
        Map<String, String> headerMap = new HashMap<>();
        String bodyStr = OkHttpUtils.doPostJson(dhtConfig.url() + api, JSONUtil.toJsonStr(req), headerMap);
        return DhtUtils.parseToJiFengResp(bodyStr, new TypeReference<DhtBaseResp<DhtQueryCustomerAccountResp>>() {});
    }


    /**
     * 查询账户信息
     * @param req
     * @return
     */
    public DhtBaseResp<DhtQueryCustomerAccountResp> queryCustomerAccountObj(DhtCommonQueryReq req) {
        DhtAuthDTO authDTO = dhtCommonService.getCorpAccessToken();
        req.setCorpAccessToken(authDTO.getCorpAccessToken());
        req.setCorpId(authDTO.getCorpId());
        req.getData().setDataObjectApiName(DhtConstants.CUSTOMER_ACCOUNT_API_NAME);
        String api = DhtConstants.CRM_QUERY_URL;
        Map<String, String> headerMap = new HashMap<>();
        String bodyStr = OkHttpUtils.doPostJson(dhtConfig.url() + api, JSONUtil.toJsonStr(req), headerMap);
        return DhtUtils.parseToJiFengResp(bodyStr, new TypeReference<DhtBaseResp<DhtQueryCustomerAccountResp>>() {});
    }
}
