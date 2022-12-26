package com.erp.model.dmp.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2022/12/15 13:12
 */
@Data
@NoArgsConstructor
public class DmpShopInfoChangeDTO {

    @NotBlank(message = "店铺id不能为空")
    private String id;

    /**
     * 负责人id
     */
    @NotBlank(message = "负责人id不能为空")
    private String chargeId;

    /**
     * 启用时间
     */
    @NotNull(message = "启用时间不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate enableTime;
}
