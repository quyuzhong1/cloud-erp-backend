package com.erp.server.file.business.mrp;

import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.mrp.dto.ReplenishmentSuggestionDTO;
import com.erp.rpc.mrp.feign.ExportMrpFeign;
import com.erp.server.file.core.AbstractPageFileEventHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
@Slf4j
public class ExportMrpPurchaseSuggestionHandler extends AbstractPageFileEventHandler<ReplenishmentSuggestionDTO.PurchaseSuggestionDTO, ReplenishmentSuggestionDTO.PagingParamDTO> {

    @Resource
    private ExportMrpFeign exportMrpFeign;


    @Override
    protected PagingVO<ReplenishmentSuggestionDTO.PurchaseSuggestionDTO> getPageData(PagingDTO<ReplenishmentSuggestionDTO.PagingParamDTO> dto) {
        return exportMrpFeign.listPurchaseSuggestion(dto);
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return FileTaskEventEnum.EXPORT_MRP_PURCHASE_SUGGESTION;
    }

    @Override
    public String getExcelPath() {
           return "excel/mrp/purchaseSuggestion.xlsx";
    }
}
