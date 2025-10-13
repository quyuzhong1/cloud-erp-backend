package com.erp.server.file.business.wms;

import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.wms.dto.SampleReturnInfoDTO;
import com.erp.rpc.wms.feign.ExportWmsFeign;
import com.erp.server.file.core.AbstractPageFileEventHandler;
import com.erp.server.file.entity.FileTask;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import javax.annotation.Resource;
import java.util.List;
import static com.common.business.enums.FileTaskEventEnum.EXPORT_WMS_SAMPLE_RETURN_INFO;

/**
 * 样品借用导出
 * @date 2025-08-21
 * @author jack
 */
@Component
@Slf4j
public class ExportWmsSampleReturnInfoHandler extends AbstractPageFileEventHandler<SampleReturnInfoDTO.ListDTO, SampleReturnInfoDTO.PagingParamDTO> {

    @Resource
    private ExportWmsFeign exportWmsFeign;

    @Override
    protected PagingVO<SampleReturnInfoDTO.ListDTO> getPageData(PagingDTO<SampleReturnInfoDTO.PagingParamDTO> dto) {
        return exportWmsFeign.exportSampleReturnInfo(dto);
    }

    @Override
    protected List<SampleReturnInfoDTO.ListDTO> getData(FileTask fileTask) {
        SampleReturnInfoDTO.PagingParamDTO dto = readValue(fileTask.getMetaInfo(), new TypeReference<SampleReturnInfoDTO.PagingParamDTO>() {});
        return listSeqData(dto);
    }

    @Override
    protected String getExcelPath() {
        return "excel/wms/sampleReturnInfoExport.xlsx";
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return EXPORT_WMS_SAMPLE_RETURN_INFO;
    }
}
