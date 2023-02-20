package com.erp.model.plm.dto;

import com.common.core.anno.StateEnumValue;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * 编辑变更
 *
 * @Classname
 * @Description TODO
 * @Date 2023-01-30 11:46
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class UpdateChangeDTO implements Serializable {


    @NotBlank(message = "id不能为空")
    private String id;


    @NotBlank(message = "源数据id不能为空")
    private String sourceId;

    @NotBlank(message = "变更类型不能为空")
    @StateEnumValue(strValues = {"sku", "bom"}, message = "类型有误")
    private String type;


    @NotBlank(message = "变更实体json 字符串不能为空")
    private String  detailsJson;
}
