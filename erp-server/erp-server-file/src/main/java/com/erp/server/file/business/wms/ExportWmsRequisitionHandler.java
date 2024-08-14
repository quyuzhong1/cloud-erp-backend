package com.erp.server.file.business.wms;

import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.wms.dto.RequisitionApplicationDTO;
import com.erp.rpc.wms.feign.ExportWmsFeign;
import com.erp.server.file.core.AbstractPageFileEventHandler;
import com.erp.server.file.entity.FileTask;
import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_WMS_REQUISITION_APPLICATION;


@Component
public class ExportWmsRequisitionHandler extends AbstractPageFileEventHandler<RequisitionApplicationDTO.ListDTO, RequisitionApplicationDTO.PagingParamDTO> {
    @Resource
    private ExportWmsFeign exportWmsFeign;
    @Override
    protected List<RequisitionApplicationDTO.ListDTO> getData(FileTask fileTask) {
        RequisitionApplicationDTO.PagingParamDTO dto = readValue(fileTask.getMetaInfo(), new TypeReference<RequisitionApplicationDTO.PagingParamDTO>() {
        });
        return listSeqData(dto);
    }

    @Override
    protected PagingVO<RequisitionApplicationDTO.ListDTO> getPageData(PagingDTO<RequisitionApplicationDTO.PagingParamDTO> dto) {
        return exportWmsFeign.exportRequisitionApplication(dto);
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return EXPORT_WMS_REQUISITION_APPLICATION;
    }

    @Override
    public String getExcelPath() {
        return "excel/wms/requisitionApplicationExport.xlsx";
    }
}
