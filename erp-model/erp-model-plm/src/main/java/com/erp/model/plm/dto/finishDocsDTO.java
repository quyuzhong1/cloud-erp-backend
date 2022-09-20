package com.erp.model.plm.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * @Classname finishDocsDTO
 * @Description TODO
 * @Date 2022-09-15 17:21
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class finishDocsDTO  implements Serializable {

    @NotBlank(message = "文档id不能为空")
    private String docsId;

    @NotBlank(message = "文档名不能为空")
    private String docsName;

    private String lcId;
}
