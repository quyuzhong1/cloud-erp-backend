package com.erp.server.sys.controller.api;


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
import com.erp.server.sys.service.AuthUserWarehouseService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.sys.dto.AuthUserWarehouseDTO;

/**
 * 用户-仓库权限
 *
 * @author zdy
 * @since 2025-02-27
 */
@Slf4j
@RestController
@LogSystemModule("用户-仓库权限")
@RequestMapping("/authUserWarehouse")
public class AuthUserWarehouseController extends BaseController {

    @Resource
    private AuthUserWarehouseService authUserWarehouseService;

    /**
    * 新增
    * @author zdy
    * @date:  2025-02-27
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "用户-仓库权限新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated AuthUserWarehouseDTO.AddDTO dto) {
        return success(authUserWarehouseService.add(dto));
    }

    /**
    * 修改
    * @author zdy
    * @date:  2025-02-27
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "用户-仓库权限修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "sys:sysUserWarehouse:update",
        serviceClass = AuthUserWarehouseService.class,
        keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated AuthUserWarehouseDTO.UpdateDTO dto) {
        authUserWarehouseService.update(dto);
        return success();
    }



}
