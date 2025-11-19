package com.erp.server.dmp.controller.feign;

import javax.annotation.Resource;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.erp.model.dmp.dto.AdsErpDiffOutstockSyncDTO;
import com.erp.model.dmp.dto.AdsErpDiffReturnInstockSyncDTO;
import com.erp.model.dmp.dto.AdsErpInventoryDiffFlowDTO;
import com.erp.model.dmp.dto.AdsErpOutstockDiffFlowDTO;
import com.erp.model.dmp.dto.AfterSaleDTO;
import com.erp.model.dmp.dto.CfgDiffStrategyDTO;
import com.erp.model.dmp.dto.DmpOutputTaskRecordDTO;
import com.erp.model.dmp.dto.DmpPullTaskDTO;
import com.erp.model.dmp.dto.DmpPushTaskDTO;
import com.erp.model.dmp.dto.excel.DmpAfterSaleExcelDTO;
import com.erp.server.dmp.query.AdsErpDiffOutstockSyncQueryHandler;
import com.erp.server.dmp.query.AdsErpDiffReturnInstockSyncQueryHandler;
import com.erp.server.dmp.query.AdsErpInventoryDiffFlowQueryHandler;
import com.erp.server.dmp.query.AdsErpOutstockDiffFlowQueryHandler;
import com.erp.server.dmp.query.AfterSaleQueryHandler;
import com.erp.server.dmp.query.CfgDiffStrategyQueryHandler;
import com.erp.server.dmp.query.DmpOutputTaskRecordQueryHandler;
import com.erp.server.dmp.query.DmpTaskQueryHandler;
import com.erp.server.dmp.service.AdsErpDiffOutstockSyncService;
import com.erp.server.dmp.service.AdsErpDiffReturnInstockSyncService;
import com.erp.server.dmp.service.AdsErpInventoryDiffFlowService;
import com.erp.server.dmp.service.AdsErpOutstockDiffFlowService;
import com.erp.server.dmp.service.AfterSaleService;
import com.erp.server.dmp.service.CfgDiffStrategyService;
import com.erp.server.dmp.service.DmpOutputTaskRecordService;
import com.erp.server.dmp.service.DmpPullTaskHistoryService;
import com.erp.server.dmp.service.DmpPullTaskService;
import com.erp.server.dmp.service.DmpPushTaskHistoryService;
import com.erp.server.dmp.service.DmpPushTaskService;

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
    private CfgDiffStrategyService cfgDiffStrategyService;
    @Resource
    private AdsErpOutstockDiffFlowService adsErpOutstockDiffFlowService;
    @Resource
    private AdsErpInventoryDiffFlowService adsErpInventoryDiffFlowService;
    @Resource
    private AdsErpDiffOutstockSyncService adsErpDiffOutstockSyncService;
    @Resource
    private AdsErpDiffReturnInstockSyncService adsErpDiffReturnInstockSyncService;

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
}
