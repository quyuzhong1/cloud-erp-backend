package com.sdk.wms.jitu.service;

import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.common.business.threadlocal.ThirdWarehouseContext;
import com.sdk.wms.jitu.dto.request.JituOverseasInboundCancelRequest;
import com.sdk.wms.jitu.dto.request.JituOverseasInboundCreateRequest;
import com.sdk.wms.jitu.dto.request.ProductRequest;
import com.sdk.wms.jitu.dto.request.WarehouseRequest;
import com.sdk.wms.jitu.dto.response.OverseasInboundCancelResponse;
import com.sdk.wms.jitu.dto.response.OverseasInboundCreateResponse;
import com.sdk.wms.jitu.dto.response.ProductResponse;
import com.sdk.wms.jitu.dto.response.WarehouseResponse;
import com.sdk.wms.jitu.utils.AuthUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

/**
 * @author zdy
 * @ClassName JituService
 * @description: TODO
 * @date 2026年03月02日
 * @version: 1.0
 */
@Slf4j
@Component
public class JituService {
    private String getPreUrl() {
//        if (BusinessCommonConstants.hasProfile("prod")) {
//            return "https://sop.jtfulfillment.cn";
//        } else {
        return "https://demo-sop.jtfulfillment.cn";
//        }
    }

    /**
     * 仓库列表
     *
     * @return
     */
    public WarehouseResponse warehouseList(Map<String, Object> authMap, WarehouseRequest request) {
        String url = getPreUrl() + "/gateway/edi/baseData/findWarehouseList";
        authMap.put("msg_type", "OBTAINWAREHOUSE");
        authMap.put("logistics_interface", JSONUtil.toJsonStr(request));
        String bodyStr = "";
        try {
            bodyStr = AuthUtils.doPost(url, authMap);
            log.warn("bodyStr: {}", bodyStr);
        } catch (IOException e) {
            log.error("请求失败,异常: {}", e);
            throw new RuntimeException(e);
        }
        return JSON.parseObject(bodyStr, new TypeReference<WarehouseResponse>() {}.getType());
    }

    /**
     * 产品列表
     * @param authMap
     * @param request
     * @return
     */
    public ProductResponse productList(Map<String, Object> authMap, ProductRequest request) {
        String url = getPreUrl() + "/gateway/edi/product/query";
        authMap.put("logistics_interface", JSONUtil.toJsonStr(request));
        String bodyStr = "";
        try {
            bodyStr = AuthUtils.doPost(url, authMap);
            log.warn("bodyStr: {}", bodyStr);
        } catch (IOException e) {
            log.error("请求失败,异常: {}", e);
            throw new RuntimeException(e);
        }
        return JSON.parseObject(bodyStr, new TypeReference<ProductResponse>() {}.getType());
    }

    /**
     * 海外仓入库单创建
     * @param request
     * @return
     */
    public OverseasInboundCreateResponse overseasInboundCreate(JituOverseasInboundCreateRequest request) {
        Map<String, Object> authMap = ThirdWarehouseContext.getAuthMap();
        String url = getPreUrl() + "/gateway/edi/entryOrder/create";
        authMap.put("msg_type","ENTRYORDERCREATE");
        authMap.put("logistics_interface", JSONUtil.toJsonStr(request));
        String bodyStr = "";
        try {
            bodyStr = AuthUtils.doPost(url, authMap);
            log.warn("bodyStr: {}", bodyStr);
        } catch (IOException e) {
            log.error("请求失败,异常: {}", e);
            throw new RuntimeException(e);
        }
        return JSON.parseObject(bodyStr, new TypeReference<OverseasInboundCreateResponse>() {}.getType());
    }

    /**
     * 海外仓入库单创建
     * @param request
     * @return
     */
    public OverseasInboundCancelResponse overseasInboundCancel(JituOverseasInboundCancelRequest request) {
        Map<String, Object> authMap = ThirdWarehouseContext.getAuthMap();
        String url = getPreUrl() + "/gateway/edi/order/cancel";
        authMap.put("logistics_interface", JSONUtil.toJsonStr(request));
        String bodyStr = "";
        try {
            bodyStr = AuthUtils.doPost(url, authMap);
            log.warn("bodyStr: {}", bodyStr);
        } catch (IOException e) {
            log.error("请求失败,异常: {}", e);
            throw new RuntimeException(e);
        }
        return JSON.parseObject(bodyStr, new TypeReference<OverseasInboundCancelResponse>() {}.getType());
    }
}
