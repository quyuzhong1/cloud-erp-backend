package com.erp.server.file.business.wms;

import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.wms.dto.inventory.InitStockDTO;
import com.erp.rpc.wms.feign.ExportWmsFeign;
import com.erp.server.file.core.AbstractPageFileEventHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_WMS_INIT_STOCK;

@Component
@Slf4j
public class ExportWmsInitStockHandler extends AbstractPageFileEventHandler<InitStockDTO.ListDTO, InitStockDTO.ExportSearchParamDTO> {

    @Resource
    private ExportWmsFeign exportWmsFeign;


    @Override
    protected PagingVO<InitStockDTO.ListDTO> getPageData(PagingDTO<InitStockDTO.ExportSearchParamDTO> dto) {
        return exportWmsFeign.exportInitStock(dto);
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return EXPORT_WMS_INIT_STOCK;
    }

    @Override
    public String getExcelPath() {
        return "excel/wms/exportInitStock.xlsx";
    }
}
