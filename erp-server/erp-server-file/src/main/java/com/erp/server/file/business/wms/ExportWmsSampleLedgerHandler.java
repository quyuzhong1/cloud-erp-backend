package com.erp.server.file.business.wms;

import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.erp.model.wms.dto.SampleLedgerDTO;
import com.erp.rpc.wms.feign.ExportWmsFeign;
import com.erp.server.file.core.AbstractPageFileEventHandler;
import com.erp.server.file.entity.FileTask;
import com.common.business.enums.FileTaskEventEnum;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_WMS_SAMPLE_LEDGER_REPORT;

/**
 * 样品台账统计异步导出处理器
 * @date 2025-08-25
 * @author wuhaotian
 */
@Component
@Slf4j
public class ExportWmsSampleLedgerHandler extends AbstractPageFileEventHandler<SampleLedgerDTO.ListDTO, SampleLedgerDTO.ExportDTO> {

    @Resource
    private ExportWmsFeign exportWmsFeign;

    @Override
    protected List<SampleLedgerDTO.ListDTO> getData(FileTask fileTask) {
        SampleLedgerDTO.ExportDTO dto = readValue(fileTask.getMetaInfo(), new TypeReference<SampleLedgerDTO.ExportDTO>() {
        });
        return listSeqData(dto);
    }

    @Override
    protected PagingVO<SampleLedgerDTO.ListDTO> getPageData(PagingDTO<SampleLedgerDTO.ExportDTO> dto) {
        return exportWmsFeign.exportSampleLedger(dto);
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return EXPORT_WMS_SAMPLE_LEDGER_REPORT;
    }

    @Override
    public String getExcelPath() {
        return "excel/wms/sampleLedger.xlsx";
    }
}
