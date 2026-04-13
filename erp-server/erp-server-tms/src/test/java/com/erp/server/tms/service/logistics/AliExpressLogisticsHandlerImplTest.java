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
        String CLIENT_CODE = "502978";
        String CHECK_WORD = "DfFGCAXMY7pptKfhz7IkWEa0zC0xddhY";
        String token = "50000700423zHPZZqMly9iku4MQdb7h0hqR18ff9902ExugZffT7nzxEiFwFyWHdZKNC";
        //prod
//        String CLIENT_CODE = "503630";
//        String CHECK_WORD = "PxkJJ2fLGh5HcwzhUJp267lQSbkuAFRJ";
//        String token = "50000800908t9Fq5iBwW1439a24doPZ9ZGVgpSEzOdcggU6LU0CiIxk2Fx9t8syRFp3v";
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
        String json3 = "{\"topUserKey\":\"226879141\",\"oaid\":\"8%2BrHMK4XNCGXEM%2B8kVlaCA\",\"orderSource\":\"soB2c\",\"orderType\":\"soB2c\",\"iossCode\":\"\",\"voecTaxNo\":\"\",\"country\":\"BR\",\"deliveryNo\":\"8210674211763627\",\"salesPlatformName\":\"速卖通\",\"platformCode\":\"8210674211763627\",\"dictPlatform\":\"AliExpress\",\"packageId\":\"\",\"deliveryType\":\"DOOR_PICKUP\",\"sourceId\":\"2039519105673355265\",\"companyName\":\"深圳市十二篮子电商有限公司\",\"usciCode\":\"914403003592776632\",\"trackNo\":\"\",\"receiverInfoVO\":{\"name\":\"Flavio Fachel\",\"contact\":\"Flavio Lampert Fachel\",\"email\":\"\",\"telNumber\":\"21998890471\",\"country\":\"BR\",\"province\":\"Rio de Janeiro\",\"city\":\"Rio de Janeiro\",\"district\":\"\",\"streetAddress\":\"Barra da Tijuca;Rua Mário Autuori;245\",\"addressFirst\":\"Barra da Tijuca;Rua Mário Autuori;245 Casa\",\"addressSecond\":\"Casa\",\"zipCode\":\"22793-276\",\"actId\":\"2039519108957495298\",\"receiverTaxNo\":\"47477105072\"},\"senderInfo\":{\"id\":\"105702038\",\"name\":\"Chenxuli\",\"type\":\"DELIVER\",\"companyName\":\"深圳市唯迹科技有限公司\",\"contact\":\"Chenxuli\",\"email\":\"dhphoto@aliyun.com\",\"telNumber\":\"17191087538\",\"country\":\"China\",\"countryName\":\"China\",\"provinceName\":\"guang dong sheng\",\"cityName\":\"dong guan shi\",\"districtName\":\"tang sha zhen\",\"addressFirst\":\" No. 24, Huanshi South Road, Huisheng Science and Technology Park, Building 2, 3rd Floor\",\"addressSecond\":\"\",\"zipCode\":\"\"},\"returnInfo\":{\"id\":\"59520550\",\"name\":\"媛媛SMT2-SMT2\",\"type\":\"REFUND\",\"companyName\":\"深圳市唯迹科技有限公司\",\"contact\":\"媛媛SMT2\",\"email\":\"dhphoto@aliyun.com\",\"telNumber\":\"17191087538\",\"country\":\"中国\",\"countryName\":\"中国\",\"provinceName\":\"广东省\",\"cityName\":\"东莞市\",\"districtName\":\"塘厦镇\",\"addressFirst\":\"环市南路24号汇胜科创园2号楼301\",\"addressSecond\":\"\",\"zipCode\":\"\"},\"parceInfoVO\":{\"hasBattery\":false,\"currency\":\"USD\",\"totalPrice\":9.6061,\"totalQuantity\":2,\"totalWeight\":214,\"height\":5,\"width\":6,\"length\":9},\"logisticsProductVOList\":[{\"quantity\":1,\"weight\":116,\"grossWeight\":116,\"url\":\"group1/M00/DC/77/rBBkCmj3QJ6AELkxAAP8ghxEL5Y876.png\",\"isElectric\":false,\"onlyBattery\":false,\"isLiquid\":false,\"id\":\"1920784158572068866\",\"platformLineNumber\":\"\",\"skuId\":\"1005010588922949\",\"skuNo\":\"S042\",\"productProperty\":\"普货\",\"productPropertyId\":\"1580456666541006850\",\"declareModel\":\"\",\"declareChineseName\":\"手机夹\",\"declareEnglishName\":\"Phone Clip\",\"declarePrice\":50.24,\"customsCode\":\"7616999000\",\"declareUnit\":\"个\",\"declareElement\":\"1|0|用途:用于手机固定，非工业用|铝合金+不锈钢+硅胶|||品牌:ULANZI\",\"declareCurrency\":\"CNY\",\"declareCurrencySymbol\":\"¥\",\"destDeclarePrice\":5.0408,\"destCurrency\":\"USD\",\"destCurrencySymbol\":\"$\",\"exemption\":\"\",\"sourceCargo\":\"\",\"sourceCountry\":\"\",\"combinationDeclareType\":\"split\",\"childOrderId\":8210674211773627,\"price\":29.39,\"currency\":\"USD\"},{\"quantity\":1,\"weight\":98,\"grossWeight\":98,\"url\":\"group1/M00/26/F8/rBBkCmjKHwqAFw3-AAHuvMtKZlo501.png\",\"isElectric\":false,\"onlyBattery\":false,\"isLiquid\":false,\"id\":\"1913042353713672194\",\"platformLineNumber\":\"\",\"skuId\":\"1005010588922949\",\"skuNo\":\"S028\",\"productProperty\":\"普货\",\"productPropertyId\":\"1580456666541006850\",\"declareModel\":\"\",\"declareChineseName\":\"支架\",\"declareEnglishName\":\"phone stand\",\"declarePrice\":45.5,\"customsCode\":\"7616999000\",\"declareUnit\":\"个\",\"declareElement\":\"1|0|用于固定手机拍摄，非工业用|铝合金+不锈钢+硅胶|||品牌:ULANZI\",\"declareCurrency\":\"CNY\",\"declareCurrencySymbol\":\"¥\",\"destDeclarePrice\":4.5653,\"destCurrency\":\"USD\",\"destCurrencySymbol\":\"$\",\"exemption\":\"\",\"sourceCargo\":\"\",\"sourceCountry\":\"\",\"combinationDeclareType\":\"split\",\"childOrderId\":8210674211783627,\"price\":26.87,\"currency\":\"USD\"}],\"authMap\":{\"clientId\":\"503630\",\"orderId\":\"3031189945277223\",\"childOrderId\":\"3031189945287223\",\"logisticsPlatform\":\"AliExpress\",\"clientSecret\":\"PxkJJ2fLGh5HcwzhUJp267lQSbkuAFRJ\",\"id\":\"1726867517643149314\",\"shopId\":\"1798238896438382594\",\"url\":\"https://api-sg.aliexpress.com\",\"token\":\"50000101907gWRZqpeb1df3be2fcDudoy1hglMJZlvTIe2iquANqxTPRxZtjHNN3nvgw\"},\"logisticsChannelEntity\":{\"sourceId\":\"\",\"sourceType\":\"\",\"mainId\":\"1798534637274796033\",\"name\":\"菜鸟无忧物流-标准--递四方东莞仓-标准\",\"code\":\"CAINIAO_STANDARD_FPXDG\",\"effectiveTime\":\"预计15-20天送达\",\"effectiveTimeUnit\":\"day\",\"trackQueryMode\":\"TRACK123\",\"paperSize\":\"100*100\",\"paperLength\":100,\"paperWidth\":100,\"sortingCode\":\"\",\"feeRule\":\"billingWeight\",\"maxCustomsAmount\":0,\"maxCustomsCurrency\":\"USD\",\"minCustomsAmount\":0,\"minCustomsCurrency\":\"USD\",\"maxWeight\":0,\"weightUnit\":\"g\",\"maxLength\":0,\"sizeUnit\":\"cm\",\"maxWidth\":0,\"maxHeight\":0,\"taxModel\":\"DDU\",\"isIossPrepay\":true,\"isApiSign\":false,\"isApiInsurance\":false,\"disabled\":false,\"syncSourceId\":\"1740637823646679044\",\"declareCodeType\":\"transportNo\",\"deliveryType\":\"DOOR_PICKUP\",\"undeliverableDecision\":\"destroy\",\"trackQueryType\":\"transportNo\",\"volumeSetting\":6000,\"shipmentOverLimitRate\":0,\"isPlatformShip\":false,\"carrierType\":\"\",\"isSendInvoice\":false,\"lastMileCarrier\":\"\",\"channelType\":\"\",\"handoverDocType\":\"\",\"id\":\"1798579692924571658\",\"createUserId\":\"1649319376794423298\",\"createUserName\":\"祝梦彬\",\"createTime\":1717649789000,\"updateUserId\":\"1730158964328370178\",\"updateUserName\":\"刘锐鹏\",\"updateTime\":1770711232000,\"version\":6,\"isDeleted\":false,\"isUserSystem\":false},\"logisticsSaleChannel\":{\"platformChannelId\":\"CAINIAO_STANDARD_FPXDG\",\"cnName\":\"递四方东莞仓-标准\",\"enName\":\"\",\"code\":\"CAINIAO_STANDARD_FPXDG\",\"servicePlatform\":\"tms\",\"channelStatus\":0,\"supplierName\":\"无忧物流-标准\",\"supplierCode\":\"\",\"shipmentMethod\":\"\",\"logisticsPlatform\":\"AliExpress\",\"sourceData\":\"\",\"isTrack\":true,\"aging\":\"预计15-20天送达\",\"overseasWarehouseId\":\"\",\"carrierType\":\"\",\"channelType\":\"\",\"id\":\"1740637823646679044\",\"createUserId\":\"0\",\"createUserName\":\"system\",\"createTime\":1703835371000,\"updateUserId\":\"1730158964328370178\",\"updateUserName\":\"刘锐鹏\",\"updateTime\":1770711228000,\"version\":0,\"isDeleted\":false,\"isUserSystem\":false}}";

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
