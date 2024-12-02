package com.erp.server.dmp.inout.handler.input.task.dmp;

import java.util.List;
import java.util.Map;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

/**
 * dmp处理下一个扩展handler，如何订单收货人信息单独一张表，使用此handler即可，因有成员变量，最终实现类由spring管理需要是多例@Scope("prototype")
 * @author Administrator
 *
 */
@Service
@Scope("prototype")
public class DmpInputWdtReturnNextDmpHandler extends DmpInputWdtNextDmpHandler{
	
	@Override
	protected List<Map<String, Object>> getDetailList(Map<String, Object> dmpInputMongoEntity){
		List<Map<String, Object>> detailList = (List<Map<String, Object>>) dmpInputMongoEntity.get("detail_list");
		detailList.forEach(d -> {
			d.put("returnLogisticsNo", dmpInputMongoEntity.get("return_logistics_no"));
			d.put("returnLogisticsCompany", dmpInputMongoEntity.get("return_logistics_name"));
			d.put("reason", dmpInputMongoEntity.get("reason_name"));
			Object type = dmpInputMongoEntity.get("type");
			if(type != null) {
				d.put("returnOriginalType",type.toString());
				d.put("solutionType", type.toString().equals("2") || type.toString().equals("3") ? "return_and_refund" : "refund");
			}
		});
		return detailList;
	}
	
}
