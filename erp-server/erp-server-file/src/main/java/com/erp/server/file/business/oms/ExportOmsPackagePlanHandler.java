package com.erp.server.file.business.oms;

import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.oms.dto.PackagePlanDTO;
import com.erp.rpc.oms.feign.ExportOmsFeign;
import com.erp.server.file.core.AbstractPageFileEventHandler;
import com.erp.server.file.entity.FileTask;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_OMS_PACKAGE_PLAN;

@Component
@Slf4j
public class ExportOmsPackagePlanHandler extends AbstractPageFileEventHandler<PackagePlanDTO.ExportDTO, PackagePlanDTO.PagingParamDTO> {
    @Resource
    private ExportOmsFeign exportOmsFeign;
    @Override
    protected List<PackagePlanDTO.ExportDTO> getData(FileTask fileTask) {
        PackagePlanDTO.PagingParamDTO dto = readValue(fileTask.getMetaInfo(), new TypeReference<PackagePlanDTO.PagingParamDTO>() {
        });
        return listSeqData(dto);
    }

    @Override
    protected PagingVO<PackagePlanDTO.ExportDTO> getPageData(PagingDTO<PackagePlanDTO.PagingParamDTO> dto) {
        return exportOmsFeign.exportPackagePlan(dto);
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return EXPORT_OMS_PACKAGE_PLAN;
    }

    @Override
    public String getExcelPath() {
        return "excel/oms/PackagePlan.xlsx";
    }
}
