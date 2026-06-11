package com.erp.server.file.business.mrp;

import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.mrp.dto.CalcSalesInfoDimDTO;
import com.erp.rpc.mrp.feign.ExportMrpFeign;
import com.erp.server.file.core.AbstractPageFileEventHandler;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_MRP_SALES_CALC_TEMPLATE_LIST;

@Component
public class ExportMrpSalesCalcTemplateListHandler extends AbstractPageFileEventHandler<CalcSalesInfoDimDTO.ExportSalesInfoTemplateListDTO, CalcSalesInfoDimDTO.ParamDTO> {

    @Resource
    private ExportMrpFeign exportMrpFeign;


    @Override
    protected String getExcelPath() {
        return "excel/mrp/salesCalcTemplateList.xlsx";
    }

    @Override
    protected PagingVO<CalcSalesInfoDimDTO.ExportSalesInfoTemplateListDTO> getPageData(PagingDTO<CalcSalesInfoDimDTO.ParamDTO> dto) {
        return exportMrpFeign.exportMrpSalesCalcTemplateList(dto);
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return EXPORT_MRP_SALES_CALC_TEMPLATE_LIST;
    }
}
