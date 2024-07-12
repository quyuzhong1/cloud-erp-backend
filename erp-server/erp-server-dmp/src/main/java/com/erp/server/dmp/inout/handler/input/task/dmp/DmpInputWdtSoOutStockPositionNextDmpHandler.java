package com.erp.server.dmp.inout.handler.input.task.dmp;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import cn.hutool.core.collection.CollUtil;

/**
 * dmp处理下一个扩展handler，如何订单收货人信息单独一张表，使用此handler即可，因有成员变量，最终实现类由spring管理需要是多例@Scope("prototype")
 * @author Administrator
 *
 */
@Service
@Scope("prototype")
public class DmpInputWdtSoOutStockPositionNextDmpHandler extends DmpInputWdtNextDmpHandler{
	
	@Override
	protected List<Map<String, Object>> getDetailList(Map<String, Object> dmpInputMongoEntity){
		List<Map<String, Object>> detailList = super.getDetailList(dmpInputMongoEntity);
		List<Map<String, Object>> positionList = new ArrayList<Map<String,Object>>();
		if(CollUtil.isNotEmpty(detailList)) {
			for(Map<String, Object> detail : detailList) {
				positionList.addAll((List<Map<String, Object>>)detail.get("position_details_list"));
			}
		}
		return positionList;
	}
	
}
