package com.erp.server.file.business.scm;

import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.scm.dto.CfgSupplierSalesDTO;
import com.erp.rpc.scm.feign.ExportScmFeign;
import com.erp.server.file.core.AbstractPageFileEventHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import javax.annotation.Resource;
import static com.common.business.enums.FileTaskEventEnum.EXPORT_SCM_CFG_SUPPLIER_SALES_REPORT;

@Component
@Slf4j
public class ExportScmCfgSupplierSalesHandler extends AbstractPageFileEventHandler<CfgSupplierSalesDTO.ListDTO, CfgSupplierSalesDTO.PagingParamDTO> {
    @Resource
    private ExportScmFeign exportScmFeign;

    @Override
    public String getExcelPath() {
        return "excel/scm/cfgSupplierSalesExport.xlsx";
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return EXPORT_SCM_CFG_SUPPLIER_SALES_REPORT;
    }


    @Override
    protected PagingVO<CfgSupplierSalesDTO.ListDTO> getPageData(PagingDTO<CfgSupplierSalesDTO.PagingParamDTO> dto) {
        return exportScmFeign.exportCfgSupplierSales(dto);
    }
}
