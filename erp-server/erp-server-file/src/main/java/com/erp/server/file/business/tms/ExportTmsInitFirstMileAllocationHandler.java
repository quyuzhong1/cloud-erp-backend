package com.erp.server.file.business.tms;

import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.tms.dto.InitFirstMileAllocationDTO;
import com.erp.rpc.tms.feign.ExportTmsFeign;
import com.erp.server.file.core.AbstractPageFileEventHandler;
import com.erp.server.file.entity.FileTask;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_TMS_INIT_FIRST_MILE_ALLOCATION;

@Component
@Slf4j
public class ExportTmsInitFirstMileAllocationHandler extends AbstractPageFileEventHandler<InitFirstMileAllocationDTO.PagingVO, InitFirstMileAllocationDTO.PagingParamDTO> {
    @Resource
    private ExportTmsFeign exportTmsFeign;

    @Override
    public String getExcelPath() {
        return "excel/tms/initFirstMileAllocationExport.xlsx";
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return EXPORT_TMS_INIT_FIRST_MILE_ALLOCATION;
    }

    @Override
    protected List<InitFirstMileAllocationDTO.PagingVO> getData(FileTask fileTask) {
        InitFirstMileAllocationDTO.PagingParamDTO dto = readValue(fileTask.getMetaInfo(), new TypeReference<InitFirstMileAllocationDTO.PagingParamDTO>() {
        });
        return listSeqData(dto);
    }

    @Override
    protected PagingVO<InitFirstMileAllocationDTO.PagingVO> getPageData(PagingDTO<InitFirstMileAllocationDTO.PagingParamDTO> dto) {
        return exportTmsFeign.exportInitFirstMileAllocation(dto);
    }
}
