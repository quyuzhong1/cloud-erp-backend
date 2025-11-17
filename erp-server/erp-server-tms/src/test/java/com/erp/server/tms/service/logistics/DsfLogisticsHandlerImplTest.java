package com.erp.server.tms.service.logistics;

import cn.hutool.json.JSONUtil;
import com.common.business.enums.LogisticsPlatformEnum;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.tms.entity.LogisticsChannelEntity;
import com.erp.model.tms.entity.LogisticsSaleChannelEntity;
import com.erp.model.tms.vo.request.*;
import com.erp.model.tms.vo.response.*;
import com.erp.server.tms.ErpServerTmsApplication;
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
 * @ClassName DsfLogisticsHandlerImplTest
 * @date 2023年11月09日
 * @version: 1.0
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {ErpServerTmsApplication.class})
public class DsfLogisticsHandlerImplTest {
    @Resource
    DsfLogisticsHandlerImpl dsfLogisticsHandler;

    private Map<String, String> authMap = new HashMap<>();

    public DsfLogisticsHandlerImplTest(){
        authMap.put("url","https://open.4px.com");
        authMap.put("clientId","e4d8f282-5ab6-49ed-86e5-5fc7ea316703");
        authMap.put("clientSecret","6c313d20-dbfd-4cc0-aaef-1c9d26f04461");
//        authMap.put("clientId","fad2854e-93a7-4598-95ff-cb60557dbc0a");
//        authMap.put("clientSecret","0e91ca81-22f8-4fce-95d1-18ed6269604b");
    }

    public Map<String, String> getLogisticsAuthConfig(){
        Map<String, String> logisticsAuthConfig = dsfLogisticsHandler.getLogisticsAuthConfigByShopId("");
        return logisticsAuthConfig;
    }
    public List<Map<String, String>> getLogisticsAuthConfigByPlatform(){
        List<Map<String, String>> logisticsAuthConfigByPlatform = dsfLogisticsHandler.getLogisticsAuthConfigByPlatform(LogisticsPlatformEnum.SHOPEE.getCode());
        return logisticsAuthConfigByPlatform;
    }

    @Test
    public void getChannel() {
        ApiResult<List<LogisticsSaleChannelEntity>> channel = dsfLogisticsHandler.getChannel(ChanelQueryVO.builder().authMap(authMap).build());
        System.out.println(channel);
    }

