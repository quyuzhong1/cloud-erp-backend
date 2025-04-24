package com.sdk.oms.shopee.dto.product.response;

import cn.hutool.core.annotation.Alias;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;
import java.util.List;

/**
 * @author zdy
 * @ClassName Attribute
 * @description: TODO
 * @date 2023年10月24日
 * @version: 1.0
 */
@Data
@ToString(callSuper = true)
public class Attribute implements Serializable {

    @Alias( "attribute_id")
    private Long attributeId;

    @Alias( "original_attribute_name")
    private String originalAttributeName;

    /**
     * This is to indicate whether this attribute is mandantory.
     */
    @Alias( "is_mandatory")
    private boolean isMandatory;

    @Alias( "attribute_value_list")
    List<AttributeValue> attributeValueList;
}
