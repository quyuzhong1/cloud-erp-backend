package com.erp.server.file.business.wms;

import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.wms.dto.SampleScrapInfoDTO;
import com.erp.rpc.wms.feign.ExportWmsFeign;
import com.erp.server.file.core.AbstractPageFileEventHandler;
import com.erp.server.file.entity.FileTask;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_WMS_SAMPLE_SCRAP_INFO_REPORT;

/**
 * 样品报废导出
 * @date 2025-08-21
 * @author jack
 */
@Component
@Slf4j
public class ExportWmsSampleScrapInfoHandler extends AbstractPageFileEventHandler<SampleScrapInfoDTO.ListDTO, SampleScrapInfoDTO.PagingParamDTO> {

    @Resource
    private ExportWmsFeign exportWmsFeign;

    @Override
    protected PagingVO<SampleScrapInfoDTO.ListDTO> getPageData(PagingDTO<SampleScrapInfoDTO.PagingParamDTO> dto) {
        return exportWmsFeign.exportSampleScrapInfo(dto);
    }

    @Override
    protected List<SampleScrapInfoDTO.ListDTO> getData(FileTask fileTask) {
        SampleScrapInfoDTO.PagingParamDTO dto = readValue(fileTask.getMetaInfo(), new TypeReference<SampleScrapInfoDTO.PagingParamDTO>() {});
        return listSeqData(dto);
    }

    @Override
    protected String getExcelPath() {
        return "excel/wms/sampleScrapInfoExport.xlsx";
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return EXPORT_WMS_SAMPLE_SCRAP_INFO_REPORT;
    }
}
