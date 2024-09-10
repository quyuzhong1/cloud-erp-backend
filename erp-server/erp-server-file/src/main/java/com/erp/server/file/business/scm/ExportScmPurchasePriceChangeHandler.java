package com.erp.server.file.business.scm;

import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.scm.dto.PurchasePriceChangeDTO;
import com.erp.model.scm.dto.excel.PurchasePriceChangeExportExcelDTO;
import com.erp.rpc.scm.feign.ExportScmFeign;
import com.erp.server.file.core.AbstractPageFileEventHandler;
import com.erp.server.file.entity.FileTask;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_SCM_PURCHASE_PRICE_CHANGE;

@Component
@Slf4j
public class ExportScmPurchasePriceChangeHandler extends AbstractPageFileEventHandler<PurchasePriceChangeExportExcelDTO, PurchasePriceChangeDTO.PagingParamDTO> {
    @Resource
    private ExportScmFeign exportScmFeign;
    @Override
    protected List<PurchasePriceChangeExportExcelDTO> getData(FileTask fileTask) {
        PurchasePriceChangeDTO.PagingParamDTO dto = readValue(fileTask.getMetaInfo(), new TypeReference<PurchasePriceChangeDTO.PagingParamDTO>() {
        });
        return listSeqData(dto);
    }

    @Override
    protected PagingVO<PurchasePriceChangeExportExcelDTO> getPageData(PagingDTO<PurchasePriceChangeDTO.PagingParamDTO> dto) {
        return exportScmFeign.exportPurchasePriceChange(dto);
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return EXPORT_SCM_PURCHASE_PRICE_CHANGE;
    }

    @Override
    public String getExcelPath() {
        return "excel/scm/purchasePriceChange.xlsx";
    }
}
