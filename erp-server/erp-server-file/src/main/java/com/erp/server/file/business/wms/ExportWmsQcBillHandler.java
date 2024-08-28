package com.erp.server.file.business.wms;

import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.wms.dto.QcInfoDTO;
import com.erp.model.wms.dto.excel.QcBillExportExcelDTO;
import com.erp.rpc.wms.feign.ExportWmsFeign;
import com.erp.server.file.core.AbstractPageFileEventHandler;
import com.erp.server.file.entity.FileTask;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_WMS_QC_BILL;

@Component
@Slf4j
public class ExportWmsQcBillHandler extends AbstractPageFileEventHandler<QcBillExportExcelDTO, QcInfoDTO.ExportDTO> {

    @Resource
    private ExportWmsFeign exportWmsFeign;

    @Override
    protected List<QcBillExportExcelDTO> getData(FileTask fileTask) {
        QcInfoDTO.ExportDTO dto = readValue(fileTask.getMetaInfo(), new TypeReference<QcInfoDTO.ExportDTO>() {
        });
        return listSeqData(dto);
    }

    @Override
    protected PagingVO<QcBillExportExcelDTO> getPageData(PagingDTO<QcInfoDTO.ExportDTO> dto) {
        return exportWmsFeign.exportQcBill(dto);
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return EXPORT_WMS_QC_BILL;
    }

    @Override
    public String getExcelPath() {
        return "excel/wms/qcBillExport.xlsx";
    }
}
