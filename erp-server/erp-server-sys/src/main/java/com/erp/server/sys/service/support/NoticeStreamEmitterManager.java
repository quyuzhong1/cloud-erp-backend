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
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
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
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Slf4j
@Component
public class NoticeStreamEmitterManager {

    private static final long SSE_TIMEOUT = 1800_000L;
    private static final long ONLINE_TTL_SECONDS = 120L;
    private static final int DEFAULT_STREAM_STATS_USER_LIMIT = 100;
    private static final int MAX_STREAM_STATS_USER_LIMIT = 500;
    private static final int DEFAULT_STREAM_STATS_CONNECTION_LIMIT = 20;
    private static final int MAX_STREAM_STATS_CONNECTION_LIMIT = 100;
    private static final int DEFAULT_HEARTBEAT_BATCH_SIZE = 200;
    private static final long DEFAULT_HEARTBEAT_WAIT_MILLIS = 20_000L;
    private static final int STREAM_MONITOR_WARN_TOTAL_CONNECTIONS = 1000;
    private static final int STREAM_MONITOR_CRITICAL_TOTAL_CONNECTIONS = 3000;
    private static final double STREAM_MONITOR_WARN_HEAP_RATIO = 0.70D;
    private static final double STREAM_MONITOR_CRITICAL_HEAP_RATIO = 0.85D;
    private static final double STREAM_MONITOR_WARN_HEARTBEAT_RATIO = 0.80D;
    private static final long STREAM_MONITOR_WARN_SKIP_COUNT = 1L;
    private static final long STREAM_MONITOR_CRITICAL_SKIP_COUNT = 5L;
    private static final long STREAM_MONITOR_WARN_REPLACE_COUNT = 100L;

    /**
     * application -> userId -> userEmitterGroup
     */
    private final Map<String, Map<String, UserEmitterGroup>> emitterMap = new ConcurrentHashMap<>();
    private final Map<String, AtomicInteger> localConnectionCountMap = new ConcurrentHashMap<>();
    private final Map<String, AtomicLong> replaceCountMap = new ConcurrentHashMap<>();

    @Resource
    private RedissonClient redissonClient;

    @Resource
    @Qualifier("noticeHeartbeatExecutor")
    private Executor noticeHeartbeatExecutor;

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
    @Value("${sys.notice.sse.force-close-after-millis:0}")
    private long forceCloseAfterMillis;

    @Value("${sys.notice.sse.heartbeat-batch-size:200}")
    private int heartbeatBatchSize;

    @Value("${sys.notice.sse.heartbeat-wait-millis:20000}")
    private long heartbeatWaitMillis;

