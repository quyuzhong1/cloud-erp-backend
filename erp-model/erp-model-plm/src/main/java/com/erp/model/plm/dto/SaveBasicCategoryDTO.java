package com.erp.model.plm.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Max;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * @Classname SaveBasicCategoryDTO
 * @Description TODO
 * @Date 2022-09-13 11:33
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class SaveBasicCategoryDTO  {



    /**
     * 分类名
     */
    @NotBlank(message = "分类名称不能为空")
    @Size(min = 0, max = 50, message = "分类名称长度不能超过50个字符")
    private String name;

    /**
     * 父 级id
     */
    @NotBlank(message = "父级id不能为空")
    private String pid;

}
