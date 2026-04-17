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
import java.util.Collections;
import java.util.Set;

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
            return;
        }
        String body = JSONUtil.toJsonStr(dispatchDTO);
        for (String application : targetApplications) {
            Set<String> onlineNodeIds = noticeStreamEmitterManager.listOnlineNodeIds(application);
            if (onlineNodeIds.isEmpty()) {
                continue;
            }
            for (String nodeId : onlineNodeIds) {
                RTopic topic = redissonClient.getTopic(noticeStreamEmitterManager.buildNodeTopic(application, nodeId));
                topic.publish(body);
            }
        }
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
