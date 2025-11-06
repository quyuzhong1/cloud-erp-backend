package com.erp.server.dmp.inout.handler.output.task.api;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.fastjson.JSON;
import com.common.core.entity.BaseEntity;
import com.common.core.utils.date.LocalDateUtil;
import com.erp.model.dmp.entity.DmpCfgInputConvertEntity;
import com.erp.model.dmp.entity.DmpPushMsgEntity;
import com.erp.model.dmp.entity.DmpSoReturnDetailEntity;
import com.erp.model.dmp.entity.DmpSoReturnInfoEntity;
import com.erp.model.dmp.kingdee.KingdeeReturnOrderEntity;
import com.erp.model.dmp.kingdee.item.KingdeeReturnOrderItemEntity;
import com.erp.server.dmp.inout.dto.request.DmpOutputTaskRequest;
import com.erp.server.dmp.inout.dto.response.DmpOutputTaskResponse;
import com.erp.server.dmp.inout.handler.output.task.mq.DmpOutputRocketMQTaskHandler;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@Scope("prototype")
public class DmpOutputErpRocketMQTaskHandler extends DmpOutputRocketMQTaskHandler {

	@Override
	public Map<String, String> getPushJsonDataMap(DmpOutputTaskRequest dmpRequest, DmpOutputTaskResponse dmpResponse) {
		Map<DmpCfgInputConvertEntity, List<BaseEntity>> convertInputDmpBaseEntityListMaps = dmpRequest.getConvertInputDmpBaseEntityListMaps();
		Map<String, String> map = new HashMap<>();
		for(Map.Entry<DmpCfgInputConvertEntity, List<BaseEntity>> convertInputDmpBaseEntityListMap : convertInputDmpBaseEntityListMaps.entrySet()) {
			List<BaseEntity> value = convertInputDmpBaseEntityListMap.getValue();
			if(CollUtil.isNotEmpty(value)) {
				for(BaseEntity v : value) {
					DmpPushMsgEntity dmpPushMsgEntity = (DmpPushMsgEntity) v;
					map.put(dmpPushMsgEntity.getId(), dmpPushMsgEntity.getPushData());
				}
			}
		}
		return map;
	}

    @Override
    protected List<String> getSourceCodeKeys() {
    	return Collections.singletonList("soCode");
    }
}
