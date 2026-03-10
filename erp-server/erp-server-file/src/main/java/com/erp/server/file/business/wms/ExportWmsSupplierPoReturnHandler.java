package com.erp.server.file.business.wms;

import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.wms.dto.PurchaseReturnOrderDTO;
import com.erp.rpc.wms.feign.ExportWmsFeign;
import com.erp.server.file.core.AbstractPageFileEventHandler;
import com.erp.server.file.entity.FileTask;
import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_WMS_SUPPLIER_PO_RETURN;


@Component
public class ExportWmsSupplierPoReturnHandler extends AbstractPageFileEventHandler<PurchaseReturnOrderDTO.SupplierPagingViewDTO, PurchaseReturnOrderDTO.SupplierPagingParamDTO> {
    @Resource
    private ExportWmsFeign exportWmsFeign;
    
    @Override
    protected List<PurchaseReturnOrderDTO.SupplierPagingViewDTO> getData(FileTask fileTask) {
        PurchaseReturnOrderDTO.SupplierPagingParamDTO dto = readValue(fileTask.getMetaInfo(), new TypeReference<PurchaseReturnOrderDTO.SupplierPagingParamDTO>() {
        });
        return listSeqData(dto);
    }

    @Override
    protected PagingVO<PurchaseReturnOrderDTO.SupplierPagingViewDTO> getPageData(PagingDTO<PurchaseReturnOrderDTO.SupplierPagingParamDTO> dto) {
        return exportWmsFeign.exportSupplierPoReturn(dto);
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return EXPORT_WMS_SUPPLIER_PO_RETURN;
    }

    @Override
    public String getExcelPath() {
        return "excel/wms/supplierPoReturnExport.xlsx";
    }
}
