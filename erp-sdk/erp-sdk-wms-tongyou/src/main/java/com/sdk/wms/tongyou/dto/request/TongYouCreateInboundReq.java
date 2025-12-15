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
public class TongYouCreateInboundReq {


    /**
     * 密钥
     */
    private String token;

    /**
     * 订单数据段
     */
    private List<AddDTO> order_list;


    @Data
    @NoArgsConstructor
    public static class AddDTO {
        /**
         * 参考单号
         */
        private String waybill;
        /**
         * 交换方式
         */
        private String jhfs;
        /**
         * 头程仓库
         */
        private String tcck;
        /**
         * 目的仓库
         */
        private String mdck;
        /**
         * 出货渠道
         */
        private String chqd;
        /**
         * 备注
         */
        private String beizhu;
        /**
         * 国家
         */
        private String country;
        /**
         * 订单类型
         */
        private String order_types;
        /**
         * 产品明细列表
         */
        private List<AddDetailDTO> order_products;
    }

    @Data
    @NoArgsConstructor
    public static class AddDetailDTO {
        /**
         * SKU
         */
        private String ck_sku;
        /**
         * 产品数量
         */
        private String nums;
        /**
         * 箱号
         */
        private String zxh;
        /**
         * 箱子重量
         */
        private String weight;
        /**
         * 箱子长
         */
        private String cc;
        /**
         * 箱子宽
         */
        private String kk;
        /**
         * 箱子高
         */
        private String gg;
    }

}
