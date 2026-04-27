package com.sdk.wms.jitu;

import cn.hutool.json.JSONUtil;
import com.common.business.threadlocal.ThirdWarehouseContext;
import com.sdk.wms.jitu.dto.request.JituOverseasInboundCreateRequest;
import com.sdk.wms.jitu.dto.request.ProductRequest;
import com.sdk.wms.jitu.dto.request.StockOutOrderRequest;
import com.sdk.wms.jitu.dto.request.WarehouseRequest;
import com.sdk.wms.jitu.dto.response.WarehouseResponse;
import com.sdk.wms.jitu.service.JituService;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = JituService.class)
class JituServiceTest {
    //test
    static final String key = "YTQ1OTM0NjA5M2VlMmZhYTIzNjQxNjE1MWZmZTAwMzA=";
    static final String eccompanyid = "CS001_SDC";
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
                .pageSize(10)
                .build();
        JituService.productList(authMap, request);
    }

    @Test
    public void stockOutOrderList() {
        StockOutOrderRequest request = new StockOutOrderRequest();

        // 基本信息
        request.setWarehouseCode("SH.001");
        request.setCustomerid("CS001");
        //``request.setEccompanyid("STANDARD");
        request.setTxlogisticid("TT20230529001");
        request.setOrderType("XSCK");
        request.setSource("pdd");
        request.setPlatformNumber("XXXXX");
        request.setPayTime("2023-05-29 23:11:12");
        request.setItemsvalue(new BigDecimal("100.78"));

        // 收件人信息
        StockOutOrderRequest.Receiver receiver = new StockOutOrderRequest.Receiver();
        receiver.setName("客户");
        receiver.setCountrycode("CHN");
        receiver.setPostcode("155555");
        receiver.setMobile(" 6218305244590");
        receiver.setPhone(" 6281276497866");
        receiver.setProv("江苏省");
        receiver.setCity("南京市");
        receiver.setArea("玄武区");
        receiver.setAddress("A通世界华新园101");
        receiver.setAddress2("A通世界华新园102");
        receiver.setDoorNo("666");
        request.setReceiver(receiver);

        // 物流信息
        request.setTransportMode("ZTJ");
        request.setCarrier("JT");
        request.setRouteid("PD");
        request.setMailno("1234564698789789");
        request.setLabel("");
        request.setDeliveryNote("备注");
        request.setIsCod("0");
        request.setStoreCode("UPFOS001");

        // 商品信息
        java.util.List<StockOutOrderRequest.Item> items = new java.util.ArrayList<>();
        StockOutOrderRequest.Item item = new StockOutOrderRequest.Item();
        item.setItemCode("test-sdflfekfeww-green");
        item.setNumber(1);
        item.setItemvalue(new BigDecimal("5000.02"));
        item.setInventoryType("ZP");
        items.add(item);
        request.setItems(items);

        // 调用API
        try {
            com.sdk.wms.jitu.dto.response.StockOutOrderResponse response = JituService.createStockOutOrder(authMap, request);
            System.out.println(JSONUtil.toJsonStr(response));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void overseasInboundCreate() {
        ThirdWarehouseContext.setAuthMap(authMap);
        List<JituOverseasInboundCreateRequest.Item> items = new ArrayList<>();
        JituOverseasInboundCreateRequest.Item item = JituOverseasInboundCreateRequest.Item.builder()
                .LineNo("1")
                .itemCode("YL001")
                .quantity(1)
                .inventoryType("ZP")
                .build();
        items.add(item);
        JituOverseasInboundCreateRequest request = JituOverseasInboundCreateRequest.builder()
                .customerid((String) authMap.getOrDefault("customerid", ""))
                .warehouseCode("SH.001")
                .entryOrderCode("FHD260326000006")
                .erpOrderCode("FHD260326000006")
                .sourceSystem("SDC")
                .orderType("CGRK")
                .warehouseCode("SH.001")
                .items(items)
                .build();
        JituService.overseasInboundCreate(request);
    }
}
