package com.erp.server.file.business.tms;

import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.tms.dto.TmsFirstMileReconciliationDetailDTO;
import com.erp.rpc.tms.feign.ExportTmsFeign;
import com.erp.server.file.core.AbstractPageFileEventHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_TMS_TMS_FIRST_MILE_RECONCILIATION_DETAIL;

@Component
@Slf4j
public class ExportTmsFirstMileReconciliationDetailHandler extends AbstractPageFileEventHandler<TmsFirstMileReconciliationDetailDTO.ExportDetailDTO, TmsFirstMileReconciliationDetailDTO.ExportDTO> {
    @Resource
    private ExportTmsFeign exportTmsFeign;

    @Override
    public String getExcelPath() {
        return "excel/tms/tmsFirstMileReconciliationDetail.xlsx";
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return EXPORT_TMS_TMS_FIRST_MILE_RECONCILIATION_DETAIL;
    }


    @Override
    protected PagingVO<TmsFirstMileReconciliationDetailDTO.ExportDetailDTO> getPageData(PagingDTO<TmsFirstMileReconciliationDetailDTO.ExportDTO> dto) {
        return exportTmsFeign.exportFirstMileReconciliationDetail(dto);
    }
}
