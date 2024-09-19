package com.erp.server.file.business.oms;

import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.oms.dto.SoB2cDTO;
import com.erp.rpc.oms.feign.ExportOmsFeign;
import com.erp.server.file.core.AbstractPageFileEventHandler;
import com.erp.server.file.entity.FileTask;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_OMS_SO_B2C;

@Component
@Slf4j
public class ExportOmsSoB2CHandler extends AbstractPageFileEventHandler<SoB2cDTO.ExcelExportDTO, SoB2cDTO.ExportParamDTO> {

    @Resource
    private ExportOmsFeign exportOmsFeign;
    @Override
    protected List<SoB2cDTO.ExcelExportDTO> getData(FileTask fileTask) {
        SoB2cDTO.ExportParamDTO dto = readValue(fileTask.getMetaInfo(), new TypeReference<SoB2cDTO.ExportParamDTO>() {
        });
        return listSeqData(dto);
    }

    @Override
    protected PagingVO<SoB2cDTO.ExcelExportDTO> getPageData(PagingDTO<SoB2cDTO.ExportParamDTO> dto) {
        return exportOmsFeign.exportSoB2C(dto);
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return EXPORT_OMS_SO_B2C;
    }

    @Override
    public String getExcelPath() {
        return "excel/oms/soB2c.xlsx";
    }

    @Override
    protected int getPageSize() {
        return 50000;
    }
}
