package com.erp.server.tms.service.support;

import cn.hutool.core.collection.CollUtil;
import com.erp.server.tms.service.BatchBusinessIdProvider;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 异步任务已选 ID 列表的分批游标辅助。
 */
public final class TmsAsyncTaskBatchIdPages {

    private TmsAsyncTaskBatchIdPages() {
    }

    public static BatchBusinessIdProvider selectedIdProvider(List<String> ids) {
        if (CollUtil.isEmpty(ids)) {
            return null;
        }
        List<String> distinctIds = ids.stream()
            .filter(StringUtils::isNotBlank)
            .distinct()
            .collect(Collectors.toList());
        if (CollUtil.isEmpty(distinctIds)) {
            return null;
        }
        return (lastId, batchSize) -> pageDistinctIds(distinctIds, lastId, batchSize);
    }

    static List<String> pageDistinctIds(List<String> distinctIds, String lastId, int batchSize) {
        int startIndex = 0;
        if (StringUtils.isNotBlank(lastId)) {
            int lastIndex = distinctIds.indexOf(lastId);
            if (lastIndex < 0 || lastIndex + 1 >= distinctIds.size()) {
                return Collections.emptyList();
            }
            startIndex = lastIndex + 1;
        }
        int endIndex = Math.min(startIndex + batchSize, distinctIds.size());
        return new ArrayList<>(distinctIds.subList(startIndex, endIndex));
    }
}
