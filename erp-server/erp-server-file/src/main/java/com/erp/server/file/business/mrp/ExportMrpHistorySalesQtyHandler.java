package com.erp.server.file.business.mrp;

import com.common.business.dto.DynamicExcelDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.mrp.dto.ReplenishmentSuggestionDTO;
import com.erp.rpc.mrp.feign.ExportMrpFeign;
import com.erp.server.file.core.AbstractDynamicHeadersFileEventHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * MRP 历史销量导出。
 * <p>
 * 不重写 {@code getSheetName()} 系有意为之：sheet 名由数据侧在
 * {@code ReplenishmentSuggestionServiceImpl#listHistorySalesQty} 通过
 * {@code DynamicExcelDTO#setSheetName("销售订单")} 提供，基类
 * {@code AbstractDynamicHeadersFileEventHandler#resolveSheetName} 会优先读取
 * {@code DynamicExcelDTO.getSheetName()}，无需 Handler 再传。请勿误判为丢失 sheet 名。
 */
@Component
@Slf4j
public class ExportMrpHistorySalesQtyHandler extends AbstractDynamicHeadersFileEventHandler<ReplenishmentSuggestionDTO.PagingParamDTO> {

    @Resource
    private ExportMrpFeign exportMrpFeign;


    @Override
    protected PagingVO<DynamicExcelDTO> getPageData(PagingDTO<ReplenishmentSuggestionDTO.PagingParamDTO> dto) {
        return exportMrpFeign.listHistorySalesQty(dto);
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return FileTaskEventEnum.EXPORT_MRP_HISTORY_SALES_QTY;
    }
}
