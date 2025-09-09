package com.erp.server.file.business.oms;

import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.oms.dto.SkuMappingDTO;
import com.erp.rpc.oms.feign.ExportOmsFeign;
import com.erp.server.file.core.AbstractPageFileEventHandler;
import com.erp.server.file.entity.FileTask;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_OMS_B2B_PLATFORM_SKU;

@Component
@Slf4j
public class ExportOmsB2BPlatformSkuHandler extends AbstractPageFileEventHandler<SkuMappingDTO.PagingViewDTO, SkuMappingDTO.PagingParamDTO> {

    @Resource
    private ExportOmsFeign exportOmsFeign;
    @Override
    protected List<SkuMappingDTO.PagingViewDTO> getData(FileTask fileTask) {
        SkuMappingDTO.PagingParamDTO dto = readValue(fileTask.getMetaInfo(), new TypeReference<SkuMappingDTO.PagingParamDTO>() {
        });
        return listSeqData(dto);
    }

    @Override
    protected PagingVO<SkuMappingDTO.PagingViewDTO> getPageData(PagingDTO<SkuMappingDTO.PagingParamDTO> dto) {
        return exportOmsFeign.exportB2bPlatformSku(dto);
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return EXPORT_OMS_B2B_PLATFORM_SKU;
    }

    @Override
    public String getExcelPath() {
        return "excel/oms/B2BPlatformSkuMapping.xlsx";
    }
}
