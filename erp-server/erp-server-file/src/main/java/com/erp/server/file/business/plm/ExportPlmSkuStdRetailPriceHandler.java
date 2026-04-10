package com.erp.server.file.business.plm;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;

import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.plm.dto.SkuStdRetailPriceDTO;
import com.erp.rpc.plm.feign.ExportPlmFeign;
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
public class ExportPlmSkuStdRetailPriceHandler extends AbstractPageFileEventHandler<SkuStdRetailPriceDTO.ListDTO, SkuStdRetailPriceDTO.PagingParamDTO> {

    @Resource
    private ExportPlmFeign exportPlmFeign;

    @Override
    protected PagingVO<SkuStdRetailPriceDTO.ListDTO> getPageData(PagingDTO<SkuStdRetailPriceDTO.PagingParamDTO> dto) {
        return exportPlmFeign.skuStdRetailPrice(dto);
    }

    @Override
    protected List<SkuStdRetailPriceDTO.ListDTO> getData(FileTask fileTask) {
        SkuStdRetailPriceDTO.PagingParamDTO dto = readValue(fileTask.getMetaInfo(), new TypeReference<SkuStdRetailPriceDTO.PagingParamDTO>() {});
        return listSeqData(dto);
    }

    @Override
    protected String getExcelPath() {
        return "excel/plm/skuStdRetailPrice.xlsx";
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return FileTaskEventEnum.EXPORT_PLM_SKU_STD_RETAIL_PRICE;
    }
}
