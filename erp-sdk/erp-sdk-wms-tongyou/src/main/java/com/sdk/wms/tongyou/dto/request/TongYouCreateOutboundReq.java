package com.sdk.wms.tongyou.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TongYouCreateOutboundReq {

    /**
     * 密钥
     */
    private String token;

    /**
     * 仓库编码
     */
    private String ck_nums;

    /**
     * 发货单号
     */
    private String deliver_no;

    /**
     * 渠道编码
     */
    private String chqd;

    /**
     * 国家
     */
    private String country;

    /**
     * 州
     */
    private String city;

    /**
     * 城市
     */
    private String district;

    /**
     * 邮编
     */
    private String zip;

    /**
     * 详细地址
     */
    private String address;

    /**
     * 详细地址2
     */
    private String address2;

    /**
     * 联系人
     */
    private String contact;

    /**
     * 电话
     */
    private String mobile;

    /**
     * 手机
     */
    private String phone;

    /**
     * 收件邮箱
     */
    private String email;

    /**
     * 门牌号
     */
    private String house_number;

    /**
     * 地址类型
     */
    private String address_type;

    /**
     * 签名服务
     */
    private String qmfw;

    /**
     * 订单类型
     */
    private String platform;

    /**
     * ApiID
     */
    private String api_type;

    /**
     * 出货单号
     */
    private String waybill;

    /**
     * 发货面单
     */
    private String pda_url;

    /**
     * 订单备注
     */
    private String beizhu;


    /**
     * 产品列表
     */
    private List<AddDetailDTO> deliver_products;

    @Data
    @NoArgsConstructor
    public static class AddDetailDTO {
        /**
         * 发货数量
         */
        private String nums;

        /**
         * 发货sku
         */
        private String sku;
    }

}
