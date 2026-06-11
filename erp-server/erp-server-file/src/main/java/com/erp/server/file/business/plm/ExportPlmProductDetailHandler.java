package com.erp.server.file.business.plm;

import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.plm.dto.ProductDetailExcelExportDTO;
import com.erp.model.plm.dto.ProductSkuExcelDTO;
import com.erp.rpc.plm.feign.ExportPlmFeign;
import com.erp.server.file.core.AbstractPageFileEventHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_PLM_SKU;

/**
 * 产品开发导出
 * @date 2025-02-14
 * @author jack
 */
@Component
@Slf4j
public class ExportPlmProductDetailHandler extends AbstractPageFileEventHandler<ProductDetailExcelExportDTO, ProductSkuExcelDTO> {

    @Resource
    private ExportPlmFeign exportPlmFeign;

    @Override
    protected PagingVO<ProductDetailExcelExportDTO> getPageData(PagingDTO<ProductSkuExcelDTO> dto) {
        return exportPlmFeign.exportProductDetail(dto);
    }


    @Override
    protected String getExcelPath() {
        return "excel/plm/productDetail.xlsx";
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return EXPORT_PLM_SKU;
    }
}
