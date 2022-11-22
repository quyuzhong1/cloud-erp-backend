package com.erp.model.plm.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2022/11/22 18:43
 */
@Data
@NoArgsConstructor
public class ProductTaskViewSearchDTO implements Serializable {

    /**
     * 人员
     */
    private List<String> nameList;

    /**
     * 产品
     */
    private List<String> productList;

    /**
     * 审核状态
     */
    private List<String> approvalStatusList;

    /**
     * 产品状态
     */
    private List<String> productStatusList;

    /**
     * 查询类型（1按人员查看，2按产品查看，3按阶段查看，4按量产入库时间查看）
     */
    private String type;

}
