package com.erp.model.bi.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.collections.CollectionUtils;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjuster;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

/**
 * 销售数据数据统计条件构建类
 * @author Cloud
 */
@Data
@NoArgsConstructor
public class TargetFinancialDTO implements Serializable {

    private LocalDate month;

    /**
     * 事业部
     */
    private List<String> department;

    /**
     * 站点
     */
    private List<String> site;

    /**
     * 店铺编号
     */
    private List<String> shopName;



}
