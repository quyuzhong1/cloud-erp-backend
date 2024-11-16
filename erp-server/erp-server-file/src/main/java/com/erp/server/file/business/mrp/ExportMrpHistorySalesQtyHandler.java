package com.erp.server.file.business.mrp;

import cn.hutool.core.util.StrUtil;
import com.common.business.dto.DynamicExcelDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.mrp.dto.ReplenishmentSuggestionDTO;
import com.erp.rpc.mrp.feign.ExportMrpFeign;
import com.erp.server.file.core.AbstractDynamicHeadersFileEventHandler;
import com.erp.server.file.entity.FileTask;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;

@Component
@Slf4j
public class ExportMrpHistorySalesQtyHandler extends AbstractDynamicHeadersFileEventHandler<ReplenishmentSuggestionDTO.PagingParamDTO> {

    @Resource
    private ExportMrpFeign exportMrpFeign;

    private static final ThreadLocal<String> threadLocal = new ThreadLocal<>();

    @Override
    protected DynamicExcelDTO getData(FileTask fileTask) {
        ReplenishmentSuggestionDTO.PagingParamDTO dto = readValue(fileTask.getMetaInfo(), new TypeReference<ReplenishmentSuggestionDTO.PagingParamDTO>() {
        });
        return listSeqData(dto);
    }

    @Override
    protected PagingVO<DynamicExcelDTO> getPageData(PagingDTO<ReplenishmentSuggestionDTO.PagingParamDTO> dto) {
        PagingVO<DynamicExcelDTO> dtoPagingVO = exportMrpFeign.listHistorySalesQty(dto);
        List<DynamicExcelDTO> list = (List<DynamicExcelDTO>) dtoPagingVO.getList();
        threadLocal.set(CollectionUtils.isEmpty(list) ? "" : list.get(0).getSheetName());
        return dtoPagingVO;
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return FileTaskEventEnum.EXPORT_MRP_HISTORY_SALES_QTY;
    }

    @Override
    public List<String> getSheetName() {
        String sheetName = threadLocal.get();
        threadLocal.remove();
        return StrUtil.isBlank(sheetName) ? null : Collections.singletonList(sheetName);
    }
}
