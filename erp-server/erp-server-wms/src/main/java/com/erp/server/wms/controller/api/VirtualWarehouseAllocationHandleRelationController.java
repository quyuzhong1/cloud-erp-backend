package com.erp.server.wms.controller.api;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import javax.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.anno.LogViewService;
import com.common.core.enums.LogActionEnum;
import com.common.business.dto.base.*;
import org.springframework.web.bind.annotation.RestController;

import com.common.core.controller.BaseController;
import com.erp.server.wms.service.VirtualWarehouseAllocationHandleRelationService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.wms.dto.VirtualWarehouseAllocationHandleRelationDTO;

/**
 * 分货单拆单关联关系表
 *
 * @author hyj
 * @since 2024-06-07
 */
@Slf4j
@RestController
@LogSystemModule("分货单拆单关联关系表")
@RequestMapping("/virtualWarehouseAllocationHandleRelation")
public class VirtualWarehouseAllocationHandleRelationController extends BaseController {

    @Resource
    private VirtualWarehouseAllocationHandleRelationService virtualWarehouseAllocationHandleRelationService;

    /**
    * 新增
    * @author hyj
    * @date:  2024-06-07
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "分货单拆单关联关系表新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated VirtualWarehouseAllocationHandleRelationDTO.AddDTO dto) {
        return success(virtualWarehouseAllocationHandleRelationService.add(dto));
    }

    /**
    * 修改
    * @author hyj
    * @date:  2024-06-07
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "分货单拆单关联关系表修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "wms:virtualWarehouseAllocationHandleRelation:update",
        serviceClass = VirtualWarehouseAllocationHandleRelationService.class,
        keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated VirtualWarehouseAllocationHandleRelationDTO.UpdateDTO dto) {
        virtualWarehouseAllocationHandleRelationService.update(dto);
        return success();
    }



}
