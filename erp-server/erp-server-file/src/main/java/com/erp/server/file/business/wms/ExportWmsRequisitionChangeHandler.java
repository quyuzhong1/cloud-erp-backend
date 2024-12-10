package com.erp.server.file.business.wms;

import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.wms.dto.RequisitionApplicationChangeDTO;
import com.erp.rpc.wms.feign.ExportWmsFeign;
import com.erp.server.file.core.AbstractPageFileEventHandler;
import com.erp.server.file.entity.FileTask;
import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_WMS_REQUISITION_APPLICATION_CHANGE;


@Component
public class ExportWmsRequisitionChangeHandler extends AbstractPageFileEventHandler<RequisitionApplicationChangeDTO.ListDTO, RequisitionApplicationChangeDTO.PagingParamDTO> {
    @Resource
    private ExportWmsFeign exportWmsFeign;
    @Override
    protected List<RequisitionApplicationChangeDTO.ListDTO> getData(FileTask fileTask) {
        RequisitionApplicationChangeDTO.PagingParamDTO dto = readValue(fileTask.getMetaInfo(), new TypeReference<RequisitionApplicationChangeDTO.PagingParamDTO>() {
        });
        return listSeqData(dto);
    }

    @Override
    protected PagingVO<RequisitionApplicationChangeDTO.ListDTO> getPageData(PagingDTO<RequisitionApplicationChangeDTO.PagingParamDTO> dto) {
        return exportWmsFeign.exportRequisitionApplicationChange(dto);
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return EXPORT_WMS_REQUISITION_APPLICATION_CHANGE;
    }

    @Override
    public String getExcelPath() {
        return "excel/wms/requisitionApplicationChangeExport.xlsx";
    }
}
