package com.erp.server.file.business.dmp;

import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.dmp.dto.AfterSaleDTO;
import com.erp.model.dmp.dto.excel.DmpAfterSaleExcelDTO;
import com.erp.rpc.dmp.feign.ExportDmpFeign;
import com.erp.server.file.core.AbstractPageFileEventHandler;
import com.erp.server.file.entity.FileTask;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_DMP_AFTER_SALE;

/**
 * 试产量产单导出
 * @date 2024-09-11
 * @author tanmujin
 */
@Component
@Slf4j
public class ExportDmpAfterSaleHandler extends AbstractPageFileEventHandler<DmpAfterSaleExcelDTO, AfterSaleDTO.PagingParamDTO> {

    @Resource
    private ExportDmpFeign exportDmpFeign;

    @Override
    protected PagingVO<DmpAfterSaleExcelDTO> getPageData(PagingDTO<AfterSaleDTO.PagingParamDTO> dto) {
        return exportDmpFeign.exportAfterSale(dto);
    }

    @Override
    protected List<DmpAfterSaleExcelDTO> getData(FileTask fileTask) {
        AfterSaleDTO.PagingParamDTO dto = readValue(fileTask.getMetaInfo(), new TypeReference<AfterSaleDTO.PagingParamDTO>() {});
        return listSeqData(dto);
    }

    @Override
    protected String getExcelPath() {
        return "excel/dmp/afterSaleExport.xlsx";
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return EXPORT_DMP_AFTER_SALE;
    }
}
