package com.erp.server.dmp.inout.handler.input.task.finish;

import cn.hutool.core.collection.CollUtil;
import com.common.business.enums.PlatformDictEnum;
import com.erp.model.dmp.entity.DmpCfgInputConvertEntity;
import com.erp.model.dmp.enums.DmpInputTaskStatusEnum;
import com.erp.server.dmp.handler.Kuaidi100WebhookPayloadParser;
import com.erp.server.dmp.inout.dto.request.DmpInputTaskRequest;
import com.erp.server.dmp.inout.dto.response.DmpInputFinishResponse;
import com.erp.server.dmp.inout.dto.response.DmpInputTaskResponse;
import com.erp.server.dmp.service.DmpLogisticsTrackWebhookRecordService;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@Scope("prototype")
public class Kuaidi100WebhookFinishHandler extends DmpInputBaseFinishHandler {

    private static final String PLATFORM_CODE = PlatformDictEnum.KUAIDI100.getCode();

    @Resource
    private DmpLogisticsTrackWebhookRecordService dmpLogisticsTrackWebhookRecordService;

    @Override
    protected void afterToDoUpdateTaskStatus(DmpInputTaskRequest dmpRequest,
                                             DmpInputTaskResponse dmpResponse,
                                             boolean updateSuccess) {
        super.afterToDoUpdateTaskStatus(dmpRequest, dmpResponse, updateSuccess);
        if (!updateSuccess || DmpInputTaskStatusEnum.FINISH != updateTaskStatus) {
            return;
        }
        DmpInputFinishResponse finishResponse = (DmpInputFinishResponse) dmpResponse;
        dmpLogisticsTrackWebhookRecordService.markFinish(PLATFORM_CODE, getWebhookRecordIds(finishResponse));
    }

    private Set<String> getWebhookRecordIds(DmpInputFinishResponse finishResponse) {
        Set<String> webhookRecordIds = new HashSet<>();
        Map<DmpCfgInputConvertEntity, List<Map<String, Object>>> mongoMaps =
                finishResponse.getConvertInputMongoEntityListMaps();
        if (CollUtil.isEmpty(mongoMaps)) {
            return webhookRecordIds;
        }
        for (List<Map<String, Object>> rows : mongoMaps.values()) {
            if (CollUtil.isEmpty(rows)) {
                continue;
            }
            for (Map<String, Object> row : rows) {
                if (row == null) {
                    continue;
                }
                Object webhookRecordId = row.get(Kuaidi100WebhookPayloadParser.WEBHOOK_RECORD_ID);
                if (webhookRecordId != null) {
                    webhookRecordIds.add(webhookRecordId.toString());
                }
            }
        }
        return webhookRecordIds;
    }
}