    private final String runtimeId = UUID.randomUUID().toString().replace("-", "");
    private final AtomicBoolean heartbeatRunning = new AtomicBoolean(false);
    private final AtomicLong heartbeatSkipCount = new AtomicLong(0);
    private final AtomicReference<HeartbeatMonitorSnapshot> lastHeartbeatSnapshotRef =
            new AtomicReference<>(HeartbeatMonitorSnapshot.empty());

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
        if (!heartbeatRunning.compareAndSet(false, true)) {
            long skipped = heartbeatSkipCount.incrementAndGet();
            log.warn("Skip notice heartbeat because previous heartbeat still running, nodeId={}, skippedCount={}", getNodeId(), skipped);
            return;
        }
        try {
            noticeHeartbeatExecutor.execute(() -> {
                long start = System.currentTimeMillis();
                HeartbeatSummary summary = new HeartbeatSummary();
                try {
                    doHeartbeat(summary, start);
                } catch (Exception e) {
                    log.error("Execute notice heartbeat error, nodeId={}", getNodeId(), e);
                } finally {
                    logHeartbeatSummary(summary, start);
                    heartbeatRunning.set(false);
                }
            });
        } catch (RejectedExecutionException e) {
            heartbeatRunning.set(false);
            log.error("Submit notice heartbeat task rejected, nodeId={}", getNodeId(), e);
        }
    }

    public Set<String> getSupportedApplications() {
        Set<String> applications = new LinkedHashSet<>();
        applications.add(SysTypeEnum.PDA.getCode());
        applications.add(SysTypeEnum.PC.getCode());
        return applications;
    }

    public MessageDTO.StreamStatsDTO getStreamStats() {
        return getStreamStats(true, DEFAULT_STREAM_STATS_USER_LIMIT, DEFAULT_STREAM_STATS_CONNECTION_LIMIT);
    }

    public MessageDTO.StreamStatsDTO getStreamStats(Boolean detail, Integer userLimit, Integer connectionLimitPerUser) {
        boolean detailEnabled = Boolean.TRUE.equals(detail);
        int safeUserLimit = sanitizeLimit(userLimit, DEFAULT_STREAM_STATS_USER_LIMIT, MAX_STREAM_STATS_USER_LIMIT);
        int safeConnectionLimit = sanitizeLimit(connectionLimitPerUser, DEFAULT_STREAM_STATS_CONNECTION_LIMIT, MAX_STREAM_STATS_CONNECTION_LIMIT);
        LocalDateTime snapshotTime = LocalDateTime.now();
        MessageDTO.StreamStatsDTO statsDTO = new MessageDTO.StreamStatsDTO();
        statsDTO.setNodeId(getNodeId());
        statsDTO.setSnapshotTime(snapshotTime);
        statsDTO.setDetailEnabled(detailEnabled);
        statsDTO.setUserLimit(safeUserLimit);
        statsDTO.setConnectionLimitPerUser(safeConnectionLimit);
        statsDTO.setPc(buildAppStats(SysTypeEnum.PC.getCode(), detailEnabled, safeUserLimit, safeConnectionLimit, snapshotTime));
        statsDTO.setPda(buildAppStats(SysTypeEnum.PDA.getCode(), detailEnabled, safeUserLimit, safeConnectionLimit, snapshotTime));
        return statsDTO;
    }

    public MessageDTO.StreamMonitorDTO getStreamMonitor(Boolean detail, Integer userLimit, Integer connectionLimitPerUser) {
        MessageDTO.StreamStatsDTO streamStatsDTO = getStreamStats(detail, userLimit, connectionLimitPerUser);
        HeartbeatMonitorSnapshot heartbeatSnapshot = lastHeartbeatSnapshotRef.get();
        MessageDTO.StreamMonitorDTO monitorDTO = new MessageDTO.StreamMonitorDTO();
        monitorDTO.setNodeId(getNodeId());
        monitorDTO.setSnapshotTime(LocalDateTime.now());
        monitorDTO.setHeartbeatRunning(heartbeatRunning.get());
        monitorDTO.setHeartbeatSkipCount(heartbeatSkipCount.get());
        monitorDTO.setLastHeartbeat(buildHeartbeatStats(heartbeatSnapshot));
        monitorDTO.setStats(streamStatsDTO);
        List<MessageDTO.StreamMonitorIssueDTO> issues = buildMonitorIssues(streamStatsDTO, heartbeatSnapshot);
        monitorDTO.setIssues(issues);
        monitorDTO.setIssueCount(issues.size());
        monitorDTO.setRiskLevel(resolveRiskLevel(issues));
        monitorDTO.setSuggestions(buildMonitorSuggestions(issues));
        return monitorDTO;
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

    private MessageDTO.StreamAppStatsDTO buildAppStats(String application,
                                                       boolean detailEnabled,
                                                       int userLimit,
                                                       int connectionLimitPerUser,
                                                       LocalDateTime snapshotTime) {
        String normalizedApplication = normalizeApplication(application);
        Map<String, UserEmitterGroup> applicationEmitterMap = getApplicationEmitterMap(normalizedApplication);
        List<MessageDTO.StreamUserStatsDTO> localUsers = buildLocalUserStats(normalizedApplication, applicationEmitterMap, detailEnabled, userLimit, connectionLimitPerUser, snapshotTime);
        Set<String> redisRegisteredUserIds = listRegisteredUserIds(normalizedApplication);
        MessageDTO.StreamAppStatsDTO appStatsDTO = new MessageDTO.StreamAppStatsDTO();
        appStatsDTO.setApplication(normalizedApplication);
        appStatsDTO.setLocalUserCount((int) applicationEmitterMap.entrySet().stream()
                .filter(entry -> entry.getValue() != null && !entry.getValue().getEmitterMap().isEmpty())
                .count());
        appStatsDTO.setLocalConnectionCount(getCurrentConnectionCount(normalizedApplication));
        appStatsDTO.setReplaceCount(getReplaceCount(normalizedApplication));
        appStatsDTO.setMaxUserConnections(maxUserConnections);
        appStatsDTO.setForceCloseAfterMillis(forceCloseAfterMillis);
        Set<String> onlineNodeIds = listOnlineNodeIds(normalizedApplication);
        appStatsDTO.setRedisOnlineNodeIds(new LinkedHashSet<>(onlineNodeIds));
        appStatsDTO.setRedisOnlineNodeCount(onlineNodeIds.size());
        appStatsDTO.setRedisRegisteredUserCount(redisRegisteredUserIds.size());
        appStatsDTO.setLocalUsers(localUsers);
        appStatsDTO.setLocalUserTruncated(detailEnabled && appStatsDTO.getLocalUserCount() > userLimit);
        appStatsDTO.setLocalConnectionDetailCount(localUsers.stream()
                .map(MessageDTO.StreamUserStatsDTO::getLocalConnections)
                .filter(java.util.Objects::nonNull)
                .mapToInt(List::size)
                .sum());
        appStatsDTO.setRedisRegisteredUserIdsSample(redisRegisteredUserIds.stream()
                .limit(userLimit)
                .collect(Collectors.toCollection(LinkedHashSet::new)));
        appStatsDTO.setRedisRegisteredUserSampleTruncated(redisRegisteredUserIds.size() > userLimit);
        return appStatsDTO;
    }

    private List<MessageDTO.StreamUserStatsDTO> buildLocalUserStats(String application,
                                                                    Map<String, UserEmitterGroup> applicationEmitterMap,
                                                                    boolean detailEnabled,
                                                                    int userLimit,
                                                                    int connectionLimitPerUser,
                                                                    LocalDateTime snapshotTime) {
        if (!detailEnabled || applicationEmitterMap == null || applicationEmitterMap.isEmpty()) {
            return Collections.emptyList();
        }
        List<MessageDTO.StreamUserStatsDTO> userStats = new ArrayList<>();
        for (Map.Entry<String, UserEmitterGroup> entry : new ArrayList<>(applicationEmitterMap.entrySet())) {
            MessageDTO.StreamUserStatsDTO userStatsDTO = buildUserStats(application, entry.getKey(), entry.getValue(), connectionLimitPerUser, snapshotTime);
            if (userStatsDTO != null) {
                userStats.add(userStatsDTO);
            }
        }
        userStats.sort(Comparator.comparing(MessageDTO.StreamUserStatsDTO::getLocalConnectionCount, Comparator.reverseOrder())
                .thenComparing(MessageDTO.StreamUserStatsDTO::getUserId, Comparator.nullsLast(String::compareTo)));
        if (userStats.size() <= userLimit) {
            return userStats;
        }
        return new ArrayList<>(userStats.subList(0, userLimit));
    }

    private MessageDTO.StreamUserStatsDTO buildUserStats(String application,
                                                         String userId,
                                                         UserEmitterGroup userEmitterGroup,
                                                         int connectionLimitPerUser,
                                                         LocalDateTime snapshotTime) {
        List<EmitterHolder> emitterHolders = copyEmitterHolders(userEmitterGroup);
        if (emitterHolders.isEmpty()) {
            return null;
        }
        emitterHolders.sort(Comparator.comparingLong(EmitterHolder::getConnectedAt));
        MessageDTO.StreamUserStatsDTO userStatsDTO = new MessageDTO.StreamUserStatsDTO();
        userStatsDTO.setUserId(userId);
        userStatsDTO.setLocalConnectionCount(emitterHolders.size());
        userStatsDTO.setConnectionTruncated(emitterHolders.size() > connectionLimitPerUser);
        userStatsDTO.setRedisOnlineNodeIds(listOnlineNodeIdsByUser(application, userId));
        userStatsDTO.setLocalConnections(emitterHolders.stream()
                .limit(connectionLimitPerUser)
                .map(emitterHolder -> buildConnectionStats(emitterHolder, snapshotTime))
                .collect(Collectors.toList()));
        return userStatsDTO;
    }

    private MessageDTO.StreamConnectionStatsDTO buildConnectionStats(EmitterHolder emitterHolder, LocalDateTime snapshotTime) {
        MessageDTO.StreamConnectionStatsDTO connectionStatsDTO = new MessageDTO.StreamConnectionStatsDTO();
        connectionStatsDTO.setEmitterId(emitterHolder.getEmitterId());
        connectionStatsDTO.setConnectedAt(toLocalDateTime(emitterHolder.getConnectedAt()));
        connectionStatsDTO.setConnectedDurationMillis(Math.max(0L,
                snapshotTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli() - emitterHolder.getConnectedAt()));
        return connectionStatsDTO;
    }

    private MessageDTO.StreamHeartbeatStatsDTO buildHeartbeatStats(HeartbeatMonitorSnapshot heartbeatSnapshot) {
        MessageDTO.StreamHeartbeatStatsDTO heartbeatStatsDTO = new MessageDTO.StreamHeartbeatStatsDTO();
        if (heartbeatSnapshot == null) {
            return heartbeatStatsDTO;
        }
        heartbeatStatsDTO.setStartedAt(toLocalDateTime(heartbeatSnapshot.getStartedAt()));
        heartbeatStatsDTO.setFinishedAt(toLocalDateTime(heartbeatSnapshot.getFinishedAt()));
        heartbeatStatsDTO.setDurationMillis(heartbeatSnapshot.getDurationMillis());
        heartbeatStatsDTO.setCompleted(heartbeatSnapshot.isCompleted());
        heartbeatStatsDTO.setApplicationCount(heartbeatSnapshot.getApplicationCount());
        heartbeatStatsDTO.setTotalUserCount(heartbeatSnapshot.getTotalUserCount());
        heartbeatStatsDTO.setTotalConnectionCount(heartbeatSnapshot.getTotalConnectionCount());
        heartbeatStatsDTO.setPcConnectionCount(heartbeatSnapshot.getPcConnectionCount());
        heartbeatStatsDTO.setPdaConnectionCount(heartbeatSnapshot.getPdaConnectionCount());
        heartbeatStatsDTO.setMaxUserId(heartbeatSnapshot.getMaxUserId());
        heartbeatStatsDTO.setMaxUserConnectionCount(heartbeatSnapshot.getMaxUserConnectionCount());
        heartbeatStatsDTO.setBatchSize(heartbeatSnapshot.getBatchSize());
        heartbeatStatsDTO.setTargetCount(heartbeatSnapshot.getTargetCount());
        heartbeatStatsDTO.setHeartbeatSuccessCount(heartbeatSnapshot.getHeartbeatSuccessCount());
        heartbeatStatsDTO.setHeartbeatFailureCount(heartbeatSnapshot.getHeartbeatFailureCount());
        heartbeatStatsDTO.setForceCloseCount(heartbeatSnapshot.getForceCloseCount());
        heartbeatStatsDTO.setSkippedCount(heartbeatSnapshot.getSkippedCount());
        heartbeatStatsDTO.setHeapUsedMb(heartbeatSnapshot.getHeapUsedMb());
        heartbeatStatsDTO.setHeapTotalMb(heartbeatSnapshot.getHeapTotalMb());
        heartbeatStatsDTO.setHeapMaxMb(heartbeatSnapshot.getHeapMaxMb());
        return heartbeatStatsDTO;
    }

    private List<MessageDTO.StreamMonitorIssueDTO> buildMonitorIssues(MessageDTO.StreamStatsDTO streamStatsDTO,
                                                                     HeartbeatMonitorSnapshot heartbeatSnapshot) {
        List<MessageDTO.StreamMonitorIssueDTO> issues = new ArrayList<>();
        int totalConnections = getTotalConnections(streamStatsDTO);
        if (totalConnections >= STREAM_MONITOR_CRITICAL_TOTAL_CONNECTIONS) {
            issues.add(buildMonitorIssue("CRITICAL",
                    "TOTAL_CONNECTIONS_HIGH",
                    "当前节点 SSE 连接数过高，连接总数=" + totalConnections + "。",
                    "优先排查前端是否存在重复建流/异常重连，并评估是否需要横向扩容或拆分长连接通道。"));
        } else if (totalConnections >= STREAM_MONITOR_WARN_TOTAL_CONNECTIONS) {
            issues.add(buildMonitorIssue("WARN",
                    "TOTAL_CONNECTIONS_WARN",
                    "当前节点 SSE 连接数偏高，连接总数=" + totalConnections + "。",
                    "建议重点关注心跳耗时、堆占用和是否有单个用户连接数异常。"));
        }

        if (heartbeatSnapshot == null || !heartbeatSnapshot.hasData()) {
            issues.add(buildMonitorIssue("INFO",
                    "HEARTBEAT_NOT_READY",
                    "最近一次心跳摘要尚未生成。",
                    "等待下一轮心跳执行后再查看，或确认定时心跳任务是否正常运行。"));
            sortMonitorIssues(issues);
            return issues;
        }

        if (!heartbeatSnapshot.isCompleted()) {
            issues.add(buildMonitorIssue("CRITICAL",
                    "HEARTBEAT_TIMEOUT",
                    "最近一次心跳未在等待窗口内完成，durationMillis=" + heartbeatSnapshot.getDurationMillis() + "。",
                    "说明当前连接规模或发送耗时已经压住心跳线程，建议先排查异常重连和僵尸连接。"));
        } else {
            long safeHeartbeatWaitMillis = heartbeatWaitMillis > 0 ? heartbeatWaitMillis : DEFAULT_HEARTBEAT_WAIT_MILLIS;
            long warnDurationMillis = Math.max(1L, (long) (safeHeartbeatWaitMillis * STREAM_MONITOR_WARN_HEARTBEAT_RATIO));
            if (heartbeatSnapshot.getDurationMillis() >= warnDurationMillis) {
                issues.add(buildMonitorIssue("WARN",
                        "HEARTBEAT_SLOW",
                        "最近一次心跳耗时偏高，durationMillis=" + heartbeatSnapshot.getDurationMillis() + "，waitMillis=" + safeHeartbeatWaitMillis + "。",
                        "建议结合连接总数、批大小和堆占用一起排查，必要时下调单节点连接数或缩短前端重连周期。"));
            }
        }

        long skippedCount = heartbeatSnapshot.getSkippedCount();
        if (skippedCount >= STREAM_MONITOR_CRITICAL_SKIP_COUNT) {
            issues.add(buildMonitorIssue("CRITICAL",
                    "HEARTBEAT_SKIPPED_CRITICAL",
                    "心跳任务累计跳过次数过多，skippedCount=" + skippedCount + "。",
                    "说明上一轮心跳经常未结束，建议立刻查看异常用户连接明细并确认连接增长是否失控。"));
        } else if (skippedCount >= STREAM_MONITOR_WARN_SKIP_COUNT) {
            issues.add(buildMonitorIssue("WARN",
                    "HEARTBEAT_SKIPPED_WARN",
                    "心跳任务已出现跳过，skippedCount=" + skippedCount + "。",
                    "建议继续观察心跳耗时和连接规模，避免单节点长连接继续堆积。"));
        }

        double heapRatio = calculateHeapRatio(heartbeatSnapshot);
        if (heapRatio >= STREAM_MONITOR_CRITICAL_HEAP_RATIO) {
            issues.add(buildMonitorIssue("CRITICAL",
                    "HEAP_USAGE_HIGH",
                    "最近一次心跳记录的堆占用偏高，heapUsedMb=" + heartbeatSnapshot.getHeapUsedMb() +
                            "，heapMaxMb=" + heartbeatSnapshot.getHeapMaxMb() + "。",
                    "说明当前节点已经接近堆上限，建议优先清理异常连接并评估是否需要扩容堆或拆分连接压力。"));
        } else if (heapRatio >= STREAM_MONITOR_WARN_HEAP_RATIO) {
            issues.add(buildMonitorIssue("WARN",
                    "HEAP_USAGE_WARN",
                    "最近一次心跳记录的堆占用较高，heapUsedMb=" + heartbeatSnapshot.getHeapUsedMb() +
                            "，heapMaxMb=" + heartbeatSnapshot.getHeapMaxMb() + "。",
                    "建议持续关注堆变化趋势，并结合 streamStats 明细检查是否存在大量长寿命 SSE 连接。"));
        }

        if (heartbeatSnapshot.getMaxUserConnectionCount() >= Math.max(1, maxUserConnections)) {
            issues.add(buildMonitorIssue("WARN",
                    "SINGLE_USER_CONNECTION_HIGH",
                    "检测到单个用户连接数已触达当前阈值，userId=" + heartbeatSnapshot.getMaxUserId() +
                            "，connectionCount=" + heartbeatSnapshot.getMaxUserConnectionCount() + "。",
                    "建议优先排查该用户是否存在多标签页、路由切换重复建流或前端重连抖动。"));
        }

        long totalReplaceCount = getTotalReplaceCount(streamStatsDTO);
        if (totalReplaceCount >= STREAM_MONITOR_WARN_REPLACE_COUNT) {
            issues.add(buildMonitorIssue("WARN",
                    "EMITTER_EVICTION_HIGH",
                    "当前节点累计淘汰的超限连接较多，replaceCount=" + totalReplaceCount + "。",
                    "通常意味着某些用户在持续重复建流，建议结合具体用户明细排查前端重连逻辑。"));
        }

        if (forceCloseAfterMillis <= 0 && totalConnections >= STREAM_MONITOR_WARN_TOTAL_CONNECTIONS) {
            issues.add(buildMonitorIssue("INFO",
                    "FORCE_CLOSE_DISABLED",
                    "当前节点连接数已经偏高，但服务端未开启连接轮换。",
                    "如果前端重连已稳定，建议评估开启 sys.notice.sse.force-close-after-millis，限制单条连接最长寿命。"));
        }

        sortMonitorIssues(issues);
        return issues;
    }

    private MessageDTO.StreamMonitorIssueDTO buildMonitorIssue(String level, String code, String message, String suggestion) {
        MessageDTO.StreamMonitorIssueDTO issueDTO = new MessageDTO.StreamMonitorIssueDTO();
        issueDTO.setLevel(level);
        issueDTO.setCode(code);
        issueDTO.setMessage(message);
        issueDTO.setSuggestion(suggestion);
        return issueDTO;
    }

    private void sortMonitorIssues(List<MessageDTO.StreamMonitorIssueDTO> issues) {
        issues.sort(Comparator.comparingInt(issue -> getIssueSeverityRank(issue.getLevel())));
    }

    private String resolveRiskLevel(List<MessageDTO.StreamMonitorIssueDTO> issues) {
        if (issues == null || issues.isEmpty()) {
            return "NORMAL";
        }
        String highestLevel = "NORMAL";
        int highestRank = Integer.MAX_VALUE;
        for (MessageDTO.StreamMonitorIssueDTO issue : issues) {
            int currentRank = getIssueSeverityRank(issue.getLevel());
            if (currentRank < highestRank) {
                highestRank = currentRank;
                highestLevel = issue.getLevel();
            }
        }
        return highestLevel;
    }

    private int getIssueSeverityRank(String level) {
        if (StringUtils.equalsIgnoreCase("CRITICAL", level)) {
            return 1;
        }
        if (StringUtils.equalsIgnoreCase("WARN", level)) {
            return 2;
        }
        if (StringUtils.equalsIgnoreCase("INFO", level)) {
            return 3;
        }
        return 4;
    }

    private List<String> buildMonitorSuggestions(List<MessageDTO.StreamMonitorIssueDTO> issues) {
        LinkedHashSet<String> suggestions = new LinkedHashSet<>();
        if (issues != null) {
            for (MessageDTO.StreamMonitorIssueDTO issue : issues) {
                if (StringUtils.isNotBlank(issue.getSuggestion())) {
                    suggestions.add(issue.getSuggestion());
                }
            }
        }
        if (suggestions.isEmpty()) {
            suggestions.add("当前未发现明显风险，建议继续关注连接总数、单用户连接数、心跳耗时和堆占用。");
        }
        suggestions.add("如需下钻具体用户和 emitter 明细，可继续调用 /sysMessage/streamStats?detail=true 查看。");
        return new ArrayList<>(suggestions);
    }

    private int getTotalConnections(MessageDTO.StreamStatsDTO streamStatsDTO) {
        if (streamStatsDTO == null) {
            return 0;
        }
        return safeInteger(streamStatsDTO.getPc() == null ? null : streamStatsDTO.getPc().getLocalConnectionCount())
                + safeInteger(streamStatsDTO.getPda() == null ? null : streamStatsDTO.getPda().getLocalConnectionCount());
    }

    private long getTotalReplaceCount(MessageDTO.StreamStatsDTO streamStatsDTO) {
        if (streamStatsDTO == null) {
            return 0L;
        }
        return safeLong(streamStatsDTO.getPc() == null ? null : streamStatsDTO.getPc().getReplaceCount())
                + safeLong(streamStatsDTO.getPda() == null ? null : streamStatsDTO.getPda().getReplaceCount());
    }

    private int safeInteger(Integer value) {
        return value == null ? 0 : value;
    }

    private long safeLong(Long value) {
        return value == null ? 0L : value;
    }

    private double calculateHeapRatio(HeartbeatMonitorSnapshot heartbeatSnapshot) {
        if (heartbeatSnapshot == null || heartbeatSnapshot.getHeapMaxMb() <= 0L) {
            return 0D;
        }
        return (double) heartbeatSnapshot.getHeapUsedMb() / (double) heartbeatSnapshot.getHeapMaxMb();
    }

    private LocalDateTime toLocalDateTime(long epochMillis) {
        if (epochMillis <= 0L) {
            return null;
        }
        return Instant.ofEpochMilli(epochMillis).atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    private int sanitizeLimit(Integer requestedLimit, int defaultLimit, int maxLimit) {
        if (requestedLimit == null || requestedLimit <= 0) {
            return defaultLimit;
        }
        return Math.min(requestedLimit, maxLimit);
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

    private void doHeartbeat(HeartbeatSummary summary, long start) throws InterruptedException {
        long now = System.currentTimeMillis();
        summary.setBatchSize(Math.max(1, heartbeatBatchSize > 0 ? heartbeatBatchSize : DEFAULT_HEARTBEAT_BATCH_SIZE));
        List<HeartbeatTarget> heartbeatTargets = snapshotHeartbeatTargets(summary);
        summary.setTargetCount(heartbeatTargets.size());
        if (heartbeatTargets.isEmpty()) {
            summary.setCompleted(true);
            summary.setDurationMillis(System.currentTimeMillis() - start);
            return;
        }

        List<List<HeartbeatTarget>> batches = partitionHeartbeatTargets(heartbeatTargets, summary.getBatchSize());
        CountDownLatch latch = new CountDownLatch(batches.size());
        for (List<HeartbeatTarget> batch : batches) {
            try {
                noticeHeartbeatExecutor.execute(() -> {
                    try {
                        processHeartbeatBatch(batch, now, summary);
                    } finally {
                        latch.countDown();
                    }
                });
            } catch (RejectedExecutionException e) {
                log.warn("Submit notice heartbeat batch rejected, nodeId={}, batchSize={}", getNodeId(), batch.size(), e);
                processHeartbeatBatch(batch, now, summary);
                latch.countDown();
            }
        }
        long waitMillis = heartbeatWaitMillis > 0 ? heartbeatWaitMillis : DEFAULT_HEARTBEAT_WAIT_MILLIS;
        boolean completed = latch.await(waitMillis, TimeUnit.MILLISECONDS);
        summary.setCompleted(completed);
        if (!completed) {
            log.warn("Notice heartbeat wait timeout, nodeId={}, waitedMillis={}, remainingBatchCount={}",
                    getNodeId(), waitMillis, latch.getCount());
        }
        summary.setDurationMillis(System.currentTimeMillis() - start);
    }

    private List<HeartbeatTarget> snapshotHeartbeatTargets(HeartbeatSummary summary) {
        List<HeartbeatTarget> heartbeatTargets = new ArrayList<>();
        summary.setApplicationCount(emitterMap.size());
        for (String application : new HashSet<>(emitterMap.keySet())) {
            int currentCount = getCurrentConnectionCount(application);
            if (currentCount > 0) {
                refreshOnlineNodeRegistration(application, currentCount);
            }
            summary.recordApplicationConnections(application, currentCount);
            Map<String, UserEmitterGroup> applicationEmitterMap = getApplicationEmitterMap(application);
            for (Map.Entry<String, UserEmitterGroup> entry : new ArrayList<>(applicationEmitterMap.entrySet())) {
                String userId = entry.getKey();
                List<EmitterHolder> emitterHolders = copyEmitterHolders(entry.getValue());
                if (emitterHolders.isEmpty()) {
                    continue;
                }
                summary.incrementUserCount();
                summary.trackMaxUserConnection(userId, emitterHolders.size());
                for (EmitterHolder emitterHolder : emitterHolders) {
                    heartbeatTargets.add(new HeartbeatTarget(application, userId, emitterHolder));
                }
            }
        }
        summary.setTotalConnectionCount(heartbeatTargets.size());
        return heartbeatTargets;
    }

    private List<List<HeartbeatTarget>> partitionHeartbeatTargets(List<HeartbeatTarget> heartbeatTargets, int batchSize) {
        if (heartbeatTargets.isEmpty()) {
            return Collections.emptyList();
        }
        List<List<HeartbeatTarget>> partitions = new ArrayList<>();
        for (int index = 0; index < heartbeatTargets.size(); index += batchSize) {
            int end = Math.min(index + batchSize, heartbeatTargets.size());
            partitions.add(heartbeatTargets.subList(index, end));
        }
        return partitions;
    }

    private void processHeartbeatBatch(List<HeartbeatTarget> batch, long now, HeartbeatSummary summary) {
        for (HeartbeatTarget target : batch) {
            EmitterHolder emitterHolder = target.getEmitterHolder();
            if (shouldForceClose(emitterHolder, now)) {
                log.info("Force close expired notice emitter, application={}, userId={}, emitterId={}, connectedAt={}, nodeId={}",
                        target.getApplication(), target.getUserId(), emitterHolder.getEmitterId(), emitterHolder.getConnectedAt(), getNodeId());
                remove(target.getApplication(), target.getUserId(), emitterHolder.getEmitterId());
                completeQuietly(emitterHolder.getEmitter(), target.getApplication(), target.getUserId(), emitterHolder.getEmitterId(), "forceClose");
                summary.incrementForceCloseCount();
                continue;
            }
            try {
                emitterHolder.getEmitter().send(buildMetaEvent("heartbeat", target.getApplication(), target.getUserId()));
                summary.incrementHeartbeatSuccessCount();
            } catch (Exception e) {
                log.debug("Heartbeat failed, userId={}, emitterId={}, application={}",
                        target.getUserId(), emitterHolder.getEmitterId(), target.getApplication(), e);
                remove(target.getApplication(), target.getUserId(), emitterHolder.getEmitterId());
                summary.incrementHeartbeatFailureCount();
            }
        }
    }

    private void logHeartbeatSummary(HeartbeatSummary summary, long start) {
        long now = System.currentTimeMillis();
        long durationMillis = summary.getDurationMillis() == 0 ? now - start : summary.getDurationMillis();
        Runtime runtime = Runtime.getRuntime();
        long usedMemoryMb = (runtime.totalMemory() - runtime.freeMemory()) / 1024 / 1024;
        long totalMemoryMb = runtime.totalMemory() / 1024 / 1024;
        long maxMemoryMb = runtime.maxMemory() / 1024 / 1024;
        lastHeartbeatSnapshotRef.set(new HeartbeatMonitorSnapshot(
                start,
                now,
                durationMillis,
                summary.isCompleted(),
                summary.getApplicationCount(),
                summary.getTotalUserCount(),
                summary.getTotalConnectionCount(),
                summary.getPcConnectionCount(),
                summary.getPdaConnectionCount(),
                summary.getMaxUserId(),
                summary.getMaxUserConnectionCount(),
                summary.getBatchSize(),
                summary.getTargetCount(),
                summary.getHeartbeatSuccessCount(),
                summary.getHeartbeatFailureCount(),
                summary.getForceCloseCount(),
                heartbeatSkipCount.get(),
                usedMemoryMb,
                totalMemoryMb,
                maxMemoryMb));
        log.info("Notice heartbeat summary, nodeId={}, emitterMapAppCount={}, totalUsers={}, totalConnections={}, pcConnections={}, pdaConnections={}, maxUserId={}, maxUserConnectionCount={}, batchSize={}, targetCount={}, heartbeatSuccessCount={}, heartbeatFailureCount={}, forceCloseCount={}, durationMs={}, completed={}, skippedCount={}, heapUsedMb={}, heapTotalMb={}, heapMaxMb={}",
                getNodeId(),
                summary.getApplicationCount(),
                summary.getTotalUserCount(),
                summary.getTotalConnectionCount(),
                summary.getPcConnectionCount(),
                summary.getPdaConnectionCount(),
                summary.getMaxUserId(),
                summary.getMaxUserConnectionCount(),
                summary.getBatchSize(),
                summary.getTargetCount(),
                summary.getHeartbeatSuccessCount(),
                summary.getHeartbeatFailureCount(),
                summary.getForceCloseCount(),
                durationMillis,
                summary.isCompleted(),
                heartbeatSkipCount.get(),
                usedMemoryMb,
                totalMemoryMb,
                maxMemoryMb);
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

    @Getter
    private static final class HeartbeatTarget {
        private final String application;
        private final String userId;
        private final EmitterHolder emitterHolder;

        private HeartbeatTarget(String application, String userId, EmitterHolder emitterHolder) {
            this.application = application;
            this.userId = userId;
            this.emitterHolder = emitterHolder;
        }
    }

    @Getter
    private static final class HeartbeatSummary {
        private int applicationCount;
        private int totalUserCount;
        private int totalConnectionCount;
        private int pcConnectionCount;
        private int pdaConnectionCount;
        private String maxUserId;
        private int maxUserConnectionCount;
        private int batchSize;
        private int targetCount;
        private long durationMillis;
        private boolean completed;
        private final AtomicInteger heartbeatSuccessCount = new AtomicInteger(0);
        private final AtomicInteger heartbeatFailureCount = new AtomicInteger(0);
        private final AtomicInteger forceCloseCount = new AtomicInteger(0);

        private void setApplicationCount(int applicationCount) {
            this.applicationCount = applicationCount;
        }

        private void incrementUserCount() {
            this.totalUserCount++;
        }

        private void setTotalConnectionCount(int totalConnectionCount) {
            this.totalConnectionCount = totalConnectionCount;
        }

        private void recordApplicationConnections(String application, int connectionCount) {
            if (StringUtils.equalsIgnoreCase(SysTypeEnum.PC.getCode(), application)) {
                this.pcConnectionCount = connectionCount;
            } else if (StringUtils.equalsIgnoreCase(SysTypeEnum.PDA.getCode(), application)) {
                this.pdaConnectionCount = connectionCount;
            }
        }

        private void trackMaxUserConnection(String userId, int connectionCount) {
            if (connectionCount > this.maxUserConnectionCount) {
                this.maxUserConnectionCount = connectionCount;
                this.maxUserId = userId;
            }
        }

        private void setBatchSize(int batchSize) {
            this.batchSize = batchSize;
        }

        private void setTargetCount(int targetCount) {
            this.targetCount = targetCount;
        }

        private void incrementHeartbeatSuccessCount() {
            this.heartbeatSuccessCount.incrementAndGet();
        }

        private void incrementHeartbeatFailureCount() {
            this.heartbeatFailureCount.incrementAndGet();
        }

        private void incrementForceCloseCount() {
            this.forceCloseCount.incrementAndGet();
        }

        private void setDurationMillis(long durationMillis) {
            this.durationMillis = durationMillis;
        }

        private void setCompleted(boolean completed) {
            this.completed = completed;
        }

        private int getHeartbeatSuccessCount() {
            return this.heartbeatSuccessCount.get();
        }

        private int getHeartbeatFailureCount() {
            return this.heartbeatFailureCount.get();
        }

        private int getForceCloseCount() {
            return this.forceCloseCount.get();
        }
    }

    @Getter
    private static final class HeartbeatMonitorSnapshot {
        private final long startedAt;
        private final long finishedAt;
        private final long durationMillis;
        private final boolean completed;
        private final int applicationCount;
        private final int totalUserCount;
        private final int totalConnectionCount;
        private final int pcConnectionCount;
        private final int pdaConnectionCount;
        private final String maxUserId;
        private final int maxUserConnectionCount;
        private final int batchSize;
        private final int targetCount;
        private final int heartbeatSuccessCount;
        private final int heartbeatFailureCount;
        private final int forceCloseCount;
        private final long skippedCount;
        private final long heapUsedMb;
        private final long heapTotalMb;
        private final long heapMaxMb;

        private HeartbeatMonitorSnapshot(long startedAt,
                                         long finishedAt,
                                         long durationMillis,
                                         boolean completed,
                                         int applicationCount,
                                         int totalUserCount,
                                         int totalConnectionCount,
                                         int pcConnectionCount,
                                         int pdaConnectionCount,
                                         String maxUserId,
                                         int maxUserConnectionCount,
                                         int batchSize,
                                         int targetCount,
                                         int heartbeatSuccessCount,
                                         int heartbeatFailureCount,
                                         int forceCloseCount,
                                         long skippedCount,
                                         long heapUsedMb,
                                         long heapTotalMb,
                                         long heapMaxMb) {
            this.startedAt = startedAt;
            this.finishedAt = finishedAt;
            this.durationMillis = durationMillis;
            this.completed = completed;
            this.applicationCount = applicationCount;
            this.totalUserCount = totalUserCount;
            this.totalConnectionCount = totalConnectionCount;
            this.pcConnectionCount = pcConnectionCount;
            this.pdaConnectionCount = pdaConnectionCount;
            this.maxUserId = maxUserId;
            this.maxUserConnectionCount = maxUserConnectionCount;
            this.batchSize = batchSize;
            this.targetCount = targetCount;
            this.heartbeatSuccessCount = heartbeatSuccessCount;
            this.heartbeatFailureCount = heartbeatFailureCount;
            this.forceCloseCount = forceCloseCount;
            this.skippedCount = skippedCount;
            this.heapUsedMb = heapUsedMb;
            this.heapTotalMb = heapTotalMb;
            this.heapMaxMb = heapMaxMb;
        }

        private boolean hasData() {
            return this.startedAt > 0L;
        }

        private static HeartbeatMonitorSnapshot empty() {
            return new HeartbeatMonitorSnapshot(0L, 0L, 0L,
                    false, 0, 0, 0, 0, 0,
                    null, 0, 0, 0, 0, 0,
                    0, 0L, 0L, 0L, 0L);
        }
    }
}
