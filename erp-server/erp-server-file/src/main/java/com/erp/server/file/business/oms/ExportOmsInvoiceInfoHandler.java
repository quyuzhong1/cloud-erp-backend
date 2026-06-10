package com.erp.server.file.business.oms;

import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.oms.dto.InvoiceInfoDTO;
import com.erp.rpc.oms.feign.ExportOmsFeign;
import com.erp.server.file.core.AbstractPageFileEventHandler;
import com.erp.server.file.entity.FileTask;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_INVOICE_INFO;

@Component
@Slf4j
public class ExportOmsInvoiceInfoHandler extends AbstractPageFileEventHandler<InvoiceInfoDTO.PagingViewDTO, InvoiceInfoDTO.PagingParamDTO> {

    @Resource
    private ExportOmsFeign exportOmsFeign;

    @Override
    protected PagingVO<InvoiceInfoDTO.PagingViewDTO> getPageData(PagingDTO<InvoiceInfoDTO.PagingParamDTO> dto) {
        return exportOmsFeign.exportInvoice(dto);
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return EXPORT_INVOICE_INFO;
    }

    @Override
    public String getExcelPath() {
        return "excel/oms/InvoiceInfo.xlsx";
    }

}
