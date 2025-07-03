package com.erp.wms.aliexpress.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.common.business.constant.BusinessCommonConstants;
import com.erp.wms.aliexpress.model.AliexpressAuthDTO;
import com.erp.wms.aliexpress.model.inbound.AliexpressInboundDTO;
import com.erp.wms.aliexpress.model.inbound.ApiInboundResponseDTO;
import com.erp.wms.aliexpress.model.inventory.ApiInventoryResponseDTO;
import com.erp.wms.aliexpress.model.order.AliexpressCancelOrderDTO;
import com.erp.wms.aliexpress.model.order.AliexpressOrderDTO;
import com.erp.wms.aliexpress.model.order.ApiOrderResponseDTO;
import com.erp.wms.aliexpress.model.product.AliexpressProductDTO;
import com.erp.wms.aliexpress.model.returnorder.AliexpressReturnInstockDTO;
import com.erp.wms.aliexpress.model.returnorder.ApiReturnOrderResponseDTO;
import com.erp.wms.aliexpress.util.ApiException;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import org.junit.Test;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;


@RunWith(SpringRunner.class)
@SpringBootTest(classes={AliexpressWarehouseService.class, BusinessCommonConstants.class})
//@TestPropertySource(properties = {"dev=dev"})
public class AliexpressWarehouseServiceTest {

    @Resource
    private AliexpressWarehouseService aliexpressWarehouseService;

    @Test
    public void pushListing() throws ApiException{
        AliexpressAuthDTO aliexpressAuthDTO = buildAuthDTO();
        String a = "{\"aliexpressAuthDTO\":{\"url\":\"https://api-sg.aliexpress.com\",\"appKey\":\"503630\",\"appSecret\":\"PxkJJ2fLGh5HcwzhUJp267lQSbkuAFRJ\",\"shopId\":\"1735117862084808706\",\"ownerCode\":\"17379911544\",\"accessToken\":\"50000701530cnHtbirhzrd7ijPeou2emiSIajwwCg118389611msriNQyROLyoVZ1y1j\"},\"productDTO\":{\"item_code\":\"test062606\",\"item_type\":\"ZC\",\"net_weight\":0.551,\"gross_weight\":0.551,\"height\":90,\"length\":112,\"item_name\":\"test062606名称\",\"bar_code\":\"test062606\",\"width\":112},\"actionType\":\"add\",\"listingId\":\"1938486948294311937\"}";
//        AliexpressProductDTO.ProductDTO productDTO =  JSON.parseObject(a,new TypeReference<AliexpressProductDTO>() {}.getType());
//        productDTO.setItemCode("2028");
//        productDTO.setItemName("YN50MM F1.8 C");
//        productDTO.setBarCode("2028");
//        productDTO.setItemType("ZC");
//        productDTO.setHeight(new BigDecimal("1.1"));
//        productDTO.setWidth(new BigDecimal("3.3"));
//        productDTO.setLength(new BigDecimal("2.2"));
//        productDTO.setNetWeight(new BigDecimal("1.11"));
//        productDTO.setGrossWeight(new BigDecimal("0.1"));
        AliexpressProductDTO aliexpressProductDTO = JSON.parseObject(a,new TypeReference<AliexpressProductDTO>() {}.getType());
//        aliexpressProductDTO.setActionType("add");
//        aliexpressProductDTO.setAliexpressAuthDTO(aliexpressAuthDTO);
//        aliexpressProductDTO.setProductDTO(productDTO);
        aliexpressWarehouseService.pushListing(aliexpressProductDTO);

    }

    @Test
    public void createOutbound() throws ApiException {
        AliexpressAuthDTO aliexpressAuthDTO = buildAuthDTO();
        AliexpressOrderDTO aliexpressCancelOrderDTO = new AliexpressOrderDTO();
        aliexpressCancelOrderDTO.setAliexpressAuthDTO(aliexpressAuthDTO);
        aliexpressCancelOrderDTO.setDeliveryOrder(AliexpressOrderDTO.DeliveryOrder.builder()
                        .orderType("JYCK")
                        .ownerCode("17379911544")
                        .receiverInfo(AliexpressOrderDTO.DeliveryOrder.ReceiverInfoDTO.builder()
                                .countryCode("US")
                                .build())
                        .deliveryOrderCode("TESTWJ062504")
                        .warehouseCode("STB")
                        .shopNick("测试店铺")
                        .logisticsCode("other")
                        .createTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                        .sourcePlatformCode("AE")
                        .expressCode("test-062504")
                .build());
        aliexpressCancelOrderDTO.setOrderLines(Arrays.asList(AliexpressOrderDTO.OrderLines.builder()
                        .inventoryType("1")
                        .planQty(2)
                        .ownerCode("17379911544")
                        .itemCode("12000043163694871")
//                        .itemId(1129930008)
                .build()));
        aliexpressCancelOrderDTO.setExtendProps(AliexpressOrderDTO.ExtendProps.builder()
                        .merchantType("POP")
                        .printInfo("https://cno-oss.oss-cn-zhangjiakou.aliyuncs.com/aePopDeliveryFlag/FB1046000016902640596-20250619143440995.pdf?Expires=1752906881&OSSAccessKeyId=LTAI5tLxwRuzKhwK2qzxjuCE&Signature=W%2BcwRRR5QpVHiI1MUCTS87ZVz6M%3D")
                .build());
        ApiOrderResponseDTO apiOrderResponseDTO = aliexpressWarehouseService.createOutbound(aliexpressCancelOrderDTO);
        System.out.println(apiOrderResponseDTO);
    }

