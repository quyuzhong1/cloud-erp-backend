package com.erp.server.file.business.tms;

import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.tms.dto.ShippingCalculationDTO;
import com.erp.rpc.tms.feign.ExportTmsFeign;
import com.erp.server.file.core.AbstractPageFileEventHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_TMS_SHIPPING_CALCULATION;

@Component
@Slf4j
public class ExportTmsShippingCalculationHandler extends AbstractPageFileEventHandler<ShippingCalculationDTO.ListDTO, ShippingCalculationDTO.PagingParamDTO> {
    @Resource
    private ExportTmsFeign exportTmsFeign;

    @Override
    public String getExcelPath() {
        throw new UnsupportedOperationException("分页导出请使用 getExcelPath(P)");
    }

    @Override
    protected String getExcelPath(ShippingCalculationDTO.PagingParamDTO params) {
        if (params != null && "first".equals(params.getShipmentMethod())) {
            return "excel/tms/shippingCalculation_first.xlsx";
        }
        return "excel/tms/shippingCalculation_self.xlsx";
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return EXPORT_TMS_SHIPPING_CALCULATION;
    }

    @Override
    protected PagingVO<ShippingCalculationDTO.ListDTO> getPageData(PagingDTO<ShippingCalculationDTO.PagingParamDTO> dto) {
        return exportTmsFeign.exportShippingCalculation(dto);
    }
}
