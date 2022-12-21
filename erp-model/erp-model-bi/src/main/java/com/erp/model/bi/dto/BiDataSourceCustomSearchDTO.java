package com.erp.model.bi.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
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
     * 类型(1年，2季度，3月，4周，5日)
     */
    private Integer type;

    /**
     * 开始创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime_begin;

    /**
     * 结束创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime_end;
}
