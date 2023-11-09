package com.erp.server.tms.service.logistics;

import com.common.core.controller.vo.ApiResult;
import com.common.core.utils.FileUtil;
import com.erp.model.tms.entity.LogisticsAuthEntity;
import com.erp.model.tms.vo.request.*;
import com.erp.model.tms.vo.response.LogisticsOrderResponseVO;
import com.erp.server.tms.ErpServerTmsApplication;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {ErpServerTmsApplication.class})
public class WeiShiLogisticsHandlerImplTest {

    @Resource
    WeiShiLogisticsHandlerImpl weiShiLogisticsHandler;

    @Test
    public void getChannel() {
        System.out.println(weiShiLogisticsHandler.getChannel(new ChanelQueryVO()));
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
        logisticsProductVO.setPrice(new BigDecimal("12"));
        logisticsProductVO.setWeight(1999);
        logisticsProductVO.setQuantity(10);
        final LogisticsOrderVO logisticsOrderVO = LogisticsOrderVO.builder()
                .channelCode("MX1001")
                .channelId("155")
                .orderSource("ERP")
                .deliveryNo("wj12345167710")
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
        logisticsQueryVO.setTransportNo(Collections.singletonList("WSHMX3133453788YQ"));
        String base64 = weiShiLogisticsHandler.getLabelUrl(logisticsQueryVO).getData();
        System.out.println(base64);
    }

    @Test
    public void queryOrderList() {
        LogisticsQueryBaseVO logisticsQueryVOList = new LogisticsQueryBaseVO();
        logisticsQueryVOList.setDeliveryNo(Arrays.asList("wj12345167710"));
        System.out.println(weiShiLogisticsHandler.queryOrderList(logisticsQueryVOList));
    }

    @Test
    public void interceptOrder() {
        LogisticsInterceptOrderVO logisticsQueryVOList = new LogisticsInterceptOrderVO();
        logisticsQueryVOList.setDeliveryNo(Arrays.asList("wj12345167710"));
        System.out.println(weiShiLogisticsHandler.interceptOrder(logisticsQueryVOList));
    }

    @Test
    public void cancelOrder() {
        LogisticsCancelOrderVO logisticsQueryVOList = new LogisticsCancelOrderVO();
        logisticsQueryVOList.setDeliveryNo(Arrays.asList("wj12345167710"));
        System.out.println(weiShiLogisticsHandler.cancelOrder(logisticsQueryVOList));
    }
}