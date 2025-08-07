package com.erp.server.dmp.inout.handler.input.task.finish;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.LocalDateTimeUtil;

import com.alibaba.fastjson.JSON;
import com.common.business.enums.PlatformDictEnum;
import com.common.core.entity.BaseEntity;
import com.erp.model.dmp.entity.*;
import com.erp.model.dmp.enums.DmpInputTaskTaskTypeEnum;
import com.erp.model.oms.enums.OrderLogisticTypeEnum;
import com.erp.server.dmp.inout.dto.request.DmpInputHotfixCreateRequest;
import com.erp.server.dmp.inout.dto.request.DmpInputTaskRequest;
import com.erp.server.dmp.inout.dto.response.DmpInputCreateResponse;
import com.erp.server.dmp.inout.dto.response.DmpInputFinishResponse;
import com.erp.server.dmp.inout.dto.response.DmpInputTaskResponse;
import com.erp.server.dmp.service.DmpCfgInputDetailService;
import com.erp.server.dmp.service.DmpInputTaskService;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
@Scope("prototype")
public class DmpInputLxFinishHandler extends DmpInputBaseFinishHandler{

	@Resource
	private DmpCfgInputDetailService dmpCfgInputDetailService;

	@Resource
	private DmpInputTaskService dmpInputTaskService;

	@Override
	protected void afterToDoStatus(DmpInputTaskRequest dmpRequest, DmpInputTaskResponse dmpResponse) {
		super.afterToDoStatus(dmpRequest, dmpResponse);
		DmpInputFinishResponse dmpInputFinishResponse = (DmpInputFinishResponse) dmpResponse;
		Map<DmpCfgInputConvertEntity, List<BaseEntity>> convertInputDmpBaseEntityListMaps = dmpInputFinishResponse.getConvertInputDmpBaseEntityListMaps();
		Map<String, DmpSoInfoEntity> dmpSoInfoEntityMap = new HashMap<>();
		Map<String, List<DmpSoDetailEntity>> dmpSoDetailEntityMap = new HashMap<>();
		for (Map.Entry<DmpCfgInputConvertEntity, List<BaseEntity>> convertInputDmpBaseEntityListMap : convertInputDmpBaseEntityListMaps.entrySet()) {
			List<BaseEntity> value = convertInputDmpBaseEntityListMap.getValue();
			if (CollUtil.isNotEmpty(value)) {
				String storageName = convertInputDmpBaseEntityListMap.getKey().getStorageName();
				if ("dmp_so_info".equals(storageName)) {
					for (BaseEntity v : value) {
						DmpSoInfoEntity dmpSoInfoEntity = (DmpSoInfoEntity) v;
						dmpSoInfoEntityMap.put(dmpSoInfoEntity.getId(), dmpSoInfoEntity);
					}
				} else if ("dmp_so_detail".equals(storageName)) {
					for (BaseEntity v : value) {
						DmpSoDetailEntity dmpSoDetailEntity = (DmpSoDetailEntity) v;
						String mainId = dmpSoDetailEntity.getMainId();
						List<DmpSoDetailEntity> list = dmpSoDetailEntityMap.get(mainId);
						if (CollUtil.isEmpty(list)) {
							list = new ArrayList<>();
						}
						list.add(dmpSoDetailEntity);
						dmpSoDetailEntityMap.put(mainId, list);
					}
				}
			}
		}
		dmpSoInfoEntityMap.forEach((id,entity)->{
			String sourcePlatform = entity.getSourcePlatform();
			boolean isPlatform = OrderLogisticTypeEnum.PLATFORM_WAREHOUSE.getCode().equals(entity.getLogisticType());
			//如果是temu半托管并且是平台仓并且已发货，创建temu发货任务,按明细创建
			if(PlatformDictEnum.TE_MU.getCode().equals(sourcePlatform) && isPlatform){
				List<DmpSoDetailEntity> detailEntityList = dmpSoDetailEntityMap.get(id);
				for (DmpSoDetailEntity dmpSoDetailEntity : detailEntityList) {
					String uniqueKey = id + "_" + dmpSoDetailEntity.getId();
					// 查询任务是否存在
					DmpInputTaskEntity dbDetail = dmpInputTaskService.getByInputIdAndExtendJson("1940979833966186779","uniqueKey",uniqueKey);
					if( dbDetail != null) {
						// 如果存在，跳过
						return;
					}
					//创建推送任务
					Map<String, Object> map = new HashMap<>();
					map.put("entity",entity);
					map.put("detailEntity",dmpSoDetailEntity);
					map.put("uniqueKey",uniqueKey);
					String dataJson = JSON.toJSONString(map);
					DmpInputHotfixCreateRequest dmpInputCreateRequest = new DmpInputHotfixCreateRequest();
					dmpInputCreateRequest.setCfgInputId("1940979833966186779");
					dmpInputCreateRequest.setDetailExtendJson(dataJson);
					dmpInputCreateRequest.setNextExecTime(LocalDateTimeUtil.offset(LocalDateTime.now(), 1, ChronoUnit.MINUTES));
					// 拉取时间
					dmpInputCreateRequest.setTaskType(DmpInputTaskTaskTypeEnum.NORMAL.getCode());
					// 创建任务
					DmpInputCreateResponse response = dmpInputCreateFactory.createHotfixInputTask(dmpInputCreateRequest);
				}

			}
		});

	}
}
