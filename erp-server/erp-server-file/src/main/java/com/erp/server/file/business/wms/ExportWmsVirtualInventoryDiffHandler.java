package com.erp.server.file.business.wms;

import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.wms.dto.VirtualInventoryDiffDTO;
import com.erp.rpc.wms.feign.ExportWmsFeign;
import com.erp.server.file.core.AbstractPageFileEventHandler;
import com.erp.server.file.entity.FileTask;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_WMS_VIRTUAL_INVENTORY_DIFF;


@Component
@Slf4j
public class ExportWmsVirtualInventoryDiffHandler extends AbstractPageFileEventHandler<VirtualInventoryDiffDTO.ListDiffExportDataDTO, VirtualInventoryDiffDTO.SearchParamDTO> {
    @Resource
    private ExportWmsFeign exportWmsFeign;

    @Override
    protected  List<VirtualInventoryDiffDTO.ListDiffExportDataDTO> getData(FileTask fileTask) {
        VirtualInventoryDiffDTO.SearchParamDTO dto = readValue(fileTask.getMetaInfo(), new TypeReference<VirtualInventoryDiffDTO.SearchParamDTO>() {
        });
        return listSeqData(dto);
    }

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
}
