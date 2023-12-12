package com.erp.oms.aliexpress.dto.response;

import com.alibaba.fastjson.annotation.JSONField;
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
    @JSONField(name = "gmt_create")
    private String gmtCreate;

    /**
     * 商品最后更新时间
     */
    @JSONField(name = "gmt_modified")
    private String gmtModified;



    /**
     * 图片URL.静态单图主图个数为1,动态多图主图个数为2-6. 多个图片url用‘;’分隔符连接。
     */
    @JSONField(name = "image_u_r_ls")
    private String imageUrls;





    /**
     * 商品ID
     */
    @JSONField(name = "product_id")
    private Long productId;



    /**
     *
     * 商品标题。
     */
    @JSONField(name = "subject")
    private String subject;

    /**
     * 产品的状态，包括onSelling（正在销售），offline（已下架），auditing（审核中），editingRequired（审核不通过）
     */
    @JSONField(name = "product_status_type")
    private String productStatusType;


    /**
     * sku重量，单位公斤
     */
    @JSONField(name = "gross_weight")
    private String grossWeight;

    /**
     * sku物流尺寸-高，单位cm
     */
    @JSONField(name = "package_height")
    private Integer packageHeight;

    /**
     * sku物流尺寸-宽，单位cm。
     */
    @JSONField(name = "package_width")
    private Integer packageWidth;

    /**
     *sku物流尺寸-长，单位cm
     */
    @JSONField(name = "package_length")
    private Integer packageLength;


    @JSONField(name = "aeop_ae_product_s_k_us")
   private List<AliExpressProductDetail>  productDetailList;








}
