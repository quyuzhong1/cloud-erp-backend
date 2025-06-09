package com.sdk.oms.shopee.dto.product.response;

import cn.hutool.core.annotation.Alias;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;

/**
 * @author zdy
 * @ClassName AttributeValue
 * @description: TODO
 * @date 2023年10月24日
 * @version: 1.0
 */
@Data
@ToString(callSuper = true)
public class AttributeValue implements Serializable {

    @Alias( "value_id")
    private Long valueId;

    @Alias( "original_value_name")
    private String originalValueName;

    @Alias( "value_unit")
    private String valueUnit;
}
