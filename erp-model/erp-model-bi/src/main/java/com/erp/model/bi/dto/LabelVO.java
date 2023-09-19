package com.erp.model.bi.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * @author zdy
 * @ClassName ProductRefLabelVO
 * @description: 产品标签列表实体
 * @date 2023年09月18日
 * @version: 1.0
 */

@Data
@NoArgsConstructor
public class LabelVO implements Serializable {
    /**
     * 关系id
     */
    private String id;
    /**
     * 标签id
     */
    @NotNull(message = "标签id不能为空")
    private String labelId;
    /**
     * 标签名称
     */
    private String name;
    /**
     * 标签颜色
     */
    private String color;
    /**
     * 标签等级
     */
    private String level;
    /**
     * sku id
     */
    private String skuId;

    /**
     * 产品id
     */
    private String productId;
}
