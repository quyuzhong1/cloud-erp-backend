package com.erp.server.file.business.wms;

import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.wms.dto.QcApplicationDTO;
import com.erp.rpc.wms.feign.ExportWmsFeign;
import com.erp.server.file.core.AbstractPageFileEventHandler;
import com.erp.server.file.entity.FileTask;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_WMS_QC_APPLICATION;

/**
 * @Author: wtr
 * @Date: 2025/12/26 16:55
 * @Param:
 * @Return:
 * @Description:
 **/
@Component
@Slf4j
public class ExportWmsQcApplicationHandler extends AbstractPageFileEventHandler<QcApplicationDTO.ListDTO, QcApplicationDTO.PagingParamDTO> {

    @Resource
    private ExportWmsFeign exportWmsFeign;

    @Override
    protected PagingVO<QcApplicationDTO.ListDTO> getPageData(PagingDTO<QcApplicationDTO.PagingParamDTO> dto) {
        return exportWmsFeign.exportQcApplication(dto);
    }

    @Override
    protected List<QcApplicationDTO.ListDTO> getData(FileTask fileTask) {
        QcApplicationDTO.PagingParamDTO dto = readValue(fileTask.getMetaInfo(), new TypeReference<QcApplicationDTO.PagingParamDTO>() {
        });
        return listSeqData(dto);
    }

    @Override
    protected String getExcelPath() {
        return "excel/wms/qcApplicationExport.xlsx";
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return EXPORT_WMS_QC_APPLICATION;
    }
}