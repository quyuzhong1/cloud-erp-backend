package com.erp.server.file.business.wms;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.wms.dto.VirtualInventoryDiffDTO;
import com.erp.rpc.wms.feign.ExportWmsFeign;
import com.erp.server.file.core.AbstractSingleSheetGroupPageFileEventHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_WMS_VIRTUAL_INVENTORY_DIFF;


@Component
@Slf4j
public class ExportWmsVirtualInventoryDiffHandler extends AbstractSingleSheetGroupPageFileEventHandler<VirtualInventoryDiffDTO.ListDiffExportDataDTO, VirtualInventoryDiffDTO.SearchParamDTO> {
    @Resource
    private ExportWmsFeign exportWmsFeign;

    @Override
    public FileTaskEventEnum getEvent() {
        return EXPORT_WMS_VIRTUAL_INVENTORY_DIFF;
    }

    @Override
    public String getExcelPath() {
        return "excel/wms/virtualInventoryDiff.xlsx";
    }

    @Override
    protected PagingVO<VirtualInventoryDiffDTO.ListDiffExportDataDTO> getPageData(PagingDTO<VirtualInventoryDiffDTO.SearchParamDTO> dto) {
        return exportWmsFeign.exportListDiffExportData(dto);
    }

    /**
     * 虚拟库存差异导出以「SKU + 实体仓」作为单据维度。
     * <p>
     * 上游 {@code exportListDiffExportData}（{@code VirtualInventoryMapper.xml#listDiffExportData}）默认按
     * {@code diff.id asc, diff.virtualWarehouseId asc} 排序，ORDER BY 处已标注导出分组依赖，禁止随意调整：
     * {@code diff.id} 来源于 Inventory CTE 中按 {@code sku_id + warehouse_id} 聚合后的 {@code min(id)}，
     * 因此同一 {@code skuId + warehouseId} 维度天然连续；{@code virtualWarehouseId} 仅用于组内稳定排序。
     * 若后续调整导出排序，必须保持 {@code skuId + warehouseId} 连续，否则基类分组保护无法生效。
     */
    @Override
    protected Object sheetGroupKey(VirtualInventoryDiffDTO.ListDiffExportDataDTO row) {
        if (row == null) {
            return null;
        }
        return CharSequenceUtil.format("{}_{}", row.getSkuId(), row.getWarehouseId());
    }

    /**
     * 本导出会在同一 sku+仓库分组内置空重复展示字段，若分组非连续会产出错误展示，必须快速失败。
     */
    @Override
    protected boolean failOnNonContinuousSheetGroup() {
        return true;
    }

    /**
     * 基类已按 {@link #sheetGroupKey(VirtualInventoryDiffDTO.ListDiffExportDataDTO)} 合并跨页尾组；
     * 此处只处理单个完整维度内的展示字段置空。
     */
    @Override
    protected void beforeWriteGroupRows(Object groupKey, List<VirtualInventoryDiffDTO.ListDiffExportDataDTO> groupRows) {
        blankRepeatedRowsInGroup(groupRows);
    }

    /**
     * 单个 sku+仓库分组内仅首条保留实体仓维度字段，其余明细行置空，避免导出展示重复。
     */
    private void blankRepeatedRowsInGroup(List<VirtualInventoryDiffDTO.ListDiffExportDataDTO> groupRows) {
        if (CollUtil.isEmpty(groupRows)) {
            return;
        }
        for (int i = 1; i < groupRows.size(); i++) {
            VirtualInventoryDiffDTO.ListDiffExportDataDTO listDTO = groupRows.get(i);
            if (listDTO == null) {
                continue;
            }
            listDTO.setSkuNo("");
            listDTO.setProductName("");
            listDTO.setWarehouseName("");
            listDTO.setRealQty(null);
            listDTO.setUsableQty(null);
            listDTO.setFrozenQty(null);
            listDTO.setInTransitQty(null);
            listDTO.setWaitQcQty(null);
        }
    }
}
