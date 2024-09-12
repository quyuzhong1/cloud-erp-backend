package com.erp.server.file.business.wms;

import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.srm.dto.DeliveryOrderDTO;
import com.erp.model.srm.dto.excel.DeliveryOrderExportExcelDTO;
import com.erp.rpc.wms.feign.ExportWmsFeign;
import com.erp.server.file.core.AbstractPageFileEventHandler;
import com.erp.server.file.entity.FileTask;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_WMS_SUPPLIER_DELIVERY_ORDER;

@Component
@Slf4j
public class ExportWmsSupplierDeliveryOrderHandler extends AbstractPageFileEventHandler<DeliveryOrderExportExcelDTO, DeliveryOrderDTO.ParamDTO> {

    @Resource
    private ExportWmsFeign exportWmsFeign;
    @Override
    public String getExcelPath() {
        return "excel/wms/supplierDeliveryOrder.xlsx";
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return EXPORT_WMS_SUPPLIER_DELIVERY_ORDER;
    }

    @Override
    protected List<DeliveryOrderExportExcelDTO> getData(FileTask fileTask) {
        DeliveryOrderDTO.ParamDTO dto = readValue(fileTask.getMetaInfo(), new TypeReference<DeliveryOrderDTO.ParamDTO>() {
        });
        return listSeqData(dto);
    }


    @Override
    protected PagingVO<DeliveryOrderExportExcelDTO> getPageData(PagingDTO<DeliveryOrderDTO.ParamDTO> dto) {
        return exportWmsFeign.exportSupplierDeliveryOrder(dto);
    }
}
