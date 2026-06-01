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

        @JSONField(name = "box_mark_num")
        private Integer boxMarkNum;

        @JSONField(name = "is_change_label")
        private Integer isChangeLabel;

        @JSONField(name = "item_list")
        private List<GoodCangCreateB2bReq.Item> itemList;

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

        @JSONField(name = "box_mark")
        private String boxMark;

        @JSONField(name = "box_no")
        private Integer boxNo;

        @JSONField(name = "box_ref_mark")
        private String boxRefMark;

        @JSONField(name = "shipment_file_id")
        private String shipmentFileId;

        @JSONField(name = "shipment_file_list")
        private List<GoodCangCreateB2bReq.ShipmentFile> shipmentFileList;

        @JSONField(name = "logistics_file_id")
        private String logisticsFileId;

        @JSONField(name = "customs_file_id")
        private String customsFileId;

        @JSONField(name = "packing_line_list")
        private List<GoodCangCreateB2bReq.PackingLine> packingLineList;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class ShipmentFile {

        @JSONField(name = "labelling_require")
        private String labellingRequire;

        @JSONField(name = "label_size")
        private String labelSize;

        @JSONField(name = "shipment_file_id")
        private String shipmentFileId;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class PackingLine {

        @JSONField(name = "product_sku")
        private String productSku;

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

        @JSONField(name = "order_desc")
        private String orderDesc;

        @JSONField(name = "packing_file_id")
        private Integer packingFileId;

    }
}
