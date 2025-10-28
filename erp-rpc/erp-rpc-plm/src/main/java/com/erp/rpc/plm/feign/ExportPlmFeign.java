package com.erp.rpc.plm.feign;

import com.common.business.config.ExportFeignConfig;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.erp.model.plm.dto.*;
import com.erp.model.plm.dto.excel.ProductPlanExcelDTO;
import com.erp.model.plm.dto.excel.TaskExportDTO;
import com.erp.model.plm.vo.BomExportExcelVO;
import com.erp.model.plm.vo.ProjectTaskTimeRecordPageVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "erp-plm",contextId = "exportPlmFeign", configuration = ExportFeignConfig.class)
public interface ExportPlmFeign {

    @PostMapping("/feign/export/exportBom")
    PagingVO<BomExportExcelVO> exportBom(@RequestBody PagingDTO<SearchPagingDTO> dto);
    @PostMapping("/feign/export/exportProductCertificate")
    PagingVO<ProductCertificateDTO.ListDTO> exportProductCertificate(@RequestBody PagingDTO<ProductCertificateDTO.ExportParamDTO> dto);
    @PostMapping("/feign/export/exportTask")
    PagingVO<TaskDTO.TaskExportDTO> exportTask(@RequestBody PagingDTO<TaskPagingDTO.ExportDTO> dto);
    @PostMapping("/feign/export/scheduleTask")
    PagingVO<TaskDTO.TaskExportDTO> exportScheduleTask(PagingDTO<ProjectPlanTaskConditionDTO> dto);
    @PostMapping("/feign/export/taskTimeRecord")
    PagingVO<ProjectTaskTimeRecordPageVO> exportTaskTimeRecord(PagingDTO<ProjectTaskTimeRecordDTO.PageRecordDto> dto);
    @PostMapping("/feign/export/logisticsProduct")
    PagingVO<LogisticsProductDTO.ExportInfoDTO> exportLogisticsProduct(PagingDTO<LogisticsProductDTO.ExportDTO> dto);
    @PostMapping("/feign/export/productPlan")
    PagingVO<ProductPlanExcelDTO> exportProductPlan(PagingDTO<ProductPlanSearchDTO> dto);
    @PostMapping("/feign/export/productPurchaseBusiness")
    PagingVO<ProjectReportFormsDTO.PagingView> exportProductPurchaseBusiness(PagingDTO<ProjectReportFormsDTO.PagingParam> dto);
    @PostMapping("/feign/export/productTaskDetail")
    PagingVO<ProjectReportFormsDTO.TaskDetail> exportProductTaskDetail(PagingDTO<ProjectReportFormsDTO.TaskDetailParam> dto);
    @PostMapping("/feign/export/productTaskView")
    PagingVO<ProductTaskViewDTO> exportProductTaskView(PagingDTO<ProductTaskViewSearchDTO> dto);
    @PostMapping("/feign/export/pilotApplication")
    PagingVO<PilotApplicationDTO.ListDTO> exportPilotApplication(@RequestBody @Validated PagingDTO<PilotApplicationDTO.PagingParamDTO> dto);
    @PostMapping("/feign/export/mouldInfo")
    PagingVO<MouldInfoDTO.MouldInfoExportDTO> exportMouldInfo(@RequestBody PagingDTO<MouldInfoDTO.PagingParamDTO> dto);
    @PostMapping("/feign/export/orderTracking")
    PagingVO<MouldInfoDTO.OrderTrackingExportDTO> exportOrderTracking(@RequestBody PagingDTO<MouldInfoDTO.PagingParamDTO> dto);
    @PostMapping("/feign/export/orderTrackingDetail")
    PagingVO<MouldInfoDTO.OrderTrackingDetailExportDTO> exportOrderTrackingDetail(@RequestBody PagingDTO<MouldInfoDTO.OrderTrackingDetailParamDTO> dto);
    @PostMapping("/feign/export/productShow")
    PagingVO<ProductShowDTO> exportProductShow(@RequestBody @Validated PagingDTO<ProductSearchDTO.ExportDTO> dto);
    @PostMapping("/feign/export/projectTask")
    PagingVO<TaskExportDTO.ProductTaskExcelDTO> exportProjectTask(@RequestBody @Validated PagingDTO<ProductSearchDTO.ExportDTO> dto);
    @PostMapping("/feign/export/exportProductDetail")
    PagingVO<ProductDetailExcelExportDTO> exportProductDetail(@RequestBody @Validated PagingDTO<ProductSkuExcelDTO> dto);
    @PostMapping("/feign/export/exportProductCustoms")
    PagingVO<ProductCustomsDTO.ListDTO> exportProductCustoms(@RequestBody @Validated PagingDTO<ProductCustomsDTO.PagingParamDTO> dto);
    @PostMapping("/feign/export/exportSkuStdCostDetail")
    PagingVO<SkuStdCostDetailDTO.ListDTO> exportSkuStdCostDetail(@RequestBody @Validated PagingDTO<SkuStdCostDetailDTO.ExportDTO> dto);
    @PostMapping("/feign/export/exportMoldInfo")
    PagingVO<MoldInfoDTO.ListDTO> exportMoldInfo(@RequestBody @Validated PagingDTO<MoldInfoDTO.PagingParamDTO> dto);
    @PostMapping("/feign/export/exportMoldRefSku")
    PagingVO<MoldRefSkuDTO.ListDTO> exportMoldRefSku(@RequestBody @Validated PagingDTO<MoldRefSkuDTO.PagingParamDTO> dto);
    @PostMapping("/feign/export/exportCfgMoldReturn")
    PagingVO<CfgMoldReturnAlertRuleDTO.ListDTO> exportCfgMoldReturn(@RequestBody @Validated PagingDTO<CfgMoldReturnAlertRuleDTO.PagingParamDTO> dto);
    @PostMapping("/feign/export/exportCfgMoldAlert")
    PagingVO<CfgMoldAlertRuleDTO.ListDTO> exportCfgMoldAlert(@RequestBody @Validated PagingDTO<CfgMoldAlertRuleDTO.PagingParamDTO> dto);
    @PostMapping("/feign/export/exportAssetNotice")
    PagingVO<AssetNoticeDTO.ListDTO> exportAssetNotice(@RequestBody @Validated PagingDTO<AssetNoticeDTO.PagingParamDTO> dto);
    @PostMapping("/feign/export/exportAssetPurchaseOrder")
    PagingVO<AssetPurchaseOrderDTO.ListDTO> exportAssetPurchaseOrder(@RequestBody @Validated PagingDTO<AssetPurchaseOrderDTO.PagingParamDTO> dto);
    @PostMapping("/feign/export/exportMoldMonitor")
    PagingVO<MoldMonitorDTO.ListDTO> exportMoldMonitor(@RequestBody @Validated PagingDTO<MoldMonitorDTO.PagingParamDTO> dto);
}
