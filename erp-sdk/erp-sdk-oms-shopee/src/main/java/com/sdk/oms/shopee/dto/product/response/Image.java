package com.sdk.oms.shopee.dto.product.response;

import cn.hutool.core.annotation.Alias;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class Image implements Serializable {

    public static final long serialVersionUID = 1L;

    /**
     * image url
     */
    @Alias( "image_url_list")
    private List<String> imageUrlList;

    /**
     * image url
     */
    @Alias( "image_id_list")
    private List<String> imageIdList;
}
