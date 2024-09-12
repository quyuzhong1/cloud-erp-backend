package com.erp.server.file.business.oms;

import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.oms.dto.CustomerB2bSellerChangeDTO;
import com.erp.model.oms.dto.excel.CustomerB2bSellerExcelDTO;
import com.erp.rpc.oms.feign.ExportOmsFeign;
import com.erp.server.file.core.AbstractPageFileEventHandler;
import com.erp.server.file.entity.FileTask;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_OMS_CUSTOMER_B2B_SELLER_CHANGE;

@Component
@Slf4j
public class ExportOmsCustomerB2BSellerChangeHandler extends AbstractPageFileEventHandler<CustomerB2bSellerExcelDTO, CustomerB2bSellerChangeDTO.ParamDTO> {

    @Resource
    private ExportOmsFeign exportOmsFeign;
    @Override
    protected List<CustomerB2bSellerExcelDTO> getData(FileTask fileTask) {
        CustomerB2bSellerChangeDTO.ParamDTO dto = readValue(fileTask.getMetaInfo(), new TypeReference<CustomerB2bSellerChangeDTO.ParamDTO>() {
        });
        return listSeqData(dto);
    }

    @Override
    protected PagingVO<CustomerB2bSellerExcelDTO> getPageData(PagingDTO<CustomerB2bSellerChangeDTO.ParamDTO> dto) {
        return exportOmsFeign.exportCustomerB2BSellerChange(dto);
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return EXPORT_OMS_CUSTOMER_B2B_SELLER_CHANGE;
    }

    @Override
    public String getExcelPath() {
        return "excel/oms/customerB2BSellerChange.xlsx";
    }
}
