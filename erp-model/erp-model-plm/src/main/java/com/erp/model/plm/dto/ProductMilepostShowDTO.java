package com.erp.model.plm.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2022/12/5 10:38
 */
@Data
@NoArgsConstructor
public class ProductMilepostShowDTO {

    /**
     * 产品状态名称
     */
    private String statusName;

    /**
     * 序号
     */
    private Integer seq;

    /**
     * 里程碑
     */
    private List<ProductMilepostDTO> list;
}
