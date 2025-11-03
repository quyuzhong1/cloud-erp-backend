package com.erp.server.oms.rocketmq;

import com.alibaba.fastjson.JSONObject;
import com.common.message.service.mq.MQProducerService;
import com.erp.server.oms.ErpServerOmsApplication;
import com.erp.server.oms.rocketmq.consumer.PlatformOrderConsumerService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Profile;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {ErpServerOmsApplication.class})
@TestPropertySource(properties = {"spring.cloud.nacos.discovery.namespace=dev"})
@Profile("dev")
public class PlatformOrderConsumerServiceTest {

    @Resource
    private PlatformOrderConsumerService platformOrderConsumerService;
    @Resource
    private MQProducerService mqProducerService;

    @Test
    public void handle() {
//        String data = "{\"sourceId\":\"i71a8f09ca68e9df7b9d8cc5a7bd4187c\",\"payTime\":\"2025-09-18 21:04:26\",\"isIntercept\":false,\"approveStatusStr\":\"approve\",\"buyerRemark\":\"\",\"sourceCode\":\"3893097985\",\"dmpOutputTaskRecordDataId\":\"1173627618071949313\",\"payAmount\":\"4610.0000\",\"exchangeRate\":\"0.0000\",\"dmpOutputTaskRecordId\":\"1173636063223361536\",\"afterTaxAmount\":\"0.0000\",\"details\":\"[{\\\"platform_sku_no\\\":\\\"2882\\\",\\\"price\\\":\\\"4610.0000\\\",\\\"qty\\\":1}]\",\"returnDTOList\":\"\",\"shopId\":\"1970735242112196610\",\"orgName\":\"\",\"receiver\":\"{\\\"receiver_tax_no\\\":\\\"\\\",\\\"country\\\":\\\"\\\",\\\"district_name\\\":\\\"\\\",\\\"street_address\\\":\\\"\\\",\\\"city_name\\\":\\\"\\\",\\\"first_address\\\":\\\"\\\",\\\"post_code\\\":\\\"\\\",\\\"name\\\":\\\"Wildberries\\\",\\\"country_name\\\":\\\"\\\",\\\"receiver_name\\\":\\\"Wildberries\\\",\\\"province_name\\\":\\\"\\\"}\",\"abnormalType\":\"\",\"extendData\":\"{\\\"office_id\\\":\\\"10105\\\",\\\"order_uid\\\":\\\"i71a8f09ca68e9df7b9d8cc5a7bd4187c\\\",\\\"supply_id\\\":\\\"WB-GI-182686515\\\",\\\"chrt_id\\\":\\\"728982504\\\",\\\"warehouse_id\\\":\\\"1515262\\\"}\",\"totalCancelGoodsAmount\":\"0.0000\",\"deliveryDTOList\":\"\",\"billDate\":\"2025-09-18 21:04:26\",\"extend\":\"\",\"shippingFee\":\"0.0000\",\"platformOrderCreateTime\":\"2025-09-18 21:04:26\",\"sourceType\":\"soB2c\",\"syncOperate\":\"\",\"finances\":\"\",\"platformCode\":\"3893097985\",\"labelJson\":\"{\\\"delivery_type\\\":\\\"fbs\\\"}\",\"thirdSystem\":\"Wildberries\",\"remark\":\"\",\"orgId\":\"\",\"sellerOrderCode\":\"\",\"billStatus\":\"shipped\",\"currency\":\"643\",\"syncKingdeeTime\":null,\"platformOrderStatus\":\"fbs\",\"logisticsList\":\"\",\"thirdCode\":\"wildberries\",\"invalidType\":\"\",\"amount\":\"4610.0000\",\"isCancel\":false,\"cancelGoodsCurrency\":\"\",\"invalidStatus\":false,\"dictPayMethod\":\"\",\"interceptRemark\":\"\",\"syncKingdeeId\":\"\",\"totalTaxFee\":\"0.0000\",\"nfeInvoiceStatus\":\"\",\"downloadTime\":null,\"totalDiscount\":\"0.0000\",\"dictPlatform\":\"wildberries\",\"syncKingdeeStatus\":\"\",\"downloadStatus\":\"\",\"invalidRemark\":\"\",\"payStatus\":\"paid\",\"refundDTOList\":\"\"}";
        String data = "{\"sourceId\":\"i71a8f09ca68e9df7b9d8cc5a7bd4187c\",\"payTime\":\"2025-09-18T21:04:26\",\"thirdSystem\":\"Wildberries\",\"isIntercept\":false,\"remark\":\"\",\"approveStatusStr\":\"approve\",\"buyerRemark\":\"\",\"sourceCode\":\"3893097985\",\"dmpOutputTaskRecordDataId\":\"1173667296154234881\",\"payAmount\":\"4610.0000\",\"exchangeRate\":0,\"dmpOutputTaskRecordId\":\"1173671627024773120\",\"billStatus\":\"shipped\",\"currency\":\"643\",\"afterTaxAmount\":0,\"details\":[{\"platform_sku_no\":\"2882\",\"price\":\"4610.0000\",\"qty\":1}],\"shopId\":\"1970735242112196610\",\"platformOrderStatus\":\"fbs\",\"thirdCode\":\"wildberries\",\"invalidType\":\"\",\"amount\":\"4610.0000\",\"isCancel\":false,\"invalidStatus\":false,\"receiver\":{\"receiver_tax_no\":\"\",\"country\":\"\",\"district_name\":\"\",\"street_address\":\"\",\"city_name\":\"\",\"first_address\":\"\",\"post_code\":\"\",\"name\":\"Wildberries\",\"country_name\":\"\",\"receiver_name\":\"Wildberries\",\"province_name\":\"\"},\"extendData\":\"{\\\"office_id\\\":\\\"10105\\\",\\\"order_uid\\\":\\\"i71a8f09ca68e9df7b9d8cc5a7bd4187c\\\",\\\"supply_id\\\":\\\"WB-GI-182686515\\\",\\\"chrt_id\\\":\\\"728982504\\\",\\\"warehouse_id\\\":\\\"1515262\\\"}\",\"totalCancelGoodsAmount\":0,\"billDate\":\"2025-09-18T21:04:26\",\"totalTaxFee\":0,\"shippingFee\":0,\"platformOrderCreateTime\":\"2025-09-18 21:04:26\",\"sourceType\":\"soB2c\",\"totalDiscount\":0,\"dictPlatform\":\"wildberries\",\"invalidRemark\":\"\",\"payStatus\":\"paid\",\"platformCode\":\"3893097985\",\"labelJson\":\"{\\\"delivery_type\\\":\\\"fbs\\\"}\"}";
        platformOrderConsumerService.handle(JSONObject.parse(data));
    }
}