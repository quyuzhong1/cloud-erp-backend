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
        Map<String, String> logisticsAuthConfig = yanWenLogisticsHandler.getLogisticsAuthConfigByShopId("");
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
        String json ="{\"oaid\":\"\",\"orderSource\":\"soB2c\",\"iossCode\":\"\",\"deliveryNo\":\"XSDS24050600003\",\"trackNo\":\"\",\"receiverInfoVO\":{\"name\":\"白木幸太\",\"contact\":\"白木幸太\",\"email\":\"\",\"telNumber\":\"09018257829\",\"country\":\"JP\",\"province\":\"北海道\",\"city\":\"札幌市\",\"streetAddress\":\"愛知県額田郡幸田町菱池蔵前162番地　ピアモント101号\",\"addressFirst\":\"愛知県額田郡幸田町菱池蔵前162番地　ピアモント101号\",\"addressSecond\":\"\",\"zipCode\":\"444-0113\",\"actId\":\"1787425729776979969\",\"receiverTaxNo\":\"\"},\"senderInfo\":{\"id\":\"\",\"name\":\"Tangxia Town, Dongguan City\",\"type\":\"DELIVER\",\"companyName\":\"SHENZHEN WEIJI TECHNOLOGY CO\",\"contact\":\"Chen Xuli\",\"email\":\"wupuming@ulanzi.cn\",\"telNumber\":\"18806656774\",\"country\":\"CN\",\"countryName\":\"中国\",\"provinceName\":\"Guangdong Province\",\"cityName\":\"Dongguan city\",\"districtName\":\"Tangxia town\",\"addressFirst\":\"DONGGUAN CITY TANGXIA TOWN FLOOR 9,BUILDING 205,NEW SUN CITY INDUSTRIAL PARK,LINCU\",\"addressSecond\":\"\",\"zipCode\":\"523000\"},\"returnInfo\":{\"id\":\"\",\"name\":\"Tangxia Town, Dongguan City\",\"type\":\"DELIVER\",\"companyName\":\"SHENZHEN WEIJI TECHNOLOGY CO\",\"contact\":\"Chen Xuli\",\"email\":\"wupuming@ulanzi.cn\",\"telNumber\":\"18806656774\",\"country\":\"CN\",\"countryName\":\"中国\",\"provinceName\":\"Guangdong Province\",\"cityName\":\"Dongguan city\",\"districtName\":\"Tangxia town\",\"addressFirst\":\"DONGGUAN CITY TANGXIA TOWN FLOOR 9,BUILDING 205,NEW SUN CITY INDUSTRIAL PARK,LINCU\",\"addressSecond\":\"\",\"zipCode\":\"523000\"},\"parceInfoVO\":{\"hasBattery\":false,\"currency\":\"JPY\",\"totalPrice\":5,\"totalQuantity\":1,\"totalWeight\":1,\"height\":0,\"width\":0,\"length\":0},\"logisticsProductVOList\":[{\"quantity\":1,\"weight\":25,\"grossWeight\":25,\"isElectric\":false,\"skuId\":\"1619215509440434177\",\"skuNo\":\"A001\",\"productPropertyId\":\"\",\"declareModel\":\"\",\"declareChineseName\":\"摄影配件\",\"declareEnglishName\":\"Photography accessories\",\"declarePrice\":0,\"customsCode\":\"\",\"declareUnit\":\"\",\"declareCurrency\":\"USD\",\"declareCurrencySymbol\":\"$\",\"destDeclarePrice\":5,\"destCurrency\":\"USD\",\"destCurrencySymbol\":\"$\",\"exemption\":\"\",\"sourceCargo\":\"\",\"sourceCountry\":\"\",\"combinationDeclareType\":\"split\"}],\"authMap\":{\"clientId\":\"30114979\",\"logisticsPlatform\":\"YanWen\",\"clientSecret\":\"62854821B3B261983589ACFE94C7D652\",\"id\":\"1770279035289473026\",\"url\":\"Https://open.yw56.com.cn\",\"token\":\"Atza|IwEBIBJ1mBce_uo4-M5jwDn7AdGzGP4NeVImqYAdC-Q_42S9XZynV4ANB9TQz5foRn6P4FOXbDJxnXKtZb0tAbukWdSsnMlvbRDO_1YWf7NJR-fiLpIXW5uSxLsMzPdTb-ICQqK-Me__SpZPlB-9lhScIR-gpMVky2-U0UmThMiGeufmPcJcIpnUbo1iIM9KAM4JLkGlIB3kiJm__E8ID9u8m5c6OurHDO46stTqGFOKXvsyz4yVBn7uNXvPqyDPeD2VmJfhH2oxfjf6gSmhPoE4DRJV4OTgoDJdAFWHBV5IIUNNj4XxPHfZAK8on54iQg6BIr4\"},\"logisticsChannelEntity\":{\"sourceId\":\"\",\"sourceType\":\"\",\"mainId\":\"1770278952271613954\",\"name\":\"燕文专线追踪-普货\",\"code\":\"481\",\"effectiveTime\":\"0\",\"effectiveTimeUnit\":\"day\",\"trackQueryMode\":\"track123\",\"paperSize\":\"100*100\",\"paperLength\":100,\"paperWidth\":100,\"sortingCode\":\"\",\"feeRule\":\"billingWeight\",\"maxCustomsAmount\":0,\"maxCustomsCurrency\":\"USD\",\"minCustomsAmount\":0,\"minCustomsCurrency\":\"USD\",\"maxWeight\":0,\"weightUnit\":\"g\",\"maxLength\":0,\"sizeUnit\":\"cm\",\"maxWidth\":0,\"maxHeight\":0,\"taxModel\":\"DDP\",\"isIossPrepay\":false,\"isApiSign\":false,\"isApiInsurance\":false,\"disabled\":false,\"syncSourceId\":\"1770279040544935937\",\"id\":\"1770304369082765320\",\"createUserId\":\"1676946232032890881\",\"createUserName\":\"张阳新\",\"createTime\":1710908426911,\"updateUserId\":\"1676946232032890881\",\"updateUserName\":\"张阳新\",\"updateTime\":1714307255399,\"version\":5,\"isDeleted\":false},\"logisticsSaleChannel\":{\"platformChannelId\":\"481\",\"cnName\":\"燕文专线追踪-普货\",\"enName\":\"Direct Line Tracked Packet-P\",\"code\":\"481\",\"channelStatus\":0,\"supplierName\":\"\",\"supplierCode\":\"\",\"shipmentMethod\":\"\",\"logisticsPlatform\":\"YanWen\",\"sourceData\":\"\",\"isTrack\":true,\"overseasWarehouseId\":\"\",\"id\":\"1770279040544935937\",\"createUserId\":\"0\",\"createUserName\":\"system\",\"createTime\":1710902388117,\"updateUserId\":\"0\",\"updateUserName\":\"system\",\"updateTime\":1710902388117,\"version\":0,\"isDeleted\":false}}";
                LogisticsOrderVO logisticsOrderVO = JSON.parseObject(json,new TypeReference<LogisticsOrderVO>() {}.getType());
        logisticsOrderVO.setAuthMap(authMap);
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
        ApiResult<Object>ApiResult<Object>= yanWenLogisticsHandler.authorization(authMap);
        System.out.println(apiResult);
    }
}