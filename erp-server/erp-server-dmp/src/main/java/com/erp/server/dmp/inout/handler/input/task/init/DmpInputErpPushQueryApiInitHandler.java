package com.erp.server.dmp.inout.handler.input.task.init;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.common.business.dto.DmpSyncMqDTO;
import com.common.business.dto.DmpSyncMqDTO.SyncParamDTO;
import com.common.business.dto.DmpSyncMqDTO.SyncParamDetailDTO;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.wrapper.FeignQuery;
import com.common.core.entity.BaseEntity;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;

/**
 * dmp输入init任务基础处理器下的旺店通api获取数据方式
 * @author Administrator
 *
 */
@Service
@Scope("prototype")
public class DmpInputErpPushQueryApiInitHandler extends DmpInputErpPushApiInitHandler{
	@Override
	protected String afterQueryData(List<BaseEntity> list) {
		if(CollUtil.isEmpty(list)) {
			return super.afterQueryData(list);
		}
		Map<String, Map<String, Object>> result = new HashMap<>();
		String system = "";
		Map<String, DmpSyncMqDTO.SyncParamDTO> sourceTypeMaps = new HashMap<>();
		for(BaseEntity baseEntity : list) {
			Map<String, Object> beanToMap = BeanUtil.beanToMap(baseEntity);
			String id = beanToMap.get("id").toString();
			result.put(id, beanToMap);
			String pushData = beanToMap.get("pushData").toString();
			Boolean isQuerySync = false;
			if(StringUtils.isNotBlank(pushData) && pushData.trim().startsWith("{")) {
				isQuerySync = JSON.parseObject(pushData).getBoolean("isQuerySync");
			}
			
			if(isQuerySync != null && isQuerySync) {
				String sourceType = beanToMap.get("sourceType").toString();
				SyncParamDTO syncParamDTO = sourceTypeMaps.get(sourceType);
				if(syncParamDTO == null) {
					syncParamDTO = new DmpSyncMqDTO.SyncParamDTO();
				}
				syncParamDTO.setSourceType(SourceTypeEnum.getEnum(sourceType));
				
				List<SyncParamDetailDTO> sourceDetailList = syncParamDTO.getSourceDetailList();
				if(CollUtil.isEmpty(sourceDetailList)) {
					sourceDetailList = new ArrayList<>();
				}
				SyncParamDetailDTO syncParamDetailDTO = new SyncParamDetailDTO();
				syncParamDetailDTO.setSourceId(beanToMap.get("sourceId").toString());
				syncParamDetailDTO.setSyncOperate(beanToMap.get("syncOperate").toString());
				syncParamDetailDTO.setDataId(id);
				syncParamDetailDTO.setNewQuerySync(true);
				sourceDetailList.add(syncParamDetailDTO);
				
				syncParamDTO.setSourceDetailList(sourceDetailList);
				
				sourceTypeMaps.put(sourceType, syncParamDTO);
				
				system = beanToMap.get("sourcePlatform").toString();
			}
			
		}
		
		for(Map.Entry<String, DmpSyncMqDTO.SyncParamDTO> sourceTypeMap : sourceTypeMaps.entrySet()) {
			Map<String, Map<String, Object>> invoke = FeignQuery.invoke(Map.class , "com.erp.server."+ system +".service.impl.SyncTaskServiceImpl", "newFindDataSendSyncTask", Arrays.asList(sourceTypeMap.getValue()));
			if(invoke != null) {
				for(Map.Entry<String, Map<String, Object>> i : invoke.entrySet()) {
					Map<String, Object> map = result.get(i.getKey());
					if(map != null) {
						map.put("pushData", JSON.toJSONString(i.getValue()));
					}
				}
			}
		}
		return JSON.toJSONString(result.values());
	}
}
