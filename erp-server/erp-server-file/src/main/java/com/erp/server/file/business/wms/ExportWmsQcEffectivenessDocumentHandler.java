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

import static com.common.business.enums.FileTaskEventEnum.EXPORT_WMS_QC_EFFECTIVENESS_DOCUMENT;

@Component
@Slf4j
public class ExportWmsQcEffectivenessDocumentHandler extends AbstractPageFileEventHandler<QcEffectivenessDTO.ViewQcForDocumentDTO, QcEffectivenessDTO.ExportExcelSearchParamDTO> {

    @Resource
    private ExportWmsFeign exportWmsFeign;

    @Override
    protected List<QcEffectivenessDTO.ViewQcForDocumentDTO> getData(FileTask fileTask) {
        QcEffectivenessDTO.ExportExcelSearchParamDTO dto = readValue(fileTask.getMetaInfo(), new TypeReference<QcEffectivenessDTO.ExportExcelSearchParamDTO>() {
        });
        return listSeqData(dto);
    }

    @Override
    protected PagingVO<QcEffectivenessDTO.ViewQcForDocumentDTO> getPageData(PagingDTO<QcEffectivenessDTO.ExportExcelSearchParamDTO> dto) {
        return exportWmsFeign.exportQcEffectivenessDocument(dto);
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return EXPORT_WMS_QC_EFFECTIVENESS_DOCUMENT;
    }

    @Override
    public String getExcelPath() {
        return "excel/wms/qcEffectivenessDocument.xlsx";
    }
}
