package com.erp.server.wms.schedule;

import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.common.business.enums.OmsPlatformEnum;
import com.erp.model.oms.enums.AuthStatusEnum;
import com.erp.model.wms.dto.WegoSkuQueryDTO;
import com.erp.model.wms.dto.WegoSkuSyncDTO;
import com.erp.model.wms.entity.OverseasProviderEntity;
import com.erp.rpc.oms.feign.OmsListingInfoFeign;
import com.erp.server.wms.service.OverseasProviderService;
import com.sdk.wms.wego.service.WegoOpenApiService;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * WEGO SKU 基础数据定时器
 * <p>
 * 从 overseas_provider 表查询 code=wego 且已授权的服务商配置，
 * 调用 WEGO product.search 分页拉取 SKU 基础数据，
 * 同步到「数大臣-sku对照表-未匹配」。
 */
@Component
@Slf4j
public class WegoSkuBaseDataJob {

    private static final String AUTH_KEY_APP_TOKEN = "appToken";
    private static final String AUTH_KEY_APP_SECRET = "appSecret";
    private static final int DEFAULT_PAGE_SIZE = 200;
    private static final int MAX_PAGE_LIMIT = 1000;

    @Resource
    private OverseasProviderService overseasProviderService;

    @Resource
    private WegoOpenApiService wegoOpenApiService;

    @Resource
    private OmsListingInfoFeign omsListingInfoFeign;

    /**
     * 拉取 WEGO SKU 基础数据
     *
     * @return XXL 任务执行结果
     */
    @XxlJob("wegoSkuBaseDataJob")
    public ReturnT<String> wegoSkuBaseDataJob() {
        XxlJobHelper.log("=====[WEGO SKU基础数据] 开始任务=====");
        long start = System.currentTimeMillis();

        List<OverseasProviderEntity> providers = overseasProviderService.lambdaQuery()
                .eq(OverseasProviderEntity::getCode, OmsPlatformEnum.WE_GO.getCode())
                .eq(OverseasProviderEntity::getAuthStatus, AuthStatusEnum.ALREADY.getCode())
                .list();
        if (CollectionUtils.isEmpty(providers)) {
            XxlJobHelper.log("[WEGO SKU基础数据] 未查询到已授权的WEGO三方仓配置，任务结束");
            XxlJobHelper.log("=====[WEGO SKU基础数据] 结束任务=====");
            return ReturnT.SUCCESS;
        }

        int successCount = 0;
        int failCount = 0;
        int syncCount = 0;
        for (OverseasProviderEntity provider : providers) {
            try {
                int providerSyncCount = syncProviderSku(provider);
                successCount++;
                syncCount += providerSyncCount;
                XxlJobHelper.log("[WEGO SKU基础数据] 服务商[id={}, shortName={}] 同步完成，本次新增未匹配记录={}条",
                        provider.getId(), provider.getShortName(), providerSyncCount);
            } catch (Exception e) {
                String errorMsg = ExceptionUtil.getMessage(e);
                XxlJobHelper.log("[WEGO SKU基础数据] 服务商[id={}, shortName={}] 调用异常：{}",
                        provider.getId(), provider.getShortName(), errorMsg);
                log.error("[WEGO SKU基础数据] 服务商[id={}, shortName={}] 调用异常",
                        provider.getId(), provider.getShortName(), e);
                failCount++;
            }
        }

        long cost = System.currentTimeMillis() - start;
        String summary = CharSequenceUtil.format(
                "[WEGO SKU基础数据] 任务执行完毕，总数={}，成功={}，失败={}，本次新增未匹配记录={}条，耗时={}ms",
                providers.size(), successCount, failCount, syncCount, cost
        );
        XxlJobHelper.log(summary);
        if (failCount > 0) {
            log.warn("[WEGO SKU基础数据] 任务存在失败服务商，failCount={}，请关注上方日志确认失败原因", failCount);
        }
        XxlJobHelper.log("=====[WEGO SKU基础数据] 结束任务=====");
        return ReturnT.SUCCESS;
    }

