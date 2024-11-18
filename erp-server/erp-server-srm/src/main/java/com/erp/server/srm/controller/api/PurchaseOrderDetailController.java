package com.erp.server.srm.controller.api;


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
import com.erp.server.srm.service.PurchaseOrderDetailService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.srm.dto.PurchaseOrderDetailDTO;

/**
 * 采购订单明细表（已确认）
 *
 * @author zdy
 * @since 2024-01-27
 */
@Slf4j
@RestController
@LogSystemModule("采购订单明细表（已确认）")
@RequestMapping("/purchaseOrderDetail")
public class PurchaseOrderDetailController extends BaseController {

    @Resource
    private PurchaseOrderDetailService purchaseOrderDetailService;

    /**
    * 新增
    * @author zdy
    * @date:  2024-01-27
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "采购订单明细表（已确认）新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated PurchaseOrderDetailDTO.AddDTO dto) {
        return success(purchaseOrderDetailService.add(dto));
    }

    /**
    * 修改
    * @author zdy
    * @date:  2024-01-27
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "采购订单明细表（已确认）修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "srm:purchaseOrderDetail:update",
        serviceClass = PurchaseOrderDetailService.class,
        keyIdName = "id")
    public ApiResult<Object> update(@RequestBody @Validated PurchaseOrderDetailDTO.UpdateDTO dto) {
        purchaseOrderDetailService.update(dto);
        return success();
    }



}
