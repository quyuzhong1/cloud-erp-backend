package com.erp.server.scm.controller.api;


import com.erp.server.scm.service.AssetPurchaseOrderSupplierService;
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
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.scm.dto.AssetPurchaseOrderSupplierDTO;

/**
 * 
 *
 * @author wtr
 * @since 2025-10-16
 */
@Slf4j
@RestController
@LogSystemModule("")
@RequestMapping("/assetPurchaseOrderSupplier")
public class AssetPurchaseOrderSupplierController extends BaseController {

    @Resource
    private AssetPurchaseOrderSupplierService assetPurchaseOrderSupplierService;

    /**
    * 新增
    * @author wtr
    * @date:  2025-10-16
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated AssetPurchaseOrderSupplierDTO.AddDTO dto) {
        return success(assetPurchaseOrderSupplierService.add(dto));
    }

    /**
    * 修改
    * @author wtr
    * @date:  2025-10-16
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "scm:assetPurchaseOrderSupplier:update",
        serviceClass = AssetPurchaseOrderSupplierService.class,
        keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated AssetPurchaseOrderSupplierDTO.UpdateDTO dto) {
        assetPurchaseOrderSupplierService.update(dto);
        return success();
    }



}
