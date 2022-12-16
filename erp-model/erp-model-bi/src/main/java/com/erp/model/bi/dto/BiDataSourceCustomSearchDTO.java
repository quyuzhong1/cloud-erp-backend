package com.erp.model.bi.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2022/12/14 16:47
 */
@Data
@NoArgsConstructor
public class BiDataSourceCustomSearchDTO {

    /**
     * 指标名称
     */
    private String targetName;

    /**
     * 类型
     */
    private Integer type;

    /**
     * 开始创建时间
     */
    private LocalDateTime createTime_begin;

    /**
     * 结束创建时间
     */
    private LocalDateTime createTime_end;
}
