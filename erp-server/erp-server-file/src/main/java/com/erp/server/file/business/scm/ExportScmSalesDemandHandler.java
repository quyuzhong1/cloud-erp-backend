package com.erp.server.file.business.scm;

import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.scm.dto.SalesDemandDTO;
import com.erp.model.scm.dto.excel.SalesDemandExportExcelDTO;
import com.erp.rpc.wms.feign.ExportScmFeign;
import com.erp.server.file.core.AbstractPageFileEventHandler;
import com.erp.server.file.entity.FileTask;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_SCM_SALES_DEMAND;

@Component
@Slf4j
public class ExportScmSalesDemandHandler extends AbstractPageFileEventHandler<SalesDemandExportExcelDTO, SalesDemandDTO.SearchParamDTO> {
    @Resource
    private ExportScmFeign exportScmFeign;
    @Override
    protected List<SalesDemandExportExcelDTO> getData(FileTask fileTask) {
        SalesDemandDTO.SearchParamDTO dto = readValue(fileTask.getMetaInfo(), new TypeReference<SalesDemandDTO.SearchParamDTO>() {
        });
        return listSeqData(dto);
    }

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
