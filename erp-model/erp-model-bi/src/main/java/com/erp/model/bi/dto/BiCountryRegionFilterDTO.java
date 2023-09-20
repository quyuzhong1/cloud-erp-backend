package com.erp.model.bi.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

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

    /**
     * 子区域代号
     */
    public String subregionCode;
}
