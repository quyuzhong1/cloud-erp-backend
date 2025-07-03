package com.erp.wms.aliexpress.model.inventory;

import cn.hutool.core.annotation.Alias;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiInventoryResponseDTO {
    // Getters & Setters
    // 公共字段（成功/失败都存在）
    @Alias("request_id")
    private String requestId;

    // 成功响应字段
    private Result result;

    // 失败响应字段
    @Alias("error_response")
    private ErrorResponse errorResponse;

    // 判断是否成功的便捷方法
    public boolean isSuccess() {
        return result != null && result.success;
    }

    // 嵌套类：成功响应结构
    @Setter
    @Getter
    public static class Result {
        private Data data;
        private boolean success;

        // 嵌套类：数据字段
        @NoArgsConstructor
        @Setter
        @Getter
        public static class Data {

            @Alias("total_count")
            private Integer totalCount;
            @Alias("items")
            private List<ItemsDTO> items;

            @NoArgsConstructor
            @Setter
            @Getter
            public static class ItemsDTO {
                @Alias("item_code")
                private String itemCode;
                @Alias("lock_quantity")
                private Integer lockQuantity;
                @Alias("quantity")
                private Integer quantity;
                @Alias("produce_code")
                private String produceCode;
                @Alias("item_id")
                private Long itemId;
                @Alias("extend_props")
                private ExtendPropsDTO extendProps;
                @Alias("inventory_type")
                private Integer inventoryType;
                @Alias("batch_code")
                private String batchCode;
                @Alias("warehouse_wode")
                private String warehouseWode;

                @NoArgsConstructor
                @Setter
                @Getter
                public static class ExtendPropsDTO {
                    @Alias("ownerCode")
                    private String ownerCode;
                    @Alias("batchNum")
                    private String batchNum;
                    @Alias("batchId")
                    private String batchId;
                }
            }
        }

        // Getters & Setters
        public Data getData() { return data; }
        public void setData(Data data) { this.data = data; }
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
    }

    // 嵌套类：错误响应结构
    public static class ErrorResponse {
        private String code;
        private String type;
        @Alias("sub_code")
        private String subCode;
        @Alias("sub_msg")
        private String subMsg;
        private String msg;

        // Getters & Setters
        public String getCode() { return code; }
        public void setCode(String code) { this.code = code; }
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public String getSubCode() { return subCode; }
        public void setSubCode(String subCode) { this.subCode = subCode; }
        public String getSubMsg() { return subMsg; }
        public void setSubMsg(String subMsg) { this.subMsg = subMsg; }
        public String getMsg() { return msg; }
        public void setMsg(String msg) { this.msg = msg; }
    }

}
