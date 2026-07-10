package com.erp.server.file.business.dmp;

import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.dmp.dto.AdsErpFirstMileInTransitDiffDTO;
import com.erp.rpc.dmp.feign.ExportDmpFeign;
import com.erp.server.file.core.AbstractPageFileEventHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 *
 */
@Component
@Slf4j
public class ExportAdsErpFirstMileInTransitDiffHandler extends AbstractPageFileEventHandler<AdsErpFirstMileInTransitDiffDTO.ListDTO, AdsErpFirstMileInTransitDiffDTO.PagingParamDTO> {

    @Resource
    private ExportDmpFeign exportDmpFeign;

    @Override
    protected PagingVO<AdsErpFirstMileInTransitDiffDTO.ListDTO> getPageData(PagingDTO<AdsErpFirstMileInTransitDiffDTO.PagingParamDTO> dto) {
        return exportDmpFeign.exportAdsErpFirstMileInTransitDiff(dto);
    }


    @Override
    protected String getExcelPath() {
        return "excel/dmp/adsErpFirstMileInTransitDiff.xlsx";
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return FileTaskEventEnum.EXPORT_ADS_ERP_FIRST_MILE_INTRANSIT_DIFF;
    }
}
