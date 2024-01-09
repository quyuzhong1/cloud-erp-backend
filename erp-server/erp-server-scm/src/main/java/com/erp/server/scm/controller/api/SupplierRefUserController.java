package com.erp.server.scm.controller.api;


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
import com.erp.server.scm.service.SupplierRefUserService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.scm.dto.SupplierRefUserDTO;

/**
 * 供应商用户关系
 *
 * @author zdy
 * @since 2024-01-05
 */
@Slf4j
@RestController
@LogSystemModule("供应商用户关系")
@RequestMapping("/supplierRefUser")
public class SupplierRefUserController extends BaseController {

    @Resource
    private SupplierRefUserService supplierRefUserService;

    /**
    * 新增
    * @author zdy
    * @date:  2024-01-05
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated SupplierRefUserDTO.AddDTO dto) {
        return success(supplierRefUserService.add(dto));
    }

    /**
    * 修改
    * @author zdy
    * @date:  2024-01-05
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "scm:supplierRefUser:update",
        serviceClass = SupplierRefUserService.class,
        keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated SupplierRefUserDTO.UpdateDTO dto) {
        supplierRefUserService.update(dto);
        return success();
    }



}
