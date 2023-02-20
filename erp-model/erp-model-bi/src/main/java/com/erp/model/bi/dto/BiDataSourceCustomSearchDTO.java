package com.erp.model.bi.dto;

import com.erp.common.business.dto.base.BaseSearchDTO;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2022/12/14 16:47
 */
@Data
@NoArgsConstructor
public class BiDataSourceCustomSearchDTO extends BaseSearchDTO {

    /**
     * 指标名称
     */
    private String targetName;

    /**
     * 数据类型（0市场数据，1供应链数据，2经营数据，3财务数据）
     */
    @NotNull(message = "数据类型不能为空")
    private Integer dataType;

    /**
     * 类型(1年，2季度，3月，4周，5日)
     */
    @NotNull(message = "类型不能为空")
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
