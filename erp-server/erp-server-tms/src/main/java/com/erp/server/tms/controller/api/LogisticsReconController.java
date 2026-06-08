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
import cn.hutool.core.util.ObjectUtil;
import com.erp.model.tms.dto.LogisticsReconDTO;
import com.erp.model.tms.entity.LogisticsReconEntity;
import com.erp.server.tms.query.LogisticsReconQueryHandler;
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
import java.util.Map;
import java.util.stream.Collectors;

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
        List<String> ids = dto.getIds();
        List<BatchResultDTO> results = new ArrayList<>(ids.size());
        List<LogisticsReconEntity> list = logisticsReconService.lambdaQuery()
                .in(LogisticsReconEntity::getId, ids)
                .list();
        Map<String, LogisticsReconEntity> idEntityMap = list.stream()
                .collect(Collectors.toMap(LogisticsReconEntity::getId, w -> w));
        // 控制层循环逐条切换，单条独立事务（缩小单次事务范围，失败不影响其它单）
        for (String id : ids) {
            BatchResultDTO result;
            try {
                result = logisticsReconService.updateCheckStatus(id, dto.getCheckStatus());
            } catch (Exception e) {
                log.error("物流商对账单校验状态切换失败 id={}", id, e);
                LogisticsReconEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    results.add(BatchResultDTO.fail(id, id, "物流商对账单不存在, 校验状态切换失败"));
                    continue;
                }
                result = BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage());
            }
            results.add(result);
        }
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
        // 统一按 id 自然排序，保证分布式多锁的获取顺序一致，避免交叉死锁
        if (dto.getIds() != null) {
            dto.getIds().sort(null);
        }
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
                results.add(logisticsReconService.confirmBill(mainId, dto.getReconciliationStatus(), dto.getConfirmTime()));
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
        List<String> ids = dto.getIds();
        List<BatchResultDTO> results = new ArrayList<>(ids.size());
        // 预查实体，删除失败时回填单号 / 区分不存在
        List<LogisticsReconEntity> list = logisticsReconService.lambdaQuery()
                .in(LogisticsReconEntity::getId, ids)
                .list();
        Map<String, LogisticsReconEntity> idEntityMap = list.stream()
                .collect(Collectors.toMap(LogisticsReconEntity::getId, w -> w));
        // 控制层循环逐条删除，单条独立事务（缩小单次事务范围，失败不影响其它单）
        for (String id : ids) {
            BatchResultDTO deleteResult;
            try {
                deleteResult = logisticsReconService.delete(id);
            } catch (Exception e) {
                log.error("物流商对账单删除失败 id={}", id, e);
                LogisticsReconEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    results.add(BatchResultDTO.fail(id, id, "物流商对账单不存在, 删除失败"));
                    continue;
                }
                deleteResult = BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage());
            }
            results.add(deleteResult);
        }
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
