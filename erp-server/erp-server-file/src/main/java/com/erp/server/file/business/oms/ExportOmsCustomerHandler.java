package com.erp.server.file.business.oms;

import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.oms.dto.CustomerDTO;
import com.erp.rpc.oms.feign.ExportOmsFeign;
import com.erp.server.file.core.AbstractPageFileEventHandler;
import com.erp.server.file.entity.FileTask;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_OMS_CUSTOMER;
import static com.common.business.enums.FileTaskEventEnum.EXPORT_OMS_SHOP;

@Component
@Slf4j
public class ExportOmsCustomerHandler extends AbstractPageFileEventHandler<CustomerDTO.PagingViewDTO, CustomerDTO.ExportDTO> {

    @Resource
    private ExportOmsFeign exportOmsFeign;
    @Override
    protected List<CustomerDTO.PagingViewDTO> getData(FileTask fileTask) {
        CustomerDTO.ExportDTO dto = readValue(fileTask.getMetaInfo(), new TypeReference<CustomerDTO.ExportDTO>() {
        });
        return listSeqData(dto);
    }

    @Override
    protected PagingVO<CustomerDTO.PagingViewDTO> getPageData(PagingDTO<CustomerDTO.ExportDTO> dto) {
        return exportOmsFeign.exportCustomer(dto);
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return EXPORT_OMS_CUSTOMER;
    }

    @Override
    public String getExcelPath() {
        return "excel/oms/CustomerExport.xlsx";
    }
}
