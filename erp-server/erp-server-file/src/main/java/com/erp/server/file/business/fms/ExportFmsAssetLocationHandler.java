package com.erp.server.file.business.fms;

import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.erp.model.fms.dto.AssetLocationDTO;
import com.erp.rpc.fms.feign.ExportFmsFeign;
import com.erp.server.file.core.AbstractPageFileEventHandler;
import com.common.business.enums.FileTaskEventEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_FMS_ASSET_LOCATION;

/**
 * 资产位置异步导出处理器
 * @date 2025-10-13
 * @author wuht
 */
@Component
@Slf4j
public class ExportFmsAssetLocationHandler extends AbstractPageFileEventHandler<AssetLocationDTO.ListDTO, AssetLocationDTO.ExportDTO> {

    @Resource
    private ExportFmsFeign exportFmsFeign;


    @Override
    protected PagingVO<AssetLocationDTO.ListDTO> getPageData(PagingDTO<AssetLocationDTO.ExportDTO> dto) {
        return exportFmsFeign.getAssetLocationPageData(dto);
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return EXPORT_FMS_ASSET_LOCATION;
    }

    @Override
    public String getExcelPath() {
        return "excel/fms/assetLocation.xlsx";
    }
}

