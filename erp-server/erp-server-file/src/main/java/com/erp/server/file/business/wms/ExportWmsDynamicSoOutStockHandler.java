package com.erp.server.file.business.wms;

import com.common.business.dto.DynamicExcelDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.wms.dto.SoOutstockDTO;
import com.erp.rpc.wms.feign.ExportWmsFeign;
import com.erp.server.file.core.AbstractDynamicHeadersFileEventHandler;
import com.erp.server.file.entity.FileTask;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_WMS_SO_OUT_STOCK_DYNAMIC;

@Component
@Slf4j
public class ExportWmsDynamicSoOutStockHandler extends AbstractDynamicHeadersFileEventHandler<SoOutstockDTO.ExportDTO> {

    private static final int PAGE_SIZE = 10000;

    @Resource
    private ExportWmsFeign exportWmsFeign;

    @Override
    protected DynamicExcelDTO getData(FileTask fileTask) {
        SoOutstockDTO.ExportDTO dto = readValue(fileTask.getMetaInfo(), new TypeReference<SoOutstockDTO.ExportDTO>() {
        });
        return listSeqData(dto);
    }

    @Override
    protected PagingVO<DynamicExcelDTO> getPageData(PagingDTO<SoOutstockDTO.ExportDTO> dto) {
        return exportWmsFeign.exportDynamicSoOutStock(dto);
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return EXPORT_WMS_SO_OUT_STOCK_DYNAMIC;
    }


    @Override
    protected int getPageSize() {
        return PAGE_SIZE;
    }
}
