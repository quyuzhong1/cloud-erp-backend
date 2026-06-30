package com.erp.server.file.core.sheetgroup;

import com.common.core.exception.ServiceException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiConsumer;

/**
 * 导出写盘时的 sheet 分组保护共用逻辑：跨页尾组缓冲、临界点整组回退、分组连续性校验。
 * <p>
 * 供 {@link com.erp.server.file.core.AbstractPageFileEventHandler}（模板 fill）与
 * {@link com.erp.server.file.core.dynamic.AbstractDynamicHeadersFileEventHandler}（动态表头 write）共用，
 * 避免两条链路行为分叉。当前使用方：模具/下单跟踪/WMS 虚拟库存差异（模板分组）、销售订单（动态表头分组）等。
 */
@Slf4j
public final class SheetGroupWriteSupport {

    private SheetGroupWriteSupport() {
    }

    /**
     * 分组保护开启时暂存分页末尾的未闭合分组。
     */
    public static final class Buffer<T> {
        public final List<T> rows = new ArrayList<>();
        public final Set<Object> writtenGroupKeys = new HashSet<>();
    }

    @FunctionalInterface
    public interface GroupKeyAccessor<T> {
        Object groupKey(T row);
    }

    /**
     * 分组保护策略与行数上限（由具体 Handler 在写出时注入）。
     */
    public static final class Policy {
        public final boolean keepSheetGroupTogether;
        public final boolean failOnNonContinuousSheetGroup;
        public final String handlerName;
        public final int maxRowsPerSheet;
        public final int maxRowsHardLimit;

        public Policy(boolean keepSheetGroupTogether, boolean failOnNonContinuousSheetGroup, String handlerName,
                int maxRowsPerSheet, int maxRowsHardLimit) {
            this.keepSheetGroupTogether = keepSheetGroupTogether;
            this.failOnNonContinuousSheetGroup = failOnNonContinuousSheetGroup;
            this.handlerName = handlerName;
            this.maxRowsPerSheet = maxRowsPerSheet;
            this.maxRowsHardLimit = maxRowsHardLimit;
        }

        public int effectiveMaxRowsPerSheet() {
            return Math.min(maxRowsPerSheet, maxRowsHardLimit);
        }
    }

    public static <T> boolean isEnabled(List<T> rows, Policy policy, GroupKeyAccessor<T> accessor) {
        if (!policy.keepSheetGroupTogether || CollectionUtils.isEmpty(rows)) {
            return false;
        }
        for (T row : rows) {
            if (row != null && accessor.groupKey(row) != null) {
                return true;
            }
        }
        return false;
    }

    /**
     * 根据分组 key 回退当前 sheet 的写入临界点；返回 0 表示须先切换 sheet。
     */
    public static <T> int adjustTakeForSheetGroup(List<T> batch, int idx, int take, GroupKeyAccessor<T> accessor) {
        int end = idx + take;
        if (end >= batch.size()) {
            return take;
        }
        Object nextKey = accessor.groupKey(batch.get(end));
        if (nextKey == null) {
            return take;
        }
        if (!Objects.equals(accessor.groupKey(batch.get(end - 1)), nextKey)) {
            return take;
        }
        while (end > idx && Objects.equals(accessor.groupKey(batch.get(end - 1)), nextKey)) {
            end--;
        }
        return end - idx;
    }

    /**
     * 合并上一页暂存尾组与当前页数据，返回当前可以安全写出的行；非末页会继续暂存尾组。
     */
    public static <T> List<T> drainReadyRows(List<T> batch, Buffer<T> buffer, boolean lastPage, Policy policy,
            GroupKeyAccessor<T> accessor) {
        if (CollectionUtils.isEmpty(buffer.rows) && !isEnabled(batch, policy, accessor)) {
            return batch;
        }
        List<T> combined = new ArrayList<>(buffer.rows.size() + batch.size());
        combined.addAll(buffer.rows);
        buffer.rows.clear();
        combined.addAll(batch);
        if (combined.isEmpty() || lastPage) {
            return combined;
        }
        int tailStart = tailGroupStart(combined, accessor);
        List<T> ready = new ArrayList<>(combined.subList(0, tailStart));
        buffer.rows.addAll(combined.subList(tailStart, combined.size()));
        assertPendingWithinLimit(buffer.rows, policy, accessor);
        return ready;
    }

    public static <T> int tailGroupStart(List<T> rows, GroupKeyAccessor<T> accessor) {
        int tailStart = rows.size() - 1;
        Object tailKey = accessor.groupKey(rows.get(tailStart));
        if (tailKey == null) {
            return rows.size();
        }
        while (tailStart > 0 && Objects.equals(accessor.groupKey(rows.get(tailStart - 1)), tailKey)) {
            tailStart--;
        }
        return tailStart;
    }

    public static <T> void assertPendingWithinLimit(List<T> rows, Policy policy, GroupKeyAccessor<T> accessor) {
        if (CollectionUtils.isEmpty(rows)) {
            return;
        }
        int maxRows = policy.effectiveMaxRowsPerSheet();
        if (rows.size() > maxRows) {
            throw new ServiceException("导出分组数据超过单 sheet 最大行数（分组=" + accessor.groupKey(rows.get(0))
                    + "，行数=" + rows.size() + "，上限=" + maxRows + " 行），请缩小筛选范围或调整单 sheet 行数配置。");
        }
    }

    public static <T> void validateContiguity(List<T> rows, Buffer<T> buffer, String pagingState, Policy policy,
            GroupKeyAccessor<T> accessor) {
        if (!policy.keepSheetGroupTogether || CollectionUtils.isEmpty(rows)) {
            return;
        }
        int start = 0;
        while (start < rows.size()) {
            Object groupKey = accessor.groupKey(rows.get(start));
            int end = start + 1;
            while (end < rows.size() && Objects.equals(groupKey, accessor.groupKey(rows.get(end)))) {
                end++;
            }
            if (groupKey != null && !buffer.writtenGroupKeys.add(groupKey)) {
                String message = "导出分组未连续：handler=" + policy.handlerName + " groupKey=" + groupKey
                        + " paging=" + pagingState + "，分组保护可能失效，请检查上游排序是否保持 sheetGroupKey 连续";
                if (policy.failOnNonContinuousSheetGroup) {
                    throw new ServiceException(message);
                }
                log.warn(message);
            }
            start = end;
        }
    }

    /**
     * 按连续 groupKey 切片并回调（用于 {@code beforeWriteGroupRows}）。
     */
    public static <T> void forEachContiguousGroup(List<T> rows, Policy policy, GroupKeyAccessor<T> accessor,
            BiConsumer<Object, List<T>> groupConsumer) {
        if (!policy.keepSheetGroupTogether || CollectionUtils.isEmpty(rows)) {
            return;
        }
        int start = 0;
        while (start < rows.size()) {
            Object groupKey = accessor.groupKey(rows.get(start));
            int end = start + 1;
            while (end < rows.size() && Objects.equals(groupKey, accessor.groupKey(rows.get(end)))) {
                end++;
            }
            groupConsumer.accept(groupKey, rows.subList(start, end));
            start = end;
        }
    }

    public static <T> ServiceException groupExceedsSingleSheetException(T row, Policy policy,
            GroupKeyAccessor<T> accessor) {
        return new ServiceException("导出分组数据超过单 sheet 最大行数（分组=" + accessor.groupKey(row)
                + "，上限=" + policy.effectiveMaxRowsPerSheet()
                + " 行），请缩小筛选范围或调整单 sheet 行数配置。");
    }
}
