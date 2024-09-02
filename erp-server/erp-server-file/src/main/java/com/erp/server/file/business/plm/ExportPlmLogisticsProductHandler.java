package com.erp.server.file.business.plm;

import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.plm.dto.LogisticsProductDTO;
import com.erp.rpc.plm.feign.ExportPlmFeign;
import com.erp.server.file.core.AbstractPageFileEventHandler;
import com.erp.server.file.entity.FileTask;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_PLM_LOGISTICS_PRODUCT;

@Component
@Slf4j
public class ExportPlmLogisticsProductHandler extends AbstractPageFileEventHandler<LogisticsProductDTO.ExportInfoDTO, LogisticsProductDTO.ExportDTO> {
    @Resource
    private ExportPlmFeign exportPlmFeign;
    @Override
    protected List<LogisticsProductDTO.ExportInfoDTO> getData(FileTask fileTask) {
        LogisticsProductDTO.ExportDTO dto = readValue(fileTask.getMetaInfo(), new TypeReference<LogisticsProductDTO.ExportDTO>() {
        });
        return listSeqData(dto);
    }

    @Override
    protected PagingVO<LogisticsProductDTO.ExportInfoDTO> getPageData(PagingDTO<LogisticsProductDTO.ExportDTO> dto) {
        return exportPlmFeign.exportLogisticsProduct(dto);
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return EXPORT_PLM_LOGISTICS_PRODUCT;
    }

    @Override
    public String getExcelPath() {
        return "excel/plm/productLogistics.xlsx";
    }
}
