package com.erp.server.file.business.plm;

import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.erp.model.plm.dto.ProductDetailExcelDTO;
import com.erp.model.plm.dto.ProductSkuExcelDTO;
import com.erp.server.file.core.AbstractPageFileEventHandler;
import com.erp.server.file.entity.FileTask;
import com.common.business.enums.FileTaskEventEnum;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class ProductDetailExportHandler extends AbstractPageFileEventHandler<ProductDetailExcelDTO, ProductSkuExcelDTO> {


    @Override
    public String getExcelPath() {
        return "excel/plm/productNoSpecDetailExport.xlsx";
    }
    

    @Override
    public FileTaskEventEnum getEvent() {
        return FileTaskEventEnum.EXPORT_PLM_PRODUCT_DETAIL;
    }

    @Override
    protected List<ProductDetailExcelDTO> getData(FileTask fileTask) {
        ProductSkuExcelDTO dto = readValue(fileTask.getMetaInfo(), new TypeReference<ProductSkuExcelDTO>() {
        });
        return listSeqData(dto);
    }

    @Override
    protected PagingVO<ProductDetailExcelDTO> getPageData(PagingDTO<ProductSkuExcelDTO> dto) {
        return null;
    }
}
