package com.erp.server.file.business.oms;

import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.oms.dto.CfgInvoiceInvalidDTO;
import com.erp.rpc.oms.feign.ExportOmsFeign;
import com.erp.server.file.core.AbstractPageFileEventHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_INVOICE_INVALID;

/**
 * @description: 作废发票号导出Handler
 * @author: hcg
 * @date: 2025/4/14 14:53
 */
@Component
@Slf4j
public class ExportOmsInvoiceInvalidHandler extends AbstractPageFileEventHandler<CfgInvoiceInvalidDTO.PagingViewDTO, CfgInvoiceInvalidDTO.PagingParamDTO> {
    @Resource
    private ExportOmsFeign exportOmsFeign;


    @Override
    protected PagingVO<CfgInvoiceInvalidDTO.PagingViewDTO> getPageData(PagingDTO<CfgInvoiceInvalidDTO.PagingParamDTO> dto) {
        return exportOmsFeign.exportInvoiceInvalid(dto);
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return EXPORT_INVOICE_INVALID;
    }
    @Override
    protected String getExcelPath() {
        return "excel/oms/InvoiceInvalid.xlsx";
    }
}
