package com.erp.model.plm.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2023/2/21 11:10
 */
@Data
@NoArgsConstructor
public class ProductPlanGroupSerachDTO implements Serializable {

    /**
     * 年份
     */
    private Integer year;

    /**
     * 产品经理ID
     */
    private List<String> chargeId;

    /**
     * 产品等级ID
     */
    private List<String> gradeId;

    /**
     * 类目ID
     */
    private List<String> categoryId;

    /**
     * 分组字段（辅助字段）
     */
    private String groupField;

    /**
     * 分组字段（辅助字段）
     */
    private String groupFields;
}
