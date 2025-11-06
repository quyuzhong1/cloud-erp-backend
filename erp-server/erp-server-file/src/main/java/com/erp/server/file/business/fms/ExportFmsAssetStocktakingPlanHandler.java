package com.erp.server.file.business.fms;

import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.erp.model.fms.dto.AssetStocktakingPlanDTO;
import com.erp.rpc.fms.feign.ExportFmsFeign;
import com.erp.server.file.core.AbstractPageFileEventHandler;
import com.erp.server.file.entity.FileTask;
import com.common.business.enums.FileTaskEventEnum;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_FMS_ASSET_STOCKTAKING_PLAN;

/**
 * 资产盘点方案异步导出处理器
 * @date 2025-10-27
 * @author wuht
 */
@Component
@Slf4j
public class ExportFmsAssetStocktakingPlanHandler extends AbstractPageFileEventHandler<AssetStocktakingPlanDTO.ListDTO, AssetStocktakingPlanDTO.ExportDTO> {

    @Resource
    private ExportFmsFeign exportFmsFeign;

    @Override
    protected List<AssetStocktakingPlanDTO.ListDTO> getData(FileTask fileTask) {
        AssetStocktakingPlanDTO.ExportDTO dto = readValue(fileTask.getMetaInfo(), new TypeReference<AssetStocktakingPlanDTO.ExportDTO>() {
        });
        return listSeqData(dto);
    }

    @Override
    protected PagingVO<AssetStocktakingPlanDTO.ListDTO> getPageData(PagingDTO<AssetStocktakingPlanDTO.ExportDTO> dto) {
        return exportFmsFeign.getAssetStocktakingPlanPageData(dto);
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return EXPORT_FMS_ASSET_STOCKTAKING_PLAN;
    }

    @Override
    public String getExcelPath() {
        return "excel/fms/assetStocktakingPlan.xlsx";
    }
}

