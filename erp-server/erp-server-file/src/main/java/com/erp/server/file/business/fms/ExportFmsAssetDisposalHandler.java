package com.erp.server.file.business.fms;

import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.fms.dto.AssetDisposalDTO;
import com.erp.rpc.fms.feign.ExportFmsFeign;
import com.erp.server.file.core.AbstractPageFileEventHandler;
import com.erp.server.file.entity.FileTask;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_FMS_ASSET_DISPOSAL;

/**
 * 资产盘点方案异步导出处理器
 * @date 2025-10-27
 * @author wuht
 */
@Component
@Slf4j
public class ExportFmsAssetDisposalHandler extends AbstractPageFileEventHandler<AssetDisposalDTO.ListDTO, AssetDisposalDTO.PagingParamDTO> {

    @Resource
    private ExportFmsFeign exportFmsFeign;

    @Override
    protected List<AssetDisposalDTO.ListDTO> getData(FileTask fileTask) {
        AssetDisposalDTO.PagingParamDTO dto = readValue(fileTask.getMetaInfo(), new TypeReference<AssetDisposalDTO.PagingParamDTO>() {
        });
        return listSeqData(dto);
    }

    @Override
    protected PagingVO<AssetDisposalDTO.ListDTO> getPageData(PagingDTO<AssetDisposalDTO.PagingParamDTO> dto) {
        return exportFmsFeign.exportAssetDisposal(dto);
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return EXPORT_FMS_ASSET_DISPOSAL;
    }

    @Override
    public String getExcelPath() {
        return "excel/fms/assetDisposal.xlsx";
    }
}

