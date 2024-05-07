package com.erp.server.tms.service.logistics;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.common.business.enums.LogisticsPlatformEnum;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.tms.entity.LogisticsAuthEntity;
import com.erp.model.tms.entity.LogisticsSaleChannelEntity;
import com.erp.model.tms.vo.request.*;
import com.erp.model.tms.vo.response.LogisticsOrderResponseVO;
import com.erp.server.tms.ErpServerTmsApplication;
import com.sdk.tms.yanwen.dto.response.YanWenCreateWayBill;
import com.sdk.tms.yanwen.dto.response.YanWenResponse;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {ErpServerTmsApplication.class})
public class YanWenLogisticsHandlerImplTest {

    @Resource
    private YanWenLogisticsHandlerImpl yanWenLogisticsHandler;

    private Map<String, String> authMap = new HashMap<>();

    /**
     * 生产环境：
     * url:Https://open.yw56.com.cn
     * clientId:30114979
     * clientSercet:62854821B3B261983589ACFE94C7D652
     *
     * UAT:
     * url:Https://ejf-fat.yw56.com.cn
     * clientId:100000
     * clientSercet:D6140AA383FD8515B09028C586493DDB
     */
    public YanWenLogisticsHandlerImplTest(){
        authMap.put("url","Https://ejf-fat.yw56.com.cn");
        authMap.put("clientId","100000");
        authMap.put("clientSecret","D6140AA383FD8515B09028C586493DDB");
    }
    public Map<String, String> getLogisticsAuthConfig(){
        Map<String, String> logisticsAuthConfig = yanWenLogisticsHandler.getLogisticsAuthConfig("");
        return logisticsAuthConfig;
    }
    public List<Map<String, String>> getLogisticsAuthConfigByPlatform(){
        List<Map<String, String>> logisticsAuthConfigByPlatform = yanWenLogisticsHandler.getLogisticsAuthConfigByPlatform(LogisticsPlatformEnum.SHOPEE.getCode());
        return logisticsAuthConfigByPlatform;
    }

    @Test
    public void getChannel() {
        System.out.println(yanWenLogisticsHandler.getChannel(ChanelQueryVO.builder().authMap(authMap).build()));
    }

