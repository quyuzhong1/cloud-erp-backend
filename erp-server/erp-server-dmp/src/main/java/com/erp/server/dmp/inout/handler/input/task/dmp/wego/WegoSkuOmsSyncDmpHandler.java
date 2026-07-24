package com.erp.server.dmp.inout.handler.input.task.dmp.wego;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.exceptions.ExceptionUtil;
import com.common.business.enums.OmsPlatformEnum;
import com.common.core.entity.BaseEntity;
import com.erp.model.wms.dto.OverseasProviderDTO;
import com.erp.model.wms.dto.WarehouseSkuSyncDTO;
import com.erp.rpc.oms.feign.OmsListingInfoFeign;
import com.erp.rpc.wms.feign.OverseasProviderFeign;
import com.erp.server.dmp.inout.handler.input.task.dmp.DmpInputBaseDmpHandler;
import com.sdk.wms.wego.enums.WegoSkuStatusEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * WEGO SKU DMP 阶段处理器。
 * <p>
 * 从 MongoDB 中读取本次拉取的 WEGO product.search 原始数据，
 * 调用 OMS {@code syncWarehouseNotMatchSku} 将 SKU 同步到未匹配对照表。
 * <p>
 * {@code storage_name} 设为 {@code dmp_sku_info} 仅用于满足
 * {@link com.erp.server.dmp.inout.handler.input.task.dmp.DmpInputDmpHandler}
 * 的反射初始化要求，本 Handler 覆写 {@code convertToDmp} 不向该表写入任何数据。
 */
@Slf4j
@Service
@Scope("prototype")
public class WegoSkuOmsSyncDmpHandler extends DmpInputBaseDmpHandler {

    /**
     * 单次推送 OMS 的最大 SKU 条数，避免单次 Feign 请求体过大导致超时或 OMS 侧长事务。
     */
    private static final int SYNC_BATCH_SIZE = 500;

    @Resource
    private OmsListingInfoFeign omsListingInfoFeign;

    @Resource
    private OverseasProviderFeign overseasProviderFeign;

