package com.erp.server.file.business.sys;

import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.sys.dto.TemplateManagementDTO;
import com.erp.rpc.sys.feign.ExportSysFeign;
import com.erp.server.file.core.AbstractPageFileEventHandler;
import com.erp.server.file.entity.FileTask;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_SYS_TEMPLATE;

@Component
@Slf4j
public class ExportSysTemplateHandler extends AbstractPageFileEventHandler<TemplateManagementDTO.ListDTO, TemplateManagementDTO.PagingParamDTO> {
    @Resource
    private ExportSysFeign exportSysFeign;

    @Override
    public String getExcelPath() {
        return "excel/sys/templateManagementExport.xlsx";
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return EXPORT_SYS_TEMPLATE;
    }

    @Override
    protected List<TemplateManagementDTO.ListDTO> getData(FileTask fileTask) {
        TemplateManagementDTO.PagingParamDTO dto = readValue(fileTask.getMetaInfo(), new TypeReference<TemplateManagementDTO.PagingParamDTO>() {
        });
        return listSeqData(dto);
    }

    @Override
    protected PagingVO<TemplateManagementDTO.ListDTO> getPageData(PagingDTO<TemplateManagementDTO.PagingParamDTO> dto) {
        return exportSysFeign.exportTemplateManagement(dto);
    }
}
