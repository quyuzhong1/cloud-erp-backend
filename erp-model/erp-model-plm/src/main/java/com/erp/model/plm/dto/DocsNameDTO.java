package com.erp.model.plm.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * @Classname DocsNameDTO
 * @Description TODO
 * @Date 2022-09-22 12:15
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class DocsNameDTO implements Serializable {


    /**
     * 文档名
     */
    @NotBlank(message = "文档名不能为空")
    private String name;

    /**
     * 产品id
     */
    @NotBlank(message = "产品id不能为空")
    private String productId;
}
