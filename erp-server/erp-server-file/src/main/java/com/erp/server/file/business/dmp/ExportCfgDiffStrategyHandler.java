package com.erp.server.file.business.dmp;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;

import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.dmp.dto.CfgDiffStrategyDTO;
import com.erp.rpc.dmp.feign.ExportDmpFeign;
import com.erp.server.file.core.AbstractPageFileEventHandler;
import com.erp.server.file.entity.FileTask;
import com.fasterxml.jackson.core.type.TypeReference;

import lombok.extern.slf4j.Slf4j;

/**
 * 试产量产单导出
 * @date 2024-09-11
 * @author tanmujin
 */
@Component
@Slf4j
public class ExportCfgDiffStrategyHandler extends AbstractPageFileEventHandler<CfgDiffStrategyDTO.ViewDTO, CfgDiffStrategyDTO.PagingParamDTO> {

    @Resource
    private ExportDmpFeign exportDmpFeign;

    @Override
    protected PagingVO<CfgDiffStrategyDTO.ViewDTO> getPageData(PagingDTO<CfgDiffStrategyDTO.PagingParamDTO> dto) {
        return exportDmpFeign.exportCfgDiffStrategy(dto);
    }

    @Override
    protected List<CfgDiffStrategyDTO.ViewDTO> getData(FileTask fileTask) {
        CfgDiffStrategyDTO.PagingParamDTO dto = readValue(fileTask.getMetaInfo(), new TypeReference<CfgDiffStrategyDTO.PagingParamDTO>() {});
        return listSeqData(dto);
    }

    @Override
    protected String getExcelPath() {
        return "excel/dmp/cfgDiffStrategyExport.xlsx";
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return FileTaskEventEnum.EXPORT_CFG_DIFF_STRATEGY;
    }
}
