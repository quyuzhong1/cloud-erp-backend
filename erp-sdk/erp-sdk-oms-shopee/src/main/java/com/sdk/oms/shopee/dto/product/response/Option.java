package com.sdk.oms.shopee.dto.product.response;

import cn.hutool.core.annotation.Alias;
import lombok.Data;

import java.io.Serializable;

/**
 * @author zdy
 * @ClassName Option
 * @description: TODO
 * @date 2024年10月15日
 * @version: 1.0
 */
@Data
public class Option implements Serializable {
    @Alias( "option")
    private String option;
    @Alias( "image")
    private ImageInfo image;
}
