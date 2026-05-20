package com.erp.server.file.business.dmp;

import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.dmp.dto.AdsErpInventoryDiffFlowDetailDTO;
import com.erp.rpc.dmp.feign.ExportDmpFeign;
import com.erp.server.file.core.AbstractPageFileEventHandler;
import com.erp.server.file.entity.FileTask;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * 导出
 * @author will
 * @date 2026/2/5 10:05
 */
@Component
@Slf4j
public class ExportAdsErpInventoryDetailSelfHandler extends AbstractPageFileEventHandler<AdsErpInventoryDiffFlowDetailDTO.SourceSelfDTO, AdsErpInventoryDiffFlowDetailDTO.PagingParamDTO> {

    @Resource
    private ExportDmpFeign exportDmpFeign;

    @Override
    protected PagingVO<AdsErpInventoryDiffFlowDetailDTO.SourceSelfDTO> getPageData(PagingDTO<AdsErpInventoryDiffFlowDetailDTO.PagingParamDTO> dto) {
        return exportDmpFeign.exportAdsErpInventoryDetailSelf(dto);
    }

    @Override
    protected List<AdsErpInventoryDiffFlowDetailDTO.SourceSelfDTO> getData(FileTask fileTask) {
        AdsErpInventoryDiffFlowDetailDTO.PagingParamDTO dto = readValue(fileTask.getMetaInfo(), new TypeReference<AdsErpInventoryDiffFlowDetailDTO.PagingParamDTO>() {});
        return listSeqData(dto);
    }

    @Override
    protected String getExcelPath() {
        return "excel/dmp/adsErpInventoryDetailSelfExport.xlsx";
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return FileTaskEventEnum.EXPORT_ADS_ERP_INVENTORY_DETAIL_SELF;
    }
}
