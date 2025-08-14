package com.erp.server.tms.service.logistics;

import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.common.business.enums.LogisticsPlatformEnum;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.tms.entity.LogisticsAddressEntity;
import com.erp.model.tms.entity.LogisticsSaleChannelEntity;
import com.erp.model.tms.enums.LogisticsAddressTypeEnum;
import com.erp.model.tms.vo.request.ChanelQueryVO;
import com.erp.model.tms.vo.request.LogisticsGetLabelVO;
import com.erp.model.tms.vo.request.LogisticsOrderVO;
import com.erp.model.tms.vo.request.LogisticsQueryBaseVO;
import com.erp.model.tms.vo.response.LogisticsOrderResponseVO;
import com.erp.model.tms.vo.response.LogisticsPrintLabelResponse;
import com.erp.model.tms.vo.response.LogisticsServiceResponseVO;
import com.erp.oms.aliexpress.constants.AliexpressConstants;
import com.erp.oms.aliexpress.dto.request.OrderRequest;
import com.erp.oms.aliexpress.dto.response.AliExpressOrder;
import com.erp.oms.aliexpress.service.AliExpressOrderService;
import com.erp.server.tms.ErpServerTmsApplication;
import com.erp.server.tms.convert.LogisticsAddressConverter;
import com.erp.server.tms.service.LogisticsAddressService;
import com.erp.tms.aliexpress.api.IopResponse;
import com.erp.tms.aliexpress.model.address.SellerResponse;
import com.erp.tms.aliexpress.model.order.request.Address;
import com.erp.tms.aliexpress.model.query.request.QueryLogisticsRequest;
import com.erp.tms.aliexpress.service.AliExpressShipperService;
import com.erp.tms.aliexpress.util.ApiException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.io.IOException;
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
public class AliExpressLogisticsHandlerImplTest {
    String TOP_USER_KEY = "2671706312";
    String CLIENT = "ISV-数大臣";
    @Resource
    private AliExpressLogisticsHandlerImpl aliExpressLogisticsHandler;
    @Resource
    private AliExpressShipperService aliExpressShipperService;

    @Resource
    private AliExpressOrderService aliExpressOrderService;

    @Resource
    private LogisticsAddressService logisticsAddressService;
    private Map<String, String> authMap = new HashMap<>();

    public AliExpressLogisticsHandlerImplTest(){
        //test
//        String CLIENT_CODE = "502978";
//        String CHECK_WORD = "DfFGCAXMY7pptKfhz7IkWEa0zC0xddhY";
//        String token = "50000700423zHPZZqMly9iku4MQdb7h0hqR18ff9902ExugZffT7nzxEiFwFyWHdZKNC";
        //prod
        String CLIENT_CODE = "503630";
        String CHECK_WORD = "PxkJJ2fLGh5HcwzhUJp267lQSbkuAFRJ";
        String token = "50000800908t9Fq5iBwW1439a24doPZ9ZGVgpSEzOdcggU6LU0CiIxk2Fx9t8syRFp3v";
        authMap.put("clientId",CLIENT_CODE);
        authMap.put("clientSecret",CHECK_WORD);
        authMap.put("token",token);
        authMap.put("url","https://api-sg.aliexpress.com");
    }

    @Test
    public void getService() {
        ApiResult<List<LogisticsServiceResponseVO>> listApiResult = aliExpressLogisticsHandler.listLogisticsService(authMap);
        System.out.println("==============================================");
        System.out.println(JSONUtil.toJsonStr(listApiResult));
        System.out.println("==============================================");
    }

