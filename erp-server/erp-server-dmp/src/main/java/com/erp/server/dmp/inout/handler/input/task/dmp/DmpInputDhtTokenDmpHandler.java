package com.erp.server.dmp.inout.handler.input.task.dmp;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * 字段映射转换
 */
@Slf4j
@Service
@Scope("prototype")
public class DmpInputDhtTokenDmpHandler extends DmpInputDbConvertDmpHandler {

    @Override
    protected void afterConvertData(Map<List<Map<String, Object>>, List<TreeMap<String, Object>>> dmpInputDataDmpRelationMaps) {
        log.debug("DmpInputDhtTokenDmpHandler afterConvertData 处理");
        for(Map.Entry<List<Map<String, Object>>, List<TreeMap<String, Object>>> dmpInputDataDmpRelationMap : dmpInputDataDmpRelationMaps.entrySet()) {
            List<TreeMap<String, Object>> dmpDataMaps = dmpInputDataDmpRelationMap.getValue();
            for(TreeMap<String, Object> dmpDataMap : dmpDataMaps) {
                Object expiresInObj = dmpDataMap.get("expiresIn");
                if (expiresInObj != null) {
                    Integer expiresIn = (Integer) expiresInObj;
                    dmpDataMap.put("expireTime", LocalDateTime.now().plusSeconds(expiresIn));
                    dmpDataMap.put("nextLevelId", nextLevelId);
                }
            }
        }
    }
}
