package com.erp.server.file.business.dmp;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;

import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.dmp.dto.AdsErpDiffOutstockSyncDTO;
import com.erp.rpc.dmp.feign.ExportDmpFeign;
import com.erp.server.file.core.AbstractPageFileEventHandler;
import com.erp.server.file.entity.FileTask;
import com.fasterxml.jackson.core.type.TypeReference;

import lombok.extern.slf4j.Slf4j;

/**
 * 试产量产单导出
 * @date 2024-09-11
 * @author tanmujin
 */
@Component
@Slf4j
public class ExportAdsErpDiffOutstockSyncHandler extends AbstractPageFileEventHandler<AdsErpDiffOutstockSyncDTO.ListDTO, AdsErpDiffOutstockSyncDTO.PagingParamDTO> {

    @Resource
    private ExportDmpFeign exportDmpFeign;

    @Override
    protected PagingVO<AdsErpDiffOutstockSyncDTO.ListDTO> getPageData(PagingDTO<AdsErpDiffOutstockSyncDTO.PagingParamDTO> dto) {
        return exportDmpFeign.exportAdsErpDiffOutstockSync(dto);
    }

    @Override
    protected List<AdsErpDiffOutstockSyncDTO.ListDTO> getData(FileTask fileTask) {
        AdsErpDiffOutstockSyncDTO.PagingParamDTO dto = readValue(fileTask.getMetaInfo(), new TypeReference<AdsErpDiffOutstockSyncDTO.PagingParamDTO>() {});
        return listSeqData(dto);
    }

    @Override
    protected String getExcelPath() {
        return "excel/dmp/adsErpDiffOutstockSyncExport.xlsx";
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return FileTaskEventEnum.EXPORT_ADS_ERP_DIFF_OUTSTOCK_SYNC;
    }
}
