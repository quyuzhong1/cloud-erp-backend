package com.sdk.wms.zhongbao.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.common.business.constant.BusinessCommonConstants;
import com.common.business.threadlocal.ThirdWarehouseContext;
import com.common.core.exception.ServiceException;
import com.sdk.wms.zhongbao.dto.request.*;
import com.sdk.wms.zhongbao.dto.response.*;
import com.sdk.wms.zhongbao.utils.AuthUtils;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
    public BaseResponse<ProductResponse> productList(ProductRequest productRequest) {
        String token = getToken(ThirdWarehouseContext.getAuthMap());
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
        Integer pageNum = channelRequest.getCommonParam().getPageParam().getPageNum();
        Integer pageSize = channelRequest.getCommonParam().getPageParam().getPageSize();
        while (hasNext) {
            String token = getToken(authMap);
            pageNum++;
            channelRequest.getCommonParam().getPageParam().setPageNum(pageNum);
            channelRequest.getCommonParam().getPageParam().setPageSize(pageSize);
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
     * @param overseasInboundCreateRequest
     * @return
     */
    public BaseResponse<OverseasInboundCreateResponse> overseasInboundCreate(OverseasInboundCreateRequest overseasInboundCreateRequest) {
        String token = getToken(ThirdWarehouseContext.getAuthMap());
        log.warn("生成的token: {}, request: {}", token, JSONUtil.toJsonStr(overseasInboundCreateRequest));
        ThirdWarehouseContext.setRequestJson(JSONUtil.toJsonStr(overseasInboundCreateRequest));
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
            ThirdWarehouseContext.setResponseJson(bodyStr);
            return JSON.parseObject(bodyStr, new TypeReference<BaseResponse<OverseasInboundCreateResponse>>() {
            }.getType());
        } catch (IOException e) {
            log.error("请求失败,异常: {}", e);
            ThirdWarehouseContext.setResponseJson("请求失败,异常: {}" + e.getMessage());
            throw new ServiceException("请求失败,异常: " + e.getMessage());
        }
    }

    /**
     * 海外入库单更新
     *
     * @param createRequest
     * @return
     */
    public BaseResponse<OverseasInboundUpdateResponse> overseasInboundUpdate(OverseasInboundCreateRequest createRequest) {
        String token = getToken(ThirdWarehouseContext.getAuthMap());
        log.warn("生成的token: {}, request: {}", token, JSONUtil.toJsonStr(createRequest));
        ThirdWarehouseContext.setRequestJson(JSONUtil.toJsonStr(createRequest));
        OkHttpClient client = new OkHttpClient().newBuilder().build();
        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(mediaType, JSONUtil.toJsonStr(createRequest));
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
            ThirdWarehouseContext.setResponseJson(bodyStr);
            return JSON.parseObject(bodyStr, new TypeReference<BaseResponse<OverseasInboundUpdateResponse>>() {
            }.getType());
        } catch (IOException e) {
            log.error("请求失败,异常: {}", e);
            ThirdWarehouseContext.setResponseJson("请求失败,异常: {}" + e.getMessage());
            throw new ServiceException("请求失败,异常: " + e.getMessage());
        }
    }

    /**
     * 海外入库单取消
     *
     * @param overseasInboundCancelRequest
     * @return
     */
    public BaseResponse<OverseasInboundCancelResponse> overseasInboundCancel(OverseasInboundCancelRequest overseasInboundCancelRequest) {
        String token = getToken(ThirdWarehouseContext.getAuthMap());
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
            ThirdWarehouseContext.setResponseJson(bodyStr);
            return JSON.parseObject(bodyStr, new TypeReference<BaseResponse<OverseasInboundCancelResponse>>() {
            }.getType());
        } catch (IOException e) {
            log.error("请求失败,异常: {}", e);
            ThirdWarehouseContext.setResponseJson("请求失败,异常: {}" + e.getMessage());
            throw new ServiceException("请求失败,异常: " + e.getMessage());
        }
    }

    /**
     * 海外入库单审批
     *
     * @param inboundCreateRequest
     * @return
     */
    public BaseResponse<OverseasInboundApproveResponse> overseasInboundApprove(OverseasInboundCreateRequest inboundCreateRequest) {
        String token = getToken(ThirdWarehouseContext.getAuthMap());
        log.warn("生成的token: {}, request: {}", token, JSONUtil.toJsonStr(inboundCreateRequest));
        OkHttpClient client = new OkHttpClient().newBuilder().build();
        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(mediaType, JSONUtil.toJsonStr(inboundCreateRequest));
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
            ThirdWarehouseContext.setResponseJson(bodyStr);
            return JSON.parseObject(bodyStr, new TypeReference<BaseResponse<OverseasInboundApproveResponse>>() {
            }.getType());
        } catch (IOException e) {
            log.error("请求失败,异常: {}", e);
            ThirdWarehouseContext.setResponseJson("请求失败,异常: {}" + e.getMessage());
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
    public BaseResponse<OverseasInboundReceiveResponse> inventoryFlow(String token, OverseasInboundReceiveRequest overseasInboundReceiveRequest) {
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
    public OverseasOutboundCreateResponse createOutboundBill(OverseasOutboundCreateRequest overseasOutboundCreateRequest){
        String token = getToken(ThirdWarehouseContext.getAuthMap());
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
            return JSON.parseObject(bodyStr, new TypeReference<OverseasOutboundCreateResponse>() {}.getType());
        } catch (IOException e) {
            log.error("请求失败,异常: {}", e);
            throw new ServiceException("请求失败,异常: " + e.getMessage());
        }
    }

    /**
     * 取消出库单
     */
    public BaseResponse<OverseasOutboundCancelResponse> cancelOutboundBill(OverseasOutboundCancelRequest overseasOutboundCancelRequest){
        String token = getToken(ThirdWarehouseContext.getAuthMap());
        log.warn("生成的token: {}, request: {}", token, JSONUtil.toJsonStr(overseasOutboundCancelRequest));
        ThirdWarehouseContext.setRequestJson(JSONUtil.toJsonStr(overseasOutboundCancelRequest));
        OkHttpClient client = new OkHttpClient().newBuilder().build();
        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(mediaType, JSONUtil.toJsonStr(overseasOutboundCancelRequest));
        Request request = new Request.Builder()
                .url(getPreUrl() + "/open/outbound-order-data/cancel")
                .method("POST", body)
                .addHeader("Authorization", token)
                .addHeader("Content-Type", "application/json")
                .build();
        try {
            Response response = client.newCall(request).execute();
            String bodyStr = response.body().string();
            log.warn("bodyStr: {}", bodyStr);
            ThirdWarehouseContext.setResponseJson(bodyStr);
            return JSON.parseObject(bodyStr, new TypeReference<BaseResponse<OverseasOutboundCancelResponse>>() {
            }.getType());
        } catch (IOException e) {
            log.error("请求失败,异常: {}", e);
            ThirdWarehouseContext.setResponseJson("请求失败,异常:" + e.getMessage());
            throw new ServiceException("请求失败,异常: " + e.getMessage());
        }
    }

    /**
     * 查询出库单
     */
    public OverseasOutboundQueryResponse queryOutboundBill(OverseasOutboundQueryRequest overseasOutboundQueryRequest){
        String token = getToken(ThirdWarehouseContext.getAuthMap());
        log.warn("生成的token: {}, request: {}", token, JSONUtil.toJsonStr(overseasOutboundQueryRequest));
        OkHttpClient client = new OkHttpClient().newBuilder().build();
        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(mediaType, JSONUtil.toJsonStr(overseasOutboundQueryRequest));
        Request request = new Request.Builder()
                .url(getPreUrl() + "/open/outbound-order-data/b2b/list")
                .method("POST", body)
                .addHeader("Authorization", token)
                .addHeader("Content-Type", "application/json")
                .build();
        try {
            Response response = client.newCall(request).execute();
            String bodyStr = response.body().string();
            log.warn("bodyStr: {}", bodyStr);
            return JSON.parseObject(bodyStr, new TypeReference<OverseasOutboundQueryResponse>() {}.getType());
        } catch (IOException e) {
            log.error("请求失败,异常: {}", e);
            throw new ServiceException("请求失败,异常: " + e.getMessage());
        }
    }

    /**
     * B2C出库单查询
     * @param queryRequest
     * @return
     */
    public BaseResponse<OutboundB2cQueryResponse> queryB2cOutboundBill(OutboundB2cQueryRequest queryRequest) {
        String token = getToken(ThirdWarehouseContext.getAuthMap());
        log.warn("生成的token: {}, request: {}", token, JSONUtil.toJsonStr(queryRequest));
        OkHttpClient client = new OkHttpClient().newBuilder().build();
        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(mediaType, JSONUtil.toJsonStr(queryRequest));
        Request request = new Request.Builder()
                .url(getPreUrl() + "/open/outbound-order-data/b2c/list")
                .method("POST", body)
                .addHeader("Authorization", token)
                .addHeader("Content-Type", "application/json")
                .build();
        try {
            Response response = client.newCall(request).execute();
            String bodyStr = response.body().string();
            log.warn("bodyStr: {}", bodyStr);
            ThirdWarehouseContext.setResponseJson(bodyStr);
            return JSON.parseObject(bodyStr, new TypeReference<BaseResponse<OutboundB2cQueryResponse>>() {
            }.getType());
        } catch (IOException e) {
            log.error("请求失败,异常: {}", e);
            ThirdWarehouseContext.setResponseJson("请求失败,异常: {}" + e.getMessage());
            throw new ServiceException("请求失败,异常: " + e.getMessage());
        }
    }

    /**
     * B2C出库单创建
     * @param createRequest
     * @return
     */
    public BaseResponse<OutboundB2cCreateResponse> createB2cOutboundBill(OutboundB2cCreateRequest createRequest) {
        String token = getToken(ThirdWarehouseContext.getAuthMap());
        log.warn("生成的token: {}, request: {}", token, JSONUtil.toJsonStr(createRequest));
        OkHttpClient client = new OkHttpClient().newBuilder().build();
        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(mediaType, JSONUtil.toJsonStr(createRequest));
        Request request = new Request.Builder()
                .url(getPreUrl() + "/open/outbound-order-data/b2c/create-review")
                .method("POST", body)
                .addHeader("Authorization", token)
                .addHeader("Content-Type", "application/json")
                .build();
        try {
            Response response = client.newCall(request).execute();
            String bodyStr = response.body().string();
            log.warn("bodyStr: {}", bodyStr);
            ThirdWarehouseContext.setResponseJson(bodyStr);
            return JSON.parseObject(bodyStr, new TypeReference<BaseResponse<OutboundB2cCreateResponse>>() {
            }.getType());
        } catch (IOException e) {
            log.error("请求失败,异常: {}", e);
            ThirdWarehouseContext.setResponseJson("请求失败,异常: {}" + e.getMessage());
            throw new ServiceException("请求失败,异常: " + e.getMessage());
        }
    }

    /**
     * B2C出库单取消
     * @param cancelRequest
     * @return
     */
    public BaseResponse<OutboundB2cCancelResponse> cancelB2cOutboundBill(OutboundB2cCancelRequest cancelRequest) {
        String token = getToken(ThirdWarehouseContext.getAuthMap());
        log.warn("生成的token: {}, request: {}", token, JSONUtil.toJsonStr(cancelRequest));
        OkHttpClient client = new OkHttpClient().newBuilder().build();
        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(mediaType, JSONUtil.toJsonStr(cancelRequest));
        Request request = new Request.Builder()
                .url(getPreUrl() + "/open/outbound-order-data/cancel")
                .method("POST", body)
                .addHeader("Authorization", token)
                .addHeader("Content-Type", "application/json")
                .build();
        try {
            Response response = client.newCall(request).execute();
            String bodyStr = response.body().string();
            log.warn("bodyStr: {}", bodyStr);
            ThirdWarehouseContext.setResponseJson(bodyStr);
            return JSON.parseObject(bodyStr, new TypeReference<BaseResponse<OutboundB2cCancelResponse>>() {
            }.getType());
        } catch (IOException e) {
            log.error("请求失败,异常: {}", e);
            ThirdWarehouseContext.setResponseJson("请求失败,异常: {}" + e.getMessage());
            throw new ServiceException("请求失败,异常: " + e.getMessage());
        }
    }
}
