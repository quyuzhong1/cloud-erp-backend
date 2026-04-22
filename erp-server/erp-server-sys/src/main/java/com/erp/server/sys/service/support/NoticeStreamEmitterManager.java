package com.erp.server.sys.service.support;

import com.common.business.constant.RedisCacheConstants;
import com.erp.model.sys.dto.MessageDTO;
import com.erp.model.sys.enums.SysTypeEnum;
import lombok.Getter;
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

import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Slf4j
@Component
public class NoticeStreamEmitterManager {

    private static final long SSE_TIMEOUT = 1800_000L;
    private static final long ONLINE_TTL_SECONDS = 120L;

    /**
     * application -> userId -> userEmitterGroup
     */
    private final Map<String, Map<String, UserEmitterGroup>> emitterMap = new ConcurrentHashMap<>();
    private final Map<String, AtomicInteger> localConnectionCountMap = new ConcurrentHashMap<>();
    private final Map<String, AtomicLong> replaceCountMap = new ConcurrentHashMap<>();

    @Resource
    private RedissonClient redissonClient;

    @Value("${spring.application.name:erp-server-sys}")
    private String applicationName;

    @Value("${server.port:0000}")
    private String serverPort;

    /**
     * 只限制同一用户在同一节点、同一端的 SSE 活跃连接数，防止前端异常重连把旧连接无限堆积。
     * 这不是登录设备限制。
     */
    @Value("${sys.notice.sse.max-user-connections:10}")
    private int maxUserConnections;

    /**
     * 可选的强制轮换时间，单位毫秒。
     * 默认 0 表示关闭该能力；大于 0 时，连接活到指定时长后会在心跳任务中被主动关闭，
     * 由客户端自行重连，从而控制单条连接在堆中的最长存活时间。
     */
    @Value("${sys.notice.sse.force-close-after-millis:300000}")
    private long forceCloseAfterMillis;

    private final String runtimeId = UUID.randomUUID().toString().replace("-", "");

    public SseEmitter register(String userId, String application) {
        String normalizedApplication = normalizeApplication(application);
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT);
        String emitterId = UUID.randomUUID().toString();
        EmitterHolder newHolder = new EmitterHolder(emitterId, emitter, System.currentTimeMillis());

        int currentConnectionCount;
        RegisterResult registerResult = new RegisterResult();
        getApplicationEmitterMap(normalizedApplication).compute(userId, (key, userEmitterGroup) -> {
            UserEmitterGroup currentGroup = userEmitterGroup == null ? new UserEmitterGroup() : userEmitterGroup;
            synchronized (currentGroup) {
                registerResult.setFirstConnectionForUser(currentGroup.getEmitterMap().isEmpty());
                currentGroup.getEmitterMap().put(emitterId, newHolder);
                registerResult.setEvictedHolders(trimOverflowEmitters(currentGroup.getEmitterMap(), emitterId));
                registerResult.setCurrentUserConnections(currentGroup.getEmitterMap().size());
            }
            return currentGroup;
        });

        currentConnectionCount = incrementConnectionCount(normalizedApplication);
        if (registerResult.isFirstConnectionForUser()) {
            registerUserNodeRoute(normalizedApplication, userId);
        }
        if (!registerResult.getEvictedHolders().isEmpty()) {
            currentConnectionCount = adjustConnectionCountAfterEviction(normalizedApplication, registerResult.getEvictedHolders().size());
        }
        refreshOnlineNodeRegistration(normalizedApplication, currentConnectionCount);

        emitter.onCompletion(() -> remove(normalizedApplication, userId, emitterId));
        emitter.onTimeout(() -> remove(normalizedApplication, userId, emitterId));
        emitter.onError(e -> remove(normalizedApplication, userId, emitterId));

        try {
            emitter.send(buildMetaEvent("connected", normalizedApplication, userId));
            log.info("Register notice emitter success, application={}, userId={}, emitterId={}, userConnectionCount={}, evictedConnectionCount={}, nodeId={}, appConnectionCount={}",
                    normalizedApplication, userId, emitterId, registerResult.getCurrentUserConnections(), registerResult.getEvictedHolders().size(), getNodeId(), currentConnectionCount);
        } catch (IOException e) {
            log.debug("Register notice emitter failed, userId={}, application={}", userId, normalizedApplication, e);
            remove(normalizedApplication, userId, emitterId);
            emitter.completeWithError(e);
        }

        for (EmitterHolder evictedHolder : registerResult.getEvictedHolders()) {
            completeQuietly(evictedHolder.getEmitter(), normalizedApplication, userId, evictedHolder.getEmitterId(), "overflow");
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
        return RedisCacheConstants.buildSysNoticeSseNodeTopic(normalizeApplication(application), nodeId);
    }

