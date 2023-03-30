package com.erp.model.dmp.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

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
        @JsonProperty("AppToken")
        private String appToken;
        /**
         * 签名字符串
         */
        @JsonProperty("Sign")
        private String sign;
        /**
         * 数据类型
         */
        @JsonProperty("MessageType")
        private String messageType;
        /**
         * 签名字符串
         */
        @JsonProperty("Message")
        private MessageDTO message;
        /**
         * 消息ID
         */
        @JsonProperty("MessageID")
        private String messageId;
        /**
         * 发送时间
         */
        @JsonProperty("SendTime")
        private LocalDateTime sendTime;
    }

    @Data
    public static class MessageDTO{
        /**
         * 入库单号
         */
        @JsonProperty("receiving_code")
        private String receivingCode;
        /**
         * 参考号
         */
        @JsonProperty("reference_no")
        private String referenceNo;
        /**
         * 状态
         */
        @JsonProperty("receiving_status")
        private Integer receivingStatus;
        /**
         * 仓库编码
         */
        @JsonProperty("warehouse_code")
        private String warehouseCode;
        /**
         * 仓库id
         */
        @JsonProperty("warehouse_id")
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
         */
        @JsonProperty("receiving_type")
        private Integer receivingType;
        /**
         * 入库明细
         */
        private List<ReceivingDetailDTO> receivingDetail;

        /**
         * 重试次数
         */
        private Integer retry = 1;
    }

    /**
     * 入库明细
     */
    @Data
    public static class ReceivingDetailDTO{
        /**
         * 商品编码 唯一
         */
        @JsonProperty("product_barcode")
        private String productBarcode;
        /**
         * 客户商品编码
         */
        @JsonProperty("product_sku")
        private String productSku;
        /**
         * 箱号编码
         */
        @JsonProperty("box_no")
        private String boxNo;
        /**
         * 参考箱号
         */
        @JsonProperty("reference_box_no")
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
        @JsonProperty("putawayQty")
        private Integer putAwayQty;
        /**
         * 不良品数量
         */
        private Integer unsellableQty;
        /**
         * 良品数量
         */
        private Integer sellableQty;
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
    }
}
