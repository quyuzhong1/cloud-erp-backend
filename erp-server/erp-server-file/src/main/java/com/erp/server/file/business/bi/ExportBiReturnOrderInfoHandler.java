package com.erp.server.file.business.bi;

import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.dmp.dto.DmpReturnOrderInfoExcelDTO;
import com.erp.model.dmp.dto.DmpReturnOrderInfoSearchDTO;
import com.erp.rpc.bi.feign.ExportBiFeign;
import com.erp.server.file.core.AbstractPageFileEventHandler;
import com.erp.server.file.entity.FileTask;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_BI_ORDER_INFO;

@Component
@Slf4j
public class ExportBiReturnOrderInfoHandler extends AbstractPageFileEventHandler<DmpReturnOrderInfoExcelDTO, DmpReturnOrderInfoSearchDTO> {

    @Resource
    private ExportBiFeign exportBiFeign;
    @Override
    protected List<DmpReturnOrderInfoExcelDTO> getData(FileTask fileTask) {
        DmpReturnOrderInfoSearchDTO dto = readValue(fileTask.getMetaInfo(), new TypeReference<DmpReturnOrderInfoSearchDTO>() {
        });
        return listSeqData(dto);
    }

    @Override
    protected PagingVO<DmpReturnOrderInfoExcelDTO> getPageData(PagingDTO<DmpReturnOrderInfoSearchDTO> dto) {
        return exportBiFeign.exportBiReturnOrderInfo(dto);
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return EXPORT_BI_ORDER_INFO;
    }

    @Override
    public String getExcelPath() {
        return "excel/bi/biReturnOrderInfo.xlsx";
    }
}
