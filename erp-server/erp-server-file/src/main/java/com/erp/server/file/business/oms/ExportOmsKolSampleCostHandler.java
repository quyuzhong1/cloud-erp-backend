package com.erp.server.file.business.oms;

import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.oms.dto.KolSampleCostDTO;
import com.erp.rpc.oms.feign.ExportOmsFeign;
import com.erp.server.file.core.AbstractPageFileEventHandler;
import com.erp.server.file.entity.FileTask;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_OMS_KOL_SAMPLE_COST_REPORT;

/**
 * 样品借用导出
 * @date 2025-08-21
 * @author jack
 */
@Component
@Slf4j
public class ExportOmsKolSampleCostHandler extends AbstractPageFileEventHandler<KolSampleCostDTO.ListDTO, KolSampleCostDTO.PagingParamDTO> {

    @Resource
    private ExportOmsFeign exportOmsFeign;

    @Override
    protected PagingVO<KolSampleCostDTO.ListDTO> getPageData(PagingDTO<KolSampleCostDTO.PagingParamDTO> dto) {
        return exportOmsFeign.exportKolSampleCost(dto);
    }

    @Override
    protected List<KolSampleCostDTO.ListDTO> getData(FileTask fileTask) {
        KolSampleCostDTO.PagingParamDTO dto = readValue(fileTask.getMetaInfo(), new TypeReference<KolSampleCostDTO.PagingParamDTO>() {});
        return listSeqData(dto);
    }

    @Override
    protected String getExcelPath() {
        return "excel/oms/kolSampleCostExport.xlsx";
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return EXPORT_OMS_KOL_SAMPLE_COST_REPORT;
    }
}
