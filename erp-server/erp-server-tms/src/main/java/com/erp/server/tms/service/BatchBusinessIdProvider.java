package com.erp.server.tms.service;

import java.util.List;

/**
 * 异步任务分批查询 businessId 的 Provider，按 lastId + batchSize 返回下一批 ID。
 */
@FunctionalInterface
public interface BatchBusinessIdProvider {

    List<String> page(String lastId, int batchSize);
}
