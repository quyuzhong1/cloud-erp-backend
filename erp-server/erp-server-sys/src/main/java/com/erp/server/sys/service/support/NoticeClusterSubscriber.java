package com.erp.server.sys.service.support;

import cn.hutool.json.JSONUtil;
import com.erp.model.sys.dto.NoticeDispatchDTO;
import com.erp.server.sys.service.MessageUserReadService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Set;

@Slf4j
@Component
public class NoticeClusterSubscriber {

    @Resource
    private NoticeStreamEmitterManager noticeStreamEmitterManager;

    @Resource
    private MessageUserReadService messageUserReadService;

    @Resource
    private RedissonClient redissonClient;

    private final Map<String, Integer> listenerIdMap = new ConcurrentHashMap<>();

    @PostConstruct
    public void subscribe() {
        for (String application : noticeStreamEmitterManager.getSupportedApplications()) {
            RTopic topic = redissonClient.getTopic(noticeStreamEmitterManager.getNodeTopic(application));
            Integer listenerId = topic.addListener(String.class, (channel, body) -> handle(application, body));
            listenerIdMap.put(application, listenerId);
        }
    }

    @PreDestroy
    public void unsubscribe() {
        listenerIdMap.forEach((application, listenerId) ->
                redissonClient.getTopic(noticeStreamEmitterManager.getNodeTopic(application)).removeListener(listenerId));
        listenerIdMap.clear();
    }

    private void handle(String application, String body) {
        if (body == null || body.isEmpty()) {
            return;
        }
        NoticeDispatchDTO dispatchDTO = JSONUtil.toBean(body, NoticeDispatchDTO.class);
        if (dispatchDTO == null || dispatchDTO.getNotice() == null) {
            return;
        }
        Set<String> successUserIds = noticeStreamEmitterManager.pushLocalNotice(application, dispatchDTO.getNotice());
        if (Boolean.TRUE.equals(dispatchDTO.getMarkReadOnSuccess()) && CollectionUtils.isNotEmpty(successUserIds)) {
            messageUserReadService.markReadByUserIds(dispatchDTO.getMessageId(), successUserIds);
        }
        log.debug("Handle notice cluster message success, messageId={}, application={}, pushUsers={}",
                dispatchDTO.getMessageId(), application, successUserIds == null ? 0 : successUserIds.size());
    }
}
