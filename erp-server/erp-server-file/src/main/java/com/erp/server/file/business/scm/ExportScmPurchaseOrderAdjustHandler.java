package com.erp.server.file.business.scm;

import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.scm.dto.PurchaseOrderDTO;
import com.erp.rpc.scm.feign.ExportScmFeign;
import com.erp.server.file.core.AbstractPageFileEventHandler;
import com.erp.server.file.entity.FileTask;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_SCM_PURCHASE_ORDER_ADJUST;

@Component
@Slf4j
public class ExportScmPurchaseOrderAdjustHandler extends AbstractPageFileEventHandler<PurchaseOrderDTO.AdjustListDTO, PurchaseOrderDTO.SearchAdjustParamDTO> {
    @Resource
    private ExportScmFeign exportScmFeign;
    @Override
    protected List<PurchaseOrderDTO.AdjustListDTO> getData(FileTask fileTask) {
        PurchaseOrderDTO.SearchAdjustParamDTO dto = readValue(fileTask.getMetaInfo(), new TypeReference<PurchaseOrderDTO.SearchAdjustParamDTO>() {
        });
        return listSeqData(dto);
    }

    @Override
    protected PagingVO<PurchaseOrderDTO.AdjustListDTO> getPageData(PagingDTO<PurchaseOrderDTO.SearchAdjustParamDTO> dto) {
        return exportScmFeign.exportPurchaseOrderAdjust(dto);
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return EXPORT_SCM_PURCHASE_ORDER_ADJUST;
    }

    @Override
    public String getExcelPath() {
        return "excel/scm/purchaseOrderAdjustExport.xlsx";
    }
}
