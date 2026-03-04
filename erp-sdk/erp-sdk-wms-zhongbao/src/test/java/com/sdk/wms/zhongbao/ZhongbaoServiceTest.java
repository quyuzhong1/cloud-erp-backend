package com.sdk.wms.zhongbao;

import cn.hutool.json.JSONUtil;
import com.sdk.wms.zhongbao.dto.request.ChannelRequest;
import com.sdk.wms.zhongbao.dto.request.ProductRequest;
import com.sdk.wms.zhongbao.dto.request.WarehouseRequest;
import com.sdk.wms.zhongbao.dto.response.BaseResponse;
import com.sdk.wms.zhongbao.dto.response.ChannelResponse;
import com.sdk.wms.zhongbao.dto.response.ProductResponse;
import com.sdk.wms.zhongbao.dto.response.WarehouseResponse;
import com.sdk.wms.zhongbao.service.ZhongbaoService;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.HashMap;
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
        ProductRequest productRequest = ProductRequest.builder().status(3).build();
        BaseResponse<ProductResponse> response = zhongbaoService.productList(token, productRequest);
        System.out.println(JSONUtil.toJsonStr(response));
    }
    @Test
    public void chanelList() {
        String token = zhongbaoService.getToken(appKey, appSecret);
        ChannelRequest channelRequest = ChannelRequest.builder().build();
        BaseResponse<ChannelResponse> response = zhongbaoService.chanelList(token, channelRequest);
        System.out.println(JSONUtil.toJsonStr(response));
    }
}
