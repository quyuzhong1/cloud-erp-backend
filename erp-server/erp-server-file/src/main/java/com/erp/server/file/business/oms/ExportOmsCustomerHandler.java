package com.erp.server.file.business.oms;

import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.oms.dto.CustomerDTO;
import com.erp.model.oms.dto.InvoiceDTO;
import com.erp.rpc.oms.feign.ExportOmsFeign;
import com.erp.server.file.core.AbstractMasterDerivedSheetHandler;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_OMS_CUSTOMER;

@Component
public class ExportOmsCustomerHandler extends AbstractMasterDerivedSheetHandler<CustomerDTO.ExportDTO, CustomerDTO.PagingExportDTO> {

    @Resource
    private ExportOmsFeign exportOmsFeign;

    @Override
    protected String getExcelPath(CustomerDTO.ExportDTO params) {
        return "excel/oms/CustomerExport.xlsx";
    }

    @Override
    protected PagingVO<CustomerDTO.PagingExportDTO> fetchMasterPage(PagingDTO<CustomerDTO.ExportDTO> dto) {
        return exportOmsFeign.exportCustomer(dto);
    }

    @Override
    protected List<Function<List<CustomerDTO.PagingExportDTO>, List<?>>> buildSheetExtractors() {
        List<Function<List<CustomerDTO.PagingExportDTO>, List<?>>> extractors = new ArrayList<>(3);
        extractors.add(ArrayList::new);
        extractors.add(mainRows -> {
            List<CustomerDTO.PagingAddressContactExportDTO> addressRows = new ArrayList<>();
            for (CustomerDTO.PagingExportDTO row : mainRows) {
                if (row != null && !CollectionUtils.isEmpty(row.getAddressContactList())) {
                    addressRows.addAll(row.getAddressContactList());
                }
            }
            return addressRows;
        });
        extractors.add(mainRows -> {
            List<InvoiceDTO.ViewDTO> invoiceRows = new ArrayList<>();
            for (CustomerDTO.PagingExportDTO row : mainRows) {
                if (row != null && !CollectionUtils.isEmpty(row.getInvoiceList())) {
                    invoiceRows.addAll(row.getInvoiceList());
                }
            }
            return invoiceRows;
        });
        return extractors;
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return EXPORT_OMS_CUSTOMER;
    }
}
