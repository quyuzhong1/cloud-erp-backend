package com.erp.server.tms.service.logistics;

import com.common.core.controller.vo.ApiResult;
import com.erp.model.tms.entity.LogisticsChannelEntity;
import com.erp.model.tms.entity.LogisticsSaleChannelEntity;
import com.erp.model.tms.vo.request.*;
import com.erp.model.tms.vo.response.*;
import com.erp.server.tms.ErpServerTmsApplication;
import com.erp.server.tms.service.LogisticsSaleChannelService;
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
 * @description: TODO
 * @date 2023年11月16日
 * @version: 1.0
 */
@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {ErpServerTmsApplication.class})
public class AmazonLogisticsHandlerImplTest {
    @Resource
    private AmazonLogisticsHandlerImpl amazonLogisticsHandler;
    @Resource
    private LogisticsSaleChannelService logisticsSaleChannelService;

    private Map<String, String> authMap = new HashMap<>();







    @Test
    public void getChannel() {
        ApiResult<List<LogisticsSaleChannelEntity>> channel = amazonLogisticsHandler.getChannel(ChanelQueryVO.builder().authMap(authMap).build());
        System.out.println(channel);
    }

    @Test
    public void createChannelData() {
        ApiResult<List<LogisticsSaleChannelEntity>> channel = amazonLogisticsHandler.getChannel(ChanelQueryVO.builder().authMap(authMap).build());
        List<LogisticsSaleChannelEntity> entityList = channel.getData();
        entityList.forEach(logisticsSaleChannelEntity -> {
            logisticsSaleChannelService.saveOrUpdateSaleChannel(logisticsSaleChannelEntity);
        });

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
        logisticsProductVO.setId("12121232");
        logisticsProductVO.setSkuId("123456");
        logisticsProductVO.setEnglishUsage("materi");
        logisticsProductVO.setDeclareChineseName("物流");
        logisticsProductVO.setDeclareEnglishName("mta");
        logisticsProductVO.setPrice(new BigDecimal("12"));
        logisticsProductVO.setWeight(1999);
        logisticsProductVO.setQuantity(10);
        logisticsProductVO.setSourceCountry("CN");
        LogisticsChannelEntity logisticsChannel = new LogisticsChannelEntity();
        logisticsChannel.setCode("UBI.CA2US.CAPOST");
        //DDU/DDP
        logisticsChannel.setTaxModel("DDU");

        LogisticsSaleChannelEntity logisticsSaleChannel = new LogisticsSaleChannelEntity();
        logisticsSaleChannel.setCode("1");
        logisticsSaleChannel.setShipmentMethod("Express-Post");
        logisticsSaleChannel.setPlatformChannelId("1");

        final LogisticsOrderVO logisticsOrderVO = LogisticsOrderVO.builder()
                .authMap(authMap)
//                .channelCode("")
//                .channelId("1725040739275055105")
                .orderSource("ERP")
//                .facility("can")
                .deliveryNo("wj12345167723")
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
                .logisticsChannelEntity(logisticsChannel)
                .build();
        ApiResult<LogisticsOrderResponseVO> order = amazonLogisticsHandler.createOrder(logisticsOrderVO);
        System.out.println(order);
    }

    @Test
    public void queryOrderList() {
        LogisticsQueryBaseVO logisticsQueryVOList = new LogisticsQueryBaseVO();
        logisticsQueryVOList.setDeliveryNo("wj12345167723");
        logisticsQueryVOList.setAuthMap(authMap);
        ApiResult<List<LogisticsOrderResponseVO>> listApiResult = amazonLogisticsHandler.queryOrderList(Collections.singletonList(logisticsQueryVOList));
        System.out.println(listApiResult);
    }

    @Test
    public void getLabelList() throws IOException {
        LogisticsGetLabelVO logisticsQueryVO2 = new LogisticsGetLabelVO();
        logisticsQueryVO2.setDeliveryNo("XSDS23122200010");
        logisticsQueryVO2.setTransportNo("SF7444475423374");
        logisticsQueryVO2.setAuthMap(authMap);
        ApiResult<List<LogisticsPrintLabelResponse>> labelList = amazonLogisticsHandler.getLabelList(Collections.singletonList(logisticsQueryVO2));
        System.out.println(labelList);
    }

    @Test
    public void interceptOrder() {
        LogisticsInterceptOrderVO logisticsQueryVOList2 = new LogisticsInterceptOrderVO();
        logisticsQueryVOList2.setDeliveryNo("wj12345167721");
        logisticsQueryVOList2.setAuthMap(authMap);
        ApiResult<List<InterceptResponseVO>> listApiResult = amazonLogisticsHandler.interceptOrder(Collections.singletonList(logisticsQueryVOList2));
        System.out.println(listApiResult);
    }

    @Test
    public void cancelOrder() {
        LogisticsCancelOrderVO logisticsQueryVOList = new LogisticsCancelOrderVO();
        logisticsQueryVOList.setDeliveryNo("wj12345167721");
        logisticsQueryVOList.setTransportNo("lBK4IuWt-IlRQrfmJhnniA");
        logisticsQueryVOList.setTrackNo("LM000002721CA");
        logisticsQueryVOList.setAuthMap(authMap);
        ApiResult<List<CancelResponseVO>> listApiResult = amazonLogisticsHandler.cancelOrder(Collections.singletonList(logisticsQueryVOList));
        System.out.println(listApiResult);
    }

    @Test
    public void confirmOrder() {
        LogisticsQueryBaseVO logisticsQueryVO = new LogisticsCancelOrderVO();
        logisticsQueryVO.setDeliveryNo("wj12345167721");
        logisticsQueryVO.setTransportNo("lBK4IuWt-IlRQrfmJhnniA");
        logisticsQueryVO.setTrackNo("LM000002721CA");
        logisticsQueryVO.setAuthMap(authMap);
        ApiResult<List<ConfirmResponseVO>> listApiResult = amazonLogisticsHandler.confirmOrder(Collections.singletonList(logisticsQueryVO));
        System.out.println(listApiResult);
    }

    @Test
    public void authorization() {
        ApiResult apiResult = amazonLogisticsHandler.authorization(authMap);
        System.out.println(apiResult);
    }
}
