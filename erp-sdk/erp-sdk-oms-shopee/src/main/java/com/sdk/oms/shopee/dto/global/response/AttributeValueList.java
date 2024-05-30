package com.sdk.oms.shopee.dto.global.response;

import cn.hutool.core.annotation.Alias;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serializable;

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
public class AttributeValueList implements Serializable {
    @Alias( "value_id")
    private int valueId;
    @Alias( "original_value_name")
    private String originalValueName;
    @Alias( "value_unit")
    private String valueUnit;
}
