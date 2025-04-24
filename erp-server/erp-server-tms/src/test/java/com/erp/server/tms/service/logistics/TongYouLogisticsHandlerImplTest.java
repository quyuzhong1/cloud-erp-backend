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
public class TongYouLogisticsHandlerImplTest {

    @Resource
    private TongYouLogisticsHandlerImpl tongYouLogisticsHandler;

    private Map<String, String> authMap = new HashMap<>();

    public TongYouLogisticsHandlerImplTest(){
        //注意！！通邮没有测试环境，用正式环境测试创建订单记得在客户端将订单删除！！！
        authMap.put("clientSecret","DADDC4078D2B7D38391A8D3F78C037BF");
    }

    public Map<String, String> getLogisticsAuthConfig(){
        Map<String, String> logisticsAuthConfig = tongYouLogisticsHandler.getLogisticsAuthConfigByShopId("");
        return logisticsAuthConfig;
    }
    public List<Map<String, String>> getLogisticsAuthConfigByPlatform(){
        List<Map<String, String>> logisticsAuthConfigByPlatform = tongYouLogisticsHandler.getLogisticsAuthConfigByPlatform(LogisticsPlatformEnum.SHOPEE.getCode());
        return logisticsAuthConfigByPlatform;
    }

    @Test
    public void getChannel() {
        System.out.println(tongYouLogisticsHandler.getChannel(ChanelQueryVO.builder().authMap(authMap).build()));
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
        logisticsProductVO.setDeclareCurrency("USD");

        LogisticsSaleChannelEntity logisticsSaleChannel = new LogisticsSaleChannelEntity();
        logisticsSaleChannel.setCode("FZXXRKVP705");
        logisticsSaleChannel.setShipmentMethod("Express-Post");
        logisticsSaleChannel.setPlatformChannelId("155");

        final LogisticsOrderVO logisticsOrderVO = LogisticsOrderVO.builder()
                .authMap(authMap)
//                .channelCode("FZXXRKVP705")
//                .channelId("155")
                .orderSource("ERP")
                .material("material")
                .deliveryNo("WJ20231102002")
                .receiverInfoVO(ReceiverInfoVO.builder()
                        .addressFirst("address")
                        .email("123@q.con")
                        .city("BALDWIN")
                        .name("mark")
                        .companyName("componey")
                        .contact("mark")
                        .country("US")
                        .zipCode("11510")
                        .province("NY")
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
        ApiResult<LogisticsOrderResponseVO> responseVOApiResult = tongYouLogisticsHandler.createOrder(logisticsOrderVO);
        System.out.println(responseVOApiResult);
    }

    @Test
    public void getLabelList() throws IOException {
        LogisticsGetLabelVO labelVO = new LogisticsGetLabelVO();
        LogisticsGetLabelVO labelVO2 = new LogisticsGetLabelVO();
        labelVO2.setDeliveryNo("WJ20231102001");
        labelVO2.setTrackNo("AT139756425CN");
        labelVO2.setAuthMap(authMap);
        labelVO.setDeliveryNo("XM1AWJJ028110");
        labelVO.setAuthMap(authMap);
        labelVO.setTrackNo("TYZPH0022783888YQ");
        LogisticsSaleChannelEntity logisticsChannelEntity = new LogisticsSaleChannelEntity();
        logisticsChannelEntity.setCode("FZXXRKVP705");
        labelVO.setLogisticsSaleChannelEntity(logisticsChannelEntity);
        labelVO2.setLogisticsSaleChannelEntity(logisticsChannelEntity);
        ApiResult<List<LogisticsPrintLabelResponse>> result = tongYouLogisticsHandler.getLabelList(Arrays.asList(labelVO,labelVO2));
        System.out.println(result);
    }

    @Test
    public void queryOrderListTest() throws IOException {
        LogisticsQueryBaseVO labelVO = new LogisticsQueryBaseVO();
        LogisticsQueryBaseVO labelVO2 = new LogisticsQueryBaseVO();
        labelVO.setDeliveryNo("WJ20231102001");
        labelVO.setAuthMap(authMap);
        labelVO2.setDeliveryNo("WJ20231102002");
        labelVO2.setAuthMap(authMap);

    }
    @Test
    public void authorization() {
    }
}