package com.erp.model.plm.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Max;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 * @Classname SaveProductTemplateDTO
 * @Description TODO
 * @Date 2022-09-20 14:15
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class SaveProductTemplateDTO implements Serializable {

    /**
     * 模板名称
     */
    @NotBlank(message = "模板名不能为空")
    @Size(max=50,message ="最多50字符")
    private String templateName;

    /**
     * 产品id
     */
    @NotBlank(message = "产品id不能为空")
    private String productId;
}
