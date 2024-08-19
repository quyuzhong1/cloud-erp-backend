package com.erp.server.dmp.inout.handler.input.task.dmp;

import cn.hutool.core.collection.CollectionUtil;
import com.erp.model.dmp.entity.DmpInputTaskEntity;
import com.erp.server.dmp.service.DmpSoDetailService;
import com.erp.server.dmp.service.DmpSoInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Service
@Scope("prototype")
public class TikTokProductDmpHandler extends DmpInputDbConvertDmpHandler{

    @Autowired
    private DmpSoInfoService dmpSoInfoService;

    @Autowired
    private DmpSoDetailService dmpSoDetailService;

    @Override
    protected void afterConvertData(Map<List<Map<String, Object>>, List<TreeMap<String, Object>>> dmpInputDataDmpRelationMaps) {
        for(Map.Entry<List<Map<String, Object>>, List<TreeMap<String, Object>>> dmpInputDataDmpRelationMap : dmpInputDataDmpRelationMaps.entrySet()) {
            List<TreeMap<String, Object>> dmpDataMaps = dmpInputDataDmpRelationMap.getValue();
            List<Map<String, Object>> mongoDataMaps = dmpInputDataDmpRelationMap.getKey();
            Map<String, Object> mongoDataMap = mongoDataMaps.get(0);
            for(TreeMap<String, Object> dmpDataMap : dmpDataMaps) {
                dmpDataMap.put("nextLevelId", nextLevelId);
                dmpDataMap.put("shopId", nextLevelId);

                //创建时间
                Object createTimeObj = mongoDataMap.get("createTime");
                if (createTimeObj != null) {
                    // 使用Instant类将Unix时间戳转换为LocalDateTime对象
                    LocalDateTime payTime = LocalDateTime.ofInstant(Instant.ofEpochSecond(Long.valueOf(createTimeObj + "")), ZoneId.systemDefault());
                    dmpDataMap.put("platformCreateTime", payTime);
                }

                //修改时间
                Object updateTimeObj = mongoDataMap.get("updateTime");
                if (updateTimeObj != null) {
                    // 使用Instant类将Unix时间戳转换为LocalDateTime对象
                    LocalDateTime payTime = LocalDateTime.ofInstant(Instant.ofEpochSecond(Long.valueOf(updateTimeObj + "")), ZoneId.systemDefault());
                    dmpDataMap.put("platformUpdateTime", payTime);
                }
            }
        }
    }
}
