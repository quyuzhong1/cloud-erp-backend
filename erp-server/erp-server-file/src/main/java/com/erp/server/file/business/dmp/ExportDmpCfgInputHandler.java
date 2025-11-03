package com.erp.server.file.business.dmp;

import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.dmp.dto.DmpCfgInputDTO;
import com.erp.rpc.dmp.feign.ExportDmpFeign;
import com.erp.server.file.core.AbstractPageFileEventHandler;
import com.erp.server.file.entity.FileTask;
import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.stereotype.Component;
import javax.annotation.Resource;
import java.util.List;
import static com.common.business.enums.FileTaskEventEnum.EXPORT_DMP_CFG_INPUT;

/**
 * 拉取配置 - 导出处理器
 */
@Component
public class ExportDmpCfgInputHandler extends AbstractPageFileEventHandler<DmpCfgInputDTO.ListDTO, DmpCfgInputDTO.ExportDTO> {
    @Resource
    private ExportDmpFeign exportDmpFeign;

    @Override
    protected PagingVO<DmpCfgInputDTO.ListDTO> getPageData(PagingDTO<DmpCfgInputDTO.ExportDTO> dto) {
        return exportDmpFeign.exportDmpCfgInput(dto);
    }

    @Override
    protected List<DmpCfgInputDTO.ListDTO> getData(FileTask fileTask) {
        DmpCfgInputDTO.ExportDTO dto = readValue(fileTask.getMetaInfo(), new TypeReference<DmpCfgInputDTO.ExportDTO>() {});
        return listSeqData(dto);
    }

    @Override
    protected String getExcelPath() {
        return "excel/dmp/dmpcfginput.xlsx";
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return EXPORT_DMP_CFG_INPUT;
    }
}
