package com.erp.server.file.business.scm;

import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.scm.dto.SalesDemandDTO;
import com.erp.model.scm.dto.excel.SalesDemandExportExcelDTO;
import com.erp.rpc.scm.feign.ExportScmFeign;
import com.erp.server.file.core.AbstractPageFileEventHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_SCM_SALES_DEMAND;

@Component
@Slf4j
public class ExportScmSalesDemandHandler extends AbstractPageFileEventHandler<SalesDemandExportExcelDTO, SalesDemandDTO.SearchParamDTO> {
    @Resource
    private ExportScmFeign exportScmFeign;

    @Override
    protected PagingVO<SalesDemandExportExcelDTO> getPageData(PagingDTO<SalesDemandDTO.SearchParamDTO> dto) {
        return exportScmFeign.exportSalesDemand(dto);
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return EXPORT_SCM_SALES_DEMAND;
    }

    @Override
    public String getExcelPath() {
        return "excel/scm/salesDemand.xlsx";
    }
}
