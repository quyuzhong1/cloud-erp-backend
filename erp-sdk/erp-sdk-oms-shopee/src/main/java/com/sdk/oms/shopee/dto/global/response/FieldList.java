package com.sdk.oms.shopee.dto.global.response;

import com.alibaba.fastjson.annotation.JSONField;
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
    @JSONField(name ="field_type")
    private String field_type;
    @JSONField(name ="text")
    private String text;
    @JSONField(name ="image_info")
    private List<ImageInfo> imageInfo;
}
