package com.erp.server.file.business.tms;

import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.tms.dto.CfgFileParseDTO;
import com.erp.rpc.tms.feign.ExportTmsFeign;
import com.erp.server.file.core.AbstractPageFileEventHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_TMS_CFG_FILE_PARSE;

/**
 * 月结文件解析配置导出。
 *
 * @author jack
 * @date 2026-06-29
 */
@Component
@Slf4j
public class ExportTmsCfgFileParseHandler extends AbstractPageFileEventHandler<CfgFileParseDTO.ListDTO, CfgFileParseDTO.ExportDTO> {

    @Resource
    private ExportTmsFeign exportTmsFeign;

    @Override
    protected PagingVO<CfgFileParseDTO.ListDTO> getPageData(PagingDTO<CfgFileParseDTO.ExportDTO> dto) {
        return exportTmsFeign.exportTmsCfgFileParse(dto);
    }

    @Override
    protected String getExcelPath() {
        return "excel/tms/cfgFileParseExport.xlsx";
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return EXPORT_TMS_CFG_FILE_PARSE;
    }
}
