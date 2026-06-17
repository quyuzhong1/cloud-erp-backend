package com.erp.server.file.business.oms;

import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.oms.dto.SoReceiptDTO;
import com.erp.rpc.oms.feign.ExportOmsFeign;
import com.erp.server.file.core.AbstractPageFileEventHandler;
import com.erp.model.file.entity.FileTask;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_OMS_SO_RECEIPT;

@Component
@Slf4j
public class ExportOmsSoReceiptHandler extends AbstractPageFileEventHandler<SoReceiptDTO.ListDTO, SoReceiptDTO.PagingParamDTO> {
    @Resource
    private ExportOmsFeign exportOmsFeign;
    @Override
    protected List<SoReceiptDTO.ListDTO> getData(FileTask fileTask) {
        SoReceiptDTO.PagingParamDTO dto = readValue(fileTask.getMetaInfo(), new TypeReference<SoReceiptDTO.PagingParamDTO>() {
        });
        return listSeqData(dto);
    }

    @Override
    protected PagingVO<SoReceiptDTO.ListDTO> getPageData(PagingDTO<SoReceiptDTO.PagingParamDTO> dto) {
        return exportOmsFeign.exportSoReceipt(dto);
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return EXPORT_OMS_SO_RECEIPT;
    }

    @Override
    public String getExcelPath() {
        return "excel/oms/SoReceipt.xlsx";
    }
}
