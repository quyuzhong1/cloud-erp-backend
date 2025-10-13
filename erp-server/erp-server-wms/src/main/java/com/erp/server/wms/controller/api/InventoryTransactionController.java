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
import com.erp.server.wms.service.InventoryTransactionService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.wms.dto.InventoryTransactionDTO;

/**
 * 库存事务表
 *
 * @author shukai
 * @since 2025-10-13
 */
@Slf4j
@RestController
@LogSystemModule("库存事务表")
@RequestMapping("/inventoryTransaction")
public class InventoryTransactionController extends BaseController {

    @Resource
    private InventoryTransactionService inventoryTransactionService;

    /**
    * 新增
    * @author shukai
    * @date:  2025-10-13
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "库存事务表新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated InventoryTransactionDTO.AddDTO dto) {
        return success(inventoryTransactionService.add(dto));
    }

    /**
    * 修改
    * @author shukai
    * @date:  2025-10-13
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "库存事务表修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "wms:inventoryTransaction:update",
        serviceClass = InventoryTransactionService.class,
        keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated InventoryTransactionDTO.UpdateDTO dto) {
        inventoryTransactionService.update(dto);
        return success();
    }



}
