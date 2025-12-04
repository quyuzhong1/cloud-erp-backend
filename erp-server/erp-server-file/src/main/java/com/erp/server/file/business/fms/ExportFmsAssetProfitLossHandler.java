package com.erp.server.file.business.fms;

import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.erp.model.fms.dto.AssetProfitLossDTO;
import com.erp.rpc.fms.feign.ExportFmsFeign;
import com.erp.server.file.core.AbstractPageFileEventHandler;
import com.erp.server.file.entity.FileTask;
import com.common.business.enums.FileTaskEventEnum;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_FMS_ASSET_PROFIT_LOSS;

/**
 * 盘盈盘亏单异步导出处理器
 * @date 2025-11-04
 * @author wuht
 */
@Component
@Slf4j
public class ExportFmsAssetProfitLossHandler extends AbstractPageFileEventHandler<AssetProfitLossDTO.ListDTO, AssetProfitLossDTO.ExportDTO> {

    @Resource
    private ExportFmsFeign exportFmsFeign;

    @Override
    protected List<AssetProfitLossDTO.ListDTO> getData(FileTask fileTask) {
        AssetProfitLossDTO.ExportDTO dto = readValue(fileTask.getMetaInfo(), new TypeReference<AssetProfitLossDTO.ExportDTO>() {
        });
        return listSeqData(dto);
    }

    @Override
    protected PagingVO<AssetProfitLossDTO.ListDTO> getPageData(PagingDTO<AssetProfitLossDTO.ExportDTO> dto) {
        return exportFmsFeign.getAssetProfitLossPageData(dto);
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return EXPORT_FMS_ASSET_PROFIT_LOSS;
    }

    @Override
    public String getExcelPath() {
        return "excel/fms/assetProfitLoss.xlsx";
    }
}

