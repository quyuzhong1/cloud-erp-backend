package com.erp.server.file.business.oms;

import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.oms.dto.SoReturnDTO;
import com.erp.rpc.oms.feign.ExportOmsFeign;
import com.erp.server.file.core.AbstractPageFileEventHandler;
import com.erp.server.file.entity.FileTask;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_OMS_SO_RETURN;

@Component
@Slf4j
public class ExportOmsSoReturnHandler extends AbstractPageFileEventHandler<SoReturnDTO.PagingView, SoReturnDTO.PagingParam> {

    @Resource
    private ExportOmsFeign exportOmsFeign;
    @Override
    protected List<SoReturnDTO.PagingView> getData(FileTask fileTask) {
        SoReturnDTO.PagingParam dto = readValue(fileTask.getMetaInfo(), new TypeReference<SoReturnDTO.PagingParam>() {
        });
        return listSeqData(dto);
    }

    @Override
    protected PagingVO<SoReturnDTO.PagingView> getPageData(PagingDTO<SoReturnDTO.PagingParam> dto) {
        return exportOmsFeign.exportSoReturn(dto);
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return EXPORT_OMS_SO_RETURN;
    }

    @Override
    public String getExcelPath() {
        return "excel/oms/SoReturnExport.xlsx";
    }
}
