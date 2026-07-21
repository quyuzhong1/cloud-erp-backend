package com.sdk.wms.aiya.dto.response;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * AIYA（爱亚/百世 GLINK）入库单批量查询（serviceType={@code GLINK_BATCH_QUERY_ASN_NOTIFY}）响应结构。
 * <p>
 * 外层 {@code success}/{@code code}/{@code message}/{@code total}，业务数据为 {@code asnInfoList[]}
 * （当前页入库单）。文档把 {@code asnInfoList} 内部结构重复罗列多次，此处按去重后的单一结构建模；
 * 各类 {@code udf1~udf12} 自定义扩展字段与 {@code fileBase64}（默认空）等无业务用途字段不纳入本 DTO。
 * <p>
 * {@code asnLineItems} 为入库单明细（申报/期望行，含 {@code quantity}），{@code asnItemReceiveDetails}
 * 为实际收货明细（含收货/上架数量），二者的 {@code skuStatus} 均区分良品（GOOD）/不良品（DAMAGE）；
 * 下游签收流水以入库单明细 {@code asnLineItems} 为准。
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AiyaInboundResp implements Serializable {

    /**
     * 成功标志
     */
    @JSONField(name = "success")
    private Boolean success;

    /**
     * 错误编码（失败时返回）
     */
    @JSONField(name = "code")
    private String code;

    /**
     * 错误消息
     */
    @JSONField(name = "message")
    private String message;

    /**
     * 总记录数（执行成功时有值），供调用方分页翻页判断
     */
    @JSONField(name = "total")
    private Integer total;

    /**
     * 当前页入库单信息
     */
    @JSONField(name = "asnInfoList")
    private List<AsnInfoDTO> asnInfoList;

    /**
     * 单个入库单（ASN）信息
     */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class AsnInfoDTO implements Serializable {

        /**
         * 仓库编码
         */
        @JSONField(name = "warehouseCode")
        private String warehouseCode;

        /**
         * 参考单号
         */
        @JSONField(name = "refNumber")
        private String refNumber;

        /**
         * 仓库备注
         */
        @JSONField(name = "warehouseNotes")
        private String warehouseNotes;

        /**
         * 入库单类型，枚举：SUPPLIER_RECEIPT、RETURN
         */
        @JSONField(name = "asnType")
        private String asnType;

        /**
         * 入库单明细（申报/期望行）
         */
        @JSONField(name = "asnLineItems")
        private List<AsnLineItemDTO> asnLineItems;

        /**
         * 备注
         */
        @JSONField(name = "remark")
        private String remark;

        /**
         * 收货时间（yyyy-MM-dd HH:mm:ss）
         */
        @JSONField(name = "receiveTime")
        private String receiveTime;

        /**
         * 用户编码
         */
        @JSONField(name = "customerCode")
        private String customerCode;

        /**
         * 状态，枚举：VOIDED、SendingToWMS、Received、Fulfilled、Error、Voided
         */
        @JSONField(name = "status")
        private String status;

        /**
         * 入库单号（回显我方创建时下发的 asnNumber）
         */
        @JSONField(name = "asnNumber")
        private String asnNumber;

        /**
         * 入库单商品种类数
         */
        @JSONField(name = "itemLineQty")
        private Integer itemLineQty;

        /**
         * 封箱号
         */
        @JSONField(name = "sealNumber")
        private String sealNumber;

        /**
         * 集装箱号
         */
        @JSONField(name = "containerNumber")
        private String containerNumber;

        /**
         * 上架完成时间
         */
        @JSONField(name = "putawayTime")
        private String putawayTime;

        /**
         * 运单号（一般用于退货入库单）
         */
        @JSONField(name = "trackingNumber")
        private String trackingNumber;

        /**
         * 活动信息
         */
        @JSONField(name = "handlings")
        private HandlingDTO handlings;

        /**
         * 批次信息
         */
        @JSONField(name = "batchinfo")
        private List<BatchInfoDTO> batchInfo;

        /**
         * 上架阶段：PENDING 等待中 / IN PROGRESS 上架中 / COMPLETED 上架完成
         */
        @JSONField(name = "putawayStage")
        private String putawayStage;

        /**
         * 单据阶段：DUE_OUT/PARTIALLY_ALLOCATED/ALLOCATED/PICK/PICKING/PICKED/PACK/PACKING/
         * ROUTING/PACKED/SHIPPING/SHIPPED/CLOSED/OPEN/CREATED
         */
        @JSONField(name = "stage")
        private String stage;

        /**
         * WMS 状态：VALID/HELD/SUSPENDED/CANCELLED
         */
        @JSONField(name = "wmsStatus")
        private String wmsStatus;

        /**
         * 外部单号
         */
        @JSONField(name = "extAsnNumber")
        private String extAsnNumber;

        /**
         * 外部用户 ID
         */
        @JSONField(name = "extUserId")
        private String extUserId;

        /**
         * WMS 入库单号（爱亚内部单号）
         */
        @JSONField(name = "wmsAsnNumber")
        private String wmsAsnNumber;

        /**
         * 承运商
         */
        @JSONField(name = "carrier")
        private String carrier;

        /**
         * PO 单号
         */
        @JSONField(name = "poNumber")
        private String poNumber;

        /**
         * 调整信息
         */
        @JSONField(name = "adjustInfos")
        private List<AdjustInfoDTO> adjustInfos;

        /**
         * 参考单号
         */
        @JSONField(name = "referenceNumber")
        private String referenceNumber;

        /**
         * 是否发送超收邮件
         */
        @JSONField(name = "overEmailFlag")
        private Boolean overEmailFlag;

        /**
         * 到货方式
         */
        @JSONField(name = "capacityType")
        private String capacityType;

        /**
         * 是否预打板
         */
        @JSONField(name = "preBuilt")
        private Boolean preBuilt;

        /**
         * 托盘数量
         */
        @JSONField(name = "palletQty")
        private String palletQty;

        /**
         * 箱数
         */
        @JSONField(name = "cartonQty")
        private Integer cartonQty;

        /**
         * 预计收货时间
         */
        @JSONField(name = "expectedReceiptDate")
        private String expectedReceiptDate;

        /**
         * 仓库单据来源：ENTRY、API、PO、AUTO_GWMS
         */
        @JSONField(name = "wmsDocSource")
        private String wmsDocSource;

        /**
         * 出库单号
         */
        @JSONField(name = "orderNumber")
        private String orderNumber;

        /**
         * 收货明细（实际收货/上架流水），下游签收以此为准
         */
        @JSONField(name = "asnItemReceiveDetails")
        private List<AsnItemReceiveDetailDTO> asnItemReceiveDetails;

        /**
         * 附加信息
         */
        @JSONField(name = "additionalInfoList")
        private List<AdditionalInfoDTO> additionalInfoList;

        /**
         * 文件列表
         */
        @JSONField(name = "asnAdditionalFiles")
        private List<AsnAdditionalFileDTO> asnAdditionalFiles;
    }

    /**
     * 入库单明细（申报/期望行）
     */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class AsnLineItemDTO implements Serializable {

        @JSONField(name = "sku")
        private String sku;

        /**
         * 数量
         */
        @JSONField(name = "quantity")
        private Integer quantity;

        /**
         * 行号
         */
        @JSONField(name = "lineNo")
        private String lineNo;

        /**
         * 上架量
         */
        @JSONField(name = "putawayedQuantity")
        private Integer putawayedQuantity;

        /**
         * 期望数量
         */
        @JSONField(name = "expectedQuantity")
        private Integer expectedQuantity;

        /**
         * SKU 状态：良品 GOOD / 不良品 DAMAGE
         */
        @JSONField(name = "skuStatus")
        private String skuStatus;

        /**
         * 生产日期
         */
        @JSONField(name = "mfgDate")
        private String mfgDate;

        /**
         * 失效日期
         */
        @JSONField(name = "expDate")
        private String expDate;

        /**
         * 批次号
         */
        @JSONField(name = "batchNo")
        private String batchNo;

        /**
         * 原产地国家
         */
        @JSONField(name = "originCountry")
        private String originCountry;

        /**
         * PO 行号
         */
        @JSONField(name = "poLineNo")
        private String poLineNo;

        /**
         * 外部单号
         */
        @JSONField(name = "poNumber")
        private String poNumber;

        /**
         * 入库单号
         */
        @JSONField(name = "asnNumber")
        private String asnNumber;

        /**
         * 箱唛
         */
        @JSONField(name = "markCode")
        private String markCode;

        /**
         * SKU 长
         */
        @JSONField(name = "skuLength")
        private Double skuLength;

        /**
         * SKU 宽
         */
        @JSONField(name = "skuWidth")
        private Double skuWidth;

        /**
         * SKU 高
         */
        @JSONField(name = "skuHeight")
        private Double skuHeight;

        /**
         * 长度单位
         */
        @JSONField(name = "dimensionUnit")
        private String dimensionUnit;

        /**
         * SKU 重量
         */
        @JSONField(name = "skuWeight")
        private Double skuWeight;

        /**
         * 重量单位
         */
        @JSONField(name = "weightUnit")
        private String weightUnit;

        /**
         * EA 规格 barCode
         */
        @JSONField(name = "barCodeEA")
        private String barCodeEA;

        /**
         * 备注
         */
        @JSONField(name = "remark")
        private String remark;

        /**
         * ERP 侧回填字段：完成收货时间（由 InitHandler 从所属 {@link AsnInfoDTO#getReceiveTime()} 复制到每行，
         * 以便下游把 asnLineItems 拍平到 detail_list_json 后仍能取到收货时间生成签收流水）。非爱亚原始接口字段。
         */
        @JSONField(name = "receiveTime")
        private String receiveTime;
    }

    /**
     * 活动信息
     */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class HandlingDTO implements Serializable {

        /**
         * 到达时间
         */
        @JSONField(name = "arrivalDate")
        private String arrivalDate;

        /**
         * 容量类型
         */
        @JSONField(name = "capacityType")
        private String capacityType;

        /**
         * 是否预制
         */
        @JSONField(name = "preBuilt")
        private Boolean preBuilt;

        /**
         * 托盘数量
         */
        @JSONField(name = "palletQty")
        private Integer palletQty;

        /**
         * 箱数
         */
        @JSONField(name = "cartonQty")
        private Integer cartonQty;

        /**
         * 容积
         */
        @JSONField(name = "volume")
        private Double volume;

        /**
         * 长度单位
         */
        @JSONField(name = "lengthUnit")
        private String lengthUnit;

        /**
         * 重量
         */
        @JSONField(name = "weight")
        private Double weight;

        /**
         * 重量单位
         */
        @JSONField(name = "weightUnit")
        private String weightUnit;

        /**
         * 备注
         */
        @JSONField(name = "note")
        private String note;

        /**
         * 活动明细
         */
        @JSONField(name = "handlingDetails")
        private List<HandlingDetailDTO> handlingDetails;
    }

    /**
     * 活动明细
     */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class HandlingDetailDTO implements Serializable {

        /**
         * 活动类型
         */
        @JSONField(name = "chargeType")
        private String chargeType;

        /**
         * 活动单位
         */
        @JSONField(name = "chargeUnit")
        private String chargeUnit;

        /**
         * 活动数量
         */
        @JSONField(name = "chargeQty")
        private Double chargeQty;

        /**
         * 活动工时
         */
        @JSONField(name = "chargeManHour")
        private Double chargeManHour;

        /**
         * 活动备注
         */
        @JSONField(name = "chargeNote")
        private String chargeNote;
    }

    /**
     * 批次信息
     */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class BatchInfoDTO implements Serializable {

        /**
         * 行号
         */
        @JSONField(name = "lineNo")
        private String lineNo;

        @JSONField(name = "sku")
        private String sku;

        /**
         * SKU 批次数量
         */
        @JSONField(name = "qty")
        private Double qty;

        /**
         * 货物状态
         */
        @JSONField(name = "stockStatus")
        private String stockStatus;

        /**
         * 批次号
         */
        @JSONField(name = "batchNumber")
        private String batchNumber;

        /**
         * 生产日期
         */
        @JSONField(name = "mfgDate")
        private String mfgDate;

        /**
         * 失效日期
         */
        @JSONField(name = "expDate")
        private String expDate;

        /**
         * 生产地国家
         */
        @JSONField(name = "originalCountry")
        private String originalCountry;
    }

    /**
     * 调整信息
     */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class AdjustInfoDTO implements Serializable {

        @JSONField(name = "sku")
        private String sku;

        /**
         * 调整数量
         */
        @JSONField(name = "adjustQty")
        private String adjustQty;

        /**
         * SKU 状态：良品 GOOD / 不良品 DAMAGE
         */
        @JSONField(name = "skuStatus")
        private String skuStatus;

        /**
         * 生产日期
         */
        @JSONField(name = "mfgDate")
        private String mfgDate;

        /**
         * 过期日期
         */
        @JSONField(name = "expDate")
        private String expDate;

        /**
         * 批次号
         */
        @JSONField(name = "batchNo")
        private String batchNo;

        /**
         * 原产地国家
         */
        @JSONField(name = "originCountry")
        private String originCountry;
    }

    /**
     * 收货明细（实际收货/上架流水）
     */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class AsnItemReceiveDetailDTO implements Serializable {

        /**
         * 行号
         */
        @JSONField(name = "lineNo")
        private String lineNo;

        /**
         * 明细 ID
         */
        @JSONField(name = "detailId")
        private String detailId;

        /**
         * 商品编码
         */
        @JSONField(name = "sku")
        private String sku;

        /**
         * 货物状态：良品 GOOD / 不良品 DAMAGE
         */
        @JSONField(name = "skuStatus")
        private String skuStatus;

        /**
         * 收货数量
         */
        @JSONField(name = "receiveQty")
        private Integer receiveQty;

        /**
         * 收货时间
         */
        @JSONField(name = "receiveTime")
        private String receiveTime;

        /**
         * 上架数量
         */
        @JSONField(name = "putawayQty")
        private Integer putawayQty;

        /**
         * 上架时间
         */
        @JSONField(name = "putawayTime")
        private String putawayTime;

        /**
         * 原产地国家
         */
        @JSONField(name = "originCountry")
        private String originCountry;

        /**
         * 批次号
         */
        @JSONField(name = "batchNo")
        private String batchNo;

        /**
         * 失效时间
         */
        @JSONField(name = "expDate")
        private String expDate;

        /**
         * 生产时间
         */
        @JSONField(name = "mfgDate")
        private String mfgDate;

        /**
         * 供应商
         */
        @JSONField(name = "supplier")
        private String supplier;

        /**
         * 到达时间
         */
        @JSONField(name = "arrivalTime")
        private String arrivalTime;

        /**
         * ERP 侧回填字段：所属入库单号（asnNumber），用于生成唯一签收流水 ID（thirdId）。非爱亚原始接口字段。
         */
        @JSONField(name = "asnNumber")
        private String asnNumber;
    }

    /**
     * 附加信息
     */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class AdditionalInfoDTO implements Serializable {

        /**
         * 文件类型
         */
        @JSONField(name = "infoType")
        private String infoType;

        /**
         * 文件内容
         */
        @JSONField(name = "content")
        private String content;

        /**
         * 文件描述
         */
        @JSONField(name = "note")
        private String note;

        /**
         * 上传时间
         */
        @JSONField(name = "uploadedTime")
        private String uploadedTime;
    }

    /**
     * 文件列表
     */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class AsnAdditionalFileDTO implements Serializable {

        /**
         * 附件名称
         */
        @JSONField(name = "fileName")
        private String fileName;

        /**
         * 附件 URL
         */
        @JSONField(name = "fileUrl")
        private String fileUrl;

        /**
         * 附件类型
         */
        @JSONField(name = "fileType")
        private String fileType;

        /**
         * 附件格式（PDF、GIF 等）
         */
        @JSONField(name = "fileFormat")
        private String fileFormat;

        /**
         * 是否更新附件
         */
        @JSONField(name = "isUpdate")
        private String isUpdate;

        /**
         * SN 列表（excel 文件 URL）
         */
        @JSONField(name = "snUrl")
        private String snUrl;
    }
}
