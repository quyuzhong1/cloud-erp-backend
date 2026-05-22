package com.erp.server.wms.rocketmq.consumer;

import com.common.core.controller.vo.ApiResult;
import com.erp.model.oms.entity.SoB2cDetailEntity;
import com.erp.model.oms.entity.SoB2cEntity;
import com.erp.model.oms.entity.SoB2cLogisticsEntity;
import com.erp.model.wms.entity.SoOutstockEntity;
import com.erp.model.wms.entity.ThirdWarehouseDeliveryDetailEntity;
import com.erp.model.wms.entity.ThirdWarehouseDeliveryEntity;
import com.erp.model.wms.enums.SoB2cWarehouseDeliveryStatusEnum;
import com.erp.rpc.oms.feign.SoB2cFeign;
import com.erp.server.wms.ErpServerWmsApplication;
import com.erp.server.wms.service.SoOutstockService;
import com.erp.server.wms.service.ThirdWarehouseDeliveryDetailService;
import com.erp.server.wms.service.ThirdWarehouseDeliveryService;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * 手工 UAT 复现测试。
 *
 * 运行前请先把下面这些线上记录搬到 UAT，并保证引用的主数据在 UAT 可用：
 * 1. so_b2c
 * 2. so_b2c_detail
 * 3. so_b2c_receiver
 * 4. so_b2c_logistics
 * 5. third_warehouse_delivery
 * 6. third_warehouse_delivery_detail
 * 7. shop_info
 * 8. customer_info
 * 9. warehouse 及其库存组织主数据
 *
 * 如果要复现“生成销售出库单时被覆盖”的问题，不要带入这两个子单对应的：
 * 1. so_outstock
 * 2. so_outstock_detail
 *
 * 运行方式：
 * 在 IDE / Maven 增加 VM 参数：-DrunManualUatReplay=true
 */
