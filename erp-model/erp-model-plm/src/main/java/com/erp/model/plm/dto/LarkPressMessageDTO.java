package com.erp.model.plm.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;


/**
 * @author Cloud
 * 飞书催办入参类
 */
@Data
@NoArgsConstructor
public class LarkPressMessageDTO {
    /**
     * 催办业务ID
     * 如 taskId
     */
    @NotBlank(message = "业务ID不能为空")
    private String businessId;

    /**
     * 催办业务类型
     *  product_task 任务
     */
    @NotBlank(message = "业务类型不能为空")
    private String businessType;
}
