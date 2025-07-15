package com.erp.server.file.business.sys;

import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.sys.dto.CfgThirdNoticeDTO;
import com.erp.rpc.sys.feign.ExportSysFeign;
import com.erp.server.file.core.AbstractPageFileEventHandler;
import com.erp.server.file.entity.FileTask;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_SYS_THIRD_NOTICE;

@Component
@Slf4j
public class ExportSysCfgThirdNoticeHandler extends AbstractPageFileEventHandler<CfgThirdNoticeDTO.ListDTO, CfgThirdNoticeDTO.PagingParamDTO> {
    @Resource
    private ExportSysFeign exportSysFeign;

    @Override
    public String getExcelPath() {
        return "excel/sys/cfgThirdNoticeExport.xlsx";
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return EXPORT_SYS_THIRD_NOTICE;
    }

    @Override
    protected List<CfgThirdNoticeDTO.ListDTO> getData(FileTask fileTask) {
        CfgThirdNoticeDTO.PagingParamDTO dto = readValue(fileTask.getMetaInfo(), new TypeReference<CfgThirdNoticeDTO.PagingParamDTO>() {
        });
        return listSeqData(dto);
    }

    @Override
    protected PagingVO<CfgThirdNoticeDTO.ListDTO> getPageData(PagingDTO<CfgThirdNoticeDTO.PagingParamDTO> dto) {
        return exportSysFeign.exportCfgThirdNotice(dto);
    }
}
