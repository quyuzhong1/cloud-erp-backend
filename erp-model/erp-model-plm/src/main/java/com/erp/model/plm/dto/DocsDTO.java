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

    /**
     * 表id
     * @author yl
     * @date 2022-10-09 10:51
     */
    private String id;

    /**
     * 文档名
     * @author yl
     * @date 2022-10-09 10:51
     */
    @NotBlank(message = "文档名不能为空")
    private String name;

    /**
     * 是否是系统文档
     * 1 是  0 不是
     */
    private Integer isSys;
}
