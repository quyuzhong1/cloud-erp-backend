package com.erp.server.file.business.oms;

import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.oms.dto.SoB2cDeclareProductDTO;
import com.erp.rpc.oms.feign.ExportOmsFeign;
import com.erp.server.file.core.AbstractPageFileEventHandler;
import com.erp.server.file.entity.FileTask;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_OMS_SO_B2C_DECLARE;

@Component
@Slf4j
public class ExportOmsSoB2CDeclareHandler extends AbstractPageFileEventHandler<SoB2cDeclareProductDTO.ViewDTO, SoB2cDeclareProductDTO.ListDTO> {

    @Resource
    private ExportOmsFeign exportOmsFeign;

    @Override
    protected List<SoB2cDeclareProductDTO.ViewDTO> getData(FileTask fileTask) {
        SoB2cDeclareProductDTO.ListDTO dto = readValue(fileTask.getMetaInfo(), new TypeReference<SoB2cDeclareProductDTO.ListDTO>() {
        });
        return listSeqData(dto);
    }

    @Override
    protected PagingVO<SoB2cDeclareProductDTO.ViewDTO> getPageData(PagingDTO<SoB2cDeclareProductDTO.ListDTO> dto) {
        return exportOmsFeign.exportSoB2CDeclare(dto);
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return EXPORT_OMS_SO_B2C_DECLARE;
    }

    @Override
    public String getExcelPath() {
        return "excel/oms/b2cDeclareProductExport.xlsx";
    }
}
