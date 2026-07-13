package com.erp.server.dmp.inout.handler.input.task.init.api.kuaidi100;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.erp.model.dmp.entity.DmpLogisticsTrackWebhookRecordEntity;
import com.erp.server.dmp.handler.Kuaidi100WebhookPayloadParser;
import com.erp.server.dmp.inout.dto.base.DmpInputTaskInitDTO;
import com.erp.server.dmp.inout.dto.request.DmpInputApiInitRequest;
import com.erp.server.dmp.inout.handler.input.task.init.api.DmpInputApiInitHandler;
import com.erp.server.dmp.service.DmpLogisticsTrackWebhookRecordService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@Scope("prototype")
public class Kuaidi100WebhookInitHandler implements DmpInputApiInitHandler {

    private static final int DEFAULT_BATCH_SIZE = 100;
    private static final int DEFAULT_ING_TIMEOUT_MINUTES = 30;
    private static final String BATCH_SIZE_PARAM = "batchSize";
    private static final String ING_TIMEOUT_MINUTES_PARAM = "ingTimeoutMinutes";
    private static final String PARSE_ERROR_PREFIX = "清洗解析失败：";

    @Resource
    private DmpLogisticsTrackWebhookRecordService dmpLogisticsTrackWebhookRecordService;

    @Override
    public List<DmpInputTaskInitDTO> getApiData(DmpInputApiInitRequest request) {
        InitConfig initConfig = parseInitConfig(request);
        dmpLogisticsTrackWebhookRecordService.prepareKuaidi100Records(initConfig.getIngTimeoutMinutes());
        List<DmpLogisticsTrackWebhookRecordEntity> records =
                dmpLogisticsTrackWebhookRecordService.claimKuaidi100LatestWaitRecords(initConfig.getBatchSize());
        if (records == null || records.isEmpty()) {
            return new ArrayList<>();
        }

        List<DmpInputTaskInitDTO> result = new ArrayList<>();
        for (DmpLogisticsTrackWebhookRecordEntity record : records) {
            try {
                Map<String, Object> data = Kuaidi100WebhookPayloadParser.buildMongoData(record.getId(), record.getRawData());
                DmpInputTaskInitDTO dto = new DmpInputTaskInitDTO();
                dto.setMsg(JSON.toJSONString(data));
                result.add(dto);
            } catch (Exception e) {
                log.error("快递100 webhook 源记录清洗解析失败，recordId={}", record.getId(), e);
                dmpLogisticsTrackWebhookRecordService.markError(record.getId(), PARSE_ERROR_PREFIX + e.getMessage());
            }
        }
        return result;
    }

    private InitConfig parseInitConfig(DmpInputApiInitRequest request) {
        int batchSize = DEFAULT_BATCH_SIZE;
        int ingTimeoutMinutes = DEFAULT_ING_TIMEOUT_MINUTES;
        if (request == null || StringUtils.isBlank(request.getRequestParam())) {
            return new InitConfig(batchSize, ingTimeoutMinutes);
        }
        try {
            JSONObject requestParam = JSON.parseObject(request.getRequestParam());
            if (requestParam == null) {
                return new InitConfig(batchSize, ingTimeoutMinutes);
            }
            batchSize = getPositiveInt(requestParam, BATCH_SIZE_PARAM, DEFAULT_BATCH_SIZE);
            ingTimeoutMinutes = getPositiveInt(requestParam, ING_TIMEOUT_MINUTES_PARAM, DEFAULT_ING_TIMEOUT_MINUTES);
        } catch (Exception e) {
            log.warn("解析快递100 webhook 初始任务配置失败，使用默认配置，requestParam={}",
                    request.getRequestParam(), e);
        }
        return new InitConfig(batchSize, ingTimeoutMinutes);
    }

    private int getPositiveInt(JSONObject requestParam, String key, int defaultValue) {
        Object value = requestParam.get(key);
        if (value == null) {
            return defaultValue;
        }
        try {
            int intValue;
            if (value instanceof Number) {
                intValue = ((Number) value).intValue();
            } else {
                String stringValue = String.valueOf(value);
                if (StringUtils.isBlank(stringValue)) {
                    return defaultValue;
                }
                intValue = Integer.parseInt(stringValue);
            }
            return intValue <= 0 ? defaultValue : intValue;
        } catch (Exception e) {
            log.warn("快递100 webhook 初始任务配置项无效，使用默认值，key={}, value={}", key, value);
            return defaultValue;
        }
    }

    private static class InitConfig {
        private final int batchSize;
        private final int ingTimeoutMinutes;

        private InitConfig(int batchSize, int ingTimeoutMinutes) {
            this.batchSize = batchSize;
            this.ingTimeoutMinutes = ingTimeoutMinutes;
        }

        private int getBatchSize() {
            return batchSize;
        }

        private int getIngTimeoutMinutes() {
            return ingTimeoutMinutes;
        }
    }
}
