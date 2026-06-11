package com.erp.server.file.business.mrp;

import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.mrp.dto.PurchaseSuggestMergeDTO;
import com.erp.rpc.mrp.feign.ExportMrpFeign;
import com.erp.server.file.core.AbstractPageFileEventHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
@Slf4j
public class ExportMrpPurchaseSuggestionMergeEntityHandler extends AbstractPageFileEventHandler<PurchaseSuggestMergeDTO.ListDTO, PurchaseSuggestMergeDTO.PagingParamDTO> {

    @Resource
    private ExportMrpFeign exportMrpFeign;


    @Override
    protected PagingVO<PurchaseSuggestMergeDTO.ListDTO> getPageData(PagingDTO<PurchaseSuggestMergeDTO.PagingParamDTO> dto) {
        return exportMrpFeign.pagingPurchaseSuggestionMerge(dto);
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return FileTaskEventEnum.EXPORT_MRP_PURCHASE_SUGGESTION_MERGE_ENTITY;
    }

    @Override
    public String getExcelPath() {
           return "excel/mrp/purchaseSuggestionMergeEntity.xlsx";
    }
}
