package com.erp.server.file.business.wms;

import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.wms.dto.QcEffectivenessDTO;
import com.erp.rpc.wms.feign.ExportWmsFeign;
import com.erp.server.file.core.AbstractPageFileEventHandler;
import com.erp.server.file.entity.FileTask;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

import static com.common.business.enums.FileTaskEventEnum.*;

@Component
@Slf4j
public class ExportWmsQcEffectivenessPersonnelHandler extends AbstractPageFileEventHandler<QcEffectivenessDTO.ViewQcForPersonnelDTO, QcEffectivenessDTO.ExportExcelSearchParamDTO> {

    @Resource
    private ExportWmsFeign exportWmsFeign;

    @Override
    protected List<QcEffectivenessDTO.ViewQcForPersonnelDTO> getData(FileTask fileTask) {
        QcEffectivenessDTO.ExportExcelSearchParamDTO dto = readValue(fileTask.getMetaInfo(), new TypeReference<QcEffectivenessDTO.ExportExcelSearchParamDTO>() {
        });
        return listSeqData(dto);
    }

    @Override
    protected PagingVO<QcEffectivenessDTO.ViewQcForPersonnelDTO> getPageData(PagingDTO<QcEffectivenessDTO.ExportExcelSearchParamDTO> dto) {
        return exportWmsFeign.exportQcEffectivenessPersonnel(dto);
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return EXPORT_WMS_QC_EFFECTIVENESS_PERSONNEL;
    }

    @Override
    public String getExcelPath() {
        return "excel/wms/qcEffectivenessPersonnel.xlsx";
    }
}
