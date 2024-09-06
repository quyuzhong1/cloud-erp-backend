package com.erp.server.file.business.mrp;

import com.common.business.dto.DynamicExcelDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.mrp.dto.ReplenishmentSuggestionDTO;
import com.erp.rpc.mrp.feign.ExportMrpFeign;
import com.erp.server.file.core.AbstractDynamicHeadersFileEventHandler;
import com.erp.server.file.entity.FileTask;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
@Slf4j
public class ExportMrpHistorySalesQtyHandler extends AbstractDynamicHeadersFileEventHandler<ReplenishmentSuggestionDTO.PagingParamDTO> {

    @Resource
    private ExportMrpFeign exportMrpFeign;

    @Override
    protected DynamicExcelDTO getData(FileTask fileTask) {
        ReplenishmentSuggestionDTO.PagingParamDTO dto = readValue(fileTask.getMetaInfo(), new TypeReference<ReplenishmentSuggestionDTO.PagingParamDTO>() {
        });
        return listSeqData(dto);
    }

    @Override
    protected PagingVO<DynamicExcelDTO> getPageData(PagingDTO<ReplenishmentSuggestionDTO.PagingParamDTO> dto) {
        return exportMrpFeign.listHistorySalesQty(dto);
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return FileTaskEventEnum.EXPORT_MRP_HISTORY_SALES_QTY;
    }

}
