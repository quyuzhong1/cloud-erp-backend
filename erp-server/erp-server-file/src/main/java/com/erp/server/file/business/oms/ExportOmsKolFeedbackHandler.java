package com.erp.server.file.business.oms;

import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.oms.dto.KolFeedbackDTO;
import com.erp.rpc.oms.feign.ExportOmsFeign;
import com.erp.server.file.core.AbstractPageFileEventHandler;
import com.erp.server.file.entity.FileTask;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import javax.annotation.Resource;
import java.util.List;
import static com.common.business.enums.FileTaskEventEnum.EXPORT_OMS_KOL_FEEDBACK;

/**
 * KOL回片列表导出
 * @date 2025-12-01
 * @author wuhaotian
 */
@Component
@Slf4j
public class ExportOmsKolFeedbackHandler extends AbstractPageFileEventHandler<KolFeedbackDTO.ListDTO, KolFeedbackDTO.ParamDTO> {

    @Resource
    private ExportOmsFeign exportOmsFeign;

    @Override
    protected PagingVO<KolFeedbackDTO.ListDTO> getPageData(PagingDTO<KolFeedbackDTO.ParamDTO> dto) {
        return exportOmsFeign.exportKolFeedback(dto);
    }

    @Override
    protected List<KolFeedbackDTO.ListDTO> getData(FileTask fileTask) {
        KolFeedbackDTO.ParamDTO dto = readValue(fileTask.getMetaInfo(), new TypeReference<KolFeedbackDTO.ParamDTO>() {});
        return listSeqData(dto);
    }

    @Override
    protected String getExcelPath() {
        return "excel/oms/kolFeedbackExport.xlsx";
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return EXPORT_OMS_KOL_FEEDBACK;
    }
}

