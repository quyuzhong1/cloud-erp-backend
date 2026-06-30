package com.erp.server.file.business.wms;

import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.wms.dto.SoReturnInstockDTO;
import com.erp.rpc.wms.feign.ExportWmsFeign;
import com.erp.server.file.core.AbstractPageFileEventHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_WMS_SO_RETURN_IN_STOCK;

@Component
@Slf4j
public class ExportWmsSoReturnInStockHandler extends AbstractPageFileEventHandler<SoReturnInstockDTO.PagingView, SoReturnInstockDTO.PagingParam> {

    @Resource
    private ExportWmsFeign exportWmsFeign;
    @Override
    public String getExcelPath() {
        return "excel/wms/soReturnInstock.xlsx";
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return EXPORT_WMS_SO_RETURN_IN_STOCK;
    }



    @Override
    protected PagingVO<SoReturnInstockDTO.PagingView> getPageData(PagingDTO<SoReturnInstockDTO.PagingParam> dto) {
        return exportWmsFeign.exportSoReturnInStock(dto);
    }
}
