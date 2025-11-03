package com.erp.server.file.business.dmp;

import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.dmp.dto.DmpCfgEtlDTO;
import com.erp.rpc.dmp.feign.ExportDmpFeign;
import com.erp.server.file.core.AbstractPageFileEventHandler;
import com.erp.server.file.entity.FileTask;
import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.stereotype.Component;
import javax.annotation.Resource;
import java.util.List;
import static com.common.business.enums.FileTaskEventEnum.EXPORT_DMP_CFG_ETL;

/**
 * 清洗调度 - 导出处理器
 */
@Component
public class ExportDmpCfgEtlHandler extends AbstractPageFileEventHandler<DmpCfgEtlDTO.ListDTO, DmpCfgEtlDTO.ExportDTO> {
    @Resource
    private ExportDmpFeign exportDmpFeign;

    @Override
    protected PagingVO<DmpCfgEtlDTO.ListDTO> getPageData(PagingDTO<DmpCfgEtlDTO.ExportDTO> dto) {
        return exportDmpFeign.exportDmpCfgEtl(dto);
    }

    @Override
    protected List<DmpCfgEtlDTO.ListDTO> getData(FileTask fileTask) {
        DmpCfgEtlDTO.ExportDTO dto = readValue(fileTask.getMetaInfo(), new TypeReference<DmpCfgEtlDTO.ExportDTO>() {});
        return listSeqData(dto);
    }

    @Override
    protected String getExcelPath() {
        return "excel/dmp/dmpcfgetl.xlsx";
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return EXPORT_DMP_CFG_ETL;
    }
}
