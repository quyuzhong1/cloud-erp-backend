package com.common.business.dto.base;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 键集分页请求（导出等场景）：{@code WHERE id > lastId ORDER BY id LIMIT limit}
 */
@Data
public class KeysetPagingDTO<P> {

    /**
     * 上一批最大 id，首轮为 null 表示从头扫描
     */
    private Long lastIdExclusive;

    private Integer limit = 5000;

    @NotNull
    private P params;
}
