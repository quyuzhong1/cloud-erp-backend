package com.erp.server.dmp.controller.feign;

import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.erp.model.dmp.dto.*;
import com.erp.model.dmp.dto.excel.DmpAfterSaleExcelDTO;
import com.erp.server.dmp.query.*;
import com.erp.server.dmp.service.*;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/feign/export")
public class ExportDmpFeignController {

    @Resource
    private DmpPushTaskService dmpPushTaskService;
    @Resource
    private DmpPushTaskHistoryService dmpPushTaskHistoryService;
    @Resource
    private DmpPullTaskService dmpPullTaskService;
    @Resource
    private DmpPullTaskHistoryService dmpPullTaskHistoryService;
    @Resource
    private DmpOutputTaskRecordService dmpOutputTaskRecordService;
    @Resource
    private AfterSaleService afterSaleService;
    @Resource
    private DmpBasicSystemService dmpBasicSystemService;
    @Resource
    private DmpCfgEtlService dmpCfgEtlService;
    @Resource
    private DmpCfgInputService dmpCfgInputService;
    @Resource
    private DmpCfgInputDetailService dmpCfgInputDetailService;
    @Resource
    private DmpCfgOutputService dmpCfgOutputService;
    @Resource
    private DmpCfgOutputDetailService dmpCfgOutputDetailService;
    @Resource
    private DmpEtlTaskService dmpEtlTaskService;
    @Resource
    private DmpInputTaskService dmpInputTaskService;
    @Resource
    private DmpOutputTaskService dmpOutputTaskService;
    @Resource
    private AdsPushTaskService adsPushTaskService;
    @Resource
    private CfgDiffStrategyService cfgDiffStrategyService;
    @Resource
    private AdsErpOutstockDiffFlowService adsErpOutstockDiffFlowService;
    @Resource
    private AdsErpInventoryDiffFlowService adsErpInventoryDiffFlowService;
    @Resource
    private AdsErpDiffOutstockSyncService adsErpDiffOutstockSyncService;
    @Resource
    private AdsErpDiffReturnInstockSyncService adsErpDiffReturnInstockSyncService;
    @Resource
    private AdsErpInventoryDiffService adsErpInventoryDiffService;
    @Resource
    private AdsErpFirstMileInTransitDiffService adsErpFirstMileInTransitDiffService;
    @Resource
    private AdsErpInventoryDiffKingdeeService adsErpInventoryDiffKingdeeService;
    @Resource
    private AdsErpReceiveFlowDiffService adsErpReceiveFlowDiffService;



    @PostMapping("/pullTaskHistory")
    @WebAdvanceQuery(handler = DmpTaskQueryHandler.class)
    public PagingVO<DmpPullTaskDTO.ListDTO> exportPullTaskHistory(@RequestBody PagingDTO<DmpPullTaskDTO.ParamDTO> dto) {
        return dmpPullTaskHistoryService.exportPullTaskHistory(dto);
    }

    @PostMapping("/pullTask")
    @WebAdvanceQuery(handler = DmpTaskQueryHandler.class)
    public PagingVO<DmpPullTaskDTO.ListDTO> exportPullTask(@RequestBody PagingDTO<DmpPullTaskDTO.ParamDTO> dto) {
        return dmpPullTaskService.exportPullTask(dto);
    }

    @PostMapping("/pushTask")
    @WebAdvanceQuery(handler = DmpTaskQueryHandler.class)
    public PagingVO<DmpPushTaskDTO.ListDTO> exportPushTask(@RequestBody PagingDTO<DmpPushTaskDTO.ParamDTO> dto) {
        return dmpPushTaskService.exportPushTask(dto);
    }

    @PostMapping("/pushTaskHistory")
    @WebAdvanceQuery(handler = DmpTaskQueryHandler.class)
    public PagingVO<DmpPushTaskDTO.ListDTO> exportPushTaskHistory(@RequestBody PagingDTO<DmpPushTaskDTO.ParamDTO> dto) {
        return dmpPushTaskHistoryService.exportPushTaskHistory(dto);
    }


    @PostMapping("/exportNewDmpPushTask")
    @WebAdvanceQuery(handler = DmpOutputTaskRecordQueryHandler.class)
    public PagingVO<DmpOutputTaskRecordDTO.PagingDTO> exportNewDmpPushTask(@RequestBody PagingDTO<DmpOutputTaskRecordDTO.PagingParamDTO> dto) {
        return dmpOutputTaskRecordService.paging(dto);
    }


    @PostMapping("/exportAfterSale")
    @WebAdvanceQuery(handler = AfterSaleQueryHandler.class)
    public PagingVO<DmpAfterSaleExcelDTO> exportAfterSale(@RequestBody PagingDTO<AfterSaleDTO.PagingParamDTO> dto) {
        return afterSaleService.exportList(dto);
    }

