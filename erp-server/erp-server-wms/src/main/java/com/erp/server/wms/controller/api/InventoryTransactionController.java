package com.erp.server.wms.controller.api;


import lombok.extern.slf4j.Slf4j;

import org.redisson.RedissonMultiLock;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import javax.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.anno.LogViewService;
import com.common.core.enums.LogActionEnum;
import com.common.core.exception.ServiceException;
import com.common.business.dto.base.*;
import com.common.core.controller.BaseController;
import com.erp.server.wms.service.InventoryTransactionService;
import com.erp.server.wms.service.impl.InventoryTransactionServiceImpl;
import com.erp.server.wms.utils.InventoryRedisUtil;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.common.business.utils.ApplicationContextUtils;
import com.erp.model.wms.dto.InventoryTransactionDTO;
import com.erp.model.wms.entity.InventoryTransactionEntity;
import com.erp.model.wms.enums.inventory.InventoryRedisOpEnum;
import com.erp.model.wms.enums.inventory.InventoryRedisOpKeyEnum;

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
    
    @Autowired
    private InventoryRedisUtil inventoryRedisUtil;

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


    /**
     * 新增
     * @author shukai
     * @date:  2025-10-13
     * @param dto
     * @return ApiResult<String>
     */
     @PostMapping("/override")
     @LogAction(value = LogActionEnum.INSERT, desc = "redis库存重算")
     public ApiResult<?> override(@RequestParam(required = false) String inventoryId) {
    	RedissonMultiLock tryLock = inventoryRedisUtil.tryLock(InventoryRedisOpKeyEnum.getKey(InventoryRedisOpKeyEnum.OVERRIDE, inventoryId));
     	if(tryLock != null) {
     		try {
 				List<InventoryTransactionEntity> inventoryTransactionEntityList = inventoryTransactionService.lambdaQuery().eq(InventoryTransactionEntity::getInventoryId, inventoryId)
 						.orderByAsc(InventoryTransactionEntity::getCreateTime).list();
 				inventoryTransactionService.inventoryTransactionToInventoryHis(inventoryTransactionEntityList);
 				inventoryRedisUtil.execute(InventoryRedisOpEnum.OVERRIDE_INVENTORY , InventoryRedisOpKeyEnum.getKey(InventoryRedisOpKeyEnum.CURRENT, inventoryId));
 			}catch (Exception e) {
 				log.error("库存重算失败" , e);
 				throw e;
 			} finally{
 				inventoryRedisUtil.unLock(tryLock);
 			}
     	}else {
     		failure("库存重算获取锁失败，请求超时");
     	}
         return success();
     }
}
