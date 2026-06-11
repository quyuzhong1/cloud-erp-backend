package com.erp.server.file.business.wms;

import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.erp.model.wms.dto.SampleRecipientDTO;
import com.erp.rpc.wms.feign.ExportWmsFeign;
import com.erp.server.file.core.AbstractPageFileEventHandler;
import com.common.business.enums.FileTaskEventEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_WMS_SAMPLE_RECIPIENT_REPORT;

/**
 * 样品领用单异步导出处理器
 * @date 2025-08-25
 * @author wuhaotian
 */
@Component
@Slf4j
public class ExportWmsSampleRecipientHandler extends AbstractPageFileEventHandler<SampleRecipientDTO.ListDTO, SampleRecipientDTO.ExportDTO> {

    @Resource
    private ExportWmsFeign exportWmsFeign;


    @Override
    protected PagingVO<SampleRecipientDTO.ListDTO> getPageData(PagingDTO<SampleRecipientDTO.ExportDTO> dto) {
        return exportWmsFeign.getSampleRecipientPageData(dto);
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return EXPORT_WMS_SAMPLE_RECIPIENT_REPORT;
    }

    @Override
    public String getExcelPath() {
        return "excel/wms/sampleRecipient.xlsx";
    }
}
