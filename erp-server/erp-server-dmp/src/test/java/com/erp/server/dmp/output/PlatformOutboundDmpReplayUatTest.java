package com.erp.server.dmp.output;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.erp.model.dmp.entity.DmpOutputTaskRecordEntity;
import com.erp.model.dmp.enums.DmpOutputTaskRecordStatusEnum;
import com.erp.model.oms.entity.SoB2cEntity;
import com.erp.model.oms.entity.SoB2cLogisticsEntity;
import com.erp.model.wms.entity.SoOutstockEntity;
import com.erp.model.wms.entity.ThirdWarehouseDeliveryEntity;
import com.erp.rpc.oms.feign.SoB2cFeign;
import com.erp.rpc.wms.feign.SoOutstockFeign;
import com.erp.rpc.wms.feign.ThirdWarehouseDeliveryFeign;
import com.erp.server.dmp.ErpServerDmpApplication;
import com.erp.server.dmp.service.DmpOutputTaskRecordService;
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
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

/**
 * 手工 UAT DMP 整链路回放测试。
 *
 * 运行前准备：
 * 1. 先导入 SYS/TMS/OMS/WMS 基础数据。
 * 2. 再导入 dmp_output_task_record 及相关 dmp_* 配置数据。
 * 3. 不要带入这两个子单对应的 so_outstock / so_outstock_detail。
 *
 * 运行方式：
 * 1. 在 IDE / Maven 增加 VM 参数：-DrunManualDmpReplay=true
 * 2. 如需覆盖默认记录，可额外指定：
 *    -DdmpReplay.recordId1=...
 *    -DdmpReplay.recordId2=...
 *    -DdmpReplay.soCode1=...
 *    -DdmpReplay.soCode2=...
 *    -DdmpReplay.referenceNo1=...
 *    -DdmpReplay.referenceNo2=...
 */
