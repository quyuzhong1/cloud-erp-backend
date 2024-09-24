package com.erp.server.file.business.tms;

import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.tms.dto.FirstMileWeightAllocationDTO;
import com.erp.rpc.tms.feign.ExportTmsFeign;
import com.erp.server.file.core.AbstractPageFileEventHandler;
import com.erp.server.file.entity.FileTask;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_TMS_FM_WEIGHT_ALLOCATION;

/**
 * 重量分摊
 * @date 2024-09-11
 * @author tanmujin
 */
@Component
@Slf4j
public class ExportTmsFirstMileWeightAllocationHandler extends AbstractPageFileEventHandler<FirstMileWeightAllocationDTO.ViewDTO, FirstMileWeightAllocationDTO.PagingParamDTO> {

    @Resource
    private ExportTmsFeign exportTmsFeign;

    @Override
    protected PagingVO<FirstMileWeightAllocationDTO.ViewDTO> getPageData(PagingDTO<FirstMileWeightAllocationDTO.PagingParamDTO> dto) {
        return exportTmsFeign.exportFirstMileWeightAllocation(dto);
    }

    @Override
    protected List<FirstMileWeightAllocationDTO.ViewDTO> getData(FileTask fileTask) {
        FirstMileWeightAllocationDTO.PagingParamDTO dto = readValue(fileTask.getMetaInfo(), new TypeReference<FirstMileWeightAllocationDTO.PagingParamDTO>() {});
        return listSeqData(dto);
    }

    @Override
    protected String getExcelPath() {
        return "excel/tms/firstMileWeightAllocationExport.xlsx";
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return EXPORT_TMS_FM_WEIGHT_ALLOCATION;
    }
}
