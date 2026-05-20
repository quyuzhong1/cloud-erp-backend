package com.common.business.mask.protect;

import com.common.business.mask.cache.CfgMaskFieldSnapshotEntry;

import java.util.List;
import java.util.Map;

/**
 * DB_VALUE_COMPARE 模式下读取数据库当前敏感字段值。
 *
 * @author cloud-erp
 */
public interface MaskProtectCurrentValueReader {

    /**
     * 在当前事务内锁定记录并读取字段当前值。
     *
     * @param entry     脱敏配置
     * @param recordIds 记录 ID 列表
     * @return recordId -> 当前值
     */
    Map<String, MaskProtectCurrentValue> lockAndRead(CfgMaskFieldSnapshotEntry entry, List<String> recordIds);
}
