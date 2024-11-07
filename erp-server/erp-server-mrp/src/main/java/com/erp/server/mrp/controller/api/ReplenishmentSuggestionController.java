package com.erp.server.mrp.controller.api;


import cn.hutool.core.util.ObjectUtil;
import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.dto.base.BaseIdsDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.common.core.anno.LogAction;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.erp.model.mrp.dto.*;
import com.erp.model.mrp.entity.ReplenishmentSuggestionEntity;
import com.erp.model.mrp.enums.CfgRulePlatformTypeEnum;
import com.erp.model.mrp.vo.*;
import com.erp.server.mrp.calculation.service.BasicReplenishmentDataService;
import com.erp.server.mrp.calculation.service.DataArchivingService;
import com.erp.server.mrp.handler.ReplenishmentSuggestionQueryHandler;
import com.erp.server.mrp.service.ReplenishmentSuggestionImportService;
import com.erp.server.mrp.service.ReplenishmentSuggestionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * 补货建议主表 前端控制器
 * @author liaohui
 * @since 2024-08-28
 */
@Slf4j
@RestController
@RequestMapping("/replenishment")
public class ReplenishmentSuggestionController extends BaseController {

    @Resource
    private ReplenishmentSuggestionService replenishmentSuggestionService;

    @Resource
    private ReplenishmentSuggestionImportService replenishmentSuggestionImportService;

    @Resource
    private BasicReplenishmentDataService basicReplenishmentDataService;
    @Resource
    private DataArchivingService dataArchivingService;


    /**
     * 补货建议列表
     * @param params 参数
     */
    @PostMapping("/paging")
    @WebAdvanceQuery(handler = ReplenishmentSuggestionQueryHandler.class)
    public ApiResult<PagingVO<ReplenishmentSuggestionVO.PagingView>> paging(@RequestBody @Validated PagingDTO<ReplenishmentSuggestionDTO.PagingParamDTO> params) {
        PagingVO<ReplenishmentSuggestionVO.PagingView> paging = replenishmentSuggestionService.paging(params);
        return success(paging);
    }

    /**
     * 补货建议明细
     * @param detailId 明细id
     */
    @GetMapping("/view")
    public ApiResult<ReplenishmentSuggestionVO.View> view(@RequestParam String detailId) {
        ReplenishmentSuggestionVO.View view = replenishmentSuggestionService.view(detailId);
        return success(view);
    }

    /**
     * fba在途明细明细
     * @param params 明细id
     */
    @PostMapping("/fbaInTransitDetail")
    public ApiResult<PagingVO<FbaInTransitDetailVO>> fbaInTransitDetail(@RequestBody @Validated PagingDTO<ReplenishmentSuggestionDTO.DetailParamDTO> params) {
        PagingVO<FbaInTransitDetailVO> paging = replenishmentSuggestionService.fbaInTransitDetail(params);
        return success(paging);
    }


    /**
     * 海外仓在途明细
     * @param params 明细id
     */
    @PostMapping("/overseasInTransitDetail")
    public ApiResult<PagingVO<OverseasInTransitDetailVO>> overseasInTransitDetail(@RequestBody @Validated PagingDTO<ReplenishmentSuggestionDTO.DetailParamDTO> params) {
        PagingVO<OverseasInTransitDetailVO> paging = replenishmentSuggestionService.overseasInTransitDetail(params);
        return success(paging);
    }

    /**
     * 本地仓在途明细
     * @param params 明细id
     */
    @PostMapping("/localInTransitDetail")
    public ApiResult<PagingVO<LocalInTransitDetailVO>> localInTransitDetail(@RequestBody @Validated PagingDTO<ReplenishmentSuggestionDTO.DetailParamDTO> params) {
        PagingVO<LocalInTransitDetailVO> paging = replenishmentSuggestionService.localInTransitDetail(params);
        return success(paging);
    }

    /**
     * 预计发货明细
     * @param params 明细id
     */
    @PostMapping("/estimatedDelivery")
    public ApiResult<PagingVO<EstimatedDeliveryVO>> estimatedDelivery(@RequestBody @Validated PagingDTO<ReplenishmentSuggestionDTO.DetailParamDTO> params) {
        PagingVO<EstimatedDeliveryVO> paging = replenishmentSuggestionService.estimatedDelivery(params);
        return success(paging);
    }

