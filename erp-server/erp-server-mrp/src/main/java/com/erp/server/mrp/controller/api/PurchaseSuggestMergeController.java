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
import com.erp.model.mrp.dto.DeliverySuggestDTO;
import com.erp.model.mrp.dto.PurchaseSuggestMergeDTO;
import com.erp.model.mrp.entity.PurchaseSuggestEntity;
import com.erp.model.mrp.entity.PurchaseSuggestMergeEntity;
import com.erp.server.mrp.service.PurchaseSuggestMergeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

/**
 * 建议采购(合并后)
 *
 * @author will
 * @since 2024-10-21
 */
@Slf4j
@RestController
@LogSystemModule("建议采购(合并后)")
@RequestMapping("/purchaseSuggestMerge")
public class PurchaseSuggestMergeController extends BaseController {

    @Resource
    private PurchaseSuggestMergeService purchaseSuggestMergeService;

    /**
     * 列表查询
     * @author will
     * @date 2024/10/16 10:21
     * @param dto
     * @return ApiResult<PagingVO<ListDTO>>
     */
    @PostMapping("/paging")
    @WebAdvanceQuery
    public ApiResult<PagingVO<PurchaseSuggestMergeDTO.ListDTO>> paging(@RequestBody @Validated PagingDTO<PurchaseSuggestMergeDTO.PagingParamDTO> dto) {
        PagingVO<PurchaseSuggestMergeDTO.ListDTO> pagingVO = purchaseSuggestMergeService.paging(dto);
        return success(pagingVO);
    }

    /**
     * 更新
     * @author will
     * @date 2024/10/16 14:26
     * @param updateDTO
     * @return ApiResult<?>
     */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "更新")
    public ApiResult<?> update(PurchaseSuggestMergeDTO.UpdateDTO updateDTO) {
        Boolean flag = purchaseSuggestMergeService.update(updateDTO);
        return flag ? success() : failure();
    }

    /**
     * 下载模板
     * @author will
     * @date 2024/10/16 10:51
     * @param response
     * @return ApiResult<?>
     */
    @GetMapping("/downloadTemplate")
    public ApiResult<?> downloadTemplate(HttpServletResponse response) {
        purchaseSuggestMergeService.downloadTemplate(response);
        return success();
    }

    /**
     * 锁定
     * @author will
     * @date 2024/10/16 11:22
     * @param dto
     * @return ApiResult<?>
     */
    @PostMapping("/locking")
    @LogAction(value = LogActionEnum.EXPORT, desc = "锁定")
    public ApiResult<?> locking(BaseIdsDTO.IdsDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        for (String id : dto.getIds()) {
            BatchResultDTO resultDTO;
            try {
                resultDTO = purchaseSuggestMergeService.locking(id);
            }catch (Exception e){
                log.error("采购建议计划(合并) 锁定失败",e);
                PurchaseSuggestMergeEntity entity = purchaseSuggestMergeService.getById(id);
                if (ObjectUtil.isEmpty(entity)) {
                    resultDTO = BatchResultDTO.fail(id, id, "采购建议计划(合并)不存在, 锁定失败");
                    resultDTOS.add(resultDTO);
                    continue;
                }
                resultDTO = BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage());
            }
            resultDTOS.add(resultDTO);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
     * 确认
     * @author will
     * @date 2024/10/16 11:00
     * @param dto
     * @return ApiResult<?>
     */
    @PostMapping("/confirm")
    @LogAction(value = LogActionEnum.CONFIRM, desc = "确认")
    public ApiResult<?> confirm(BaseIdsDTO.IdsDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        for (String id : dto.getIds()) {
            BatchResultDTO resultDTO;
            try {
                resultDTO = purchaseSuggestMergeService.confirm(id);
            }catch (Exception e){
                log.error("采购建议计划(合并) 确定失败",e);
                PurchaseSuggestMergeEntity entity = purchaseSuggestMergeService.getById(id);
                if (ObjectUtil.isEmpty(entity)) {
                    resultDTO = BatchResultDTO.fail(id, id, "采购建议计划(合并)不存在, 确定失败");
                    resultDTOS.add(resultDTO);
                    continue;
                }
                resultDTO = BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage());
            }
            resultDTOS.add(resultDTO);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
     * 作废
     * @author will
     * @date 2024/10/16 11:29
     * @param dto
     * @return ApiResult<?>
     */
    @PostMapping("/invalid")
    @LogAction(value = LogActionEnum.INVALID, desc = "作废")
    public ApiResult<?> invalid(BaseIdsDTO.RemarkDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        for (String id : dto.getIds()) {
            BatchResultDTO resultDTO;
            try {
                resultDTO = purchaseSuggestMergeService.invalid(id,dto.getRemark());
            }catch (Exception e){
                log.error("采购建议计划(合并) 作废失败",e);
                PurchaseSuggestMergeEntity entity = purchaseSuggestMergeService.getById(id);
                if (ObjectUtil.isEmpty(entity)) {
                    resultDTO = BatchResultDTO.fail(id, id, "采购建议计划(合并)不存在, 作废失败");
                    resultDTOS.add(resultDTO);
                    continue;
                }
                resultDTO = BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage());
            }
            resultDTOS.add(resultDTO);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
     * 更新备注
     * @author will
     * @date 2024/10/22 17:14
     * @param dto
     * @return ApiResult<?>
     */
    @PostMapping("/updateRemark")
    @LogAction(value = LogActionEnum.UPDATE, desc = "更新备注")
    public ApiResult<?> updateRemark(BaseIdsDTO.RemarkDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        for (String id : dto.getIds()) {
            BatchResultDTO resultDTO;
            try {
                resultDTO = purchaseSuggestMergeService.updateRemark(id,dto.getRemark());
            }catch (Exception e){
                log.error("采购建议计划 更新备注失败",e);
                PurchaseSuggestMergeEntity entity = purchaseSuggestMergeService.getById(id);
                if (ObjectUtil.isEmpty(entity)) {
                    resultDTO = BatchResultDTO.fail(id, id, "采购建议计划不存在, 更新备注失败");
                    resultDTOS.add(resultDTO);
                    continue;
                }
                resultDTO = BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage());
            }
            resultDTOS.add(resultDTO);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
     * 导出采购建议
     * @author will
     * @date 2024/10/16 12:26
     * @param pagingParamDTO
     * @return ApiResult
     */
    @LogAction(value = LogActionEnum.EXPORT, desc = "导出采购建议（合并）")
    @PostMapping(value = "/export")
    @WebAdvanceQuery
    public ApiResult export(@RequestBody DeliverySuggestDTO.PagingParamDTO pagingParamDTO) {
        Boolean flag = purchaseSuggestMergeService.export(pagingParamDTO);
        return flag == true ? success() : failure();
    }

}
