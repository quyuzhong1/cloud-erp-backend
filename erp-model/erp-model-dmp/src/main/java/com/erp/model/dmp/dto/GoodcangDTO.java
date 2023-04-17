package com.erp.model.dmp.dto;

import com.common.business.enums.OmsPlatformEnum;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * TODO
 *
 * @Author Cloud
 * @Date 2023/3/29 10:45
 **/
public class GoodcangDTO {

    /**
     * 入库单推送 接收数据格式
     * {
     *     "AppToken": "abc123",
     *     "Sign": "md5string",
     *     "MessageType": "StockChangeRecord",
     *     "Message": "{'receiving_code':'RV61-160804-0001','reference_no':'112-8525766-8344247','receiving_status':'1','warehouse_code':'USEA','warehouse_id':1,'addTime':'2016-08-04 16:38:06','updateTime':'2016-08-04 16:56:06','receiving_type':'0','receivingDetail':[{'product_barcode':'61-GX-B00023B1','product_sku':'','box_no':'1','reference_box_no':'','deliveryQty':10,'receiptQty':10,'putawayQty':10,'unsellableQty':0,'sellableQty':10}]}",
     *     "MessageID": "6daf1a43-283b-42a2",
     *     "SendTime": "2022-01-03 09:00:00"
     * }
     */
    @Data
    public static class SendReceivingDTO{
        /**
         * 用户令牌
         */
        @SerializedName("AppToken")
        private String appToken;
        /**
         * 签名字符串
         */
        @SerializedName("Sign")
        private String sign;
        /**
         * 数据类型
         */
        @SerializedName("MessageType")
        private String messageType;
        /**
         * 签名字符串
         */
        @SerializedName("Message")
        private MessageDTO message;
        /**
         * 消息ID
         */
        @SerializedName("MessageID")
        private String messageId;
        /**
         * 发送时间
         */
        @SerializedName("SendTime")
        private LocalDateTime sendTime;
    }

    @Data
    @ToString
    @NoArgsConstructor
    public static class MessageDTO{
        /**
         * 入库单号
         */
        @SerializedName("receiving_code")
        private String receivingCode;
        /**
         * 参考号
         */
        @SerializedName("reference_no")
        private String referenceNo;
        /**
         * 状态
         */
        @SerializedName("receiving_status")
        private Integer receivingStatus;
        /**
         * 仓库编码
         */
        @SerializedName("warehouse_code")
        private String warehouseCode;
        /**
         * 仓库id
         */
        @SerializedName("warehouse_id")
        private Integer warehouseId;
        /**
         * 创建时间
         */
        private LocalDateTime addTime;
        /**
         * 更新时间
         */
        private LocalDateTime updateTime;
        /**
         * 入库单类型
         * 0标准
         * 3中转(易渡代发)
         * 4原标 5FBA
         */
        @SerializedName("receiving_type")
        private Integer receivingType;

        private String platformSign;
        /**
         * 入库明细
         */
        private List<ReceivingDetailDTO> receivingDetail;

        /**
         * 重试次数
         */
        private Integer retry = 1;

        public MessageDTO(OmsImlDTO.MessageDTO dto) {
            this.receivingCode = dto.getReceivingCode();
            this.referenceNo = dto.getReferenceNo();
            this.receivingStatus = 1;
            this.warehouseCode = dto.getWarehouseCode();
            this.warehouseId = 0;
            this.addTime = dto.getReceivingAddTime();
            this.updateTime = dto.getReceivingModifyTime();
            this.receivingType = 0;
            this.receivingDetail = ReceivingDetailDTO.createReceivingDetail(dto.getItems(), dto.getWarehouseCode());
            this.platformSign = OmsPlatformEnum.OMS_IML.getName();
        }

    }

    /**
     * 入库明细
     */
    @Data
    @ToString
    @NoArgsConstructor
    public static class ReceivingDetailDTO{
        /**
         * 商品编码 唯一
         */
        @SerializedName("product_barcode")
        private String productBarcode;
        /**
         * 客户商品编码
         */
        @SerializedName("product_sku")
        private String productSku;
        /**
         * 箱号编码
         */
        @SerializedName("box_no")
        private String boxNo;
        /**
         * 参考箱号
         */
        @SerializedName("reference_box_no")
        private String referenceBoxNo;
        /**
         * 送货数量
         */
        private Integer deliveryQty;
        /**
         * 收货数量
         */
        private Integer receiptQty;
        /**
         * 上架数量
         */
        @SerializedName("putawayQty")
        private Integer putawayQty;
        /**
         * 不良品数量
         */
        private Integer unsellableQty;
        /**
         * 良品数量
         */
        private Integer sellableQty;

