package com.sdk.oms.shopee.dto.product.response;

import cn.hutool.core.annotation.Alias;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author zdy
 * @ClassName TierVariation
 * @description: TODO
 * @date 2024年10月15日
 * @version: 1.0
 */
@Data
public class TierVariation implements Serializable {
    @Alias( "option_list")
    private List<Option> optionList;
    @Alias( "name")
    private String name;
}
