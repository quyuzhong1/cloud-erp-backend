package com.erp.server.dmp.inout.handler.input.task.dmp;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.core.anno.ParamData;
import com.common.core.entity.BaseEntity;
import com.common.core.enums.PannoEnum;
import com.common.core.exception.ServiceException;
import com.erp.model.dmp.entity.DmpCfgInputConvertEntity;
import com.erp.model.dmp.entity.DmpInputTaskEntity;
import com.erp.server.dmp.inout.handler.input.task.init.DmpInputAmzCommonInitHandler;
import com.erp.server.dmp.inout.handler.input.task.mongo.DmpInputMongoHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * FBA InboundPlan 货件明细子任务处理器
 */
@Slf4j
@Service
@Scope("prototype")
public class DmpInputAmzFbaInboundPlanShipmentDetailDmpHandler extends DmpInputDoChildDmpHandler {

    @Override
    protected List<Map<String, Object>> getDmpInputMongoChildEntityList(List<Map<String, Object>> dmpInputMongoEntityList, String childMongoStorageName) {
        String childCfgInputId = this.getDmpCfgInputChildId();
        if (StringUtils.isBlank(childCfgInputId)) {
            return Collections.emptyList();
        }
        List<DmpInputTaskEntity> childTaskList = dmpInputTaskService.lambdaQuery()
                .eq(DmpInputTaskEntity::getParentTaskId, inputTaskId)
                .eq(DmpInputTaskEntity::getCfgInputId, childCfgInputId)
                .list();
        if (CollUtil.isEmpty(childTaskList)) {
            return Collections.emptyList();
        }
        List<String> childTaskIds = childTaskList.stream()
                .map(DmpInputTaskEntity::getId)
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.toList());
        if (CollUtil.isEmpty(childTaskIds)) {
            return Collections.emptyList();
        }
        List<ParamData> paramDataList = new ArrayList<>();
        paramDataList.add(new ParamData(DmpInputMongoHandler.MONGO_BASE_INPUTTASKID,
                DmpInputMongoHandler.MONGO_BASE_INPUTTASKID,
                PannoEnum.IN,
                childTaskIds));
        return mongoService.findMongoData(paramDataList, childMongoStorageName);
    }

    @Override
    protected void putDmpId(List<Map<String, Object>> dmpInputMongoChildEntityList) {
        DmpCfgInputConvertEntity mainConvertId = this.getMainConvertId();
        String parentStorageName = mainConvertId.getStorageName();
        ServiceImpl parentServiceImpl = this.getServiceImpl(parentStorageName);
        QueryWrapper<?> wrapper = new QueryWrapper<>();
        wrapper.eq(INPUT_TASK_ID, inputTaskId);
        List<Map<String, Object>> parentDataList = parentServiceImpl.listMaps(wrapper);
        Map<String, String> shipmentIdDmpIdMap = new HashMap<>();
        if (CollUtil.isNotEmpty(parentDataList)) {
            for (Map<String, Object> parentData : parentDataList) {
                Object dmpIdObj = parentData.get(BaseEntity.FIELD_ID);
                if (dmpIdObj == null) {
                    continue;
                }
                String dmpId = dmpIdObj.toString();
                Object shipmentIdObj = parentData.get("fba_shipment_id");
                if (shipmentIdObj != null && StringUtils.isNotBlank(shipmentIdObj.toString())) {
                    shipmentIdDmpIdMap.putIfAbsent(shipmentIdObj.toString(), dmpId);
                }
            }
        }
        int unmatchedCount = 0;
        for (Map<String, Object> childData : dmpInputMongoChildEntityList) {
            String shipmentKey = DmpInputAmzCommonInitHandler.firstNonBlankString(childData,
                    "shipmentConfirmationId", "fbaShipmentId", "shipmentId");
            if (StringUtils.isBlank(shipmentKey)) {
                unmatchedCount++;
                log.warn("未匹配主表货件ID, 明细货件键为空, inputTaskId={}", inputTaskId);
                continue;
            }
            String dmpId = shipmentIdDmpIdMap.get(shipmentKey);
            if (StringUtils.isBlank(dmpId)) {
                unmatchedCount++;
                log.warn("未匹配主表货件ID, shipmentKey={}, inputTaskId={}", shipmentKey, inputTaskId);
                continue;
            }
            childData.put(MAIN_ID, dmpId);
        }
        if (unmatchedCount > 0) {
            log.warn("FBA入库计划货件明细关联主表失败, unmatchedCount={}, inputTaskId={}", unmatchedCount, inputTaskId);
            if (isManualInboundPlanShipmentPull()) {
                throw new ServiceException("手动拉取FBA货件明细关联主表失败, unmatchedCount=" + unmatchedCount
                        + ", inputTaskId=" + inputTaskId);
            }
        }
    }

    private boolean isManualInboundPlanShipmentPull() {
        DmpInputTaskEntity startTask = dmpInputTaskService.getById(inputTaskId);
        DmpInputTaskEntity rootTask = dmpInputTaskService.findRootTaskInChain(startTask);
        return DmpInputAmzCommonInitHandler.hasManualShipmentCodeFilter(rootTask);
    }
}
