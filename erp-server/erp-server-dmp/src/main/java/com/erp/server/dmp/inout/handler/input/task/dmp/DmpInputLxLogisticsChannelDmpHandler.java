package com.erp.server.dmp.inout.handler.input.task.dmp;

import cn.hutool.core.text.CharSequenceUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * dmp处理下一个扩展handler，如何订单收货人信息单独一张表，使用此handler即可，因有成员变量，最终实现类由spring管理需要是多例@Scope("prototype")
 *
 * @author Administrator
 */
@Slf4j
@Service
@Scope("prototype")
public class DmpInputLxLogisticsChannelDmpHandler extends DmpInputDbConvertDmpHandler {

    public static final String LX_USED_LOGISTICS_TYPE_DATA_MONGO_TABLE = "lingxing_listUsedLogisticsType_data";


    @Override
    protected Map<List<Map<String, Object>>, List<TreeMap<String, Object>>> convertData(List<Map<String, Object>> dmpInputMongoEntityList) {
        // 查询历史所有信息
        List<Map<String, Object>> mongoData = mongoService.findMongoData(new ArrayList<>(), LX_USED_LOGISTICS_TYPE_DATA_MONGO_TABLE);

        List<String> newMongoListKeyList = dmpInputMongoEntityList.stream().map(e -> {
            String type = e.getOrDefault("type", "").toString();
            String logisticsProviderId = e.getOrDefault("logistics_provider_id", "").toString();
            String typeId = e.getOrDefault("type_id", "").toString();
            return CharSequenceUtil.format("{}_{}_{}", type, logisticsProviderId, typeId);
        }).distinct().collect(Collectors.toList());

        // 不存在mongo的信息设置为禁用
        for (Map<String, Object> mongoItem : mongoData) {
            String type = mongoItem.getOrDefault("type", "").toString();
            String logisticsProviderId = mongoItem.getOrDefault("logistics_provider_id", "").toString();
            String typeId = mongoItem.getOrDefault("type_id", "").toString();
            String curKey = CharSequenceUtil.format("{}_{}_{}", type, logisticsProviderId, typeId);
            if (newMongoListKeyList.contains(curKey)){
                continue;
            }
            mongoItem.put("is_used", 0);
            dmpInputMongoEntityList.add(mongoItem);
        }
        return super.convertData(dmpInputMongoEntityList);
    }

    @Override
    protected void afterConvertData(Map<List<Map<String, Object>>, List<TreeMap<String, Object>>> dmpInputDataDmpRelationMaps) {
        log.debug("DmpInputLxLogisticsChannelDmpHandler 处理完成: taskId={}", dmpInputTaskEntity.getId());

        for (Map.Entry<List<Map<String, Object>>, List<TreeMap<String, Object>>> dmpInputDataDmpRelationMap : dmpInputDataDmpRelationMaps.entrySet()) {
            List<TreeMap<String, Object>> dmpDataMaps = dmpInputDataDmpRelationMap.getValue();
            for (TreeMap<String, Object> dmpDataMap : dmpDataMaps) {
                String isSync = dmpDataMap.getOrDefault("is_used", 0).toString();
                dmpDataMap.put("disabled", !"1".equalsIgnoreCase(isSync));
            }
        }
    }



}
