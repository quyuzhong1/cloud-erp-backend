package com.erp.server.file.business.fms;

import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.erp.model.fms.dto.AssetCardDTO;
import com.erp.rpc.fms.feign.ExportFmsFeign;
import com.erp.server.file.core.AbstractPageFileEventHandler;
import com.erp.server.file.entity.FileTask;
import com.common.business.enums.FileTaskEventEnum;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_FMS_ASSET_CARD;

/**
 * 资产卡片异步导出处理器
 * @date 2025-10-24
 * @author wuht
 */
@Component
@Slf4j
public class ExportFmsAssetCardHandler extends AbstractPageFileEventHandler<AssetCardDTO.ListDTO, AssetCardDTO.ExportDTO> {

    @Resource
    private ExportFmsFeign exportFmsFeign;

    @Override
    protected List<AssetCardDTO.ListDTO> getData(FileTask fileTask) {
        AssetCardDTO.ExportDTO dto = readValue(fileTask.getMetaInfo(), new TypeReference<AssetCardDTO.ExportDTO>() {
        });
        return listSeqData(dto);
    }

    @Override
    protected PagingVO<AssetCardDTO.ListDTO> getPageData(PagingDTO<AssetCardDTO.ExportDTO> dto) {
        return exportFmsFeign.getAssetCardPageData(dto);
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return EXPORT_FMS_ASSET_CARD;
    }

    @Override
    public String getExcelPath() {
        return "excel/fms/assetCard.xlsx";
    }
}

