package com.erp.server.sys.service.support;

import com.erp.model.sys.dto.MessageDTO;
import com.erp.model.sys.enums.SysTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RLock;
import org.redisson.api.RMap;
import org.redisson.api.RMapCache;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Component
public class NoticeStreamEmitterManager {

    private static final long SSE_TIMEOUT = 1800_000L;
    private static final long ONLINE_TTL_SECONDS = 120L;
    private static final String ONLINE_NODE_MAP_KEY_PREFIX = "sys:notice:sse:online:nodes:";
    private static final String ONLINE_USER_NODE_MAP_KEY_PREFIX = "sys:notice:sse:online:user:nodes:";
    private static final String USER_ROUTE_LOCK_KEY_PREFIX = "sys:notice:sse:online:user:lock:";
    private static final String NODE_TOPIC_PREFIX = "sys:notice:";

    private final Map<String, Map<String, Map<String, SseEmitter>>> emitterMap = new ConcurrentHashMap<>();
    private final Map<String, AtomicInteger> localConnectionCountMap = new ConcurrentHashMap<>();

    @Resource
    private RedissonClient redissonClient;

    @Value("${spring.application.name:erp-server-sys}")
    private String applicationName;

    @Value("${server.port:0000}")
    private String serverPort;

    private final String runtimeId = UUID.randomUUID().toString().replace("-", "");

    public SseEmitter register(String userId, String application) {
        String normalizedApplication = normalizeApplication(application);
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT);
        String emitterId = UUID.randomUUID().toString();

        Map<String, SseEmitter> userEmitters = emitterMap.computeIfAbsent(normalizedApplication, key -> new ConcurrentHashMap<>())
                .computeIfAbsent(userId, key -> new ConcurrentHashMap<>());
        userEmitters.put(emitterId, emitter);
        int currentCount = incrementConnectionCount(normalizedApplication);
        refreshOnlineNodeRegistration(normalizedApplication, currentCount);
        if (userEmitters.size() == 1) {
            registerUserNodeRoute(normalizedApplication, userId);
        }

        emitter.onCompletion(() -> remove(normalizedApplication, userId, emitterId));
        emitter.onTimeout(() -> remove(normalizedApplication, userId, emitterId));
        emitter.onError(e -> remove(normalizedApplication, userId, emitterId));

