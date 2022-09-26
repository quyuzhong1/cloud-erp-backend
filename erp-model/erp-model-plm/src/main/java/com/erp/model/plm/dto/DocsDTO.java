package com.erp.model.plm.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

/**
 * @Classname DocsDTO
 * @Description TODO
 * @Date 2022-09-15 9:48
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class DocsDTO {

    private String id;

    @NotBlank(message = "文档名不能为空")
    private String name;

    private Integer isSys;
}
