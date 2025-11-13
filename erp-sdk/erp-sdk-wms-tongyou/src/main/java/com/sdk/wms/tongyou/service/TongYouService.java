package com.sdk.wms.tongyou.service;

import com.alibaba.fastjson.JSONObject;
import com.common.business.constant.BusinessCommonConstants;
import com.common.business.threadlocal.ThirdWarehouseContext;
import com.common.core.utils.OkHttpUtils;
import com.sdk.wms.tongyou.dto.request.TongYouCancelOutboundReq;
import com.sdk.wms.tongyou.dto.request.TongYouCreateInboundReq;
import com.sdk.wms.tongyou.dto.request.TongYouCreateOutboundReq;
import com.sdk.wms.tongyou.dto.response.TongYouBaseResp;
import com.sdk.wms.tongyou.dto.response.TongYouInboundResp;
import com.sdk.wms.tongyou.dto.response.TongYouOutboundResp;
import com.sdk.wms.tongyou.utils.TongYouUtils;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

/**
 * @author liuruipeng
 */
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
     * 创建入库单
     */
    public TongYouBaseResp<TongYouInboundResp> createInboundBill(@Valid TongYouCreateInboundReq TongYouGetReceiptReq){

        String path = "hwc_api/add_order_tc.php";
        Map<String, String> headerMap = new HashMap<>();
        String bodyStr = OkHttpUtils.doPostJson(getPreUrl()+path,JSONObject.toJSONString(TongYouGetReceiptReq), headerMap);
        ThirdWarehouseContext.setRequestJson(JSONObject.toJSONString(TongYouGetReceiptReq));
        TongYouBaseResp<TongYouInboundResp> respDto = TongYouUtils.parseToTongYouResp(bodyStr, TongYouInboundResp.class);
        ThirdWarehouseContext.setResponseJson(bodyStr);
        return respDto;
    }

    /**
     * 创建出库单
     */
    public TongYouBaseResp<TongYouOutboundResp> createOutboundBill(@Valid TongYouCreateOutboundReq TongYouCreateOutboundReq){
        String path = "hwc_api/add_order.php";
        Map<String, String> headerMap = new HashMap<>();
        ThirdWarehouseContext.setRequestJson(JSONObject.toJSONString(TongYouCreateOutboundReq));
        String bodyStr = OkHttpUtils.doPostJson(getPreUrl()+path,JSONObject.toJSONString(TongYouCreateOutboundReq), headerMap);
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
        ThirdWarehouseContext.setRequestJson(JSONObject.toJSONString(TongYouCancelOutboundReq));
        String bodyStr = OkHttpUtils.doPostJson(getPreUrl()+path,JSONObject.toJSONString(TongYouCancelOutboundReq), headerMap);
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
