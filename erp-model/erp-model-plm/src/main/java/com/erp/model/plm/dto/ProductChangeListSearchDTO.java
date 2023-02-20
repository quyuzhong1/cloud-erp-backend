package com.erp.model.plm.dto;

import com.common.core.anno.StateEnumValue;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * @Classname ProductChangeListSearchDTO
 * @Description TODO
 * @Date 2023-01-28 17:18
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class ProductChangeListSearchDTO implements Serializable {

    @NotBlank(message = "变更类型不能为空")
    @StateEnumValue(strValues = {"sku", "bom"}, message = "类型有误")
    private String type;

    private String searchKeyword;


}
