package com.erp.server.plm.controller.feign;

import javax.annotation.Resource;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.common.business.annotation.DataPermission;
import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.dto.DynamicExcelDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.DataAttributeEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.plm.dto.CfgMoldAlertRuleDTO;
import com.erp.model.plm.dto.CfgMoldReturnAlertRuleDTO;
import com.erp.model.plm.dto.LogisticsProductDTO;
import com.erp.model.plm.dto.MoldInfoDTO;
import com.erp.model.plm.dto.MoldMonitorDTO;
import com.erp.model.plm.dto.MoldRefSkuDTO;
import com.erp.model.plm.dto.MouldInfoDTO;
import com.erp.model.plm.dto.PilotApplicationDTO;
import com.erp.model.plm.dto.ProductCertificateDTO;
import com.erp.model.plm.dto.ProductChangeDTO;
import com.erp.model.plm.dto.ProductCustomsDTO;
import com.erp.model.plm.dto.ProductDetailExcelExportDTO;
import com.erp.model.plm.dto.ProductPlanSearchDTO;
import com.erp.model.plm.dto.ProductSearchDTO;
import com.erp.model.plm.dto.ProductShowDTO;
import com.erp.model.plm.dto.ProductSkuExcelDTO;
import com.erp.model.plm.dto.ProductTaskViewDTO;
import com.erp.model.plm.dto.ProductTaskViewSearchDTO;
import com.erp.model.plm.dto.ProjectPlanTaskConditionDTO;
import com.erp.model.plm.dto.ProjectReportFormsDTO;
import com.erp.model.plm.dto.ProjectTaskTimeRecordDTO;
import com.erp.model.plm.dto.RefProductImgAttachmentDTO;
import com.erp.model.plm.dto.SearchPagingDTO;
import com.erp.model.plm.dto.SkuStdCostDetailDTO;
import com.erp.model.plm.dto.SkuStdRetailPriceDTO;
import com.erp.model.plm.dto.TaskDTO;
import com.erp.model.plm.dto.TaskPagingDTO;
import com.erp.model.plm.dto.excel.ProductPlanExcelDTO;
import com.erp.model.plm.dto.excel.TaskExportDTO;
import com.erp.model.plm.vo.BomExportExcelVO;
import com.erp.model.plm.vo.ProjectTaskTimeRecordPageVO;
import com.erp.server.plm.query.BomInfoHandler;
import com.erp.server.plm.query.CfgMoldAlertRuleQueryHandler;
import com.erp.server.plm.query.CfgMoldReturnAlertRuleQueryHandler;
import com.erp.server.plm.query.MoldInfoQueryHandler;
import com.erp.server.plm.query.MoldMonitorQueryHandler;
import com.erp.server.plm.query.MoldRefSkuQueryHandler;
import com.erp.server.plm.query.MouldInfoQueryHandler;
import com.erp.server.plm.query.OrderTrackingHandler;
import com.erp.server.plm.query.PilotApplicationQueryHandler;
import com.erp.server.plm.query.ProductChangeQueryHandler;
import com.erp.server.plm.query.ProjectReportFormsQueryHandler;
import com.erp.server.plm.query.SkuStdCostDetailQueryHandler;
import com.erp.server.plm.query.SkuStdRetailPriceQueryHandler;
import com.erp.server.plm.service.BomInfoService;
import com.erp.server.plm.service.CfgMoldAlertRuleService;
import com.erp.server.plm.service.CfgMoldReturnAlertRuleService;
import com.erp.server.plm.service.LogisticsProductService;
import com.erp.server.plm.service.MoldInfoService;
import com.erp.server.plm.service.MoldMonitorService;
import com.erp.server.plm.service.MoldRefSkuService;
import com.erp.server.plm.service.MouldInfoService;
import com.erp.server.plm.service.PilotApplicationService;
import com.erp.server.plm.service.ProductCertificateService;
import com.erp.server.plm.service.ProductChangeService;
import com.erp.server.plm.service.ProductCustomsService;
import com.erp.server.plm.service.ProductDetailService;
import com.erp.server.plm.service.ProductInfoService;
import com.erp.server.plm.service.ProductPlanService;
import com.erp.server.plm.service.ProjectPlanTaskService;
import com.erp.server.plm.service.ProjectReportFormsService;
import com.erp.server.plm.service.ProjectTaskTimeRecordService;
import com.erp.server.plm.service.ProjectTaskViewService;
import com.erp.server.plm.service.RefProductImgAttachmentService;
import com.erp.server.plm.service.SkuStdCostDetailService;
import com.erp.server.plm.service.SkuStdRetailPriceService;
import com.erp.server.plm.service.TaskService;

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
    @Resource
    private ProductInfoService productInfoService;
    @Resource
    private ProductDetailService productDetailService;
    @Resource
    private ProductCustomsService productCustomsService;
    @Resource
    private SkuStdCostDetailService skuStdCostDetailService;
    @Resource
    private MoldInfoService moldInfoService;
    @Resource
    private MoldRefSkuService moldRefSkuService;
    @Resource
    private CfgMoldReturnAlertRuleService cfgMoldReturnAlertRuleService;
    @Resource
    private CfgMoldAlertRuleService cfgMoldAlertRuleService;
    @Resource
    private MoldMonitorService moldMonitorService;
    @Resource
    private RefProductImgAttachmentService refProductImgAttachmentService;
    @Resource
    private ProductChangeService productChangeService;
    @Resource
    private SkuStdRetailPriceService skuStdRetailPriceService;

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
    @WebAdvanceQuery(handler = ProjectReportFormsQueryHandler.class)
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
    @WebAdvanceQuery(handler = OrderTrackingHandler.class)
    public PagingVO<MouldInfoDTO.OrderTrackingExportDTO> exportOrderTracking(@RequestBody PagingDTO<MouldInfoDTO.PagingParamDTO> dto) {
        return mouldInfoService.exportOrderTracking(dto);
    }

    @PostMapping("/orderTrackingDetail")
    @WebAdvanceQuery
    public PagingVO<MouldInfoDTO.OrderTrackingDetailExportDTO> exportOrderTrackingDetail(@RequestBody PagingDTO<MouldInfoDTO.OrderTrackingDetailParamDTO> dto) {
        return mouldInfoService.exportOrderTrackingDetail(dto);
    }

    @PostMapping("/productShow")
    public PagingVO<ProductShowDTO> exportProductShow(@RequestBody @Validated PagingDTO<ProductSearchDTO.ExportDTO> dto) {
        return productInfoService.exportProductShow(dto);
    }

    @PostMapping("/projectTask")
    public PagingVO<TaskExportDTO.ProductTaskExcelDTO> exportProjectTask(@RequestBody @Validated PagingDTO<ProductSearchDTO.ExportDTO> dto) {
        return productInfoService.exportProductTaskExcelDTO(dto);
    }

    @PostMapping("/exportProductDetail")
    public PagingVO<ProductDetailExcelExportDTO> exportProductDetail(@RequestBody @Validated PagingDTO<ProductSkuExcelDTO> dto) {
        return productDetailService.exportProductDetail(dto);
    }

    @PostMapping("/exportDynamicProductDetail")
    public PagingVO<DynamicExcelDTO> exportDynamicProductDetail(@RequestBody @Validated PagingDTO<ProductSkuExcelDTO> dto) {
        return productDetailService.exportDynamicProductDetail(dto);
    }

    @PostMapping("/exportProductCustoms")
    @WebAdvanceQuery
    public PagingVO<ProductCustomsDTO.ListDTO> exportProductCustoms(@RequestBody @Validated PagingDTO<ProductCustomsDTO.PagingParamDTO> dto) {
        return productCustomsService.paging(dto);
    }

    @PostMapping("/exportSkuStdCostDetail")
    @WebAdvanceQuery(handler = SkuStdCostDetailQueryHandler.class)
    public PagingVO<SkuStdCostDetailDTO.ListDTO> exportSkuStdCostDetail(@RequestBody @Validated PagingDTO<SkuStdCostDetailDTO.ExportDTO> dto){
        return skuStdCostDetailService.listExport(dto);
    }

    @PostMapping("/exportMoldInfo")
    @WebAdvanceQuery(handler = MoldInfoQueryHandler.class)
    public PagingVO<MoldInfoDTO.ListDTO> exportMoldInfo(@RequestBody @Validated PagingDTO<MoldInfoDTO.PagingParamDTO> dto){
        return moldInfoService.paging(dto);
    }
    @PostMapping("/exportMoldRefSku")
    @WebAdvanceQuery(handler = MoldRefSkuQueryHandler.class)
    public PagingVO<MoldRefSkuDTO.ListDTO> exportMoldRefSku(@RequestBody @Validated PagingDTO<MoldRefSkuDTO.PagingParamDTO> dto){
        return moldRefSkuService.paging(dto);
    }

    @PostMapping("/exportCfgMoldReturn")
    @WebAdvanceQuery(handler = CfgMoldReturnAlertRuleQueryHandler.class)
    public PagingVO<CfgMoldReturnAlertRuleDTO.ListDTO> exportCfgMoldReturn(@RequestBody @Validated PagingDTO<CfgMoldReturnAlertRuleDTO.PagingParamDTO> dto) {
        return cfgMoldReturnAlertRuleService.paging(dto);
    }

    @PostMapping("/exportCfgMoldAlert")
    @WebAdvanceQuery(handler = CfgMoldAlertRuleQueryHandler.class)
    public PagingVO<CfgMoldAlertRuleDTO.ListDTO> exportCfgMoldAlert(@RequestBody @Validated PagingDTO<CfgMoldAlertRuleDTO.PagingParamDTO> dto) {
        return cfgMoldAlertRuleService.paging(dto);
    }

    @PostMapping("/exportMoldMonitor")
    @WebAdvanceQuery(handler = MoldMonitorQueryHandler.class)
    public PagingVO<MoldMonitorDTO.ListDTO> exportMoldMonitor(@RequestBody @Validated PagingDTO<MoldMonitorDTO.PagingParamDTO> dto) {
        return moldMonitorService.paging(dto);
    }

    @PostMapping("/buildProductImagesFolderStructure")
    public String buildProductImagesFolderStructure(@RequestBody @Validated RefProductImgAttachmentDTO.BatchDownloadDTO dto) {
        return refProductImgAttachmentService.buildProductImagesFolderStructure(dto);
    }

    @PostMapping("/productChange")
    @WebAdvanceQuery(handler = ProductChangeQueryHandler.class)
    public PagingVO<ProductChangeDTO.ListDTO> productChange(@RequestBody @Validated PagingDTO<ProductChangeDTO.PagingParamDTO> dto) {
        return productChangeService.paging(dto);
    }
    
    @PostMapping("/skuStdRetailPrice")
    @WebAdvanceQuery(handler = SkuStdRetailPriceQueryHandler.class)
    public PagingVO<SkuStdRetailPriceDTO.ListDTO> skuStdRetailPrice(@RequestBody @Validated PagingDTO<SkuStdRetailPriceDTO.PagingParamDTO> dto) {
    	return skuStdRetailPriceService.paging(dto);
    }
}
