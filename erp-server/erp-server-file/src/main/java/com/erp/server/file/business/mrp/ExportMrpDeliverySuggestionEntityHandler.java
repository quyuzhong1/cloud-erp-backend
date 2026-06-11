package com.erp.server.file.business.mrp;

import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.mrp.dto.DeliverySuggestDTO;
import com.erp.rpc.mrp.feign.ExportMrpFeign;
import com.erp.server.file.core.AbstractPageFileEventHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
@Slf4j
public class ExportMrpDeliverySuggestionEntityHandler extends AbstractPageFileEventHandler<DeliverySuggestDTO.ListDTO, DeliverySuggestDTO.PagingParamDTO> {

    @Resource
    private ExportMrpFeign exportMrpFeign;


    @Override
    protected PagingVO<DeliverySuggestDTO.ListDTO> getPageData(PagingDTO<DeliverySuggestDTO.PagingParamDTO> dto) {
        return exportMrpFeign.pagingDeliverySuggestion(dto);
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return FileTaskEventEnum.EXPORT_MRP_DELIVERY_SUGGESTION_ENTITY;
    }

    @Override
    public String getExcelPath() {
           return "excel/mrp/deliverySuggestionEntity.xlsx";
    }
}
