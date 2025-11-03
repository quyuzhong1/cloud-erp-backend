package com.erp.server.file.business.dmp;

import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.dmp.dto.DmpCfgInputDetailDTO;
import com.erp.rpc.dmp.feign.ExportDmpFeign;
import com.erp.server.file.core.AbstractPageFileEventHandler;
import com.erp.server.file.entity.FileTask;
import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.stereotype.Component;
import javax.annotation.Resource;
import java.util.List;
import static com.common.business.enums.FileTaskEventEnum.EXPORT_DMP_CFG_INPUT_DETAIL;

/**
 * 拉取调度 - 导出处理器
 */
@Component
public class ExportDmpCfgInputDetailHandler extends AbstractPageFileEventHandler<DmpCfgInputDetailDTO.ListDTO, DmpCfgInputDetailDTO.ExportDTO> {
    @Resource
    private ExportDmpFeign exportDmpFeign;

    @Override
    protected PagingVO<DmpCfgInputDetailDTO.ListDTO> getPageData(PagingDTO<DmpCfgInputDetailDTO.ExportDTO> dto) {
        return exportDmpFeign.exportDmpCfgInputDetail(dto);
    }

    @Override
    protected List<DmpCfgInputDetailDTO.ListDTO> getData(FileTask fileTask) {
        DmpCfgInputDetailDTO.ExportDTO dto = readValue(fileTask.getMetaInfo(), new TypeReference<DmpCfgInputDetailDTO.ExportDTO>() {});
        return listSeqData(dto);
    }

    @Override
    protected String getExcelPath() {
        return "excel/dmp/dmpcfginputdetail.xlsx";
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return EXPORT_DMP_CFG_INPUT_DETAIL;
    }
}
