package com.erp.model.admin.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * @Classname SysFindMenuDTO
 * @Description TODO
 * @Date 2022-07-19 10:25
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class SysFindMenuDTO implements Serializable {

    @NotNull(message = "菜单属性不能为空")
    @Range(min = 1, max = 4, message = "类型错误只能是1到3")
    private Integer menuType;

}
