package com.erp.server.dmp.controller.feign;

import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.erp.model.dmp.dto.*;
import com.erp.model.dmp.dto.excel.DmpAfterSaleExcelDTO;
import com.erp.server.dmp.query.AdsPushTaskQueryHandler;
import com.erp.server.dmp.query.AfterSaleQueryHandler;
import com.erp.server.dmp.query.DmpOutputTaskRecordQueryHandler;
import com.erp.server.dmp.query.DmpTaskQueryHandler;
import com.erp.server.dmp.service.*;
import org.springframework.beans.factory.annotation.Autowired;
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
    private AdsPushTaskService adsPushTaskService;
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

    @PostMapping("/exportRestcloudPushTask")
    @WebAdvanceQuery(handler = AdsPushTaskQueryHandler.class)
    public PagingVO<DmpOutputTaskRecordDTO.PagingDTO> exportRestcloudPushTask(@RequestBody PagingDTO<DmpOutputTaskRecordDTO.PagingParamDTO> dto) {
        return adsPushTaskService.paging(dto);
    }

    @PostMapping("/exportBasicSystemDetail")
    public PagingVO<DmpBasicSystemDTO.ListDTO> exportBasicSystemDetail(@RequestBody @Validated PagingDTO<DmpBasicSystemDTO.PagingParamDTO> dto) {
        return dmpBasicSystemService.paging(dto);
    }

    @PostMapping("/exportDmpCfgEtlDetail")
    public PagingVO<DmpCfgEtlDTO.ListDTO> exportDmpCfgEtlDetail(@RequestBody @Validated PagingDTO<DmpCfgEtlDTO.PagingParamDTO> dto) {
        return dmpCfgEtlService.paging(dto);
    }

    @PostMapping("/exportDmpCfgInputDetail")
    public PagingVO<DmpCfgInputDTO.ListDTO> exportDmpCfgInputDetail(@RequestBody @Validated PagingDTO<DmpCfgInputDTO.PagingParamDTO> dto) {
        return dmpCfgInputService.paging(dto);
    }

    @PostMapping("/exportDmpCfgInputDetailDetail")
    public PagingVO<DmpCfgInputDetailDTO.ListDTO> exportDmpCfgInputDetailDetail(@RequestBody @Validated PagingDTO<DmpCfgInputDetailDTO.PagingParamDTO> dto) {
        return dmpCfgInputDetailService.paging(dto);
    }

    @PostMapping("/exportDmpCfgOutputDetail")
    public PagingVO<DmpCfgOutputDTO.ListDTO> exportDmpCfgOutputDetail(@RequestBody @Validated PagingDTO<DmpCfgOutputDTO.PagingParamDTO> dto) {
        return dmpCfgOutputService.paging(dto);
    }

    @PostMapping("/exportDmpCfgOutputDetailDetail")
    public PagingVO<DmpCfgOutputDetailDTO.ListDTO> exportDmpCfgOutputDetailDetail(@RequestBody @Validated PagingDTO<DmpCfgOutputDetailDTO.PagingParamDTO> dto) {
        return dmpCfgOutputDetailService.paging(dto);
    }

    @PostMapping("/exportDmpEtlTaskDetail")
    public PagingVO<DmpEtlTaskDTO.ListDTO> exportDmpEtlTaskDetail(@RequestBody @Validated PagingDTO<DmpEtlTaskDTO.PagingParamDTO> dto) {
        return dmpEtlTaskService.paging(dto);
    }

    @PostMapping("/exportDmpInputTaskDetail")
    public PagingVO<DmpInputTaskDTO.ListDTO> exportDmpInputTaskDetail(@RequestBody @Validated PagingDTO<DmpInputTaskDTO.PagingParamDTO> dto) {
        return dmpInputTaskService.paging(dto);
    }

    @PostMapping("/exportDmpOutputTaskDetail")
    public PagingVO<DmpOutputTaskDTO.ListDTO> exportDmpOutputTaskDetail(@RequestBody @Validated PagingDTO<DmpOutputTaskDTO.PagingParamDTO> dto) {
        return dmpOutputTaskService.paging(dto);
    }
}
