package com.erp.model.wms.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * AIYA（爱亚/百世 GLINK）创建/修改 2C 出库单（serviceType={@code GLINK_CREATE_ORDER_NOTIFY}）请求报文。
 * <p>
 * 字段以爱亚开放平台接口文档为准（2026-07-21 截图核对），并按《爱亚海外仓对接方案文档》6.3.3
 * 映射表收敛：只保留<strong>官方必填</strong>字段，以及产品文档明确映射到数大臣字段的可选字段；
 * 官方文档里大量非必填且产品方案未提及的字段（udf*、代收货款、保价、托盘等）不纳入本 DTO，
 * 避免无业务赋值的空字段污染请求体（fastjson 默认也忽略 null）。
 * <p>
 * {@code customerCode} 由 {@code AiyaOpenApiService} 从授权信息统一注入 bizData，本 DTO 不重复承载。
 * <p>
 * 与方案文档翻译稿的主要差异（以官方截图为准）：
 * <ul>
 *     <li>{@code orderNumber} 为<strong>必填</strong>唯一键（客户交易物流订单号），对应三方仓发货单号；
 *         方案文档把「指定物流单号」也叫 orderNumber 属于误译，官方对应
 *         {@link ShippingInstructions#trackingNumber}；</li>
 *     <li>收件/物流/寄件字段分别嵌套在 {@code shipTo}/{@code shippingInstructions}/{@code shipFrom}，
 *         不是方案文档里的扁平结构；</li>
 *     <li>{@code shipFrom} 官方标必填；其中 {@code name}/{@code company} 须<strong>至少填一个</strong>；
 *         {@code shipTo.district} 官方为可选（方案文档标必填，按官方）。</li>
 * </ul>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiyaOutboundSaveDTO implements Serializable {

    /**
     * 客户交易生成物流订单号（必填）：客户系统保证唯一。
     * <p>
     * 映射：三方仓发货单号。爱亚创建成功不回传独立出库单号，后续查询/取消均以此为 key。
     */
    private String orderNumber;

    /**
     * 仓库编码（必填）。映射：B2C 销售订单的发货仓库（平台仓库编码）。
     */
    private String warehouseCode;

    /**
     * 客户销售平台编号 / 客户单号（可选）。映射：平台订单号等业务参考号。
     */
    private String extOrderNumber;

    /**
     * 下单时间（必填）。格式：{@code yyyy-MM-dd'T'HH:mm:ssZ}，如 {@code 2017-05-01T16:00:00+0800}。
     * 映射：B2C 销售订单的付款时间。
     */
    private String orderTime;

    /**
     * 销售渠道（可选）。映射：B2C 销售订单的销售平台。
     */
    private String salesChannel;

    /**
     * 店铺（可选）。映射：B2C 销售订单的店铺。
     */
    private String storeNumber;

    /**
     * 物流指示（必填）。
     */
    private ShippingInstructions shippingInstructions;

    /**
     * 收件人信息（必填）。
     */
    private ShipTo shipTo;

    /**
     * 出库明细（必填），至少一行。
     */
    private List<Item> items;

    /**
     * 寄件人信息（必填）。方案文档：默认取海外仓库维度寄件地址。
     */
    private ShipFrom shipFrom;

    /**
     * 面单等附件（可选）。{@code shippingLabelSource=ATTACHMENT} 时必须且只能下发一个 Shipping Label 附件。
     * 一旦下发 {@link FileItem}，该条的 {@code fileType} 必填（取值见 {@link FileItem} 常量）。
     * 映射：B2C 销售订单的物流面单。
     */
    private List<FileItem> files;

    /**
     * 物流指示。
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ShippingInstructions implements Serializable {

        /**
         * 承运商（必填）。平台有面单时推送对应物流公司；海外仓取号时传物流渠道映射名。
         */
        private String carrier;

        /**
         * 承运商服务（必填）。无特殊要求填 {@code STD}。
         */
        private String carrierService;

        /**
         * 发货标签来源（必填）：
         * <ul>
         *     <li>{@code ATTACHMENT}：订单自带面单，须同时下发运单号、carrier，且只能有一个面单附件；</li>
         *     <li>{@code API}：通过 GWMS 向快递系统取号（对应物流渠道「是否推海外仓面单=否」）；</li>
         *     <li>{@code WMS_GEN}：由 GWMS 配置模板生成面单。</li>
         * </ul>
         */
        private String shippingLabelSource;

        /**
         * 运单号（可选）。平台有面单时附带；对应方案文档误称为 orderNumber 的「指定物流单号」。
         */
        private String trackingNumber;
    }

    /**
     * 收件人。
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ShipTo implements Serializable {

        /** 收件人名称（必填） */
        private String name;

        /** 收件人手机（必填） */
        private String mobileNumber;

        /** 收件人邮箱（可选） */
        private String email;

        /** 收件人详细地址（必填） */
        private String streetLine1;

        /** 收件人地址行 2（可选） */
        private String streetLine2;

        /**
         * 收件人区/县（可选；官方截图为否，方案文档标必填，以官方为准）。
         */
        private String district;

        /** 收件人城市（必填） */
        private String city;

        /** 收件人省/州（必填） */
        private String state;

        /** 收件人邮编（必填，下游系统校验） */
        private String postalCode;

        /** 收件人国家编码（必填） */
        private String countryCode;
    }

    /**
     * 出库明细行。方案文档映射为 {@code sku}/{@code quantity}；官方建单截图未单独展开 items 子字段，
     * 当前按方案文档这两项落地。
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Item implements Serializable {

        /** SKU 编码（必填），取 sku_mapping 中的平台 SKU */
        private String sku;

        /** 数量（必填） */
        private Integer quantity;
    }

    /**
     * 寄件人。
     * <p>
     * 地址类必填：{@code streetLine1}/{@code city}/{@code state}/{@code postalCode}/{@code countryCode}。
     * 身份类约束：{@code name} 与 {@code company} <strong>必须至少填一个</strong>（可同时填）。
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ShipFrom implements Serializable {

        /**
         * 寄件人姓名。与 {@link #company} 二选一必填（可同时填）。
         */
        private String name;

        /**
         * 寄件人公司名。与 {@link #name} 二选一必填（可同时填）。
         */
        private String company;

        /** 寄件人详细地址（必填） */
        private String streetLine1;

        /** 寄件人城市（必填） */
        private String city;

        /** 寄件人省/州（必填） */
        private String state;

        /** 寄件人邮编（必填） */
        private String postalCode;

        /** 寄件人所在国家（必填）；方案文档：默认取仓库所属国家 */
        private String countryCode;
    }

    /**
     * 附件（面单等）。
     * <p>
     * 顶层 {@code files[]} 本身可选；一旦下发某条附件对象，该条的 {@link #fileType} 为必填
     * （{@code shippingLabelSource=ATTACHMENT} 时必须且只能下发一个 Shipping Label 附件）。
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FileItem implements Serializable {

        /** 面单 */
        public static final String FILE_TYPE_SHIPPING_LABEL = "Shipping Label";
        /** 发票 */
        public static final String FILE_TYPE_COMMERCIAL_INVOICE = "Commercial Invoice";
        /** 提货单 */
        public static final String FILE_TYPE_BILL_OF_LADING = "Bill of Lading";
        /** 商品标签 */
        public static final String FILE_TYPE_PRODUCT_LABEL = "Product Label";
        /** 箱贴 */
        public static final String FILE_TYPE_CARTON_LABEL = "Carton Label";
        /** 装箱单 */
        public static final String FILE_TYPE_PACKING_LIST = "Packing List";
        /** 板贴 */
        public static final String FILE_TYPE_PALLET_LABEL = "Pallet Label";
        /** 其它 */
        public static final String FILE_TYPE_OTHER = "Other";

        /**
         * 文件类型（下发 files 条目时必填）。取值（英文原样）：
         * <ul>
         *     <li>{@link #FILE_TYPE_SHIPPING_LABEL} — 面单</li>
         *     <li>{@link #FILE_TYPE_COMMERCIAL_INVOICE} — 发票</li>
         *     <li>{@link #FILE_TYPE_BILL_OF_LADING} — 提货单</li>
         *     <li>{@link #FILE_TYPE_PRODUCT_LABEL} — 商品标签</li>
         *     <li>{@link #FILE_TYPE_CARTON_LABEL} — 箱贴</li>
         *     <li>{@link #FILE_TYPE_PACKING_LIST} — 装箱单</li>
         *     <li>{@link #FILE_TYPE_PALLET_LABEL} — 板贴</li>
         *     <li>{@link #FILE_TYPE_OTHER} — 其它</li>
         * </ul>
         */
        private String fileType;

        /** 文件名 */
        private String fileName;

        /** 文件 URL（可直接访问的面单地址） */
        private String fileUrl;
    }
}
