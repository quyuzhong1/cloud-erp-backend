package com.sdk.wms.zhongbao.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.common.business.constant.BusinessCommonConstants;
import com.common.core.exception.ServiceException;
import com.sdk.wms.zhongbao.dto.request.*;
import com.sdk.wms.zhongbao.dto.response.*;
import com.sdk.wms.zhongbao.utils.AuthUtils;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.stereotype.Component;

import javax.validation.Valid;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author zdy
 * @ClassName zhongbaoService
 * @description: TODO
 * @date 2026年03月02日
 * @version: 1.0
 */
@Slf4j
@Component
public class ZhongbaoService {
//    static final String appKey = "keyxvMMiIKs1m9rwzjHI26AiaPW7TNmUGnO";
//    static final String appSecret = "T8ICf0y9tDYA5JPI9iI0EbHkPjj2QNAKtHykwskrzxQkW20qC8SeEnByEVWzSjIXFlBmc5h4XT6GR1C26lbWLsDGGOXwqbkycFJByzZhtgwa3wJWTaQ7LNbxSVUdWvBL";

    public static void main(String[] args) {
        ZhongbaoService service = new ZhongbaoService();
//        BaseResponse open = service.open(service.getToken(appKey, appSecret));
//        log.info("open: {}", open);
    }

    /**
     * 获取token
     *
     * @param appKey
     * @param appSecret
     * @return
     */
    public String getToken(String appKey, String appSecret) {
        String token = "";
        if (BusinessCommonConstants.hasProfile("prod")) {
            token = AuthUtils.getToken(appKey, appSecret);
        } else {
            token = getTestToken(appKey);
        }
        log.warn("token: {}", token);
        return token;
    }

    public String getToken(Map<String, Object> authMap) {
        String appKey = authMap.get("appKey").toString();
        String appSecret = authMap.get("appSecret").toString();
        return getToken(appKey, appSecret);
    }

    private String getTestToken(String appKey) {
        OkHttpClient client = new OkHttpClient().newBuilder()
                .build();
        Request request = new Request.Builder()
                .url(getPreUrl() + "/open/open/api-token?apiKey=" + appKey)
                .method("GET", null)
                .build();
        try {
            Response response = client.newCall(request).execute();
            String bodyStr = response.body().string();
            log.warn("response: {}", bodyStr);
            //{"code":"20000","success":true,"data":"QlppRkpwMXlWY3FhS2lCeVk0QmtqbGdmZmNrRDh2SmgtMTc3MjQyNDYyMTI3Ny00MmViNjkyMzRmOTY0ZDM2OWY2ZjBkNDNmYWM4ZmU5Zi1qVEE3WUNKbStSMElYVm83VXFHcEkybG01c1Vmcmd3QnVCT0dSQjhPS3ZzPQ==","message":"请求成功","errors":[],"timestamp":"1772424621280","duration":0.005,"requestId":"aafeda4e70e442c4b4ef5c8db258b21f"}
            BaseResponse<String> baseResponse = JSONUtil.toBean(bodyStr, BaseResponse.class);
            if (baseResponse.getSuccess()) {
                return baseResponse.getData();
            } else {
                log.error("请求失败, message: {}, code: {}", baseResponse.getMessage(), baseResponse.getCode());
                throw new ServiceException("请求失败, message: " + baseResponse.getMessage() + ", code: " + baseResponse.getCode());
            }
        } catch (ServiceException e) {
            log.error("请求失败,异常: {}", e);
            throw new ServiceException(e.getMessage());
        } catch (IOException e) {
            log.error("请求失败,异常: {}", e);
            throw new ServiceException("请求失败,异常: " + e.getMessage());
        }
    }

    private String getPreUrl() {
        if (BusinessCommonConstants.hasProfile("prod")) {
            return "https://oms-api.zbao56.com";
        } else {
            return "https://oms-api-dev.zbao56.com";
        }
    }

