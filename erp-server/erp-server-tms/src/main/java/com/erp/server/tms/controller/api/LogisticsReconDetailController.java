package com.erp.server.tms.controller.api;

import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.erp.model.tms.dto.LogisticsReconDetailDTO;
import com.erp.model.tms.dto.LogisticsReconDetailSubDTO;
import com.erp.model.tms.dto.LogisticsReconRefLogisticsBillDTO;
import com.erp.server.tms.query.LogisticsReconDetailQueryHandler;
import com.erp.server.tms.service.LogisticsReconDetailService;
import com.erp.server.tms.service.LogisticsReconDetailSubService;
import com.erp.server.tms.service.LogisticsReconRefLogisticsBillService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * 物流商对账明细
 *
 * @author Will
 * @since 2026-05-29
 */
@Slf4j
@RestController
@LogSystemModule("物流商对账明细")
@RequestMapping("/tms/logisticsReconDetail")
public class LogisticsReconDetailController extends BaseController {

    @Resource
    private LogisticsReconDetailService logisticsReconDetailService;

    @Resource
    private LogisticsReconDetailSubService logisticsReconDetailSubService;

    @Resource
    private LogisticsReconRefLogisticsBillService logisticsReconRefLogisticsBillService;


    /**
     * 物流商对账明细分页列表查询
     * @author Will
     * @date: 2026/05/29
     * @param dto
     * @return ApiResult<PagingVO<LogisticsReconDetailDTO.ListDTO>>
     */
    @PostMapping("/paging")
    @WebAdvanceQuery(handler = LogisticsReconDetailQueryHandler.class)
    public ApiResult<PagingVO<LogisticsReconDetailDTO.ListDTO>> paging(
            @RequestBody @Validated PagingDTO<LogisticsReconDetailDTO.PagingParamDTO> dto) {
        return success(logisticsReconDetailService.paging(dto));
    }

    /**
     * 物流商对账明细列表数量合计（按 match_status 分组）
     * @author Will
     * @date: 2026/05/29
     * @param dto
     * @return ApiResult<List<LogisticsReconDetailDTO.TabListDTO>>
     */
    @PostMapping("/tabList")
    public ApiResult<List<LogisticsReconDetailDTO.TabListDTO>> tabList(
            @RequestBody @Validated LogisticsReconDetailDTO.TabListParamDTO dto) {
        return success(logisticsReconDetailService.tabList(dto));
    }

    /**
     * 物流商对账费用项查询（按 detail_id 集合，详情页展开/匹配结果展示）
     * @author Will
     * @date: 2026/05/29
     * @param dto
     * @return ApiResult<List<LogisticsReconDetailSubDTO.ListDTO>>
     */
    @PostMapping("/detailSubListByDetailIds")
    public ApiResult<List<LogisticsReconDetailSubDTO.ListDTO>> detailSubListByDetailIds(
            @RequestBody @Validated LogisticsReconDetailSubDTO.ListByDetailIdsDTO dto) {
        return success(logisticsReconDetailSubService.listByDetailIds(dto.getDetailIds()));
    }

    /**
     * 物流商对账明细 - 关联关系查询（按 detail_id 集合，展示已匹配的物流单/费用单号）
     * @author Will
     * @date: 2026/05/29
     * @param dto
     * @return ApiResult<List<LogisticsReconRefLogisticsBillDTO.ListDTO>>
     */
    @PostMapping("/refListByDetailIds")
    public ApiResult<List<LogisticsReconRefLogisticsBillDTO.ListDTO>> refListByDetailIds(
            @RequestBody @Validated LogisticsReconDetailDTO.ListByIdsDTO dto) {
        return success(logisticsReconRefLogisticsBillService.listByDetailIds(dto.getIds()));
    }


    /**
     * 物流商对账费用项手动匹配（指定 detail_sub ↔ 已存在的 ERP 物流单）
     * @author Will
     * @date: 2026/05/29
     * @param dto
     * @return ApiResult<BatchResultDTO>
     */
    @PostMapping("/manualMatch")
    @LogAction(value = LogActionEnum.UPDATE, desc = "物流商对账费用项手动匹配")
    public ApiResult<BatchResultDTO> manualMatch(
            @RequestBody @Validated LogisticsReconDetailDTO.ManualMatchDTO dto) {
        BatchResultDTO result = logisticsReconDetailService.manualMatch(dto);
        return result.getSuccess() ? success(result) : failure(result);
    }

    /**
     * 物流商对账费用项导入匹配（按对账费用项批量触发合并 & 匹配）
     * @author Will
     * @date: 2026/05/29
     * @param dto
     * @return ApiResult<List<BatchResultDTO>>
     */
    @PostMapping("/importMatch")
    @LogAction(value = LogActionEnum.UPDATE, desc = "物流商对账费用项导入匹配")
    public ApiResult<List<BatchResultDTO>> importMatch(
            @RequestBody @Validated LogisticsReconDetailDTO.ImportMatchDTO dto) {
        List<BatchResultDTO> results = logisticsReconDetailService.importMatch(dto);
        return results.stream().allMatch(BatchResultDTO::getSuccess) ? success(results) : failure(results);
    }

    /**
     * 物流商对账明细新增费用单（基于对账明细补建物流费用单后绑定，match_type=newBill）
     * @author Will
     * @date: 2026/05/29
     * @param dto
     * @return ApiResult<BatchResultDTO>
     */
    @PostMapping("/addLogisticsBillCost")
    @LogAction(value = LogActionEnum.INSERT, desc = "物流商对账明细新增费用单")
    public ApiResult<BatchResultDTO> addLogisticsBillCost(
            @RequestBody @Validated LogisticsReconDetailDTO.AddLogisticsBillCostDTO dto) {
        BatchResultDTO result = logisticsReconDetailService.addLogisticsBillCost(dto);
        return result.getSuccess() ? success(result) : failure(result);
    }


    /**
     * 物流商对账明细导出（异步：提交文件中心下载任务）
     * @author Will
     * @date: 2026/05/29
     * @param dto
     * @return ApiResult<Boolean>
     */
    @PostMapping("/exportExcel")
    @LogAction(value = LogActionEnum.EXPORT, desc = "物流商对账明细导出")
    public ApiResult<Boolean> exportExcel(@RequestBody @Validated LogisticsReconDetailDTO.PagingParamDTO dto) {
        logisticsReconDetailService.exportList(dto);
        return success(Boolean.TRUE);
    }
}
