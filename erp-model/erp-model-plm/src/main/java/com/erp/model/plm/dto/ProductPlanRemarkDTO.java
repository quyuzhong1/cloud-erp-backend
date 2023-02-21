package com.erp.model.plm.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2023/2/21 10:32
 */
@Data
@NoArgsConstructor
public class ProductPlanRemarkDTO implements Serializable {

    /**
     * 规划id
     */
    @NotBlank(message = "规划id不能为空")
    private String productPlanId;

    /**
     * 规划备注
     */
    @NotBlank(message = "备注不能为空")
    private String remark;
}
