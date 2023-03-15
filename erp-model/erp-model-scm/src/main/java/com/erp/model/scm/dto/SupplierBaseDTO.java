package com.erp.model.scm.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 * @author yl
 * @Classname
 * @Description TODO
 * @Date 2023-03-15 16:36
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class SupplierBaseDTO implements Serializable {

    /**
     * 表id
     */
    private String id;


    /**
     * 名称
     */
    @NotBlank(message = "供应商名称不能为空")
    @Size(max = 50, message = "最大50字符")
    private String name;


    /**
     * 分类id
     */
    @NotBlank(message = "分类id不能为空")
    private String categoryId;


    /**
     * 采购员id
     */
    private String purchaseUserId;
}
