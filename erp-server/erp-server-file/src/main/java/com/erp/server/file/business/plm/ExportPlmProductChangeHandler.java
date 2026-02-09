package com.erp.server.file.business.plm;

import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.plm.dto.PilotApplicationDTO;
import com.erp.model.plm.dto.ProductChangeDTO;
import com.erp.rpc.plm.feign.ExportPlmFeign;
import com.erp.server.file.core.AbstractPageFileEventHandler;
import com.erp.server.file.entity.FileTask;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_PLM_PRODUCT_CHANGE;

/**
 * 试产量产单导出
 * @date 2024-09-11
 * @author tanmujin
 */
@Component
@Slf4j
public class ExportPlmProductChangeHandler extends AbstractPageFileEventHandler<ProductChangeDTO.ListDTO, ProductChangeDTO.PagingParamDTO> {

    @Resource
    private ExportPlmFeign exportPlmFeign;

    @Override
    protected PagingVO<ProductChangeDTO.ListDTO> getPageData(PagingDTO<ProductChangeDTO.PagingParamDTO> dto) {
        return exportPlmFeign.productChange(dto);
    }

    @Override
    protected List<ProductChangeDTO.ListDTO> getData(FileTask fileTask) {
        ProductChangeDTO.PagingParamDTO dto = readValue(fileTask.getMetaInfo(), new TypeReference<ProductChangeDTO.PagingParamDTO>() {});
        return listSeqData(dto);
    }

    @Override
    protected String getExcelPath() {
        return "excel/plm/productChange.xlsx";
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return EXPORT_PLM_PRODUCT_CHANGE;
    }
}
