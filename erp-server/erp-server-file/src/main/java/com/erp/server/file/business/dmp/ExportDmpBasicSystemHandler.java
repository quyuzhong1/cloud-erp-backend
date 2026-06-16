package com.erp.server.file.business.dmp;

import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.dmp.dto.DmpBasicSystemDTO;
import com.erp.rpc.dmp.feign.ExportDmpFeign;
import com.erp.server.file.core.AbstractPageFileEventHandler;
import com.erp.model.file.entity.FileTask;
import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.stereotype.Component;
import javax.annotation.Resource;
import static com.common.business.enums.FileTaskEventEnum.EXPORT_DMP_BASIC_SYSTEM;

/**
 * 平台管理 - 导出处理器
 */
@Component
public class ExportDmpBasicSystemHandler extends AbstractPageFileEventHandler<DmpBasicSystemDTO.ListDTO, DmpBasicSystemDTO.PagingParamDTO> {
    @Resource
    private ExportDmpFeign exportDmpFeign;

    @Override
    protected PagingVO<DmpBasicSystemDTO.ListDTO> getPageData(PagingDTO<DmpBasicSystemDTO.PagingParamDTO> dto) {
        return exportDmpFeign.exportDmpBasicSystem(dto);
    }


    @Override
    protected String getExcelPath() {
        return "excel/dmp/dmpBasicSystem.xlsx";
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return EXPORT_DMP_BASIC_SYSTEM;
    }
}
