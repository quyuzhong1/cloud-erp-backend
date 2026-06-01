package com.erp.server.file.business.wms;

import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.wms.dto.PoInstockDTO;
import com.erp.rpc.wms.feign.ExportWmsFeign;
import com.erp.server.file.core.AbstractPageFileEventHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_WMS_PO_IN_STOCK;

@Component
@Slf4j
public class ExportWmsPoInStockHandler extends AbstractPageFileEventHandler<PoInstockDTO.ListDTO, PoInstockDTO.ExportParamDTO> {

    @Resource
    private ExportWmsFeign exportWmsFeign;

    @Override
    public String getExcelPath() {
        throw new UnsupportedOperationException("分页导出请使用 getExcelPath(P)");
    }

    @Override
    protected String getExcelPath(PoInstockDTO.ExportParamDTO params) {
        Boolean isHaveFieldPower = params == null ? null : params.getIsHaveFieldPower();
        if (isHaveFieldPower != null && isHaveFieldPower) {
            return "excel/wms/poInStock.xlsx";
        }
        return "excel/wms/poInStockNotField.xlsx";
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return EXPORT_WMS_PO_IN_STOCK;
    }

    @Override
    protected PagingVO<PoInstockDTO.ListDTO> getPageData(PagingDTO<PoInstockDTO.ExportParamDTO> dto) {
        return exportWmsFeign.exportPoInStock(dto);
    }
}
