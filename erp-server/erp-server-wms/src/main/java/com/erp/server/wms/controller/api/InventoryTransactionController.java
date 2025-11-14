package com.erp.server.wms.controller.api;


import java.time.LocalDate;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.common.business.dto.base.BaseIdsDTO;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.erp.server.wms.service.InventoryTransactionService;

/**
 * 库存事务表
 *
 * @author shukai
 * @since 2025-10-13
 */
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
     @PostMapping("/overrideDbInventory")
     @LogAction(value = LogActionEnum.INSERT, desc = "db库存重算")
     public ApiResult<?> overrideDbInventory(@RequestBody BaseIdsDTO.DateDTO dto) {
    	 LocalDate date = dto.getBillDate();
    	 if(date == null) {
    		date = LocalDate.parse("2000-01-01");
    	}
        return success(inventoryTransactionService.overrideDbInventory(date, dto.getIds()));
     }
     
     /**
      * 新增
      * @author shukai
      * @date:  2025-10-13
      * @param dto
      * @return ApiResult<String>
      */
     @PostMapping("/overrideRedisInventory")
     @LogAction(value = LogActionEnum.INSERT, desc = "redis库存重算")
     public ApiResult<?> overrideRedisInventory(@RequestBody BaseIdsDTO.StatusDTO dto) {
    	 boolean isCheck = false;
    	 String status = dto.getStatus();
    	 if("check".equals(status)) {
    		 isCheck = true;
    	 }
    	 return success(inventoryTransactionService.overrideRedisInventory(dto.getIds() , isCheck));
     }
}