    /**
     * 校验身份是否有效
     *
     * @param token
     * @return
     */
    public BaseResponse open(String token) {
        log.warn("生成的token: {}", token);
        OkHttpClient client = new OkHttpClient().newBuilder().build();
        Request request = new Request.Builder()
                .url(getPreUrl() + "/open")
                .method("GET", null)
                .addHeader("Authorization", token)
                .build();
        try {
            Response response = client.newCall(request).execute();
            String bodyStr = response.body().string();
            log.warn("bodyStr: {}", bodyStr);
            return JSONUtil.toBean(bodyStr, BaseResponse.class);
        } catch (IOException e) {
            log.error("请求失败,异常: {}", e);
            throw new ServiceException("请求失败,异常: " + e.getMessage());
        }
    }

    /**
     * 仓库列表
     *
     * @return
     */
    public BaseResponse<WarehouseResponse> warehouseList(String token, WarehouseRequest warehouseRequest) {
        log.warn("生成的token: {}, request: {}", token, JSONUtil.toJsonStr(warehouseRequest));
        OkHttpClient client = new OkHttpClient().newBuilder().build();
        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(mediaType, JSONUtil.toJsonStr(warehouseRequest));
        Request request = new Request.Builder()
                .url(getPreUrl() + "/open/common-data/warehouse-list")
                .method("POST", body)
                .addHeader("Authorization", token)
                .addHeader("Content-Type", "application/json")
                .build();
        try {
            Response response = client.newCall(request).execute();
            String bodyStr = response.body().string();
            log.warn("bodyStr: {}", bodyStr);
            return JSONUtil.toBean(bodyStr, BaseResponse.class);
        } catch (IOException e) {
            log.error("请求失败,异常: {}", e);
            throw new ServiceException("请求失败,异常: " + e.getMessage());
        }
    }

    /*
     * 产品列表
     */
    public BaseResponse<ProductResponse> productList(String token, ProductRequest productRequest) {
        log.warn("生成的token: {}, request: {}", token, JSONUtil.toJsonStr(productRequest));
        OkHttpClient client = new OkHttpClient().newBuilder().build();
        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(mediaType, JSONUtil.toJsonStr(productRequest));
        Request request = new Request.Builder()
                .url(getPreUrl() + "/open/product-data/list")
                .method("POST", body)
                .addHeader("Authorization", token)
                .addHeader("Content-Type", "application/json")
                .build();
        try {
            Response response = client.newCall(request).execute();
            String bodyStr = response.body().string();
            log.warn("bodyStr: {}", bodyStr);
            return JSON.parseObject(bodyStr, new TypeReference<BaseResponse<ProductResponse>>() {
            }.getType());
        } catch (IOException e) {
            log.error("请求失败,异常: {}", e);
            throw new ServiceException("请求失败,异常: " + e.getMessage());
        }
    }

    /**
     * 渠道列表
     *
     * @param channelRequest
     * @return
     */
    public List<ChannelResponse.Channel> chanelList(Map<String, Object> authMap, ChannelRequest channelRequest) {
        log.warn("生成的 request: {}", JSONUtil.toJsonStr(channelRequest));
        List<ChannelResponse.Channel> list = new ArrayList<>();
        boolean hasNext = true;
        Integer pageNum = Integer.valueOf(channelRequest.getCommonParam().getPageParam().getPageNum());
        Integer pageSize = Integer.valueOf(channelRequest.getCommonParam().getPageParam().getPageSize());
        while (hasNext) {
            String token = getToken(authMap);
            pageNum++;
            channelRequest.getCommonParam().getPageParam().setPageNum(String.valueOf(pageNum));
            channelRequest.getCommonParam().getPageParam().setPageSize(String.valueOf(pageSize));
            OkHttpClient client = new OkHttpClient().newBuilder().build();
            MediaType mediaType = MediaType.parse("application/json");
            RequestBody body = RequestBody.create(mediaType, JSONUtil.toJsonStr(channelRequest));
            Request request = new Request.Builder()
                    .url(getPreUrl() + "/open/common-data/shipping-method-list")
                    .method("POST", body)
                    .addHeader("Authorization", token)
                    .addHeader("Content-Type", "application/json")
                    .build();
            try {
                Response response = client.newCall(request).execute();
                String bodyStr = response.body().string();
                log.warn("bodyStr: {}", bodyStr);
                BaseResponse<ChannelResponse> channelResponseBaseResponse = JSON.parseObject(bodyStr, new TypeReference<BaseResponse<ChannelResponse>>() {
                }.getType());
                if (channelResponseBaseResponse.getSuccess()) {
                    String pageNum1 = channelResponseBaseResponse.getData().getPageNum();
                    String totalPage1 = channelResponseBaseResponse.getData().getTotalPage();
                    if (CollUtil.isNotEmpty(channelResponseBaseResponse.getData().getList())) {
                        list.addAll(channelResponseBaseResponse.getData().getList());
                    }
                    hasNext = Integer.valueOf(pageNum1).compareTo(Integer.valueOf(totalPage1)) < 0;
                } else {
                    hasNext = false;
                }
            } catch (IOException e) {
                log.error("请求失败,异常: {}", e);
                throw new ServiceException("请求失败,异常: " + e.getMessage());
            }
        }
        return list;
    }

