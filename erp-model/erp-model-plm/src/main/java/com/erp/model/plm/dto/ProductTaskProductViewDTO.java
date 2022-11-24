package com.erp.model.plm.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * @author Will
 * @version 1.0
 * @description: 产品任务视图
 * @date 2022/11/22 18:09
 */
@Data
@NoArgsConstructor
public class ProductTaskProductViewDTO implements Serializable {

    /**
     * 产品id（分组条件）
     */
    private String productId;

    /**
     * 产品名称
     */
    private String productName;

    /**
     * 计划开始日期
     */
    private Date planStartTime;

    /**
     * 计划结束日期
     */
    private Date planEndTime;

    /**
     * 子集
     */
    private List<ProductTaskProductChildDTO> childrenList;

}