@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {ErpServerDmpApplication.class}, webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class PlatformOutboundDmpReplayUatTest {

    private static final ReplaySeed SEED_1 = new ReplaySeed(
            System.getProperty("dmpReplay.recordId1", "2018364854708805644"),
            System.getProperty("dmpReplay.referenceNo1", "WFHD26020200462"),
            System.getProperty("dmpReplay.soCode1", "A091-260202-0086"),
            System.getProperty("dmpReplay.expectedTrack1", "AN545697435BR")
    );

    private static final ReplaySeed SEED_2 = new ReplaySeed(
            System.getProperty("dmpReplay.recordId2", "2018364854708805636"),
            System.getProperty("dmpReplay.referenceNo2", "WFHD26020200697"),
            System.getProperty("dmpReplay.soCode2", "A091-260202-0317"),
            System.getProperty("dmpReplay.expectedTrack2", "AN545716409BR")
    );

    @Resource
    private DmpOutputTaskRecordService dmpOutputTaskRecordService;

    @Resource
    private SoB2cFeign soB2cFeign;

    @Resource
    private SoOutstockFeign soOutstockFeign;

    @Resource
    private ThirdWarehouseDeliveryFeign thirdWarehouseDeliveryFeign;

    @Test
    public void replayImportedDmpRecordsConcurrently() throws Exception {
        Assume.assumeTrue("手工 UAT DMP 回放测试默认跳过，请加 VM 参数 -DrunManualDmpReplay=true",
                Boolean.parseBoolean(System.getProperty("runManualDmpReplay", "false")));

        List<ReplayCase> cases = resolveReplayCases(Arrays.asList(SEED_1, SEED_2));
        assertNoOutstockBeforeReplay(cases);
        printCurrentState("before", cases);

        CountDownLatch startGate = new CountDownLatch(1);
        ExecutorService executorService = Executors.newFixedThreadPool(cases.size());
        try {
            Future<Boolean> future1 = executorService.submit(() -> triggerBatchSync(cases.get(0), startGate));
            Future<Boolean> future2 = executorService.submit(() -> triggerBatchSync(cases.get(1), startGate));

            startGate.countDown();

            Assert.assertTrue("CASE_1 DMP batchSync 调用失败", getFuture(future1, cases.get(0).recordId));
            Assert.assertTrue("CASE_2 DMP batchSync 调用失败", getFuture(future2, cases.get(1).recordId));
        } finally {
            executorService.shutdownNow();
        }

        waitForReplayResult(cases);
        printCurrentState("after", cases);
        logReplayResult(cases);
    }

    private List<ReplayCase> resolveReplayCases(List<ReplaySeed> seeds) {
        List<String> recordIds = seeds.stream().map(seed -> seed.recordId).collect(Collectors.toList());
        Map<String, DmpOutputTaskRecordEntity> recordMap = dmpOutputTaskRecordService.listByIds(recordIds).stream()
                .collect(Collectors.toMap(DmpOutputTaskRecordEntity::getId, record -> record));

        return seeds.stream().map(seed -> {
            DmpOutputTaskRecordEntity record = recordMap.get(seed.recordId);
            Assert.assertNotNull("UAT 缺少 dmp_output_task_record, id=" + seed.recordId, record);

            JSONObject requestJson = JSON.parseObject(record.getRequestData());
            if (requestJson == null) {
                requestJson = new JSONObject();
            }

            String soCode = valueOrDefault(requestJson.getString("orderCode"), seed.soCode);
            String referenceNo = valueOrDefault(requestJson.getString("referenceNo"), seed.referenceNo);
            String expectedTrackNo = valueOrDefault(requestJson.getString("trackNo"), seed.expectedTrackNo);

            SoB2cEntity soB2c = soB2cFeign.getSoCode(soCode);
            Assert.assertNotNull("UAT 缺少 so_b2c, soCode=" + soCode, soB2c);
            Assert.assertFalse("so_b2c 已作废, soCode=" + soCode, Boolean.TRUE.equals(soB2c.getInvalidStatus()));

            ThirdWarehouseDeliveryEntity delivery = thirdWarehouseDeliveryFeign.getByCodeAndSoId(referenceNo, soB2c.getId());
            Assert.assertNotNull("UAT 缺少 third_warehouse_delivery, referenceNo=" + referenceNo + ", soCode=" + soCode, delivery);

            List<SoB2cLogisticsEntity> logisticsList = soB2cFeign.listSoB2cLogisticsByMainIdList(Collections.singletonList(soB2c.getId()));
            Assert.assertFalse("UAT 缺少 so_b2c_logistics, soCode=" + soCode, logisticsList.isEmpty());

            ReplayCase replayCase = new ReplayCase();
            replayCase.recordId = seed.recordId;
            replayCase.recordDataId = record.getDataId();
            replayCase.referenceNo = referenceNo;
            replayCase.soCode = soCode;
            replayCase.soId = soB2c.getId();
            replayCase.expectedTrackNo = expectedTrackNo;
            return replayCase;
        }).collect(Collectors.toList());
    }

    private void assertNoOutstockBeforeReplay(List<ReplayCase> cases) {
        List<String> soIds = cases.stream().map(replayCase -> replayCase.soId).collect(Collectors.toList());
        List<SoOutstockEntity> outstocks = soOutstockFeign.listBySoIds(soIds);
        Assert.assertTrue(
                "UAT 已存在 so_outstock，请先清理这两个子单的 so_outstock/so_outstock_detail 后再跑。existing="
                        + outstocks.stream().map(SoOutstockEntity::getCode).collect(Collectors.joining(",")),
                outstocks.isEmpty()
        );
    }

    private Boolean triggerBatchSync(ReplayCase replayCase, CountDownLatch startGate) throws InterruptedException {
        startGate.await();
        log.info("开始 DMP 重发 recordId={}, dataId={}, referenceNo={}, soCode={}, expectedTrackNo={}",
                replayCase.recordId, replayCase.recordDataId, replayCase.referenceNo, replayCase.soCode, replayCase.expectedTrackNo);

        dmpOutputTaskRecordService.lambdaUpdate()
                .set(DmpOutputTaskRecordEntity::getIsNeedSync, Boolean.TRUE)
                .set(DmpOutputTaskRecordEntity::getStatus, DmpOutputTaskRecordStatusEnum.INIT.getCode())
                .in(DmpOutputTaskRecordEntity::getId, Collections.singletonList(replayCase.recordId))
                .update();

        List<DmpOutputTaskRecordEntity> recordList = dmpOutputTaskRecordService.listByIds(Collections.singletonList(replayCase.recordId));
        Assert.assertFalse("重发前未找到记录, recordId=" + replayCase.recordId, recordList.isEmpty());
        return Boolean.TRUE.equals(dmpOutputTaskRecordService.batchSync(recordList));
    }

    private Boolean getFuture(Future<Boolean> future, String recordId) throws Exception {
        try {
            return future.get();
        } catch (ExecutionException e) {
            Throwable cause = e.getCause() == null ? e : e.getCause();
            throw new RuntimeException("DMP 重发调用失败, recordId=" + recordId + ", cause=" + cause.getMessage(), cause);
        }
    }

    private void waitForReplayResult(List<ReplayCase> cases) throws InterruptedException {
        long timeoutMs = Long.parseLong(System.getProperty("dmpReplay.waitTimeoutMs", "90000"));
        long pollMs = Long.parseLong(System.getProperty("dmpReplay.pollMs", "1000"));
        long deadline = System.currentTimeMillis() + timeoutMs;

        while (System.currentTimeMillis() < deadline) {
            boolean anyMqError = cases.stream()
                    .map(replayCase -> dmpOutputTaskRecordService.getById(replayCase.recordId))
                    .filter(Objects::nonNull)
                    .anyMatch(record -> DmpOutputTaskRecordStatusEnum.MQERROR.getCode().equals(record.getStatus()));
            if (anyMqError) {
                Assert.fail("DMP 重发出现 MQERROR，请查看 dmp_output_task_record 当前状态: " + currentRecordStatusSummary(cases));
            }

            boolean allGenerated = cases.stream()
                    .allMatch(replayCase -> getOutstockBySoId(replayCase.soId) != null);
            if (allGenerated) {
                return;
            }

            Thread.sleep(pollMs);
        }

        Assert.fail("等待 UAT 生成 so_outstock 超时，recordStatus=" + currentRecordStatusSummary(cases)
                + ", outstockState=" + currentOutstockSummary(cases));
    }

    private void printCurrentState(String stage, List<ReplayCase> cases) {
        for (ReplayCase replayCase : cases) {
            DmpOutputTaskRecordEntity record = dmpOutputTaskRecordService.getById(replayCase.recordId);
            SoOutstockEntity outstock = getOutstockBySoId(replayCase.soId);
            SoB2cLogisticsEntity logistics = getFirstLogistics(replayCase.soId);
            log.info(
                    "[{}] recordId={}, referenceNo={}, soCode={}, soId={}, expectedTrackNo={}, recordStatus={}, isNeedSync={}, outstockCode={}, outstockTrackNo={}, logisticsTrackNo={}, logisticsTransportNo={}",
                    stage,
                    replayCase.recordId,
                    replayCase.referenceNo,
                    replayCase.soCode,
                    replayCase.soId,
                    replayCase.expectedTrackNo,
                    record == null ? null : record.getStatus(),
                    record == null ? null : record.getIsNeedSync(),
                    outstock == null ? null : outstock.getCode(),
                    outstock == null ? null : outstock.getTrackNo(),
                    logistics == null ? null : logistics.getTrackNo(),
                    logistics == null ? null : logistics.getCode()
            );
        }
    }

    private void logReplayResult(List<ReplayCase> cases) {
        ReplayCase first = cases.get(0);
        ReplayCase second = cases.get(1);

        SoOutstockEntity outstock1 = getOutstockBySoId(first.soId);
        SoOutstockEntity outstock2 = getOutstockBySoId(second.soId);
        String outTrack1 = outstock1 == null ? null : outstock1.getTrackNo();
        String outTrack2 = outstock2 == null ? null : outstock2.getTrackNo();

        SoB2cLogisticsEntity logistics1 = getFirstLogistics(first.soId);
        SoB2cLogisticsEntity logistics2 = getFirstLogistics(second.soId);
        String logisticsTrack1 = logistics1 == null ? null : logistics1.getTrackNo();
        String logisticsTrack2 = logistics2 == null ? null : logistics2.getTrackNo();

        boolean sameOutstockTrack = Objects.equals(outTrack1, outTrack2);
        boolean differentLogisticsTrack = !Objects.equals(logisticsTrack1, logisticsTrack2);
        boolean reproduced = sameOutstockTrack && differentLogisticsTrack;

        log.warn("DMP 并发回放结果 => reproduced={}, outstockTrack1={}, outstockTrack2={}, logisticsTrack1={}, logisticsTrack2={}",
                reproduced, outTrack1, outTrack2, logisticsTrack1, logisticsTrack2);
    }

    private SoOutstockEntity getOutstockBySoId(String soId) {
        List<SoOutstockEntity> outstocks = soOutstockFeign.listBySoIds(Collections.singletonList(soId));
        return outstocks.isEmpty() ? null : outstocks.get(0);
    }

    private SoB2cLogisticsEntity getFirstLogistics(String soId) {
        List<SoB2cLogisticsEntity> logisticsList = soB2cFeign.listSoB2cLogisticsByMainIdList(Collections.singletonList(soId));
        return logisticsList.isEmpty() ? null : logisticsList.get(0);
    }

    private String currentRecordStatusSummary(List<ReplayCase> cases) {
        return cases.stream()
                .map(replayCase -> {
                    DmpOutputTaskRecordEntity record = dmpOutputTaskRecordService.getById(replayCase.recordId);
                    return replayCase.recordId + ":" + (record == null ? "missing" : record.getStatus());
                })
                .collect(Collectors.joining(", "));
    }

    private String currentOutstockSummary(List<ReplayCase> cases) {
        return cases.stream()
                .map(replayCase -> {
                    SoOutstockEntity outstock = getOutstockBySoId(replayCase.soId);
                    return replayCase.soCode + ":" + (outstock == null ? "missing" : outstock.getTrackNo());
                })
                .collect(Collectors.joining(", "));
    }

    private String valueOrDefault(String value, String fallback) {
        return value == null || value.trim().isEmpty() ? fallback : value;
    }

    private static class ReplaySeed {
        private final String recordId;
        private final String referenceNo;
        private final String soCode;
        private final String expectedTrackNo;

        private ReplaySeed(String recordId, String referenceNo, String soCode, String expectedTrackNo) {
            this.recordId = recordId;
            this.referenceNo = referenceNo;
            this.soCode = soCode;
            this.expectedTrackNo = expectedTrackNo;
        }
    }

    private static class ReplayCase {
        private String recordId;
        private String recordDataId;
        private String referenceNo;
        private String soCode;
        private String soId;
        private String expectedTrackNo;
    }
}
