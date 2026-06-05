package com.erp.server.file.business.wms;

import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.wms.dto.SoOutstockDTO;
import com.erp.rpc.wms.feign.ExportWmsFeign;
import com.erp.server.file.core.AbstractPageFileEventHandler;
import com.erp.server.file.entity.FileTask;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_WMS_SO_OUT_STOCK;

@Component
@Slf4j
public class ExportWmsSoOutStockHandler extends AbstractPageFileEventHandler<SoOutstockDTO.PagingViewDTO, SoOutstockDTO.PagingParamDTO> {

    @Resource
    private ExportWmsFeign exportWmsFeign;
    @Override
    public String getExcelPath() {
        return "excel/wms/soOutstock.xlsx";
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return EXPORT_WMS_SO_OUT_STOCK;
    }

    @Override
    protected List<SoOutstockDTO.PagingViewDTO> getData(FileTask fileTask) {
        SoOutstockDTO.PagingParamDTO dto = readValue(fileTask.getMetaInfo(), new TypeReference<SoOutstockDTO.PagingParamDTO>() {
        });
        return listSeqData(dto);
    }


    @Override
    protected PagingVO<SoOutstockDTO.PagingViewDTO> getPageData(PagingDTO<SoOutstockDTO.PagingParamDTO> dto) {
        return exportWmsFeign.exportSoOutStock(dto);
    }

    @Override
    protected int getPageSize() {
        return 10000;
    }
}
