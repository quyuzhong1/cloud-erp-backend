package com.erp.server.sys.service.support;

import cn.hutool.json.JSONUtil;
import com.erp.model.sys.dto.NoticeDispatchDTO;
import com.erp.model.sys.enums.MessageDispatchTaskSceneEnum;
import com.erp.model.sys.enums.SysTypeEnum;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Collections;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class NoticeClusterPublisher {

    @Resource
    private RedissonClient redissonClient;

    @Resource
    private NoticeStreamEmitterManager noticeStreamEmitterManager;

    public void publish(NoticeDispatchDTO dispatchDTO) {
        if (dispatchDTO == null) {
            return;
        }
        Set<String> targetApplications = resolveTargetApplications(dispatchDTO);
        if (targetApplications.isEmpty()) {
            log.info("Skip notice cluster publish because no target application resolved, messageId={}, scene={}",
                    dispatchDTO.getMessageId(), dispatchDTO.getScene());
            return;
        }
        for (String application : targetApplications) {
            Set<String> targetUserIds = resolveTargetUserIds(application, dispatchDTO);
            if (targetUserIds.isEmpty()) {
                log.info("Skip notice cluster publish because no routed users found, messageId={}, scene={}, application={}",
                        dispatchDTO.getMessageId(), dispatchDTO.getScene(), application);
                continue;
            }
            Map<String, Set<String>> nodeUserIdsMap = noticeStreamEmitterManager.groupUserIdsByOnlineNode(application, targetUserIds);
            if (nodeUserIdsMap.isEmpty()) {
                log.info("Skip notice cluster publish because no online node matched target users, messageId={}, scene={}, application={}, targetUsers={}",
                        dispatchDTO.getMessageId(), dispatchDTO.getScene(), application, targetUserIds.size());
                continue;
            }
            for (Map.Entry<String, Set<String>> entry : nodeUserIdsMap.entrySet()) {
                String nodeId = entry.getKey();
                NoticeDispatchDTO nodeDispatchDTO = copyDispatch(dispatchDTO, entry.getValue());
                String body = JSONUtil.toJsonStr(nodeDispatchDTO);
                RTopic topic = redissonClient.getTopic(noticeStreamEmitterManager.buildNodeTopic(application, nodeId));
                topic.publish(body);
            }
            log.info("Publish notice cluster message success, messageId={}, scene={}, application={}, targetUsers={}, targetNodes={}, nodeRoute={}",
                    dispatchDTO.getMessageId(),
                    dispatchDTO.getScene(),
                    application,
                    targetUserIds.size(),
                    nodeUserIdsMap.size(),
                    nodeUserIdsMap.entrySet().stream().collect(java.util.stream.Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().size())));
        }
    }

    private Set<String> resolveTargetUserIds(String application, NoticeDispatchDTO dispatchDTO) {
        if (dispatchDTO.getTargetUserIds() != null && !dispatchDTO.getTargetUserIds().isEmpty()) {
            return new LinkedHashSet<>(dispatchDTO.getTargetUserIds());
        }
        return noticeStreamEmitterManager.listRegisteredUserIds(application);
    }

    private NoticeDispatchDTO copyDispatch(NoticeDispatchDTO source, Set<String> targetUserIds) {
        NoticeDispatchDTO target = new NoticeDispatchDTO();
        target.setMessageId(source.getMessageId());
        target.setScene(source.getScene());
        target.setMarkReadOnSuccess(source.getMarkReadOnSuccess());
        target.setNotice(source.getNotice());
        target.setTargetUserIds(new LinkedHashSet<>(targetUserIds));
        return target;
    }

    private Set<String> resolveTargetApplications(NoticeDispatchDTO dispatchDTO) {
        String application = dispatchDTO.getNotice() == null ? null : dispatchDTO.getNotice().getApplication();
        if (StringUtils.equalsIgnoreCase(application, SysTypeEnum.PDA.getCode())) {
            return Collections.singleton(SysTypeEnum.PDA.getCode());
        }
        if (StringUtils.equalsIgnoreCase(application, SysTypeEnum.PC.getCode())) {
            return Collections.singleton(SysTypeEnum.PC.getCode());
        }
        if (StringUtils.equalsIgnoreCase(application, "ALL")) {
            return noticeStreamEmitterManager.getSupportedApplications();
        }
        if (StringUtils.equals(dispatchDTO.getScene(), MessageDispatchTaskSceneEnum.PDA_UPGRADE_PUSH.getCode())) {
            return Collections.singleton(SysTypeEnum.PDA.getCode());
        }
        return Collections.emptySet();
    }
}
