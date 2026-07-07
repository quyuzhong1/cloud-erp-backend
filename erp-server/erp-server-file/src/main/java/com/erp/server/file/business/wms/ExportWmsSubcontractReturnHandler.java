package com.erp.server.file.business.wms;

import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.wms.dto.SubcontractReturnDTO;
import com.erp.rpc.wms.feign.ExportWmsFeign;
import com.erp.server.file.core.AbstractPageFileEventHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_WMS_SUBCONTRACT_RETURN;

@Component
@Slf4j
public class ExportWmsSubcontractReturnHandler extends AbstractPageFileEventHandler<SubcontractReturnDTO.ListDTO, SubcontractReturnDTO.PagingParamDTO> {
    @Resource
    private ExportWmsFeign exportWmsFeign;

    @Override
    public String getExcelPath() {
        return "excel/wms/subcontractReturn.xlsx";
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return EXPORT_WMS_SUBCONTRACT_RETURN;
    }



    @Override
    protected PagingVO<SubcontractReturnDTO.ListDTO> getPageData(PagingDTO<SubcontractReturnDTO.PagingParamDTO> dto) {
        return exportWmsFeign.exportSubcontractReturn(dto);
    }
}
