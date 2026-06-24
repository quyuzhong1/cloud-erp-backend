package com.erp.server.file.business.dmp;

import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.dmp.dto.AdsErpDiffReturnInstockSyncDTO;
import com.erp.rpc.dmp.feign.ExportDmpFeign;
import com.erp.server.file.core.AbstractPageFileEventHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 朔源查询-erp出库单
 * @author jack
 * @date 2026/2/5 10:11
 */
@Component
@Slf4j
public class ExportAdsErpDiffReturnInstockSelfHandler extends AbstractPageFileEventHandler<AdsErpDiffReturnInstockSyncDTO.SourcePlatformDTO, AdsErpDiffReturnInstockSyncDTO.PagingParamDTO> {

    @Resource
    private ExportDmpFeign exportDmpFeign;

    @Override
    protected PagingVO<AdsErpDiffReturnInstockSyncDTO.SourcePlatformDTO> getPageData(PagingDTO<AdsErpDiffReturnInstockSyncDTO.PagingParamDTO> dto) {
        return exportDmpFeign.exportDiffReturnInstockSyncSourcePlatform(dto);
    }


    @Override
    protected String getExcelPath() {
        return "excel/dmp/adsErpDiffReturnInstockSyncSelfExport.xlsx";
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return FileTaskEventEnum.EXPORT_ADS_ERP_DIFF_RETURN_INSTOCK_SYNC_SELF;
    }
}
