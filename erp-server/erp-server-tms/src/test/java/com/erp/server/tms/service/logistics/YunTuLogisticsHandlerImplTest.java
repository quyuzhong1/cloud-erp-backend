package com.erp.server.tms.service.logistics;

import com.common.business.enums.LogisticsPlatformEnum;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.tms.entity.LogisticsSaleChannelEntity;
import com.erp.model.tms.vo.request.*;
import com.erp.model.tms.vo.response.LogisticsOrderResponseVO;
import com.erp.model.tms.vo.response.LogisticsPrintLabelResponse;
import com.erp.server.tms.ErpServerTmsApplication;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {ErpServerTmsApplication.class})
public class YunTuLogisticsHandlerImplTest {

    @Resource
    private YunTuLogisticsHandlerImpl yunTuLogisticsHandler;

    private Map<String, String> authMap = new HashMap<>();

    public YunTuLogisticsHandlerImplTest(){
//        authMap.put("clientId","ITC0893791");
//        authMap.put("clientSecret","axzc2utvPbfc9UbJDOh+7w==");
        //正式环境
        authMap.put("clientId","CNH1896658");
        authMap.put("clientSecret","58d89eba9f63431f9883de6800bd98df");
        authMap.put("url","http://oms.api.yunexpress.com");
    }
    public Map<String, String> getLogisticsAuthConfig(){
        Map<String, String> logisticsAuthConfig = yunTuLogisticsHandler.getLogisticsAuthConfigByShopId("");
        return logisticsAuthConfig;
    }
    public List<Map<String, String>> getLogisticsAuthConfigByPlatform(){
        List<Map<String, String>> logisticsAuthConfigByPlatform = yunTuLogisticsHandler.getLogisticsAuthConfigByPlatform(LogisticsPlatformEnum.SHOPEE.getCode());
        return logisticsAuthConfigByPlatform;
    }
    @Test
    public void getChannel() {
        System.out.println(yunTuLogisticsHandler.getChannel(ChanelQueryVO.builder().authMap(authMap).build()));
    }


    @Test
    public void createOrder() {
        SenderInfo senderInfo = new SenderInfo();
        senderInfo.setAddressFirst("address");
        senderInfo.setContact("contact");
        senderInfo.setCityName("newyork");
        senderInfo.setCompanyName("componeny");
        senderInfo.setName("name");
        senderInfo.setProvinceName("shenzhen");
        senderInfo.setTelNumber("12345678");
        senderInfo.setEmail("321546");
        senderInfo.setCountry("CN");
        senderInfo.setZipCode("515800");
        LogisticsProductVO logisticsProductVO = new LogisticsProductVO();
        logisticsProductVO.setEnglishUsage("materi");
        logisticsProductVO.setDeclareChineseName("物流");
        logisticsProductVO.setDeclareEnglishName("mta");
        logisticsProductVO.setDestDeclarePrice(new BigDecimal("12"));
        logisticsProductVO.setWeight(1999);
        logisticsProductVO.setQuantity(1);
        LogisticsSaleChannelEntity logisticsSaleChannel = new LogisticsSaleChannelEntity();
        logisticsSaleChannel.setCode("THPHR");
        logisticsSaleChannel.setShipmentMethod("Express-Post");
        logisticsSaleChannel.setPlatformChannelId("155");
        final LogisticsOrderVO logisticsOrderVO = LogisticsOrderVO.builder()
//                .channelCode("THPHR")
                .authMap(authMap)
//                .channelId("155")
                .orderSource("ERP")
                .deliveryNo("WEIJI2023111001007")
                .receiverInfoVO(ReceiverInfoVO.builder()
                        .addressFirst("address")
                        .email("123@q.con")
                        .city("shenz")
                        .name("mark")
                        .companyName("componey")
                        .contact("mark")
                        .country("US")
                        .zipCode("11510")
                        .province("state")
                        .telNumber("123456789")
                        .build())
                .senderInfo(senderInfo)
                .parceInfoVO(ParceInfoVO.builder()
                        .currency("USD")
                        .height(1)
                        .hasBattery(true)
                        .totalPrice(new BigDecimal("123"))
                        .totalQuantity(1)
                        .totalWeight(1999)
                        .length(1)
                        .totalWeight(123)
                        .width(123)
                        .build())
                .logisticsProductVOList(Arrays.asList(
                        logisticsProductVO
                ))
                .build();
        ApiResult<LogisticsOrderResponseVO> responseVOApiResult =  yunTuLogisticsHandler.createOrder(logisticsOrderVO);
        System.out.println(responseVOApiResult);
    }


    @Test
    public void getLabelList() throws IOException {
        LogisticsGetLabelVO labelVO = new LogisticsGetLabelVO();
        LogisticsGetLabelVO labelVO2 = new LogisticsGetLabelVO();
        labelVO.setDeliveryNo("WEIJI2023110901004");
        labelVO2.setDeliveryNo("WEIJI2023111001007");
        labelVO2.setAuthMap(authMap);
        labelVO.setAuthMap(authMap);
        ApiResult<List<LogisticsPrintLabelResponse>> result = yunTuLogisticsHandler.getLabelList(Arrays.asList(labelVO,labelVO2));
        System.out.println(result);
    }

    @Test
    public void queryOrderListTest() throws IOException {
        LogisticsQueryBaseVO labelVO = new LogisticsQueryBaseVO();
        LogisticsQueryBaseVO labelVO2 = new LogisticsQueryBaseVO();
        labelVO.setDeliveryNo("XSDD24052302333");
        labelVO.setTransportNo("YT2414421901000199");
        labelVO.setAuthMap(authMap);
        labelVO2.setAuthMap(authMap);
        labelVO2.setDeliveryNo("XSDD24052300936");
        labelVO2.setTransportNo("YT2414421901000237");
    }

    @Test
    public void interceptOrder() throws IOException {
        LogisticsInterceptOrderVO labelVO = new LogisticsInterceptOrderVO();
        LogisticsInterceptOrderVO labelVO2 = new LogisticsInterceptOrderVO();
        labelVO.setDeliveryNo("WEIJI2023110901004");
        labelVO.setAuthMap(authMap);
        labelVO2.setDeliveryNo("WEIJI2023110901003");
        labelVO2.setAuthMap(authMap);
    }

    @Test
    public void cancelOrder() throws IOException {
        LogisticsCancelOrderVO labelVO = new LogisticsCancelOrderVO();
        LogisticsCancelOrderVO labelVO2 = new LogisticsCancelOrderVO();
        labelVO.setDeliveryNo("WEIJI2023110901004");
        labelVO.setAuthMap(authMap);
        labelVO2.setDeliveryNo("WEIJI2023110901003");
        labelVO2.setAuthMap(authMap);
    }

    @Test
    public void authorization() {
    }
}