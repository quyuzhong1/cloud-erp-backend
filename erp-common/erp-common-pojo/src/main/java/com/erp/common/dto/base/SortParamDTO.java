package com.erp.common.dto.base;

import com.erp.common.annotation.StateEnumValue;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * @Classname SortParamDTO
 * @Description TODO
 * @Date 2023-02-08 11:51
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class SortParamDTO implements Serializable {

    /**
     * 排序字段
     */
    @NotBlank(message = "排序字段不能为空")
    private String field;

    /**
     * 排序值，DESC,ASC
     */
    @NotBlank(message = "排序值不能为空")
    @StateEnumValue(strValues = {"DESC","ASC"},message = "排序值有误")
    private String sort;

}
