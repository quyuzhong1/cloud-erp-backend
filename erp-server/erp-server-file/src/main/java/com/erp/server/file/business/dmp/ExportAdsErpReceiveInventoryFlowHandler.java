package com.erp.server.file.business.dmp;

import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.dmp.dto.AdsErpReceiveFlowDiffDetailDTO;
import com.erp.rpc.dmp.feign.ExportDmpFeign;
import com.erp.server.file.core.AbstractPageFileEventHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 签收流水差异导出
 */
@Component
@Slf4j
public class ExportAdsErpReceiveInventoryFlowHandler extends AbstractPageFileEventHandler<AdsErpReceiveFlowDiffDetailDTO.SourcePlatformFlowDTO, AdsErpReceiveFlowDiffDetailDTO.PagingParamDTO> {

    @Resource
    private ExportDmpFeign exportDmpFeign;

    @Override
    protected PagingVO<AdsErpReceiveFlowDiffDetailDTO.SourcePlatformFlowDTO> getPageData(PagingDTO<AdsErpReceiveFlowDiffDetailDTO.PagingParamDTO> dto) {
        return exportDmpFeign.exportAdsErpReceiveInventoryFlow(dto);
    }


    @Override
    protected String getExcelPath() {
        return "excel/dmp/adsErpReceiveInventoryFlow.xlsx";
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return FileTaskEventEnum.EXPORT_ADS_ERP_RECEIVE_INVENTORY_FLOW;
    }
}
