package com.erp.model.plm.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2023/2/21 12:05
 */
@Data
@NoArgsConstructor
public class ProductPlanImageDTO implements Serializable {

    /**
     * 规划id
     */
    @NotBlank(message = "规划id不能为空")
    private String id;

    /**
     * 图片URL
     */
    @NotBlank(message = "图片URL不能为空")
    private String imagesUrl;
}
