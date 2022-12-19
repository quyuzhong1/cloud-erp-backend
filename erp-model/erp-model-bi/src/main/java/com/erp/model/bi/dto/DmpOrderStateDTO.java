package com.erp.model.bi.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2022/12/15 9:35
 */
@Data
@NoArgsConstructor
public class DmpOrderStateDTO {

    @NotBlank(message = "订单id不能为空")
    private String id;

    @NotNull(message = "订单修正状态不能为空")
    private Integer state;
}
