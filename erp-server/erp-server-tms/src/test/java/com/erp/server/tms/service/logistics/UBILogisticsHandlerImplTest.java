package com.erp.server.tms.service.logistics;

import com.common.business.enums.LogisticsPlatformEnum;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.tms.entity.LogisticsChannelEntity;
import com.erp.model.tms.entity.LogisticsSaleChannelEntity;
import com.erp.model.tms.vo.request.*;
import com.erp.model.tms.vo.response.*;
import com.erp.server.tms.ErpServerTmsApplication;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

/**
 * @author zdy
 * @ClassName UBILogisticsHandlerImplTest
 * @date 2023年11月16日
 * @version: 1.0
 */
@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {ErpServerTmsApplication.class})
public class UBILogisticsHandlerImplTest {
    @Resource
    private UbiLogisticsHandlerImpl ubiLogisticsHandler;

    private Map<String, String> authMap = new HashMap<>();

    public UBILogisticsHandlerImplTest(){
//        //测试环境账号
        authMap.put("clientId","test5AdbzO5OEeOpvgAVXUFE0A");
        authMap.put("clientSecret","79db9e5OEeOpvgAVXUFWSD");
        //正式环境账号
//        authMap.put("clientId","pcloTVPCXZCD5G-RRlhBfR");
//        authMap.put("clientSecret","N1S3O3OlKKRDRfcfYFONqg");
    }
    public Map<String, String> getLogisticsAuthConfig(){
        Map<String, String> logisticsAuthConfig = ubiLogisticsHandler.getLogisticsAuthConfigByShopId("");
        return logisticsAuthConfig;
    }
    public List<Map<String, String>> getLogisticsAuthConfigByPlatform(){
        List<Map<String, String>> logisticsAuthConfigByPlatform = ubiLogisticsHandler.getLogisticsAuthConfigByPlatform(LogisticsPlatformEnum.SHOPEE.getCode());
        return logisticsAuthConfigByPlatform;
    }
    @Test
    public void getChannel() {
        ApiResult<List<LogisticsSaleChannelEntity>> channel = ubiLogisticsHandler.getChannel(ChanelQueryVO.builder().authMap(authMap).build());
        System.out.println(channel);
    }

