package com.erp.model.plm.dto;

import com.erp.common.dto.base.PermissionsDTO;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author Will
 * @version 1.0
 * @description: 项目视图查询条件DTO
 * @date 2022/11/22 18:43
 */
@Data
@NoArgsConstructor
public class ProductTaskViewSearchDTO extends PermissionsDTO {

    /**
     * 人员
     */
    private List<String> chargeIds;

    /**
     * 产品
     */
    private List<String> productIds;

    /**
     * 审核状态
     */
    private List<Integer> approvalStatusList;

    /**
     * 产品状态
     */
    private List<Integer> productStatusList;

    /**
     * 项目状态
     */
    private List<Integer> projectStatusList;

    /**
     * 查询类型（1按人员查看，2按产品查看，3按阶段查看，4按量产入库时间查看）
     */
    private Integer type;

}
