package com.erp.server.file.business.dmp;

import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.dmp.dto.DmpCfgOutputDetailDTO;
import com.erp.rpc.dmp.feign.ExportDmpFeign;
import com.erp.server.file.core.AbstractPageFileEventHandler;
import com.erp.server.file.entity.FileTask;
import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.stereotype.Component;
import javax.annotation.Resource;
import java.util.List;
import static com.common.business.enums.FileTaskEventEnum.EXPORT_DMP_CFG_OUTPUT_DETAIL;

/**
 * 推送调度 - 导出处理器
 */
@Component
public class ExportDmpCfgOutputDetailHandler extends AbstractPageFileEventHandler<DmpCfgOutputDetailDTO.ListDTO, DmpCfgOutputDetailDTO.ExportDTO> {
    @Resource
    private ExportDmpFeign exportDmpFeign;

    @Override
    protected PagingVO<DmpCfgOutputDetailDTO.ListDTO> getPageData(PagingDTO<DmpCfgOutputDetailDTO.ExportDTO> dto) {
        return exportDmpFeign.exportDmpCfgOutputDetail(dto);
    }

    @Override
    protected List<DmpCfgOutputDetailDTO.ListDTO> getData(FileTask fileTask) {
        DmpCfgOutputDetailDTO.ExportDTO dto = readValue(fileTask.getMetaInfo(), new TypeReference<DmpCfgOutputDetailDTO.ExportDTO>() {});
        return listSeqData(dto);
    }

    @Override
    protected String getExcelPath() {
        return "excel/dmp/dmpcfgoutputdetail.xlsx";
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return EXPORT_DMP_CFG_OUTPUT_DETAIL;
    }
}
