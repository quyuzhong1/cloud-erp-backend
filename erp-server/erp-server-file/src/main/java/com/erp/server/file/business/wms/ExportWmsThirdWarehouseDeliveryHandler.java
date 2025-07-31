package com.erp.server.file.business.wms;

import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.wms.dto.QcNoticeDTO;
import com.erp.model.wms.dto.ThirdWarehouseDeliveryDTO;
import com.erp.rpc.wms.feign.ExportWmsFeign;
import com.erp.server.file.core.AbstractPageFileEventHandler;
import com.erp.server.file.entity.FileTask;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_WMS_QC_NOTICE_REPORT;
import static com.common.business.enums.FileTaskEventEnum.EXPORT_WMS_THIRD_WAREHOUSE_DELIVERY_REPORT;

/**
 * 三方仓发货单导出
 * @date 2024-09-11
 * @author tanmujin
 */
@Component
@Slf4j
public class ExportWmsThirdWarehouseDeliveryHandler extends AbstractPageFileEventHandler<ThirdWarehouseDeliveryDTO.PagingViewDTO, ThirdWarehouseDeliveryDTO.PagingParamDTO> {

    @Resource
    private ExportWmsFeign exportWmsFeign;

    @Override
    protected PagingVO<ThirdWarehouseDeliveryDTO.PagingViewDTO> getPageData(PagingDTO<ThirdWarehouseDeliveryDTO.PagingParamDTO> dto) {
        return exportWmsFeign.exportThirdWarehouseDelivery(dto);
    }

    @Override
    protected List<ThirdWarehouseDeliveryDTO.PagingViewDTO> getData(FileTask fileTask) {
        ThirdWarehouseDeliveryDTO.PagingParamDTO dto = readValue(fileTask.getMetaInfo(), new TypeReference<ThirdWarehouseDeliveryDTO.PagingParamDTO>() {});
        return listSeqData(dto);
    }

    @Override
    protected String getExcelPath() {
        return "excel/wms/thirdWarehouseDeliveryExport.xlsx";
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return EXPORT_WMS_THIRD_WAREHOUSE_DELIVERY_REPORT;
    }
}
