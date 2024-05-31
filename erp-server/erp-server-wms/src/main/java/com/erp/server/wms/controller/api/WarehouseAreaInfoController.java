package com.erp.server.wms.controller.api;


import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.dto.base.BaseIdsDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.UpdateStateDTO;
import com.common.business.vo.PagingVO;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogViewService;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.erp.model.wms.dto.pickingstrategy.WarehouseAreaDTO;
import com.erp.server.wms.service.WarehouseLocationService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

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
    private WarehouseLocationService warehouseLocationService;

    /**
     * 分页查询
     *
     * @param dto 分页查询条件
     */
    @PostMapping("/paging")
    @WebAdvanceQuery
    public ApiResult<PagingVO<WarehouseAreaDTO.PagingView>> paging(@RequestBody @Validated PagingDTO<WarehouseAreaDTO.PagingParam> dto) {
        PagingVO<WarehouseAreaDTO.PagingView> pagingVO = warehouseLocationService.areaPaging(dto);
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
        warehouseLocationService.addArea(dto);
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
        warehouseLocationService.updateArea(dto, id);
        return success();
    }

    /**
     * 查询详情
     *
     * @param id id
     **/
    @LogViewService
    @GetMapping("/view/{id}")
    public ApiResult<WarehouseAreaDTO.View> view(@PathVariable("id") String id) {
        WarehouseAreaDTO.View dto = warehouseLocationService.viewArea(id);
        return success(dto);
    }

    /**
     * 批量删除
     *
     * @param idsDTO idsDTO
     **/
    @LogAction(value = LogActionEnum.DELETE, desc = "批量删除库区")
    @PostMapping("/delete")
    public ApiResult<String> delete(@RequestBody @Validated BaseIdsDTO.IdsDTO idsDTO) {
        warehouseLocationService.deleteArea(idsDTO.getIds());
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
        warehouseLocationService.updateStatusArea(dto);
        return success();
    }
}
