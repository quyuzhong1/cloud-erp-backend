package com.erp.model.plm.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;

/**
 * @Description 产品图片信息请求参数
 * @Author Luo_WG
 * @Date 2022/9/26 16:38
 **/
@Data
@NoArgsConstructor
public class ProductImagesDTO implements Serializable {

    /**
     * 主键id 无id：新增 有id：修改
     */
    private String id;

    /**
     * 产品表id
     */
    private String productId;

    /**
     * 产品sku明细表id
     */
    private String skuId;

    /**
     * 图片地址
     */
    private String imagesUrl;

    private static final long serialVersionUID = 1L;
}