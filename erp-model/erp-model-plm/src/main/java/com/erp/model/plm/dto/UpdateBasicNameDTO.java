package com.erp.model.plm.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * @Classname UpdateBasicCategoryDTO

 * @Date 2022-09-13 14:03
 * @Created by yl
 */
@Data
@NoArgsConstructor
@Validated
public class UpdateBasicNameDTO {

    /**
     * 表id
     *
     * @author yl
     * @date 2022-10-09 10:45
     * @param null
     * @return
     */
    private String id;

    /**
     * 名称
     *
     * @author yl
     * @date 2022-10-09 10:45
     * @param null
     * @return
     */
    @NotBlank(message = "名称不能为空")
    private String name;

    /**
     * 分类代码
     */
    @Size(max = 4, message = "分类代码长度不能超过4个字符")
    private String code;

}
