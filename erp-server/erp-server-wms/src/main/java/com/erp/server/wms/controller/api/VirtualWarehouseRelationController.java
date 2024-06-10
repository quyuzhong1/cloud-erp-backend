package com.erp.server.wms.controller.api;


import com.common.business.vo.PagingVO;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.enums.LogActionEnum;
import com.common.business.dto.base.*;
import org.springframework.web.bind.annotation.RestController;

import com.common.core.controller.BaseController;
import com.erp.server.wms.service.VirtualWarehouseRelationService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.wms.dto.VirtualWarehouseRelationDTO;

/**
 * 虚拟仓实体仓关联关系
 *
 * @author hyj
 * @since 2024-06-02
 */
@Slf4j
@RestController
@LogSystemModule("虚拟仓实体仓关联关系")
@RequestMapping("/virtualWarehouseRelation")
public class VirtualWarehouseRelationController extends BaseController {

    @Resource
    private VirtualWarehouseRelationService virtualWarehouseRelationService;

    /**
    * 新增
    * @author hyj
    * @date:  2024-06-02
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "虚拟仓实体仓关联关系新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated VirtualWarehouseRelationDTO.AddDTO dto) {
        return success(virtualWarehouseRelationService.add(dto));
    }

    /**
    * 新增
    * @author hyj
    * @date:  2024-06-02
    * @param batchAddDTO
    * @return ApiResult<String>
    */
    @PostMapping("/batchAdd")
    @LogAction(value = LogActionEnum.INSERT, desc = "虚拟仓实体仓关联关系新增")
    public ApiResult<BaseResultDTO.AddDTO> batchAdd(@RequestBody @Validated VirtualWarehouseRelationDTO.BatchAddDTO batchAddDTO) {
        return success(virtualWarehouseRelationService.batchAdd(batchAddDTO));
    }

    /**
    * 修改
    * @author hyj
    * @date:  2024-06-02
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "虚拟仓实体仓关联关系修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "wms:virtualWarehouseRelation:update",
        serviceClass = VirtualWarehouseRelationService.class,
        keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated VirtualWarehouseRelationDTO.UpdateDTO dto) {
        virtualWarehouseRelationService.update(dto);
        return success();
    }

    /**
     * 获取实体仓
     *
     * @return ApiResult
     * @author hyj
     */
    @PostMapping("/warehouse/pagingSelect")
    public ApiResult<PagingVO<VirtualWarehouseRelationDTO.SelectResultDTO>> pagingSelect(@RequestBody @Validated PagingDTO<VirtualWarehouseRelationDTO.SelectDTO> dto) {
        PagingVO<VirtualWarehouseRelationDTO.SelectResultDTO> list = virtualWarehouseRelationService.warehousePagingSelect(dto);
        return success(list);
    }
    /**
     * 获取虚拟仓
     *
     * @return ApiResult
     * @author hyj
     */
    @PostMapping("/virtualWarehouse/pagingSelect")
    public ApiResult<PagingVO<VirtualWarehouseRelationDTO.SelectResultDTO>> vmPagingSelect(@RequestBody @Validated PagingDTO<VirtualWarehouseRelationDTO.SelectDTO> dto) {
        PagingVO<VirtualWarehouseRelationDTO.SelectResultDTO> list = virtualWarehouseRelationService.vmPagingSelect(dto);
        return success(list);
    }

}
