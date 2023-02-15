package com.erp.model.workflow.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * @Classname FindProcessDTO
 * @Description TODO
 * @Date 2023-01-30 16:04
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class FindProcessDTO implements Serializable {

    @NotBlank(message = "业务类型不能为空")
    private String businessType;

    @NotBlank(message = "业务类型不能为空")
    private String platform;
}