        try {
            emitter.send(SseEmitter.event().comment("connected"));
            log.info("Register notice emitter success, application={}, userId={}, emitterId={}, nodeId={}, userEmitterCount={}, appConnectionCount={}",
                    normalizedApplication, userId, emitterId, getNodeId(), userEmitters.size(), currentCount);
        } catch (IOException e) {
            log.debug("Register notice emitter failed, userId={}, application={}", userId, normalizedApplication, e);
            remove(normalizedApplication, userId, emitterId);
            emitter.completeWithError(e);
        }
        return emitter;
    }

    public SseEmitter registerPda(String userId) {
        return register(userId, SysTypeEnum.PDA.getCode());
    }

    public SseEmitter registerPc(String userId) {
        return register(userId, SysTypeEnum.PC.getCode());
    }

    public boolean sendCompensationNotice(SseEmitter emitter, String application, String userId, MessageDTO.NoticeDTO noticeDTO) {
        if (emitter == null || noticeDTO == null) {
            return false;
        }
        String normalizedApplication = normalizeApplication(application);
        try {
            emitter.send(buildNoticeEvent(normalizedApplication, noticeDTO));
            log.info("Send compensation notice success, application={}, userId={}, noticeId={}, nodeId={}",
                    normalizedApplication, userId, noticeDTO.getId(), getNodeId());
            return true;
        } catch (Exception e) {
            log.warn("Send compensation notice failed, application={}, userId={}, noticeId={}, nodeId={}",
                    normalizedApplication, userId, noticeDTO.getId(), getNodeId(), e);
            emitter.completeWithError(e);
            return false;
        }
    }

    public String getNodeId() {
        return applicationName + ":" + serverPort + ":" + runtimeId;
    }

    public String getNodeTopic(String application) {
        return buildNodeTopic(application, getNodeId());
    }

    public Set<String> listOnlineNodeIds(String application) {
        return new HashSet<>(getOnlineNodeMap(application).readAllKeySet());
    }

    public Set<String> listOnlineNodeIdsByUser(String application, String userId) {
        if (StringUtils.isBlank(userId)) {
            return Collections.emptySet();
        }
        Set<String> userNodeIds = getUserNodeIds(application, userId);
        if (userNodeIds.isEmpty()) {
            return Collections.emptySet();
        }
        Set<String> onlineNodeIds = listOnlineNodeIds(application);
        userNodeIds.retainAll(onlineNodeIds);
        return userNodeIds;
    }

    public Set<String> listRegisteredUserIds(String application) {
        return new LinkedHashSet<>(getOnlineUserNodeMap(application).keySet());
    }

    public Map<String, Set<String>> groupUserIdsByOnlineNode(String application, Collection<String> userIds) {
        String normalizedApplication = normalizeApplication(application);
        Map<String, Set<String>> result = new LinkedHashMap<>();
        if (userIds == null || userIds.isEmpty()) {
            return result;
        }
        for (String userId : new LinkedHashSet<>(userIds)) {
            if (StringUtils.isBlank(userId)) {
                continue;
            }
            Set<String> nodeIds = listOnlineNodeIdsByUser(normalizedApplication, userId);
            if (nodeIds.isEmpty()) {
                log.info("Skip notice user route because user has no online SSE node, application={}, userId={}",
                        normalizedApplication, userId);
                continue;
            }
            for (String nodeId : nodeIds) {
                result.computeIfAbsent(nodeId, key -> new LinkedHashSet<>()).add(userId);
            }
        }
        return result;
    }

    public String buildNodeTopic(String application, String nodeId) {
        return NODE_TOPIC_PREFIX + normalizeApplication(application) + ":node:" + nodeId;
    }

    public Set<String> getOnlineUserIds(String application) {
        Set<String> result = new HashSet<>();
        getApplicationEmitterMap(application).forEach((userId, emitters) -> {
            if (emitters != null && !emitters.isEmpty()) {
                result.add(userId);
            }
        });
        return result;
    }

    public Set<String> pushNoticeToUsers(String application, Collection<String> userIds, MessageDTO.NoticeDTO noticeDTO) {
        Set<String> successUserIds = new HashSet<>();
        if (noticeDTO == null || userIds == null || userIds.isEmpty()) {
            return successUserIds;
        }
        String normalizedApplication = normalizeApplication(application);
        for (String userId : new HashSet<>(userIds)) {
            Map<String, SseEmitter> userEmitters = getApplicationEmitterMap(normalizedApplication)
                    .getOrDefault(userId, Collections.emptyMap());
            if (userEmitters.isEmpty()) {
                continue;
            }
            boolean pushed = false;
            for (Map.Entry<String, SseEmitter> entry : userEmitters.entrySet()) {
                try {
                    entry.getValue().send(buildNoticeEvent(normalizedApplication, noticeDTO));
                    pushed = true;
                } catch (Exception e) {
                    log.debug("Push notice failed, userId={}, emitterId={}, application={}",
                            userId, entry.getKey(), normalizedApplication, e);
                    remove(normalizedApplication, userId, entry.getKey());
                }
            }
            if (pushed) {
                successUserIds.add(userId);
            }
        }
        log.info("Push notice to local users finished, application={}, noticeId={}, requestUsers={}, successUsers={}, nodeId={}",
                normalizedApplication,
                noticeDTO.getId(),
                new LinkedHashSet<>(userIds).size(),
                successUserIds.size(),
                getNodeId());
        return successUserIds;
    }

    public Set<String> pushLocalNotice(String application, MessageDTO.NoticeDTO noticeDTO) {
        return pushNoticeToUsers(application, getOnlineUserIds(application), noticeDTO);
    }

    public void heartbeat() {
        for (String application : new HashSet<>(emitterMap.keySet())) {
            int currentCount = getCurrentConnectionCount(application);
            if (currentCount > 0) {
                refreshOnlineNodeRegistration(application, currentCount);
            }
            getApplicationEmitterMap(application).forEach((userId, emitters) -> {
                if (emitters == null || emitters.isEmpty()) {
                    return;
                }
                for (Map.Entry<String, SseEmitter> entry : emitters.entrySet()) {
                    try {
                        entry.getValue().send(SseEmitter.event().comment("heartbeat"));
                    } catch (Exception e) {
                        log.debug("Heartbeat failed, userId={}, emitterId={}, application={}",
                                userId, entry.getKey(), application, e);
                        remove(application, userId, entry.getKey());
                    }
                }
            });
        }
    }

    public Set<String> getSupportedApplications() {
        Set<String> applications = new LinkedHashSet<>();
        applications.add(SysTypeEnum.PDA.getCode());
        applications.add(SysTypeEnum.PC.getCode());
        return applications;
    }

    private void remove(String application, String userId, String emitterId) {
        String normalizedApplication = normalizeApplication(application);
        Map<String, Map<String, SseEmitter>> applicationEmitterMap = emitterMap.get(normalizedApplication);
        if (applicationEmitterMap == null) {
            return;
        }
        Map<String, SseEmitter> userEmitters = applicationEmitterMap.get(userId);
        if (userEmitters == null) {
            return;
        }
        SseEmitter removedEmitter = userEmitters.remove(emitterId);
        if (removedEmitter == null) {
            return;
        }
        if (userEmitters.isEmpty()) {
            applicationEmitterMap.remove(userId);
            unregisterUserNodeRoute(normalizedApplication, userId);
        }
        int currentCount = decrementConnectionCount(normalizedApplication);
        if (currentCount > 0) {
            refreshOnlineNodeRegistration(normalizedApplication, currentCount);
            log.info("Remove notice emitter success, application={}, userId={}, emitterId={}, nodeId={}, appConnectionCount={}",
                    normalizedApplication, userId, emitterId, getNodeId(), currentCount);
            return;
        }
        emitterMap.remove(normalizedApplication);
        localConnectionCountMap.remove(normalizedApplication);
        getOnlineNodeMap(normalizedApplication).remove(getNodeId());
        log.info("Notice application offline on current node, application={}, nodeId={}", normalizedApplication, getNodeId());
    }

    private void refreshOnlineNodeRegistration(String application, int currentCount) {
        if (currentCount <= 0) {
            getOnlineNodeMap(application).remove(getNodeId());
            return;
        }
        getOnlineNodeMap(application).fastPut(getNodeId(), currentCount, ONLINE_TTL_SECONDS, TimeUnit.SECONDS);
    }

    private RMapCache<String, Integer> getOnlineNodeMap(String application) {
        return redissonClient.getMapCache(ONLINE_NODE_MAP_KEY_PREFIX + normalizeApplication(application));
    }

    private RMap<String, String> getOnlineUserNodeMap(String application) {
        return redissonClient.getMap(ONLINE_USER_NODE_MAP_KEY_PREFIX + normalizeApplication(application));
    }

    private void registerUserNodeRoute(String application, String userId) {
        updateUserNodeRoute(application, userId, true);
    }

    private void unregisterUserNodeRoute(String application, String userId) {
        updateUserNodeRoute(application, userId, false);
    }

    private void updateUserNodeRoute(String application, String userId, boolean addCurrentNode) {
        if (StringUtils.isBlank(userId)) {
            return;
        }
        String normalizedApplication = normalizeApplication(application);
        String nodeId = getNodeId();
        RLock lock = redissonClient.getLock(USER_ROUTE_LOCK_KEY_PREFIX + normalizedApplication + ":" + userId);
        lock.lock(5, TimeUnit.SECONDS);
        try {
            Set<String> userNodeIds = getUserNodeIds(normalizedApplication, userId);
            if (addCurrentNode) {
                userNodeIds.add(nodeId);
            } else {
                userNodeIds.remove(nodeId);
            }
            RMap<String, String> onlineUserNodeMap = getOnlineUserNodeMap(normalizedApplication);
            if (userNodeIds.isEmpty()) {
                onlineUserNodeMap.remove(userId);
            } else {
                onlineUserNodeMap.put(userId, String.join(",", userNodeIds));
            }
            log.info("Refresh notice user route, application={}, userId={}, nodeId={}, onlineNodes={}",
                    normalizedApplication, userId, nodeId, userNodeIds);
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    private Set<String> getUserNodeIds(String application, String userId) {
        String nodesValue = getOnlineUserNodeMap(application).get(userId);
        if (StringUtils.isBlank(nodesValue)) {
            return new LinkedHashSet<>();
        }
        return Arrays.stream(StringUtils.split(nodesValue, ','))
                .filter(StringUtils::isNotBlank)
                .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));
    }

    private Map<String, Map<String, SseEmitter>> getApplicationEmitterMap(String application) {
        return emitterMap.computeIfAbsent(normalizeApplication(application), key -> new ConcurrentHashMap<>());
    }

    private int incrementConnectionCount(String application) {
        return localConnectionCountMap
                .computeIfAbsent(normalizeApplication(application), key -> new AtomicInteger(0))
                .incrementAndGet();
    }

    private int decrementConnectionCount(String application) {
        AtomicInteger counter = localConnectionCountMap.get(normalizeApplication(application));
        if (counter == null) {
            return 0;
        }
        return counter.updateAndGet(value -> value > 0 ? value - 1 : 0);
    }

    private int getCurrentConnectionCount(String application) {
        AtomicInteger counter = localConnectionCountMap.get(normalizeApplication(application));
        return counter == null ? 0 : counter.get();
    }

    private SseEmitter.SseEventBuilder buildNoticeEvent(String application, MessageDTO.NoticeDTO noticeDTO) {
        SseEmitter.SseEventBuilder event = SseEmitter.event().data(noticeDTO, MediaType.APPLICATION_JSON);
        if (StringUtils.equals(SysTypeEnum.PDA.getCode(), normalizeApplication(application))) {
            event.name("pdaNotice");
        }
        return event;
    }

    private String normalizeApplication(String application) {
        if (StringUtils.equalsIgnoreCase(SysTypeEnum.PDA.getCode(), application)) {
            return SysTypeEnum.PDA.getCode();
        }
        if (StringUtils.equalsIgnoreCase(SysTypeEnum.PC.getCode(), application)) {
            return SysTypeEnum.PC.getCode();
        }
        throw new IllegalArgumentException("Unsupported notice application: " + application);
    }
}
