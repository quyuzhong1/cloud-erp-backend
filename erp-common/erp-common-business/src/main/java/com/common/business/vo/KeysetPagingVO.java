package com.common.business.vo;

import lombok.Data;

import java.util.Collections;
import java.util.List;

/**
 * 键集分页响应
 */
@Data
public class KeysetPagingVO<T> {

    private List<T> list = Collections.emptyList();

    /**
     * 是否可能还有下一批（列表大小达到 limit 时常为 true，最终以空列表为准）
     */
    private boolean hasNext;

    /**
     * 下一批查询使用的 {@code lastIdExclusive}；可由服务端直接返回，避免客户端再从行数据推导
     */
    private Long nextCursorId;
}
