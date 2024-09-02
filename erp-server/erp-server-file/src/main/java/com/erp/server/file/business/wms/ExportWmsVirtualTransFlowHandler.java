package com.erp.server.file.business.wms;

import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.wms.dto.TransferOutDTO;
import com.erp.model.wms.dto.VirtualTransFlowDTO;
import com.erp.rpc.wms.feign.ExportWmsFeign;
import com.erp.server.file.core.AbstractPageFileEventHandler;
import com.erp.server.file.entity.FileTask;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_WMS_VIRTUAL_TRANS_FLOW;

@Component
@Slf4j
public class ExportWmsVirtualTransFlowHandler extends AbstractPageFileEventHandler<VirtualTransFlowDTO.ListDTO, VirtualTransFlowDTO.SearchParamDTO> {
    @Resource
    private ExportWmsFeign exportWmsFeign;

    @Override
    public String getExcelPath() {
        return "excel/wms/virtualTransFlow.xlsx";
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return EXPORT_WMS_VIRTUAL_TRANS_FLOW;
    }

    @Override
    protected List<VirtualTransFlowDTO.ListDTO> getData(FileTask fileTask) {
        VirtualTransFlowDTO.SearchParamDTO dto = readValue(fileTask.getMetaInfo(), new TypeReference<VirtualTransFlowDTO.SearchParamDTO>() {
        });
        return listSeqData(dto);
    }


    @Override
    protected PagingVO<VirtualTransFlowDTO.ListDTO> getPageData(PagingDTO<VirtualTransFlowDTO.SearchParamDTO> dto) {
        return exportWmsFeign.exportVirtualTransFlow(dto);
    }
}
