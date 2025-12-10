package com.sdk.wms.tongyou.service;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.common.business.constant.BusinessCommonConstants;
import com.common.business.threadlocal.ThirdWarehouseContext;
import com.common.core.utils.OkHttpUtils;
import com.sdk.wms.tongyou.dto.request.TongYouCancelOutboundReq;
import com.sdk.wms.tongyou.dto.request.TongYouCreateInboundReq;
import com.sdk.wms.tongyou.dto.request.TongYouCreateOutboundReq;
import com.sdk.wms.tongyou.dto.response.TongYouBaseResp;
import com.sdk.wms.tongyou.dto.response.TongYouInboundResp;
import com.sdk.wms.tongyou.dto.response.TongYouLogisticChannelResp;
import com.sdk.wms.tongyou.dto.response.TongYouOutboundResp;
import com.sdk.wms.tongyou.utils.TongYouUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author liuruipeng
 */
@Slf4j
@Component
@Validated
public class TongYouService {

    private String getPreUrl(){
        if (BusinessCommonConstants.hasProfile("prod")) {
            return "https://www.jia-wms.com/";
        } else {
            return "https://www.jia-wms.com/";
        }
    }

    /**
     * 获取产品列表
     */
    public TongYouBaseResp<String> getProduct(Map<String,Object> body){
        Map<String, String> headerMap = new HashMap<>();
        String path = "hwc_api/hwc_products_api.php";
        String bodyStr = OkHttpUtils.doPostJson(getPreUrl()+path,JSONObject.toJSONString(body), headerMap);
        return TongYouUtils.parseToTongYouResp(bodyStr, String.class);
    }
    /**
     * 获取库存列表
     */
    public TongYouBaseResp<String> getInventory(Map<String,Object> body){
        Map<String, String> headerMap = new HashMap<>();
        String path = "hwc_api/hwc_stock.php";
        String bodyStr = OkHttpUtils.doPostJson(getPreUrl()+path,JSONObject.toJSONString(body), headerMap);
        return TongYouUtils.parseToTongYouResp(bodyStr, String.class);
    }
    /**
     * 获取仓库列表
     */
    public TongYouBaseResp<String> getWarehouse(Map<String, Object> authJson){
        Map<String, String> headerMap = new HashMap<>();
        String path = "hwc_api/hwc_storage_list.php";
        String bodyStr = OkHttpUtils.doPostJson(getPreUrl()+path,authJson, headerMap);
        return TongYouUtils.parseToTongYouResp(bodyStr, String.class);
    }


    /**
     * 获取物流渠道列表
     */
    public TongYouBaseResp<List<TongYouLogisticChannelResp>> getLogisticsChannel(Map<String, Object> authJson){
        Map<String, String> headerMap = new HashMap<>();
        String path = "hwc_api/hwc_paisongx.php";
        String bodyStr = OkHttpUtils.doPostJson(getPreUrl()+path,authJson, headerMap);
        return TongYouUtils.parseToTongYouResp(bodyStr, new TypeReference<TongYouBaseResp<List<TongYouLogisticChannelResp>>>() {});

    }

    /**
     * 查询入库单
     */
    public TongYouBaseResp<List<TongYouInboundResp>> getInboundBill(Map<String, Object> authJson){
        Map<String, String> headerMap = new HashMap<>();
        String path = "hwc_api/hwc_order_tc.php";
        log.warn("通邮 getInboundBill request:{}",JSONObject.toJSONString(authJson));
        String bodyStr = OkHttpUtils.doPostJson(getPreUrl()+path,authJson, headerMap);
        return TongYouUtils.parseToTongYouResp(bodyStr, new TypeReference<TongYouBaseResp<List<TongYouInboundResp>>>() {});

    }

    /**
     * 查询出库单
     */
    public TongYouBaseResp<String> getOutboundBill(Map<String, Object> authJson){
        Map<String, String> headerMap = new HashMap<>();
        String path = "hwc_api/hwc_order.php";
        log.warn("通邮 getOutboundBill request:{}",JSONObject.toJSONString(authJson));
        String bodyStr = OkHttpUtils.doPostJson(getPreUrl()+path,authJson, headerMap);
        return TongYouUtils.parseToTongYouResp(bodyStr, new TypeReference<TongYouBaseResp<String>>() {});

    }

