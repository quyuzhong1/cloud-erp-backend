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
    public void createOrderByJSON() {
        String json = "{\"oaid\":\"\",\"orderSource\":\"soB2c\",\"orderType\":\"selfAdd\",\"iossCode\":\"\",\"voecTaxNo\":\"\",\"country\":\"CN\",\"deliveryNo\":\"XSDS25072500002\",\"platformCode\":\"202507240080\",\"packageId\":\"\",\"deliveryType\":\"\",\"sourceId\":\"1948562664987770882\",\"trackNo\":\"\",\"receiverInfoVO\":{\"name\":\"吴嘉杰\",\"contact\":\"吴嘉杰\",\"email\":\"\",\"telNumber\":\"17727443881\",\"country\":\"CN\",\"province\":\"广东省\",\"city\":\"深圳市龙华区\",\"streetAddress\":\"\",\"addressFirst\":\"龙观西路39号龙城工业区绿联科技股份有限公司国际5栋\\n \",\"addressSecond\":\"\",\"zipCode\":\"518109\",\"actId\":\"1948562668854919170\",\"receiverTaxNo\":\"\"},\"senderInfo\":{\"id\":\"\",\"name\":\"XULI CHEN\",\"type\":\"DELIVER\",\"companyName\":\"XULI CHEN\",\"contact\":\"XULI CHEN\",\"email\":\"wuliubu@ulanzi.cn\",\"telNumber\":\"171 9108 7538\",\"country\":\"CN\",\"countryName\":\"中国大陆\",\"provinceName\":\"Guangdong Province\",\"cityName\":\"Dongguan City\",\"districtName\":\"Tangxia Town\",\"addressFirst\":\"3rd Floor Building 2 Huisheng Science and Technology Innovation Park\",\"addressSecond\":\"\",\"zipCode\":\"523649\"},\"returnInfo\":{\"id\":\"\",\"name\":\"XULI CHEN\",\"type\":\"DELIVER\",\"companyName\":\"XULI CHEN\",\"contact\":\"XULI CHEN\",\"email\":\"wuliubu@ulanzi.cn\",\"telNumber\":\"171 9108 7538\",\"country\":\"CN\",\"countryName\":\"中国大陆\",\"provinceName\":\"Guangdong Province\",\"cityName\":\"Dongguan City\",\"districtName\":\"Tangxia Town\",\"addressFirst\":\"3rd Floor Building 2 Huisheng Science and Technology Innovation Park\",\"addressSecond\":\"\",\"zipCode\":\"523649\"},\"parceInfoVO\":{\"hasBattery\":false,\"currency\":\"USD\",\"totalPrice\":64.0937,\"totalQuantity\":1,\"totalWeight\":2506,\"height\":10,\"width\":10,\"length\":53},\"logisticsProductVOList\":[{\"quantity\":1,\"weight\":2506,\"grossWeight\":2506,\"url\":\"group1/M00/83/B5/rBBkCmgAZnKACMhMAAWCa6b2fXQ907.png\",\"isElectric\":false,\"onlyBattery\":false,\"isLiquid\":false,\"id\":\"1765564975763427329\",\"platformLineNumber\":\"\",\"skuId\":\"1765564975763427329\",\"skuNo\":\"T081\",\"productProperty\":\"普货\",\"productPropertyId\":\"1580456666541006850\",\"declareModel\":\"\",\"declareChineseName\":\"三脚架\",\"declareEnglishName\":\"tripod\",\"declarePrice\":1023.7,\"customsCode\":\"9620001000\",\"declareUnit\":\"\",\"declareCurrency\":\"CNY\",\"declareCurrencySymbol\":\"¥\",\"destDeclarePrice\":64.0937,\"destCurrency\":\"USD\",\"destCurrencySymbol\":\"$\",\"exemption\":\"\",\"sourceCargo\":\"\",\"sourceCountry\":\"\",\"combinationDeclareType\":\"split\",\"childOrderId\":0,\"price\":0.1,\"currency\":\"USD\"}],\"authMap\":{\"clientId\":\"WJKJVX16Y0N\",\"logisticsPlatform\":\"EXPRESS\",\"clientSecret\":\"8vNVKqq0LNWKJxXtD5bjhlHZe1RVgOBd\",\"id\":\"1881885703664885761\",\"url\":\"https://bspgw.sf-express.com/std/service\"},\"logisticsChannelEntity\":{\"sourceId\":\"\",\"sourceType\":\"\",\"mainId\":\"1743158209943703553\",\"name\":\"顺丰标快\",\"code\":\"2\",\"effectiveTime\":\"T6\",\"effectiveTimeUnit\":\"day\",\"trackQueryMode\":\"TRACK123\",\"paperSize\":\"100*100\",\"paperLength\":100,\"paperWidth\":100,\"sortingCode\":\"\",\"feeRule\":\"billingWeight\",\"maxCustomsAmount\":0,\"maxCustomsCurrency\":\"USD\",\"minCustomsAmount\":0,\"minCustomsCurrency\":\"USD\",\"maxWeight\":0,\"weightUnit\":\"g\",\"maxLength\":0,\"sizeUnit\":\"cm\",\"maxWidth\":0,\"maxHeight\":0,\"taxModel\":\"DDP\",\"isIossPrepay\":true,\"isApiSign\":false,\"isApiInsurance\":false,\"disabled\":false,\"syncSourceId\":\"1732719670153310210\",\"declareCodeType\":\"transportNo\",\"deliveryType\":\"\",\"undeliverableDecision\":\"destroy\",\"trackQueryType\":\"transportNo\",\"volumeSetting\":0,\"shipmentOverLimitRate\":0,\"isPlatformShip\":false,\"carrierType\":\"\",\"isSendInvoice\":false,\"id\":\"1825450657969364993\",\"createUserId\":\"45\",\"createUserName\":\"吴俊鑫\",\"createTime\":1724056326000,\"updateUserId\":\"1650047251856232450\",\"updateUserName\":\"詹如娜\",\"updateTime\":1749557659000,\"version\":4,\"isDeleted\":false},\"logisticsSaleChannel\":{\"platformChannelId\":\"2\",\"cnName\":\"顺丰标快\",\"enName\":\"\",\"code\":\"2\",\"servicePlatform\":\"oms,tms\",\"channelStatus\":0,\"supplierName\":\"\",\"supplierCode\":\"\",\"shipmentMethod\":\"\",\"logisticsPlatform\":\"EXPRESS\",\"sourceData\":\"\",\"isTrack\":true,\"aging\":\"T6\",\"overseasWarehouseId\":\"\",\"carrierType\":\"\",\"id\":\"1732719670153310210\",\"createUserId\":\"0\",\"createUserName\":\"system\",\"createTime\":1701947536000,\"updateUserId\":\"0\",\"updateUserName\":\"system\",\"updateTime\":1737511490000,\"version\":0,\"isDeleted\":false}}";


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
    }
}
