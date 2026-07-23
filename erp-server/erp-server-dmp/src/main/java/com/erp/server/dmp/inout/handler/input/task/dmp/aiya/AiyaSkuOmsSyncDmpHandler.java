package com.erp.server.dmp.inout.handler.input.task.dmp.aiya;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.exceptions.ExceptionUtil;
import com.common.business.enums.OmsPlatformEnum;
import com.erp.model.wms.dto.OverseasProviderDTO;
import com.erp.model.wms.dto.WegoSkuSyncDTO;
import com.erp.rpc.oms.feign.OmsListingInfoFeign;
import com.erp.rpc.wms.feign.OverseasProviderFeign;
import com.erp.server.dmp.inout.handler.input.task.dmp.DmpInputBaseDmpHandler;
import com.sdk.wms.aiya.enums.AiyaSkuStatusEnum;
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
 * 爱亚 SKU DMP 阶段处理器，对齐 {@code WegoSkuOmsSyncDmpHandler}。
 * <p>
 * 从 MongoDB 中读取本次拉取的爱亚 {@code GLINK_QUERY_ITEM_NOTIFY} 原始数据，按 {@code SYNC_BATCH_SIZE}
 * 分批调用 OMS {@code syncWarehouseNotMatchSku}：每一批都会完成新增/更新未匹配对照表，
 * 并对本批 SKU 中源端状态非启用（{@code status} 非 {@code Active}）的已映射记录置为禁用。
 * 该判断只依赖每条 SKU 自身携带的状态，不需要"完整快照"用于比对是否有 SKU 消失
 * （爱亚等三方仓通常会把已下架/停用的 SKU 继续保留在拉取结果里，只是状态变化，不会整条消失），
 * 因此各批次调用之间互不依赖，无需区分"是否最后一批"。
 * <p>
 * 复用的 {@link WegoSkuSyncDTO} 已确认为平台无关的通用 DTO（按 {@code dto.getPlatform()}/{@code dto.getAuthId()}
 * 落库），命名沿用历史 "Wego" 前缀，但对爱亚可直接复用；{@code syncWarehouseNotMatchSku} 同样按
 * {@code authId} 维度实现禁用回收，为平台无关的公共能力，本轮仅接入爱亚。WEGO 现有调用不传 {@code status}，
 * 因此不会触发禁用分支，行为不受影响。
 * <p>
 * {@code storage_name} 设为 {@code dmp_sku_info} 仅用于满足
 * {@link com.erp.server.dmp.inout.handler.input.task.dmp.DmpInputDmpHandler}
 * 的反射初始化要求，本 Handler 覆写 {@code convertToDmp} 不向该表写入任何数据。
 */
