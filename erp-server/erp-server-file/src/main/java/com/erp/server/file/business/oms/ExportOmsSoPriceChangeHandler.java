package com.erp.server.file.business.oms;

import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.oms.dto.SoPriceChangeDTO;
import com.erp.model.oms.dto.excel.SoPriceChangeExportExcelDTO;
import com.erp.rpc.oms.feign.ExportOmsFeign;
import com.erp.server.file.core.AbstractPageFileEventHandler;
import com.erp.model.file.entity.FileTask;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_SO_PRICE_CHANGE;

@Component
@Slf4j
public class ExportOmsSoPriceChangeHandler extends AbstractPageFileEventHandler<SoPriceChangeExportExcelDTO, SoPriceChangeDTO.PagingParamDTO> {
    @Resource
    private ExportOmsFeign exportOmsFeign;
    @Override
    protected List<SoPriceChangeExportExcelDTO> getData(FileTask fileTask) {
        SoPriceChangeDTO.PagingParamDTO dto = readValue(fileTask.getMetaInfo(), new TypeReference<SoPriceChangeDTO.PagingParamDTO>() {
        });
        return listSeqData(dto);
    }

    @Override
    protected PagingVO<SoPriceChangeExportExcelDTO> getPageData(PagingDTO<SoPriceChangeDTO.PagingParamDTO> dto) {
        return exportOmsFeign.exportSoPriceChange(dto);
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return EXPORT_SO_PRICE_CHANGE;
    }

    @Override
    public String getExcelPath() {
        return "excel/oms/soPriceChange.xlsx";
    }
}
