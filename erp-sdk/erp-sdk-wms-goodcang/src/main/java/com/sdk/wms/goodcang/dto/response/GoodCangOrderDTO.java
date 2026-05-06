package com.sdk.wms.goodcang.dto.response;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
@ToString
@AllArgsConstructor
public class GoodCangOrderDTO implements Serializable {

    @JSONField(name = "order_code")
    private String orderCode;
    @JSONField(name = "order_type")
    private String orderType;
    @JSONField(name = "packing_type")
    private Integer packingType;
    @JSONField(name = "order_status")
    private String orderStatus;
    @JSONField(name = "reference_no")
    private String referenceNo;
    @JSONField(name = "ship_status")
    private String shipStatus;
    @JSONField(name = "site_source")
    private String siteSource;
    @JSONField(name = "carrier_name")
    private String carrierName;
    @JSONField(name = "delivery_type")
    private Integer deliveryType;
    @JSONField(name = "distributor_type")
    private Integer distributorType;
    @JSONField(name = "is_warehouse_packing")
    private Integer isWarehousePacking;
    @JSONField(name = "platform")
    private String platform;
    @JSONField(name = "shipping_method")
    private String shippingMethod;
    @JSONField(name = "tracking_no")
    private String trackingNo;
    @JSONField(name = "warehouse_code")
    private String warehouseCode;
    @JSONField(name = "wp_code")
    private String wpCode;
    @JSONField(name = "wp_code_list")
    private List<?> wpCodeList;
    @JSONField(name = "fba_shipment_id")
    private String fbaShipmentId;
    @JSONField(name = "fba_shipment_id_create_time")
    private String fbaShipmentIdCreateTime;
    @JSONField(name = "is_change_label")
    private Integer isChangeLabel;
    @JSONField(name = "is_transparency_label")
    private Integer isTransparencyLabel;
    @JSONField(name = "platform_order_code")
    private String platformOrderCode;
    @JSONField(name = "platform_warehouse_code")
    private String platformWarehouseCode;
    @JSONField(name = "property_label")
    private String propertyLabel;
    @JSONField(name = "is_optional_board")
    private String isOptionalBoard;
    @JSONField(name = "area_code")
    private String areaCode;
    @JSONField(name = "consignee_address1")
    private String consigneeAddress1;
    @JSONField(name = "consignee_address2")
    private String consigneeAddress2;
    @JSONField(name = "consignee_address3")
    private String consigneeAddress3;
    @JSONField(name = "consignee_city")
    private String consigneeCity;
    @JSONField(name = "consignee_company")
    private String consigneeCompany;
    @JSONField(name = "consignee_country_code")
    private String consigneeCountryCode;
    @JSONField(name = "consignee_country_name")
    private String consigneeCountryName;
    @JSONField(name = "consignee_district")
    private String consigneeDistrict;
    @JSONField(name = "consignee_doorplate")
    private String consigneeDoorplate;
    @JSONField(name = "consignee_email")
    private String consigneeEmail;
    @JSONField(name = "consignee_last_name")
    private String consigneeLastName;
    @JSONField(name = "consignee_name")
    private String consigneeName;
    @JSONField(name = "consignee_phone")
    private String consigneePhone;
    @JSONField(name = "consignee_state")
    private String consigneeState;
    @JSONField(name = "consignee_zipcode")
    private String consigneeZipcode;
    @JSONField(name = "items")
    private List<ItemsDTO> items;
    @JSONField(name = "age_detection")
    private Integer ageDetection;
    @JSONField(name = "LiftGate")
    private Integer liftGate;
    @JSONField(name = "estimated_arrival_date")
    private String estimatedArrivalDate;
    @JSONField(name = "estimated_arrival_time")
    private String estimatedArrivalTime;
    @JSONField(name = "is_insurance")
    private Integer isInsurance;
    @JSONField(name = "gc_insurance_value")
    private String gcInsuranceValue;
    @JSONField(name = "insurance_value")
    private String insuranceValue;
    @JSONField(name = "insurance_currency_code")
    private String insuranceCurrencyCode;
    @JSONField(name = "customer_package_type")
    private String customerPackageType;
    @JSONField(name = "customer_package_requirement")
    private Integer customerPackageRequirement;
    @JSONField(name = "customer_package_code")
    private String customerPackageCode;
    @JSONField(name = "vas")
    private VasDTO vas;
    @JSONField(name = "is_euro_label")
    private String isEuroLabel;
    @JSONField(name = "vat_change_info")
    private VatChangeInfoDTO vatChangeInfo;
    @JSONField(name = "attachment_list")
    private List<AttachmentListDTO> attachmentList;
    @JSONField(name = "charge_details")
    private ChargeDetailsDTO chargeDetails;
    @JSONField(name = "ooh_info")
    private OohInfoDTO oohInfo;
    @JSONField(name = "orderBoxInfo")
    private List<OrderBoxInfoDTO> orderBoxInfo;
    @JSONField(name = "pallet_list")
    private List<PalletListDTO> palletList;
    @JSONField(name = "pallet_truck_info")
    private PalletTruckInfoDTO palletTruckInfo;
    @JSONField(name = "sender_info")
    private SenderInfoDTO senderInfo;
    @JSONField(name = "sn_info_list")
    private List<SnInfoListDTO> snInfoList;
    @JSONField(name = "truck_attachment_list")
    private List<TruckAttachmentListDTO> truckAttachmentList;
    @JSONField(name = "truck_info")
    private TruckInfoDTO truckInfo;
    @JSONField(name = "vc_info")
    private VcInfoDTO vcInfo;
    @JSONField(name = "ascan_time")
    private String ascanTime;
    @JSONField(name = "dscan_time")
    private String dscanTime;
    @JSONField(name = "order_weight")
    private String orderWeight;
    @JSONField(name = "payment_time")
    private String paymentTime;
    @JSONField(name = "plank_time")
    private String plankTime;
    @JSONField(name = "shipper_time")
    private String shipperTime;
    @JSONField(name = "validity_type")
    private Integer validityType;
    @JSONField(name = "date_create")
    private String dateCreate;
    @JSONField(name = "date_modify")
    private String dateModify;
    @JSONField(name = "date_release")
    private String dateRelease;
    @JSONField(name = "date_shipping")
    private String dateShipping;
    @JSONField(name = "product_quanlity")
    private Integer productQuanlity;
    @JSONField(name = "abnormal_problem_reason")
    private String abnormalProblemReason;
    @JSONField(name = "order_desc")
    private String orderDesc;
    @JSONField(name = "consignee_type")
    private String consigneeType;
    @JSONField(name = "customer_po_code")
    private String customerPoCode;
    @JSONField(name = "extend_props")
    private ExtendPropsDTO extendProps;
    @JSONField(name = "fee_details")
    private FeeDetailsDTO feeDetails;

