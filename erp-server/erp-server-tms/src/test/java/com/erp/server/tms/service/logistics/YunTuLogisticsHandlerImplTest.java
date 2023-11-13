package com.erp.server.tms.service.logistics;

import com.common.core.controller.vo.ApiResult;
import com.erp.model.tms.entity.LogisticsAuthEntity;
import com.erp.model.tms.vo.request.*;
import com.erp.model.tms.vo.response.CancelResponseVO;
import com.erp.model.tms.vo.response.InterceptResponseVO;
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
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {ErpServerTmsApplication.class})
public class YunTuLogisticsHandlerImplTest {

    @Resource
    private YunTuLogisticsHandlerImpl yunTuLogisticsHandler;

    private LogisticsAuthEntity logisticsAuthEntity = new LogisticsAuthEntity();

    public YunTuLogisticsHandlerImplTest(){
        logisticsAuthEntity.setAccount("ITC0893791");
        logisticsAuthEntity.setPassword("axzc2utvPbfc9UbJDOh+7w==");
    }

    @Test
    public void getChannel() {
        System.out.println(yunTuLogisticsHandler.getChannel(ChanelQueryVO.builder().logisticsAuthEntity(logisticsAuthEntity).build()));
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
        logisticsProductVO.setQuantity(1);
        final LogisticsOrderVO logisticsOrderVO = LogisticsOrderVO.builder()
                .channelCode("THPHR")
                .logisticsAuthEntity(logisticsAuthEntity)
                .channelId("155")
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
        labelVO2.setLogisticsAuthEntity(logisticsAuthEntity);
        labelVO.setLogisticsAuthEntity(logisticsAuthEntity);
        ApiResult<List<LogisticsPrintLabelResponse>> result = yunTuLogisticsHandler.getLabelList(Arrays.asList(labelVO,labelVO2));
        System.out.println(result);
    }

    @Test
    public void queryOrderListTest() throws IOException {
        LogisticsQueryBaseVO labelVO = new LogisticsQueryBaseVO();
        LogisticsQueryBaseVO labelVO2 = new LogisticsQueryBaseVO();
        labelVO.setDeliveryNo("WEIJI2023110901004");
        labelVO.setLogisticsAuthEntity(logisticsAuthEntity);
        labelVO2.setLogisticsAuthEntity(logisticsAuthEntity);
        labelVO2.setDeliveryNo("WEIJI2023111001007");
        ApiResult<List<LogisticsOrderResponseVO>> apiResult = yunTuLogisticsHandler.queryOrderList(Arrays.asList(labelVO,labelVO2));
        System.out.println(apiResult);
    }

    @Test
    public void interceptOrder() throws IOException {
        LogisticsInterceptOrderVO labelVO = new LogisticsInterceptOrderVO();
        LogisticsInterceptOrderVO labelVO2 = new LogisticsInterceptOrderVO();
        labelVO.setDeliveryNo("WEIJI2023110901004");
        labelVO.setLogisticsAuthEntity(logisticsAuthEntity);
        labelVO2.setDeliveryNo("WEIJI2023110901003");
        labelVO2.setLogisticsAuthEntity(logisticsAuthEntity);
        ApiResult<List<InterceptResponseVO>> apiResult = yunTuLogisticsHandler.interceptOrder(Arrays.asList(labelVO,labelVO2));
        System.out.println(apiResult);
    }

    @Test
    public void cancelOrder() throws IOException {
        LogisticsCancelOrderVO labelVO = new LogisticsCancelOrderVO();
        LogisticsCancelOrderVO labelVO2 = new LogisticsCancelOrderVO();
        labelVO.setDeliveryNo("WEIJI2023110901004");
        labelVO.setLogisticsAuthEntity(logisticsAuthEntity);
        labelVO2.setDeliveryNo("WEIJI2023110901003");
        labelVO2.setLogisticsAuthEntity(logisticsAuthEntity);
        ApiResult<List<CancelResponseVO>> apiResult = yunTuLogisticsHandler.cancelOrder(Arrays.asList(labelVO,labelVO2));
        System.out.println(apiResult);
    }
}