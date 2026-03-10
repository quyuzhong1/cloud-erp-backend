package com.erp.server.wms.schedule;

import java.util.List;
import java.util.concurrent.ExecutorService;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.erp.model.wms.entity.VirtualInventoryTransactionEntity;
import com.erp.server.wms.service.VirtualInventoryTransactionService;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;

import lombok.extern.slf4j.Slf4j;

/**
 * 库存流水重算定时任务
 *
 * @Author Cloud
 * @Date 2023/8/2 14:44
 **/
@Slf4j
@Component
public class VirtualInventoryTransactionJob {

    @Resource
    private VirtualInventoryTransactionService virtualInventoryTransactionService;
    
    @Resource
    @Qualifier("virtualInventoryTransactionToInventoryHisPool")
    private ExecutorService virtualInventoryTransactionToInventoryHisPool;
    
    @XxlJob("virtualInventoryTransactionToInventoryHis")
    public ReturnT virtualInventoryTransactionToInventoryHis() {
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
        List<VirtualInventoryTransactionEntity> list = virtualInventoryTransactionService.lambdaQuery().eq(VirtualInventoryTransactionEntity::getIsDeleted, false)
        	.last(" group by inventory_id limit " + transactionSize)
        	.select(VirtualInventoryTransactionEntity::getInventoryId)
        	.list();
        for(VirtualInventoryTransactionEntity l : list) {
        	virtualInventoryTransactionToInventoryHisPool.execute(() -> {
        		String inventoryId = l.getInventoryId();
            	MDC.put("traceId", inventoryId);
    			try {
    				virtualInventoryTransactionService.inventoryIdToInventoryHis(inventoryId , "");
    			} catch (Exception e) {
    				log.error("自动迁移redis库存失败：{}" , inventoryId);
    			}finally {
    				MDC.remove("traceId");
    			}
        	});
        }
        return ReturnT.SUCCESS;
    }

    @XxlJob("virtualInventoryCheckRollback")
    public ReturnT virtualInventoryCheckRollback() {
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
        
        virtualInventoryTransactionService.inventoryCheckRollback(timeout);
        
        return ReturnT.SUCCESS;
    }
    
    @XxlJob("virtualInventoryCheckSame")
    public ReturnT virtualInventoryCheckSame() {
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
         virtualInventoryTransactionService.queryInventoryCheckSame(warnSize);
    	return ReturnT.SUCCESS;
    }
}
