package com.erp.model.bi.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * @Classname DeleteLayoutModuleDTO
 * @Description TODO
 * @Date 2022-12-14 12:15
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class DeleteLayoutModuleDTO implements Serializable {

    @NotBlank(message = "专题id不能为空")
    private String subjectId;


    @NotBlank(message = "布局id不能为空")
    private String layoutId;



    private String moduleId;
}
