package com.common.business.enums;

/**
 * 异步分页导出时的取数策略（与 {@code erp-server-file} 中分页导出 Handler 配合使用）。
 * <p>
 * 新增模式时：在此增加枚举常量，并在分页导出基类的 {@code writePagedExcel} 中增加对应分支与拉数实现。
 */
public enum ExportPaginationMode {

    /**
     * 传统偏移分页：{@code currPage + pageSize}，首屏依赖 {@code totalCount} 判断是否还有下一页。
     */
    OFFSET,

    /**
     * 键集分页：按单调递增的排序 id（雪花或数值主键），远端典型实现为
     * {@code WHERE id > ? ORDER BY id LIMIT ?}；无需 count 全表，适合大数据量导出。
     * <p>
     * 子类需重写导出 Handler 中的 {@code fetchKeyset} 方法。
     */
    KEYSET_BY_SORT_ID
}
