package com.erp.server.plm.controller.feign;

import com.common.business.annotation.DataPermission;
import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.DataAttributeEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.plm.dto.*;
import com.erp.model.plm.dto.excel.ProductPlanExcelDTO;
import com.erp.model.plm.vo.BomExportExcelVO;
import com.erp.model.plm.vo.ProjectTaskTimeRecordPageVO;
import com.erp.server.plm.query.BomInfoHandler;
import com.erp.server.plm.query.MouldInfoQueryHandler;
import com.erp.server.plm.query.PilotApplicationQueryHandler;
import com.erp.server.plm.service.*;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/feign/export/")
public class ExportPlmFeignController {
    @Resource
    private BomInfoService bomInfoService;
    @Resource
    private ProductCertificateService productCertificateService;
    @Resource
    private TaskService taskService;
    @Resource
    private LogisticsProductService logisticsProductService;
    @Resource
    private ProductPlanService productPlanService;
    @Resource
    private ProjectPlanTaskService projectPlanTaskService;
    @Resource
    private ProjectReportFormsService projectReportFormsService;
    @Resource
    private ProjectTaskTimeRecordService projectTaskTimeRecordService;
    @Resource
    private ProjectTaskViewService projectTaskViewService;
    @Resource
    private PilotApplicationService pilotApplicationService;
    @Resource
    private MouldInfoService mouldInfoService;
    @PostMapping("/exportBom")
    @WebAdvanceQuery(handler = BomInfoHandler.class)
    public PagingVO<BomExportExcelVO> exportBom(@RequestBody PagingDTO<SearchPagingDTO> dto) {
       return bomInfoService.exportBom(dto);
    }

    @PostMapping("/exportProductCertificate")
    public PagingVO<ProductCertificateDTO.ListDTO> exportProductCertificate(@RequestBody PagingDTO<ProductCertificateDTO.ExportParamDTO> dto) {
        return productCertificateService.exportProductCertificate(dto);
    }

    @PostMapping("/exportTask")
    public PagingVO<TaskDTO.TaskExportDTO> exportTask(@RequestBody PagingDTO<TaskPagingDTO.ExportDTO> dto){
        return taskService.exportTask(dto);
    }

    @PostMapping("/scheduleTask")
    public PagingVO<TaskDTO.TaskExportDTO> exportScheduleTask(@RequestBody PagingDTO<ProjectPlanTaskConditionDTO> dto){
        return projectPlanTaskService.exportScheduleTask(dto);
    }
    @PostMapping("/taskTimeRecord")
    public PagingVO<ProjectTaskTimeRecordPageVO> exportTaskTimeRecord(@RequestBody PagingDTO<ProjectTaskTimeRecordDTO.PageRecordDto> dto){
        return projectTaskTimeRecordService.exportTaskTimeRecord(dto);
    }
    @PostMapping("/logisticsProduct")
    public PagingVO<LogisticsProductDTO.ExportInfoDTO> exportLogisticsProduct(@RequestBody PagingDTO<LogisticsProductDTO.ExportDTO> dto){
        return logisticsProductService.exportLogisticsProduct(dto);
    }
    @PostMapping("/productPlan")
    @DataPermission(operationType = DataAttributeEnum.LIST, tableField = "charge_id", menuCode = "plm:product:plan:paging", tableAlias = "pp")
    public PagingVO<ProductPlanExcelDTO> exportProductPlan(@RequestBody PagingDTO<ProductPlanSearchDTO> dto){
        return productPlanService.productPlan(dto);
    }
    @PostMapping("/productPurchaseBusiness")
    public PagingVO<ProjectReportFormsDTO.PagingView> exportProductPurchaseBusiness(@RequestBody PagingDTO<ProjectReportFormsDTO.PagingParam> dto){
        return projectReportFormsService.exportProductPurchaseBusiness(dto);
    }
    @PostMapping("/productTaskDetail")
    public PagingVO<ProjectReportFormsDTO.TaskDetail> exportProductTaskDetail(@RequestBody PagingDTO<ProjectReportFormsDTO.TaskDetailParam> dto){
        return projectReportFormsService.exportProductTaskDetail(dto);
    }
    @PostMapping("/productTaskView")
    @DataPermission(operationType = DataAttributeEnum.LIST, tableField = "charge_id", menuCode = "plm:task:view:exportExcel", tableAlias = "t")
    public PagingVO<ProductTaskViewDTO> exportProductTaskView(@RequestBody PagingDTO<ProductTaskViewSearchDTO> dto){
        return projectTaskViewService.exportProductTaskView(dto);
    }
    @PostMapping("/pilotApplication")
    @WebAdvanceQuery(handler = PilotApplicationQueryHandler.class)
    public PagingVO<PilotApplicationDTO.ListDTO> exportPilotApplication(@RequestBody @Validated PagingDTO<PilotApplicationDTO.PagingParamDTO> dto) {
        return pilotApplicationService.paging(dto);
    }

    @PostMapping("/mouldInfo")
    @WebAdvanceQuery(handler = MouldInfoQueryHandler.class)
    public PagingVO<MouldInfoDTO.MouldInfoExportDTO> exportMouldInfo(@RequestBody PagingDTO<MouldInfoDTO.PagingParamDTO> dto) {
        return mouldInfoService.exportMouldInfo(dto);
    }

    @PostMapping("/orderTracking")
    @WebAdvanceQuery
    public PagingVO<MouldInfoDTO.OrderTrackingExportDTO> exportOrderTracking(@RequestBody PagingDTO<MouldInfoDTO.PagingParamDTO> dto) {
        return mouldInfoService.exportOrderTracking(dto);
    }

    @PostMapping("/orderTrackingDetail")
    @WebAdvanceQuery
    public PagingVO<MouldInfoDTO.OrderTrackingDetailExportDTO> exportOrderTrackingDetail(@RequestBody PagingDTO<MouldInfoDTO.OrderTrackingDetailParamDTO> dto) {
        return mouldInfoService.exportOrderTrackingDetail(dto);
    }
}
