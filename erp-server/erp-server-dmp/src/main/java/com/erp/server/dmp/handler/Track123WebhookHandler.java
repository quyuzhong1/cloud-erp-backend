package com.erp.server.dmp.handler;

import cn.hutool.Hutool;
import cn.hutool.json.JSONUtil;
import com.erp.model.dmp.track123.WebhookRequest;
import com.erp.model.tms.dto.LogisticsTrackDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author zdy
 * @ClassName Track123WebhookHandler
 * @description: TODO
 * @date 2024年11月11日
 * @version: 1.0
 */
public class Track123WebhookHandler implements WebhookHandler{
    @Override
    public void process(String request) {
        LogisticsTrackDTO.TrackWebHookDTO trackWebHookDTO = JSONUtil.toBean(request, LogisticsTrackDTO.TrackWebHookDTO.class);
        //TODO 推送mq
        System.out.println(trackWebHookDTO);
    }
}
