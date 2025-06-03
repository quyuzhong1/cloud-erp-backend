package com.erp.server.scm.controller.api;


import com.erp.model.scm.entity.PurchaseSkuOrgRefEntity;
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
import com.erp.server.scm.service.PurchaseSkuOrgRefService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.scm.dto.PurchaseSkuOrgRefDTO;

import java.util.List;

/**
 * SKU与采购组织关系
 *
 * @author zdy
 * @since 2025-05-28
 */
@Slf4j
@RestController
@LogSystemModule("SKU与采购组织关系")
@RequestMapping("/purchaseSkuOrgRef")
public class PurchaseSkuOrgRefController extends BaseController {

    @Resource
    private PurchaseSkuOrgRefService purchaseSkuOrgRefService;

    /**
    * 新增
    * @author zdy
    * @date:  2025-05-28
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "SKU与采购组织关系新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated PurchaseSkuOrgRefDTO.AddDTO dto) {
        return success(purchaseSkuOrgRefService.add(dto));
    }

    /**
    * 修改
    * @author zdy
    * @date:  2025-05-28
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "SKU与采购组织关系修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "scm:purchaseSkuOrgRef:update",
        serviceClass = PurchaseSkuOrgRefService.class,
        keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated PurchaseSkuOrgRefDTO.UpdateDTO dto) {
        purchaseSkuOrgRefService.update(dto);
        return success();
    }

    /**
     * 根据sku获取采购组织关系
     */
    @PostMapping("/getBySkuIdList")
    public ApiResult<List<PurchaseSkuOrgRefEntity>> getBySkuIdList(@RequestBody @Validated PurchaseSkuOrgRefDTO.QuerySkuDTO querySkuDTO) {
        return success(purchaseSkuOrgRefService.getBySkuIdList(querySkuDTO.getSkuIdList()));
    }
}