    /**
     * 海外入库单创建
     *
     * @param token
     * @param overseasInboundCreateRequest
     * @return
     */
    public BaseResponse<OverseasInboundCreateResponse> overseasInboundCreate(String token, OverseasInboundCreateRequest overseasInboundCreateRequest) {
        log.warn("生成的token: {}, request: {}", token, JSONUtil.toJsonStr(overseasInboundCreateRequest));
        OkHttpClient client = new OkHttpClient().newBuilder().build();
        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(mediaType, JSONUtil.toJsonStr(overseasInboundCreateRequest));
        Request request = new Request.Builder()
                .url(getPreUrl() + "/open/asn-order-data/create")
                .method("POST", body)
                .addHeader("Authorization", token)
                .addHeader("Content-Type", "application/json")
                .build();
        try {
            Response response = client.newCall(request).execute();
            String bodyStr = response.body().string();
            log.warn("bodyStr: {}", bodyStr);
            return JSON.parseObject(bodyStr, new TypeReference<BaseResponse<OverseasInboundCreateResponse>>() {
            }.getType());
        } catch (IOException e) {
            log.error("请求失败,异常: {}", e);
            throw new ServiceException("请求失败,异常: " + e.getMessage());
        }
    }

    /**
     * 海外入库单更新
     *
     * @param token
     * @param overseasInboundUpdateRequest
     * @return
     */
    public BaseResponse<OverseasInboundUpdateResponse> overseasInboundUpdate(String token, OverseasInboundUpdateRequest overseasInboundUpdateRequest) {
        log.warn("生成的token: {}, request: {}", token, JSONUtil.toJsonStr(overseasInboundUpdateRequest));
        OkHttpClient client = new OkHttpClient().newBuilder().build();
        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(mediaType, JSONUtil.toJsonStr(overseasInboundUpdateRequest));
        Request request = new Request.Builder()
                .url(getPreUrl() + "/open/asn-order-data/update")
                .method("POST", body)
                .addHeader("Authorization", token)
                .addHeader("Content-Type", "application/json")
                .build();
        try {
            Response response = client.newCall(request).execute();
            String bodyStr = response.body().string();
            log.warn("bodyStr: {}", bodyStr);
            return JSON.parseObject(bodyStr, new TypeReference<BaseResponse<OverseasInboundUpdateResponse>>() {
            }.getType());
        } catch (IOException e) {
            log.error("请求失败,异常: {}", e);
            throw new ServiceException("请求失败,异常: " + e.getMessage());
        }
    }

    /**
     * 海外入库单取消
     *
     * @param token
     * @param overseasInboundCancelRequest
     * @return
     */
    public BaseResponse<OverseasInboundCancelResponse> overseasInboundUpdate(String token, OverseasInboundCancelRequest overseasInboundCancelRequest) {
        log.warn("生成的token: {}, request: {}", token, JSONUtil.toJsonStr(overseasInboundCancelRequest));
        OkHttpClient client = new OkHttpClient().newBuilder().build();
        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(mediaType, JSONUtil.toJsonStr(overseasInboundCancelRequest));
        Request request = new Request.Builder()
                .url(getPreUrl() + "/open/asn-order-data/cancel")
                .method("POST", body)
                .addHeader("Authorization", token)
                .addHeader("Content-Type", "application/json")
                .build();
        try {
            Response response = client.newCall(request).execute();
            String bodyStr = response.body().string();
            log.warn("bodyStr: {}", bodyStr);
            return JSON.parseObject(bodyStr, new TypeReference<BaseResponse<OverseasInboundCancelResponse>>() {
            }.getType());
        } catch (IOException e) {
            log.error("请求失败,异常: {}", e);
            throw new ServiceException("请求失败,异常: " + e.getMessage());
        }
    }

