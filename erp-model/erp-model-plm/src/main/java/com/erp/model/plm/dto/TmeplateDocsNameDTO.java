package com.erp.model.plm.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * @author Will
 * @version 1.0
 * @description: 模板交付文件名称
 * @date 2022/11/16 12:27
 */
@Data
@NoArgsConstructor
public class TmeplateDocsNameDTO implements Serializable {


    /**
     * 输出物id
     */
    private String deliveryDocsId;
    /**
     * 文档名
     */
    @NotBlank(message = "文档名不能为空")
    private String name;

    /**
     * 模板id
     */
    @NotBlank(message = "模板id不能为空")
    private String templateId;
}
