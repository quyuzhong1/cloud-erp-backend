package com.sdk.wms.jitu;

import cn.hutool.json.JSONUtil;
import com.sdk.wms.jitu.dto.request.ProductRequest;
import com.sdk.wms.jitu.dto.request.WarehouseRequest;
import com.sdk.wms.jitu.dto.response.WarehouseResponse;
import com.sdk.wms.jitu.service.JituService;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = JituService.class)
class JituServiceTest {
    //test
    static final String key = "MGY3MTU3M2M3NjYxN2RhNjc3NzM4MzM2Y2Q5ZjQ0ZWU=";
    static final String eccompanyid = "STANDARD";
    static final String customerid = "CS001";

    @Resource
    private JituService JituService;

    private Map<String, Object> authMap = new HashMap<>();

    public JituServiceTest() {
        authMap.put("key", key);
        authMap.put("eccompanyid", eccompanyid);
        authMap.put("customerid", customerid);
    }


    @Test
    public void warehouseList() {
        WarehouseRequest request = WarehouseRequest.builder().customerid((String) authMap.getOrDefault("customerid", "")).build();
        WarehouseResponse response = JituService.warehouseList(authMap, request);
        System.out.println(JSONUtil.toJsonStr(response));
    }

    @Test
    public void productList() {
        ProductRequest request = ProductRequest.builder()
                .customerid((String) authMap.getOrDefault("customerid", ""))
                .warehouseCode("SH.001")
                .startPage(1)
                .pageSize(100)
                .build();
        JituService.productList(authMap, request);
    }
}
