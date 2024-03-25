package com.sdk.oms.shopee.dto.product.response;

import cn.hutool.core.annotation.Alias;
import lombok.Data;

import java.io.Serializable;

/**
 * @author zdy
 * @ClassName ImageInfo
 * @description: TODO
 * @date 2023年10月24日
 * @version: 1.0
 */
@Data
public class ImageInfo implements Serializable {
    @Alias( "image_id")
    private String imageId;

    @Alias( "image_url")
    private String imageUrl;
}
