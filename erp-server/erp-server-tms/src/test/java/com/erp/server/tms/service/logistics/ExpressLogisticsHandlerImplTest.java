package com.erp.server.tms.service.logistics;

import com.common.business.enums.LogisticsPlatformEnum;
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
 * @date 2023年11月16日
 * @version: 1.0
 */
@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {ErpServerTmsApplication.class})
public class ExpressLogisticsHandlerImplTest {
    @Resource
    private ExpressLogisticsHandlerImpl expressLogisticsHandler;
    @Resource
    private LogisticsSaleChannelService logisticsSaleChannelService;

    private Map<String, String> authMap = new HashMap<>();

    public ExpressLogisticsHandlerImplTest() {
        String CLIENT_CODE = "WJKJVX16Y0N";  //此处替换为您在丰桥平台获取的顾客编码
        String CHECK_WORD = "BP6oSEoP3dnELDGMtnbYh7Ig5UKlWIBS";
        String url = "https://sfapi-sbox.sf-express.com/std/service";
        authMap.put("clientId", CLIENT_CODE);
        authMap.put("clientSecret", CHECK_WORD);
        authMap.put("url", url);
    }

    public Map<String, String> getLogisticsAuthConfig() {
        Map<String, String> logisticsAuthConfig = expressLogisticsHandler.getLogisticsAuthConfigByShopId("");
        return logisticsAuthConfig;
    }

    public List<Map<String, String>> getLogisticsAuthConfigByPlatform() {
        List<Map<String, String>> logisticsAuthConfigByPlatform = expressLogisticsHandler.getLogisticsAuthConfigByPlatform(LogisticsPlatformEnum.SHOPEE.getCode());
        return logisticsAuthConfigByPlatform;
    }

    @Test
    public void getChannel() {
        ApiResult<List<LogisticsSaleChannelEntity>> channel = expressLogisticsHandler.getChannel(ChanelQueryVO.builder().authMap(authMap).build());
        System.out.println(channel);
    }

    @Test
    public void createChannelData() {
        List<LogisticsSaleChannelEntity> entityList = new ArrayList<>();
        entityList.add(new LogisticsSaleChannelEntity().setCode("1").setPlatformChannelId("1").setAging("T4").setCnName("顺丰特快").setLogisticsPlatform(LogisticsPlatformEnum.SF_EXPRESS.getCode()));
        entityList.add(new LogisticsSaleChannelEntity().setCode("2").setPlatformChannelId("2").setAging("T6").setCnName("顺丰标快").setLogisticsPlatform(LogisticsPlatformEnum.SF_EXPRESS.getCode()));
        entityList.add(new LogisticsSaleChannelEntity().setCode("6").setPlatformChannelId("6").setAging("T104").setCnName("顺丰即日").setLogisticsPlatform(LogisticsPlatformEnum.SF_EXPRESS.getCode()));
        entityList.add(new LogisticsSaleChannelEntity().setCode("10").setPlatformChannelId("10").setAging("T14").setCnName("国际小包").setLogisticsPlatform(LogisticsPlatformEnum.SF_EXPRESS.getCode()));
        entityList.add(new LogisticsSaleChannelEntity().setCode("23").setPlatformChannelId("23").setAging("T9").setCnName("顺丰国际特惠(文件)").setLogisticsPlatform(LogisticsPlatformEnum.SF_EXPRESS.getCode()));
        entityList.add(new LogisticsSaleChannelEntity().setCode("24").setPlatformChannelId("24").setAging("T9").setCnName("顺丰国际特惠(包裹)").setLogisticsPlatform(LogisticsPlatformEnum.SF_EXPRESS.getCode()));
        entityList.add(new LogisticsSaleChannelEntity().setCode("26").setPlatformChannelId("26").setAging("T7").setCnName("国际大件").setLogisticsPlatform(LogisticsPlatformEnum.SF_EXPRESS.getCode()));
        entityList.add(new LogisticsSaleChannelEntity().setCode("60").setPlatformChannelId("60").setAging("T4").setCnName("顺丰特快（文件）").setLogisticsPlatform(LogisticsPlatformEnum.SF_EXPRESS.getCode()));
        entityList.add(new LogisticsSaleChannelEntity().setCode("99").setPlatformChannelId("99").setAging("T4").setCnName("顺丰国际标快(文件)").setLogisticsPlatform(LogisticsPlatformEnum.SF_EXPRESS.getCode()));
        entityList.add(new LogisticsSaleChannelEntity().setCode("100").setPlatformChannelId("100").setAging("T4").setCnName("顺丰国际标快(包裹)").setLogisticsPlatform(LogisticsPlatformEnum.SF_EXPRESS.getCode()));
        entityList.add(new LogisticsSaleChannelEntity().setCode("242").setPlatformChannelId("242").setAging("T77").setCnName("丰网速运").setLogisticsPlatform(LogisticsPlatformEnum.SF_EXPRESS.getCode()));
        entityList.add(new LogisticsSaleChannelEntity().setCode("247").setPlatformChannelId("247").setAging("T68").setCnName("电商标快").setLogisticsPlatform(LogisticsPlatformEnum.SF_EXPRESS.getCode()));
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
        logisticsProductVO.setDestDeclarePrice(new BigDecimal("12"));
        logisticsProductVO.setWeight(1999);
        logisticsProductVO.setQuantity(10);
        logisticsProductVO.setSourceCountry("CN");
        LogisticsChannelEntity logisticsChannel = new LogisticsChannelEntity();
        logisticsChannel.setCode("1");
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
                .logisticsSaleChannel(logisticsSaleChannel)
                .build();
        ApiResult<LogisticsOrderResponseVO> order = expressLogisticsHandler.createOrder(logisticsOrderVO);
        System.out.println("order:"+ order);
    }

