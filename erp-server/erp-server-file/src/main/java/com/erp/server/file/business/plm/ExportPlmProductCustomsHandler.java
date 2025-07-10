package com.erp.server.file.business.plm;

import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.plm.dto.ProductCustomsDTO;
import com.erp.rpc.plm.feign.ExportPlmFeign;
import com.erp.server.file.core.AbstractPageFileEventHandler;
import com.erp.server.file.entity.FileTask;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_PLM_PRODUCT_CUSTOMS;

/**
 * 试产量产单导出
 * @date 2024-09-11
 * @author tanmujin
 */
@Component
@Slf4j
public class ExportPlmProductCustomsHandler extends AbstractPageFileEventHandler<ProductCustomsDTO.ListDTO, ProductCustomsDTO.PagingParamDTO> {

    @Resource
    private ExportPlmFeign exportPlmFeign;

    @Override
    protected PagingVO<ProductCustomsDTO.ListDTO> getPageData(PagingDTO<ProductCustomsDTO.PagingParamDTO> dto) {
        return exportPlmFeign.exportProductCustoms(dto);
    }

    @Override
    protected List<ProductCustomsDTO.ListDTO> getData(FileTask fileTask) {
        ProductCustomsDTO.PagingParamDTO dto = readValue(fileTask.getMetaInfo(), new TypeReference<ProductCustomsDTO.PagingParamDTO>() {});
        return listSeqData(dto);
    }

    @Override
    protected String getExcelPath() {
        return "excel/plm/productCustoms.xlsx";
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return EXPORT_PLM_PRODUCT_CUSTOMS;
    }
}
