package com.erp.server.file.business.oms;

import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.oms.dto.KolFeedbackCostDTO;
import com.erp.rpc.oms.feign.ExportOmsFeign;
import com.erp.server.file.core.AbstractPageFileEventHandler;
import com.erp.server.file.entity.FileTask;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import javax.annotation.Resource;
import java.util.List;
import static com.common.business.enums.FileTaskEventEnum.EXPORT_OMS_KOL_FEEDBACK_COST;

/**
 * KOL回片费用导出
 * @date 2025-12-03
 * @author wuhaotian
 */
@Component
@Slf4j
public class ExportOmsKolFeedbackCostHandler extends AbstractPageFileEventHandler<KolFeedbackCostDTO.ListDTO, KolFeedbackCostDTO.ParamDTO> {

    @Resource
    private ExportOmsFeign exportOmsFeign;

    @Override
    protected PagingVO<KolFeedbackCostDTO.ListDTO> getPageData(PagingDTO<KolFeedbackCostDTO.ParamDTO> dto) {
        return exportOmsFeign.exportKolFeedbackCost(dto);
    }

    @Override
    protected List<KolFeedbackCostDTO.ListDTO> getData(FileTask fileTask) {
        KolFeedbackCostDTO.ParamDTO dto = readValue(fileTask.getMetaInfo(), new TypeReference<KolFeedbackCostDTO.ParamDTO>() {});
        return listSeqData(dto);
    }

    @Override
    protected String getExcelPath() {
        return "excel/oms/kolFeedbackCostExport.xlsx";
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return EXPORT_OMS_KOL_FEEDBACK_COST;
    }
}

