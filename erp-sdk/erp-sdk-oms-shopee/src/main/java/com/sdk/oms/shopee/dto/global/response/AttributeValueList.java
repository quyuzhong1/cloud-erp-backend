package com.sdk.oms.shopee.dto.global.response;

import com.alibaba.fastjson.annotation.JSONField;
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
    @JSONField(name = "value_id")
    private int valueId;
    @JSONField(name = "original_value_name")
    private String originalValueName;
    @JSONField(name = "value_unit")
    private String valueUnit;
}
