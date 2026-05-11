package com.erp.server.dmp.inout.handler.input.task.dmp;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.core.anno.ParamData;
import com.common.core.entity.BaseEntity;
import com.common.core.enums.PannoEnum;
import com.erp.model.dmp.entity.DmpCfgInputConvertEntity;
import com.erp.model.dmp.entity.DmpInputTaskEntity;
import com.erp.server.dmp.inout.handler.input.task.mongo.DmpInputMongoHandler;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * FBA InboundPlan 货件明细子任务处理器
 */
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
        List<ParamData> paramDataList = new ArrayList<>();
        paramDataList.add(new ParamData(DmpInputMongoHandler.MONGO_BASE_INPUTTASKID,
                DmpInputMongoHandler.MONGO_BASE_INPUTTASKID,
                PannoEnum.EQ,
                childTaskList.get(0).getId()));
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
                Object shipmentIdObj = parentData.get("fba_shipment_id");
                Object dmpIdObj = parentData.get(BaseEntity.FIELD_ID);
                if (shipmentIdObj == null || dmpIdObj == null) {
                    continue;
                }
                shipmentIdDmpIdMap.put(shipmentIdObj.toString(), dmpIdObj.toString());
            }
        }
        for (Map<String, Object> childData : dmpInputMongoChildEntityList) {
            Object shipmentIdObj = childData.get("shipmentId");
            if (shipmentIdObj == null) {
                continue;
            }
            String dmpId = shipmentIdDmpIdMap.get(shipmentIdObj.toString());
            childData.put(MAIN_ID, dmpId);
        }
    }
}
