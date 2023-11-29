package com.sdk.oms.shopee.dto.global.response;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author zdy
 * @ClassName ExtendedDescription
 * @description: TODO
 * @date 2023年11月29日
 * @version: 1.0
 */
@Data
public class ExtendedDescription implements Serializable {
    @JSONField(name ="field_list")
    private List<FieldList> fieldList;
}
