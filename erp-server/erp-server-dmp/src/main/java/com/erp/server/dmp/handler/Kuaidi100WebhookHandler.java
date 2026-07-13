package com.erp.server.dmp.handler;

import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.common.business.dto.WebhookResult;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.erp.model.dmp.dto.Kuaidi100WebhookResponseDTO;
import com.erp.server.dmp.service.DmpLogisticsTrackWebhookRecordService;
import com.sdk.tms.kuaidi100.service.Kuaidi100Service;
import org.apache.commons.codec.digest.DigestUtils;

import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * 快递100订阅回调处理
 *
 * @author jack
 */
public class Kuaidi100WebhookHandler implements WebhookHandler {

    private DmpLogisticsTrackWebhookRecordService webhookRecordService;

    @Override
    public void verify(String data, Map<String, String> headers, String serviceFlag) {
        Map<String, String> formMap = Kuaidi100WebhookPayloadParser.parseFormBody(data);
        String param = formMap.get(Kuaidi100WebhookPayloadParser.PARAM);
        String sign = formMap.get(Kuaidi100WebhookPayloadParser.SIGN);
        if (StrUtil.isBlank(param) || StrUtil.isBlank(sign)) {
            throw new ServiceException(ApiError.DMP_KUAIDI100_WEBHOOK_PARAM_SIGN_REQUIRED);
        }
        String expectedSign = DigestUtils.md5Hex((param + Kuaidi100Service.SALT).getBytes(StandardCharsets.UTF_8)).toUpperCase();
        if (!expectedSign.equals(sign.trim())) {
            throw new ServiceException(ApiError.DMP_KUAIDI100_WEBHOOK_SIGN_INVALID);
        }
    }

    @Override
    public WebhookResult<Kuaidi100WebhookResponseDTO> process(String data, Map<String, String> headers, String serviceFlag) {
        Map<String, String> formMap = Kuaidi100WebhookPayloadParser.parseFormBody(data);
        getWebhookRecordService().saveKuaidi100RawRecord(
                formMap.get(Kuaidi100WebhookPayloadParser.PARAM),
                formMap.get(Kuaidi100WebhookPayloadParser.SIGN)
        );
        WebhookResult<Kuaidi100WebhookResponseDTO> result = new WebhookResult<>();
        result.setData(Kuaidi100WebhookResponseDTO.success());
        return result;
    }

    private DmpLogisticsTrackWebhookRecordService getWebhookRecordService() {
        if (webhookRecordService == null) {
            webhookRecordService = SpringUtil.getBean(DmpLogisticsTrackWebhookRecordService.class);
        }
        return webhookRecordService;
    }
}