    @Test
    public void queryOrderList() {
        LogisticsQueryBaseVO logisticsQueryVOList = new LogisticsQueryBaseVO();
        logisticsQueryVOList.setDeliveryNo("wj12345167723");
        logisticsQueryVOList.setAuthMap(authMap);
        ApiResult<List<LogisticsOrderResponseVO>> listApiResult = expressLogisticsHandler.queryOrderList(Collections.singletonList(logisticsQueryVOList));
        System.out.println(listApiResult);
    }

    @Test
    public void getLabelList() throws IOException {
        LogisticsGetLabelVO logisticsQueryVO2 = new LogisticsGetLabelVO();
        logisticsQueryVO2.setDeliveryNo("XSDS23122200010");
        logisticsQueryVO2.setTransportNo("SF7444475423374");
        logisticsQueryVO2.setAuthMap(authMap);
        ApiResult<List<LogisticsPrintLabelResponse>> labelList = expressLogisticsHandler.getLabelList(Collections.singletonList(logisticsQueryVO2));
        System.out.println(labelList);
    }

    @Test
    public void interceptOrder() {
        LogisticsInterceptOrderVO logisticsQueryVOList2 = new LogisticsInterceptOrderVO();
        logisticsQueryVOList2.setDeliveryNo("1746722100644679681");
        logisticsQueryVOList2.setAuthMap(authMap);
        ApiResult<List<InterceptResponseVO>> listApiResult = expressLogisticsHandler.interceptOrder(Collections.singletonList(logisticsQueryVOList2));
        System.out.println(listApiResult);
    }

    @Test
    public void cancelOrder() {
        LogisticsCancelOrderVO logisticsQueryVOList = new LogisticsCancelOrderVO();
        logisticsQueryVOList.setDeliveryNo("wj12345167723");
        logisticsQueryVOList.setTransportNo("SF7444476771065");
//        logisticsQueryVOList.setTrackNo("LM000002721CA");
        logisticsQueryVOList.setAuthMap(authMap);
        ApiResult<List<CancelResponseVO>> listApiResult = expressLogisticsHandler.cancelOrder(Collections.singletonList(logisticsQueryVOList));
        System.out.println(listApiResult);
    }

    @Test
    public void confirmOrder() {
        LogisticsQueryBaseVO logisticsQueryVO = new LogisticsCancelOrderVO();
        logisticsQueryVO.setDeliveryNo("wj12345167721");
        logisticsQueryVO.setTransportNo("lBK4IuWt-IlRQrfmJhnniA");
        logisticsQueryVO.setTrackNo("LM000002721CA");
        logisticsQueryVO.setAuthMap(authMap);
        ApiResult<List<ConfirmResponseVO>> listApiResult = expressLogisticsHandler.confirmOrder(Collections.singletonList(logisticsQueryVO));
        System.out.println(listApiResult);
    }

    @Test
    public void authorization() {
        ApiResult<Object>ApiResult<Object>= expressLogisticsHandler.authorization(authMap);
        System.out.println(apiResult);
    }
}
