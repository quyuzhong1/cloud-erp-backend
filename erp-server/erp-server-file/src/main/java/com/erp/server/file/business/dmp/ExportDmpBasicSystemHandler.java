package com.erp.server.file.business.dmp;

import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.dmp.dto.DmpBasicSystemDTO;
import com.erp.rpc.dmp.feign.ExportDmpFeign;
import com.erp.server.file.core.AbstractPageFileEventHandler;
import com.erp.server.file.entity.FileTask;
import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.stereotype.Component;
import javax.annotation.Resource;
import java.util.List;
import static com.common.business.enums.FileTaskEventEnum.EXPORT_DMP_BASIC_SYSTEM;

/**
 * 平台管理 - 导出处理器
 */
@Component
public class ExportDmpBasicSystemHandler extends AbstractPageFileEventHandler<DmpBasicSystemDTO.ListDTO, DmpBasicSystemDTO.ExportDTO> {
    @Resource
    private ExportDmpFeign exportDmpFeign;

    @Override
    protected PagingVO<DmpBasicSystemDTO.ListDTO> getPageData(PagingDTO<DmpBasicSystemDTO.ExportDTO> dto) {
        return exportDmpFeign.exportDmpBasicSystem(dto);
    }

    @Override
    protected List<DmpBasicSystemDTO.ListDTO> getData(FileTask fileTask) {
        DmpBasicSystemDTO.ExportDTO dto = readValue(fileTask.getMetaInfo(), new TypeReference<DmpBasicSystemDTO.ExportDTO>() {});
        return listSeqData(dto);
    }

    @Override
    protected String getExcelPath() {
        return "excel/dmp/dmpbasicsystem.xlsx";
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return EXPORT_DMP_BASIC_SYSTEM;
    }
}
