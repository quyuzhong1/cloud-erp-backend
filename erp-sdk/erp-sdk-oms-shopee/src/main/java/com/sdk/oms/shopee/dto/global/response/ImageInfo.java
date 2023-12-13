package com.sdk.oms.shopee.dto.global.response;

import com.alibaba.fastjson.annotation.JSONField;
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
    @JSONField(name ="image_info")
    private String image_info;
    @JSONField(name ="image_url")
    private String image_url;
}
