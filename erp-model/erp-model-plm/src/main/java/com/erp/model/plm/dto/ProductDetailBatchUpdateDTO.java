package com.erp.model.plm.dto;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
public class ProductDetailBatchUpdateDTO implements Serializable {
    /**
     * 主键id
     */
    @NotEmpty(message = "主键id不能为空")
    private List<String> ids;

    /**
     * 修改的字段名称编号
     * wms/common/enumDropDown?type=ProductBatchField
     */
    @NotBlank(message = "修改的字段名称编号不能为空")

    private String updateFiledCode;

    /**
     * 字段内容
     */
    private Object values;
}
