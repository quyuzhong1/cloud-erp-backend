package com.erp.server.file.business.tms;

import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.tms.dto.TransferDeclareDTO;
import com.erp.rpc.tms.feign.ExportTmsFeign;
import com.erp.rpc.workflow.ExportWorkflowFeign;
import com.erp.server.file.core.AbstractPageFileEventHandler;
import com.erp.server.file.entity.FileTask;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_TMS_TRANSFER_DECLARE;

@Component
@Slf4j
public class ExportTmsTransferDeclareHandler extends AbstractPageFileEventHandler<TransferDeclareDTO.ExportListDTO, TransferDeclareDTO.PagingParamDTO> {
    @Resource
    private ExportTmsFeign exportTmsFeign;

    @Override
    public String getExcelPath() {
        return "excel/tms/transferDeclare.xlsx";
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return EXPORT_TMS_TRANSFER_DECLARE;
    }

    @Override
    protected List<TransferDeclareDTO.ExportListDTO> getData(FileTask fileTask) {
        TransferDeclareDTO.PagingParamDTO dto = readValue(fileTask.getMetaInfo(), new TypeReference<TransferDeclareDTO.PagingParamDTO>() {
        });
        return listSeqData(dto);
    }

    @Override
    protected PagingVO<TransferDeclareDTO.ExportListDTO> getPageData(PagingDTO<TransferDeclareDTO.PagingParamDTO> dto) {
        return exportTmsFeign.exportTransferDeclare(dto);
    }
}
