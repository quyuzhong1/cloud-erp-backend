package com.erp.server.wms.controller.api;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.dto.base.*;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogViewService;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.erp.model.wms.dto.pickingstrategy.WarehouseAreaDTO;
import com.erp.server.wms.service.WarehouseAreaInfoService;
import com.erp.server.wms.service.impl.BatchApproveService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * <p>
 * 库区管理 前端控制器
 * </p>
 *
 * @author liaohui
 * @since 2024-05-29
 */
@RestController
@RequestMapping("/warehouse-area")
public class WarehouseAreaInfoController extends BaseController {

    @Resource
    private WarehouseAreaInfoService warehouseAreaInfoService;
    @Resource
    private BatchApproveService batchApproveService;

    /**
     * 分页查询
     *
     * @param dto 分页查询条件
     */
    @PostMapping("/paging")
    @WebAdvanceQuery
    public ApiResult<IPage<WarehouseAreaDTO.PagingView>> paging(@RequestBody @Validated PagingDTO<WarehouseAreaDTO.PagingParam> dto) {
        IPage<WarehouseAreaDTO.PagingView> pagingVO = warehouseAreaInfoService.paging(dto);
        return success(pagingVO);
    }


    /**
     * 新增
     *
     * @param dto 新增参数
     */
    @LogAction(value = LogActionEnum.INSERT, desc = "新增库区")
    @PostMapping("/add")
    public ApiResult<Void> add(@RequestBody @Validated WarehouseAreaDTO.Add dto) {
        warehouseAreaInfoService.add(dto);
        return success();
    }

    /**
     * 修改
     *
     * @param dto 编辑参数
     **/
    @LogAction(value = LogActionEnum.UPDATE, desc = "修改库区")
    @PostMapping("/update/{id}")
    public ApiResult<String> update(@RequestBody @Validated WarehouseAreaDTO.Add dto, @PathVariable(value = "id") String id) {
        warehouseAreaInfoService.update(dto, id);
        return success();
    }

    /**
     * 查询详情
     *
     * @param id id
     **/
    @LogViewService
    @GetMapping("/view")
    public ApiResult<WarehouseAreaDTO.View> view(@RequestParam("id") String id) {
        WarehouseAreaDTO.View dto = warehouseAreaInfoService.view(id);
        return success(dto);
    }

    /**
     * 提交
     *
     * @param dto dto
     **/
    @LogAction(value = LogActionEnum.SUBMIT, desc = "提交库区")
    @PostMapping("/submit")
    public ApiResult<List<BatchResultDTO>> submit(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        batchApproveService.setApproveHandler(warehouseAreaInfoService);
        List<BatchResultDTO> results = batchApproveService.submit(dto.getIds());
        return results.stream().allMatch(BatchResultDTO::getSuccess) ? success(results) : failure(results);
    }

    /**
     * 批量审核
     *
     * @param baseApproveParamDTO baseApproveParamDTO
     **/
    @LogAction(value = LogActionEnum.APPROVE, desc = "批量审核库区")
    @PostMapping("/approve")
    public ApiResult<List<BatchResultDTO>> approve(@RequestBody @Validated BaseApproveParamDTO baseApproveParamDTO) {
        batchApproveService.setApproveHandler(warehouseAreaInfoService);
        List<BatchResultDTO> results = batchApproveService.approve(baseApproveParamDTO);
        return results.stream().allMatch(BatchResultDTO::getSuccess) ? success(results) : failure(results);
    }

    /**
     * 批量反审核
     *
     * @param dto dto
     **/
    @LogAction(value = LogActionEnum.DISAPPROVE, desc = "批量反审核库区")
    @PostMapping("/disApprove")
    public ApiResult<List<BatchResultDTO>> disApprove(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        batchApproveService.setApproveHandler(warehouseAreaInfoService);
        List<BatchResultDTO> results = batchApproveService.disApprove(dto.getIds());
        return results.stream().allMatch(BatchResultDTO::getSuccess) ? success(results) : failure(results);
    }

    /**
     * 取消流程
     *
     * @param dto dto
     **/
    @LogAction(value = LogActionEnum.CANCEL, desc = "撤销库区")
    @PostMapping("/cancelProcess")
    public ApiResult<List<BatchResultDTO>> cancelProcess(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        batchApproveService.setApproveHandler(warehouseAreaInfoService);
        List<BatchResultDTO> results = batchApproveService.cancelProcess(dto.getIds());
        return results.stream().allMatch(BatchResultDTO::getSuccess) ? success(results) : failure(results);
    }

    /**
     * 批量删除
     *
     * @param idsDTO idsDTO
     **/
    @LogAction(value = LogActionEnum.DELETE, desc = "批量删除库区")
    @PostMapping("/delete")
    public ApiResult<String> delete(@RequestBody @Validated BaseIdsDTO.IdsDTO idsDTO) {
        warehouseAreaInfoService.delete(idsDTO.getIds());
        return success();
    }

    /**
     * 启用/禁用
     *
     * @param dto dto
     */
    @LogAction(value = LogActionEnum.CUSTOM_UPDATE, desc = "启用库区:id={id},状态值={state}(true=禁用,false=启用)")
    @PostMapping("/updateStatus")
    public ApiResult<String> updateStatus(@RequestBody @Validated UpdateStateDTO.BatchUpdateDTO dto) {
        warehouseAreaInfoService.updateStatus(dto);
        return success();
    }
}
