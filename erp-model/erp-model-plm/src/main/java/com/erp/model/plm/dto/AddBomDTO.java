package com.erp.model.plm.dto;

import com.common.core.anno.StateEnumValue;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.util.List;

/**
 * @Classname AddBomDTO

 * @Date 2023-01-09 12:03
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class AddBomDTO implements Serializable {

    /**
     * 版本
     */
    @NotBlank(message = "版本不能为空")
    private String version;

    /**
     * 主键id
     */
    private String id;

    /**
     * 类型
     * combination 组合
     * single 单品
     */
    @StateEnumValue(strValues = {"combination", "single"}, message = "类型有误")
    private String type;

    /**
     * 提交类型
     */
    @StateEnumValue(strValues = {"submitAudit", "create"}, message = "提交类型有误")
    private String submitType;

    /**
     * 来源类型
     */
    private String sourceType;


    @Valid
    @NotEmpty(message = "BOM子级SKU不能为空")
    private List<BomSkuDTO> skuList;
}
