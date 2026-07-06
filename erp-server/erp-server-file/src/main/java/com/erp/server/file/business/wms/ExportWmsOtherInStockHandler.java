package com.erp.server.file.business.wms;

import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.wms.dto.OtherInstockDTO;
import com.erp.rpc.wms.feign.ExportWmsFeign;
import com.erp.server.file.core.AbstractPageFileEventHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_WMS_OTHER_IN_STOCK;

@Component
@Slf4j
public class ExportWmsOtherInStockHandler extends AbstractPageFileEventHandler<OtherInstockDTO.ListDTO, OtherInstockDTO.SearchParamDTO> {

    @Resource
    private ExportWmsFeign exportWmsFeign;


    @Override
    protected PagingVO<OtherInstockDTO.ListDTO> getPageData(PagingDTO<OtherInstockDTO.SearchParamDTO> dto) {
        return exportWmsFeign.exportOtherInStock(dto);
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return EXPORT_WMS_OTHER_IN_STOCK;
    }

    @Override
    public String getExcelPath() {
           return "excel/wms/otherInstock.xlsx";
    }
}
