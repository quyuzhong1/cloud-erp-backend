package com.erp.server.dmp.inout.handler.input.task.dmp.wego;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.exceptions.ExceptionUtil;
import com.common.business.enums.OmsPlatformEnum;
import com.common.core.entity.BaseEntity;
import com.erp.model.wms.dto.OverseasProviderDTO;
import com.erp.model.wms.dto.WegoSkuSyncDTO;
import com.erp.rpc.oms.feign.OmsListingInfoFeign;
import com.erp.rpc.wms.feign.OverseasProviderFeign;
import com.erp.server.dmp.inout.handler.input.task.dmp.DmpInputBaseDmpHandler;
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
            WegoSkuSyncDTO.SkuItemDTO item = new WegoSkuSyncDTO.SkuItemDTO();
            item.setSku(sku);
            item.setName(toStr(mongoData.get("name")));
            item.setBarcode(parseBarcodeList(mongoData));
            skuItems.add(item);
        }

        if (CollectionUtils.isEmpty(skuItems) || authId == null) {
            log.warn("[WEGO SKU OMS同步] skuItems为空或authId为空，跳过OMS同步");
            return new ArrayList<>();
        }

        // 通过 authId 查询绑定的系统仓库，填充到同步请求中
        String warehouseId = "";
        String warehouseName = "";
        try {
            List<OverseasProviderDTO.ListWithWarehouseDTO> allProviders = overseasProviderFeign.listAllMatch();
            if (CollUtil.isNotEmpty(allProviders)) {
                for (OverseasProviderDTO.ListWithWarehouseDTO p : allProviders) {
                    if (authId.equals(p.getId()) && p.getWarehouseId() != null && !p.getWarehouseId().isEmpty()) {
                        warehouseId = p.getWarehouseId();
                        warehouseName = p.getWarehouseName() != null ? p.getWarehouseName() : "";
                        break;
                    }
                }
            }
        } catch (Exception e) {
            log.warn("[WEGO SKU OMS同步] 查询服务商仓库信息失败，warehouseId将为空，authId={}: {}", authId, e.getMessage());
        }
        if (warehouseId.isEmpty()) {
            log.warn("[WEGO SKU OMS同步] 服务商[authId={}]未绑定系统仓库，sku_mapping.warehouse_id将为空", authId);
        }

        WegoSkuSyncDTO.SyncReqDTO syncReqDTO = new WegoSkuSyncDTO.SyncReqDTO();
        syncReqDTO.setAuthId(authId);
        syncReqDTO.setPlatform(OmsPlatformEnum.WE_GO.getCode());
        syncReqDTO.setWarehouseId(warehouseId);
        syncReqDTO.setWarehouseName(warehouseName);
        syncReqDTO.setSkuList(skuItems);

        try {
            Integer syncCount = omsListingInfoFeign.syncWarehouseNotMatchSku(syncReqDTO);
            log.info("[WEGO SKU OMS同步] 服务商[authId={}] 新增未匹配记录={}条",
                    authId, Objects.isNull(syncCount) ? 0 : syncCount);
        } catch (Exception e) {
            log.error("[WEGO SKU OMS同步] 服务商[authId={}] 调用OMS异常: {}",
                    authId, ExceptionUtil.getMessage(e), e);
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