    @NoArgsConstructor
    @Data
    public static class VasDTO {
        @JSONField(name = "box_mark_num")
        private Integer boxMarkNum;
        @JSONField(name = "label_replacement_option")
        private Integer labelReplacementOption;
        @JSONField(name = "logistics_recommendation_option")
        private Integer logisticsRecommendationOption;
    }

    @NoArgsConstructor
    @Data
    public static class VatChangeInfoDTO {
        @JSONField(name = "customs_clearance_file_id_list")
        private List<?> customsClearanceFileIdList;
        @JSONField(name = "ioss_number")
        private String iossNumber;
        @JSONField(name = "pid_number")
        private String pidNumber;
        @JSONField(name = "recipient_eori")
        private String recipientEori;
        @JSONField(name = "recipient_eori_country")
        private String recipientEoriCountry;
        @JSONField(name = "recipient_vat")
        private String recipientVat;
        @JSONField(name = "recipient_vat_country")
        private String recipientVatCountry;
        @JSONField(name = "sent_number")
        private String sentNumber;
        @JSONField(name = "shipper_company_name")
        private String shipperCompanyName;
        @JSONField(name = "shipper_eori")
        private String shipperEori;
        @JSONField(name = "shipper_vat")
        private String shipperVat;
        @JSONField(name = "shipper_vat_city")
        private String shipperVatCity;
        @JSONField(name = "shipper_vat_company_name")
        private String shipperVatCompanyName;
        @JSONField(name = "shipper_vat_country")
        private String shipperVatCountry;
        @JSONField(name = "shipper_vat_street_address1")
        private String shipperVatStreetAddress1;
        @JSONField(name = "shipper_vat_street_address2")
        private String shipperVatStreetAddress2;
        @JSONField(name = "shipper_vat_zip_code")
        private String shipperVatZipCode;
    }

