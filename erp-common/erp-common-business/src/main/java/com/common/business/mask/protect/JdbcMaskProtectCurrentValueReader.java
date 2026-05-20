package com.common.business.mask.protect;

import com.common.business.mask.cache.CfgMaskFieldSnapshotEntry;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 基于固定 SQL 模板的当前值锁读器。
 *
 * @author cloud-erp
 */
@Component
public class JdbcMaskProtectCurrentValueReader implements MaskProtectCurrentValueReader {

    private static final Pattern IDENTIFIER = Pattern.compile("^[A-Za-z_][A-Za-z0-9_]*$");
    private static final int DEFAULT_BATCH_SIZE = 500;
    private static final int MAX_BATCH_SIZE = 1000;

    @Autowired(required = false)
    private JdbcTemplate jdbcTemplate;

    @Value("${mask.protect.db-compare.batch-size:500}")
    private Integer batchSize;

    @Value("${mask.protect.db-compare.lock-timeout-ms:2000}")
    private Integer lockTimeoutMs;

    public JdbcMaskProtectCurrentValueReader() {
    }

    JdbcMaskProtectCurrentValueReader(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Map<String, MaskProtectCurrentValue> lockAndRead(CfgMaskFieldSnapshotEntry entry, List<String> recordIds) {
        if (jdbcTemplate == null || entry == null || recordIds == null || recordIds.isEmpty()) {
            throw new MaskProtectException();
        }
        String tableName = safeTableName(entry.getProtectTableName());
        String idColumn = safeColumnName(StringUtils.defaultIfBlank(entry.getProtectRecordIdColumn(), "id"));
        String valueColumn = safeColumnName(entry.getProtectValueColumn());
        String deletedColumn = safeOptionalColumnName(entry.getProtectDeletedColumn());
        List<String> ids = recordIds.stream()
                .filter(StringUtils::isNotBlank)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
        if (ids.isEmpty()) {
            throw new MaskProtectException();
        }
        Map<String, MaskProtectCurrentValue> result = new LinkedHashMap<>();
        Integer previousTimeout = null;
        try {
            previousTimeout = setLocalLockTimeout();
            for (List<String> part : partitions(ids, normalizeBatchSize())) {
                result.putAll(queryPart(tableName, idColumn, valueColumn, deletedColumn, part));
            }
        } finally {
            restoreQueryTimeout(previousTimeout);
        }
        return result;
    }

    private Map<String, MaskProtectCurrentValue> queryPart(String tableName, String idColumn, String valueColumn,
                                                           String deletedColumn, List<String> ids) {
        String placeholders = ids.stream().map(id -> "?").collect(Collectors.joining(","));
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT ").append(idColumn).append(", ").append(valueColumn)
                .append(" FROM ").append(tableName)
                .append(" WHERE ").append(idColumn).append(" IN (").append(placeholders).append(")");
        if (StringUtils.isNotBlank(deletedColumn)) {
            sql.append(" AND COALESCE(").append(deletedColumn).append(", false) = false");
        }

        // 按 id 顺序加锁，保证并发批量更新时锁获取顺序尽量一致。
        sql.append(" ORDER BY ").append(idColumn).append(" FOR UPDATE");
        Object[] args = ids.toArray(new Object[0]);
        ResultSetExtractor<Map<String, MaskProtectCurrentValue>> extractor = this::toMap;
        return jdbcTemplate.query(sql.toString(), args, extractor);
    }

    private Map<String, MaskProtectCurrentValue> toMap(ResultSet rs) throws SQLException {
        Map<String, MaskProtectCurrentValue> map = new LinkedHashMap<>();
        while (rs.next()) {
            Object id = rs.getObject(1);
            Object value = rs.getObject(2);
            if (id != null) {
                map.put(MaskProtectReflectionUtils.normalizeValue(id),
                        new MaskProtectCurrentValue(MaskProtectReflectionUtils.normalizeValue(value),
                                value == null));
            }
        }
        return map;
    }

    private Integer setLocalLockTimeout() {
        int timeout = lockTimeoutMs == null ? 2000 : lockTimeoutMs;
        if (timeout <= 0) {
            return null;
        }
        try {
            // PostgreSQL 下该配置只在当前事务生效；其他数据库不支持时直接跳过。
            jdbcTemplate.execute("SET LOCAL lock_timeout = '" + timeout + "ms'");
        } catch (Throwable ignored) {
            // Some non-PostgreSQL databases do not support SET LOCAL lock_timeout.
        }
        return null;
    }

    private void restoreQueryTimeout(Integer previousTimeout) {
        if (previousTimeout != null) {
            try {
                jdbcTemplate.setQueryTimeout(previousTimeout);
            } catch (Throwable ignored) {
                // ignore
            }
        }
    }

    private int normalizeBatchSize() {
        int size = batchSize == null ? DEFAULT_BATCH_SIZE : batchSize;
        if (size <= 0) {
            return DEFAULT_BATCH_SIZE;
        }
        return Math.min(size, MAX_BATCH_SIZE);
    }

    private List<List<String>> partitions(List<String> ids, int size) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyList();
        }
        List<List<String>> result = new ArrayList<>();
        for (int i = 0; i < ids.size(); i += size) {
            result.add(ids.subList(i, Math.min(ids.size(), i + size)));
        }
        return result;
    }

    private String safeTableName(String name) {
        if (StringUtils.isBlank(name)) {
            throw new MaskProtectException();
        }
        String[] parts = name.trim().split("\\.");
        if (parts.length > 2) {
            throw new MaskProtectException();
        }
        List<String> safe = new ArrayList<>(parts.length);
        for (String part : parts) {
            safe.add(safeColumnName(part));
        }
        return String.join(".", safe);
    }

    private String safeColumnName(String name) {
        if (StringUtils.isBlank(name) || !IDENTIFIER.matcher(name.trim()).matches()) {
            throw new MaskProtectException();
        }
        return name.trim();
    }

    private String safeOptionalColumnName(String name) {
        if (StringUtils.isBlank(name)) {
            return "";
        }
        return safeColumnName(name);
    }
}
