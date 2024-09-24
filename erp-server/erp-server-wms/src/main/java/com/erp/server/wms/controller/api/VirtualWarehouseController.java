package com.erp.server.wms.controller.api;


import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.vo.PagingVO;
import com.erp.model.oms.dto.ShopDTO;
import com.erp.model.wms.dto.WarehouseLocationMoveDTO;
import com.erp.rpc.oms.feign.ShopInfoFeign;
import com.erp.server.wms.query.MarehouseMoveInfoQueryHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Resource;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.anno.LogViewService;
import com.common.core.enums.LogActionEnum;
import com.common.business.dto.base.*;
import org.springframework.web.bind.annotation.RestController;

import com.common.core.controller.BaseController;
import com.erp.server.wms.service.VirtualWarehouseService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.wms.dto.VirtualWarehouseDTO;

import java.util.List;

/**
 * 虚拟仓
 *
 * @author hyj
 * @since 2024-06-02
 */
@Slf4j
@RestController
@LogSystemModule("虚拟仓")
@RequestMapping("/virtualWarehouse")
public class VirtualWarehouseController extends BaseController {

    @Resource
    private VirtualWarehouseService virtualWarehouseService;

    /**
     * 新增
     *
     * @param addDTO
     * @return ApiResult<String>
     * @author hyj
     * @date: 2024-06-02
     */
    @PostMapping("/add")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:virtualWarehouse:add",
            serviceClass = VirtualWarehouseService.class,
            keyIdName = "id")
    @LogAction(value = LogActionEnum.INSERT, desc = "虚拟仓新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated VirtualWarehouseDTO.AddDTO addDTO) {
        return success(virtualWarehouseService.add(addDTO));
    }

    /**
     * 修改
     *
     * @param dto
     * @return ApiResult
     * @author hyj
     * @date: 2024-06-02
     */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "虚拟仓修改")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:virtualWarehouse:update",
            serviceClass = VirtualWarehouseService.class,
            keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated VirtualWarehouseDTO.UpdateDTO dto) {
        virtualWarehouseService.update(dto);
        return success();
    }

    /**
     * 列表查询
     *
     * @param dto
     * @return ApiResult
     * @author hyj
     * @date: 2024-06-02
     */
    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "wms:virtualWarehouse:paging",
            tableAlias = "vw"
    )
    @WebAdvanceQuery
    public ApiResult<PagingVO<VirtualWarehouseDTO.ListDTO>> paging(@RequestBody @Validated PagingDTO<VirtualWarehouseDTO.PagingParamDTO> dto) {
        return success(virtualWarehouseService.paging(dto));
    }

    /**
     * 修改状态
     */
    @PostMapping("/updateState")
    @LogAction(value = LogActionEnum.UPDATE, desc = "虚拟仓修改")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:virtualWarehouse:updateState",
            serviceClass = VirtualWarehouseService.class,
            keyIdName = "id")
    public ApiResult<?> updateState(@RequestBody @Validated VirtualWarehouseDTO.UpdateStateDTO updateStateDTO) {
        return success(virtualWarehouseService.updateState(updateStateDTO));
    }

    /**
     * 详情
     */
    @GetMapping("/view")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:virtualWarehouse:view",
            serviceClass = VirtualWarehouseService.class,
            keyIdName = "id")
    @LogViewService
    public ApiResult<VirtualWarehouseDTO.ViewDTO> view(@RequestParam(value = "id") String id) {
        return success(virtualWarehouseService.view(id));
    }
    /**
     * 平台
     */
    @GetMapping("/tree")
    @LogViewService
    public ApiResult<List<VirtualWarehouseDTO.Tree>> tree(@RequestParam("key") String key,@RequestParam("id") String id) {
        return success(virtualWarehouseService.tree(key,id));
    }
    /**
     * 搜索店铺
     */
    @PostMapping("/pagingSelect")
    public ApiResult<PagingVO<ShopDTO.ListDTO>> pagingSelect(@RequestBody @Validated PagingDTO<VirtualWarehouseDTO.ShopSelectDTO> dto){
        return success(virtualWarehouseService.pagingSelect(dto));
    }

    /**
     * 搜索虚拟仓库
     */
    @PostMapping("/warehouse/pagingSelect")
    public ApiResult<PagingVO<VirtualWarehouseDTO.SelectDTO>> warehousePagingSelect(@RequestBody PagingDTO<VirtualWarehouseDTO.WarehouseSelectDTO> dto){
        return success(virtualWarehouseService.warehousePagingSelect(dto));
    }
    /**
     * 虚拟仓库列表
     */
    @PostMapping("/list")
    public ApiResult<List<VirtualWarehouseDTO.SelectDTO>> warehouseSelectList(@RequestBody PagingDTO<VirtualWarehouseDTO.WarehouseSelectDTO> dto){
        return success(virtualWarehouseService.warehouseSelectList(dto));
    }

    /**
     * 虚拟仓库列表
     */
    @PostMapping("/listByParam")
    public ApiResult<List<VirtualWarehouseDTO.SelectDTO>> listByParam(@RequestBody @Validated VirtualWarehouseDTO.SearchDTO dto){
        return success(virtualWarehouseService.listByParam(dto));
    }
}
