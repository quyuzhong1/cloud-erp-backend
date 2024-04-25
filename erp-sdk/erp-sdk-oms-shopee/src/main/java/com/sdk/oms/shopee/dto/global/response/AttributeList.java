package com.sdk.oms.shopee.dto.global.response;

import cn.hutool.core.annotation.Alias;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serializable;
import java.util.List;

/**
 * @author zdy
 * @ClassName AttributeList
 * @description: TODO
 * @date 2023年10月19日
 * @version: 1.0
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = false)
public class AttributeList implements Serializable {
    @Alias( "attribute_id")
    private int attributeId;
    @Alias( "original_attribute_name")
    private String originalAttributeName;
    @Alias( "attribute_value_list")
    private List<AttributeValueList> attributeValueList;
}
