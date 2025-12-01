package com.erp.server.file.business.fms;

import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.erp.model.fms.dto.AssetStocktakingDTO;
import com.erp.rpc.fms.feign.ExportFmsFeign;
import com.erp.server.file.core.AbstractPageFileEventHandler;
import com.erp.server.file.entity.FileTask;
import com.common.business.enums.FileTaskEventEnum;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_FMS_ASSET_STOCKTAKING;

/**
 * 资产盘点单异步导出处理器
 * @date 2025-10-31
 * @author wuht
 */
@Component
@Slf4j
public class ExportFmsAssetStocktakingHandler extends AbstractPageFileEventHandler<AssetStocktakingDTO.ListDTO, AssetStocktakingDTO.ExportDTO> {

    @Resource
    private ExportFmsFeign exportFmsFeign;

    @Override
    protected List<AssetStocktakingDTO.ListDTO> getData(FileTask fileTask) {
        AssetStocktakingDTO.ExportDTO dto = readValue(fileTask.getMetaInfo(), new TypeReference<AssetStocktakingDTO.ExportDTO>() {
        });
        return listSeqData(dto);
    }

    @Override
    protected PagingVO<AssetStocktakingDTO.ListDTO> getPageData(PagingDTO<AssetStocktakingDTO.ExportDTO> dto) {
        return exportFmsFeign.getAssetStocktakingPageData(dto);
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return EXPORT_FMS_ASSET_STOCKTAKING;
    }

    @Override
    public String getExcelPath() {
        return "excel/fms/assetStocktaking.xlsx";
    }
}

