package com.sdk.wms.tongyou.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

/**
 * 通邮查询出库单响应
 */
@Data
@NoArgsConstructor
@ToString
@AllArgsConstructor
public class TongYouQueryOutboundResp {

    /**
     * 国家
     */
    private String country;

    /**
     * 运费币种
     */
    private String yunfei_bz;

    /**
     * 发货产品明细
     */
    private List<DeliverProductDTO> deliver_products;

    /**
     * 州/省
     */
    private String city;

    /**
     * 重量类型
     */
    private String weight_types;

    /**
     * 门牌号
     */
    private String house_number;

    /**
     * 仓库名称
     */
    private String storage;

    /**
     * 代发长
     */
    private String df_cc;

    /**
     * 代发宽
     */
    private String df_kk;

    /**
     * 代发高
     */
    private String df_gg;

    /**
     * 燃油费
     */
    private String ry_fee;

    /**
     * 运单号/跟踪号
     */
    private String waybill;

    /**
     * 计费重量
     */
    private String jf_weight;

    /**
     * 出库时间
     */
    private String ck_time;

    /**
     * 渠道编码
     */
    private String chqd;

    /**
     * 联系人
     */
    private String contact;

    /**
     * 签名服务
     */
    private String qmfw;

    /**
     * 订单ID
     */
    private String id;

    /**
     * 其他费用
     */
    private String other_fee;

    /**
     * 增值信息
     */
    private List<Object> zz_info;

    /**
     * 邮编
     */
    private String zip;

    /**
     * 订单类型
     */
    private String types;

    /**
     * 详细地址
     */
    private String address;

    /**
     * 地址类型
     */
    private String address_type;

    /**
     * 详细地址2
     */
    private String address2;

    /**
     * 发货单号
     */
    private String deliver_no;

    /**
     * 运费
     */
    private String yunfei;

    /**
     * 电话
     */
    private String mobile;

    /**
     * 重量
     */
    private String weight;

    /**
     * 仓库编码
     */
    private String ck_nums;

    /**
     * 配送费
     */
    private Integer ps_fee;

    /**
     * 增值服务列表
     */
    private List<ZzfwDTO> zzfw_list;

    /**
     * 订单状态
     */
    private String pb;

    /**
     * 手机
     */
    private String phone;

    /**
     * 创建时间（时间戳）
     */
    private String addtime;

    /**
     * 城市
     */
    private String district;

    /**
     * 仓库ID
     */
    private String ck_id;

    /**
     * 快递名称
     */
    private String kd_name;

    /**
     * 合并信息
     */
    private List<Object> hb_info;

    /**
     * 发货面单URL
     */
    private String pda_url;

    /**
     * 发货产品明细
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DeliverProductDTO {

        /**
         * 产品运单号
         */
        private String pwaybill;

        /**
         * 长
         */
        private String cc2;

        /**
         * 宽
         */
        private String kk2;

        /**
         * 高
         */
        private String gg2;

        /**
         * 仓库SKU
         */
        private String ck_sku;

        /**
         * 产品名称
         */
        private String pname;

        /**
         * 发货明细ID
         */
        private String deliver_id;

        /**
         * 发货数量
         */
        private String nums;

    }

    /**
     * 增值服务
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ZzfwDTO {

        /**
         * 增值服务费
         */
        private String zz_price;

        /**
         * 增值服务名称
         */
        private String zz_pname;

    }

}
