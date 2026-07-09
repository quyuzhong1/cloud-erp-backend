package com.erp.server.dmp.inout.handler.output.task.mq;

import cn.hutool.core.collection.CollUtil;
import com.common.business.dto.PlatformFbaShipmentDTO;
import com.common.core.entity.BaseEntity;
import com.erp.model.dmp.entity.DmpCfgInputConvertEntity;
import com.erp.model.dmp.entity.DmpFbaShipmentDetailEntity;
import com.erp.model.dmp.entity.DmpFbaShipmentEntity;
import com.erp.server.dmp.inout.dto.request.DmpOutputTaskRequest;
import com.erp.server.dmp.inout.dto.response.DmpOutputTaskResponse;
import com.erp.server.dmp.service.DmpInputTaskService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * FBA InboundPlan 货件输出（沿用 FBA 货件 MQ 结构）
 */
@Service
@Scope("prototype")
public class DmpOutputAmzFbaPlanShipmentRocketMQTaskHandler extends DmpOutputAmzFbaShipmentRocketMQTaskHandler {

    @Resource
    private DmpInputTaskService dmpInputTaskService;

    private Map<String, String> rootTaskShopIdCache = Collections.emptyMap();

    @Override
    public Map<String, String> getPushJsonDataMap(DmpOutputTaskRequest dmpRequest, DmpOutputTaskResponse dmpResponse) {
        try {
            rootTaskShopIdCache = dmpInputTaskService.batchResolveRootTaskShopId(collectInputTaskIds(dmpRequest));
            return super.getPushJsonDataMap(dmpRequest, dmpResponse);
        } finally {
            rootTaskShopIdCache = Collections.emptyMap();
        }
    }

    @Override
    public PlatformFbaShipmentDTO convert(DmpFbaShipmentEntity dmpMainEntity,
                                          List<DmpFbaShipmentDetailEntity> dmpDetailEntityList,
                                          String cfgOutputId) {
        PlatformFbaShipmentDTO dto = super.convert(dmpMainEntity, dmpDetailEntityList, cfgOutputId);
        if (dto == null) {
            return null;
        }
        dto.setShopId(resolveRootTaskShopId(dmpMainEntity));
        return dto;
    }

    private Set<String> collectInputTaskIds(DmpOutputTaskRequest dmpRequest) {
        Set<String> inputTaskIds = new HashSet<>();
        Map<DmpCfgInputConvertEntity, List<BaseEntity>> convertInputDmpBaseEntityListMaps =
                dmpRequest.getConvertInputDmpBaseEntityListMaps();
        if (CollUtil.isEmpty(convertInputDmpBaseEntityListMaps)) {
            return inputTaskIds;
        }
        for (Map.Entry<DmpCfgInputConvertEntity, List<BaseEntity>> entry : convertInputDmpBaseEntityListMaps.entrySet()) {
            if (entry.getKey() == null || !"dmp_fba_shipment".equals(entry.getKey().getStorageName())) {
                continue;
            }
            List<BaseEntity> entityList = entry.getValue();
            if (CollUtil.isEmpty(entityList)) {
                continue;
            }
            for (BaseEntity entity : entityList) {
                if (!(entity instanceof DmpFbaShipmentEntity)) {
                    continue;
                }
                String inputTaskId = ((DmpFbaShipmentEntity) entity).getInputTaskId();
                if (StringUtils.isNotBlank(inputTaskId)) {
                    inputTaskIds.add(inputTaskId);
                }
            }
        }
        return inputTaskIds;
    }

    private String resolveRootTaskShopId(DmpFbaShipmentEntity dmpMainEntity) {
        if (dmpMainEntity == null) {
            return "";
        }
        if (StringUtils.isNotBlank(dmpMainEntity.getInputTaskId())) {
            String cachedShopId = rootTaskShopIdCache.get(dmpMainEntity.getInputTaskId());
            if (cachedShopId != null) {
                return StringUtils.isNotBlank(cachedShopId)
                        ? cachedShopId
                        : StringUtils.defaultString(dmpMainEntity.getNextLevelId());
            }
        }
        return StringUtils.defaultString(dmpMainEntity.getNextLevelId());
    }
}
