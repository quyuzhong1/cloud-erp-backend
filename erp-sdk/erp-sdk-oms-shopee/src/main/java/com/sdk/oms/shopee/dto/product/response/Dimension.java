package com.sdk.oms.shopee.dto.product.response;

import cn.hutool.core.annotation.Alias;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serializable;

/**
 * @author zdy
 * @ClassName Dimension
 * @description: TODO
 * @date 2023年10月19日
 * @version: 1.0
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = false)
public class Dimension implements Serializable {

    @Alias( "package_length")
    private Integer packageLength;
    @Alias( "package_width")
    private Integer packageWidth;
    @Alias( "package_height")
    private Integer packageHeight;
}
