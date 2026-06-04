package com.erp.server.file.business.tms;

import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.tms.dto.LogisticsReconDTO;
import com.erp.rpc.tms.feign.ExportTmsFeign;
import com.erp.server.file.core.AbstractPageFileEventHandler;
import com.erp.server.file.entity.FileTask;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_TMS_LOGISTICS_RECON;

/**
 * 物流商对账单（主表）异步导出 Handler
 *
 * @author Will
 * @since 2026-06-01
 */
@Component
@Slf4j
public class ExportTmsLogisticsReconHandler
        extends AbstractPageFileEventHandler<LogisticsReconDTO.ListDTO, LogisticsReconDTO.PagingParamDTO> {

    @Resource
    private ExportTmsFeign exportTmsFeign;

    @Override
    public String getExcelPath() {
        return "excel/tms/logisticsRecon.xlsx";
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return EXPORT_TMS_LOGISTICS_RECON;
    }

    @Override
    protected List<LogisticsReconDTO.ListDTO> getData(FileTask fileTask) {
        LogisticsReconDTO.PagingParamDTO dto = readValue(fileTask.getMetaInfo(),
                new TypeReference<LogisticsReconDTO.PagingParamDTO>() {
                });
        return listSeqData(dto);
    }

    @Override
    protected PagingVO<LogisticsReconDTO.ListDTO> getPageData(PagingDTO<LogisticsReconDTO.PagingParamDTO> dto) {
        return exportTmsFeign.exportLogisticsRecon(dto);
    }
}
