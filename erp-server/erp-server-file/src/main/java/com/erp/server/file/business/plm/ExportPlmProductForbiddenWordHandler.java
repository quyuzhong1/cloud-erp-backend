package com.erp.server.file.business.plm;

import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.file.entity.FileTask;
import com.erp.model.plm.dto.CfgProductForbiddenWordDTO;
import com.erp.model.plm.dto.excel.CfgProductForbiddenWordExportExcelDTO;
import com.erp.rpc.plm.feign.ExportPlmFeign;
import com.erp.server.file.core.AbstractPageFileEventHandler;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_PLM_PRODUCT_FORBIDDEN_WORD;

/**
 * 违禁词库导出。
 */
@Component
@Slf4j
public class ExportPlmProductForbiddenWordHandler extends AbstractPageFileEventHandler<CfgProductForbiddenWordExportExcelDTO, CfgProductForbiddenWordDTO.PagingParamDTO> {

    @Resource
    private ExportPlmFeign exportPlmFeign;


    @Override
    protected PagingVO<CfgProductForbiddenWordExportExcelDTO> getPageData(PagingDTO<CfgProductForbiddenWordDTO.PagingParamDTO> dto) {
        return exportPlmFeign.exportProductForbiddenWord(dto);
    }

    @Override
    protected String getExcelPath() {
        return "excel/plm/productForbiddenWord.xlsx";
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return EXPORT_PLM_PRODUCT_FORBIDDEN_WORD;
    }
}
