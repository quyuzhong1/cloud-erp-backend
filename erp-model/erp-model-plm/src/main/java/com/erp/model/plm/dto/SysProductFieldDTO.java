package com.erp.model.plm.dto;

import com.erp.common.annotation.StateEnumValue;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.util.List;

/**
 * @Classname SysProductFieldDTO
 * @Description TODO
 * @Date 2022-09-15 12:09
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class SysProductFieldDTO  {


    private String id;

    @NotBlank(message = "字段名称不能为空")
    private String name;

    @StateEnumValue(intValues = {0, 1}, message = "使用范围只能是0或者1")
    private Integer scope;

    @StateEnumValue(intValues = {0, 1}, message = "是否必填只能是0或者1")
    private Integer ifRequired;


    //字段类型 1：单选框 2 文本框 3：多选框 4 ： 日期  5：成员
    @StateEnumValue(intValues = {1, 2, 3, 4, 5}, message = "字段类型出错")
    private Integer type;



    private List<String> contents;


}
