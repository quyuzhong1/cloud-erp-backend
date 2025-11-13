package com.erp.server.file.business.wms;

import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.wms.dto.inventory.InventoryReportDTO;
import com.erp.rpc.wms.feign.ExportWmsFeign;
import com.erp.server.file.core.AbstractPageFileEventHandler;
import com.erp.server.file.entity.FileTask;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_WMS_INVENTORY_DAILY_BILLDATE;

@Component
@Slf4j
public class ExportWmsInventoryDailyBillDateHandler extends AbstractPageFileEventHandler<InventoryReportDTO.ListDailyInventoryDTO, InventoryReportDTO.DailyInventoryParamDTO> {

    @Resource
    private ExportWmsFeign exportWmsFeign;

    @Override
    protected List<InventoryReportDTO.ListDailyInventoryDTO> getData(FileTask fileTask) {
        InventoryReportDTO.DailyInventoryParamDTO dto = readValue(fileTask.getMetaInfo(), new TypeReference<InventoryReportDTO.DailyInventoryParamDTO>() {
        });
        return listSeqData(dto);
    }

    @Override
    protected PagingVO<InventoryReportDTO.ListDailyInventoryDTO> getPageData(PagingDTO<InventoryReportDTO.DailyInventoryParamDTO> dto) {
        return exportWmsFeign.exportInventoryDaily(dto);
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return EXPORT_WMS_INVENTORY_DAILY_BILLDATE;
    }

    @Override
    public String getExcelPath() {
        return "excel/wms/dailyInventoryBillDate.xlsx";
    }
}
