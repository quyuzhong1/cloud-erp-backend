package com.erp.model.plm.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * @author Will
 * @version 1.0
 * @description: 产品信息审批参数
 * @date 2022/11/28 16:39
 */
@Data
@NoArgsConstructor
public class ProductDetailApproveParamDTO implements Serializable {

    /**
     * 产品信息审批表id
     */
    private String id;

    /**
     * 审批人1
     */
    @NotBlank(message = "审批人1不能为空")
    private String firstApproveId;

    /**
     * 审批人2
     */
    @NotBlank(message = "审批人2不能为空")
    private String secondApproveId;

    /**
     * 审批人3
     */
    @NotBlank(message = "审批人3不能为空")
    private String thirdApproveId;

}
