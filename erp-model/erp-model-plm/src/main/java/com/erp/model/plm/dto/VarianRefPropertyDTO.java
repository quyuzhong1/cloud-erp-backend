package com.erp.model.plm.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @Description 变体类型和变体值关联实体类
 * @Author Luo_WG
 * @Date 2022/9/26 14:37
 **/
@Data
@NoArgsConstructor
public class VarianRefPropertyDTO {
    /**
     * 主键id
     */
    private String id;

    /**
     * 变体属性类型
     */
    private String propertyType;

    /**
     * 变体属性值
     */
    private List<String> varianList;
}