    public Map<String, String> getLogisticsAuthConfig(){
        Map<String, String> logisticsAuthConfig = aliExpressLogisticsHandler.getLogisticsAuthConfigByShopId("");
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
    public void getSaleChannel() throws InterruptedException, ApiException {
        aliExpressShipperService.getChanelList(authMap);
    }
    @Test
    public void createOrder(){
//        //发货人
//        SenderInfo senderInfo = new SenderInfo();
//        senderInfo.setId("440174196461");
//        senderInfo.setAddressFirst("5-8#lift 9th floor buliding 205 xinyang street xintaiyang Industrial Park lin village");
//        senderInfo.setContact("contact");
//        senderInfo.setCityName("Dongguan");
//        senderInfo.setCompanyName("4PX");
//        senderInfo.setName("chenxuli");
//        senderInfo.setProvinceName("Guangdong Province");
//        senderInfo.setTelNumber("17191087538");
//        senderInfo.setEmail("dhphoto@aliyun.com");
//        senderInfo.setCountry("CN");
//        senderInfo.setZipCode("523000");
//        //上门揽收
//        SenderInfo pickUp = new SenderInfo();
//        pickUp.setId("440174520062");
//        pickUp.setAddressFirst("5-8#lift 9th floor buliding 205 xinyang street xintaiyang Industrial Park lin village");
//        pickUp.setContact("contact");
//        pickUp.setCityName("Dongguan");
//        pickUp.setCompanyName("4PX");
//        pickUp.setName("chenxuli");
//        pickUp.setProvinceName("Guangdong Province");
//        pickUp.setTelNumber("17191087538");
//        pickUp.setEmail("dhphoto@aliyun.com");
//        pickUp.setCountry("CN");
//        pickUp.setZipCode("523000");
//        //退货
//        SenderInfo returnInfo = new SenderInfo();
//        returnInfo.setId("440173332446");
//        returnInfo.setAddressFirst("5-8#lift 9th floor buliding 205 xinyang street xintaiyang Industrial Park lin village");
//        returnInfo.setContact("contact");
//        returnInfo.setCityName("Dongguan");
//        returnInfo.setCompanyName("4PX");
//        returnInfo.setName("chenxuli");
//        returnInfo.setProvinceName("Guangdong Province");
//        returnInfo.setTelNumber("17191087538");
//        returnInfo.setEmail("dhphoto@aliyun.com");
//        returnInfo.setCountry("CN");
//        returnInfo.setZipCode("523000");
//
//        LogisticsProductVO logisticsProductVO = new LogisticsProductVO();
//        logisticsProductVO.setId("1005005951455393");
//        logisticsProductVO.setSkuId("1005005616949322");
//        logisticsProductVO.setEnglishUsage("materi");
//        logisticsProductVO.setDeclareChineseName("物流");
//        logisticsProductVO.setDeclareEnglishName("mta");
//        logisticsProductVO.setDestDeclarePrice(new BigDecimal("197.07"));
//        logisticsProductVO.setWeight(1);
//        logisticsProductVO.setQuantity(2);
//        logisticsProductVO.setSourceCountry("CN");
//        logisticsProductVO.setIsElectric(false);
//        logisticsProductVO.setDeclarePrice(BigDecimal.valueOf(2));
//        logisticsProductVO.setDestDeclarePrice(BigDecimal.valueOf(2));
//        logisticsProductVO.setChildOrderId(8184516086176025L);
//        logisticsProductVO.setScItemCode("");
////        logisticsProductVO.setScItemId(40414943126L);
////        logisticsProductVO.setScItemName("");
////        logisticsProductVO.setSkuCode("L083GBB1");
////        logisticsProductVO.setSkuName("A018GBB1");
//
//        LogisticsSaleChannelEntity logisticsSaleChannel = new LogisticsSaleChannelEntity();
//        logisticsSaleChannel.setCode("CAINIAO_STANDARD_FPXQZ");
//        logisticsSaleChannel.setShipmentMethod("Express-Post");
//        logisticsSaleChannel.setPlatformChannelId("11169435");
//        logisticsSaleChannel.setSupplierName("CAINIAONNRM");
//        LogisticsChannelEntity logisticsChannel = new LogisticsChannelEntity();
////        logisticsChannel.setCode("S832");
//        logisticsChannel.setId("1724614809662599171");
//        //DDU/DDP
//        logisticsChannel.setTaxModel("DDU");
//        final LogisticsOrderVO logisticsOrderVO = LogisticsOrderVO.builder()
//                .authMap(authMap)
//                .orderSource("ERP")
//                .pickupType("DOOR_PICKUP")
////                .pickupType("SELF_SEND")
////                .pickupType("SELF_POST")
////                .facility("can")
//                .deliveryNo("8184516086166025")
////                .deliveryNo("1102175972276889")
//                .receiverInfoVO(ReceiverInfoVO.builder()
//                        .streetAddress("Calle Alcatraz 244, Fraccionamiento Vistas de Tesistán, 45200 Zapopan, J")
//                        .email("965656546@qq.con")
//                        .city("Uskudar")
//                        .name("tr1011044319")
//                        .companyName("tr1011044319")
//                        .contact("zhang san")
//                        .country("US")
//                        .zipCode("CV56DY")
//                        .province("Istanbul")
//                        .telNumber("1234567890")
//                        .build())
//                .senderInfo(senderInfo)
//                .pickUpInfo(pickUp)
//                .returnInfo(returnInfo)
//                .parceInfoVO(ParceInfoVO.builder()
//                        .currency("CNY")
//                        .height(1)
//                        .hasBattery(true)
//                        .totalPrice(new BigDecimal("394.14"))
//                        .totalQuantity(1)
//                        .totalWeight(1)
//                        .length(1)
//                        .totalWeight(1)
//                        .width(1)
//                        .build())
//                .logisticsProductVOList(Arrays.asList(
//                        logisticsProductVO
//                ))
//                .logisticsChannelEntity(logisticsChannel)
//                .logisticsSaleChannel(logisticsSaleChannel)
//                .topUserKey(TOP_USER_KEY)
//                .build();
//        String json = "{\"topUserKey\":\"229375667\",\"oaid\":\"keHTpgPCNYGL4H0mr9kBLQ\",\"orderSource\":\"soB2c\",\"orderType\":\"soB2c\",\"iossCode\":\"\",\"deliveryNo\":\"3038918675202664\",\"sourceId\":\"1815520780038172674\",\"trackNo\":\"\",\"receiverInfoVO\":{\"name\":\"Mahmutcem m\",\"contact\":\"Mahmut Cem Yıldırım\",\"email\":\"\",\"telNumber\":\"05423973327\",\"country\":\"TR\",\"province\":\"Mersin\",\"city\":\"Mezitli\",\"district\":\"\",\"streetAddress\":\"\",\"addressFirst\":\"YENİ MAH. 33195 SK. EFE KONUTLARI SİTESİ B BLOK NO: 30B Kat 7 no 14 mezitli/ mersin\",\"addressSecond\":\"\",\"zipCode\":\"34600\",\"actId\":\"1815520780843479042\",\"receiverTaxNo\":\"\"},\"senderInfo\":{\"id\":\"105689752\",\"name\":\"Chen Xuli\",\"type\":\"DELIVER\",\"companyName\":\"深圳市唯迹科技有限公司\",\"contact\":\"Chen Xuli\",\"email\":\"dhphoto@aliyun.com\",\"telNumber\":\"17191087538\",\"country\":\"China\",\"countryName\":\"China\",\"provinceName\":\"guang dong sheng\",\"cityName\":\"dong guan shi\",\"districtName\":\"tang sha zhen\",\"addressFirst\":\"Building 2, 3rd Floor, Huisheng Science and Technology Innovation Park, No. 24 Huanshi South Road\",\"addressSecond\":\"\",\"zipCode\":\"\"},\"returnInfo\":{\"id\":\"78840011\",\"name\":\"Chen Xuli【SMT4】\",\"type\":\"REFUND\",\"companyName\":\"深圳市唯迹科技有限公司\",\"contact\":\"Chen Xuli\",\"email\":\"dhphoto@aliyun.com\",\"telNumber\":\"17191087538\",\"country\":\"中国\",\"countryName\":\"中国\",\"provinceName\":\"广东省\",\"cityName\":\"东莞市\",\"districtName\":\"塘厦镇\",\"addressFirst\":\"Building 2, 3rd Floor, Huisheng Science and Technology Innovation Park, No. 24 Huanshi South Road\",\"addressSecond\":\"\",\"zipCode\":\"\"},\"parceInfoVO\":{\"hasBattery\":true,\"currency\":\"USD\",\"totalPrice\":8.98,\"totalQuantity\":2,\"totalWeight\":301,\"height\":23,\"width\":21,\"length\":22},\"logisticsProductVOList\":[{\"quantity\":1,\"weight\":129,\"grossWeight\":129,\"url\":\"\",\"isElectric\":true,\"onlyBattery\":false,\"isLiquid\":false,\"id\":\"1619182380155670530\",\"skuId\":\"4000262959653\",\"skuNo\":\"1672\",\"productProperty\":\"带电池\",\"productPropertyId\":\"1773576997822271492\",\"declareModel\":\"\",\"declareChineseName\":\"补光灯\",\"declareEnglishName\":\"Fill light\",\"customsCode\":\"9405429000\",\"declareUnit\":\"\",\"declareElement\":\"1|0|用于相机拍照补光照明|品牌:Ulanzi\",\"declareCurrency\":\"USD\",\"declareCurrencySymbol\":\"$\",\"destDeclarePrice\":2.98,\"destCurrency\":\"USD\",\"destCurrencySymbol\":\"$\",\"exemption\":\"\",\"sourceCargo\":\"\",\"sourceCountry\":\"\",\"combinationDeclareType\":\"split\",\"childOrderId\":3038918675222664},{\"quantity\":1,\"weight\":172,\"grossWeight\":172,\"url\":\"\",\"isElectric\":true,\"onlyBattery\":false,\"isLiquid\":false,\"id\":\"1809039744026320897\",\"skuId\":\"1005006924581539\",\"skuNo\":\"L042GBB1+3204\",\"productProperty\":\"带电池\",\"productPropertyId\":\"1773576997822271492\",\"declareModel\":\"\",\"declareChineseName\":\"运动相机兔笼带灯套装\",\"declareEnglishName\":\"Sports camera cage with light set\",\"customsCode\":\"\",\"declareUnit\":\"\",\"declareCurrency\":\"CNY\",\"declareCurrencySymbol\":\"¥\",\"destDeclarePrice\":6,\"destCurrency\":\"USD\",\"destCurrencySymbol\":\"$\",\"exemption\":\"\",\"sourceCargo\":\"\",\"sourceCountry\":\"\",\"combinationDeclareType\":\"split\",\"childOrderId\":3038918675212664}],\"authMap\":{\"clientId\":\"503630\",\"orderId\":\"3031189945277223\",\"childOrderId\":\"3031189945287223\",\"logisticsPlatform\":\"AliExpress\",\"clientSecret\":\"PxkJJ2fLGh5HcwzhUJp267lQSbkuAFRJ\",\"id\":\"1726867517643149314\",\"shopId\":\"1798236692721700865\",\"url\":\"https://api-sg.aliexpress.com\",\"token\":\"50000700423zHPZZqMly9iku4MQdb7h0hqR18ff9902ExugZffT7nzxEiFwFyWHdZKNC\"},\"logisticsChannelEntity\":{\"sourceId\":\"\",\"sourceType\":\"\",\"mainId\":\"1798534637274796033\",\"name\":\"无忧物流-递四方东莞仓-标准（不带电or带电）\",\"code\":\"CAINIAO_STANDARD_FPXDG\",\"effectiveTime\":\"预计16-21天送达\",\"effectiveTimeUnit\":\"day\",\"trackQueryMode\":\"track123\",\"paperSize\":\"100*100\",\"paperLength\":100,\"paperWidth\":100,\"sortingCode\":\"\",\"feeRule\":\"billingWeight\",\"maxCustomsAmount\":0,\"maxCustomsCurrency\":\"USD\",\"minCustomsAmount\":0,\"minCustomsCurrency\":\"USD\",\"maxWeight\":0,\"weightUnit\":\"g\",\"maxLength\":0,\"sizeUnit\":\"cm\",\"maxWidth\":0,\"maxHeight\":0,\"taxModel\":\"DDP\",\"isIossPrepay\":true,\"isApiSign\":false,\"isApiInsurance\":false,\"disabled\":false,\"syncSourceId\":\"1740637823646679044\",\"declareCodeType\":\"transportNo\",\"deliveryType\":\"DOOR_PICKUP\",\"id\":\"1798579692924571658\",\"createUserId\":\"1649319376794423298\",\"createUserName\":\"祝梦彬\",\"createTime\":1717649789000,\"updateUserId\":\"1549948476757303297\",\"updateUserName\":\"admin\",\"updateTime\":1719918537000,\"version\":2,\"isDeleted\":false},\"logisticsSaleChannel\":{\"platformChannelId\":\"CAINIAO_STANDARD_FPXDG\",\"cnName\":\"递四方东莞仓-标准\",\"enName\":\"\",\"code\":\"CAINIAO_STANDARD_FPXDG\",\"servicePlatform\":\"tms\",\"channelStatus\":0,\"supplierName\":\"菜鸟无忧物流-标准\",\"supplierCode\":\"\",\"shipmentMethod\":\"\",\"logisticsPlatform\":\"AliExpress\",\"sourceData\":\"\",\"isTrack\":true,\"aging\":\"预计16-21天送达\",\"overseasWarehouseId\":\"\",\"id\":\"1740637823646679044\",\"createUserId\":\"0\",\"createUserName\":\"system\",\"createTime\":1703835371000,\"updateUserId\":\"0\",\"updateUserName\":\"system\",\"updateTime\":1719906708000,\"version\":0,\"isDeleted\":false}}";
//        String json2 = "{\"topUserKey\":\"229375667\",\"oaid\":\"keHTpgPCNYGL4H0mr9kBLQ\",\"orderSource\":\"soB2c\",\"orderType\":\"soB2c\",\"iossCode\":\"\",\"deliveryNo\":\"3038918675202664\",\"sourceId\":\"1815520780038172674\",\"trackNo\":\"\",\"receiverInfoVO\":{\"name\":\"Mahmutcem m\",\"contact\":\"Mahmut Cem Yıldırım\",\"email\":\"\",\"telNumber\":\"05423973327\",\"country\":\"TR\",\"province\":\"Mersin\",\"city\":\"Mezitli\",\"district\":\"\",\"streetAddress\":\"YENİ MAH. 33195 SK. EFE KONUTLARI SİTESİ B BLOK NO: 30B Kat 7 no 14 mezitli/ mersin\",\"addressFirst\":\"YENİ MAH. 33195 SK. EFE KONUTLARI SİTESİ B BLOK NO: 30B Kat 7 no 14 mezitli/ mersin\",\"addressSecond\":\"\",\"zipCode\":\"34600\",\"actId\":\"1815520780843479042\",\"receiverTaxNo\":\"\"},\"senderInfo\":{\"id\":\"105689752\",\"name\":\"Chen Xuli\",\"type\":\"DELIVER\",\"companyName\":\"深圳市唯迹科技有限公司\",\"contact\":\"Chen Xuli\",\"email\":\"dhphoto@aliyun.com\",\"telNumber\":\"17191087538\",\"country\":\"China\",\"countryName\":\"China\",\"provinceName\":\"guang dong sheng\",\"cityName\":\"dong guan shi\",\"districtName\":\"tang sha zhen\",\"addressFirst\":\"Building 2, 3rd Floor, Huisheng Science and Technology Innovation Park, No. 24 Huanshi South Road\",\"addressSecond\":\"\",\"zipCode\":\"\"},\"returnInfo\":{\"id\":\"78840011\",\"name\":\"Chen Xuli【SMT4】\",\"type\":\"REFUND\",\"companyName\":\"深圳市唯迹科技有限公司\",\"contact\":\"Chen Xuli\",\"email\":\"dhphoto@aliyun.com\",\"telNumber\":\"17191087538\",\"country\":\"中国\",\"countryName\":\"中国\",\"provinceName\":\"广东省\",\"cityName\":\"东莞市\",\"districtName\":\"塘厦镇\",\"addressFirst\":\"Building 2, 3rd Floor, Huisheng Science and Technology Innovation Park, No. 24 Huanshi South Road\",\"addressSecond\":\"\",\"zipCode\":\"\"},\"parceInfoVO\":{\"hasBattery\":true,\"currency\":\"USD\",\"totalPrice\":8.98,\"totalQuantity\":2,\"totalWeight\":301,\"height\":23,\"width\":21,\"length\":22},\"logisticsProductVOList\":[{\"quantity\":1,\"weight\":129,\"grossWeight\":129,\"url\":\"\",\"isElectric\":true,\"onlyBattery\":false,\"isLiquid\":false,\"id\":\"1619182380155670530\",\"skuId\":\"4000262959653\",\"skuNo\":\"1672\",\"productProperty\":\"带电池\",\"productPropertyId\":\"1773576997822271492\",\"declareModel\":\"\",\"declareChineseName\":\"补光灯\",\"declareEnglishName\":\"Fill light\",\"customsCode\":\"9405429000\",\"declareUnit\":\"\",\"declareElement\":\"1|0|用于相机拍照补光照明|品牌:Ulanzi\",\"declareCurrency\":\"USD\",\"declareCurrencySymbol\":\"$\",\"destDeclarePrice\":2.98,\"destCurrency\":\"USD\",\"destCurrencySymbol\":\"$\",\"exemption\":\"\",\"sourceCargo\":\"\",\"sourceCountry\":\"\",\"combinationDeclareType\":\"split\",\"childOrderId\":3038918675222664},{\"quantity\":1,\"weight\":172,\"grossWeight\":172,\"url\":\"\",\"isElectric\":true,\"onlyBattery\":false,\"isLiquid\":false,\"id\":\"1809039744026320897\",\"skuId\":\"1005006924581539\",\"skuNo\":\"L042GBB1+3204\",\"productProperty\":\"带电池\",\"productPropertyId\":\"1773576997822271492\",\"declareModel\":\"\",\"declareChineseName\":\"运动相机兔笼带灯套装\",\"declareEnglishName\":\"Sports camera cage with light set\",\"customsCode\":\"\",\"declareUnit\":\"\",\"declareCurrency\":\"CNY\",\"declareCurrencySymbol\":\"¥\",\"destDeclarePrice\":6,\"destCurrency\":\"USD\",\"destCurrencySymbol\":\"$\",\"exemption\":\"\",\"sourceCargo\":\"\",\"sourceCountry\":\"\",\"combinationDeclareType\":\"split\",\"childOrderId\":3038918675212664}],\"authMap\":{\"clientId\":\"503630\",\"orderId\":\"3031189945277223\",\"childOrderId\":\"3031189945287223\",\"logisticsPlatform\":\"AliExpress\",\"clientSecret\":\"PxkJJ2fLGh5HcwzhUJp267lQSbkuAFRJ\",\"id\":\"1726867517643149314\",\"shopId\":\"1798236692721700865\",\"url\":\"https://api-sg.aliexpress.com\",\"token\":\"50000700423zHPZZqMly9iku4MQdb7h0hqR18ff9902ExugZffT7nzxEiFwFyWHdZKNC\"},\"logisticsChannelEntity\":{\"sourceId\":\"\",\"sourceType\":\"\",\"mainId\":\"1798534637274796033\",\"name\":\"无忧物流-递四方东莞仓-标准（不带电or带电）\",\"code\":\"CAINIAO_STANDARD_FPXDG\",\"effectiveTime\":\"预计16-21天送达\",\"effectiveTimeUnit\":\"day\",\"trackQueryMode\":\"track123\",\"paperSize\":\"100*100\",\"paperLength\":100,\"paperWidth\":100,\"sortingCode\":\"\",\"feeRule\":\"billingWeight\",\"maxCustomsAmount\":0,\"maxCustomsCurrency\":\"USD\",\"minCustomsAmount\":0,\"minCustomsCurrency\":\"USD\",\"maxWeight\":0,\"weightUnit\":\"g\",\"maxLength\":0,\"sizeUnit\":\"cm\",\"maxWidth\":0,\"maxHeight\":0,\"taxModel\":\"DDP\",\"isIossPrepay\":true,\"isApiSign\":false,\"isApiInsurance\":false,\"disabled\":false,\"syncSourceId\":\"1740637823646679044\",\"declareCodeType\":\"transportNo\",\"deliveryType\":\"DOOR_PICKUP\",\"id\":\"1798579692924571658\",\"createUserId\":\"1649319376794423298\",\"createUserName\":\"祝梦彬\",\"createTime\":1717649789000,\"updateUserId\":\"1549948476757303297\",\"updateUserName\":\"admin\",\"updateTime\":1719918537000,\"version\":2,\"isDeleted\":false},\"logisticsSaleChannel\":{\"platformChannelId\":\"CAINIAO_STANDARD_FPXDG\",\"cnName\":\"递四方东莞仓-标准\",\"enName\":\"\",\"code\":\"CAINIAO_STANDARD_FPXDG\",\"servicePlatform\":\"tms\",\"channelStatus\":0,\"supplierName\":\"菜鸟无忧物流-标准\",\"supplierCode\":\"\",\"shipmentMethod\":\"\",\"logisticsPlatform\":\"AliExpress\",\"sourceData\":\"\",\"isTrack\":true,\"aging\":\"预计16-21天送达\",\"overseasWarehouseId\":\"\",\"id\":\"1740637823646679044\",\"createUserId\":\"0\",\"createUserName\":\"system\",\"createTime\":1703835371000,\"updateUserId\":\"0\",\"updateUserName\":\"system\",\"updateTime\":1719906708000,\"version\":0,\"isDeleted\":false}}";
        String json3 ="{\"topUserKey\":\"229375667\",\"oaid\":\"rcEothkOPUG40TUgYYXQwg\",\"orderSource\":\"soB2c\",\"orderType\":\"selfAdd\",\"iossCode\":\"\",\"voecTaxNo\":\"\",\"country\":\"JP\",\"deliveryNo\":\"1114853774127971\",\"platformCode\":\"1114853774127971\",\"packageId\":\"\",\"deliveryType\":\"SELF_SEND\",\"sourceId\":\"1952540519916613633\",\"trackNo\":\"\",\"receiverInfoVO\":{\"name\":\"dirceushimizu300 user\",\"contact\":\"Shimizu Dirceu (ジルセウ シミズ)\",\"email\":\"\",\"telNumber\":\"08058057829\",\"country\":\"JP\",\"province\":\"Aichi ken\",\"city\":\"Toyota-shi\",\"district\":\"\",\"streetAddress\":\"Homigaoka\",\"addressFirst\":\"Homigaoka 5-1-1 Kodan 138-307\",\"addressSecond\":\"5-1-1 Kodan 138-307\",\"zipCode\":\"4700353\",\"actId\":\"1952326995114180610\",\"receiverTaxNo\":\"\"},\"senderInfo\":{\"id\":\"105689752\",\"name\":\"Xu Shuhao\",\"type\":\"DELIVER\",\"companyName\":\"深圳市唯迹科技有限公司\",\"contact\":\"Xu Shuhao\",\"email\":\"dhphoto@aliyun.com\",\"telNumber\":\"17191087538\",\"country\":\"China\",\"countryName\":\"China\",\"provinceName\":\"guang dong sheng\",\"cityName\":\"dong guan shi\",\"districtName\":\"tang sha zhen\",\"addressFirst\":\"Room 301, Building 2, Huisheng Science and Technology Park, No. 24, Huanshi South Road\",\"addressSecond\":\"\",\"zipCode\":\"\"},\"returnInfo\":{\"id\":\"79040021\",\"name\":\"徐书豪 SMT4\",\"type\":\"REFUND\",\"companyName\":\"深圳市唯迹科技有限公司\",\"contact\":\"徐书豪 SMT4\",\"email\":\"dhphoto@aliyun.com\",\"telNumber\":\"17191087538\",\"country\":\"中国\",\"countryName\":\"中国\",\"provinceName\":\"广东省\",\"cityName\":\"东莞市\",\"districtName\":\"塘厦镇\",\"addressFirst\":\"环市南路24号汇胜科创园2号楼301\",\"addressSecond\":\"\",\"zipCode\":\"\"},\"parceInfoVO\":{\"hasBattery\":false,\"currency\":\"USD\",\"totalPrice\":18.5882,\"totalQuantity\":2,\"totalWeight\":1517,\"height\":25,\"width\":19,\"length\":25},\"logisticsProductVOList\":[{\"quantity\":1,\"weight\":305,\"grossWeight\":305,\"url\":\"group1/M00/00/64/rBBkCmU2UZCAS_r-AAD-OienrV4724.jpg\",\"isElectric\":false,\"onlyBattery\":false,\"isLiquid\":false,\"id\":\"1714611767138586625\",\"platformLineNumber\":\"\",\"skuId\":\"1005007039162519\",\"skuNo\":\"L079CNA1+L008+P014\",\"productProperty\":\"\",\"productPropertyId\":\"\",\"declareModel\":\"\",\"declareChineseName\":\"反光罩\",\"declareEnglishName\":\"Reflective cover\",\"customsCode\":\"9405990000\",\"declareUnit\":\"\",\"declareCurrency\":\"\",\"declareCurrencySymbol\":\"\",\"destDeclarePrice\":2.18,\"destCurrency\":\"USD\",\"destCurrencySymbol\":\"$\",\"exemption\":\"\",\"sourceCargo\":\"\",\"sourceCountry\":\"\",\"combinationDeclareType\":\"split\",\"childOrderId\":1114853774137971,\"price\":5.8877,\"currency\":\"USD\"},{\"quantity\":1,\"weight\":1212,\"grossWeight\":1212,\"url\":\"group1/M00/00/DE/rBBkCmXYC8OANsUqABKaRVtNBos345.png\",\"isElectric\":false,\"onlyBattery\":false,\"isLiquid\":false,\"id\":\"1760862126286573571\",\"platformLineNumber\":\"\",\"skuId\":\"1005007039162519\",\"skuNo\":\"L079CNA1+L008+P014\",\"productProperty\":\"\",\"productPropertyId\":\"\",\"declareModel\":\"\",\"declareChineseName\":\"电源适配器\",\"declareEnglishName\":\"The power adapter\",\"declarePrice\":183.9,\"customsCode\":\"8504409999\",\"declareUnit\":\"\",\"declareCurrency\":\"CNY\",\"declareCurrencySymbol\":\"¥\",\"destDeclarePrice\":16.4082,\"destCurrency\":\"USD\",\"destCurrencySymbol\":\"$\",\"exemption\":\"\",\"sourceCargo\":\"\",\"sourceCountry\":\"\",\"combinationDeclareType\":\"split\",\"childOrderId\":1114853774137971,\"price\":53.3347,\"currency\":\"USD\"}],\"authMap\":{\"clientId\":\"503630\",\"orderId\":\"3031189945277223\",\"childOrderId\":\"3031189945287223\",\"logisticsPlatform\":\"AliExpress\",\"clientSecret\":\"PxkJJ2fLGh5HcwzhUJp267lQSbkuAFRJ\",\"id\":\"1726867517643149314\",\"shopId\":\"1798236692721700865\",\"url\":\"https://api-sg.aliexpress.com\",\"token\":\"50000701530cnHtbirhzrd7ijPeou2emiSIajwwCg118389611msriNQyROLyoVZ1y1j\"},\"logisticsChannelEntity\":{\"sourceId\":\"\",\"sourceType\":\"\",\"mainId\":\"1798534637274796033\",\"name\":\"无忧物流-标准-递四方香港仓-标准\",\"code\":\"CAINIAO_STANDARD_FPXXG\",\"effectiveTime\":\"预计15-20天送达\",\"effectiveTimeUnit\":\"day\",\"trackQueryMode\":\"TRACK123\",\"paperSize\":\"100*100\",\"paperLength\":100,\"paperWidth\":100,\"sortingCode\":\"\",\"feeRule\":\"billingWeight\",\"maxCustomsAmount\":0,\"maxCustomsCurrency\":\"USD\",\"minCustomsAmount\":0,\"minCustomsCurrency\":\"USD\",\"maxWeight\":0,\"weightUnit\":\"g\",\"maxLength\":0,\"sizeUnit\":\"cm\",\"maxWidth\":0,\"maxHeight\":0,\"taxModel\":\"DDU\",\"isIossPrepay\":true,\"isApiSign\":false,\"isApiInsurance\":false,\"disabled\":false,\"syncSourceId\":\"1740637824183549955\",\"declareCodeType\":\"trackNo\",\"deliveryType\":\"SELF_SEND\",\"undeliverableDecision\":\"destroy\",\"trackQueryType\":\"transportNo\",\"volumeSetting\":0,\"shipmentOverLimitRate\":0,\"isPlatformShip\":false,\"carrierType\":\"\",\"isSendInvoice\":false,\"id\":\"1798579692899405828\",\"createUserId\":\"1649319376794423298\",\"createUserName\":\"祝梦彬\",\"createTime\":1717649789000,\"updateUserId\":\"45\",\"updateUserName\":\"吴俊鑫\",\"updateTime\":1753407982000,\"version\":5,\"isDeleted\":false},\"logisticsSaleChannel\":{\"platformChannelId\":\"CAINIAO_STANDARD_FPXXG\",\"cnName\":\"递四方香港仓-标准\",\"enName\":\"\",\"code\":\"CAINIAO_STANDARD_FPXXG\",\"servicePlatform\":\"tms\",\"channelStatus\":0,\"supplierName\":\"菜鸟无忧物流-标准\",\"supplierCode\":\"\",\"shipmentMethod\":\"\",\"logisticsPlatform\":\"AliExpress\",\"sourceData\":\"\",\"isTrack\":true,\"aging\":\"预计17-20天送达\",\"overseasWarehouseId\":\"\",\"carrierType\":\"\",\"id\":\"1740637824183549955\",\"createUserId\":\"0\",\"createUserName\":\"system\",\"createTime\":1703835371000,\"updateUserId\":\"0\",\"updateUserName\":\"system\",\"updateTime\":1753439162000,\"version\":0,\"isDeleted\":false}}";
        LogisticsOrderVO logisticsOrderVO = JSON.parseObject(json3, LogisticsOrderVO.class);
        ApiResult<LogisticsOrderResponseVO> order = aliExpressLogisticsHandler.createOrder(logisticsOrderVO);
        System.out.println(order);
    }

    @Test
    public void queryOrderList(){
        LogisticsQueryBaseVO logisticsQueryVOList = new LogisticsQueryBaseVO();
        logisticsQueryVOList.setDeliveryNo("8189874257569996");
        logisticsQueryVOList.setTransportNo("LP00662249706638");
        logisticsQueryVOList.setAuthMap(authMap);
        ApiResult<List<LogisticsOrderResponseVO>> listApiResult = aliExpressLogisticsHandler.queryOrderList(Collections.singletonList(logisticsQueryVOList));
        System.out.println("结果输出");
        System.out.println(JSON.toJSONString(listApiResult));
    }

    /**
     * 获取面签
     * @throws IOException
     */
    @Test
    public void getLabelList() throws IOException {
        LogisticsGetLabelVO logisticsQueryVO2 = new LogisticsGetLabelVO();
        logisticsQueryVO2.setDeliveryNo("3040480645907417");
        logisticsQueryVO2.setTransportNo("CNG00673225193224");
        logisticsQueryVO2.setTrackNo("LP00673225193224");
//        logisticsQueryVO2.setTransportNo("PQ936A0792035050134690Z ");
        logisticsQueryVO2.setAuthMap(authMap);
        logisticsQueryVO2.setLabelType("1");
        ApiResult<List<LogisticsPrintLabelResponse>> labelList = aliExpressLogisticsHandler.getLabelList(Collections.singletonList(logisticsQueryVO2));
        System.out.println(labelList);
    }
    @Test
    public void authorization() {
    }

    /**
     * 订单可发仓库列表
     * @throws ApiException
     */
    @Test
    public void getLogisticsService() throws ApiException {
        QueryLogisticsRequest queryLogisticsRequest =  QueryLogisticsRequest.builder()
                .order_id(8183524352516601L)
                .goods_weight("1")
                .goods_height(1L)
                .goods_width(1L)
                .goods_length(1L)
//                .order_id(1102175972276889L)
                .build();
        QueryLogisticsRequest queryLogisticsRequest1 =  QueryLogisticsRequest.builder()
                .order_id(8183524352506601L)
                .goods_weight("1")
                .goods_height(1L)
                .goods_width(1L)
                .goods_length(1L)
                .sub_order_list(Collections.singletonList(queryLogisticsRequest))
                .build();
        IopResponse logisticsService = aliExpressShipperService.getLogisticsService(authMap, queryLogisticsRequest1);
        System.out.println(logisticsService);
    }

    /**
     * 订单列表
     * @throws com.erp.oms.aliexpress.util.ApiException
     */
    @Test
    public void getOrderList() throws com.erp.oms.aliexpress.util.ApiException {
        String apiName = AliexpressConstants.LIST_ORDER;
        OrderRequest orderRequest = OrderRequest.builder().
                clientId(authMap.get("clientId")).
                clientSecret(authMap.get("clientSecret")).
                startTime("2024-02-18 00:00:00").
                endTime("2024-02-20 00:00:00").
                baseUrl(authMap.get("url")).
                apiName(apiName).
                currentPage(1).
                token(authMap.get("token")).build();
        List<AliExpressOrder > orderList = new ArrayList<>();
        aliExpressOrderService.listOrder(orderRequest, orderList);
        System.out.println("订单列表");
        System.out.println(JSON.toJSONString(orderList));
    }

    /**
     * 卖家信息
     */
    @Test
    public void getSellerInfo() throws ApiException {
        IopResponse sellerInfo = aliExpressShipperService.getSellerInfo(authMap);
        System.out.println(sellerInfo);
    }

    /**
     * 卖家地址信息
     */
    @Test
    public void getLogisticsAddress() throws ApiException {
        IopResponse sellerInfo = aliExpressShipperService.getLogisticsAddress(authMap);
        SellerResponse responseMsg = JSON.parseObject(sellerInfo.getBody(), SellerResponse.class);
        List<Address> senders = responseMsg.getSenders();
        List<Address> pickups = responseMsg.getPickups();
        List<Address> refunds = responseMsg.getRefunds();
        if (CollectionUtils.isNotEmpty(senders)){
            List<LogisticsAddressEntity> addressEntities = LogisticsAddressConverter.INSTANCE.sellerAddressToLogisticsAddress(senders);
            addressEntities.forEach(sender ->{
                sender.setType(LogisticsAddressTypeEnum.DELIVER);
            });
            logisticsAddressService.saveBatch(addressEntities);
        }
        if (CollectionUtils.isNotEmpty(pickups)){
            List<LogisticsAddressEntity> addressEntities = LogisticsAddressConverter.INSTANCE.sellerAddressToLogisticsAddress(pickups);
            addressEntities.forEach(sender ->{
                sender.setType(LogisticsAddressTypeEnum.COLLECT);
            });
            logisticsAddressService.saveBatch(addressEntities);
        }
        if (CollectionUtils.isNotEmpty(refunds)){
            List<LogisticsAddressEntity> addressEntities = LogisticsAddressConverter.INSTANCE.sellerAddressToLogisticsAddress(refunds);
            addressEntities.forEach(sender ->{
                sender.setType(LogisticsAddressTypeEnum.REFUND);
            });
            logisticsAddressService.saveBatch(addressEntities);
        }
        System.out.println(responseMsg);
    }
}
