package com.sdk.wms.zhongbao;

import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.common.business.threadlocal.ThirdWarehouseContext;
import com.sdk.wms.zhongbao.dto.request.*;
import com.sdk.wms.zhongbao.dto.response.*;
import com.sdk.wms.zhongbao.service.ZhongbaoService;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest(classes= ZhongbaoService.class)
class ZhongbaoServiceTest {
    //prod
//    static final String appKey = "Va2lD4obI60xIFJUHMaY0d9j0HUtTZ9S";
//    static final String appSecret = "6LzmNOSa9AZQfhltff3v3DnKHJkwXpBUuGvvSbwJtH0Vx24MMXP2D43TYfAakvZTWvslGxck1Z8iKTIzhxE3g9xW9D0wRKpGn18pT0WViHNvcD5BcUXqLKXTTliIoh9V";
    //test
    static final String appKey = "xvMMiIKs1m9rwzjHI26AiaPW7TNmUGnO";
    static final String appSecret = "4lFb4ZGFnMRiPsZTfjqwXdL8TPQzsDHpA6RqjTOIElS9WuLsNNnc7agBE7H6DctEXBFr99ZlSh7DA8SCZFa9fw4GFXjtnDuBbQxCqtAoLVaiN0NV8LccSNFRpa6tnEMG";

    @Resource
    private ZhongbaoService zhongbaoService;

    private Map<String, Object> authMap = new HashMap<>();

    public ZhongbaoServiceTest(){
        authMap.put("appKey",appKey);
        authMap.put("appSecret",appSecret);
    }
    @Test
    public void open() {
        String token = zhongbaoService.getToken(appKey, appSecret);
        BaseResponse response = zhongbaoService.open(token);
        System.out.println(JSONUtil.toJsonStr(response));
    }
    @Test
    public void warehouseList() {
        String token = zhongbaoService.getToken(appKey, appSecret);
        WarehouseRequest warehouseRequest = WarehouseRequest.builder().build();
        BaseResponse<WarehouseResponse> response = zhongbaoService.warehouseList(token, warehouseRequest);
        System.out.println(JSONUtil.toJsonStr(response));
    }

    @Test
    public void productList() {
        String token = zhongbaoService.getToken(appKey, appSecret);
        ProductRequest productRequest = ProductRequest.builder().build();
        BaseResponse<ProductResponse> response = zhongbaoService.productList(token, productRequest);
        System.out.println(JSONUtil.toJsonStr(response));
    }
    @Test
    public void chanelList() {
//        String token = zhongbaoService.getToken(appKey, appSecret);
        ChannelRequest channelRequest = ChannelRequest.builder().commonParam(CommonRequest.builder().pageParam(PageRequest.builder().pageNum("0").pageSize("10").build()).build()).build();
        List<ChannelResponse.Channel> channels = zhongbaoService.chanelList(authMap, channelRequest);
        System.out.println(JSONUtil.toJsonStr(channels));
    }
    @Test
    public void inventoryFlow(){
        String token = zhongbaoService.getToken(appKey, appSecret);
        OverseasInboundReceiveRequest overseasInboundReceiveRequest = OverseasInboundReceiveRequest.builder().build();
        BaseResponse<OverseasInboundReceiveResponse> response = zhongbaoService.inventoryFlow(token, overseasInboundReceiveRequest);
        System.out.println(JSONUtil.toJsonStr(response));
    }

    @Test
    public void jsonTest(){
        String bodyStr = "{\"code\":\"20000\",\"success\":true,\"data\":{\"orderNo\":\"AO26031134616930\",\"referenceNo\":\"FHD260311000002\",\"companyName\":\"\",\"transitType\":1,\"warehouseCode\":\"DEMO\",\"shippingType\":1,\"containerType\":-1,\"containerNo\":\"\",\"shelfMode\":1,\"trackingNo\":\"SF7444701803899\",\"asnDesc\":\"SF7444701803899\",\"orderType\":1,\"isUsePallet\":1,\"isMixedPallet\":-1,\"palletQty\":0,\"mixedPalletQty\":0,\"carrierCode\":\"\",\"etaDate\":\"2026-03-12\",\"stockType\":1,\"emailOfTowingContainer\":\"\",\"totalBox\":1,\"receiveTotalBox\":0,\"totalCategory\":1,\"receiveTotalCategory\":0,\"totalProduct\":10,\"totalPutaway\":0,\"totalWeight\":70.000000,\"totalVolume\":193.430000,\"totalPrice\":0.0000,\"remark\":\"SF7444701803899\",\"feePrice\":0,\"status\":1,\"cancelType\":1,\"cancelRemark\":\"\",\"errorReason\":\"\"},\"message\":\"请求成功\",\"errors\":[],\"timestamp\":\"1773209890641\",\"duration\":0.896,\"requestId\":\"faaca8db97004995afdaea1c45c612d6\"}";
        BaseResponse<OverseasInboundCreateResponse> response = JSON.parseObject(bodyStr, new com.alibaba.fastjson.TypeReference<BaseResponse<OverseasInboundCreateResponse>>() {}.getType());
        System.out.println(JSONUtil.toJsonStr(response));
    }

    @Test
    public void outboundTest(){
        ThirdWarehouseContext.setAuthMap(authMap);
        OutboundB2cQueryRequest queryRequest = OutboundB2cQueryRequest.builder().startUpdateTime("2026-03-11 00:00:00").endUpdateTime("2026-03-17 00:00:00").build();
        BaseResponse<OutboundB2cQueryResponse> listBaseResponse = zhongbaoService.queryB2cOutboundBill(queryRequest);
        System.out.println(JSONUtil.toJsonStr(listBaseResponse));
    }
}