    @Test
    public void testCreateOrder() {
        String json = "{\"oaid\":\"\",\"orderSource\":\"soB2c\",\"iossCode\":\"\",\"deliveryNo\":\"XSDS24050400001\",\"trackNo\":\"\",\"receiverInfoVO\":{\"name\":\"林亮(ハヤシリョウ)\",\"contact\":\"林亮(ハヤシリョウ)\",\"email\":\"\",\"telNumber\":\"09097542558\",\"country\":\"JP\",\"province\":\"北海道\",\"city\":\"札幌市\",\"district\":\"西区発寒六条14丁目\",\"streetAddress\":\"札幌市西区発寒六条14丁目13-28 グリニッジタウン西札幌206号室\",\"addressFirst\":\"札幌市西区発寒六条14丁目13-28 グリニッジタウン西札幌206号室\",\"addressSecond\":\"\",\"zipCode\":\"0630826\",\"actId\":\"1786733972802244610\",\"receiverTaxNo\":\"\"},\"senderInfo\":{\"id\":\"\",\"name\":\"Tangxia Town, Dongguan City\",\"type\":\"DELIVER\",\"companyName\":\"SHENZHEN WEIJI TECHNOLOGY CO\",\"contact\":\"Chen Xuli\",\"email\":\"wupuming@ulanzi.cn\",\"telNumber\":\"18806656774\",\"country\":\"CN\",\"countryName\":\"中国\",\"provinceName\":\"Guangdong Province\",\"cityName\":\"Dongguan city\",\"districtName\":\"Tangxia town\",\"addressFirst\":\"DONGGUAN CITY TANGXIA TOWN FLOOR 9,BUILDING 205,NEW SUN CITY INDUSTRIAL PARK,LINCU\",\"addressSecond\":\"\",\"zipCode\":\"523000\"},\"returnInfo\":{\"id\":\"\",\"name\":\"Tangxia Town, Dongguan City\",\"type\":\"DELIVER\",\"companyName\":\"SHENZHEN WEIJI TECHNOLOGY CO\",\"contact\":\"Chen Xuli\",\"email\":\"wupuming@ulanzi.cn\",\"telNumber\":\"18806656774\",\"country\":\"CN\",\"countryName\":\"中国\",\"provinceName\":\"Guangdong Province\",\"cityName\":\"Dongguan city\",\"districtName\":\"Tangxia town\",\"addressFirst\":\"DONGGUAN CITY TANGXIA TOWN FLOOR 9,BUILDING 205,NEW SUN CITY INDUSTRIAL PARK,LINCU\",\"addressSecond\":\"\",\"zipCode\":\"523000\"},\"parceInfoVO\":{\"hasBattery\":false,\"currency\":\"USD\",\"totalPrice\":15,\"totalQuantity\":3,\"totalWeight\":30,\"height\":0,\"width\":0,\"length\":0},\"logisticsProductVOList\":[{\"quantity\":3,\"weight\":25,\"grossWeight\":25,\"isElectric\":false,\"skuId\":\"1619215509440434177\",\"skuNo\":\"A001\",\"productPropertyId\":\"\",\"declareModel\":\"\",\"declareChineseName\":\"摄影配件\",\"declareEnglishName\":\"Photography accessories\",\"declarePrice\":0,\"customsCode\":\"\",\"declareUnit\":\"\",\"declareCurrency\":\"USD\",\"declareCurrencySymbol\":\"$\",\"destDeclarePrice\":5,\"destCurrency\":\"USD\",\"destCurrencySymbol\":\"$\",\"exemption\":\"\",\"sourceCargo\":\"\",\"sourceCountry\":\"\",\"combinationDeclareType\":\"split\"}],\"authMap\":{\"clientId\":\"30114979\",\"logisticsPlatform\":\"YanWen\",\"clientSecret\":\"62854821B3B261983589ACFE94C7D652\",\"id\":\"1770279035289473026\",\"url\":\"Https://open.yw56.com.cn\",\"token\":\"Atza|IwEBIMB42jgHQgijIqa1u7OBdRAZQ-_CcE6soYO78BvYguU0BQXdllddyznICuG2AcQ33gGdfNoSYyI6cyLIhdEI5zsmQgez1EXVpador02BiFxeH2K24qxOZx556QiM7lM8PxUtdaDI53yR0_H0PW1uCFnnmLurRRr7tg9GYb6B4NMamQ3xuuSu4Eq0GGwQ29x3QcfAD4z4jlJeMFeV1CI1Kg4q-wH4wlAo5fND_mkhGUW91-BczV46_-kV1TzwuLYj42q6D6rC_98YQMlKuQlVczigg3XqJ3NmQgXjaUtLqqDCbMCgu3nEBIJtSfUvdzMw4sY\"},\"logisticsChannelEntity\":{\"sourceId\":\"\",\"sourceType\":\"\",\"mainId\":\"1770278952271613954\",\"name\":\"燕文专线追踪-普货\",\"code\":\"481\",\"effectiveTime\":\"0\",\"effectiveTimeUnit\":\"day\",\"trackQueryMode\":\"track123\",\"paperSize\":\"100*100\",\"paperLength\":100,\"paperWidth\":100,\"sortingCode\":\"\",\"feeRule\":\"billingWeight\",\"maxCustomsAmount\":0,\"maxCustomsCurrency\":\"USD\",\"minCustomsAmount\":0,\"minCustomsCurrency\":\"USD\",\"maxWeight\":0,\"weightUnit\":\"g\",\"maxLength\":0,\"sizeUnit\":\"cm\",\"maxWidth\":0,\"maxHeight\":0,\"taxModel\":\"DDP\",\"isIossPrepay\":false,\"isApiSign\":false,\"isApiInsurance\":false,\"disabled\":false,\"syncSourceId\":\"1770279040544935937\",\"id\":\"1770304369082765320\",\"createUserId\":\"1676946232032890881\",\"createUserName\":\"张阳新\",\"createTime\":1710908426911,\"updateUserId\":\"1676946232032890881\",\"updateUserName\":\"张阳新\",\"updateTime\":1714307255399,\"version\":5,\"isDeleted\":false},\"logisticsSaleChannel\":{\"platformChannelId\":\"481\",\"cnName\":\"燕文专线追踪-普货\",\"enName\":\"Direct Line Tracked Packet-P\",\"code\":\"481\",\"channelStatus\":0,\"supplierName\":\"\",\"supplierCode\":\"\",\"shipmentMethod\":\"\",\"logisticsPlatform\":\"YanWen\",\"sourceData\":\"\",\"isTrack\":true,\"overseasWarehouseId\":\"\",\"id\":\"1770279040544935937\",\"createUserId\":\"0\",\"createUserName\":\"system\",\"createTime\":1710902388117,\"updateUserId\":\"0\",\"updateUserName\":\"system\",\"updateTime\":1710902388117,\"version\":0,\"isDeleted\":false}}";
                LogisticsOrderVO logisticsOrderVO = JSONObject.parseObject(json,new TypeReference<LogisticsOrderVO>() {}.getType());
        logisticsOrderVO.setAuthMap(authMap);
//        SenderInfo senderInfo = new SenderInfo();
//        senderInfo.setAddressFirst("address");
//        senderInfo.setContact("contact");
////        senderInfo.setCity("newyork");
////        senderInfo.setCityId("1");
//        senderInfo.setCompanyName("componeny");
//        senderInfo.setName("name");
////        senderInfo.setProvince("shenzhen");
//        senderInfo.setTelNumber("12345678");
//        senderInfo.setEmail("321546");
//        senderInfo.setCountry("China");
//        senderInfo.setZipCode("515800");
//        LogisticsProductVO logisticsProductVO = new LogisticsProductVO();
//        logisticsProductVO.setEnglishUsage("materi");
//        logisticsProductVO.setDeclareChineseName("物流");
//        logisticsProductVO.setDeclareEnglishName("mta");
//        logisticsProductVO.setDestDeclarePrice(new BigDecimal("12345"));
//        logisticsProductVO.setWeight(123456);
//        logisticsProductVO.setQuantity(1324);
//        LogisticsSaleChannelEntity logisticsSaleChannel = new LogisticsSaleChannelEntity();
//        logisticsSaleChannel.setCode("S832");
//        logisticsSaleChannel.setShipmentMethod("Express-Post");
//        logisticsSaleChannel.setPlatformChannelId("155");
//        final LogisticsOrderVO logisticsOrderVO = LogisticsOrderVO.builder()
////                .channelId("155")
//                .orderSource("ERP")
//                .deliveryNo("wj12345168")
//                .authMap(authMap)
//                .receiverInfoVO(ReceiverInfoVO.builder()
//                        .addressFirst("address")
//                        .email("123@q.con")
//                        .city("shenz")
//                        .name("mark")
//                        .companyName("componey")
//                        .contact("mark")
//                        .country("US")
//                        .zipCode("12201")
//                        .province("state")
//                        .telNumber("123456789")
//                        .build())
//                .senderInfo(senderInfo)
//                .parceInfoVO(ParceInfoVO.builder()
//                        .currency("USD")
//                        .height(1)
//                        .hasBattery(true)
//                        .totalPrice(new BigDecimal("123"))
//                        .totalQuantity(12)
//                        .length(1)
//                        .totalWeight(123)
//                        .width(123)
////                        .ioss("123456")
//                        .build())
//                .logisticsProductVOList(Arrays.asList(
//                        logisticsProductVO
//                ))
//                .build();
        ApiResult<LogisticsOrderResponseVO> result = yanWenLogisticsHandler.createOrder(logisticsOrderVO);
        System.out.println(result);
    }

