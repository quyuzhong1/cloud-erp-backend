package com.erp.rpc.dmp.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.common.business.config.ExportFeignConfig;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.erp.model.dmp.dto.AdsErpDiffOutstockSyncDTO;
import com.erp.model.dmp.dto.AdsErpInventoryDiffFlowDTO;
import com.erp.model.dmp.dto.AdsErpOutstockDiffFlowDTO;
import com.erp.model.dmp.dto.AfterSaleDTO;
import com.erp.model.dmp.dto.CfgDiffStrategyDTO;
import com.erp.model.dmp.dto.DmpOutputTaskRecordDTO;
import com.erp.model.dmp.dto.DmpPullTaskDTO;
import com.erp.model.dmp.dto.DmpPushTaskDTO;
import com.erp.model.dmp.dto.excel.DmpAfterSaleExcelDTO;

@FeignClient(name = "erp-dmp", contextId = "exportDmpFeign", configuration = ExportFeignConfig.class)
public interface ExportDmpFeign {

    @PostMapping("/feign/export/pullTaskHistory")
    PagingVO<DmpPullTaskDTO.ListDTO> exportPullTaskHistory(@RequestBody PagingDTO<DmpPullTaskDTO.ParamDTO> dto);
    @PostMapping("/feign/export/pullTask")
    PagingVO<DmpPullTaskDTO.ListDTO> exportPullTask(@RequestBody PagingDTO<DmpPullTaskDTO.ParamDTO> dto);
    @PostMapping("/feign/export/pushTask")
    PagingVO<DmpPushTaskDTO.ListDTO> exportPushTask(@RequestBody PagingDTO<DmpPushTaskDTO.ParamDTO> dto);
    @PostMapping("/feign/export/pushTaskHistory")
    PagingVO<DmpPushTaskDTO.ListDTO> exportPushTaskHistory(@RequestBody PagingDTO<DmpPushTaskDTO.ParamDTO> dto);


    @PostMapping("/feign/export/exportNewDmpPushTask")
    PagingVO<DmpOutputTaskRecordDTO.PagingDTO> exportNewDmpPushTask(@RequestBody PagingDTO<DmpOutputTaskRecordDTO.ExpotParamDTO> dto);

    @PostMapping("/feign/export/exportAfterSale")
    PagingVO<DmpAfterSaleExcelDTO> exportAfterSale(@RequestBody @Validated PagingDTO<AfterSaleDTO.PagingParamDTO> dto);
    
    @PostMapping("/feign/export/exportCfgDiffStrategy")
    PagingVO<CfgDiffStrategyDTO.ViewDTO> exportCfgDiffStrategy(@RequestBody @Validated PagingDTO<CfgDiffStrategyDTO.PagingParamDTO> dto);
    
    @PostMapping("/feign/export/exportAdsErpOutstockDiffFlow")
    PagingVO<AdsErpOutstockDiffFlowDTO.PagingDTO> exportAdsErpOutstockDiffFlow(@RequestBody @Validated PagingDTO<AdsErpOutstockDiffFlowDTO.PagingParamDTO> dto);
    
    @PostMapping("/feign/export/exportAdsErpInventoryDiffFlow")
    PagingVO<AdsErpInventoryDiffFlowDTO.ListDTO> exportAdsErpInventoryDiffFlow(@RequestBody @Validated PagingDTO<AdsErpInventoryDiffFlowDTO.PagingParamDTO> dto);
    
    @PostMapping("/feign/export/exportAdsErpDiffOutstockSync")
    PagingVO<AdsErpDiffOutstockSyncDTO.ListDTO> exportAdsErpDiffOutstockSync(@RequestBody @Validated PagingDTO<AdsErpDiffOutstockSyncDTO.PagingParamDTO> dto);
}
