package com.erp.server.wms.controller.api;

import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.server.wms.rocketmq.consumer.PlatformNewReturnInstockConsumerService;
import com.erp.server.wms.rocketmq.consumer.RestCloudPlatformNewReturnInstockConsumerService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

/**
 * 本地调试专用 Controller，仅用于开发阶段手动触发消费者逻辑，上线前删除。
 *
 * <p><b>测试数据约定：</b>
 * 调用 {@code /dev/debug/wegoReturnInstock} 时，{@code platformReturnOrderNo} 必须以
 * {@code SAMPLE_NO_} 为前缀（如 {@code SAMPLE_NO_123}），这样测完后可通过
 * {@code /dev/debug/cleanTestData} 一键物理清除所有测试数据。
 * </p>
 */
@Slf4j
@RestController
@RequestMapping("/dev/debug")
public class DevDebugController extends BaseController {

    /** 测试单据号前缀约定，cleanTestData 以此为识别依据 */
    private static final String TEST_THIRD_CODE_PREFIX = "SAMPLE_NO_";

    @Resource
    private PlatformNewReturnInstockConsumerService platformNewReturnInstockConsumerService;

    @Resource
    private RestCloudPlatformNewReturnInstockConsumerService restCloudPlatformNewReturnInstockConsumerService;

    @Resource
    private JdbcTemplate jdbcTemplate;

    /**
     * 触发 WEGO 退货入库消费（platform=wego，走 overseasWarehouseHandle）
     *
     * <p><b>注意：</b>测试时 {@code platformReturnOrderNo} 请务必以 {@code SAMPLE_NO_} 开头，
     * 测完后调用 {@code /dev/debug/cleanTestData} 清除产生的测试数据。</p>
     *
     * 示例 Body（无物流单号/参考单号，走 createSoReturnPrestockHeadless 生成预入库单）：
     * {
     *   "platform": "wego",
     *   "authId": "2060283046716510209",
     *   "warehouseCode": "WG01",
     *   "platformReturnOrderNo": "SAMPLE_NO_123",
     *   "putawayTime": "2026-06-29 00:00:00",
     *   "returnLogisticCode": "",
     *   "orderReferenceNo": "",
     *   "returnType": "other",
     *   "productDetailList": [
     *     { "productSku": "kktwo", "realQty": 2, "mustQty": 2, "receiveQty": 2, "defectiveProductFlag": false }
     *   ]
     * }
     */
    @PostMapping("/wegoReturnInstock")
    public ApiResult<?> wegoReturnInstock(@RequestBody String json) {
        log.info("[DevDebug] 触发 WEGO 退货入库消费, json={}", json);
        platformNewReturnInstockConsumerService.handle(json);
        return success();
    }

    /**
     * 触发 RestCloud 平台退货入库消费（iml 等平台）
     */
    @PostMapping("/restCloudReturnInstock")
    public ApiResult<?> restCloudReturnInstock(@RequestBody String json) {
        log.info("[DevDebug] 触发 RestCloud 退货入库消费, json={}", json);
        restCloudPlatformNewReturnInstockConsumerService.handle(json);
        return success();
    }

    /**
     * 物理清除由 wegoReturnInstock 产生的测试数据。
     *
     * <p>按 {@code third_code} 前缀匹配，依次物理删除：
     * <ol>
     *   <li>so_return_instock_detail（退货入库单明细）</li>
     *   <li>so_return_instock（退货入库单主表）</li>
     *   <li>so_return_prestock_detail（预入库单明细）</li>
     *   <li>so_return_prestock（预入库单主表）</li>
     * </ol>
     *
     * @param prefix third_code 前缀，默认 {@value #TEST_THIRD_CODE_PREFIX}；
     *               必须非空且长度 ≥ 3，防止误删全表
     */
    @PostMapping("/cleanTestData")
    public ApiResult<Map<String, Integer>> cleanTestData(
            @RequestParam(defaultValue = TEST_THIRD_CODE_PREFIX) String prefix) {
        if (StringUtils.isBlank(prefix) || prefix.trim().length() < 3) {
            return failure("prefix 长度不得少于 3 个字符，防止误删全表");
        }
        String like = prefix.trim() + "%";
        log.warn("[DevDebug] 开始清除测试数据, third_code LIKE '{}'", like);

        int instockDetailRows = jdbcTemplate.update(
                "DELETE FROM so_return_instock_detail WHERE main_id IN " +
                "(SELECT id FROM so_return_instock WHERE third_code LIKE ?)", like);

        int instockRows = jdbcTemplate.update(
                "DELETE FROM so_return_instock WHERE third_code LIKE ?", like);

        int prestockDetailRows = jdbcTemplate.update(
                "DELETE FROM so_return_prestock_detail WHERE main_id IN " +
                "(SELECT id FROM so_return_prestock WHERE third_code LIKE ?)", like);

        int prestockRows = jdbcTemplate.update(
                "DELETE FROM so_return_prestock WHERE third_code LIKE ?", like);

        Map<String, Integer> result = new HashMap<>();
        result.put("so_return_instock_detail", instockDetailRows);
        result.put("so_return_instock", instockRows);
        result.put("so_return_prestock_detail", prestockDetailRows);
        result.put("so_return_prestock", prestockRows);

        log.warn("[DevDebug] 测试数据清除完成: {}", result);
        return success(result);
    }
}
