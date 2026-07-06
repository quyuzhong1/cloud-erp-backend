package com.erp.server.file.business.dmp;


import javax.annotation.Resource;

import org.springframework.stereotype.Component;

import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.dmp.dto.AdsErpDiffReturnInstockSyncDTO;
import com.erp.rpc.dmp.feign.ExportDmpFeign;
import com.erp.server.file.core.AbstractPageFileEventHandler;

import lombok.extern.slf4j.Slf4j;

/**
 * 试产量产单导出
 * @date 2024-09-11
 * @author tanmujin
 */
@Component
@Slf4j
public class ExportAdsErpDiffReturnInstockSyncHandler extends AbstractPageFileEventHandler<AdsErpDiffReturnInstockSyncDTO.ListDTO, AdsErpDiffReturnInstockSyncDTO.PagingParamDTO> {

    @Resource
    private ExportDmpFeign exportDmpFeign;

    @Override
    protected PagingVO<AdsErpDiffReturnInstockSyncDTO.ListDTO> getPageData(PagingDTO<AdsErpDiffReturnInstockSyncDTO.PagingParamDTO> dto) {
        return exportDmpFeign.exportAdsErpDiffReturnInstockSync(dto);
    }


    @Override
    protected String getExcelPath() {
        return "excel/dmp/adsErpDiffReturnInstockSyncExport.xlsx";
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return FileTaskEventEnum.EXPORT_ADS_ERP_DIFF_RETURN_INSTOCK_SYNC;
    }
}
