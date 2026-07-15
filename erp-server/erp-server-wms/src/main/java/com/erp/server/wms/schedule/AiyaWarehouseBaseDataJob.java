package com.erp.server.wms.schedule;

import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.erp.model.dmp.enums.DmpBasicSystemCodeEnum;
import com.erp.model.oms.enums.AuthStatusEnum;
import com.erp.model.wms.entity.OverseasProviderEntity;
import com.erp.server.wms.service.OverseasProviderService;
import com.erp.server.wms.service.OverseasProviderWarehouseService;
import com.sdk.wms.aiya.service.AiyaOpenApiService;
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
 * 爱亚（AIYA）仓库基础数据定时器，参照 {@link WegoWarehouseBaseDataJob} 搭建。
 * <p>
 * 每天定时从 overseas_provider 表查询 code=aiya 且已授权的三方仓配置，
 * 解析 auth_json 中的 partnerId / customerCode / partnerKey，
 * 通过 {@link AiyaOpenApiService} 调用爱亚 {@code GLINK_QUERY_WAREHOUSE_NOTIFY} 拉取全量仓库，
 * 再交由 {@link OverseasProviderWarehouseService#syncFromAiya} 对「三方仓配置-仓库设置」排重：
 * <ol>
 *     <li>数大臣已存在的忽略；</li>
 *     <li>数大臣未存在的新增存储；</li>
 *     <li>数大臣存在、爱亚不存在且未映射：删除；</li>
 *     <li>数大臣存在、爱亚不存在且已映射：禁用映射关系。</li>
 * </ol>
 * <p>
 * 授权约定：爱亚不走 OAuth，凭证以 partnerId（客户ID）/ customerCode（客户code）/ partnerKey（合作方密钥）
 * 形式存放于 overseas_provider.auth_json；partnerKey 仅用于本地签名，不发送给第三方。
 */
@Component
@Slf4j
@EnableScheduling
public class AiyaWarehouseBaseDataJob {

    private static final String AUTH_KEY_PARTNER_ID = "partnerId";
    private static final String AUTH_KEY_CUSTOMER_CODE = "customerCode";
    private static final String AUTH_KEY_PARTNER_KEY = "partnerKey";

    @Resource
    private OverseasProviderService overseasProviderService;

    @Resource
    private AiyaOpenApiService aiyaOpenApiService;

    @Resource
    private OverseasProviderWarehouseService overseasProviderWarehouseService;

    /**
     * 拉取爱亚仓库基础数据
     */
    @XxlJob("aiyaWarehouseBaseDataJob")
    public ReturnT<String> aiyaWarehouseBaseDataJob() {
        XxlJobHelper.log("=====[爱亚仓库基础数据] 开始任务=====");
        long start = System.currentTimeMillis();

        List<OverseasProviderEntity> providers = overseasProviderService.lambdaQuery()
                .eq(OverseasProviderEntity::getCode, DmpBasicSystemCodeEnum.AIYA.getCode())
                .eq(OverseasProviderEntity::getAuthStatus, AuthStatusEnum.ALREADY.getCode())
                .list();
        if (CollectionUtils.isEmpty(providers)) {
            XxlJobHelper.log("[爱亚仓库基础数据] 未查询到已授权的爱亚三方仓配置，任务结束");
            XxlJobHelper.log("=====[爱亚仓库基础数据] 结束任务=====");
            return ReturnT.SUCCESS;
        }

        int successCount = 0;
        int failCount = 0;
        for (OverseasProviderEntity provider : providers) {
            try {
                Map<String, Object> authJson = provider.getAuthJson();
                if (Objects.isNull(authJson) || authJson.isEmpty()) {
                    XxlJobHelper.log("[爱亚仓库基础数据] 服务商[id={}, shortName={}] auth_json为空，跳过",
                            provider.getId(), provider.getShortName());
                    failCount++;
                    continue;
                }
                String partnerId = toStr(authJson.get(AUTH_KEY_PARTNER_ID));
                String customerCode = toStr(authJson.get(AUTH_KEY_CUSTOMER_CODE));
                String partnerKey = toStr(authJson.get(AUTH_KEY_PARTNER_KEY));
                if (CharSequenceUtil.hasBlank(partnerId, customerCode, partnerKey)) {
                    XxlJobHelper.log("[爱亚仓库基础数据] 服务商[id={}, shortName={}] partnerId/customerCode/partnerKey缺失，跳过",
                            provider.getId(), provider.getShortName());
                    failCount++;
                    continue;
                }

                JSONObject response = aiyaOpenApiService.queryWarehouse(partnerId, partnerKey, customerCode, null);
                JSONArray warehouseList = extractWarehouseList(response);
                if (Objects.isNull(warehouseList)) {
                    XxlJobHelper.log("[爱亚仓库基础数据] 服务商[id={}, shortName={}] 接口返回失败或resultList为空，跳过落库",
                            provider.getId(), provider.getShortName());
                    failCount++;
                    continue;
                }
                int[] syncStat = overseasProviderWarehouseService.syncFromAiya(provider.getId(), warehouseList);
                XxlJobHelper.log("[爱亚仓库基础数据] 服务商[id={}, shortName={}] 同步完成，平台返回={}条，新增={}条，软删={}条，禁用={}条",
                        provider.getId(), provider.getShortName(),
                        warehouseList.size(), syncStat[0], syncStat[1], syncStat[2]);
                successCount++;
            } catch (Exception e) {
                String errorMsg = ExceptionUtil.getMessage(e);
                XxlJobHelper.log("[爱亚仓库基础数据] 服务商[id={}, shortName={}] 调用异常：{}",
                        provider.getId(), provider.getShortName(), errorMsg);
                log.error("[爱亚仓库基础数据] 服务商[id={}, shortName={}] 调用异常",
                        provider.getId(), provider.getShortName(), e);
                failCount++;
            }
        }

        long cost = System.currentTimeMillis() - start;
        XxlJobHelper.log("[爱亚仓库基础数据] 任务执行完毕，总数={}，成功={}，失败={}，耗时={}ms",
                providers.size(), successCount, failCount, cost);
        XxlJobHelper.log("=====[爱亚仓库基础数据] 结束任务=====");
        return ReturnT.SUCCESS;
    }

    private String toStr(Object value) {
        return Objects.isNull(value) ? null : value.toString();
    }

    /**
     * 从爱亚原始响应中提取 resultList 数组，校验业务成功标识。
     * <p>
     * 接口结构：{@code {code, message, success, resultList:[...]}}，success=true 为成功。
     */
    private JSONArray extractWarehouseList(JSONObject response) {
        if (Objects.isNull(response)) {
            return null;
        }
        Boolean success = response.getBoolean("success");
        if (!Boolean.TRUE.equals(success)) {
            XxlJobHelper.log("[爱亚仓库基础数据] 接口返回失败: code={}, message={}",
                    response.get("code"), response.get("message"));
            return null;
        }
        return response.getJSONArray("resultList");
    }
}
