package com.erp.server.file.business.tms;

import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.tms.dto.LogisticsOrderDTO;
import com.erp.model.tms.dto.excel.TmsLogisticsOrderExcelDTO;
import com.erp.rpc.tms.feign.ExportTmsFeign;
import com.erp.server.file.core.AbstractPageFileEventHandler;
import com.erp.server.file.entity.FileTask;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_TMS_LOGISTICS_ORDER;

/**
 * 物流订单导出
 *
 * @author lei
 * @date 2024-09-11
 */
@Component
@Slf4j
public class ExportTmsLogisticsOrderHandler extends AbstractPageFileEventHandler<TmsLogisticsOrderExcelDTO, LogisticsOrderDTO.PagingParamDTO> {

    @Resource
    private ExportTmsFeign exportTmsFeign;

    @Override
    protected PagingVO<TmsLogisticsOrderExcelDTO> getPageData(PagingDTO<LogisticsOrderDTO.PagingParamDTO> dto) {
        return exportTmsFeign.exportTmsLogisticsOrder(dto);
    }

    @Override
    protected List<TmsLogisticsOrderExcelDTO> getData(FileTask fileTask) {
        LogisticsOrderDTO.PagingParamDTO dto = readValue(fileTask.getMetaInfo(), new TypeReference<LogisticsOrderDTO.PagingParamDTO>() {
        });
        return listSeqData(dto);
    }

    @Override
    protected String getExcelPath() {
        return "excel/tms/logisticsOrder.xlsx";
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return EXPORT_TMS_LOGISTICS_ORDER;
    }
}
