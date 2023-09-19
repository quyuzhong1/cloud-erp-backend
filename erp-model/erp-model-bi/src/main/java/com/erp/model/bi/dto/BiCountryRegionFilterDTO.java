package com.erp.model.bi.dto;

import com.common.business.dto.base.PermissionsDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

/**
 * BI国家区域 筛选条件
 *
 * @author Jim
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class BiCountryRegionFilterDTO extends BiFilterDTO {

    /**
     * 区域代号
     */
    public String regionCode;
}
