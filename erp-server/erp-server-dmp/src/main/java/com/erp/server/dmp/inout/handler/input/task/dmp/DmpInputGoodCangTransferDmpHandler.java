package com.erp.server.dmp.inout.handler.input.task.dmp;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.erp.server.dmp.inout.handler.input.task.mongo.DmpInputMongoHandler;

import cn.hutool.core.collection.CollUtil;

/**
 * dmp处理下一个扩展handler，如何订单收货人信息单独一张表，使用此handler即可，因有成员变量，最终实现类由spring管理需要是多例@Scope("prototype")
 * @author Administrator
 *
 */
@Service
@Scope("prototype")
public class DmpInputGoodCangTransferDmpHandler extends DmpInputDbConvertDmpHandler{

	@Override
	protected Map<List<Map<String, Object>>, List<TreeMap<String, Object>>> convertData(
			List<Map<String, Object>> dmpInputMongoEntityList) {
		List<Map<String, Object>> newDmpInputMongoEntityList = new ArrayList<>();
		if(CollUtil.isNotEmpty(dmpInputMongoEntityList)) {
			for(Map<String, Object> dmpInputMongoEntity : dmpInputMongoEntityList) {
				this.addNewDmpInputMongoEntity(dmpInputMongoEntity, "AIR", newDmpInputMongoEntityList);
				this.addNewDmpInputMongoEntity(dmpInputMongoEntity, "EXPRESS", newDmpInputMongoEntityList);
				this.addNewDmpInputMongoEntity(dmpInputMongoEntity, "LCL", newDmpInputMongoEntityList);
			}
		}
		return super.convertData(newDmpInputMongoEntityList);
	}
	
	private void addNewDmpInputMongoEntity(Map<String, Object> dmpInputMongoEntity , String key , List<Map<String, Object>> newDmpInputMongoEntityList) {
		Object object = dmpInputMongoEntity.get(key);
		if(object != null) {
			List<Map<String , Object>> list = (List<Map<String , Object>>)object;
			for(Map<String , Object> l : list) {
				Object warehouseObj = l.get("twc_to_warehouse");
				if(warehouseObj != null) {
					List<Map<String , Object>> warehouseList = (List<Map<String , Object>>)warehouseObj;
					warehouseList.forEach(w -> {
						w.put("logisticsChannelCode", l.get("sm_code"));
						w.put("logisticsChannelName", l.get("sm_code_name"));
						w.put("authId", dmpInputMongoEntity.get("authId"));
						w.put(DmpInputMongoHandler.MONGO_BASE_ID, dmpInputMongoEntity.get(DmpInputMongoHandler.MONGO_BASE_ID));
						w.put(DmpInputMongoHandler.MONGO_BASE_NEXTLEVELID, dmpInputMongoEntity.get(DmpInputMongoHandler.MONGO_BASE_NEXTLEVELID));
					});
					newDmpInputMongoEntityList.addAll(warehouseList);
				}
			}
		}
	}
}
