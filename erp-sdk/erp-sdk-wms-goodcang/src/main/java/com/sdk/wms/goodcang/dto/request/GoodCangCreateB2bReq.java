package com.sdk.wms.goodcang.dto.request;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GoodCangCreateB2bReq {

    //订单参考号
    @JSONField(name = "reference_no")
    private String referenceNo;

    //平台
    @JSONField(name = "packing_type")
    private String packingType;

    //是否直接审核
    //默认为0
    //审核通过之后，不可编辑
    @JSONField(name = "verify")
    private Integer verify;

    //配送仓库代码
    //参考getWarehouse。（当使用物流优选服务时，可选填项，参见vas.logistics_recommendation_option选项）。
    //当使用物流优选时，且入参提供了仓库，则系统在该仓库范围内选择适用的物流方式。
    @JSONField(name = "warehouse_code")
    @NotNull(message = "配送仓库代码不能为空")
    private String warehouseCode;

    @JSONField(name = "recipient_info")
    private RecipientInfo recipientInfo;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class RecipientInfo {

        @JSONField(name = "address1")
        private String address1;

        @JSONField(name = "city")
        private String city;

        @JSONField(name = "country_code")
        private String countryCode;

        @JSONField(name = "name")
        private String name;

        @JSONField(name = "phone")
        private String phone;

        @JSONField(name = "province")
        private String province;

        @JSONField(name = "zipcode")
        private String zipcode;

    }

    @JSONField(name = "delivery_service")
    private DeliveryService deliveryService;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class DeliveryService {

        @JSONField(name = "is_insurance")
        private Integer isInsurance;

        @JSONField(name = "is_signature")
        private Integer isSignature;

        @JSONField(name = "sm_code")
        private String smCode;

    }


    @JSONField(name = "warehouse_service")
    private WarehouseService warehouseService;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class WarehouseService {

        /**
         * 每箱张贴货件标签数
         */
        @JSONField(name = "box_mark_num")
        private Integer boxMarkNum;

        /**
         * 是否换标
         */
        @JSONField(name = "is_change_label")
        private Integer isChangeLabel;

        /**
         * 整单SKU汇总列表
         */
        @JSONField(name = "item_list")
        private List<GoodCangCreateB2bReq.Item> itemList;

        /**
         * 装箱信息列表
         */
        @JSONField(name = "packing_list")
        private List<GoodCangCreateB2bReq.Packing> packingList;

    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class Item {

        @JSONField(name = "product_sku")
        private String productSku;

        @JSONField(name = "quantity")
        private Integer quantity;

    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class Packing {

        /**
         * 箱唛号
         */
        @JSONField(name = "box_mark")
        private String boxMark;

        /**
         * 箱序号
         */
        @JSONField(name = "box_no")
        private Integer boxNo;

        /**
         * 箱唛参考号
         */
        @JSONField(name = "box_ref_mark")
        private String boxRefMark;

        /**
         * 货件标签文件ID
         */
        @JSONField(name = "shipment_file_id")
        private String shipmentFileId;

        /**
         * 货件标签文件列表
         */
        @JSONField(name = "shipment_file_list")
        private List<GoodCangCreateB2bReq.ShipmentFile> shipmentFileList;

        /**
         * 物流文件ID
         */
        @JSONField(name = "logistics_file_id")
        private String logisticsFileId;

        /**
         * 报关文件ID
         */
        @JSONField(name = "customs_file_id")
        private String customsFileId;

        /**
         * 箱内SKU列表
         */
        @JSONField(name = "packing_line_list")
        private List<GoodCangCreateB2bReq.PackingLine> packingLineList;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class ShipmentFile {

        /**
         * 贴标要求
         */
        @JSONField(name = "labelling_require")
        private String labellingRequire;

        /**
         * 标签尺寸
         */
        @JSONField(name = "label_size")
        private String labelSize;

        /**
         * 货件标签文件ID
         */
        @JSONField(name = "shipment_file_id")
        private String shipmentFileId;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class PackingLine {

        /**
         * 三方仓SKU
         */
        @JSONField(name = "product_sku")
        private String productSku;

        /**
         * 当前箱内该SKU数量
         */
        @JSONField(name = "quantity")
        private Integer quantity;
    }

    @JSONField(name = "other_info")
    private OtherInfo otherInfo;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class OtherInfo {

        /**
         * 订单备注
         */
        @JSONField(name = "order_desc")
        private String orderDesc;

        /**
         * 装箱文件ID
         */
        @JSONField(name = "packing_file_id")
        private Integer packingFileId;

    }
}
