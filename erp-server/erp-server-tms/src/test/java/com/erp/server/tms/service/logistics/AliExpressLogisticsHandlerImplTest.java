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
 * @description: TODO
 * @date 2023年11月16日
 * @version: 1.0
 */
@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {ErpServerTmsApplication.class})
public class AliExpressLogisticsHandlerImplTest {
    @Resource
    private AliExpressLogisticsHandlerImpl aliExpressLogisticsHandler;

    private Map<String, String> authMap = new HashMap<>();

    public AliExpressLogisticsHandlerImplTest(){
//        String CLIENT_CODE = "502978";  //此处替换为您在丰桥平台获取的顾客编码
//        String CHECK_WORD = "DfFGCAXMY7pptKfhz7IkWEa0zC0xddhY";//此处替换为您在丰桥平台获取的校验码
//        String token = "50000201815x0JpYsqi9bBs8MR11cd7a16dGmlyIWdSwlD3HOSDuQ1xrO34XX6CU58SN";
        String CLIENT_CODE = "503630";  //此处替换为您在丰桥平台获取的顾客编码
        String CHECK_WORD = "PxkJJ2fLGh5HcwzhUJp267lQSbkuAFRJ";//此处替换为您在丰桥平台获取的校验码
        String token = "50000201815x0JpYsqi9bBs8MR11cd7a16dGmlyIWdSwlD3HOSDuQ1xrO34XX6CU58SN";
        authMap.put("clientId",CLIENT_CODE);
        authMap.put("clientSecret",CHECK_WORD);
        authMap.put("token",token);
        authMap.put("url","https://api-sg.aliexpress.com");
    }

    public Map<String, String> getLogisticsAuthConfig(){
        Map<String, String> logisticsAuthConfig = aliExpressLogisticsHandler.getLogisticsAuthConfig("");
        return logisticsAuthConfig;
    }
    public List<Map<String, String>> getLogisticsAuthConfigByPlatform(){
        List<Map<String, String>> logisticsAuthConfigByPlatform = aliExpressLogisticsHandler.getLogisticsAuthConfigByPlatform(LogisticsPlatformEnum.SHOPEE.getCode());
        return logisticsAuthConfigByPlatform;
    }

    @Test
    public void getChannel() {
        ApiResult<List<LogisticsSaleChannelEntity>> channel = aliExpressLogisticsHandler.getChannel(ChanelQueryVO.builder().authMap(authMap).build());
        System.out.println(channel);
    }

    @Test
    public void createOrder(){
        SenderInfo senderInfo = new SenderInfo();
        senderInfo.setId("440174196461");
        senderInfo.setAddressFirst("5-8#lift 9th floor buliding 205 xinyang street xintaiyang Industrial Park lin village");
        senderInfo.setContact("contact");
        senderInfo.setCityName("Dongguan");
        senderInfo.setCompanyName("4PX");
        senderInfo.setName("chenxuli");
        senderInfo.setProvinceName("Guangdong Province");
        senderInfo.setTelNumber("17191087538");
        senderInfo.setEmail("dhphoto@aliyun.com");
        senderInfo.setCountry("China");
        senderInfo.setZipCode("523000");
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
        logisticsProductVO.setIsElectric(false);
        logisticsProductVO.setDeclarePrice(BigDecimal.valueOf(2));
        logisticsProductVO.setDestDeclarePrice(BigDecimal.valueOf(2));

        LogisticsSaleChannelEntity logisticsSaleChannel = new LogisticsSaleChannelEntity();
        logisticsSaleChannel.setCode("CAINIAO_ECONOMY_TS");
        logisticsSaleChannel.setShipmentMethod("Express-Post");
        logisticsSaleChannel.setPlatformChannelId("11169435");
        logisticsSaleChannel.setSupplierName("CAINIAONNRM");
        LogisticsChannelEntity logisticsChannel = new LogisticsChannelEntity();
//        logisticsChannel.setCode("S832");
        logisticsChannel.setId("1724614809662599171");
        //DDU/DDP
        logisticsChannel.setTaxModel("DDU");
        final LogisticsOrderVO logisticsOrderVO = LogisticsOrderVO.builder()
                .authMap(authMap)
                .orderSource("ERP")
                .pickupType("SELF_POST")
//                .facility("can")
                .deliveryNo("8180834369177849")
                .receiverInfoVO(ReceiverInfoVO.builder()
                        .addressFirst("address")
                        .email("965656546@qq.con")
                        .city("2900000")
                        .name("cl114341849dzvae")
                        .companyName("")
                        .contact("zhang san")
                        .country("CL")
                        .zipCode("13021")
                        .province("NY")
                        .telNumber("1234567890")
                        .build())
                .senderInfo(senderInfo)
                .parceInfoVO(ParceInfoVO.builder()
                        .currency("USD")
                        .height(1)
                        .hasBattery(true)
                        .totalPrice(new BigDecimal("20"))
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
                .logisticsSaleChannel(logisticsSaleChannel)
                .build();
        ApiResult<LogisticsOrderResponseVO> order = aliExpressLogisticsHandler.createOrder(logisticsOrderVO);
        System.out.println(order);
    }

    @Test
    public void queryOrderList(){
        LogisticsQueryBaseVO logisticsQueryVOList = new LogisticsQueryBaseVO();
        logisticsQueryVOList.setDeliveryNo("580555992124");
        logisticsQueryVOList.setAuthMap(authMap);
        ApiResult<List<LogisticsOrderResponseVO>> listApiResult = aliExpressLogisticsHandler.queryOrderList(Collections.singletonList(logisticsQueryVOList));
        System.out.println(listApiResult);
    }

    @Test
    public void getLabelList() throws IOException {
        LogisticsGetLabelVO logisticsQueryVO2 = new LogisticsGetLabelVO();
        logisticsQueryVO2.setTransportNo("580555992124");
        logisticsQueryVO2.setAuthMap(authMap);
        logisticsQueryVO2.setLabelType("1");
        ApiResult<List<LogisticsPrintLabelResponse>> labelList = aliExpressLogisticsHandler.getLabelList(Collections.singletonList(logisticsQueryVO2));
        System.out.println(labelList);
    }
    @Test
    public void authorization() {
        ApiResult apiResult = aliExpressLogisticsHandler.authorization(authMap);
        System.out.println(apiResult);
    }
}
