package com.erp.server.file.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class StatementDTO <R, T> {
    /**
     * 主表数据
     */
    private R mainData;
    /**
     * 明细数据
     */
    private List<T> detailData;
}