    @Test
    public void cancelOrder() throws ApiException {
        AliexpressAuthDTO aliexpressAuthDTO = buildAuthDTO();
        AliexpressCancelOrderDTO aliexpressCancelOrderDTO = new AliexpressCancelOrderDTO();
        aliexpressCancelOrderDTO.setAliexpressAuthDTO(aliexpressAuthDTO);
            aliexpressCancelOrderDTO.setOrderId("LBX0395442105096030");
            aliexpressCancelOrderDTO.setOwnerCode("17379911544");
            aliexpressCancelOrderDTO.setWarehouseCode("STB");
            aliexpressCancelOrderDTO.setOrderType("JYCK");
            aliexpressCancelOrderDTO.setOrderCode("TEST13456");
        ApiOrderResponseDTO apiOrderResponseDTO = aliexpressWarehouseService.cancelOrder(aliexpressCancelOrderDTO);
        System.out.println(apiOrderResponseDTO);
    }

    @Test
    public void createInbound() throws ApiException{
        AliexpressInboundDTO aliexpressInboundDTO = AliexpressInboundDTO.builder()
                .aliexpressAuthDTO(buildAuthDTO())
                .entryOrder(AliexpressInboundDTO.EntryOrder.builder()
                        .orderType("SCRK")
                        .entryOrderCode("TEST-WJ062602")
                        .ownerCode("17379911544")
                        .orderCreateTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                        .warehouseCode("STB")
                        .build())
                .OrderLines(Arrays.asList(
                        AliexpressInboundDTO.OrderLines.builder()
                                .itemCode("12000043163694871")
                                .planQty(100)
                                .inventoryType("1")
                                .ownerCode("17379911544")
                                .build()
                ))
                .build();
        ApiInboundResponseDTO apiOrderResponseDTO = aliexpressWarehouseService.createInbound(aliexpressInboundDTO);
        System.out.println(apiOrderResponseDTO);
    }
    @Test
    public void createReturnInstockOrder() throws ApiException{
        AliexpressReturnInstockDTO aliexpressInboundDTO = AliexpressReturnInstockDTO.builder()
                .aliexpressAuthDTO(buildAuthDTO())
                .returnOrder(AliexpressReturnInstockDTO.ReturnOrder.builder()
                        .orderType("THRK")
                        .returnOrderCode("TEST-WJ062602")
                        .ownerCode("17379911544")
                        .senderInfo(AliexpressReturnInstockDTO.ReturnOrder.SenderInfoDTO.builder()
//                                .countryCode("CN")
//                                .province("广东省")
                                .detailAddress("广东省广州市天河区天汇大厦")
//                                .city("广州市")
//                                .mobile("123456789")
                                .build())
                        .warehouseCode("STB")
                        .build())
                .OrderLines(Arrays.asList(
                        AliexpressReturnInstockDTO.OrderLines.builder()
                                .itemCode("12000043163694871")
                                .planQty(100)
                                .inventoryType("1")
                                .ownerCode("17379911544")
                                .build()
                ))
                .build();
        ApiReturnOrderResponseDTO apiOrderResponseDTO = aliexpressWarehouseService.createReturnInstockOrder(aliexpressInboundDTO);
        System.out.println(apiOrderResponseDTO);
    }
    @Test
    public void getInventory() throws ApiException{
        ApiInventoryResponseDTO apiOrderResponseDTO = aliexpressWarehouseService.getInventory(buildAuthDTO());
        System.out.println(apiOrderResponseDTO);
    }

    @Test
    public void cancelInbound() throws ApiException{
        AliexpressAuthDTO aliexpressAuthDTO = buildAuthDTO();
        AliexpressCancelOrderDTO aliexpressCancelOrderDTO = new AliexpressCancelOrderDTO();
        aliexpressCancelOrderDTO.setAliexpressAuthDTO(aliexpressAuthDTO);
        aliexpressCancelOrderDTO.setOrderId("LBX00954421068094802");
        aliexpressCancelOrderDTO.setOwnerCode("17379911544");
        aliexpressCancelOrderDTO.setWarehouseCode("STB");
        aliexpressCancelOrderDTO.setOrderType("SCRK");
        aliexpressCancelOrderDTO.setOrderCode("TEST-WJ062601");
        ApiOrderResponseDTO apiOrderResponseDTO = aliexpressWarehouseService.cancelOrder(aliexpressCancelOrderDTO);
        System.out.println(apiOrderResponseDTO);
    }

    private static AliexpressAuthDTO buildAuthDTO() {
        AliexpressAuthDTO aliexpressAuthDTO = new AliexpressAuthDTO();
        aliexpressAuthDTO.setUrl("https://api-sg.aliexpress.com");
        aliexpressAuthDTO.setAppKey("503630");
        aliexpressAuthDTO.setAppSecret("PxkJJ2fLGh5HcwzhUJp267lQSbkuAFRJ");
        aliexpressAuthDTO.setAccessToken("50000701530cnHtbirhzrd7ijPeou2emiSIajwwCg118389611msriNQyROLyoVZ1y1j");
        aliexpressAuthDTO.setOwnerCode("17379911544");
        return aliexpressAuthDTO;
    }

}