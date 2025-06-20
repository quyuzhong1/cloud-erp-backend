package com.erp.server.file.business.wms;

import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.wms.dto.SupplierInventoryDTO;
import com.erp.rpc.wms.feign.ExportWmsFeign;
import com.erp.server.file.core.AbstractPageFileEventHandler;
import com.erp.server.file.entity.FileTask;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_SCM_SUPPLIER_INVENTORY;

@Component
@Slf4j
public class ExportSupplierInventoryHandler extends AbstractPageFileEventHandler<SupplierInventoryDTO.ListDTO, SupplierInventoryDTO.PagingParamDTO> {
    @Resource
    private ExportWmsFeign exportWmsFeign;
    @Override
    protected List<SupplierInventoryDTO.ListDTO> getData(FileTask fileTask) {
        SupplierInventoryDTO.PagingParamDTO dto = readValue(fileTask.getMetaInfo(), new TypeReference<SupplierInventoryDTO.PagingParamDTO>() {
        });
        return listSeqData(dto);
    }

    @Override
    protected PagingVO<SupplierInventoryDTO.ListDTO> getPageData(PagingDTO<SupplierInventoryDTO.PagingParamDTO> dto) {
        return exportWmsFeign.exportSupplierInventory(dto);
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return EXPORT_SCM_SUPPLIER_INVENTORY;
    }

    @Override
    public String getExcelPath() {
        return "excel/scm/supplierInventory.xlsx";
    }
}
