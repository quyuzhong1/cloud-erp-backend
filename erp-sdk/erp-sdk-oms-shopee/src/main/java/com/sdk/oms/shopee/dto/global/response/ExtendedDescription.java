package com.sdk.oms.shopee.dto.global.response;

import cn.hutool.core.annotation.Alias;
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
    @Alias("field_list")
    private List<FieldList> fieldList;
}
