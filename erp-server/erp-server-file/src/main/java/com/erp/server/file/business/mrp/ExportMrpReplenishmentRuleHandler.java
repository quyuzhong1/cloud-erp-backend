package com.erp.server.file.business.mrp;

import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.mrp.dto.ReplenishmentSuggestionDTO;
import com.erp.rpc.mrp.feign.ExportMrpFeign;
import com.erp.server.file.core.AbstractPageFileEventHandler;
import com.erp.server.file.entity.FileTask;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

@Component
@Slf4j
public class ExportMrpReplenishmentRuleHandler extends AbstractPageFileEventHandler<ReplenishmentSuggestionDTO.ReplenishmentRuleExportDTO, ReplenishmentSuggestionDTO.PagingParamDTO> {

    @Resource
    private ExportMrpFeign exportMrpFeign;

    @Override
    protected List<ReplenishmentSuggestionDTO.ReplenishmentRuleExportDTO> getData(FileTask fileTask) {
        ReplenishmentSuggestionDTO.PagingParamDTO dto = readValue(fileTask.getMetaInfo(), new TypeReference<ReplenishmentSuggestionDTO.PagingParamDTO>() {
        });
        return listSeqData(dto);
    }

    @Override
    protected PagingVO<ReplenishmentSuggestionDTO.ReplenishmentRuleExportDTO> getPageData(PagingDTO<ReplenishmentSuggestionDTO.PagingParamDTO> dto) {
        return exportMrpFeign.listReplenishmentRule(dto);
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return FileTaskEventEnum.EXPORT_MRP_REPLENISHMENT_RULE;
    }

    @Override
    public String getExcelPath() {
           return "excel/mrp/replenishmentRule.xlsx";
    }
}
