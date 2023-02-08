package com.erp.common.dto.base;

import com.erp.common.annotation.StateEnumValue;
import lombok.Data;
import lombok.NoArgsConstructor;

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


    private String field;

    @StateEnumValue(strValues = {"DESC","ASC"},message = "排序值有误")
    private String sort;
}
