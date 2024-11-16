package com.erp.server.tms.service.logistics;

import com.common.business.enums.LogisticsPlatformEnum;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.tms.entity.LogisticsAuthEntity;
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
public class WeiShiLogisticsHandlerImplTest {

    @Resource
    WeiShiLogisticsHandlerImpl weiShiLogisticsHandler;

    private Map<String, String> authMap = new HashMap<>();

    public WeiShiLogisticsHandlerImplTest(){
        //测试环境账号
//        authMap.put("url","http://218.17.123.141:18080/toms/service");
//        authMap.put("clientId","dcfe81e2059c1f0e6e6263dbcb764885");
//        authMap.put("clientSecret","dcfe81e2059c1f0e6e6263dbcb7648850d0c1386bae3caf82229e7cf472d7b53");
        //正式环境账号
        authMap.put("url","http://track.360lion.com/api/service");
        authMap.put("clientId","f8067aa0dc9ab7e927e03dfbce54ff4d");
        authMap.put("clientSecret","f8067aa0dc9ab7e927e03dfbce54ff4d7b6fc31d87f0c7e913c112f8b33423ce");
    }

    public Map<String, String> getLogisticsAuthConfig(){
        Map<String, String> logisticsAuthConfig = weiShiLogisticsHandler.getLogisticsAuthConfigByShopId("");
        return logisticsAuthConfig;
    }
    public List<Map<String, String>> getLogisticsAuthConfigByPlatform(){
        List<Map<String, String>> logisticsAuthConfigByPlatform = weiShiLogisticsHandler.getLogisticsAuthConfigByPlatform(LogisticsPlatformEnum.SHOPEE.getCode());
        return logisticsAuthConfigByPlatform;
    }
    @Test
    public void getChannel() {
        System.out.println(weiShiLogisticsHandler.getChannel(ChanelQueryVO.builder().authMap(authMap).build()));
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
        logisticsProductVO.setQuantity(10);
        LogisticsSaleChannelEntity logisticsSaleChannel = new LogisticsSaleChannelEntity();
//        logisticsSaleChannel.setCode("MX1001");
        logisticsSaleChannel.setCode("MX100100");
        logisticsSaleChannel.setShipmentMethod("Express-Post");
        logisticsSaleChannel.setPlatformChannelId("155");

        final LogisticsOrderVO logisticsOrderVO = LogisticsOrderVO.builder()
                .authMap(authMap)
//                .channelCode("MX1001")
//                .channelId("155")
                .orderSource("ERP")
                .deliveryNo("wj12345167721")
                .receiverInfoVO(ReceiverInfoVO.builder()
                        .addressFirst("address")
                        .email("123@q.con")
                        .city("shenz")
                        .name("mark")
                        .companyName("componey")
                        .contact("mark")
                        .country("MX")
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
                        .totalQuantity(12)
                        .totalWeight(1999)
                        .length(1)
                        .totalWeight(123)
                        .width(123)
                        .build())
                .logisticsSaleChannel(logisticsSaleChannel)
                .logisticsProductVOList(Arrays.asList(
                        logisticsProductVO
                ))
                .build();
        ApiResult<LogisticsOrderResponseVO> result = weiShiLogisticsHandler.createOrder(logisticsOrderVO);
        System.out.println(result);
    }

    @Test
    public void getLabelUrl() throws IOException {
        LogisticsGetLabelVO logisticsQueryVO = new LogisticsGetLabelVO();
        logisticsQueryVO.setDeliveryNo("XSDD24071804969");
        logisticsQueryVO.setAuthMap(authMap);
//        LogisticsGetLabelVO logisticsQueryVO2 = new LogisticsGetLabelVO();
//        logisticsQueryVO2.setDeliveryNo("wj12345167720");
//        logisticsQueryVO2.setAuthMap(authMap);
        ApiResult<List<LogisticsPrintLabelResponse>> result = weiShiLogisticsHandler.getLabelList(Arrays.asList(logisticsQueryVO));
        System.out.println(result);
    }

    @Test
    public void queryOrderList() {
        LogisticsQueryBaseVO logisticsQueryVOList = new LogisticsQueryBaseVO();
        logisticsQueryVOList.setDeliveryNo("wj12345167721");
        logisticsQueryVOList.setAuthMap(authMap);
        LogisticsQueryBaseVO logisticsQueryVOList2 = new LogisticsQueryBaseVO();
        logisticsQueryVOList2.setDeliveryNo("wj12345167720");
        logisticsQueryVOList2.setAuthMap(authMap);
        System.out.println(weiShiLogisticsHandler.queryOrderList(Arrays.asList(logisticsQueryVOList2,logisticsQueryVOList)));
    }

    @Test
    public void interceptOrder() {
        LogisticsInterceptOrderVO logisticsQueryVOList = new LogisticsInterceptOrderVO();
        logisticsQueryVOList.setDeliveryNo("wj12345167721");
        logisticsQueryVOList.setTransportNo("wj1234516772");
        logisticsQueryVOList.setTrackNo("wj123451677");
        logisticsQueryVOList.setAuthMap(authMap);
        LogisticsInterceptOrderVO logisticsQueryVOList2 = new LogisticsInterceptOrderVO();
        logisticsQueryVOList2.setDeliveryNo("wj12345167720");
        logisticsQueryVOList2.setAuthMap(authMap);
        System.out.println(weiShiLogisticsHandler.interceptOrder(Arrays.asList(logisticsQueryVOList,logisticsQueryVOList2)));
    }

    @Test
    public void cancelOrder() {
        LogisticsCancelOrderVO logisticsQueryVOList = new LogisticsCancelOrderVO();
        logisticsQueryVOList.setDeliveryNo("wj12345167721");
        logisticsQueryVOList.setTransportNo("wj1234516772");
        logisticsQueryVOList.setTrackNo("wj123451677");
        logisticsQueryVOList.setAuthMap(authMap);
        System.out.println(weiShiLogisticsHandler.cancelOrder(Arrays.asList(logisticsQueryVOList)));
    }

    @Test
    public void authorization() {
    }
}