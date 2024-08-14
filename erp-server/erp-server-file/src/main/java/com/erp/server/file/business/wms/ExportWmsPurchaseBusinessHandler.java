package com.erp.server.file.business.wms;

import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.scm.dto.PurchaseBusinessGatherTableDTO;
import com.erp.rpc.wms.feign.ExportWmsFeign;
import com.erp.server.file.core.AbstractPageFileEventHandler;
import com.erp.server.file.entity.FileTask;
import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_WMS_PURCHASE_BUSINESS;


@Component
public class ExportWmsPurchaseBusinessHandler extends AbstractPageFileEventHandler<PurchaseBusinessGatherTableDTO.PagingViewDTO, PurchaseBusinessGatherTableDTO.PagingParamDTO> {
    @Resource
    private ExportWmsFeign exportWmsFeign;
    @Override
    protected List<PurchaseBusinessGatherTableDTO.PagingViewDTO> getData(FileTask fileTask) {
        PurchaseBusinessGatherTableDTO.PagingParamDTO dto = readValue(fileTask.getMetaInfo(), new TypeReference<PurchaseBusinessGatherTableDTO.PagingParamDTO>() {
        });
        return listSeqData(dto);
    }

    @Override
    protected PagingVO<PurchaseBusinessGatherTableDTO.PagingViewDTO> getPageData(PagingDTO<PurchaseBusinessGatherTableDTO.PagingParamDTO> dto) {
        return exportWmsFeign.exportPurchaseBusiness(dto);
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return EXPORT_WMS_PURCHASE_BUSINESS;
    }

    @Override
    public String getExcelPath() {
        return "excel/wms/purchaseBusinessGatherExport.xlsx";
    }
}
