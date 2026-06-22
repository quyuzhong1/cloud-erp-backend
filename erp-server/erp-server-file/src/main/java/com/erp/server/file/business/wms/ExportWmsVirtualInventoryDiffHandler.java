package com.erp.server.file.business.wms;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.wms.dto.VirtualInventoryDiffDTO;
import com.erp.rpc.wms.feign.ExportWmsFeign;
import com.erp.server.file.core.AbstractPageFileEventHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_WMS_VIRTUAL_INVENTORY_DIFF;


@Component
@Slf4j
public class ExportWmsVirtualInventoryDiffHandler extends AbstractPageFileEventHandler<VirtualInventoryDiffDTO.ListDiffExportDataDTO, VirtualInventoryDiffDTO.SearchParamDTO> {
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

    @Override
    protected void afterFetchPage(List<VirtualInventoryDiffDTO.ListDiffExportDataDTO> pageList) {
        blankRepeatedSkuWarehouse(pageList);
    }

    /**
     * 相同 sku+仓库仅首条保留，其余置空。改为按页判重：流式分页后跨页/跨 sheet 的同组首行会各自保留，
     * 与历史全量判重相比，分页边界处少量重复行会重新展示，不影响数据正确性。
     */
    private void blankRepeatedSkuWarehouse(List<VirtualInventoryDiffDTO.ListDiffExportDataDTO> pageList) {
        if (CollUtil.isEmpty(pageList)) {
            return;
        }
        List<String> flagList = new ArrayList<>();
        for (VirtualInventoryDiffDTO.ListDiffExportDataDTO listDTO : pageList) {
            if (listDTO == null) {
                continue;
            }
            String flag = CharSequenceUtil.format("{}_{}", listDTO.getSkuId(), listDTO.getWarehouseId());
            if (flagList.contains(flag)) {
                listDTO.setSkuNo("");
                listDTO.setProductName("");
                listDTO.setWarehouseName("");
                listDTO.setRealQty(null);
                listDTO.setUsableQty(null);
                listDTO.setFrozenQty(null);
                listDTO.setInTransitQty(null);
                listDTO.setWaitQcQty(null);
                continue;
            }
            flagList.add(flag);
        }
    }
}
