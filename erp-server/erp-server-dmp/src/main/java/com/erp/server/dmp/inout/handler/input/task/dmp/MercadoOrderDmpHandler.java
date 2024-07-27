package com.erp.server.dmp.inout.handler.input.task.dmp;

import cn.hutool.core.collection.CollectionUtil;
import com.common.business.enums.ApproveStatusEnum;
import com.common.core.anno.ParamData;
import com.common.core.enums.PannoEnum;
import com.erp.model.dmp.entity.DmpInputTaskEntity;
import com.erp.model.oms.enums.SoB2cBillStatusEnum;
import com.erp.server.dmp.inout.handler.input.task.mongo.DmpInputMongoHandler;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 订单主表字段映射转换
 */
@Service
@Scope("prototype")
public class MercadoOrderDmpHandler extends MercadoDmpHandler {




    @Override
    protected void afterConvertData(Map<List<Map<String, Object>>, List<TreeMap<String, Object>>> dmpInputDataDmpRelationMaps) {
        String id = dmpInputTaskEntity.getId();
        List<DmpInputTaskEntity> list = dmpInputTaskService.lambdaQuery().eq(DmpInputTaskEntity::getParentTaskId, id).list();
        if (CollectionUtil.isEmpty(list)) {
            return;
        }
        DmpInputTaskEntity dmpInputTaskEntity = list.get(0);
        List<DmpInputTaskEntity> inputTaskEntityList = dmpInputTaskService.lambdaQuery()
                .eq(DmpInputTaskEntity::getParentTaskId, dmpInputTaskEntity.getId())
                .eq(DmpInputTaskEntity::getCfgInputId, "1816384798088779541")
                .list();

        List<String> taskIds = inputTaskEntityList.stream().map(req -> req.getId()).distinct().collect(Collectors.toList());
        List<ParamData> paramDataList = new ArrayList<>();
        paramDataList.add(new ParamData(DmpInputMongoHandler.MONGO_BASE_INPUTTASKID, DmpInputMongoHandler.MONGO_BASE_INPUTTASKID, PannoEnum.EQ, taskIds));
        List<Map<String, Object>> dmpInputMongoChildList = mongoService.findMongoData(paramDataList, "mercadolibre_shipment_data");


        for (Map.Entry<List<Map<String, Object>>, List<TreeMap<String, Object>>> dmpInputDataDmpRelationMap : dmpInputDataDmpRelationMaps.entrySet()) {
            List<TreeMap<String, Object>> dmpDataMaps = dmpInputDataDmpRelationMap.getValue();
            for (TreeMap<String, Object> dmpDataMap : dmpDataMaps) {
                //作废状态
                Object statusObj = dmpDataMap.get("status");
                if (statusObj != null) {
                    String status = String.valueOf(statusObj);
                    if ("invalid".equalsIgnoreCase(status)) {
                        dmpDataMap.put("invalidStatus", Boolean.TRUE);
                    } else if ("cancelled".equalsIgnoreCase(status)) {
                        dmpDataMap.put("invalidStatus", Boolean.TRUE);
                    }
                }



                //物流信息
//                dmpInputMongoChildList.stream().filter(req -> req.get(""))
                dmpDataMap.get("");

            }
        }
    }
}
