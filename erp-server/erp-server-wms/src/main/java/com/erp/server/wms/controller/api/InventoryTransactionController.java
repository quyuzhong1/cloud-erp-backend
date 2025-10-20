package com.erp.server.wms.controller.api;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.redisson.RedissonMultiLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
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
    
    @Autowired
    private TransactionFlowService transactionFlowService;

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
             			int i = 0;
             			while(i < 3) {
             				try {
								inventoryTransactionService.inventoryIdToInventoryHis(id , "");
								Integer qty = 0;
								QueryWrapper<TransactionFlowEntity> queryWrapper = new QueryWrapper<>();
								queryWrapper.eq("inventory_id", id);
								queryWrapper.groupBy("inventory_id");
								queryWrapper.select(" sum(qty) qty ");
								List<TransactionFlowEntity> transactionFlowEntityList = transactionFlowService.list(queryWrapper);
								if(CollUtil.isNotEmpty(transactionFlowEntityList)) {
									qty = transactionFlowEntityList.get(0).getQty();
								}
								inventoryRedisUtil.execute(InventoryRedisOpEnum.OVERRIDE , InventoryRedisOpKeyEnum.getKey(InventoryRedisOpKeyEnum.CURRENT, id) , qty.toString());
								result.put(id, "成功");
								break;
							} catch (Exception e) {
								log.error("{}库存重算第{}次失败" , id , i , e);
								result.put(id, "失败");
							}
             				i = i + 1;
             			}
         			}catch (Exception e) {
         				result.put(id, "失败");
         				log.error("{}库存重算最终失败" , id , e);
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
