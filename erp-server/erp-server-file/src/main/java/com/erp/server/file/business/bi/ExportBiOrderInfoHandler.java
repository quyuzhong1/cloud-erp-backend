package com.erp.server.file.business.bi;

import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.dmp.dto.DmpOrderInfoExcelDTO;
import com.erp.model.dmp.dto.DmpOrderInfoSearchDTO;
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
public class ExportBiOrderInfoHandler extends AbstractPageFileEventHandler<DmpOrderInfoExcelDTO, DmpOrderInfoSearchDTO> {

    @Resource
    private ExportBiFeign exportBiFeign;
    @Override
    protected List<DmpOrderInfoExcelDTO> getData(FileTask fileTask) {
        DmpOrderInfoSearchDTO dto = readValue(fileTask.getMetaInfo(), new TypeReference<DmpOrderInfoSearchDTO>() {
        });
        return listSeqData(dto);
    }

    @Override
    protected PagingVO<DmpOrderInfoExcelDTO> getPageData(PagingDTO<DmpOrderInfoSearchDTO> dto) {
        return exportBiFeign.exportBiOrderInfo(dto);
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return EXPORT_BI_ORDER_INFO;
    }

    @Override
    public String getExcelPath() {
        return "excel/bi/biOrderInfo.xlsx";
    }
}
