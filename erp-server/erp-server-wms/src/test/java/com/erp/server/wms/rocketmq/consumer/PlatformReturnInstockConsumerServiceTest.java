package com.erp.server.wms.rocketmq.consumer;

import com.erp.server.wms.ErpServerWmsApplication;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {ErpServerWmsApplication.class})
public class PlatformReturnInstockConsumerServiceTest {

    @Resource
    private KingdeeB2CSoOutstockConsumer service;

    @Resource
    private RestCloudPlatformNewReturnInstockConsumerService restCloudPlatformNewReturnInstockConsumerService;

    /**
     * 携带退货物流单号：现已接入 {@code matchAndCreateByReturnLogisticCode}，会尝试按物流单号+SKU匹配
     * B2B/B2C售后单。若测试环境中该物流单号 46343175619-2 没有对应售后单数据，则等价于未匹配，
     * 明细会原样交给下一分支（参考单号为空，最终落到 {@code createSoReturnPrestockHeadless} 生成预入库单）；
     * 若测试环境中恰好存在对应售后单，则会生成《已审核-退货入库单》。两种走向都需要人工核对生成结果，
     * 本用例本身不做断言，仅作为可用性冒烟验证（依赖真实 Nacos/Redis/PostgreSQL 环境）。
     */
    @Test
    public void handleTest() {
        String json = "{\n" +
                "  \"sourceId\": \"\",\n" +
                "  \"platformReturnOrderNo\": \"WRI2025080100002\",\n" +
                "  \"reason\": \"\",\n" +
                "  \"putawayTime\": \"2025-08-01 09:57:07\",\n" +
                "  \"platform\": \"iml\",\n" +
                "  \"orderReferenceNo\": \"\",\n" +
                "  \"authId\": \"1976194554104430593\",\n" +
                "  \"warehouseCode\": \"\",\n" +
                "  \"dmpOutputTaskRecordDataId\": \"1180152941215232001\",\n" +
                "  \"createTime\": \"2025-10-27 11:47:32\",\n" +
                "  \"productDetailList\": [\n" +
                "    {\n" +
                "      \"realQty\": \"1\",\n" +
                "      \"productSku\": \"22298-GD-V1S\",\n" +
                "      \"mustQty\": \"1\",\n" +
                "      \"receiveQty\": \"1\"\n" +
                "    }\n" +
                "  ],\n" +
                "  \"platformOrderNo\": \"\",\n" +
                "  \"returnLogisticCode\": \"46343175619-2\",\n" +
                "  \"dmpOutputTaskRecordId\": \"1181655983894835200\",\n" +
                "  \"returnType\": \"claim\",\n" +
                "  \"status\": \"\"\n" +
                "}";
        restCloudPlatformNewReturnInstockConsumerService.handle(json);
    }

    /**
     * 退货物流单号在B2B/B2C售后单中均无命中：验证会整单回退，走既有生成预入库单逻辑，
     * 与未携带物流单号时行为一致（不会因为新增的匹配分支而抛异常或漏单）。
     */
    @Test
    public void handleReturnLogisticCodeNoMatchTest() {
        String json = "{\n" +
                "  \"sourceId\": \"\",\n" +
                "  \"platformReturnOrderNo\": \"WRI-LOGISTIC-NOMATCH-0001\",\n" +
                "  \"reason\": \"物流单号无匹配售后单-验证\",\n" +
                "  \"putawayTime\": \"2025-08-01 09:57:07\",\n" +
                "  \"platform\": \"iml\",\n" +
                "  \"orderReferenceNo\": \"\",\n" +
                "  \"authId\": \"1976194554104430593\",\n" +
                "  \"warehouseCode\": \"\",\n" +
                "  \"dmpOutputTaskRecordDataId\": \"1180152941215232003\",\n" +
                "  \"createTime\": \"2025-10-27 11:47:32\",\n" +
                "  \"productDetailList\": [\n" +
                "    {\n" +
                "      \"realQty\": \"1\",\n" +
                "      \"productSku\": \"22298-GD-V1S\",\n" +
                "      \"mustQty\": \"1\",\n" +
                "      \"receiveQty\": \"1\"\n" +
                "    }\n" +
                "  ],\n" +
                "  \"platformOrderNo\": \"\",\n" +
                "  \"returnLogisticCode\": \"NO-SUCH-LOGISTIC-CODE-000000\",\n" +
                "  \"dmpOutputTaskRecordId\": \"1181655983894835202\",\n" +
                "  \"returnType\": \"claim\",\n" +
                "  \"status\": \"\"\n" +
                "}";
        restCloudPlatformNewReturnInstockConsumerService.handle(json);
    }

    // 说明：完全匹配 / 部分匹配+剩余转预入库单 两种场景，需要测试环境中预先存在指定 returnLogisticCode
    // 且SKU一致的B2B或B2C售后单（so_return/so_b2c_return + 明细），本地无法在不连接真实测试库、
    // 不新增测试数据构造脚本的前提下稳定构造，因此本次未新增对应自动化用例，需人工在测试环境中验证：
    // 1) 造一条售后单退货数量为2、其中1件已生成过已审核退货入库单（剩余缺口1）；
    // 2) 推送本消息传入相同SKU、mustQty=2的物流单号退货入库消息；
    // 3) 核对生成的退货入库单明细mustQty=1（命中缺口部分），且剩余1件走预入库单无头件兜底。

    /**
     * 既无退货物流单号又无参考单号：验证生成预入库单（so_return_prestock）而非退货入库单，
     * 且重复推送同一条消息（同一 thirdCode/platformReturnOrderNo）不会重复建单。
     */
    @Test
    public void handleHeadlessTest() {
        String json = "{\n" +
                "  \"sourceId\": \"\",\n" +
                "  \"platformReturnOrderNo\": \"WRI-HEADLESS-TEST-0001\",\n" +
                "  \"reason\": \"无头件验证\",\n" +
                "  \"putawayTime\": \"2025-08-01 09:57:07\",\n" +
                "  \"platform\": \"iml\",\n" +
                "  \"orderReferenceNo\": \"\",\n" +
                "  \"authId\": \"1976194554104430593\",\n" +
                "  \"warehouseCode\": \"\",\n" +
                "  \"dmpOutputTaskRecordDataId\": \"1180152941215232002\",\n" +
                "  \"createTime\": \"2025-10-27 11:47:32\",\n" +
                "  \"productDetailList\": [\n" +
                "    {\n" +
                "      \"realQty\": \"1\",\n" +
                "      \"productSku\": \"22298-GD-V1S\",\n" +
                "      \"mustQty\": \"1\",\n" +
                "      \"receiveQty\": \"1\"\n" +
                "    }\n" +
                "  ],\n" +
                "  \"platformOrderNo\": \"\",\n" +
                "  \"returnLogisticCode\": \"\",\n" +
                "  \"dmpOutputTaskRecordId\": \"1181655983894835201\",\n" +
                "  \"returnType\": \"claim\",\n" +
                "  \"status\": \"\"\n" +
                "}";
        // 首次推送：预期生成 so_return_prestock 主表 + 明细
        restCloudPlatformNewReturnInstockConsumerService.handle(json);
        // 重复推送同一条消息：thirdCode 幂等应生效，不重复建单
        restCloudPlatformNewReturnInstockConsumerService.handle(json);
    }
}