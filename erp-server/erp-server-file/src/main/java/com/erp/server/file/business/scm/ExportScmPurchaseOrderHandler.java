package com.erp.server.file.business.scm;

import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.scm.dto.PurchaseOrderDTO;
import com.erp.rpc.scm.feign.ExportScmFeign;
import com.erp.server.file.core.AbstractPageFileEventHandler;
import com.erp.model.file.entity.FileTask;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_SCM_PURCHASE_ORDER;

@Component
@Slf4j
public class ExportScmPurchaseOrderHandler extends AbstractPageFileEventHandler<PurchaseOrderDTO.ListDTO, PurchaseOrderDTO.SearchParamDTO> {
    @Resource
    private ExportScmFeign exportScmFeign;

    @Override
    protected PagingVO<PurchaseOrderDTO.ListDTO> getPageData(PagingDTO<PurchaseOrderDTO.SearchParamDTO> dto) {
        return exportScmFeign.exportPurchaseOrder(dto);
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return EXPORT_SCM_PURCHASE_ORDER;
    }

    @Override
    public String getExcelPath() {
        return "excel/scm/purchaseOrderExport.xlsx";
    }
}