    @NoArgsConstructor
    @Data
    public static class ChargeDetailsDTO {
        @JSONField(name = "calc_volume")
        private String calcVolume;
        @JSONField(name = "charge_list")
        private List<ChargeListDTO> chargeList;
        @JSONField(name = "currency_code")
        private String currencyCode;
        @JSONField(name = "goods_sum_volume_process")
        private String goodsSumVolumeProcess;
        @JSONField(name = "total_fee")
        private String totalFee;
        @JSONField(name = "use_algorithm_size")
        private Integer useAlgorithmSize;
        @JSONField(name = "volume_coefficient")
        private String volumeCoefficient;
        @JSONField(name = "volume_factor")
        private String volumeFactor;
        @JSONField(name = "vol_weight")
        private String volWeight;

        @NoArgsConstructor
        @Data
        public static class ChargeListDTO {
            @JSONField(name = "billing_time")
            private String billingTime;
            @JSONField(name = "fee_amount")
            private String feeAmount;
            @JSONField(name = "fee_code")
            private String feeCode;
            @JSONField(name = "fee_group")
            private String feeGroup;
            @JSONField(name = "fee_name")
            private String feeName;
            @JSONField(name = "ut_code")
            private String utCode;
        }
    }

    @NoArgsConstructor
    @Data
    public static class OohInfoDTO {
        @JSONField(name = "ooh_name")
        private String oohName;
        @JSONField(name = "ooh_code")
        private String oohCode;
        @JSONField(name = "ooh_business_time")
        private String oohBusinessTime;
        @JSONField(name = "ooh_business_time_list")
        private List<OohBusinessTimeListDTO> oohBusinessTimeList;
        @JSONField(name = "ooh_city")
        private String oohCity;
        @JSONField(name = "ooh_country")
        private String oohCountry;
        @JSONField(name = "ooh_distance")
        private String oohDistance;
        @JSONField(name = "ooh_distance_in_km")
        private String oohDistanceInKm;
        @JSONField(name = "ooh_postcode")
        private String oohPostcode;
        @JSONField(name = "ooh_state")
        private String oohState;
        @JSONField(name = "ooh_street1")
        private String oohStreet1;
        @JSONField(name = "ooh_street2")
        private String oohStreet2;
        @JSONField(name = "ooh_street3")
        private String oohStreet3;
        @JSONField(name = "suburb")
        private String suburb;

        @NoArgsConstructor
        @Data
        public static class OohBusinessTimeListDTO {
            @JSONField(name = "day_of_week")
            private String dayOfWeek;
            @JSONField(name = "day_of_week_text")
            private String dayOfWeekText;
            @JSONField(name = "work_time_list")
            private List<?> workTimeList;
        }
    }

    @NoArgsConstructor
    @Data
    public static class PalletTruckInfoDTO {
        @JSONField(name = "actual_dscan_time")
        private String actualDscanTime;
        @JSONField(name = "estimated_dscan_time")
        private String estimatedDscanTime;
        @JSONField(name = "inquiry_order")
        private String inquiryOrder;
    }

    @NoArgsConstructor
    @Data
    public static class SenderInfoDTO {
        @JSONField(name = "name")
        private String name;
        @JSONField(name = "phone")
        private String phone;
    }

    @NoArgsConstructor
    @Data
    public static class TruckInfoDTO {
        @JSONField(name = "fba_warehouse_code")
        private String fbaWarehouseCode;
        @JSONField(name = "reference_id")
        private String referenceId;
        @JSONField(name = "seller_name")
        private String sellerName;
    }

    @NoArgsConstructor
    @Data
    public static class VcInfoDTO {
        @JSONField(name = "estimated_pick_up_date")
        private String estimatedPickUpDate;
        @JSONField(name = "packing_info_list")
        private List<PackingInfoListDTO> packingInfoList;
        @JSONField(name = "po_code")
        private String poCode;

        @NoArgsConstructor
        @Data
        public static class PackingInfoListDTO {
            @JSONField(name = "box_mark")
            private String boxMark;
            @JSONField(name = "packing_line_list")
            private List<PackingLineListDTO> packingLineList;