    /**
     * 海外入库单审批
     *
     * @param token
     * @param overseasInboundApproveRequest
     * @return
     */
    public BaseResponse<OverseasInboundApproveResponse> overseasInboundUpdate(String token, OverseasInboundApproveRequest overseasInboundApproveRequest) {
        log.warn("生成的token: {}, request: {}", token, JSONUtil.toJsonStr(overseasInboundApproveRequest));
        OkHttpClient client = new OkHttpClient().newBuilder().build();
        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(mediaType, JSONUtil.toJsonStr(overseasInboundApproveRequest));
        Request request = new Request.Builder()
                .url(getPreUrl() + "/open/asn-order-data/create-review")
                .method("POST", body)
                .addHeader("Authorization", token)
                .addHeader("Content-Type", "application/json")
                .build();
        try {
            Response response = client.newCall(request).execute();
            String bodyStr = response.body().string();
            log.warn("bodyStr: {}", bodyStr);
            return JSON.parseObject(bodyStr, new TypeReference<BaseResponse<OverseasInboundApproveResponse>>() {
            }.getType());
        } catch (IOException e) {
            log.error("请求失败,异常: {}", e);
            throw new ServiceException("请求失败,异常: " + e.getMessage());
        }
    }

    /**
     * 库存流水列表
     *
     * @param token
     * @param overseasInboundReceiveRequest
     * @return
     */
    public BaseResponse<OverseasInboundReceiveResponse> overseasInboundUpdate(String token, OverseasInboundReceiveRequest overseasInboundReceiveRequest) {
        log.warn("生成的token: {}, request: {}", token, JSONUtil.toJsonStr(overseasInboundReceiveRequest));
        OkHttpClient client = new OkHttpClient().newBuilder().build();
        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(mediaType, JSONUtil.toJsonStr(overseasInboundReceiveRequest));
        Request request = new Request.Builder()
                .url(getPreUrl() + "/open/inventory-data/record-list")
                .method("POST", body)
                .addHeader("Authorization", token)
                .addHeader("Content-Type", "application/json")
                .build();
        try {
            Response response = client.newCall(request).execute();
            String bodyStr = response.body().string();
            log.warn("bodyStr: {}", bodyStr);
            return JSON.parseObject(bodyStr, new TypeReference<BaseResponse<OverseasInboundReceiveResponse>>() {
            }.getType());
        } catch (IOException e) {
            log.error("请求失败,异常: {}", e);
            throw new ServiceException("请求失败,异常: " + e.getMessage());
        }
    }

    /**
     * 创建出库单
     */
    public BaseResponse<OverseasOutboundCreateResponse> createOutboundBill(String token, OverseasOutboundCreateRequest overseasOutboundCreateRequest){
        log.warn("生成的token: {}, request: {}", token, JSONUtil.toJsonStr(overseasOutboundCreateRequest));
        OkHttpClient client = new OkHttpClient().newBuilder().build();
        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(mediaType, JSONUtil.toJsonStr(overseasOutboundCreateRequest));
        Request request = new Request.Builder()
                .url(getPreUrl() + "/open/outbound-order-data/b2b/create-review")
                .method("POST", body)
                .addHeader("Authorization", token)
                .addHeader("Content-Type", "application/json")
                .build();
        try {
            Response response = client.newCall(request).execute();
            String bodyStr = response.body().string();
            log.warn("bodyStr: {}", bodyStr);
            return JSON.parseObject(bodyStr, new TypeReference<BaseResponse<OverseasInboundReceiveResponse>>() {
            }.getType());
        } catch (IOException e) {
            log.error("请求失败,异常: {}", e);
            throw new ServiceException("请求失败,异常: " + e.getMessage());
        }
    }
}