@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {ErpServerWmsApplication.class}, webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class PlatformOutboundConsumerReplayUatTest {

    private static final ReplayCase CASE_1 = new ReplayCase(
            System.getProperty("replay.ref1", "WFHD26020200462"),
            System.getProperty("replay.expectedTrack1", "AN545697435BR"),
            "{"
                    + "\"warehousePlatformType\":\"overseasWarehouse\","
                    + "\"provider\":\"antu\","
                    + "\"orderCode\":\"A091-260202-0086\","
                    + "\"referenceNo\":\"WFHD26020200462\","
                    + "\"orderStatus\":\"shipped\","
                    + "\"thirdOrderStatus\":\"已发货\","
                    + "\"outBoundTime\":\"2026-02-03T00:27:33\","
                    + "\"trackNo\":\"AN545697435BR\","
                    + "\"swOrderNumber\":\"8209120538680296\","
                    + "\"warehouseCode\":\"BR01\","
                    + "\"shippingMethod\":\"PAC\","
                    + "\"carrierName\":\"CORREIOS_L_BR\","
                    + "\"platform\":\"antu\""
                    + "}"
    );

    private static final ReplayCase CASE_2 = new ReplayCase(
            System.getProperty("replay.ref2", "WFHD26020200697"),
            System.getProperty("replay.expectedTrack2", "AN545716409BR"),
            "{"
                    + "\"warehousePlatformType\":\"overseasWarehouse\","
                    + "\"provider\":\"antu\","
                    + "\"orderCode\":\"A091-260202-0317\","
                    + "\"referenceNo\":\"WFHD26020200697\","
                    + "\"orderStatus\":\"shipped\","
                    + "\"thirdOrderStatus\":\"已发货\","
                    + "\"outBoundTime\":\"2026-02-03T00:28:46\","
                    + "\"trackNo\":\"AN545716409BR\","
                    + "\"swOrderNumber\":\"8209120538680296\","
                    + "\"warehouseCode\":\"BR01\","
                    + "\"shippingMethod\":\"PAC\","
                    + "\"carrierName\":\"CORREIOS_L_BR\","
                    + "\"platform\":\"antu\""
                    + "}"
    );

    @Resource
    private PlatformOutboundConsumerService<?> platformOutboundConsumerService;

    @Resource
    private ThirdWarehouseDeliveryService thirdWarehouseDeliveryService;

    @Resource
    private ThirdWarehouseDeliveryDetailService thirdWarehouseDeliveryDetailService;

    @Resource
    private SoB2cFeign soB2cFeign;

    @Resource
    private SoOutstockService soOutstockService;

    @Test
    public void replayProdLogsConcurrently() throws Exception {
        Assume.assumeTrue("手工 UAT 回放测试默认跳过，请加 VM 参数 -DrunManualUatReplay=true",
                Boolean.parseBoolean(System.getProperty("runManualUatReplay", "false")));

        List<ReplayCase> cases = Arrays.asList(CASE_1, CASE_2);
        prepareReplayCases(cases);
        assertNoOutstockBeforeReplay(cases);
        printCurrentState("before", cases);

        CountDownLatch startGate = new CountDownLatch(1);
        ExecutorService executorService = Executors.newFixedThreadPool(cases.size());
        try {
            Future<ApiResult<?>> future1 = executorService.submit(() -> invokeWithBarrier(CASE_1, startGate));
            Future<ApiResult<?>> future2 = executorService.submit(() -> invokeWithBarrier(CASE_2, startGate));

            startGate.countDown();

            ApiResult<?> result1 = getFuture(future1, CASE_1.referenceNo);
            ApiResult<?> result2 = getFuture(future2, CASE_2.referenceNo);
            Assert.assertTrue("CASE_1 处理失败: " + result1.getMsg(), result1.isSuccess());
            Assert.assertTrue("CASE_2 处理失败: " + result2.getMsg(), result2.isSuccess());
        } finally {
            executorService.shutdownNow();
        }

        printCurrentState("after", cases);
        assertOutstockGenerated(cases);
        logReplayResult(cases);
    }

    private ApiResult<?> invokeWithBarrier(ReplayCase replayCase, CountDownLatch startGate) throws InterruptedException {
        startGate.await();
        log.info("开始回放 referenceNo={}, expectedTrackNo={}", replayCase.referenceNo, replayCase.expectedTrackNo);
        return platformOutboundConsumerService.handle(replayCase.payloadJson);
    }

    private ApiResult<?> getFuture(Future<ApiResult<?>> future, String referenceNo) throws Exception {
        try {
            return future.get();
        } catch (ExecutionException e) {
            Throwable cause = e.getCause() == null ? e : e.getCause();
            throw new RuntimeException("回放失败, referenceNo=" + referenceNo + ", cause=" + cause.getMessage(), cause);
        }
    }

    private void prepareReplayCases(List<ReplayCase> cases) {
        for (ReplayCase replayCase : cases) {
            ThirdWarehouseDeliveryEntity delivery = thirdWarehouseDeliveryService.getLatestByCode(replayCase.referenceNo);
            Assert.assertNotNull("UAT 缺少 third_warehouse_delivery, referenceNo=" + replayCase.referenceNo, delivery);
            Assert.assertNotEquals("三方仓发货单已取消, 无法复现, referenceNo=" + replayCase.referenceNo,
                    SoB2cWarehouseDeliveryStatusEnum.CANCEL_DELIVERY.getStatus(), delivery.getStatus());

            replayCase.deliveryId = delivery.getId();
            replayCase.soCode = delivery.getSoCode();
            replayCase.soId = delivery.getSoId();

            SoB2cEntity soB2c = soB2cFeign.getSoCode(replayCase.soCode);
            Assert.assertNotNull("UAT 缺少 so_b2c, soCode=" + replayCase.soCode, soB2c);
            Assert.assertFalse("so_b2c 已作废, soCode=" + replayCase.soCode, Boolean.TRUE.equals(soB2c.getInvalidStatus()));

            List<ThirdWarehouseDeliveryDetailEntity> deliveryDetails = thirdWarehouseDeliveryDetailService.listByMainId(delivery.getId());
            Assert.assertFalse("UAT 缺少 third_warehouse_delivery_detail, referenceNo=" + replayCase.referenceNo, deliveryDetails.isEmpty());

            List<SoB2cDetailEntity> soDetails = soB2cFeign.listDetailByMainIds(Collections.singletonList(soB2c.getId()));
            Assert.assertFalse("UAT 缺少 so_b2c_detail, soCode=" + replayCase.soCode, soDetails.isEmpty());

            List<SoB2cLogisticsEntity> logisticsList = soB2cFeign.listSoB2cLogisticsByMainIdList(Collections.singletonList(soB2c.getId()));
            Assert.assertFalse("UAT 缺少 so_b2c_logistics, soCode=" + replayCase.soCode, logisticsList.isEmpty());
        }
    }

    private void assertNoOutstockBeforeReplay(List<ReplayCase> cases) {
        List<String> soIds = Arrays.asList(cases.get(0).soId, cases.get(1).soId);
        List<SoOutstockEntity> outstocks = soOutstockService.listBySoIds(soIds);
        Assert.assertTrue(
                "UAT 已存在 so_outstock，请先清理这两个子单的 so_outstock/so_outstock_detail 后再跑。existing="
                        + outstocks.stream().map(SoOutstockEntity::getCode).reduce((a, b) -> a + "," + b).orElse(""),
                outstocks.isEmpty()
        );
    }

    private void assertOutstockGenerated(List<ReplayCase> cases) {
        for (ReplayCase replayCase : cases) {
            SoOutstockEntity outstock = soOutstockService.getBySoId(replayCase.soId);
            Assert.assertNotNull("未生成 so_outstock, soCode=" + replayCase.soCode, outstock);
        }
    }

    private void logReplayResult(List<ReplayCase> cases) {
        ReplayCase first = cases.get(0);
        ReplayCase second = cases.get(1);

        SoOutstockEntity outstock1 = soOutstockService.getBySoId(first.soId);
        SoOutstockEntity outstock2 = soOutstockService.getBySoId(second.soId);
        String outTrack1 = outstock1 == null ? null : outstock1.getTrackNo();
        String outTrack2 = outstock2 == null ? null : outstock2.getTrackNo();

        SoB2cLogisticsEntity logistics1 = getFirstLogistics(first.soId);
        SoB2cLogisticsEntity logistics2 = getFirstLogistics(second.soId);
        String logisticsTrack1 = logistics1 == null ? null : logistics1.getTrackNo();
        String logisticsTrack2 = logistics2 == null ? null : logistics2.getTrackNo();

        boolean sameOutstockTrack = Objects.equals(outTrack1, outTrack2);
        boolean differentLogisticsTrack = !Objects.equals(logisticsTrack1, logisticsTrack2);
        boolean reproduced = sameOutstockTrack && differentLogisticsTrack;

        log.warn("并发回放结果 => reproduced={}, outstockTrack1={}, outstockTrack2={}, logisticsTrack1={}, logisticsTrack2={}",
                reproduced, outTrack1, outTrack2, logisticsTrack1, logisticsTrack2);
    }

    private void printCurrentState(String stage, List<ReplayCase> cases) {
        for (ReplayCase replayCase : cases) {
            SoOutstockEntity outstock = replayCase.soId == null ? null : soOutstockService.getBySoId(replayCase.soId);
            SoB2cLogisticsEntity logistics = replayCase.soId == null ? null : getFirstLogistics(replayCase.soId);
            log.info(
                    "[{}] referenceNo={}, soCode={}, soId={}, expectedTrackNo={}, outstockCode={}, outstockTrackNo={}, logisticsTrackNo={}, logisticsTransportNo={}",
                    stage,
                    replayCase.referenceNo,
                    replayCase.soCode,
                    replayCase.soId,
                    replayCase.expectedTrackNo,
                    outstock == null ? null : outstock.getCode(),
                    outstock == null ? null : outstock.getTrackNo(),
                    logistics == null ? null : logistics.getTrackNo(),
                    logistics == null ? null : logistics.getCode()
            );
        }
    }

    private SoB2cLogisticsEntity getFirstLogistics(String soId) {
        List<SoB2cLogisticsEntity> logisticsList = soB2cFeign.listSoB2cLogisticsByMainIdList(Collections.singletonList(soId));
        return logisticsList.isEmpty() ? null : logisticsList.get(0);
    }

    private static class ReplayCase {
        private final String referenceNo;
        private final String expectedTrackNo;
        private final String payloadJson;
        private String soId;
        private String soCode;
        private String deliveryId;

        private ReplayCase(String referenceNo, String expectedTrackNo, String payloadJson) {
            this.referenceNo = referenceNo;
            this.expectedTrackNo = expectedTrackNo;
            this.payloadJson = payloadJson;
        }
    }
}
