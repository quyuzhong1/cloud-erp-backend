package com.sdk.oms.dht.service;

import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.common.core.utils.OkHttpUtils;
import com.sdk.oms.dht.config.DhtConfig;
import com.sdk.oms.dht.constant.DhtConstants;
import com.sdk.oms.dht.dto.DhtAuthDTO;
import com.sdk.oms.dht.dto.DhtBaseResp;
import com.sdk.oms.dht.dto.req.DhtCommonQueryReq;
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
public class DhtReceiptService {

    @Resource
    private DhtConfig dhtConfig;
    
    @Resource
    private DhtCommonService dhtCommonService;

    /**
     * @param req
     * @return
     */
    public DhtBaseResp<String> queryReceipt(DhtCommonQueryReq req) {
        DhtAuthDTO authDTO = dhtCommonService.getCorpAccessToken();
        req.setCorpAccessToken(authDTO.getCorpAccessToken());
        req.setCorpId(authDTO.getCorpId());
        req.getData().setDataObjectApiName(DhtConstants.RECEIPT_API_NAME);
        String api = DhtConstants.CRM_QUERY_URL;
        Map<String, String> headerMap = new HashMap<>();
        String bodyStr = OkHttpUtils.doPostJson(dhtConfig.url() + api, JSONUtil.toJsonStr(req), headerMap);
        return DhtUtils.parseToJiFengResp(bodyStr, new TypeReference<DhtBaseResp<String>>() {});
    }
    /**
     * @param req
     * @return
     */
    public DhtBaseResp<String> queryReceiptDetail(DhtCommonQueryReq req) {
        DhtAuthDTO authDTO = dhtCommonService.getCorpAccessToken();
        req.setCorpAccessToken(authDTO.getCorpAccessToken());
        req.setCorpId(authDTO.getCorpId());
        req.getData().setDataObjectApiName(DhtConstants.RECEIPT_DETAIL_API_NAME);
        String api = DhtConstants.CRM_QUERY_URL;
        Map<String, String> headerMap = new HashMap<>();
        String bodyStr = OkHttpUtils.doPostJson(dhtConfig.url() + api, JSONUtil.toJsonStr(req), headerMap);
        return DhtUtils.parseToJiFengResp(bodyStr, new TypeReference<DhtBaseResp<String>>() {});
    }

    /**
     */
    public DhtBaseResp<String> updateReceipt(Map<String, Object> bodyMap) {
        DhtAuthDTO authDTO = dhtCommonService.getCorpAccessToken();
        bodyMap.put("corpAccessToken", authDTO.getCorpAccessToken());
        bodyMap.put("currentOpenUserId", authDTO.getOpenUserId());
        bodyMap.put("corpId", authDTO.getCorpId());
        String api = DhtConstants.CRM_UPDATE_URL;
        Map<String, String> headerMap = new HashMap<>();
        String bodyStr = OkHttpUtils.doPostJson(dhtConfig.url() + api, JSONUtil.toJsonStr(bodyMap), headerMap);
        return DhtUtils.parseToJiFengResp(bodyStr, new TypeReference<DhtBaseResp<String>>() {});
    }
}
