package com.erp.server.wms.schedule;

import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.common.business.enums.OmsPlatformEnum;
import com.erp.model.oms.enums.AuthStatusEnum;
import com.erp.model.wms.dto.WegoWarehouseQueryDTO;
import com.erp.model.wms.entity.OverseasProviderEntity;
import com.erp.server.wms.service.OverseasProviderService;
import com.erp.server.wms.service.OverseasProviderWarehouseService;
import com.sdk.wms.wego.service.WegoOpenApiService;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * WEGO 仓库基础数据定时器
 * <p>
 * 从 overseas_provider 表查询 code=wego 且已授权的服务商配置，
 * 解析 auth_json 中的 appToken / appSecret / domain，
 * 通过 {@link WegoOpenApiService} 调用 WEGO warehouse.get 接口拉取仓库基础数据。
 *
 * @Author Cloud ERP
 * @Date 2026-06-01
 */
@Component
@Slf4j
@EnableScheduling
public class WegoWarehouseBaseDataJob {

    private static final String AUTH_KEY_APP_TOKEN = "appToken";
    private static final String AUTH_KEY_APP_SECRET = "appSecret";

    @Resource
    private OverseasProviderService overseasProviderService;

    @Resource
    private WegoOpenApiService wegoOpenApiService;

    @Resource
    private OverseasProviderWarehouseService overseasProviderWarehouseService;

    /**
     * 拉取 WEGO 仓库基础数据
     */
    @XxlJob("wegoWarehouseBaseDataJob")
    public ReturnT<String> wegoWarehouseBaseDataJob() {
        XxlJobHelper.log("=====[WEGO仓库基础数据] 开始任务=====");
        long start = System.currentTimeMillis();

        List<OverseasProviderEntity> providers = overseasProviderService.lambdaQuery()
                .eq(OverseasProviderEntity::getCode, OmsPlatformEnum.WE_GO.getCode())
                .eq(OverseasProviderEntity::getAuthStatus, AuthStatusEnum.ALREADY.getCode())
                .list();
        if (CollectionUtils.isEmpty(providers)) {
            XxlJobHelper.log("[WEGO仓库基础数据] 未查询到已授权的WEGO三方仓配置，任务结束");
            XxlJobHelper.log("=====[WEGO仓库基础数据] 结束任务=====");
            return ReturnT.SUCCESS;
        }

        int successCount = 0;
        int failCount = 0;
        for (OverseasProviderEntity provider : providers) {
            try {
                Map<String, Object> authJson = provider.getAuthJson();
                if (Objects.isNull(authJson) || authJson.isEmpty()) {
                    XxlJobHelper.log("[WEGO仓库基础数据] 服务商[id={}, shortName={}] auth_json为空，跳过",
                            provider.getId(), provider.getShortName());
                    failCount++;
                    continue;
                }
                String appToken = toStr(authJson.get(AUTH_KEY_APP_TOKEN));
                String appSecret = toStr(authJson.get(AUTH_KEY_APP_SECRET));
                if (CharSequenceUtil.hasBlank(appToken, appSecret)) {
                    XxlJobHelper.log("[WEGO仓库基础数据] 服务商[id={}, shortName={}] appToken/appSecret缺失，跳过",
                            provider.getId(), provider.getShortName());
                    failCount++;
                    continue;
                }

                WegoWarehouseQueryDTO.QueryReqDTO reqDTO = new WegoWarehouseQueryDTO.QueryReqDTO();
                reqDTO.setAccessToken(appToken);
                reqDTO.setSecret(appSecret);

                JSONObject response = wegoOpenApiService.queryWarehouse(reqDTO);
                JSONArray warehouseList = extractWarehouseList(response);
                if (Objects.isNull(warehouseList)) {
                    XxlJobHelper.log("[WEGO仓库基础数据] 服务商[id={}, shortName={}] 接口返回失败或result为空，跳过落库",
                            provider.getId(), provider.getShortName());
                    failCount++;
                    continue;
                }
                int[] syncStat = overseasProviderWarehouseService.syncFromWego(provider.getId(), warehouseList);
                XxlJobHelper.log("[WEGO仓库基础数据] 服务商[id={}, shortName={}] 同步完成，平台返回={}条，新增={}条，软删={}条，禁用={}条",
                        provider.getId(), provider.getShortName(),
                        warehouseList.size(), syncStat[0], syncStat[1], syncStat[2]);
                successCount++;
            } catch (Exception e) {
                String errorMsg = ExceptionUtil.getMessage(e);
                XxlJobHelper.log("[WEGO仓库基础数据] 服务商[id={}, shortName={}] 调用异常：{}",
                        provider.getId(), provider.getShortName(), errorMsg);
                log.error("[WEGO仓库基础数据] 服务商[id={}, shortName={}] 调用异常",
                        provider.getId(), provider.getShortName(), e);
                failCount++;
            }
        }

        long cost = System.currentTimeMillis() - start;
        XxlJobHelper.log("[WEGO仓库基础数据] 任务执行完毕，总数={}，成功={}，失败={}，耗时={}ms",
                providers.size(), successCount, failCount, cost);
        XxlJobHelper.log("=====[WEGO仓库基础数据] 结束任务=====");
        return ReturnT.SUCCESS;
    }

    private String toStr(Object value) {
        return Objects.isNull(value) ? null : value.toString();
    }

    /**
     * 从 WEGO 原始响应中提取 result 数组，校验业务成功标识。
     * <p>
     * 接口结构: {success, errorCode, errorMsg, serverTime, result:[...]}
     */
    private JSONArray extractWarehouseList(JSONObject response) {
        if (Objects.isNull(response)) {
            return null;
        }
        Boolean success = response.getBoolean("success");
        if (!Boolean.TRUE.equals(success)) {
            XxlJobHelper.log("[WEGO仓库基础数据] 接口返回失败: errorCode={}, errorMsg={}",
                    response.get("errorCode"), response.get("errorMsg"));
            return null;
        }
        return response.getJSONArray("result");
    }
}
