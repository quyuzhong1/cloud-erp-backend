package com.erp.model.bi.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.apache.commons.lang.StringUtils;

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

    /**
     * 过滤区域
     */
    public boolean filterRegion(BiCountryAnalyzeDTO e) {
        // 区域和子区域都不过滤
        if (StringUtils.isBlank(this.getSubregionCode())){
            return true;
        }
        // 子区域过滤
        if (StringUtils.isNotBlank(this.getSubregionCode())){
            return e.getSubregionCode().equalsIgnoreCase(this.getSubregionCode());
        }
        // 区域过滤
        return e.getRegionCode().equalsIgnoreCase(this.getRegionCode());
    }

    /**
     * 组合文件名参数
     */
    public String convertFileParams() {
        return super.getStartTime().toLocalDate().toString() + '到' + super.getEndTime().toLocalDate().minusDays(1);
    }
}
