package com.erp.model.plm.dto;

import com.common.core.anno.StateEnumValue;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @Classname SysProductFieldDTO
 * @Description TODO
 * @Date 2022-09-15 12:09
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class SysProductFieldDTO {


    /**
     * 表id
     */
    private String id;

    /**
     * 字段名称
     */
    @NotBlank(message = "字段名称不能为空")
    private String name;

    /**
     * 使用范围 1：全局  0： 单个
     */
    @NotNull(message = "使用范围不能为空")
    @StateEnumValue(intValues = {0, 1}, message = "使用范围只能是0或者1")
    private Integer scope;

    /**
     * 是否必填  1：是 0 不是
     */
    @NotNull(message = "是否必填不能为空")
    @StateEnumValue(intValues = {0, 1}, message = "是否必填只能是0或者1")
    private Integer ifRequired;


    /**
     * 字段类型 1：单选框 2 文本框 3：多选框 4 ： 日期  5：成员
     */
    //字段类型 1：单选框 2 文本框 3：多选框 4 ： 日期  5：成员
    @StateEnumValue(intValues = {1, 2, 3, 4, 5}, message = "字段类型出错")
    private Integer type;


    /**
     * 内容
     */

    private List<String> contents;


}
