package com.erp.server.scm.controller.api;


import com.erp.server.scm.service.AssetPurchaseOrderDetailService;
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
import com.erp.model.scm.dto.AssetPurchaseOrderDetailDTO;

/**
 * 
 *
 * @author wtr
 * @since 2025-10-16
 */
@Slf4j
@RestController
@LogSystemModule("")
@RequestMapping("/assetPurchaseOrderDetail")
public class AssetPurchaseOrderDetailController extends BaseController {

    @Resource
    private AssetPurchaseOrderDetailService assetPurchaseOrderDetailService;

    /**
    * 新增
    * @author wtr
    * @date:  2025-10-16
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated AssetPurchaseOrderDetailDTO.AddDTO dto) {
        return success(assetPurchaseOrderDetailService.add(dto));
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
        menuCode = "plm:assetPurchaseOrderDetail:update",
        serviceClass = AssetPurchaseOrderDetailService.class,
        keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated AssetPurchaseOrderDetailDTO.UpdateDTO dto) {
        assetPurchaseOrderDetailService.update(dto);
        return success();
    }



}