    @PostMapping("/exportDmpBasicSystem")
    @WebAdvanceQuery(handler = DmpBasicSystemQueryHandler.class)
    public PagingVO<DmpBasicSystemDTO.ListDTO> exportBasicSystem(@RequestBody @Validated PagingDTO<DmpBasicSystemDTO.PagingParamDTO> dto) {
        return dmpBasicSystemService.paging(dto);
    }

    @PostMapping("/exportDmpCfgEtl")
    @WebAdvanceQuery(handler = DmpCfgEtlQueryHandler.class)
    public PagingVO<DmpCfgEtlDTO.ListDTO> exportDmpCfgEtl(@RequestBody @Validated PagingDTO<DmpCfgEtlDTO.PagingParamDTO> dto) {
        return dmpCfgEtlService.paging(dto);
    }

    @PostMapping("/exportDmpCfgInput")
    @WebAdvanceQuery(handler = DmpCfgInputQueryHandler.class)
    public PagingVO<DmpCfgInputDTO.ListDTO> exportDmpCfgInput(@RequestBody @Validated PagingDTO<DmpCfgInputDTO.PagingParamDTO> dto) {
        return dmpCfgInputService.paging(dto);
    }

    @PostMapping("/exportDmpCfgInputDetail")
    @WebAdvanceQuery(handler = DmpCfgOutputDetailQueryHandler.class)
    public PagingVO<DmpCfgInputDetailDTO.ListDTO> exportDmpCfgInputDetail(@RequestBody @Validated PagingDTO<DmpCfgInputDetailDTO.PagingParamDTO> dto) {
        return dmpCfgInputDetailService.paging(dto);
    }

    @PostMapping("/exportDmpCfgOutput")
    @WebAdvanceQuery(handler = DmpCfgOutputQueryHandler.class)
    public PagingVO<DmpCfgOutputDTO.ListDTO> exportDmpCfgOutput(@RequestBody @Validated PagingDTO<DmpCfgOutputDTO.PagingParamDTO> dto) {
        return dmpCfgOutputService.paging(dto);
    }

    @PostMapping("/exportDmpCfgOutputDetail")
    @WebAdvanceQuery(handler = DmpCfgInputDetailQueryHandler.class)
    public PagingVO<DmpCfgOutputDetailDTO.ListDTO> exportDmpCfgOutputDetai(@RequestBody @Validated PagingDTO<DmpCfgOutputDetailDTO.PagingParamDTO> dto) {
        return dmpCfgOutputDetailService.paging(dto);
    }

    @PostMapping("/exportDmpEtlTask")
    @WebAdvanceQuery(handler = DmpEtlTaskQueryHandler.class)
    public PagingVO<DmpEtlTaskDTO.ListDTO> exportDmpEtlTask(@RequestBody @Validated PagingDTO<DmpEtlTaskDTO.PagingParamDTO> dto) {
        return dmpEtlTaskService.paging(dto);
    }

    @PostMapping("/exportDmpInputTask")
    @WebAdvanceQuery(handler = DmpInputTaskQueryHandler.class)
    public PagingVO<DmpInputTaskDTO.ListDTO> exportDmpInputTask(@RequestBody @Validated PagingDTO<DmpInputTaskDTO.PagingParamDTO> dto) {
        return dmpInputTaskService.paging(dto);
    }

    @PostMapping("/exportDmpOutputTask")
    @WebAdvanceQuery(handler = DmpOutputTaskQueryHandler.class)
    public PagingVO<DmpOutputTaskDTO.ListDTO> exportDmpOutputTask(@RequestBody @Validated PagingDTO<DmpOutputTaskDTO.PagingParamDTO> dto) {
        return dmpOutputTaskService.paging(dto);
    }

    @PostMapping("/exportRestcloudPushTask")
    @WebAdvanceQuery(handler = AdsPushTaskQueryHandler.class)
    public PagingVO<DmpOutputTaskRecordDTO.PagingDTO> exportRestcloudPushTask(@RequestBody PagingDTO<DmpOutputTaskRecordDTO.PagingParamDTO> dto) {
        return adsPushTaskService.paging(dto);
    }

    @PostMapping("/exportCfgDiffStrategy")
    @WebAdvanceQuery(handler = CfgDiffStrategyQueryHandler.class)
    public PagingVO<CfgDiffStrategyDTO.ViewDTO> exportCfgDiffStrategy(@RequestBody @Validated PagingDTO<CfgDiffStrategyDTO.PagingParamDTO> dto) {
    	return cfgDiffStrategyService.paging(dto);
    }

    @PostMapping("/exportAdsErpOutstockDiffFlow")
    @WebAdvanceQuery(handler = AdsErpOutstockDiffFlowQueryHandler.class)
    public PagingVO<AdsErpOutstockDiffFlowDTO.PagingDTO> exportAdsErpOutstockDiffFlow(@RequestBody @Validated PagingDTO<AdsErpOutstockDiffFlowDTO.PagingParamDTO> dto) {
    	return adsErpOutstockDiffFlowService.paging(dto);
    }

