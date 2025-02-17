package com.erp.server.dmp.inout.handler.input.task.dmp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.core.anno.ParamData;
import com.common.core.entity.BaseEntity;
import com.common.core.enums.PannoEnum;
import com.erp.model.dmp.entity.DmpCfgInputConvertEntity;
import com.erp.model.dmp.entity.DmpInputTaskEntity;
import com.erp.server.dmp.inout.handler.input.task.mongo.DmpInputMongoHandler;

import cn.hutool.core.collection.CollUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * dmp处理子类任务handler，因有成员变量，最终实现类由spring管理需要是多例@Scope("prototype")
 * @author Administrator
 *
 */
@Service
@Scope("prototype")
@Slf4j
public class DmpInputShopeeReceiverDoChildDmpHandler extends DmpInputDoChildDmpHandler{
	
	@Override
	protected List<Map<String, Object>> getDmpInputMongoChildEntityList(List<Map<String, Object>> dmpInputMongoEntityList , String childMongoStorageName){
		List<ParamData> paramDataList = new ArrayList<>();
		List<DmpInputTaskEntity> list = dmpInputTaskService.lambdaQuery().eq(DmpInputTaskEntity::getParentTaskId, inputTaskId).orderByAsc(DmpInputTaskEntity::getCreateTime).list();
		paramDataList.add(new ParamData(DmpInputMongoHandler.MONGO_BASE_INPUTTASKID, DmpInputMongoHandler.MONGO_BASE_INPUTTASKID, PannoEnum.EQ, list.get(0).getId()));
		List<Map<String, Object>> dmpInputMongoChildList = mongoService.findMongoData(paramDataList, childMongoStorageName);
		return this.afterDoDmpInputMongoChildEntityList(dmpInputMongoChildList);
	}
	
	protected List<Map<String, Object>> afterDoDmpInputMongoChildEntityList(List<Map<String, Object>> dmpInputMongoChildList){
		List<Map<String, Object>> resultList = new ArrayList<>();
		if(CollUtil.isNotEmpty(dmpInputMongoChildList)) {
			for(Map<String, Object> dmpInputMongoChild : dmpInputMongoChildList) {
				dmpInputMongoChild.put("buyerId", dmpInputMongoChild.get("buyer_user_id"));
				dmpInputMongoChild.put("buyerName", dmpInputMongoChild.get("buyer_username"));
				Object recipient_address = dmpInputMongoChild.get("recipient_address");
				if(recipient_address != null) {
					Map<String, Object> recipientAddress = (Map<String, Object>)recipient_address;
					dmpInputMongoChild.put("receiverName", recipientAddress.get("name"));
					dmpInputMongoChild.put("receiverTelNumber", recipientAddress.get("phone"));
					dmpInputMongoChild.put("country", recipientAddress.get("region"));
					dmpInputMongoChild.put("province", recipientAddress.get("state"));
					
					String district = "";
					Object districtObj = recipientAddress.get("district");
					if(districtObj != null) {
						district = districtObj.toString();
					}
					Object townObj = recipientAddress.get("town");
					if(townObj != null) {
						district = district + townObj.toString();
					}
					dmpInputMongoChild.put("district", district);
					dmpInputMongoChild.put("postCode", recipientAddress.get("zipcode"));
					dmpInputMongoChild.put("fullAddress", recipientAddress.get("full_address"));
				}
				resultList.add(dmpInputMongoChild);
			}
		}
		return resultList;
	}
	
	@Override
	protected void putDmpId(List<Map<String, Object>> dmpInputMongoChildEntityList){
		DmpCfgInputConvertEntity mainConvertId = this.getMainConvertId();
		String parentStorageName = mainConvertId.getStorageName();
		ServiceImpl parentServiceImpl = this.getServiceImpl(parentStorageName);
		QueryWrapper<?> wrapper = new QueryWrapper<>();
		wrapper.eq(INPUT_TASK_ID, inputTaskId);
		List<Map<String, Object>> listMaps = parentServiceImpl.listMaps(wrapper);
		Map<String, String> billNoIdMap = new HashMap<>();
		if(CollUtil.isNotEmpty(listMaps)) {
			for(Map<String, Object> listMap : listMaps) {
				billNoIdMap.put(listMap.get("third_code").toString(), listMap.get(BaseEntity.FIELD_ID).toString());
			}
		}
		for(Map<String, Object> dmpInputMongoChildEntity : dmpInputMongoChildEntityList) {
			String billNo = dmpInputMongoChildEntity.get("order_sn").toString();
			String dmpId = billNoIdMap.get(billNo);
			dmpInputMongoChildEntity.put(MAIN_ID, dmpId);
		}
	}
}
