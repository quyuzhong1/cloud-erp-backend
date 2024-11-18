package com.erp.server.file.business.oms;

import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.oms.dto.RefundOrderDTO;
import com.erp.rpc.oms.feign.ExportOmsFeign;
import com.erp.server.file.core.AbstractPageFileEventHandler;
import com.erp.server.file.entity.FileTask;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_BI_RETURN_INFO;

@Component
@Slf4j
public class ExportOmsRefundHandler extends AbstractPageFileEventHandler<RefundOrderDTO.PagingViewDTO, RefundOrderDTO.PagingParamDTO> {

    @Resource
    private ExportOmsFeign exportOmsFeign;
    @Override
    protected List<RefundOrderDTO.PagingViewDTO> getData(FileTask fileTask) {
        RefundOrderDTO.PagingParamDTO dto = readValue(fileTask.getMetaInfo(), new TypeReference<RefundOrderDTO.PagingParamDTO>() {
        });
        return listSeqData(dto);
    }

    @Override
    protected PagingVO<RefundOrderDTO.PagingViewDTO> getPageData(PagingDTO<RefundOrderDTO.PagingParamDTO> dto) {
        return exportOmsFeign.exportRefund(dto);
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return EXPORT_BI_RETURN_INFO;
    }

    @Override
    public String getExcelPath() {
        return "excel/oms/RefundOrder.xlsx";
    }
}
