package com.erp.server.file.business.plm;

import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.plm.dto.ProductCustomsDTO;
import com.erp.model.plm.dto.SkuStdCostDetailDTO;
import com.erp.rpc.plm.feign.ExportPlmFeign;
import com.erp.server.file.core.AbstractPageFileEventHandler;
import com.erp.server.file.entity.FileTask;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_PLM_SKU_STD_COST;

/**
 * 目的国清关信息导出
 *
 * @author Jim
 * @date 2025-08-11
 */
@Component
@Slf4j
public class ExportPlmSkuStdCostHandler extends AbstractPageFileEventHandler<SkuStdCostDetailDTO.ListDTO, SkuStdCostDetailDTO.ExportDTO> {

    @Resource
    private ExportPlmFeign exportPlmFeign;

    @Override
    protected PagingVO<SkuStdCostDetailDTO.ListDTO> getPageData(PagingDTO<SkuStdCostDetailDTO.ExportDTO> dto) {
        return exportPlmFeign.exportSkuStdCostDetail(dto);
    }

    @Override
    protected List<SkuStdCostDetailDTO.ListDTO> getData(FileTask fileTask) {
        SkuStdCostDetailDTO.ExportDTO dto = readValue(fileTask.getMetaInfo(), new TypeReference<SkuStdCostDetailDTO.ExportDTO>() {
        });
        return listSeqData(dto);
    }

    @Override
    protected String getExcelPath() {
        return "excel/plm/skuStdCostDetail.xlsx";
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return EXPORT_PLM_SKU_STD_COST;
    }
}
