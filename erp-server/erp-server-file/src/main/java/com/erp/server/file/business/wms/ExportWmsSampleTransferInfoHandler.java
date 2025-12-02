package com.erp.server.file.business.wms;

import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.wms.dto.SampleTransferInfoDTO;
import com.erp.rpc.wms.feign.ExportWmsFeign;
import com.erp.server.file.core.AbstractPageFileEventHandler;
import com.erp.server.file.entity.FileTask;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_WMS_SAMPLE_TRANSFER_INFO_REPORT;

/**
 * 样品转移单导出
 * @date 2025-10-28
 * @author wuhaotian
 */
@Component
@Slf4j
public class ExportWmsSampleTransferInfoHandler extends AbstractPageFileEventHandler<SampleTransferInfoDTO.ListDTO, SampleTransferInfoDTO.ExportDTO> {

    @Resource
    private ExportWmsFeign exportWmsFeign;

    @Override
    protected PagingVO<SampleTransferInfoDTO.ListDTO> getPageData(PagingDTO<SampleTransferInfoDTO.ExportDTO> dto) {
        return exportWmsFeign.getSampleTransferInfoPageData(dto);
    }

    @Override
    protected List<SampleTransferInfoDTO.ListDTO> getData(FileTask fileTask) {
        SampleTransferInfoDTO.ExportDTO dto = readValue(fileTask.getMetaInfo(), new TypeReference<SampleTransferInfoDTO.ExportDTO>() {});
        return listSeqData(dto);
    }

    @Override
    protected String getExcelPath() {
        return "excel/wms/sampleTransferInfoExport.xlsx";
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return EXPORT_WMS_SAMPLE_TRANSFER_INFO_REPORT;
    }
}

