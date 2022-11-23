package com.erp.model.plm.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * @author Will
 * @version 1.0
 * @description: 产品里程碑参数DTO
 * @date 2022/11/18 16:23
 */
@Data
@NoArgsConstructor
public class ProductMilepostParamDTO implements Serializable {

    /**
     * 里程碑类型（1创建，2项目立项，3任务名称，4项目归档）
     */
    @NotNull(message = "里程碑类型不能为空")
    private Integer type;

    /**
     * 产品id
     */
    private String productId;

    /**
     * 任务id
     */
    private String taskId;
}
