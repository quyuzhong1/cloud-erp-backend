package com.erp.server.file.business.scm;

import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.scm.dto.PurchasePriceDTO;
import com.erp.model.scm.dto.excel.PurchasePriceExportExcelDTO;
import com.erp.rpc.scm.feign.ExportScmFeign;
import com.erp.server.file.core.AbstractPageFileEventHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_SCM_PURCHASE_PRICE;

@Component
@Slf4j
public class ExportScmPurchasePriceHandler extends AbstractPageFileEventHandler<PurchasePriceExportExcelDTO, PurchasePriceDTO.PagingParamDTO> {
    @Resource
    private ExportScmFeign exportScmFeign;

    @Override
    protected PagingVO<PurchasePriceExportExcelDTO> getPageData(PagingDTO<PurchasePriceDTO.PagingParamDTO> dto) {
        return exportScmFeign.exportPurchasePrice(dto);
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return EXPORT_SCM_PURCHASE_PRICE;
    }

    @Override
    public String getExcelPath() {
        return "excel/scm/purchasePrice.xlsx";
    }
}
