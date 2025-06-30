package com.erp.server.file.business.wms;

import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.wms.dto.VirtualAdjustDTO;
import com.erp.model.wms.dto.VirtualAdjustDTO;
import com.erp.rpc.wms.feign.ExportWmsFeign;
import com.erp.server.file.core.AbstractPageFileEventHandler;
import com.erp.server.file.entity.FileTask;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_WMS_VIRTUAL_ADJUST_REPORT;

/**
 * 虚拟库存调整导出
 * @date 2025-06-11
 * @author zdy
 */
@Component
@Slf4j
public class ExportWmsVirtualAdjustHandler extends AbstractPageFileEventHandler<VirtualAdjustDTO.ListDTO, VirtualAdjustDTO.PagingParamDTO> {

    @Resource
    private ExportWmsFeign exportWmsFeign;

    @Override
    protected PagingVO<VirtualAdjustDTO.ListDTO> getPageData(PagingDTO<VirtualAdjustDTO.PagingParamDTO> dto) {
        return exportWmsFeign.exportVirtualAdjust(dto);
    }

    @Override
    protected List<VirtualAdjustDTO.ListDTO> getData(FileTask fileTask) {
        VirtualAdjustDTO.PagingParamDTO dto = readValue(fileTask.getMetaInfo(), new TypeReference<VirtualAdjustDTO.PagingParamDTO>() {});
        return listSeqData(dto);
    }

    @Override
    protected String getExcelPath() {
        return "excel/wms/virtualAdjustExport.xlsx";
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return EXPORT_WMS_VIRTUAL_ADJUST_REPORT;
    }
}
