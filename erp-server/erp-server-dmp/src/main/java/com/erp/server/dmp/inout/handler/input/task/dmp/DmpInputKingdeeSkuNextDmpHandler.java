package com.erp.server.dmp.inout.handler.input.task.dmp;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

/**
 * dmp处理下一个扩展handler，如何订单收货人信息单独一张表，使用此handler即可，因有成员变量，最终实现类由spring管理需要是多例@Scope("prototype")
 * @author Administrator
 *
 */
@Service
@Scope("prototype")
public class DmpInputKingdeeSkuNextDmpHandler extends DmpInputKingdeeNextDmpHandler{
	
	@Override
	protected void afterConvertData(Map<List<Map<String, Object>>, List<TreeMap<String, Object>>> dmpInputDataDmpRelationMaps) {
		super.afterConvertData(dmpInputDataDmpRelationMaps);
		for(Map.Entry<List<Map<String, Object>>, List<TreeMap<String, Object>>> dmpInputDataDmpRelationMap : dmpInputDataDmpRelationMaps.entrySet()) {
			List<TreeMap<String, Object>> dmpDataMaps = dmpInputDataDmpRelationMap.getValue();
			for(TreeMap<String, Object> dmpDataMap : dmpDataMaps) {
				Integer status = 3;
				Object FForbidStatusObj = dmpDataMap.get("FForbidStatus");
		        if (FForbidStatusObj != null && "C".equals(FForbidStatusObj.toString())) {
		            status = 5;
		        }
		        dmpDataMap.put("status", status);
		        
		        Object F_PRVD_AssistantFDataValueObj = dmpDataMap.get("F_PRVD_AssistantFDataValue");
		        if (F_PRVD_AssistantFDataValueObj != null) {
		        	String F_PRVD_AssistantFDataValue = F_PRVD_AssistantFDataValueObj.toString();
		        	if(StringUtils.isNotBlank(F_PRVD_AssistantFDataValue) && !"null".equals(F_PRVD_AssistantFDataValue)) {
		        		dmpDataMap.put("parentCategoryName", F_PRVD_AssistantFDataValue);
		        	}
		        }
		        
		        Object F_PRVD_Assistant1FDataValueObj = dmpDataMap.get("F_PRVD_Assistant1FDataValue");
		        if (F_PRVD_Assistant1FDataValueObj != null) {
		        	String F_PRVD_Assistant1FDataValue = F_PRVD_Assistant1FDataValueObj.toString();
		        	if(StringUtils.isNotBlank(F_PRVD_Assistant1FDataValue) && !"null".equals(F_PRVD_Assistant1FDataValue)) {
		        		dmpDataMap.put("categoryName", F_PRVD_Assistant1FDataValue);
		        	}
		        }
		        
		        Object F_SSRQObj = dmpDataMap.get("F_SSRQ");
		        if(F_SSRQObj != null) {
		        	String F_SSRQ = F_SSRQObj.toString();
		        	if (StringUtils.isNotBlank(F_SSRQ) && !"null".equals(F_SSRQ)){
		        		dmpDataMap.put("listingTime", LocalDateTime.parse(F_SSRQ));
			        }
		        }
		        
		        String itemProperty = "";
		        Object FErpClsID = dmpDataMap.get("FErpClsID");
		        if(FErpClsID != null) {
		        	switch (FErpClsID.toString()) {
		            case "1" :
		                itemProperty = "外购";
		                break;
		            case "2" :
		                itemProperty = "自制";
		                break;
		            case "3" :
		                itemProperty = "委外";
		                break;
		            case "6" :
		                itemProperty = "服务";
		                break;
		            default:
		                itemProperty = "";
		                break;
		        	}
		        }
		        dmpDataMap.put("prodcutProperty", itemProperty);
			}
		}
	}
}
