package com.erp.server.wms.schedule;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.erp.model.wms.entity.InventoryTransactionEntity;
import com.erp.server.wms.service.InventoryTransactionService;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;

import cn.hutool.core.collection.CollUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * 库存流水重算定时任务
 *
 * @Author Cloud
 * @Date 2023/8/2 14:44
 **/
@Slf4j
@Component
public class InventoryTransactionJob {

    @Resource
    private InventoryTransactionService inventoryTransactionService;
    
    @XxlJob("inventoryTransactionToInventoryHis")
    public ReturnT inventoryTransactionToInventoryHis() {
        int transactionSize = 100;
        String jobParam = XxlJobHelper.getJobParam();
        if(StringUtils.isNotBlank(jobParam)) {
        	try {
				JSONObject parseObject = JSON.parseObject(jobParam);
				transactionSize = parseObject.getIntValue("transactionSize");
			} catch (Exception e) {
				log.error("inventoryTransactionToInventoryHis转换参数失败");
			}
        }
        List<InventoryTransactionEntity> list = inventoryTransactionService.lambdaQuery().eq(InventoryTransactionEntity::getIsDeleted, false)
        	.last(" group by inventory_id limit " + transactionSize)
        	.select(InventoryTransactionEntity::getInventoryId)
        	.list();
        for(InventoryTransactionEntity l : list) {
        	String inventoryId = l.getInventoryId();
        	MDC.put("traceId", inventoryId);
			try {
				inventoryTransactionService.inventoryIdToInventoryHis(inventoryId , "");
			} catch (Exception e) {
				log.error("自动迁移redis库存失败：{}" , inventoryId);
			}finally {
				MDC.remove("traceId");
			}
        }
        return ReturnT.SUCCESS;
    }

    @XxlJob("inventoryCheckRollback")
    public ReturnT inventoryCheckRollback() {
        int timeout = 1800;
        String jobParam = XxlJobHelper.getJobParam();
        if(StringUtils.isNotBlank(jobParam)) {
        	try {
				JSONObject parseObject = JSON.parseObject(jobParam);
				timeout = parseObject.getIntValue("timeout");
			} catch (Exception e) {
				log.error("inventoryCheckRollback转换参数失败");
			}
        }
        
        inventoryTransactionService.inventoryCheckRollback(timeout);
        
        return ReturnT.SUCCESS;
    }
    
    @XxlJob("inventoryCheckSame")
    public ReturnT inventoryCheckSame() {
    	 int warnSize = 100;
         String jobParam = XxlJobHelper.getJobParam();
         if(StringUtils.isNotBlank(jobParam)) {
         	try {
 				JSONObject parseObject = JSON.parseObject(jobParam);
 				warnSize = parseObject.getIntValue("warnSize");
 			} catch (Exception e) {
 				log.error("inventoryCheckRollback转换参数失败");
 			}
         }
    	inventoryTransactionService.queryInventoryCheckSame(warnSize);
    	return ReturnT.SUCCESS;
    }
}
