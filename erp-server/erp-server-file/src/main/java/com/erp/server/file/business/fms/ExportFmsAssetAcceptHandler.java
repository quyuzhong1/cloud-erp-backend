package com.erp.server.file.business.fms;

import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.erp.model.fms.dto.AssetAcceptDTO;
import com.erp.rpc.fms.feign.ExportFmsFeign;
import com.erp.server.file.core.AbstractPageFileEventHandler;
import com.erp.server.file.entity.FileTask;
import com.common.business.enums.FileTaskEventEnum;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_FMS_ASSET_ACCEPT_REPORT;

/**
 * 资产验收表异步导出处理器
 * @date 2025-10-11
 * @author wuht
 */
@Component
@Slf4j
public class ExportFmsAssetAcceptHandler extends AbstractPageFileEventHandler<AssetAcceptDTO.ListDTO, AssetAcceptDTO.ExportDTO> {

    @Resource
    private ExportFmsFeign exportFmsFeign;

    @Override
    protected List<AssetAcceptDTO.ListDTO> getData(FileTask fileTask) {
        AssetAcceptDTO.ExportDTO dto = readValue(fileTask.getMetaInfo(), new TypeReference<AssetAcceptDTO.ExportDTO>() {
        });
        return listSeqData(dto);
    }

    @Override
    protected PagingVO<AssetAcceptDTO.ListDTO> getPageData(PagingDTO<AssetAcceptDTO.ExportDTO> dto) {
        return exportFmsFeign.getAssetAcceptPageData(dto);
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return EXPORT_FMS_ASSET_ACCEPT_REPORT;
    }

    @Override
    public String getExcelPath() {
        return "excel/fms/assetAccept.xlsx";
    }
}
