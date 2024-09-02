package com.erp.server.file.business.oms;

import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.oms.dto.ReportDTO;
import com.erp.rpc.oms.feign.ExportOmsFeign;
import com.erp.server.file.core.AbstractPageFileEventHandler;
import com.erp.server.file.entity.FileTask;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_OMS_SO_B2C_PRODUCT_SALES;

@Component
@Slf4j
public class ExportOmsSoB2CProductSalesHandler extends AbstractPageFileEventHandler<ReportDTO.ProductSalesPagingViewDTO, ReportDTO.ProductSalesPagingParamDTO> {

    @Resource
    private ExportOmsFeign exportOmsFeign;
    @Override
    protected List<ReportDTO.ProductSalesPagingViewDTO> getData(FileTask fileTask) {
        ReportDTO.ProductSalesPagingParamDTO dto = readValue(fileTask.getMetaInfo(), new TypeReference<ReportDTO.ProductSalesPagingParamDTO>() {
        });
        return listSeqData(dto);
    }

    @Override
    protected PagingVO<ReportDTO.ProductSalesPagingViewDTO> getPageData(PagingDTO<ReportDTO.ProductSalesPagingParamDTO> dto) {
        return exportOmsFeign.exportSoB2CProductSales(dto);
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return EXPORT_OMS_SO_B2C_PRODUCT_SALES;
    }

    @Override
    public String getExcelPath() {
        return "excel/oms/ProductSalesCount.xlsx";
    }
}