    /**
     * 预计采购明细
     * @param params 明细id
     */
    @PostMapping("/estimatedPurchase")
    public ApiResult<PagingVO<EstimatedPurchaseVO>> estimatedPurchase(@RequestBody @Validated PagingDTO<ReplenishmentSuggestionDTO.DetailParamDTO> params) {
        PagingVO<EstimatedPurchaseVO> paging = replenishmentSuggestionService.estimatedPurchase(params);
        return success(paging);
    }

    /**
     * 店铺库存明细
     * @param params 明细id
     */
    @PostMapping("/inventoryDetail")
    public ApiResult<PagingVO<InventoryDetailVO>> inventoryDetail(@RequestBody @Validated PagingDTO<InventoryTotalDTO> params) {
        PagingVO<InventoryDetailVO> inventoryDetail = replenishmentSuggestionService.inventoryDetail(params);
        return success(inventoryDetail);
    }


    /**
     * 库存预测
     * @param dto 参数
     */
    @PostMapping("/inventoryEstimation")
    public ApiResult<EstimationResultDTO> inventoryEstimation(@RequestBody @Validated InventoryEstimationDTO dto) {
        EstimationResultDTO result = replenishmentSuggestionService.inventoryEstimation(dto);
        return success(result);
    }

    /**
     * 数量统计
     * @param params 明细id
     */
    @PostMapping("/inventoryTotal")
    public ApiResult<Integer> inventoryTotal(@RequestBody @Validated InventoryTotalDTO params) {
        Integer inventoryTotal = replenishmentSuggestionService.inventoryTotal(params);
        return success(inventoryTotal);
    }

    /**
     * 库存预测明细
     * @param dto 参数
     */
    @PostMapping("/inventoryEstimationDetail")
    public ApiResult<EstimationDetailResultDTO> inventoryEstimationDetail(@RequestBody @Validated InventoryEstimationDetailDTO dto) {
        EstimationDetailResultDTO result = replenishmentSuggestionService.inventoryEstimationDetail(dto);
        return success(result);
    }

    /**
     * 销量分析
     * @param dto 参数
     */
    @PostMapping("/salesAnalysis")
    public ApiResult<SalesAnalysisVO> salesAnalysis(@RequestBody @Validated SalesAnalysisDTO dto) {
        SalesAnalysisVO salesAnalysis = replenishmentSuggestionService.salesAnalysis(dto);
        return success(salesAnalysis);
    }

    /**
     * 历史库存
     * @param dto 参数
     */
    @PostMapping("/historyInventory")
    public ApiResult<HistoryInventoryVO> historyInventory(@RequestBody @Validated HistoryInventoryDTO dto) {
        HistoryInventoryVO historyInventory = replenishmentSuggestionService.historyInventory(dto);
        return success(historyInventory);
    }

    /**
     * 断货报告
     */
    @GetMapping("/outOfStockReport")
    public ApiResult<List<RptOutOfStockVO>> outOfStockReport(@RequestParam String detailId) {
        List<RptOutOfStockVO> rptOutOfStockVOS = replenishmentSuggestionService.outOfStockReport(detailId);
        return success(rptOutOfStockVOS);
    }

