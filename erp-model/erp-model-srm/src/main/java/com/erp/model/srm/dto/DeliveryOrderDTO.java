package com.erp.model.srm.dto;

import java.time.LocalDateTime;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * <p>
 * 送货单请求响应实体
 * </p>
 *
 * @author lrp
 * @since 2024-01-12
*/
@Data
@NoArgsConstructor
public class DeliveryOrderDTO implements Serializable {




    /**
    * 详情
    */
    @Data
    @NoArgsConstructor
    public static class ViewDTO {

        /**
        * 主键id
        */
        private String  id;

        /**
        * 送货单号
        */
        private String code;

        /**
        * 供应商id
        */
        private String supplierId;

        /**
        * 来源id
        */
        private String sourceId;

        /**
        * 预计到达日期
        */
        private LocalDateTime expectedDate;

        /**
        * 收货单号
        */
        private String receiveCode;

        /**
        * 来源订单号
        */
        private String sourceCode;

        /**
        * 客户名称
        */
        private String customerName;

        /**
        * 联系人id
        */
        private String contactId;

        /**
        * 联系人name
        */
        private String contactName;

        /**
        * 目的仓id
        */
        private String toWarehouseId;

        /**
        * 目的仓名称
        */
        private String toWarehouseName;

        /**
        * 打印日期
        */
        private LocalDateTime printDate;

        /**
        * 确认收货日期
        */
        private LocalDateTime confirmReceiveDate;

        /**
        * 收货员id
        */
        private String receiveUserId;

        /**
        * 收货员名
        */
        private String receiveUserName;

        /**
        * 收货电话
        */
        private String receivePhone;

        /**
        * 收货地址
        */
        private String receiveAddress;

        /**
        * 来源类型
        */
        private String sourceType;

        /**
        * 收货状态
        */
        private String receiptStatus;

        /**
        * 是否打印
        */
        private Boolean isPrint;


    }

    /**
    * 新增
    */
    @Data
    @NoArgsConstructor
    public static class AddDTO extends CommonDTO {


    }

    /**
    * 修改
    */
    @Data
    @NoArgsConstructor
    public static class UpdateDTO extends CommonDTO {

        /**
        * 主键id
        */
        @NotBlank(message = "主键id不能为空")
        private String id;

    }

    @Data
    @NoArgsConstructor
    public static class CommonDTO {

        /**
        * 供应商id
        */
        @NotBlank(message = "供应商id不能为空")
        @Size(max = 19,message = "供应商id最大长度不能超过19位")
        private String supplierId;

        /**
        * 来源id
        */
        @NotBlank(message = "来源id不能为空")
        @Size(max = 19,message = "来源id最大长度不能超过19位")
        private String sourceId;

        /**
        * 预计到达日期
        */
        private LocalDateTime expectedDate;

        /**
        * 收货单号
        */
        private String receiveCode;

        /**
        * 来源订单号
        */
        @NotBlank(message = "来源订单号不能为空")
        @Size(max = 50,message = "来源订单号最大长度不能超过50位")
        private String sourceCode;

        /**
        * 客户名称
        */
        private String customerName;

        /**
        * 联系人id
        */
        private String contactId;

        /**
        * 联系人name
        */
        private String contactName;

        /**
        * 目的仓id
        */
        @NotBlank(message = "目的仓id不能为空")
        @Size(max = 19,message = "目的仓id最大长度不能超过19位")
        private String toWarehouseId;

        /**
        * 目的仓名称
        */
        @NotBlank(message = "目的仓名称不能为空")
        @Size(max = 50,message = "目的仓名称最大长度不能超过50位")
        private String toWarehouseName;

        /**
        * 打印日期
        */
        private LocalDateTime printDate;

        /**
        * 确认收货日期
        */
        private LocalDateTime confirmReceiveDate;

        /**
        * 收货员id
        */
        private String receiveUserId;

        /**
        * 收货员名
        */
        private String receiveUserName;

        /**
        * 收货电话
        */
        private String receivePhone;

        /**
        * 收货地址
        */
        private String receiveAddress;

        /**
        * 来源类型
        */
        @NotBlank(message = "来源类型不能为空")
        @Size(max = 20,message = "来源类型最大长度不能超过20位")
        private String sourceType;

        /**
        * 收货状态
        */
        @NotBlank(message = "收货状态不能为空")
        @Size(max = 50,message = "收货状态最大长度不能超过50位")
        private String receiptStatus;

        /**
        * 是否打印
        */
        @NotNull(message = "是否打印不能为空")
        private Boolean isPrint;


    }


}