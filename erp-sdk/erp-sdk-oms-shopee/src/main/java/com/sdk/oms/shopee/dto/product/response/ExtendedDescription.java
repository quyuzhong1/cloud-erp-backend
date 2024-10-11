package com.sdk.oms.shopee.dto.product.response;

import cn.hutool.core.annotation.Alias;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author zdy
 * @ClassName ExtendedDescription
 * @description: TODO
 * @date 2023年10月24日
 * @version: 1.0
 */
@Data
public class ExtendedDescription implements Serializable {

    @Alias( "field_list")
    private List<Field> fieldList;
}
