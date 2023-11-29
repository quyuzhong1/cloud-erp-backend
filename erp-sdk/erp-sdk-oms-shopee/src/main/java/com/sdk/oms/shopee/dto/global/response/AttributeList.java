package com.sdk.oms.shopee.dto.global.response;

import com.alibaba.fastjson.annotation.JSONField;
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
    @JSONField(name = "attribute_id")
    private int attributeId;
    @JSONField(name = "original_attribute_name")
    private String originalAttributeName;
    @JSONField(name = "attribute_value_list")
    private List<AttributeValueList> attributeValueList;
}