    @Test
    public void createOrder(){
        SenderInfo senderInfo = new SenderInfo();
        senderInfo.setAddressFirst("caifugang 4PX 25-26");
        senderInfo.setContact("contact");
        senderInfo.setCityName("Shengzhen");
        senderInfo.setCompanyName("4PX");
        senderInfo.setName("Wu Rao");
        senderInfo.setProvinceName("GuangDong");
        senderInfo.setTelNumber("13000000000");
        senderInfo.setEmail("123@q.con");
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
        logisticsProductVO.setIsElectric(false);
        logisticsProductVO.setDeclarePrice(BigDecimal.valueOf(2));
        logisticsProductVO.setDestDeclarePrice(BigDecimal.valueOf(2));

        LogisticsSaleChannelEntity logisticsSaleChannel = new LogisticsSaleChannelEntity();
        logisticsSaleChannel.setCode("PY");
        logisticsSaleChannel.setShipmentMethod("Express-Post");
        logisticsSaleChannel.setPlatformChannelId("155");

        LogisticsChannelEntity logisticsChannel = new LogisticsChannelEntity();
//        logisticsChannel.setCode("S832");
        logisticsChannel.setId("1724614809662599171");
        //DDU/DDP
        logisticsChannel.setTaxModel("DDU");
        final LogisticsOrderVO logisticsOrderVO = LogisticsOrderVO.builder()
                .authMap(authMap)
                .orderSource("ERP")
//                .facility("can")
                .deliveryNo("wj12345167721")
                .receiverInfoVO(ReceiverInfoVO.builder()
                        .addressFirst("address")
                        .email("965656546@qq.con")
                        .city("Auburn")
                        .name("zhang san")
                        .companyName("")
                        .contact("zhang san")
                        .country("US")
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
        ApiResult<LogisticsOrderResponseVO> order = dsfLogisticsHandler.createOrder(logisticsOrderVO);
        System.out.println(order);
    }
    @Test
    public void createOrder1(){
        String json = "{\"oaid\":\"\",\"orderSource\":\"soB2c\",\"orderType\":\"selfAdd\",\"iossCode\":\"\",\"voecTaxNo\":\"3017745\",\"country\":\"PE\",\"deliveryNo\":\"XSDS25061200038\",\"platformCode\":\"\",\"packageId\":\"\",\"deliveryType\":\"SELF_SEND\",\"sourceId\":\"1933116688529358849\",\"trackNo\":\"UK183900254YP\",\"receiverInfoVO\":{\"name\":\" Kwan Hei Tam\",\"contact\":\" Kwan Hei Tam\",\"email\":\"\",\"telNumber\":\"+61434360157\",\"country\":\"PE\",\"province\":\"CUSCO\",\"city\":\"CUSCO\",\"streetAddress\":\"\",\"addressFirst\":\"AV EL SOL 679\",\"addressSecond\":\"\",\"zipCode\":\"08002\",\"actId\":\"1933116693860319234\",\"receiverTaxNo\":\"\"},\"senderInfo\":{\"id\":\"\",\"name\":\"XULI CHEN\",\"type\":\"DELIVER\",\"companyName\":\"XULI CHEN\",\"contact\":\"XULI CHEN\",\"email\":\"wuliubu@ulanzi.cn\",\"telNumber\":\"171 9108 7538\",\"country\":\"CN\",\"countryName\":\"中国大陆\",\"provinceName\":\"Guangdong Province\",\"cityName\":\"Dongguan City\",\"districtName\":\"Tangxia Town\",\"addressFirst\":\"3rd Floor Building 2 Huisheng Science and Technology Innovation Park\",\"addressSecond\":\"\",\"zipCode\":\"523649\"},\"returnInfo\":{\"id\":\"\",\"name\":\"Huisheng Science and Technology Innovation Park \",\"type\":\"REFUND\",\"companyName\":\"\",\"contact\":\"XULI CHEN\",\"email\":\"wuliubu@ulanzi.cn\",\"telNumber\":\"171 9108 7538\",\"country\":\"CN\",\"countryName\":\"中国\",\"provinceName\":\"Guangdong Province\",\"cityName\":\"Dongguan City\",\"districtName\":\"\",\"addressFirst\":\"3rd Floor, Building 2, Huisheng Science and Technology Innovation Park, No. 24 Huanshi South Road, Tangxia Town\",\"addressSecond\":\"\",\"zipCode\":\"523649\"},\"parceInfoVO\":{\"hasBattery\":true,\"currency\":\"USD\",\"totalPrice\":25.7272,\"totalQuantity\":8,\"totalWeight\":728,\"height\":49,\"width\":35,\"length\":55},\"logisticsProductVOList\":[{\"quantity\":1,\"weight\":64,\"grossWeight\":64,\"url\":\"group1/M00/00/85/rBBkCmVcIbiAYgfpAAEVuxtu154047.png\",\"isElectric\":true,\"onlyBattery\":false,\"isLiquid\":false,\"id\":\"1674319681973915650\",\"platformLineNumber\":\"\",\"skuId\":\"1674319681973915650\",\"skuNo\":\"C071GBB1\",\"productProperty\":\"带电池\",\"productPropertyId\":\"1773576997822271492\",\"declareModel\":\"\",\"declareChineseName\":\"云台\",\"declareEnglishName\":\"Gimbal\",\"customsCode\":\"8529904900\",\"declareUnit\":\"\",\"declareCurrency\":\"\",\"declareCurrencySymbol\":\"\",\"destDeclarePrice\":4.7671,\"destCurrency\":\"USD\",\"destCurrencySymbol\":\"$\",\"exemption\":\"\",\"sourceCargo\":\"\",\"sourceCountry\":\"\",\"combinationDeclareType\":\"split\",\"childOrderId\":0,\"price\":0,\"currency\":\"USD\"},{\"quantity\":1,\"weight\":32,\"grossWeight\":32,\"url\":\"group1/M00/00/C7/rBBkCmWp-FyAA8yWABLX5h_iwRE064.png\",\"isElectric\":false,\"onlyBattery\":false,\"isLiquid\":false,\"id\":\"1748197082478415873\",\"platformLineNumber\":\"\",\"skuId\":\"1748197082478415873\",\"skuNo\":\"C015\",\"productProperty\":\"普货\",\"productPropertyId\":\"1580456666541006850\",\"declareModel\":\"\",\"declareChineseName\":\"保护壳\",\"declareEnglishName\":\"Protector\",\"customsCode\":\"3926909090\",\"declareUnit\":\"\",\"declareCurrency\":\"CNY\",\"declareCurrencySymbol\":\"¥\",\"destDeclarePrice\":0.4,\"destCurrency\":\"USD\",\"destCurrencySymbol\":\"$\",\"exemption\":\"\",\"sourceCargo\":\"\",\"sourceCountry\":\"\",\"combinationDeclareType\":\"split\",\"childOrderId\":0,\"price\":0,\"currency\":\"USD\"},{\"quantity\":1,\"weight\":44,\"grossWeight\":44,\"url\":\"group1/M00/0A/B7/rBBkCmaeE2qAC_S0ADgHijF1APU914.png\",\"isElectric\":false,\"onlyBattery\":false,\"isLiquid\":false,\"id\":\"1787668751768883201\",\"platformLineNumber\":\"\",\"skuId\":\"1787668751768883201\",\"skuNo\":\"C039\",\"productProperty\":\"\",\"productPropertyId\":\"\",\"declareModel\":\"\",\"declareChineseName\":\"脚架\",\"declareEnglishName\":\"tripod\",\"customsCode\":\"9620009000\",\"declareUnit\":\"\",\"declareCurrency\":\"CNY\",\"declareCurrencySymbol\":\"¥\",\"destDeclarePrice\":2.8625,\"destCurrency\":\"USD\",\"destCurrencySymbol\":\"$\",\"exemption\":\"\",\"sourceCargo\":\"\",\"sourceCountry\":\"\",\"combinationDeclareType\":\"split\",\"childOrderId\":0,\"price\":0,\"currency\":\"USD\"},{\"quantity\":1,\"weight\":38,\"grossWeight\":38,\"url\":\"group1/M00/05/19/rBBkCmZ6hguARQO3ACm1lfegn_c486.png\",\"isElectric\":false,\"onlyBattery\":false,\"isLiquid\":false,\"id\":\"1805454229104496642\",\"platformLineNumber\":\"\",\"skuId\":\"1805454229104496642\",\"skuNo\":\"C043\",\"productProperty\":\"\",\"productPropertyId\":\"\",\"declareModel\":\"\",\"declareChineseName\":\"底座\",\"declareEnglishName\":\"base\",\"customsCode\":\"7616999000\",\"declareUnit\":\"\",\"declareCurrency\":\"CNY\",\"declareCurrencySymbol\":\"¥\",\"destDeclarePrice\":2.5763,\"destCurrency\":\"USD\",\"destCurrencySymbol\":\"$\",\"exemption\":\"\",\"sourceCargo\":\"\",\"sourceCountry\":\"\",\"combinationDeclareType\":\"split\",\"childOrderId\":0,\"price\":0,\"currency\":\"USD\"},{\"quantity\":1,\"weight\":216,\"grossWeight\":216,\"url\":\"group1/M00/00/70/rBBkCmVCC1KAbITeAAo0IyRT0Qc330.png,group1/M00/00/70/rBBkCmVCC1KAHkGiAAKhUpu6AYc932.png\",\"isElectric\":false,\"onlyBattery\":false,\"isLiquid\":false,\"id\":\"1719631191855861762\",\"platformLineNumber\":\"\",\"skuId\":\"1719631191855861762\",\"skuNo\":\"B012\",\"productProperty\":\"普货\",\"productPropertyId\":\"1580456666541006850\",\"declareModel\":\"\",\"declareChineseName\":\"收纳包\",\"declareEnglishName\":\"Storage bag\",\"customsCode\":\"4202129000\",\"declareUnit\":\"\",\"declareCurrency\":\"\",\"declareCurrencySymbol\":\"\",\"destDeclarePrice\":2.7,\"destCurrency\":\"USD\",\"destCurrencySymbol\":\"$\",\"exemption\":\"\",\"sourceCargo\":\"\",\"sourceCountry\":\"\",\"combinationDeclareType\":\"split\",\"childOrderId\":0,\"price\":0,\"currency\":\"USD\"},{\"quantity\":1,\"weight\":52,\"grossWeight\":52,\"url\":\"group1/M00/00/BC/rBBkCmWfyAuAW35aAAaKib_TX0k082.png\",\"isElectric\":true,\"onlyBattery\":false,\"isLiquid\":false,\"id\":\"1664525643150266369\",\"platformLineNumber\":\"\",\"skuId\":\"1664525643150266369\",\"skuNo\":\"L042GBB1\",\"productProperty\":\"带电池\",\"productPropertyId\":\"1773576997822271492\",\"declareModel\":\"\",\"declareChineseName\":\"补光灯\",\"declareEnglishName\":\"Fill light\",\"customsCode\":\"9405429000\",\"declareUnit\":\"\",\"declareCurrency\":\"CNY\",\"declareCurrencySymbol\":\"¥\",\"destDeclarePrice\":1.7513,\"destCurrency\":\"USD\",\"destCurrencySymbol\":\"$\",\"exemption\":\"\",\"sourceCargo\":\"\",\"sourceCountry\":\"\",\"combinationDeclareType\":\"split\",\"childOrderId\":0,\"price\":0,\"currency\":\"USD\"},{\"quantity\":1,\"weight\":124,\"grossWeight\":124,\"url\":\"group1/M00/00/C0/rBBkCmWk8oqAbedyAALbNGtSslo818.jpg\",\"isElectric\":false,\"onlyBattery\":false,\"isLiquid\":false,\"id\":\"1746816531796332545\",\"platformLineNumber\":\"\",\"skuId\":\"1746816531796332545\",\"skuNo\":\"C014\",\"productProperty\":\"\",\"productPropertyId\":\"\",\"declareModel\":\"\",\"declareChineseName\":\"快装板\",\"declareEnglishName\":\"Quick installation board\",\"customsCode\":\"7616999000\",\"declareUnit\":\"\",\"declareCurrency\":\"\",\"declareCurrencySymbol\":\"\",\"destDeclarePrice\":8.33,\"destCurrency\":\"USD\",\"destCurrencySymbol\":\"$\",\"exemption\":\"\",\"sourceCargo\":\"\",\"sourceCountry\":\"\",\"combinationDeclareType\":\"split\",\"childOrderId\":0,\"price\":0,\"currency\":\"USD\"},{\"quantity\":1,\"weight\":158,\"grossWeight\":158,\"url\":\"group1/M00/00/88/rBBkCmVezzaAPk7EAAjVAQuA0wI529.png\",\"isElectric\":false,\"onlyBattery\":false,\"isLiquid\":false,\"id\":\"1665905819171950594\",\"platformLineNumber\":\"\",\"skuId\":\"1665905819171950594\",\"skuNo\":\"C064GBB1\",\"productProperty\":\"普货\",\"productPropertyId\":\"1580456666541006850\",\"declareModel\":\"\",\"declareChineseName\":\"背包支架\",\"declareEnglishName\":\"Backpack holder\",\"customsCode\":\"3926909090\",\"declareUnit\":\"\",\"declareCurrency\":\"\",\"declareCurrencySymbol\":\"\",\"destDeclarePrice\":2.34,\"destCurrency\":\"USD\",\"destCurrencySymbol\":\"$\",\"exemption\":\"\",\"sourceCargo\":\"\",\"sourceCountry\":\"\",\"combinationDeclareType\":\"split\",\"childOrderId\":0,\"price\":0,\"currency\":\"USD\"}],\"authMap\":{\"clientId\":\"e4d8f282-5ab6-49ed-86e5-5fc7ea316703\",\"logisticsPlatform\":\"DSF\",\"clientSecret\":\"6c313d20-dbfd-4cc0-aaef-1c9d26f04461\",\"id\":\"1798615319959310337\",\"url\":\"https://open.4px.com\"},\"logisticsChannelEntity\":{\"sourceId\":\"\",\"sourceType\":\"\",\"mainId\":\"1743157865641676801\",\"name\":\"香港D记标准速递\",\"code\":\"A1\",\"effectiveTime\":\"0\",\"effectiveTimeUnit\":\"day\",\"trackQueryMode\":\"TRACK123\",\"paperSize\":\"100*100\",\"paperLength\":100,\"paperWidth\":100,\"sortingCode\":\"\",\"feeRule\":\"billingWeight\",\"maxCustomsAmount\":0,\"maxCustomsCurrency\":\"USD\",\"minCustomsAmount\":0,\"minCustomsCurrency\":\"USD\",\"maxWeight\":0,\"weightUnit\":\"g\",\"maxLength\":0,\"sizeUnit\":\"cm\",\"maxWidth\":0,\"maxHeight\":0,\"taxModel\":\"DDP\",\"isIossPrepay\":true,\"isApiSign\":false,\"isApiInsurance\":false,\"disabled\":false,\"syncSourceId\":\"1798615326842163201\",\"declareCodeType\":\"transportNo\",\"deliveryType\":\"SELF_SEND\",\"undeliverableDecision\":\"destroy\",\"trackQueryType\":\"transportNo\",\"volumeSetting\":0,\"shipmentOverLimitRate\":0,\"isPlatformShip\":false,\"carrierType\":\"\",\"isSendInvoice\":false,\"id\":\"1798615454168649729\",\"createUserId\":\"45\",\"createUserName\":\"吴俊鑫\",\"createTime\":1717658315000,\"updateUserId\":\"45\",\"updateUserName\":\"吴俊鑫\",\"updateTime\":1749557659000,\"version\":2,\"isDeleted\":false},\"logisticsSaleChannel\":{\"platformChannelId\":\"\",\"cnName\":\"4PX国际快递特快速递\",\"enName\":\"4PX国际快递特快速递\",\"code\":\"A1\",\"servicePlatform\":\"oms,tms\",\"channelStatus\":0,\"supplierName\":\"\",\"supplierCode\":\"\",\"shipmentMethod\":\"\",\"logisticsPlatform\":\"DSF\",\"sourceData\":\"\",\"isTrack\":true,\"overseasWarehouseId\":\"\",\"carrierType\":\"\",\"id\":\"1798615326842163201\",\"createUserId\":\"0\",\"createUserName\":\"system\",\"createTime\":1717658285000,\"updateUserId\":\"0\",\"updateUserName\":\"system\",\"updateTime\":1740972626000,\"version\":0,\"isDeleted\":false}}";
        ApiResult<LogisticsOrderResponseVO> order = dsfLogisticsHandler.createOrder(JSONUtil.toBean(json,LogisticsOrderVO.class));
        System.out.println(order);
    }
    @Test
    public void queryOrderList(){
        LogisticsQueryBaseVO logisticsQueryVOList = new LogisticsQueryBaseVO();
        logisticsQueryVOList.setDeliveryNo("WJ12345167721");
        logisticsQueryVOList.setAuthMap(authMap);
        ApiResult<List<LogisticsOrderResponseVO>> listApiResult = dsfLogisticsHandler.queryOrderList(Collections.singletonList(logisticsQueryVOList));
        System.out.println(listApiResult);
    }

    @Test
    public void getLabelList() throws IOException {
        LogisticsGetLabelVO logisticsQueryVO2 = new LogisticsGetLabelVO();
        logisticsQueryVO2.setDeliveryNo("XSDS25101000003_2");
        logisticsQueryVO2.setIsPdn("Y");
        logisticsQueryVO2.setAuthMap(authMap);
        LogisticsSaleChannelEntity logisticsChannelEntity = new LogisticsSaleChannelEntity();
        logisticsChannelEntity.setCode("OH");
        logisticsQueryVO2.setLogisticsSaleChannelEntity(logisticsChannelEntity);
        ApiResult<List<LogisticsPrintLabelResponse>> labelList = dsfLogisticsHandler.getLabelList(Collections.singletonList(logisticsQueryVO2));
        System.out.println(labelList);

    }

    @Test
    public void interceptOrder(){
        LogisticsInterceptOrderVO logisticsQueryVOList2 = new LogisticsInterceptOrderVO();
        logisticsQueryVOList2.setDeliveryNo("WJ12345167721");
        logisticsQueryVOList2.setAuthMap(authMap);
        ApiResult<List<InterceptResponseVO>> listApiResult = dsfLogisticsHandler.interceptOrder(Collections.singletonList(logisticsQueryVOList2));
        System.out.println(listApiResult);
    }

    @Test
    public void cancelOrder(){
        LogisticsCancelOrderVO logisticsQueryVOList = new LogisticsCancelOrderVO();
        logisticsQueryVOList.setDeliveryNo("WJ12345167721");
//        logisticsQueryVOList.setTransportNo("DS4PX3000001184782CN");
//        logisticsQueryVOList.setTrackNo("LM000002721CA");
        logisticsQueryVOList.setAuthMap(authMap);
        ApiResult<List<CancelResponseVO>> listApiResult = dsfLogisticsHandler.cancelOrder(Collections.singletonList(logisticsQueryVOList));
        System.out.println(listApiResult);
    }

    @Test
    public void confirmOrder(){
        LogisticsQueryBaseVO logisticsQueryVO = new LogisticsCancelOrderVO();
        logisticsQueryVO.setDeliveryNo("wj12345167721");
        logisticsQueryVO.setTransportNo("lBK4IuWt-IlRQrfmJhnniA");
        logisticsQueryVO.setTrackNo("LM000002721CA");
        logisticsQueryVO.setAuthMap(authMap);
        ApiResult<List<ConfirmResponseVO>> listApiResult = dsfLogisticsHandler.confirmOrder(Collections.singletonList(logisticsQueryVO));
        System.out.println(listApiResult);
    }
    @Test
    public void authorization() {

    }
}
