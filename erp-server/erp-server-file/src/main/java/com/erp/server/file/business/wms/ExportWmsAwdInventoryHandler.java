package com.erp.server.file.business.wms;

import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.wms.dto.AwdInventoryDTO;
import com.erp.rpc.wms.feign.ExportWmsFeign;
import com.erp.server.file.core.AbstractPageFileEventHandler;
import com.erp.server.file.entity.FileTask;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import javax.annotation.Resource;
import java.util.List;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_WMS_AWD_INVENTORY;

/**
 * @Author: wtr
 * @Date: 2025/12/26 16:55
 * @Param:
 * @Return:
 * @Description:
 **/
@Component
@Slf4j
public class ExportWmsAwdInventoryHandler extends AbstractPageFileEventHandler<AwdInventoryDTO.ListDTO, AwdInventoryDTO.PagingParamDTO> {

    @Resource
    private ExportWmsFeign exportWmsFeign;

    @Override
    protected PagingVO<AwdInventoryDTO.ListDTO> getPageData(PagingDTO<AwdInventoryDTO.PagingParamDTO> dto) {
        return exportWmsFeign.exportAwdInventory(dto);
    }

    @Override
    protected List<AwdInventoryDTO.ListDTO> getData(FileTask fileTask) {
        AwdInventoryDTO.PagingParamDTO dto = readValue(fileTask.getMetaInfo(), new TypeReference<AwdInventoryDTO.PagingParamDTO>() {
        });
        return listSeqData(dto);
    }

    @Override
    protected String getExcelPath() {
        return "excel/wms/awdInventoryExport.xlsx";
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return EXPORT_WMS_AWD_INVENTORY;
    }
}