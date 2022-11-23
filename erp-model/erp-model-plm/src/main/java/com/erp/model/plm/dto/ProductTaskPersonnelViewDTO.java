package com.erp.model.plm.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * @author Will
 * @version 1.0
 * @description: 产品任务视图
 * @date 2022/11/22 18:09
 */
@Data
@NoArgsConstructor
public class ProductTaskPersonnelViewDTO implements Serializable {

    /**
     * 人员id
     */
    private String chargeId;
    /**
     * 人员名称（分组条件）
     */
    private String chargeName;

    /**
     * 子集
     */
    private List<ProductTaskPersonnelChildDTO> childrenList;

}
