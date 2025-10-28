package com.erp.server.file.business.plm;

import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.plm.dto.MoldMonitorDTO;
import com.erp.rpc.plm.feign.ExportPlmFeign;
import com.erp.server.file.core.AbstractPageFileEventHandler;
import com.erp.server.file.entity.FileTask;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_PLM_MOLD_MONITOR_ALERT;
import static com.common.business.enums.FileTaskEventEnum.EXPORT_PLM_MOLD_MONITOR_RETURN;

/**
 * 模具返还监控导出
 *
 * @author jack
 * @date 2025-10-11
 */
@Component
@Slf4j
public class ExportPlmMoldMonitorAlertHandler extends AbstractPageFileEventHandler<MoldMonitorDTO.ListDTO, MoldMonitorDTO.PagingParamDTO> {

    @Resource
    private ExportPlmFeign exportPlmFeign;

    @Override
    protected PagingVO<MoldMonitorDTO.ListDTO> getPageData(PagingDTO<MoldMonitorDTO.PagingParamDTO> dto) {
        return exportPlmFeign.exportMoldMonitor(dto);
    }

    @Override
    protected List<MoldMonitorDTO.ListDTO> getData(FileTask fileTask) {
        MoldMonitorDTO.PagingParamDTO dto = readValue(fileTask.getMetaInfo(), new TypeReference<MoldMonitorDTO.PagingParamDTO>() {
        });
        return listSeqData(dto);
    }

    @Override
    protected String getExcelPath() {
        return "excel/plm/moldMonitorAlert.xlsx";
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return EXPORT_PLM_MOLD_MONITOR_ALERT;
    }
}
