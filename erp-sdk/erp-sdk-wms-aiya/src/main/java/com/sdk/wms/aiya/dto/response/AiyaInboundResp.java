package com.sdk.wms.aiya.dto.response;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * AIYA（爱亚/百世 GLINK）入库单验货明细查询（serviceType={@code GLINK_QUERY_ASN_INSPECT_DETAIL_NOTIFY}）响应结构。
 * <p>
 * 外层 success / code / message，业务数据为 {@code asnInfoList[]}，每个 ASN 下挂 {@code asnItems[]}
 * （SKU × 货物状态 的验货明细行）。数据模型与 wego 一致——按「上架完成时间」分页查询，
 * {@code asnItems} 为验货明细流水，其中 {@code skuStatus} 区分良品（GOOD）/不良品（DAMAGE），
 * 下游据此把良品/不良品分别写入签收记录、直接调拨单与即时库存。
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AiyaInboundResp implements Serializable {

    /**
     * 是否成功
     */
    @JSONField(name = "success")
    private Boolean success;

    /**
     * 操作状态码（失败时返回错误类型）
     */
    @JSONField(name = "code")
    private String code;

    /**
     * 提示信息
     */
    @JSONField(name = "message")
    private String message;

    /**
     * 批量入库单验货结果
     */
    @JSONField(name = "asnInfoList")
    private List<AsnInfoDTO> asnInfoList;

    /**
     * 单个入库单（ASN）验货结果
     */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class AsnInfoDTO implements Serializable {

        /**
         * 入库单状态
         */
        @JSONField(name = "status")
        private String status;

        /**
         * 预计到货时间
         */
        @JSONField(name = "expectReceivedTime")
        private String expectReceivedTime;

        /**
         * 完成收货时间
         */
        @JSONField(name = "receiveTime")
        private String receiveTime;

        /**
         * 外部 ASN 编号
         */
        @JSONField(name = "extAsnNumber")
        private String extAsnNumber;

        /**
         * 客户编码
         */
        @JSONField(name = "customerCode")
        private String customerCode;

        /**
         * 仓库编码
         */
        @JSONField(name = "warehouseCode")
        private String warehouseCode;

        /**
         * 仓库备注
         */
        @JSONField(name = "warehouseNotes")
        private String warehouseNotes;

        /**
         * 用户标识
         */
        @JSONField(name = "extUserId")
        private String extUserId;

        /**
         * 仓库入库单号（爱亚内部单号）
         */
        @JSONField(name = "wmsAsnNumber")
        private String wmsAsnNumber;

        /**
         * 承运商
         */
        @JSONField(name = "carrier")
        private String carrier;

        /**
         * 仓库单据来源，枚举值（ENTRY, API, PO, AUTO_GWMS）
         */
        @JSONField(name = "wmsDocSource")
        private String wmsDocSource;

        /**
         * 参考单号
         */
        @JSONField(name = "referenceNumber")
        private String referenceNumber;

        /**
         * 入库单分类
         */
        @JSONField(name = "asnType")
        private String asnType;

        /**
         * 入库单号（回显我方创建时下发的 asnNumber，即发货单号/referenceNo）
         */
        @JSONField(name = "asnNumber")
        private String asnNumber;

        /**
         * 验货明细
         */
        @JSONField(name = "asnItems")
        private List<AsnItemDTO> asnItems;
    }

    /**
     * ASN 验货明细行（SKU × 货物状态）
     */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class AsnItemDTO implements Serializable {

        /**
         * 原产国
         */
        @JSONField(name = "originCountry")
        private String originCountry;

        /**
         * 批次号
         */
        @JSONField(name = "batchNo")
        private String batchNo;

        /**
         * 生产日期
         */
        @JSONField(name = "mfgDate")
        private String mfgDate;

        /**
         * 到期日期
         */
        @JSONField(name = "expDate")
        private String expDate;

        /**
         * 商品编码
         */
        @JSONField(name = "sku")
        private String sku;

        /**
         * 验货数量
         */
        @JSONField(name = "quantity")
        private Integer quantity;

        /**
         * 货物状态：良品-GOOD，不良品-DAMAGE
         */
        @JSONField(name = "skuStatus")
        private String skuStatus;

        /**
         * 备注
         */
        @JSONField(name = "remark")
        private String remark;

        /**
         * ERP 侧回填字段：完成收货时间（由 InitHandler 从所属 {@link AsnInfoDTO#getReceiveTime()} 复制到每行，
         * 以便下游把 asnItems 拍平到 detail_list_json 后仍能取到收货时间生成签收流水）。非爱亚原始接口字段。
         */
        @JSONField(name = "receiveTime")
        private String receiveTime;

        /**
         * ERP 侧回填字段：所属入库单号（asnNumber），用于生成唯一的签收流水 ID（thirdId）。非爱亚原始接口字段。
         */
        @JSONField(name = "asnNumber")
        private String asnNumber;
    }
}
