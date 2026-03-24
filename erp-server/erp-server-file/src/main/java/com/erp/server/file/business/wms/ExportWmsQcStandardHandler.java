package com.erp.server.file.business.wms;

import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.wms.dto.QcStandardDTO;
import com.erp.rpc.wms.feign.ExportWmsFeign;
import com.erp.server.file.core.AbstractPageFileEventHandler;
import com.erp.server.file.entity.FileTask;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_WMS_QC_STANDARD;

/**
 * 质检标准异步导出处理器
 *
 * @author jack
 * @since 2026-03-22
 */
@Component
@Slf4j
public class ExportWmsQcStandardHandler extends AbstractPageFileEventHandler<QcStandardDTO.ListDTO, QcStandardDTO.PagingParamDTO> {

    @Resource
    private ExportWmsFeign exportWmsFeign;

    @Override
    protected List<QcStandardDTO.ListDTO> getData(FileTask fileTask) {
        QcStandardDTO.PagingParamDTO dto = readValue(fileTask.getMetaInfo(), new TypeReference<QcStandardDTO.PagingParamDTO>() {
        });
        return listSeqData(dto);
    }

    @Override
    protected PagingVO<QcStandardDTO.ListDTO> getPageData(PagingDTO<QcStandardDTO.PagingParamDTO> dto) {
        return exportWmsFeign.exportQcStandard(dto);
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return EXPORT_WMS_QC_STANDARD;
    }

    @Override
    public String getExcelPath() {
        return "excel/wms/qcStandard.xlsx";
    }
}
