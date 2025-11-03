package com.erp.server.file.business.dmp;

import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.dmp.dto.DmpCfgOutputDTO;
import com.erp.rpc.dmp.feign.ExportDmpFeign;
import com.erp.server.file.core.AbstractPageFileEventHandler;
import com.erp.server.file.entity.FileTask;
import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.stereotype.Component;
import javax.annotation.Resource;
import java.util.List;
import static com.common.business.enums.FileTaskEventEnum.EXPORT_DMP_CFG_OUTPUT;

/**
 * 推送配置 - 导出处理器
 */
@Component
public class ExportDmpCfgOutputHandler extends AbstractPageFileEventHandler<DmpCfgOutputDTO.ListDTO, DmpCfgOutputDTO.ExportDTO> {
    @Resource
    private ExportDmpFeign exportDmpFeign;

    @Override
    protected PagingVO<DmpCfgOutputDTO.ListDTO> getPageData(PagingDTO<DmpCfgOutputDTO.ExportDTO> dto) {
        return exportDmpFeign.exportDmpCfgOutput(dto);
    }

    @Override
    protected List<DmpCfgOutputDTO.ListDTO> getData(FileTask fileTask) {
        DmpCfgOutputDTO.ExportDTO dto = readValue(fileTask.getMetaInfo(), new TypeReference<DmpCfgOutputDTO.ExportDTO>() {});
        return listSeqData(dto);
    }

    @Override
    protected String getExcelPath() {
        return "excel/dmp/dmpcfgoutput.xlsx";
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return EXPORT_DMP_CFG_OUTPUT;
    }
}
