package com.erp.server.file.business.plm;

import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.plm.dto.ProductPlanSearchDTO;
import com.erp.model.plm.dto.excel.ProductPlanExcelDTO;
import com.erp.rpc.plm.feign.ExportPlmFeign;
import com.erp.server.file.core.AbstractPageFileEventHandler;
import com.erp.server.file.entity.FileTask;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_PLM_PRODUCT_PLAN;

@Component
@Slf4j
public class ExportPlmProductPlanHandler extends AbstractPageFileEventHandler<ProductPlanExcelDTO, ProductPlanSearchDTO> {
    @Resource
    private ExportPlmFeign exportPlmFeign;
    @Override
    protected List<ProductPlanExcelDTO> getData(FileTask fileTask) {
        ProductPlanSearchDTO dto = readValue(fileTask.getMetaInfo(), new TypeReference<ProductPlanSearchDTO>() {
        });
        return listSeqData(dto);
    }

    @Override
    protected PagingVO<ProductPlanExcelDTO> getPageData(PagingDTO<ProductPlanSearchDTO> dto) {
        return exportPlmFeign.exportProductPlan(dto);
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return EXPORT_PLM_PRODUCT_PLAN;
    }

    @Override
    public String getExcelPath() {
        return "excel/plm/productPlanExport.xlsx";
    }
}
