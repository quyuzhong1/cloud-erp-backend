package com.erp.server.file.business.tms;

import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.tms.dto.LogisticsBillCostDTO;
import com.erp.rpc.tms.feign.ExportTmsFeign;
import com.erp.server.file.core.AbstractPageFileEventHandler;
import com.erp.server.file.entity.FileTask;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_TMS_LOGISTICS_LAST_MILE_COST;

@Component
@Slf4j
public class ExportTmsLogisticsLastMileCostHandler extends AbstractPageFileEventHandler<LogisticsBillCostDTO.ListDTO, LogisticsBillCostDTO.PagingParamDTO> {
    @Resource
    private ExportTmsFeign exportTmsFeign;

    @Override
    public String getExcelPath() {
        return "excel/tms/logisticsLastMileCost.xlsx";
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return EXPORT_TMS_LOGISTICS_LAST_MILE_COST;
    }

    @Override
    protected List<LogisticsBillCostDTO.ListDTO> getData(FileTask fileTask) {
        LogisticsBillCostDTO.PagingParamDTO dto = readValue(fileTask.getMetaInfo(), new TypeReference<LogisticsBillCostDTO.PagingParamDTO>() {
        });
        return listSeqData(dto);
    }

    @Override
    protected PagingVO<LogisticsBillCostDTO.ListDTO> getPageData(PagingDTO<LogisticsBillCostDTO.PagingParamDTO> dto) {
        return exportTmsFeign.exportLogisticsLastMileCost(dto);
    }
}
