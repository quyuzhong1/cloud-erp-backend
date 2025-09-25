package com.erp.server.file.business.wms;

import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.wms.dto.SampleBorrowInfoDTO;
import com.erp.rpc.wms.feign.ExportWmsFeign;
import com.erp.server.file.core.AbstractPageFileEventHandler;
import com.erp.server.file.entity.FileTask;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_WMS_SAMPLE_BORROW_INFO;

/**
 * 样品借用导出
 * @date 2025-08-21
 * @author jack
 */
@Component
@Slf4j
public class ExportWmsSampleBorrowInfoHandler extends AbstractPageFileEventHandler<SampleBorrowInfoDTO.ListDTO, SampleBorrowInfoDTO.PagingParamDTO> {

    @Resource
    private ExportWmsFeign exportWmsFeign;

    @Override
    protected PagingVO<SampleBorrowInfoDTO.ListDTO> getPageData(PagingDTO<SampleBorrowInfoDTO.PagingParamDTO> dto) {
        return exportWmsFeign.exportSampleBorrowInfo(dto);
    }

    @Override
    protected List<SampleBorrowInfoDTO.ListDTO> getData(FileTask fileTask) {
        SampleBorrowInfoDTO.PagingParamDTO dto = readValue(fileTask.getMetaInfo(), new TypeReference<SampleBorrowInfoDTO.PagingParamDTO>() {});
        return listSeqData(dto);
    }

    @Override
    protected String getExcelPath() {
        return "excel/wms/sampleBorrowInfoExport.xlsx";
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return EXPORT_WMS_SAMPLE_BORROW_INFO;
    }
}
