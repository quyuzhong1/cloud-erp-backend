package com.erp.server.file.business.wms;

import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.wms.dto.AwdOutstockDTO;
import com.erp.rpc.wms.feign.ExportWmsFeign;
import com.erp.server.file.core.AbstractPageFileEventHandler;
import com.erp.server.file.entity.FileTask;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import javax.annotation.Resource;
import java.util.List;
import static com.common.business.enums.FileTaskEventEnum.EXPORT_WMS_AWD_OUT_STOCK;

/**
 * @Author: wtr
 * @Date: 2025/12/24 14:39
 * @Param:
 * @Return:
 * @Description:
 **/
@Component
@Slf4j
public class ExportWmsAwdOutStockHandler extends AbstractPageFileEventHandler<AwdOutstockDTO.ListDTO, AwdOutstockDTO.PagingParamDTO> {

    @Resource
    private ExportWmsFeign exportWmsFeign;

    @Override
    protected PagingVO<AwdOutstockDTO.ListDTO> getPageData(PagingDTO<AwdOutstockDTO.PagingParamDTO> dto) {
        return exportWmsFeign.exportAwdOutStock(dto);
    }

    @Override
    protected List<AwdOutstockDTO.ListDTO> getData(FileTask fileTask) {
        AwdOutstockDTO.PagingParamDTO dto = readValue(fileTask.getMetaInfo(), new TypeReference<AwdOutstockDTO.PagingParamDTO>() {
        });
        return listSeqData(dto);
    }

    @Override
    protected String getExcelPath() {
        return "excel/wms/awdOutStockExport.xlsx";
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return EXPORT_WMS_AWD_OUT_STOCK;
    }
}