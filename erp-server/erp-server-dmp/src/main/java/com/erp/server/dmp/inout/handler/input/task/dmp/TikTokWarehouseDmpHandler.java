package com.erp.server.dmp.inout.handler.input.task.dmp;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.erp.server.dmp.service.DmpSoDetailService;
import com.erp.server.dmp.service.DmpSoInfoService;
import com.sdk.oms.temu.dto.TemuOrderDTO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
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
public class TikTokWarehouseDmpHandler extends DmpInputDbConvertDmpHandler{
	
	@Override
	protected void afterConvertData(Map<List<Map<String, Object>>, List<TreeMap<String, Object>>> dmpInputDataDmpRelationMaps) {
		for (Map.Entry<List<Map<String, Object>>, List<TreeMap<String, Object>>> dmpInputDataDmpRelationMap : dmpInputDataDmpRelationMaps.entrySet()) {
			List<TreeMap<String, Object>> dmpDataMaps = dmpInputDataDmpRelationMap.getValue();
			for (TreeMap<String, Object> dmpDataMap : dmpDataMaps) {
				dmpDataMap.put("warehouseCode",dmpDataMap.get("id"));
				dmpDataMap.put("warehouseName",dmpDataMap.get("name"));

				String json = dmpDataMap.getOrDefault("address","").toString();
				dmpDataMap.put("address","");
				if(StringUtils.isNotBlank(json)){
					JSONObject jsonObject = JSON.parseObject(json);
					String fullAddress = jsonObject.getString("full_address");
					dmpDataMap.put("address",fullAddress);
				}

				dmpDataMap.remove("id");
			}
		}
	}

}