@Slf4j
@Service
@Scope("prototype")
public class AiyaSkuOmsSyncDmpHandler extends DmpInputBaseDmpHandler {

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
            log.warn("[爱亚SKU OMS同步] MongoDB数据为空，跳过");
            return new ArrayList<>();
        }

        String authId = null;
        List<WegoSkuSyncDTO.SkuItemDTO> skuItems = new ArrayList<>();

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
            // 防御性兜底：Init 阶段已按 status 过滤过非 Active/Inactive 的异常状态，此处再校验一次，
            // 避免未来其他写入路径绕过 Init 过滤时，未知状态 SKU 被同步进 OMS 未匹配对照表
            String status = toStr(mongoData.get("status"));
            if (!AiyaSkuStatusEnum.needSync(status)) {
                continue;
            }
            WegoSkuSyncDTO.SkuItemDTO item = new WegoSkuSyncDTO.SkuItemDTO();
            item.setSku(sku);
            // description（商品名称描述）为文档必填字段，实测响应必带；name（商品名-前端不展示）为可选字段，
            // 2026-07-16 实测两条真实测试SKU（未设置name）响应里完全没有 name 键，只有 description。
            // 优先用 name，为空则退到 description，避免因 name 未设置导致 OMS 未匹配表里名称长期为空
            String name = toStr(mongoData.get("name"));
            if (name == null || name.isEmpty()) {
                name = toStr(mongoData.get("description"));
            }
            item.setName(name);
            item.setBarcode(parseBarcodeList(mongoData));
            // 随行透传源端原始状态，供 OMS 侧判断已映射且源端停用→禁用
            item.setStatus(status);
            skuItems.add(item);
        }

        if (CollectionUtils.isEmpty(skuItems) || authId == null) {
            log.warn("[爱亚SKU OMS同步] skuItems为空或authId为空，跳过OMS同步");
            return new ArrayList<>();
        }

        // 查询 provider 绑定的系统仓库（按 authId 精确查询，避免每次全表扫描 listAllMatch）：
        //   - Feign 调用抛异常 → 直接上抛，终止本次同步，避免写入空 warehouse_id 的脏数据，等待任务重试
        //   - 调用成功但未找到绑定仓库 → 属于配置缺失，跳过同步并 warn，同样不写脏数据
        //   - 同一 authId 可能绑定多个系统仓库：爱亚的对照关系按服务商维度共享给其名下所有仓库
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
            log.warn("[爱亚SKU OMS同步] 服务商[authId={}]未绑定系统仓库，跳过本次同步，请在海外物流商页面完成仓库绑定", authId);
            return new ArrayList<>();
        }

        // 大批量 SKU 分批推送，避免单次 Feign 请求体过大导致超时/OMS 长事务/OOM。
        // syncWarehouseNotMatchSku 的禁用判断只看本批 SKU 各自携带的状态，不需要"完整快照"比对，
        // 因此每一批都独立调用同一个接口即可，不需要区分"是否最后一批"。
        List<List<WegoSkuSyncDTO.SkuItemDTO>> batches = ListUtil.split(skuItems, SYNC_BATCH_SIZE);
        int totalBatches = batches.size();
        int totalAddedCount = 0;
        int totalDisabledCount = 0;
        int totalDeletedCount = 0;
        // 记录已成功处理的批次序号，便于中途失败时定位断点、人工核对 OMS 侧是否已产生重复处理
        int succeededBatchIndex = 0;
        try {
            for (List<WegoSkuSyncDTO.SkuItemDTO> batch : batches) {
                int currentBatchIndex = succeededBatchIndex + 1;

                WegoSkuSyncDTO.SyncReqDTO reqDTO = new WegoSkuSyncDTO.SyncReqDTO();
                reqDTO.setAuthId(authId);
                reqDTO.setPlatform(OmsPlatformEnum.AI_YA.getCode());
                reqDTO.setWarehouseId(warehouseId);
                reqDTO.setWarehouseName(warehouseName);
                reqDTO.setSkuList(batch);

                WegoSkuSyncDTO.ReconcileResultDTO reconcileResult = omsListingInfoFeign.syncWarehouseNotMatchSku(reqDTO);
                int addedCount = Objects.isNull(reconcileResult) ? 0 : reconcileResult.getAddedCount();
                int disabledCount = Objects.isNull(reconcileResult) ? 0 : reconcileResult.getDisabledCount();
                int deletedCount = Objects.isNull(reconcileResult) ? 0 : reconcileResult.getDeletedCount();
                totalAddedCount += addedCount;
                totalDisabledCount += disabledCount;
                totalDeletedCount += deletedCount;
                succeededBatchIndex = currentBatchIndex;
                log.warn("[爱亚SKU OMS同步] 服务商[authId={}] 批次{}/{} 处理成功, 本批={}条, 新增={}条, 禁用={}条, 删除未匹配={}条",
                        authId, currentBatchIndex, totalBatches, batch.size(), addedCount, disabledCount, deletedCount);
            }
            log.warn("[爱亚SKU OMS同步] 服务商[authId={}] SKU总数={}条，分{}批处理，新增未匹配记录={}条，禁用映射关系={}条，删除未匹配={}条",
                    authId, skuItems.size(), totalBatches, totalAddedCount, totalDisabledCount, totalDeletedCount);
        } catch (Exception e) {
            log.error("[爱亚SKU OMS同步] 服务商[authId={}] 批次{}/{} 调用OMS异常，已成功批次={}/{}，已成功新增={}条: {}",
                    authId, succeededBatchIndex + 1, totalBatches, succeededBatchIndex, totalBatches,
                    totalAddedCount, ExceptionUtil.getMessage(e), e);
            throw e;
        }

        return new ArrayList<>();
    }

    /**
     * 解析条码字段。
     * <p>
     * 2026-07-16 联调实测确认真实响应结构：条码不是顶层 {@code barcode} 字段（文档"产品条形码"的表述
     * 曾被误理解为顶层字符串/数组），而是顶层 {@code barcodeList} 数组，每个元素为
     * {@code {unit, barcode}} 对象（如 {@code [{"unit":"EA","barcode":"test1602"}]}），
     * 与 {@code packagingList} 是同级的两个独立数组，不是嵌套关系。
     * 按此结构解析每个元素的 {@code barcode} 字段；额外兼容极端情况下 {@code barcodeList} 元素本身是
     * 纯字符串的写法，避免未来接口变更时直接抛异常。
     */
    private List<String> parseBarcodeList(Map<String, Object> mongoData) {
        List<String> barcodeList = new ArrayList<>();
        Object barcodeListObj = mongoData.get("barcodeList");
        if (barcodeListObj instanceof List) {
            for (Object entry : (List<?>) barcodeListObj) {
                String barcode;
                if (entry instanceof Map) {
                    barcode = toStr(((Map<?, ?>) entry).get("barcode"));
                } else {
                    barcode = toStr(entry);
                }
                if (barcode != null && !barcode.isEmpty()) {
                    barcodeList.add(barcode);
                }
            }
        }
        return barcodeList;
    }

    private String toStr(Object value) {
        return Objects.isNull(value) ? null : value.toString();
    }
}
