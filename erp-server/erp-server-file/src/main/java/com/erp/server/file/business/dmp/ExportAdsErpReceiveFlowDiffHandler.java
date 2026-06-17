package com.erp.server.file.business.dmp;

import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.dmp.dto.AdsErpReceiveFlowDiffDTO;
import com.erp.rpc.dmp.feign.ExportDmpFeign;
import com.erp.server.file.core.AbstractPageFileEventHandler;
import com.erp.model.file.entity.FileTask;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 签收流水差异导出
 */
@Component
@Slf4j
public class ExportAdsErpReceiveFlowDiffHandler extends AbstractPageFileEventHandler<AdsErpReceiveFlowDiffDTO.ListDTO, AdsErpReceiveFlowDiffDTO.PagingParamDTO> {

    @Resource
    private ExportDmpFeign exportDmpFeign;

    @Override
    protected PagingVO<AdsErpReceiveFlowDiffDTO.ListDTO> getPageData(PagingDTO<AdsErpReceiveFlowDiffDTO.PagingParamDTO> dto) {
        return exportDmpFeign.exportAdsErpReceiveFlowDiff(dto);
    }


    @Override
    protected String getExcelPath() {
        return "excel/dmp/adsErpReceiveFlowDiff.xlsx";
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return FileTaskEventEnum.EXPORT_ADS_ERP_RECEIVE_FLOW_DIFF;
    }
}
