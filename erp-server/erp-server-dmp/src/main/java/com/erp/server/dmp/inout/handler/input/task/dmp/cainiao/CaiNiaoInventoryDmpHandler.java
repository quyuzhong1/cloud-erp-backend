package com.erp.server.dmp.inout.handler.input.task.dmp.cainiao;

import com.erp.server.dmp.inout.handler.input.task.dmp.DmpInputDbConvertDmpHandler;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

/**
 * dmp处理下一个扩展handler，如何订单收货人信息单独一张表，使用此handler即可，因有成员变量，最终实现类由spring管理需要是多例@Scope("prototype")
 *
 * @author Administrator
 */
@Service
@Scope("prototype")
public class CaiNiaoInventoryDmpHandler extends DmpInputDbConvertDmpHandler {

    @Override
    protected void afterConvertData(Map<List<Map<String, Object>>, List<TreeMap<String, Object>>> dmpInputDataDmpRelationMaps) {
        super.afterConvertData(dmpInputDataDmpRelationMaps);
        for (Map.Entry<List<Map<String, Object>>, List<TreeMap<String, Object>>> dmpInputDataDmpRelationMap : dmpInputDataDmpRelationMaps.entrySet()) {
            List<TreeMap<String, Object>> dmpDataMaps = dmpInputDataDmpRelationMap.getValue();
            for (TreeMap<String, Object> dmpDataMap : dmpDataMaps) {
                Integer inventoryType = dmpDataMap.get("inventoryType") != null ? Integer.parseInt(dmpDataMap.get("inventoryType").toString()) : null;
				if (inventoryType != null) {;
					dmpDataMap.put("platformWarehouseCode",dmpDataMap.get("warehouseWode"));
					dmpDataMap.put("productSku",dmpDataMap.get("itemCode"));
					if(inventoryType.equals(1)){
						Integer lockQuantity = dmpDataMap.get("lockQuantity") != null ? Integer.parseInt(dmpDataMap.get("lockQuantity").toString()) : 0;
						Integer quantity = dmpDataMap.get("quantity") != null ? Integer.parseInt(dmpDataMap.get("quantity").toString()) : 0;
						dmpDataMap.put("sellable",quantity - lockQuantity);
						//不可售
						dmpDataMap.put("unsellable",0);
					}else{
						Integer quantity = dmpDataMap.get("quantity") != null ? Integer.parseInt(dmpDataMap.get("quantity").toString()) : 0;
						dmpDataMap.put("unsellable",quantity);
					}
				}
            }
        }
    }
}
