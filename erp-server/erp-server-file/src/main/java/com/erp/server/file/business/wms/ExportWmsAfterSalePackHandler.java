package com.erp.server.file.business.wms;

import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.wms.dto.AfterSalePackDTO;
import com.erp.rpc.wms.feign.ExportWmsFeign;
import com.erp.server.file.core.AbstractPageFileEventHandler;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_WMS_AFTER_SALE_PACK;

@Component
public class ExportWmsAfterSalePackHandler extends AbstractPageFileEventHandler<AfterSalePackDTO.ExportViewDTO, AfterSalePackDTO.ExportDTO> {

    @Resource
    private ExportWmsFeign exportWmsFeign;

    @Override
    protected PagingVO<AfterSalePackDTO.ExportViewDTO> getPageData(PagingDTO<AfterSalePackDTO.ExportDTO> dto) {
        return exportWmsFeign.exportAfterSalePack(dto);
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return EXPORT_WMS_AFTER_SALE_PACK;
    }

    @Override
    public String getExcelPath() {
        return "excel/wms/afterSalePackExport.xlsx";
    }
}
