package com.erp.server.file.business.tms;

import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.tms.dto.LogisticsReconDetailDTO;
import com.erp.rpc.tms.feign.ExportTmsFeign;
import com.erp.server.file.core.AbstractPageFileEventHandler;
import com.erp.server.file.entity.FileTask;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_TMS_LOGISTICS_RECON_DETAIL;

/**
 * 物流商对账明细异步导出 Handler
 *
 * @author Will
 * @since 2026-06-01
 */
@Component
@Slf4j
public class ExportTmsLogisticsReconDetailHandler
        extends AbstractPageFileEventHandler<LogisticsReconDetailDTO.ListDTO, LogisticsReconDetailDTO.PagingParamDTO> {

    @Resource
    private ExportTmsFeign exportTmsFeign;

    @Override
    public String getExcelPath() {
        return "excel/tms/logisticsReconDetail.xlsx";
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return EXPORT_TMS_LOGISTICS_RECON_DETAIL;
    }

    @Override
    protected List<LogisticsReconDetailDTO.ListDTO> getData(FileTask fileTask) {
        LogisticsReconDetailDTO.PagingParamDTO dto = readValue(fileTask.getMetaInfo(),
                new TypeReference<LogisticsReconDetailDTO.PagingParamDTO>() {
                });
        return listSeqData(dto);
    }

    @Override
    protected PagingVO<LogisticsReconDetailDTO.ListDTO> getPageData(PagingDTO<LogisticsReconDetailDTO.PagingParamDTO> dto) {
        return exportTmsFeign.exportLogisticsReconDetail(dto);
    }
}