    public Set<String> getOnlineUserIds(String application) {
        return getApplicationEmitterMap(application).entrySet().stream()
                .filter(entry -> entry.getValue() != null && !entry.getValue().getEmitterMap().isEmpty())
                .map(Map.Entry::getKey)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    public Set<String> pushNoticeToUsers(String application, Collection<String> userIds, MessageDTO.NoticeDTO noticeDTO) {
        Set<String> successUserIds = new HashSet<>();
        if (noticeDTO == null || userIds == null || userIds.isEmpty()) {
            return successUserIds;
        }
        String normalizedApplication = normalizeApplication(application);
        for (String userId : new LinkedHashSet<>(userIds)) {
            UserEmitterGroup userEmitterGroup = getApplicationEmitterMap(normalizedApplication).get(userId);
            if (userEmitterGroup == null || userEmitterGroup.getEmitterMap().isEmpty()) {
                continue;
            }
            List<EmitterHolder> emitterHolders = copyEmitterHolders(userEmitterGroup);
            boolean userPushed = false;
            for (EmitterHolder emitterHolder : emitterHolders) {
                boolean pushed = false;
                try {
                    emitterHolder.getEmitter().send(buildNoticeEvent(normalizedApplication, noticeDTO));
                    pushed = true;
                } catch (Exception e) {
                    log.debug("Push notice failed, userId={}, emitterId={}, application={}",
                            userId, emitterHolder.getEmitterId(), normalizedApplication, e);
                    remove(normalizedApplication, userId, emitterHolder.getEmitterId());
                }
                if (pushed) {
                    userPushed = true;
                }
            }
            if (userPushed) {
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
        long now = System.currentTimeMillis();
        for (String application : new HashSet<>(emitterMap.keySet())) {
            int currentCount = getCurrentConnectionCount(application);
            if (currentCount > 0) {
                refreshOnlineNodeRegistration(application, currentCount);
            }
            Map<String, UserEmitterGroup> applicationEmitterMap = getApplicationEmitterMap(application);
            for (Map.Entry<String, UserEmitterGroup> entry : new ArrayList<>(applicationEmitterMap.entrySet())) {
                String userId = entry.getKey();
                List<EmitterHolder> emitterHolders = copyEmitterHolders(entry.getValue());
                for (EmitterHolder emitterHolder : emitterHolders) {
                    if (shouldForceClose(emitterHolder, now)) {
                        log.info("Force close expired notice emitter, application={}, userId={}, emitterId={}, connectedAt={}, nodeId={}",
                                application, userId, emitterHolder.getEmitterId(), emitterHolder.getConnectedAt(), getNodeId());
                        remove(application, userId, emitterHolder.getEmitterId());
                        completeQuietly(emitterHolder.getEmitter(), application, userId, emitterHolder.getEmitterId(), "forceClose");
                        continue;
                    }
                    try {
                        emitterHolder.getEmitter().send(buildMetaEvent("heartbeat", application, userId));
                    } catch (Exception e) {
                        log.debug("Heartbeat failed, userId={}, emitterId={}, application={}",
                                userId, emitterHolder.getEmitterId(), application, e);
                        remove(application, userId, emitterHolder.getEmitterId());
                    }
                }
            }
        }
    }

    public Set<String> getSupportedApplications() {
        Set<String> applications = new LinkedHashSet<>();
        applications.add(SysTypeEnum.PDA.getCode());
        applications.add(SysTypeEnum.PC.getCode());
        return applications;
    }

    public MessageDTO.StreamStatsDTO getStreamStats() {
        MessageDTO.StreamStatsDTO statsDTO = new MessageDTO.StreamStatsDTO();
        statsDTO.setNodeId(getNodeId());
        statsDTO.setSnapshotTime(LocalDateTime.now());
        statsDTO.setPc(buildAppStats(SysTypeEnum.PC.getCode()));
        statsDTO.setPda(buildAppStats(SysTypeEnum.PDA.getCode()));
        return statsDTO;
    }

    private void remove(String application, String userId, String emitterId) {
        String normalizedApplication = normalizeApplication(application);
        Map<String, UserEmitterGroup> applicationEmitterMap = emitterMap.get(normalizedApplication);
        if (applicationEmitterMap == null) {
            return;
        }

        RemoveResult removeResult = new RemoveResult();
        applicationEmitterMap.computeIfPresent(userId, (key, userEmitterGroup) -> {
            synchronized (userEmitterGroup) {
                boolean removed = userEmitterGroup.getEmitterMap().remove(emitterId) != null;
                if (!removed) {
                    removeResult.setRemoved(false);
                    return userEmitterGroup;
                }
                removeResult.setRemoved(true);
                removeResult.setCurrentUserConnections(userEmitterGroup.getEmitterMap().size());
                if (userEmitterGroup.getEmitterMap().isEmpty()) {
                    removeResult.setUserOffline(true);
                    return null;
                }
                return userEmitterGroup;
            }
        });

        if (!removeResult.isRemoved()) {
            return;
        }
        if (removeResult.isUserOffline()) {
            unregisterUserNodeRoute(normalizedApplication, userId);
        }
        int currentCount = decrementConnectionCount(normalizedApplication);
        if (currentCount > 0) {
            refreshOnlineNodeRegistration(normalizedApplication, currentCount);
            log.info("Remove notice emitter success, application={}, userId={}, emitterId={}, userConnectionCount={}, nodeId={}, appConnectionCount={}",
                    normalizedApplication, userId, emitterId, removeResult.getCurrentUserConnections(), getNodeId(), currentCount);
            return;
        }
        emitterMap.remove(normalizedApplication, applicationEmitterMap);
        localConnectionCountMap.remove(normalizedApplication);
        replaceCountMap.remove(normalizedApplication);
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
        return redissonClient.getMapCache(RedisCacheConstants.buildSysNoticeSseOnlineNodeKey(normalizeApplication(application)));
    }

    private RMap<String, String> getOnlineUserNodeMap(String application) {
        return redissonClient.getMap(RedisCacheConstants.buildSysNoticeSseUserNodeKey(normalizeApplication(application)));
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
        RLock lock = redissonClient.getLock(RedisCacheConstants.buildSysNoticeSseUserLockKey(normalizedApplication, userId));
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
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private Map<String, UserEmitterGroup> getApplicationEmitterMap(String application) {
        return emitterMap.computeIfAbsent(normalizeApplication(application), key -> new ConcurrentHashMap<>());
    }

    private int incrementConnectionCount(String application) {
        return localConnectionCountMap
                .computeIfAbsent(normalizeApplication(application), key -> new AtomicInteger(0))
                .incrementAndGet();
    }

    private int adjustConnectionCountAfterEviction(String application, int evictedCount) {
        if (evictedCount <= 0) {
            return getCurrentConnectionCount(application);
        }
        incrementReplaceCount(normalizeApplication(application), evictedCount);
        AtomicInteger counter = localConnectionCountMap.get(normalizeApplication(application));
        if (counter == null) {
            return 0;
        }
        return counter.updateAndGet(value -> Math.max(0, value - evictedCount));
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

    private long incrementReplaceCount(String application, int delta) {
        return replaceCountMap
                .computeIfAbsent(normalizeApplication(application), key -> new AtomicLong(0))
                .addAndGet(Math.max(delta, 0));
    }

    private long getReplaceCount(String application) {
        AtomicLong counter = replaceCountMap.get(normalizeApplication(application));
        return counter == null ? 0L : counter.get();
    }

    private MessageDTO.StreamAppStatsDTO buildAppStats(String application) {
        String normalizedApplication = normalizeApplication(application);
        MessageDTO.StreamAppStatsDTO appStatsDTO = new MessageDTO.StreamAppStatsDTO();
        appStatsDTO.setApplication(normalizedApplication);
        appStatsDTO.setLocalUserCount(getApplicationEmitterMap(normalizedApplication).size());
        appStatsDTO.setLocalConnectionCount(getCurrentConnectionCount(normalizedApplication));
        appStatsDTO.setReplaceCount(getReplaceCount(normalizedApplication));
        Set<String> onlineNodeIds = listOnlineNodeIds(normalizedApplication);
        appStatsDTO.setRedisOnlineNodeIds(new LinkedHashSet<>(onlineNodeIds));
        appStatsDTO.setRedisOnlineNodeCount(onlineNodeIds.size());
        appStatsDTO.setRedisRegisteredUserCount(listRegisteredUserIds(normalizedApplication).size());
        return appStatsDTO;
    }

    private SseEmitter.SseEventBuilder buildNoticeEvent(String application, MessageDTO.NoticeDTO noticeDTO) {
        SseEmitter.SseEventBuilder event = SseEmitter.event().data(noticeDTO, MediaType.APPLICATION_JSON);
        if (StringUtils.equals(SysTypeEnum.PDA.getCode(), normalizeApplication(application))) {
            event.name("pdaNotice");
        } else {
            event.name("message");
        }
        return event;
    }

    private SseEmitter.SseEventBuilder buildMetaEvent(String eventName, String application, String userId) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("event", eventName);
        payload.put("application", normalizeApplication(application));
        payload.put("userId", userId);
        payload.put("nodeId", getNodeId());
        payload.put("time", System.currentTimeMillis());
        return SseEmitter.event()
                .name(eventName)
                .data(payload, MediaType.APPLICATION_JSON);
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

    private boolean shouldForceClose(EmitterHolder emitterHolder, long now) {
        return forceCloseAfterMillis > 0
                && emitterHolder != null
                && now - emitterHolder.getConnectedAt() >= forceCloseAfterMillis;
    }

    private List<EmitterHolder> trimOverflowEmitters(Map<String, EmitterHolder> userEmitterMap, String keepEmitterId) {
        int safeMaxUserConnections = Math.max(1, maxUserConnections);
        if (userEmitterMap.size() <= safeMaxUserConnections) {
            return Collections.emptyList();
        }
        List<Map.Entry<String, EmitterHolder>> sortedEntries = new ArrayList<>(userEmitterMap.entrySet());
        sortedEntries.sort(Comparator.comparingLong(entry -> entry.getValue().getConnectedAt()));
        List<EmitterHolder> evictedHolders = new ArrayList<>();
        for (Map.Entry<String, EmitterHolder> entry : sortedEntries) {
            if (userEmitterMap.size() <= safeMaxUserConnections) {
                break;
            }
            if (StringUtils.equals(entry.getKey(), keepEmitterId) && userEmitterMap.size() > 1) {
                continue;
            }
            EmitterHolder removed = userEmitterMap.remove(entry.getKey());
            if (removed != null) {
                evictedHolders.add(removed);
            }
        }
        return evictedHolders;
    }

    private List<EmitterHolder> copyEmitterHolders(UserEmitterGroup userEmitterGroup) {
        if (userEmitterGroup == null || userEmitterGroup.getEmitterMap().isEmpty()) {
            return Collections.emptyList();
        }
        synchronized (userEmitterGroup) {
            return new ArrayList<>(userEmitterGroup.getEmitterMap().values());
        }
    }

    @PreDestroy
    public void destroy() {
        for (String application : new HashSet<>(emitterMap.keySet())) {
            Map<String, UserEmitterGroup> applicationEmitterMap = emitterMap.getOrDefault(application, Collections.emptyMap());
            for (Map.Entry<String, UserEmitterGroup> userEntry : applicationEmitterMap.entrySet()) {
                for (EmitterHolder emitterHolder : copyEmitterHolders(userEntry.getValue())) {
                    completeQuietly(emitterHolder.getEmitter(), application, userEntry.getKey(), emitterHolder.getEmitterId(), "shutdown");
                }
                unregisterUserNodeRoute(application, userEntry.getKey());
            }
            emitterMap.remove(application);
            localConnectionCountMap.remove(application);
            replaceCountMap.remove(application);
            getOnlineNodeMap(application).remove(getNodeId());
        }
        log.info("Destroy notice stream emitter manager completed, nodeId={}", getNodeId());
    }

    private void completeQuietly(SseEmitter emitter, String application, String userId, String emitterId, String reason) {
        if (emitter == null) {
            return;
        }
        try {
            emitter.complete();
        } catch (Exception e) {
            log.debug("Complete notice emitter ignored, application={}, userId={}, emitterId={}, reason={}",
                    application, userId, emitterId, reason, e);
        }
    }

    @Getter
    private static final class EmitterHolder {
        private final String emitterId;
        private final SseEmitter emitter;
        private final long connectedAt;

        private EmitterHolder(String emitterId, SseEmitter emitter, long connectedAt) {
            this.emitterId = emitterId;
            this.emitter = emitter;
            this.connectedAt = connectedAt;
        }
    }

    @Getter
    private static final class UserEmitterGroup {
        private final Map<String, EmitterHolder> emitterMap = new ConcurrentHashMap<>();
    }

    @Getter
    private static final class RegisterResult {
        private boolean firstConnectionForUser;
        private int currentUserConnections;
        private List<EmitterHolder> evictedHolders = Collections.emptyList();

        private void setFirstConnectionForUser(boolean firstConnectionForUser) {
            this.firstConnectionForUser = firstConnectionForUser;
        }

        private void setCurrentUserConnections(int currentUserConnections) {
            this.currentUserConnections = currentUserConnections;
        }

        private void setEvictedHolders(List<EmitterHolder> evictedHolders) {
            this.evictedHolders = evictedHolders;
        }
    }

    @Getter
    private static final class RemoveResult {
        private boolean removed;
        private boolean userOffline;
        private int currentUserConnections;

        private void setRemoved(boolean removed) {
            this.removed = removed;
        }

        private void setUserOffline(boolean userOffline) {
            this.userOffline = userOffline;
        }

        private void setCurrentUserConnections(int currentUserConnections) {
            this.currentUserConnections = currentUserConnections;
        }
    }
}
