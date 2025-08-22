package com.sdk.oms.dht.service;

import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.TypeReference;
import com.common.core.utils.OkHttpUtils;
import com.sdk.oms.dht.config.DhtConfig;
import com.sdk.oms.dht.constant.DhtConstants;
import com.sdk.oms.dht.dto.DhtAuthDTO;
import com.sdk.oms.dht.dto.DhtBaseResp;
import com.sdk.oms.dht.dto.req.DhtOperationCustomerAddressReq;
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
        req.getObjectData().setDataObjectApiName(DhtConstants.CUSTOMER_ADDRESS_API_NAME);
        String api = DhtConstants.CRM_CREATE_URL;
        Map<String, String> headerMap = new HashMap<>();
        log.warn("订货通创建客户地址请求参数：{}", JSONUtil.toJsonStr(req));
        String bodyStr = OkHttpUtils.doPostJson(dhtConfig.url() + api, JSONUtil.toJsonStr(req), headerMap);
        return DhtUtils.parseToJiFengResp(bodyStr, new TypeReference<DhtBaseResp<String>>() {});
    }

}
