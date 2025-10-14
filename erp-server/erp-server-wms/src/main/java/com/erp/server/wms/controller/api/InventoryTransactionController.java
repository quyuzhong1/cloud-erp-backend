package com.erp.server.wms.controller.api;


import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.redisson.RedissonMultiLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.common.business.annotation.DataPermission;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.enums.DataAttributeEnum;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.erp.model.wms.dto.InventoryTransactionDTO;
import com.erp.model.wms.entity.InventoryEntity;
import com.erp.model.wms.entity.TransactionFlowEntity;
import com.erp.model.wms.enums.inventory.InventoryRedisOpEnum;
import com.erp.model.wms.enums.inventory.InventoryRedisOpKeyEnum;
import com.erp.server.wms.service.InventoryService;
import com.erp.server.wms.service.InventoryTransactionService;
import com.erp.server.wms.service.TransactionFlowService;
import com.erp.server.wms.utils.InventoryRedisUtil;

import cn.hutool.core.collection.CollUtil;
import lombok.extern.slf4j.Slf4j;

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
    
    @Autowired
    private InventoryService inventoryService;

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
     public ApiResult<?> override(@RequestParam(required = false) String id) {
    	Map<String , String> result = new HashMap<>();
    	List<InventoryEntity> list = inventoryService.lambdaQuery().eq(InventoryEntity::getIsDeleted, false)
    			.eq(StringUtils.isNotBlank(id), InventoryEntity::getId , id).list();
    	if(CollUtil.isNotEmpty(list)) {
    		for(InventoryEntity l : list) {
    			id = l.getId();
    			log.info("{}库存重算开始" , id);
    			RedissonMultiLock tryLock = inventoryRedisUtil.tryLock(InventoryRedisOpKeyEnum.getKey(InventoryRedisOpKeyEnum.OVERRIDE, id));
             	if(tryLock != null) {
             		try {
         				inventoryTransactionService.inventoryTransactionToInventoryHis(id , -1);
         				inventoryRedisUtil.execute(InventoryRedisOpEnum.OVERRIDE , InventoryRedisOpKeyEnum.getKey(InventoryRedisOpKeyEnum.CURRENT, id) , inventoryService.getById(id).getQty().toString());
         				result.put(id, "成功");
         			}catch (Exception e) {
         				result.put(id, "失败");
         				log.error("{}库存重算失败" , id , e);
         			} finally{
         				inventoryRedisUtil.unLock(tryLock);
         			}
             	}else {
             		result.put(id, "获取锁失败");
             	}
             	log.info("{}库存重算结束" , id);
    		}
    	}
        return success(result);
     }
}
