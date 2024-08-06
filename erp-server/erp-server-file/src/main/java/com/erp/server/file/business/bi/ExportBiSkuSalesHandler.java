package com.erp.server.file.business.bi;

import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.bi.dto.SkuSalesDTO;
import com.erp.rpc.bi.feign.ExportBiFeign;
import com.erp.server.file.core.AbstractPageFileEventHandler;
import com.erp.server.file.entity.FileTask;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_SKU_SALES;
@Component
@Slf4j
public class ExportBiSkuSalesHandler extends AbstractPageFileEventHandler<SkuSalesDTO.PagingSalesInfoDTO, SkuSalesDTO.SearchSkuDTO> {

    @Resource
    private ExportBiFeign exportBiFeign;

    @Override
    protected List<SkuSalesDTO.PagingSalesInfoDTO> getData(FileTask fileTask) {
        SkuSalesDTO.SearchSkuDTO dto = readValue(fileTask.getMetaInfo(), new TypeReference<SkuSalesDTO.SearchSkuDTO>() {
        });
        return listSeqData(dto);
    }

    @Override
    protected PagingVO<SkuSalesDTO.PagingSalesInfoDTO> getPageData(PagingDTO<SkuSalesDTO.SearchSkuDTO> dto) {
        return exportBiFeign.exportSkuSales(dto);
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return EXPORT_SKU_SALES;
    }

    @Override
    public String getExcelPath() {
        return "excel/bi/SkuSales.xlsx";
    }
}
