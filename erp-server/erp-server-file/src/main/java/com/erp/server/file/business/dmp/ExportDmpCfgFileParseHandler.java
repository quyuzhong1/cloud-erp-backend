package com.erp.server.file.business.dmp;

import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.dmp.dto.CfgFileParseDTO;
import com.erp.rpc.dmp.feign.ExportDmpFeign;
import com.erp.server.file.core.AbstractPageFileEventHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_DMP_CFG_FILE_PARSE;

/**
 * 月结文件解析配置导出。
 *
 * @author jack
 * @date 2026-06-29
 */
@Component
@Slf4j
public class ExportDmpCfgFileParseHandler extends AbstractPageFileEventHandler<CfgFileParseDTO.ListDTO, CfgFileParseDTO.ExportDTO> {

    @Resource
    private ExportDmpFeign exportDmpFeign;

    @Override
    protected PagingVO<CfgFileParseDTO.ListDTO> getPageData(PagingDTO<CfgFileParseDTO.ExportDTO> dto) {
        return exportDmpFeign.exportDmpCfgFileParse(dto);
    }

    @Override
    protected String getExcelPath() {
        return "excel/dmp/cfgFileParseExport.xlsx";
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return EXPORT_DMP_CFG_FILE_PARSE;
    }
}
