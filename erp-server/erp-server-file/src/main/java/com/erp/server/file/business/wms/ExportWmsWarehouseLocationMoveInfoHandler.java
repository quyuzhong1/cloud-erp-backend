package com.erp.server.file.business.wms;

import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.wms.dto.WarehouseLocationDTO;
import com.erp.model.wms.dto.WarehouseLocationMoveDTO;
import com.erp.rpc.wms.feign.ExportWmsFeign;
import com.erp.server.file.core.AbstractPageFileEventHandler;
import com.erp.server.file.entity.FileTask;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_WMS_PDA_WAREHOUSE_LOCATION_MOVE_INFO;

@Component
@Slf4j
public class ExportWmsWarehouseLocationMoveInfoHandler extends AbstractPageFileEventHandler<WarehouseLocationMoveDTO.PdaPcListDTO, WarehouseLocationMoveDTO.ExportDTO> {

    @Resource
    private ExportWmsFeign exportWmsFeign;
    @Override
    public String getExcelPath() {
        return "excel/wms/pdaMoveInfo.xlsx";
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return EXPORT_WMS_PDA_WAREHOUSE_LOCATION_MOVE_INFO;
    }

    @Override
    protected List<WarehouseLocationMoveDTO.PdaPcListDTO> getData(FileTask fileTask) {
        WarehouseLocationMoveDTO.ExportDTO dto = readValue(fileTask.getMetaInfo(), new TypeReference<WarehouseLocationMoveDTO.ExportDTO>() {
        });
        return listSeqData(dto);
    }


    @Override
    protected PagingVO<WarehouseLocationMoveDTO.PdaPcListDTO> getPageData(PagingDTO<WarehouseLocationMoveDTO.ExportDTO> dto) {
        return exportWmsFeign.exportWarehouseLocationMoveInfo(dto);
    }
}
