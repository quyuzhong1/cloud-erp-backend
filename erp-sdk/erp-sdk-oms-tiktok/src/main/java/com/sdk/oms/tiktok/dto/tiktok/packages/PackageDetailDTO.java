package com.sdk.oms.tiktok.dto.tiktok.packages;

import lombok.Data;

import java.util.List;

@Data
public class PackageDetailDTO {
    private int code; // 响应码
    private ResponseData data; // 数据部分
    private String message; // 响应消息
    private String request_id; // 请求ID

    @Data
    public static class ResponseData{
        private long create_time; // 创建时间
        private String delivery_option_id; // 配送选项ID
        private String delivery_option_name; // 配送选项名称
        private Dimension dimension; // 包裹尺寸
        private String handover_method; // 交接方式
        private boolean has_multi_skus; // 是否包含多个SKU
        private Insurance insurance; // 保险信息
        private String last_mile_tracking_number; // 最后一公里追踪号
        private String note_tag; // 备注标签
        private List<String> order_line_item_ids; // 订单行项目ID列表
        private List<Order> orders; // 订单列表
        private String package_id; // 包裹ID
        private String package_status; // 包裹状态
        private PickupSlot pickup_slot; // 取件时间段
        private Address recipient_address; // 收件人地址
        private Address sender_address; // 发件人地址
        private String shipping_provider_id; // 物流提供商ID
        private String shipping_provider_name; // 物流提供商名称
        private String shipping_type; // 物流类型
        private String split_and_combine_tag; // 拆分和合并标签
        private String tracking_number; // 追踪号
        private long update_time; // 更新时间
        private Weight weight; // 重量信息
    }

    @Data
    public static class Dimension {
        private String height; // 高度
        private String length; // 长度
        private String unit; // 单位
        private String width; // 宽度
    }
    @Data
    public static class Insurance {
        private String claim_status; // 理赔状态
        private String coverage_amount; // 保额
        private boolean is_claim_eligible; // 是否符合理赔条件
        private boolean is_purchased; // 是否已购买保险
    }
    @Data
    public static class Order {
        private String id; // 订单ID
        private List<Sku> skus; // SKU列表
    }

    @Data
    public static class Sku {
        private String id; // SKU ID
        private String image_url; // 图片URL
        private String name; // SKU名称
        private int quantity; // 数量
    }

    @Data
    public static class PickupSlot {
        private long end_time; // 结束时间
        private long start_time; // 开始时间
    }

    @Data
    public static class Address {
        private String address_detail; // 详细地址
        private String address_line1; // 地址行1
        private String address_line2; // 地址行2
        private String address_line3; // 地址行3
        private String address_line4; // 地址行4
        private String full_address; // 完整地址
        private String name; // 姓名
        private String phone_number; // 电话号码
        private String postal_code; // 邮政编码
        private String region_code; // 地区代码
    }

    @Data
    public static class Weight {
        private String unit; // 单位
        private String value; // 值
    }
}
