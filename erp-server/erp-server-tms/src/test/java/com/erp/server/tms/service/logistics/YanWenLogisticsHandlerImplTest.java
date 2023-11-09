package com.erp.server.tms.service.logistics;

import com.common.core.controller.vo.ApiResult;
import com.erp.model.tms.entity.LogisticsAuthEntity;
import com.erp.model.tms.vo.request.*;
import com.erp.model.tms.vo.response.LogisticsOrderResponseVO;
import com.erp.server.tms.ErpServerTmsApplication;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {ErpServerTmsApplication.class})
public class YanWenLogisticsHandlerImplTest {

    @Resource
    private YanWenLogisticsHandlerImpl yanWenLogisticsHandler;



    @Test
    public void getChannel() {
        System.out.println(yanWenLogisticsHandler.getChannel(new ChanelQueryVO()));
    }

    @Test
    public void testCreateOrder() {
        SenderInfo senderInfo = new SenderInfo();
        senderInfo.setAddressFirst("address");
        senderInfo.setContact("contact");
//        senderInfo.setCity("newyork");
//        senderInfo.setCityId("1");
        senderInfo.setCompanyName("componeny");
        senderInfo.setName("name");
//        senderInfo.setProvince("shenzhen");
        senderInfo.setTelNumber("12345678");
        senderInfo.setEmail("321546");
        senderInfo.setCountry("China");
        senderInfo.setZipCode("515800");
        LogisticsProductVO logisticsProductVO = new LogisticsProductVO();
        logisticsProductVO.setEnglishUsage("materi");
        logisticsProductVO.setDeclareChineseName("物流");
        logisticsProductVO.setDeclareEnglishName("mta");
        logisticsProductVO.setPrice(new BigDecimal("12345"));
        logisticsProductVO.setWeight(123456);
        logisticsProductVO.setQuantity(1324);
        final LogisticsOrderVO logisticsOrderVO = LogisticsOrderVO.builder()
                .channelId("155")
                .orderSource("ERP")
                .deliveryNo("wj12345167")
                .receiverInfoVO(ReceiverInfoVO.builder()
                        .addressFirst("address")
                        .email("123@q.con")
                        .city("shenz")
                        .name("mark")
                        .companyName("componey")
                        .contact("mark")
                        .country("US")
                        .zipCode("12201")
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
                        .length(1)
                        .totalWeight(123)
                        .width(123)
                        .ioss("123456")
                        .build())
                .logisticsProductVOList(Arrays.asList(
                                logisticsProductVO
                        ))
                .build();
        ApiResult<LogisticsOrderResponseVO> result = yanWenLogisticsHandler.createOrder(logisticsOrderVO);
        System.out.println(result);
    }

    @Test
    public void getLabelUrl() {
//        LogisticsGetLabelVO logisticsQueryVO = new LogisticsGetLabelVO();
//        logisticsQueryVO.setTransportNo(Collections.singletonList("LR08531450CN"));
//        System.out.println(yanWenLogisticsHandler.getLabelUrl(logisticsQueryVO));
    }

    @Test
    public void cancelOrder() {
        LogisticsCancelOrderVO cancelOrderVO = new LogisticsCancelOrderVO();
        cancelOrderVO.setTransportNo(Collections.singletonList("LR085325186CN"));
        System.out.println(yanWenLogisticsHandler.cancelOrder(cancelOrderVO));
    }

    @Test
    public void queryOrder() {
        LogisticsQueryBaseVO logisticsQueryBaseVO = new LogisticsQueryBaseVO();
        logisticsQueryBaseVO.setDeliveryNo(Collections.singletonList("weiji1233211"));
        System.out.println(yanWenLogisticsHandler.queryOrderList(logisticsQueryBaseVO));
    }
}