            @NoArgsConstructor
            @Data
            public static class PackingLineListDTO {
                @JSONField(name = "product_sku")
                private String productSku;
                @JSONField(name = "quantity")
                private String quantity;
            }
        }
    }

    @NoArgsConstructor
    @Data
    public static class ExtendPropsDTO {
        @JSONField(name = "key_1")
        private String key1;
        @JSONField(name = "key_2")
        private String key2;
    }

    @NoArgsConstructor
    @Data
    public static class FeeDetailsDTO {
        @JSONField(name = "currency_code")
        private String currencyCode;
        @JSONField(name = "DT")
        private Integer dt;
        @JSONField(name = "FSC")
        private Integer fsc;
        @JSONField(name = "OPF")
        private String opf;
        @JSONField(name = "OTF")
        private String otf;
        @JSONField(name = "RSF")
        private Integer rsf;
        @JSONField(name = "SHIPPING")
        private Integer shipping;
        @JSONField(name = "totalFee")
        private String totalFee;
    }

    @NoArgsConstructor
    @Data
    public static class ItemsDTO {
        @JSONField(name = "product_sku")
        private String productSku;
        @JSONField(name = "quantity")
        private Integer quantity;
        @JSONField(name = "batch_list")
        private String batchList;
        @JSONField(name = "box_gauge_qty")
        private String boxGaugeQty;
        @JSONField(name = "cargo_type")
        private String cargoType;
        @JSONField(name = "change_label_qty_piece")
        private Integer changeLabelQtyPiece;
        @JSONField(name = "euro_terms_code")
        private String euroTermsCode;
        @JSONField(name = "euro_terms_company")
        private String euroTermsCompany;
        @JSONField(name = "fba_product_code")
        private String fbaProductCode;
        @JSONField(name = "gpsr_label_url")
        private String gpsrLabelUrl;
        @JSONField(name = "hs_code")
        private String hsCode;
        @JSONField(name = "item_id")
        private String itemId;
        @JSONField(name = "label_replacement_qty")
        private String labelReplacementQty;
        @JSONField(name = "product_declared_value")
        private String productDeclaredValue;
        @JSONField(name = "transaction_id")
        private String transactionId;
    }

    @NoArgsConstructor
    @Data
    public static class AttachmentListDTO {
        @JSONField(name = "customer_file_name")
        private String customerFileName;
        @JSONField(name = "file_category")
        private String fileCategory;
        @JSONField(name = "path")
        private String path;
    }

    @NoArgsConstructor
    @Data
    public static class OrderBoxInfoDTO {
        @JSONField(name = "box_mark")
        private String boxMark;
        @JSONField(name = "box_no")
        private String boxNo;
        @JSONField(name = "fba_box_mark")
        private String fbaBoxMark;
        @JSONField(name = "ob_add_time")
        private String obAddTime;
        @JSONField(name = "ob_height")
        private Integer obHeight;
        @JSONField(name = "ob_length")
        private Integer obLength;
        @JSONField(name = "ob_qty")
        private Integer obQty;
        @JSONField(name = "ob_weight")
        private Integer obWeight;
        @JSONField(name = "ob_width")
        private Integer obWidth;
        @JSONField(name = "order_code")
        private String orderCode;
        @JSONField(name = "package_code")
        private String packageCode;
        @JSONField(name = "product_barcode")
        private String productBarcode;
        @JSONField(name = "product_sku")
        private String productSku;
        @JSONField(name = "tracking_number")
        private String trackingNumber;
    }

    @NoArgsConstructor
    @Data
    public static class PalletListDTO {
        @JSONField(name = "package_no_list")
        private List<?> packageNoList;
        @JSONField(name = "package_qty")
        private String packageQty;
        @JSONField(name = "pallet_no")
        private String palletNo;
        @JSONField(name = "size")
        private String size;
        @JSONField(name = "weight")
        private String weight;
    }

    @NoArgsConstructor
    @Data
    public static class SnInfoListDTO {
        @JSONField(name = "product_sku")
        private String productSku;
        @JSONField(name = "serial_number")
        private String serialNumber;
    }

    @NoArgsConstructor
    @Data
    public static class TruckAttachmentListDTO {
        @JSONField(name = "customer_file_name")
        private String customerFileName;
        @JSONField(name = "file_category")
        private String fileCategory;
        @JSONField(name = "path")
        private String path;
    }
}
