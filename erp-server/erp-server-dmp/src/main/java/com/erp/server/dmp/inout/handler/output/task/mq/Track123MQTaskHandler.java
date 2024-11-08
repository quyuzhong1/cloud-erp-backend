package com.erp.server.dmp.inout.handler.output.task.mq;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.common.business.dto.PlatformOrderDTO;
import com.common.business.dto.PlatformProductDTO;
import com.common.business.enums.PlatformDictEnum;
import com.common.core.entity.BaseEntity;
import com.erp.model.dmp.entity.DmpCfgInputConvertEntity;
import com.erp.model.dmp.entity.DmpLogisticsTrackEntity;
import com.erp.model.dmp.entity.DmpProductInfoEntity;
import com.erp.model.dmp.entity.DmpSkuInfoEntity;
import com.erp.model.tms.enums.LogisticTrackStatusEnum;
import com.erp.server.dmp.inout.dto.request.DmpOutputTaskRequest;
import com.erp.server.dmp.inout.dto.response.DmpOutputTaskResponse;
import com.sdk.tms.track123.dto.PlatformTrack123OceanTrackDTO;
import com.sdk.tms.track123.dto.PlatformTrackDTO;
import com.sdk.tms.track123.dto.PlatformTrackDetail;
import com.sdk.tms.track123.model.response.OceanContainerInfo;
import com.sdk.tms.track123.model.response.OceanTrackInfo;
import com.sdk.tms.track123.model.response.OceanTrackingDetail;
import com.sdk.tms.track123.model.response.Rejected;
import io.seata.common.util.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Scope("prototype")
public class Track123MQTaskHandler extends DmpOutputRocketMQTaskHandler{

	@Override
	public Map<String, String> getPushJsonDataMap(DmpOutputTaskRequest dmpRequest, DmpOutputTaskResponse dmpResponse) {
		Map<DmpCfgInputConvertEntity, List<BaseEntity>> convertInputDmpBaseEntityListMaps = dmpRequest.getConvertInputDmpBaseEntityListMaps();
		Map<String , DmpLogisticsTrackEntity> dmpLogisticsTrackEntityMap = new HashMap<>();
		for(Map.Entry<DmpCfgInputConvertEntity, List<BaseEntity>> convertInputDmpBaseEntityListMap : convertInputDmpBaseEntityListMaps.entrySet()) {
			List<BaseEntity> value = convertInputDmpBaseEntityListMap.getValue();
			if(CollUtil.isNotEmpty(value)) {
				String storageName = convertInputDmpBaseEntityListMap.getKey().getStorageName();
				if("dmp_logistics_track".equals(storageName)) {
					for(BaseEntity v : value) {
						DmpLogisticsTrackEntity dmpLogisticsTrackEntity = (DmpLogisticsTrackEntity) v;
						dmpLogisticsTrackEntityMap.put(dmpLogisticsTrackEntity.getId(), dmpLogisticsTrackEntity);
					}
				}
			}
		}
		
		Map<DmpCfgInputConvertEntity, List<BaseEntity>> changeConvertInputDmpBaseEntityListMaps = dmpRequest.getChangeConvertInputDmpBaseEntityListMaps();
		Set<String> changeIds = new HashSet<>(); 
		for(Map.Entry<DmpCfgInputConvertEntity, List<BaseEntity>> changeConvertInputDmpBaseEntityListMap : changeConvertInputDmpBaseEntityListMaps.entrySet()) {
			List<BaseEntity> value = changeConvertInputDmpBaseEntityListMap.getValue();
			if(CollUtil.isNotEmpty(value)) {
				String storageName = changeConvertInputDmpBaseEntityListMap.getKey().getStorageName();
				if("dmp_logistics_track".equals(storageName)) {
					for(BaseEntity v : value) {
						changeIds.add(v.getId());
					}
				}
			}
		}
		Map<String, String> map = new HashMap<>();
		String cfgOutputId = dmpResponse.getDmpCfgOutputEntity().getId();

		List<DmpLogisticsTrackEntity> trackEntityList = new ArrayList<>(dmpLogisticsTrackEntityMap.values());
		Map<String, List<DmpLogisticsTrackEntity>> trackMap = trackEntityList.stream().collect(Collectors.groupingBy(DmpLogisticsTrackEntity::getTrackNo));
		for (Map.Entry<String, List<DmpLogisticsTrackEntity>> stringListEntry : trackMap.entrySet()) {
			PlatformTrackDTO platformTrackDTO = this.convert(stringListEntry.getValue(), cfgOutputId);
			if (platformTrackDTO != null) {
				map.put(stringListEntry.getValue().get(0).getId(), JSON.toJSONString(platformTrackDTO));
			}
		}
		return map;
	}
	
	/**
     * 解析订单数据
     **/
    public PlatformTrackDTO convert(List<DmpLogisticsTrackEntity> trackEntityList , String cfgOutputId) {
    	if (CollectionUtils.isEmpty(trackEntityList)) {
    		return null;
		}
		if(this.validateDataBlack(trackEntityList.get(0), cfgOutputId)) {
			return null;
		}

		PlatformTrackDTO platformTrackDTO = new PlatformTrackDTO();

		platformTrackDTO.setTrackNo(trackEntityList.get(0).getTrackNo());

		List<PlatformTrackDetail> details = new ArrayList<>();

		for (DmpLogisticsTrackEntity dmpLogisticsTrackEntity : trackEntityList) {
			PlatformTrackDetail platformTrackDetail = new PlatformTrackDetail();
			platformTrackDetail.setContent(dmpLogisticsTrackEntity.getContent());
			platformTrackDetail.setTrackNo(dmpLogisticsTrackEntity.getTrackNo());
			platformTrackDetail.setTrackTime(dmpLogisticsTrackEntity.getTrackTime());
			platformTrackDetail.setStatus(dmpLogisticsTrackEntity.getStatus());

			details.add(platformTrackDetail);
		}

		platformTrackDTO.setDetails(details);


		platformTrackDTO.setPlatform(PlatformDictEnum.TRACK123.getCode());
		platformTrackDTO.setUniqueId(UUID.randomUUID().toString());
        return platformTrackDTO;
    }

	@Override
	protected List<String> getSourceCodeKeys() {
		return Arrays.asList("trackNo");
	}
}
