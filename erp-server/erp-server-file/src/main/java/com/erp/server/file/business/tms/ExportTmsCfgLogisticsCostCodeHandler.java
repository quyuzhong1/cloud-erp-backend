package com.erp.server.file.business.tms;

import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.tms.dto.CfgLogisticsCostImportDTO;
import com.erp.rpc.tms.feign.ExportTmsFeign;
import com.erp.server.file.core.AbstractPageFileEventHandler;
import com.erp.server.file.entity.FileTask;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_TMS_CFG_LOGISTICS_COST;
import static com.common.business.enums.FileTaskEventEnum.EXPORT_TMS_DICT_HS_CODE;

/**
 * 费用配置导出
 * @date 2026-01-23
 * @author jack
 */
@Component
@Slf4j
public class ExportTmsCfgLogisticsCostCodeHandler extends AbstractPageFileEventHandler<CfgLogisticsCostImportDTO.ListDTO, CfgLogisticsCostImportDTO.PagingParamDTO> {

    @Resource
    private ExportTmsFeign exportTmsFeign;

    @Override
    protected PagingVO<CfgLogisticsCostImportDTO.ListDTO> getPageData(PagingDTO<CfgLogisticsCostImportDTO.PagingParamDTO> dto) {
        return exportTmsFeign.exportTmsCfgLogisticsCost(dto);
    }

    @Override
    protected List<CfgLogisticsCostImportDTO.ListDTO> getData(FileTask fileTask) {
        CfgLogisticsCostImportDTO.PagingParamDTO dto = readValue(fileTask.getMetaInfo(), new TypeReference<CfgLogisticsCostImportDTO.PagingParamDTO>() {});
        return listSeqData(dto);
    }

    @Override
    protected String getExcelPath() {
        return "excel/tms/cfgLogisticsCostExport.xlsx";
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return EXPORT_TMS_CFG_LOGISTICS_COST;
    }
}
