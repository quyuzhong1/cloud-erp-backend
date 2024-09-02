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
import com.erp.model.mrp.vo.*;
import com.erp.server.mrp.handler.ReplenishmentSuggestionQueryHandler;
import com.erp.server.mrp.service.ReplenishmentSuggestionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
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
    public ApiResult<PagingVO<FbaInTransitDetailVO>> fbaInTransitDetail(@RequestBody @Validated PagingDTO<String> params) {
        PagingVO<FbaInTransitDetailVO> paging = replenishmentSuggestionService.fbaInTransitDetail(params);
        return success(paging);
    }


    /**
     * 海外仓在途明细
     * @param params 明细id
     */
    @PostMapping("/overseasInTransitDetail")
    public ApiResult<PagingVO<OverseasInTransitDetailVO>> overseasInTransitDetail(@RequestBody @Validated PagingDTO<String> params) {
        PagingVO<OverseasInTransitDetailVO> paging = replenishmentSuggestionService.overseasInTransitDetail(params);
        return success(paging);
    }

    /**
     * 本地仓在途明细
     * @param params 明细id
     */
    @PostMapping("/localInTransitDetail")
    public ApiResult<PagingVO<LocalInTransitDetailVO>> localInTransitDetail(@RequestBody @Validated PagingDTO<LocalInTransitDetailDTO> params) {
        PagingVO<LocalInTransitDetailVO> paging = replenishmentSuggestionService.localInTransitDetail(params);
        return success(paging);
    }

    /**
     * 预计发货明细
     * @param params 明细id
     */
    @PostMapping("/estimatedDelivery")
    public ApiResult<PagingVO<EstimatedDeliveryVO>> estimatedDelivery(@RequestBody @Validated PagingDTO<EstimatedDeliveryDTO> params) {
        PagingVO<EstimatedDeliveryVO> paging = replenishmentSuggestionService.estimatedDelivery(params);
        return success(paging);
    }

    /**
     * 预计采购明细
     * @param params 明细id
     */
    @PostMapping("/estimatedPurchase")
    public ApiResult<PagingVO<EstimatedPurchaseVO>> estimatedPurchase(@RequestBody @Validated PagingDTO<EstimatedPurchaseDTO> params) {
        PagingVO<EstimatedPurchaseVO> paging = replenishmentSuggestionService.estimatedPurchase(params);
        return success(paging);
    }

    /**
     * 店铺库存明细
     * @param params 明细id
     */
    @PostMapping("/inventoryDetail")
    public ApiResult<InventoryDetailVO> inventoryDetail(@RequestBody @Validated InventoryTotalDTO params) {
        InventoryDetailVO inventoryDetail = replenishmentSuggestionService.inventoryDetail(params);
        return success(inventoryDetail);
    }



    public ApiResult<?> inventoryEstimationDetail(@RequestBody @Validated InventoryEstimationDTO dto) {
        return null;
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
     * 单个设置规则
     * @author will
     * @date 2024/8/29 15:56
     * @param dto
     * @return ApiResult<?>
     */
    @PostMapping("/updateRule")
    public ApiResult<?> updateRule(@RequestBody @Validated ReplenishmentSuggestionDTO.UpdateRuleDTO dto) {
        replenishmentSuggestionService.updateRule(dto);
        return success();
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
        replenishmentSuggestionService.downloadRuleTemplate(response);
        return success();
    }

    /**
     * 导入补货规则
     * @author will
     * @date 2024/8/30 16:48
     * @param excelFile
     * @param response
     * @return ApiResult<?>
     */
    @LogAction(value = LogActionEnum.IMPORT, desc = "导入补货规则")
    @PostMapping("/importRule")
    public ApiResult<?> importRule(@RequestParam(value = "excelFile") MultipartFile excelFile, HttpServletResponse response) {
        replenishmentSuggestionService.importRule(excelFile, response);
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
        replenishmentSuggestionService.downloadSalesEstimateTemplate(response);
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
    public ApiResult<?> importSalesEstimate(@RequestParam(value = "excelFile") MultipartFile excelFile, HttpServletResponse response) {
        replenishmentSuggestionService.importSalesEstimate(excelFile, response);
        return success();
    }
}
