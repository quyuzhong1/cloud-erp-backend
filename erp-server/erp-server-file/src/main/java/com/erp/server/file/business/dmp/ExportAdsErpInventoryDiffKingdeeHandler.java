package com.erp.server.file.business.dmp;

import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.dmp.dto.AdsErpInventoryDiffDTO;
import com.erp.model.dmp.dto.AdsErpInventoryDiffKingdeeDTO;
import com.erp.rpc.dmp.feign.ExportDmpFeign;
import com.erp.server.file.core.AbstractPageFileEventHandler;
import com.erp.server.file.entity.FileTask;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 *
 */
@Component
@Slf4j
public class ExportAdsErpInventoryDiffKingdeeHandler extends AbstractPageFileEventHandler<AdsErpInventoryDiffKingdeeDTO.ListDTO, AdsErpInventoryDiffKingdeeDTO.PagingParamDTO> {

    @Resource
    private ExportDmpFeign exportDmpFeign;

    @Override
    protected PagingVO<AdsErpInventoryDiffKingdeeDTO.ListDTO> getPageData(PagingDTO<AdsErpInventoryDiffKingdeeDTO.PagingParamDTO> dto) {
        return exportDmpFeign.exportAdsErpInventoryDiffKingdee(dto);
    }

    @Override
    protected List<AdsErpInventoryDiffKingdeeDTO.ListDTO> getData(FileTask fileTask) {
        AdsErpInventoryDiffKingdeeDTO.PagingParamDTO dto = readValue(fileTask.getMetaInfo(), new TypeReference<AdsErpInventoryDiffKingdeeDTO.PagingParamDTO>() {});
        return listSeqData(dto);
    }

    @Override
    protected String getExcelPath() {
        return "excel/dmp/adsErpInventoryDiffKingdee.xlsx";
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return FileTaskEventEnum.EXPORT_ADS_ERP_INVENTORY_DIFF_KINGDEE;
    }
}