    @Test
    public void getLabelUrl() {
        LogisticsGetLabelVO logisticsQueryVO = new LogisticsGetLabelVO();
        logisticsQueryVO.setAuthMap(authMap);
        logisticsQueryVO.setTransportNo("LR085325053CN");
        System.out.println(yanWenLogisticsHandler.getLabelList(Arrays.asList(logisticsQueryVO)));
    }

    @Test
    public void cancelOrder() {
        LogisticsCancelOrderVO cancelOrderVO = new LogisticsCancelOrderVO();
        cancelOrderVO.setTransportNo("LR085933164CN");
        cancelOrderVO.setDeliveryNo("WJ085933164CN");
        cancelOrderVO.setAuthMap(authMap);
        System.out.println(yanWenLogisticsHandler.cancelOrder(Arrays.asList(cancelOrderVO)));
    }

    @Test
    public void queryOrder() {
        LogisticsQueryBaseVO logisticsQueryBaseVO = new LogisticsQueryBaseVO();
        logisticsQueryBaseVO.setDeliveryNo("wj12345168");
        logisticsQueryBaseVO.setAuthMap(authMap);
        System.out.println(yanWenLogisticsHandler.queryOrderList(Arrays.asList(logisticsQueryBaseVO)));
    }

    @Test
    public void authorization() {
        ApiResult apiResult = yanWenLogisticsHandler.authorization(authMap);
        System.out.println(apiResult);
    }
}