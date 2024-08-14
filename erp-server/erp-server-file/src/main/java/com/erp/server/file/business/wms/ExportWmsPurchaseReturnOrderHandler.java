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

import static com.common.business.enums.FileTaskEventEnum.EXPORT_WMS_PURCHASE_RETURN_ORDER;


@Component
public class ExportWmsPurchaseReturnOrderHandler extends AbstractPageFileEventHandler<PurchaseReturnOrderDTO.PagingViewDTO, PurchaseReturnOrderDTO.PagingParamDTO> {
    @Resource
    private ExportWmsFeign exportWmsFeign;
    @Override
    protected List<PurchaseReturnOrderDTO.PagingViewDTO> getData(FileTask fileTask) {
        PurchaseReturnOrderDTO.PagingParamDTO dto = readValue(fileTask.getMetaInfo(), new TypeReference<PurchaseReturnOrderDTO.PagingParamDTO>() {
        });
        return listSeqData(dto);
    }

    @Override
    protected PagingVO<PurchaseReturnOrderDTO.PagingViewDTO> getPageData(PagingDTO<PurchaseReturnOrderDTO.PagingParamDTO> dto) {
        return exportWmsFeign.exportPurchaseReturnOrder(dto);
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return EXPORT_WMS_PURCHASE_RETURN_ORDER;
    }

    @Override
    public String getExcelPath() {
        return "excel/wms/poReturnExport.xlsx";
    }
}
