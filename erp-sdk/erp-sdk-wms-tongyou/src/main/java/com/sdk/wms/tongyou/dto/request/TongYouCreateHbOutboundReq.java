package com.sdk.wms.tongyou.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 通邮 B2B 换标/混装创建出库单（add_order_hb.php）
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TongYouCreateHbOutboundReq {

    private String token;

    private String ck_nums;

    private String deliver_no;

    private String chqd;

    private String country;

    /**
     * 州/省
     */
    private String city;

    /**
     * 城市
     */
    private String district;

    private String zip;

    private String address;

    private String address2;

    private String contact;

    private String mobile;

    private String phone;

    private String email;

    /**
     * 产品标签 PDF URL
     */
    private String file1;

    /**
     * 外箱面单 PDF URL
     */
    private String file2;

    /**
     * 其他附件 PDF URL
     */
    private String file3;

    private String beizhu;

    /**
     * 是否换标：1不换标；2换标
     */
    private String is_hb;

    /**
     * 是否混装：1混装；2不混装
     */
    private String is_hz;

    private List<DeliverProductDTO> deliver_products;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DeliverProductDTO {

        private String nums;

        private String sku;

        private String sku_news;
    }
}
