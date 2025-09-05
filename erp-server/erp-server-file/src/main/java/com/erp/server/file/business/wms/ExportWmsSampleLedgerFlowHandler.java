package com.erp.server.file.business.wms;

import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.erp.model.wms.dto.SampleLedgerFlowDTO;
import com.erp.rpc.wms.feign.ExportWmsFeign;
import com.erp.server.file.core.AbstractPageFileEventHandler;
import com.erp.server.file.entity.FileTask;
import com.common.business.enums.FileTaskEventEnum;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_WMS_SAMPLE_LEDGER_FLOW_REPORT;

/**
 * 样品台账流水异步导出处理器
 * @date 2025-08-25
 * @author wuhaotian
 */
@Component
@Slf4j
public class ExportWmsSampleLedgerFlowHandler extends AbstractPageFileEventHandler<SampleLedgerFlowDTO.ListDTO, SampleLedgerFlowDTO.ExportDTO> {

    @Resource
    private ExportWmsFeign exportWmsFeign;

    @Override
    protected List<SampleLedgerFlowDTO.ListDTO> getData(FileTask fileTask) {
        SampleLedgerFlowDTO.ExportDTO dto = readValue(fileTask.getMetaInfo(), new TypeReference<SampleLedgerFlowDTO.ExportDTO>() {
        });
        return listSeqData(dto);
    }

    @Override
    protected PagingVO<SampleLedgerFlowDTO.ListDTO> getPageData(PagingDTO<SampleLedgerFlowDTO.ExportDTO> dto) {
        return exportWmsFeign.exportSampleLedgerFlow(dto);
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return EXPORT_WMS_SAMPLE_LEDGER_FLOW_REPORT;
    }

    @Override
    public String getExcelPath() {
        return "excel/wms/sampleLedgerFlow.xlsx";
    }
}
