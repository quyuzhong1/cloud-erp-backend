package com.erp.server.file.business.tms;

import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.tms.dto.FirstMileCostAllocationDTO;
import com.erp.rpc.tms.feign.ExportTmsFeign;
import com.erp.server.file.core.AbstractPageFileEventHandler;
import com.erp.server.file.entity.FileTask;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_TMS_FIRST_MILE_COST_ALLOCATION;

@Component
@Slf4j
public class ExportTmsFirstMileCostAllocationHandler extends AbstractPageFileEventHandler<FirstMileCostAllocationDTO.PagingVO, FirstMileCostAllocationDTO.PagingParamDTO> {
    @Resource
    private ExportTmsFeign exportTmsFeign;

    @Override
    public String getExcelPath() {
        return "excel/tms/firstMileCostAllocationExport.xlsx";
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return EXPORT_TMS_FIRST_MILE_COST_ALLOCATION;
    }

    @Override
    protected List<FirstMileCostAllocationDTO.PagingVO> getData(FileTask fileTask) {
        FirstMileCostAllocationDTO.PagingParamDTO dto = readValue(fileTask.getMetaInfo(), new TypeReference<FirstMileCostAllocationDTO.PagingParamDTO>() {
        });
        return listSeqData(dto);
    }

    @Override
    protected PagingVO<FirstMileCostAllocationDTO.PagingVO> getPageData(PagingDTO<FirstMileCostAllocationDTO.PagingParamDTO> dto) {
        return exportTmsFeign.exportFirstMileCostAllocation(dto);
    }
}
