package com.erp.server.mrp.controller.api;


import cn.hutool.core.util.ObjectUtil;
import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.dto.base.BaseIdsDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.erp.model.mrp.dto.CalcSalesInfoDimDTO;
import com.erp.model.mrp.entity.CalcSalesInfoDimEntity;
import com.erp.server.mrp.service.CalcSalesInfoDimService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

/**
 * 销量试算表
 *
 * @author liaohui
 * @since 2024-11-11
 */
@Slf4j
@RestController
@LogSystemModule("销量试算表")
@RequestMapping("/calcSalesInfoDim")
public class CalcSalesInfoDimController extends BaseController {

    @Resource
    private CalcSalesInfoDimService calcSalesInfoDimService;


    /**
     * 销量试算列表
     * @param params 参数
     */
    @PostMapping("/paging")
    @WebAdvanceQuery
    public ApiResult<PagingVO<CalcSalesInfoDimDTO.PagingView>> paging(@RequestBody @Validated PagingDTO<CalcSalesInfoDimDTO.PagingParamDTO> params) {
        PagingVO<CalcSalesInfoDimDTO.PagingView> paging = calcSalesInfoDimService.paging(params);
        return success(paging);
    }

    /**
     * 销量试算明细
     * @param id id
     */
    @GetMapping("/view")
    public ApiResult<CalcSalesInfoDimDTO.ViewDTO> view(@RequestParam String id) {
        CalcSalesInfoDimDTO.ViewDTO view = calcSalesInfoDimService.view(id);
        return success(view);
    }

    /**
     * 历史库存
     * @param dto 参数
     */
    @PostMapping("/historySales")
    public ApiResult<CalcSalesInfoDimDTO.HistorySalesVO> historySales(@RequestBody @Validated CalcSalesInfoDimDTO.HistorySalesDTO dto) {
        CalcSalesInfoDimDTO.HistorySalesVO historySales = calcSalesInfoDimService.historySales(dto);
        return success(historySales);
    }

    /**
     * 日销量预估
     * @param dto 参数
     */
    @PostMapping("/salesEstimation")
    public ApiResult<CalcSalesInfoDimDTO.SalesEstimateDTO> salesEstimation(@RequestBody @Validated CalcSalesInfoDimDTO.HistorySalesDTO dto) {
        CalcSalesInfoDimDTO.SalesEstimateDTO result = calcSalesInfoDimService.salesEstimation(dto);
        return success(result);
    }


    /**
     * 导出
     */
    @PostMapping("/exportSalesInfo")
    public ApiResult<String> exportSalesInfo(@RequestBody CalcSalesInfoDimDTO.ExportSalesInfoDTO dto) {
        calcSalesInfoDimService.exportSalesInfo(dto);
       return success();
    }

    /**
     *
     * 试算跟踪明细
     * @param params 参数
     */
    @PostMapping("/pagingDetail")
    @WebAdvanceQuery
    public ApiResult<PagingVO<CalcSalesInfoDimDTO.DetailViewDTO>> pagingDetail(@RequestBody @Validated PagingDTO<CalcSalesInfoDimDTO.ParamDTO> params) {
        PagingVO<CalcSalesInfoDimDTO.DetailViewDTO> page = calcSalesInfoDimService.pagingDetail(params);
        return success(page);
    }


    /**
     *
     * 试算跟踪模板
     * @param params 参数
     */
    @PostMapping("/pagingTemplate")
    @WebAdvanceQuery
    public ApiResult<PagingVO<CalcSalesInfoDimDTO.TemplateViewDTO>> pagingTemplate(@RequestBody @Validated PagingDTO<CalcSalesInfoDimDTO.ParamDTO> params) {
        PagingVO<CalcSalesInfoDimDTO.TemplateViewDTO> page = calcSalesInfoDimService.pagingTemplate(params);
        return success(page);
    }


    /**
     * 添加备注
     * @param dto 参数
     */
    @PostMapping("/updateRemark")
    @LogAction(value = LogActionEnum.CUSTOM_UPDATE, desc = "编辑备注")
    public ApiResult<List<BatchResultDTO>> updateRemark(@RequestBody @Validated BaseIdsDTO.BlankRemarkDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        for (String id : dto.getIds()) {
            BatchResultDTO resultDTO;
            try {
                resultDTO = calcSalesInfoDimService.updateRemark(id,dto.getRemark());
            }catch (Exception e){
                log.error("销量试算编辑备注",e);
                CalcSalesInfoDimEntity entity = calcSalesInfoDimService.getById(id);
                if (ObjectUtil.isEmpty(entity)) {
                    resultDTO = BatchResultDTO.fail(id, id, "销量试算不存在, 编辑备注失败");
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
     * 下载系统历史销量
     * @param calcSalesInfoDimId 参数
     */
    @GetMapping("/downloadHistorySales")
    public void downloadHistorySales(@RequestParam String calcSalesInfoDimId, HttpServletResponse response) {
        calcSalesInfoDimService.downloadHistorySales(calcSalesInfoDimId, response);
    }

    /**
     * 下载系统模板历史销量
     * @param cfgRuleCalcId 参数
     */
    @GetMapping("/downloadTemplateHistorySales")
    public void downloadTemplateHistorySales(@RequestParam String cfgRuleCalcId, HttpServletResponse response) {
        calcSalesInfoDimService.downloadTemplateHistorySales(cfgRuleCalcId, response);
    }

    /**
     * 试算比较
     * @param dto 参数
     */
    @PostMapping("/calcCompare")
    public ApiResult<CalcSalesInfoDimDTO.CalcCompareDTO> calcCompare(@RequestBody CalcSalesInfoDimDTO.CalcCompareParamsDTO dto) {
        CalcSalesInfoDimDTO.CalcCompareDTO result = calcSalesInfoDimService.calcCompare(dto);
        return success(result);
    }

    /**
     * 试算比较sku相关数据
     * @param dto 参数
     */
    @PostMapping("/calcCompareData")
    public ApiResult<CalcSalesInfoDimDTO.CalcCompareDataDTO> calcCompareData(@RequestBody CalcSalesInfoDimDTO.CalcCompareParamsDTO dto) {
        CalcSalesInfoDimDTO.CalcCompareDataDTO result = calcSalesInfoDimService.calcCompareData(dto);
        return success(result);
    }
}
