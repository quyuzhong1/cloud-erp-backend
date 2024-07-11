package com.erp.server.dmp.inout.handler.input.task.dmp;

import com.erp.model.dmp.enums.DmpOrderReturnStatusEnum;
import com.erp.model.dmp.enums.MabangSourcePlatformEnum;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * dmp处理下一个扩展handler，如何订单收货人信息单独一张表，使用此handler即可，因有成员变量，最终实现类由spring管理需要是多例@Scope("prototype")
 * @author Administrator
 *
 */
@Service
@Scope("prototype")
public class DmpInputMabangOrderNextDmpHandler extends DmpInputMabangNextDmpHandler{
	
	@Override
	protected void afterConvertData(Map<List<Map<String, Object>>, List<TreeMap<String, Object>>> dmpInputDataDmpRelationMaps) {
		for(Map.Entry<List<Map<String, Object>>, List<TreeMap<String, Object>>> dmpInputDataDmpRelationMap : dmpInputDataDmpRelationMaps.entrySet()) {
			List<TreeMap<String, Object>> dmpDataMaps = dmpInputDataDmpRelationMap.getValue();
			for(TreeMap<String, Object> dmpDataMap : dmpDataMaps) {

				//销售平台
				Object isGift = dmpDataMap.get("isGift");
				if (isGift != null) {
					//是否是赠品 1是 2否
					Integer returnedStatus = Integer.valueOf(isGift + "");
					if (returnedStatus == 1) {
						dmpDataMap.put("isGift", Boolean.TRUE);
					} else {
						dmpDataMap.put("isGift", Boolean.FALSE);
					}
				}
			}
		}
	}
}
