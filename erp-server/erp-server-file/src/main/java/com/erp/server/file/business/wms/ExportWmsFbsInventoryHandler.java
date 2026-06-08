package com.erp.server.file.business.wms;

import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.wms.dto.FbsInventoryDTO;
import com.erp.rpc.wms.feign.ExportWmsFeign;
import com.erp.server.file.core.AbstractPageFileEventHandler;
import com.erp.server.file.entity.FileTask;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_WMS_FBS_INVENTORY;

/**
 * FBS库存导出
 *
 * @author Cursor
 * @since 2026-05-25
 */
@Component
@Slf4j
public class ExportWmsFbsInventoryHandler extends AbstractPageFileEventHandler<FbsInventoryDTO.ListDTO, FbsInventoryDTO.PagingParamDTO> {

    @Resource
    private ExportWmsFeign exportWmsFeign;

    @Override
    protected PagingVO<FbsInventoryDTO.ListDTO> getPageData(PagingDTO<FbsInventoryDTO.PagingParamDTO> dto) {
        return exportWmsFeign.exportFbsInventory(dto);
    }

    @Override
    protected List<FbsInventoryDTO.ListDTO> getData(FileTask fileTask) {
        FbsInventoryDTO.PagingParamDTO dto = readValue(fileTask.getMetaInfo(), new TypeReference<FbsInventoryDTO.PagingParamDTO>() {
        });
        return listSeqData(dto);
    }

    @Override
    protected String getExcelPath() {
        return "excel/wms/fbsInventoryExport.xlsx";
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return EXPORT_WMS_FBS_INVENTORY;
    }
}
