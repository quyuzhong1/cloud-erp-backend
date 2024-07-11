package com.erp.oms.aliexpress.dto.response;

import cn.hutool.core.annotation.Alias;
import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

/**
 * @author Lambda
 * @Classname AliExpressProduct
 * @Description TODO
 * @Date 2023-11-30 9:00
 * @Created by yl
 */
@Data
@Accessors(chain = true)
@NoArgsConstructor
public class AliExpressProduct implements Serializable {



    /**
     * 创建日期
     */
    @SerializedName("gmt_create")
    @Alias("gmt_create")
    private String gmtCreate;

    /**
     * 商品最后更新时间
     */
    @SerializedName("gmt_modified")
    @Alias("gmt_modified")
    private String gmtModified;


    /**
     * 图片URL.静态单图主图个数为1,动态多图主图个数为2-6. 多个图片url用‘;’分隔符连接。
     */
    @SerializedName("image_u_r_ls")
    @Alias("image_u_r_ls")
    private String imageUrls;



    /**
     * 商品ID
     */
    @SerializedName("product_id")
    @Alias("product_id")
    private Long productId;


    /**
     *
     * 商品标题。
     */
    @SerializedName("subject")
    @Alias("subject")
    private String subject;

    /**
     * 产品的状态，包括onSelling（正在销售），offline（已下架），auditing（审核中），editingRequired（审核不通过）
     */
    @SerializedName("product_status_type")
    @Alias("product_status_type")
    private String productStatusType;


    /**
     * sku重量，单位公斤
     */
    @SerializedName("gross_weight")
    @Alias("gross_weight")
    private String grossWeight;

    /**
     * sku物流尺寸-高，单位cm
     */
    @SerializedName("package_height")
    @Alias("package_height")
    private Integer packageHeight;

    /**
     * sku物流尺寸-宽，单位cm。
     */
    @SerializedName("package_width")
    @Alias("package_width")
    private Integer packageWidth;

    /**
     *sku物流尺寸-长，单位cm
     */
    @SerializedName("package_length")
    @Alias("package_length")
    private Integer packageLength;


    @SerializedName("aeop_ae_product_s_k_us")
    @Alias("aeop_ae_product_s_k_us")
    private ProductSku productSku;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class ProductSku {

        @SerializedName("aeop_ae_product_sku")
        @Alias("aeop_ae_product_sku")
        private List<AliExpressProductDetail>  productDetailList;
    }
}
