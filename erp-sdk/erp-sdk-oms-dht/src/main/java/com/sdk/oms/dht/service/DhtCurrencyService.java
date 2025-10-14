package com.sdk.oms.dht.service;

import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.TypeReference;
import com.common.core.utils.OkHttpUtils;
import com.sdk.oms.dht.config.DhtConfig;
import com.sdk.oms.dht.constant.DhtConstants;
import com.sdk.oms.dht.dto.BaseReq;
import com.sdk.oms.dht.dto.DhtAuthDTO;
import com.sdk.oms.dht.dto.DhtBaseResp;
import com.sdk.oms.dht.dto.req.*;
import com.sdk.oms.dht.dto.resp.DhtQueryCurrencyResp;
import com.sdk.oms.dht.dto.resp.DhtQueryCustomerResp;
import com.sdk.oms.dht.dto.resp.DhtUserResp;
import com.sdk.oms.dht.util.DhtUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

/**
 * 订货通币种
 **/
@Slf4j
@Component
public class DhtCurrencyService {

    @Resource
    private DhtConfig dhtConfig;
    
    @Resource
    private DhtCommonService dhtCommonService;

    public DhtBaseResp<DhtQueryCurrencyResp> queryCurrency() {
        DhtAuthDTO authDTO = dhtCommonService.getCorpAccessToken();
        BaseReq req = new BaseReq();
        req.setCorpAccessToken(authDTO.getCorpAccessToken());
        req.setCorpId(authDTO.getCorpId());
        req.setCurrentOpenUserId(authDTO.getOpenUserId());
        String api = "/cgi/crm/v2/currency/list";
        Map<String, String> headerMap = new HashMap<>();
        String bodyStr = OkHttpUtils.doPostJson(dhtConfig.url() + api, JSONUtil.toJsonStr(req), headerMap);
        return DhtUtils.parseToJiFengResp(bodyStr, new TypeReference<DhtBaseResp<DhtQueryCurrencyResp>>() {});
    }

    public DhtBaseResp<String> createCurrency(DhtCreateCurrencyReq dhtCreateCurrencyReq) {
        DhtAuthDTO authDTO = dhtCommonService.getCorpAccessToken();
        dhtCreateCurrencyReq.setCorpAccessToken(authDTO.getCorpAccessToken());
        dhtCreateCurrencyReq.setCorpId(authDTO.getCorpId());
        String api = "/cgi/crm/v2/add/currency";
        Map<String, String> headerMap = new HashMap<>();
        String bodyStr = OkHttpUtils.doPostJson(dhtConfig.url() + api, JSONUtil.toJsonStr(dhtCreateCurrencyReq), headerMap);
        return DhtUtils.parseToJiFengResp(bodyStr, new TypeReference<DhtBaseResp<String>>() {});
    }

    public DhtBaseResp<String> updateCurrency(DhtUpdateCurrencyReq dhtUpdateCurrencyReq) {
        DhtAuthDTO authDTO = dhtCommonService.getCorpAccessToken();
        dhtUpdateCurrencyReq.setCorpAccessToken(authDTO.getCorpAccessToken());
        dhtUpdateCurrencyReq.setCorpId(authDTO.getCorpId());
        String api = "/cgi/crm/v2/update/currency";
        Map<String, String> headerMap = new HashMap<>();
        String bodyStr = OkHttpUtils.doPostJson(dhtConfig.url() + api, JSONUtil.toJsonStr(dhtUpdateCurrencyReq), headerMap);
        return DhtUtils.parseToJiFengResp(bodyStr, new TypeReference<DhtBaseResp<String>>() {});
    }
}
