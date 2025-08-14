package com.erp.server.wms.controller.api;


import com.common.business.annotation.DataPermission;
import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.dto.base.BaseIdsDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.UpdateStateDTO;
import com.common.business.enums.DataAttributeEnum;
import com.common.business.vo.PagingVO;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogViewService;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.erp.model.wms.dto.WarehouseLocationDTO;
import com.erp.model.wms.dto.pickingstrategy.WarehouseAreaDTO;
import com.erp.server.wms.service.WarehouseLocationService;
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
    private WarehouseLocationService warehouseLocationService;

    /**
     * 分页查询
     *
     * @param dto 分页查询条件
     */
    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            warehouseTableField = "wl.warehouse_id",
            menuCode = "wms:warehouse-area:paging"
    )
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
    @PostMapping("/update")
    public ApiResult<String> update(@RequestBody @Validated WarehouseAreaDTO.Update dto) {
        warehouseLocationService.updateArea(dto);
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
    public ApiResult<List<BatchResultDTO>> delete(@RequestBody @Validated BaseIdsDTO.IdsDTO idsDTO) {
        List<BatchResultDTO> resultDTOList =warehouseLocationService.deleteArea(idsDTO.getIds());
        return resultDTOList.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOList) : failure(resultDTOList);
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

    /**
     * 库区下拉
     * 根据仓库ID查询库区信息：id，code，name
     * @param warehouseId 仓库ID
     */
    @GetMapping("/listAreaByWarehouseId")
    public ApiResult<List<WarehouseLocationDTO.ViewDto>> listAreaByWarehouseId(@RequestParam @Validated String warehouseId){
        return success(warehouseLocationService.listAreaByWarehouseId(warehouseId));
    }

    /**
     * 查询所有库区
     */
    @GetMapping("/listAllArea")
    public ApiResult<List<WarehouseLocationDTO.CoreDTO>> listAllArea(){
        return success(warehouseLocationService.listAllArea());
    }
}