    /**
     * 按服务商分页拉取 WEGO SKU，并同步到 OMS 的未匹配对照表。
     *
     * @param provider 三方仓服务商配置
     * @return 本服务商本次成功同步的 SKU 数量
     */
    private int syncProviderSku(OverseasProviderEntity provider) {
        Map<String, Object> authJson = provider.getAuthJson();
        if (Objects.isNull(authJson) || authJson.isEmpty()) {
            throw new IllegalArgumentException("auth_json为空");
        }
        String appToken = toStr(authJson.get(AUTH_KEY_APP_TOKEN));
        String appSecret = toStr(authJson.get(AUTH_KEY_APP_SECRET));
        if (CharSequenceUtil.hasBlank(appToken, appSecret)) {
            throw new IllegalArgumentException("appToken/appSecret缺失");
        }

        int pageNum = 1;
        int fetchCount = 0;
        int syncCount = 0;
        while (pageNum <= MAX_PAGE_LIMIT) {
            // 分页拉取 WEGO SKU，避免单次请求数据量过大
            WegoSkuQueryDTO.QueryReqDTO reqDTO = new WegoSkuQueryDTO.QueryReqDTO();
            reqDTO.setAccessToken(appToken);
            reqDTO.setSecret(appSecret);
            reqDTO.setPageNum(pageNum);
            reqDTO.setPageSize(DEFAULT_PAGE_SIZE);

            JSONObject result = wegoOpenApiService.querySku(reqDTO);
            JSONObject pageResult = extractSkuPageResult(result);

            JSONArray skuList = pageResult.getJSONArray("list");
            if (Objects.nonNull(skuList) && !skuList.isEmpty()) {
                fetchCount += skuList.size();
                WegoSkuSyncDTO.SyncReqDTO syncReqDTO = buildSyncReqDTO(provider.getId(), skuList);
                // 仅当有效 SKU 非空时才触发 OMS 入库
                if (CollectionUtils.isNotEmpty(syncReqDTO.getSkuList())) {
                    Integer currentSync = omsListingInfoFeign.syncWarehouseNotMatchSku(syncReqDTO);
                    syncCount += Objects.isNull(currentSync) ? 0 : currentSync;
                }
            }

            Boolean emptyFlag = pageResult.getBoolean("emptyFlag");
            Integer pages = pageResult.getInteger("pages");
            // 兼容多种结束条件：接口显式空标记、当前页无数据、达到总页数
            if (Boolean.TRUE.equals(emptyFlag)
                    || Objects.isNull(skuList)
                    || skuList.isEmpty()
                    || (Objects.nonNull(pages) && pageNum >= pages)) {
                break;
            }
            pageNum++;
        }
        XxlJobHelper.log("[WEGO SKU基础数据] 服务商[id={}, shortName={}] 共拉取SKU={}条，有效页数={}页",
                provider.getId(), provider.getShortName(), fetchCount, pageNum);
        if (pageNum > MAX_PAGE_LIMIT) {
            XxlJobHelper.log("[WEGO SKU基础数据] 警告：已达到最大翻页上限({})，服务商[id={}, shortName={}] 可能存在未同步数据",
                    MAX_PAGE_LIMIT, provider.getId(), provider.getShortName());
            log.warn("[WEGO SKU基础数据] 服务商[id={}, shortName={}] 已达到最大翻页上限({})",
                    provider.getId(), provider.getShortName(), MAX_PAGE_LIMIT);
        }
        return syncCount;
    }

