package com.erp.server.file.business.wms;

import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.wms.dto.StocktakingProfitLossDTO;
import com.erp.rpc.wms.feign.ExportWmsFeign;
import com.erp.server.file.core.AbstractPageFileEventHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_WMS_STOCKTAKING_PROFIT_LOSS;

@Component
@Slf4j
public class ExportWmsStocktakingProfitLossHandler extends AbstractPageFileEventHandler<StocktakingProfitLossDTO.ExportViewDTO, StocktakingProfitLossDTO.ExportDTO> {
    @Resource
    private ExportWmsFeign exportWmsFeign;

    @Override
    public String getExcelPath() {
        return "excel/wms/StocktakingProfitLoss.xlsx";
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return EXPORT_WMS_STOCKTAKING_PROFIT_LOSS;
    }



    @Override
    protected PagingVO<StocktakingProfitLossDTO.ExportViewDTO> getPageData(PagingDTO<StocktakingProfitLossDTO.ExportDTO> dto) {
        return exportWmsFeign.exportStocktakingProfitLoss(dto);
    }
}
