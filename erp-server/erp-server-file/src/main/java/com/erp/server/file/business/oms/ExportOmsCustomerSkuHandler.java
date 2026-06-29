package com.erp.server.file.business.oms;

import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.oms.dto.SkuMappingDTO;
import com.erp.rpc.oms.feign.ExportOmsFeign;
import com.erp.server.file.core.AbstractPageFileEventHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_OMS_CUSTOMER_SKU;

@Component
@Slf4j
public class ExportOmsCustomerSkuHandler extends AbstractPageFileEventHandler<SkuMappingDTO.CustomerPagingViewDTO, SkuMappingDTO.CustomerPagingParamDTO> {

    @Resource
    private ExportOmsFeign exportOmsFeign;

    @Override
    protected PagingVO<SkuMappingDTO.CustomerPagingViewDTO> getPageData(PagingDTO<SkuMappingDTO.CustomerPagingParamDTO> dto) {
        return exportOmsFeign.exportCustomerSku(dto);
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return EXPORT_OMS_CUSTOMER_SKU;
    }

    @Override
    public String getExcelPath() {
        return "excel/oms/CustomerSkuMapping.xlsx";
    }
}
