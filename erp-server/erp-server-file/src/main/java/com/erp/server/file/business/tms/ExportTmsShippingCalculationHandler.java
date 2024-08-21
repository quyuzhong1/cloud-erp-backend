package com.erp.server.file.business.tms;

import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.tms.dto.ShippingCalculationDTO;
import com.erp.model.wms.dto.inventory.InventoryDTO;
import com.erp.rpc.tms.feign.ExportTmsFeign;
import com.erp.rpc.workflow.ExportWorkflowFeign;
import com.erp.server.file.core.AbstractPageFileEventHandler;
import com.erp.server.file.entity.FileTask;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_TMS_SHIPPING_CALCULATION;

@Component
@Slf4j
public class ExportTmsShippingCalculationHandler extends AbstractPageFileEventHandler<ShippingCalculationDTO.ListDTO, ShippingCalculationDTO.PagingParamDTO> {
    @Resource
    private ExportTmsFeign exportTmsFeign;
    private static final ThreadLocal<ShippingCalculationDTO.PagingParamDTO> threadLocal = new ThreadLocal<>();
    @Override
    public String getExcelPath() {
        ShippingCalculationDTO.PagingParamDTO dto = threadLocal.get();
        String excelPath = "excel/shippingCalculation_self.xlsx";
        if ("first".equals(dto.getShipmentMethod())) {
            excelPath = "excel/shippingCalculation_first.xlsx";
        }
        threadLocal.remove();
        return excelPath;
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return EXPORT_TMS_SHIPPING_CALCULATION;
    }

    @Override
    protected List<ShippingCalculationDTO.ListDTO> getData(FileTask fileTask) {
        ShippingCalculationDTO.PagingParamDTO dto = readValue(fileTask.getMetaInfo(), new TypeReference<ShippingCalculationDTO.PagingParamDTO>() {
        });
        threadLocal.set(dto);
        return listSeqData(dto);
    }

    @Override
    protected PagingVO<ShippingCalculationDTO.ListDTO> getPageData(PagingDTO<ShippingCalculationDTO.PagingParamDTO> dto) {
        return exportTmsFeign.exportShippingCalculation(dto);
    }
}