    @Test
    public void createOrder(){
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
        logisticsProductVO.setId("12121232");
        logisticsProductVO.setSkuId("123456");
        logisticsProductVO.setEnglishUsage("materi");
        logisticsProductVO.setDeclareChineseName("物流");
        logisticsProductVO.setDeclareEnglishName("mta");
        logisticsProductVO.setDestDeclarePrice(new BigDecimal("12"));
        logisticsProductVO.setWeight(1999);
        logisticsProductVO.setQuantity(10);
        logisticsProductVO.setSourceCountry("CN");
        LogisticsChannelEntity logisticsChannel = new LogisticsChannelEntity();
        logisticsChannel.setCode("UBI.CA2US.CAPOST");
        //DDU/DDP
        logisticsChannel.setTaxModel("DDU");

        LogisticsSaleChannelEntity logisticsSaleChannel = new LogisticsSaleChannelEntity();
        logisticsSaleChannel.setCode("UBI.CA2US.CAPOST");
        logisticsSaleChannel.setShipmentMethod("Express-Post");
        logisticsSaleChannel.setPlatformChannelId("1725040739275055105");

        final LogisticsOrderVO logisticsOrderVO = LogisticsOrderVO.builder()
                .authMap(authMap)
//                .channelCode("")
//                .channelId("1725040739275055105")
                .orderSource("ERP")
//                .facility("can")
                .deliveryNo("wj12345167728")
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
                        .totalPrice(new BigDecimal("120"))
                        .totalQuantity(10)
                        .totalWeight(1999)
                        .length(1)
                        .totalWeight(123)
                        .width(123)
                        .build())
                .logisticsProductVOList(Arrays.asList(
                        logisticsProductVO
                ))
                .logisticsSaleChannel(logisticsSaleChannel)
                .logisticsChannelEntity(logisticsChannel)
                .build();
        ApiResult<LogisticsOrderResponseVO> order = ubiLogisticsHandler.createOrder(logisticsOrderVO);
        System.out.println(order);
    }

    @Test
    public void queryOrderList(){
        LogisticsQueryBaseVO logisticsQueryVOList = new LogisticsQueryBaseVO();
        logisticsQueryVOList.setDeliveryNo("wj12345167721");
        logisticsQueryVOList.setAuthMap(authMap);
        ApiResult<List<LogisticsOrderResponseVO>> listApiResult = ubiLogisticsHandler.queryOrderList(Collections.singletonList(logisticsQueryVOList));
        System.out.println(listApiResult);
    }

    @Test
    public void getLabelList() throws IOException {
        List<LogisticsGetLabelVO> logisticsQueryVO = new ArrayList<>();
        LogisticsGetLabelVO logisticsQueryVO2 = new LogisticsGetLabelVO();
        logisticsQueryVO2.setDeliveryNo("wj12345167726");
        logisticsQueryVO2.setAuthMap(authMap);
        logisticsQueryVO.add(logisticsQueryVO2);
        LogisticsGetLabelVO logisticsQueryVO3 = new LogisticsGetLabelVO();
        logisticsQueryVO3.setDeliveryNo("wj12345167728");
        logisticsQueryVO3.setAuthMap(authMap);
        logisticsQueryVO.add(logisticsQueryVO3);
//        ApiResult<List<LogisticsPrintLabelResponse>> labelList = ubiLogisticsHandler.getLabelList(Collections.singletonList(logisticsQueryVO2));
        ApiResult<List<LogisticsPrintLabelResponse>> labelList = ubiLogisticsHandler.getLabelList(logisticsQueryVO);
        System.out.println(labelList);
    }

    @Test
    public void interceptOrder(){
        LogisticsInterceptOrderVO logisticsQueryVOList2 = new LogisticsInterceptOrderVO();
        logisticsQueryVOList2.setDeliveryNo("wj12345167721");
        logisticsQueryVOList2.setAuthMap(authMap);
        ApiResult<List<InterceptResponseVO>> listApiResult = ubiLogisticsHandler.interceptOrder(Collections.singletonList(logisticsQueryVOList2));
        System.out.println(listApiResult);
    }

    @Test
    public void cancelOrder(){
        LogisticsCancelOrderVO logisticsQueryVOList = new LogisticsCancelOrderVO();
        logisticsQueryVOList.setDeliveryNo("wj12345167721");
        logisticsQueryVOList.setTransportNo("lBK4IuWt-IlRQrfmJhnniA");
        logisticsQueryVOList.setTrackNo("LM000002721CA");
        logisticsQueryVOList.setAuthMap(authMap);
        ApiResult<List<CancelResponseVO>> listApiResult = ubiLogisticsHandler.cancelOrder(Collections.singletonList(logisticsQueryVOList));
        System.out.println(listApiResult);
    }

    @Test
    public void confirmOrder(){
        LogisticsQueryBaseVO logisticsQueryVO = new LogisticsCancelOrderVO();
        logisticsQueryVO.setDeliveryNo("wj12345167721");
        logisticsQueryVO.setTransportNo("lBK4IuWt-IlRQrfmJhnniA");
        logisticsQueryVO.setTrackNo("LM000002721CA");
        logisticsQueryVO.setAuthMap(authMap);
        ApiResult<List<ConfirmResponseVO>> listApiResult = ubiLogisticsHandler.confirmOrder(Collections.singletonList(logisticsQueryVO));
        System.out.println(listApiResult);
    }

    @Test
    public void authorization() {
        ApiResult<Object>ApiResult<Object>= ubiLogisticsHandler.authorization(authMap);
        System.out.println(apiResult);
    }
}
