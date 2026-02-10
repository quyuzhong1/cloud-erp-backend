package com.erp.server.file.business.dmp;

import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.dmp.dto.AdsErpDiffOutstockSyncDTO;
import com.erp.rpc.dmp.feign.ExportDmpFeign;
import com.erp.server.file.core.AbstractPageFileEventHandler;
import com.erp.server.file.entity.FileTask;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * 朔源查询-erp出库单
 * @author jack
 * @date 2026/2/5 10:11
 */
@Component
@Slf4j
public class ExportAdsErpDiffOutstockSyncDetailSelfHandler extends AbstractPageFileEventHandler<AdsErpDiffOutstockSyncDTO.SourcePlatformDTO, AdsErpDiffOutstockSyncDTO.PagingParamDTO> {

    @Resource
    private ExportDmpFeign exportDmpFeign;

    @Override
    protected PagingVO<AdsErpDiffOutstockSyncDTO.SourcePlatformDTO> getPageData(PagingDTO<AdsErpDiffOutstockSyncDTO.PagingParamDTO> dto) {
        return exportDmpFeign.exportDiffOutstockSyncSourcePlatform(dto);
    }

    @Override
    protected List<AdsErpDiffOutstockSyncDTO.SourcePlatformDTO> getData(FileTask fileTask) {
        AdsErpDiffOutstockSyncDTO.PagingParamDTO dto = readValue(fileTask.getMetaInfo(), new TypeReference<AdsErpDiffOutstockSyncDTO.PagingParamDTO>() {});
        return listSeqData(dto);
    }

    @Override
    protected String getExcelPath() {
        return "excel/dmp/adsErpDiffOutstockSyncDetailSelfExport.xlsx";
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return FileTaskEventEnum.EXPORT_ADS_ERP_DIFF_OUTSTOCK_SYNC_DETAIL_SELF;
    }
}
