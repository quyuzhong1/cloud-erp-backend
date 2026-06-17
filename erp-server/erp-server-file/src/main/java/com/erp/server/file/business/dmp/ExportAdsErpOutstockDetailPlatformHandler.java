package com.erp.server.file.business.dmp;

import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.dmp.dto.AdsErpOutstockDiffFlowDetailDTO;
import com.erp.rpc.dmp.feign.ExportDmpFeign;
import com.erp.server.file.core.AbstractPageFileEventHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 朔源查询-平台出库单
 * @author will
 * @date 2026/2/5 10:11
 */
@Component
@Slf4j
public class ExportAdsErpOutstockDetailPlatformHandler extends AbstractPageFileEventHandler<AdsErpOutstockDiffFlowDetailDTO.SourcePlatformDTO, AdsErpOutstockDiffFlowDetailDTO.PagingParamDTO> {

    @Resource
    private ExportDmpFeign exportDmpFeign;

    @Override
    protected PagingVO<AdsErpOutstockDiffFlowDetailDTO.SourcePlatformDTO> getPageData(PagingDTO<AdsErpOutstockDiffFlowDetailDTO.PagingParamDTO> dto) {
        return exportDmpFeign.exportAdsErpOutstockDetailPlatform(dto);
    }


    @Override
    protected String getExcelPath() {
        return "excel/dmp/adsErpOutstockDetailPlatformExport.xlsx";
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return FileTaskEventEnum.EXPORT_ADS_ERP_OUTSTOCK_DETAIL_PLATFORM;
    }
}
