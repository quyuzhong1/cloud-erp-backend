package com.erp.server.file.business.oms;

import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.oms.dto.SoPriceDTO;
import com.erp.model.oms.dto.excel.SoPriceExportExcelDTO;
import com.erp.rpc.oms.feign.ExportOmsFeign;
import com.erp.server.file.core.AbstractPageFileEventHandler;
import com.erp.server.file.entity.FileTask;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_SO_PRICE;

@Component
@Slf4j
public class ExportOmsSoPriceHandler extends AbstractPageFileEventHandler<SoPriceExportExcelDTO, SoPriceDTO.PagingParamDTO> {
    @Resource
    private ExportOmsFeign exportOmsFeign;
    @Override
    protected List<SoPriceExportExcelDTO> getData(FileTask fileTask) {
        SoPriceDTO.PagingParamDTO dto = readValue(fileTask.getMetaInfo(), new TypeReference<SoPriceDTO.PagingParamDTO>() {
        });
        return listSeqData(dto);
    }

    @Override
    protected PagingVO<SoPriceExportExcelDTO> getPageData(PagingDTO<SoPriceDTO.PagingParamDTO> dto) {
        return exportOmsFeign.exportSoPrice(dto);
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return EXPORT_SO_PRICE;
    }

    @Override
    public String getExcelPath() {
        return "excel/oms/soPrice.xlsx";
    }
}
