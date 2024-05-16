package com.sdk.oms.shopee.dto.product.response;

import cn.hutool.core.annotation.Alias;
import lombok.Data;

import java.io.Serializable;

/**
 * @author zdy
 * @ClassName DescriptionInfo
 * @description: TODO
 * @date 2023年10月24日
 * @version: 1.0
 */
@Data
public class DescriptionInfo implements Serializable {
    @Alias( "extended_description")
    private ExtendedDescription extendedDescription;
}
