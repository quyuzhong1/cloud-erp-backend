package com.erp.server.file.business.oms;

import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.oms.dto.ExhibitionOrderDTO;
import com.erp.rpc.oms.feign.ExportOmsFeign;
import com.erp.server.file.core.AbstractPageFileEventHandler;
import com.erp.server.file.entity.FileTask;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import javax.annotation.Resource;
import java.util.List;
import static com.common.business.enums.FileTaskEventEnum.EXPORT_OMS_EXHIBITION_ORDER;

/**
 * 样品借用导出
 * @date 2025-08-21
 * @author jack
 */
@Component
@Slf4j
public class ExportOmsExhibitionOrderHandler extends AbstractPageFileEventHandler<ExhibitionOrderDTO.ListDTO, ExhibitionOrderDTO.PagingParamDTO> {

    @Resource
    private ExportOmsFeign exportOmsFeign;

    @Override
    protected PagingVO<ExhibitionOrderDTO.ListDTO> getPageData(PagingDTO<ExhibitionOrderDTO.PagingParamDTO> dto) {
        return exportOmsFeign.exportExhibitionOrder(dto);
    }

    @Override
    protected List<ExhibitionOrderDTO.ListDTO> getData(FileTask fileTask) {
        ExhibitionOrderDTO.PagingParamDTO dto = readValue(fileTask.getMetaInfo(), new TypeReference<ExhibitionOrderDTO.PagingParamDTO>() {});
        return listSeqData(dto);
    }

    @Override
    protected String getExcelPath() {
        return "excel/oms/exhibitionOrderExport.xlsx";
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return EXPORT_OMS_EXHIBITION_ORDER;
    }
}
