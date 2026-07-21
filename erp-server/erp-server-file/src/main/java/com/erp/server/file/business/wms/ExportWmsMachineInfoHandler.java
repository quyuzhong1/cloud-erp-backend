package com.erp.server.file.business.wms;

import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.wms.dto.MachineInfoDTO;
import com.erp.rpc.wms.feign.ExportWmsFeign;
import com.erp.server.file.core.AbstractPageFileEventHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_WMS_MACHINE_INFO;

@Component
@Slf4j
public class ExportWmsMachineInfoHandler extends AbstractPageFileEventHandler<MachineInfoDTO.ListDTO, MachineInfoDTO.SearchParamDTO> {

    @Resource
    private ExportWmsFeign exportWmsFeign;


    @Override
    protected PagingVO<MachineInfoDTO.ListDTO> getPageData(PagingDTO<MachineInfoDTO.SearchParamDTO> dto) {
        return exportWmsFeign.exportMachineInfo(dto);
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return EXPORT_WMS_MACHINE_INFO;
    }

    @Override
    public String getExcelPath() {
        return "excel/wms/machineInfo.xlsx";
    }
}
