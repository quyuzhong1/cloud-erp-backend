package com.erp.server.tms.controller.api;

import com.common.business.annotation.DataPermission;
import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.dto.base.BaseIdsDTO;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.enums.DataAttributeEnum;
import com.common.business.vo.PagingVO;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.anno.LogViewService;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.erp.model.tms.dto.LogisticsReconDTO;
import com.erp.server.tms.query.LogisticsReconQueryHandler;
import com.erp.server.tms.service.LogisticsReconDetailSubService;
import com.erp.server.tms.service.LogisticsReconService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * 物流商对账单（主表）
 *
 * @author Will
 * @since 2026-05-29
 */
@Slf4j
@RestController
@LogSystemModule("物流商对账单")
@RequestMapping("/logisticsRecon")
public class LogisticsReconController extends BaseController {

    @Resource
    private LogisticsReconService logisticsReconService;

    @Resource
    private LogisticsReconDetailSubService logisticsReconDetailSubService;


    /**
     * 物流商对账单列表数量合计
     * @author Will
     * @date: 2026/05/29
     * @param dto
     * @return ApiResult<List<LogisticsReconDTO.TabListDTO>>
     */
    @PostMapping("/tabList")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "tms:logisticsRecon:paging",
            tableAlias = "logistics_recon"
    )
    public ApiResult<List<LogisticsReconDTO.TabListDTO>> tabList(@RequestBody PermissionsDTO dto) {
        return success(logisticsReconService.tabList(dto));
    }

    /**
     * 物流商对账单分页列表查询
     * @author Will
     * @date: 2026/05/29
     * @param dto
     * @return ApiResult<PagingVO<LogisticsReconDTO.ListDTO>>
     */
    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "tms:logisticsRecon:paging",
            tableAlias = "logistics_recon"
    )
    @WebAdvanceQuery(handler = LogisticsReconQueryHandler.class)
    public ApiResult<PagingVO<LogisticsReconDTO.ListDTO>> paging(
            @RequestBody @Validated PagingDTO<LogisticsReconDTO.PagingParamDTO> dto) {
        return success(logisticsReconService.paging(dto));
    }

    /**
     * 物流商对账单查看详情
     * @author Will
     * @date: 2026/05/29
     * @param id
     * @return ApiResult<LogisticsReconDTO.ViewDTO>
     */
    @GetMapping("/view")
    @LogViewService
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "tms:logisticsRecon:view",
            serviceClass = LogisticsReconService.class,
            keyIdName = "id")
    public ApiResult<LogisticsReconDTO.ViewDTO> view(@RequestParam("id") String id) {
        return success(logisticsReconService.view(id));
    }

    /**
     * 物流商对账单预处理导入（试解析，不落库）
     * @author Will
     * @date: 2026/05/29
     * @param dto
     * @return ApiResult<List<BatchResultDTO>>
     */
    @PostMapping("/preprocessingImportExcel")
    @LogAction(value = LogActionEnum.IMPORT, desc = "物流商对账单预处理导入")
    public ApiResult<List<BatchResultDTO>> preprocessingImportExcel(
            @RequestBody @Validated LogisticsReconDTO.PreprocessingDTO dto) {
        List<BatchResultDTO> results = logisticsReconService.preprocessingImportExcel(dto);
        return results.stream().allMatch(BatchResultDTO::getSuccess) ? success(results) : failure(results);
    }

    /**
     * 物流商对账单导入 Excel（processingType=importOnly，仅落对账单 + 明细）
     * @author Will
     * @date: 2026/05/29
     * @param dto
     * @return ApiResult<BaseResultDTO.AddDTO>
     */
    @PostMapping("/importExcel")
    @LogAction(value = LogActionEnum.IMPORT, desc = "物流商对账单导入")
    public ApiResult<BaseResultDTO.AddDTO> importExcel(
            @RequestBody @Validated LogisticsReconDTO.ImportDTO dto) {
        return success(logisticsReconService.importExcel(dto));
    }


    /**
     * 物流商对账单校验状态切换（待确认 ↔ 已确认，批量）
     * @author Will
     * @date: 2026/05/29
     * @param dto
     * @return ApiResult<List<BatchResultDTO>>
     */
    @PostMapping("/batchUpdateCheckStatus")
    @LogAction(value = LogActionEnum.UPDATE, desc = "物流商对账单校验状态切换")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "tms:logisticsRecon:batchUpdateCheckStatus",
            serviceClass = LogisticsReconService.class,
            keyIdName = "ids")
    public ApiResult<List<BatchResultDTO>> batchUpdateCheckStatus(
            @RequestBody @Validated LogisticsReconDTO.UpdateCheckStatusDTO dto) {
        List<BatchResultDTO> results = logisticsReconService.batchUpdateCheckStatus(dto);
        return results.stream().allMatch(BatchResultDTO::getSuccess) ? success(results) : failure(results);
    }

    // ============================== 合并 & 匹配 ==============================

    /**
     * 物流商对账单合并并匹配（按对账单整批触发）
     * @author Will
     * @date: 2026/05/29
     * @param dto
     * @return ApiResult<List<BatchResultDTO>>
     */
    @PostMapping("/batchMatch")
    @LogAction(value = LogActionEnum.UPDATE, desc = "物流商对账单合并并匹配")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "tms:logisticsRecon:batchMatch",
            serviceClass = LogisticsReconService.class,
            keyIdName = "ids")
    public ApiResult<List<BatchResultDTO>> batchMatch(
            @RequestBody @Validated LogisticsReconDTO.BatchMatchDTO dto) {
        List<BatchResultDTO> results = logisticsReconService.batchMatch(dto);
        return results.stream().allMatch(BatchResultDTO::getSuccess) ? success(results) : failure(results);
    }

    /**
     * 物流商对账单账单确认（更新关联物流费用单对账状态）
     * @author Will
     * @date: 2026/06/01
     * @param dto
     * @return ApiResult<List<BatchResultDTO>>
     */
    @PostMapping("/batchConfirmBill")
    @LogAction(value = LogActionEnum.UPDATE, desc = "物流商对账单账单确认")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "tms:logisticsRecon:batchConfirmBill",
            serviceClass = LogisticsReconService.class,
            keyIdName = "ids")
    public ApiResult<List<BatchResultDTO>> batchConfirmBill(
            @RequestBody @Validated LogisticsReconDTO.BatchConfirmBillDTO dto) {
        List<BatchResultDTO> results = new ArrayList<>(dto.getIds().size());
        for (String mainId : dto.getIds()) {
            try {
                results.add(logisticsReconService.confirmBill(mainId, dto.getReconciliationStatus()));
            } catch (Exception e) {
                log.error("[batchConfirmBill] 失败 mainId={}", mainId, e);
                results.add(BatchResultDTO.fail(mainId, mainId, e.getMessage()));
            }
        }
        return results.stream().allMatch(BatchResultDTO::getSuccess) ? success(results) : failure(results);
    }

    // ============================== 删除 / 导出 ==============================

    /**
     * 物流商对账单批量删除（导入中 / 待确认可删，已确认不可删，级联 detail / detail_sub / ref）
     * @author Will
     * @date: 2026/05/29
     * @param dto
     * @return ApiResult<List<BatchResultDTO>>
     */
    @PostMapping("/batchDelete")
    @LogAction(value = LogActionEnum.DELETE, desc = "物流商对账单批量删除")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "tms:logisticsRecon:batchDelete",
            serviceClass = LogisticsReconService.class,
            keyIdName = "ids")
    public ApiResult<List<BatchResultDTO>> batchDelete(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<BatchResultDTO> results = logisticsReconService.batchDelete(dto.getIds());
        return results.stream().allMatch(BatchResultDTO::getSuccess) ? success(results) : failure(results);
    }

    /**
     * 物流商对账单导出（异步：提交文件中心下载任务）
     * @author Will
     * @date: 2026/05/29
     * @param dto
     * @return ApiResult<Boolean>
     */
    @PostMapping("/exportExcel")
    @LogAction(value = LogActionEnum.EXPORT, desc = "物流商对账单导出")
    public ApiResult<Boolean> exportExcel(@RequestBody @Validated LogisticsReconDTO.ExportDTO dto) {
        logisticsReconService.exportList(dto);
        return success(Boolean.TRUE);
    }
}
