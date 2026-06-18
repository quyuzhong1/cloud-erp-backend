package com.erp.server.file.business.wms;

import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.wms.dto.CfgQcUserDTO;
import com.erp.rpc.wms.feign.ExportWmsFeign;
import com.erp.server.file.core.AbstractPageFileEventHandler;
import com.erp.server.file.entity.FileTask;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_WMS_CFG_QC_USER;

/**
 * 质检员配置异步导出处理器
 */
@Component
@Slf4j
public class ExportWmsCfgQcUserHandler extends AbstractPageFileEventHandler<CfgQcUserDTO.ListDTO, CfgQcUserDTO.ExportDTO> {

    @Resource
    private ExportWmsFeign exportWmsFeign;

    @Override
    protected PagingVO<CfgQcUserDTO.ListDTO> getPageData(PagingDTO<CfgQcUserDTO.ExportDTO> dto) {
        return exportWmsFeign.exportCfgQcUser(dto);
    }

    @Override
    protected List<CfgQcUserDTO.ListDTO> getData(FileTask fileTask) {
        CfgQcUserDTO.ExportDTO dto = readValue(fileTask.getMetaInfo(), new TypeReference<CfgQcUserDTO.ExportDTO>() {
        });
        return listSeqData(dto);
    }

    @Override
    protected String getExcelPath() {
        return "excel/wms/cfgQcUserExport.xlsx";
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return EXPORT_WMS_CFG_QC_USER;
    }
}
