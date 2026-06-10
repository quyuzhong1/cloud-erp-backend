package com.erp.server.file.business.mrp;

import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.mrp.dto.CalcSalesInfoDimDTO;
import com.erp.rpc.mrp.feign.ExportMrpFeign;
import com.erp.server.file.core.AbstractPageFileEventHandler;
import com.erp.server.file.entity.FileTask;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_MRP_SALES_CALC_LIST;

@Component
public class ExportMrpSalesCalcListHandler extends AbstractPageFileEventHandler<CalcSalesInfoDimDTO.ExportSalesInfoListDTO, CalcSalesInfoDimDTO.ParamDTO> {

    @Resource
    private ExportMrpFeign exportMrpFeign;


    @Override
    protected String getExcelPath() {
        return "excel/mrp/salesCalcList.xlsx";
    }

    @Override
    protected PagingVO<CalcSalesInfoDimDTO.ExportSalesInfoListDTO> getPageData(PagingDTO<CalcSalesInfoDimDTO.ParamDTO> dto) {
        return exportMrpFeign.exportMrpSalesCalcList(dto);
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return EXPORT_MRP_SALES_CALC_LIST;
    }
}