    /**
     * 覆写父类的 DMP 写入逻辑，改为调用 OMS 未匹配 SKU 同步接口。
     * 返回空列表告知框架本阶段无 DMP 记录写入。
     */
    @Override
    @SuppressWarnings("rawtypes")
    protected List convertToDmp(List<Map<String, Object>> inputMongoEntityList) {
        if (CollUtil.isEmpty(inputMongoEntityList)) {
            log.warn("[WEGO SKU OMS同步] MongoDB数据为空，跳过");
            return new ArrayList<>();
        }

        String authId = null;
        List<WarehouseSkuSyncDTO.SkuItemDTO> skuItems = new ArrayList<>();

        for (Map<String, Object> mongoData : inputMongoEntityList) {
            if (authId == null) {
                Object authIdObj = mongoData.get("authId");
                if (authIdObj != null) {
                    authId = authIdObj.toString();
                }
            }
            String sku = toStr(mongoData.get("sku"));
            if (sku == null || sku.isEmpty()) {
                continue;
            }
            // 防御性兜底：Init 阶段已按 status 过滤过草稿态 SKU，此处再校验一次，
            // 避免未来其他写入路径绕过 Init 过滤时，草稿 SKU 被同步进 OMS 未匹配对照表
            Object statusObj = mongoData.get("status");
            Integer status = statusObj instanceof Number ? ((Number) statusObj).intValue() : null;
            if (!WegoSkuStatusEnum.needSync(status)) {
                continue;
            }
            WarehouseSkuSyncDTO.SkuItemDTO item = new WarehouseSkuSyncDTO.SkuItemDTO();
            item.setSku(sku);
            item.setName(toStr(mongoData.get("name")));
            item.setBarcode(parseBarcodeList(mongoData));
            skuItems.add(item);
        }

        if (CollectionUtils.isEmpty(skuItems) || authId == null) {
            log.warn("[WEGO SKU OMS同步] skuItems为空或authId为空，跳过OMS同步");
            return new ArrayList<>();
        }

        // 查询 provider 绑定的系统仓库（按 authId 精确查询，避免每次全表扫描 listAllMatch）：
        //   - Feign 调用抛异常 → 直接上抛，终止本次同步，避免写入空 warehouse_id 的脏数据，等待任务重试
        //   - 调用成功但未找到绑定仓库 → 属于配置缺失，跳过同步并 warn，同样不写脏数据
        //   - 同一 authId 可能绑定多个系统仓库：WEGO 的对照关系按服务商维度共享给其名下所有仓库
        //     （下游 syncWarehouseNotMatchSku 写入 sku_mapping 时 hasMappingAll=true），
        //     因此这里任取一条绑定仓库仅作为 warehouseId/warehouseName 的默认展示值，
        //     不代表 SKU 实际归属的唯一仓库，不需要也不应该为了"归属哪个仓库"再做额外区分处理。
        List<OverseasProviderDTO.ListWithWarehouseDTO> matchedProviders = overseasProviderFeign.listMatchByMainId(authId);
        String warehouseId = "";
        String warehouseName = "";
        if (CollUtil.isNotEmpty(matchedProviders)) {
            for (OverseasProviderDTO.ListWithWarehouseDTO p : matchedProviders) {
                if (p.getWarehouseId() != null && !p.getWarehouseId().isEmpty()) {
                    warehouseId = p.getWarehouseId();
                    warehouseName = p.getWarehouseName() != null ? p.getWarehouseName() : "";
                    break;
                }
            }
        }
        if (warehouseId.isEmpty()) {
            log.warn("[WEGO SKU OMS同步] 服务商[authId={}]未绑定系统仓库，跳过本次同步，请在海外物流商页面完成仓库绑定", authId);
            return new ArrayList<>();
        }

        // 大批量 SKU 分批推送，避免单次 Feign 请求体过大导致超时/OMS 长事务/OOM
        List<List<WarehouseSkuSyncDTO.SkuItemDTO>> batches = ListUtil.split(skuItems, SYNC_BATCH_SIZE);
        int totalBatches = batches.size();
        int totalSyncCount = 0;
        // 记录已成功推送的批次序号，便于中途失败时定位断点、人工核对 OMS 侧是否已产生重复未匹配记录
        int succeededBatchIndex = 0;
        try {
            for (List<WarehouseSkuSyncDTO.SkuItemDTO> batch : batches) {
                int currentBatchIndex = succeededBatchIndex + 1;
                WarehouseSkuSyncDTO.SyncReqDTO syncReqDTO = new WarehouseSkuSyncDTO.SyncReqDTO();
                syncReqDTO.setAuthId(authId);
                syncReqDTO.setPlatform(OmsPlatformEnum.WE_GO.getCode());
                syncReqDTO.setWarehouseId(warehouseId);
                syncReqDTO.setWarehouseName(warehouseName);
                syncReqDTO.setSkuList(batch);

                WarehouseSkuSyncDTO.ReconcileResultDTO reconcileResult = omsListingInfoFeign.syncWarehouseNotMatchSku(syncReqDTO);
                int syncCount = Objects.isNull(reconcileResult) ? 0 : reconcileResult.getAddedCount();
                totalSyncCount += syncCount;
                succeededBatchIndex = currentBatchIndex;
                log.warn("[WEGO SKU OMS同步] 服务商[authId={}] 批次{}/{} 推送成功, 本批={}条, 新增未匹配记录={}条",
                        authId, currentBatchIndex, totalBatches, batch.size(), syncCount);
            }
            log.info("[WEGO SKU OMS同步] 服务商[authId={}] SKU总数={}条，分{}批推送，新增未匹配记录={}条",
                    authId, skuItems.size(), totalBatches, totalSyncCount);
        } catch (Exception e) {
            log.error("[WEGO SKU OMS同步] 服务商[authId={}] 批次{}/{} 调用OMS异常，已成功批次={}/{}，已成功新增={}条: {}",
                    authId, succeededBatchIndex + 1, totalBatches, succeededBatchIndex, totalBatches,
                    totalSyncCount, ExceptionUtil.getMessage(e), e);
            throw e;
        }

        return new ArrayList<>();
    }

    /**
     * 解析条码字段，兼容 JSON Array 和单字符串两种格式。
     */
    private List<String> parseBarcodeList(Map<String, Object> mongoData) {
        List<String> barcodeList = new ArrayList<>();
        Object barcodeObj = mongoData.get("barcode");
        if (barcodeObj instanceof List) {
            for (Object b : (List<?>) barcodeObj) {
                if (b != null && !b.toString().isEmpty()) {
                    barcodeList.add(b.toString());
                }
            }
            return barcodeList;
        }
        String barcode = toStr(barcodeObj);
        if (barcode != null && !barcode.isEmpty()) {
            barcodeList.add(barcode);
        }
        return barcodeList;
    }

    private String toStr(Object value) {
        return Objects.isNull(value) ? null : value.toString();
    }
}
