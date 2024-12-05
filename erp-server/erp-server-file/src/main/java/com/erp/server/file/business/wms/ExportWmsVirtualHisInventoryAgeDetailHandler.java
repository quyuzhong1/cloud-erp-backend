package com.erp.server.file.business.wms;

import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.wms.dto.VirtualInventoryAgeDTO;
import com.erp.rpc.wms.feign.ExportWmsFeign;
import com.erp.server.file.core.AbstractPageFileEventHandler;
import com.erp.server.file.entity.FileTask;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_WMS_VIRTUAL_HIS_INVENTORY_AGE_DETAIL;

@Component
@Slf4j
public class ExportWmsVirtualHisInventoryAgeDetailHandler extends AbstractPageFileEventHandler<VirtualInventoryAgeDTO.HisInventoryAgeDetailDTO, VirtualInventoryAgeDTO.HisInventoryAgeParamDTO> {

    @Resource
    private ExportWmsFeign exportWmsFeign;

    @Override
    protected List<VirtualInventoryAgeDTO.HisInventoryAgeDetailDTO> getData(FileTask fileTask) {
        VirtualInventoryAgeDTO.HisInventoryAgeParamDTO dto = readValue(fileTask.getMetaInfo(), new TypeReference<VirtualInventoryAgeDTO.HisInventoryAgeParamDTO>() {
        });
        return listSeqData(dto);
    }

    @Override
    protected PagingVO<VirtualInventoryAgeDTO.HisInventoryAgeDetailDTO> getPageData(PagingDTO<VirtualInventoryAgeDTO.HisInventoryAgeParamDTO> dto) {
        return exportWmsFeign.hisInventoryAgeDetailPaging(dto);
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return EXPORT_WMS_VIRTUAL_HIS_INVENTORY_AGE_DETAIL;
    }

    @Override
    public String getExcelPath() {
        return "excel/wms/virtualInventoryAge.xlsx";
    }
}