    @PostMapping("/exportAdsErpInventoryDiffFlow")
    @WebAdvanceQuery(handler = AdsErpInventoryDiffFlowQueryHandler.class)
    public PagingVO<AdsErpInventoryDiffFlowDTO.ListDTO> exportAdsErpInventoryDiffFlow(@RequestBody @Validated PagingDTO<AdsErpInventoryDiffFlowDTO.PagingParamDTO> dto) {
    	return adsErpInventoryDiffFlowService.paging(dto);
    }

    @PostMapping("/exportAdsErpDiffOutstockSync")
    @WebAdvanceQuery(handler = AdsErpDiffOutstockSyncQueryHandler.class)
    public PagingVO<AdsErpDiffOutstockSyncDTO.ListDTO> exportAdsErpDiffOutstockSync(@RequestBody @Validated PagingDTO<AdsErpDiffOutstockSyncDTO.PagingParamDTO> dto) {
    	return adsErpDiffOutstockSyncService.paging(dto);
    }

    @PostMapping("/exportAdsErpDiffReturnInstockSync")
    @WebAdvanceQuery(handler = AdsErpDiffReturnInstockSyncQueryHandler.class)
    public PagingVO<AdsErpDiffReturnInstockSyncDTO.ListDTO> exportAdsErpDiffReturnInstockSync(@RequestBody @Validated PagingDTO<AdsErpDiffReturnInstockSyncDTO.PagingParamDTO> dto) {
    	return adsErpDiffReturnInstockSyncService.paging(dto);
    }

    @PostMapping("/exportAdsErpInventoryDiff")
    @WebAdvanceQuery(handler = AdsErpInventoryDiffQueryHandler.class)
    public PagingVO<AdsErpInventoryDiffDTO.ListDTO> exportAdsErpInventoryDiff(@RequestBody @Validated PagingDTO<AdsErpInventoryDiffDTO.PagingParamDTO> dto){
        return adsErpInventoryDiffService.paging(dto);
    }

    @PostMapping("/exportAdsErpInventoryDiffKingdee")
    @WebAdvanceQuery(handler = AdsErpInventoryDiffKingdeeQueryHandler.class)
    public PagingVO<AdsErpInventoryDiffKingdeeDTO.ListDTO> exportAdsErpInventoryDiffKingdee(@RequestBody @Validated PagingDTO<AdsErpInventoryDiffKingdeeDTO.PagingParamDTO> dto){
        return adsErpInventoryDiffKingdeeService.paging(dto);
    }

    @PostMapping("/exportAdsErpFirstMileInTransitDiff")
    @WebAdvanceQuery(handler = AdsErpFirstMileInTransitDiffQueryHandler.class)
    public PagingVO<AdsErpFirstMileInTransitDiffDTO.ListDTO> exportAdsErpFirstMileInTransitDiff(@RequestBody @Validated PagingDTO<AdsErpFirstMileInTransitDiffDTO.PagingParamDTO> dto){
        return adsErpFirstMileInTransitDiffService.paging(dto);
    }

    /**
     * 签收流水差异导出
     */
    @PostMapping("/exportAdsErpReceiveFlowDiff")
    @WebAdvanceQuery(handler = AdsErpReceiveFlowDiffQueryHandler.class)
    public PagingVO<AdsErpReceiveFlowDiffDTO.ListDTO> exportAdsErpReceiveFlowDiff(@RequestBody @Validated PagingDTO<AdsErpReceiveFlowDiffDTO.PagingParamDTO> dto){
        return adsErpReceiveFlowDiffService.paging(dto);
    }
    /**
     * 签收流水溯源-库存流水
     */
    @PostMapping("/exportAdsErpReceiveInventoryFlow")
    public PagingVO<AdsErpReceiveFlowDiffDetailDTO.SourcePlatformFlowDTO> exportAdsErpReceiveInventoryFlow(@RequestBody @Validated PagingDTO<AdsErpReceiveFlowDiffDetailDTO.PagingParamDTO> dto){
        return adsErpReceiveFlowDiffService.sourcePlatformFlowPaging(dto);
    }
    /**
     * 签收流水溯源-直接调拨单
     */
    @PostMapping("/exportAdsErpReceiveTransfer")
    public PagingVO<AdsErpReceiveFlowDiffDetailDTO.SourceTransferInfoDTO> exportAdsErpReceiveTransfer(@RequestBody @Validated PagingDTO<AdsErpReceiveFlowDiffDetailDTO.PagingParamDTO> dto){
        return adsErpReceiveFlowDiffService.transferInfoPaging(dto);
    }
}
