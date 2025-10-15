package io.seata.rm.datasource.xa;

import java.lang.reflect.Method;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class InventoryXAUtil {
	
	public static String name;
	
	@Value("${spring.application.name}")
    public void setName(String name) {
		InventoryXAUtil.name = name;
    }
	
	public static void xaCommit(XAXid xaXid) {
		doXa(xaXid, TransactionSynchronization.STATUS_COMMITTED);
	}
	
	public static void xaRollback(XAXid xaXid) {
		doXa(xaXid, TransactionSynchronization.STATUS_ROLLED_BACK);
	}
	
	private static void doXa(XAXid xaXid , Integer status) {
		String transactionId = "";
		try {
			if(!"erp-wms".equals(name)) {
				return;
			}
			transactionId = xaXid.getGlobalXid();
			Class<?> forName = Class.forName("com.erp.server.wms.config.InventoryTransactionSynchronizationAdapter");
			Method method = forName.getMethod("doXa", String.class , Integer.class);
			method.invoke(forName.newInstance(), transactionId , status);
		} catch (Exception e) {
			log.error("处理全局事务提交库存失败transactionId={}，status={}" , transactionId , status , e);
		}
	}
}
