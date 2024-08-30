package com.erp.server.file.business.tms;

import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.tms.dto.TmsDeclareBillDTO;
import com.erp.rpc.tms.feign.ExportTmsFeign;
import com.erp.server.file.core.AbstractPageFileEventHandler;
import com.erp.server.file.entity.FileTask;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_TMS_TMS_B2B_DECLARE_DECLARE_BILL;

@Component
@Slf4j
public class ExportTmsB2BDeclareBillDeclareHandler extends AbstractPageFileEventHandler<TmsDeclareBillDTO.ExportDTO, TmsDeclareBillDTO.PagingParamDTO> {
    @Resource
    private ExportTmsFeign exportTmsFeign;

    @Override
    public String getExcelPath() {
        return "excel/workflow/processDefinition.xlsx";
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return EXPORT_TMS_TMS_B2B_DECLARE_DECLARE_BILL;
    }

    @Override
    protected List<TmsDeclareBillDTO.ExportDTO> getData(FileTask fileTask) {
        TmsDeclareBillDTO.PagingParamDTO dto = readValue(fileTask.getMetaInfo(), new TypeReference<TmsDeclareBillDTO.PagingParamDTO>() {
        });
        return listSeqData(dto);
    }

    @Override
    protected PagingVO<TmsDeclareBillDTO.ExportDTO> getPageData(PagingDTO<TmsDeclareBillDTO.PagingParamDTO> dto) {
        return exportTmsFeign.exportB2BDeclareBillDeclare(dto);
    }
}
