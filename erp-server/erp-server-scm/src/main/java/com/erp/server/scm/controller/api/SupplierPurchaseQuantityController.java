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
import com.erp.server.scm.service.SupplierPurchaseQuantityService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.scm.dto.SupplierPurchaseQuantityDTO;

/**
 * 供应商采购数量
 *
 * @author jack
 * @since 2025-06-18
 */
@Slf4j
@RestController
@LogSystemModule("供应商采购数量")
@RequestMapping("/supplierPurchaseQuantity")
public class SupplierPurchaseQuantityController extends BaseController {

    @Resource
    private SupplierPurchaseQuantityService supplierPurchaseQuantityService;

    /**
    * 新增
    * @author jack
    * @date:  2025-06-18
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "供应商采购数量新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated SupplierPurchaseQuantityDTO.AddDTO dto) {
        return success(supplierPurchaseQuantityService.add(dto));
    }

    /**
    * 修改
    * @author jack
    * @date:  2025-06-18
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "供应商采购数量修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "scm:supplierPurchaseQuantity:update",
        serviceClass = SupplierPurchaseQuantityService.class,
        keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated SupplierPurchaseQuantityDTO.UpdateDTO dto) {
        supplierPurchaseQuantityService.update(dto);
        return success();
    }



}
