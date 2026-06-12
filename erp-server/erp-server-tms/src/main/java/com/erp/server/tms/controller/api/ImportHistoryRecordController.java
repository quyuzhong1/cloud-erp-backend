package com.erp.server.tms.controller.api;


import cn.hutool.core.util.ObjectUtil;
import com.common.business.annotation.DataPermission;
import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.dto.base.BaseDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.DataAttributeEnum;
import com.common.business.vo.PagingVO;
import com.common.core.anno.LogSystemModule;
import com.common.core.anno.LogViewService;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.tms.dto.ImportHistoryRecordDTO;
import com.erp.model.tms.entity.ImportHistoryRecordEntity;
import com.erp.server.tms.service.ImportHistoryRecordService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * 导入历史记录表
 *
 * @author will
 * @since 2026-01-19
 */
@Slf4j
@RestController
@LogSystemModule("导入历史记录表")
@RequestMapping("/importHistoryRecord")
public class ImportHistoryRecordController extends BaseController {

    @Resource
    private ImportHistoryRecordService importHistoryRecordService;


    /**
     * 列表查询
     * @author will
     * @date: 2026-01-19
     * @param dto
     * @return ApiResult<PagingVO<ImportHistoryRecordDTO.ListDTO>>
     */
    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "tms:importHistoryRecord:paging",
            tableAlias = "ihr"
    )
    @WebAdvanceQuery
    public ApiResult<PagingVO<ImportHistoryRecordDTO.ListDTO>> paging(@RequestBody @Validated PagingDTO<ImportHistoryRecordDTO.PagingParamDTO> dto) {
        return success(importHistoryRecordService.paging(dto));
    }


    /**
     * 详情
     * @author will
     * @date:  2026-01-19
     * @param id
     * @return ApiResult<ImportHistoryRecordDTO.ViewDTO>>
     */
    @GetMapping("/view")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "tms:importHistoryRecord:view",
            serviceClass = ImportHistoryRecordService.class,
            keyIdName = "id")
    @LogViewService
    public ApiResult<ImportHistoryRecordDTO.ViewDTO> view(@RequestParam("id") String id) {
        return success(importHistoryRecordService.view(id));
    }


    /**
     * 物流商模板导入统一入口。
     *
     * <p>业务规则：</p>
     * <ul>
     * <li>同一个接口承接预处理、正式导入、导入确认三种动作，由 processingType 区分。</li>
     * <li>接口只负责校验请求、识别模板并创建异步导入任务，Excel 解析和费用落库在任务回调中执行，避免大文件导入阻塞 HTTP 请求。</li>
     * <li>一个请求允许携带多个文件，每个文件独立创建任务；单个文件失败不影响同批次其他文件返回任务结果。</li>
     * </ul>
     *
     * @author will
     * @date 2026/1/20 18:43
     * @param dto 导入请求，包含文件列表、业务类型、处理类型和对账月份
     * @return ApiResult<List<BatchResultDTO>> 每个文件对应一个任务创建结果
     */
    @PostMapping(value = "/preprocessingImportExcel")
    public ApiResult<List<BatchResultDTO>> preprocessingImportExcel(@RequestBody @Validated ImportHistoryRecordDTO.ImportDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getList().size());
        for (BaseDTO.ImportDTO importDTO : dto.getList()) {
            BatchResultDTO resultDTO;
            try {
                // 每个文件独立识别模板并创建任务，避免同批次中一个文件异常导致全部文件无法进入导入队列。
                resultDTO = importHistoryRecordService.importFile(new ImportHistoryRecordDTO.ImportSyncDTO(dto,importDTO));
            }catch (Exception e){
                resultDTO = BatchResultDTO.fail(importDTO.getTaskId(), importDTO.getFileUrl(), e.getMessage());
            }
            resultDTOS.add(resultDTO);
        }
        // 前端需要明确感知同批次是否存在失败文件；只要有一个文件失败，整体响应按 failure 返回并携带明细。
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
     * 重新生成
     * @author will
     * @date 2026/1/26 16:33
     * @param dto
     * @return ApiResult<List<BatchResultDTO>>
     */
    @PostMapping(value = "/regenerateImportExcel")
    public ApiResult<List<BatchResultDTO>> regenerateImportExcel(@RequestBody @Validated ImportHistoryRecordDTO.RegenerateImportDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        for (String id : dto.getIds()) {
            BatchResultDTO result;
            try {
                result = importHistoryRecordService.regenerateImportExcel(id,dto.getProcessingType());
            } catch (Exception e) {
                log.error("重新生成失败{}", e.getMessage());
                ImportHistoryRecordEntity entity = importHistoryRecordService.getById(id);
                if (ObjectUtil.isEmpty(entity)) {
                    result = BatchResultDTO.fail(id, id, "导入历史记录不存在");
                    resultDTOS.add(result);
                    continue;
                }
                result = BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage());
            }
            resultDTOS.add(result);

        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }
}
