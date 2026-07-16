package com.erp.server.wms.controller.api;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.server.wms.service.OverseasProviderWarehouseService;
import com.sdk.wms.aiya.service.AiyaOpenApiService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

/**
 * 爱亚（AIYA）海外仓联调测试用 Controller。
 * <p>
 * 仅用于手动传入鉴权参数（partnerId / customerCode / partnerKey）联调「拉取仓库」链路，
 * 不接入网关鉴权与数据权限，不用于线上业务。正式拉取由 {@code AiyaWarehouseBaseDataJob} 定时执行。
 */
@Slf4j
@RestController
@RequestMapping("/aiya/test")
public class AiyaController extends BaseController {

    @Resource
    private AiyaOpenApiService aiyaOpenApiService;

    @Resource
    private OverseasProviderWarehouseService overseasProviderWarehouseService;

    /**
     * 仅测试「调用爱亚接口拉取仓库列表」，不落库。
     * <p>
     * 直接返回爱亚 {@code GLINK_QUERY_WAREHOUSE_NOTIFY} 的原始响应（含 success / code / message / resultList），
     * 用于验证外层字段、customerCode 业务参数、bizData 签名及网关连通性是否正确。
     *
     * @param partnerId    爱亚 partnerId（客户ID，外层字段）
     * @param customerCode 爱亚 customerCode（客户code，必填业务参数）
     * @param partnerKey   爱亚 partnerKey（合作方密钥，仅用于本地签名）
     * @return 爱亚接口原始响应
     */
    @GetMapping("/queryWarehouse")
    public ApiResult<JSONObject> queryWarehouse(@RequestParam("partnerId") String partnerId,
                                                @RequestParam("customerCode") String customerCode,
                                                @RequestParam("partnerKey") String partnerKey) {
        JSONObject resp = aiyaOpenApiService.queryWarehouse(partnerId, partnerKey, customerCode, null);
        return success(resp);
    }

    /**
     * 仅测试「调用爱亚接口查询派送渠道」，不落库。
     * <p>
     * 直接返回爱亚 {@code GLINK_QUERY_CARRIER_NOTIFY} 的原始响应（含 success / code / message / resultList），
     * 用于验证外层字段、customerCode 业务参数、bizData 签名及网关连通性是否正确。
     *
     * @param partnerId           爱亚 partnerId（客户ID，外层字段）
     * @param customerCode        爱亚 customerCode（客户code，必填业务参数）
     * @param partnerKey          爱亚 partnerKey（合作方密钥，仅用于本地签名）
     * @param warehouseCode       仓库编码（必填业务参数，写入 bizData）
     * @param needActualLogistics 是否需要实际物流商（可选）
     * @return 爱亚接口原始响应
     */
    @GetMapping("/queryTransport")
    public ApiResult<JSONObject> queryTransport(@RequestParam("partnerId") String partnerId,
                                                @RequestParam("customerCode") String customerCode,
                                                @RequestParam("partnerKey") String partnerKey,
                                                @RequestParam("warehouseCode") String warehouseCode,
                                                @RequestParam(value = "needActualLogistics", required = false) Boolean needActualLogistics) {
        Map<String, Object> bizParams = new HashMap<>();
        bizParams.put("warehouseCode", warehouseCode);
        if (needActualLogistics != null) {
            bizParams.put("needActualLogistics", needActualLogistics);
        }
        JSONObject resp = aiyaOpenApiService.queryTransport(partnerId, partnerKey, customerCode, bizParams);
        return success(resp);
    }

    /**
     * 测试「拉取仓库 + 排重落库」完整链路。
     * <p>
     * 调用爱亚接口拉取仓库后，交由 {@link OverseasProviderWarehouseService#syncFromAiya} 排重落库，
     * 返回同步统计。{@code mainId} 需为库中已存在的爱亚三方仓配置（overseas_provider.id）。
     *
     * @param partnerId    爱亚 partnerId（客户ID）
     * @param customerCode 爱亚 customerCode（客户code）
     * @param partnerKey   爱亚 partnerKey（合作方密钥）
     * @param mainId       爱亚三方仓配置 ID（overseas_provider.id），作为仓库落库归属
     * @return 同步统计：新增 / 软删 / 禁用 条数
     */
    @GetMapping("/syncWarehouse")
    public ApiResult<Map<String, Object>> syncWarehouse(@RequestParam("partnerId") String partnerId,
                                                        @RequestParam("customerCode") String customerCode,
                                                        @RequestParam("partnerKey") String partnerKey,
                                                        @RequestParam("mainId") String mainId) {
        JSONObject resp = aiyaOpenApiService.queryWarehouse(partnerId, partnerKey, customerCode, null);
        Map<String, Object> result = new HashMap<>();
        result.put("rawResponse", resp);
        if (resp == null || !Boolean.TRUE.equals(resp.getBoolean("success"))) {
            log.warn("[爱亚仓库测试] 接口返回失败或无响应, mainId={}, resp={}", mainId, resp);
            result.put("synced", false);
            return success(result);
        }
        JSONArray warehouseList = resp.getJSONArray("resultList");
        int[] stat = overseasProviderWarehouseService.syncFromAiya(mainId, warehouseList);
        result.put("synced", true);
        result.put("platformCount", warehouseList == null ? 0 : warehouseList.size());
        result.put("insertCount", stat[0]);
        result.put("deleteCount", stat[1]);
        result.put("disableCount", stat[2]);
        return success(result);
    }
}
