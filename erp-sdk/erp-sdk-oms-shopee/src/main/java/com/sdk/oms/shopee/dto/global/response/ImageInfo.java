package com.sdk.oms.shopee.dto.global.response;

import cn.hutool.core.annotation.Alias;
import lombok.Data;

import java.io.Serializable;

/**
 * @author zdy
 * @ClassName ImageInfo
 * @description: TODO
 * @date 2023年11月29日
 * @version: 1.0
 */
@Data
public class ImageInfo implements Serializable {
    @Alias("image_info")
    private String image_info;
    @Alias("image_url")
    private String image_url;
}