    /**
     * 将 WEGO 返回的 SKU JSON 列表转换为内部同步 DTO。
     *
     * @param authId  overseas_provider.id
     * @param skuList WEGO result.list
     * @return OMS 同步入参
     */
    private WegoSkuSyncDTO.SyncReqDTO buildSyncReqDTO(String authId, JSONArray skuList) {
        List<WegoSkuSyncDTO.SkuItemDTO> itemList = new ArrayList<>();
        for (int i = 0; i < skuList.size(); i++) {
            JSONObject skuJson = skuList.getJSONObject(i);
            if (Objects.isNull(skuJson)) {
                continue;
            }
            String sku = skuJson.getString("sku");
            if (CharSequenceUtil.isBlank(sku)) {
                continue;
            }
            WegoSkuSyncDTO.SkuItemDTO itemDTO = new WegoSkuSyncDTO.SkuItemDTO();
            itemDTO.setSku(sku);
            itemDTO.setName(skuJson.getString("name"));
            itemDTO.setBarcode(parseBarcodeList(skuJson));
            itemList.add(itemDTO);
        }
        WegoSkuSyncDTO.SyncReqDTO reqDTO = new WegoSkuSyncDTO.SyncReqDTO();
        reqDTO.setAuthId(authId);
        reqDTO.setPlatform(OmsPlatformEnum.WE_GO.getCode());
        reqDTO.setSkuList(itemList);
        return reqDTO;
    }

    /**
     * 解析 WEGO SKU 条码字段，兼容数组和单字符串两种格式。
     *
     * @param skuJson WEGO 单条 SKU JSON
     * @return 条码列表（自动过滤空值）
     */
    private List<String> parseBarcodeList(JSONObject skuJson) {
        List<String> barcodeList = new ArrayList<>();
        Object barcodeObj = skuJson.get("barcode");
        if (barcodeObj instanceof JSONArray) {
            JSONArray barcodeArray = (JSONArray) barcodeObj;
            for (int i = 0; i < barcodeArray.size(); i++) {
                String barcode = barcodeArray.getString(i);
                if (CharSequenceUtil.isNotBlank(barcode)) {
                    barcodeList.add(barcode);
                }
            }
            return barcodeList;
        }

        String barcode = skuJson.getString("barcode");
        if (CharSequenceUtil.isNotBlank(barcode)) {
            barcodeList.add(barcode);
        }
        return barcodeList;
    }

    /**
     * 接口结构:
     * {success, errorCode, errorMsg, serverTime, result:{pageNum,pageSize,total,pages,list,emptyFlag}}
     *
     * @param result WEGO 原始响应
     * @return result 分页对象
     * @throws IllegalStateException 接口返回失败或 result 结构异常时抛出，携带 errorCode/errorMsg 信息
     */
    private JSONObject extractSkuPageResult(JSONObject result) {
        if (Objects.isNull(result)) {
            throw new IllegalStateException("WEGO SKU接口响应为空");
        }
        Boolean success = result.getBoolean("success");
        if (!Boolean.TRUE.equals(success)) {
            String errorCode = String.valueOf(result.get("errorCode"));
            String errorMsg = String.valueOf(result.get("errorMsg"));
            XxlJobHelper.log("[WEGO SKU基础数据] 接口返回失败: errorCode={}, errorMsg={}", errorCode, errorMsg);
            throw new IllegalStateException(
                    CharSequenceUtil.format("WEGO SKU接口返回失败: errorCode={}, errorMsg={}", errorCode, errorMsg));
        }
        Object pageObj = result.get("result");
        if (pageObj instanceof JSONObject) {
            return (JSONObject) pageObj;
        }
        if (pageObj instanceof JSONArray) {
            JSONObject wrap = new JSONObject();
            wrap.put("list", pageObj);
            wrap.put("pageNum", 1);
            wrap.put("pages", 1);
            wrap.put("emptyFlag", ((JSONArray) pageObj).isEmpty());
            return wrap;
        }
        String resultStr = Objects.isNull(pageObj) ? "null" : pageObj.toString();
        XxlJobHelper.log("[WEGO SKU基础数据] result结构异常: {}", resultStr);
        throw new IllegalStateException(CharSequenceUtil.format("WEGO SKU接口result结构异常: {}", resultStr));
    }

    /**
     * 对象安全转字符串。
     */
    private String toStr(Object value) {
        return Objects.isNull(value) ? null : value.toString();
    }
}
