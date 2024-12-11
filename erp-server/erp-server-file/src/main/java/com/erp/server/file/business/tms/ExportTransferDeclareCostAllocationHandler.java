package com.erp.server.file.business.tms;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;

import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.tms.dto.TransferDeclareCostAllocationDTO;
import com.erp.rpc.tms.feign.ExportTmsFeign;
import com.erp.server.file.core.AbstractPageFileEventHandler;
import com.erp.server.file.entity.FileTask;
import com.fasterxml.jackson.core.type.TypeReference;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class ExportTransferDeclareCostAllocationHandler extends AbstractPageFileEventHandler<TransferDeclareCostAllocationDTO.ListDTO, TransferDeclareCostAllocationDTO.PagingParamDTO> {
    @Resource
    private ExportTmsFeign exportTmsFeign;

    @Override
    public String getExcelPath() {
        return "excel/tms/transferDeclareCostAllocation.xlsx";
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return FileTaskEventEnum.EXPORT_TRANSFER_DECLARE_COST_ALLOCATION;
    }

    @Override
    protected List<TransferDeclareCostAllocationDTO.ListDTO> getData(FileTask fileTask) {
        TransferDeclareCostAllocationDTO.PagingParamDTO dto = readValue(fileTask.getMetaInfo(), new TypeReference<TransferDeclareCostAllocationDTO.PagingParamDTO>() {
        });
        return listSeqData(dto);
    }

    @Override
    protected PagingVO<TransferDeclareCostAllocationDTO.ListDTO> getPageData(PagingDTO<TransferDeclareCostAllocationDTO.PagingParamDTO> dto) {
        return exportTmsFeign.exportTransferDeclareCostAllocation(dto);
    }
}