        /**
         * 仓库编码
         */
        private String warehouseCode;

        /**
         * 更新时间
         */
        private LocalDateTime updateTime;

        public ReceivingDetailDTO(ReceivingDetailDTO dto, String warehouseCode) {
            this.productBarcode = dto.getProductBarcode();
            this.productSku = dto.getProductSku();
            this.boxNo = dto.getBoxNo().toString();
            this.referenceBoxNo = dto.getReferenceBoxNo();
            this.deliveryQty = dto.getDeliveryQty();
            this.receiptQty = dto.getReceiptQty();
            this.putawayQty = dto.getPutawayQty();
            this.unsellableQty = dto.getUnsellableQty();
            this.sellableQty = dto.getSellableQty();
            this.warehouseCode = warehouseCode;
        }

        public static List<ReceivingDetailDTO> createReceivingDetail(List<OmsImlDTO.ReceivingDetailDTO> receivingDetail, String warehouseCode) {
            return new ArrayList<>(receivingDetail.stream()
                    .collect(Collectors.toMap(OmsImlDTO.ReceivingDetailDTO::getProductSku,
                            x -> new ReceivingDetailDTO(x, warehouseCode),
                            (p1, p2) -> {
                                p1.setDeliveryQty(p1.getDeliveryQty() + p2.getDeliveryQty());
                                p1.setReceiptQty(p1.getReceiptQty() + p2.getReceiptQty());
                                p1.setPutawayQty(p1.getPutawayQty() + p2.getPutawayQty());
                                p1.setUnsellableQty(p1.getUnsellableQty() + p2.getUnsellableQty());
                                p1.setSellableQty(p1.getSellableQty() + p2.getSellableQty());
                                return p1;
                            })).values());
        }

        public static List<ReceivingDetailDTO> initReceivingDetail(List<ReceivingDetailDTO> receivingDetail, String warehouseCode) {
            return new ArrayList<>(receivingDetail.stream()
                    .collect(Collectors.toMap(ReceivingDetailDTO::getProductSku,
                            x -> new ReceivingDetailDTO(x, warehouseCode),
                            (p1, p2) -> {
                                p1.setDeliveryQty(p1.getDeliveryQty() + p2.getDeliveryQty());
                                p1.setReceiptQty(p1.getReceiptQty() + p2.getReceiptQty());
                                p1.setPutawayQty(p1.getPutawayQty() + p2.getPutawayQty());
                                p1.setUnsellableQty(p1.getUnsellableQty() + p2.getUnsellableQty());
                                p1.setSellableQty(p1.getSellableQty() + p2.getSellableQty());
                                return p1;
                            })).values());
        }
        public ReceivingDetailDTO(OmsImlDTO.ReceivingDetailDTO dto, String warehouseCode){
            this.productBarcode = dto.getProductBarcode();
            this.productSku = dto.getProductSku();
            this.boxNo = dto.getBoxNo().toString();
            this.referenceBoxNo = dto.getReferenceBoxNo();
            this.deliveryQty = dto.getQuantity();
            this.receiptQty = dto.getReceivedQuantity();
            this.putawayQty = dto.getPutawayQty();
            this.unsellableQty = null != dto.getLoTypeCount() ? (dto.getLoTypeCount().size() > 1 ? dto.getLoTypeCount().get(1) : 0) : 0;
            this.sellableQty = null != dto.getLoTypeCount() ? dto.getLoTypeCount().get(0) : 0;
            this.warehouseCode = warehouseCode;
        }
    }


    /**
     * 返回结果
     * {
     *     "Status": "SUCCESS",
     *     "ErrorMessage": ""
     * }
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ResultDTO{
        @JsonProperty("Status")
        private String status;

        @JsonProperty("ErrorMessage")
        private String errorMessage;

        public static ResultDTO success(){
            ResultDTO success = new ResultDTO("SUCCESS", "");
            return success;
        }
        public static ResultDTO fail(String msg){
            ResultDTO success = new ResultDTO("FAILED", msg);
            return success;
        }
    }
}
