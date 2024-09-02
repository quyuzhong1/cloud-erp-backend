package com.erp.server.file.business.sys;

import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.sys.dto.DictGlobalAreaDTO;
import com.erp.rpc.sys.feign.ExportSysFeign;
import com.erp.server.file.core.AbstractPageFileEventHandler;
import com.erp.server.file.entity.FileTask;
import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_SYS_GLOBAL_AREA;

@Component
public class ExportSysGlobalAreaHandler extends AbstractPageFileEventHandler<DictGlobalAreaDTO.PagingViewDTO, DictGlobalAreaDTO.PagingParamDTO> {
    @Resource
    private ExportSysFeign exportSysFeign;
    @Override
    protected List<DictGlobalAreaDTO.PagingViewDTO> getData(FileTask fileTask) {
        DictGlobalAreaDTO.PagingParamDTO dto = readValue(fileTask.getMetaInfo(), new TypeReference<DictGlobalAreaDTO.PagingParamDTO>() {
        });
        return listSeqData(dto);
    }

    @Override
    protected String getExcelPath() {
        return "excel/sys/globalArea.xlsx";
    }

    @Override
    protected PagingVO<DictGlobalAreaDTO.PagingViewDTO> getPageData(PagingDTO<DictGlobalAreaDTO.PagingParamDTO> dto) {
        return exportSysFeign.exportGlobalArea(dto);
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return EXPORT_SYS_GLOBAL_AREA;
    }
}
