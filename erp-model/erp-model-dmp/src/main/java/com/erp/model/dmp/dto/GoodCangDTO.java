package com.erp.model.dmp.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
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
public class GoodCangDTO {

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
        private String appToken;
        /**
         * 签名字符串
         */
        private String sign;
        /**
         * 数据类型
         */
        private String messageType;
        /**
         * 签名字符串
         */
        private MessageDTO message;
        /**
         * 消息ID
         */
        private String messageID;
        /**
         * 发送时间
         */
        private LocalDateTime sendTime;
    }

    @Data
    public static class MessageDTO{
        /**
         * 入库单号
         */
        private String receiving_code;
        /**
         * 参考号
         */
        private String reference_no;
        /**
         * 状态
         */
        private Integer receiving_status;
        /**
         * 仓库编码
         */
        private String warehouse_code;
        /**
         * 仓库id
         */
        private Integer warehouse_id;
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
        private Integer receiving_type;
        /**
         * 入库明细
         */
        private List<ReceivingDetailDTO> receivingDetail;
    }

    /**
     * 入库明细
     */
    @Data
    public static class ReceivingDetailDTO{
        /**
         * 商品编码 唯一
         */
        private String product_barcode;
        /**
         * 客户商品编码
         */
        private String product_sku;
        /**
         * 箱号编码
         */
        private String box_no;
        /**
         * 参考箱号
         */
        private String reference_box_no;
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
        private Integer putawayQty;
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
        @JsonProperty(value = "Status")
        private String status;

        @JsonProperty(value = "ErrorMessage")
        private String errorMessage;

        public static ResultDTO success(){
            ResultDTO success = new ResultDTO("SUCCESS", "");
            return success;
        }
    }
}
