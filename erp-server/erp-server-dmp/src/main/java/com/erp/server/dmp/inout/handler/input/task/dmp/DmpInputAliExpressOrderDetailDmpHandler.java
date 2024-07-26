package com.erp.server.dmp.inout.handler.input.task.dmp;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.erp.server.dmp.inout.handler.input.task.mongo.DmpInputMongoHandler;

import cn.hutool.core.collection.CollUtil;

/**
 * dmp处理金蝶明细子类任务handler，因有成员变量，最终实现类由spring管理需要是多例@Scope("prototype")
 * @author Administrator
 *
 */
@Service
@Scope("prototype")
public class DmpInputAliExpressOrderDetailDmpHandler extends DmpInputAliExpressOrderDoChildDmpHandler{
	
	@Override
	protected List<Map<String, Object>> afterDoDmpInputMongoChildEntityList(List<Map<String, Object>> dmpInputMongoChildList){
		List<Map<String, Object>> dmpInputMongoChildEntityList = new ArrayList<>();
		
		if(CollUtil.isNotEmpty(dmpInputMongoChildList)) {
			for(Map<String, Object> dmpInputMongoChild : dmpInputMongoChildList) {
				Object child_order_list = dmpInputMongoChild.get("child_order_list");
				if(child_order_list != null) {
					List<Map<String, Object>> child_order_map_list = (List<Map<String, Object>>) child_order_list;
					child_order_map_list.forEach(c -> {
						c.put("order_id", dmpInputMongoChild.get("order_id"));
						c.put(DmpInputMongoHandler.MONGO_BASE_ID, dmpInputMongoChild.get(DmpInputMongoHandler.MONGO_BASE_ID));
						c.put(DmpInputMongoHandler.MONGO_BASE_NEXTLEVELID, dmpInputMongoChild.get(DmpInputMongoHandler.MONGO_BASE_NEXTLEVELID));
					});
					dmpInputMongoChildEntityList.addAll(child_order_map_list);
				}
			}
		}
		
		return dmpInputMongoChildEntityList;
	}
	
}