    /**
     * 创建入库单
     */
    public TongYouBaseResp<TongYouInboundResp> createInboundBill(@Valid TongYouCreateInboundReq tongYouCreateInboundReq){

        String path = "hwc_api/add_order_tc.php";
        Map<String, String> headerMap = new HashMap<>();
        //密钥
        Object object = ThirdWarehouseContext.getAuthMap().get("appToken");
        tongYouCreateInboundReq.setToken(ObjectUtil.isEmpty(object) ? "" : object.toString());

        log.warn("通邮 createInboundBill request:{}",JSONObject.toJSONString(tongYouCreateInboundReq));
        String bodyStr = OkHttpUtils.doPostJson(getPreUrl()+path,JSONObject.toJSONString(tongYouCreateInboundReq), headerMap);
        ThirdWarehouseContext.setRequestJson(JSONObject.toJSONString(tongYouCreateInboundReq));
        TongYouBaseResp<TongYouInboundResp> respDto = TongYouUtils.parseToTongYouResp(bodyStr, TongYouInboundResp.class);
        ThirdWarehouseContext.setResponseJson(bodyStr);
        return respDto;
    }

    /**
     * 创建出库单
     */
    public TongYouBaseResp<TongYouOutboundResp> createOutboundBill(@Valid TongYouCreateOutboundReq tongYouCreateOutboundReq){
        String path = "hwc_api/add_order.php";
        Map<String, String> headerMap = new HashMap<>();
        ThirdWarehouseContext.setRequestJson(JSONObject.toJSONString(tongYouCreateOutboundReq));
        //密钥
        Object object = ThirdWarehouseContext.getAuthMap().get("appToken");
        tongYouCreateOutboundReq.setToken(ObjectUtil.isEmpty(object) ? "" : object.toString());
        String jsonString = JSONObject.toJSONString(Collections.singletonList(tongYouCreateOutboundReq));
        log.warn("通邮 createOutboundBill request:{}",jsonString);
        String bodyStr = OkHttpUtils.doPostJson(getPreUrl()+path,jsonString, headerMap);
        TongYouBaseResp<TongYouOutboundResp> respDto = TongYouUtils.parseToTongYouResp(bodyStr, TongYouOutboundResp.class);
        ThirdWarehouseContext.setResponseJson(bodyStr);
        return respDto;
    }

    /**
     * 取消出库单
     */
    public TongYouBaseResp<String> cancelOutboundBill(@Valid TongYouCancelOutboundReq TongYouCancelOutboundReq){
        String path = "hwc_api/hwc_deliver_del.php";
        Map<String, String> headerMap = new HashMap<>();
        Map<String,Object> bodyMap = new HashMap<>();
        //密钥
        Object object = ThirdWarehouseContext.getAuthMap().get("appToken");
        bodyMap.put("token",ObjectUtil.isEmpty(object) ? "" : object.toString());
        bodyMap.put("deliver_list", Collections.singletonList(TongYouCancelOutboundReq.getOrderNo()));

        log.warn("通邮 cancelOutboundBill request:{}",JSONObject.toJSONString(bodyMap));

        ThirdWarehouseContext.setRequestJson(JSONObject.toJSONString(bodyMap));
        String bodyStr = OkHttpUtils.doPostJson(getPreUrl()+path,bodyMap, headerMap);
        TongYouBaseResp<String> respDto = TongYouUtils.parseToTongYouResp(bodyStr, String.class);
        ThirdWarehouseContext.setResponseJson(bodyStr);
        return respDto;
    }


    /**
     * 查询退货入库单
     */
    public TongYouBaseResp<TongYouOutboundResp> createReturnInboundBill(@Valid TongYouCreateOutboundReq TongYouCreateOutboundReq){
        String path = "hwc_api/hwc_order_tc.php";
        Map<String, String> headerMap = new HashMap<>();
        ThirdWarehouseContext.setRequestJson(JSONObject.toJSONString(TongYouCreateOutboundReq));
        String bodyStr = OkHttpUtils.doPostJson(getPreUrl()+path,JSONObject.toJSONString(TongYouCreateOutboundReq), headerMap);
        TongYouBaseResp<TongYouOutboundResp> respDto = TongYouUtils.parseToTongYouResp(bodyStr, TongYouOutboundResp.class);
        ThirdWarehouseContext.setResponseJson(bodyStr);
        return respDto;
    }

}
