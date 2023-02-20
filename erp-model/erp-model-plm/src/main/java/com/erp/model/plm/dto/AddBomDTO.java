package com.erp.model.plm.dto;

import com.common.core.anno.StateEnumValue;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

/**
 * @Classname AddBomDTO
 * @Description TODO
 * @Date 2023-01-09 12:03
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class AddBomDTO implements Serializable {

    /**
     * 版本
     */
    @NotNull(message = "版本不能为空")
    private Integer version;



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


    @Valid
    private List<BomSkuDTO> skuList;
}
