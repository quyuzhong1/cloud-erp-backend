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
import com.erp.server.scm.service.SupplierPlantAddrService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.scm.dto.SupplierPlantAddrDTO;

/**
 * 供应商工厂地信息
 *
 * @author will
 * @since 2025-07-21
 */
@Slf4j
@RestController
@LogSystemModule("供应商工厂地信息")
@RequestMapping("/supplierPlantAddr")
public class SupplierPlantAddrController extends BaseController {

    @Resource
    private SupplierPlantAddrService supplierPlantAddrService;

    /**
    * 新增
    * @author will
    * @date:  2025-07-21
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "供应商工厂地信息新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated SupplierPlantAddrDTO.AddDTO dto) {
        return success(supplierPlantAddrService.add(dto));
    }

    /**
    * 修改
    * @author will
    * @date:  2025-07-21
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "供应商工厂地信息修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "scm:supplierPlantAddr:update",
        serviceClass = SupplierPlantAddrService.class,
        keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated SupplierPlantAddrDTO.UpdateDTO dto) {
        supplierPlantAddrService.update(dto);
        return success();
    }



}
