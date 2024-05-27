package com.sdk.oms.shopee.dto.global.response;

import cn.hutool.core.annotation.Alias;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author zdy
 * @ClassName FieldList
 * @description: TODO
 * @date 2023年11月29日
 * @version: 1.0
 */
@Data
public class FieldList implements Serializable {
    @Alias("field_type")
    private String field_type;
    @Alias("text")
    private String text;
    @Alias("image_info")
    private List<ImageInfo> imageInfo;
}