    /**
     * 断货报告数量
     */
    @GetMapping("/outOfStockReportTotal")
    public ApiResult<BigDecimal> outOfStockReportTotal(@RequestParam String detailId) {
        BigDecimal outOfStockReportTotal = replenishmentSuggestionService.outOfStockReportTotal(detailId);
        return success(outOfStockReportTotal);
    }
    /**
     * 暂不补货
     * @author will
     * @date 2024/8/29 14:57
     * @param dto
     * @return ApiResult<?>
     */
    @PostMapping("/notRestockingReplenishment")
    @LogAction(value = LogActionEnum.CUSTOM_UPDATE, desc = "补货建议暂不补货")
    public ApiResult<?> notRestockingReplenishment(@RequestBody @Validated ReplenishmentSuggestionDTO.ReplenishmentDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        for (String id : dto.getIds()) {
            BatchResultDTO resultDTO;
            try {
                resultDTO = replenishmentSuggestionService.notRestockingReplenishment(id,dto.getReplenishmentRemark());
            }catch (Exception e){
                log.error("暂不补货失败",e);
                ReplenishmentSuggestionEntity entity = replenishmentSuggestionService.getById(id);
                if (ObjectUtil.isEmpty(entity)) {
                    resultDTO = BatchResultDTO.fail(id, id, "补货建议不存在, 暂不补货失败");
                    resultDTOS.add(resultDTO);
                    continue;
                }
                resultDTO = BatchResultDTO.fail(entity.getId(), entity.getSkuNo(), e.getMessage());
            }
            resultDTOS.add(resultDTO);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
     * 恢复补货
     * @author will
     * @date 2024/8/29 14:57
     * @param dto
     * @return ApiResult<?>
     */
    @PostMapping("/restoreReplenishment")
    @LogAction(value = LogActionEnum.CUSTOM_UPDATE, desc = "补货建议恢复补货")
    public ApiResult<?> restoreReplenishment(@RequestBody @Validated ReplenishmentSuggestionDTO.ReplenishmentDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        for (String id : dto.getIds()) {
            BatchResultDTO resultDTO;
            try {
                resultDTO = replenishmentSuggestionService.restoreReplenishment(id,dto.getReplenishmentRemark());
            }catch (Exception e){
                log.error("恢复补货失败",e);
                ReplenishmentSuggestionEntity entity = replenishmentSuggestionService.getById(id);
                if (ObjectUtil.isEmpty(entity)) {
                    resultDTO = BatchResultDTO.fail(id, id, "补货建议不存在, 恢复补货失败");
                    resultDTOS.add(resultDTO);
                    continue;
                }
                resultDTO = BatchResultDTO.fail(entity.getId(), entity.getSkuNo(), e.getMessage());
            }
            resultDTOS.add(resultDTO);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }


    /**
     * 批量设置规则
     * @author will
     * @date 2024/8/29 15:51
     * @param dto
     * @return ApiResult<?>
     */
    @PostMapping("/batchUpdateRule")
    @LogAction(value = LogActionEnum.CUSTOM_UPDATE, desc = "补货建议批量设置规则")
    public ApiResult<?> batchUpdateRule(@RequestBody @Validated ReplenishmentSuggestionDTO.BatchUpdateRuleDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        for (String id : dto.getIds()) {
            BatchResultDTO resultDTO;
            try {
                resultDTO = replenishmentSuggestionService.batchUpdateRule(id, dto.getStockUpUpdateDTO(),dto.getSalesQtyUpdateDTO());
            } catch (Exception e) {
                log.error("批量设置规则", e);
                ReplenishmentSuggestionEntity entity = replenishmentSuggestionService.getById(id);
                if (ObjectUtil.isEmpty(entity)) {
                    resultDTO = BatchResultDTO.fail(id, id, "补货建议不存在, 批量设置规则失败");
                    resultDTOS.add(resultDTO);
                    continue;
                }
                resultDTO = BatchResultDTO.fail(entity.getId(), entity.getSkuNo(), e.getMessage());
            }
            resultDTOS.add(resultDTO);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
     * 恢复规则设置
     * @author will
     * @date 2024/8/29 15:48
     * @param dto
     * @return ApiResult<?>
     */
    @PostMapping("/restoreRule")
    @LogAction(value = LogActionEnum.CUSTOM_UPDATE, desc = "补货建议恢复规则设置")
    public ApiResult<?> restoreRule(@RequestBody @Validated ReplenishmentSuggestionDTO.RestoreRuleDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        for (String id : dto.getIds()) {
            BatchResultDTO resultDTO;
            try {
                resultDTO = replenishmentSuggestionService.restoreRule(id,dto.getRuleTypeList());
            }catch (Exception e){
                log.error("恢复规则设置",e);
                ReplenishmentSuggestionEntity entity = replenishmentSuggestionService.getById(id);
                if (ObjectUtil.isEmpty(entity)) {
                    resultDTO = BatchResultDTO.fail(id, id, "补货建议不存在, 恢复规则设置失败");
                    resultDTOS.add(resultDTO);
                    continue;
                }
                resultDTO = BatchResultDTO.fail(entity.getId(), entity.getSkuNo(), e.getMessage());
            }
            resultDTOS.add(resultDTO);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
     * 关注
     * @author will
     * @date 2024/8/30 10:53
     * @param dto
     * @return ApiResult<?>
     */
    @PostMapping("/favorite")
    @LogAction(value = LogActionEnum.CUSTOM_UPDATE, desc = "关注补货建议")
    public ApiResult<?> favorite(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        for (String id : dto.getIds()) {
            BatchResultDTO resultDTO;
            try {
                resultDTO = replenishmentSuggestionService.favorite(id);
            }catch (Exception e){
                log.error("关注补货建议",e);
                ReplenishmentSuggestionEntity entity = replenishmentSuggestionService.getById(id);
                if (ObjectUtil.isEmpty(entity)) {
                    resultDTO = BatchResultDTO.fail(id, id, "补货建议不存在, 关注补货建议失败");
                    resultDTOS.add(resultDTO);
                    continue;
                }
                resultDTO = BatchResultDTO.fail(entity.getId(), entity.getSkuNo(), e.getMessage());
            }
            resultDTOS.add(resultDTO);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
     * 取消关注
     * @author will
     * @date 2024/8/30 11:08
     * @param dto
     * @return ApiResult<?>
     */
    @PostMapping("/cancelFavorite")
    @LogAction(value = LogActionEnum.CUSTOM_UPDATE, desc = "补货建议取消关注")
    public ApiResult<?> cancelFavorite(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        for (String id : dto.getIds()) {
            BatchResultDTO resultDTO;
            try {
                resultDTO = replenishmentSuggestionService.cancelFavorite(id);
            }catch (Exception e){
                log.error("补货建议取消关注",e);
                ReplenishmentSuggestionEntity entity = replenishmentSuggestionService.getById(id);
                if (ObjectUtil.isEmpty(entity)) {
                    resultDTO = BatchResultDTO.fail(id, id, "补货建议不存在, 补货建议取消关注失败");
                    resultDTOS.add(resultDTO);
                    continue;
                }
                resultDTO = BatchResultDTO.fail(entity.getId(), entity.getSkuNo(), e.getMessage());
            }
            resultDTOS.add(resultDTO);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
     * 根据id查询标签
     * @author will
     * @date 2024/9/4 16:42
     * @param id
     * @return ApiResult<List<ViewDTO>>
     */
    @GetMapping("/listLabelInfoById")
    public ApiResult<List<LabelInfoDTO.ViewDTO>> listLabelInfoById(@RequestParam("id") String id) {
        List<LabelInfoDTO.ViewDTO> list = replenishmentSuggestionService.listLabelInfoById(id);
        return success(list);
    }

    /**
     * 根据id查询标签id集合
     * @author will
     * @date 2024/9/5 11:16
     * @param id
     * @return ApiResult<List<String>>
     */
    @GetMapping("/listLabelIdById")
    public ApiResult<List<String>> listLabelIdById(@RequestParam("id") String id) {
        List<String> list = replenishmentSuggestionService.listLabelIdById(id);
        return success(list);
    }

    /**
     * 编辑标签（单个）
     * @author will
     * @date 2024/8/30 14:50
     * @param updateLabelDTO
     * @return ApiResult<?>
     */
    @PostMapping("/updateLabel")
    @LogAction(value = LogActionEnum.CUSTOM_UPDATE, desc = "补货建议添加标签")
    public ApiResult<?> updateLabel(@RequestBody @Validated ReplenishmentSuggestionDTO.UpdateLabelDTO updateLabelDTO) {
        replenishmentSuggestionService.updateLabel(updateLabelDTO);
        return success();
    }

    /**
     * 批量添加标签
     * @author will
     * @date 2024/8/30 15:55
     * @param dto
     * @return ApiResult<?>
     */
    @PostMapping("/batchAddLabel")
    @LogAction(value = LogActionEnum.CUSTOM_UPDATE, desc = "补货建议批量添加标签")
    public ApiResult<?> batchAddLabel(@RequestBody @Validated ReplenishmentSuggestionDTO.BatchSaveLabelDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        for (String id : dto.getIds()) {
            BatchResultDTO resultDTO;
            try {
                resultDTO = replenishmentSuggestionService.batchAddLabel(id,dto.getLabelIdList());
            }catch (Exception e){
                log.error("补货建议添加标签",e);
                ReplenishmentSuggestionEntity entity = replenishmentSuggestionService.getById(id);
                if (ObjectUtil.isEmpty(entity)) {
                    resultDTO = BatchResultDTO.fail(id, id, "补货建议不存在, 补货建议批量添加标签失败");
                    resultDTOS.add(resultDTO);
                    continue;
                }
                resultDTO = BatchResultDTO.fail(entity.getId(), entity.getSkuNo(), e.getMessage());
            }
            resultDTOS.add(resultDTO);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
     * 取消标签
     * @author will
     * @date 2024/8/30 15:56
     * @param dto
     * @return ApiResult<?>
     */
    @PostMapping("/cancelLabel")
    @LogAction(value = LogActionEnum.CUSTOM_UPDATE, desc = "补货建议取消标签")
    public ApiResult<?> cancelLabel(@RequestBody @Validated ReplenishmentSuggestionDTO.BatchSaveLabelDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        for (String id : dto.getIds()) {
            BatchResultDTO resultDTO;
            try {
                resultDTO = replenishmentSuggestionService.cancelLabel(id,dto.getLabelIdList());
            }catch (Exception e){
                log.error("补货建议取消标签",e);
                ReplenishmentSuggestionEntity entity = replenishmentSuggestionService.getById(id);
                if (ObjectUtil.isEmpty(entity)) {
                    resultDTO = BatchResultDTO.fail(id, id, "补货建议不存在, 补货建议取消标签失败");
                    resultDTOS.add(resultDTO);
                    continue;
                }
                resultDTO = BatchResultDTO.fail(entity.getId(), entity.getSkuNo(), e.getMessage());
            }
            resultDTOS.add(resultDTO);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
     * 补货规则导入模板
     * @author will
     * @date 2024/8/30 16:40
     * @param response
     * @return ApiResult<?>
     */
    @GetMapping("/downloadRuleTemplate")
    public ApiResult<?> downloadRuleTemplate(HttpServletResponse response) {
        replenishmentSuggestionImportService.downloadRuleTemplate(response);
        return success();
    }

    /**
     * 导入补货规则
     * @author will
     * @date 2024/8/30 16:48
     * @param excelFile
     * @param platformType
     * @param response
     * @return ApiResult<?>
     */
    @LogAction(value = LogActionEnum.IMPORT, desc = "导入补货规则")
    @PostMapping("/importRule")
    public ApiResult<?> importRule(@RequestParam(value = "excelFile") MultipartFile excelFile,@RequestParam(value = "platformType") String platformType, HttpServletResponse response) {
        replenishmentSuggestionImportService.importRule(excelFile,platformType, response);
        return success();
    }


    /**
     * 运营预估月销导入模板
     * @author will
     * @date 2024/8/30 16:40
     * @param response
     * @return ApiResult<?>
     */
    @GetMapping("/downloadSalesEstimateTemplate")
    public ApiResult<?> downloadSalesEstimateTemplate(HttpServletResponse response) {
        replenishmentSuggestionImportService.downloadSalesEstimateTemplate(response);
        return success();
    }

    /**
     * 导入运营预估月销
     * @author will
     * @date 2024/8/30 16:49
     * @param excelFile
     * @param response
     * @return ApiResult<?>
     */
    @LogAction(value = LogActionEnum.IMPORT, desc = "导入运营预估月销")
    @PostMapping("/importSalesEstimate")
    public ApiResult<?> importSalesEstimate(@RequestParam(value = "excelFile") MultipartFile excelFile,@RequestParam(value = "platformType") String platformType, HttpServletResponse response) {
        replenishmentSuggestionImportService.importSalesEstimate(excelFile,platformType, response);
        return success();
    }

    /**
     * 编辑备注
     * @author will
     * @date 2024/9/5 11:34
     * @param dto
     * @return ApiResult<?>
     */
    @PostMapping("/updateRemark")
    @LogAction(value = LogActionEnum.CUSTOM_UPDATE, desc = "编辑备注")
    public ApiResult<?> updateRemark(@RequestBody @Validated BaseIdsDTO.BlankRemarkDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        for (String id : dto.getIds()) {
            BatchResultDTO resultDTO;
            try {
                resultDTO = replenishmentSuggestionService.updateRemark(id,dto.getRemark());
            }catch (Exception e){
                log.error("补货建议编辑备注",e);
                ReplenishmentSuggestionEntity entity = replenishmentSuggestionService.getById(id);
                if (ObjectUtil.isEmpty(entity)) {
                    resultDTO = BatchResultDTO.fail(id, id, "补货建议不存在, 补货建议编辑备注失败");
                    resultDTOS.add(resultDTO);
                    continue;
                }
                resultDTO = BatchResultDTO.fail(entity.getId(), entity.getSkuNo(), e.getMessage());
            }
            resultDTOS.add(resultDTO);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
     * 导出补货规则
     * @author will
     * @date 2024/9/5 19:25
     * @param pagingParamDTO
     * @return ApiResult
     */
    @LogAction(value = LogActionEnum.EXPORT, desc = "导出补货规则")
    @PostMapping(value = "/exportReplenishmentRule")
    @WebAdvanceQuery(handler = ReplenishmentSuggestionQueryHandler.class)
    public ApiResult exportExcel(@RequestBody ReplenishmentSuggestionDTO.PagingParamDTO pagingParamDTO) {
        Boolean flag = replenishmentSuggestionService.exportReplenishmentRule(pagingParamDTO);
        return flag == true ? success() : failure();
    }

    /**
     * 导出历史销量
     * @author will
     * @date 2024/9/5 19:36
     * @param pagingParamDTO
     * @return ApiResult
     */
    @LogAction(value = LogActionEnum.EXPORT, desc = "导出历史销量")
    @PostMapping(value = "/exportHistorySalesQty")
    @WebAdvanceQuery(handler = ReplenishmentSuggestionQueryHandler.class)
    public ApiResult exportHistorySalesQty(@RequestBody ReplenishmentSuggestionDTO.PagingParamDTO pagingParamDTO) {
        Boolean flag = replenishmentSuggestionService.exportHistorySalesQty(pagingParamDTO);
        return flag == true ? success() : failure();
    }

    /**
     * 导出补货计划_采购建议
     * @author will
     * @date 2024/9/5 19:40
     * @param pagingParamDTO
     * @return ApiResult
     */
    @LogAction(value = LogActionEnum.EXPORT, desc = "导出补货计划_采购建议")
    @PostMapping(value = "/exportPurchaseSuggestion")
    @WebAdvanceQuery(handler = ReplenishmentSuggestionQueryHandler.class)
    public ApiResult exportPurchaseSuggestion(@RequestBody ReplenishmentSuggestionDTO.PagingParamDTO pagingParamDTO) {
        Boolean flag = replenishmentSuggestionService.exportPurchaseSuggestion(pagingParamDTO);
        return flag == true ? success() : failure();
    }


    /**
     * 导出补货计划_发货建议
     * @author will
     * @date 2024/10/12 14:46
     * @param pagingParamDTO
     * @return ApiResult
     */
    @LogAction(value = LogActionEnum.EXPORT, desc = "导出补货计划_建议发货")
    @PostMapping(value = "/exportDeliverySuggest")
    @WebAdvanceQuery(handler = ReplenishmentSuggestionQueryHandler.class)
    public ApiResult exportDeliverySuggest(@RequestBody ReplenishmentSuggestionDTO.PagingParamDTO pagingParamDTO) {
        Boolean flag = replenishmentSuggestionService.exportDeliverySuggest(pagingParamDTO);
        return flag == true ? success() : failure();
    }

    /**
     * 归档，全量更新数据
     */
    @GetMapping("/dataArchiving")
    public void dataArchiving(@RequestParam(required = false) LocalDate calculationDate,@RequestParam(required = false) Integer cleanDay) {
        dataArchivingService.dataArchiving(calculationDate, cleanDay);
    }

    /**
     * 更新数据
     * @param dto id集合
     */
    @PostMapping("/renewData")
    public ApiResult<List<BatchResultDTO>> renewData(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        for (String id : dto.getIds()) {
            BatchResultDTO resultDTO;
            try {
                resultDTO = replenishmentSuggestionService.renewData(id);
            }catch (Exception e){
                log.error("补货建议更新",e);
                ReplenishmentSuggestionEntity entity = replenishmentSuggestionService.getById(id);
                if (ObjectUtil.isEmpty(entity)) {
                    resultDTO = BatchResultDTO.fail(id, id, "补货建议不存在, 补货建议更新失败");
                    resultDTOS.add(resultDTO);
                    continue;
                }
                resultDTO = BatchResultDTO.fail(entity.getId(), entity.getSkuNo(), e.getMessage());
            }
            resultDTOS.add(resultDTO);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
     * 模拟销量分析
     * @param dto 参数
     */
    @PostMapping("/mockSalesAnalysis")
    public ApiResult<SalesAnalysisVO> mockSalesAnalysis(@RequestBody @Validated MockSalesAnalysisDTO dto) {
        SalesAnalysisVO salesAnalysis = replenishmentSuggestionService.mockSalesAnalysis(dto);
        return success(salesAnalysis);
    }

    /**
     * 判断需要补货的数据
     * @param platformType 平台
     */
    @GetMapping("/isReplenishment")
    public ApiResult<String> isReplenishment(String platformType) {
        List<ReplenishmentSuggestionEntity> suggestions = replenishmentSuggestionService.listCalculationData(platformType);
        basicReplenishmentDataService.isReplenishment(suggestions,platformType,LocalDate.now());
        return success();
    }

    /**
     * 判断需要补货的数据
     * @param platformType 平台
     */
    @GetMapping("/calculationDetail")
    public ApiResult<String> calculationDetail(String platformType) {
        basicReplenishmentDataService.calculationDetail(platformType, LocalDate.now());
        return success();
    }


    /**
     * 清洗历史库存
     * @param platformType 平台
     */
    @GetMapping("/cleanHistoryInventory")
    public ApiResult<String> cleanHistoryInventory(@RequestParam String platformType, @RequestParam Integer cleanDay) {
        List<ReplenishmentSuggestionEntity> suggestionList = replenishmentSuggestionService.listByPlatform(platformType);
        basicReplenishmentDataService.cleanHistoryInventory(LocalDate.now(), suggestionList, CfgRulePlatformTypeEnum.getEnum(platformType), cleanDay);
        return success();
    }

    /**
     * 销售订单历史
     * @param platformType 平台
     */
    @GetMapping("/cleanHistorySalesByOrder")
    public ApiResult<String> cleanHistorySalesByOrder(@RequestParam String platformType, @RequestParam Integer cleanDay) {
        List<ReplenishmentSuggestionEntity> suggestionList = replenishmentSuggestionService.listByPlatform(platformType);
        basicReplenishmentDataService.cleanHistorySalesByOrder(LocalDate.now(), suggestionList, CfgRulePlatformTypeEnum.getEnum(platformType), cleanDay);
        return success();
    }


    /**
     * 销售订单历史
     * @param platformType 平台
     */
    @GetMapping("/cleanHistorySalesByOutStock")
    public ApiResult<String> cleanHistorySalesByOutStock(@RequestParam String platformType, @RequestParam Integer cleanDay) {
        List<ReplenishmentSuggestionEntity> suggestionList = replenishmentSuggestionService.listByPlatform(platformType);
        basicReplenishmentDataService.cleanHistorySalesByOutStock(LocalDate.now(), suggestionList, CfgRulePlatformTypeEnum.getEnum(platformType), cleanDay);
        return success();
    }
}
