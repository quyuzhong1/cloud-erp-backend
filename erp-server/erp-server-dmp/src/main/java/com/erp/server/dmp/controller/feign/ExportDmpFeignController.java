package com.erp.server.dmp.controller.feign;

import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.erp.model.dmp.dto.AfterSaleDTO;
import com.erp.model.dmp.dto.DmpOutputTaskRecordDTO;
import com.erp.model.dmp.dto.DmpPullTaskDTO;
import com.erp.model.dmp.dto.DmpPushTaskDTO;
import com.erp.model.dmp.dto.excel.DmpAfterSaleExcelDTO;
import com.erp.server.dmp.query.AdsPushTaskQueryHandler;
import com.erp.server.dmp.query.AfterSaleQueryHandler;
import com.erp.server.dmp.query.DmpOutputTaskRecordQueryHandler;
import com.erp.server.dmp.query.DmpTaskQueryHandler;
import com.erp.server.dmp.service.*;
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
}
