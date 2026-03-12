package com.erp.rpc.dmp.feign;

import com.common.business.config.ExportFeignConfig;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.erp.model.dmp.dto.*;
import com.erp.model.dmp.dto.excel.DmpAfterSaleExcelDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

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

    @PostMapping("/feign/export/exportDmpBasicSystem")
    PagingVO<DmpBasicSystemDTO.ListDTO> exportDmpBasicSystem(@RequestBody @Validated PagingDTO<DmpBasicSystemDTO.PagingParamDTO> dto);

    @PostMapping("/feign/export/exportDmpCfgEtl")
    PagingVO<DmpCfgEtlDTO.ListDTO> exportDmpCfgEtl(@RequestBody @Validated PagingDTO<DmpCfgEtlDTO.ExportDTO> dto);

    @PostMapping("/feign/export/exportDmpCfgInput")
    PagingVO<DmpCfgInputDTO.ListDTO> exportDmpCfgInput(@RequestBody @Validated PagingDTO<DmpCfgInputDTO.ExportDTO> dto);

    @PostMapping("/feign/export/exportDmpCfgInputDetail")
    PagingVO<DmpCfgInputDetailDTO.ListDTO> exportDmpCfgInputDetail(@RequestBody @Validated PagingDTO<DmpCfgInputDetailDTO.ExportDTO> dto);

    @PostMapping("/feign/export/exportDmpCfgOutput")
    PagingVO<DmpCfgOutputDTO.ListDTO> exportDmpCfgOutput(@RequestBody @Validated PagingDTO<DmpCfgOutputDTO.ExportDTO> dto);

    @PostMapping("/feign/export/exportDmpCfgOutputDetail")
    PagingVO<DmpCfgOutputDetailDTO.ListDTO> exportDmpCfgOutputDetail(@RequestBody @Validated PagingDTO<DmpCfgOutputDetailDTO.ExportDTO> dto);

    @PostMapping("/feign/export/exportDmpEtlTask")
    PagingVO<DmpEtlTaskDTO.ListDTO> exportDmpEtlTask(@RequestBody @Validated PagingDTO<DmpEtlTaskDTO.ExportDTO> dto);

    @PostMapping("/feign/export/exportDmpInputTask")
    PagingVO<DmpInputTaskDTO.ListDTO> exportDmpInputTask(@RequestBody @Validated PagingDTO<DmpInputTaskDTO.ExportDTO> dto);

    @PostMapping("/feign/export/exportDmpOutputTask")
    PagingVO<DmpOutputTaskDTO.ListDTO> exportDmpOutputTask(@RequestBody @Validated PagingDTO<DmpOutputTaskDTO.ExportDTO> dto);

    @PostMapping("/feign/export/exportRestcloudPushTask")
    PagingVO<DmpOutputTaskRecordDTO.PagingDTO> exportRestcloudPushTask(@RequestBody PagingDTO<DmpOutputTaskRecordDTO.ExpotParamDTO> dto);

    @PostMapping("/feign/export/exportCfgDiffStrategy")
    PagingVO<CfgDiffStrategyDTO.ViewDTO> exportCfgDiffStrategy(@RequestBody @Validated PagingDTO<CfgDiffStrategyDTO.PagingParamDTO> dto);

    @PostMapping("/feign/export/exportAdsErpOutstockDiffFlow")
    PagingVO<AdsErpOutstockDiffFlowDTO.PagingDTO> exportAdsErpOutstockDiffFlow(@RequestBody @Validated PagingDTO<AdsErpOutstockDiffFlowDTO.PagingParamDTO> dto);

    @PostMapping("/feign/export/exportAdsErpInventoryDiffFlow")
    PagingVO<AdsErpInventoryDiffFlowDTO.ListDTO> exportAdsErpInventoryDiffFlow(@RequestBody @Validated PagingDTO<AdsErpInventoryDiffFlowDTO.PagingParamDTO> dto);

    @PostMapping("/feign/export/exportAdsErpDiffOutstockSync")
    PagingVO<AdsErpDiffOutstockSyncDTO.ListDTO> exportAdsErpDiffOutstockSync(@RequestBody @Validated PagingDTO<AdsErpDiffOutstockSyncDTO.PagingParamDTO> dto);

    @PostMapping("/feign/export/exportAdsErpDiffReturnInstockSync")
    PagingVO<AdsErpDiffReturnInstockSyncDTO.ListDTO> exportAdsErpDiffReturnInstockSync(@RequestBody @Validated PagingDTO<AdsErpDiffReturnInstockSyncDTO.PagingParamDTO> dto);

    @PostMapping("/feign/export/exportAdsErpInventoryDiff")
    PagingVO<AdsErpInventoryDiffDTO.ListDTO> exportAdsErpInventoryDiff(@RequestBody @Validated PagingDTO<AdsErpInventoryDiffDTO.PagingParamDTO> dto);

    @PostMapping("/feign/export/exportAdsErpInventoryDiffKingdee")
    PagingVO<AdsErpInventoryDiffKingdeeDTO.ListDTO> exportAdsErpInventoryDiffKingdee(@RequestBody @Validated PagingDTO<AdsErpInventoryDiffKingdeeDTO.PagingParamDTO> dto);

    @PostMapping("/feign/export/exportAdsErpFirstMileInTransitDiff")
    PagingVO<AdsErpFirstMileInTransitDiffDTO.ListDTO> exportAdsErpFirstMileInTransitDiff(@RequestBody @Validated PagingDTO<AdsErpFirstMileInTransitDiffDTO.PagingParamDTO> dto);
    /**
     * 朔源查询-平台出库单
     * @author will
     * @date 2026/2/5 10:14
     * @param dto
     * @return PagingVO<SourcePlatformDTO>
     */
    @PostMapping("/feign/export/exportAdsErpOutstockDetailPlatform")
    PagingVO<AdsErpOutstockDiffFlowDetailDTO.SourcePlatformDTO> exportAdsErpOutstockDetailPlatform(@RequestBody @Validated PagingDTO<AdsErpOutstockDiffFlowDetailDTO.PagingParamDTO> dto);

    /**
     * 朔源查询-库存流水
     * @author will
     * @date 2026/2/5 10:16
     * @param dto
     * @return PagingVO<SourceSelfDTO>
     */
    @PostMapping("/feign/export/exportAdsErpOutstockDetailSelf")
    PagingVO<AdsErpOutstockDiffFlowDetailDTO.SourceSelfDTO> exportAdsErpOutstockDetailSelf(@RequestBody @Validated PagingDTO<AdsErpOutstockDiffFlowDetailDTO.PagingParamDTO> dto);
    /**
     * 朔源查询-即时库存
     * @author will
     * @date 2026/2/5 10:18
     * @param dto
     * @return PagingVO<SourcePlatformDTO>
     */
    @PostMapping("/feign/export/exportAdsErpInventoryDetailPlatform")
    PagingVO<AdsErpInventoryDiffFlowDetailDTO.SourcePlatformDTO> exportAdsErpInventoryDetailPlatform(@RequestBody @Validated PagingDTO<AdsErpInventoryDiffFlowDetailDTO.PagingParamDTO> dto);
    /**
     * 朔源查询-库存流水
     * @author will
     * @date 2026/2/5 10:19
     * @param dto
     * @return PagingVO<SourceSelfDTO>
     */
    @PostMapping("/feign/export/exportAdsErpInventoryDetailSelf")
    PagingVO<AdsErpInventoryDiffFlowDetailDTO.SourceSelfDTO> exportAdsErpInventoryDetailSelf(@RequestBody @Validated PagingDTO<AdsErpInventoryDiffFlowDetailDTO.PagingParamDTO> dto);

    @PostMapping("/feign/export/exportDiffOutstockSyncSourcePlatform")
    PagingVO<AdsErpDiffOutstockSyncDTO.SourcePlatformDTO> exportDiffOutstockSyncSourcePlatform(PagingDTO<AdsErpDiffOutstockSyncDTO.PagingParamDTO> dto);


    @PostMapping("/feign/export/exportDiffReturnInstockSyncSourcePlatform")
    PagingVO<AdsErpDiffReturnInstockSyncDTO.SourcePlatformDTO> exportDiffReturnInstockSyncSourcePlatform(PagingDTO<AdsErpDiffReturnInstockSyncDTO.PagingParamDTO> dto);
}
